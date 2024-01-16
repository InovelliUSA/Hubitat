def getDriverDate() { return "2024-01-15" /* + orangeRed(" (beta)") */ }	// **** DATE OF THE DEVICE DRIVER
//  ^^^^^^^^^^  UPDATE THIS DATE IF YOU MAKE ANY CHANGES  ^^^^^^^^^^
/*
* Inovelli VZM31-SN Blue Series Zigbee 2-in-1 Dimmer
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
* 2024-01-15(MA) fix additional Aux scenes (P123 enabled) not getting parsed
* 2024-01-01(MA) reverse order of changelog (newest on top)
* 2023-12-22(MA) remove (comment out) code that does automatic group binding
* 2023-10-18(MA) move configParams map down to the bottom for easier scrolling
* 2023-10-14(MA) fix "Switch Mode" not changing; warn null ClusterID; fix null setLevel
* 2023-10-10(EM) add additional description information for aux switch and non-neutral settings
* 2023-10-01(MA) remove "beta" designation
* 2023-09-15(MA) streamlined Config All and Refresh All
* 2023-09-13(MA) streamlined parsing of individual LED parameters (P60-P94)
* 2023-09-06(MA) streamlined logging with traceCluster() method; change read-only params from Settings to Attributes
* 2023-08-28(MA) add P30-P33; add P60-P94; change "defaultDelay" to "shortDelay"
* 2023-07-01(MA) removed "beta" designation
* 2023-06-15(MA) re-sync all models (VZM31/VZM35/VZW31) for consistent verbiage/function
* 2023-06-01(MA) remove "double-click to update firmware" logic since new Hubitat firmware now prompts user to click OK or CANCEL 
* 2023-05-08(MA) fix duration on startLevelChange; fix input number range for P23
* 2023-05-01(MA) misc. minor bug fixes
* 2023-04-22(MA) update presetLevel to set Local AND Remote default levels; more code cleanup
* 2023-04-19(MA) rename bindSlave/bindSource back to bindTarget/bindInitiator to keep in line with Zigbee terminology
* 2023-04-17(MA) add gray background to the lighter yellow-green hues for easier viewing; lots of code cleanup; 
* 2023-04-16(MA) enhanced Group Binding to automatically bind/unbind groups that add/remove this switch in the Groups and Scenes App
* 2023-04-11(MA) Hotfix Add parsing for P125; fix P55-56 not scaling to percent
* 2023-04-10(MA) Hotfix P25 is 1-bit boolean, not 1-byte integer
* 2023-04-09(MA) added range/error checking to group binding; enhanced cluster name reporting; remove "beta" designation for v2.14 public release
* 2023-04-07(EM) Adding group binding support. See: https://community.inovelli.com/t/how-to-s-setup-zigbee-group-binding-hubitat/13909/1
* 2023-04-03(MA) add parameters 25,100,125 (for Dimmer v2.14+ firmware)
* 2023-03-31(MA) display effect name instead of number in ledEffect attribute 
* 2023-03-23(MA) rename bindInitiator/bindTarget to bindSlave/bindSource to reduce confusion
* 2023-03-12(MA) add params 55,56; fix minor bugs and typos; prep for production firmware release.
* 2023-03-01(MA) synchronize all changes up to this point between VZM31, VZM35, and VZW31; includes all current firmware changes
* 2023-02-26(MA) fix missing preferences; fix state.auxType; enhance parsing of Unknown Command and Unknown Attribute
* 2023-02-23(MA) fix Leading/Trailing error in non-neutral; misc code cleanup; more standardization between the different VMark devices
* 2023-02-21(MA) add setParameter/getParameter; add Aux Unique Scenes option; add state.dimmingMethod (leading/trailing)
* 2023-01-24(MA) decrease shortDelay slightly and increase longDelay slightly; small tweak to setLevel
* 2023-01-22(MA) fix ledEffect sendEvent
* 2023-01-18(MA) updates for Dimmer v2.10 firmware
* 2023-01-12(MA) change QuickStart description to experimental
* 2023-01-11(MA) cleanup sendEvent doesn't use "displayed:false" on Hubitat
* 2023-01-10(MA) improved ledEffect reporting in log and state variables
* 2023-01-08(MA) HOTFIX for ledEffect Integer/String error when called from Rule Machine
* 2023-01-04(MA) P257 is read-only, must be changed with remoteControl command 
* 2023-01-01(MA) enhanced Unknown Cluster/Attribute logging; fix typo in remoteControl command
* 2022-12-31(MA) more changes to quickStart emulation.
* 2022-12-30(MA) add more context to LED Effect dropdowns; add more detail to state.lastCommand
* 2022-12-28(MA) add Identify command; add device names to trace logging; add remoteControl command to allow re-enabling if Remote Protection (P257) is disabled
* 2022-12-26(MA) sync with updates to Fan driver
* 2022-12-12(MA) add presetLevel command; allow param15 to be set in on/off mode; workaround for setLevel bug in firmware (firmware ignores off-on duration)
* 2022-11-26(MA) fix Config Default not defaulting all parameters
* 2022-11-24(MA) improvements to quickStartEmulation
* 2022-11-18(MA) fix startLevelChange with null duration
* 2022-11-17(MA) more fixes for when user enters decimal (floating) values for an integer parameter
* 2022-11-05(MA) fix selection of multiple individual leds with ledEffectOne (e.g.1357 to select the odd leds and 246 to select the even leds)
* 2022-11-04(MA) fix 'siren' fast/slow effects (18/19) were backwards
* 2022-11-03(MA) updates for fw2.05: addeded param 262; added additional LED effects rising,falling,fast/slow, etc.
* 2022-11-02(EM) added warning for firmware update and requirement for double click. Enabled maximum level setting in on/off mode for "problem load" troubleshooting in 3-way dumb mode
* 2022-08-14(MA) emulate QuickStart for dimmer(can be disabled); add presetLevel command to use in Rule Machine - can also be done with setPrivateCluster custom command
* 2022-08-09(MA) added Trace logging; setCluster/setAttribute commands will Get current value if you leave Value blank
* 2022-08-02(MA) fix fan high speed not working with neutral; don't display min-level and max-level when in on/off mode; fix p9-p10 so they only get written if changed
* 2022-07-30(MA) fix params 9-10 so they send the full 0-255 to the switch; don't log Config() calls if info logging is off
* 2022-07-29(MA) add Quick Start to parsing/reporting; add driverDate variable so it can be seen on the Device page in Hubitat
* 2022-07-27(MA) updates for v1.12 firmware; remove details from last.command to keep it simple (details are in log.info)
* 2022-07-22(MA) don't request ramp rate for fan; more cleanup for production
* 2022-07-15(MA) cleanup and remove some unneeded debug code; add grey background to white text; remove unused report bindings
* 2022-07-08(MA) add param#261, add Aurora Effect, and other updates for firmware v1.11
* 2022-07-03(MA) new Refresh User option to only refresh User-changed settings; enhanced support for custom LED bar colors
* 2022-06-27(MA) param 95-98 titles change color to match selection; Add common Led groupings for the Led Effect One notification
* 2022-06-23(MA) add weblink to Hue Color Wheel for Custom LED color; add color to some log entries
* 2022-06-21(MA) enhanced logic for Fan speed changes when using setLevel()
* 2022-06-20(MA) fix condition where user enters decimal string (e.g. "12.3") for a parameter
* 2022-06-19(MA) minor adjustment to percent conversions to be consistent with Fan v4 firmware
* 2022-06-18(MA) fix startLevelChange to accept a duration value; fix setPrivateCluster and setZigbeeAttribute; standardize all variables to camelCase
* 2022-06-10(MA) merge with changes to 2022-06-10 VZM31-SN
* 2022-06-08(MA) lots of cleanup and minor bug fixes; add some code to detect model and more merges with VZM35
* 2022-06-07(MA) fix bug with null level on initial pairing
* 2022-06-06(MA) more updates to stay in sync with Fan v4 firmware
* 2022-06-04(MA) added parsing and logging for binding clusters - still under development
* 2022-05-31(MA) fix bug with dimRate in StartLevelChange
* 2022-05-30(MA) merge with changes to zigbee fan v4
* 2022-05-04(MA) added logging for Alexa Cluster
* 2022-05-03(MA) some additional support for zigbee binding app and a couple small tweaks to text and logging
* 2022-04-25(MA) updated for v10 (0x0A) firmware
* 2022-04-21(MA) change default LED intensity (params 97-98 ) to match PRD (33%/1%).  Also change switch mode (param 258) default to On/Off
* 2022-04-11(MA) sync up with changes to Zigbee Fan driver for consistency.  No functional changes to dimmer
* 2022-03-28(MA) added Alexa clusters to fingerprint ID, cleaned up a little code to match a little better with ST driver
* 2022-03-26(MA) updated for v9 firmware
* 2022-03-21(MA) replace empty ENUM values with " "  and fix basic on/off via rules and dashboard
* 2022-03-16(MA) remove doubleTap and add full 5-tap capability for Config button.
* 2022-03-12(MA) move switch config options to the top.  Add doubleTapped event for Config button. fix another rounding error
* 2022-03-10(MA) add null check before sending attribute
* 2022-03-09(MA) various tweaks and code optimizations to help merge with Fan driver.  Remove lastRan carryover from Red Series as its not used with the Blue Series
* 2022-03-08(MA) Hotfix: found another case where some settings were not getting sent to device, and remove extra level report from StopLevelChange
* 2022-03-07(MA) Even more detailed parsing.  Stub in some code for possible future "Preset Level" command
* 2022-03-06(MA) change hubitat hexutils to zigbee hexutils for compatibility with ST driver (which doesn't have the hubitat libraries)
* 2022-03-04(MA) Parameter21 auto-senses Neutral and is read-only.  Add powerSource attribute to display what the switch detected instead of what the user selected.
* 2022-03-03(MA) Hotfix: in some edge cases settings were not getting sent.
* 2022-03-01(MA) Rename with official model name in preparation for production release and standardize across other drivers (like the VZM35 Fan Switch)
* 2022-03_02(MA) More detailed parsing of Zigbee Description Map reports
* 2022-02-28(EM) Adding individual LED notifications, modifying parameters for firmware v8, & fixing issue that was preventing "initialize()" commands from being sent. 
* 2022-02-27(MA) Add default delay to Refresh commands so we're not waiting 2 seconds on every attribute
* 2022-02-24(MA) Replace recently added 'Reset Parameters' command with new options for the existing Config command. Options to reset all settings to Default or force All current settings to device.
* 2022-02-22(MA) Hotfix to remove spaces that broke multi-tap button events
* 2022-02-21(MA) Excluding Parmeter258 (Switch/Dimmer Output Mode) from Reset All command since it creates confusion with different parameter sets.
* 2022-02-20(MA) New 'Reset Parameters' command to reset ALL parameters and not just the ones it thinks have changed. 
* 2022-02-19(MA) Patches for bugs still in v7 firmware. (mostly for parameters 13-14)
* 2022-02-17(MA) Clean up tab/space and other formatting to simplify diff comparisons between HE and ST groovy drivers
* 2022-02-16(MA) Fix button released event.  Add Config button Held and Released events.  Add digital button support for testing scenes (un-comment in the metadata section to enable) 
* 2022-02-15(MA) Merged most (not all) changes into the ST driver.  No changes to this HE driver *** placeholder only                                                                                            
* 2022-02-14(MA) Arrange procedures in alphabetical order to help match edits between HE and ST. No other code changes
* 2022-02-11(MA) Add range checking for LEDeffect
* 2022-02-10(MA) Add bind() method to support the Zigbee Bindings app.
* 2022-02-09(MA) Enhance the 0-255 to 0-99 conversion formulas. Fix rounding down 1 to 0. ZigBee LEVEL range is 0x01-0xfe
* 2022-02-08(EM) Reverse change that broke LEDeffect
* 2022-02-04(MA) Fix startLevelChange to use dimming params in seconds.  Fix 0-99 vs 0-255 scaling on Default Levels (param 13-14)
* 2022-02-03(MA) Fix LEDeffect. Clean up some text/spelling/formatting.  More enhancements to logging
* 2022-02-02(EM) Changing speed parameters to match functionality. Updated setLevel method to use firmware speed options (param 1-8)
* 2022-02-01(MA) Lots of tweaks and enhancements to support v6 firmware update
* 2022-01-27(MA) Fix setLevel so it uses separate Dim Up / Dim Down rates (parameter1 and parameter5) 
* 2022-01-26(MA) Restore formatting on custom hue value - its needed to avoid div() errors on null values
* 2022-01-25(MA) Parameter259 (On/Off LED mode) should only be visible when in On/Off Mode
* 2022-01-24(MA) Add Custom Color override for the LED Indicator to allow any color from a standard hue color wheel
* 2022-01-23(MA) Fix range on Active Energy Report (parameter20)
* 2022-01-22(MA) More cleanup of the new parameter descriptions
* 2022-01-21(MA) Fix typo's and cleanup some of the new parameter descriptions
* 2022-01-20(EM) Some config parameter fixes. Energy measurement was changed to the simple metering cluster.
* 2022-01-20(EM) Fix scene reports not working since firmware v5. Need to "Save Preferences" to configure the reporting.
* 2022-01-20(EM) Changes to make the driver compatible with firmware v5.
* 2022-01-11(EM) Adding code for firmware update.
* 2022-01-06(EM) Updating parse section of code. Requesting firmware version and date.
* 2022-01-05(MA) Fix ranges for params 1-4.  These are slightly different for zigbee(blue) than they were for zwave(red)
* 2022-01-04(MA) Fix 'div() on null object' error when Param9 is blank.  Fix attribute size is 'bits' not 'bytes'
* 2022-01-03(EM) Change wording and make settings dynamic based on which mode is chosen
* 2021-12-30(EM) Adding options for power type and switch type
* 2021-12-27(EM) Starting to standardize logging
* 2021-12-23(EM) Adding min & max level parameters
* 2021-12-22(EM) Cleaning up and consolidating code
* 2021-12-21(EM) Adding configuration options
* 2021-12-20(EM) Adding additional parameters
* 2021-12-16(EM) Adding configuration options and working on code
* 2021-12-15(EM) Initial release.
* 
*/

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field
import hubitat.helper.ColorUtils
//import hubitat.helper.HexUtils
import java.security.MessageDigest

