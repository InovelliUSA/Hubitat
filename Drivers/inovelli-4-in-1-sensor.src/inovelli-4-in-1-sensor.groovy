/**
 *
 *  Inovelli 4-in-1 Sensor 
 *   
 *    github: InovelliUSA
 *    Date: 2021-07-02
 *    Copyright Inovelli / Eric Maycock
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
 *  2021-07-02: Fix for negative values with luminance reports. 
 *  
 *  2021-06-04: Adding preference for "wake interval" to be used with threshold reporting. 
 *  
 *  2021-05-25: Updating method that is used to determine whether to send non-secure, S0, or S2. 
 *  
 *  2020-09-01: Cleaning up fingerprint info. 
 *
 *  2020-08-05: Adding S2 support for C-7 Hub. 
 *
 *  2020-07-07: Fix bug for enum parameters not showing correctly. 
 *
 *  2020-06-24: Requesting battery report on every wake up.
 *
 *  2020-06-18: Removing unnecessary code. Additional logging options.
 *
 *  2020-01-28: Update VersionReport parsing because of Hubitat change. Removing unnecessary reports.
 *
 *  2020-01-16: Support for all device configuration parameters.
 *              Offset options for temperature, humidity, and illuminance.
 *              Fix illuminance scale.
 *              Options to enable & disable logging.
 *              Association support added for use with the Inovelli Z-Wave association tool.
 *
 */

