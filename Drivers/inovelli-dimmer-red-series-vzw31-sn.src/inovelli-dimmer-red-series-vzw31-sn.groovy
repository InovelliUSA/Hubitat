def getDriverDate() { return "2023-08-14" /** + orangeRed(" (beta)") **/ }  // **** DATE OF THE DEVICE DRIVER **** //
//  ^^^^^^^^^^  UPDATE THIS DATE IF YOU MAKE ANY CHANGES  ^^^^^^^^^^
/**
* Inovelli VZW31-SN Red Series Z-Wave 2-in-1 Dimmer
*
* Author: Eric Maycock (erocm123)
* Contributor: Mark Amber (marka75160)
* Platform: Hubitat
*
* Copyright 2023 Eric Maycock / Inovelli
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
* 2022-11-28(EM) Initial commit
* 2022-12-02(EM) Added custom command ledEffectAll & ledEffectOne
* 2022-12-06(EM) Fix led effects when integer value is too large
* 2022-12-06(EM) Bug fixes for incorrect parameter size and workaround for unsigned integers above a certain value
* 2022-12-12(EM) Fix default values of param 2-8
* 2022-01-03(EM) Update things for firmware .03
* 2023-01-06(MA) Update parameter descriptions; add color text; add descriptions to ledEffect dropdowns and dim/ramp rates;
*                display detected Power Source (Neutral/non-Neutral); flip incorrect ledEffect color and duration until next firmware fix;
*                add driverDate, lastCommand, and lastCommandTime state variables; fix multi-line parameter logging in refresh() command
* 2023-01-08(MA) Reorder some things to align a little better with Blue series (helps with diff/compare); add trace logging
* 2023-01-09(MA) fix ledEffectOne ledNum offset calculation
* 2023-01-10(MA) improved ledEffect reporting in log and state variables
* 2023-01-11(MA) cleanup sendEvent doesn't use "displayed:false" on Hubitat
* 2023-01-12(MA) Updates for firmware v0.04
* 2023-01-12(MA) change QuickStart description to experimental
* 2023-01-22(MA) fix ledEffect sendEvent
* 2023-02-06(MA) Updates for firmware v0.05
* 2023-02-09(MA) Updates for firmware v0.06
* 2023-02-21(MA) Updates to get ready for production release.   Standardize across all V-Mark Blue and Red Gen2
* 2023-02-23(MA) fix Leading/Trailing error in non-neutral; misc code cleanup; more standardization between the different VMark devices
* 2023-02-26(MA) fix missing preferences; fix state.auxType; enhance parsing of Unknown Command and Unknown Attribute
* 2023-03-01(MA) synchronize all changes up to this point between VZM31, VZM35, and VZW31
* 2023-03-12(MA) add params 55,56; fix minor bugs and typos; prep for production firmware release.
* 2023-03-15(MA) display effect name instead of number in ledEffect attribute 
* 2023-03-31(MA) updates for v0.14 firmware; add p25,p58,p100; 
* 2023-04-03(MA) fix scene button mappings
* 2023-05-08(MA) fix duration on startLevelChange; fix input number range for P23
* 2023-06-15(MA) re-sync all models (VZM31/VZM35/VSW31) for consistent verbiage/function
* 2023-07-01(MA) removed "beta" designation
* 2023-07-03(EM) added URL to metadata
* 2023-08-14(EM) add processAssociation to updated() & configure() method
*
* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
* !!                                                                 !!
* !! DON'T FORGET TO UPDATE THE DRIVER DATE AT THE TOP OF THIS PAGE  !!
* !!                                                                 !!
* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
**/
import groovy.transform.Field

