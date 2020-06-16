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
 *  updated by bcopeland 1/21/2020
 *		fixes for reported bugs
 *		correct comand class versions to match what the hardware supports
 *		add z-wave color component ids manually as it didnt seem to match in correct command class version from he
 *  updated by bcopeland 2/6/2020
 *      added ChangeLevel capability and relevant commands
 *  updated by bcopeland 2/15/2020
 *		dramatically improved speed of CT operations and reduced packet count - Make sure to hit configure after updating.
 *		improved speed of on/off events also reducing packets
 *		improved speed of setLevel events also reducing packets
 *		bug fix for null value in setColor
 *	updated by bcopeland 3/11/2020
 *		improved speed / reduced packets on CT set operations
 *		added color fade time preference for smoother CT transitions
 *	update by bcopeland 4/9/2020
 *      major re-write for new coding standards / cleanup
 *      stabilization of color temp and color reporting
 *      re-organization of device data for standardization / addition of serialnumber, hardware ver, protocol ver, firmware
 *      re-work of associations
 *	updated by npk22 4/9/2020
 *		added dimming speed parameter
 *		added dimming speed to on / off
 *	updated by bcopeland 4/11/2020
 *  	fixed type definitions
 *  	fixed fingerprint
 *  updated by bcopeland 4/12/2020
 *  	added duplicate event filtering (optional as it has a slight possibility of causing issues with voice assistants)
 *  	changed dimming speed default to 1 to match previous default functionality
 *  updated by InovelliUSA 4/15/2020
 *  	corrected incorrect options for parameter 2
 *  updated by bcopeland 4/15/2020
 *  	fixed bug in CT report
 *    	added gamma correction as an optional setting
 *  updated by bcopeland 4/16/2020
 *      updated ambiguous language
 *
 */

import groovy.transform.Field

metadata {
	definition (name: "Inovelli Bulb Multi-Color LZW42", namespace: "InovelliUSA", author: "InovelliUSA", importUrl: "https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-bulb-multi-color-lzw42.src/inovelli-bulb-multi-color-lzw42.groovy") {
		capability "SwitchLevel"
		capability "ColorTemperature"
		capability "ColorControl"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Configuration"
		capability "ChangeLevel"
		capability "ColorMode"

		attribute "colorName", "string"
		attribute "firmware", "String"

		fingerprint  mfr:"031E", prod:"0005", deviceId:"0001", inClusters:"0x5E,0x85,0x59,0x86,0x72,0x5A,0x33,0x26,0x70,0x27,0x98,0x73,0x7A", deviceJoinName: "Inovelli Bulb Multi-Color"

	}
	preferences {
		configParams.each { input it.value.input }
		input name: "colorStaging", type: "bool", description: "", title: "Enable color pre-staging", defaultValue: false
		input name: "colorTransition", type: "number", description: "", title: "Color fade time:", defaultValue: 0
		input name: "dimmingSpeed", type: "number", description: "", title: "Dimming speed:", defaultValue: 1
		input name: "eventFilter", type: "bool", title: "Filter out duplicate events", defaultValue: false
		input name: "enableGammaCorrect", type: "bool", description: "May cause a slight difference in reported color", title: "Enable gamma correction on setColor", defaultValue: false
		input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
	}
}
@Field static Map configParams = [
		2: [input: [name: "configParam2", type: "enum", title: "Power fail load state restore", description: "", defaultValue: 0, options: [0:"Bulb turns ON",1:"Bulb remembers last state"]], parameterSize: 1]
]
@Field static Map CMD_CLASS_VERS=[0x33:2,0x26:2,0x86:2,0x70:1]
@Field static int COLOR_TEMP_MIN=2700
@Field static int COLOR_TEMP_MAX=6500
@Field static int WARM_WHITE_CONFIG=0x51
@Field static int COLD_WHITE_CONFIG=0x52
@Field static String RED="red"
@Field static String GREEN="green"
@Field static String BLUE="blue"
@Field static String WARM_WHITE="warmWhite"
@Field static String COLD_WHITE="coldWhite"
@Field static Map ZWAVE_COLOR_COMPONENT_ID=[warmWhite: 0, coldWhite: 1, red: 2, green: 3, blue: 4]
@Field static List<String> RGB_NAMES=["red", "green", "blue"]
@Field static List<String> WHITE_NAMES=["warmWhite", "coldWhite"]
private getCOLOR_TEMP_DIFF() { COLOR_TEMP_MAX - COLOR_TEMP_MIN }

