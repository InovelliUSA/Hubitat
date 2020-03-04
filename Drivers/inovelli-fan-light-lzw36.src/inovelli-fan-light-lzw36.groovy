/**
 *  Inovelli Fan + Light LZW36
 *  Author: Eric Maycock (erocm123)
 *  Date: 2020-01-30
 *
 *  Copyright 2020 Inovelli / Eric Maycock
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
 * 
 */
 
metadata {
    definition(name: "Inovelli Fan + Light LZW36", namespace: "InovelliUSA", author: "Eric Maycock", vid: "generic-switch") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Polling"
        capability "Refresh"
        //capability "Health Check"
        capability "PushableButton"
        capability "Switch Level"
        
        attribute "lastActivity", "String"
        attribute "lastEvent", "String"
        attribute "firmware", "String"
        
        command "setAssociationGroup", ["number", "enum", "number", "number"] // group number, nodes, action (0 - remove, 1 - add), multi-channel endpoint (optional)
        command "childOn"
        command "childOff"
        command "childRefresh"
        command "childSetLevel"

        fingerprint manufacturer: "031E", prod: "000D", model: "0001", deviceJoinName: "Inovelli Fan + Light"
        
        fingerprint deviceId: "0x1100", inClusters: "0x5E,0x55,0x98,0x9F,0x6C,0x26,0x70,0x85,0x59,0x8E,0x86,0x72,0x5A,0x73,0x75,0x22,0x7A,0x5B,0x87,0x60"
    }
    
    simulator {}
    
    preferences {
        generate_preferences()
    }
}

private getCommandClassVersions() {
	[0x20: 1, 0x25: 1, 0x70: 1, 0x98: 1]
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        state.sec = 1
        zwaveEvent(encapsulatedCommand)
    }
}

def parse(String description) {
    def result = []
    def cmd = zwave.parse(description, commandClassVersions)
    if (cmd) {
        result += zwaveEvent(cmd)
        //log.debug "Parsed ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    def now
    if(location.timeZone)
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    else
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a")
    sendEvent(name: "lastActivity", value: now, displayed:false)
    return result
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd, ep = null) {
    log.debug "BasicReport ${cmd} - ep ${ep}"
    def event
    if (ep) {
        
        childDevices.each {
            childDevice ->
                if (childDevice.deviceNetworkId == "$device.deviceNetworkId-ep$ep") {
                    childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
                }
        }
        if (cmd.value) {
            event = [createEvent([name: "switch", value: "on"])]
        } else {
            def allOff = true
            childDevices.each {
                childDevice ->
				    if (childDevice.deviceNetworkId != "$device.deviceNetworkId-ep$ep") 
                       if (childDevice.currentState("switch").value != "off") allOff = false
            }
            if (allOff) {
                event = [createEvent([name: "switch", value: "off"])]
            } else {
                event = [createEvent([name: "switch", value: "on"])]
            }
        }
        return event
    } else {
        event = [createEvent([name: "level", value: cmd.value])]
    }
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    log.debug "BasicSet ${cmd}"
    def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def cmds = []
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
    return [result, response(commands(cmds))] // returns the result of reponse()
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep = null) {
    log.debug "SwitchBinaryReport ${cmd} - ep ${ep}"
    if (ep) {
        def event
        def childDevice = childDevices.find {
            it.deviceNetworkId == "$device.deviceNetworkId-ep$ep"
        }
        if (childDevice) childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
        if (cmd.value) {
            event = [createEvent([name: "switch", value: "on"])]
        } else {
            def allOff = true
            childDevices.each {
                n->
                    if (n.currentState("switch").value != "off") allOff = false
            }
            if (allOff) {
                event = [createEvent([name: "switch", value: "off"])]
            } else {
                event = [createEvent([name: "switch", value: "on"])]
            }
        }
        return event
    } else {
        def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
        def cmds = []
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
        return [result, response(commands(cmds))] // returns the result of reponse()
    }
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd, ep = null) {
    log.debug "SwitchMultilevelReport ${cmd} - ep${ep}"
    if (ep) {
        def event
        def childDevice = childDevices.find {
            it.deviceNetworkId == "$device.deviceNetworkId-ep$ep"
        }
        if (childDevice) {
            childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
            if (cmd.value && cmd.value <= 100) {
            	childDevice.sendEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value)
            }
        }
        if (cmd.value) {
            event = [createEvent([name: "switch", value: "on"])]
        } else {
            def allOff = true
            childDevices.each {
                n ->
                    if (n.currentState("switch").value != "off") allOff = false
            }
            if (allOff) {
                event = [createEvent([name: "switch", value: "off"])]
            } else {
                event = [createEvent([name: "switch", value: "on"])]
            }
        }
        return event
    } else {
        def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
        def cmds = []
        cmds << encap(zwave.switchMultilevelV2.switchMultilevelGet(), 1)
        cmds << encap(zwave.switchMultilevelV2.switchMultilevelGet(), 2)
        return [result, response(commands(cmds))] // returns the result of response()
    }
}

