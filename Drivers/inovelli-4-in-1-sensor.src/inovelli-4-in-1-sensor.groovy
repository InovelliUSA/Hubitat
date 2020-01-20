/**
 *
 *  Inovelli 4-in-1 Sensor 
 *   
 *	github: InovelliUSA
 *	Date: 2020-01-17
 *	Copyright Inovelli / Eric Maycock
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  2020-01-20: scott_inovelli changes:
 *              added PowerSource
 *              added powersource preference
 *              removed ST code
 *              Tamper and Acceleration test code commented out.. not used on this device yet
 *  2020-01-16: Support for all device configuration parameters.
 *              Offset options for temperature, humidity, and illuminance.
 *              Fix illuminance scale.
 *              Options to enable & disable logging.
 *              Association support added for use with the Inovelli Z-Wave association tool.
 *
 */

 metadata {
	definition (name: "Inovelli 4-in-1 Sensor", namespace: "InovelliUSA", author: "Eric Maycock", vid:"generic-motion-7", importUrl: "https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-4-in-1-sensor.src/inovelli-4-in-1-sensor.groovy") {
        capability "Motion Sensor"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Illuminance Measurement"
        capability "Configuration"
        capability "Sensor"
        capability "Battery"
        capability "Refresh"
        //capability "Health Check"
        capability "PowerSource"
        
        command "resetBatteryRuntime"
        command "setAssociationGroup", ["number", "enum", "number", "number"] // group number, nodes, action (0 - remove, 1 - add), multi-channel endpoint (optional)

        attribute "lastActivity", "String"
        attribute "lastEvent", "String"
        attribute "firmware", "String"

        fingerprint mfr: "0072", prod: "0503", model: "0002", deviceJoinName: "Inovelli 4-in-1 Sensor"
        fingerprint mfr: "0072", prod: "0503", model: "1E00", deviceJoinName: "Inovelli 4-in-1 Sensor"
        fingerprint mfr: "031E", prod: "000D", model: "0001", deviceJoinName: "Inovelli 4-in-1 Sensor"
        fingerprint deviceId: "0x0701", inClusters: "0x5E,0x55,0x9F,0x98,0x6C,0x85,0x59,0x72,0x80,0x84,0x73,0x70,0x7A,0x5A,0x71,0x31,0x86"
        fingerprint deviceId: "0x0701", inClusters: "0x5E,0x85,0x59,0x72,0x80,0x84,0x73,0x70,0x7A,0x5A,0x71,0x31,0x86,0x55,0x9F,0x98,0x6C"
        
        
	}
    preferences {
        input description: "If battery powered, the configuration options (aside from temp, humidity, & lux offsets) will not be updated until the sensor wakes up (once every 24-Hours). To manually wake up the sensor, press the button on the back 3 times quickly.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        generate_preferences() 
    }
}

def parse(description) {
    def result = []
    //log.debug "description: ${description}"
    if (description.startsWith("Err 106")) {
        state.sec = 0
        result = createEvent(descriptionText: description, isStateChange: true)
    } else if (description != "updated") {
        def cmd = zwave.parse(description, commandClassVersions)
        if (cmd) {
            result += zwaveEvent(cmd)
            if (debugEnable != false) "'$cmd' parsed to $result"
        } else {
            if (debugEnable != false) "Couldn't zwave.parse '$description'"
        }
    }
    def now
    if(location.timeZone)
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    else
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a")
    sendEvent(name: "lastActivity", value: now, displayed:false)
    updateStatus()
    result
}

private getCommandClassVersions() {
	[0x31: 5, 0x30: 2, 0x84: 1, 0x20: 1, 0x25: 1, 0x70: 2, 0x98: 1, 0x32: 3]
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        state.sec = 1
        zwaveEvent(encapsulatedCommand)
    }
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	response(configure())
}

def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
    state."parameter${cmd.parameterNumber}value" = cmd2Integer(cmd.configurationValue)
}

def zwaveEvent(hubitat.zwave.commands.wakeupv1.WakeUpIntervalReport cmd)
{
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
	if (infoEnable != false) log.info "${device.label?device.label:device.name}: WakeUpIntervalReport ${cmd.seconds} seconds"
    state.wakeInterval = cmd.seconds
}

def zwaveEvent(hubitat.zwave.commands.batteryv1.BatteryReport cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Battery report received: ${cmd.batteryLevel}"
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastBatteryReport = now()
	createEvent(map)
}