metadata {
    definition (name: "Inovelli Dimmer 2-in-1 Red Series VZW31-SN", namespace: "InovelliUSA", author: "E.Maycock/M.Amber", importUrl:  "https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-dimmer-red-series-vzw31-sn.src/inovelli-dimmer-red-series-vzw31-sn.groovy")
	{
		capability "Actuator"	//device can "do" something (has commands)
        capability "Sensor"		//device can "report" something (has attributes)

        capability "ChangeLevel"
        capability "Configuration"
        capability "EnergyMeter"				//Fan does not support energy monitoring but Dimmer does
        //capability "FanControl"
        capability "HoldableButton"
        capability "LevelPreset"
        capability "PowerMeter"					//Fan does not support power monitoring but Dimmer does
        capability "PushableButton"
        capability "Refresh"
        capability "ReleasableButton"
        //capability "SignalStrength"			//placeholder for future testing to see if this can be implemented
        capability "Switch"
        capability "SwitchLevel"

        attribute "lastButton", "String"		//last button event
        attribute "ledEffect", "String"			//LED effect that was requested
        //attribute "numberOfBindings", "String"	//(read only)
        attribute "smartBulb", "String"			//Smart Bulb mode enabled or disabled
        //attribute "smartFan", "String"			//Smart Fan mode enabled or disabled
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

        command "configure",           [[name:"Option",    type:"ENUM",   description:"blank=current states only, User=user changed settings only, All=configure all settings, Default=set all settings to default", constraints:[" ","User","All","Default"]]]

//		command "identify",			   [[name:"Seconds",   type:"NUMBER", description:"number of seconds to blink the LED bar so it can be identified (leave blank to see remaining seconds in the logs)"],
//										[name:"number of seconds to blink the LED bar so it can be identified (leave blank to see remaining seconds in the logs)"]]
		
        command "initialize",		   [[name:"clear state variables, clear LED notifications, refresh current states"]]
        
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
        
        command "refresh",             [[name:"Option",    type:"ENUM",   description:"blank=current states only, User=user changed settings only, All=refresh all settings", constraints: [" ","User","All"]]]
		
//		command "remoteControl",	   [[name:"Option*",   type:"ENUM",   description:"change the setting of Remote Protection (P257)", constraints: [" ","Enabled","Disabled"]]]

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

        fingerprint mfr:"031E", prod:"0015", deviceId:"0001", inClusters:"0x5E,0x20,0x26,0x85,0x8E,0x59,0x55,0x86,0x72,0x5A,0x87,0x73,0x98,0x9F,0x60,0x6C,0x70,0x5B,0x32,0x75,0x7A"
	fingerprint mfr:"031E", prod:"0015", deviceId:"0001", inClusters:"0x5E,0x26,0x85,0x8E,0x59,0x55,0x86,0x72,0x5A,0x87,0x73,0x98,0x9F,0x6C,0x70,0x5B,0x32,0x75,0x7A"
																																							   
    }
    preferences {
        getParameterNumbers().each{ i ->
            switch(configParams["parameter${i.toString().padLeft(3,"0")}"].type){
                case "number":
                    switch(i){
                        case 23:
							//special case for Quick Start is below
                            break
                        case 51:    //Device Bind Number
                            input "parameter${i}", "number",
                                title: "${i}. " + darkGreen(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name)),
                                description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description +
                                     "<br>Range=" + configParams["parameter${i.toString().padLeft(3,"0")}"].range),
                                //defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                                range: configParams["parameter${i.toString().padLeft(3,"0")}"].range
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
                        case 23:
							//special case for Quick Start is below
                            break
                        case 21:    //Power Source
						case 157:	//Remote Protection Zwave
						case 257:	//Remote Protection Zigbee
                            input "parameter${i}", "enum",
                                title: "${i}. " + darkGreen(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name)),
                                description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description),
                                //defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                                options: configParams["parameter${i.toString().padLeft(3,"0")}"].range
                            break
                        case 22:    //Aux Type
                        case 52:    //Smart Bulb Mode
                        case 158:   //Switch Mode Zwave
                        case 258:   //Switch Mode Zigbee
                            input "parameter${i}", "enum",
                                title: "${i}. " + indianRed(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name)),
                                description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description),
                                //defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
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
            }

            if (i==23) {  //quickStart is implemented in firmware for the fan, emulated in this driver for 2-in-1 Dimmer (experimental)
                if (state.model?.substring(0,5)!="VZM35") {
                    input "parameter${i}", "number",
                        title: "${i}. " + orangeRed(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name + " Level")),
                        description: orangeRed(italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description +
                            "<br>Range=" + configParams["parameter${i.toString().padLeft(3,"0")}"].range +
				    	    " Default=" +  configParams["parameter${i.toString().padLeft(3,"0")}"].default)),
                        //defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                        range: configParams["parameter${i.toString().padLeft(3,"0")}"].range
                } else {
					input "parameter${i}", "number",
						title: "${i}. " + darkSlateBlue(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name + " Duration")),
                        description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description +
                            "<br>Range=" + configParams["parameter${i.toString().padLeft(3,"0")}"].range +
				    	    " Default=" +  configParams["parameter${i.toString().padLeft(3,"0")}"].default),
                        //defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                        range: configParams["parameter${i.toString().padLeft(3,"0")}"].range
				}
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
        input name: "infoEnable",          type: "bool",   title: bold("Enable Info Logging"),   defaultValue: true
        input name: "traceEnable",         type: "bool",   title: bold("Enable Trace Logging"),  defaultValue: false
        input name: "debugEnable",         type: "bool",   title: bold("Enable Debug Logging"),  defaultValue: false
        input name: "disableInfoLogging",  type: "number", title: bold("Disable Info Logging after this number of minutes"),  description: italic("(0=Do not disable)"), defaultValue: 20
        input name: "disableTraceLogging", type: "number", title: bold("Disable Trace Logging after this number of minutes"), description: italic("(0=Do not disable)"), defaultValue: 10
        input name: "disableDebugLogging", type: "number", title: bold("Disable Debug Logging after this number of minutes"), description: italic("(0=Do not disable)"), defaultValue: 5
    }
}

def getParameterNumbers() {   //controls which options are available depending on whether the device is configured as a switch or a dimmer.
    if (parameter158 == "1") return [158,22,52,                  10,11,12,      15,17,18,19,20,21,25,50,            58,59,95,96,97,98,100,123,159,160,161,162]  //on/off mode
    else                     return [158,22,52,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,17,18,19,20,21,25,50,53,54,55,56,58,59,95,96,97,98,100,123,    160,    162]  //dimmer mode
}