void logsOff(){
	log.warn "debug logging disabled..."
	device.updateSetting("logEnable",[value:"false",type:"bool"])
}

void configure() {
	if (!state.initialized) initializeVars()
	runIn(5,pollDeviceData)
}

void initializeVars() {
	// first run only
	state.colorReceived=[red: null, green: null, blue: null, warmWhite: null, coldWhite: null]
	state.initialized=true
	runIn(5, refresh)
}

void updated() {
	log.info "updated..."
	log.warn "debug logging is: ${logEnable == true}"
	unschedule()
	if (logEnable) runIn(1800,logsOff)
	runConfigs()
}

void runConfigs() {
	List<hubitat.zwave.Command> cmds=[]
	configParams.each { param, data ->
		if (settings[data.input.name]) {
			cmds.addAll(configCmd(param, data.parameterSize, settings[data.input.name]))
		}
	}
	cmds.add(zwave.versionV2.versionGet())
	sendToDevice(cmds)
}

List<hubitat.zwave.Command> configCmd(parameterNumber, size, scaledConfigurationValue) {
	List<hubitat.zwave.Command> cmds = []
	cmds.add(zwave.configurationV1.configurationSet(parameterNumber: parameterNumber.toInteger(), size: size.toInteger(), scaledConfigurationValue: scaledConfigurationValue.toInteger()))
	cmds.add(zwave.configurationV1.configurationGet(parameterNumber: parameterNumber.toInteger()))
	return cmds
}

void zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
	if(configParams[cmd.parameterNumber.toInteger()]) {
		Map configParam=configParams[cmd.parameterNumber.toInteger()]
		int scaledValue
		cmd.configurationValue.reverse().eachWithIndex { v, index -> scaledValue=scaledValue | v << (8*index) }
		device.updateSetting(configParam.input.name, [value: "${scaledValue}", type: configParam.input.type])
	}
}

void pollDeviceData() {
	List<hubitat.zwave.Command> cmds = []
	cmds.add(zwave.versionV2.versionGet())
	cmds.add(zwave.manufacturerSpecificV2.deviceSpecificGet(deviceIdType: 1))
	cmds.add(zwave.configurationV1.configurationGet(parameterNumber: 2))
	cmds.addAll(configCmd(51,2,1387))
	cmds.addAll(configCmd(52,2,6500))
	cmds.addAll(processAssociations())
	sendToDevice(cmds)
}

void refresh() {
	List<hubitat.zwave.Command> cmds=[]
	cmds.add(zwave.switchMultilevelV2.switchMultilevelGet())
	cmds.add(zwave.basicV1.basicGet())
	cmds.addAll(queryAllColors())
	sendToDevice(cmds)
}

private void refreshColor() {
	sendToDevice(queryAllColors())
}

void installed() {
	if (logEnable) log.debug "installed()..."
	sendEvent(name: "level", value: 100, unit: "%")
	sendEvent(name: "colorTemperature", value: 2700)
	sendEvent(name: "color", value: "#000000")
	sendEvent(name: "hue", value: 0)
	sendEvent(name: "saturation", value: 0)
	initializeVars()
}

private List<hubitat.zwave.Command> queryAllColors() {
	List<hubitat.zwave.Command> cmds=[]
	WHITE_NAMES.each { cmds.add(zwave.switchColorV2.switchColorGet(colorComponent: it, colorComponentId: ZWAVE_COLOR_COMPONENT_ID[it])) }
	RGB_NAMES.each { cmds.add(zwave.switchColorV2.switchColorGet(colorComponent: it, colorComponentId: ZWAVE_COLOR_COMPONENT_ID[it])) }
	return cmds
}

