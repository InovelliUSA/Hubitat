/**
 *  Inovelli Switch LZW30
 *  Author: Eric Maycock (erocm123)
 *  Date: 2019-08-28
 *
 *  Copyright 2019 Eric Maycock / Inovelli
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
 */
 
metadata {
    definition (name: "Inovelli Switch LZW30", namespace: "inovelliUSA", author: "Eric Maycock", vid: "generic-switch", importUrl: "") {
        capability "Switch"
        capability "Refresh"
        capability "Actuator"
        capability "Sensor"
        capability "Configuration"
        
        attribute "lastActivity", "String"
        attribute "lastEvent", "String"
        attribute "firmware", "String"
        
        command "pressUpX1"
        command "pressDownX1"
        command "pressUpX2"
        command "pressDownX2"
        command "pressUpX3"
        command "pressDownX3"
        command "pressUpX4"
        command "pressDownX4"
        command "pressUpX5"
        command "pressDownX5"
        command "holdUp"
        command "holdDown"
        command "pressConfig"
        
        command "setAssociationGroup", ["number", "enum", "number", "number"] // group number, nodes, action (0 - remove, 1 - add), multi-channel endpoint (optional)

        fingerprint mfr: "031E", prod: "0004", model: "0001", deviceJoinName: "Inovelli Switch"
        fingerprint deviceId: "0x1001", inClusters: "0x5E,0x55,0x98,0x9F,0x6C,0x22,0x70,0x85,0x59,0x86,0x25,0x72,0x5A,0x5B,0x73,0x75,0x7A" // Standard Switch
         
        
    }

    simulator {
    }
    
    preferences {
        generate_preferences()
    }
}

def generate_preferences()
{
    getParameterNumbers().each { i ->
        
        switch(getParameterInfo(i, "type"))
        {   
            case "number":
                input "parameter${i}", "number",
                    title:getParameterInfo(i, "name") + "\n" + getParameterInfo(i, "description") + "\nRange: " + getParameterInfo(i, "options") + "\nDefault: " + getParameterInfo(i, "default"),
                    range: getParameterInfo(i, "options")
                    //defaultValue: getParameterInfo(i, "default")
            break
            case "enum":
                input "parameter${i}", "enum",
                    title:getParameterInfo(i, "name") + "\n" + getParameterInfo(i, "description"), 
                    //defaultValue: getParameterInfo(i, "default"),
                    options: getParameterInfo(i, "options")
            break
        }  
    }
    
    input description: "When each notification set (Color, Level, Duration, Type) is configured, a switch child device is created that can be used in SmartApps to activate that notification.", title: "LED Notifications", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    
    [1,2,3,4,5].each { i ->
                input "parameter8-${i}a", "enum", title: "LED Effect Color - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    0:"Red",
                    21:"Orange",
                    42:"Yellow",
                    85:"Green",
                    127:"Cyan",
                    170:"Blue",
                    212:"Violet",
                    234:"Pink"]
                input "parameter8-${i}b", "enum", title: "LED Effect Level - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    0:"0%",
                    1:"10%",
                    2:"20%",
                    3:"30%",
                    4:"40%",
                    5:"50%",
                    6:"60%",
                    7:"70%",
                    8:"80%",
                    9:"90%",
                    10:"100%"]
                input "parameter8-${i}c", "enum", title: "LED Effect Duration - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    1:"1 Second",
                    2:"2 Seconds",
                    3:"3 Seconds",
                    4:"4 Seconds",
                    5:"5 Seconds",
                    6:"6 Seconds",
                    7:"7 Seconds",
                    8:"8 Seconds",
                    9:"9 Seconds",
                    10:"10 Seconds",
                    20:"20 Seconds",
                    30:"30 Seconds",
                    40:"40 Seconds",
                    50:"50 Seconds",
                    60:"60 Seconds",
                    62:"2 Minutes",
                    63:"3 Minutes",
                    64:"4 Minutes",
                    65:"5 Minutes",
                    255:"Indefinetly"]
                input "parameter8-${i}d", "enum", title: "LED Effect Type - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    0:"Off",
                    1:"Solid",
                    //2:"Chase",
                    2:"Fast Blink",
                    3:"Slow Blink",
                    4:"Pulse"]
    
    }
    input "disableLocal", "enum", title: "Disable Local Control\n\nDisable ability to control switch from the wall", description: "Tap to set", required: false, options:[["1": "Yes"], ["0": "No"]], defaultValue: "0"
}

