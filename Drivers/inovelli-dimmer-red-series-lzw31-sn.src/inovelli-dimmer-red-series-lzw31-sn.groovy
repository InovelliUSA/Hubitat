/**
 *  Inovelli Dimmer Red Series LZW31-SN
 *  Author: Eric Maycock (erocm123)
 *  Date: 2020-01-28
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
 *  2020-01-28: Update VersionReport parsing because of Hubitat change. Removing unnecessary reports.
 *
 *  2019-12-03: Specify central scene command class version for upcoming Hubitat update.
 *
 *  2019-11-16: Ability to choose custom LED bar color. Child devices to control default level (local & z-wave),
 *              local & remote protection. Ability to turn on / off info & debug logging. Additional logging added.
 *              Prevent overwriting parameters that are configured at the switch. Bumping number of notifications to 5.
 *              Added additional button events for "released" vs "held". Button 8 pushed (up released)
 *              & button 8 released (down released). Other bug fixes and improvements. 
 *
 *  2019-11-13: Bug fix for not being able to set default level back to 0
 *  2020-02-06: updated by bcopeland - added ChangeLevel capability and relevant commands 
 */
 
metadata {
    definition (name: "Inovelli Dimmer Red Series LZW31-SN", namespace: "InovelliUSA", author: "Eric Maycock", vid: "generic-dimmer", importUrl:"https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-dimmer-red-series-lzw31-sn.src/inovelli-dimmer-red-series-lzw31-sn.groovy") {
        capability "Switch"
        capability "Refresh"
        capability "Polling"
        capability "Actuator"
        capability "Sensor"
        //capability "Health Check"
        capability "PushableButton"
        capability "HoldableButton"
        capability "Switch Level"
        capability "Configuration"
        capability "Energy Meter"
        capability "Power Meter"
        capability "ChangeLevel"
        
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
        
        command "childOn"
        command "childOff"
        command "childSetLevel"
        command "childRefresh"
        command "reset"
        
        command "setAssociationGroup", ["number", "enum", "number", "number"] // group number, nodes, action (0 - remove, 1 - add), multi-channel endpoint (optional)

        fingerprint mfr: "031E", prod: "0001", model: "0001", deviceJoinName: "Inovelli Dimmer Red Series"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x55,0x98,0x9F,0x6C,0x22,0x26,0x70,0x85,0x59,0x86,0x32,0x72,0x5A,0x5B,0x73,0x75,0x7A" // Red Series
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x26,0x70,0x85,0x59,0x55,0x86,0x72,0x5A,0x73,0x32,0x98,0x9F,0x5B,0x6C,0x75,0x22,0x7A" // Red Series
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
        if (i == 13){
            input "parameter13custom", "number", 
                title: "Custom LED RGB Value", 
                description: "\nInput a custom value in this field to override the above setting. The value should be between 0 - 360 and can be determined by using the typical hue color wheel.", 
                required: false,
                range: "0..360"
        }
    }
    
    input description: "When each notification set (Color, Level, Duration, Type) is configured, a switch child device is created that can be used in SmartApps to activate that notification.", title: "LED Notifications", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    
    [1,2,3,4,5].each { i ->
                input "parameter16-${i}a", "enum", title: "LED Effect Color - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    0:"Red",
                    21:"Orange",
                    42:"Yellow",
                    85:"Green",
                    127:"Cyan",
                    170:"Blue",
                    212:"Violet",
                    234:"Pink"]
                input "parameter16-${i}b", "enum", title: "LED Effect Level - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
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
                input "parameter16-${i}c", "enum", title: "LED Effect Duration - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
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
                input "parameter16-${i}d", "enum", title: "LED Effect Type - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    0:"Off",
                    1:"Solid",
                    2:"Chase",
                    3:"Fast Blink",
                    4:"Slow Blink",
                    5:"Pulse"]
    
    }
    input "disableLocal", "enum", title: "Disable Local Control", description: "\nDisable ability to control switch from the wall", required: false, options:[["1": "Yes"], ["0": "No"]], defaultValue: "0"
    input "disableRemote", "enum", title: "Disable Remote Control", description: "\nDisable ability to control switch from inside SmartThings", required: false, options:[["1": "Yes"], ["0": "No"]], defaultValue: "0"
    input description: "Use the below options to enable child devices for the specified settings. This will allow you to adjust these settings using SmartApps such as Smart Lighting. If any of the options are enabled, make sure you have the appropriate child device handlers installed.\n(Firmware 1.02+)", title: "Child Devices", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    input "enableDisableLocalChild", "bool", title: "Disable Local Control", description: "", required: false, defaultValue: false
    input "enableDisableRemoteChild", "bool", title: "Disable Remote Control", description: "", required: false, defaultValue: false
    input "enableDefaultLocalChild", "bool", title: "Default Level (Local)", description: "", required: false, defaultValue: false
    input "enableDefaultZWaveChild", "bool", title: "Default Level (Z-Wave)", description: "", required: false, defaultValue: false
    input name: "debugEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    input name: "infoEnable", type: "bool", title: "Enable informational logging", defaultValue: true
}

