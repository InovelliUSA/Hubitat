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

        fingerprint manufacturer: "031E", prod: "0004", model: "0001", deviceJoinName: "Inovelli Fan + Light"
        
        fingerprint deviceId: "0x1100", inClusters: "0x5E,0x55,0x98,0x9F,0x6C,0x26,0x70,0x85,0x59,0x8E,0x86,0x72,0x5A,0x73,0x75,0x22,0x7A,0x5B,0x87,0x60"
    }
    
    simulator {}
    
    preferences {
        input name: "debugEnable", type: "bool", title: "Enable debug logging", defaultValue: true
        input name: "infoEnable", type: "bool", title: "Enable informational logging", defaultValue: true
    }
    
    tiles {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute("device.lastEvent", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}',icon: "st.unknown.zwave.remote-controller")
            }
        }
        
        
        valueTile("lastActivity", "device.lastActivity", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label: 'Last Activity: ${currentValue}',icon: "st.Health & Wellness.health9"
        }
        
        valueTile("firmware", "device.firmware", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: 'fw: ${currentValue}', icon: ""
        }
        
        //valueTile("info", "device.info", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
        //    state "default", label: 'Tap on the ▲▲ button below to test your scene'
        //}
        
        valueTile("icon", "device.icon", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label: '', icon: "https://inovelli.com/wp-content/uploads/Device-Handler/Inovelli-Device-Handler-Logo.png"
        }
        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        
        //standardTile("pressUpX2", "device.button", width: 6, height: 1, decoration: "flat") {
        //    state "default", label: "Tap ▲▲", backgroundColor: "#ffffff", action: "pressUpX2"
        //}
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
        log.debug "Parsed ${cmd} to ${result.inspect()}"
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
    log.debug "updated()"
    if (!childDevices) {
        createChildDevices()
    } else if (device.label != state.oldLabel) {
        childDevices.each {
            if (it.label == "${state.oldLabel} (CH${channelNumber(it.deviceNetworkId)})") {
                def newLabel = "${device.displayName} (CH${channelNumber(it.deviceNetworkId)})"
                it.setLabel(newLabel)
            }
        }
        state.oldLabel = device.label
    }
    sendEvent(name: "checkInterval", value: 3 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    sendEvent(name: "numberOfButtons", value: 1, displayed: true)
    def cmds = []
    cmds << zwave.versionV1.versionGet()
    cmds << zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
    cmds << zwave.associationV2.associationGet(groupingIdentifier:1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [ledIndicator? ledIndicator.toInteger() : 0], parameterNumber: 1, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 1)
    cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: autoOff1? autoOff1.toInteger() : 0, parameterNumber: 2, size: 2)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 2)
    cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: autoOff2? autoOff2.toInteger() : 0, parameterNumber: 3, size: 2)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 3)
    commands(cmds)
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

def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
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
        addChildDevice("Switch Level Child Device", "${device.deviceNetworkId}-ep1", [completedSetup: true, label: "${device.displayName} (Light)",
            isComponent: false, componentName: "ep1", componentLabel: "Channel 1"
        ])
        addChildDevice("Switch Level Child Device", "${device.deviceNetworkId}-ep2", [completedSetup: true, label: "${device.displayName} (Fan)",
            isComponent: false, componentName: "ep2", componentLabel: "Channel 2"
        ])
    //}
}

def pressUpX2() {
    sendEvent(buttonEvent(1, "pushed"))
}