metadata {
    definition (name: "Inovelli Dimmer 2-in-1 Blue Series VZM31-SN", namespace: "InovelliUSA", author: "E.Maycock/M.Amber", filename: "Inovelli-zigbee-2-in-1-dimmer", importUrl:"https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-dimmer-blue-series-vzm31-sn.src/inovelli-dimmer-blue-series-vzm31-sn.groovy" ) 
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

        attribute "lastButton", "String"		//last button event
        attribute "ledEffect", "String"			//last LED effect requested (may have timed-out and not necessarily displaying currently)
		attribute "internalTemp", "String"		//Internal Temperature in Celsius	(read-only P32)
        attribute "numberOfBindings", "String"	//Group bindings count as 2			(read only P51)
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
        
        command "bind",				   [[name:"Command String", type:"STRING", description: "passthru for Binding Apps but may be used to manually enter ZDO Bind/Unbind commands"]]
        command "bindInitiator",       [[name:"use this 2nd on source (initiator) switch to COMPLETE binding with slave switch"]]
        command "bindTarget",		   [[name:"use this 1st on slave (target) switch to START binding with source switch"]]

        command "configure",           [[name:"Option",    type:"ENUM",   description:"blank=current states and user-changed settings, All=configure all settings, Default=set all settings to default", constraints:[" ","All","Default"]]]

		command "identify",			   [[name:"Seconds",   type:"NUMBER", description:"number of seconds to blink the LED bar so it can be identified (leave blank to see remaining seconds in the logs)"],
										[name:"number of seconds to blink the LED bar so it can be identified (leave blank to see remaining seconds in the logs)"]]
		
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
        
        command "refresh",             [[name:"Option",    type:"ENUM",   description:"blank=current states and user-changed settings, All=refresh all settings", constraints: [" ","All"]]]
		
		command "remoteControl",	   [[name:"Option*",   type:"ENUM",   description:"ability to control the switch remotely", constraints: [" ","Enabled","Disabled"]]]

        command "resetEnergyMeter"		//Fan does not support power/energy reporting but Dimmer does

        command "setParameter",        [[name:"Parameter*",type:"NUMBER", description:"Parameter number"],
                                        [name:"Raw Value", type:"NUMBER", description:"Value for the parameter (leave blank to get current value)"],
										[name:"Enter the internal raw value. Percentages and Color Hues are entered as 0-255. Leave blank to get current value"]]

		//uncomment this command if you need it for backward compatibility
        //command "setPrivateCluster",   [[name:"Number*", type:"NUMBER",   description:"setPrivateCluster is DEPRECIATED. Use setParameter instead"], 
        //                                [name:"Value*",  type:"NUMBER",   description:"setPrivateCluster is DEPRECIATED. Use setParameter instead"], 
        //                                [name:"Size*",   type:"ENUM",     description:"setPrivateCluster is DEPRECIATED. Use setParameter instead", constraints: ["8", "16","1"]],
        //                                [name:"DEPRECIATED",              description:"This command is depreciated.  Use setParameter instead"]]

        //Dimmer does not support setSpeed commands but Fan does
        //command "setSpeed",            [[name:"FanSpeed*", type:"ENUM",   constraints:["off","low","medium-low","medium","medium-high","high","up","down"]]]
		
        command "setZigbeeAttribute",  [[name:"Cluster*",  type:"NUMBER", description:"Cluster (in decimal) ex. Inovelli Private Cluster=0xFC31 input 64561"], 
                                        [name:"Attribute*",type:"NUMBER", description:"Attribute (in decimal) ex. 0x0100 input 256"], 
                                        [name:"Value",     type:"NUMBER", description:"Enter the value (in decimal, ex. 0x0F input 15) Leave blank to get current value without changing it"], 
                                        [name:"Size",      type:"ENUM",   description:"8=uint8, 16=uint16, 32=unint32, 1=bool",constraints: ["8", "16","32","1"]]]
        
        command "startLevelChange",    [[name:"Direction*",type:"ENUM",   description:"Direction for level change", constraints: ["up","down"]],
                                        [name:"Duration",  type:"NUMBER", description:"Transition duration in seconds"]]
        
        command "toggle"
        
        command "updateFirmware",	   [[name:"Firmware in this channel may be \"beta\" quality"]]

		fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,0008,0702,0B04,0B05,FC57,FC31", outClusters:"0003,0019",           model:"VZM31-SN", manufacturer:"Inovelli"
		fingerprint profileId:"0104", endpointId:"02", inClusters:"0000,0003",                                              outClusters:"0003,0019,0006,0008", model:"VZM31-SN", manufacturer:"Inovelli"
//      fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,0008,0702,0B04,FC31",           outClusters:"0003,0019",           model:"VZM31-SN", manufacturer:"Inovelli"
    }

    preferences {
        getParameterNumbers().each{ i ->
            switch(configParams["parameter${i.toString().padLeft(3,"0")}"].type){
                case "number":
                    switch(i){
						case getReadOnlyParams().contains(i):	
							//read-only params are non-settable, so skip user input
							break 
                        case 23:
							//special case for Quick Start is below
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
						case getReadOnlyParams().contains(i):	
							//read-only params are non-settable, so skip user input
							break 
                        case 23:
							//special case for Quick Start is below
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
            }

            if (i==23) {  //quickStart is implemented in firmware for the fan, emulated in this driver for 2-in-1 Dimmer (experimental)
                if (state.model?.substring(0,5)!="VZM35") {
                    input "parameter${i}", "number",
                        title: "${i}. " + orangeRed(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name + " Level (experimental)")),
                        description: orangeRed(italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description +
                            "<br>Range=" + configParams["parameter${i.toString().padLeft(3,"0")}"].range +
							" Default=" +  configParams["parameter${i.toString().padLeft(3,"0")}"].default)),
                        //defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                        range: configParams["parameter${i.toString().padLeft(3,"0")}"].range
                } else {
					input "parameter${i}", "number",
						title: "${i}. " + bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name + " Duration"),
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
		
        input name: "groupBinding1", type: "number", title: bold("Group Bind #1"), description: italic("Enter the Zigbee Group ID or leave blank to UNBind"), defaultValue: null, range: "1..65527"
        input name: "groupBinding2", type: "number", title: bold("Group Bind #2"), description: italic("Enter the Zigbee Group ID or leave blank to UNBind"), defaultValue: null, range: "1..65527"
        input name: "groupBinding3", type: "number", title: bold("Group Bind #3"), description: italic("Enter the Zigbee Group ID or leave blank to UNBind"), defaultValue: null, range: "1..65527"

        input name: "infoEnable",          type: "bool",   title: bold("Enable Info Logging"),   defaultValue: true,  description: italic("Log general device activity<br>(optional and not required for normal operation)")
        input name: "traceEnable",         type: "bool",   title: bold("Enable Trace Logging"),  defaultValue: false, description: italic("Additional info for trouble-shooting (not needed unless having issues)")
        input name: "debugEnable",         type: "bool",   title: bold("Enable Debug Logging"),  defaultValue: false, description: italic("Detailed diagnostic data<br>"+fireBrick("(only enable when asked by a developer)"))
        input name: "disableInfoLogging",  type: "number", title: bold("Disable Info Logging after this number of minutes"),  description: italic("(0=Do not disable)"), defaultValue: 20
        input name: "disableTraceLogging", type: "number", title: bold("Disable Trace Logging after this number of minutes"), description: italic("(0=Do not disable)"), defaultValue: 10
        input name: "disableDebugLogging", type: "number", title: bold("Disable Debug Logging after this number of minutes"), description: italic("(0=Do not disable)"), defaultValue: 5
    }
}

def getParameterNumbers() {   //controls which options are available depending on whether the device is configured as a switch or a dimmer.
    if (parameter258 == "1") return [258,22,52,                  10,11,12,      15,17,18,19,20,21,25,50,51,            95,96,97,98,100,123,125,256,257,259,260,261,262]  //on/off mode
    else                     return [258,22,52,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,17,18,19,20,21,25,50,51,53,54,55,56,95,96,97,98,100,123,125,256,257,    260,    262]  //dimmer mode
}
def getReadOnlyParams() {
	return [0,21,32,33,51,157,257]
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

def bind(cmds=[]) {
    if (infoEnable) log.info "${device.displayName} bind(${cmds})"
    state.lastCommandSent =						   "bind(${cmds})"
    state.lastCommandTime = nowFormatted()
    return cmds
}

def bindGroup(action="", group=0) {
    if (infoEnable) log.info "${device.displayName} bindGroup($action, $group))"
    state.lastCommandSent =                        "bindGroup($action, $group))"
    state.lastCommandTime = nowFormatted()
	def cmds = []
	if (action=="bind" || action=="unbind") {
		cmds += ["zdo $action 0x${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {${zigbee.convertToHexString(group.toInteger(),4)}}"]
		cmds += ["zdo $action 0x${device.deviceNetworkId} 0x02 0x01 0x0008 {${device.zigbeeId}} {${zigbee.convertToHexString(group.toInteger(),4)}}"]
		cmds += "delay 60000"		//binding can take up to 60 seconds 
		cmds += getParameter(51)	//update number of bindings counter
		sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
	} else {
		if (infoEnable) log.warn "${device.displayName} " + fireBrick("Invalid Bind action: '$action'")
		}
    if (debugEnable) log.debug "${device.displayName} bindGroup $cmds"
    return
}

def bindInitiator() {
    if (infoEnable) log.info "${device.displayName} bindInitiator()" 
    state.lastCommandSent =            			   "bindInitiator()" 
    state.lastCommandTime = nowFormatted()
    def cmds = zigbee.command(0xfc31,0x04,["mfgCode":"0x122F"],shortDelay,"0")
    if (debugEnable) log.debug "${device.displayName} bindInitiator $cmds"
    return cmds
}

def bindTarget(Integer timeout=30) {
    if (infoEnable) log.info "${device.displayName} bindTarget($timeout)"
    state.lastCommandSent =             		   "bindTarget($timeout)"
    state.lastCommandTime = nowFormatted()
    def cmds = setZigbeeAttribute(3,0,timeout,16)
    if (debugEnable) log.debug "${device.displayName} bindTarget $cmds"
    return cmds
}

def calculateParameter(paramNum) {
	paramNum = paramNum?:0
    def value = Math.round((settings?."parameter${paramNum}"!=null?settings?."parameter${paramNum}":getDefaultValue(paramNum))?.toFloat())?.toInteger()
    switch (paramNum){
        case 9:     //Min Level
        case 10:    //Max Level
        case 13:    //Default Level (local)
        case 14:    //Default Level (remote)
        case 15:    //Level after power restored
		case 55:	//Double-Tap UP Level
		case 56:	//Double-Tap DOWN Level
            value = convertPercentToByte(value)    //convert levels from percent to byte values before sending to the device
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
                    device.removeSetting("parameter${paramNum}custom")
                    if (infoEnable||traceEnable||debugEnable) log.warn "${device.displayName} " + fireBrick("Cleared invalid custom hue: ${settings."parameter${paramNum}custom"}")
                }
            }
            break
    }
    return value?:0
}

def calculateSize(size) {
    if (traceEnable) log.trace "${device.displayName} calculateSize(${size})"
	if (size==null || size==" ") size = configParams["parameter${number.toString().padLeft(3,'0')}"]?.size?:8
    if      (size.toInteger() == 1)  return 0x10    //1-bit boolean
    else if (size.toInteger() == 8)  return 0x20    //1-byte unsigned integer
    else if (size.toInteger() == 16) return 0x21    //2-byte unsigned integer
    else if (size.toInteger() == 24) return 0x22    //3-byte unsigned integer
    else if (size.toInteger() == 32) return 0x23    //4-byte unsigned integer
    else if (size.toInteger() == 40) return 0x24    //5-byte unsigned integer
    else if (size.toInteger() == 48) return 0x25    //6-byte unsigned integer
    else if (size.toInteger() == 56) return 0x26    //7-byte unsigned integer
    else if (size.toInteger() == 64) return 0x27    //8-byte unsigned integer
    else                             return 0x20    //default to 1-byte unsigned if no other matches
}

def clearSetting(i) {
	i = i?:0
	def cleared = false
	if (settings."parameter${i}"!=null)   {cleared=true; device.removeSetting("parameter" + i)}
	if (state."parameter${i}value"!=null) {cleared=true; state.remove("parameter" + i + "value")}
	if (cleared && (traceEnable||debugEnable)) log.trace "${device.displayName} cleared P${i} since it is the default"
}

def clusterLookup(cluster) {
	if (cluster==null) return "null ClusterID"
	else {
		//return zigbee.clusterLookup(cluster)
		return zigbee.clusterLookup(cluster)?:cluster==0x8021?"Binding Cluster":
										   cluster==0x8022?"UNBinding Cluster":
										   cluster==0x8032?"Routing Table Cluster":
										   cluster==0xFC31?"Private Cluster":
										   "Cluster:0x${zigbee.convertToHexString(cluster,4)}"
	}
}

def configure(option) {    //THIS GETS CALLED AUTOMATICALLY WHEN NEW DEVICE IS ADDED OR WHEN CONFIGURE BUTTON SELECTED ON DEVICE PAGE
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.displayName} configure($option)"
    state.lastCommandSent =                        "configure($option)"
    state.lastCommandTime = nowFormatted()
	sendEvent(name: "numberOfButtons", value: settings.parmeter23?28:14)
    def cmds = []
	if (infoEnable) log.info "${device.displayName} re-establish lifeline bindings to hub"
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0000 {${device.zigbeeId}} {}", "delay ${666}"] //Basic Cluster
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0000 {${device.zigbeeId}} {}", "delay ${666}"] //Basic Cluster ep2
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0003 {${device.zigbeeId}} {}", "delay ${666}"] //Identify Cluster
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0003 {${device.zigbeeId}} {}", "delay ${666}"] //Identify Cluster ep2
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0004 {${device.zigbeeId}} {}", "delay ${666}"] //Group Cluster
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0004 {${device.zigbeeId}} {}", "delay ${666}"] //Group Cluster ep2
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0005 {${device.zigbeeId}} {}", "delay ${666}"] //Scenes Cluster
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0005 {${device.zigbeeId}} {}", "delay ${666}"] //Scenes Cluster ep2
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}", "delay ${666}"] //On_Off Cluster
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}", "delay ${666}"] //On_Off Cluster ep2
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0008 {${device.zigbeeId}} {}", "delay ${666}"] //Level Control Cluster
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0008 {${device.zigbeeId}} {}", "delay ${666}"] //Level Control Cluster ep2
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0019 {${device.zigbeeId}} {}", "delay ${666}"] //OTA Upgrade Cluster
	if (state.model?.substring(0,5)!="VZM35") {  //Fan does not support power/energy reports
		cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0702 {${device.zigbeeId}} {}", "delay ${666}"] //Simple Metering - to get energy reports
		cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0B04 {${device.zigbeeId}} {}", "delay ${666}"] //Electrical Measurement - to get power reports
	}
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x8021 {${device.zigbeeId}} {}", "delay ${666}"] //Binding Cluster 
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x8022 {${device.zigbeeId}} {}", "delay ${666}"] //UnBinding Cluster
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0xFC31 {${device.zigbeeId}} {}", "delay ${666}"] //Private Cluster
	cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0xFC31 {${device.zigbeeId}} {}", "delay ${666}"] //Private Cluster ep2

    if (option=="") {		//IF   we didn't pick an option 
		cmds += refresh()	//THEN refresh read-only and key parameters
    } else { 				//ELSE read device attributes and pass on to update settings.
		cmds += readDeviceAttributes()
		cmds += updated(option)
	}
    if (debugEnable) log.debug "${device.displayName} configure $cmds"
    return cmds
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
	paramValue=configParams["parameter${paramNum.toString()?.padLeft(3,"0")}"]?.default?.toInteger()
	return paramValue?:0
	}