private channelNumber(String dni) {
    dni.split("-ep")[-1] as Integer
}

private sendAlert(data) {
    sendEvent(
        descriptionText: data.message,
        eventType: "ALERT",
        name: "failedOperation",
        value: "failed",
        displayed: true,
    )
}

void childSetLevel(String dni, value) {
    def valueaux = value as Integer
    def level = Math.max(Math.min(valueaux, 99), 0)    
    def cmds = []
    switch (channelNumber(dni)) {
        case 101:
            cmds << new hubitat.device.HubAction(command(zwave.protectionV2.protectionSet(localProtectionState : level > 0 ? 2 : 0, rfProtectionState: 0) ))
            cmds << new hubitat.device.HubAction(command(zwave.protectionV2.protectionGet() ))
        break
    }
	sendHubCommand(cmds, 1000)
}

private toggleTiles(number, value) {
   for (int i = 1; i <= 5; i++){
       if ("${i}" != number){
           def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep$i"}
           if (childDevice) {         
                childDevice.sendEvent(name: "switch", value: "off")
           }
       } else {
           def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep$i"}
           if (childDevice) {         
                childDevice.sendEvent(name: "switch", value: value)
           }
       }
   }
}

void childOn(String dni) {
    log.debug "childOn($dni)"
    def cmds = []
    if(channelNumber(dni).toInteger() <= 5) {
        toggleTiles("${channelNumber(dni)}", "on")
        cmds << new hubitat.device.HubAction(command(setParameter(8, calculateParameter("8-${channelNumber(dni)}"), 4)) )
        sendHubCommand(cmds, 1000)
    } else {
        childSetLevel(dni, 99)
    }
}

void childOff(String dni) {
    log.debug "childOff($dni)"
    def cmds = []
    if(channelNumber(dni).toInteger() <= 5) {
        toggleTiles("${channelNumber(dni)}", "off")
        cmds << new hubitat.device.HubAction(command(setParameter(8, 0, 4)) )
        sendHubCommand(cmds, 1000)
    } else {
        childSetLevel(dni, 99)
    }
}

void childRefresh(String dni) {
    log.debug "childRefresh($dni)"
}

def childExists(ep) {
    def children = childDevices
    def childDevice = children.find{it.deviceNetworkId.endsWith(ep)}
    if (childDevice) 
        return true
    else
        return false
}

def installed() {
    log.debug "installed()"
    refresh()
}

def configure() {
    log.debug "configure()"
    def cmds = initialize()
    commands(cmds)
}

def updated() {
    if (!state.lastRan || now() >= state.lastRan + 2000) {
        log.debug "updated()"
        state.lastRan = now()
        def cmds = initialize()
        if (cmds != [])
            commands(cmds, 1000)
        else 
            return null
    } else {
        log.debug "updated() ran within the last 2 seconds. Skipping execution."
    }
}

