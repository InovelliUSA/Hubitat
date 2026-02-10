def getDriverDate() { return "2026-02-05" }	// **** DATE OF THE DEVICE DRIVER
//  ^^^^^^^^^^  UPDATE THIS DATE IF YOU MAKE ANY CHANGES  ^^^^^^^^^^
/**
* Inovelli VZW32-SN Red Series Z-Wave 2-in-1 mmWave
*
* Author: Eric Maycock (erocm123)
* Contributor: Mark Amber (marka75160)
* Platform: Hubitat
*
* Copyright 2026 Eric Maycock / Inovelli
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at:
*
*	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
* for the specific language governing permissions and limitations under the License.
*
* 2
* 2026-02-05(EM) Enabling Single LED Notifications, quick start, single tap behavior, etc.
*                Cleaning up some code and removing Target Info Report Option (it is not supported at this time)
* 2026-02-04(EM) Fixing comparison issue in parameter 101-106 reporting.
* 2026-02-04(EM) Not clearing default settings or reading back reports into settings fields. Fixing issue in parameter 101-106 reporting.
* 2026-02-04(EM) Fixing issue in parameter 101-106 reporting. Updating size of Stay Life to 4 bytes.
* 2026-02-02(EM) Updating parameter descriptions to include units.
* 2026-01-21(EM) Adding parameter 115 to readOnlyParams() for firmware version reporting.
* 2026-01-21(EM) Fixing bug in undefined button function logging. Fixing issue with setParameter().
* 2025-12-20(EM) Adding getTemperature command to retrieve the internal temperature of the switch.
* 2025-11-25(EM) Removing delayBetween from return values for initialize()
* 2025-10-16(EM) Update SwitchMultilevelReport and BasicReport to set targetValue to value so it can override the value parsed from the command.
* 2025-10-03(EM) Added feature to prevent updates to unchanged parameters.
* 2025-07-30(EM) Added parameter parsing cases for mmWave parameters 101-120 and parameter 115 state variable for mmWave firmware version.
* 2025-07-29(EM) Fixed model detection for VZW32-SN and updated aux types to only support 2 types (0=Single Pole, 1=Aux Switch).
* 2025-07-17(EM) Added parameter 117 (Room Size) for firmware 1.09.
* 2025-04-03(EM) Adding P109 for fw 1.04 & changed default of P110 to 30. Temporary calculator:
*                https://inovelli-my.sharepoint.com/:x:/p/ericm/EUOV-S7C_-ZMkzCWhs0CoOcBS_6H-Xk_HTErfgIn-qofgw?e=81s8Rk
* 2025-03-25(EM) Making changes to parameters for fw 1.04 & 1.03
* 2025-02-21(EM) Adding P101-106 for fw 1.02
* 2025-02-17(EM) Updating P110 for fw 1.02
**/

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field
import hubitat.helper.ColorUtils
//import hubitat.helper.HexUtils
import java.security.MessageDigest

metadata {
    definition (name: "Inovelli mmWave Dimmer Red Series VZW32-SN", namespace: "InovelliUSA", author: "E.Maycock/M.Amber", importUrl:  "https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-mmwave-dimmer-red-series-vzw32-sn.src/inovelli-mmwave-dimmer-red-series-vzw32-sn.groovy")
	{
		capability "Actuator"	//device can "do" something (has commands)
        capability "Sensor"		//device can "report" something (has attributes)

        capability "ChangeLevel"
        capability "Configuration"
        capability "EnergyMeter"				//Fan does not support energy monitoring but Dimmer does
        //capability "FanControl"
        capability "HoldableButton"
		capability "Initialize"
        capability "LevelPreset"
        capability "PowerMeter"					//Fan does not support power monitoring but Dimmer does
        capability "PushableButton"
        capability "Refresh"
        capability "ReleasableButton"
        //capability "SignalStrength"			//placeholder for future testing to see if this can be implemented
        capability "Switch"
        capability "SwitchLevel"
        capability "Motion Sensor"
        capability "Illuminance Measurement"

        attribute "lastButton", "String"		//last button event
        attribute "ledEffect", "String"			//last LED effect requested (may have timed-out and not necessarily displaying currently)
		attribute "internalTemp", "String"		//Internal Temperature in Celsius	(read-only P32)
        //attribute "numberOfBindings", "String" //Group bindings count as 2		(read only P51)
		attribute "areaReport", "String"			//mmWave Person in Reporting Area	(read-only P116)
		attribute "overHeat", "String"			//Overheat Indicator				(read-only P33)
		attribute "powerSource", "String"		//Neutral/non-Neutral				(read-only P21)
		attribute "remoteProtection", "String"	//Enabled or Disabled				(read-only P257)
        attribute "smartBulb", "String"			//Smart Bulb mode enabled or disabled
        //attribute "smartFan", "String"		//Smart Fan mode enabled or disabled
        attribute "switchMode", "String"		//Dimmer or On/Off only

        // Uncomment these lines if you would like to test your scenes with digital button presses.
        /**
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
        command "releaseUp"
        command "releaseDown"
        command "pressConfigX1"
        command "pressConfigX2"
        command "pressConfigX3"
        command "pressConfigX4"
        command "pressConfigX5"
        command "holdConfig"
        command "releaseConfig"
        **/
/**
        command "childOn", ["string"]
        command "childOff", ["string"]
        command "childSetLevel", ["string"]
        command "childRefresh", ["string"]
        command "componentOn"
        command "componentOff"
        command "componentSetLevel"
        command "componentRefresh"
        command "customEffectStart",   [[name: "Custom Effect Start*",type:"STRING", description: "Output from \"Inovelli Light Strip Effect\" app"]]
        command "customEffectStop"
**/

        command "configure",           [[name:"Option",    type:"ENUM",   description:"blank=current states and user-changed settings, All=configure all settings, Default=set all settings to default", constraints:[" ","All","Default"]]]

//		command "identify",			   [[name:"Seconds",   type:"NUMBER", description:"number of seconds to blink the LED bar so it can be identified (leave blank to see remaining seconds in the logs)"],
//										[name:"number of seconds to blink the LED bar so it can be identified (leave blank to see remaining seconds in the logs)"]]
		
        command "initialize",		   [[name:"clear state variables, clear LED notifications, refresh current states"]]
        
        command "getTemperature",	   [[name:"Get the switch internal operating temperature"]]

        command "ledEffectAll",        [[name:"Effect*",   type:"ENUM",
											description:  "255=Stop,  1=Solid,  2=Fast Blink,  3=Slow Blink,  4=Pulse,  5=Chase,  6=Open/Close,  7=Small-to-Big,  8=Aurora,  9=Slow Falling,  10=Medium Falling,  11=Fast Falling,  12=Slow Rising,  13=Medium Rising,  14=Fast Rising,  15=Medium Blink,  16=Slow Chase,  17=Fast Chase,  18=Fast Siren,  19=Slow Siren,  0=LEDs off",
											constraints: ["255=Stop","1=Solid","2=Fast Blink","3=Slow Blink","4=Pulse","5=Chase","6=Open/Close","7=Small-to-Big","8=Aurora","9=Slow Falling","10=Medium Falling","11=Fast Falling","12=Slow Rising","13=Medium Rising","14=Fast Rising","15=Medium Blink","16=Slow Chase","17=Fast Chase","18=Fast Siren","19=Slow Siren","0=LEDs off"]],
                                        [name:"Color",     type:"NUMBER", description: "0-254=Hue Color, 255=White, default=Red"],
                                        [name:"Level",     type:"NUMBER", description: "0-100=LED Intensity, default=100"],
                                        [name:"Duration",  type:"NUMBER", description: "1-60=seconds, 61-120=1-120 minutes, 121-254=1-134 hours, 255=Indefinitely, default=60"]]
        
        command "ledEffectOne",        [[name:"LEDnum*",   type:"ENUM",   description: "LED 1-7", constraints: ["7 (top)","6","5","4 (middle)","3","2","1 (bottom)","123 (bottom half)","567 (top half)","12 (bottom 3rd)","345 (middle 3rd)","67 (top 3rd)","147 (bottom-middle-top)","1357 (odd)","246 (even)"]],
                                        [name:"Effect*",   type:"ENUM", 
											description: "255=Stop,  1=Solid,  2=Fast Blink,  3=Slow Blink,  4=Pulse,  5=Chase,  6=Falling,  7=Rising,  8=Aurora,  0=LED off", 
											constraints:["255=Stop","1=Solid","2=Fast Blink","3=Slow Blink","4=Pulse","5=Chase","6=Falling","7=Rising","8=Aurora","0=LED off"]],
                                        [name:"Color",     type:"NUMBER", description:"0-254=Hue Color, 255=White, default=Red"],
                                        [name:"Level",     type:"NUMBER", description:"0-100=LED Intensity, default=100"],
                                        [name:"Duration",  type:"NUMBER", description:"1-60=seconds, 61-120=1-120 minutes, 121-254=1-134 hours, 255=Indefinitely, default=60"]]

        command "presetLevel",         [[name:"Level",     type:"NUMBER", description:"Level to preset (1 to 101)"]]
        
        command "refresh",             [[name:"Option",    type:"ENUM",   description:"blank=current states and user-changed settings, All=refresh all settings", constraints: [" ","All"]]]
		
//		command "remoteControl",	   [[name:"Option*",   type:"ENUM",   description:"ability to control the switch remotely", constraints: [" ","Enabled","Disabled"]]]

        command "resetEnergyMeter"

        command "setAssociationGroup", [[name: "Group Number*",type:"NUMBER", description: "Provide the association group number to edit"],
                                        [name: "Z-Wave Node*", type:"STRING", description: "Enter the node number (in hex) associated with the node"],
                                        [name: "Action*",      type:"ENUM", constraints: ["Add", "Remove"]],
                                        [name:"Multi-channel Endpoint", type:"NUMBER", description: "Currently not implemented"]]

		//uncomment this command if you need it for backward compatibility
        //command "setIndicator",        [[name: "Set Indicator*",type:"NUMBER", description: "For configuration values see: https://nathanfiscus.github.io/inovelli-switch-toolbox/"]]

        command "setParameter",        [[name:"Parameter*",type:"NUMBER", description:"Parameter number"],
                                        [name:"Raw Value", type:"NUMBER", description:"Value for the parameter (leave blank to get current value)"],
										[name:"Enter the internal raw value. Percentages and Color Hues are entered as 0-255. Leave blank to get current value"]]

		//uncomment this command if you need it for backward compatibility
        //command "startNotification",   [[name: "Notification Code*",type:"NUMBER", description: "For configuration values see: https://nathanfiscus.github.io/inovelli-switch-toolbox/"],
        //                                [name: "DEPRECIATED",                      description: "This command is depreciated.  Use ledEffectAll instead"]]

		//uncomment this command if you need it for backward compatibility										
        //command "stopNotification",    [[name: "DEPRECIATED",                      description: "This command is depreciated.  Use ledEffectAll instead"]]


        command "startLevelChange",    [[name:"Direction*",type:"ENUM",   description:"Direction for level change", constraints: ["up","down"]],
                                        [name:"Duration",  type:"NUMBER", description:"Transition duration in seconds"]]
        command "toggle"

        fingerprint mfr:"031E", prod:"0017", deviceId:"0001", inClusters:"0x5E,0x98,0x9F,0x55,0x85,0x59,0x8E,0x5B,0x70,0x5A,0x7A,0x87,0x72,0x31,0x26,0x73,0x6C,0x86,0x32,0x75,0x91", controllerType: "ZWV"
    }

    preferences {
        userSettableParams().each{ i ->
            switch(configParams["parameter${i.toString().padLeft(3,"0")}"].type){
                case "number":
                    switch(i){
						case readOnlyParams().contains(i):	
							//read-only params are non-settable, so skip user input
							break 
                        default:
                            input "parameter${i}", "number",
								title: "${i}. " + bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name),
                                description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description +
                                    "<br>Range=" + configParams["parameter${i.toString().padLeft(3,"0")}"].range +
				    	    	    " Default=" +  configParams["parameter${i.toString().padLeft(3,"0")}"].default),
                                //defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                                range: configParams["parameter${i.toString().padLeft(3,"0")}"].range
                            break
                    }
                    break
                case "enum":
                    switch(i){
						case readOnlyParams().contains(i):	
							//read-only params are non-settable, so skip user input
							break 
                        case 22:    //Aux Type
                        case 52:    //Smart Bulb Mode
                        case 158:   //Switch Mode Zwave
                        case 258:   //Switch Mode Zigbee
							//these are important parameters so display in red to draw attention
                            input "parameter${i}", "enum",
                                title: "${i}. " + indianRed(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name)),
                                description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description),
                                defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                                options: configParams["parameter${i.toString().padLeft(3,"0")}"].range
                            break
                        case 95:
                        case 96:
							//special case for custom color is below
                            break
                        default:
                            input "parameter${i}", "enum",
                                title: "${i}. " + bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name),
                                description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description),
                                //defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                                options: configParams["parameter${i.toString().padLeft(3,"0")}"].range
                            break
					}
                    break
                case "text":
                    if (!readOnlyParams().contains(i)) {
                        input "parameter${i}", "text",
                            title: "${i}. " + bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name),
                            description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description +
                                "<br>Range=" + configParams["parameter${i.toString().padLeft(3,"0")}"].range +
                                " Default=" + configParams["parameter${i.toString().padLeft(3,"0")}"].default)
                    }
                    break
            }

            if (i==95 || i==96) {
                if ((i==95 && parameter95custom==null)||(i==96 && parameter96custom==null)){
                    input "parameter${i}", "enum",
                        title: "${i}. " + hue((settings?."parameter${i}"!=null?settings?."parameter${i}":configParams["parameter${i.toString().padLeft(3,"0")}"].default)?.toInteger(),
                            bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name)),
                        description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description),
						//defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                        options: configParams["parameter${i.toString().padLeft(3,"0")}"].range
                } else {
                    input "parameter${i}", "enum",
                        title: "${i}. " + hue((settings?."parameter${i}"!=null?settings?."parameter${i}":configParams["parameter${i.toString().padLeft(3,"0")}"].default)?.toInteger(),
                            strike(configParams["parameter${i.toString().padLeft(3,"0")}"].name)) +
                            hue((settings?."parameter${i}custom"!=null?(settings."parameter${i}custom"/360*255):configParams["parameter${i.toString().padLeft(3,"0")}"].default)?.toInteger(),
                                italic(bold(" Overridden by Custom Hue Value"))),
                        description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description),
						//defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                        options: configParams["parameter${i.toString().padLeft(3,"0")}"].range
                }
                input "parameter${i}custom", "number",
                    title: settings?."parameter${i}custom"!=null?
                        (hue((settings."parameter${i}custom"/360*255)?.toInteger(),
                            bold("Custom " + configParams["parameter${i.toString().padLeft(3,"0")}"].name))):
						(	bold("Custom " + configParams["parameter${i.toString().padLeft(3,"0")}"].name)),
                    description: italic("Hue value to override " + configParams["parameter${i.toString().padLeft(3,"0")}"].name+".<br>Range: 0-360 chosen from a "+
                        underline(''' <a href="https://community-assets.home-assistant.io/original/3X/6/c/6c0d1ea7c96b382087b6a34dee6578ac4324edeb.png" target="_blank">'''+
                        hue(0,"h")+hue(15,"u")+hue(30,"e")+hue(70," c")+hue(85,"o")+hue(100,"l")+hue(120,"o")+hue(140,"r")+hue(160," w")+hue(180,"h")+hue(200,"e")+hue(220,"e")+hue(240,"l")+"</a>")),
                    required: false,
                    range: "0..360"
            }
        }
        input name: "infoEnable",          type: "bool",   title: bold("Enable Info Logging"),   defaultValue: true,  description: italic("Log general device activity<br>(optional and not required for normal operation)")
        input name: "traceEnable",         type: "bool",   title: bold("Enable Trace Logging"),  defaultValue: false, description: italic("Additional info for trouble-shooting (not needed unless having issues)")
        input name: "debugEnable",         type: "bool",   title: bold("Enable Debug Logging"),  defaultValue: false, description: italic("Detailed diagnostic data<br>"+fireBrick("(only enable when asked by a developer)"))
        input name: "disableInfoLogging",  type: "number", title: bold("Disable Info Logging after this number of minutes"),  description: italic("(0=Do not disable)"), defaultValue: 20
        input name: "disableTraceLogging", type: "number", title: bold("Disable Trace Logging after this number of minutes"), description: italic("(0=Do not disable)"), defaultValue: 10
        input name: "disableDebugLogging", type: "number", title: bold("Disable Debug Logging after this number of minutes"), description: italic("(0=Do not disable)"), defaultValue: 5
    }
}

def motionEvent(value) {
    def map = [name: "motion"]
    if (value != 0) {
        map.value = "active"
        map.descriptionText = "$device.displayName detected motion"
    } else {
        map.value = "inactive"
        map.descriptionText = "$device.displayName motion has stopped"
    }
    createEvent(map)
}

private getAdjustedLuminance(value) {
    
    value = Math.round((value as Double) * 100) / 100

    if (settings.luminanceOffset) {
       return value =  value + Math.round(settings.luminanceOffset * 100) /100
    } else {
       return value
    }
    
}

def getTemperature() {
    if (infoEnable) log.info "${device.displayName} getTemperature()"
    state.lastCommandSent =                        "getTemperature()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += getParameter(32)
    cmds += getParameter(33)
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def zwaveEvent(hubitat.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            map.name = "temperature"
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            state.realTemperature = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
            map.value = getAdjustedTemp(state.realTemperature)
            map.unit = getTemperatureScale()
            if (infoEnable != false) log.info "${device.label?device.label:device.name}: Temperature report received: ${map.value}"
            break;
        case 3:
            map.name = "illuminance"
            state.realLuminance = cmd.scaledSensorValue.toInteger()<0?(((cmd.scaledSensorValue.toInteger()+32768) % 65536) + 32768) : cmd.scaledSensorValue.toInteger()
            map.value = getAdjustedLuminance(cmd.scaledSensorValue.toInteger()<0?(((cmd.scaledSensorValue.toInteger()+32768) % 65536) + 32768) : cmd.scaledSensorValue.toInteger())
            map.unit = "lux"
            if (infoEnable != false) log.info "${device.label?device.label:device.name}: Illuminance report received: ${map.value}"
            break;
        case 5:
            map.name = "humidity"
            state.realHumidity = cmd.scaledSensorValue.toInteger()
            map.value = getAdjustedHumidity(cmd.scaledSensorValue.toInteger())
            map.unit = "%"
            if (infoEnable != false) log.info "${device.label?device.label:device.name}: Humidity report received: ${map.value}"
            break;
        default:
            map.descriptionText = cmd.toString()
            if (infoEnable != false) log.info "${device.label?device.label:device.name}: Unhandled sensor multilevel report received: ${map.descriptionText}"
    }
    createEvent(map)
}