def logsOff(){
    log.info "${device.label?device.label:device.name}: Disabling logging after timeout"
    device.updateSetting("debugEnable",[value:"false",type:"bool"])
    //device.updateSetting("infoEnable",[value:"false",type:"bool"])
}

def zwaveEvent(hubitat.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
            state.realTemperature = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.value = getAdjustedTemp(state.realTemperature)
			map.unit = getTemperatureScale()
            if (infoEnable != false) log.info "${device.label?device.label:device.name}: Temperature report received: ${map.value}"
			break;
		case 3:
			map.name = "illuminance"
            state.realLuminance = cmd.scaledSensorValue.toInteger()
			map.value = getAdjustedLuminance(cmd.scaledSensorValue.toInteger())
			map.unit = "lux"
            if (infoEnable != false) log.info "${device.label?device.label:device.name}: Illuminance report received: ${map.value}"
			break;
        case 5:
			map.name = "humidity"
            state.realHumidity = cmd.scaledSensorValue.toInteger()
			map.value = getAdjustedHumidity(cmd.scaledSensorValue.toInteger())
			map.unit = "%"
            if (infoEnable != false) log.info "${device.label?device.label:device.name}: Humidity report received: ${map.value}"
			break;
		default:
			map.descriptionText = cmd.toString()
            if (infoEnable != false) log.info "${device.label?device.label:device.name}: Unhandled sensor multilevel report received: ${map.descriptionText}"
	}
	createEvent(map)
}

def motionEvent(value) {
	def map = [name: "motion"]
	if (value != 0) {
		map.value = "active"
		map.descriptionText = "$device.displayName detected motion"
	} else {
		map.value = "inactive"
		map.descriptionText = "$device.displayName motion has stopped"
	}
	createEvent(map)
}

def zwaveEvent(hubitat.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Sensor Binary report received: ${cmd.value}"
	motionEvent(cmd.sensorValue)
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Basic Set received: ${cmd.value}"
	motionEvent(cmd.value)
}

def zwaveEvent(hubitat.zwave.commands.notificationv3.NotificationReport cmd) {
	if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    //if (infoEnable != false) log.info "${device.label?device.label:device.name}: Notification report received: event - ${cmd.event},  notificationType - ${cmd.notificationType}"
    def result = []
	if (cmd.notificationType == 7) {
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: notificationType 7 (Home Security)"
		switch (cmd.event) {
			case 0:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 0 (State Idle)"
                result << motionEvent(0)
                //Test code commented out
				//result << createEvent(name: "tamper", value: "clear", descriptionText: "$device.displayName tamper cleared")
                //result << createEvent(name: "acceleration", value: "inactive", descriptionText: "$device.displayName tamper cleared")
				break
            case 1:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 1 (Intrusion - location provided)"
				result << motionEvent(1)
				break
			case 3:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 3 (Tampering - product cover removed)"
                //Test code commented out
				//result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName was moved")
                //result << createEvent(name: "acceleration", value: "active", descriptionText: "$device.displayName was moved")
				break
			case 7:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 7 (Motion detection - location provided)"
				result << motionEvent(1)
				break
            case 8:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 8 (Motion detection)"
				result << motionEvent(1)
				break
		}
	} else {
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Unhandled Notification report received: ${cmd}"
		result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
	}
	result
}

def zwaveEvent(hubitat.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: WakeUp Notification received"

    def cmds = initialize()
    
    if (!state.lastBatteryReport || (now() - state.lastBatteryReport) / 60000 >= 60 * 24)
    {
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Over 24hr since last battery report. Requesting report"
        cmds << zwave.batteryV1.batteryGet()
    }
    
    cmds << zwave.wakeUpV1.wakeUpNoMoreInformation()
    
    response(commands(cmds))
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Unknown Z-Wave Command: ${cmd.toString()}"
}

def refresh() {
   	if (infoEnable != false) log.info "${device.label?device.label:device.name}: refresh()"

    def request = []
    if (state.lastRefresh != null && now() - state.lastRefresh < 5000) {
        log.debug "Refresh Double Press"
        def configuration = new XmlSlurper().parseText(configuration_model())
        configuration.Value.each
        {
            if ( "${it.@setting_type}" == "zwave" ) {
                request << zwave.configurationV1.configurationGet(parameterNumber: "${it.@index}".toInteger())
            }
        } 
        request << zwave.wakeUpV1.wakeUpIntervalGet()
    }
    state.lastRefresh = now()
    request << zwave.batteryV1.batteryGet()
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:1)
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5, scale:1)
    commands(request)
}