import groovy.transform.Field

 metadata {
    definition (name: "Inovelli 4-in-1 Sensor", namespace: "InovelliUSA", author: "Eric Maycock", vid:"generic-motion-7", 
                importUrl: "https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-4-in-1-sensor.src/inovelli-4-in-1-sensor.groovy") {
        capability "Motion Sensor"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Illuminance Measurement"
        capability "Sensor"
        capability "Battery"
        
        command "resetBatteryRuntime"
        command "setAssociationGroup", [[name: "Group Number*",type:"NUMBER", description: "Provide the association group number to edit"], 
                                        [name: "Z-Wave Node*", type:"STRING", description: "Enter the node number (in hex) associated with the node"], 
                                        [name: "Action*", type:"ENUM", constraints: ["Add", "Remove"]],
                                        [name:"Multi-channel Endpoint", type:"NUMBER", description: "Currently not implemented"]] 
        
        attribute "lastActivity", "String"
        attribute "firmware", "String"
        attribute "groups", "Number"

        fingerprint mfr: "031E", prod: "000D", deviceId: "0001", inClusters: "0x5E,0x55,0x9F,0x98,0x6C" 
    }
     
    preferences {
        input description: "If battery powered, the configuration options (aside from temp, humidity, & lux offsets) will not be updated until the " +
            "sensor wakes up (once every 24-Hours). To manually wake up the sensor, press the button on the back 3 times quickly.", 
            title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input "wakeInterval", "number",
            title: "Wake Interval",
            description: "Interval, in seconds, used with threshold reporting. Range: 0..2678400\nDefault: 43200",
            range: "0..2678400"
        input "parameter10", "number",
            title: "Low Battery Alert Level",
            description: "At what battery level should the sensor send a low battery alert\nRange: 10..50\nDefault: 10",
            range: "10..50"
        input "parameter12", "number",
            title: "Motion Sensor Sensitivity",
            description: "Sensitivity level of the motion sensor. 0=Disabled 1=Low 10=High\nRange: 0..10\nDefault: 8",
            range: "0..10"
        input "parameter13", "number",
            title: "Motion Sensor Reset Time",
            description: "How long after motion stops should the sensor wait before sending a no-motion report\nRange: 5..15300\nDefault: 30",
            range: "5..15300"
        input "parameter14", "enum",
            title: "Send Basic Set on Motion",
            description: "Send a Basic Set report to devices in association group 2\nDefault: No",
            options: ["1":"Yes", "0":"No"]
        input "parameter15", "enum",
            title: "Reverse Basic Set ON / OFF",
            description: "Send OFF to devices in association group 2 when motion is triggered and ON when motion stops\nDefault: No",
            options: ["1":"Yes", "0":"No"]
        input "parameter101", "number",
            title: "Temperature Reporting Interval",
            description: "Interval, in seconds, in which temperature reports should be sent. 0=Disabled\nRange: 0..2678400\nDefault: 7200",
            range: "0..2678400"
        input "parameter102", "number",
            title: "Humidity Reporting Interval",
            description: "Interval, in seconds, in which humidity reports should be sent. 0=Disabled\nRange: 0..2678400\nDefault: 7200",
            range: "0..2678400"
        input "parameter103", "number",
            title: "Luminance Reporting Interval",
            description: "Interval, in seconds, in which luminance reports should be sent. 0=Disabled\nRange: 0..2678400\nDefault: 7200",
            range: "0..2678400"
        input "parameter104", "number",
            title: "Battery Level Reporting Interval",
            description: "Interval, in seconds, in which battery reports should be sent. 0=Disabled\nRange: 0..2678400\nDefault: 7200",
            range: "0..2678400"
        input "parameter110", "enum",
            title: "Send Reports According to Threshold",
            description: "Only send sensor reports if the below thresholds are met\nDefault: No",
            options: ["1":"Yes", "0":"No"]
        input "parameter111", "number",
            title: "Temperature Threshold",
            description: "Threshold for temperature reports to be sent\nRange: 1..500\nDefault: 10",
            range: "1..500"
        input "parameter112", "number",
            title: "Humidity Threshold",
            description: "Threshold for humidity reports to be sent\nRange: 1..32\nDefault: 5",
            range: "1..32"
        input "parameter113", "number",
            title: "Luminance Threshold",
            description: "Threshold for luminance reports to be sent\nRange: 1..65528\nDefault: 150",
            range: "1..65528"
        input "parameter114", "number",
            title: "Battery Threshold",
            description: "Threshold for battery reports to be sent\nRange: 1..100\nDefault: 10",
            range: "1..100"
        input name: "temperatureOffset", type: "decimal", 
            title: "Temperature Offset\nAdjust the reported temperature by this positive or negative value\nRange: -10.0..10.0\nDefault: 0.0", 
            range: "-10.0..10.0", defaultValue: 0
        input name: "humidityOffset", type: "number", 
            title: "Humidity Offset\nAdjust the reported humidity percentage by this positive or negative value\nRange: -10 ..10\nDefault: 0", 
            range: "-10..10", defaultValue: 0
        input name: "luminanceOffset", type: "number", 
            title: "Luminance Offset\nAdjust the reported luminance by this positive or negative value\nRange: -100..100\nDefault: 0", 
            range: "-100..100", defaultValue: 0
        input name: "debugEnable", type: "bool", 
            title: "Enable Debug Logging", defaultValue: true
        input name: "infoEnable", type: "bool", 
            title: "Enable Informational Logging", defaultValue: true
        input name: "disableDebugLogging", type: "number", 
            title: "Disable Debug Logging", description: "Disable debug logging after this number of minutes (0=Do not disable)", defaultValue: 0
        input name: "disableInfoLogging", type: "number", 
            title: "Disable Info Logging", description: "Disable info logging after this number of minutes (0=Do not disable)", defaultValue: 30
    }
}