def identify(seconds) {
    if (infoEnable) log.info "${device.displayName} identify(${seconds==null?"":seconds})"
    state.lastCommandSent =                        "identify(${seconds==null?"":seconds})"
    state.lastCommandTime = nowFormatted()
    def cmds = setZigbeeAttribute(3,0,seconds,16)
    if (debugEnable) log.debug "${device.displayName} identify $cmds"
    return cmds
}

def initialize() {    //CALLED DURING HUB BOOTUP IF "INITIALIZE" CAPABILITY IS DECLARED IN METADATA SECTION
    log.info "${device.displayName} initialize()"
    //save the group IDs before clearing all the state variables and reset them after
	if (state.groupBinding1) saveBinding1 = state.groupBinding1
	if (state.groupBinding2) saveBinding2 = state.groupBinding2
	if (state.groupBinding3) saveBinding3 = state.groupBinding3
    state.clear()
	if (saveBinding1) state.groupBinding1 = saveBinding1
	if (saveBinding2) state.groupBinding2 = saveBinding2
	if (saveBinding3) state.groupBinding3 = saveBinding3
    state.lastCommandSent = "initialize()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = device.getDataValue('model')
    device.removeSetting("parameter23level")
    device.removeSetting("parameter95custom")
    device.removeSetting("parameter96custom")
    def cmds = []
	cmds += ledEffectOne(1234567,255,0,0,0)	//clear any outstanding oneLED Effects
	cmds += ledEffectAll(255,0,0,0)			//clear any outstanding allLED Effects
    cmds += refresh()
    if (debugEnable) log.debug "${device.displayName} initialize $cmds"
    return cmds
}

def installed() {    //THIS IS CALLED WHEN A DEVICE IS INSTALLED
    log.info "${device.displayName} installed()"
    state.lastCommandSent =        "installed()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = device.getDataValue('model')
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
    cmds += zigbee.command(0xfc31,0x01,["mfgCode":"0x122F"],shortDelay,"${intTo8bitUnsignedHex(cmdEffect)} ${intTo8bitUnsignedHex(cmdColor)} ${intTo8bitUnsignedHex(cmdLevel)} ${intTo8bitUnsignedHex(cmdDuration)}")
    if (debugEnable) log.debug "${device.displayName} ledEffectAll $cmds"
    return cmds
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
        Integer cmdLedNum = (it.toInteger()-1)    //lednum is 0-based in firmware 
        Integer cmdEffect = effect.toInteger()
        Integer cmdColor = color.toInteger()
        Integer cmdLevel = level.toInteger()
        Integer cmdDuration = duration.toInteger()
        cmds += zigbee.command(0xfc31,0x03,["mfgCode":"0x122F"],100,"${intTo8bitUnsignedHex(cmdLedNum)} ${intTo8bitUnsignedHex(cmdEffect)} ${intTo8bitUnsignedHex(cmdColor)} ${intTo8bitUnsignedHex(cmdLevel)} ${intTo8bitUnsignedHex(cmdDuration)}")
    }
    if (debugEnable) log.debug "${device.displayName} ledEffectOne $cmds"
    return cmds
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
    cmds += zigbee.off(shortDelay)
    if (debugEnable) log.debug "${device.displayName} off $cmds"
    return cmds
}

def on() {
    if (infoEnable) log.info "${device.displayName} on()"
    state.lastCommandSent =                        "on()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    if (settings.parameter23?.toInteger()>0) cmds += quickStart() //do quickStart if enabled
	else cmds += zigbee.on(shortDelay)						  //ELSE just turn On
    if (debugEnable) log.debug "${device.displayName} on $cmds"
    return cmds
}
def traceCluster(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
    //try {      def eventDesc=zigbee.getEvent(description)}
	//catch (e) {def eventDesc=null}
	def traceClusterName= clusterLookup(descMap.clusterInt?.toInteger())
	log.trace "${device.displayName} ${darkOrange(traceClusterName)} " +
		"(cluster:${descMap.clusterId!=null?descMap.clusterId:descMap.cluster}" +
        (descMap.attrId==null?"":" attr:${descMap.attrId}") +
        (descMap.value ==null?"":" value:${descMap.value}") +
        (descMap.data  ==null?"":" data:${descMap.data}") + 
        ")"	
}

