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
 *  Forked and updated by bcopeland 1/2/2020 
 *    - removed turn on when set color temp
 *    - removed turn on when set color
 *  Modified with colorStaging preference to match official standards
 */

metadata {
	definition (name: "Inovelli Bulb Multi-Color LZW42 DDD", namespace: "djdizzyd", author: "InovelliUSA", importUrl: "https://raw.githubusercontent.com/djdizzyd/Hubitat-Inovelli/master/Drivers/inovelli-bulb-multi-color-lzw42.src/inovelli-bulb-multi-color-lzw42.groovy") {
		capability "Switch Level"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"
		capability "Configuration"

        fingerprint mfr: "031E", prod: "0005", model: "0001", deviceJoinName: "Inovelli Bulb Multi-Color"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x85,0x59,0x86,0x72,0x5A,0x33,0x26,0x70,0x27,0x98,0x73,0x7A"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x98,0x86,0x85,0x59,0x72,0x73,0x33,0x26,0x70,0x27,0x5A,0x7A" //Secure
	}
	preferences {
        	// added for official hubitat standards
        	input name: "colorStaging", type: "bool", description: "", title: "Enable color pre-staging", defaultValue: false
			input name: "logEnable", type: "bool", description: "", title: "Enable Debug Logging", defaultVaule: true
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
	log.warn "debug logging is: ${logEnable == true}"
	log.warn "color staging is: ${colorStaging == false}"
	if (logEnable) runIn(1800,logsOff)
	response(refresh())
}

def installed() {
	if (logEnable) log.debug "installed()..."
	state.colorReceived = [RED: null, GREEN: null, BLUE: null, WARM_WHITE: null, COLD_WHITE: null]
	sendEvent(name: "checkInterval", value: 1860, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "0"])
	sendEvent(name: "level", value: 100, unit: "%")
	sendEvent(name: "colorTemperature", value: COLOR_TEMP_MIN)
	sendEvent(name: "color", value: "#000000")
	sendEvent(name: "hue", value: 0)
	sendEvent(name: "saturation", value: 0)
}

def configure() {
	commands([
		// Set the dimming ramp rate
		zwave.configurationV2.configurationSet(parameterNumber: 0x10, size: 1, scaledConfigurationValue: 5)
	])
}

def parse(description) {
	def result = null
	if (description != "updated") {
        def cmd
        try {
		    cmd = zwave.parse(description)
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

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    if (logEnable) log.debug cmd
	unschedule(offlinePing)
	dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchcolorv3.SwitchColorReport cmd) {
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
		def hsv = rgbToHSV(*colors)
		result << createEvent(name: "hue", value: hsv.hue)
		result << createEvent(name: "saturation", value: hsv.saturation)
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
	commands([zwave.switchMultilevelV3.switchMultilevelGet()] + queryAllColors())
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
	setColor(saturation: percent)
}

def setHue(value) {
	if (logEnable) log.debug "setHue($value)"
	setColor(hue: value)
}

def setColor(value) {
	if (logEnable) log.debug "setColor($value)"
	def result = []
	if (value.hex) {
		def c = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
		result << zwave.switchColorV3.switchColorSet(red: c[0], green: c[1], blue: c[2], warmWhite: 0, coldWhite: 0)
	} else {
		def rgb = huesatToRGB(value.hue, value.saturation)
		result << zwave.switchColorV3.switchColorSet(red: rgb[0], green: rgb[1], blue: rgb[2], warmWhite:0, coldWhite:0)
	}
    if ((device.currentValue("switch") != "on") && (!colorStaging)) {
		if (logEnable) log.debug "Bulb is off. Turning on"
 		result << zwave.basicV1.basicSet(value: 0xFF)
	}
	commands(result) + "Delay 7000" + commands(queryAllColors(), 1000)
}

def setColorTemperature(temp) {
	if (logEnable) log.debug "setColorTemperature($temp)"
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
		cmds << zwave.switchMultiLevelV3.switchMultilevelGet()
	}
	commands(cmds) + "delay 7000" + commands(queryAllColors(), 1000)
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

def rgbToHSV(red, green, blue) {
	def hex = colorUtil.rgbToHex(red as int, green as int, blue as int)
	def hsv = colorUtil.hexToHsv(hex)
	return [hue: hsv[0], saturation: hsv[1], value: hsv[2]]
}

def huesatToRGB(hue, sat) {
	def color = colorUtil.hsvToHex(Math.round(hue) as int, Math.round(sat) as int)
	return colorUtil.hexToRgb(color)
}

def hexToRgb(colorHex) {
	def rrInt = Integer.parseInt(colorHex.substring(1,3),16)
    def ggInt = Integer.parseInt(colorHex.substring(3,5),16)
    def bbInt = Integer.parseInt(colorHex.substring(5,7),16)
    
    def colorData = [:]
    colorData = [r: rrInt, g: ggInt, b: bbInt]
    colorData
}

// huesatToRGB Changed method provided by daved314
def huesatToRGB(float hue, float sat) {
	if (hue <= 100) {
		hue = hue * 3.6
    }
    sat = sat / 100
    float v = 1.0
    float c = v * sat
    float x = c * (1 - Math.abs(((hue/60)%2) - 1))
    float m = v - c
    int mod_h = (int)(hue / 60)
    int cm = Math.round((c+m) * 255)
    int xm = Math.round((x+m) * 255)
    int zm = Math.round((0+m) * 255)
    switch(mod_h) {
    	case 0: return [cm, xm, zm]
       	case 1: return [xm, cm, zm]
        case 2: return [zm, cm, xm]
        case 3: return [zm, xm, cm]
        case 4: return [xm, zm, cm]
        case 5: return [cm, zm, xm]
	}   	
}

private rgbwToHSV(Map colorMap) {
    //log.debug "rgbwToHSV(): colorMap: ${colorMap}"

    if (colorMap.containsKey("r") & colorMap.containsKey("g") & colorMap.containsKey("b")) { 

        float r = colorMap.r / 255f
        float g = colorMap.g / 255f
        float b = colorMap.b / 255f
        float w = (colorMap.white) ? colorMap.white / 255f : 0.0
        float max = [r, g, b].max()
        float min = [r, g, b].min()
        float delta = max - min

        float h,s,v = 0

        if (delta) {
            s = delta / max
            if (r == max) {
                h = ((g - b) / delta) / 6
            } else if (g == max) {
                h = (2 + (b - r) / delta) / 6
            } else {
                h = (4 + (r - g) / delta) / 6
            }
            while (h < 0) h += 1
            while (h >= 1) h -= 1
        }

        v = [max,w].max() 

        return colorMap << [ hue: h * 100, saturation: s * 100, level: Math.round(v * 100) ]
    }
    else {
        log.error "rgbwToHSV(): Cannot obtain color information from colorMap: ${colorMap}"
    }
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