def zwaveEvent(hubitat.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    log.debug "MultiChannelCmdEncap ${cmd}"
    def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
    }
}

def zwaveEvent(hubitat.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.debug "ManufacturerSpecificReport ${cmd}"
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    log.debug "msr: $msr"
    updateDataValue("MSR", msr)
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

def zwaveEvent(hubitat.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    log.debug "Unhandled Event: ${cmd}"
}

def on() {
    log.debug "on()"
    commands([
            zwave.basicV1.basicSet(value: 0xFF),
            encap(zwave.basicV1.basicGet(), 1),
            encap(zwave.basicV1.basicGet(), 2)
    ])
}

def off() {
    log.debug "off()"
    commands([
            zwave.basicV1.basicSet(value: 0x00),
            encap(zwave.basicV1.basicGet(), 1),
            encap(zwave.basicV1.basicGet(), 2)
    ])
}

def setLevel(value) {
    if (!infoEnable) log.info "${device.label?device.label:device.name}: setLevel($value)"
    commands([
        zwave.basicV1.basicSet(value: value < 100 ? value : 99),
        encap(zwave.basicV1.basicGet(), 1),
        encap(zwave.basicV1.basicGet(), 2)
        //zwave.basicV1.basicGet()
    ])
}

def childOn(String dni) {
    log.debug "childOn($dni)"
    def cmds = []
    commands([
		encap(zwave.basicV1.basicSet(value: 0xFF), channelNumber(dni)),
        encap(zwave.basicV1.basicGet(), channelNumber(dni))
    ])
}

def childOff(String dni) {
    log.debug "childOff($dni)"
    def cmds = []
    commands([
		encap(zwave.basicV1.basicSet(value: 0x00), channelNumber(dni)),
        encap(zwave.basicV1.basicGet(), channelNumber(dni))
    ])
}

def childRefresh(String dni) {
    log.debug "childRefresh($dni)"
    def cmds = []
    cmds << encap(zwave.basicV1.basicGet(), channelNumber(dni))
    cmds
}

def childSetLevel(String dni, value) {
	log.debug "childSetLevel($dni,$value)"   
    def childDevice = childDevices.find {
    	it.deviceNetworkId == "$dni"
    } 
    def valueaux = value as Integer
    def level = Math.max(Math.min(valueaux, 99), 0)
    if (level > 0) {
        childDevice.sendEvent(name: "switch", value: "on")
    } else {
        childDevice.sendEvent(name: "switch", value: "off")
    }
    def cmds = []
    commands([
        encap(zwave.switchMultilevelV1.switchMultilevelSet(value: level), channelNumber(dni)),
        encap(zwave.switchMultilevelV1.switchMultilevelGet(), channelNumber(dni))
    ])
}

def poll() {
    log.debug "poll()"
    commands([
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
    ])
}

def refresh() {
    log.debug "refresh()"
    commands([
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
    ])
}

def ping() {
    log.debug "ping()"
    refresh()
}

def installed() {
    log.debug "installed()"
    command(zwave.manufacturerSpecificV1.manufacturerSpecificGet())
    createChildDevices()
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

def logsOff(){
    log.warn "${device.label?device.label:device.name}: Disabling logging after timeout"
    //device.updateSetting("debugEnable",[value:"false",type:"bool"])
    device.updateSetting("infoEnable",[value:"false",type:"bool"])
}

def initialize() {
    sendEvent(name: "checkInterval", value: 3 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    sendEvent(name: "numberOfButtons", value: 8, displayed: true)
    
    if (enableDefaultLocalChild && !childExists("ep9")) {
    try {
        addChildDevice("Switch Level Child Device", "${device.deviceNetworkId}-ep9", 
                [completedSetup: true, label: "${device.displayName} (Default Local Level)",
                isComponent: false, componentName: "ep9", componentLabel: "Default Local Level"])
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
                isComponent: false, componentName: "ep10", componentLabel: "Default Z-Wave Level"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Level Child Device\" is installed"]])
    }
    } else if (!enableDefaultZWaveChild && childExists("ep10")) {
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
                isComponent: false, componentName: "ep101", componentLabel: "Disable Local Control"])
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
                isComponent: false, componentName: "ep102", componentLabel: "Disable Remote Control"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Level Child Device\" is installed"]])
    }
    } else if (!enableDisableRemoteChild && childExists("ep102")) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: Trying to delete child device ep102. If this fails it is likely that there is a SmartApp using the child device in question."
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
                isComponent: false, componentName: "ep${i}", componentLabel: "Notification ${i}"])
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
        childDevice = children.find{it.deviceNetworkId.endsWith("ep5")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 5)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep9")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Default Local Level)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep10")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Default Z-Wave Level)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Disable Local Control)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep102")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Disable Remote Control)")
        state.oldLabel = device.label
    }
    
    def cmds = processAssociations()
    
    getParameterNumbers().each{ i ->
      if ((state."parameter${i}value" != ((settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger()))){
          //if (infoEnable) log.info "Parameter $i is not set correctly. Setting it to ${settings."parameter${i}"!=null? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger()}."
          cmds << setParameter(i, (settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger(), getParameterInfo(i, "size").toInteger())
          cmds << getParameter(i)
      }
      else {
          //if (infoEnable) log.info "${device.label?device.label:device.name}: Parameter $i already set"
      }
    }
    
    cmds << zwave.versionV1.versionGet()
    
    if (state.localProtectionState?.toInteger() != settings.disableLocal?.toInteger() || state.rfProtectionState?.toInteger() != settings.disableRemote?.toInteger()) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: Protection command class settings need to be updated"
        cmds << zwave.protectionV2.protectionSet(localProtectionState : disableLocal!=null? disableLocal.toInteger() : 0, rfProtectionState: disableRemote!=null? disableRemote.toInteger() : 0)
        cmds << zwave.protectionV2.protectionGet()
    } else {
        if (infoEnable) log.info "${device.label?device.label:device.name}: No Protection command class settings to update"
    }

    if (cmds != []) return cmds else return []
}