def parse(String description) {
    if (traceEnable) log.trace "${device.displayName} parse($description)"
    Map descMap = zigbee.parseDescriptionAsMap(description)
    try {
		if (debugEnable && (zigbee.getEvent(description)!=[:])) log.debug "${device.displayName} zigbee.getEvent ${zigbee.getEvent(description)}"
	} catch (e) {
		if (debugEnable) log.debug "${device.displayName} "+magenta(bold("There was an error while calling zigbee.getEvent: $description"))
	}
	def attrId=		descMap.attrId
    def attrInt=	descMap.attrInt
    def clusterId=	descMap.clusterId!=null?descMap.clusterId:descMap.cluster
    def clusterInt=	descMap.clusterInt
	def clusterName=clusterLookup(clusterInt)
	def valueInt	//declared empty at top level so values can be assigned in each case and then displayed at the bottom
    //try {
	//	def valueInt=Integer.parseInt(descMap['value'],16)
	//} catch (e) {
	//	def valueInt=null
	//}
    def valueStr =   descMap['value']?:"unknown"
    switch (clusterInt){
        case 0x0000:    //BASIC CLUSTER
            if (traceEnable||debugEnable) traceCluster(description)
            switch (attrInt) {
                case 0x0000:
                    if (infoEnable) log.info "${device.displayName} ZCL Version=$valueStr"
                    state.zclVersion = valueStr
                    break
                case 0x0001:
                    if (infoEnable) log.info "${device.displayName} Application Version=$valueStr"
                    state.applicationVersion = valueStr
                    break
                case 0x0002:
                    if (infoEnable) log.info "${device.displayName} Stack Version=$valueStr"
                    state.stackVersion = valueStr
                    break
                case 0x0003:
                    if (infoEnable) log.info "${device.displayName} HW Version=$valueStr"
                    state.hwVersion = valueStr
                    break
                case 0x0004:
                    if (infoEnable) log.info "${device.displayName} Mfg=$valueStr"
                    state.manufacturer = valueStr
					if (device.getDataValue('manufacturer')!= valueStr) device.updateDataValue('manufacturer',valueStr)
                    break
                case 0x0005:
                    if (infoEnable) log.info "${device.displayName} Model=$valueStr"
                    state.model = valueStr
					if (device.getDataValue('model')!= valueStr) device.updateDataValue('model',valueStr)
                    break
                case 0x0006:
                    if (infoEnable) log.info "${device.displayName} FW Date=$valueStr"
					state.fwDate = valueStr
                    break
                case 0x0007:
                    valueInt = Integer.parseInt(descMap['value'],16)
                    valueStr = (valueInt==0?"Non-Neutral":(valueInt==1?"Neutral":"undefined"))
                    if (infoEnable) log.info "${device.displayName} Power Source=$valueInt ($valueStr)"
					sendEvent(name:"powerSource", value:valueStr)
                    state.parameter21value = valueInt
                    device.updateSetting("parameter21",[value:"${valueInt}",type:"enum"]) 
                    break
                case 0x4000:
                    if (infoEnable) log.info "${device.displayName} FW Version=$valueStr"
					state.fwVersion = valueStr
                    break
                default:
                    log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Attribute:$attrInt ") //+ descMap
                    break
            }
            break
        case 0x0001:    //Power configuration
            //if (infoEnable||debugEnable) log.info "${device.displayName} " + darkOrange("Power_Configuration Cluster:") + (debugEnable?descMap:"")
            if (infoEnable||traceEnable||debugEnable) traceCluster(description)
            break
        case 0x0002:    //Device temperature configuration
            //if (infoEnable||debugEnable) log.info "${device.displayName} " + darkOrange("Device_Temperature Cluster:") + (debugEnable?descMap:"")
            if (infoEnable||traceEnable||debugEnable) traceCluster(description)
            break
        case 0x0003:    //IDENTIFY CLUSTER
            if (traceEnable||debugEnable) traceCluster(description)
            switch (attrInt) {
                case 0x0000:
                    if (infoEnable) log.info "${device.displayName} Remaining Identify Time=${zigbee.convertHexToInt(valueStr)}s"
                    break
                default:
                    log.warn "${device.displayName} "+fireBrick("${clusterName} Unknown Attribute:$attrInt ") //+ descMap
                    break
            }
            break
        case 0x0004:    //GROUP CLUSTER
            if (traceEnable||debugEnable) traceCluster(description)
            switch (attrInt) {
                case 0x0000:
                    if (infoEnable) log.info "${device.displayName} Group Name Support=$valueStr"
                    break
                default:
                    if (attrInt==null && descMap.messageType=="00" && descMap.direction=="01") {
						def groupNumHex = descMap.data[2]+descMap.data[1]
						def groupNumInt = zigbee.convertHexToInt(groupNumHex)
						switch (descMap.command) {
							case "00":
								if (infoEnable) log.info "${device.displayName} Add Group $groupNumInt (0x$groupNumHex)"
								//bindGroup("bind",groupNumInt)
								//if (settings.groupBinding1==null || settings.groupBinding1?.toInteger()==groupNumInt || state.groupBinding1?.toInteger()==groupNumInt) {
								//	device.updateSetting("groupBinding1",[value:groupNumInt,type:"number"])
								//	state.groupBinding1=groupNumInt
								//} else if (settings.groupBinding2==null || settings.groupBinding2?.toInteger()==groupNumInt || state.groupBinding2?.toInteger()==groupNumInt) {
								//	device.updateSetting("groupBinding2",[value:groupNumInt,type:"number"])
								//	state.groupBinding2=groupNumInt
								//} else if (settings.groupBinding3==null || settings.groupBinding3?.toInteger()==groupNumInt || state.groupBinding3?.toInteger()==groupNumInt) {
								//	device.updateSetting("groupBinding3",[value:groupNumInt,type:"number"])
								//	state.groupBinding3=groupNumInt
								//}
								break
							case "01":
								if (infoEnable) log.info "${device.displayName} View Group $groupNumInt (0x$groupNumHex)"
								break
							case "02":
								if (infoEnable) log.info "${device.displayName} Get Group $groupNumInt (0x$groupNumHex)"
								break
							case "03":
								if (infoEnable) log.info "${device.displayName} Remove Group $groupNumInt (0x$groupNumHex)"
								//bindGroup("unbind",groupNumInt)
								//if      (settings.groupBinding1?.toInteger()==groupNumInt || state.groupBinding1?.toInteger()==groupNumInt) {device.removeSetting("groupBinding1"); state.remove(groupBinding1)}
								//else if (settings.groupBinding2?.toInteger()==groupNumInt || state.groupBinding2?.toInteger()==groupNumInt) {device.removeSetting("groupBinding2"); state.remove(groupBinding2)}
								//else if (settings.groupBinding3?.toInteger()==groupNumInt || state.groupBinding3?.toInteger()==groupNumInt) {device.removeSetting("groupBinding3"); state.remove(groupBinding3)}
								break
							case "04":
								if (infoEnable) log.info "${device.displayName} Remove All Groups"
								device.removeSetting("groupBinding1"); state.remove(groupBinding1)
								device.removeSetting("groupBinding2"); state.remove(groupBinding2)
								device.removeSetting("groupBinding3"); state.remove(groupBinding3)
								break
							case "05":
								if (infoEnable) log.info "${device.displayName} Add group if Identifying"
								break
							default:
								log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
								break
						}
						// remove duplicate groups
						if (settings.groupBinding3!=null && settings.groupBinding3==settings.groupBinding2) {device.removeSetting("groupBinding3"); state.remove(groupBinding3); log.info "${device.displayName} Removed duplicate Group Bind #3"}
						if (settings.groupBinding2!=null && settings.groupBinding2==settings.groupBinding1) {device.removeSetting("groupBinding2"); state.remove(groupBinding2); log.info "${device.displayName} Removed duplicate Group Bind #2"}
						if (settings.groupBinding1!=null && settings.groupBinding1==settings.groupBinding3) {device.removeSetting("groupBinding3"); state.remove(groupBinding3); log.info "${device.displayName} Removed duplicate Group Bind #3"}
					} else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Attribute:$attrInt ") //+ descMap
                    break
            }
            break
        case 0x0005:    //SCENES CLUSTER
            if (traceEnable||debugEnable) traceCluster(description)
            switch (attrInt) {
                case 0x0000:
                    if (infoEnable) log.info "${device.displayName} Scene Count=$valueStr"
                    break
                case 0x0001:
                    if (infoEnable) log.info "${device.displayName} Current Scene=$valueStr"
                    break
                case 0x0002:
                    if (infoEnable) log.info "${device.displayName} Current Group=$valueStr"
                    break
                case 0x0003:
                    if (infoEnable) log.info "${device.displayName} Scene Valid=$valueStr"
                    break
                case 0x0004:
                    if (infoEnable) log.info "${device.displayName} Scene Name Support=$valueStr"
                    break
                default:
                    log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Attribute:$attrInt ") //+ descMap
                    break
            }
            break
        case 0x0006:    //ON_OFF CLUSTER
            if (traceEnable||debugEnable) traceCluster(description)
            switch (attrInt) {
                case 0x0000:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        valueStr = (valueInt==0?"off":(valueInt==1?"on":"undefined"))
                        if (infoEnable) log.info "${device.displayName} Switch=$valueInt ($valueStr)"
                        sendEvent(name:"switch", value: valueStr)
						def currentLevel = device.currentValue("level")==null?0:device.currentValue("level").toInteger()
                        if (state.model?.substring(0,5)=="VZM35") { //FOR FAN ONLY 
							if (device.currentValue("smartFan")=="Enabled") {
								if      (currentLevel<=20)  newSpeed="low"
								else if (currentLevel<=40)  newSpeed="medium-low"
								else if (currentLevel<=60)  newSpeed="medium"
								else if (currentLevel<=80)  newSpeed="medium-high"
								else if (currentLevel<=100) newSpeed="high"
							} else {
								if      (currentLevel<=33)  newSpeed="low"
								else if (currentLevel<=66)  newSpeed="medium"
								else if (currentLevel<=100) newSpeed="high"
								}
                            if      (valueStr=="off")                        newSpeed="off"
                            else if (parameter158=="1" || parameter258=="1") newSpeed="high"
                            if (traceEnable||debugEnable) log.trace "${device.displayName} Speed=${newSpeed}"
                            sendEvent(name:"speed", value: "${newSpeed}")   
                        }
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
                    break
                case 0x4000:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        if (infoEnable) log.info "${device.displayName} Global Scene Control " + descMap
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap																									  
                    break
                case 0x4001:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        if (infoEnable) log.info "${device.displayName} On Time=${valueInt/10}s"
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap																									  
                    break
                case 0x4002:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        if (infoEnable) log.info "${device.displayName} Off Wait Time=${valueInt/10}s"
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap																									  
                    break
                case 0x4003:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        valueStr = (valueInt==0?"off":(valueInt==1?"on":(valueInt==2?"toggle":(valueInt==255?"Previous":"undefined"))))
                        if (infoEnable) log.info "${device.displayName} Power-On State=$valueInt ($valueStr)"
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap																									  
                    break
                default:
                    if (descMap.profileId=="0000" && descMap.command=="00" && descMap.direction=="00") {
                        if (traceEnable||debugEnable) log.info "${device.displayName} " + 
							fireBrick("MATCH DESCRIPTOR REQUEST Device:${descMap.data[2]}${descMap.data[1]} Profile:${descMap.data[4]}${descMap.data[3]} Cluster:${descMap.data[7]}${descMap.data[6]}")
                    } else if (attrInt==null && descMap.command=="0B" && descMap.direction=="01") {
                        if (settings?.parameter51) {    //not sure why the firmware sends these when there are no bindings
                            if (descMap.data[0]=="00" && infoEnable) log.info "${device.displayName} Bind Command Sent: Switch OFF"
                            if (descMap.data[0]=="01" && infoEnable) log.info "${device.displayName} Bind Command Sent: Switch ON"
                            if (descMap.data[0]=="02" && infoEnable) log.info "${device.displayName} Bind Command Sent: Toggle"
                        }
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Attribute:$attrInt ") //+ descMap
                    break
            }
            break
        case 0x0008:    //LEVEL CONTROL CLUSTER
            if (traceEnable||debugEnable) traceCluster(description)
            switch (attrInt) {
                case 0x0000:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        valueInt=Math.min(Math.max(valueInt.toInteger(),0),254)
                        def percentValue = convertByteToPercent(valueInt)
                        valueStr = percentValue.toString()+"%"
                        if (infoEnable) log.info "${device.displayName} Level=$valueInt ($valueStr)"
                        sendEvent(name:"level", value: percentValue, unit: "%")
		                def newSpeed =""
                        if (state.model?.substring(0,5)=="VZM35") { //FOR FAN ONLY
							if (device.currentValue("smartFan")=="Enabled") {
								if      (percentValue<=20)  newSpeed="low"
								else if (percentValue<=40)  newSpeed="medium-low"
								else if (percentValue<=60)  newSpeed="medium"
								else if (percentValue<=80)  newSpeed="medium-high"
								else if (percentValue<=100) newSpeed="high"
							} else {
								if      (percentValue<=33)  newSpeed="low"
								else if (percentValue<=66)  newSpeed="medium"
								else if (percentValue<=100) newSpeed="high"
								}
                            if (device.currentValue("switch")=="off")        newSpeed="off"
                            else if (parameter158=="1" || parameter258=="1") newSpeed="high"
                            if (infoEnable) log.info "${device.displayName} Speed=${newSpeed}"
                            sendEvent(name:"speed", value: "${newSpeed}")   
                        }
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
					break
                case 0x0001:
                    if(descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
						if (infoEnable) log.info "${device.displayName} Remaining Time=${valueInt/10}s"
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
                    break
                case 0x0002:
                    if(descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
						if (infoEnable) log.info "${device.displayName} Min Level=${valueInt}s"
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
                    break
                case 0x0003:
                    if(descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
						if (infoEnable) log.info "${device.displayName} Max Level=${valueInt/10}s"
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
                    break
				case 0x000F:
                    if(descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        if (infoEnable) log.info "${device.displayName} Level Control Options: 0x${zigbee.convertToHexString(valueInt,2)}"
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
                    break
                case 0x0010:
                    if(descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        if (infoEnable) log.info "${device.displayName} On/Off Transition=${valueInt/10}s"
                        state.parameter3value = valueInt
                        device.updateSetting("parameter3",[value:"${valueInt}",type:configParams["parameter003"].type?.toString()])
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
                    break
                case 0x0011:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        valueStr = (valueInt==255?"Previous":convertByteToPercent(valueInt).toString()+"%")
                        if (infoEnable) log.info "${device.displayName} Default On Level=$valueInt ($valueStr)"
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
                    break
                case 0x0012:
                    if(descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        if (infoEnable) log.info "${device.displayName} On Transition=${valueInt/10}s"
                        state.parameter3value = valueInt
                        state.parameter4value = valueInt
                        device.updateSetting("parameter3",[value:"${valueInt}",type:configParams["parameter003"].type?.toString()])
                        device.updateSetting("parameter4",[value:"${valueInt}",type:configParams["parameter004"].type?.toString()])
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
                    break
                case 0x0013:
                    if(descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        if (infoEnable) log.info "${device.displayName} Off Transition=${valueInt/10}s"
                        state.parameter7value = valueInt
                        state.parameter8value = valueInt
                        device.updateSetting("parameter7",[value:"${valueInt}",type:configParams["parameter007"].type?.toString()])
                        device.updateSetting("parameter8",[value:"${valueInt}",type:configParams["parameter008"].type?.toString()])
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
                    break
                case 0x4000:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        valueStr = (valueInt==255?"Previous":convertByteToPercent(valueInt).toString()+"%")
                        if (infoEnable) log.info "${device.displayName} Power-On Level=$valueInt ($valueStr)"
                        state.parameter15value = convertByteToPercent(valueInt)
                        device.updateSetting("parameter15",[value:"${convertByteToPercent(valueInt)}",type:configParams["parameter015"].type?.toString()])
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
					break
				default:
                    if (attrInt==null && descMap.command=="0B" && descMap.direction=="01") {
                        if (parameter51) {    //not sure why the firmware sends these when there are no bindings
                            if (descMap.data[0]=="00" && infoEnable) log.info "${device.displayName} Bind Command Sent: Move To Level"
                            if (descMap.data[0]=="01" && infoEnable) log.info "${device.displayName} Bind Command Sent: Move Up/Down"
                            if (descMap.data[0]=="02" && infoEnable) log.info "${device.displayName} Bind Command Sent: Step"
                            if (descMap.data[0]=="03" && infoEnable) log.info "${device.displayName} Bind Command Sent: Stop Level Change"
                            if (descMap.data[0]=="04" && infoEnable) log.info "${device.displayName} Bind Command Sent: Move To Level (with On/Off)"
                            if (descMap.data[0]=="05" && infoEnable) log.info "${device.displayName} Bind Command Sent: Move Up/Down (with On/Off)"
                            if (descMap.data[0]=="06" && infoEnable) log.info "${device.displayName} Bind Command Sent: Step (with On/Off)"
                        }
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Attribute:$attrInt ") //+ descMap
					break
			}
			break
        case 0x0013:    //ALEXA CLUSTER
            if (traceEnable||debugEnable) traceCluster(description)
            else if (infoEnable) log.info "${device.displayName} " + darkOrange("Alexa Heartbeat") //+ " $descMap"
            break
        case 0x0019:    //OTA CLUSTER
            if (traceEnable||debugEnable) traceCluster(description)
            else if (infoEnable) log.info "${device.displayName} " + darkOrange("OTA Cluster") //+ " $descMap"
            switch (attrInt) {
                case 0x0000:
                    if (infoEnable) log.info "${device.displayName} Server ID=$valueStr"
                    break
                case 0x0001:
                    if (infoEnable) log.info "${device.displayName} File Offset=$valueStr"
                    break
                case 0x0006:
                    if (infoEnable) log.info "${device.displayName} Upgrade Status=$valueStr"
                    break
                default:
                    log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Attribute:$attrInt ") //+ descMap
                    break
            }
            break
        case 0x0300:    //COLOR CONTROL CLUSTER
            if (infoEnable||traceEnable||debugEnable) traceCluster(description)
            break
        case 0x0702:    //SIMPLE METERING CLUSTER
            if (traceEnable||debugEnable) traceCluster(description)
            switch (attrInt) {
                case 0x0000:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        float energy
                        energy = valueInt/100
                        if (infoEnable) log.info "${device.displayName} Energy=${energy}kWh"
                        sendEvent(name:"energy",value:energy ,unit: "kWh")
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap		  													  
                    break
                default:
                    log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Attribute:$attrInt ") //+ descMap
                    break
            }
            break
        case 0x0B04:    //ELECTRICAL MEASUREMENT CLUSTER
            if (traceEnable||debugEnable) traceCluster(description)
            switch (attrInt) {
                case 0x0501:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        float amps
                        amps = valueInt/100
                        if (infoEnable) log.info "${device.displayName} Amps=${amps}A"
                        sendEvent(name:"amps",value:amps ,unit: "A")
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
                    break
                case 0x050b:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        valueInt = Integer.parseInt(descMap['value'],16)
                        float power 
                        power = valueInt/10
                        if (infoEnable) log.info "${device.displayName} Power=${power}W"
                        sendEvent(name: "power", value: power, unit: "W")
                    } else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap				  																																	  
                    break
                default:
                    log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Attribute:$attrInt ") //+ descMap
                    break
            }
            break
        case 0x8021:    //BINDING CLUSTER
            if (infoEnable||debugEnable) log.info  "${device.displayName} " + darkOrange("Binding Cluster ") + (debugEnable?descMap:(descMap.data==null?"":"data:${descMap.data}"))
            if (traceEnable) traceCluster(description)
            break
        case 0x8022:    //UNBINDING CLUSTER
            if (infoEnable||debugEnable) log.info "${device.displayName} "+darkOrange("UNBinding Cluster ") + (debugEnable?descMap:(descMap.data==null?"":"data:${descMap.data}"))
            if (traceEnable) traceCluster(description)
            break
        case 0x8032:    //ROUTING TABLE CLUSTER
            //if (infoEnable||debugEnable) log.info "${device.displayName} "+darkOrange("Routing_Table Cluster ") + (debugEnable?descMap:"")
            if (infoEnable||traceEnable||debugEnable) traceCluster(description)
            break
        case 0xfc31:    //PRIVATE CLUSTER
            if (traceEnable||debugEnable) traceCluster(description)
            if (attrInt == null) {
                if (descMap.isClusterSpecific) {
                    if (descMap.command == "00") ZigbeePrivateCommandEvent(descMap.data)        //Button Events
                    if (descMap.command == "04") bindInitiator()                                //Start Binding
                    if (descMap.command == "24") ZigbeePrivateLEDeffectStopEvent(descMap.data)  //LED start/stop events
                }
            } else if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                valueInt = Integer.parseInt(descMap['value'],16)
				def valueHex = intTo32bitUnsignedHex(valueInt)
				def infoDev = "${device.displayName} "
				def infoTxt = "Report: P${attrInt}=${valueInt}"
				def infoMsg = infoDev + infoTxt
                switch (attrInt){
                    case 0:
                        infoMsg += " (temporarily stored level during transitions)"
                        break
                    case 1:
                        infoMsg += " (Remote Dim Rate Up: " + (valueInt<127?((valueInt/10).toString()+"s)"):"default)")
                        break
                    case 2:
                        infoMsg += " (Local Dim Rate Up: " + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 1)")
                        break
                    case 3:
                        infoMsg += " (Remote Ramp Rate On: " + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 1)")
                        break
                    case 4:
                        infoMsg += " (Local Ramp Rate On: " + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 3)")
                        break
                    case 5:
                        infoMsg += " (Remote Dim Rate Down: " + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 1)")
                        break
                    case 6:
                        infoMsg += " (Local Dim Rate Down: " + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 2)")
                        break
                    case 7:
                        infoMsg += " (Remote Ramp Rate Off: " + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 3)")
                        break
                    case 8:
                        infoMsg += " (Local  Ramp Rate Off: " + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 4)")
                        break
                    case 9:     //Min Level
                        infoMsg += " (min level ${convertByteToPercent(valueInt)}%)"
                        break
                    case 10:    //Max Level
                        infoMsg += " (max level ${convertByteToPercent(valueInt)}%)" 
                        break
                    case 11:    //Invert Switch
                        infoMsg += valueInt==0?" (not Inverted)":" (Inverted)" 
                        break
                    case 12:    //Auto Off Timer
                        infoMsg += " (Auto Off Timer " + (valueInt==0?red("disabled"):"${valueInt}s") + ")"
                        break
                    case 13:    //Default Level (local)
                        infoMsg += " (default local level " + (valueInt==255?" = previous)":" ${convertByteToPercent(valueInt)}%)")
						sendEvent(name:"levelPreset", value:convertByteToPercent(valueInt))
                        break
                    case 14:    //Default Level (remote)
                        infoMsg += " (default remote level " + (valueInt==255?" = previous)":"${convertByteToPercent(valueInt)}%)")
                        break
                    case 15:    //Level After Power Restored
                        infoMsg += " (power-on level " + (valueInt==255?" = previous)":"${convertByteToPercent(valueInt)}%)")
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
                    case 23:    //Quick Start (in firmware on Fan, emulated in this driver for dimmer)
                        if  (state.model?.substring(0,5)!="VZM35") 
                            infoMsg += " (Quick Start " + (valueInt==0?red("disabled"):"${valueInt}%") + ")"
                        else 
                            infoMsg += " (Quick Start " + (valueInt==0?red("disabled"):"${valueInt} seconds") + ")"
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
                        infoMsg += " (Double-Tap Up level ${convertByteToPercent(valueInt)}%)"
                        break
                    case 56:  //Double-Tap DOWN level
                        infoMsg += " (Double-Tap Down level ${convertByteToPercent(valueInt)}%)"
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
						infoMsg += " [0x${valueHex}] " + (effectInt==255?"(Stop Effect)":"(Start Effect ${effectInt})")
						break
					case 100:	//LED Bar Scaling
                        infoMsg += " (LED Scaling " + (valueInt==0?blue("VZM-style"):red("LZW-style")) + ")"
						break
					case 123:	//Aux Switch Scenes
                        infoMsg += " (Aux Scenes " + (valueInt==0?red("disabled"):limeGreen("enabled")) + ")"
						sendEvent(name: "numberOfButtons", value: valueInt?28:14)
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
                if (infoEnable) log.info infoMsg //+ (traceEnable?" P:$attrInt V:$valueInt D:${getDefaultValue(attrInt)}":"")
                if ((attrInt==9)
				|| (attrInt==10)
				|| (attrInt==13)
				|| (attrInt==14)
				|| (attrInt==15)
				|| (attrInt==55)
				|| (attrInt==56)) {
					valueInt = convertByteToPercent(valueInt) //these attributes are stored as bytes but displayed as percentages
				}
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
				&& (!getReadOnlyParams().contains(attrInt))	//AND  not a read-only param
				&& (![22,52,158,258].contains(attrInt))) {	//AND  not a key parameter
					clearSetting(attrInt)					//THEN clear the setting (so only changed settings are displayed)
				} else {									//ELSE update local setting
					device.updateSetting("parameter${attrInt}",[value:"${valueInt}",type:configParams["parameter${attrInt.toString().padLeft(3,"0")}"]?.type?.toString()])
				}
				if (settings."parameter${attrInt}"!=null) {											//IF   device setting is not null
					state."parameter${attrInt}value" = settings."parameter${attrInt}"?.toInteger()	//THEN set state variable to device setting
				}
			}
			else log.warn "${device.displayName} " + fireBrick("${clusterName} Unknown Command:$descMap.command ") //+ descMap
            break
        default:
            log.warn "${device.displayName} " + fireBrick("Unknown Cluster($clusterName)  ") + description
			break
	}
    state.lastEventCluster =   clusterName
    state.lastEventTime =      nowFormatted()
    state.lastEventAttribute = attrInt
    state.lastEventValue =     descMap.value
    return
}