def initialize() {
    sendEvent(name: "checkInterval", value: 3 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    sendEvent(name: "numberOfButtons", value: 7, displayed: true)
    
    if (enableDisableLocalChild && !childExists("ep101")) {
    try {
        addChildDevice("Switch Level Child Device", "${device.deviceNetworkId}-ep101", null,
                [completedSetup: true, label: "${device.displayName} (Disable Local Control)",
                isComponent: true, componentName: "ep101", componentLabel: "Disable Local Control"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Level Child Device\" is installed"]])
    }
    } else if (!enableDisableLocalChild && childExists("ep101")) {
        log.debug "Trying to delete child device ep101. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
        try {
            log.debug "SmartThings has issues trying to delete the child device when it is in use. Need to manually delete them."
            //if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }
    
    [1,2,3,4,5].each { i ->
    if ((settings."parameter8-${i}a"!=null && settings."parameter8-${i}b"!=null && settings."parameter8-${i}c"!=null && settings."parameter8-${i}d"!=null) && !childExists("ep${i}")) {
    try {
        addChildDevice("Switch Child Device", "${device.deviceNetworkId}-ep${i}", null,
                [completedSetup: true, label: "${device.displayName} (Notification ${i})",
                isComponent: true, componentName: "ep${i}", componentLabel: "Notification ${i}"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Child Device\" is installed"]])
    }
    } else if ((settings."parameter8-${i}a"==null || settings."parameter8-${i}b"==null || settings."parameter8-${i}c"==null || settings."parameter8-${i}d"==null) && childExists("ep${i}")) {
        log.debug "Trying to delete child device ep${i}. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep${i}")}
        try {
            log.debug "SmartThings has issues trying to delete the child device when it is in use. Need to manually delete them."
            //if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }}
    if (device.label != state.oldLabel) {
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep1")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 1)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep2")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 2)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep3")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 3)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep4")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 4)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Disable Local Control)")
        state.oldLabel = device.label
    }
    
    def cmds = processAssociations()
    
    getParameterNumbers().each{ i ->
      if ((state."parameter${i}value" != (settings."parameter${i}"!=null? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger()))){
          cmds << setParameter(i, settings."parameter${i}"!=null? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger(), getParameterInfo(i, "size").toInteger())
          cmds << getParameter(i)
      }
      else {
          //log.debug "Parameter already set"
      }
    }
    
    cmds << zwave.versionV1.versionGet()
    
    if (state.disableLocal != settings.disableLocal) {
        cmds << zwave.protectionV2.protectionSet(localProtectionState : disableLocal!=null? disableLocal.toInteger() : 0, rfProtectionState: disableRemote!=null? disableRemote.toInteger() : 0)
        cmds << zwave.protectionV2.protectionGet()
    }
    
    state.disableRemote = settings.disableRemote
    state.disableLocal = settings.disableLocal
    if (cmds != []) return cmds else return []
}

def calculateParameter(number) {
    def value = 0
    switch (number){
      case "8-1":
      case "8-2":
      case "8-3": 
      case "8-4":
      case "8-5":
         value += settings."parameter${number}a"!=null ? settings."parameter${number}a".toInteger() * 1 : 0
         value += settings."parameter${number}b"!=null ? settings."parameter${number}b".toInteger() * 256 : 0
         value += settings."parameter${number}c"!=null ? settings."parameter${number}c".toInteger() * 65536 : 0
         value += settings."parameter${number}d"!=null ? settings."parameter${number}d".toInteger() * 16777216 : 0
      break
      default:
          value = settings."parameter${number}"
      break
    }
    return value
}

def setParameter(number, value, size) {
    log.debug "Setting parameter $number with a size of $size bytes to $value"
    return zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(value.toInteger(),size), parameterNumber: number, size: size)
}

def getParameter(number) {
    log.debug "Retreiving value of parameter $number"
    return zwave.configurationV1.configurationGet(parameterNumber: number)
}

def getParameterNumbers(){
    return [1,2,3,4,5,6,7,10,11,12]
}