void eventProcess(Map evt) {
	if (device.currentValue(evt.name).toString() != evt.value.toString() || !eventFilter) {
		evt.isStateChange=true
		sendEvent(evt)
	}
}

void startLevelChange(direction) {
	boolean upDownVal = direction == "down" ? true : false
	if (logEnable) log.debug "got startLevelChange(${direction})"
	sendToDevice(zwave.switchMultilevelV2.switchMultilevelStartLevelChange(ignoreStartLevel: true, startLevel: device.currentValue("level"), upDown: upDownVal))
}

void stopLevelChange() {
	sendToDevice(zwave.switchMultilevelV2.switchMultilevelStopLevelChange())
}

void zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
	if (logEnable) log.debug cmd
	dimmerEvents(cmd)
}

void zwaveEvent(hubitat.zwave.commands.switchmultilevelv2.SwitchMultilevelReport cmd) {
	if (logEnable) log.debug cmd
	dimmerEvents(cmd)
}

void zwaveEvent(hubitat.zwave.commands.switchcolorv2.SwitchColorReport cmd) {
	if (logEnable) log.debug "got SwitchColorReport: $cmd"
	if (!state.colorReceived) initializeVars()
	state.colorReceived[cmd.colorComponent] = cmd.value
	if (RGB_NAMES.every { state.colorReceived[it] != null } && device.currentValue("colorMode")=="RGB") {
		List<Integer> colors = RGB_NAMES.collect { state.colorReceived[it] }
		if (logEnable) log.debug "colors: $colors"
		// Send the color as hex format
		String hexColor = "#" + colors.collect { Integer.toHexString(it).padLeft(2, "0") }.join("")
		eventProcess(name: "color", value: hexColor)
		// Send the color as hue and saturation
		List hsv = hubitat.helper.ColorUtils.rgbToHSV(colors)
		eventProcess(name: "hue", value: hsv[0].round())
		eventProcess(name: "saturation", value: hsv[1].round())

		if ((hsv[0] > 0) && (hsv[1] > 0)) {
			setGenericName(hsv[0])
			eventProcess(name: "level", value: hsv[2].round())
		}
	} else if (WHITE_NAMES.every { state.colorReceived[it] != null} && device.currentValue("colorMode")=="CT") {
		int warmWhite = state.colorReceived[WARM_WHITE]
		int coldWhite = state.colorReceived[COLD_WHITE]
		if (logEnable) log.debug "warmWhite: $warmWhite, coldWhite: $coldWhite"
		if (warmWhite == 0 && coldWhite == 0) {
			eventProcess(name: "colorTemperature", value: COLOR_TEMP_MIN)
		} else {
			int colorTemp = COLOR_TEMP_MIN + (COLOR_TEMP_DIFF / 2)
			if (warmWhite != coldWhite) {
				colorTemp = (COLOR_TEMP_MAX - (COLOR_TEMP_DIFF * warmWhite) / 255) as Integer
			}
			eventProcess(name: "colorTemperature", value: colorTemp)
			setGenericTempName(colorTemp)
		}
	}
}

private void dimmerEvents(hubitat.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	eventProcess(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
	if (cmd.value) {
		eventProcess(name: "level", value: cmd.value == 99 ? 100 : cmd.value , unit: "%")
	}
}

void on() {
	//Check if dimming speed exists and set the duration
	int duration=0
	if (dimmingSpeed) duration=dimmingSpeed.toInteger()
	sendToDevice(zwave.switchMultilevelV2.switchMultilevelSet(value: 0xFF, dimmingDuration: duration))
}

void off() {
	//Check if dimming speed exists and set the duration
	int duration=0
	if (dimmingSpeed) duration=dimmingSpeed.toInteger()
	sendToDevice(zwave.switchMultilevelV2.switchMultilevelSet(value: 0x00, dimmingDuration: duration))
}

void setLevel(level) {
	//Check if dimming speed exists and set the duration
	int duration=1
	if (dimmingSpeed) duration=dimmingSpeed.toInteger()
	setLevel(level, duration)
}

void setLevel(level, duration) {
	if (logEnable) log.debug "setLevel($level, $duration)"
	if(level > 99) level = 99
	sendToDevice(zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: duration))
}