private channelNumber(String dni) {
    dni.split("-ep")[-1] as Integer
}

def logsOff(){
    log.warn "${device.label?device.label:device.name}: Disabling logging after timeout"
    device.updateSetting("debugEnable",[value:"false",type:"bool"])
    device.updateSetting("infoEnable",[value:"false",type:"bool"])
}

private sendAlert(data) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: Error while creating child device"
    sendEvent(
        descriptionText: data.message,
        eventType: "ALERT",
        name: "failedOperation",
        value: "failed",
        displayed: true,
    )
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

def startLevelChange(direction) {
    def upDownVal = direction == "down" ? true : false
	if (logEnable) log.debug "got startLevelChange(${direction})"
    commands([zwave.switchMultilevelV2.switchMultilevelStartLevelChange(ignoreStartLevel: true, startLevel: device.currentValue("level"), upDown: upDownVal)])
}

def stopLevelChange() {
    commands([zwave.switchMultilevelV2.switchMultilevelStopLevelChange()])
}

def childSetLevel(String dni, value) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childSetLevel($dni, $value)"
    state.lastRan = now()
    def valueaux = value as Integer
    def level = Math.max(Math.min(valueaux, 99), 0)    
    def cmds = []
    switch (channelNumber(dni)) {
        case 9:
            cmds << new hubitat.device.HubAction(command(zwave.configurationV1.configurationSet(scaledConfigurationValue: value, parameterNumber: channelNumber(dni), size: 1) ), hubitat.device.Protocol.ZWAVE)
            cmds << new hubitat.device.HubAction(command(zwave.configurationV1.configurationGet(parameterNumber: channelNumber(dni) )), hubitat.device.Protocol.ZWAVE)
        break
        case 10:
            cmds << new hubitat.device.HubAction(command(zwave.configurationV1.configurationSet(scaledConfigurationValue: value, parameterNumber: channelNumber(dni), size: 1) ), hubitat.device.Protocol.ZWAVE)
            cmds << new hubitat.device.HubAction(command(zwave.configurationV1.configurationGet(parameterNumber: channelNumber(dni) )), hubitat.device.Protocol.ZWAVE)
        break
        case 101:
            cmds << new hubitat.device.HubAction(command(zwave.protectionV2.protectionSet(localProtectionState : level > 0 ? 1 : 0, rfProtectionState: state.rfProtectionState? state.rfProtectionState:0) ), hubitat.device.Protocol.ZWAVE)
            cmds << new hubitat.device.HubAction(command(zwave.protectionV2.protectionGet() ), hubitat.device.Protocol.ZWAVE)
        break
        case 102:
            cmds << new hubitat.device.HubAction(command(zwave.protectionV2.protectionSet(localProtectionState : state.localProtectionState? state.localProtectionState:0, rfProtectionState : level > 0 ? 1 : 0) ), hubitat.device.Protocol.ZWAVE)
            cmds << new hubitat.device.HubAction(command(zwave.protectionV2.protectionGet() ), hubitat.device.Protocol.ZWAVE)
        break
    }
	return cmds
}

