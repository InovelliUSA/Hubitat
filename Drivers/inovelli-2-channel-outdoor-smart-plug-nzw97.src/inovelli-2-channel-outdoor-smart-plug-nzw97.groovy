/**
 *  Inovelli 2-Channel Outdoor Smart Plug NZW97
 *  Author: Eric Maycock (erocm123)
 *  Date: 2017-11-14
 *
 *  Copyright 2017 Eric Maycock
 *
 *	Edited for Hubitat by Stephan Hackett 3/26/18
 *
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
    definition(name: "Inovelli 2-Channel Outdoor Smart Plug NZW97", namespace: "InovelliUSA", author: "Eric Maycock") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Polling"
        capability "Refresh"
        capability "Health Check"
        capability "PushableButton"
        
        attribute "lastActivity", "String"
        attribute "lastEvent", "String"
        attribute "switch1", "String"
        attribute "switch2", "String"
        
        command "on1"
        command "on2"
        command "off1"
        command "off2"

        fingerprint manufacturer: "015D", prod: "6100", model: "6100", deviceJoinName: "Inovelli 2-Channel Outdoor Smart Plug"
        fingerprint manufacturer: "0312", prod: "6100", model: "6100", deviceJoinName: "Inovelli 2-Channel Outdoor Smart Plug"
        fingerprint manufacturer: "015D", prod: "0221", model: "611C", deviceJoinName: "Inovelli 2-Channel Outdoor Smart Plug"
        fingerprint manufacturer: "0312", prod: "0221", model: "611C", deviceJoinName: "Inovelli 2-Channel Outdoor Smart Plug"
    }
    
    preferences {
        input "autoOff1", "number", title: "Auto Off Channel 1\n\nAutomatically turn switch off after this number of seconds\nRange: 0 to 32767", description: "Tap to set", required: false, range: "0..32767"
        input "autoOff2", "number", title: "Auto Off Channel 2\n\nAutomatically turn switch off after this number of seconds\nRange: 0 to 32767", description: "Tap to set", required: false, range: "0..32767"
        input "ledIndicator", "enum", title: "LED Indicator\n\nTurn LED indicator on when switch is:\n", description: "Tap to set", required: false, options:[[0: "On"], [1: "Off"], [2: "Disable"]], defaultValue: 0
    }
    
}
def parse(String description) {
    def result = []
    def cmd = zwave.parse(description)
    if (cmd) {
        result += zwaveEvent(cmd)
        log.debug "Parsed ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd, ep = null) {
    log.debug "BasicReport ${cmd} - ep ${ep}"
    if (ep) {
        def event
        if(ep != 0) sendEvent(name: "switch${ep}", value: cmd.value ? "on" : "off")
        
        if (cmd.value) {
            event = [createEvent([name: "switch", value: "on"])]
        } else {
            def allOff = true
            if (device.currentState("switch1").value != "off") allOff = false
            if (device.currentState("switch2").value != "off") allOff = false
            if (allOff) {
                event = [createEvent([name: "switch", value: "off"])]
            } else {
                event = [createEvent([name: "switch", value: "on"])]
            }
        }
        return event
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
        if(ep != 0) sendEvent(name: "switch${ep}", value: cmd.value ? "on" : "off")
        if (cmd.value) {
            event = [createEvent([name: "switch", value: "on"])]
        } 
        else {
            def allOff = true
            if (device.currentState("switch1").value != "off") allOff = false
            if (device.currentState("switch2").value != "off") allOff = false
            if (allOff) {
                event = [createEvent([name: "switch", value: "off"])]
            } else {
                event = [createEvent([name: "switch", value: "on"])]
            }
        }
        return event
    } 
    else {
        def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
        def cmds = []
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
        return [result, response(commands(cmds))] // returns the result of reponse()
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

def zwaveEvent(hubitat.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    log.debug "Unhandled Event: ${cmd}"
}

def on() {
    log.debug "on()"
    commands([
            zwave.switchAllV1.switchAllOn(),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
    ])
}

def off() {
    log.debug "off()"
    commands([
            zwave.switchAllV1.switchAllOff(),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
    ])
}

def on1(){
    log.debug "Switch 1 On"
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[255]).format()
    //def cmds = []
    //cmds << new hubitat.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0xFF), 1)))
    //cmds << new hubitat.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), 1)))
    //sendHubCommand(cmds, 1000)
}

def on2(){
    log.debug "Switch 2 On"
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[255]).format()
    //def cmds = []
    //cmds << new hubitat.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0xFF), 2)))
    //cmds << new hubitat.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), 2)))
    //sendHubCommand(cmds, 1000)
}

def off1() {
    log.debug "Switch 1 Off"
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[0]).format()
    //def cmds = []
    //cmds << new hubitat.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0x00), 1)))
    //cmds << new hubitat.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), 1)))
    //sendHubCommand(cmds, 1000)
}

def off2() {
    log.debug "Switch 2 Off"
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[0]).format()
    //def cmds = []
    //cmds << new hubitat.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0x00), 2)))
    //cmds << new hubitat.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), 2)))
    //sendHubCommand(cmds, 1000)
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
    setSwitchDefaults()
}

def updated() {
    log.debug "updated()"
    sendEvent(name: "checkInterval", value: 3 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    sendEvent(name: "numberOfButtons", value: 1, displayed: true)
    def cmds = []
    cmds += zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId).format()
    cmds += zwave.associationV2.associationGet(groupingIdentifier:1).format()
    cmds += zwave.configurationV1.configurationSet(configurationValue: [ledIndicator? ledIndicator.toInteger() : 0], parameterNumber: 1, size: 1).format()
    cmds += zwave.configurationV1.configurationGet(parameterNumber: 1).format()
    cmds += zwave.configurationV1.configurationSet(scaledConfigurationValue: autoOff1? autoOff1.toInteger() : 0, parameterNumber: 2, size: 2).format()
    cmds += zwave.configurationV1.configurationGet(parameterNumber: 2).format()
    cmds += zwave.configurationV1.configurationSet(scaledConfigurationValue: autoOff2? autoOff2.toInteger() : 0, parameterNumber: 3, size: 2).format()
    cmds += zwave.configurationV1.configurationGet(parameterNumber: 3).format()
    if (cmds) return delayBetween(cmds, 500)
    //response(commands(cmds))
}

def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
}

def setSwitchDefaults() {
    sendEvent(name: "switch1", value: "off", displayed: true)
    sendEvent(name: "switch2", value: "off", displayed: true)
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
