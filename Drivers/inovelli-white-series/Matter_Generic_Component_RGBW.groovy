/*
Copyright James V. Mahon
Distribution requires permission / Not for Commercial distribution
*/
metadata {
    definition(name: "Matter Generic Component RGBW", namespace: "matterTools", author: "jvm", component: true) {
        // capability "Actuator"
        capability "Bulb"
        capability "Refresh"
        capability "Switch"
		capability "SwitchLevel"
        capability "ChangeLevel"
        capability "ColorControl"
        capability "ColorMode"
        capability "ColorTemperature"
        
        command "onFor", [[name: "Remain on for (seconds)*", type:"NUMBER", description:"Turn the device on, then off after this many seconds"]]
        command "setLevel"  , [[name: "Level*", type:"NUMBER", description:"Level to set (0 to 100)"],
                               [name: "Duration", type:"NUMBER", description:"Transition duration in seconds"], 
                               [name: "Remain on for (seconds)", type:"NUMBER", description:"Turn off the device after the specified number of seconds"]
                              ]

        command "toggleOnOff" 
   
        // Identify Cluster
        attribute "IdentifyTime", "number"
        attribute "IdentifyType", "string"
        
        // Switch Cluster
        attribute "OnTime", "number"
        attribute "OffWaitTime", "number"
        attribute "StartUpOnOff", "string"
        
        // Level Cluster
        attribute "OnOffTransitionTime", "number"
        attribute "OnTransitionTime", "number"
        attribute "OffTransitionTime", "number"

        attribute "RemainingTime", "number"
        attribute "MinLevel", "number"
        attribute "MaxLevel", "number"
        attribute "OnLevel", "number"
        attribute "DefaultMoveRate", "number"
        attribute "StartUpOnOff", "string"
        
        // Color Cluster
        attribute "colorCapabilities", "string"
        attribute "ColorTemperatureMinKelvin", "number"
        attribute "ColorTemperatureMaxKelvin", "number"
    }
    preferences {
        input(name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: false)
    }
}


import groovy.transform.Field
import hubitat.helper.HexUtils
import hubitat.matter.DataType

@Field static Map StartUpOnOffEnum = [ 0:"Off", 1:"On", 2:"Toggle"]

void installed() {
    log.info "Installed..."
    device.updateSetting("txtEnable",[type:"bool",value:false])
    refresh()
}

// This parse routine handles Hubitat SendEvent type messages (not the description raw strings originating from the device). 
// Hubitat's convntion is to include a parse() routine with this function in Generic Component drivers (child device drivers).
// It accepts a List of one or more SendEvent-type Maps and operates to determine how those Hubitat sendEvent Maps should be handled.
// The List of SendEvent Maps may include event Maps that are not needed by a particular driver (as determined based on the attributes of the driver)
// and those "extra" Maps are discarded. This allows a more generic "event Map" producting method (e.g., matterTools.createListOfMatterSendEventMaps)
void parse(List sendEventTypeOfEvents) {
    try {
		List updateLocalStateOnlyAttributes = ["OnOffTransitionTime", "OnTransitionTime", "OffTransitionTime", 
											   "ColorCapabilities","ColorTemperatureMinKelvin", "ColorTemperatureMaxKelvin", 
											   "MinLevel", "MaxLevel", "DefaultMoveRate", "OffWaitTime", "OnLevel", "Binding", "UserLabelList", "FixedLabelList", "VisibleIndicator", 
                                               "DeviceTypeList", "ServerList", "ClientList", "PartsList", "TagList"]
		sendEventTypeOfEvents.each {
			if (device.hasAttribute (it.name)) {
				if (txtEnable) {
					if(device.currentValue(it.name) == it.value) {
						log.info ((it.descriptionText) ? (it.descriptionText) : ("${device.displayName}: ${it.name} set to ${it.value}") )+" (unchanged)" // Log if txtEnable and the value is the same
					} else {
						log.info ((it.descriptionText) ? (it.descriptionText) : ("${device.displayName}: ${it.name} set to ${it.value}") ) // Log if txtEnable and the value is the same
					}
				}
                sendEvent(it)
            } else if (updateLocalStateOnlyAttributes.contains(it.name)) {
                device.updateDataValue(it.name, "${it.value}")
			}
		}
		// Always check and reset the color name after any update. 
		// Only need to do it after a hue, saturation, or color temperature change, 
		// but for code simplicity, just let sendEvent handle that filtering!
		setColorName()
    } catch (AssertionError e) {
        log.error "<pre>${e}<br><br>Stack trace:<br>${getStackTrace(e) }"
    } catch(e){
        log.error "<pre>${e}<br><br>when processing parse with inputs ${sendEventTypeOfEvents}<br><br>Stack trace:<br>${getStackTrace(e) }"
    } 
}