def getParameterInfo(number, type){
    def parameter = [:]

    parameter.parameter1default=0
    parameter.parameter2default=0
    parameter.parameter3default=0
    parameter.parameter4default=15
    parameter.parameter5default=170
    parameter.parameter6default=5
    parameter.parameter7default=1
    parameter.parameter8default=0
    parameter.parameter9default=0
    parameter.parameter10default=10
    parameter.parameter11default=3600
    parameter.parameter12default=10
    
    parameter.parameter1type="enum"
    parameter.parameter2type="enum"
    parameter.parameter3type="number"
    parameter.parameter4type="number"
    parameter.parameter5type="enum"
    parameter.parameter6type="enum"
    parameter.parameter7type="enum"
    parameter.parameter8type="enum"
    parameter.parameter9type="enum"
    parameter.parameter10type="number"
    parameter.parameter11type="number"
    parameter.parameter12type="number"
    
    parameter.parameter1size=1
    parameter.parameter2size=1
    parameter.parameter3size=2
    parameter.parameter4size=1
    parameter.parameter5size=2
    parameter.parameter6size=1
    parameter.parameter7size=1
    parameter.parameter8size=4
    parameter.parameter9size=1
    parameter.parameter10size=1
    parameter.parameter11size=2
    parameter.parameter12size=1
    
	parameter.parameter1options=["0":"Previous", "1":"On", "2":"Off"]
    parameter.parameter2options=["1":"Yes", "0":"No"]
    parameter.parameter3options="1..32767"
    parameter.parameter4options="0..15"
    parameter.parameter5options=["0":"Red","21":"Orange","42":"Yellow","85":"Green","127":"Cyan","170":"Blue","212":"Violet","234":"Pink"]
    parameter.parameter6options=["0":"0%","1":"10%","2":"20%","3":"30%","4":"40%","5":"50%","6":"60%","7":"70%","8":"80%","9":"90%","10":"100%"]
    parameter.parameter7options=["0":"0%","1":"10%","2":"20%","3":"30%","4":"40%","5":"50%","6":"60%","7":"70%","8":"80%","9":"90%","10":"100%"]
    parameter.parameter8options=["1":"Yes", "2":"No"]
    parameter.parameter9options=["0":"Stay Off","1":"1 Second","2":"2 Seconds","3":"3 Seconds","4":"4 Seconds","5":"5 Seconds","6":"6 Seconds","7":"7 Seconds","8":"8 Seconds","9":"9 Seconds","10":"10 Seconds"]
    parameter.parameter10options="1..100"
    parameter.parameter11options="1..32767"
    parameter.parameter12options="1..100"
    
    parameter.parameter1name="State After Power Restored"
    parameter.parameter2name="Invert Switch"
    parameter.parameter3name="Auto Off Timer"
    parameter.parameter4name="Association Behavior"
    parameter.parameter5name="LED Strip Color"
    parameter.parameter6name="LED Strip Intensity"
    parameter.parameter7name="LED Strip Intensity (When OFF)"
    parameter.parameter8name="LED Strip Effect"
    parameter.parameter9name="LED Strip Timeout"
    parameter.parameter10name="Active Power Reports"
    parameter.parameter11name="Periodic Power & Energy Reports"
    parameter.parameter12name="Energy Reports"
    
    
    parameter.parameter1description="The state the switch should return to once power is restored after power failure."
	parameter.parameter2description="Inverts the orientation of the switch. Useful when the switch is installed upside down. Essentially up becomes down and down becomes up."
    parameter.parameter3description="Automatically turns the switch off after this many seconds. When the switch is turned on a timer is started that is the duration of this setting. When the timer expires, the switch is turned off."
    parameter.parameter4description="When should the switch send commands to associated devices?\n\n01 - local\n02 - 3way\n03 - 3way & local\n04 - z-wave hub\n05 - z-wave hub & local\n06 - z-wave hub & 3-way\n07 - z-wave hub & local & 3way\n08 - timer\n09 - timer & local\n10 - timer & 3-way\n11 - timer & 3-way & local\n12 - timer & z-wave hub\n13 - timer & z-wave hub & local\n14 - timer & z-wave hub & 3-way\n15 - all"
    parameter.parameter5description="This is the color of the LED strip."
    parameter.parameter6description="This is the intensity of the LED strip."
    parameter.parameter7description="This is the intensity of the LED strip when the switch is off. This is useful for users to see the light switch location when the lights are off."
    parameter.parameter8description="LED Strip Effect"
    parameter.parameter9description="When the LED strip is disabled (LED Strip Intensity is set to 0), this setting allows the LED strip to turn on temporarily while being adjusted."
    parameter.parameter10description="The power level change that will result in a new power report being sent. The value is a percentage of the previous report. 0 = disabled."
    parameter.parameter11description="Time period between consecutive power & energy reports being sent (in seconds). The timer is reset after each report is sent."
    parameter.parameter12description="The energy level change that will result in a new energy report being sent. The value is a percentage of the previous report."
    
    return parameter."parameter${number}${type}"
}