void setSaturation(percent) {
	if (logEnable) log.debug "setSaturation($percent)"
	setColor([saturation: percent, hue: device.currentValue("hue"), level: device.currentValue("level")])
}

void setHue(value) {
	if (logEnable) log.debug "setHue($value)"
	setColor([hue: value, saturation: 100, level: device.currentValue("level")])
}

void setColor(value) {
	if (value.hue == null || value.saturation == null) return
	if (value.level == null) value.level=100
	if (logEnable) log.debug "setColor($value)"
	int dimmingDuration=0
	if (colorTransition) dimmingDuration=colorTransition
	List<hubitat.zwave.Command> cmds = []
	List rgb = hubitat.helper.ColorUtils.hsvToRGB([value.hue, value.saturation, value.level])
	log.debug "r:" + rgb[0] + ", g: " + rgb[1] +", b: " + rgb[2]
	if (enableGammaCorrect) {
		cmds.add(zwave.switchColorV3.switchColorSet(red: gammaCorrect(rgb[0]), green: gammaCorrect(rgb[1]), blue: gammaCorrect(rgb[2]), warmWhite: 0, coldWhite: 0, dimmingDuration: dimmingDuration))
	} else {
		cmds.add(zwave.switchColorV2.switchColorSet(red: rgb[0], green: rgb[1], blue: rgb[2], warmWhite: 0, coldWhite: 0, dimmingDuration: dimmingDuration))
	}
	if ((device.currentValue("switch") != "on") && (!colorStaging)){
		if (logEnable) log.debug "Bulb is off. Turning on"
		cmds.add(zwave.basicV1.basicSet(value: 0xFF))
	}
	sendToDevice(cmds)
	eventProcess(name: "colorMode", value: "RGB", descriptionText: "${device.getDisplayName()} color mode is RGB")
	runIn(dimmingDuration, "refreshColor")
}

void setColorTemperature(temp) {
	if (logEnable) log.debug "setColorTemperature($temp)"
	int dimmingDuration=0
	if (colorTransition) dimmingDuration=colorTransition
	List<hubitat.zwave.Command> cmds = []
	if (temp < COLOR_TEMP_MIN) temp = COLOR_TEMP_MIN
	if (temp > COLOR_TEMP_MAX) temp = COLOR_TEMP_MAX
	int warmValue = ((COLOR_TEMP_MAX - temp) / COLOR_TEMP_DIFF * 255) as Integer
	int coldValue = 255 - warmValue
	cmds.add(zwave.switchColorV2.switchColorSet(warmWhite: warmValue, coldWhite: coldValue, dimmingDuration: dimmingDuration))
	if ((device.currentValue("switch") != "on") && (!colorStaging)) {
		if (logEnable) log.debug "Bulb is off. Turning on"
		cmds.add(zwave.basicV1.basicSet(value: 0xFF))
	}
	sendToDevice(cmds)
	eventProcess(name: "colorMode", value: "CT", descriptionText: "${device.getDisplayName()} color mode is CT")
	runIn(dimmingDuration, "refreshColor")
}

private void setGenericTempName(temp){
	if (!temp) return
	String genericName
	int value = temp.toInteger()
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
	String descriptionText = "${device.getDisplayName()} color is ${genericName}"
	eventProcess(name: "colorName", value: genericName ,descriptionText: descriptionText)
}

private void setGenericName(hue){
	String colorName
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
	String descriptionText = "${device.getDisplayName()} color is ${colorName}"
	eventProcess(name: "colorName", value: colorName ,descriptionText: descriptionText)
}

void zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(CMD_CLASS_VERS)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