void updated() {
    if (logEnable) {
		log.info "${device.displayName}: Debug logging enabled for 30 minutes"
		runIn(1800,logsOff)
	}
}

void logsOff(){
    if (txtEnable) "${device.displayName}: Turning off Debug logging."
    device.updateSetting("logEnable", [value:"false",type:"bool"])
}

void refresh() { parent?.refreshMatter(ep: getEndpoint() ) }

Integer getEndpoint() {
    String raw = getDataValue("endpointId")
    if (!raw?.trim()) {
        log.error "${device.displayName} is missing endpointId, so commands cannot be sent."
        return null
    }
    raw = raw.trim()
    if (raw.startsWith("0x") || raw.startsWith("0X")) raw = raw.substring(2)
    return Integer.parseInt(raw, 16)
}

void on() { parent?.on(ep: getEndpoint()) }

void onFor(seconds) {
    parent?.onWithTimedOff(ep: getEndpoint(), onTime10ths: (seconds * 10 as Integer))
}

void off() { parent?.off(ep: getEndpoint() ) }
void toggleOnOff() { parent?.toggleOnOff(ep: getEndpoint()) }
void OnWithTimedOff(timeInSeconds, guardTime = 0) {
    parent?.onWithTimedOff(ep: getEndpoint(), 
                           onTime10ths:(timeInSeconds * 10) as Integer, 
                           ((timeInSeconds + guardTime) * 10) as Integer)
}

void setLevel(level, ramp = null, onTime = null ) {
    Integer transitionTime10ths = (ramp == null || ramp == "") ? null : (ramp * 10) as Integer
    Integer onTime10ths = (onTime == null || onTime == "") ? null : (onTime * 10) as Integer
    parent?.setLevel(ep: getEndpoint(), level:level as Integer, transitionTime10ths: transitionTime10ths, onTime10ths: onTime10ths)
}

void startLevelChange(direction) { parent?.startLevelChange(ep: getEndpoint(), direction:direction) }

void stopLevelChange() { parent?.stopLevelChange(ep: getEndpoint()) }

// Additional Methods for handling of color

void setColor(colormap){  parent?.setColor(ep: getEndpoint(), *:colormap) }

void setHue(hue) { parent?.setHue(ep: getEndpoint(), hue: hue as Integer) }

void setSaturation(saturation) { parent?.setSaturation(ep: getEndpoint(), saturation:saturation as Integer) }

void setColorTemperature(colortemperature, level = null, transitionTime = null) {
    Integer levelInt = (level == null || level == "") ? null : level as Integer
    Integer transitionTime10ths = (transitionTime == null || transitionTime == "") ? null : (transitionTime * 10) as Integer
    parent?.setColorTemperature(ep: getEndpoint(), colortemperature: colortemperature, level: levelInt, transitionTime10ths: transitionTime10ths)
}