def zwaveEvent(hubitat.zwave.commands.notificationv3.NotificationReport cmd) {
    if (debugEnable != false) log.debug "${device.label?device.label:device.name}: ${cmd}"
    def result = []
    if (cmd.notificationType == 7) {
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: notificationType 7 (Home Security)"
        switch (cmd.event) {
            case 0:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 0 (State Idle)"
                result << motionEvent(0)
                break
            case 1:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 1 (Intrusion - location provided)"
                result << motionEvent(1)
                break
            case 3:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 3 (Tampering - product cover removed)"
                result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName was moved")
                result << createEvent(name: "acceleration", value: "active", descriptionText: "$device.displayName was moved")
                break
            case 7:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 7 (Motion detection - location provided)"
                result << motionEvent(1)
                break
            case 8:
                if (infoEnable != false) log.info "${device.label?device.label:device.name}: event 8 (Motion detection)"
                result << motionEvent(1)
                break
        }
    } else {
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Unhandled Notification report received: ${cmd}"
        result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
    }
    result
}

def validConfigParams() {	//all valid parameters for this specific device (configParams Map contains definitions for all parameters for all devices)
	return [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,17,18,19,20,21,22,23,24,25,50,52,53,54,55,56,58,59,64,69,74,79,84,89,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,118,119,120,123,130,131,132,133,134,156,157,158,159,160,161,162]
}

def userSettableParams() {   //controls which options are available depending on whether the device is configured as a switch or a dimmer.
    if (parameter158 == "1") return [158,22,52,                  10,11,12,      15,17,18,19,20,23,24,25,50,            58,59,64,69,74,79,84,89,94,95,96,97,98,100,101,102,103,104,105,106,108,109,110,111,112,113,114,115,116,117,118,119,120,123,130,131,132,133,134,159,160,161,162]  //on/off mode
    else                     return [158,22,52,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,17,18,19,20,23,24,25,50,53,54,55,56,58,59,64,69,74,79,84,89,94,95,96,97,98,100,101,102,103,104,105,106,108,109,110,111,112,113,114,115,116,117,118,119,120,123,130,131,132,133,134,159,160,    162]  //dimmer mode
}

def readOnlyParams() {
	return [21,32,33,51,115,116,157,257]
}

@Field static Integer shortDelay = 500		//default delay to use for zwave commands (in milliseconds)
@Field static Integer longDelay = 1000		//long delay to use for changing modes (in milliseconds)
@Field static Integer defaultQuickLevel=50	//default startup level for QuickStart emulation
@Field static List ledNotificationEndpoints = [99]