def zwaveEvent(hubitat.zwave.commands.meterv3.MeterReport cmd) {
    log.debug cmd
    def event
	if (cmd.scale == 0) {
    	if (cmd.meterType == 161) {
		    event = createEvent(name: "voltage", value: cmd.scaledMeterValue, unit: "V")
        } else if (cmd.meterType == 1) {
        	event = createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
        }
	} else if (cmd.scale == 1) {
		event = createEvent(name: "amperage", value: cmd.scaledMeterValue, unit: "A")
	} else if (cmd.scale == 2) {
		event = createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
	}

    return event
}

def zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug cmd
    log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
    state."parameter${cmd.parameterNumber}value" = cmd2Integer(cmd.configurationValue)
}

def cmd2Integer(array) {
    switch(array.size()) {
        case 1:
            array[0]
            break
        case 2:
            ((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
            break
        case 3:
            ((array[0] & 0xFF) << 16) | ((array[1] & 0xFF) << 8) | (array[2] & 0xFF)
            break
        case 4:
            ((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
            break
    }
}

def integer2Cmd(value, size) {
    try{
	switch(size) {
	case 1:
		[value]
    break
	case 2:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        [value2, value1]
    break
    case 3:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        [value3, value2, value1]
    break
	case 4:
    	def short value1 = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        def short value4 = (value >> 24) & 0xFF
		[value4, value3, value2, value1]
	break
	}
    } catch (e) {
        log.debug "Error: integer2Cmd $e Value: $value"
    }
}

private getCommandClassVersions() {
	[0x20: 1, 0x25: 1, 0x70: 1, 0x98: 1, 0x32: 3]
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        state.sec = 1
        zwaveEvent(encapsulatedCommand)
    }
}

def parse(description) {
    def result = null
    if (description.startsWith("Err 106")) {
        state.sec = 0
        result = createEvent(descriptionText: description, isStateChange: true)
    } else if (description != "updated") {
        def cmd = zwave.parse(description, commandClassVersions)
        if (cmd) {
            result = zwaveEvent(cmd)
            //log.debug("'$cmd' parsed to $result")
        } else {
            log.debug("Couldn't zwave.parse '$description'")
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

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}

def zwaveEvent(hubitat.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    log.debug cmd
    switch (cmd.keyAttributes) {
       case 0:
       if (cmd.sceneNumber == 3) createEvent(buttonEvent(7, "pushed", "physical"))
       else createEvent(buttonEvent(cmd.keyAttributes + 1, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
       case 1:
       createEvent(buttonEvent(6, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
       case 2:
       null
       break
       default:
       createEvent(buttonEvent(cmd.keyAttributes - 1, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
    }
}

def buttonEvent(button, value, type = "digital") {
    if(button != 6)
        sendEvent(name:"lastEvent", value: "${value != 'pushed'?' Tap '.padRight(button+5, '▼'):' Tap '.padRight(button+5, '▲')}", displayed:false)
    else
        sendEvent(name:"lastEvent", value: "${value != 'pushed'?' Hold ▼':' Hold ▲'}", displayed:false)
    [name: value, value: button, isStateChange:true]
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    log.debug "Unhandled: $cmd"
    null
}

def on() {
    commands([
        zwave.basicV1.basicSet(value: 0xFF),
        zwave.basicV1.basicGet()
    ])
}

def off() {
    commands([
        zwave.basicV1.basicSet(value: 0x00),
        zwave.basicV1.basicGet()
    ])
}

def ping() {
    log.debug "ping()"
    refresh()
}

def poll() {
    log.debug "poll()"
    refresh()
}

def refresh() {
    log.debug "refresh()"
    def cmds = []
    cmds << zwave.basicV1.basicGet()
    cmds << zwave.meterV3.meterGet(scale: 0)
	cmds << zwave.meterV3.meterGet(scale: 2)
    cmds << zwave.protectionV2.protectionGet()
    return commands(cmds)
}

private command(hubitat.zwave.Command cmd) {
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay=500) {
    delayBetween(commands.collect{ command(it) }, delay)
}

def pressUpX1() {
    sendEvent(buttonEvent(1, "pushed"))
}

def pressDownX1() {
    sendEvent(buttonEvent(1, "held"))
}

def pressUpX2() {
    sendEvent(buttonEvent(2, "pushed"))
}

def pressDownX2() {
    sendEvent(buttonEvent(2, "held"))
}

def pressUpX3() {
    sendEvent(buttonEvent(3, "pushed"))
}

def pressDownX3() {
    sendEvent(buttonEvent(3, "held"))
}

def pressUpX4() {
    sendEvent(buttonEvent(4, "pushed"))
}

def pressDownX4() {
    sendEvent(buttonEvent(4, "held"))
}

def pressUpX5() {
    sendEvent(buttonEvent(5, "pushed"))
}

def pressDownX5() {
    sendEvent(buttonEvent(5, "held"))
}

def holdUp() {
    sendEvent(buttonEvent(6, "pushed"))
}

def holdDown() {
    sendEvent(buttonEvent(6, "held"))
}

def pressConfig() {
    sendEvent(buttonEvent(7, "pushed"))
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
       log.debug "Getting supported association groups from device"
       cmds <<  zwave.associationV2.associationGroupingsGet()
   }
   for (int i = 1; i <= associationGroups; i++){
      if(state."actualAssociation${i}" != null){
         if(state."desiredAssociation${i}" != null || state."defaultG${i}") {
            def refreshGroup = false
            ((state."desiredAssociation${i}"? state."desiredAssociation${i}" : [] + state."defaultG${i}") - state."actualAssociation${i}").each {
                log.debug "Adding node $it to group $i"
                cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                refreshGroup = true
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                log.debug "Removing node $it from group $i"
                cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                refreshGroup = true
            }
            if (refreshGroup == true) cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
            else log.debug "There are no association actions to complete for group $i"
         }
      } else {
         log.debug "Association info not known for group $i. Requesting info from device."
         cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
      }
   }
   return cmds
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
    def temp = []
    if (cmd.nodeId != []) {
       cmd.nodeId.each {
          temp += it.toString().format( '%02x', it.toInteger() ).toUpperCase()
       }
    } 
    state."actualAssociation${cmd.groupingIdentifier}" = temp
    log.debug "Associations for Group ${cmd.groupingIdentifier}: ${temp}"
    updateDataValue("associationGroup${cmd.groupingIdentifier}", "$temp")
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    sendEvent(name: "groups", value: cmd.supportedGroupings)
    log.debug "Supported association groups: ${cmd.supportedGroupings}"
    state.associationGroups = cmd.supportedGroupings
}

def zwaveEvent(hubitat.zwave.commands.versionv1.VersionReport cmd) {
    log.debug cmd
    if(cmd.applicationVersion != null && cmd.applicationSubVersion != null) {
	    def firmware = "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2,'0')}"
        state.needfwUpdate = "false"
        createEvent(name: "firmware", value: "${firmware}")
    }
}

def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionReport cmd) {
    log.debug cmd
    def integerValue = cmd.localProtectionState
    def children = childDevices
    def childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
    if (childDevice) {
        childDevice.sendEvent(name: "switch", value: integerValue > 0 ? "on" : "off")        
    }
}
