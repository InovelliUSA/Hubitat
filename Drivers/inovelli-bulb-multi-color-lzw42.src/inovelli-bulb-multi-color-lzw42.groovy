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
 *  Inovelli Bulb Multi-Color LZW42
 *
 *  Author: Eric Maycock
 *  Date: 2019-9-9
 *  updated by bcopeland 1/7/2020 
 *		Added color pre-staging option
 *		Added power restored memory configuration
 *		Added debug logging configuration
 *		Fixed color setting 
 *		Fixed color temperature setting 
 *		Fixed reporting 
 *		Removed SmartThings related code
 *		Added importURL
 *		Added color name
 *	updated by bcopeland 1/9/2020
 *		added firmware version reporting
 *		fix for scene capture and level in setcolor
 *	updated by bcopeland 1/10/2020
 *		fix for hsl level from received color report
 */


metadata {
	definition (name: "Inovelli Bulb Multi-Color LZW42", namespace: "InovelliUSA", author: "InovelliUSA", importUrl: "https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-bulb-multi-color-lzw42.src/inovelli-bulb-multi-color-lzw42.groovy") {
		capability "Switch Level"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"
		capability "Configuration"
		capability "ColorMode"

		attribute "colorName", "string"
		attribute "firmware", "decimal"

        fingerprint mfr: "031E", prod: "0005", model: "0001", deviceJoinName: "Inovelli Bulb Multi-Color"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x85,0x59,0x86,0x72,0x5A,0x33,0x26,0x70,0x27,0x98,0x73,0x7A"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x98,0x86,0x85,0x59,0x72,0x73,0x33,0x26,0x70,0x27,0x5A,0x7A" //Secure
	}
	preferences {
        	// added for official hubitat standards
			input name: "colorStaging", type: "bool", description: "", title: "Enable color pre-staging", defaultValue: false
			input name: "logEnable", type: "bool", description: "", title: "Enable Debug Logging", defaultVaule: true
			input name: "bulbMemory", type: "enum", title: "Power outage state", options: [0:"Remembers Last State",1:"Bulb turns ON",2:"Bulb turns OFF"], defaultValue: 0
	}
	
}

private getCOLOR_TEMP_MIN() { 2700 }
private getCOLOR_TEMP_MAX() { 6500 }
private getWARM_WHITE_CONFIG() { 0x51 }
private getCOLD_WHITE_CONFIG() { 0x52 }
private getRED() { "red" }
private getGREEN() { "green" }
private getBLUE() { "blue" }
private getWARM_WHITE() { "warmWhite" }
private getCOLD_WHITE() { "coldWhite" }
private getRGB_NAMES() { [RED, GREEN, BLUE] }
private getWHITE_NAMES() { [WARM_WHITE, COLD_WHITE] }

def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def updated() {
	log.info "updated().."
	log.warn "debug logging is: ${logEnable}"
	log.warn "color staging is: ${colorStaging}"
	if (!state.powerStateMem) initializeVars()
	if (state.powerStateMem.toInteger() != bulbMemory.toInteger()) device.configure() 
	if (logEnable) runIn(1800,logsOff)
	response(refresh())
}

def installed() {
	if (logEnable) log.debug "installed()..."
	initializeVars()
	sendEvent(name: "checkInterval", value: 1860, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "0"])
	sendEvent(name: "level", value: 100, unit: "%")
	sendEvent(name: "colorTemperature", value: COLOR_TEMP_MIN)
	sendEvent(name: "color", value: "#000000")
	sendEvent(name: "hue", value: 0)
	sendEvent(name: "saturation", value: 0)
}

def initializeVars() {
	if (!state.colorReceived) state.colorReceived = [red: null, green: null, blue: null, warmWhite: null, coldWhite: null]
	if (!state.powerStateMem) state.powerStateMem=0
}

def configure() {
	def cmds = []
	cmds << zwave.configurationV1.configurationSet([scaledConfigurationValue: bulbMemory.toInteger(), parameterNumber: 2, size:1])
	cmds << zwave.configurationV2.configurationGet([parameterNumber: 2])
	commands(cmds)
}

