/**
 *  Copyright 2019 Inovelli / Eric Maycock
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
 *  Inovelli Bulb Multi-White LZW41
 *
 *  Author: Eric Maycock
 *  Date: 2019-9-9
 *  Forked and updated by bcopeland 1/2/2020 
 *    - removed turn on when set color temp
 *  Modified to allow for colorStaging preference per official standards
 */

metadata {
	definition (name: "Inovelli Bulb Multi-White LZW41 DDD", namespace: "djdizzyd", author: "InovelliUSA") {
		capability "Switch Level"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"
        
        fingerprint mfr: "0300", prod: "0006", model: "0001", deviceJoinName: "Inovelli Bulb Multi-White" //US
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x85,0x59,0x86,0x72,0x5A,0x26,0x33,0x27,0x70,0x7A,0x73,0x98,0x7A"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x98,0x86,0x85,0x59,0x72,0x73,0x26,0x33,0x70,0x27,0x5A,0x7A" // Secure
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x85,0x59,0x86,0x72,0x5A,0x26,0x33,0x27,0x70,0x73,0x98,0x7A"
	}
	preferences {
		// added for official hubitat standards
		input name: "colorStaging", type: "bool", description: "", title: "Enable color pre-staging", defaultValue: false
        input name: "debugLogging", type: "bool", description: "", title: "Enable Debug Logging", defaultVaule: false
    	}
}

private getWARM_WHITE_CONFIG() { 0x51 }
private getCOLD_WHITE_CONFIG() { 0x52 }
private getWARM_WHITE() { "warmWhite" }
private getCOLD_WHITE() { "coldWhite" }
private getWHITE_NAMES() { [WARM_WHITE, COLD_WHITE] }

def updated() {
	if (debugLogging) log.debug "updated().."
	response(refresh())
}

def installed() {
	if (debugLogging) log.debug "installed()..."
	sendEvent(name: "checkInterval", value: 1860, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "0"])
	sendEvent(name: "level", value: 100, unit: "%")
	sendEvent(name: "colorTemperature", value: 2700)
}