def ping() {
   	if (infoEnable != false) log.info "${device.label?device.label:device.name}: ping()"
    return command(zwave.batteryV1.batteryGet())
}

def installed() {
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: installed()"
    refresh()
}

def configure() {
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: configure()"
    def cmds = initialize()
    commands(cmds)
}

def updated() {
    if (!state.lastRan || now() >= state.lastRan + 2000) {
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: updated()"
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: If this sensor is battery powered, the configuration options (aside from temp, hum, & lum offsets) will not be updated until the sensor wakes up. To manually wake up the sensor, press the button on the back 3 times quickly."
        if (debugEnable || infoEnable) runIn(1800,logsOff)
        state.needfwUpdate = ""
        state.lastRan = now()
        if (state.realTemperature != null) sendEvent(name:"temperature", value: getAdjustedTemp(state.realTemperature))
        if (state.realHumidity != null) sendEvent(name:"humidity", value: getAdjustedHumidity(state.realHumidity))
        if (state.realLuminance != null) sendEvent(name:"illuminance", value: getAdjustedLuminance(state.realLuminance))

        state.powerSource = settings?.powersource
        sendEvent(name: "powerSource", value: state.powerSource)
        
        def cmds = initialize()
        
        updateStatus()
        
        if (cmds != [])
            commands(cmds, 1000)
        else 
            return null
    } else {
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: updated() ran within the last 2 seconds. Skipping execution."
    }
}