void setColorName(){
    String color
    switch (device.currentValue("colorMode")){
        case "RGB":
            Integer hue = device.currentValue("hue") 
            Integer saturation =  device.currentValue("saturation")
            Integer level =  device.currentValue("level")
            if(hue.is(null) || saturation.is(null) || level.is(null)) return // During startup, one of these may be null!
            // color = convertHueToGenericColorName(device.currentValue("hue") as Integer )
            color = getColorNameFromHSV( hue:hue, saturation:saturation, level:level)
            break
        case "CT":
            Integer colorTemperature = device.currentValue("colorTemperature")
            if(colorTemperature.is(null)) return;
            color = convertTemperatureToGenericColorName( colorTemperature )
            break
    }
    if (color && (device.currentValue("colorName") != color)  ) { 
        if(txtEnable) log.info "${device.displayName} set to color: ${color}"
        sendEvent(name:"colorName", value:color) 
    }
}
// ~~~~~ start include (16) matterTools.getExpandedColorNames ~~~~~
library ( // library marker matterTools.getExpandedColorNames, line 1
        base: "driver", // library marker matterTools.getExpandedColorNames, line 2
        author: "jvm33", // library marker matterTools.getExpandedColorNames, line 3
        category: "matter", // library marker matterTools.getExpandedColorNames, line 4
        description: "Color Name Transform", // library marker matterTools.getExpandedColorNames, line 5
        name: "getExpandedColorNames", // library marker matterTools.getExpandedColorNames, line 6
        namespace: "matterTools", // library marker matterTools.getExpandedColorNames, line 7
        documentationLink: "https://github.com/jvmahon/Hubitat-Matter", // library marker matterTools.getExpandedColorNames, line 8
		version: "0.0.1" // library marker matterTools.getExpandedColorNames, line 9
) // library marker matterTools.getExpandedColorNames, line 10
import groovy.transform.Field // library marker matterTools.getExpandedColorNames, line 11