def presetLevel(value) {
    if (infoEnable) log.info "${device.displayName} presetLevel(${value})"
    state.lastCommandSent =                        "presetLevel(${value})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    Integer scaledValue = value==null?null:Math.min(Math.max(convertPercentToByte(value.toInteger()),1),255)  //ZigBee levels range from 0x01-0xfe with 00 and ff = 'use previous'
    cmds += setParameter(13, scaledValue)
    cmds += setParameter(14, scaledValue)
    if (debugEnable) log.debug "${device.displayName} preset $cmds"
    return cmds
}

def quickStart() {
    quickStartVariables()
	def startLevel = device.currentValue("level").toInteger()
	def cmds= []
	if (settings.parameter23?.toInteger()>0 ) {          //only do quickStart if enabled
		if (infoEnable) log.info "${device.displayName} quickStart(" + (state.model?.substring(0,5)!="VZM35"?"${settings.parameter23}%)":"${settings.parameter23}s)")
		if (state.model?.substring(0,5)!="VZM35") {      //IF not the Fan switch THEN emulate quickStart 
			if (startLevel<state.parameter23value) cmds += zigbee.setLevel(state.parameter23value?.toInteger(),0,34)  //only do quickStart if currentLevel is < Quick Start Level (34ms is two sinewave cycles)
			cmds += zigbee.setLevel(startLevel,0,longDelay) 
			if (debugEnable) log.debug "${device.displayName} quickStart $cmds"
		}
	}
    return cmds
}

def quickStartVariables() {
    if (state.model?.substring(0,5)!="VZM35") {  //IF not the Fan switch THEN set the quickStart variables manually
        settings.parameter23 =  (settings.parameter23!=null?settings.parameter23:getDefaultValue(23))
        state.parameter23value = Math.round((settings.parameter23?:0).toFloat())
        //state.parameter23level = Math.round((settings.parameter23level?:defaultQuickLevel).toFloat())
    }
}

def readDeviceAttributes() {
	if (traceEnable||debugEnable) log.trace "${device.displayName} readDeviceAttributes()"
	def cmds = []
//	cmds += zigbee.readAttribute(0x0000, 0x0000, [:], shortDelay)    //BASIC ZCL Version
//	cmds += zigbee.readAttribute(0x0000, 0x0001, [:], shortDelay)    //BASIC Application Version
//	cmds += zigbee.readAttribute(0x0000, 0x0002, [:], shortDelay)    //BASIC Stack Version
//	cmds += zigbee.readAttribute(0x0000, 0x0003, [:], shortDelay)    //BASIC HW Version
//	cmds += zigbee.readAttribute(0x0000, 0x0004, [:], shortDelay)    //BASIC Manufacturer Name
	cmds += zigbee.readAttribute(0x0000, 0x0005, [:], shortDelay)    //BASIC Model
	cmds += zigbee.readAttribute(0x0000, 0x0006, [:], shortDelay)    //BASIC SW Date Code
//	cmds += zigbee.readAttribute(0x0000, 0x0007, [:], shortDelay)    //BASIC Power Source
//	cmds += zigbee.readAttribute(0x0000, 0x0008, [:], shortDelay)    //BASIC GenericDevice-Class
//	cmds += zigbee.readAttribute(0x0000, 0x0009, [:], shortDelay)    //BASIC GenericDevice-Type
//	cmds += zigbee.readAttribute(0x0000, 0x000A, [:], shortDelay)    //BASIC ProductCode
//	cmds += zigbee.readAttribute(0x0000, 0x000B, [:], shortDelay)    //BASIC ProductURL
//	cmds += zigbee.readAttribute(0x0000, 0x000C, [:], shortDelay)    //BASIC ManufacturerVersionDetails
//	cmds += zigbee.readAttribute(0x0000, 0x000D, [:], shortDelay)    //BASIC SerialNumber
//	cmds += zigbee.readAttribute(0x0000, 0x000E, [:], shortDelay)    //BASIC ProductLabel
//	cmds += zigbee.readAttribute(0x0000, 0x0010, [:], shortDelay)    //BASIC LocationDescription
//	cmds += zigbee.readAttribute(0x0000, 0x0011, [:], shortDelay)    //BASIC PhysicalEnvironment
//	cmds += zigbee.readAttribute(0x0000, 0x0012, [:], shortDelay)    //BASIC DeviceEnabled
//	cmds += zigbee.readAttribute(0x0000, 0x0013, [:], shortDelay)    //BASIC AlarmMask
//	cmds += zigbee.readAttribute(0x0000, 0x0014, [:], shortDelay)    //BASIC DisableLocalConfig
	cmds += zigbee.readAttribute(0x0000, 0x4000, [:], shortDelay)    //BASIC SWBuildID
//	cmds += zigbee.readAttribute(0x0003, 0x0000, [:], shortDelay)    //IDENTIFY Identify Time
//	cmds += zigbee.readAttribute(0x0004, 0x0000, [:], shortDelay)    //GROUP Name Support
//	cmds += zigbee.readAttribute(0x0005, 0x0000, [:], shortDelay)    //SCENES Scene Count
//	cmds += zigbee.readAttribute(0x0005, 0x0001, [:], shortDelay)    //SCENES Current Scene
//	cmds += zigbee.readAttribute(0x0005, 0x0002, [:], shortDelay)    //SCENES Current Group
//	cmds += zigbee.readAttribute(0x0005, 0x0003, [:], shortDelay)    //SCENES Scene Valid
//	cmds += zigbee.readAttribute(0x0005, 0x0004, [:], shortDelay)    //SCENES Name Support
//	cmds += zigbee.readAttribute(0x0005, 0x0005, [:], shortDelay)    //SCENES LastConfiguredBy
	cmds += zigbee.readAttribute(0x0006, 0x0000, [:], shortDelay)    //ON_OFF Current OnOff state
//	cmds += zigbee.readAttribute(0x0006, 0x4000, [:], shortDelay)    //ON_OFF GlobalSceneControl
//	cmds += zigbee.readAttribute(0x0006, 0x4001, [:], shortDelay)    //ON_OFF OnTime
//	cmds += zigbee.readAttribute(0x0006, 0x4002, [:], shortDelay)    //ON_OFF OffWaitTime
	cmds += zigbee.readAttribute(0x0006, 0x4003, [:], shortDelay)    //ON_OFF Startup OnOff state
	cmds += zigbee.readAttribute(0x0008, 0x0000, [:], shortDelay)    //LEVEL_CONTROL CurrentLevel
//	cmds += zigbee.readAttribute(0x0008, 0x0001, [:], shortDelay)    //LEVEL_CONTROL RemainingTime
//	cmds += zigbee.readAttribute(0x0008, 0x0002, [:], shortDelay)    //LEVEL_CONTROL MinLevel
//	cmds += zigbee.readAttribute(0x0008, 0x0003, [:], shortDelay)    //LEVEL_CONTROL MaxLevel
//	cmds += zigbee.readAttribute(0x0008, 0x0004, [:], shortDelay)    //LEVEL_CONTROL CurrentFrequency
//	cmds += zigbee.readAttribute(0x0008, 0x0005, [:], shortDelay)    //LEVEL_CONTROL MinFrequency
//	cmds += zigbee.readAttribute(0x0008, 0x0006, [:], shortDelay)    //LEVEL_CONTROL MaxFrequency
//	cmds += zigbee.readAttribute(0x0008, 0x000F, [:], shortDelay)    //LEVEL_CONTROL Options
//	cmds += zigbee.readAttribute(0x0008, 0x0010, [:], shortDelay)    //LEVEL_CONTROL OnOff Transition Time
//	cmds += zigbee.readAttribute(0x0008, 0x0011, [:], shortDelay)    //LEVEL_CONTROL OnLevel
//  cmds += zigbee.readAttribute(0x0008, 0x0012, [:], shortDelay)    //LEVEL_CONTROL OnTransitionTime
//  cmds += zigbee.readAttribute(0x0008, 0x0013, [:], shortDelay)    //LEVEL_CONTROL OffTransitionTime
//  cmds += zigbee.readAttribute(0x0008, 0x0014, [:], shortDelay)    //LEVEL_CONTROL DefaultMoveRate
//  cmds += zigbee.readAttribute(0x0008, 0x4000, [:], shortDelay)    //LEVEL_CONTROL StartUpCurrentLevel
//  cmds += zigbee.readAttribute(0x0019, 0x0000, [:], shortDelay)    //OTA Upgrade Server ID
//  cmds += zigbee.readAttribute(0x0019, 0x0001, [:], shortDelay)    //OTA File Offset
//  cmds += zigbee.readAttribute(0x0019, 0x0002, [:], shortDelay)    //OTA CurrentFileVersion
//  cmds += zigbee.readAttribute(0x0019, 0x0003, [:], shortDelay)    //OTA CurrentZigBeeStackVersion
//  cmds += zigbee.readAttribute(0x0019, 0x0004, [:], shortDelay)    //OTA DownloadedFileVersion
//  cmds += zigbee.readAttribute(0x0019, 0x0005, [:], shortDelay)    //OTA DownloadedZigBeeStackVersion
//  cmds += zigbee.readAttribute(0x0019, 0x0006, [:], shortDelay)    //OTA Image Upgrade Status
//  cmds += zigbee.readAttribute(0x0019, 0x0007, [:], shortDelay)    //OTA ManufacturerID 
//  cmds += zigbee.readAttribute(0x0019, 0x0008, [:], shortDelay)    //OTA Image TypeID 
//  cmds += zigbee.readAttribute(0x0019, 0x0009, [:], shortDelay)    //OTA MinimumBlockPeriod
//  cmds += zigbee.readAttribute(0x0019, 0x000A, [:], shortDelay)    //OTA Image Stamp
//  cmds += zigbee.readAttribute(0x0019, 0x000B, [:], shortDelay)    //OTA UpgradeActivationPolicy
//  cmds += zigbee.readAttribute(0x0019, 0x000C, [:], shortDelay)    //OTA UpgradeTimeoutPolicy 
    if (state.model?.substring(0,5)!="VZM35")	//Fan does not support power/energy reports
      cmds += zigbee.readAttribute(0x0702, 0x0000, [:], shortDelay)  //SIMPLE_METERING Energy Report
//  cmds += zigbee.readAttribute(0x0702, 0x0200, [:], shortDelay)    //SIMPLE_METERING Status
//  cmds += zigbee.readAttribute(0x0702, 0x0300, [:], shortDelay)    //SIMPLE_METERING Units
//  cmds += zigbee.readAttribute(0x0702, 0x0301, [:], shortDelay)    //SIMPLE_METERING AC Multiplier
//  cmds += zigbee.readAttribute(0x0702, 0x0302, [:], shortDelay)    //SIMPLE_METERING AC Divisor
//  cmds += zigbee.readAttribute(0x0702, 0x0303, [:], shortDelay)    //SIMPLE_METERING Formatting
//  cmds += zigbee.readAttribute(0x0702, 0x0306, [:], shortDelay)    //SIMPLE_METERING Metering Device Type
//  cmds += zigbee.readAttribute(0x0B04, 0x0501, [:], shortDelay)    //ELECTRICAL_MEASUREMENT Line Current
//  cmds += zigbee.readAttribute(0x0B04, 0x0502, [:], shortDelay)    //ELECTRICAL_MEASUREMENT Active Current
//  cmds += zigbee.readAttribute(0x0B04, 0x0503, [:], shortDelay)    //ELECTRICAL_MEASUREMENT Reactive Current
//  cmds += zigbee.readAttribute(0x0B04, 0x0505, [:], shortDelay)    //ELECTRICAL_MEASUREMENT RMS Voltage
//  cmds += zigbee.readAttribute(0x0B04, 0x0506, [:], shortDelay)    //ELECTRICAL_MEASUREMENT RMS Voltage min
//  cmds += zigbee.readAttribute(0x0B04, 0x0507, [:], shortDelay)    //ELECTRICAL_MEASUREMENT RMS Voltage max
//  cmds += zigbee.readAttribute(0x0B04, 0x0508, [:], shortDelay)    //ELECTRICAL_MEASUREMENT RMS Current
//  cmds += zigbee.readAttribute(0x0B04, 0x0509, [:], shortDelay)    //ELECTRICAL_MEASUREMENT RMS Current min
//  cmds += zigbee.readAttribute(0x0B04, 0x050A, [:], shortDelay)    //ELECTRICAL_MEASUREMENT RMS Current max
    if (state.model?.substring(0,5)!="VZM35")  //Fan does not support power/energy reports
      cmds += zigbee.readAttribute(0x0B04, 0x050B, [:], shortDelay)    //ELECTRICAL_MEASUREMENT Active Power
//  cmds += zigbee.readAttribute(0x0B04, 0x050C, [:], shortDelay)    //ELECTRICAL_MEASUREMENT Active Power min
//  cmds += zigbee.readAttribute(0x0B04, 0x050D, [:], shortDelay)    //ELECTRICAL_MEASUREMENT Active Power max
//  cmds += zigbee.readAttribute(0x0B04, 0x050E, [:], shortDelay)    //ELECTRICAL_MEASUREMENT Reactive Power
//  cmds += zigbee.readAttribute(0x0B04, 0x050F, [:], shortDelay)    //ELECTRICAL_MEASUREMENT Apparent Power
//  cmds += zigbee.readAttribute(0x0B04, 0x0510, [:], shortDelay)    //ELECTRICAL_MEASUREMENT Power Factor
//  cmds += zigbee.readAttribute(0x0B04, 0x0604, [:], shortDelay)    //ELECTRICAL_MEASUREMENT Power Multiplier
//  cmds += zigbee.readAttribute(0x0B04, 0x0605, [:], shortDelay)    //ELECTRICAL_MEASUREMENT Power Divisor
//  cmds += zigbee.readAttribute(0x8021, 0x0000, [:], shortDelay)    //Binding
//  cmds += zigbee.readAttribute(0x8022, 0x0000, [:], shortDelay)    //UnBinding
	return cmds
}