def childOn(String dni) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childOn($dni)"
    state.lastRan = now()
    def cmds = []
    if(channelNumber(dni).toInteger() <= 5) {
        toggleTiles("${channelNumber(dni)}", "on")
        cmds << new hubitat.device.HubAction(command(setParameter(16, calculateParameter("16-${channelNumber(dni)}"), 4)), hubitat.device.Protocol.ZWAVE )
        return cmds
    } else {
        childSetLevel(dni, 99)
    }
}

def childOff(String dni) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childOff($dni)"
    state.lastRan = now()
    def cmds = []
    if(channelNumber(dni).toInteger() <= 5) {
        toggleTiles("${channelNumber(dni)}", "off")
        cmds << new hubitat.device.HubAction(command(setParameter(16, 0, 4)), hubitat.device.Protocol.ZWAVE )
        return cmds
    } else {
        childSetLevel(dni, 0)
    }
}

void childRefresh(String dni) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childRefresh($dni)"
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
    if (infoEnable) log.info "${device.label?device.label:device.name}: installed()"
    refresh()
}

def configure() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: configure()"
    def cmds = initialize()
    commands(cmds)
}

def updated() {
    if (!state.lastRan || now() >= state.lastRan + 2000) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: updated()"
        if (debugEnable || infoEnable) runIn(1800,logsOff)
        state.lastRan = now()
        def cmds = initialize()
        if (cmds != [])
            commands(cmds, 1000)
        else 
            return null
    } else {
        if (infoEnable) log.info "${device.label?device.label:device.name}: updated() ran within the last 2 seconds. Skipping execution."
    }
}