def initialize() {
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: initialize()"
    sendEvent(name: "checkInterval", value: 3 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    
    def cmds = processAssociations()
    
    if(!state.needfwUpdate || state.needfwUpdate == "") {
       if (infoEnable != false) log.info "${device.label?device.label:device.name}: Requesting device firmware version"
       cmds << zwave.versionV1.versionGet()
    }
    
    if (device.currentValue("temperature") == null) {
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Temperature report not yet received. Sending request"
        cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
    }
    if (device.currentValue("humidity") == null) {
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Humidity report not yet received. Sending request"
        cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5, scale:1)
    }
    if (device.currentValue("illuminance") == null) {
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Illuminance report not yet received. Sending request"
        cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:1)
    }
    
    if(state.wakeInterval == null || state.wakeInterval != 43200){
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Setting Wake Interval to 43200"
        cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds: 43200, nodeid:zwaveHubNodeId)
        cmds << zwave.wakeUpV1.wakeUpIntervalGet()
    }
    
    getParameterNumbers().each{ i ->
      if ((state."parameter${i}value" != ((settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger()))){
          //if (infoEnable != false) log.info "Parameter $i is not set correctly. Setting it to ${settings."parameter${i}"!=null? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger()}."
          cmds << setParameter(i, (settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger(), getParameterInfo(i, "size").toInteger())
          cmds << getParameter(i)
      }
      else {
          //if (infoEnable != false) log.info "${device.label?device.label:device.name}: Parameter $i already set"
      }
    }

    if (cmds != []) return cmds else return []
}

/**
* Convert 1 and 2 bytes values to integer
*/
def cmd2Integer(array) { 
switch(array.size()) {
	case 1:
		array[0]
    break
	case 2:
    	((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
    break
	case 4:
    	((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
	break
}
}

def integer2Cmd(value, size) {
	switch(size) {
	case 1:
		[value]
    break
	case 2:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        [value2, value1]
    break
	case 4:
    	def short value1 = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        def short value4 = (value >> 24) & 0xFF
		[value4, value3, value2, value1]
	break
	}
}

private command(hubitat.zwave.Command cmd) {
	if (state.sec && cmd.toString()) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=1000) {
	delayBetween(commands.collect{ command(it) }, delay)
}

private getBatteryRuntime() {
   def currentmillis = now() - state.batteryRuntimeStart
   def days=0
   def hours=0
   def mins=0
   def secs=0
   secs = (currentmillis/1000).toInteger() 
   mins=(secs/60).toInteger() 
   hours=(mins/60).toInteger() 
   days=(hours/24).toInteger() 
   secs=(secs-(mins*60)).toString().padLeft(2, '0') 
   mins=(mins-(hours*60)).toString().padLeft(2, '0') 
   hours=(hours-(days*24)).toString().padLeft(2, '0') 
 
    if (state.powerSource != "mains") {
    if (days>0) {
      return "$days days and $hours:$mins:$secs"
  } else {
      return "$hours:$mins:$secs"
    }
    }
}

private getAdjustedTemp(value) {

    value = Math.round((value as Double) * 100) / 100

	if (settings.temperatureOffset) {
	   return value =  value + Math.round(settings.temperatureOffset * 100) /100
	} else {
       return value
    }
    
}

private getAdjustedHumidity(value) {
    
    value = Math.round((value as Double) * 100) / 100

	if (settings.humidityOffset) {
	   return value =  value + Math.round(settings.humidityOffset * 100) /100
	} else {
       return value
    }
    
}

private getAdjustedLuminance(value) {
    
    value = Math.round((value as Double) * 100) / 100

	if (settings.luminanceOffset) {
	   return value =  value + Math.round(settings.luminanceOffset * 100) /100
	} else {
       return value
    }
    
}

def resetBatteryRuntime() {
    if (state.lastReset != null && now() - state.lastReset < 5000) {
        if (infoEnable != false) log.info "Reset Double Press"
        state.batteryRuntimeStart = now()
        updateStatus()
    }
    state.lastReset = now()
}

private updateStatus(){
   def result = []
   if (state.powerSource != "mains") {
   if(state.batteryRuntimeStart != null){
        sendEvent(name:"batteryRuntime", value:getBatteryRuntime(), displayed:false)
        if (device.currentValue('currentFirmware') != null){
            sendEvent(name:"statusText2", value: "Firmware: v${device.currentValue('currentFirmware')} - Battery: ${getBatteryRuntime()} Double tap to reset", displayed:false)
        } else {
            sendEvent(name:"statusText2", value: "Battery: ${getBatteryRuntime()} Double tap to reset", displayed:false)
        }
    } else {
        state.batteryRuntimeStart = now()
    }
   }

    String statusText = ""
    if(device.currentValue('humidity') != null)
        statusText = "RH ${device.currentValue('humidity')}% - "
    if(device.currentValue('illuminance') != null)
        statusText = statusText + "LUX ${device.currentValue('illuminance')} - "
        
    if (statusText != ""){
        statusText = statusText.substring(0, statusText.length() - 2)
        sendEvent(name:"statusText", value: statusText, displayed:false)
    }
}

def calculateParameter(number) {
    def value = 0
    switch (number){
      case "999":
      break
      default:
          value = settings."parameter${number}"
      break
    }
    return value
}

def setParameter(number, value, size) {
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Setting parameter $number with a size of $size bytes to $value"
    return zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(value.toInteger(),size), parameterNumber: number, size: size)
}

def getParameter(number) {
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Retreiving value of parameter $number"
    return zwave.configurationV1.configurationGet(parameterNumber: number)
}
def getParameterNumbers(){
    return [10,12,13,14,15,101,102,103,104,110,111,112,113,114]
}

def getParameterInfo(number, value){
    def parameter = [:]
    
    parameter.parameter10description="At what battery level should the sensor send a low battery alert"
    parameter.parameter12description="Sensitivity level of the motion sensor. 0=Disabled 1=Low 10=High"
    parameter.parameter13description="How long after motion stops should the sensor wait before sending a no-motion report"
    parameter.parameter14description="Send a Basic Set report to devices in association group 2"
    parameter.parameter15description="Send OFF to devices in association group 2 when motion is triggered and ON when motion stops"
    parameter.parameter100description="Resets parameters 101-104 to their default settings"
    parameter.parameter101description="Interval, in seconds, in which temperature reports should be sent. 0=Disabled"
    parameter.parameter102description="Interval, in seconds, in which humidity reports should be sent. 0=Disabled"
    parameter.parameter103description="Interval, in seconds, in which luminance reports should be sent. 0=Disabled"
    parameter.parameter104description="Interval, in seconds, in which battery reports should be sent. 0=Disabled"
    parameter.parameter110description="Only send sensor reports if the below thresholds are met"
    parameter.parameter111description="Threshold for temperature reports to be sent"
    parameter.parameter112description="Threshold for humidity reports to be sent"
    parameter.parameter113description="Threshold for luminance reports to be sent"
    parameter.parameter114description="Threshold for battery reports to be sent"
    
    parameter.parameter10name="Low Battery Alert Level"
    parameter.parameter12name="Motion Sensor Sensitivity"
    parameter.parameter13name="Motion Sensor Reset Time"
    parameter.parameter14name="Send Basic Set on Motion"
    parameter.parameter15name="Reverse Basic Set ON / OFF"
    parameter.parameter100name="Set 101-104 to Default"
    parameter.parameter101name="Temperature Reporting Interval"
    parameter.parameter102name="Humidity Reporting Interval"
    parameter.parameter103name="Luminance Reporting Interval"
    parameter.parameter104name="Battery Level Reporting Interval"
    parameter.parameter110name="Send Reports According to Threshold"
    parameter.parameter111name="Temperature Threshold"
    parameter.parameter112name="Humidity Threshold"
    parameter.parameter113name="Luminance Threshold"
    parameter.parameter114name="Battery Threshold"
    
    parameter.parameter10options="10..50"
    parameter.parameter12options="0..10"
    parameter.parameter13options="5..15300"
    parameter.parameter14options=["1":"Yes", "0":"No"]
    parameter.parameter15options=["1":"Yes", "0":"No"]
    parameter.parameter100options=["1":"Yes", "0":"No"]
    parameter.parameter101options="0..2678400"
    parameter.parameter102options="0..2678400"
    parameter.parameter103options="0..2678400"
    parameter.parameter104options="0..2678400"
    parameter.parameter110options=["1":"Yes", "0":"No"]
    parameter.parameter111options="1..500"
    parameter.parameter112options="1..32"
    parameter.parameter113options="1..65528"
    parameter.parameter114options="1..100"
    
    parameter.parameter10size=1
    parameter.parameter12size=1
    parameter.parameter13size=2
    parameter.parameter14size=1
    parameter.parameter15size=1
    parameter.parameter100size=1
    parameter.parameter101size=4
    parameter.parameter102size=4
    parameter.parameter103size=4
    parameter.parameter104size=4
    parameter.parameter110size=1
    parameter.parameter111size=2
    parameter.parameter112size=1
    parameter.parameter113size=2
    parameter.parameter114size=1
    
    parameter.parameter10type="number"
    parameter.parameter12type="number"
    parameter.parameter13type="number"
    parameter.parameter14type="enum"
    parameter.parameter15type="enum"
    parameter.parameter100type="enum"
    parameter.parameter101type="number"
    parameter.parameter102type="number"
    parameter.parameter103type="number"
    parameter.parameter104type="number"
    parameter.parameter110type="enum"
    parameter.parameter111type="number"
    parameter.parameter112type="number"
    parameter.parameter113type="number"
    parameter.parameter114type="number"
    
    parameter.parameter10default=10
    parameter.parameter12default=8
    parameter.parameter13default=30
    parameter.parameter14default=0
    parameter.parameter15default=0
    parameter.parameter100default=0
    parameter.parameter101default=7200
    parameter.parameter102default=7200
    parameter.parameter103default=7200
    parameter.parameter104default=7200
    parameter.parameter110default=0
    parameter.parameter111default=10
    parameter.parameter112default=5
    parameter.parameter113default=150
    parameter.parameter114default=10
    
    return parameter."parameter${number}${value}"
}

def generate_preferences()
{
    input("powersource", "enum", title:"Mains/Battery", description:"Power Source", defaultValue: true, options: [mains:"Mains",battery:"Battery"], required: true)
    getParameterNumbers().each { i ->
        
        switch(getParameterInfo(i, "type"))
        {   
            case "number":
                input "parameter${i}", "number",
                    title:getParameterInfo(i, "name") + "\n" + getParameterInfo(i, "description") + "\nRange: " + getParameterInfo(i, "options") + "\nDefault: " + getParameterInfo(i, "default"),
                    range: getParameterInfo(i, "options")
                    //defaultValue: getParameterInfo(i, "default")
                    //displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "enum":
                input "parameter${i}", "enum",
                    title:getParameterInfo(i, "name") + "\n" + getParameterInfo(i, "description"),
                    //defaultValue: getParameterInfo(i, "default"),
                    //displayDuringSetup: "${it.@displayDuringSetup}",
                    options: getParameterInfo(i, "options")
            break
        }
    }
    input name: "temperatureOffset", type: "decimal", title: "Temperature Offset\nAdjust the reported temperature by this positive or negative value\nRange: -10.0..10.0\nDefault: 0.0", range: "-10.0..10.0", defaultValue: 0
    input name: "humidityOffset", type: "number", title: "Humidity Offset\nAdjust the reported humidity percentage by this positive or negative value\nRange: -10 ..10\nDefault: 0", range: "-10..10", defaultValue: 0
    input name: "luminanceOffset", type: "number", title: "Luminance Offset\nAdjust the reported luminance by this positive or negative value\nRange: -100..100\nDefault: 0", range: "-100..100", defaultValue: 0
    input name: "debugEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    input name: "infoEnable", type: "bool", title: "Enable informational logging", defaultValue: true
}

def setDefaultAssociations() {
    def smartThingsHubID = (zwaveHubNodeId.toString().format( '%02x', zwaveHubNodeId )).toUpperCase()
    state.defaultG1 = [smartThingsHubID]
    state.defaultG2 = []
    state.defaultG3 = []
}

def setAssociationGroup(group, nodes, action, endpoint = null){
    if (!state."desiredAssociation${group}") {
        state."desiredAssociation${group}" = nodes
    } else {
        switch (action) {
            case 0:
                state."desiredAssociation${group}" = state."desiredAssociation${group}" - nodes
            break
            case 1:
                state."desiredAssociation${group}" = state."desiredAssociation${group}" + nodes
            break
        }
    }
}

def processAssociations(){
   def cmds = []
   setDefaultAssociations()
   def associationGroups = 5
   if (state.associationGroups) {
       associationGroups = state.associationGroups
   } else {
       if (infoEnable != false) log.info "${device.label?device.label:device.name}: Getting supported association groups from device"
       cmds <<  zwave.associationV2.associationGroupingsGet()
   }
   for (int i = 1; i <= associationGroups; i++){
      if(state."actualAssociation${i}" != null){
         if(state."desiredAssociation${i}" != null || state."defaultG${i}") {
            def refreshGroup = false
            ((state."desiredAssociation${i}"? state."desiredAssociation${i}" : [] + state."defaultG${i}") - state."actualAssociation${i}").each {
                if (it != null){
                    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Adding node $it to group $i"
                    cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                    refreshGroup = true
                }
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                if (it != null){
                    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Removing node $it from group $i"
                    cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                    refreshGroup = true
                }
            }
            if (refreshGroup == true) cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
            else if (infoEnable != false) log.info "${device.label?device.label:device.name}: There are no association actions to complete for group $i"
         }
      } else {
         if (infoEnable != false) log.info "${device.label?device.label:device.name}: Association info not known for group $i. Requesting info from device."
         cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
      }
   }
   return cmds
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    def temp = []
    if (cmd.nodeId != []) {
       cmd.nodeId.each {
          temp += it.toString().format( '%02x', it.toInteger() ).toUpperCase()
       }
    } 
    state."actualAssociation${cmd.groupingIdentifier}" = temp
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Associations for Group ${cmd.groupingIdentifier}: ${temp}"
    updateDataValue("associationGroup${cmd.groupingIdentifier}", "$temp")
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    sendEvent(name: "groups", value: cmd.supportedGroupings)
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Supported association groups: ${cmd.supportedGroupings}"
    state.associationGroups = cmd.supportedGroupings
}

def zwaveEvent(hubitat.zwave.commands.versionv1.VersionReport cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if(cmd.firmware0Version != null && cmd.firmware0SubVersion != null) {
	    def firmware = "${cmd.firmware0Version}.${cmd.firmware0SubVersion.toString().padLeft(2,'0')}"
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Firmware report received: ${firmware}"
        state.needfwUpdate = "false"
        createEvent(name: "firmware", value: "${firmware}")
    }
}

def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionReport cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Protection report received: Local protection is ${cmd.localProtectionState > 0 ? "on" : "off"} & Remote protection is ${cmd.rfProtectionState > 0 ? "on" : "off"}"
    if (!state.lastRan || now() <= state.lastRan + 60000) {
        state.localProtectionState = cmd.localProtectionState
        state.rfProtectionState = cmd.rfProtectionState
    } else {
        if (infoEnable != false) log.debug "${device.label?device.label:device.name}: Protection report received more than 60 seconds after running updated(). Possible configuration made at switch"
    }
    //device.updateSetting("disableLocal",[value:cmd.localProtectionState?cmd.localProtectionState:0,type:"enum"])
    //device.updateSetting("disableRemote",[value:cmd.rfProtectionState?cmd.rfProtectionState:0,type:"enum"])
    def children = childDevices
    def childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
    if (childDevice) {
        childDevice.sendEvent(name: "switch", value: cmd.localProtectionState > 0 ? "on" : "off")        
    }
    childDevice = children.find{it.deviceNetworkId.endsWith("ep102")}
    if (childDevice) {
        childDevice.sendEvent(name: "switch", value: cmd.rfProtectionState > 0 ? "on" : "off")        
    }
}