def parse(description) {
	def result = null
	if (description != "updated") {
        def cmd
        try {
		    cmd = zwave.parse(description,[0x33:1,0x08:2,0x26:3])
        } catch (e) {
            //log.debug "An exception was caught $e"
        }
		if (cmd) {
			result = zwaveEvent(cmd)
			if (logEnable) log.debug("'$description' parsed to $result")
		} else {
			if (logEnable) log.debug("Couldn't zwave.parse '$description'")
		}
	}
	result
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
    if (logEnable) log.debug cmd
	dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.versionv1.VersionReport cmd) {
	if (logEnable) log.debug "got version report"
	BigDecimal fw = cmd.applicationVersion + (cmd.applicationSubVersion / 100)
	state.firmware = fw
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    if (logEnable) log.debug cmd
	unschedule(offlinePing)
	dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchcolorv1.SwitchColorReport cmd) {
	if (!state.colorReceived) initializeVars()
	if (logEnable) log.debug "got SwitchColorReport: $cmd"
	state.colorReceived[cmd.colorComponent] = cmd.value
	def result = []
	// Check if we got all the RGB color components
	if (RGB_NAMES.every { state.colorReceived[it] != null }) {
		def colors = RGB_NAMES.collect { state.colorReceived[it] }
		if (logEnable) log.debug "colors: $colors"
		// Send the color as hex format
		def hexColor = "#" + colors.collect { Integer.toHexString(it).padLeft(2, "0") }.join("")
		result << createEvent(name: "color", value: hexColor)
		// Send the color as hue and saturation
		def hsv = hubitat.helper.ColorUtils.rgbToHSV(colors)
		result << createEvent(name: "hue", value: hsv[0])
		result << createEvent(name: "saturation", value: hsv[1])
		result << createEvent(name: "level", value: hsv[2])
		if ((hsv[0] > 0) && (hsv[1] > 0)) setGenericName(hsv[0])
		// Reset the values
		RGB_NAMES.collect { state.colorReceived[it] = null}
	}
	// Check if we got all the color temperature values
	if (WHITE_NAMES.every { state.colorReceived[it] != null}) {
		def warmWhite = state.colorReceived[WARM_WHITE]
		def coldWhite = state.colorReceived[COLD_WHITE]
		if (logEnable) log.debug "warmWhite: $warmWhite, coldWhite: $coldWhite"
		if (warmWhite == 0 && coldWhite == 0) {
			result = createEvent(name: "colorTemperature", value: COLOR_TEMP_MIN)
		} else {
			def parameterNumber = warmWhite ? WARM_WHITE_CONFIG : COLD_WHITE_CONFIG
			result << response(command(zwave.configurationV2.configurationGet([parameterNumber: parameterNumber])))
		}
		// Reset the values
		WHITE_NAMES.collect { state.colorReceived[it] = null }
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

def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
	if (logEnable) log.debug "got ConfigurationReport: $cmd"
	def result = null
	if (cmd.parameterNumber == WARM_WHITE_CONFIG || cmd.parameterNumber == COLD_WHITE_CONFIG) {
		result = createEvent(name: "colorTemperature", value: cmd.scaledConfigurationValue)
		setGenericTempName(cmd.scaledConfigurationValue)
	}
		if (cmd.parameterNumber == 0x02) {
		state.powerStateMem = cmd.scaledConfigurationValue
	}
	result
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
	commands([zwave.switchMultilevelV3.switchMultilevelGet()] + queryAllColors() + zwave.versionV1.versionGet())
}

def ping() {
	if (logEnable) log.debug "ping().."
	unschedule(offlinePing)
	runEvery30Minutes(offlinePing)
	command(zwave.switchMultilevelV3.switchMultilevelGet())
}

def offlinePing() {
	if (logEnable) log.debug "offlinePing()..."
	sendHubCommand(new hubitat.device.HubAction(command(zwave.switchMultilevelV3.switchMultilevelGet())))
}

def setLevel(level) {
	setLevel(level, 1)
}

def setLevel(level, duration) {
	if (logEnable) log.debug "setLevel($level, $duration)"
	if(level > 99) level = 99
	commands([
		zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: duration),
		zwave.switchMultilevelV3.switchMultilevelGet(),
	], (duration && duration < 12) ? (duration * 1000) : 3500)
}

def setSaturation(percent) {
	if (logEnable) log.debug "setSaturation($percent)"
	setColor([saturation: percent, hue: device.currentValue("hue"), level: device.currentValue("level")])
}

def setHue(value) {
	if (logEnable) log.debug "setHue($value)"
	setColor([hue: value, saturation: 100, level: device.currentValue("level")])
}

def setColor(value) {
	if (value.hue == null || value.saturation == null) return
	if (logEnable) log.debug "setColor($value)"
	def result = []
	def rgb = hubitat.helper.ColorUtils.hsvToRGB([value.hue, value.saturation, value.level])
    log.debug "r:" + rgb[0] + ", g: " + rgb[1] +", b: " + rgb[2]
	result << zwave.switchColorV3.switchColorSet(red: rgb[0], green: rgb[1], blue: rgb[2], warmWhite:0, coldWhite:0)
	if ((device.currentValue("switch") != "on") && (!colorStaging)){
		if (logEnable) log.debug "Bulb is off. Turning on"
 		result << zwave.basicV1.basicSet(value: 0xFF)
        result << zwave.switchMultilevelV3.switchMultilevelGet()
	}
    commands(result+queryAllColors())
}