@Field static Map configParams = [
    parameter010 : [
        number: 10,
        name:"Low Battery Alert Level",
        desciption: "At what battery level should the sensor send a low battery alert",
        range: "10..50",
        default: 10,
        size: 1,
        type: "number",
        value: null
        ],
    parameter012 : [
        number: 12,
        name:"Motion Sensor Sensitivity",
        desciption: "Sensitivity level of the motion sensor. 0=Disabled 1=Low 10=High",
        range: "0..10",
        default: 8,
        size: 1,
        type: "number",
        value: null
        ],
    parameter013 : [
        number: 13,
        name:"Motion Sensor Reset Time",
        desciption: "How long after motion stops should the sensor wait before sending a no-motion report",
        range: "5..15300",
        default: 30,
        size: 2,
        type: "number",
        value: null
        ],
    parameter014 : [
        number: 14,
        name:"Send Basic Set on Motion",
        desciption: "Send a Basic Set report to devices in association group 2",
        range: "[1:Yes, 0:No]",
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter015 : [
        number: 15,
        name:"Reverse Basic Set ON / OFF",
        desciption: "Send OFF to devices in association group 2 when motion is triggered and ON when motion stops",
        range: "[1:Yes, 0:No]",
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter101 : [
        number: 101,
        name:"Temperature Reporting Interval",
        desciption: "Interval, in seconds, in which temperature reports should be sent. 0=Disabled",
        range: "0..2678400",
        default: 7200,
        size: 4,
        type: "number",
        value: null
        ],
    parameter102 : [
        number: 102,
        name:"Humidity Reporting Interval",
        desciption: "Interval, in seconds, in which humidity reports should be sent. 0=Disabled",
        range: "0..2678400",
        default: 7200,
        size: 4,
        type: "number",
        value: null
        ],
    parameter103 : [
        number: 103,
        name:"Luminance Reporting Interval",
        desciption: "Interval, in seconds, in which luminance reports should be sent. 0=Disabled",
        range: "0..2678400",
        default: 7200,
        size: 4,
        type: "number",
        value: null
        ],
    parameter104 : [
        number: 104,
        name:"Battery Level Reporting Interval",
        desciption: "Interval, in seconds, in which battery reports should be sent. 0=Disabled",
        range: "0..2678400",
        default: 7200,
        size: 4,
        type: "number",
        value: null
        ],
    parameter110 : [
        number: 110,
        name:"Send Reports According to Threshold",
        desciption: "Only send sensor reports if the below thresholds are met",
        range: "[1:Yes, 0:No]",
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter111 : [
        number: 111,
        name:"Temperature Threshold",
        desciption: "Threshold for temperature reports to be sent",
        range: "1..500",
        default: 10,
        size: 2,
        type: "number",
        value: null
        ],
    parameter112 : [
        number: 112,
        name:"Humidity Threshold",
        desciption: "Threshold for humidity reports to be sent",
        range: "1..32",
        default: 5,
        size: 1,
        type: "number",
        value: null
        ],
    parameter113 : [
        number: 113,
        name:"Luminance Threshold",
        desciption: "Threshold for luminance reports to be sent",
        range: "1..65528",
        default: 150,
        size: 2,
        type: "number",
        value: null
        ],
    parameter114 : [
        number: 114,
        name:"Battery Threshold",
        desciption: "Threshold for battery reports to be sent",
        range: "1..100",
        default: 10,
        size: 1,
        type: "number",
        value: null
        ]
]

private getCommandClassVersions() {
    [
     0x20: 1, // Basic
     0x30: 2, // Sensor Binary
     0x71: 3, // Notification Report
     0x70: 2, // Configuration
     0x98: 1, // Security
     0x80: 1, // Battery
     0x31: 5, // Sensor Multilevel
     0x84: 1, // Wake Up
     0x72: 2, // Manufacturer Specific
     0x85: 2, // Association
     0x86: 1, // Version
    ]
}

def parse(description) {
    def result = []
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
    result
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand)
    }
}

def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is " +
        "set to '${cmd2Integer(cmd.configurationValue)}'"
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
            state.realLuminance = cmd.scaledSensorValue.toInteger()<0?(((cmd.scaledSensorValue.toInteger()+32768) % 65536) + 32768) : cmd.scaledSensorValue.toInteger()
            map.value = getAdjustedLuminance(cmd.scaledSensorValue.toInteger()<0?(((cmd.scaledSensorValue.toInteger()+32768) % 65536) + 32768) : cmd.scaledSensorValue.toInteger())
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
    def result = []
    if (cmd.notificationType == 7) {
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: notificationType 7 (Home Security)"
        switch (cmd.event) {
            case 0:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 0 (State Idle)"
                result << motionEvent(0)
                break
            case 1:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 1 (Intrusion - location provided)"
                result << motionEvent(1)
                break
            case 3:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 3 (Tampering - product cover removed)"
                result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName was moved")
                result << createEvent(name: "acceleration", value: "active", descriptionText: "$device.displayName was moved")
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
    
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Requesting battery report"
    cmds << zwave.batteryV1.batteryGet()
    
    cmds << zwave.wakeUpV1.wakeUpNoMoreInformation()
    
    response(commands(cmds))
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: Unknown Z-Wave Command: ${cmd.toString()}"
}

def debugLogsOff(){
    log.warn "${device.label?device.label:device.name}: Disabling debug logging after timeout"
    device.updateSetting("debugEnable",[value:"false",type:"bool"])
}

def infoLogsOff(){
    log.warn "${device.label?device.label:device.name}: Disabling info logging after timeout"
    device.updateSetting("infoEnable",[value:"false",type:"bool"])
}

def installed() {
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: installed()"
    initialize()
}

def updated() {
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: updated()"
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: If this sensor is battery powered, the configuration options " +
        "(aside from temp, hum, & lum offsets) will not be updated until the sensor wakes up. To manually wake up the sensor, press the button on the back 3 times quickly."
    if (debugEnable && disableDebugLogging) runIn(disableDebugLogging*60,debugLogsOff)
    if (infoEnable && disableInfoLogging) runIn(disableInfoLogging*60,infoLogsOff)
    if (state.realTemperature != null) sendEvent(name:"temperature", value: getAdjustedTemp(state.realTemperature))
    if (state.realHumidity != null) sendEvent(name:"humidity", value: getAdjustedHumidity(state.realHumidity))
    if (state.realLuminance != null) sendEvent(name:"illuminance", value: getAdjustedLuminance(state.realLuminance))
    if (settings.parameter12 == 0) sendEvent(name:"motion", value: "inactive")
    state.needfwUpdate = ""
}

def initialize() {
    if (infoEnable != false) log.info "${device.label?device.label:device.name}: initialize()"
    
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
    if(state.wakeInterval == null || state.wakeInterval != settings.wakeInterval){
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Setting Wake Interval to ${settings.wakeInterval? settings.wakeInterval : 43200}"
        cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds: settings.wakeInterval? settings.wakeInterval : 43200, nodeid:zwaveHubNodeId)
        cmds << zwave.wakeUpV1.wakeUpIntervalGet()
    }
    
    getParameterNumbers().each{ i ->
      if ((state."parameter${i}value" != ((settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : configParams["parameter${i.toString().padLeft(3,"0")}"].default.toInteger()))){
          //if (infoEnable != false) log.info "Parameter $i is not set correctly. Setting it to ${settings."parameter${i}"!=null? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger()}."
          cmds << setParameter(i, (settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : configParams["parameter${i.toString().padLeft(3,"0")}"].default.toInteger(), configParams["parameter${i.toString().padLeft(3,"0")}"].size)
          cmds << getParameter(i)
      }
      else {
          //if (infoEnable != false) log.info "${device.label?device.label:device.name}: Parameter $i already set"
      }
    }

    if (cmds != []) return cmds else return []
}

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
    return zwaveSecureEncap(cmd)
}

void zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd){
    hubitat.zwave.Command encapCmd = cmd.encapsulatedCommand(commandClassVersions)
    if (encapCmd) {
        zwaveEvent(encapCmd)
    }
    sendHubCommand(new hubitat.device.HubAction(command(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0)), hubitat.device.Protocol.ZWAVE))
}

