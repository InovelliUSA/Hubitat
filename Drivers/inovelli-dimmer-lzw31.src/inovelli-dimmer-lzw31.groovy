/**
 *  Inovelli Dimmer LZW31
 *  Author: Eric Maycock (erocm123)
 *  Date: 2019-11-05
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
    definition (name: "Inovelli Dimmer LZW31", namespace: "InovelliUSA", author: "Eric Maycock", vid: "generic-dimmer", importUrl:"https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-dimmer-lzw31.src/inovelli-dimmer-lzw31.groovy") {
        capability "Switch"
        capability "Refresh"
        capability "Polling"
        capability "Actuator"
        capability "Sensor"
        //capability "Health Check"
        capability "Switch Level"
        capability "Configuration"
        
        attribute "lastActivity", "String"
        attribute "lastEvent", "String"
        attribute "firmware", "String"
        
        command "reset"
        command "setAssociationGroup", ["number", "enum", "number", "number"] // group number, nodes, action (0 - remove, 1 - add), multi-channel endpoint (optional)

        fingerprint mfr: "031E", prod: "0003", model: "0001", deviceJoinName: "Inovelli Dimmer"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x55,0x98,0x9F,0x6C,0x22,0x26,0x70,0x85,0x59,0x86,0x72,0x5A,0x73,0x75,0x7A" 
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x26,0x70,0x85,0x59,0x55,0x86,0x72,0x5A,0x73,0x98,0x9F,0x6C,0x75,0x22,0x7A" 
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
                    //displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "enum":
                input "parameter${i}", "enum",
                    title:getParameterInfo(i, "name"), // + getParameterInfo(i, "description"),
                    //defaultValue: getParameterInfo(i, "default"),
                    //displayDuringSetup: "${it.@displayDuringSetup}",
                    options: getParameterInfo(i, "options")
            break
        }  
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
        case 8:
            cmds << new hubitat.device.HubAction(command(zwave.configurationV1.configurationSet(scaledConfigurationValue: value, parameterNumber: channelNumber(dni), size: 1) ))
            cmds << new hubitat.device.HubAction(command(zwave.configurationV1.configurationGet(parameterNumber: channelNumber(dni) )))
        break
        case 9:
            cmds << new hubitat.device.HubAction(command(zwave.configurationV1.configurationSet(scaledConfigurationValue: value, parameterNumber: channelNumber(dni), size: 1) ))
            cmds << new hubitat.device.HubAction(command(zwave.configurationV1.configurationGet(parameterNumber: channelNumber(dni) )))
        break
        case 101:
            cmds << new hubitat.device.HubAction(command(zwave.protectionV2.protectionSet(localProtectionState : level > 0 ? 2 : 0, rfProtectionState: 0) ))
            cmds << new hubitat.device.HubAction(command(zwave.protectionV2.protectionGet() ))
        break
    }
	sendHubCommand(cmds, 1000)
}

private toggleTiles(number, value) {
   log.debug "$number:$value"
   for (int i = 1; i <= 4; i++){
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
    if(channelNumber(dni).toInteger() <= 4) {
        toggleTiles("${channelNumber(dni)}", "on")
        cmds << new hubitat.device.HubAction(command(setParameter(16, calculateParameter("16-${channelNumber(dni)}"), 4)) )
        sendHubCommand(cmds, 1000)
    } else {
        childSetLevel(dni, 99)
    }
}

void childOff(String dni) {
    log.debug "childOff($dni)"
    def cmds = []
    if(channelNumber(dni).toInteger() <= 4) {
        toggleTiles("${channelNumber(dni)}", "off")
        cmds << new hubitat.device.HubAction(command(setParameter(16, 0, 4)) )
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
    
    if (enableDefaultLocalChild && !childExists("ep8")) {
    try {
        addChildDevice("Switch Level Child Device", "${device.deviceNetworkId}-ep8", 
                [completedSetup: true, label: "${device.displayName} (Default Local Level)",
                isComponent: true, componentName: "ep8", componentLabel: "Default Local Level"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Level Child Device\" is installed"]])
    }
    } else if (!enableDefaultLocalChild && childExists("ep8")) {
        log.debug "Trying to delete child device ep8. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep8")}
        try {
            log.debug "SmartThings has issues trying to delete the child device when it is in use. Need to manually delete them."
            //if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }
    if (enableDefaultZWaveChild && !childExists("ep9")) {
    try {
        addChildDevice("Switch Level Child Device", "${device.deviceNetworkId}-ep9", 
                [completedSetup: true, label: "${device.displayName} (Default Z-Wave Level)",
                isComponent: true, componentName: "ep9", componentLabel: "Default Z-Wave Level"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Level Child Device\" is installed"]])
    }
    } else if (!enableDefaultLocalChild && childExists("ep9")) {
        log.debug "Trying to delete child device ep9. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep9")}
        try {
            log.debug "SmartThings has issues trying to delete the child device when it is in use. Need to manually delete them."
            //if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }
    if (enableDisableLocalChild && !childExists("ep101")) {
    try {
        addChildDevice("Switch Level Child Device", "${device.deviceNetworkId}-ep101", 
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
    [1,2,3,4].each { i ->
    if ((settings."parameter16-${i}a"!=null && settings."parameter16-${i}b"!=null && settings."parameter16-${i}c"!=null && settings."parameter16-${i}d"!=null) && !childExists("ep${i}")) {
    try {
        addChildDevice("Switch Child Device", "${device.deviceNetworkId}-ep${i}", 
                [completedSetup: true, label: "${device.displayName} (Notification ${i})",
                isComponent: true, componentName: "ep${i}", componentLabel: "Notification ${i}"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Child Device\" is installed"]])
    }
    } else if ((settings."parameter16-${i}a"==null || settings."parameter16-${i}b"==null || settings."parameter16-${i}c"==null || settings."parameter16-${i}d"==null) && childExists("ep${i}")) {
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
        childDevice.setLabel("${device.displayName} (Notification 1)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep8")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Default Local Level)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep9")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Default Z-Wave Level)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Disable Local Control)")
        state.oldLabel = device.label
    }

    sendEvent([name:"pressUpX1", value:pressUpX1Label? "${pressUpX1Label} ▲" : "Tap ▲", displayed: false])
    sendEvent([name:"pressDownX1", value:pressDownX1Label? "${pressDownX1Label} ▼" : "Tap ▼", displayed: false])
    sendEvent([name:"pressUpX2", value:pressUpX2Label? "${pressUpX2Label} ▲▲" : "Tap ▲▲", displayed: false])
    sendEvent([name:"pressDownX2", value:pressDownX2Label? "${pressDownX2Label} ▼▼" : "Tap ▼▼", displayed: false])
    sendEvent([name:"pressUpX3", value:pressUpX3Label? "${pressUpX3Label} ▲▲▲" : "Tap ▲▲▲", displayed: false])
    sendEvent([name:"pressDownX3", value:pressDownX3Label? "${pressDownX3Label} ▼▼▼" : "Tap ▼▼▼", displayed: false])
    sendEvent([name:"pressUpX4", value:pressUpX4Label? "${pressUpX4Label} ▲▲▲▲" : "Tap ▲▲▲▲", displayed: false])
    sendEvent([name:"pressDownX4", value:pressDownX4Label? "${pressDownX4Label} ▼▼▼▼" : "Tap ▼▼▼▼", displayed: false])
    sendEvent([name:"pressUpX5", value:pressUpX5Label? "${pressUpX5Label} ▲▲▲▲▲" : "Tap ▲▲▲▲▲", displayed: false])
    sendEvent([name:"pressDownX5", value:pressDownX5Label? "${pressDownX5Label} ▼▼▼▼▼" : "Tap ▼▼▼▼▼", displayed: false])
    sendEvent([name:"holdUp", value:pressHoldUpLabel? "${pressHoldUpLabel} ▲" : "Hold ▲", displayed: false])
    sendEvent([name:"holdDown", value:pressHoldDownLabel? "${pressHoldDownLabel} ▼" : "Hold ▼", displayed: false])
    
    def cmds = processAssociations()
    
    getParameterNumbers().each{ i ->
      //log.debug "$i: ${state."parameter${i}value"}"
      if ((state."parameter${i}value" != (settings."parameter${i}"!=null? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger())) || i == 16){
          //log.debug "Parameter $i is not set correctly. Setting it to ${settings."parameter${i}"!=null? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger()}."
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

    state.defaultLocal = settings.defaultLocal
    state.defaultZWave = settings.defaultZWave
    state.disableLocal = settings.disableLocal
    if (cmds != []) return cmds else return []
}

def calculateParameter(number) {
    def value = 0
    switch (number){
      case "16-1":
      case "16-2":
      case "16-3": 
      case "16-4":
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
    //log.debug "Setting parameter $number with a size of $size bytes to $value"
    return zwave.configurationV1.configurationGet(parameterNumber: number)
}
def getParameterNumbers(){
    return [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,17,21,22]
}

def getParameterInfo(number, value){
    def parameter = [:]
    
    parameter.parameter1default=3
    parameter.parameter2default=101
    parameter.parameter3default=101
    parameter.parameter4default=101
    parameter.parameter5default=1
    parameter.parameter6default=99
    parameter.parameter7default=0
    parameter.parameter8default=0
    parameter.parameter9default=0
    parameter.parameter10default=0
    parameter.parameter11default=0
    parameter.parameter12default=15
    parameter.parameter13default=170
    parameter.parameter14default=5
    parameter.parameter15default=1
    parameter.parameter16default=0
    parameter.parameter17default=3
    parameter.parameter18default=10
    parameter.parameter19default=3600
    parameter.parameter20default=10
    parameter.parameter21default=1
    parameter.parameter22default=0
    
    parameter.parameter1type="number"
    parameter.parameter2type="number"
    parameter.parameter3type="number"
    parameter.parameter4type="number"
    parameter.parameter5type="number"
    parameter.parameter6type="number"
    parameter.parameter7type="enum"
    parameter.parameter8type="number"
    parameter.parameter9type="number"
    parameter.parameter10type="number"
    parameter.parameter11type="number"
    parameter.parameter12type="number"
    parameter.parameter13type="enum"
    parameter.parameter14type="enum"
    parameter.parameter15type="enum"
    parameter.parameter16type="enum"
    parameter.parameter17type="enum"
    parameter.parameter18type="number"
    parameter.parameter19type="number"
    parameter.parameter20type="number"
    parameter.parameter21type="enum"
    parameter.parameter22type="enum"
    
    parameter.parameter1size=1
    parameter.parameter2size=1
    parameter.parameter3size=1
    parameter.parameter4size=1
    parameter.parameter5size=1
    parameter.parameter6size=1
    parameter.parameter7size=1
    parameter.parameter8size=2
    parameter.parameter9size=1
    parameter.parameter10size=1
    parameter.parameter11size=1
    parameter.parameter12size=1
    parameter.parameter13size=2
    parameter.parameter14size=1
    parameter.parameter15size=1
    parameter.parameter16size=4
    parameter.parameter17size=1
    parameter.parameter18size=1
    parameter.parameter19size=2
    parameter.parameter20size=1
    parameter.parameter21size=1
    parameter.parameter22size=1
    
    parameter.parameter1options="0..100"
    parameter.parameter2options="0..101"
    parameter.parameter3options="0..101"
    parameter.parameter4options="0..101"
    parameter.parameter5options="1..45"
    parameter.parameter6options="55..99"
    parameter.parameter7options=["1":"Yes", "0":"No"]
    parameter.parameter8options="0..32767"
    parameter.parameter9options="1..100"
    parameter.parameter10options="1..100"
    parameter.parameter11options="0..100"
    parameter.parameter12options="0..15"
    parameter.parameter13options=["0":"Red","21":"Orange","42":"Yellow","85":"Green","127":"Cyan","170":"Blue","212":"Violet","234":"Pink"]
    parameter.parameter14options=["0":"0%","1":"10%","2":"20%","3":"30%","4":"40%","5":"50%","6":"60%","7":"70%","8":"80%","9":"90%","10":"100%"]
    parameter.parameter15options=["0":"0%","1":"10%","2":"20%","3":"30%","4":"40%","5":"50%","6":"60%","7":"70%","8":"80%","9":"90%","10":"100%"]
    parameter.parameter16options=["1":"Yes", "2":"No"]
    parameter.parameter17options=["0":"Stay Off","1":"1 Second","2":"2 Seconds","3":"3 Seconds","4":"4 Seconds","5":"5 Seconds","6":"6 Seconds","7":"7 Seconds","8":"8 Seconds","9":"9 Seconds","10":"10 Seconds"]
    parameter.parameter18options="0..100"
    parameter.parameter19options="0..32767"
    parameter.parameter20options="0..100"
    parameter.parameter21options=["0":"Non Neutral", "1":"Neutral"]
    parameter.parameter22options=["0":"Load Only", "1":"3-way Toggle", "2":"3-way Momentary"]
    
    parameter.parameter1name="Dimming Speed"
    parameter.parameter2name="Dimming Speed (From Switch)"
    parameter.parameter3name="Ramp Rate"
    parameter.parameter4name="Ramp Rate (From Switch)"
    parameter.parameter5name="Minimum Level"
    parameter.parameter6name="Maximum Level"
    parameter.parameter7name="Invert Switch"
    parameter.parameter8name="Auto Off Timer"
    parameter.parameter9name="Default Level (Local)"
    parameter.parameter10name="Default Level (Z-Wave)"
    parameter.parameter11name="State After Power Restored"
    parameter.parameter12name="Association Behavior"
    parameter.parameter13name="LED Strip Color"
    parameter.parameter14name="LED Strip Intensity"
    parameter.parameter15name="LED Strip Intensity (When OFF)"
    parameter.parameter16name="LED Strip Effect"
    parameter.parameter17name="LED Strip Timeout"
    parameter.parameter18name="Active Power Reports"
    parameter.parameter19name="Periodic Power & Energy Reports"
    parameter.parameter20name="Energy Reports"
    parameter.parameter21name="AC Power Type"
    parameter.parameter22name="Switch Type"
    
    parameter.parameter1description="This changes the speed in which the attached light dims up or down. A setting of 0 should turn the light immediately on or off (almost like an on/off switch). Increasing the value should slow down the transition speed."
    parameter.parameter2description="This changes the speed in which the attached light dims up or down when controlled from the physical switch. A setting of 0 should turn the light immediately on or off (almost like an on/off switch). Increasing the value should slow down the transition speed. A setting of 101 should keep this in sync with parameter 1."
    parameter.parameter3description="This changes the speed in which the attached light turns on or off. For example, when a user sends the switch a basicSet(value: 0xFF) or basicSet(value: 0x00), this is the speed in which those actions take place. A setting of 0 should turn the light immediately on or off (almost like an on/off switch). Increasing the value should slow down the transition speed. A setting of 101 should keep this in sync with parameter 1."
    parameter.parameter4description="This changes the speed in which the attached light turns on or off from the physical switch. For example, when a user presses the up or down button, this is the speed in which those actions take place. A setting of 0 should turn the light immediately on or off (almost like an on/off switch). Increasing the value should slow down the transition speed. A setting of 101 should keep this in sync with parameter 1."
    parameter.parameter5description="The minimum level that the dimmer allows the bulb to be dimmed to. Useful when the user has an LED bulb that does not turn on or flickers at a lower level."
    parameter.parameter6description="The maximum level that the dimmer allows the bulb to be dimmed to. Useful when the user has an LED bulb that reaches its maximum level before the dimmer value of 99."
    parameter.parameter7description="Inverts the orientation of the switch. Useful when the switch is installed upside down. Essentially up becomes down and down becomes up."
    parameter.parameter8description="Automatically turns the switch off after this many seconds. When the switch is turned on a timer is started that is the duration of this setting. When the timer expires, the switch is turned off."
    parameter.parameter9description="Default level for the dimmer when it is powered on from the local switch. A setting of 0 means that the switch will return to the level that it was on before it was turned off."
    parameter.parameter10description="Default level for the dimmer when it is powered on from a Z-Wave command. A setting of 0 means that the switch will return to the level that it was on before it was turned off."
    parameter.parameter11description="The state the switch should return to once power is restored after power failure. 0 = off, 1-99 = level, 100=previous."
    parameter.parameter12description="When should the switch send commands to associated devices?\n\n01 - local\n02 - 3way\n03 - 3way & local\n04 - z-wave hub\n05 - z-wave hub & local\n06 - z-wave hub & 3-way\n07 - z-wave hub & local & 3way\n08 - timer\n09 - timer & local\n10 - timer & 3-way\n11 - timer & 3-way & local\n12 - timer & z-wave hub\n13 - timer & z-wave hub & local\n14 - timer & z-wave hub & 3-way\n15 - all"
    parameter.parameter13description="This is the color of the LED strip."
    parameter.parameter14description="This is the intensity of the LED strip."
    parameter.parameter15description="This is the intensity of the LED strip when the switch is off. This is useful for users to see the light switch location when the lights are off."
    parameter.parameter16description="LED Strip Effect"
    parameter.parameter17description="When the LED strip is disabled (LED Strip Intensity is set to 0), this setting allows the LED strip to turn on temporarily while being adjusted."
    parameter.parameter18description="The power level change that will result in a new power report being sent. The value is a percentage of the previous report. 0 = disabled."
    parameter.parameter19description="Time period between consecutive power & energy reports being sent (in seconds). The timer is reset after each report is sent."
    parameter.parameter20description="The energy level change that will result in a new energy report being sent. The value is a percentage of the previous report."
    parameter.parameter21description="Configure the switch to use a neutral wire."
    parameter.parameter22description="Configure the type of 3-way switch connected to the dimmer."
    
    return parameter."parameter${number}${value}"
}

def zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
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
            log.debug("'$cmd' parsed to $result")
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
    // Since SmartThings isn't filtering duplicate events, we are skipping these
    // Switch is sending SwitchMultilevelReport as well (which we will use)
    dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    dimmerEvents(cmd)
}

private dimmerEvents(hubitat.zwave.Command cmd) {
    def value = (cmd.value ? "on" : "off")
    def result = [createEvent(name: "switch", value: value)]
    if (cmd.value) {
        result << createEvent(name: "level", value: cmd.value, unit: "%")
    }
    return result
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    log.debug "Unhandled: $cmd"
    null
}

def reset() {
	def cmds = []
    cmds << zwave.meterV2.meterReset()
    cmds << zwave.meterV2.meterGet(scale: 0)
    cmds << zwave.meterV2.meterGet(scale: 2)
	commands(cmds, 1000)
}

def on() {
    commands([
        //zwave.basicV1.basicSet(value: 0xFF),
        //zwave.basicV1.basicGet()
        zwave.switchMultilevelV1.switchMultilevelSet(value: 0xFF),
        zwave.switchMultilevelV1.switchMultilevelGet()
    ])
}

def off() {
    commands([
        //zwave.basicV1.basicSet(value: 0x00),
        //zwave.basicV1.basicGet()
        zwave.switchMultilevelV1.switchMultilevelSet(value: 0x00),
        zwave.switchMultilevelV1.switchMultilevelGet()
    ])
}

def setLevel(value) {
    if (disableRemote != "2") {
    commands([
        zwave.basicV1.basicSet(value: value < 100 ? value : 99),
        zwave.basicV1.basicGet()
    ])
    } else {
    commands([
        zwave.basicV1.basicGet()
    ])
    }
}

def setLevel(value, duration) {
    if (disableRemote != "2") {
    def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
        commands([
            zwave.switchMultilevelV2.switchMultilevelSet(value: value < 100 ? value : 99, dimmingDuration: dimmingDuration),
            zwave.switchMultilevelV1.switchMultilevelGet()
        ])
    } else {
    commands([
        zwave.switchMultilevelV1.switchMultilevelGet()
    ])
    }
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
    cmds << zwave.switchMultilevelV1.switchMultilevelGet()
    cmds << zwave.meterV3.meterGet(scale: 0)
	cmds << zwave.meterV3.meterGet(scale: 2)
    return commands(cmds)
}

private command(hubitat.zwave.Command cmd) {
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay=1000) {
    delayBetween(commands.collect{ command(it) }, delay)
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
                if (it != null){
                    log.debug "Adding node $it to group $i"
                    cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                    refreshGroup = true
                }
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                if (it != null){
                    log.debug "Removing node $it from group $i"
                    cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                    refreshGroup = true
                }
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