@Field static Map configParams = [
    parameter001 : [
        number: 1,
        name: "Dimming Speed - Up (Remote)",
        description: "Sets the rate that the light dims up when controlled from the hub. A setting of 'instant' turns the light immediately on.<br>Default=2.5s",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s (default)","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"1s","102":"2s","103":"3s","104":"4s","105":"5s","106":"6s","107":"7s","108":"8s","109":"9s","110":"10s","111":"11s","112":"12s","113":"13s","114":"14s","115":"15s","116":"16s","117":"17s","118":"18s","119":"19s","120":"20s","121":"21s","122":"22s","123":"23s","124":"24s","125":"25s","126":"26s","127":"27s","128":"28s","129":"29s","130":"30s","131":"31s","132":"32s","133":"33s","134":"34s","135":"35","136":"36s","137":"37s","138":"38s","139":"39s","140":"40s","141":"41s","142":"42s","143":"43s","144":"44s","145":"45s","146":"46s","147":"47s","148":"48s","149":"49s","150":"50s","151":"51s","152":"52s","153":"53s","154":"54s","155":"55s","156":"56s","157":"57s","158":"58s","159":"59s","160":"60s","161":"1m","162":"2m","163":"3m","164":"4m","165":"5m","166":"6m","167":"7m","168":"8m","169":"9m","170":"10m","171":"11m","172":"12m","173":"13m","174":"14m","175":"15m","176":"16m","177":"17m","178":"18m","179":"19m","180":"20m","181":"21m","182":"22m","183":"23m","184":"24m","185":"25m","186":"26m","187":"27m","188":"28m","189":"29m","190":"30m","191":"31m","192":"32m","193":"33m","194":"34m","195":"35m","196":"36m","197":"37m","198":"38m","199":"39m","200":"40m","201":"41m","202":"42m","203":"43m","204":"44m","205":"45m","206":"46m","207":"47m","208":"48m","209":"49m","210":"50m","211":"51m","212":"52m","213":"53m","214":"54m","215":"55m","216":"56m","217":"57m","218":"58m","219":"59m","220":"60m","221":"61m","222":"62m","223":"63m","224":"64m","225":"65m","226":"66m","227":"67m","228":"68m","229":"69m","230":"70m","231":"71m","232":"72m","233":"73m","234":"74m","235":"75m","236":"76m","237":"77m","238":"78m","239":"79m","240":"80m","241":"81m","242":"82m","243":"83m","244":"84m","245":"85m","246":"86m","247":"87m","248":"88m","249":"89m","250":"90m","251":"91m","252":"92m","253":"93m","254":"94m"],
        default: 25,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter002 : [
        number: 2,
        name: "Dimming Speed - Up (Local)",
        description: "Sets the rate that the light dims up when controlled at the switch. A setting of 'instant' turns the light immediately on.<br>Default=Sync with parameter1",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"1s","102":"2s","103":"3s","104":"4s","105":"5s","106":"6s","107":"7s","108":"8s","109":"9s","110":"10s","111":"11s","112":"12s","113":"13s","114":"14s","115":"15s","116":"16s","117":"17s","118":"18s","119":"19s","120":"20s","121":"21s","122":"22s","123":"23s","124":"24s","125":"25s","126":"26s","127":"27s","128":"28s","129":"29s","130":"30s","131":"31s","132":"32s","133":"33s","134":"34s","135":"35","136":"36s","137":"37s","138":"38s","139":"39s","140":"40s","141":"41s","142":"42s","143":"43s","144":"44s","145":"45s","146":"46s","147":"47s","148":"48s","149":"49s","150":"50s","151":"51s","152":"52s","153":"53s","154":"54s","155":"55s","156":"56s","157":"57s","158":"58s","159":"59s", "160":"60s","161":"1m","162":"2m","163":"3m","164":"4m","165":"5m","166":"6m","167":"7m","168":"8m","169":"9m","170":"10m","171":"11m","172":"12m","173":"13m","174":"14m","175":"15m","176":"16m","177":"17m","178":"18m","179":"19m","180":"20m","181":"21m","182":"22m","183":"23m","184":"24m","185":"25m","186":"26m","187":"27m","188":"28m","189":"29m","190":"30m","191":"31m","192":"32m","193":"33m","194":"34m","195":"35m","196":"36m","197":"37m","198":"38m","199":"39m","200":"40m","201":"41m","202":"42m","203":"43m","204":"44m","205":"45m","206":"46m","207":"47m","208":"48m","209":"49m","210":"50m","211":"51m","212":"52m","213":"53m","214":"54m","215":"55m","216":"56m","217":"57m","218":"58m","219":"59m","220":"60m","221":"61m","222":"62m","223":"63m","224":"64m","225":"65m","226":"66m","227":"67m","228":"68m","229":"69m","230":"70m","231":"71m","232":"72m","233":"73m","234":"74m","235":"75m","236":"76m","237":"77m","238":"78m","239":"79m","240":"80m","241":"81m","242":"82m","243":"83m","244":"84m","245":"85m","246":"86m","247":"87m","248":"88m","249":"89m","250":"90m","251":"91m","252":"92m","253":"93m","254":"94m","255":"sync with parameter 1"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter003 : [
        number: 3,
        name: "Ramp Rate - Off to On (Remote)",
        description: "Sets the rate that the light turns on when controlled from the hub. A setting of 'instant' turns the light immediately on.<br>Default=Sync with parameter1",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"1s","102":"2s","103":"3s","104":"4s","105":"5s","106":"6s","107":"7s","108":"8s","109":"9s","110":"10s","111":"11s","112":"12s","113":"13s","114":"14s","115":"15s","116":"16s","117":"17s","118":"18s","119":"19s","120":"20s","121":"21s","122":"22s","123":"23s","124":"24s","125":"25s","126":"26s","127":"27s","128":"28s","129":"29s","130":"30s","131":"31s","132":"32s","133":"33s","134":"34s","135":"35","136":"36s","137":"37s","138":"38s","139":"39s","140":"40s","141":"41s","142":"42s","143":"43s","144":"44s","145":"45s","146":"46s","147":"47s","148":"48s","149":"49s","150":"50s","151":"51s","152":"52s","153":"53s","154":"54s","155":"55s","156":"56s","157":"57s","158":"58s","159":"59s","160":"60s","161":"1m","162":"2m","163":"3m","164":"4m","165":"5m","166":"6m","167":"7m","168":"8m","169":"9m","170":"10m","171":"11m","172":"12m","173":"13m","174":"14m","175":"15m","176":"16m","177":"17m","178":"18m","179":"19m","180":"20m","181":"21m","182":"22m","183":"23m","184":"24m","185":"25m","186":"26m","187":"27m","188":"28m","189":"29m","190":"30m","191":"31m","192":"32m","193":"33m","194":"34m","195":"35m","196":"36m","197":"37m","198":"38m","199":"39m","200":"40m","201":"41m","202":"42m","203":"43m","204":"44m","205":"45m","206":"46m","207":"47m","208":"48m","209":"49m","210":"50m","211":"51m","212":"52m","213":"53m","214":"54m","215":"55m","216":"56m","217":"57m","218":"58m","219":"59m","220":"60m","221":"61m","222":"62m","223":"63m","224":"64m","225":"65m","226":"66m","227":"67m","228":"68m","229":"69m","230":"70m","231":"71m","232":"72m","233":"73m","234":"74m","235":"75m","236":"76m","237":"77m","238":"78m","239":"79m","240":"80m","241":"81m","242":"82m","243":"83m","244":"84m","245":"85m","246":"86m","247":"87m","248":"88m","249":"89m","250":"90m","251":"91m","252":"92m","253":"93m","254":"94m","255":"sync with parameter 1"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter004 : [
        number: 4,
        name: "Ramp Rate - Off to On (Local)",
        description: "Sets the rate that the light turns on when controlled at the switch. A setting of 'instant' turns the light immediately on.<br>Default=Sync with parameter3",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"1s","102":"2s","103":"3s","104":"4s","105":"5s","106":"6s","107":"7s","108":"8s","109":"9s","110":"10s","111":"11s","112":"12s","113":"13s","114":"14s","115":"15s","116":"16s","117":"17s","118":"18s","119":"19s","120":"20s","121":"21s","122":"22s","123":"23s","124":"24s","125":"25s","126":"26s","127":"27s","128":"28s","129":"29s","130":"30s","131":"31s","132":"32s","133":"33s","134":"34s","135":"35","136":"36s","137":"37s","138":"38s","139":"39s","140":"40s","141":"41s","142":"42s","143":"43s","144":"44s","145":"45s","146":"46s","147":"47s","148":"48s","149":"49s","150":"50s","151":"51s","152":"52s","153":"53s","154":"54s","155":"55s","156":"56s","157":"57s","158":"58s","159":"59s","160":"60s","161":"1m","162":"2m","163":"3m","164":"4m","165":"5m","166":"6m","167":"7m","168":"8m","169":"9m","170":"10m","171":"11m","172":"12m","173":"13m","174":"14m","175":"15m","176":"16m","177":"17m","178":"18m","179":"19m","180":"20m","181":"21m","182":"22m","183":"23m","184":"24m","185":"25m","186":"26m","187":"27m","188":"28m","189":"29m","190":"30m","191":"31m","192":"32m","193":"33m","194":"34m","195":"35m","196":"36m","197":"37m","198":"38m","199":"39m","200":"40m","201":"41m","202":"42m","203":"43m","204":"44m","205":"45m","206":"46m","207":"47m","208":"48m","209":"49m","210":"50m","211":"51m","212":"52m","213":"53m","214":"54m","215":"55m","216":"56m","217":"57m","218":"58m","219":"59m","220":"60m","221":"61m","222":"62m","223":"63m","224":"64m","225":"65m","226":"66m","227":"67m","228":"68m","229":"69m","230":"70m","231":"71m","232":"72m","233":"73m","234":"74m","235":"75m","236":"76m","237":"77m","238":"78m","239":"79m","240":"80m","241":"81m","242":"82m","243":"83m","244":"84m","245":"85m","246":"86m","247":"87m","248":"88m","249":"89m","250":"90m","251":"91m","252":"92m","253":"93m","254":"94m","255":"sync with parameter 3"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter005 : [
        number: 5,
        name: "Dimming Speed - Down (Remote)",
        description: "Sets the rate that the light dims down when controlled from the hub. A setting of 'instant' turns the light immediately off.<br>Default=Sync with parameter1",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"1s","102":"2s","103":"3s","104":"4s","105":"5s","106":"6s","107":"7s","108":"8s","109":"9s","110":"10s","111":"11s","112":"12s","113":"13s","114":"14s","115":"15s","116":"16s","117":"17s","118":"18s","119":"19s","120":"20s","121":"21s","122":"22s","123":"23s","124":"24s","125":"25s","126":"26s","127":"27s","128":"28s","129":"29s","130":"30s","131":"31s","132":"32s","133":"33s","134":"34s","135":"35","136":"36s","137":"37s","138":"38s","139":"39s","140":"40s","141":"41s","142":"42s","143":"43s","144":"44s","145":"45s","146":"46s","147":"47s","148":"48s","149":"49s","150":"50s","151":"51s","152":"52s","153":"53s","154":"54s","155":"55s","156":"56s","157":"57s","158":"58s","159":"59s","160":"60s","161":"1m","162":"2m","163":"3m","164":"4m","165":"5m","166":"6m","167":"7m","168":"8m","169":"9m","170":"10m","171":"11m","172":"12m","173":"13m","174":"14m","175":"15m","176":"16m","177":"17m","178":"18m","179":"19m","180":"20m","181":"21m","182":"22m","183":"23m","184":"24m","185":"25m","186":"26m","187":"27m","188":"28m","189":"29m","190":"30m","191":"31m","192":"32m","193":"33m","194":"34m","195":"35m","196":"36m","197":"37m","198":"38m","199":"39m","200":"40m","201":"41m","202":"42m","203":"43m","204":"44m","205":"45m","206":"46m","207":"47m","208":"48m","209":"49m","210":"50m","211":"51m","212":"52m","213":"53m","214":"54m","215":"55m","216":"56m","217":"57m","218":"58m","219":"59m","220":"60m","221":"61m","222":"62m","223":"63m","224":"64m","225":"65m","226":"66m","227":"67m","228":"68m","229":"69m","230":"70m","231":"71m","232":"72m","233":"73m","234":"74m","235":"75m","236":"76m","237":"77m","238":"78m","239":"79m","240":"80m","241":"81m","242":"82m","243":"83m","244":"84m","245":"85m","246":"86m","247":"87m","248":"88m","249":"89m","250":"90m","251":"91m","252":"92m","253":"93m","254":"94m","255":"sync with parameter 1"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter006 : [
        number: 6,
        name: "Dimming Speed - Down (Local)",
        description: "Sets the rate that the light dims down when controlled at the switch. A setting of 'instant' turns the light immediately off.<br>Default=Sync with parameter2",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"1s","102":"2s","103":"3s","104":"4s","105":"5s","106":"6s","107":"7s","108":"8s","109":"9s","110":"10s","111":"11s","112":"12s","113":"13s","114":"14s","115":"15s","116":"16s","117":"17s","118":"18s","119":"19s","120":"20s","121":"21s","122":"22s","123":"23s","124":"24s","125":"25s","126":"26s","127":"27s","128":"28s","129":"29s","130":"30s","131":"31s","132":"32s","133":"33s","134":"34s","135":"35","136":"36s","137":"37s","138":"38s","139":"39s","140":"40s","141":"41s","142":"42s","143":"43s","144":"44s","145":"45s","146":"46s","147":"47s","148":"48s","149":"49s","150":"50s","151":"51s","152":"52s","153":"53s","154":"54s","155":"55s","156":"56s","157":"57s","158":"58s","159":"59s","160":"60s","161":"1m","162":"2m","163":"3m","164":"4m","165":"5m","166":"6m","167":"7m","168":"8m","169":"9m","170":"10m","171":"11m","172":"12m","173":"13m","174":"14m","175":"15m","176":"16m","177":"17m","178":"18m","179":"19m","180":"20m","181":"21m","182":"22m","183":"23m","184":"24m","185":"25m","186":"26m","187":"27m","188":"28m","189":"29m","190":"30m","191":"31m","192":"32m","193":"33m","194":"34m","195":"35m","196":"36m","197":"37m","198":"38m","199":"39m","200":"40m","201":"41m","202":"42m","203":"43m","204":"44m","205":"45m","206":"46m","207":"47m","208":"48m","209":"49m","210":"50m","211":"51m","212":"52m","213":"53m","214":"54m","215":"55m","216":"56m","217":"57m","218":"58m","219":"59m","220":"60m","221":"61m","222":"62m","223":"63m","224":"64m","225":"65m","226":"66m","227":"67m","228":"68m","229":"69m","230":"70m","231":"71m","232":"72m","233":"73m","234":"74m","235":"75m","236":"76m","237":"77m","238":"78m","239":"79m","240":"80m","241":"81m","242":"82m","243":"83m","244":"84m","245":"85m","246":"86m","247":"87m","248":"88m","249":"89m","250":"90m","251":"91m","252":"92m","253":"93m","254":"94m","255":"sync with parameter 2"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter007 : [
        number: 7,
        name: "Ramp Rate - On to Off (Remote)",
        description: "Sets the rate that the light turns off when controlled from the hub. A setting of 'instant' turns the light immediately off.<br>Default=Sync with parameter3",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"1s","102":"2s","103":"3s","104":"4s","105":"5s","106":"6s","107":"7s","108":"8s","109":"9s","110":"10s","111":"11s","112":"12s","113":"13s","114":"14s","115":"15s","116":"16s","117":"17s","118":"18s","119":"19s","120":"20s","121":"21s","122":"22s","123":"23s","124":"24s","125":"25s","126":"26s","127":"27s","128":"28s","129":"29s","130":"30s","131":"31s","132":"32s","133":"33s","134":"34s","135":"35","136":"36s","137":"37s","138":"38s","139":"39s","140":"40s","141":"41s","142":"42s","143":"43s","144":"44s","145":"45s","146":"46s","147":"47s","148":"48s","149":"49s","150":"50s","151":"51s","152":"52s","153":"53s","154":"54s","155":"55s","156":"56s","157":"57s","158":"58s","159":"59s","160":"60s","161":"1m","162":"2m","163":"3m","164":"4m","165":"5m","166":"6m","167":"7m","168":"8m","169":"9m","170":"10m","171":"11m","172":"12m","173":"13m","174":"14m","175":"15m","176":"16m","177":"17m","178":"18m","179":"19m","180":"20m","181":"21m","182":"22m","183":"23m","184":"24m","185":"25m","186":"26m","187":"27m","188":"28m","189":"29m","190":"30m","191":"31m","192":"32m","193":"33m","194":"34m","195":"35m","196":"36m","197":"37m","198":"38m","199":"39m","200":"40m","201":"41m","202":"42m","203":"43m","204":"44m","205":"45m","206":"46m","207":"47m","208":"48m","209":"49m","210":"50m","211":"51m","212":"52m","213":"53m","214":"54m","215":"55m","216":"56m","217":"57m","218":"58m","219":"59m","220":"60m","221":"61m","222":"62m","223":"63m","224":"64m","225":"65m","226":"66m","227":"67m","228":"68m","229":"69m","230":"70m","231":"71m","232":"72m","233":"73m","234":"74m","235":"75m","236":"76m","237":"77m","238":"78m","239":"79m","240":"80m","241":"81m","242":"82m","243":"83m","244":"84m","245":"85m","246":"86m","247":"87m","248":"88m","249":"89m","250":"90m","251":"91m","252":"92m","253":"93m","254":"94m","255":"sync with parameter 3"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter008 : [
        number: 8,
        name: "Ramp Rate - On to Off (Local)",
        description: "Sets the rate that the light turns off when controlled at the switch. A setting of 'instant' turns the light immediately off.<br>Default=Sync with parameter4",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"1s","102":"2s","103":"3s","104":"4s","105":"5s","106":"6s","107":"7s","108":"8s","109":"9s","110":"10s","111":"11s","112":"12s","113":"13s","114":"14s","115":"15s","116":"16s","117":"17s","118":"18s","119":"19s","120":"20s","121":"21s","122":"22s","123":"23s","124":"24s","125":"25s","126":"26s","127":"27s","128":"28s","129":"29s","130":"30s","131":"31s","132":"32s","133":"33s","134":"34s","135":"35","136":"36s","137":"37s","138":"38s","139":"39s","140":"40s","141":"41s","142":"42s","143":"43s","144":"44s","145":"45s","146":"46s","147":"47s","148":"48s","149":"49s","150":"50s","151":"51s","152":"52s","153":"53s","154":"54s","155":"55s","156":"56s","157":"57s","158":"58s","159":"59s","160":"60s","161":"1m","162":"2m","163":"3m","164":"4m","165":"5m","166":"6m","167":"7m","168":"8m","169":"9m","170":"10m","171":"11m","172":"12m","173":"13m","174":"14m","175":"15m","176":"16m","177":"17m","178":"18m","179":"19m","180":"20m","181":"21m","182":"22m","183":"23m","184":"24m","185":"25m","186":"26m","187":"27m","188":"28m","189":"29m","190":"30m","191":"31m","192":"32m","193":"33m","194":"34m","195":"35m","196":"36m","197":"37m","198":"38m","199":"39m","200":"40m","201":"41m","202":"42m","203":"43m","204":"44m","205":"45m","206":"46m","207":"47m","208":"48m","209":"49m","210":"50m","211":"51m","212":"52m","213":"53m","214":"54m","215":"55m","216":"56m","217":"57m","218":"58m","219":"59m","220":"60m","221":"61m","222":"62m","223":"63m","224":"64m","225":"65m","226":"66m","227":"67m","228":"68m","229":"69m","230":"70m","231":"71m","232":"72m","233":"73m","234":"74m","235":"75m","236":"76m","237":"77m","238":"78m","239":"79m","240":"80m","241":"81m","242":"82m","243":"83m","244":"84m","245":"85m","246":"86m","247":"87m","248":"88m","249":"89m","250":"90m","251":"91m","252":"92m","253":"93m","254":"94m","255":"sync with parameter 5"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter009 : [
        number: 9,
        name: "Minimum Level",
        description: "The minimum level that the light can be dimmed. Useful when the user has a light that does not turn on or flickers at a lower level.",
        range: "1..54",
        default: 1,
        size: 1,
        type: "number",
        value: null
        ],
    parameter010 : [
        number: 10,
        name: "Maximum Level",
        description: "The maximum level that the light can be dimmed. Useful when the user wants to limit the maximum brighness.",
        range: "55..99",
        default: 99,
        size: 1,
        type: "number",
        value: null
        ],
    parameter011 : [
        number: 11,
        name: "Invert Switch",
        description: "Inverts the orientation of the switch. Useful when the switch is installed upside down. Essentially up becomes down and down becomes up.",
        range: ["0":"No (default)", "1":"Yes"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter012 : [
        number: 12,
        name: "Auto Off Timer",
        description: "Automatically turns the switch off after this many seconds. When the switch is turned on a timer is started. When the timer expires the switch turns off.<br>0=Auto Off Disabled.",
        range: "0..32767",
        default: 0,
        size: 2,
        type: "number",
        value: null
        ],
    parameter013 : [
        number: 13,
        name: "Default Level (Local)",
        description: "Default level for the dimmer when turned on at the switch.<br>1-99=Set Level<br>0=Use previous level.",
        range: "0..99",
        default: 0,
        size: 1,
        type: "number",
        value: null
        ],
    parameter014 : [
        number: 14,
        name: "Default Level (Remote)",
        description: "Default level for the dimmer when turned on from the hub.<br>1-99=Set Level<br>0=Use previous level.",
        range: "0..99",
        default: 0,
        size: 1,
        type: "number",
        value: null
        ],
    parameter015 : [
        number: 15,
        name: "Level After Power Restored",
        description: "Level the dimmer will return to when power is restored after power failure (if Switch is in On/Off Mode any level 1-99 will convert to 99).<br>0=Off<br>1-99=Set Level<br>100=Use previous level.",
        range: "0..100",
        default: 100,
        size: 1,
        type: "number",
        value: null
        ],
    parameter017 : [
        number: 17,
        name: "Load Level Indicator Timeout",
        description: "Shows the level that the load is at for x number of seconds after the load is adjusted and then returns to the Default LED state.",
        range: ["0":"Do not display Load Level","1":"1 Second","2":"2 Seconds","3":"3 Seconds","4":"4 Seconds","5":"5 Seconds","6":"6 Seconds","7":"7 Seconds","8":"8 Seconds","9":"9 Seconds","10":"10 Seconds","11":"Display Load Level with no timeout (default)"],
        default: 11,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter018 : [
        number: 18,
        name: "Active Power Reports",
        description: "Percent power level change that will result in a new power report being sent.<br>0 = Disabled",
        range: "0..100",
        default: 10,
        size: 1,
        type: "number",
        value: null
        ],
    parameter019 : [
        number: 19,
        name: "Periodic Power & Energy Reports",
        description: "Time period between consecutive power & energy reports being sent (in seconds). The timer is reset after each report is sent.",
        range: "0..32767",
        default: 3600,
        size: 2,
        type: "number",
        value: null
        ],
    parameter020 : [
        number: 20,
        name: "Active Energy Reports",
        description: "Energy level change that will result in a new energy report being sent.<br>0 = Disabled<br>1-32767 = 0.01kWh-327.67kWh.",
        range: "0..32767",
        default: 10,
        size: 2,
        type: "number",
        value: null
        ],
    parameter021 : [
        number: 21,
        name: "Power Source (read only)",
        description: "Neutral or Non-Neutral wiring is automatically sensed.",
        range: [0:"Non Neutral", 1:"Neutral"],
        default: 1,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter022 : [
        number: 22,
        name: "Aux Switch Type",
        description: "Set the Aux switch type for VZW32-SN mmWave switch",
        range: ["0":"Single Pole", "1":"Aux Switch (default)"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter023 : [
        number: 23,
        name: "Quick Start Time",
        description: "Duration of initial increased power output when light turns on. 0=Disabled. Unit: 1/60 seconds (e.g. 60 = 1 second).",
        range: "0..60",
        default: 0,
        size: 1,
        type: "number",
        value: null
        ],
    parameter024 : [
        number: 24,
        name: "Quick Start Level",
        description: "Level of initial increased power output when light turns on.",
        range: "1..99",
        default: 99,
        size: 1,
        type: "number",
        value: null
        ],
    parameter025 : [
        number: 25,
        name: "Higher Output in non-Neutral",
        description: "Ability to increase level in non-neutral mode but may cause problems with high level ficker or aux switch detection. Adjust max level (P10) if you have problems with this enabled.",
        range: ["0":"Disabled (default)","1":"Enabled"],
        default:0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter026 : [
        number: 26,
        name: "Dimming Mode (Leading or Trailing)",
        description: "Change the dimming mode to leading or trailing. Leading is the default mode, but trailing will sometimes work better with some LEDs.",
        range: ["0":"Leading (default)","1":"Trailing"],
        default:0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter030 : [
        number: 30,
        name: "non-Neutral AUX medium gear learn value (read only)",
        description: "In the case of non-neutral, to make the AUX switch better compatible.",
        range: "0..255",
        default: 90,
        size: 1,
        type: "number",
        value: null
        ],
    parameter031 : [
        number: 31,
        name: "non-Neutral AUX low gear learn value (read only)",
        description: "In the case of non-neutral, to make the AUX switch better compatible.",
        range: "0..255",
        default: 110,
        size: 1,
        type: "number",
        value: null
        ],
    parameter032 : [
        number: 32,
        name: "Internal Temperature (read only)",
        description: "Internal temperature in Celsius",
        range: "0..100",
        default: 25,
        size: 1,
        type: "number",
        value: null
        ],
    parameter033 : [
        number: 33,
        name: "Overheat indicator (read only)",
        description: "Indicates if switch is in overheat protection mode",
        range: "0..1",
        default: 0,
        size: 1,
        type: "number",
        value: null
        ],
    parameter050 : [
        number: 50,
        name: "Button Press Delay",
        description: "Adjust the button delay used in scene control. 0=no delay (disables multi-tap scenes), Default=500ms",
        range: ["0":"0ms","3":"300ms","4":"400ms","5":"500ms (default)","6":"600ms","7":"700ms","8":"800ms","9":"900ms"],
        default: 5,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter051 : [
        number: 51,
        name: "Device Bind Number (read only)",
        description: "Number of devices currently bound and counts one group as two devices.",
        range: "0..255",
        default: 0,
        size: 1,
        type: "number",
        value: null
        ],
    parameter052 : [
        number: 52,
        name: "Smart Bulb Mode",
        description: "For use with Smart Bulbs that need constant power and are controlled via commands rather than power.",
        range: ["0":"Disabled (default)", "1":"Enabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter053 : [
        number: 53,
        name: "Double-Tap UP to parameter 55",
        description: "Enable or Disable setting brightness to parameter 55 on double-tap UP.",
        range: ["0":"Disabled (default)", "1":"Enabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter054 : [
        number: 54,
        name: "Double-Tap DOWN to parameter 56",
        description: "Enable or Disable setting brightness to parameter 56 on double-tap DOWN.",
        range: ["0":"Disabled (default)", "1":"Enabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter055 : [
        number: 55,
        name: "Brightness level for Double-Tap UP",
        description: "Set this level on double-tap UP (if enabled by P53)",
        range: "1..99",
        default: 99,
        size: 1,
        type: "number",
        value: null
        ],
    parameter056 : [
        number: 56,
        name: "Brightness level for Double-Tap DOWN",
        description: "Set this level on double-tap DOWN (if enabled by P54)",
        range: "0..99",
        default: 1,
        size: 1,
        type: "number",
        value: null
        ],
    parameter058 : [
        number: 58,
        name: "Exclusion Behavior",
        description: "How device behaves during Exclusion",
        range: ["0":"LED Bar does not pulse", "1":"LED Bar pulses blue (default)", "2":"Device does not enter exclusion mode (requires factory reset to leave network or change this parameter)"],
        default: 1,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter059 : [
        number: 59,
        name: "Association Behavior",
        description: "Choose when the switch sends commands to associated devices",
        range: ["0":"Never", "1":"Local (default)", "2":"Z-Wave", "3":"Both"],
        default: 1,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter060 : [
        number: 60,
        name: "LED1 Color (when On)",
        description: "Set the color of LED1 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter061 : [
        number: 61,
        name: "LED1 Color (when Off)",
        description: "Set the color of LED1 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter062 : [
        number: 62,
        name: "LED1 Intensity (when On)",
        description: "Set the intensity of LED1 when the load is on.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter063 : [
        number: 63,
        name: "LED1 Intensity (when Off)",
        description: "Set the intensity of LED1 when the load is off.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter064 : [
        number: 64,
        name: "LED #1 Notification",
        description: "4-byte encoded LED #1 Notification (duration, level, color, effect)",
        range: "0..4294967295",
        default: 0xFF000000,
        size: 4,
        type: "text",
        value: null
        ],
    parameter065 : [
        number: 65,
        name: "LED2 Color (when On)",
        description: "Set the color of LED2 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter066 : [
        number: 66,
        name: "LED2 Color (when Off)",
        description: "Set the color of LED2 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter067 : [
        number: 67,
        name: "LED2 Intensity (when On)",
        description: "Set the intensity of LED2 when the load is on.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter068 : [
        number: 68,
        name: "LED2 Intensity (when Off)",
        description: "Set the intensity of LED2 when the load is off.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter069 : [
        number: 69,
        name: "LED #2 Notification",
        description: "4-byte encoded LED #2 Notification (duration, level, color, effect). Enter decimal value, no commas.",
        range: "0..4294967295",
        default: 0xFF000000,
        size: 4,
        type: "text",
        value: null
        ],
    parameter070 : [
        number: 70,
        name: "LED3 Color (when On)",
        description: "Set the color of LED3 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter071 : [
        number: 71,
        name: "LED3 Color (when Off)",
        description: "Set the color of LED3 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter072 : [
        number: 72,
        name: "LED3 Intensity (when On)",
        description: "Set the intensity of LED3 when the load is on.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter073 : [
        number: 73,
        name: "LED3 Intensity (when Off)",
        description: "Set the intensity of LED3 when the load is off.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter074 : [
        number: 74,
        name: "LED #3 Notification",
        description: "4-byte encoded LED3 Notification",
        range: "0..4294967295",
        default: 0xFF000000,
        size: 4,
        type: "text",
        value: null
        ],
    parameter075 : [
        number: 75,
        name: "LED4 Color (when On)",
        description: "Set the color of LED4 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter076 : [
        number: 76,
        name: "LED4 Color (when Off)",
        description: "Set the color of LED4 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter077 : [
        number: 77,
        name: "LED4 Intensity (when On)",
        description: "Set the intensity of LED4 when the load is on.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter078 : [
        number: 78,
        name: "LED4 Intensity (when Off)",
        description: "Set the intensity of LED4 when the load is off.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter079 : [
        number: 79,
        name: "LED #4 Notification",
        description: "4-byte encoded LED4 Notification",
        range: "0..4294967295",
        default: 0xFF000000,
        size: 4,
        type: "text",
        value: null
        ],
    parameter080 : [
        number: 80,
        name: "LED5 Color (when On)",
        description: "Set the color of LED5 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter081 : [
        number: 81,
        name: "LED5 Color (when Off)",
        description: "Set the color of LED5 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter082 : [
        number: 82,
        name: "LED5 Intensity (when On)",
        description: "Set the intensity of LED5 when the load is on.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter083 : [
        number: 83,
        name: "LED5 Intensity (when Off)",
        description: "Set the intensity of LED5 when the load is off.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter084 : [
        number: 84,
        name: "LED #5 Notification",
        description: "4-byte encoded LED5 Notification",
        range: "0..4294967295",
        default: 0xFF000000,
        size: 4,
        type: "text",
        value: null
        ],
    parameter085 : [
        number: 85,
        name: "LED6 Color (when On)",
        description: "Set the color of LED6 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter086 : [
        number: 86,
        name: "LED6 Color (when Off)",
        description: "Set the color of LED6 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter087 : [
        number: 87,
        name: "LED6 Intensity (when On)",
        description: "Set the intensity of LED6 when the load is on.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter088 : [
        number: 88,
        name: "LED6 Intensity (when Off)",
        description: "Set the intensity of LED6 when the load is off.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter089 : [
        number: 89,
        name: "LED #6 Notification",
        description: "4-byte encoded LED6 Notification",
        range: "0..4294967295",
        default: 0xFF000000,
        size: 4,
        type: "text",
        value: null
        ],
    parameter090 : [
        number: 90,
        name: "LED7 Color (when On)",
        description: "Set the color of LED7 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter091 : [
        number: 91,
        name: "LED7 Color (when Off)",
        description: "Set the color of LED7 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter092 : [
        number: 92,
        name: "LED7 Intensity (when On)",
        description: "Set the intensity of LED7 when the load is on.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter093 : [
        number: 93,
        name: "LED7 Intensity (when Off)",
        description: "Set the intensity of LED7 when the load is off.",
        range: "0..101",
        default: 101,
        size: 1,
        type: "number",
        value: null
        ],
    parameter094 : [
        number: 94,
        name: "LED #7 Notification",
        description: "4-byte encoded LED Notification",
        range: "0..4294967295",
        default: 0xFF000000,
        size: 4,
        type: "text",
        value: null
        ],
    parameter095 : [
        number: 95,
        name: "LED Bar Color (when On)",
        description: "Set the color of the LED Bar when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 170,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter096 : [
        number: 96,
        name: "LED Bar Color (when Off)",
        description: "Set the color of the LED Bar when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 170,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter097 : [
        number: 97,
        name: "LED Bar Intensity (when On)",
        description: "Set the intensity of the LED Bar when the load is on.",
        range: "0..100",
        default: 33,
        size: 1,
        type: "number",
        value: null
        ],
    parameter098 : [
        number: 98,
        name: "LED Bar Intensity (when Off)",
        description: "Set the intensity of the LED Bar when the load is off.",
        range: "0..100",
        default: 3,
        size: 1,
        type: "number",
        value: null
        ],
    parameter099 : [
        number: 99,
        name: "All LED Notification",
        description: "4-byte encoded All LED Notification (duration, level, color, effect)",
        range: "0..4294967295",
        default: 0xFF000000,
        size: 4,
        type: "text",
        value: null
        ],
    parameter100 : [
        number: 100,
        name: "LED Bar Scaling",
        description: "Method used for scaling.  This allows you to match the scaling when two different generations are in the same gang box",
        range: ["0":"Gen3 method (VZM-style)","1":"Gen2 method (LZW-style)"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter101 : [
        number: 101,
        name: "mmWave Height Minimum (Floor)",
        description: "Defines the detection area (negative values are below the switch, positive values are above). Values are in cm.",
        range: "-600..600",
        default: -600,
        size: 2,
        type: "number",
        value: null
        ],
    parameter102 : [
        number: 102,
        name: "mmWave Height Maximum (Ceiling)",
        description: "Defines the detection area (negative values are below the switch, positive values are above). Values are in cm.",
        range: "-600..600",
        default: 600,
        size: 2,
        type: "number",
        value: null
        ],
    parameter103 : [
        number: 103,
        name: "mmWave Width Minimum (Left)",
        description: "Defines the detection area (negative values are left of the switch facing away from the wall, positive values are right). Values are in cm.",
        range: "-600..600",
        default: -600,
        size: 2,
        type: "number",
        value: null
        ],
    parameter104 : [
        number: 104,
        name: "mmWave Width Maximum (Right)",
        description: "Defines the detection area (negative values are left of the switch facing away from the wall, positive values are right). Values are in cm.",
        range: "-600..600",
        default: 600,
        size: 2,
        type: "number",
        value: null
        ],
    parameter105 : [
        number: 105,
        name: "mmWave Depth Minimum (Near)",
        description: "Defines the detection area (from the switch forward). Values are in cm.",
        range: "0..600",
        default: 0,
        size: 2,
        type: "number",
        value: null
        ],
    parameter106 : [
        number: 106,
        name: "mmWave Depth Maximum (Far)",
        description: "Defines the detection area (from the switch forward). Values are in cm.",
        range: "0..600",
        default: 600,
        size: 2,
        type: "number",
        value: null
        ],
    parameter107 : [
        number: 107,
        name: "mmWave Target Info Report",
        description: "Enable/disable mmWave target information reporting",
        range: ["0":"Disabled",
                "1":"Enabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter108 : [
        number: 108,
        name: "mmWave Stay Life",
        description: "Time in seconds before reporting no motion after target leaves detection area",
        range: "0..3600",
        default: 300,
        size: 4,
        type: "number",
        value: null
        ],
    parameter109 : [
        number: 109,
        name: "UTC Time Range",
        description: "Time range in UTC format. First byte: start hour, second byte: start minutes, third byte: stop hour, fourth byte: stop minutes",
        range: "0..991378199",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter110 : [
        number: 110,
        name: "Light On Presence Behavior",
        description: "When presence is detected, choose how to control the light load",
        range: ["0":"Disabled","1":"Auto On/Off when occupied: light is on when room is occupied, off when unoccupied (default)","2":"Auto Off when vacant: light turns off when room becomes vacant, never automatically turns on","3":"Auto On when occupied: light turns on when room becomes occupied, never automatically turns off","4":"Auto On/Off when Vacant: light is on when room is vacant, off when occupied","5":"Auto On when Vacant: light turns on when room becomes vacant, never automatically turns off","6":"Auto Off when Occupied: light turns off when room becomes occupied, never automatically turns on"],
        default: 1,
        size: 1,
        type: "enum",
        value: null
        ],
     parameter111 : [
        number: 111,
        name: "mmWave Module Commands",
        description: "Various commands to view and edit mmwave functionality. See documentation",
        range: ["0":"Restore the mmwave module factory configuration",
                "1":"The interference area is automatically generated",
                "2":"Obtains the interference region and detection region",
                "3":"Clears the interference area",
                "4":"Resets the detection area",
                "5":"Clears the stay area"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter112 : [
        number: 112,
        name: "mmWave Sensitivity",
        description: "Adjust the sensitivity of the mmWave sensor. 0-Low, 1-Medium, 2-High",
        range: "0..2",
        default: 2,
        size: 1,
        type: "number",
        value: null
        ],
    parameter113 : [
        number: 113,
        name: "mmWave Trigger Speed",
        description: "Adjust the trigger speed of the mmWave sensor. 0-Slow, 1-Medium, 2-Fast",
        range: "0..2",
        default: 2,
        size: 1,
        type: "number",
        value: null
        ],
    parameter114 : [
        number: 114,
        name: "mmWave Detection Timeout",
        description: "Amount of time in seconds after presence detection that a no-presence report is sent.",
        range: "0..4294967296",
        default: 30,
        size: 4,
        type: "number",
        value: null
        ],
    parameter115 : [
        number: 115,
        name: "mmWave Firmware Version",
        description: "Firmware version of the mmWave module. Read Only",
        range: "0..4294967296",
        default: 0,
        size: 4,
        type: "text",
        value: null
        ],
    parameter116 : [
        number: 116,
        name: "mmWave Person in the Reporting area",
        description: "This parameter is composed of four bytes, each byte representing a specific area. Read Only",
        range: "0..16843009",
        default: 0,
        size: 4,
        type: "text",
        value: null
        ],
    parameter117 : [
        number: 117,
        name: "Room Size",
        description: "Sets the x, y, and z dimensions of the room for mmWave detection. Changing this parameter will update parameters 101-106 to reflect the preset.",
        range: [
            "0":"Custom (User-defined)",
            "1":"X-Small (X: −100 to 100, Y: 0 to 200, Z: −100 to 100)",
            "2":"Small (X: −160 to 160, Y: 0 to 280, Z: −100 to 100)",
            "3":"Medium (X: −210 to 210, Y: 0 to 360, Z: −100 to 100)",
            "4":"Large (X: −260 to 260, Y: 0 to 400, Z: −100 to 100)",
            "5":"X-Large (X: −310 to 310, Y: 0 to 460, Z: −100 to 100)"
        ],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter118 : [
        number: 118,
        name: "Lux Threshold",
        description: "Threshold to send lux report.",
        range: "0..32767",
        default: 20,
        size: 2,
        type: "number",
        value: null
        ],
    parameter119 : [
        number: 119,
        name: "Lux Interval",
        description: "Interval, in seconds, to send Lux reports.",
        range: "0..32767",
        default: 600,
        size: 2,
        type: "number",
        value: null
        ],
    parameter120 : [
        number: 120,
        name: "Single Tap Handling",
        description: "What happens when the switch is single-tapped",
        range: ["0":"Up - On, down - Off","1":"Up - Increment level up (Off > low > medium > high), down - Increment level down","2":"Up - Increment level up (Off > low > medium > high > low...), down - Off"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter123 : [
        number: 123,
        name: "Aux Switch Unique Scenes",
        description: "Have unique scene numbers for scenes activated with the aux switch",
        range: ["0":"Disabled (default)","1":"Enabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter125 : [
        number: 125,
        name: "Binding Off-to-On Sync Level",
        description: "Send Move_To_Level using Default Level with Off/On to bound devices",
        range: ["0":"Disabled (default)","1":"Enabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter130 : [
        number: 130,
        name: "Z-Wave Association Device Control (Group 7) – Enabled/Disabled",
        description: "Enable or disable Z-Wave association group 7 device control",
        range: ["0":"Disabled","1":"Enabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter131 : [
        number: 131,
        name: "Z-Wave Association Device Control – Preset Value #1",
        description: "Low level to use when group 7 cycles levels",
        range: "1..99",
        default: 25,
        size: 1,
        type: "number",
        value: null
        ],
    parameter132 : [
        number: 132,
        name: "Z-Wave Association Device Control – Preset Value #2",
        description: "Medium level to use when group 7 cycles levels",
        range: "1..99",
        default: 50,
        size: 1,
        type: "number",
        value: null
        ],
    parameter133 : [
        number: 133,
        name: "Z-Wave Association Device Control – Preset Value #3",
        description: "High level to use when group 7 cycles levels",
        range: "1..99",
        default: 99,
        size: 1,
        type: "number",
        value: null
        ],
    parameter134 : [
        number: 134,
        name: "Z-Wave Association Device Control – LED Bar Color",
        description: "Color for the LED bar while briefly indicating group 7 command transmission",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter156 : [
        number: 156,
        name: "Local Protection",
        description: "Ability to control switch from the wall.",
        range: ["0":"Local control enabled (default)", "1":"Local control disabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ] ,
    parameter157 : [
        number: 157,
        name: "Remote Protection (read only) <i>use Remote Control command to change.</i>",
        description: "Ability to control switch from the hub.",
        range: ["0":"Remote control enabled (default)", "1":"Remote control disabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter158 : [
        number: 158,
        name: "Switch Mode",
        description: "Dimmer or On/Off only",
        range: ["0":"Dimmer", "1":"On/Off (default)"],
        default: 1,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter159 : [
        number: 159,
        name: "LED Bar in On/Off Switch Mode",
        description: "When the device is in On/Off mode, use full LED bar or just one LED",
        range: ["0":"Full bar (default)", "1":"One LED"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter160 : [
        number: 160,
        name: "Firmware Update-In-Progess Bar",
        description: "Display firmware update progress on LED Bar",
        range: ["1":"Enabled (default)", "0":"Disabled"],
        default: 1,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter161 : [
        number: 161,
        name: "Relay Click",
        description: "Audible Click in On/Off mode",
        range: ["0":"Enabled (default)", "1":"Disabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter162 : [
        number: 162,
        name: "Double-Tap config to clear notification",
        description: "Double-Tap the Config button to clear notifications",
        range: ["0":"Enabled (default)", "1":"Disabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter163 : [
        number: 163,
        name: "LED bar display levels",
        description: "Levels of the LED bar in Smart Bulb Mode<br>0=full range",
        range: "0..9",
        default: 3,
        size: 1,
        type: "number",
        value: null
        ],
]
private getCommandClassVersions() {
    [0x20: 1, 0x25: 1, 0x70: 1, 0x98: 1, 0x32: 3, 0x5B: 1]
}

def getVersion() {
    if (infoEnable) log.info "${device.displayName} getVersion()"
	def cmds = []
	cmds = [zwave.versionV1.versionGet()]
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

String secure(String cmd){
    return zwaveSecureEncap(cmd)
}

String secure(hubitat.zwave.Command cmd){
    return zwaveSecureEncap(cmd)
}

String secureCmd(cmd) {
    if (getDataValue("zwaveSecurePairingComplete") == "true" && getDataValue("S2") == null) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
		return secure(cmd)
    }	
}

def infoLogsOff() {
    log.warn "${device.displayName} " + fireBrick("Disabling Info logging after timeout")
    device.updateSetting("infoEnable",[value:"false",type:"bool"])
    //device.updateSetting("disableInfoLogging",[value:"",type:"number"])
}

def traceLogsOff() {
    log.warn "${device.displayName} " + fireBrick("Disabling Trace logging after timeout")
    device.updateSetting("traceEnable",[value:"false",type:"bool"])
    //device.updateSetting("disableTraceLogging",[value:"",type:"number"])
}

def debugLogsOff() {
    log.warn "${device.displayName} " + fireBrick("Disabling Debug logging after timeout")
    device.updateSetting("debugEnable",[value:"false",type:"bool"])
    //device.updateSetting("disableDebugLogging",[value:"",type:"number"])
}

															 
def calculateParameter(paramNum) {
	paramNum = paramNum?:0
    // Use toDouble() not toFloat(): Float has ~7 digits precision so values like 67046099 get rounded (e.g. to 67046100)
    def value = Math.round((settings?."parameter${paramNum}"!=null?settings?."parameter${paramNum}":getDefaultValue(paramNum))?.toDouble())?.toInteger()
    switch (paramNum){
        case 9:     //Min Level
			value = Math.min(Math.max(value.toInteger(),1),54)
            break
        case 10:    //Max Level
			value = Math.min(Math.max(value.toInteger(),55),99)
            break
        case 13:    //Default Level (local)
        case 14:    //Default Level (remote)
		case 55:	//Double-Tap UP Level
		case 56:	//Double-Tap DOWN Level
		case 131:   //Group 7 Preset #1
		case 132:   //Group 7 Preset #2
		case 133:   //Group 7 Preset #3
			value = Math.min(Math.max(value.toInteger(),0),99)
            break
        case 23:    //Quick Start Time (0-60, 1/60 sec)
			value = Math.min(Math.max(value.toInteger(),0),60)
            break
        case 24:    //Quick Start Level (1-99)
			value = Math.min(Math.max(value.toInteger(),1),99)
            break
        case 18:    //Active Power Reports (percent change)
        case 97:    //LED Bar Intensity(when On)
        case 98:    //LED Bar Intensity(when Off)
			value = Math.min(Math.max(value.toInteger(),0),100)
            break
        case 95:    //custom hue for LED Bar (when On)
        case 96:    //custom hue for LED Bar (when Off)
            //360-hue values need to be converted to byte values before sending to the device
            if (settings."parameter${paramNum}custom" =~ /^([0-9]{1}|[0-9]{2}|[0-9]{3})$/) {
                value = Math.round((settings."parameter${paramNum}custom").toInteger()/360*255)
            } else {   //else custom hue is invalid format or not selected
                if(settings."parameter${paramNum}custom"!=null) {
                    //device.removeSetting("parameter${paramNum}custom")
                    if (infoEnable||traceEnable||debugEnable) log.warn "${device.displayName} " + fireBrick("Cleared invalid custom hue: ${settings."parameter${paramNum}custom"}")
                }
            }
            break
    }
    return value?:0
}

def clearSetting(i) {
	i = i?:0
	def cleared = false
	if (settings."parameter${i}"!=null)   {cleared=true; device.removeSetting("parameter" + i)}
	if (state."parameter${i}value"!=null) {cleared=true; state.remove("parameter" + i + "value")}
	if (cleared && (infoEnable||traceEnable||debugEnable)) log.info "${device.displayName} cleared P${i} since it is the default"
}


def configure(option) {    //THIS GETS CALLED AUTOMATICALLY WHEN NEW DEVICE IS ADDED OR WHEN CONFIGURE BUTTON SELECTED ON DEVICE PAGE
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.displayName} configure($option)"
    state.lastCommandSent =                        "configure($option)"
    state.lastCommandTime = nowFormatted()
    sendEvent(name: "numberOfButtons", value: 14)
    def cmds = []
    cmds += processAssociations()
    cmds += zwave.versionV1.versionGet()
    cmds += getParameter(115)
    if (option=="") {		//IF   we didn't pick an option 
		cmds += refresh()	//THEN refresh read-only and key parameters
    } else { 				//ELSE read device attributes and pass on to update settings.
		if (option=="Default") settings.each {settings.remove(it)}	//if DEFAULT was requested then clear any user settings
		//cmds += readDeviceAttributes()
		cmds += updated(option)
	}
    if (debugEnable) log.debug "${device.displayName} configure $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def convertByteToPercent(int value=0) {                  //convert a 0-254 range where 254=100%.  255 is reserved for special meaning.
    //if (debugEnable) log.debug "${device.displayName} convertByteToPercent(${value})"
    value = value==null?0:value                          //default to 0 if null
    value = Math.min(Math.max(value.toInteger(),0),255)  //make sure input byte value is in the 0-255 range
    value = value>=255?256:value                         //this ensures that byte values of 255 get rounded up to 101%
    value = Math.ceil(value/255*100)                     //convert to 0-100 where 254=100% and 255 becomes 101 for special meaning
    return value
}

def convertPercentToByte(int value=0) {                  //convert a 0-100 range where 100%=254.  255 is reserved for special meaning.
    //if (debugEnable) log.debug "${device.displayName} convertPercentToByte(${value})"
    value = value==null?0:value                          //default to 0 if null
    value = Math.min(Math.max(value.toInteger(),0),101)  //make sure input percent value is in the 0-101 range
    value = Math.floor(value/100*255)                    //convert to 0-255 where 100%=254 and 101 becomes 255 for special meaning
    value = value==255?254:value                         //this ensures that 100% rounds down to byte value 254
    value = value>255?255:value                          //this ensures that 101% rounds down to byte value 255
    return value
}

def cycleSpeed() {    // FOR FAN ONLY
    def cmds =[]
    if (parameter158=="1" || parameter258=="1") cmds += toggle()    //if we are in on/off mode then do a toggle instead of cycle
    else {
        def currentLevel = device.currentValue("level")==null?0:device.currentValue("level").toInteger()
        if (device.currentValue("switch")=="off") currentLevel = 0
		boolean smartMode = device.currentValue("smartFan")=="Enabled"
        def newLevel = 0
		def newSpeed =""
		if      (currentLevel<=0 ) {newLevel=20;               newSpeed="low" }
		else if (currentLevel<=20) {newLevel=smartMode?40:60;  newSpeed=smartMode?"medium-low":"medium"}
		else if (currentLevel<=40) {newLevel=60;               newSpeed="medium"}
		else if (currentLevel<=60) {newLevel=smartMode?80:100; newSpeed=smartMode?"medium-high":"high"}
		else if (currentLevel<=80) {newLevel=100;              newSpeed="high"}
        else                       {newLevel=0;                newSpeed="off"}
        if (infoEnable) log.info "${device.displayName} cycleSpeed(${device.currentValue("speed")}->${newSpeed})"
        state.lastCommandSent =                        "cycleSpeed(${device.currentValue("speed")}->${newSpeed})"
        state.lastCommandTime = nowFormatted()
        cmds += zigbee.setLevel(newLevel)
        if (debugEnable) log.debug "${device.displayName} cycleSpeed $cmds"
    }
    return cmds
}

def getDefaultValue(paramNum=0) {
	paramValue=configParams["parameter${paramNum.toString()?.padLeft(3,"0")}"]?.default
	return paramValue?:0
	}
def initialize() {    //CALLED DURING HUB BOOTUP IF "INITIALIZE" CAPABILITY IS DECLARED IN METADATA SECTION
    log.info "${device.displayName} initialize()"
    state.clear()
    state.lastCommandSent = "initialize()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = "VZW32-SN"
    device.removeSetting("parameter23level")
    device.removeSetting("parameter95custom")
    device.removeSetting("parameter96custom")
    def cmds = []
	cmds += ledEffectOne(1234567,255,0,0,0)	//clear any outstanding oneLED Effects
	cmds += ledEffectAll(255,0,0,0)			//clear any outstanding allLED Effects
    cmds += processAssociations()
    cmds += refresh()
    if (debugEnable) log.debug "${device.displayName} initialize $cmds"
    return cmds
}

def installed() {    //THIS IS CALLED WHEN A DEVICE IS INSTALLED
    log.info "${device.displayName} installed()"
    state.lastCommandSent =        "installed()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = "VZW32-SN"
    log.info "${device.displayName} Driver Date $state.driverDate"
    log.info "${device.displayName} Model=$state.model"
    //configure()     //I confirmed configure() gets called at Install time so this isn't needed here
    return
}

def intTo8bitUnsignedHex(value) {
    return zigbee.convertToHexString(value.toInteger(),2)
}

def intTo16bitUnsignedHex(value) {
    return zigbee.convertToHexString(value.toInteger(),4)
}

def intTo32bitUnsignedHex(value) {
    return hexStr = zigbee.convertToHexString(value.toInteger(),8)
}

def ledEffectAll(effect=255, color=0, level=100, duration=60) {
	effect   = effect.toString().split(/=/)[0]
	def effectName = "unknown($effect)"
    switch (effect){
        case "255":	effectName = "Stop"; break
        case "1":	effectName = "Solid"; break
        case "2":	effectName = "Fast Blink"; break
        case "3":	effectName = "Slow Blink"; break
        case "4":	effectName = "Pulse"; break
        case "5":	effectName = "Chase"; break
        case "6":	effectName = "Open/Close"; break
        case "7":	effectName = "Small-to-Big"; break
        case "8":	effectName = "Aurora"; break
        case "9":	effectName = "Slow Falling"; break
        case "10":	effectName = "Medium Falling"; break
        case "11":	effectName = "Fast Falling"; break
        case "12":	effectName = "Slow Rising"; break
        case "13":	effectName = "Medium Rising"; break
        case "14":	effectName = "Fast Rising"; break
        case "15":	effectName = "Medium Blink"; break
        case "16":	effectName = "Slow Chase"; break
        case "17":	effectName = "Fast Chase"; break
        case "18":	effectName = "Fast Siren"; break
        case "19":	effectName = "Slow Siren"; break
        case "0":	effectName = "LEDs Off"; break
		default:	effectName = "Unknown Effect #$effect"; break
	}
    sendEvent(name:"ledEffect", value: "$effectName All")
    if (infoEnable) log.info "${device.displayName} ledEffectAll(${effect},${color},${level},${duration})"
    state.lastCommandSent =                        "ledEffectAll(${effect},${color},${level},${duration})"
    state.lastCommandTime = nowFormatted()
    effect   = Math.min(Math.max((effect!=null?effect:255).toInteger(),0),255)
    color    = Math.min(Math.max((color!=null?color:0).toInteger(),0),255)
    level    = Math.min(Math.max((level!=null?level:100).toInteger(),0),100)
    duration = Math.min(Math.max((duration!=null?duration:60).toInteger(),0),255)
    def cmds =[]
    Integer cmdEffect = effect.toInteger()
    Integer cmdColor = color.toInteger()
    Integer cmdLevel = level.toInteger()
    Integer cmdDuration = duration.toInteger()
    //Integer value = Integer.parseInt("${intTo8bitUnsignedHex(cmdEffect)}${intTo8bitUnsignedHex(cmdColor)}${intTo8bitUnsignedHex(cmdLevel)}${intTo8bitUnsignedHex(cmdDuration)}", 16)
    BigInteger value = new BigInteger("${intTo8bitUnsignedHex(cmdEffect)}${intTo8bitUnsignedHex(cmdColor)}${intTo8bitUnsignedHex(cmdLevel)}${intTo8bitUnsignedHex(cmdDuration)}", 16)
    cmds += zwave.configurationV4.configurationSet(scaledConfigurationValue: value,  parameterNumber: ledNotificationEndpoints[(ep == null)? 0:ep?.toInteger()-1], size: 4)
    cmds += zwave.configurationV4.configurationGet(parameterNumber: ledNotificationEndpoints[(ep == null)? 0:ep?.toInteger()-1])
    if (debugEnable) log.debug "${device.displayName} ledEffectAll $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}
                                        
def ledEffectOne(lednum, effect=255, color=0, level=100, duration=60) {
	lednum   = lednum.toString().split(/ /)[0].replace(",","")
	effect   = effect.toString().split(/=/)[0]
	def effectName = "unknown($effect)"
    switch (effect){
        case "255":	effectName = "Stop"; break
        case "1":	effectName = "Solid"; break
        case "2":	effectName = "Fast Blink"; break
        case "3":	effectName = "Slow Blink"; break
        case "4":	effectName = "Pulse"; break
        case "5":	effectName = "Chase"; break
        case "6":	effectName = "Falling"; break
        case "7":	effectName = "Rising"; break
        case "8":	effectName = "Aurora"; break
        case "0":	effectName = "LEDs Off"; break
		default:	effectName = "Unknown Effect #$effect"; break
	}
	sendEvent(name:"ledEffect", value: "$effectName LED${lednum.toString().split(/ /)[0]}")
    if (infoEnable) log.info "${device.displayName} ledEffectOne(${lednum},${effect},${color},${level},${duration})"
    state.lastCommandSent =                        "ledEffectOne(${lednum},${effect},${color},${level},${duration})"
    state.lastCommandTime = nowFormatted()
    effect   = Math.min(Math.max((effect!=null?effect:255).toInteger(),0),255)
    color    = Math.min(Math.max((color!=null?color:0).toInteger(),0),255)
    level    = Math.min(Math.max((level!=null?level:100).toInteger(),0),100)
    duration = Math.min(Math.max((duration!=null?duration:60).toInteger(),0),255)
    def cmds = []
    lednum.toString().each {
        it= Math.min(Math.max((it!=null?it:1).toInteger(),1),7)
        Integer cmdLedNum = (it.toInteger()-1)*5+64    //param#s for leds 1-7 are 64,69,74,79,84,89,94
        Integer cmdEffect = effect.toInteger()
        Integer cmdColor = color.toInteger()
        Integer cmdLevel = level.toInteger()
        Integer cmdDuration = duration.toInteger()
        BigInteger value = new BigInteger("${intTo8bitUnsignedHex(cmdEffect)}${intTo8bitUnsignedHex(cmdColor)}${intTo8bitUnsignedHex(cmdLevel)}${intTo8bitUnsignedHex(cmdDuration)}", 16)
        cmds += zwave.configurationV4.configurationSet(scaledConfigurationValue: value,  parameterNumber: cmdLedNum, size: 4)
        cmds += zwave.configurationV4.configurationGet(parameterNumber: cmdLedNum)
    }
    if (debugEnable) log.debug "${device.displayName} ledEffectOne $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def nowFormatted() {
    if(location.timeZone) return new Date().format("yyyy-MMM-dd h:mm:ss a", location.timeZone)
    else                  return new Date().format("yyyy MMM dd EEE h:mm:ss a")
}

def off() {
	if (infoEnable) log.info "${device.displayName} off()"
    state.lastCommandSent =                        "off()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zwave.basicV2.basicSet(value: 0x00)
    cmds += zwave.basicV2.basicGet()
    if (debugEnable) log.debug "${device.displayName} off $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def on() {
    if (infoEnable) log.info "${device.displayName} on()"
    state.lastCommandSent =                        "on()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zwave.basicV2.basicSet(value: 0xFF)
    cmds += zwave.basicV2.basicGet()
    if (debugEnable) log.debug "${device.displayName} on $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def parse(String description) {
    if (traceEnable) log.trace "${device.displayName} parse($description)"
    hubitat.zwave.Command cmd = zwave.parse(description,[0x85:1,0x86:2])
    if (cmd) {
        if (debugEnable) log.debug "Parsed ${description} to ${cmd}"
        zwaveEvent(cmd)
    } else {
        if (debugEnable) log.debug "Non-parsed event: ${description}"
    }
}

void zwaveEvent(hubitat.zwave.Command cmd) {
    if (traceEnable) log.trace "${device.displayName} zwaveEvent(${cmd})"
	def parsedCommand = cmd.toString().split("\\(")[0]
	state.lastEventReceived = parsedCommand
    state.lastEventTime = nowFormatted()
	switch(parsedCommand) {
		case "AssociationReport":
			def temp = []
			if (cmd.nodeId != []) {
				cmd.nodeId.each {	//a
				temp += it.toString().format( '%02x', it.toInteger() ).toUpperCase()
				}
			}
			state."actualAssociation${cmd.groupingIdentifier}" = temp
			if (infoEnable) log.info "${device.displayName} Associations for Group ${cmd.groupingIdentifier}: ${temp}"
			updateDataValue("associationGroup${cmd.groupingIdentifier}", "$temp")
			break
		case "AssociationGroupingsReport":
			if (infoEnable) log.info "${device.displayName} Supported association groups: ${cmd.supportedGroupings}"
			//sendEvent(name: "groups", value: cmd.supportedGroupings)
			state.associationGroups = cmd.supportedGroupings
			break
		case "BasicReport":
			if (infoEnable) log.info "${device.displayName} Basic Report: value ${cmd.value ? "on" : "off"} ($cmd.value)"
            cmd.targetValue = cmd.value
			dimmerEvents(cmd, (!state.lastRan || now() <= state.lastRan + 2000)?"digital":"physical")
			break
		case "CentralSceneNotification":
			if (infoEnable) log.info "${device.displayName} ${cmd}"
			switch(zigbee.convertToHexString(cmd.sceneNumber,2) + zigbee.convertToHexString(cmd.keyAttributes,2)) {
				case "0200":    //Tap Up 1x
					buttonEvent(1, "pushed", "physical")
					break
				case "0203":    //Tap Up 2x
					buttonEvent(2, "pushed", "physical")
					break
				case "0204":    //Tap Up 3x
					buttonEvent(3, "pushed", "physical")
					break
				case "0205":    //Tap Up 4x
					buttonEvent(4, "pushed", "physical")
					break
				case "0206":    //Tap Up 5x
					buttonEvent(5, "pushed", "physical")
					break
				case "0202":    //Hold Up
					buttonEvent(6, "pushed", "physical")
					break
				case "0201":    //Release Up
					buttonEvent(7, "pushed", "physical")
					break
				case "0100":    //Tap Down 1x
					buttonEvent(1, "held", "physical")
					break
				case "0103":    //Tap Down 2x
					buttonEvent(2, "held", "physical")
					break
				case "0104":    //Tap Down 3x
					buttonEvent(3, "held", "physical")
					break
				case "0105":    //Tap Down 4x
					buttonEvent(4, "held", "physical")
					break
				case "0106":    //Tap Down 5x
					buttonEvent(5, "held", "physical")
					break
				case "0102":    //Hold Down
					buttonEvent(6, "held", "physical")
					break
				case "0101":    //Release Down
					buttonEvent(7, "held", "physical")
					break
				case "0300":    //Tap Config 1x
					buttonEvent(8, "pushed", "physical")
					break
				case "0303":    //Tap Config 2x
					buttonEvent(9, "pushed", "physical")
					break
				case "0304":    //Tap Config 3x
					buttonEvent(10, "pushed", "physical")
					break
				case "0305":    //Tap Config 4x
					buttonEvent(11, "pushed", "physical")
					break
				case "0306":    //Tap Config 5x
					buttonEvent(12, "pushed", "physical")
					break
				case "0302":    //Hold Config
					buttonEvent(13, "pushed", "physical")
					break
				case "0301":    //Release Config
					buttonEvent(14, "pushed", "physical")
					break
				default:       //undefined button function
					if (infoEnable||traceEnable||debugEnable) log.warn "${device.displayName} " + fireBrick("Undefined button function Scene: ${data[0]} Attributes: ${data[1]}")
					break
			}
			break
		case "ConfigurationReport":
			//if (traceEnable) log.trace "${device.displayName} Received parameter=${cmd?.parameterNumber} value=${cmd?.scaledConfigurationValue} size=${cmd?.size}"
			def attrInt = cmd?.parameterNumber
			def scaled = cmd?.scaledConfigurationValue
			def valueInt = cmd?.size==1?(scaled<0?scaled+0x100:scaled):cmd.size==4?(scaled<0?scaled+0x100000000:scaled):scaled
			// Some hubs report P101-106 (2-byte) as size 1; scaledConfigurationValue still has the correct signed 16-bit value
			if (attrInt?.toInteger() in [101,102,103,104,105,106] && cmd?.size == 1 && scaled != null) valueInt = scaled
			def valueStr = valueInt.toString()
			def valueHex = intTo32bitUnsignedHex(valueInt)
			def infoDev = "${device.displayName} "
			def infoTxt = "Config Report: P${attrInt}=${valueInt}"
			def infoMsg = infoDev + infoTxt
			if (attrInt>=1 && attrInt<=8) {
				if      (valueInt<101) valueStr=(valueInt/10).toString()+"s)"
				else if (valueInt<161) valueStr=(valueInt-100).toString()+"s)"
				else if (valueInt<255) valueStr=(valueInt-160).toString()+"m)"
			}
            switch (attrInt) {
					case 0:
						infoMsg += " (temporarily stored level during transitions)"
						break
					case 1:
						infoMsg += " (Remote Dim Rate Up: " + (valueInt<255?valueStr:"default)")
						break
					case 2:
						infoMsg += " (Local Dim Rate Up: " + (valueInt<255?valueStr:"sync with 1)")
						break
					case 3:
						infoMsg += " (Remote Ramp Rate On: " + (valueInt<255?valueStr:"sync with 1)")
						break
					case 4:
						infoMsg += " (Local Ramp Rate On: " + (valueInt<255?valueStr:"sync with 3)")
						break
					case 5:
						infoMsg += " (Remote Dim Rate Down: " + (valueInt<255?valueStr:"sync with 1)")
						break
					case 6:
						infoMsg += " (Local Dim Rate Down: " + (valueInt<255?valueStr:"sync with 2)")
                        break
                    case 7:
                        infoMsg += " (Remote Ramp Rate Off: " + (valueInt<255?valueStr:"sync with 3)")
                        break
                    case 8:
                        infoMsg += " (Local  Ramp Rate Off: " + (valueInt<255?valueStr:"sync with 4)")
                        break
                    case 9:     //Min Level
                        infoMsg += " (min level ${valueInt}%)"
                        break
                    case 10:    //Max Level
                        infoMsg += " (max level ${valueInt}%)" 
                        break
                    case 11:    //Invert Switch
                        infoMsg += valueInt==0?" (not Inverted)":" (Inverted)" 
                        break
                    case 12:    //Auto Off Timer
                        infoMsg += " (Auto Off Timer " + (valueInt==0?red("disabled"):"${valueInt}s") + ")"
                        break
                    case 13:    //Default Level (local)
                        infoMsg += " (default local level " + (valueInt==0?" = previous)":" ${valueInt}%)")
						sendEvent(name:"levelPreset", value:valueInt)
                        break
                    case 14:    //Default Level (remote)
                        infoMsg += " (default remote level " + (valueInt==0?" = previous)":"${valueInt}%)")
                        break
                    case 15:    //Level After Power Restored
                        infoMsg += " (power-on level " + (valueInt==0?" = previous)":"${valueInt}%)")
                        break
                    case 17:    //Load Level Timeout
                        infoMsg += (valueInt==0?" (do not display load level)":(valueInt==11?" (always display load level)":"s load level timeout"))
                        break
                    case 18: 
                        infoMsg += " (Active Power Report" + (valueInt==0?red(" disabled"):" ${valueInt}% change") + ")"
                        break
                    case 19:
                        infoMsg += "s (Periodic Power/Energy " + (valueInt==0?red(" disabled"):"") + ")"
                        break
                    case 20:
                        infoMsg += " (Active Energy Report " + (valueInt==0?red(" disabled"):" ${valueInt/100}kWh change") + ")"
                        break
                    case 21:    //Power Source
                        infoMsg += (valueInt==0?red(" (Non-Neutral)"):limeGreen(" (Neutral)"))
						sendEvent(name:"powerSource", value:valueInt==0?"Non-Neutral":"Neutral")
                        break
                    case 22:    //Aux Type
                        switch (state.model?.substring(0,5)){
							case "VZW32":    //Red mmWave Switch
                                infoMsg += " " + (valueInt==0?"(Single Pole)":(valueInt==1?"(Aux Switch)":"(unknown type)"))
								state.auxType =  (valueInt==0? "Single Pole": (valueInt==1? "Aux Switch": "unknown type $valueInt"))
                                break
                            case "VZM31":    //Blue 2-in-1 Dimmer
                            case "VZW31":    //Red  2-in-1 Dimmer
                                infoMsg += " " + (valueInt==0?"(No Aux)":(valueInt==1?"(Dumb 3-way)":(valueInt==2?"(Smart Aux)":(valueInt==3?"(No Aux Full Wave)":"(unknown type)"))))
								state.auxType =  (valueInt==0? "No Aux": (valueInt==1? "Dumb 3-way": (valueInt==2? "Smart Aux": (valueInt==3? "No Aux Full Wave":  "unknown type $valueInt"))))
                                break
                            case "VZM35":    //Fan Switch
                                infoMsg += " " + (valueInt==0?"(No Aux)":"(Smart Aux)")
                                state.auxType =   valueInt==0? "No Aux":  "Smart Aux"
                                break
                            default:
                                infoMsg = infoDev + indianRed(infoTxt + " unknown model $state.model")
                                state.auxType = "unknown model ${state.model}"
                                break
                        }
                        break
                    case 23:    //Quick Start Time (0-60 in 1/60 sec, firmware-driven)
                        infoMsg += " (Quick Start Time " + (valueInt==0?red("disabled"):"${valueInt}/60 s") + ")"
                        break
                    case 24:    //Quick Start Level
                        infoMsg += " (Quick Start Level ${valueInt}%)"
                        break
                    case 25:    //Higher Output in non-Neutral
                        infoMsg += " (non-Neutral High Output " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
                        break
					case 30:	//non-Neutral AUX med gear learn value
						infoMsg += " (non-Neutral AUX medium gear)"
						break
					case 31:	//non-Neutral AUX low gear learn value
						infoMsg += " (non-Neutral AUX low gear)"
						break
                    case 32:    //Internal Temperature (read only)
						valueStr = "${Math.round(valueInt*9/5+32)}°F"
                        infoMsg += " (Internal Temp: " + hue(100-valueInt.toInteger(),"${valueStr}") + ")"
                        sendEvent(name:"internalTemp", value:valueStr)
                        break
                    case 33:    //Overheat (read only)
                        infoMsg += " (Overheat: " + (valueInt==0?limeGreen("False"):valueInt==1?red("TRUE"):"undefined") + ")"
                        sendEvent(name:"overHeat",value:valueInt==0?"False":valueInt==1?"TRUE":"undefined")
                        break
                    case 50:    //Button Press Delay
                        infoMsg += " (${valueInt*100}ms Button Delay)"
						break
                    case 51:    //Device Bind Number
                        infoMsg += " (Bindings)"
                        sendEvent(name:"numberOfBindings", value:valueInt)
                        break
                    case 52:    //Smart Bulb/Fan Mode
                        if (state.model?.substring(0,5)=="VZM35") {
                            infoMsg += " (SFM " + (valueInt==0?red("disabled)"):limeGreen("enabled)"))
                            sendEvent(name:"smartFan", value:valueInt==0?"Disabled":"Enabled")
						} else {
                            infoMsg += " (SBM " + (valueInt==0?red("disabled)"):limeGreen("enabled)")) + ")"
                            sendEvent(name:"smartBulb", value:valueInt==0?"Disabled":"Enabled")
						}
                        break
                    case 53:  //Double-Tap UP
                        infoMsg += " (Double-Tap Up " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
                        break
                    case 54:  //Double-Tap DOWN
                        infoMsg += " (Double-Tap Down " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
                        break
                    case 55:  //Double-Tap UP level
                        infoMsg += " (Double-Tap Up level ${valueInt}%)"
                        break
                    case 56:  //Double-Tap DOWN level
                        infoMsg += " (Double-Tap Down level ${valueInt}%)"
                        break
					case 58:  //Exclusion Behavior
                        infoMsg += " (Exclusion: " + (valueInt==0?"LED Bar does not pulse":valueInt==1?"LED Bar pulses blue":valueInt==2?"do not Exclude":"unknown") + ")"
						break
					case 59:  //Association Behavior
                        infoMsg += " (Association: " + (valueInt==0?"None":valueInt==1?"Local":valueInt==2?"Hub":"Local+Hub") + ")"
						break
					case 60:
					case 65:
					case 70:
					case 75:
					case 80:
					case 85:
					case 90:
					case 95:	//LED(x) color when On
						if (valueInt<255 || attrInt==95)
							infoMsg += " " + hue(valueInt,"(LED${attrInt/5-11<8?(attrInt/5-11).toInteger():" bar"} color when On: ${Math.round(valueInt/255*360)}°)")
						else
							infoMsg +=                   " (LED${attrInt/5-11<8?(attrInt/5-11).toInteger():" bar"} color " + hue(settings.parameter95?.toInteger(),"sync with P95") + " when On)"
						break
					case 61:
					case 66:
					case 71:
					case 76:
					case 81:
					case 86:
					case 91:
					case 96:	//LED(x) color when Off
						if (valueInt<255 || attrInt==96)
							infoMsg += " " + hue(valueInt,"(LED${attrInt/5-11<8?(attrInt/5-11).toInteger():" bar"} color when Off: ${Math.round(valueInt/255*360)}°)")
						else
							infoMsg +=                   " (LED${attrInt/5-11<8?(attrInt/5-11).toInteger():" bar"} color " + hue(settings.parameter96?.toInteger(),"sync with P96") + " when Off)"
						break
					case 62:
					case 67:
					case 72:
					case 77:
					case 82:
					case 87:
					case 92:
					case 97:	//LED(x) intensity when On
						if (valueInt<101 || attrInt==97)
							infoMsg += "% (LED${attrInt/5-11<8?(attrInt/5-11).toInteger():" bar"} intensity when On)"
						else
							infoMsg +=  " (LED${attrInt/5-11<8?(attrInt/5-11).toInteger():" bar"} intensity sync with P97 when On)"
						break
					case 63:
					case 68:
					case 73:
					case 78:
					case 83:
					case 88:
					case 93:
					case 98:	//LED(x) intensity when Off
						if (valueInt<101 || attrInt==98)
							infoMsg += "% (LED${attrInt/5-11<8?(attrInt/5-11).toInteger():" bar"} intensity when Off)"
						else
							infoMsg +=  " (LED${attrInt/5-11<8?(attrInt/5-11).toInteger():" bar"} intensity sync with P98 when Off)"
						break
                    case 64:
					case 69:
					case 74:
					case 79:
					case 84:
					case 89:
					case 94:
					case 99:	//LED(x) Notification [zwave]
						def effectHex = valueHex.substring(0,2)
						int effectInt = Integer.parseInt(effectHex,16)
						infoMsg += " [0x${valueHex}] (LED${attrInt/5-11<8?(attrInt/5-11).toInteger():" bar"} Effect " + (effectInt==255?"Stop":"${effectInt}") + ")"
						break
					case 100:	//LED Bar Scaling
                        infoMsg += " (LED Scaling " + (valueInt==0?blue("VZM-style"):red("LZW-style")) + ")"
						break
					case 101:	//mmWave Height Minimum (Floor)
                        infoMsg += " (mmWave Height Min: ${valueInt}cm)"
                        break
					case 102:	//mmWave Height Maximum (Ceiling)
                        infoMsg += " (mmWave Height Max: ${valueInt}cm)"
                        break
					case 103:	//mmWave Width Minimum (Left)
                        infoMsg += " (mmWave Width Min: ${valueInt}cm)"
                        break
					case 104:	//mmWave Width Maximum (Right)
                        infoMsg += " (mmWave Width Max: ${valueInt}cm)"
                        break
					case 105:	//mmWave Depth Minimum (Near)
                        infoMsg += " (mmWave Depth Min: ${valueInt}cm)"
                        break
					case 106:	//mmWave Depth Maximum (Far)
                        infoMsg += " (mmWave Depth Max: ${valueInt}cm)"
                        break
					case 107:	//mmWave Target Info Report
                        infoMsg += " (mmWave Target Info " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
                        break
					case 108:	//mmWave Stay Life
                        infoMsg += " (mmWave Stay Life: ${valueInt}s)"
                        break
					case 109:	//UTC Time Range
                        infoMsg += " (UTC Time Range: ${valueInt})"
                        break
					case 110:	//Light On Presence Behavior
                        def behaviorText = ["Disabled", "Auto On/Off when occupied", "Auto Off when vacant", "Auto On when occupied", "Auto On/Off when Vacant", "Auto On when Vacant", "Auto Off when Occupied"]
                        infoMsg += " (Light On Presence: " + (valueInt<behaviorText.size()?behaviorText[valueInt]:"unknown") + ")"
                        break
					case 111:	//mmWave Module Commands
                        def commandText = ["Restore factory config", "Auto generate interference area", "Get interference/detection region", "Clear interference area", "Reset detection area", "Clear stay area"]
                        infoMsg += " (mmWave Command: " + (valueInt<commandText.size()?commandText[valueInt]:"unknown") + ")"
                        break
					case 112:	//mmWave Sensitivity
                        def sensitivityText = ["Low", "Medium", "High"]
                        infoMsg += " (mmWave Sensitivity: " + (valueInt<sensitivityText.size()?sensitivityText[valueInt]:"unknown") + ")"
                        break
					case 113:	//mmWave Trigger Speed
                        def speedText = ["Slow", "Medium", "Fast"]
                        infoMsg += " (mmWave Trigger Speed: " + (valueInt<speedText.size()?speedText[valueInt]:"unknown") + ")"
                        break
					case 114:	//mmWave Detection Timeout
                        infoMsg += " (mmWave Detection Timeout: ${valueInt}s)"
                        break
					case 115:	//mmWave Firmware Version
                        infoMsg += " (mmWave Firmware Version: ${valueInt})"
                        state.mmWaveFwVersion = valueInt
                        break
					case 116:	//mmWave Person in Reporting Area
                        infoMsg += " (mmWave Person in Area: ${valueInt})"
                        sendEvent(name: "areaReport", value: valueInt.toString())
                        break
					case 117:	//Room Size
                        def roomSizeText = ["Custom", "X-Small", "Small", "Medium", "Large", "X-Large"]
                        infoMsg += " (Room Size: " + (valueInt<roomSizeText.size()?roomSizeText[valueInt]:"unknown") + ")"
                        break
					case 118:	//Lux Threshold
                        infoMsg += " (Lux Threshold: ${valueInt})"
                        break
					case 119:	//Lux Interval
                        infoMsg += " (Lux Interval: ${valueInt}s)"
                        break
					case 120:	//Single Tap Handling
                        def singleTapText = ["Up - On, down - Off","Up - Increment up, down - Increment down","Up - Increment up (cycle), down - Off"]
                        infoMsg += " (Single Tap: " + (valueInt<singleTapText.size()?singleTapText[valueInt]:"${valueInt}") + ")"
                        break
					case 130:	//Z-Wave Association Group 7 Enable
                        infoMsg += " (Group 7 " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
                        break
					case 131:	//Group 7 Preset #1
                        infoMsg += " (Group 7 Level 1: ${valueInt}%)"
                        break
					case 132:	//Group 7 Preset #2
                        infoMsg += " (Group 7 Level 2: ${valueInt}%)"
                        break
					case 133:	//Group 7 Preset #3
                        infoMsg += " (Group 7 Level 3: ${valueInt}%)"
                        break
					case 134:	//Group 7 LED Bar Color
                        infoMsg += " (Group 7 LED Color: ${valueInt})"
                        break
					case 123:	//Aux Switch Scenes
                        infoMsg += " (Aux Scenes " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
						break
					case 125:	//Binding Off-to-On Sync Level
                        infoMsg += " (Send Level with Binding " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
						break
                    case 156:    //Local Protection
					case 256:
                        infoMsg += " (Local Control " + (valueInt==0?limeGreen("enabled"):red("disabled")) + ")"
                        break
                    case 157:    //Remote Protection
					case 257:
                        infoMsg += " (Remote Control " + (valueInt==0?limeGreen("enabled"):red("disabled")) + ")"
                        break
                    case 158:    //Switch Mode
					case 258:
                        switch (state.model?.substring(0,5)){
                            case "VZM31":    //Blue 2-in-1 Dimmer
                            case "VZW31":    //Red  2-in-1 Dimmer
                            case "VZW32":    //Red mmWave Switch
                                infoMsg += " " + (valueInt==0?"(Dimmer mode)":"(On/Off mode)")
                                sendEvent(name:"switchMode", value:valueInt==0?"Dimmer":"On/Off")
                                break
                            case "VZM35":    //Fan Switch
								infoMsg += " " + (valueInt==0?"(Multi-Speed mode)":"(On/Off mode)")
								sendEvent(name:"switchMode", value:valueInt==0?"Multi-Speed":"On/Off")
								break
                            default:
                                infoMsg += " " + red(" unknown model $state.model")
                                sendEvent(name:"switchMode", value:"unknown model")
                                break
                        }
						break
                    case 159:    //On-Off LED
					case 259:
                        infoMsg += " (On-Off LED mode: " + (valueInt==0?"All)":"One)")
                        break
					case 160:    //Firmware Update Indicator
                    case 260:
                        infoMsg += " (Firmware Update Indicator " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
                        break
					case 161:    //Relay Click
                    case 261:
                        infoMsg += " (Relay Click " + (valueInt==0?limeGreen("enabled"):red("disabled")) + ")"
                        break
					case 162:    //Double-Tap config button to clear notification
                    case 262:
                        infoMsg += " (Double-Tap config button " + (valueInt==0?limeGreen("enabled"):red("disabled")) + ")"
                        break
					case 163:    //LED bar display levels
                    case 263:
                        infoMsg += " (LED bar display levels: ${valueInt?:'full range'})"
                        break
                    default:
						infoMsg += " [0x${valueInt<=0xFF?valueHex.substring(6):valueInt<=0xFFFF?valueHex.substring(4):valueHex}] " + orangeRed(bold("Undefined Parameter $attrInt"))
                        break
                }
                if (infoEnable) log.info infoMsg + ((traceEnable||debugEnable)?" [P:$attrInt V:$valueInt D:${getDefaultValue(attrInt)}]":"")
                //if ((attrInt==9)    //for zwave these are stored as 0-100, no need to convert
				//|| (attrInt==10)
				//|| (attrInt==13)
				//|| (attrInt==14)
				//|| (attrInt==15)
				//|| (attrInt==55)
				//|| (attrInt==56)) {
				//	valueInt = convertByteToPercent(valueInt) //these attributes are stored as bytes but displayed as percentages
				//}
                if ((attrInt==95 && parameter95custom!=null)||(attrInt==96 && parameter96custom!=null)) {   //if custom hue was set, update the custom user setting also
                    device.updateSetting("parameter${attrInt}custom",[value:"${Math.round(valueInt/255*360)}",type:configParams["parameter${attrInt.toString().padLeft(3,"0")}"].type?.toString()])
                    state."parameter${attrInt}custom" = Math.round(valueInt/255*360)
                }
				if (state.model?.substring(0,5)!="VZM35" && (attrInt==21 || attrInt==22 || attrInt==158 || attrInt==258)) {  //fan does not support leading/trailing edge dimming
					state.dimmingMethod = "Leading Edge"							//default to Leading Edge
					if (parameter21=="1") {											//if neutral wiring then select based on remote switch type
						if (parameter22=="0") state.dimmingMethod = "Trailing Edge"	//no aux
						if (parameter22=="1") state.dimmingMethod = "Leading Edge"	//dumb 3-way
						if (parameter22=="2") state.dimmingMethod = "Trailing Edge"	//smart aux
						if (parameter22=="3") {
							if (parameter158=="1" || parameter258=="1") {			//Switch Mode is On-Off
								state.dimmingMethod = "Full Wave"
							} else {												//Switch Mode is Dimmer
								state.dimmingMethod = "Trailing Edge"
								device.updateSetting("parameter22",[value:"0",type:"enum"])
								state.parameter22value=0
							}
						}
					}
					if (infoEnable||traceEnable||debugEnable) log.info "${device.displayName} Dimming Method = ${state.dimmingMethod}"
				}
				//Update UI setting with value received from device
				if ((valueInt==getDefaultValue(attrInt))	//IF   value is the default
				&& (!readOnlyParams().contains(attrInt))	//AND  not a read-only param
				&& (![22,52,158,258].contains(attrInt))) {	//AND  not a key parameter
					//clearSetting(attrInt)					//THEN clear the setting (so only changed settings are displayed)
				} else if([115,116].contains(attrInt)) {									//ELSE update local setting (string so UI shows no commas)
					device.updateSetting("parameter${attrInt}",[value:"${valueInt}",type:configParams["parameter${attrInt.toString().padLeft(3,"0")}"]?.type?.toString()])
					state."parameter${attrInt}value" = "${valueInt}"	//store as string so display has no commas
				} else {
					//device.updateSetting("parameter${attrInt}",[value:"${valueInt}",type:configParams["parameter${attrInt.toString().padLeft(3,"0")}"]?.type?.toString()])
				}
				if (settings."parameter${attrInt}"!=null && ![115,116].contains(attrInt)) {		//IF   device setting is not null (and not 115/116, already set above)
					state."parameter${attrInt}value" = settings."parameter${attrInt}"?.toInteger()	//THEN set state variable to device setting
				}
			break
		case "FirmwareUpdateMdGet":
			if (infoEnable) log.info "${device.displayName} ${cmd}"
			break
		case "FirmwareMdReport":
			if (infoEnable) log.info "${device.displayName} ${cmd}"
			break
		case "FirmwareUpdateMdRequestReport":
			if (infoEnable) log.info "${device.displayName} ${cmd}"
			if (cmd.status==255) log.info "${device.displayName} Firmware Update Started"
			break
		case "FirmwareUpdateMdStatusReport":
			if (infoEnable) log.info "${device.displayName} ${cmd}"
			if (cmd.status==255) log.info "${device.displayName} Firmware Update Completed"
			break
		case "MeterReport":
			if (cmd.scale == 0) {
				if (cmd.meterType == 161) {
					sendEvent(name: "voltage", value: cmd.scaledMeterValue, unit: "V")
					if (infoEnable) log.info "${device.displayName} Voltage Report: value ${cmd.scaledMeterValue} V"
				} else if (cmd.meterType == 1) {
					sendEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
					if (infoEnable) log.info "${device.displayName} Energy Report: value ${cmd.scaledMeterValue} kWh"
				}
			} else if (cmd.scale == 1) {
				sendEvent(name: "amperage", value: cmd.scaledMeterValue, unit: "A")
				if (infoEnable) log.info "${device.displayName} Amperage Report: value ${cmd.scaledMeterValue} A"
			} else if (cmd.scale == 2) {
				sendEvent(name: "power", value: cmd.scaledMeterValue, unit: "W")
				if (infoEnable) log.info "${device.displayName} Power Report: value ${cmd.scaledMeterValue} W"
			}
			break
		case "ProtectionReport":
			if (infoEnable) log.info "${device.displayName} Protection Report: Local protection is ${cmd.localProtectionState > 0 ? "on" : "off"} & Remote protection is ${cmd.rfProtectionState > 0 ? "on" : "off"}"
			state.localProtectionState = cmd.localProtectionState
			state.rfProtectionState = cmd.rfProtectionState
			device.updateSetting("disableLocal",[value:cmd.localProtectionState?"1":"0",type:"enum"])
			device.updateSetting("disableRemote",[value:cmd.rfProtectionState?"1":"0",type:"enum"])
			def children = childDevices
			def childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
			if (childDevice) childDevice.sendEvent(name: "switch", value: cmd.localProtectionState > 0 ? "on" : "off")
			childDevice = children.find{it.deviceNetworkId.endsWith("ep102")}
			if (childDevice) childDevice.sendEvent(name: "switch", value: cmd.rfProtectionState > 0 ? "on" : "off")
			break
		case "SecurityMessageEncapsulation":
			if (infoEnable) log.info "${device.displayName} ${cmd}"
			hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(CMD_CLASS_VERS)
			if (encapsulatedCommand) zwaveEvent(encapsulatedCommand)
			break
		case "SupervisionGet":
			if (infoEnable) log.info "${device.displayName} ${cmd}"
			hubitat.zwave.Command encapCmd = cmd.encapsulatedCommand(commandClassVersions)
			if (encapCmd) zwaveEvent(encapCmd)
			sendHubCommand(new hubitat.device.HubAction(secureCmd(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0)), hubitat.device.Protocol.ZWAVE))
			break
		case "SwitchMultilevelReport":
			if (infoEnable) log.info "${device.displayName} Switch Multilevel Report: value ${cmd.targetValue ? "on" : "off"} ($cmd.targetValue)"
            cmd.targetValue = cmd.value
			dimmerEvents(cmd, (!state.lastRan || now() <= state.lastRan + 2000)?"digital":"physical")
			break
		case "VersionCommandClassReport":
			if (infoEnable) log.info "${device.displayName} ${cmd}"
			break
		case "VersionReport":
			Double firmware0Version = cmd.firmware0Version + (cmd.firmware0SubVersion / 100)
			Double protocolVersion = cmd.zWaveProtocolVersion + (cmd.zWaveProtocolSubVersion / 100)
			if (infoEnable) log.info "Version Report - FirmwareVersion: ${firmware0Version}, ProtocolVersion: ${protocolVersion}, HardwareVersion: ${cmd.hardwareVersion}"
			state.fwVersion = firmware0Version
			break
		default:
			if (infoEnable||traceEnable||debugEnable) log.warn "${device.displayName} ${fireBrick('Unhandled:')} ${cmd}"
			break
	}
}

def presetLevel(value) {
    if (infoEnable) log.info "${device.displayName} presetLevel(${value})"
    state.lastCommandSent =                        "presetLevel(${value})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    Integer scaledValue = value==null?null:Math.min(Math.max(value.toInteger(),0),99)  //Zwave levels range from 1-99 with 0 = 'use previous'
    cmds += setParameter(13, scaledValue, null)
    if (debugEnable) log.debug  "${device.displayName} preset $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def refresh(option) {
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.displayName} refresh(${option})"
    state.lastCommandSent =                        "refresh(${option})"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = "VZW32-SN"
    if (infoEnable||traceEnable||debugEnable) log.info "${device.displayName} Driver Date $state.driverDate"
    def cmds = []
	cmds += zwave.versionV1.versionGet()
	cmds += getParameter(115)
    //cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
    cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:1)
    //cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5, scale:1)
    //cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:27, scale:1)
	validConfigParams().each { i ->	//loop through valid parameters (z-wave returns p1 value if we ask for unsupported param)
		switch (option) {
			case "":									//option is blank or null 
				if (([22,52,158,258].contains(i))		//refresh primary settings
				|| (readOnlyParams().contains(i))		//refresh read-only params
				|| (settings."parameter${i}"!=null)) {	//refresh user settings
					cmds += getParameter(i)
				}
				break
			case "All":
				cmds += getParameter(i) //if option is All then refresh all params
				break
			default: 
				if (traceEnable||debugEnable) log.error "${device.displayName} Unknonwn option 'refresh($option)'"
				break
		}
    }
    if (debugEnable) {
		userSettableParams().each { i ->
			def param_output = ""
			param_output = param_output + " name: \"parameter${i}\"" + "\n"
			//log.debug "- name: \"${getParameterInfo(i, "name").replaceAll("\\s","").uncapitalize()}\""
			param_output = param_output + " title: \"${configParams["parameter${i.toString().padLeft(3,"0")}"].name}\"" + "\n"
			param_output = param_output + " description: \"${configParams["parameter${i.toString().padLeft(3,"0")}"].description.replace('\n', ' ').take(300)}\"" + "\n"
			param_output = param_output + " required: true" + "\n"
			param_output = param_output + " preferenceType: ${configParams["parameter${i.toString().padLeft(3,"0")}"].type=="enum"?"enumeration":configParams["parameter${i.toString().padLeft(3,"0")}"].type}" + "\n"
			param_output = param_output + " definition:" + "\n"
			if (configParams["parameter${i.toString().padLeft(3,"0")}"].type =="enum") {
				param_output = param_output + " options:" + "\n"
				configParams["parameter${i.toString().padLeft(3,"0")}"].range.each {
					//log.debug "\"${it[0]}\": \"${it[1]}\""
					param_output = param_output +  " \"${it.key}\": \"${it.value}\"" + "\n"
				}
			} else {
				param_output = param_output + " minimum: ${configParams["parameter${i.toString().padLeft(3,"0")}"].range.split("\\.\\.")[0]}" + "\n"
				param_output = param_output + " maximum: ${configParams["parameter${i.toString().padLeft(3,"0")}"].range.split("\\.\\.")[1]}" + "\n"
				param_output = param_output + " default: ${configParams["parameter${i.toString().padLeft(3,"0")}"].default}" + "\n"
			}
			log.debug param_output 
		}
	}
	return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}
def resetEnergyMeter() {
    if (infoEnable) log.info "${device.displayName} resetEnergyMeter(" + device.currentValue("energy") + "kWh)"
    state.lastCommandSent =                        "resetEnergyMeter(" + device.currentValue("energy") + "kWh)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zwave.meterV2.meterReset()
    cmds += zwave.meterV2.meterGet(scale: 0)
    cmds += zwave.meterV2.meterGet(scale: 2)
    if (debugEnable) log.debug  "${device.displayName} resetEnergyMeter $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def setLevel(value) {
	if (infoEnable) log.info "${device.displayName} setLevel($value)"
    state.lastCommandSent =                        "setLevel($value)"
    state.lastCommandTime = nowFormatted()
	def cmds = []
	cmds += zwave.switchMultilevelV4.switchMultilevelSet(value: value<100?value:99)
    cmds += zwave.switchMultilevelV4.switchMultilevelGet()
	if (debugEnable) log.debug "${device.displayName} setLevel $cmds"
	return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def setLevel(value, duration) {
    if (infoEnable) log.info "${device.displayName} setLevel($value" + (duration==null?")":", ${duration}s)")
    state.lastCommandSent =                        "setLevel($value" + (duration==null?")":", ${duration}s)")
    state.lastCommandTime = nowFormatted()
    duration = duration<128?duration:128+Math.round(duration/60)
    def cmds = []
    cmds += zwave.switchMultilevelV4.switchMultilevelSet(value: value<100?value:99, dimmingDuration: duration)
    cmds += zwave.switchMultilevelV4.switchMultilevelGet()
    if (debugEnable) log.debug "${device.displayName} setLevel $cmds"
	return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def setConfigParameter(number, value, size) {	//for backward compatibility
    return delayBetween(setParameter(paramNum, value, size.toInteger()).collect{ secureCmd(it) }, shortDelay)
}

def setParameter(paramNum) {
    // User interface version - wraps result in delayBetween
    return delayBetween(setParameter(paramNum, value, null).collect{ secureCmd(it) }, shortDelay)
}

def setParameter(paramNum, value) {
    // User interface version - set then get; update preference so device screen shows the value sent
    paramNum = paramNum?.toInteger()
    if (value != null && value != "" && paramNum != null) {
        def type = configParams["parameter${paramNum.toString().padLeft(3,'0')}"]?.type?.toString() ?: "number"
        device.updateSetting("parameter${paramNum}", [value: value.toString(), type: type])
    }
    return delayBetween(setParameter(paramNum, value, null).collect{ secureCmd(it) }, shortDelay)
}

def setParameter(paramNum, value, size) {
	paramNum = paramNum?.toInteger()
	value    = value?.toInteger()
	size     = size?.toInteger()
	if (size==null || size==" ") size = configParams["parameter${paramNum.toString().padLeft(3,'0')}"]?.size?:8
	if (traceEnable) log.trace value!=null?"${device.displayName} setParameter($paramNum, $value, $size)":"${device.displayName} getParameter($paramNum)"
	state.lastCommandSent =    value!=null?                      "setParameter($paramNum, $value, $size)":                      "getParameter($paramNum)"
	state.lastCommandTime = nowFormatted()
	def cmds = []
    if (value!=null) cmds += zwave.configurationV4.configurationSet(parameterNumber: paramNum, scaledConfigurationValue: size==1?(value<0x80?value:value-0x100):size==4?(value<0x80000000?value:value-0x100000000):value, size: size)

	cmds += zwave.configurationV4.configurationGet(parameterNumber: paramNum)
    if (debugEnable) log.debug value!=null?"${device.displayName} setParameter $cmds":"${device.displayName} getParameter $cmds"
    return cmds
}

def getParameter(paramNum=0, delay=shortDelay) {
	paramNum = paramNum?.toInteger()
    if (traceEnable) log.trace "${device.displayName} getParameter($paramNum)"
    //state.lastCommandSent =                        "getParameter($paramNum)"
    //state.lastCommandTime = nowFormatted() //this is not a custom command.  Only use state variable for commands on the device details page
    def cmds = []
	if (paramNum<0) {	//special case, if negative then read all params from 0-max (for debugging)
		for(int i = 0;i<=validConfigParams().max();i++) {
			cmds += zwave.configurationV4.configurationGet(parameterNumber: i)
		}	
	} else {	//otherwise, just get the requested parameter
		cmds += zwave.configurationV4.configurationGet(parameterNumber: paramNum)
	}
    if (debugEnable) log.debug "${device.displayName} getParameter $cmds"
    return cmds
}

def startLevelChange(direction, duration=null) {
    def newLevel = direction=="up"?100:device.currentValue("switch")=="off"?0:1
    if (infoEnable) log.info "${device.displayName} startLevelChange(${direction}" + (duration==null?")":", ${duration}s)")
    state.lastCommandSent =                        "startLevelChange(${direction}" + (duration==null?")":", ${duration}s)")
    state.lastCommandTime = nowFormatted()
    duration = duration<128?duration:128+Math.round(duration/60)
    Boolean upDownVal = direction == "down" ? true : false
	def cmds = []
	cmds += zwave.switchMultilevelV4.switchMultilevelStartLevelChange(upDown: upDownVal, dimmingDuration: duration, ignoreStartLevel: true, startLevel: device.currentValue("level"))
    cmds += zwave.switchMultilevelV4.switchMultilevelGet()
    if (debugEnable) log.debug "${device.displayName} startLevelChange $cmds"
	return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def stopLevelChange() {
    if (infoEnable) log.info "${device.displayName} stopLevelChange()" // at level " + device.currentValue("level")
    state.lastCommandSent =                        "stopLevelChange()"
    state.lastCommandTime = nowFormatted()
	def cmds = []
	cmds += zwave.switchMultilevelV4.switchMultilevelStopLevelChange()
    cmds += zwave.switchMultilevelV4.switchMultilevelGet()
    if (debugEnable) log.debug "${device.displayName} stopLevelChange $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def startNotification(value, ep = null){	//for backward compatibility
    def hexStr = zigbee.convertToHexString(value.toInteger(),8)	//flip bytes 2 and 4 since they are incorrect in nathan's tool
    def bigValue = new BigInteger(hexStr.substring(0, 2) + hexStr.substring(6, 8) + hexStr.substring(4, 6) + hexStr.substring(2, 4), 16)
	log.warn "${device.displayName} startNotification(${red(bold('command is depreciated. Use ledEffectAll instead'))})"
    if (infoEnable) log.info "${device.displayName} startNotification($bigValue [0x$hexStr])"
    state.lastCommandSent =                        "startNotification($bigValue [0x$hexStr])"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zwave.configurationV4.configurationSet(scaledConfigurationValue: bigValue, parameterNumber: ledNotificationEndpoints[(ep == null)? 0:ep?.toInteger()-1], size: 4)
    cmds += zwave.configurationV4.configurationGet(parameterNumber: ledNotificationEndpoints[(ep == null)? 0:ep?.toInteger()-1])
    if (debugEnable) log.debug "${device.displayName} startNotification $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def stopNotification(ep = null){	//for backward compatibility
	log.warn "${device.displayName} stopNotification(${red(bold('command is depreciated. Use ledEffectAll instead'))})"
    if (infoEnable) log.info "${device.displayName} stopNotification()"
    state.lastCommandSent =                        "stopNotification()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zwave.configurationV4.configurationSet(scaledConfigurationValue: 0, parameterNumber: ledNotificationEndpoints[(ep == null)? 0:ep?.toInteger()-1], size: 4)
    cmds += zwave.configurationV4.configurationGet(parameterNumber: ledNotificationEndpoints[(ep == null)? 0:ep?.toInteger()-1])
    if (debugEnable) log.debug "${device.displayName} stopNotification $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def toggle() {	
    def toggleDirection = device.currentValue("switch")=="off"?"off->on":"on->off"
    if (infoEnable) log.info "${device.displayName} toggle(${toggleDirection})"
    state.lastCommandSent =                        "toggle(${toggleDirection})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    // emulate toggle since z-wave does not have native toggle command like zigbee
    if (device.currentValue("switch")=="off") {
		cmds += zwave.basicV2.basicSet(value: 0xFF)
		cmds += zwave.basicV2.basicGet()
	} else {
		cmds += zwave.basicV2.basicSet(value: 0x00)
		cmds += zwave.basicV2.basicGet()
	}
    if (debugEnable) log.debug "${device.displayName} toggle $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
}

def updated(option) { // called when "Save Preferences" is requested
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.displayName} updated(${option})" + (traceEnable||debugEnable)?" $settings":""
    state.lastCommandSent =                        "updated(${option})"
    state.lastCommandTime = nowFormatted()
    if (state?.lastRan && now()<state?.lastRan + 2000) {
        if (infoEnable) log.info "${device.displayName} updated() ran within the last 2 seconds. Skipping execution."
		return null
	} else {
		state.lastRan = now()
	}
	runIn(2,lastRanRemove)
    def changedParams = []
    def cmds = []
    cmds += processAssociations()
    def nothingChanged = true
    int defaultValue
    int newValue
	validConfigParams().each { i ->	//loop through all parameters
		//int i = it.value.number.toInteger()
		newValue = calculateParameter(i)
		defaultValue=getDefaultValue(i)
		if ([9,10,13,14,15,55,56].contains(i)) defaultValue=convertPercentToByte(defaultValue) //convert percent values back to byte values
		if ((i==95 && parameter95custom!=null)||(i==96 && parameter96custom!=null)) {                                         //IF   a custom hue value is set
			if ((Math.round(settings?."parameter${i}custom"?.toInteger()/360*255)==settings?."parameter${i}"?.toInteger())) { //AND  custom setting is same as normal setting
				device.removeSetting("parameter${i}custom")                                                                   //THEN clear custom hue and use normal color 
				if (infoEnable||traceEnable||debugEnable) log.info "${device.displayName} Cleared Custom Hue setting since it equals standard color setting"
			}
		}
		switch (option) {
			case "":
				if ((userSettableParams().contains(i))		//IF   this is a valid parameter for this device mode
				&& (settings."parameter$i"!=null)			//AND  this is a non-default setting
				&& (!readOnlyParams().contains(i))) {		//AND  this is not a read-only parameter
                    // Check if parameter value has actually changed from what's stored on the device
                    def currentValue = state."parameter${i}value"?.toInteger()
					if (currentValue != newValue) {
						cmds += setParameter(i, newValue, null)		//THEN set the new value
						nothingChanged = false
					}
				}
                if ([64,69,74,79,84,89,94,99].contains(i)) clearSetting(i)	//LED notification params: clear after send so user can trigger again
				break
			case "All":
			case "Default":
				if (option=="Default") newValue = defaultValue	//if user selected "Default" then set the new value to the default value
				if (((i!=158)&&(i!=258))					//IF   we are not changing Switch Mode
				&& (!readOnlyParams().contains(i))) {		//AND  this is not a read-only parameter
					cmds += setParameter(i, newValue, null)		//THEN Set the new value
					nothingChanged = false
				} else {									//ELSE this is a read-only parameter or Switch Mode parameter
					cmds += getParameter(i)					//so Get current value from device
				}
                if ([64,69,74,79,84,89,94,99].contains(i)) clearSetting(i)	//LED notification params: clear after send so user can trigger again
				break
			default: 
				if (traceEnable||debugEnable) log.error "${device.displayName} Unknown option 'updated($option)'"
				break
		}
    }
    if (settings?.groupBinding1 && !state?.groupBinding1) {
        bindGroup("bind",settings.groupBinding1?.toInteger())
		//device.updateSetting("groupBinding1",[value:settings.groupBinding1?.toInteger(),type:"number"])
		state.groupBinding1=settings.groupBinding1?.toInteger()
        nothingChanged = false
    } else {
        if (!settings?.groupBinding1 && state?.groupBinding1) {
            bindGroup("unbind",state.groupBinding1?.toInteger())
			device.removeSetting("groupBinding1")
			state.groupBinding1=null
            nothingChanged = false
        }
    }
    if (settings?.groupBinding2 && !state?.groupBinding2) {
        bindGroup("bind",settings.groupBinding2?.toInteger())
		//device.updateSetting("groupBinding2",[value:settings.groupBinding2?.toInteger(),type:"number"])
		state.groupBinding2=settings.groupBinding2?.toInteger()
        nothingChanged = false
    } else {
        if (!settings?.groupBinding2 && state?.groupBinding2) {
            bindGroup("unbind",state.groupBinding2?.toInteger())
			device.removeSetting("groupBinding2")
			state.groupBinding2=null
            nothingChanged = false
        }
    }
    if (settings?.groupBinding3 && !state?.groupBinding3) {
        bindGroup("bind",settings.groupBinding3?.toInteger())
		//device.updateSetting("groupBinding3",[value:state.groupBinding3?.toInteger(),type:"number"])
		state.groupBinding3=state.groupBinding3?.toInteger()
        nothingChanged = false
    } else {
        if (!settings?.groupBinding3 && state?.groupBinding3) {
            bindGroup("unbind",state.groupBinding3?.toInteger())
			device.removeSetting("groupBinding3")
			state.groupBinding3=null
            nothingChanged = false
        }
    }
	// remove duplicate groups
	if (settings.groupBinding3!=null && settings.groupBinding3==settings.groupBinding2) {device.removeSetting("groupBinding3"); state.groupBinding3 = null; if (infoEnable) log.info "${device.displayName} Removed duplicate Group Bind #3"}
	if (settings.groupBinding2!=null && settings.groupBinding2==settings.groupBinding1) {device.removeSetting("groupBinding2"); state.groupBinding2 = null; if (infoEnable) log.info "${device.displayName} Removed duplicate Group Bind #2"}
	if (settings.groupBinding1!=null && settings.groupBinding1==settings.groupBinding3) {device.removeSetting("groupBinding3"); state.groupBinding3 = null; if (infoEnable) log.info "${device.displayName} Removed duplicate Group Bind #3"}
	
    if (nothingChanged && (infoEnable||traceEnable||debugEnable)) log.info "${device.displayName} No DEVICE settings were changed"
	log.info  "${device.displayName} Info logging  " + (infoEnable?limeGreen("Enabled"):red("Disabled"))
	log.trace "${device.displayName} Trace logging " + (traceEnable?limeGreen("Enabled"):red("Disabled"))
	log.debug "${device.displayName} Debug logging " + (debugEnable?limeGreen("Enabled"):red("Disabled"))

    if (infoEnable && disableInfoLogging) {
		log.info "${device.displayName} Info Logging will be disabled in $disableInfoLogging minutes"
		runIn(disableInfoLogging*60,infoLogsOff)
	}
    if (traceEnable && disableTraceLogging) {
		log.trace "${device.displayName} Trace Logging will be disabled in $disableTraceLogging minutes"
		runIn(disableTraceLogging*60,traceLogsOff)
	}
    if (debugEnable && disableDebugLogging) {
		log.debug "${device.displayName} Debug Logging will be disabled in $disableDebugLogging minutes"
		runIn(disableDebugLogging*60,debugLogsOff) 
	}
    if (cmds) return delayBetween(cmds.collect{ secureCmd(it) }, shortDelay)
	else return
}
def lastRanRemove() {if (state?.lastRan) state.remove("lastRan")}

private dimmerEvents(hubitat.zwave.Command cmd, type="physical") {
    def value = (cmd.targetValue ? "on" : "off")
    def result = [sendEvent(name: "switch", value: value, type: type)]
    if (cmd.targetValue) {
        result += sendEvent(name: "level", value: cmd.targetValue, unit: "%", type: type)
    }
    return result
}

void buttonEvent(button, action, type = "digital") {
    if (infoEnable) log.info "${device.displayName} ${type} Button ${button} was ${action}"
    sendEvent(name: action, value: button, isStateChange: true, type: type)
    switch (button) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
            sendEvent(name:"lastButton", value: "${action=='pushed'?'Tap '.padRight(button+4, '▲'):'Tap '.padRight(button+4, '▼')}")
            break
        case 6:
            sendEvent(name:"lastButton", value: "${action=='pushed'?'Hold ▲':'Hold ▼'}")
            break
        case 7:
            sendEvent(name:"lastButton", value: "${action=='pushed'?'Release ▲':'Release ▼'}")
            break
        case 8:
        case 9:
        case 10:
        case 11:
        case 12:
            sendEvent(name:"lastButton", value: "Tap ".padRight(button-3, "►"))
            break
        case 13:
            sendEvent(name:"lastButton", value: "Hold ►")
            break
        case 14:
            sendEvent(name:"lastButton", value: "Release ►")
            break
    }
}

void hold(button)    {buttonEvent(button, "held", "digital")}
void push(button)    {buttonEvent(button, "pushed", "digital")}
void release(button) {buttonEvent(button, "released", "digital")}

def pressUpX1()      {buttonEvent(1, "pushed", "digital")}
def pressDownX1()    {buttonEvent(1, "held", "digital")}
def pressUpX2()      {buttonEvent(2, "pushed", "digital")}
def pressDownX2()    {buttonEvent(2, "held", "digital")}
def pressUpX3()      {buttonEvent(3, "pushed", "digital")}
def pressDownX3()    {buttonEvent(3, "held", "digital")}
def pressUpX4()      {buttonEvent(4, "pushed", "digital")}
def pressDownX4()    {buttonEvent(4, "held", "digital")}
def pressUpX5()      {buttonEvent(5, "pushed", "digital")}
def pressDownX5()    {buttonEvent(5, "held", "digital")}
def holdUp()         {buttonEvent(6, "pushed", "digital")}
def holdDown()       {buttonEvent(6, "held", "digital")}
def releaseUp()      {buttonEvent(7, "pushed", "digital")}
def releaseDown()    {buttonEvent(7, "held", "digital")}
def pressConfigX1()  {buttonEvent(8, "pushed", "digital")}
def pressConfigX2()  {buttonEvent(9, "pushed", "digital")}
def pressConfigX3()  {buttonEvent(10, "pushed", "digital")}
def pressConfigX4()  {buttonEvent(11, "pushed", "digital")}
def pressConfigX5()  {buttonEvent(12, "pushed", "digital")}
def holdConfig()     {buttonEvent(13, "held", "digital")}
def releaseConfig()  {buttonEvent(14, "released", "digital")}

private int gammaCorrect(value) {
    def temp=value/255
    def correctedValue=(temp>0.4045) ? Math.pow((temp+0.055)/ 1.055, 2.4) : (temp / 12.92)
    return Math.round(correctedValue * 255) as Integer
}

def setDefaultAssociations() {
    def HubID = String.format('%02x', zwaveHubNodeId).toUpperCase()
    state.defaultG1 = [HubID]
    state.defaultG2 = []
    state.defaultG3 = []
}

def setAssociationGroup(group, nodes, action, endpoint = null){
    // Normalize the arguments to be backwards compatible with the old method
    action = "${action}" == "1" ? "Add" : "${action}" == "0" ? "Remove" : "${action}" // convert 1/0 to Add/Remove
    group  = "${group}" =~ /\d+/ ? (group as int) : group                             // convert group to int (if possible)
    nodes  = [] + nodes ?: [nodes]                                                    // convert to collection if not already a collection

    if (! nodes.every { it =~ /[0-9A-F]+/ }) {
        log.error "${device.displayName} invalid Nodes ${nodes}"
        return
    }

    if (group < 1 || group > maxAssociationGroup()) {
        log.error "${device.displayName} Association group is invalid 1 <= ${group} <= ${maxAssociationGroup()}"
        return
    }

    def associations = state."desiredAssociation${group}"?:[]
    nodes.each {
        node = "${it}"
        switch (action) {
        case "Remove":
            if (infoEnable) log.info "${device.displayName} Removing node ${node} from association group ${group}"
            associations = associations - node
            break
        case "Add":
            if (infoEnable) log.info "${device.displayName} Adding node ${node} to association group ${group}"
            associations += node
            break
        }
    }
    state."desiredAssociation${group}" = associations.unique()
    return
}

def maxAssociationGroup(){
   if (!state.associationGroups) {
       if (infoEnable) log.info "${device.displayName} Getting supported association groups from device"
       sendHubCommand(new hubitat.device.HubAction(secureCmd(zwave.associationV2.associationGroupingsGet()), hubitat.device.Protocol.ZWAVE )) // execute the update immediately
   }
   (state.associationGroups?: 5) as int
}

def processAssociations(){
   def cmds = []
   setDefaultAssociations()
   def associationGroups = maxAssociationGroup()
   for (int i = 1; i <= associationGroups; i++){
      if(state."actualAssociation${i}" != null){
         if(state."desiredAssociation${i}" != null || state."defaultG${i}") {
            def refreshGroup = false
            ((state."desiredAssociation${i}"? state."desiredAssociation${i}" : [] + state."defaultG${i}") - state."actualAssociation${i}").each {
                if (it){
                    if (infoEnable) log.info "${device.displayName} Adding node $it to group $i"
                    cmds += zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:hubitat.helper.HexUtils.hexStringToInt(it))
                    refreshGroup = true
                }
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                if (it){
                    if (infoEnable) log.info "${device.displayName} Removing node $it from group $i"
                    cmds += zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:hubitat.helper.HexUtils.hexStringToInt(it))
                    refreshGroup = true
                }
            }
            if (refreshGroup == true) cmds += zwave.associationV2.associationGet(groupingIdentifier:i)
            else if (infoEnable) log.info "${device.displayName} There are no association actions to complete for group $i"
         }
      } else {
         if (infoEnable) log.info "${device.displayName} Association info not known for group $i. Requesting info from device."
         cmds += zwave.associationV2.associationGet(groupingIdentifier:i)
      }
   }
   if (cmds) cmds -= null	//remove nulls from list
   if (cmds)
       return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
   else 
       return []
}

/****************************************************************************
	This section contains all the command classes supported by the VZW switch

	The reason it is commented out is that we don't know exactly which
	commands and versions are reported back.  The known ones have been
	defined above.  Unknown ones will get reported by the is "Unhandled" 
	method above.  This will appear in the log file and when that happens
	that specifc command can be moved up to a new case section and code  
	added to take the appropriate action for the associated command 
****************************************************************************/

//def zwaveEvent(hubitat.zwave.commands.associationv3.AssociationReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.associationv3.AssociationRemove cmd) {}
//def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationGroupingsGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationSpecificGroupGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationSpecificGroupReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.associationgrpinfov3.AssociationGroupNameGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.associationgrpinfov3.AssociationGroupNameReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.associationgrpinfov3.AssociationGroupInfoGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.associationgrpinfov3.AssociationGroupInfoReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.associationgrpinfov3.AssociationGroupCommandListGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.associationgrpinfov3.AssociationGroupCommandListReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.basicv2.BasicSet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.basicv1.BasicGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.centralscenev2.CentralSceneSupportedGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.centralscenev2.CentralSceneSupportedReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.configurationv4.ConfigurationSet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.configurationv4.ConfigurationGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.configurationv4.ConfigurationBulkSet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.configurationv4.ConfigurationBulkGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.configurationv4.ConfigurationBulkReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.configurationv4.ConfigurationNameGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.configurationv4.ConfigurationNameReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.configurationv4.ConfigurationPropertiesGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.configurationv4.ConfigurationPropertiesReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.configurationv4.ConfigurationDefaultReset cmd) {}
//def zwaveEvent(hubitat.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {}
//def zwaveEvent(hubitat.zwave.commands.firmwareupdatemdv5.FirmwareMdGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.firmwareupdatemdv5.FirmwareMdReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.firmwareupdatemdv5.FirmwareUpdateMdRequestGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.firmwareupdatemdv5.FirmwareUpdateMdRequestReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.firmwareupdatemdv5.FirmwareUpdateMdGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.firmwareupdatemdv5.FirmwareUpdateMdStatusReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.indicatorv3.IndicatorGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.indicatorv3.IndicatorReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.indicatorv3.IndicatorSupportedGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.indicatorv3.IndicatorSupportedReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.manufacturerspecificv1.ManufacturerSpecificGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.meterv2.MeterGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.meterv2.MeterSupportedGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.meterv2.MeterSupportedReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.meterv2.MeterReset cmd) {}
//def zwaveEvent(hubitat.zwave.commands.multichannelassociationv2.MultiChannelAssociationSet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.multichannelassociationv2.MultiChannelAssociationGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.multichannelassociationv2.MultiChannelAssociationRemove cmd) {}
//def zwaveEvent(hubitat.zwave.commands.multichannelassociationv2.MultiChannelAssociationGroupingsGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.multichannelassociationv2.MultiChannelAssociationGroupingsReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.powerlevelv1.PowerlevelSet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.powerlevelv1.PowerlevelGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.powerlevelv1.PowerlevelReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.powerlevelv1.PowerlevelTestNodeSet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.powerlevelv1.PowerlevelTestNodeGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.powerlevelv1.PowerlevelTestNodeReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionSet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionSupportedGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionSupportedReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionEcSet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionEcGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionEcReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionTimeoutSet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionTimeoutGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionTimeoutReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityCommandsSupportedGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.securityv1.SecuritySchemeGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.securityv1.SecuritySchemeReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionReport cmd){}
//def zwaveEvent(hubitat.zwave.commands.switchmultilevelv4.SwitchMultilevelSet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.switchmultilevelv4.SwitchMultilevelGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.switchmultilevelv4.SwitchMultilevelSupportedGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.switchmultilevelv4.SwitchMultilevelSupportedReport cmd) {} 
//def zwaveEvent(hubitat.zwave.commands.switchmultilevelv4.SwitchMultilevelStartLevelChange cmd) {}
//def zwaveEvent(hubitat.zwave.commands.switchmultilevelv4.SwitchMultilevelStopLevelChange cmd) {}
//def zwaveEvent(hubitat.zwave.commands.versionv3.VersionGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.transportservicev1.CommandFirstFragment cmd) {}
//def zwaveEvent(hubitat.zwave.commands.transportservicev1.CommandFragmentRequest cmd) {}
//def zwaveEvent(hubitat.zwave.commands.transportservicev1.CommandSubsequentFragment cmd) {}
//def zwaveEvent(hubitat.zwave.commands.transportservicev1.CommandFragmentComplete cmd) {}
//def zwaveEvent(hubitat.zwave.commands.transportservicev1.CommandFragmentWait cmd) {}
//def zwaveEvent(hubitat.zwave.commands.versionv3.VersionGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.versionv3.VersionCommandClassGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.versionv3.VersionCommandClassReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.versionv3.VersionCapabilitiesGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.versionv3.VersionCapabilitiesReport cmd) {}
//def zwaveEvent(hubitat.zwave.commands.versionv3.VersionZWaveSoftwareGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.versionv3.VersionZWaveSoftwareGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.zwaveplusinfov2.ZwaveplusInfoGet cmd) {}
//def zwaveEvent(hubitat.zwave.commands.zwaveplusinfov2.ZwaveplusInfoReport cmd) {}
//*******************************************************************************/

//Functions to enhance text appearance
String bold(s)      { return "<b>$s</b>" }
String italic(s)    { return "<i>$s</i>" }
String mark(s)      { return "<mark>$s</mark>" }    //yellow background
String strike(s)    { return "<s>$s</s>" }
String underline(s) { return "<u>$s</u>" }
String hue(Integer h, String s) {
    h = Math.min(Math.max((h!=null?h:170),1),255)    //170 is Inovelli factory default blue
	def result =  '<font '
	if (h==255)     result += 'style="background-color:Gray" '
	if (h>30&&h<70) result += 'style="background-color:DarkGray" '
    if (h==255)     result += 'color="White"'
	else            result += 'color="' + hubitat.helper.ColorUtils.rgbToHEX(hubitat.helper.ColorUtils.hsvToRGB([(h/255*100), 100, 100])) + '"' 
	result += ">$s</font>"
    return result
}

//Reds
String indianRed(s)  { return '<font color = "IndianRed">' + s + '</font>'}
String lightCoral(s) { return '<font color = "LightCoral">' + s + '</font>'}
String crimson(s)    { return '<font color = "Crimson">' + s + '</font>'}
String red(s)        { return '<font color = "Red">' + s + '</font>'}
String fireBrick(s)  { return '<font color = "FireBrick">' + s + '</font>'}
String coral(s)      { return '<font color = "Coral">' + s + '</font>'}

//Oranges
String orangeRed(s)  { return '<font color = "OrangeRed">' + s + '</font>'}
String darkOrange(s) { return '<font color = "DarkOrange">' + s + '</font>'}
String orange(s)     { return '<font color = "Orange">' + s + '</font>'}

//Yellows
String gold(s)          { return '<font color = "Gold">' + s + '</font>'}
String yellow(s)        { return '<font color = "yellow">' + s + '</font>'}
String paleGoldenRod(s) { return '<font color = "PaleGoldenRod">' + s + '</font>'}
String peachPuff(s)     { return '<font color = "PeachPuff">' + s + '</font>'}
String darkKhaki(s)     { return '<font color = "DarkKhaki">' + s + '</font>'}

//Greens
String limeGreen(s)      { return '<font color = "LimeGreen">' + s + '</font>'}
String green(s)          { return '<font color = "green">' + s + '</font>'}
String darkGreen(s)      { return '<font color = "DarkGreen">' + s + '</font>'}
String olive(s)          { return '<font color = "Olive">' + s + '</font>'}
String darkOliveGreen(s) { return '<font color = "DarkOliveGreen">' + s + '</font>'}
String lightSeaGreen(s)  { return '<font color = "LightSeaGreen">' + s + '</font>'}
String darkCyan(s)       { return '<font color = "DarkCyan">' + s + '</font>'}
String teal(s)           { return '<font color = "Teal">' + s + '</font>'}

//Blues
String cyan(s)           { return '<font color = "Cyan">' + s + '</font>'}
String lightSteelBlue(s) { return '<font color = "LightSteelBlue">' + s + '</font>'}
String steelBlue(s)      { return '<font color = "SteelBlue">' + s + '</font>'}
String lightSkyBlue(s)   { return '<font color = "LightSkyBlue">' + s + '</font>'}
String deepSkyBlue(s)    { return '<font color = "DeepSkyBlue">' + s + '</font>'}
String dodgerBlue(s)     { return '<font color = "DodgerBlue">' + s + '</font>'}
String blue(s)           { return '<font color = "blue">' + s + '</font>'}
String midnightBlue(s)   { return '<font color = "midnightBlue">' + s + '</font>'}

//Purples
String magenta(s)       { return '<font color = "Magenta">' + s + '</font>'}
String rebeccaPurple(s) { return '<font color = "RebeccaPurple">' + s + '</font>'}
String blueViolet(s)    { return '<font color = "BlueViolet">' + s + '</font>'}
String slateBlue(s)     { return '<font color = "SlateBlue">' + s + '</font>'}
String darkSlateBlue(s) { return '<font color = "DarkSlateBlue">' + s + '</font>'}

//Browns
String burlywood(s)     { return '<font color = "Burlywood">' + s + '</font>'}
String goldenrod(s)     { return '<font color = "Goldenrod">' + s + '</font>'}
String darkGoldenrod(s) { return '<font color = "DarkGoldenrod">' + s + '</font>'}
String sienna(s)        { return '<font color = "Sienna">' + s + '</font>'}

//Grays
String lightGray(s) { return '<font color = "LightGray">' + s + '</font>'}
String darkGray(s)  { return '<font color = "DarkGray">' + s + '</font>'}
String gray(s)      { return '<font color = "Gray">' + s + '</font>'}
String dimGray(s)   { return '<font color = "DimGray">' + s + '</font>'}
String slateGray(s) { return '<font color = "SlateGray">' + s + '</font>'}
String black(s)     { return '<font color = "Black">' + s + '</font>'}

//**********************************************************************************
//****** End of HTML enhancement functions.
//**********************************************************************************