private commands(commands, delay=250) {
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
 

  if (days>0) { 
      return "$days days and $hours:$mins:$secs"
  } else {
      return "$hours:$mins:$secs"
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
    }
    state.lastReset = now()
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
    return zwave.configurationV2.configurationGet(parameterNumber: number)
}
def getParameterNumbers(){
    return [10,12,13,14,15,101,102,103,104,110,111,112,113,114]
}

def setDefaultAssociations() {
    def hubitatHubID = (zwaveHubNodeId.toString().format( '%02x', zwaveHubNodeId )).toUpperCase()
    state.defaultG1 = [hubitatHubID]
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
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if(cmd.applicationVersion != null && cmd.applicationSubVersion != null) {
        def firmware = "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2,'0')}"
        if (infoEnable) log.info "${device.label?device.label:device.name}: Firmware report received: ${firmware}"
        state.needfwUpdate = "false"
        createEvent(name: "firmware", value: "${firmware}")
    } else if(cmd.firmware0Version != null && cmd.firmware0SubVersion != null) {
        def firmware = "${cmd.firmware0Version}.${cmd.firmware0SubVersion.toString().padLeft(2,'0')}"
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Firmware report received: ${firmware}"
        state.needfwUpdate = "false"
        createEvent(name: "firmware", value: "${firmware}")
    }
}