def parse(description) {
	def result = null
	if (description != "updated") {
        if (debugLogging) log.debug("description: $description")
		def cmd = zwave.parse(description,[0x33:1,0x08:2,0x26:3])
		if (cmd) {
			result = zwaveEvent(cmd)
			if(debugLogging) log.debug("'$description' parsed to $result")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
	result
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
    if (debugLogging) log.debug cmd
	dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    if (debugLogging) log.debug cmd
	unschedule(offlinePing)
	dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchcolorv1.SwitchColorReport cmd) {
	if (debugLogging) log.debug "got SwitchColorReport: $cmd"
	def result = []
	if (cmd.value == 255) {
		def parameterNumber = (cmd.colorComponent == WARM_WHITE) ? WARM_WHITE_CONFIG : COLD_WHITE_CONFIG
		result << response(command(zwave.configurationV2.configurationGet([parameterNumber: parameterNumber])))
	}
	result
}



private dimmerEvents(hubitat.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")]
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value , unit: "%")
	}
	return result
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

/*def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "got ConfigurationReport: $cmd"
	def result = null
	if (cmd.parameterNumber == WARM_WHITE_CONFIG || cmd.parameterNumber == COLD_WHITE_CONFIG)
		result = createEvent(name: "colorTemperature", value: cmd.scaledConfigurationValue)
	result
}*/

def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
    //log.debug cmd
    if (debugLogging) log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
    if (cmd.parameterNumber == 81 || cmd.parameterNumber == 82) {
       // log.debug "Got parameter: " + cmd.parameterNumber
        sendEvent(name: "colorTemperature", value: cmd.scaledConfigurationValue)
    }
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

def zwaveEvent(hubitat.zwave.Command cmd) {
	def linkText = device.label ?: device.name
	[linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}

def buildOffOnEvent(cmd){
	[zwave.basicV1.basicSet(value: cmd), zwave.switchMultilevelV3.switchMultilevelGet()]
}

def on() {
	commands(buildOffOnEvent(0xFF), 5000)
}

def off() {
	commands(buildOffOnEvent(0x00), 5000)
}

def refresh() {
	//commands([zwave.switchMultilevelV3.switchMultilevelGet()] + queryAllColors(), 500)
    //def commands = []
    def commands = processAssociations()
    //commands << zwave.configurationV1.configurationSet(parameterNumber: 80, scaledConfigurationValue: 1, size: 1).format()

    //[2,80,81,82].each { i ->
    //    commands << zwave.configurationV1.configurationGet(parameterNumber: i).format()
    //}
    delayBetween(commands,1000)
}

def ping() {
	if (debugLogging) log.debug "ping().."
	unschedule(offlinePing)
	runEvery30Minutes(offlinePing)
	command(zwave.switchMultilevelV3.switchMultilevelGet())
}

def offlinePing() {
	if (debugLogging) log.debug "offlinePing()..."
	sendHubCommand(new hubitat.device.HubAction(command(zwave.switchMultilevelV3.switchMultilevelGet())))
}

def setLevel(level) {
	setLevel(level, 1)
}

def setLevel(level, duration) {
	if (debugLogging) log.debug "setLevel($level, $duration)"
	if(level > 99) level = 99
	commands([
		zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: duration),
		zwave.switchMultilevelV3.switchMultilevelGet(),
	], (duration && duration < 12) ? (duration * 1000) : 3500)
}

def setColorTemperature(temp) {
	if (debugLogging) log.debug "setColorTemperature($temp)"
	def warmValue = temp < 5000 ? 255 : 0
	def coldValue = temp >= 5000 ? 255 : 0
	def parameterNumber = temp < 5000 ? WARM_WHITE_CONFIG : COLD_WHITE_CONFIG
	def cmds = []
    cmds << zwave.switchColorV3.switchColorSet(warmWhite: warmValue, coldWhite: coldValue)
    if (temp < 2700) {
        // min is 2700
        temp = 2700
    }
    if (temp > 6500) {
         // max is 6500
         temp = 6500
    }
    if ((temp < 5000) && (temp >= 2700)) {   
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: temp, parameterNumber: 81, size:2)
    }
    if ((temp >=5000) && (temp <= 6500)) {
         cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: temp, parameterNumber: 82, size:2)   
    }

    if ((device.currentValue("switch") != "on") && (!colorStaging)) {
		if (debugLogging) log.debug "Bulb is off. Turning on"
		cmds << zwave.basicV1.basicSet(value: 0xFF)
	}
	commands(cmds) + "delay 4000" + commands(queryAllColors(), 500) + commands(zwave.configurationV2.configurationGet([parameterNumber: parameterNumber]))
}

private queryAllColors() {
	[zwave.basicV1.basicGet()] /*+ WHITE_NAMES.collect { zwave.switchColorV3.switchColorGet(colorComponentId: it) }*/
}

private secEncap(hubitat.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(hubitat.zwave.Command cmd) {
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private command(hubitat.zwave.Command cmd) {
	if (getDataValue("zwaveSecurePairingComplete") == "true") {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
		return cmd.format()
    }	
}

private commands(commands, delay=200) {
	delayBetween(commands.collect{ command(it) }, delay)
}

def setDefaultAssociations() {
    def hubitatHubID = zwaveHubNodeId.toString().format( '%02x', zwaveHubNodeId )
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
   if (debugLogging) log.debug state.associationGroups
   if (state.associationGroups) {
       associationGroups = state.associationGroups
   } else {
       if (debugLogging) log.debug "Getting supported association groups from device"
       cmds <<  zwave.associationV2.associationGroupingsGet().format()
   }
   for (int i = 1; i <= associationGroups; i++){
      if(state."actualAssociation${i}" != null){
         if(state."desiredAssociation${i}" != null || state."defaultG${i}") {
            def refreshGroup = false
            ((state."desiredAssociation${i}"? state."desiredAssociation${i}" : [] + state."defaultG${i}") - state."actualAssociation${i}").each {
                if (debugLogging) log.debug "Adding node $it to group $i"
                cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:Integer.parseInt(it,16)).format()
                refreshGroup = true
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                if (debugLogging) log.debug "Removing node $it from group $i"
                cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:Integer.parseInt(it,16)).format()
                refreshGroup = true
            }
            if (refreshGroup == true) cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
            else log.debug "There are no association actions to complete for group $i"
         }
      } else {
         if (debugLogging) log.debug "Association info not known for group $i. Requesting info from device."
         cmds << zwave.associationV2.associationGet(groupingIdentifier:i).format()
      }
   }
   return cmds
}