def childExists(ep) {
    def children = childDevices
    def childDevice = children.find{it.deviceNetworkId.endsWith(ep)}
    if (childDevice) 
        return true
    else
        return false
}

def zwaveEvent(hubitat.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    log.debug "${device.label?device.label:device.name}: ${device.label?device.label:device.name}: ${cmd}"
    switch (cmd.keyAttributes) {
       case 0:
       if (cmd.sceneNumber == 3) createEvent(buttonEvent(7, "pushed", "physical"))
       else if (cmd.sceneNumber == 6) createEvent(buttonEvent(7, "held", "physical"))
       else createEvent(buttonEvent(cmd.keyAttributes + 1, (cmd.sceneNumber == 2||cmd.sceneNumber == 5? "held" : "pushed"), "physical"))
       break
       case 1:
       createEvent(buttonEvent(6, (cmd.sceneNumber == 2||cmd.sceneNumber == 5? "held" : "pushed"), "physical"))
       break
       case 2:
       createEvent(buttonEvent(8, (cmd.sceneNumber == 2||cmd.sceneNumber == 5? "pushed" : "held"), "physical"))
       break
       default:
       createEvent(buttonEvent(cmd.keyAttributes - 1, (cmd.sceneNumber == 2||cmd.sceneNumber == 5? "held" : "pushed"), "physical"))
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

def zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
    def integerValue = cmd2Integer(cmd.configurationValue)
    state."parameter${cmd.parameterNumber}value" = integerValue
    switch (cmd.parameterNumber) {
        case 9:
            device.updateSetting("parameter${cmd.parameterNumber}",[value:integerValue,type:"number"])
            def children = childDevices
            def childDevice = children.find{it.deviceNetworkId.endsWith("ep9")}
            if (childDevice) {
            childDevice.sendEvent(name: "switch", value: integerValue > 0 ? "on" : "off")
            childDevice.sendEvent(name: "level", value: integerValue)            
            }
        break
        case 10:
            device.updateSetting("parameter${cmd.parameterNumber}",[value:integerValue,type:"number"])
            def children = childDevices
            def childDevice = children.find{it.deviceNetworkId.endsWith("ep10")}
            if (childDevice) {
            childDevice.sendEvent(name: "switch", value: integerValue > 0 ? "on" : "off")
            childDevice.sendEvent(name: "level", value: integerValue)
            }
        break
    }
}

def getParameterNumbers(){
    return [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,26,27,28,29,30]
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

def getParameterInfo(number, value){
    def parameter = [:]
    
    parameter.parameter1type="number"
    parameter.parameter2type="number"
    parameter.parameter3type="number"
    parameter.parameter4type="number"
    parameter.parameter5type="number"
    parameter.parameter6type="number"
    parameter.parameter7type="number"
    parameter.parameter8type="number"
    parameter.parameter9type="enum"
    parameter.parameter10type="number"
    parameter.parameter11type="number"
    parameter.parameter12type="number"
    parameter.parameter13type="number"
    parameter.parameter14type="number"
    parameter.parameter15type="number"
    parameter.parameter16type="number"
    parameter.parameter17type="number"
    parameter.parameter18type="number"
    parameter.parameter19type="number"
    parameter.parameter20type="number"
    parameter.parameter21type="number"
    parameter.parameter22type="number"
    parameter.parameter23type="number"
    parameter.parameter26type="enum"
    parameter.parameter27type="enum"
    parameter.parameter28type="number"
    parameter.parameter29type="number"
    parameter.parameter30type="number"

    parameter.parameter1default=4
    parameter.parameter2default=99
    parameter.parameter3default=99
    parameter.parameter4default=99
    parameter.parameter5default=1
    parameter.parameter6default=99
    parameter.parameter7default=1
    parameter.parameter8default=99
    parameter.parameter9default=0
    parameter.parameter10default=0
    parameter.parameter11default=0
    parameter.parameter12default=0
    parameter.parameter13default=0
    parameter.parameter14default=0
    parameter.parameter15default=0
    parameter.parameter16default=0
    parameter.parameter17default=0
    parameter.parameter18default=170
    parameter.parameter19default=5
    parameter.parameter20default=170
    parameter.parameter21default=5
    parameter.parameter22default=1
    parameter.parameter23default=1
    parameter.parameter26default=3
    parameter.parameter27default=3
    parameter.parameter28default=10
    parameter.parameter29default=3600
    parameter.parameter30default=10

    parameter.parameter1options="0..98"
    parameter.parameter2options="0..99"
    parameter.parameter3options="0..99"
    parameter.parameter4options="0..99"
    parameter.parameter5options="1..45"
    parameter.parameter6options="55..99"
    parameter.parameter7options="1..45"
    parameter.parameter8options="55..99"
    parameter.parameter9options=["1":"Yes", "0":"No"]
    parameter.parameter10options="0..32767"
    parameter.parameter11options="0..32767"
    parameter.parameter12options="1..99"
    parameter.parameter13options="1..99"
    parameter.parameter14options="1..99"
    parameter.parameter15options="1..99"
    parameter.parameter16options="0..100"
    parameter.parameter17options="0..100"
    parameter.parameter18options="0..255"
    parameter.parameter19options="0..9"
    parameter.parameter20options="0..255"
    parameter.parameter21options="0..9"
    parameter.parameter22options="0..9"
    parameter.parameter23options="0..9"
    parameter.parameter26options=["0":"Stay Off","1":"1 Second","2":"2 Seconds","3":"3 Seconds","4":"4 Seconds","5":"5 Seconds","6":"6 Seconds","7":"7 Seconds","8":"8 Seconds","9":"9 Seconds","10":"10 Seconds"]
    parameter.parameter27options=["0":"Stay Off","1":"1 Second","2":"2 Seconds","3":"3 Seconds","4":"4 Seconds","5":"5 Seconds","6":"6 Seconds","7":"7 Seconds","8":"8 Seconds","9":"9 Seconds","10":"10 Seconds"]
    parameter.parameter28options="0..100"
    parameter.parameter29options="0..32767"
    parameter.parameter30options="0..100"

    parameter.parameter1size=1
    parameter.parameter2size=1
    parameter.parameter3size=1
    parameter.parameter4size=1
    parameter.parameter5size=1
    parameter.parameter6size=1
    parameter.parameter7size=1
    parameter.parameter8size=1
    parameter.parameter9size=1
    parameter.parameter10size=2
    parameter.parameter11size=2
    parameter.parameter12size=1
    parameter.parameter13size=1
    parameter.parameter14size=1
    parameter.parameter15size=1
    parameter.parameter16size=1
    parameter.parameter17size=1
    parameter.parameter18size=1
    parameter.parameter19size=1
    parameter.parameter20size=1
    parameter.parameter21size=1
    parameter.parameter22size=1
    parameter.parameter23size=1
    parameter.parameter26size=1
    parameter.parameter27size=1
    parameter.parameter28size=1
    parameter.parameter29size=2
    parameter.parameter30size=1

    parameter.parameter1description="This changes the speed in which the attached light dims up or down. A setting of 0 should turn the light immediately on or off (almost like an on/off switch). Increasing the value should slow down the transition speed."
	parameter.parameter2description="This changes the speed in which the attached light dims up or down when controlled from the physical switch. A setting of 0 should turn the light immediately on or off (almost like an on/off switch). Increasing the value should slow down the transition speed. A setting of 99 should keep this in sync with parameter 1."
	parameter.parameter3description="This changes the speed in which the attached light turns on or off. For example, when a user sends the switch a basicSet(value: 0xFF) or basicSet(value: 0x00), this is the speed in which those actions take place. A setting of 0 should turn the light immediately on or off (almost like an on/off switch). Increasing the value should slow down the transition speed. A setting of 99 should keep this in sync with parameter 1."
	parameter.parameter4description="This changes the speed in which the attached light turns on or off from the physical switch. For example, when a user presses the up or down button, this is the speed in which those actions take place. A setting of 0 should turn the light immediately on or off (almost like an on/off switch). Increasing the value should slow down the transition speed. A setting of 99 should keep this in sync with parameter 1."
	parameter.parameter5description="The minimum level that the dimmer allows the bulb to be dimmed to. Useful when the user has a bulb that does not turn at a lower level."
	parameter.parameter6description="The maximum level that the dimmer allows the bulb to be dimmed to. Useful when the user has an LED bulb that reaches its maximum level before the dimmer value of 99."
	parameter.parameter7description="The minimum level that the dimmer allows the fan to be dimmed to. Useful when the user has a fan that does not turn at a lower level."
    parameter.parameter8description="The maximum level that the dimmer allows the fan to be dimmed to."
    parameter.parameter10description="Automatically turns the light switch off after this many seconds. When the switch is turned on a timer is started that is the duration of this setting. When the timer expires, the switch is turned off."
    parameter.parameter11description="Automatically turns the fan switch off after this many seconds. When the switch is turned on a timer is started that is the duration of this setting. When the timer expires, the switch is turned off."
    parameter.parameter12description="Default level for the dimmer when it is powered on from the local switch. A setting of 0 means that the switch will return to the level that it was on before it was turned off."
    parameter.parameter13description="Default level for the dimmer when it is powered on from a Z-Wave command. A setting of 0 means that the switch will return to the level that it was on before it was turned off."
    parameter.parameter14description="Default level for the dimmer when it is powered on from the local switch. A setting of 0 means that the switch will return to the level that it was on before it was turned off."
    parameter.parameter15description="Default level for the dimmer when it is powered on from a Z-Wave command. A setting of 0 means that the switch will return to the level that it was on before it was turned off."
    parameter.parameter16description="The state the switch should return to once power is restored after power failure. 0 = off, 1-99 = level, 100=previous."
    parameter.parameter17description="The state the switch should return to once power is restored after power failure. 0 = off, 1-99 = level, 100=previous."
    parameter.parameter18description="This is the color of the Light LED strip represented as part of the HUE color wheel. Since the wheel has 360 values and this parameter only has 255, the following equation can be used to determine the color: value/255 * 350 = Hue color wheel value"
    parameter.parameter19description="This is the intensity of the Light LED strip."
    parameter.parameter20description="This is the color of the Fan LED strip represented as part of the HUE color wheel. Since the wheel has 360 values and this parameter only has 255, the following equation can be used to determine the color: value/255 * 350 = Hue color wheel value"
    parameter.parameter21description="This is the intensity of the Fan LED strip."
    parameter.parameter22description="This is the intensity of the Light LED strip when the switch is off. This is useful for users to see the light switch location when the lights are off."
    parameter.parameter23description="This is the intensity of the Fan LED strip when the switch is off. This is useful for users to see the light switch location when the lights are off."
    parameter.parameter26description="When the LED strip is disabled (Light LED Strip Intensity is set to 0), this setting allows the LED strip to turn on temporarily while being adjusted."
    parameter.parameter27description="When the LED strip is disabled (Fan LED Strip Intensity is set to 0), this setting allows the LED strip to turn on temporarily while being adjusted."
    parameter.parameter28description="The power level change that will result in a new power report being sent. The value is a percentage of the previous report. 0 = disabled."
    parameter.parameter29description="Time period between consecutive power & energy reports being sent (in seconds). The timer is reset after each report is sent."


    parameter.parameter1name="Dimming Speed"
    parameter.parameter2name="Dimming Speed (From Switch)"
    parameter.parameter3name="Ramp Rate"
    parameter.parameter4name="Ramp Rate (From Switch)"
    parameter.parameter5name="Minimum Light Level"
    parameter.parameter6name="Maximum Light Level"
    parameter.parameter7name="Minimum Fan Level"
    parameter.parameter8name="Maximum Fan Level"
    parameter.parameter9name="Invert Switch"
    parameter.parameter10name="Auto Off Light Timer"
    parameter.parameter11name="Auto Off Fan Timer"
    parameter.parameter12name="Default Light Level (Local)"
    parameter.parameter13name="Default Light Level (Z-Wave)"
    parameter.parameter14name="Default Fan Level (Local)"
    parameter.parameter15name="Default Fan Level (Z-Wave)"
    parameter.parameter16name="Light State After Power Restored"
    parameter.parameter17name="Fan State After Power Restored"
    parameter.parameter18name="Light LED Indicator Color"
    parameter.parameter19name="Light LED Strip Intensity"
    parameter.parameter20name="Fan LED Indicator Color"
    parameter.parameter21name="Fan LED Strip Intensity"
    parameter.parameter22name="Light LED Strip Intensity (When OFF)"
    parameter.parameter23name="Fan LED Strip Intensity (When OFF)"
    parameter.parameter26name="Light LED Strip Timeout"
    parameter.parameter27name="Fan LED Strip Timeout"
    parameter.parameter28name="Active Power Reports"
    parameter.parameter29name="Periodic Power & Energy Reports"
    parameter.parameter30name="Energy Reports"

    return parameter."parameter${number}${value}"
}

private encap(cmd, endpoint) {
    if (endpoint) {
        zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint).encapsulate(cmd)
    } else {
        cmd
    }
}

private command(hubitat.zwave.Command cmd) {
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay = 1000) {
    delayBetween(commands.collect {
        command(it)
    }, delay)
}

private channelNumber(String dni) {
    dni.split("-ep")[-1] as Integer
}
private void createChildDevices() {
    state.oldLabel = device.label
    //for (i in 1..2) {
        addChildDevice("Inovelli Fan Child Device", "${device.deviceNetworkId}-ep1", [completedSetup: true, label: "${device.displayName} (Light)",
            isComponent: false, componentName: "ep1", componentLabel: "Channel 1"
        ])
        addChildDevice("Inovelli Fan Child Device", "${device.deviceNetworkId}-ep2", [completedSetup: true, label: "${device.displayName} (Fan)",
            isComponent: false, componentName: "ep2", componentLabel: "Channel 2"
        ])
    //}
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

def pressUpX2() {
    sendEvent(buttonEvent(1, "pushed"))
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

def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Protection report received: Local protection is ${cmd.localProtectionState > 0 ? "on" : "off"} & Remote protection is ${cmd.rfProtectionState > 0 ? "on" : "off"}"
    state.localProtectionState = cmd.localProtectionState
    state.rfProtectionState = cmd.rfProtectionState
    device.updateSetting("disableLocal",[value:cmd.localProtectionState?"1":"0",type:"enum"])
    device.updateSetting("disableRemote",[value:cmd.rfProtectionState?"1":"0",type:"enum"])
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