@Field static List colorNames = [ // library marker matterTools.getExpandedColorNames, line 13
	[name:"Alice Blue",    		r:0xF0, g:0xF8, b:0xFF], // library marker matterTools.getExpandedColorNames, line 14
	[name:"Antique White",    	r:0xFA, g:0xEB, b:0xD7], // library marker matterTools.getExpandedColorNames, line 15
	[name:"Aqua",    			r:0x00, g:0xFF, b:0xFF], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 16
	[name:"Aquamarine",    		r:0x7F, g:0xFF, b:0xD4], // library marker matterTools.getExpandedColorNames, line 17
	[name:"Azure",    			r:0xF0, g:0xFF, b:0xFF], // library marker matterTools.getExpandedColorNames, line 18
	[name:"Beige",    			r:0xF5, g:0xF5, b:0xDC], // library marker matterTools.getExpandedColorNames, line 19
	[name:"Bisque",    			r:0xFF, g:0xE4, b:0xC4], // library marker matterTools.getExpandedColorNames, line 20
	[name:"Black",    			r:0x00, g:0x00, b:0x00], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 21
	[name:"Blanched Almond",    r:0xFF, g:0xEB, b:0xCD], // library marker matterTools.getExpandedColorNames, line 22
	[name:"Blue",    			r:0x00, g:0x00, b:0xFF], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 23
	[name:"Blue Violet",    	r:0x8A, g:0x2B, b:0xE2], // library marker matterTools.getExpandedColorNames, line 24
	[name:"Brown",    			r:0xA5, g:0x2A, b:0x2A], // library marker matterTools.getExpandedColorNames, line 25
	[name:"Burly Wood",    		r:0xDE, g:0xB8, b:0x87], // library marker matterTools.getExpandedColorNames, line 26
	[name:"Cadet Blue",    		r:0x5F, g:0x9E, b:0xA0], // library marker matterTools.getExpandedColorNames, line 27
	[name:"Chartreuse",    		r:0x7F, g:0xFF, b:0x00], // library marker matterTools.getExpandedColorNames, line 28
	[name:"Chocolate",    		r:0xD2, g:0x69, b:0x1E], // library marker matterTools.getExpandedColorNames, line 29
	[name:"Coral",    			r:0xFF, g:0x7F, b:0x50], // library marker matterTools.getExpandedColorNames, line 30
	[name:"Cornflower Blue",    r:0x64, g:0x95, b:0xED], // library marker matterTools.getExpandedColorNames, line 31
	[name:"Cornsilk",    		r:0xFF, g:0xF8, b:0xDC], // library marker matterTools.getExpandedColorNames, line 32
	[name:"Crimson",    		r:0xDC, g:0x14, b:0x3C], // library marker matterTools.getExpandedColorNames, line 33
	[name:"Cyan",    			r:0x00, g:0xFF, b:0xFF], // library marker matterTools.getExpandedColorNames, line 34
	[name:"Dark Blue",    		r:0x00, g:0x00, b:0x8B], // library marker matterTools.getExpandedColorNames, line 35
	[name:"Dark Cyan",    		r:0x00, g:0x8B, b:0x8B], // library marker matterTools.getExpandedColorNames, line 36
	[name:"Dark Goldenrod",    	r:0xB8, g:0x86, b:0x0B], // library marker matterTools.getExpandedColorNames, line 37
	[name:"Dark Gray",    		r:0xA9, g:0xA9, b:0xA9], // library marker matterTools.getExpandedColorNames, line 38
	[name:"Dark Green",    		r:0x00, g:0x64, b:0x00], // library marker matterTools.getExpandedColorNames, line 39
	[name:"Dark Khaki",    		r:0xBD, g:0xB7, b:0x6B], // library marker matterTools.getExpandedColorNames, line 40
	[name:"Dark Magenta",    	r:0x8B, g:0x00, b:0x8B], // library marker matterTools.getExpandedColorNames, line 41
	[name:"Dark Olive Green",   r:0x55, g:0x6B, b:0x2F], // library marker matterTools.getExpandedColorNames, line 42
	[name:"Dark Orange",    	r:0xFF, g:0x8C, b:0x00], // library marker matterTools.getExpandedColorNames, line 43
	[name:"Dark Orchid",    	r:0x99, g:0x32, b:0xCC], // library marker matterTools.getExpandedColorNames, line 44
	[name:"Dark Red",    		r:0x8B, g:0x00, b:0x00], // library marker matterTools.getExpandedColorNames, line 45
	[name:"Dark Salmon",    	r:0xE9, g:0x96, b:0x7A], // library marker matterTools.getExpandedColorNames, line 46
	[name:"Dark Sea Green",    	r:0x8F, g:0xBC, b:0x8F], // library marker matterTools.getExpandedColorNames, line 47
	[name:"Dark Slate Blue",    r:0x48, g:0x3D, b:0x8B], // library marker matterTools.getExpandedColorNames, line 48
	[name:"Dark Slate Gray",    r:0x2F, g:0x4F, b:0x4F], // library marker matterTools.getExpandedColorNames, line 49
	[name:"Dark Turquoise",    	r:0x00, g:0xCE, b:0xD1], // library marker matterTools.getExpandedColorNames, line 50
	[name:"Dark Violet",    	r:0x94, g:0x00, b:0xD3], // library marker matterTools.getExpandedColorNames, line 51
	[name:"Deep Pink",    		r:0xFF, g:0x14, b:0x93], // library marker matterTools.getExpandedColorNames, line 52
	[name:"Deep Sky Blue",    	r:0x00, g:0xBF, b:0xFF], // library marker matterTools.getExpandedColorNames, line 53
	[name:"Dim Gray",    		r:0x69, g:0x69, b:0x69], // library marker matterTools.getExpandedColorNames, line 54
	[name:"Dodger Blue",    	r:0x1E, g:0x90, b:0xFF], // library marker matterTools.getExpandedColorNames, line 55
	[name:"Fire Brick",    		r:0xB2, g:0x22, b:0x22], // library marker matterTools.getExpandedColorNames, line 56
	[name:"Floral White",    	r:0xFF, g:0xFA, b:0xF0], // library marker matterTools.getExpandedColorNames, line 57
	[name:"Forest Green",    	r:0x22, g:0x8B, b:0x22], // library marker matterTools.getExpandedColorNames, line 58
	[name:"Fuchsia",    		r:0xFF, g:0x00, b:0xFF], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 59
	[name:"Gainsboro",    		r:0xDC, g:0xDC, b:0xDC], // library marker matterTools.getExpandedColorNames, line 60
	[name:"Ghost White",    	r:0xF8, g:0xF8, b:0xFF], // library marker matterTools.getExpandedColorNames, line 61
	[name:"Gold",    			r:0xFF, g:0xD7, b:0x00], // library marker matterTools.getExpandedColorNames, line 62
	[name:"Goldenrod",    		r:0xDA, g:0xA5, b:0x20], // library marker matterTools.getExpandedColorNames, line 63
	[name:"Gray",    			r:0x80, g:0x80, b:0x80], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 64
	[name:"Green",    			r:0x00, g:0x80, b:0x00], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 65
	[name:"Green Yellow",    	r:0xAD, g:0xFF, b:0x2F], // library marker matterTools.getExpandedColorNames, line 66
	[name:"Honey Dew",    		r:0xF0, g:0xFF, b:0xF0], // library marker matterTools.getExpandedColorNames, line 67
	[name:"Hot Pink",    		r:0xFF, g:0x69, b:0xB4], // library marker matterTools.getExpandedColorNames, line 68
	[name:"Indian Red",    		r:0xCD, g:0x5C, b:0x5C], // library marker matterTools.getExpandedColorNames, line 69
	[name:"Indigo",    			r:0x4B, g:0x00, b:0x82], // library marker matterTools.getExpandedColorNames, line 70
	[name:"Ivory",    			r:0xFF, g:0xFF, b:0xF0], // library marker matterTools.getExpandedColorNames, line 71
	[name:"Khaki",    			r:0xF0, g:0xE6, b:0x8C], // library marker matterTools.getExpandedColorNames, line 72
	[name:"Lavender",    		r:0xE6, g:0xE6, b:0xFA], // library marker matterTools.getExpandedColorNames, line 73
	[name:"Lavender Blush",    	r:0xFF, g:0xF0, b:0xF5], // library marker matterTools.getExpandedColorNames, line 74
	[name:"Lawn Green",    		r:0x7C, g:0xFC, b:0x00], // library marker matterTools.getExpandedColorNames, line 75
	[name:"Lemon Chiffon",    	r:0xFF, g:0xFA, b:0xCD], // library marker matterTools.getExpandedColorNames, line 76
	[name:"Light Blue",    		r:0xAD, g:0xD8, b:0xE6], // library marker matterTools.getExpandedColorNames, line 77
	[name:"Light Coral",    	r:0xF0, g:0x80, b:0x80], // library marker matterTools.getExpandedColorNames, line 78
	[name:"Light Cyan",    		r:0xE0, g:0xFF, b:0xFF], // library marker matterTools.getExpandedColorNames, line 79
	[name:"Light Goldenrod Yellow", r:0xFA, g:0xFA, b:0xD2], // library marker matterTools.getExpandedColorNames, line 80
	[name:"Light Gray",    		r:0xD3, g:0xD3, b:0xD3], // library marker matterTools.getExpandedColorNames, line 81
	[name:"Light Green",    	r:0x90, g:0xEE, b:0x90], // library marker matterTools.getExpandedColorNames, line 82
	[name:"Light Pink",    		r:0xFF, g:0xB6, b:0xC1], // library marker matterTools.getExpandedColorNames, line 83
	[name:"Light Salmon",    	r:0xFF, g:0xA0, b:0x7A], // library marker matterTools.getExpandedColorNames, line 84
	[name:"Light SeaGreen",    	r:0x20, g:0xB2, b:0xAA], // library marker matterTools.getExpandedColorNames, line 85
	[name:"Light SkyBlue",    	r:0x87, g:0xCE, b:0xFA], // library marker matterTools.getExpandedColorNames, line 86
	[name:"Light SlateGray",    r:0x77, g:0x88, b:0x99], // library marker matterTools.getExpandedColorNames, line 87
	[name:"Light SteelBlue",    r:0xB0, g:0xC4, b:0xDE], // library marker matterTools.getExpandedColorNames, line 88
	[name:"Light Yellow",    	r:0xFF, g:0xFF, b:0xE0], // library marker matterTools.getExpandedColorNames, line 89
	[name:"Lime",    			r:0x00, g:0xFF, b:0x00], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 90
	[name:"LimeGreen",    		r:0x32, g:0xCD, b:0x32], // library marker matterTools.getExpandedColorNames, line 91
	[name:"Linen",    			r:0xFA, g:0xF0, b:0xE6], // library marker matterTools.getExpandedColorNames, line 92
	[name:"Magenta",    		r:0xFF, g:0x00, b:0xFF], // library marker matterTools.getExpandedColorNames, line 93
	[name:"Maroon",    			r:0x80, g:0x00, b:0x00], // Matter Spec 11.1.4.2 - Error in Matter Spec. 1.2 - Purple / Maroon are set the same in the spec! Value here is correct! // library marker matterTools.getExpandedColorNames, line 94
	[name:"Medium Aqua Marine", r:0x66, g:0xCD, b:0xAA], // library marker matterTools.getExpandedColorNames, line 95
	[name:"Medium Blue",    	r:0x00, g:0x00, b:0xCD], // library marker matterTools.getExpandedColorNames, line 96
	[name:"Medium Orchid",    	r:0xBA, g:0x55, b:0xD3], // library marker matterTools.getExpandedColorNames, line 97
	[name:"Medium Purple",    	r:0x93, g:0x70, b:0xDB], // library marker matterTools.getExpandedColorNames, line 98
	[name:"Medium Sea Green",   r:0x3C, g:0xB3, b:0x71], // library marker matterTools.getExpandedColorNames, line 99
	[name:"Medium Slate Blue",  r:0x7B, g:0x68, b:0xEE], // library marker matterTools.getExpandedColorNames, line 100
	[name:"Medium Spring Green", r:0x00, g:0xFA, b:0x9A], // library marker matterTools.getExpandedColorNames, line 101
	[name:"Medium Turquoise",   r:0x48, g:0xD1, b:0xCC], // library marker matterTools.getExpandedColorNames, line 102
	[name:"Medium Violet Red",  r:0xC7, g:0x15, b:0x85], // library marker matterTools.getExpandedColorNames, line 103
	[name:"Midnight Blue",    	r:0x19, g:0x19, b:0x70], // library marker matterTools.getExpandedColorNames, line 104
	[name:"Mint Cream",    		r:0xF5, g:0xFF, b:0xFA], // library marker matterTools.getExpandedColorNames, line 105
	[name:"Misty Rose",    		r:0xFF, g:0xE4, b:0xE1], // library marker matterTools.getExpandedColorNames, line 106
	[name:"Moccasin",    		r:0xFF, g:0xE4, b:0xB5], // library marker matterTools.getExpandedColorNames, line 107
	[name:"Navajo White",    	r:0xFF, g:0xDE, b:0xAD], // library marker matterTools.getExpandedColorNames, line 108
	[name:"Navy",    			r:0x00, g:0x00, b:0x00], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 109
	[name:"Old Lace",    		r:0xFD, g:0xF5, b:0xE6], // library marker matterTools.getExpandedColorNames, line 110
	[name:"Olive",    			r:0x80, g:0x80, b:0x00],  // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 111
	[name:"Olive Drab",    		r:0x6B, g:0x8E, b:0x23], // library marker matterTools.getExpandedColorNames, line 112
	[name:"Orange",    			r:0xFF, g:0xA5, b:0x00], // library marker matterTools.getExpandedColorNames, line 113
	[name:"Orange Red",    		r:0xFF, g:0x45, b:0x00], // library marker matterTools.getExpandedColorNames, line 114
	[name:"Orchid",    			r:0xDA, g:0x70, b:0xD6], // library marker matterTools.getExpandedColorNames, line 115
	[name:"Pale Goldenrod",    	r:0xEE, g:0xE8, b:0xAA], // library marker matterTools.getExpandedColorNames, line 116
	[name:"Pale Green",    		r:0x98, g:0xFB, b:0x98], // library marker matterTools.getExpandedColorNames, line 117
	[name:"Pale Turquoise",    	r:0xAF, g:0xEE, b:0xEE], // library marker matterTools.getExpandedColorNames, line 118
	[name:"Pale Violet Red",    r:0xDB, g:0x70, b:0x93], // library marker matterTools.getExpandedColorNames, line 119
	[name:"Papaya Whip",   		r:0xFF, g:0xEF, b:0xD5], // library marker matterTools.getExpandedColorNames, line 120
	[name:"Peach Puff",    		r:0xFF, g:0xDA, b:0xB9], // library marker matterTools.getExpandedColorNames, line 121
	[name:"Peru",    			r:0xCD, g:0x85, b:0x3F], // library marker matterTools.getExpandedColorNames, line 122
	[name:"Pink",    			r:0xFF, g:0xC0, b:0xCB], // library marker matterTools.getExpandedColorNames, line 123
	[name:"Plum",    			r:0xDD, g:0xA0, b:0xDD], // library marker matterTools.getExpandedColorNames, line 124
	[name:"Powder Blue",    	r:0xB0, g:0xE0, b:0xE6], // library marker matterTools.getExpandedColorNames, line 125
	[name:"Purple",    			r:0x80, g:0x00, b:0x80], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 126
	[name:"Red",    			r:0xFF, g:0x00, b:0x00], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 127
	[name:"Rosy Brown",    		r:0xBC, g:0x8F, b:0x8F], // library marker matterTools.getExpandedColorNames, line 128
	[name:"Royal Blue",    		r:0x41, g:0x69, b:0xE1], // library marker matterTools.getExpandedColorNames, line 129
	[name:"Saddle Brown",    	r:0x8B, g:0x45, b:0x13], // library marker matterTools.getExpandedColorNames, line 130
	[name:"Salmon",    			r:0xFA, g:0x80, b:0x72], // library marker matterTools.getExpandedColorNames, line 131
	[name:"Sandy Brown",    	r:0xF4, g:0xA4, b:0x60], // library marker matterTools.getExpandedColorNames, line 132
	[name:"Sea Green",    		r:0x2E, g:0x8B, b:0x57], // library marker matterTools.getExpandedColorNames, line 133
	[name:"Seashell",    		r:0xFF, g:0xF5, b:0xEE], // library marker matterTools.getExpandedColorNames, line 134
	[name:"Sienna",    			r:0xA0, g:0x52, b:0x2D], // library marker matterTools.getExpandedColorNames, line 135
	[name:"Silver",    			r:0xC0, g:0xC0, b:0xC0], // library marker matterTools.getExpandedColorNames, line 136
	[name:"Sky Blue",    		r:0x87, g:0xCE, b:0xEB], // library marker matterTools.getExpandedColorNames, line 137
	[name:"Slate Blue",    		r:0x6A, g:0x5A, b:0xCD], // library marker matterTools.getExpandedColorNames, line 138
	[name:"Slate Gray",    		r:0x70, g:0x80, b:0x90], // library marker matterTools.getExpandedColorNames, line 139
	[name:"Snow",    			r:0xFF, g:0xFA, b:0xFA], // library marker matterTools.getExpandedColorNames, line 140
	[name:"Spring Green",    	r:0x00, g:0xFF, b:0x7F], // library marker matterTools.getExpandedColorNames, line 141
	[name:"Steel Blue",    		r:0x46, g:0x82, b:0xB4], // library marker matterTools.getExpandedColorNames, line 142
	[name:"Tan",    			r:0xD2, g:0xB4, b:0x8C], // library marker matterTools.getExpandedColorNames, line 143
	[name:"Teal",    			r:0x00, g:0x80, b:0x80], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 144
	[name:"Thistle",    		r:0xD8, g:0xBF, b:0xD8], // library marker matterTools.getExpandedColorNames, line 145
	[name:"Tomato",    			r:0xFF, g:0x63, b:0x47], // library marker matterTools.getExpandedColorNames, line 146
	[name:"Turquoise",    		r:0x40, g:0xE0, b:0xD0], // library marker matterTools.getExpandedColorNames, line 147
	[name:"Violet",    			r:0xEE, g:0x82, b:0xEE], // library marker matterTools.getExpandedColorNames, line 148
	[name:"Wheat",    			r:0xF5, g:0xDE, b:0xB3], // library marker matterTools.getExpandedColorNames, line 149
	[name:"White",    			r:0xFF, g:0xFF, b:0xFF], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 150
	[name:"White Smoke",    	r:0xF5, g:0xF5, b:0xF5], // library marker matterTools.getExpandedColorNames, line 151
	[name:"Yellow",    			r:0xFF, g:0xFF, b:0x00], // Matter Spec 11.1.4.2 // library marker matterTools.getExpandedColorNames, line 152
	[name:"Yellow Green",    	r:0x9A, g:0xCD, b:0x32], // library marker matterTools.getExpandedColorNames, line 153
] // library marker matterTools.getExpandedColorNames, line 154