def setColorTemperature(temp) {
	if (logEnable) log.debug "setColorTemperature($temp)"
	if ((device.currentValue("colorMode") != "CT") || (device.currentValue("colorTemperature") != temp)) {
		def warmValue = temp < 5000 ? 255 : 0
		def coldValue = temp >= 5000 ? 255 : 0
		def parameterNumber = temp < 5000 ? WARM_WHITE_CONFIG : COLD_WHITE_CONFIG
		def cmds = []
		cmds << zwave.switchColorV3.switchColorSet(red: 0, green: 0, blue:0, warmWhite: warmValue, coldWhite: coldValue)
		if (temp < COLOR_TEMP_MIN) temp = 2700
		if (temp > COLOR_TEMP_MAX) temp = 6500
		cmds << zwave.configurationV1.configurationSet([scaledConfigurationValue: temp, parameterNumber: parameterNumber, size:2])
		if ((device.currentValue("switch") != "on") && (!colorStaging)) {
			if (logEnable) log.debug "Bulb is off. Turning on"
			cmds << zwave.basicV1.basicSet(value: 0xFF)
			cmds << zwave.switchMultilevelV3.switchMultilevelGet()
		}
		commands(cmds + queryAllColors())
        
	}
}

private queryAllColors() {
	def colors = WHITE_NAMES + RGB_NAMES
	colors.collect { zwave.switchColorV3.switchColorGet(colorComponent: it)}
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

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
    def temp = []
    if (cmd.nodeId != []) {
       cmd.nodeId.each {
          temp += it.toString().format( '%02x', it.toInteger() ).toUpperCase()
       }
    } 
    state."actualAssociation${cmd.groupingIdentifier}" = temp
    if (logEnable) log.debug "Associations for Group ${cmd.groupingIdentifier}: ${temp}"
    updateDataValue("associationGroup${cmd.groupingIdentifier}", "$temp")
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    sendEvent(name: "groups", value: cmd.supportedGroupings)
    if (logEnable) log.debug "Supported association groups: ${cmd.supportedGroupings}"
    state.associationGroups = cmd.supportedGroupings
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
   if (state.associationGroups) {
       associationGroups = state.associationGroups
   } else {
       if (logEnable) log.debug "Getting supported association groups from device"
       cmds <<  zwave.associationV2.associationGroupingsGet().format()
   }
   for (int i = 1; i <= associationGroups; i++){
      if(state."actualAssociation${i}" != null){
         if(state."desiredAssociation${i}" != null || state."defaultG${i}") {
            def refreshGroup = false
            ((state."desiredAssociation${i}"? state."desiredAssociation${i}" : [] + state."defaultG${i}") - state."actualAssociation${i}").each {
                if (logEnable) log.debug "Adding node $it to group $i"
                cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:Integer.parseInt(it,16)).format()
                refreshGroup = true
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                if (logEnable) log.debug "Removing node $it from group $i"
                cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:Integer.parseInt(it,16)).format()
                refreshGroup = true
            }
            if (refreshGroup == true) cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
            else log.debug "There are no association actions to complete for group $i"
         }
      } else {
         log.debug "Association info not known for group $i. Requesting info from device."
         cmds << zwave.associationV2.associationGet(groupingIdentifier:i).format()
      }
   }
   return cmds
}


def setGenericTempName(temp){
    if (!temp) return
    def genericName
    def value = temp.toInteger()
    if (value <= 2000) genericName = "Sodium"
    else if (value <= 2100) genericName = "Starlight"
    else if (value < 2400) genericName = "Sunrise"
    else if (value < 2800) genericName = "Incandescent"
    else if (value < 3300) genericName = "Soft White"
    else if (value < 3500) genericName = "Warm White"
    else if (value < 4150) genericName = "Moonlight"
    else if (value <= 5000) genericName = "Horizon"
    else if (value < 5500) genericName = "Daylight"
    else if (value < 6000) genericName = "Electronic"
    else if (value <= 6500) genericName = "Skylight"
    else if (value < 20000) genericName = "Polar"
    def descriptionText = "${device.getDisplayName()} color is ${genericName}"
    if (txtEnable) log.info "${descriptionText}"
	sendEvent(name: "colorMode", value: "CT", descriptionText: "${device.getDisplayName()} color mode is CT")
    sendEvent(name: "colorName", value: genericName ,descriptionText: descriptionText)
}



def setGenericName(hue){
    def colorName
    hue = hue.toInteger()
    hue = (hue * 3.6)
    switch (hue.toInteger()){
        case 0..15: colorName = "Red"
            break
        case 16..45: colorName = "Orange"
            break
        case 46..75: colorName = "Yellow"
            break
        case 76..105: colorName = "Chartreuse"
            break
        case 106..135: colorName = "Green"
            break
        case 136..165: colorName = "Spring"
            break
        case 166..195: colorName = "Cyan"
            break
        case 196..225: colorName = "Azure"
            break
        case 226..255: colorName = "Blue"
            break
        case 256..285: colorName = "Violet"
            break
        case 286..315: colorName = "Magenta"
            break
        case 316..345: colorName = "Rose"
            break
        case 346..360: colorName = "Red"
            break
    }
    def descriptionText = "${device.getDisplayName()} color is ${colorName}"
    if (txtEnable) log.info "${descriptionText}"
	sendEvent(name: "colorMode", value: "RGB", descriptionText: "${device.getDisplayName()} color mode is RGB")
    sendEvent(name: "colorName", value: colorName ,descriptionText: descriptionText)
}