@Field static Integer defaultDelay = 500    //default delay to use for zwave commands (in milliseconds)
@Field static Integer longDelay = 1000      //long delay to use for changing modes (in milliseconds)
@Field static Integer defaultQuickLevel=50 //default startup level for QuickStart emulation
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
        description: "Level the dimmer will return to when power is restored after power failure (if Switch is in On/Off Mode any level 1-99 will convert to 99).<br>0=Off<br>1-99=Set Level<br>0=Use previous level.",
        range: "0..99",
        default: 99,
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
        description: "Set the Aux switch type.",
        range: ["0":"No Aux (default)", "1":"Dumb 3-Way Switch", "2":"Smart Aux Switch", "3":"No Aux Full Wave (On/Off only)"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter023 : [ //implemented in firmware for the fan, emulated in this driver for 2-in-1 Dimmer
        number: 23,
        name: "Quick Start",
        description: "EXPERIMENTAL (hub commands only): Startup Level from OFF to ON (for LEDs that need higher level to turn on but can be dimmed lower) 0=Disabled",
        range: "0..99",
        default: 0,
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
        size: 8,
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
    parameter064 : [
        number: 64,
        name: "LED1 Notification",
        description: "4-byte encoded LED Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter069 : [
        number: 69,
        name: "LED2 Notification",
        description: "4-byte encoded LED Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter074 : [
        number: 74,
        name: "LED3 Notification",
        description: "4-byte encoded LED Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter079 : [
        number: 79,
        name: "LED4 Notification",
        description: "4-byte encoded LED Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter084 : [
        number: 84,
        name: "LED5 Notification",
        description: "4-byte encoded LED Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter089 : [
        number: 89,
        name: "LED6 Notification",
        description: "4-byte encoded LED Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter094 : [
        number: 94,
        name: "LED7 Notification",
        description: "4-byte encoded LED Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
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
        description: "4-byte encoded LED Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
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
        ]
]
private getCommandClassVersions() {
    [0x20: 1, 0x25: 1, 0x70: 1, 0x98: 1, 0x32: 3, 0x5B: 1]
}

def getVersion() {
    if (infoEnable) log.info "${device.displayName} getVersion()"
	def cmds = []
	cmds = [zwave.versionV1.versionGet()]
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
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

def calculateParameter(number) {
    def value = Math.round((settings."parameter${number}"!=null?settings."parameter${number}":configParams["parameter${number.toString().padLeft(3,'0')}"].default).toFloat()).toInteger()
    switch (number){
		case 21:	//Read-Only (Neutral/non-Neutral)
		case 51:	//Read-Only (Bindings)
		case 257:	//Read-Only (Remote Protecion)
			value = configParams["parameter${number.toString().padLeft(3,'0')}"].default	//Read-Only parameters calculate to their default values
			break
        case 9:     //Min Level
			value = Math.min(Math.max(value.toInteger(),1),54)
            break
        case 10:    //Max Level
			value = Math.min(Math.max(value.toInteger(),55),99)
            break
        case 13:    //Default Level (local)
        case 14:    //Default Level (remote)
        case 15:    //Level after power restored
		case 55:	//Double-Tap UP Level
		case 56:	//Double-Tap DOWN Level
			value = Math.min(Math.max(value.toInteger(),0),99)
            break
        case 18:    //Active Power Reports (percent change)
			value = Math.min(Math.max(value.toInteger(),0),100)
            break
        case 95:    //custom hue for LED Bar (when On)
        case 96:    //custom hue for LED Bar (when Off)
            //360-hue values need to be converted to byte values before sending to the device
            if (settings."parameter${number}custom" =~ /^([0-9]{1}|[0-9]{2}|[0-9]{3})$/) {
                value = Math.round((settings."parameter${number}custom").toInteger()/360*255)
            } else {   //else custom hue is invalid format or not selected
                if(settings."parameter${number}custom"!=null) {
                    device.clearSetting("parameter${number}custom")
                    if (infoEnable) log.warn "${device.displayName} " + fireBrick("Cleared invalid custom hue: ${settings."parameter${number}custom"}")
                }
            }
            break
        case 97:    //LED Bar Intensity(when On)
        case 98:    //LED Bar Intensity(when Off)
			value = Math.min(Math.max(value.toInteger(),0),100)
            break
    }
    return value
}

def configure(option) {    //THIS GETS CALLED AUTOMATICALLY WHEN NEW DEVICE IS ADDED OR WHEN CONFIGURE BUTTON SELECTED ON DEVICE PAGE
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.displayName} configure($option)"
    state.lastCommandSent =                        "configure($option)"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = "VZW31-SN"
    if (infoEnable||traceEnable||debugEnable) log.info "${device.displayName} Driver Date $state.driverDate"
    sendEvent(name: "numberOfButtons", value: 14)
    def cmds = []
    cmds += zwave.versionV1.versionGet()
    cmds += processAssociations()
    if (option!="All" && option!="Default") { //if we didn't pick option "All" or "Default" (so we don't read them twice) then preload the dimming/ramp rates and key parameters so they are not null in calculations
        for(int i = 1;i<=8;i++) if (state."parameter${i}value"==null) cmds += getParameter(i)
        cmds += getParameter(158)       //switch mode
        cmds += getParameter(22)        //aux switch type
        cmds += getParameter(52)        //smart bulb mode
        cmds += getParameter(21)        //power source (read-only)
        //cmds += getParameter(51)        //number of bindings (read-only)
        //cmds += getParameter(157)       //remote protection (read-only)
    }
    if (option!="") cmds += updated(option) //if option was selected on Configure button, pass it on to update settings.
    if (traceEnable) log.trace "${device.displayName} configure $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
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
        if (traceEnable) log.trace "${device.displayName} cycleSpeed $cmds"
    }
    return cmds
}

def initialize() {    //CALLED DURING HUB BOOTUP IF "INITIALIZE" CAPABILITY IS DECLARED IN METADATA SECTION
    state.clear()
    if (infoEnable) log.info "${device.displayName} initialize()"
    state.lastCommandSent =                        "initialize()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = "VZW31-SN"
    device.clearSetting("parameter23level") 
    device.clearSetting("parameter95custom") 
    device.clearSetting("parameter96custom") 
    def cmds = []
	cmds += ledEffectOne(1234567,255,0,0,0)	//clear any outstanding oneLED Effects
	cmds += ledEffectAll(255,0,0,0)			//clear any outstanding allLED Effects
    cmds += processAssociations()
    cmds += refresh()
    if (traceEnable) log.trace "${device.displayName} initialize $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def installed() {    //THIS IS CALLED WHEN A DEVICE IS INSTALLED
    log.info "${device.displayName} installed()"
    state.lastCommandSent =        "installed()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = "VZW31-SN"
    //configure()     //I confirmed configure() gets called at Install time so this isn't needed here
    return
}