String getColorNameFromRGB(Map inputs = [:] ) { // library marker matterTools.getExpandedColorNames, line 156
    assert inputs.r instanceof Integer && inputs.g instanceof Integer && inputs.b instanceof Integer // library marker matterTools.getExpandedColorNames, line 157
    assert inputs.r >=0 && inputs.r <=0xFF  // library marker matterTools.getExpandedColorNames, line 158
    assert inputs.g >=0 && inputs.g <=0xFF // library marker matterTools.getExpandedColorNames, line 159
    assert inputs.b >=0 && inputs.b <=0xFF // library marker matterTools.getExpandedColorNames, line 160
    // Find mininum distance to a color using next nearest neighbor calculation - don't need to do the squareroot part though // library marker matterTools.getExpandedColorNames, line 161
    // Geometric distance between two points in 3D space is calculated as SQRT( (X-x)**2 + (Y-y)**2 + (Z-z)**) where X,Y,Z is a first point  // library marker matterTools.getExpandedColorNames, line 162
    // and (x,y,z) is the second.  That concept is used to find distance between colors. // library marker matterTools.getExpandedColorNames, line 163
    // Can skip applying the SQRT part in this case, as the minimum can be determined without it // library marker matterTools.getExpandedColorNames, line 164
   return colorNames.min { (inputs.r - it.r)**2 + (inputs.g - it.g)**2 + (inputs.b - it.b)**2 }.name // library marker matterTools.getExpandedColorNames, line 165
} // library marker matterTools.getExpandedColorNames, line 166