def initialize() {
    sendEvent(name: "checkInterval", value: 3 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    sendEvent(name: "numberOfButtons", value: 8, displayed: true)
    
    if (enableDefaultLocalChild && !childExists("ep9")) {
    try {
        addChildDevice("Switch Level Child Device", "${device.deviceNetworkId}-ep9", 
                [completedSetup: true, label: "${device.displayName} (Default Local Level)",
                isComponent: true, componentName: "ep9", componentLabel: "Default Local Level"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Level Child Device\" is installed"]])
    }
    } else if (!enableDefaultLocalChild && childExists("ep9")) {
        if (infoEnable) log.info "Trying to delete child device ep9. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep9")}
        try {
            if (infoEnable) log.info "SmartThings has issues trying to delete the child device when it is in use. Need to manually delete them."
            //if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }
    if (enableDefaultZWaveChild && !childExists("ep10")) {
    try {
        addChildDevice("Switch Level Child Device", "${device.deviceNetworkId}-ep10", 
                [completedSetup: true, label: "${device.displayName} (Default Z-Wave Level)",
                isComponent: true, componentName: "ep10", componentLabel: "Default Z-Wave Level"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Level Child Device\" is installed"]])
    }
    } else if (!enableDefaultLocalChild && childExists("ep10")) {
        if (infoEnable) log.info "Trying to delete child device ep10. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep10")}
        try {
            if (infoEnable) log.info "SmartThings has issues trying to delete the child device when it is in use. Need to manually delete them."
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
        if (infoEnable) log.info "${device.label?device.label:device.name}: Trying to delete child device ep101. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
        try {
            if (infoEnable) log.info "${device.label?device.label:device.name}: SmartThings has issues trying to delete the child device when it is in use. Need to manually delete them."
            //if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }
    if (enableDisableRemoteChild && !childExists("ep102")) {
    try {
        addChildDevice("Switch Level Child Device", "${device.deviceNetworkId}-ep102", 
                [completedSetup: true, label: "${device.displayName} (Disable Remote Control)",
                isComponent: true, componentName: "ep102", componentLabel: "Disable Remote Control"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Level Child Device\" is installed"]])
    }
    } else if (!enableDisableRemoteChild && childExists("ep102")) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: Trying to delete child device ep101. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep102")}
        try {
            if (infoEnable) log.info "${device.label?device.label:device.name}: SmartThings has issues trying to delete the child device when it is in use. Need to manually delete them."
            //if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }
    
    [1,2,3,4,5].each { i ->
    if ((settings."parameter16-${i}a"!=null && settings."parameter16-${i}b"!=null && settings."parameter16-${i}c"!=null && settings."parameter16-${i}d"!=null) && !childExists("ep${i}")) {
    try {
        addChildDevice("Switch Child Device", "${device.deviceNetworkId}-ep${i}", 
                [completedSetup: true, label: "${device.displayName} (Notification ${i})",
                isComponent: true, componentName: "ep${i}", componentLabel: "Notification ${i}"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Child Device\" is installed"]])
    }
    } else if ((settings."parameter16-${i}a"==null || settings."parameter16-${i}b"==null || settings."parameter16-${i}c"==null || settings."parameter16-${i}d"==null) && childExists("ep${i}")) {
        if (infoEnable) log.info "Trying to delete child device ep${i}. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep${i}")}
        try {
            if (infoEnable) log.info "SmartThings has issues trying to delete the child device when it is in use. Need to manually delete them."
            //if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }}
    
    if (device.label != state.oldLabel) {
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep1")}
        if (childDevice) childDevice.setLabel("${device.displayName} (Notification 1)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep2")}
        if (childDevice) childDevice.setLabel("${device.displayName} (Notification 2)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep3")}
        if (childDevice) childDevice.setLabel("${device.displayName} (Notification 3)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep4")}
        if (childDevice) childDevice.setLabel("${device.displayName} (Notification 4)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep5")}
        if (childDevice) childDevice.setLabel("${device.displayName} (Notification 5)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep9")}
        if (childDevice) childDevice.setLabel("${device.displayName} (Default Local Level)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep10")}
        if (childDevice) childDevice.setLabel("${device.displayName} (Default Z-Wave Level)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
        if (childDevice) childDevice.setLabel("${device.displayName} (Disable Local Control)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep102")}
        if (childDevice) childDevice.setLabel("${device.displayName} (Disable Remote Control)")
        state.oldLabel = device.label
    }
    
    def cmds = processAssociations()
    
    getParameterNumbers().each{ i ->
      if ((state."parameter${i}value" != ((settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger()))){
          if (infoEnable) log.info "Parameter $i is not set correctly. Setting it to ${settings."parameter${i}"!=null? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger()}."
          cmds << setParameter(i, (settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger(), getParameterInfo(i, "size").toInteger())
          cmds << getParameter(i)
      }
      else {
          if (infoEnable) log.info "${device.label?device.label:device.name}: Parameter $i already set"
      }
    }
    
    cmds << zwave.versionV1.versionGet()
    
    if (state.localProtectionState != settings.disableLocal || state.rfProtectionState != settings.disableRemote) {
        cmds << zwave.protectionV2.protectionSet(localProtectionState : disableLocal!=null? disableLocal.toInteger() : 0, rfProtectionState: disableRemote!=null? disableRemote.toInteger() : 0)
        cmds << zwave.protectionV2.protectionGet()
    }

    if (cmds != []) return cmds else return []
}

def calculateParameter(number) {
    def value = 0
    switch (number){
      case "13":
          if (settings.parameter13custom =~ /^([0-9]{1}|[0-9]{2}|[0-9]{3})$/) value = settings.parameter13custom.toInteger() / 360 * 255
          else value = settings."parameter${number}"
      break
      case "16-1":
      case "16-2":
      case "16-3": 
      case "16-4":
      case "16-5":
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
    if (infoEnable) log.info "${device.label?device.label:device.name}: Setting parameter $number with a size of $size bytes to $value"
    return zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(value.toInteger(),size), parameterNumber: number, size: size)
}

def getParameter(number) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: Retreiving value of parameter $number"
    return zwave.configurationV1.configurationGet(parameterNumber: number)
}
def getParameterNumbers(){
    return [1,2,3,4,5,6,7,8,9,10,11,13,14,15,17,18,19,20,21,22]
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
    parameter.parameter9options="0..100"
    parameter.parameter10options="0..100"
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

def zwaveEvent(hubitat.zwave.commands.meterv3.MeterReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    def event
	if (cmd.scale == 0) {
    	if (cmd.meterType == 161) {
		    event = createEvent(name: "voltage", value: cmd.scaledMeterValue, unit: "V")
            if (infoEnable) log.info "${device.label?device.label:device.name}: Voltage report received with value of ${cmd.scaledMeterValue} V"
        } else if (cmd.meterType == 1) {
        	event = createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
            if (infoEnable) log.info "${device.label?device.label:device.name}: Energy report received with value of ${cmd.scaledMeterValue} kWh"
        }
	} else if (cmd.scale == 1) {
		event = createEvent(name: "amperage", value: cmd.scaledMeterValue, unit: "A")
        if (infoEnable) log.info "${device.label?device.label:device.name}: Amperage report received with value of ${cmd.scaledMeterValue} A"
	} else if (cmd.scale == 2) {
		event = createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
        if (infoEnable) log.info "${device.label?device.label:device.name}: Power report received with value of ${cmd.scaledMeterValue} W"
	}

    return event
}

def zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
    if (!state.lastRan || now() <= state.lastRan + 60000) {
        state."parameter${cmd.parameterNumber}value" = cmd2Integer(cmd.configurationValue)
    } else {
        if (infoEnable) log.debug "${device.label?device.label:device.name}: Configuration report received more than 60 seconds after running updated(). Possible configuration made at switch"
    }
    def integerValue = cmd2Integer(cmd.configurationValue)
    switch (cmd.parameterNumber) {
        case 9:
            def children = childDevices
            def childDevice = children.find{it.deviceNetworkId.endsWith("ep9")}
            if (childDevice) {
            childDevice.sendEvent(name: "switch", value: integerValue > 0 ? "on" : "off")
            childDevice.sendEvent(name: "level", value: integerValue)            
            }
        break
        case 10:
            def children = childDevices
            def childDevice = children.find{it.deviceNetworkId.endsWith("ep10")}
            if (childDevice) {
            childDevice.sendEvent(name: "switch", value: integerValue > 0 ? "on" : "off")
            childDevice.sendEvent(name: "level", value: integerValue)
            }
        break
    }
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
        if (debugEnable) log.debug "Error: integer2Cmd $e Value: $value"
    }
}