def refresh(option) {
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.displayName} refresh(${option})"
    state.lastCommandSent =                        "refresh(${option})"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = device.getDataValue('model')
    if (infoEnable||traceEnable||debugEnable) log.info "${device.displayName} Driver Date $state.driverDate"
    def cmds = []
	cmds += readDeviceAttributes()
	configParams.each {	//loop through all parameters
		int i = it.value.number.toInteger()
		if (i==23 && (state.model?.substring(0,5)!="VZM35")) quickStartVariables()  //quickStart is implemented in firmware for the fan, emulated in this driver for 2-in-1 Dimmer

		switch (option) {
			case "":									//option is blank or null 
				if (([22,52,158,258].contains(i))		//refresh primary settings
				|| (getReadOnlyParams().contains(i))	//refresh read-only params
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
	return cmds
}

def remoteControl(option) {
    if (infoEnable) log.info "${device.displayName} remoteControl($option)"
    state.lastCommandSent =                        "remoteControl($option)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zigbee.command(0xfc31,0x10,["mfgCode":"0x122F"],shortDelay,"${option=="Disabled"?"01":"00"}")
    if (debugEnable) log.debug "${device.displayName} remoteControl $cmds"
	return cmds 
}

def resetEnergyMeter() {
    if (infoEnable) log.info "${device.displayName} resetEnergyMeter(" + device.currentValue("energy") + "kWh)"
    state.lastCommandSent =                        "resetEnergyMeter(" + device.currentValue("energy") + "kWh)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zigbee.command(0xfc31,0x02,["mfgCode":"0x122F"],shortDelay,"0")
    cmds += zigbee.readAttribute(CLUSTER_SIMPLE_METERING, 0x0000)
    if (debugEnable) log.debug "${device.displayName} resetEnergyMeter $cmds"
    return cmds 
}
        
def setAttribute(Integer cluster, Integer attrInt, Integer dataType, Integer value, Map additionalParams = [:], Integer delay=shortDelay) {
    if (cluster==0xfc31) additionalParams = ["mfgCode":"0x122F"]
    if (delay==null||delay==0) delay = shortDelay
    def infoMsg = "${device.displayName} Set " + clusterLookup(cluster)
    if (cluster==0xfc31) {
        infoMsg += " attribute ${attrInt} dataType 0x${zigbee.convertToHexString(dataType,2)} value "
        switch (attrInt) {
			case 9:     //Min Level
			case 10:    //Max Level
			case 13:    //Default Level (local)
			case 14:    //Default Level (remote)
			case 15:    //Level after power restored
			case 55:	//Double-Tap UP Level
			case 56:	//Double-Tap DOWN Level
                infoMsg += "${value} = ${convertByteToPercent(value)}% on 255 scale"
                break
            case 23:
                quickStartVariables()
                infoMsg = ""
                break
			case 60:	//LED1 color when on
			case 61:	//LED1 color when off
			case 65:	//LED2 color when on
			case 66:	//LED2 color when off
			case 70:	//LED3 color when on
			case 71:	//LED3 color when off
			case 75:	//LED4 color when on
			case 76:	//LED4 color when off
			case 80:	//LED5 color when on
			case 81:	//LED5 color when off
			case 85:	//LED6 color when on
			case 86:	//LED6 color when off
			case 90:	//LED7 color when on
			case 91:	//LED7 color when off
            case 95:	//LED bar color when on
            case 96:	//LED bar color when off
                infoMsg += "${value} = ${Math.round(value/255*360)}° on 360° scale"
                break
            default:
                infoMsg += "${value}"
                break 
        }
    } else {
        infoMsg += " attribute 0x${zigbee.convertToHexString(attrInt,4)} value ${value}"
    }
    if (traceEnable) log.trace infoMsg + (delay==shortDelay?"":" [delay ${delay}]")
    def cmds = zigbee.writeAttribute(cluster, attrInt, dataType, value, additionalParams, delay)
    if (debugEnable) log.debug "${device.displayName} setAttribute $cmds"
    return cmds
}

def getAttribute(Integer cluster, Integer attrInt=0, Map additionalParams = [:], Integer delay=shortDelay) {
    if (cluster==0xfc31) additionalParams = ["mfgCode":"0x122F"]
    if (delay==null||delay==0) delay = shortDelay
    if (debugEnable) log.debug  "${device.displayName} Get "+clusterLookup(cluster)+" attribute ${attrInt}"+(delay==shortDelay?"":" [delay ${delay}]")
    if (cluster==0xfc31 && attrInt==23 && state.model?.substring(0,5)!="VZM35") {  //if not Fan, get the quickStart values from state variables since dimmer does not store these
		if (infoEnable) log.info "${device.displayName} Report: P${attrInt}=${state?.parameter23value} (QuickStart " + (state.parameter23value?.toInteger()==0?red("disabled"):"${state.parameter23value?.toInteger()}" + state.model?.substring(0,5)!="VZM35"?"${settings.parameter23}%":"${settings.parameter23}s") + ")"
		if (infoEnable) log.info "${device.displayName} Report: P${attrInt} level=${state?.parameter23level} (QuickStart startup level)"
		if (settings.parameter23level?.toInteger()==defaultQuickLevel) device.removeSetting("parameter23level") 
    }
	def cmds = []
    cmds += zigbee.readAttribute(cluster, attrInt, additionalParams, delay)
	if (debugEnable) log.debug "${device.displayName} getAttribute $cmds"
    return cmds
}

def setLevel(value, duration=0xFFFF) {
	if (duration==null) duration=0xFFFF
    if (infoEnable) log.info "${device.displayName} setLevel($value" + (duration==0xFFFF?")":", ${duration}s)")
    state.lastCommandSent =                        "setLevel($value" + (duration==0xFFFF?")":", ${duration}s)")
    state.lastCommandTime = nowFormatted()
    if (duration!=0xFFFF) duration = duration.toInteger()*10  //firmware duration in 10ths
    def cmds = []
    cmds += zigbee.setLevel(value.toInteger(),duration,shortDelay)
    if (debugEnable) log.debug "${device.displayName} setLevel $cmds"
	return cmds
}

def setParameter(paramNum=0, value=null, size=null, delay=shortDelay) {
	paramNum = paramNum?.toInteger()
	value    = value?.toInteger()
	size     = size?.toInteger()
	if (size==null || size==" ") size = configParams["parameter${paramNum.toString().padLeft(3,'0')}"]?.size?:8
	if (traceEnable) log.trace "${device.displayName} setParameter($paramNum, $value, $size)"
	state.lastCommandSent =                          "setParameter($paramNum, $value, $size)"
	state.lastCommandTime = nowFormatted()
	def cmds = []
    size = calculateSize(size).toInteger()
    if (value!=null) cmds += setAttribute(0xfc31,paramNum,size,value)	//no value means we only want to GET the value
	if (paramNum==52 || paramNum==158 || paramNum==258) cmds += "delay $longDelay"	//allow extra time when changing modes
    cmds += getAttribute(0xfc31, paramNum, [:], delay)
    if (debugEnable) log.debug value!=null?"${device.displayName} setParameter $cmds":"${device.displayName} getParameter $cmds"
    return cmds
}

def getParameter(paramNum=0, delay=shortDelay) {
	paramNum = paramNum?.toInteger()
    if (traceEnable) log.trace "${device.displayName} getParameter($paramNum,$delay)"
    //state.lastCommandSent =                        "getParameter($paramNum,$delay)"
    //state.lastCommandTime = nowFormatted() //this is not a custom command.  Only use state variable for commands on the device details page
    def cmds = []
	if (paramNum<0) {	//special case, if negative then read all params from 0-max (for debugging)
		for(int i = 0;i<=(getParameterNumbers()+getReadOnlyParams()).max();i++) {
			cmds += getAttribute(0xfc31, i, [:], 100)	//override shortDelay with very short (100ms) delay
		}	
	} else {	//otherwise, just get the requested parameter
		cmds += getAttribute(0xfc31, paramNum, [:], delay)
	}
    if (debugEnable) log.debug "${device.displayName} getParameter $cmds"
    return cmds
}

def setPrivateCluster(attributeId, value, size=8) {	//for backward compatibility
	log.warn "${device.displayName} setPrivateCluster(${red(italic(bold('command is depreciated. Use setParameter instead')))})"
    return setParameter(attributeId, value, size.toInteger())
}

def setSpeed(value) {  // FOR FAN ONLY
    if (infoEnable) log.info "${device.displayName} setSpeed(${value})"
    state.lastCommandSent =                        "setSpeed(${value})"
    state.lastCommandTime = nowFormatted()
	def currentLevel = device.currentValue("level")==null?0:device.currentValue("level").toInteger()
	if (device.currentValue("switch")=="off") currentLevel = 0
	boolean smartMode = device.currentValue("smartFan")=="Enabled"
	def newLevel = 0
    def cmds = []
    switch (value) {
        case "off":
            cmds += zigbee.off(shortDelay) 
            break
        case "low": 
            cmds += zigbee.setLevel(smartMode?20:33) 
            break
        case "medium-low":             //placeholder since Hubitat natively supports 5-speed fans
            cmds += zigbee.setLevel(40) 
            break
        case "medium": 
            cmds += zigbee.setLevel(smartMode?60:66) 
            break
        case "medium-high":            //placeholder since Hubitat natively supports 5-speed fans
            cmds += zigbee.setLevel(80) 
            break
        case "high": 
            cmds += zigbee.setLevel(100) 
            break
        case "on":
            cmds += zigbee.on(shortDelay)
            break
		case "up":
			if      (currentLevel<=0 )  {newLevel=20}
			else if (currentLevel<=20)  {newLevel=smartMode?40:60}
			else if (currentLevel<=40)  {newLevel=60}
			else if (currentLevel<=60)  {newLevel=smartMode?80:100}
			else if (currentLevel<=100) {newLevel=100}
            cmds += zigbee.setLevel(newLevel)
			break
		case "down":
			if      (currentLevel>80) {newLevel=smartMode?80:60}
			else if (currentLevel>60) {newLevel=60}
			else if (currentLevel>40) {newLevel=smartMode?40:20}
			else if (currentLevel>20) {newLevel=20}
			else if (currentLevel>0)  {newLevel=currentLevel}
            cmds += zigbee.setLevel(newLevel)
			break
    }
    if (debugEnable) log.debug "${device.displayName} setSpeed $cmds"
    return cmds
}

def setZigbeeAttribute(cluster, attributeId, value, size) {
    if (traceEnable) log.trace value!=null?"${device.displayName} setZigbeeAttribute(${cluster}, ${attributeId}, ${value}, ${size})":"${device.displayName} getZigbeeAttribute(${cluster}, ${attributeId})"
    state.lastCommandSent =    value!=null?                      "setZigbeeAttribute(${cluster}, ${attributeId}, ${value}, ${size})":                      "getZigbeeAttribute(${cluster}, ${attributeId})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    Integer setCluster = cluster.toInteger()
    Integer attId = attributeId.toInteger()
    Integer attValue = (value?:0).toInteger()
    Integer attSize = calculateSize(size).toInteger()
    if (value!=null) cmds += setAttribute(setCluster,attId,attSize,attValue,[:],attId==258?longDelay:shortDelay)
    cmds += getAttribute(setCluster, attId)
    if (debugEnable) log.debug "${device.displayName} setZigbeeAttribute $cmds"
    return cmds
}

def startLevelChange(direction, duration=null) {
    def newLevel = direction=="up"?100:device.currentValue("switch")=="off"?0:1
	duration = duration!=null?duration:(calculateParameter(1)/10)
    if (infoEnable) log.info "${device.displayName} startLevelChange(${direction}" + (duration==null?")":", ${duration}s)")
    state.lastCommandSent =                        "startLevelChange(${direction}" + (duration==null?")":", ${duration}s)")
    state.lastCommandTime = nowFormatted()
	def cmds = []
    cmds += duration==null?zigbee.setLevel(newLevel):zigbee.setLevel(newLevel, duration.toInteger()*10)
    if (debugEnable) log.debug "${device.displayName} startLevelChange $cmds"
	return cmds
}

def stopLevelChange() {
    if (infoEnable) log.info "${device.displayName} stopLevelChange()" // at level " + device.currentValue("level")
    state.lastCommandSent =                        "stopLevelChange()"
    state.lastCommandTime = nowFormatted()
	def cmds = []
    cmds += ["he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} ${CLUSTER_LEVEL_CONTROL} ${COMMAND_STOP} {}","delay $shortDelay"]
    if (debugEnable) log.debug "${device.displayName} stopLevelChange $cmds"
    return cmds
}

def toggle() {	
    def toggleDirection = device.currentValue("switch")=="off"?"off->on":"on->off"
    if (infoEnable) log.info "${device.displayName} toggle(${toggleDirection})"
    state.lastCommandSent =                        "toggle(${toggleDirection})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zigbee.command(CLUSTER_ON_OFF, COMMAND_TOGGLE)
    //// if having trouble keeping multiple bulbs in sync then comment-out the toggle command
	//// and use the below code to emulate toggle with on/off commands
    //if (device.currentValue("switch")=="off") {
	//	if (settings.parameter23?.toInteger()>0) {
	//		cmds += quickStart()  //IF quickStart is enabled THEN quickStart
	//	} else {
	//		cmds += zigbee.on(shortDelay)
	//	}
	//} else {
	//	cmds += zigbee.off(shortDelay)
	//}
    if (debugEnable) log.debug "${device.displayName} toggle $cmds"
    return cmds
}

def updated(option) { // called when "Save Preferences" is requested
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.displayName} updated(${option})"
    state.lastCommandSent =                        "updated(${option})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    def nothingChanged = true
    int defaultValue
    int newValue
	configParams.each {	//loop through all parameters
		int i = it.value.number.toInteger()
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
				if ((getParameterNumbers().contains(i))		//IF   this is a valid parameter for this device mode
				&& (settings."parameter$i"!=null)			//AND  this is a non-default setting
				&& (!getReadOnlyParams().contains(i))) {	//AND  this is not a read-only parameter
					cmds += setParameter(i, newValue)		//THEN set the new value
					nothingChanged = false
				}
				break
			case "All":
			case "Default":
				if (option=="Default") newValue = defaultValue	//if user selected "Default" then set the new value to the default value
				if (((i!=158)&&(i!=258))					//IF   we are not changing Switch Mode
				&& (!getReadOnlyParams().contains(i))) {	//AND  this is not a read-only parameter
					cmds += setParameter(i, newValue)		//THEN Set the new value
					nothingChanged = false
				} else {									//ELSE this is a read-only parameter or Switch Mode parameter
					cmds += getParameter(i)					//so Get current value from device
				}
				break
			default: 
				if (traceEnable||debugEnable) log.error "${device.displayName} Unknown option 'updated($option)'"
				break
		}
        if ((i==23)&&(state.model?.substring(0,5)!="VZM35")) {  //IF not Fan switch THEN manually update the quickStart state variables since Dimmer does not store these
            quickStartVariables()
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
    return cmds
}

List updateFirmware() {
    if (infoEnable) log.info "${device.displayName} updateFirmware(switch's fwDate: ${state.fwDate}, switch's fwVersion: ${state.fwVersion})"
    state.lastCommandSent =                        "updateFirmware()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    //if (state?.lastUpdateFw && now() - state.lastUpdateFw < 2000) {
	//	state.remove("lastUpdateFw")
        cmds += zigbee.updateFirmware()
        if (debugEnable) log.debug "${device.displayName} updateFirmware $cmds"
    //} else {
    //    log.warn "Firmware in this channel may be \"beta\" quality. Please check https://community.inovelli.com/t/blue-series-2-1-firmware-changelog-vzm31-sn/12326 before proceeding. Double-click \"Update Firmware\" to proceed"
	//	state.lastUpdateFw = now()
	//	runIn(2,lastUpdateFwRemove)
    //}
    return cmds
}
//def lastUpdateFwRemove() {if (state?.lastUpdateFw) state.remove("lastUpdateFw")}

void ZigbeePrivateCommandEvent(data) {
    if (infoEnable) log.info "${device.displayName} Scene Button=${data[0]} ButtonAttributes=${data[1]}"
    Integer ButtonNumber = Integer.parseInt(data[0],16)
    Integer ButtonAttributes = Integer.parseInt(data[1],16)
    switch(zigbee.convertToHexString(ButtonNumber,2) + zigbee.convertToHexString(ButtonAttributes,2)) {
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
        case "0400":    //Aux Tap Down 1x
            buttonEvent(15, "held", "physical")
            break
        case "0403":    //Aux Tap Down 2x
            buttonEvent(16, "held", "physical")
            break
        case "0404":    //Aux Tap Down 3x
            buttonEvent(17, "held", "physical")
            break
        case "0405":    //Aux Tap Down 4x
            buttonEvent(18, "held", "physical")
            break
        case "0406":    //Aux Tap Down 5x
            buttonEvent(19, "held", "physical")
            break
        case "0402":    //Aux Hold Down
            buttonEvent(20, "held", "physical")
            break
        case "0401":    //Aux Release Down
            buttonEvent(21, "held", "physical")
            break
        case "0500":    //Aux Tap Up 1x
            buttonEvent(15, "pushed", "physical")
            break
        case "0503":    //Aux Tap Up 2x
            buttonEvent(16, "pushed", "physical")
            break
        case "0504":    //Aux Tap Up 3x
            buttonEvent(17, "pushed", "physical")
            break
        case "0505":    //Aux Tap Up 4x
            buttonEvent(18, "pushed", "physical")
            break
        case "0506":    //Aux Tap Up 5x
            buttonEvent(19, "pushed", "physical")
            break
        case "0502":    //Aux Hold Up
            buttonEvent(20, "pushed", "physical")
            break
        case "0501":    //Aux Release Up
            buttonEvent(21, "pushed", "physical")
            break
        case "0600":    //Aux Tap Config 1x
            buttonEvent(22, "pushed", "physical")
            break
        case "0603":    //Aux Tap Config 2x
            buttonEvent(23, "pushed", "physical")
            break
        case "0604":    //Aux Tap Config 3x
            buttonEvent(24, "pushed", "physical")
            break
        case "0605":    //Aux Tap Config 4x
            buttonEvent(25, "pushed", "physical")
            break
        case "0606":    //Aux Tap Config 5x
            buttonEvent(26, "pushed", "physical")
            break
        case "0602":    //Aux Hold Config
            buttonEvent(27, "pushed", "physical")
            break
        case "0601":    //Aux Release Config
            buttonEvent(28, "pushed", "physical")
            break
        default:       //undefined scene
            log.warn "${device.displayName} " + fireBrick("Undefined Scene=${data[0]} Attributes=${data[1]}")
            break
    }
}

void ZigbeePrivateLEDeffectStopEvent(data) {
    Integer ledNumber = Integer.parseInt(data[0],16)+1 //internal LED number is 0-based
    String  ledStatus = ledNumber==17?"Stop All":ledNumber==256?"User Cleared":"Stop LED${ledNumber}"
    if (infoEnable) log.info "${device.displayName} ledEffectStopEvent=${ledStatus}"
	switch(ledNumber){
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        case 17:  //Full LED bar effects
        case 256: //user double-pressed the config button to clear the notification
			sendEvent(name:"ledEffect", value: "${ledStatus}")
            break
        default:  
			log.warn "${device.displayName} " + fireBrick("Undefined LEDeffectStopEvent=${data[0]}")
            break
    }
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
        case 15:
        case 16:
        case 17:
        case 18:
        case 19:
            sendEvent(name:"lastButton", value: "${action=='pushed'?'Aux Tap '.padRight(button-6, '▲'):'Aux Tap '.padRight(button-6, '▼')}")
            break
        case 20:
            sendEvent(name:"lastButton", value: "${action=='pushed'?'Aux Hold ▲':'Aux Hold ▼'}")
            break
        case 21:
            sendEvent(name:"lastButton", value: "${action=='pushed'?'Aux Release ▲':'Aux Release ▼'}")
            break
        case 22:
        case 23:
        case 24:
        case 25:
        case 26:
            sendEvent(name:"lastButton", value: "Aux Tap ".padRight(button-13, "►"))
            break
        case 27:
            sendEvent(name:"lastButton", value: "Aux Hold ►")
            break
        case 28:
            sendEvent(name:"lastButton", value: "Aux Release ►")
            break
        default:       //undefined button event
            log.warn "${device.displayName} " + fireBrick("Undefined Button=$button Action=$action Type=$type")
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

@Field static Integer shortDelay = 333		//default delay to use for zigbee commands (in milliseconds)
@Field static Integer longDelay = 1000		//long delay to use for changing modes (in milliseconds)
@Field static Integer defaultQuickLevel=50	//default startup level for QuickStart emulation
@Field static Map configParams = [
    parameter001 : [
        number: 1,
        name: "Dimming Speed - Up (Remote)",
        description: "Sets the rate that the light dims up when controlled from the hub. A setting of 'instant' turns the light immediately on.<br>Default=2.5s",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s (default)","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"10.1s","102":"10.2s","103":"10.3s","104":"10.4s","105":"10.5s","106":"10.6s","107":"10.7s","108":"10.8s","109":"10.9s","110":"11.0s","111":"11.1s","112":"11.2s","113":"11.3s","114":"11.4s","115":"11.5s","116":"11.6s","117":"11.7s","118":"11.8s","119":"11.9s","120":"12.0s","121":"12.1s","122":"12.2s","123":"12.3s","124":"12.4s","125":"12.5s","126":"12.6s"],
        default: 25,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter002 : [
        number: 2,
        name: "Dimming Speed - Up (Local)",
        description: "Sets the rate that the light dims up when controlled at the switch. A setting of 'instant' turns the light immediately on.<br>Default=Sync with parameter1",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"10.1s","102":"10.2s","103":"10.3s","104":"10.4s","105":"10.5s","106":"10.6s","107":"10.7s","108":"10.8s","109":"10.9s","110":"11.0s","111":"11.1s","112":"11.2s","113":"11.3s","114":"11.4s","115":"11.5s","116":"11.6s","117":"11.7s","118":"11.8s","119":"11.9s","120":"12.0s","121":"12.1s","122":"12.2s","123":"12.3s","124":"12.4s","125":"12.5s","126":"12.6s","127":"Sync with parameter1"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter003 : [
        number: 3,
        name: "Ramp Rate - Off to On (Remote)",
        description: "Sets the rate that the light fades on when controlled from the hub. A setting of 'instant' turns the light immediately on.<br>Default=Sync with parameter1",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"10.1s","102":"10.2s","103":"10.3s","104":"10.4s","105":"10.5s","106":"10.6s","107":"10.7s","108":"10.8s","109":"10.9s","110":"11.0s","111":"11.1s","112":"11.2s","113":"11.3s","114":"11.4s","115":"11.5s","116":"11.6s","117":"11.7s","118":"11.8s","119":"11.9s","120":"12.0s","121":"12.1s","122":"12.2s","123":"12.3s","124":"12.4s","125":"12.5s","126":"12.6s","127":"Sync with parameter1"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter004 : [
        number: 4,
        name: "Ramp Rate - Off to On (Local)",
        description: "Sets the rate that the light fades on when controlled at the switch. A setting of 'instant' turns the light immediately on.<br>Default=Sync with parameter3",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"10.1s","102":"10.2s","103":"10.3s","104":"10.4s","105":"10.5s","106":"10.6s","107":"10.7s","108":"10.8s","109":"10.9s","110":"11.0s","111":"11.1s","112":"11.2s","113":"11.3s","114":"11.4s","115":"11.5s","116":"11.6s","117":"11.7s","118":"11.8s","119":"11.9s","120":"12.0s","121":"12.1s","122":"12.2s","123":"12.3s","124":"12.4s","125":"12.5s","126":"12.6s","127":"Sync with parameter3"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter005 : [
        number: 5,
        name: "Dimming Speed - Down (Remote)",
        description: "Sets the rate that the light dims down when controlled from the hub. A setting of 'instant' turns the light immediately off.<br>Default=Sync with parameter1",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"10.1s","102":"10.2s","103":"10.3s","104":"10.4s","105":"10.5s","106":"10.6s","107":"10.7s","108":"10.8s","109":"10.9s","110":"11.0s","111":"11.1s","112":"11.2s","113":"11.3s","114":"11.4s","115":"11.5s","116":"11.6s","117":"11.7s","118":"11.8s","119":"11.9s","120":"12.0s","121":"12.1s","122":"12.2s","123":"12.3s","124":"12.4s","125":"12.5s","126":"12.6s","127":"Sync with parameter1"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter006 : [
        number: 6,
        name: "Dimming Speed - Down (Local)",
        description: "Sets the rate that the light dims down when controlled at the switch. A setting of 'instant' turns the light immediately off.<br>Default=Sync with parameter2",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"10.1s","102":"10.2s","103":"10.3s","104":"10.4s","105":"10.5s","106":"10.6s","107":"10.7s","108":"10.8s","109":"10.9s","110":"11.0s","111":"11.1s","112":"11.2s","113":"11.3s","114":"11.4s","115":"11.5s","116":"11.6s","117":"11.7s","118":"11.8s","119":"11.9s","120":"12.0s","121":"12.1s","122":"12.2s","123":"12.3s","124":"12.4s","125":"12.5s","126":"12.6s","127":"Sync with parameter2"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter007 : [
        number: 7,
        name: "Ramp Rate - On to Off (Remote)",
        description: "Sets the rate that the light fades off when controlled from the hub. A setting of 'instant' turns the light immediately off.<br>Default=Sync with parameter3",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"10.1s","102":"10.2s","103":"10.3s","104":"10.4s","105":"10.5s","106":"10.6s","107":"10.7s","108":"10.8s","109":"10.9s","110":"11.0s","111":"11.1s","112":"11.2s","113":"11.3s","114":"11.4s","115":"11.5s","116":"11.6s","117":"11.7s","118":"11.8s","119":"11.9s","120":"12.0s","121":"12.1s","122":"12.2s","123":"12.3s","124":"12.4s","125":"12.5s","126":"12.6s","127":"Sync with parameter3"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter008 : [
        number: 8,
        name: "Ramp Rate - On to Off (Local)",
        description: "Sets the rate that the light fades off when controlled at the switch. A setting of 'instant' turns the light immediately off.<br>Default=Sync with parameter4",
        range: ["0":"instant","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1.0s","11":"1.1s","12":"1.2s","13":"1.3s","14":"1.4s","15":"1.5s","16":"1.6s","17":"1.7s","18":"1.8s","19":"1.9s","20":"2.0s","21":"2.1s","22":"2.2s","23":"2.3s","24":"2.4s","25":"2.5s","26":"2.6s","27":"2.7s","28":"2.8s","29":"2.9s","30":"3.0s","31":"3.1s","32":"3.2s","33":"3.3s","34":"3.4s","35":"3.5s","36":"3.6s","37":"3.7s","38":"3.8s","39":"3.9s","40":"4.0s","41":"4.1s","42":"4.2s","43":"4.3s","44":"4.4s","45":"4.5s","46":"4.6s","47":"4.7s","48":"4.8s","49":"4.9s","50":"5.0s","51":"5.1s","52":"5.2s","53":"5.3s","54":"5.4s","55":"5.5s","56":"5.6s","57":"5.7s","58":"5.8s","59":"5.9s","60":"6.0s","61":"6.1s","62":"6.2s","63":"6.3s","64":"6.4s","65":"6.5s","66":"6.6s","67":"6.7s","68":"6.8s","69":"6.9s","70":"7.0s","71":"7.1s","72":"7.2s","73":"7.3s","74":"7.4s","75":"7.5s","76":"7.6s","77":"7.7s","78":"7.8s","79":"7.9s","80":"8.0s","81":"8.1s","82":"8.2s","83":"8.3s","84":"8.4s","85":"8.5s","86":"8.6s","87":"8.7s","88":"8.8s","89":"8.9s","90":"9.0s","91":"9.1s","92":"9.2s","93":"9.3s","94":"9.4s","95":"9.5s","96":"9.6s","97":"9.7s","98":"9.8s","99":"9.9s","100":"10.0s","101":"10.1s","102":"10.2s","103":"10.3s","104":"10.4s","105":"10.5s","106":"10.6s","107":"10.7s","108":"10.8s","109":"10.9s","110":"11.0s","111":"11.1s","112":"11.2s","113":"11.3s","114":"11.4s","115":"11.5s","116":"11.6s","117":"11.7s","118":"11.8s","119":"11.9s","120":"12.0s","121":"12.1s","122":"12.2s","123":"12.3s","124":"12.4s","125":"12.5s","126":"12.6s","127":"Sync with parameter4"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter009 : [
        number: 9,
        name: "Minimum Level",
        description: "The minimum level that the light can be dimmed. Useful when the user has a light that does not turn on or flickers at a lower level.",
        range: "1..99",
        default: 1,
        size: 8,
        type: "number",
        value: null
        ],
    parameter010 : [
        number: 10,
        name: "Maximum Level",
        description: "The maximum level that the light can be dimmed. Useful when the user wants to limit the maximum brighness.",
        range: "2..100",
        default: 100,
        size: 8,
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
        size: 16,
        type: "number",
        value: null
        ],
    parameter013 : [
        number: 13,
        name: "Default Level (Local)",
        description: "Default level for the dimmer when turned on at the switch.<br>1-100=Set Level<br>101=Use previous level.",
        range: "1..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter014 : [
        number: 14,
        name: "Default Level (Remote)",
        description: "Default level for the dimmer when turned on from the hub.<br>1-100=Set Level<br>101=Use previous level.",
        range: "1..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter015 : [
        number: 15,
        name: "Level After Power Restored",
        description: "Level the dimmer will return to when power is restored after power failure (if Switch is in On/Off Mode any level 1-100 will convert to 100).<br>0=Off<br>1-100=Set Level<br>101=Use previous level.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter017 : [
        number: 17,
        name: "Load Level Indicator Timeout",
        description: "Shows the level that the load is at for x number of seconds after the load is adjusted and then returns to the Default LED state.",
        range: ["0":"Do not display Load Level","1":"1 Second","2":"2 Seconds","3":"3 Seconds","4":"4 Seconds","5":"5 Seconds","6":"6 Seconds","7":"7 Seconds","8":"8 Seconds","9":"9 Seconds","10":"10 Seconds","11":"Display Load Level with no timeout (default)"],
        default: 11,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter018 : [
        number: 18,
        name: "Active Power Reports",
        description: "Percent power level change that will result in a new power report being sent.<br>0 = Disabled",
        range: "0..100",
        default: 10,
        size: 8,
        type: "number",
        value: null
        ],
    parameter019 : [
        number: 19,
        name: "Periodic Power & Energy Reports",
        description: "Time period between consecutive power & energy reports being sent (in seconds). The timer is reset after each report is sent.",
        range: "0..32767",
        default: 3600,
        size: 16,
        type: "number",
        value: null
        ],
    parameter020 : [
        number: 20,
        name: "Active Energy Reports",
        description: "Energy level change that will result in a new energy report being sent.<br>0 = Disabled<br>1-32767 = 0.01kWh-327.67kWh.",
        range: "0..32767",
        default: 10,
        size: 16,
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
        description: "Set the Aux switch type (Smart Bulb Mode does not work in Dumb 3-Way Switch mode)",
        range: ["0":"No Aux (default)", "1":"Dumb 3-Way Switch", "2":"Smart Aux Switch", "3":"No Aux Full Wave (On/Off only)"],
        default: 0,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter023 : [ //implemented in firmware for the fan, emulated in this driver for 2-in-1 Dimmer
        number: 23,
        name: "Quick Start",
        description: "EXPERIMENTAL (hub commands only): Startup Level from OFF to ON (for LEDs that need higher level to turn on but can be dimmed lower) 0=Disabled",
        range: "0..100",
        default: 0,
        size: 8,
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
    parameter030 : [
        number: 30,
        name: "non-Neutral AUX medium gear learn value (read only)",
        description: "In the case of non-neutral, to make the AUX switch better compatible.",
        range: "0..255",
        default: 90,
        size: 8,
        type: "number",
        value: null
        ],
    parameter031 : [
        number: 31,
        name: "non-Neutral AUX low gear learn value (read only)",
        description: "In the case of non-neutral, to make the AUX switch better compatible.",
        range: "0..255",
        default: 110,
        size: 8,
        type: "number",
        value: null
        ],
    parameter032 : [
        number: 32,
        name: "Internal Temperature (read only)",
        description: "Internal temperature in Celsius",
        range: "0..100",
        default: 25,
        size: 8,
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
        size: 8,
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
        range: "1..100",
        default: 100,
        size: 8,
        type: "number",
        value: null
        ],
    parameter056 : [
        number: 56,
        name: "Brightness level for Double-Tap DOWN",
        description: "Set this level on double-tap DOWN (if enabled by P54)",
        range: "0..100",
        default: 1,
        size: 8,
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
        size: 8,
        type: "enum",
        value: null
        ],
    parameter061 : [
        number: 61,
        name: "LED1 Color (when Off)",
        description: "Set the color of LED1 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter062 : [
        number: 62,
        name: "LED1 Intensity (when On)",
        description: "Set the intensity of LED1 when the load is on.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter063 : [
        number: 63,
        name: "LED1 Intensity (when Off)",
        description: "Set the intensity of LED1 when the load is off.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter064 : [
        number: 64,
        name: "LED1 Notification",
        description: "4-byte encoded LED1 Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter065 : [
        number: 65,
        name: "LED2 Color (when On)",
        description: "Set the color of LED2 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter066 : [
        number: 66,
        name: "LED2 Color (when Off)",
        description: "Set the color of LED2 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter067 : [
        number: 67,
        name: "LED2 Intensity (when On)",
        description: "Set the intensity of LED2 when the load is on.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter068 : [
        number: 68,
        name: "LED2 Intensity (when Off)",
        description: "Set the intensity of LED2 when the load is off.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter069 : [
        number: 69,
        name: "LED2 Notification",
        description: "4-byte encoded LED2 Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter070 : [
        number: 70,
        name: "LED3 Color (when On)",
        description: "Set the color of LED3 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter071 : [
        number: 71,
        name: "LED3 Color (when Off)",
        description: "Set the color of LED3 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter072 : [
        number: 72,
        name: "LED3 Intensity (when On)",
        description: "Set the intensity of LED3 when the load is on.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter073 : [
        number: 73,
        name: "LED3 Intensity (when Off)",
        description: "Set the intensity of LED3 when the load is off.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter074 : [
        number: 74,
        name: "LED3 Notification",
        description: "4-byte encoded LED3 Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter075 : [
        number: 75,
        name: "LED4 Color (when On)",
        description: "Set the color of LED4 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter076 : [
        number: 76,
        name: "LED4 Color (when Off)",
        description: "Set the color of LED4 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter077 : [
        number: 77,
        name: "LED4 Intensity (when On)",
        description: "Set the intensity of LED4 when the load is on.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter078 : [
        number: 78,
        name: "LED4 Intensity (when Off)",
        description: "Set the intensity of LED4 when the load is off.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter079 : [
        number: 79,
        name: "LED4 Notification",
        description: "4-byte encoded LED4 Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter080 : [
        number: 80,
        name: "LED5 Color (when On)",
        description: "Set the color of LED5 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter081 : [
        number: 81,
        name: "LED5 Color (when Off)",
        description: "Set the color of LED5 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter082 : [
        number: 82,
        name: "LED5 Intensity (when On)",
        description: "Set the intensity of LED5 when the load is on.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter083 : [
        number: 83,
        name: "LED5 Intensity (when Off)",
        description: "Set the intensity of LED5 when the load is off.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter084 : [
        number: 84,
        name: "LED5 Notification",
        description: "4-byte encoded LED5 Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter085 : [
        number: 85,
        name: "LED6 Color (when On)",
        description: "Set the color of LED6 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter086 : [
        number: 86,
        name: "LED6 Color (when Off)",
        description: "Set the color of LED6 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter087 : [
        number: 87,
        name: "LED6 Intensity (when On)",
        description: "Set the intensity of LED6 when the load is on.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter088 : [
        number: 88,
        name: "LED6 Intensity (when Off)",
        description: "Set the intensity of LED6 when the load is off.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter089 : [
        number: 89,
        name: "LED6 Notification",
        description: "4-byte encoded LED6 Notification",
        range: "0..4294967295",
        default: 0,
        size: 4,
        type: "number",
        value: null
        ],
    parameter090 : [
        number: 90,
        name: "LED7 Color (when On)",
        description: "Set the color of LED7 when the load is on.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter091 : [
        number: 91,
        name: "LED7 Color (when Off)",
        description: "Set the color of LED7 when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 255,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter092 : [
        number: 92,
        name: "LED7 Intensity (when On)",
        description: "Set the intensity of LED7 when the load is on.",
        range: "0..101",
        default: 101,
        size: 8,
        type: "number",
        value: null
        ],
    parameter093 : [
        number: 93,
        name: "LED7 Intensity (when Off)",
        description: "Set the intensity of LED7 when the load is off.",
        range: "0..101",
        default: 101,
        size: 8,
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
        size: 8,
        type: "enum",
        value: null
        ],
    parameter096 : [
        number: 96,
        name: "LED Bar Color (when Off)",
        description: "Set the color of the LED Bar when the load is off.",
        range: ["0":"Red","14":"Orange","35":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","149":"Aqua","170":"Blue (default)","191":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 170,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter097 : [
        number: 97,
        name: "LED Bar Intensity (when On)",
        description: "Set the intensity of the LED Bar when the load is on.",
        range: "0..100",
        default: 33,
        size: 8,
        type: "number",
        value: null
        ],
    parameter098 : [
        number: 98,
        name: "LED Bar Intensity (when Off)",
        description: "Set the intensity of the LED Bar when the load is off.",
        range: "0..100",
        default: 3,
        size: 8,
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
    parameter256 : [
        number: 256,
        name: "Local Protection",
        description: "Ability to control switch from the wall.",
        range: ["0":"Local control enabled (default)", "1":"Local control disabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ] ,
    parameter257 : [
        number: 257,
        name: "Remote Protection (read only) <i>use Remote Control command to change.</i>",
        description: "Ability to control switch from the hub.",
        range: ["0":"Remote control enabled (default)", "1":"Remote control disabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter258 : [
        number: 258,
        name: "Switch Mode",
        description: "Dimmer or On/Off only",
        range: ["0":"Dimmer", "1":"On/Off (default)"],
        default: 1,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter259 : [
        number: 259,
        name: "LED Bar in On/Off Switch Mode",
        description: "When the device is in On/Off mode, use full LED bar or just one LED",
        range: ["0":"Full bar (default)", "1":"One LED"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter260 : [
        number: 260,
        name: "Firmware Update-In-Progess Bar",
        description: "Display firmware update progress on LED Bar",
        range: ["1":"Enabled (default)", "0":"Disabled"],
        default: 1,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter261 : [	//not valid for fan switch
        number: 261,
        name: "Relay Click",
        description: "Audible Click in On/Off mode",
        range: ["0":"Enabled (default)", "1":"Disabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter262 : [
        number: 262,
        name: "Double-Tap config to clear notification",
        description: "Double-Tap the Config button to clear notifications",
        range: ["0":"Enabled (default)", "1":"Disabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter263 : [	//not valid for dimmer
        number: 263,
        name: "LED bar display levels",
        description: "Levels of the LED bar in Smart Bulb Mode<br>0=full range",
        range: "0..9",
        default: 3,
        size: 8,
        type: "number",
        value: null
        ]
]

/*
 *  -----------------------------------------------------------------------------
 *  Everything below here are LIBRARY includes and should NOT be edited manually!
 *  -----------------------------------------------------------------------------
 */

private getCLUSTER_BASIC()           { 0x0000 }
private getCLUSTER_POWER()           { 0x0001 }
private getCLUSTER_IDENTIFY()        { 0x0003 }
private getCLUSTER_GROUP()           { 0x0004 }
private getCLUSTER_SCENES()          { 0x0005 }
private getCLUSTER_ON_OFF()          { 0x0006 }
private getCLUSTER_LEVEL_CONTROL()   { 0x0008 }
private getCLUSTER_WINDOW_POSITION() { 0x000d }
private getCLUSTER_WINDOW_COVERING() { 0x0102 }
private getCLUSTER_SIMPLE_METERING() { 0x0702 }
private getCLUSTER_ELECTRICAL_MEASUREMENT() { 0x0b04 }
private getCLUSTER_PRIVATE()         { 0xFC31 } 

private getCOMMAND_MOVE_LEVEL()       { 0x00 }
private getCOMMAND_MOVE()             { 0x01 }
private getCOMMAND_STEP()             { 0x02 }
private getCOMMAND_STOP()             { 0x03 }
private getCOMMAND_MOVE_LEVEL_ONOFF() { 0x04 }
private getCOMMAND_MOVE_ONOFF()       { 0x05 }
private getCOMMAND_STEP_ONOFF()       { 0x06 }

private getBASIC_ATTR_POWER_SOURCE()                 { 0x0007 }
private getPOWER_ATTR_BATTERY_PERCENTAGE_REMAINING() { 0x0021 }
private getPOSITION_ATTR_VALUE()                     { 0x0055 }

private getCOMMAND_OPEN()   { 0x00 }
private getCOMMAND_CLOSE()  { 0x01 }
private getCOMMAND_OFF()    { 0x00 }
private getCOMMAND_ON()     { 0x01 }
private getCOMMAND_TOGGLE() { 0x02 }
private getCOMMAND_PAUSE()  { 0x02 }
private getENCODING_SIZE()  { 0x39 }


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