String getColorNameFromHSV(Map inputs = [:]){ // library marker matterTools.getExpandedColorNames, line 168
    assert inputs.hue instanceof Integer && inputs.saturation instanceof Integer && inputs.level instanceof Integer // library marker matterTools.getExpandedColorNames, line 169
    inputs.hue %= 100 // hue maps to degrees on a circle. Hubitat treats it as a % with each % being 3.6 degres. Values greater than 100% wrap around so Mod by 100 gets to same place // library marker matterTools.getExpandedColorNames, line 170
    if (inputs.hue < 0) inputs.hue += 100 // hue maps to degrees on a circle. A negative value gets to the same place by adding 100% and treating as a positive value // library marker matterTools.getExpandedColorNames, line 171
    List rgbList = hubitat.helper.ColorUtils.hsvToRGB([inputs.hue, inputs.saturation, inputs.level]) // library marker matterTools.getExpandedColorNames, line 172
    Map colors = [r:(rgbList[0]), g:(rgbList[1]), b:(rgbList[2])] // library marker matterTools.getExpandedColorNames, line 173
    getColorNameFromRGB(colors) // library marker matterTools.getExpandedColorNames, line 174
} // library marker matterTools.getExpandedColorNames, line 175

// ~~~~~ end include (16) matterTools.getExpandedColorNames ~~~~~