def intTo8bitUnsignedHex(value) {
    return zigbee.convertToHexString(value?.toInteger(),2)
}

def intTo16bitUnsignedHex(value) {
    def hexStr = zigbee.convertToHexString(value.toInteger(),4)
    return new String(hexStr.substring(2, 4) + hexStr.substring(0, 2))
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
    if (traceEnable) log.trace "${device.displayName} ledEffectAll $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
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
    if (traceEnable) log.trace "${device.displayName} ledEffectOne $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
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
    if (traceEnable) log.trace "${device.displayName} off $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def on() {
    if (infoEnable) log.info "${device.displayName} on()"
    state.lastCommandSent =                        "on()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zwave.basicV2.basicSet(value: 0xFF)
    cmds += zwave.basicV2.basicGet()
    if (traceEnable) log.trace "${device.displayName} on $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def parse(String description) {
    if (debugEnable) log.debug "${device.displayName} parse($description)"
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
				cmd.nodeId.each {a
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
			if (infoEnable) log.info "${device.displayName} Basic Report received value ${cmd.value ? "on" : "off"} ($cmd.value)"
			dimmerEvents(cmd, (!state.lastRan || now() <= state.lastRan + 2000)?"digital":"physical")
			break
		case "CentralSceneNotification":
			if (infoEnable) log.info "${device.displayName} ${cmd}"
			switch(zigbee.convertToHexString(cmd.sceneNumber,2) + zigbee.convertToHexString(cmd.keyAttributes,2)) {
				case "0200":    //Tap Up 1x
					//if (state.model?.substring(0,5)!="VZM35") quickStart()  //If not Fan then emulate quickStart for local button push (this doesn't appear to work - not sure why)
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
					log.warn "${device.displayName} " + fireBrick("Undefined button function Scene: ${data[0]} Attributes: ${data[1]}")
					break
			}
			break
		case "ConfigurationReport":
				if (traceEnable) log.trace "${device.displayName} $cmd" //Received parameter=${cmd?.parameterNumber} value=${cmd?.scaledConfigurationValue} size=${cmd?.size}"
				def attrInt = cmd?.parameterNumber
				def scaled = cmd?.scaledConfigurationValue
				def valueInt = cmd?.size==1?(scaled<0?scaled+0x100:scaled):cmd.size==4?(scaled<0?scaled+0x100000000:scaled):scaled
				def valueStr = valueInt.toString()
				def valueHex = intTo32bitUnsignedHex(valueInt)
				def infoDev = "${device.displayName} "
				def infoTxt = "Receive parameter ${attrInt} value ${valueInt}"
				def infoMsg = infoDev + infoTxt
				if (attrInt>=1 && attrInt<=8) {
					if      (valueInt<101) valueStr=(valueInt/10).toString()+"s)"
					else if (valueInt<161) valueStr=(valueInt-100).toString()+"s)"
					else if (valueInt<255) valueStr=(valueInt-160).toString()+"m)"
				}
                switch (attrInt){
                    case 0:
                        infoMsg += " (temporarily saved current level ${valueInt}%)"
                        break
                    case 1:
                        infoMsg += " (Remote Dim Rate Up: " + (valueInt<255?valueStr:"default)")
                        break
                    case 2:
                        infoMsg += " (Local  Dim Rate Up: " + (valueInt<255?valueStr:"sync with 1)")
                        break
                    case 3:
                        infoMsg += " (Remote Ramp Rate On: " + (valueInt<255?valueStr:"sync with 1)")
                        break
                    case 4:
                        infoMsg += " (Local  Ramp Rate On: " + (valueInt<255?valueStr:"sync with 3)")
                        break
                    case 5:
                        infoMsg += " (Remote Dim Rate Down: " + (valueInt<255?valueStr:"sync with 1)")
                        break
                    case 6:
                        infoMsg += " (Local  Dim Rate Down: " + (valueInt<255?valueStr:"sync with 2)")
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
                        infoMsg = infoDev + darkGreen(infoTxt) + (valueInt==0?red(" (Non-Neutral)"):limeGreen(" (Neutral)"))
						state.powerSource = valueInt==0?"Non-Neutral":"Neutral"
                        break
                    case 22:    //Aux Type
                        switch (state.model?.substring(0,5)){
							case "VZM31":    //Blue 2-in-1 Dimmer
                            case "VZW31":    //Red  2-in-1 Dimmer
                                infoMsg = infoDev + indianRed(infoTxt + " " + (valueInt==0?"(No Aux)":(valueInt==1?"(Dumb 3-way)":(valueInt==2?"(Smart Aux)":(valueInt==3?"(No Aux Full Wave)":"(unknown type)")))))
								state.auxType =                         	  (valueInt==0? "No Aux": (valueInt==1? "Dumb 3-way": (valueInt==2? "Smart Aux": (valueInt==3? "No Aux Full Wave":  "unknown type $valueInt"))))
                                break
                            case "VZM35":    //Fan Switch
                                infoMsg = infoDev + indianRed(infoTxt + " " + (valueInt==0?"(No Aux)":"(Smart Aux)"))
                                state.auxType =                                valueInt==0? "No Aux":  "Smart Aux"
                                break
                            default:
                                infoMsg = infoDev + indianRed(infoTxt + " unknown model $state.model")
                                state.auxType =                          "unknown model ${state.model}"
                                break
                        }
                        break
                    case 23:    //Quick Start (in firmware on Fan, emulated in this driver for dimmer)
                        if  (state.model?.substring(0,5)!="VZM35") 
                            infoMsg += " (Quick Start " + (valueInt==0?red("disabled"):"${valueInt}%") + ")"
                        else 
                            infoMsg += " (Quick Start " + (valueInt==0?red("disabled"):"${valueInt} seconds") + ")"
                        break
                    case 25:    //Higher Output in non-Neutral
                        infoMsg += " (non-Neutral High Output " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
                        break
                    case 50:    //Button Press Delay
                        infoMsg += " (${valueInt*100}ms Button Delay)"
                        break
                    case 51:    //Device Bind Number
                        infoMsg = infoDev + darkGreen(infoTxt + " (Bindings)")
                        sendEvent(name:"numberOfBindings", value:valueInt)
                        break
                    case 52:    //Smart Bulb/Fan Mode
                        if (state.model?.substring(0,5)=="VZM35") { //FOR FAN ONLY
                            infoMsg = infoDev + indianRed(infoTxt) + (valueInt==0?red(" (SFM disabled)"):limeGreen(" (SFM enabled)"))
                            sendEvent(name:"smartFan", value:valueInt==0?"Disabled":"Enabled")
						} else { 
                            infoMsg = infoDev + indianRed(infoTxt) + (valueInt==0?red(" (SBM disabled)"):limeGreen(" (SBM enabled)"))
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
                    case 64:	//LED1 Notification
					case 69:	//LED2 Notification
					case 74:	//LED3 Notification
					case 79:	//LED4 Notification
					case 84:	//LED5 Notification
					case 89:	//LED6 Notification
					case 94:	//LED7 Notification
					case 99:	//All LED Notification
						def effectHex = valueHex.substring(0,2)
						int effectInt = Integer.parseInt(effectHex,16)
						infoMsg += " [0x${valueHex}] " + (effectInt==255?"(Stop Effect)":"(Start Effect ${effectInt})")
						break
                    case 95:  //LED bar color when on
                    case 96:  //LED bar color when off
                        infoMsg += hue(valueInt," (LED bar color when " + (attrInt==95?"On:":"Off:") + " ${Math.round(valueInt/255*360)}°)")
                        break
                    case 97:  //LED bar intensity when on
                    case 98:  //LED bar intensity when off
                        infoMsg += "% (LED bar intensity when " + (attrInt==97?"On)":"Off)")
                        break
					case 100:	//LED Bar Scaling
                        infoMsg += " (LED Scaling " + (valueInt==0?blue("VZM-style"):red("LZW-style")) + ")"
						break
					case 123:	//Aux Switch Scenes
                        infoMsg += " (Aux Scenes " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
						break
					case 125:	//Binding Off-to-On Sync Level
                        infoMsg += " (Send Level with Binding Off/On " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
						break
                    case 156:    //Local Protection
					case 256:
                        infoMsg += " (Local Control " + (valueInt==0?limeGreen("enabled"):red("disabled")) + ")"
                        break
                    case 157:    //Remote Protection
					case 257:
                        infoMsg = infoDev + darkGreen("$infoTxt (Remote Control ") + (valueInt==0?limeGreen("enabled"):red("disabled")) + darkGreen(")")
                        break
                    case 158:    //Switch Mode
					case 258:
                        switch (state.model?.substring(0,5)){
                            case "VZM31":    //Blue 2-in-1 Dimmer
                            case "VZW31":    //Red  2-in-1 Dimmer
                                infoMsg = infoDev + indianRed(infoTxt + " " + (valueInt==0?"(Dimmer mode)":"(On/Off mode)"))
                                sendEvent(name:"switchMode", value:valueInt==0?"Dimmer":"On/Off")
                                break
                            case "VZM35":    //Fan Switch
								infoMsg = infoDev + indianRed(infoTxt + " " + (valueInt==0?"(Multi-Speed mode)":"(On/Off mode)"))
								sendEvent(name:"switchMode", value:valueInt==0?"Multi-Speed":"On/Off")
								break
                            default:
                                infoMsg = infoDev + indianRed(infoTxt + " unknown model $state.model")
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
                        infoMsg += " [0x${intTo32bitUnsignedHex(valueInt)}] " + orangeRed("Undefined Parameter $attrInt")
                        break
                }
                if (infoEnable) log.info infoMsg
                //if ((attrInt==9)    //for zwave these are stored as 0-100, no need to convert
				//|| (attrInt==10)
				//|| (attrInt==13)
				//|| (attrInt==14)
				//|| (attrInt==15)
				//|| (attrInt==55)
				//|| (attrInt==56)) valueInt = convertByteToPercent(valueInt) //these attributes are stored as bytes but presented as percentages
                state."parameter${attrInt}value" = valueInt                   //update state variable with value received from device
                if (attrInt>0) device.updateSetting("parameter${attrInt}",[value:"${valueInt}",type:configParams["parameter${attrInt.toString().padLeft(3,"0")}"].type?.toString()]) //update local setting with value received from device  
                if ((attrInt==95 && parameter95custom!=null)||(attrInt==96 && parameter96custom!=null)) {   //if custom hue was set, update the custom state variable also
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
					if (traceEnable||debugEnable) log.trace "${device.displayName} Dimming Method = ${state.dimmingMethod}"
				}
                if ((valueInt==configParams["parameter${attrInt.toString()?.padLeft(3,"0")}"]?.default?.toInteger())             				 //IF  setting is the default
                && (attrInt!=21)&&(attrInt!=22)&&(attrInt!=51)&&(attrInt!=52)&&(attrInt!=157&&(attrInt!=257)&&(attrInt!=158)&&(attrInt!=258))) { //AND not read-only or primary config params
                    device.clearSetting("parameter${attrInt}")                                                                   				 //THEN clear the setting (so only changed settings are displayed)
                    if (traceEnable||debugEnable) log.trace "${device.displayName} parse() cleared parameter${attrInt} since it is the default"
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
					if (infoEnable) log.info "${device.displayName} Voltage report received value ${cmd.scaledMeterValue} V"
				} else if (cmd.meterType == 1) {
					sendEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
					if (infoEnable) log.info "${device.displayName} Energy report received value ${cmd.scaledMeterValue} kWh"
				}
			} else if (cmd.scale == 1) {
				sendEvent(name: "amperage", value: cmd.scaledMeterValue, unit: "A")
				if (infoEnable) log.info "${device.displayName} Amperage report received value ${cmd.scaledMeterValue} A"
			} else if (cmd.scale == 2) {
				sendEvent(name: "power", value: cmd.scaledMeterValue, unit: "W")
				if (infoEnable) log.info "${device.displayName} Power report received value ${cmd.scaledMeterValue} W"
			}
			break
		case "ProtectionReport":
			if (infoEnable) log.info "${device.displayName} Protection report received: Local protection is ${cmd.localProtectionState > 0 ? "on" : "off"} & Remote protection is ${cmd.rfProtectionState > 0 ? "on" : "off"}"
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
			if (infoEnable) log.info "${device.displayName} Switch Multilevel report received value ${cmd.targetValue ? "on" : "off"} ($cmd.targetValue)"
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
			if (infoEnable) log.info "${device.displayName} ${red('Unhandled:')} ${cmd}"
			break
	}
}

def presetLevel(value) {
    if (infoEnable) log.info "${device.displayName} presetLevel(${value})"
    state.lastCommandSent =                        "presetLevel(${value})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    Integer scaledValue = value==null?null:Math.min(Math.max(value.toInteger(),0),99)  //Zwave levels range from 1-99 with 0 = 'use previous'
    cmds += setParameter(13, scaledValue)
    if (traceEnable) log.trace "${device.displayName} preset $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def quickStart() {
    quickStartVariables()
	def startLevel = device.currentValue("level").toInteger()
	def cmds= []
	if (settings.parameter23?.toInteger()>0 ) {          //only do quickStart if enabled
		if (infoEnable) log.info "${device.displayName} quickStart(" + (state.model?.substring(0,5)!="VZM35"?"${settings.parameter23}%)":"${settings.parameter23}s)")
		if (state.model?.substring(0,5)!="VZM35") {      //IF not the Fan switch THEN emulate quickStart 
			//if (startLevel<state.parameter23value.toInteger()) cmds += zigbee.setLevel(state.parameter23value?.toInteger(),0,34)  //only do quickStart if currentLevel is < Quick Start Level (34ms is two sinewave cycles)
			//cmds += zigbee.setLevel(startLevel.toInteger(),0,longDelay) 
			if (startLevel<state.parameter23value.toInteger()) {
			cmds += zwave.switchMultilevelV4.switchMultilevelSet(value:settings.parameter23,dimmingDuration:0)}  //only do quickStart if currentLevel is < Quick Start Level
			cmds += zwave.switchMultilevelV4.switchMultilevelSet(value:startLevel,dimmingDuration:0)
			cmds += zwave.switchMultilevelV4.switchMultilevelGet()
			if (traceEnable) log.trace "${device.displayName} quickStart $cmds"
		}
	}
    return delayBetween(cmds.collect{ secureCmd(it) }, 34)  //34ms is two sinewave cycles
}

def quickStartVariables() {
    if (state.model?.substring(0,5)!="VZM35") {  //IF not the Fan switch THEN set the quickStart variables manually
        settings.parameter23 =  (settings.parameter23!=null?settings.parameter23:configParams["parameter023"].default).toInteger()
        state.parameter23value = Math.round((settings.parameter23?:0).toFloat())
        //state.parameter23level = Math.round((settings.parameter23level?:defaultQuickLevel).toFloat())
    }
}

def refresh(option) {
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.displayName} refresh(${option})"
    state.lastCommandSent =                        "refresh(${option})"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = "VZW31-SN"
    if (infoEnable||traceEnable||debugEnable) log.info "${device.displayName} Driver Date $state.driverDate"
    def cmds = []
	cmds += zwave.versionV1.versionGet()
    getParameterNumbers().each { i ->
        //def param_output = ""
        //param_output = param_output +  " parameter${i} value=${settings."parameter${i}"}, size=${configParams["parameter${i.toString().padLeft(3,"0")}"].size}"
        //if (infoEnable && (settings."parameter${i}"!=null)) log.info "${device.displayName}:" + param_output
		if (i==23 && (state.model?.substring(0,5)!="VZM35")) {  //quickStart is implemented in firmware for the fan, emulated in this driver for 2-in-1 Dimmer
			quickStartVariables()
		}
		switch (option) {
			case "":
			case " ":
			case null:
				if (((i>=1)&&(i<=8))&&(state?."parameter${i}value"==null)||(i==21)||(i==22)||(i==51)||(i==52)||(i==157)||(i==158)||(i==257)||(i==258)) cmds += getParameter(i) //if option is blank or null then refresh primary and read-only settings
				break
			case "User":                
				if (settings."parameter${i}"!=null) cmds += getParameter(i) //if option is User then refresh settings that are non-blank
				break
			case "All":
				cmds += getParameter(i) //if option is All then refresh all settings
				break
			default: 
				if (infoEnable||traceEnable||debugEnable) log.warn "${device.displayName} Unknonwn refresh option '${option}'"
				break
		}
    }
    if (debugEnable) {
		getParameterNumbers().each { i ->
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
	return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def resetEnergyMeter() {
    if (infoEnable) log.info "${device.displayName} resetEnergyMeter(" + device.currentValue("energy") + "kWh)"
    state.lastCommandSent =                        "resetEnergyMeter(" + device.currentValue("energy") + "kWh)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zwave.meterV2.meterReset()
    cmds += zwave.meterV2.meterGet(scale: 0)
    cmds += zwave.meterV2.meterGet(scale: 2)
    if (traceEnable) log.trace "${device.displayName} resetEnergyMeter $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def setLevel(value) {
	if (infoEnable) log.info "${device.displayName} setLevel($value)"
    state.lastCommandSent =                        "setLevel($value)"
    state.lastCommandTime = nowFormatted()
	def cmds = []
	cmds += zwave.switchMultilevelV4.switchMultilevelSet(value: value<100?value:99)
    cmds += zwave.switchMultilevelV4.switchMultilevelGet()
	if (traceEnable) log.trace "${device.displayName} setLevel $cmds"
	return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def setLevel(value, duration) {
    if (infoEnable) log.info "${device.displayName} setLevel($value" + (duration==null?")":", ${duration}s)")
    state.lastCommandSent =                        "setLevel($value" + (duration==null?")":", ${duration}s)")
    state.lastCommandTime = nowFormatted()
    duration = duration<128?duration:128+Math.round(duration/60)
    def cmds = []
    cmds += zwave.switchMultilevelV4.switchMultilevelSet(value: value<100?value:99, dimmingDuration: duration)
    cmds += zwave.switchMultilevelV4.switchMultilevelGet()
    if (traceEnable) log.trace "${device.displayName} setLevel $cmds"
	return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def setConfigParameter(number, value, size) {	//for backward compatibility
    return setParameter(number, value, size.toInteger())
}

def setParameter(number, value=null, size=null) {
	number = number?.toInteger()
	value  = value?.toInteger()
	size   = size?.toInteger()
	if (size==null || size==" ") size = configParams["parameter${number.toString().padLeft(3,'0')}"]?.size?:8
	if (infoEnable) log.info value!=null?"${device.displayName} setParameter($number, $value, $size)":"${device.displayName} getParameter($number)"
    state.lastCommandSent =  value!=null?                      "setParameter($number, $value, $size)":                      "getParameter($number)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    if (value!=null) cmds += zwave.configurationV4.configurationSet(parameterNumber: number, scaledConfigurationValue: size==1?(value<0x80?value:value-0x100):size==4?(value<0x80000000?value:value-0x100000000):value, size: size)
	if (number==52 || number==158 || number==258) cmds += "delay $longDelay"	//allow extra time when changing modes
	cmds += zwave.configurationV4.configurationGet(parameterNumber: number)
    if (traceEnable) log.trace value!=null?"${device.displayName} setParameter $cmds":"${device.displayName} getParameter $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def getParameter(number=0) {
	number = number?.toInteger()
    //if (infoEnable) log.info "${device.displayName} getParameter($number)"
    //state.lastCommandSent =                        "getParameter($number)"
    //state.lastCommandTime = nowFormatted() //this is not a custom command.  Only use state variable for commands on the device details page
    def cmds = []
	cmds += zwave.configurationV1.configurationGet(parameterNumber: number)
    if (traceEnable) log.trace "${device.displayName} getParameter $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
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
    if (traceEnable) log.trace "${device.displayName} startLevelChange $cmds"
	return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def stopLevelChange() {
    if (infoEnable) log.info "${device.displayName} stopLevelChange()" // at level " + device.currentValue("level")
    state.lastCommandSent =                        "stopLevelChange()"
    state.lastCommandTime = nowFormatted()
	def cmds = []
	cmds += zwave.switchMultilevelV4.switchMultilevelStopLevelChange()
    cmds += zwave.switchMultilevelV4.switchMultilevelGet()
    if (traceEnable) log.trace "${device.displayName} stopLevelChange $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
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
    if (traceEnable) log.trace "${device.displayName} startNotification $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def stopNotification(ep = null){	//for backward compatibility
	log.warn "${device.displayName} stopNotification(${red(bold('command is depreciated. Use ledEffectAll instead'))})"
    if (infoEnable) log.info "${device.displayName} stopNotification()"
    state.lastCommandSent =                        "stopNotification()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zwave.configurationV4.configurationSet(scaledConfigurationValue: 0, parameterNumber: ledNotificationEndpoints[(ep == null)? 0:ep?.toInteger()-1], size: 4)
    cmds += zwave.configurationV4.configurationGet(parameterNumber: ledNotificationEndpoints[(ep == null)? 0:ep?.toInteger()-1])
    if (traceEnable) log.trace "${device.displayName} stopNotification $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def toggle() {	
    def toggleDirection = device.currentValue("switch")=="off"?"off->on":"on->off"
    if (infoEnable) log.info "${device.displayName} toggle(${toggleDirection})"
    state.lastCommandSent =                        "toggle(${toggleDirection})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    // emulate toggle
    if (device.currentValue("switch")=="off") {
		if (settings.parameter23?.toInteger()>0) {
			cmds += quickStart()  //IF quickStart is enabled THEN quickStart
		} else {
			cmds += zwave.basicV2.basicSet(value: 0xFF)
			cmds += zwave.basicV2.basicGet()
		}
	} else {
		cmds += zwave.basicV2.basicSet(value: 0x00)
		cmds += zwave.basicV2.basicGet()
	}
    if (traceEnable) log.trace "${device.displayName} toggle $cmds"
    return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
}

def updated(option) { // called when "Save Preferences" is requested
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.displayName} updated(${option})"
    state.lastCommandSent =                        "updated(${option})"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = "VZW31-SN"
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
    int oldValue
    getParameterNumbers().each{ i ->
        defaultValue=configParams["parameter${i.toString().padLeft(3,'0')}"].default.toInteger()
        oldValue=state."parameter${i}value"!=null?state."parameter${i}value".toInteger():defaultValue
		//if ((i==9)||(i==10)||(i==13)||(i==14)||(i==15)||(i==55)||(i==56)) {    //convert the percent preferences back to byte values before testing for changes
		//    defaultValue=convertPercentToByte(defaultValue)
		//    oldValue=convertPercentToByte(oldValue)
		//}
		if (i==23 && parameter23level!=null) {
			if (parameter23level.toInteger()==defaultQuickLevel.toInteger()) device.clearSetting("parameter23level")
			}
        if ((i==95 && parameter95custom!=null)||(i==96 && parameter96custom!=null)) {                                         //IF  a custom hue value is set
            if ((Math.round(settings?."parameter${i}custom"?.toInteger()/360*255)==settings?."parameter${i}"?.toInteger())) { //AND custom setting is same as normal setting
                device.clearSetting("parameter${i}custom")                                                                    //THEN clear custom hue and use normal color 
                if (infoEnable) log.info "${device.displayName} Cleared Custom Hue setting since it equals standard color setting"
            }
            oldvalue=state."parameter${i}custom"!=null?state."parameter${i}custom".toInteger():oldValue
        }
        newValue = calculateParameter(i)
        if ((option == "Default")&&(i!=21)&&(i!=22)&&(i!=51)&&(i!=52)&&(i!=157)&&(i!=158)&&(i!=257)&&(i!=258)){    //if DEFAULT option was selected then use the default value (but don't change switch modes)
            newValue = defaultValue
            if (traceEnable||debugEnable) log.trace "${device.displayName} updated() has cleared parameter${i}"
            device.clearSetting("parameter${i}")  //and clear the local settings so they go back to default values
            if (i==23)          device.clearSetting("parameter${i}level")    //clear the custom quickstart level
            if (i==95 || i==96) device.clearSetting("parameter${i}custom")   //clear the custom custom hue values
        }
        //If a setting changed OR we selected ALL then update parameters in the switch (but don't change switch modes when ALL is selected)
        //log.debug "Param:$i default:$defaultValue oldValue:$oldValue newValue:$newValue setting:${settings."parameter$i"} `$option`"
        if ((newValue!=oldValue) 
        || ((option=="User")&&(settings."parameter${i}"!=null)) 
        || ((option=="Default"||option=="All")&&(i!=158)&&(i!=258))) {
			if (i!=21 && i!=51 && i!=257) {						//IF this is not a read-only parameter															
				cmds += setParameter(i, newValue.toInteger())	//THEN set the new value
				changedParams += i
				nothingChanged = false
			}
        }
        if ((i==23)&&(state.model?.substring(0,5)!="VZM35")) {  //IF not Fan switch THEN manually update the quickStart state variables since Dimmer does not store these
            quickStartVariables()
		}
    }
    //changedParams.each{ i ->     //read back the parameters we've changed so the state variables are updated 
    //    cmds += getParameter(i)  //no longer needed since we do a getParam after every setParam
    //}
    if (nothingChanged && (infoEnable||traceEnable||debugEnable)) {
	    log.info  "${device.displayName} No DEVICE settings were changed"
		log.info  "${device.displayName} Info logging  " + (infoEnable?limeGreen("Enabled"):red("Disabled"))
		log.trace "${device.displayName} Trace logging " + (traceEnable?limeGreen("Enabled"):red("Disabled"))
		log.debug "${device.displayName} Debug logging " + (debugEnable?limeGreen("Enabled"):red("Disabled"))
    }
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
    if (cmds) return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
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
   return delayBetween(cmds.collect{ secureCmd(it) }, defaultDelay)
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
String hue(h,s) {
    h = Math.min(Math.max((h!=null?h:170),1),255)    //170 is Inovelli factory default blue
	def result =  '<font '
	if (h==255 
	|| (h>40&&h<60)) result += 'style="background-color:lightGray" '
    if (h==255)      result += 'color="White"'
	else             result += 'color="' + hubitat.helper.ColorUtils.rgbToHEX(hubitat.helper.ColorUtils.hsvToRGB([(h/255*100), 100, 100])) + '"' 
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
String gray(s)      { return '<font color = "Gray">' + s + '</font>'}
String dimGray(s)   { return '<font color = "DimGray">' + s + '</font>'}
String slateGray(s) { return '<font color = "SlateGray">' + s + '</font>'}
String black(s)     { return '<font color = "Black">' + s + '</font>'}

//**********************************************************************************
//****** End of HTML enhancement functions.
//**********************************************************************************