void parse(String description) {
	if (logEnable) log.debug "parse:${description}"
	hubitat.zwave.Command cmd = zwave.parse(description, CMD_CLASS_VERS)
	if (cmd) {
		zwaveEvent(cmd)
	}
}

void zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd) {
	if (logEnable) log.debug "Supervision get: ${cmd}"
	hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(CMD_CLASS_VERS)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
	sendToDevice(new hubitat.zwave.commands.supervisionv1.SupervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0))
}

void zwaveEvent(hubitat.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
	if (logEnable) log.debug "Device Specific Report: ${cmd}"
	switch (cmd.deviceIdType) {
		case 1:
			// serial number
			def serialNumber=""
			if (cmd.deviceIdDataFormat==1) {
				cmd.deviceIdData.each { serialNumber += hubitat.helper.HexUtils.integerToHexString(it & 0xff,1).padLeft(2, '0')}
			} else {
				cmd.deviceIdData.each { serialNumber += (char) it }
			}
			device.updateDataValue("serialNumber", serialNumber)
			break
	}
}

void zwaveEvent(hubitat.zwave.commands.versionv2.VersionReport cmd) {
	if (logEnable) log.debug "version report: ${cmd}"
	device.updateDataValue("firmwareVersion", "${cmd.firmware0Version}.${cmd.firmware0SubVersion}")
	device.updateDataValue("protocolVersion", "${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}")
	device.updateDataValue("hardwareVersion", "${cmd.hardwareVersion}")
	sendEvent(name: "firmware", value: "${cmd.firmware0Version}.${cmd.firmware0SubVersion.toString().padLeft(2,'0')}")
}

void sendToDevice(List<hubitat.zwave.Command> cmds) {
	sendHubCommand(new hubitat.device.HubMultiAction(commands(cmds), hubitat.device.Protocol.ZWAVE))
}

void sendToDevice(hubitat.zwave.Command cmd) {
	sendHubCommand(new hubitat.device.HubAction(secureCommand(cmd), hubitat.device.Protocol.ZWAVE))
}

void sendToDevice(String cmd) {
	sendHubCommand(new hubitat.device.HubAction(secureCommand(cmd), hubitat.device.Protocol.ZWAVE))
}

List<String> commands(List<hubitat.zwave.Command> cmds, Long delay=200) {
	return delayBetween(cmds.collect{ secureCommand(it) }, delay)
}

String secureCommand(hubitat.zwave.Command cmd) {
	secureCommand(cmd.format())
}

String secureCommand(String cmd) {
	String encap=""
	if (getDataValue("zwaveSecurePairingComplete") != "true") {
		return cmd
	} else {
		encap = "988100"
	}
	return "${encap}${cmd}"
}

void zwaveEvent(hubitat.zwave.Command cmd) {
	if (logEnable) log.debug "skip:${cmd}"
}

List<hubitat.zwave.Command> setDefaultAssociation() {
	List<hubitat.zwave.Command> cmds=[]
	cmds.add(zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId))
	cmds.add(zwave.associationV2.associationGet(groupingIdentifier: 1))
	return cmds
}

List<hubitat.zwave.Command> processAssociations(){
	List<hubitat.zwave.Command> cmds = []
	cmds.addAll(setDefaultAssociation())
	return cmds
}

void zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
	if (logEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
	List<String> temp = []
	if (cmd.nodeId != []) {
		cmd.nodeId.each {
			temp.add(it.toString().format( '%02x', it.toInteger() ).toUpperCase())
		}
	}
	updateDataValue("zwaveAssociationG${cmd.groupingIdentifier}", "$temp")
}

void zwaveEvent(hubitat.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
	if (logEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
	log.info "${device.label?device.label:device.name}: Supported association groups: ${cmd.supportedGroupings}"
	state.associationGroups = cmd.supportedGroupings
}

private int gammaCorrect(value) {
	def temp=value/255
	def correctedValue=(temp>0.4045) ? Math.pow((temp+0.055)/ 1.055, 2.4) : (temp / 12.92)
	return Math.round(correctedValue * 255) as Integer
}