private getCommandClassVersions() {
	[0x20: 1, 0x25: 1, 0x70: 1, 0x98: 1, 0x32: 3, 0x5B: 1]
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
            if (debugEnable) log.debug "Couldn't zwave.parse '$description'" 
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
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Basic report received with value of ${cmd.value ? "on" : "off"} ($cmd.value)"
    // Since SmartThings isn't filtering duplicate events, we are skipping these
    // Switch is sending SwitchMultilevelReport as well (which we will use)
    dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Basic set received with value of ${cmd.value ? "on" : "off"}"
    dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Switch Binary report received with value of ${cmd.value ? "on" : "off"}"
    dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Switch Multilevel report received with value of ${cmd.value ? "on" : "off"} ($cmd.value)"
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

def zwaveEvent(hubitat.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    switch (cmd.keyAttributes) {
       case 0:
       if (cmd.sceneNumber == 3) createEvent(buttonEvent(7, "pushed", "physical"))
       else createEvent(buttonEvent(cmd.keyAttributes + 1, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
       case 1:
       createEvent(buttonEvent(6, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
       case 2:
       createEvent(buttonEvent(8, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
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
    if (infoEnable) log.info "${device.label?device.label:device.name}: Button ${button} was ${value}"
    [name: value, value: button, isStateChange:true]
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: Unhandled: $cmd"
    null
}

def reset() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: Resetting energy statistics"
	def cmds = []
    cmds << zwave.meterV2.meterReset()
    cmds << zwave.meterV2.meterGet(scale: 0)
    cmds << zwave.meterV2.meterGet(scale: 2)
	commands(cmds, 1000)
}

def on() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: on()"
    commands([
        zwave.switchMultilevelV1.switchMultilevelSet(value: 0xFF)//,
        //zwave.switchMultilevelV1.switchMultilevelGet()
    ])
}

def off() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: off()"
    commands([
        zwave.switchMultilevelV1.switchMultilevelSet(value: 0x00)//,
        //zwave.switchMultilevelV1.switchMultilevelGet()
    ])
}

def setLevel(value) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: setLevel($value)"
    commands([
        zwave.basicV1.basicSet(value: value < 100 ? value : 99)//,
        //zwave.basicV1.basicGet()
    ])
}

def setLevel(value, duration) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: setLevel($value, $duration)"
    def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
    commands([
        zwave.switchMultilevelV2.switchMultilevelSet(value: value < 100 ? value : 99, dimmingDuration: dimmingDuration)//,
        //zwave.switchMultilevelV1.switchMultilevelGet()
    ])
}

def ping() {
    if (debugEnable) log.debug "ping()"
    refresh()
}

def poll() {
    if (debugEnable) log.debug "poll()"
    refresh()
}

def refresh() {
    if (debugEnable) log.debug "refresh()"
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
       if (infoEnable) log.info "${device.label?device.label:device.name}: Getting supported association groups from device"
       cmds <<  zwave.associationV2.associationGroupingsGet()
   }
   for (int i = 1; i <= associationGroups; i++){
      if(state."actualAssociation${i}" != null){
         if(state."desiredAssociation${i}" != null || state."defaultG${i}") {
            def refreshGroup = false
            ((state."desiredAssociation${i}"? state."desiredAssociation${i}" : [] + state."defaultG${i}") - state."actualAssociation${i}").each {
                if (it != null){
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Adding node $it to group $i"
                    cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                    refreshGroup = true
                }
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                if (it != null){
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Removing node $it from group $i"
                    cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                    refreshGroup = true
                }
            }
            if (refreshGroup == true) cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
            else if (infoEnable) log.info "${device.label?device.label:device.name}: There are no association actions to complete for group $i"
         }
      } else {
         if (infoEnable) log.info "${device.label?device.label:device.name}: Association info not known for group $i. Requesting info from device."
         cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
      }
   }
   return cmds
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    def temp = []
    if (cmd.nodeId != []) {
       cmd.nodeId.each {
          temp += it.toString().format( '%02x', it.toInteger() ).toUpperCase()
       }
    } 
    state."actualAssociation${cmd.groupingIdentifier}" = temp
    if (infoEnable) log.info "${device.label?device.label:device.name}: Associations for Group ${cmd.groupingIdentifier}: ${temp}"
    updateDataValue("associationGroup${cmd.groupingIdentifier}", "$temp")
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    sendEvent(name: "groups", value: cmd.supportedGroupings)
    if (infoEnable) log.info "${device.label?device.label:device.name}: Supported association groups: ${cmd.supportedGroupings}"
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

def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Protection report received: Local protection is ${cmd.localProtectionState > 0 ? "on" : "off"} & Remote protection is ${cmd.rfProtectionState > 0 ? "on" : "off"}"
    if (!state.lastRan || now() <= state.lastRan + 60000) {
        state.localProtectionState = cmd.localProtectionState
        state.rfProtectionState = cmd.rfProtectionState
    } else {
        if (infoEnable) log.debug "${device.label?device.label:device.name}: Protection report received more than 60 seconds after running updated(). Possible configuration made at switch"
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
