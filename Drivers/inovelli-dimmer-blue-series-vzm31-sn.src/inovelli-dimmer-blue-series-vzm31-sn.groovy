def getDriverDate() { return "2022-11-02" }  // **** DATE OF THE DEVICE DRIVER **** //
/**
* Inovelli VZM31-SN Blue Series Zigbee 2-in-1 Dimmer
*
* Author: Eric Maycock (erocm123)
* Contributor: Mark Amber (marka75160)
*
* Copyright 2022 Eric Maycock / Inovelli
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at:
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
* for the specific language governing permissions and limitations under the License.
* 
* 2021-12-15(EM) Initial release.
* 2021-12-16(EM) Adding configuration options and working on code
* 2021-12-20(EM) Adding additional parameters
* 2021-12-21(EM) Adding configuration options
* 2021-12-22(EM) Cleaning up and consolidating code
* 2021-12-23(EM) Adding min & max level parameters
* 2021-12-27(EM) Starting to standardize logging
* 2021-12-30(EM) Adding options for power type and switch type
* 2022-01-03(EM) Change wording and make settings dynamic based on which mode is chosen
                 Fix for parameter 9 not working correctly. More text changes to LED parameters
                 Fixing Set Level when you enter a value > 99
* 2022-01-04(MA) Fix 'div() on null object' error when Param9 is blank.  Fix attribute size is 'bits' not 'bytes'
* 2022-01-05(MA) Fix ranges for params 1-4.  These are slightly different for zigbee(blue) than they were for zwave(red)
* 2022-01-06(EM) Updating parse section of code. Requesting firmware version and date.
* 2022-01-11(EM) Adding code for firmware update.
* 2022-01-20(EM) Changes to make the driver compatible with firmware v5.
* 2022-01-20(EM) Some config parameter fixes. Energy measurement was changed to the simple metering cluster.
* 2022-01-20(EM) Fix scene reports not working since firmware v5. Need to "Save Preferences" to configure the reporting.
* 2022-01-21(MA) Fix typo's and cleanup some of the new parameter descriptions
* 2022-01-22(MA) More cleanup of the new parameter descriptions
* 2022-01-23(MA) Fix range on Active Energy Report (parameter20)
* 2022-01-24(MA) Add Custom Color override for the LED Indicator to allow any color from a standard hue color wheel
* 2022-01-25(MA) Parameter259 (On/Off LED mode) should only be visible when in On/Off Mode
* 2022-01-26(MA) Restore formatting on custom hue value - its needed to avoid div() errors on null values
*                Fix issue using dropdown color after clearing custom color
* 2022-01-27(MA) Fix setLevel so it uses separate Dim Up / Dim Down rates (parameter1 and parameter5) 
* 2022-02-01(MA) Lots of tweaks and enhancements to support v6 firmware update
* 2022-02-02(EM) Changing speed parameters to match functionality. Updated setLevel method to use firmware speed options (param 1-8)
* 2022-02-03(MA) Fix LEDeffect. Clean up some text/spelling/formatting.  More enhancements to logging
* 2022-02-04(MA) Fix startLevelChange to use dimming params in seconds.  Fix 0-99 vs 0-255 scaling on Default Levels (param 13-14)
* 2022-02-08(EM) Reverse change that broke LEDeffect
* 2022-02-09(MA) Enhance the 0-255 to 0-99 conversion formulas. Fix rounding down 1 to 0. ZigBee LEVEL range is 0x01-0xfe
*                Fix levelChange UP so it now turns light on if off, levelChange DOWN now stops at 1% not 0%.  This matches Red Series dimmers
*                Removed extra 'rattr' commands from level changing events since the firmware automatically sends them anyway.  Doing both was affecting performance
*                Add lastActivity, lastEvent, and lastRan features to match Red Series dimmers.
*                Add lastCommand as a feature enhancement over the Red Series.  I can easily remove if its not desired
* 2022-02-10(MA) Add bind() method to support the Zigbee Bindings app.
* 2022-02-11(MA) Add range checking for LEDeffect
* 2022-02-14(MA) Arrange procedures in alphabetical order to help match edits between HE and ST. No other code changes
* 2022-02-15(MA) Merged most (not all) changes into the ST driver.  No changes to this HE driver *** placeholder only                                                                                            
* 2022-02-16(MA) Fix button released event.  Add Config button Held and Released events.  Add digital button support for testing scenes (un-comment in the metadata section to enable) 
* 2022-02-17(MA) Clean up tab/space and other formatting to simplify diff comparisons between HE and ST groovy drivers
* 2022-02-19(MA) Patches for bugs still in v7 firmware. (mostly for parameters 13-14)
* 2022-02-20(MA) New 'Reset Parameters' command to reset ALL parameters and not just the ones it thinks have changed. 
*                This is needed when settings in the device don't match settings in the hub (typical after a factory reset and some firmware updates). 
*                It has the option to reset all parameters to their Current Settings on the hub or reset all parameters to their Default values.
* 2022-02-21(MA) Excluding Parmeter258 (Switch/Dimmer Output Mode) from Reset All command since it creates confusion with different parameter sets.
*                Extended delay time between bulk parameter changes to try and avoid lockups
* 2022-02-22(MA) Hotfix to remove spaces that broke multi-tap button events
* 2022-02-24(MA) Replace recently added 'Reset Parameters' command with new options for the existing Config command. Options to reset all settings to Default or force All current settings to device.
*                Add 'switchMode' attribute so the current Operating Mode (Dimmer or On/Off) is diplayed under Current States.
*                Adjusted the 0-99 ranges to 0-100 for consistency.
*                Update local settings variable whenever a device report is received and the device value is different than the local setting
*                Refresh command now includes 'get all attributes' so a refresh will ensure all state variables match whats in the device
*                Created new 'calculateSize' common method to use wherever a bitsize needs to be converted to a hex DataType
*                Add temporary hack to prevent parameter 22 from getting set to the same value as this causes freeze in v7 firmware.  Will remove hack when fixed in future firmware
* 2022-02-27(MA) Add default delay to Refresh commands so we're not waiting 2 seconds on every attribute
* 2022-02-28(EM) Adding individual LED notifications, modifying parameters for firmware v8, & fixing issue that was preventing "initialize()" commands from being sent. 
* 2022-03-01(MA) Rename with official model name in preparation for production release and standardize across other drivers (like the VZM35 Fan Switch)
*                Add Tertiary colors to LED Indicator options.  More tweaks for v8 firmware.  Hotfix: percent conversion fix and p22 hack removal
* 2022-03_02(MA) More detailed parsing of Zigbee Description Map reports
*                Display Level values as 0-100% instead of 0-255.  
*                Allow default values for LEDeffects - easier to enable/disable with just one or two clicks 
*                Move "Save Preferences" code from config() to updated(), move bindings from initialize() to configure()
*                Created Refresh-ALL and simplified Config-ALL & Config-DEFAULT commands
*                CLICKING ON CONFIGURE WILL RE-ESTABLISH ZIGBEE REPORT BINDINGS AFTER FACTORY RESET OF SWITCH
* 2022-03-03(MA) Hotfix: in some edge cases settings were not getting sent.
* 2022-03-04(MA) Parameter21 auto-senses Neutral and is read-only.  Add powerSource attribute to display what the switch detected instead of what the user selected.
* 2022-03-06(MA) change hubitat hexutils to zigbee hexutils for compatibility with ST driver (which doesn't have the hubitat libraries)
* 2022-03-07(MA) Even more detailed parsing.  Stub in some code for possible future "Preset Level" command
* 2022-03-08(MA) Hotfix: found another case where some settings were not getting sent to device, and remove extra level report from StopLevelChange
* 2022-03-09(MA) various tweaks and code optimizations to help merge with Fan driver.  Remove lastRan carryover from Red Series as its not used with the Blue Series
* 2022-03-10(MA) add null check before sending attribute
* 2022-03-12(MA) move switch config options to the top.  Add doubleTapped event for Config button. fix another rounding error
* 2022-03-16(MA) remove doubleTap and add full 5-tap capability for Config button.
* 2022-03-21(MA) replace empty ENUM values with " "  and fix basic on/off via rules and dashboard
* 2022-03-26(MA) updated for v9 firmware
* 2022-03-28(MA) added Alexa clusters to fingerprint ID, cleaned up a little code to match a little better with ST driver
* 2022-04-11(MA) sync up with changes to Zigbee Fan driver for consistency.  No functional changes to dimmer
* 2022-04-21(MA) change default LED intensity (params 97-98 ) to match PRD (33%/1%).  Also change switch mode (param 258) default to On/Off
* 2022-04-25(MA) updated for v10 (0x0A) firmware
* 2022-05-03(MA) some additional support for zigbee binding app and a couple small tweaks to text and logging
* 2022-05-04(MA) added logging for Alexa Cluster
* 2022-05-30(MA) merge with changes to zigbee fan v4
* 2022-05-31(MA) fix bug with dimRate in StartLevelChange
* 2022-06-04(MA) added parsing and logging for binding clusters - still under development
* 2022-06-06(MA) more updates to stay in sync with Fan v4 firmware
* 2022-06-07(MA) fix bug with null level on initial pairing
* 2022-06-08(MA) lots of cleanup and minor bug fixes; add some code to detect model and more merges with VZM35
* 2022-06-10(MA) merge with changes to 2022-06-10 VZM31-SN	
* 2022-06-18(MA) fix startLevelChange to accept a duration value; fix setPrivateCluster and setZigbeeAttribute; standardize all variables to camelCase
* 2022-06-19(MA) minor adjustment to percent conversions to be consistent with Fan v4 firmware
* 2022-06-20(MA) fix condition where user enters decimal string (e.g. "12.3") for a parameter
* 2022-06-21(MA) enhanced logic for Fan speed changes when using setLevel()
* 2022-06-23(MA) add weblink to Hue Color Wheel for Custom LED color; add color to some log entries
* 2022-06-27(MA) param 95-98 titles change color to match selection; Add common Led groupings for the Led Effect One notification
* 2022-07-03(MA) new Refresh User option to only refresh User-changed settings; enhanced support for custom LED bar colors
* 2022-07-08(MA) add param#261, add Aurora Effect, and other updates for firmware v1.11
* 2022-07-15(MA) cleanup and remove some unneeded debug code; add grey background to white text; remove unused report bindings
* 2022-07-22(MA) don't request ramp rate for fan; more cleanup for production
* 2022-07-27(MA) updates for v1.12 firmware; remove details from last.command to keep it simple (details are in log.info)
* 2022-07-29(MA) add Quick Start to parsing/reporting; add driverDate variable so it can be seen on the Device page in Hubitat
* 2022-07-30(MA) fix params 9-10 so they send the full 0-255 to the switch; don't log Config() calls if info logging is off
* 2022-08-02(MA) fix fan high speed not working with neutral; don't display min-level and max-level when in on/off mode; fix p9-p10 so they only get written if changed
* 2022-08-09(MA) added Trace logging; setCluster/setAttribute commands will Get current value if you leave Value blank
* 2022-08-14(MA) emulate QuickStart for dimmer(can be disabled); add presetLevel command to use in Rule Machine - can also be done with setPrivateCluster custom command
* 2022-11-02(EM) added warning for firmware update and requirement for double click. Enabled maximum level setting in on/off mode for "problem load" troubleshooting in 3-way dumb mode
*
* !!!!!!!!!! DON'T FORGET TO UPDATE THE DRIVER DATE AT THE TOP OF THIS PAGE !!!!!!!!!!
**/

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field
import hubitat.helper.ColorUtils
//import hubitat.helper.HexUtils
import java.security.MessageDigest

metadata {
    definition (name: "Inovelli Dimmer 2-in-1 Blue Series VZM31-SN", namespace: "InovelliUSA", author: "E.Maycock/M.Amber", filename: "Inovelli-zigbee-2-in-1-dimmer", importUrl:"https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-dimmer-blue-series-vzm31-sn.src/inovelli-dimmer-blue-series-vzm31-sn.groovy") { 
        
        capability "Actuator"
        capability "Bulb"
        capability "ChangeLevel"
        capability "Configuration"
        capability "EnergyMeter"
        //capability "FanControl"
        capability "HoldableButton"
        //capability "LevelPreset" //V-Mark firmware incorrectly uses Remote Default (p14) instead of Local Default (p13) for levelPreset. Users want to preset level of local buttons not remote commands
        //capability "Light"
        capability "PowerMeter"
        capability "PushableButton"
        capability "Refresh"
        capability "ReleasableButton"
        //capability "Sensor"
        //capability "SignalStrength"          //placeholder for future testing to see if this can be implemented
        capability "Switch"
        capability "SwitchLevel"

        attribute "auxType", "String"          //type of Aux switch
        attribute "lastButton", "String"       //last button event
        attribute "ledEffect", "String"        //LED effect that was requested
        attribute "numberOfBindings", "String" //(read only)
        attribute "smartBulb", "String"        //Smart Bulb mode enabled or disabled
        attribute "switchMode", "String"       //Dimmer or On/Off only

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
        
        command "bind",                ["string"]
        command "bindInitiator"
        command "bindTarget"

        command "configure",           [[name: "Option", type: "ENUM", description: "User=user changed settings only, All=configure all settings, Default=set all settings to default", constraints: [" ","User","All","Default"]]]

        command "initialize"
        
        command "ledEffectAll",        [[name: "Type*",type:"ENUM", description: "1=Solid, 2=Fast Blink, 3=Slow Blink, 4=Pulse, 5=Chase, 6=Open/Close, 7=Small-to-Big, 8=Aurora, 0=LEDs off, 255=Clear Notification", constraints: [1,2,3,4,5,6,7,8,0,255]],
                                        [name: "Color",type:"NUMBER", description: "0-254=Hue Color, 255=White, default=Red"], 
                                        [name: "Level", type:"NUMBER", description: "0-100=LED Intensity, default=100"], 
                                        [name: "Duration", type:"NUMBER", description: "1-60=seconds, 61-120=1-120 minutes, 121-254=1-134 hours, 255=Indefinitely, default=255"]]
        
        command "ledEffectOne",        [[name: "LEDnum*",type:"ENUM", description: "LED 1-7", constraints: ["7","6","5","4","3","2","1","123","567","12","345","67","147","1357","246"]],
                                        [name: "Type*",type:"ENUM", description: "1=Solid, 2=Fast Blink, 3=Slow Blink, 4=Pulse, 5=Chase, 0=LED off, 255=Clear Notification", constraints: [1,2,3,4,5,0,255]], 
                                        [name: "Color",type:"NUMBER", description: "0-254=Hue Color, 255=White, default=Red"], 
                                        [name: "Level", type:"NUMBER", description: "0-100=LED Intensity, default=100"], 
                                        [name: "Duration", type:"NUMBER", description: "1-60=seconds, 61-120=1-120 minutes, 121-254=1-134 hours, 255=Indefinitely, default=255"]]
        
        //uncomment the next line if you want a "presetLevel" command to use in Rule Manager.  Can also be done with setPrivateCluster(13, level, 8) instead
        //command "presetLevel",          [[name: "Level", type: "NUMBER", description: "Level to preset (1 to 101)"]]           
        
        command "refresh",             [[name: "Option", type: "ENUM", description: "blank=current states only, User=user changed settings only, All=refresh all settings",constraints: [" ","User","All"]]]

        command "resetEnergyMeter"

        //Dimmer does not support setSpeed commands      
        //command "setSpeed",            [[name: "FanSpeed*", type: "ENUM", constraints: ["off","low","medium","high"]]]

        command "setPrivateCluster",   [[name: "Attribute*",type:"NUMBER", description: "Attribute (in decimal) ex. 0x000F input 15"], 
                                        [name: "Value", type:"NUMBER", description: "Enter the value (in decimal) Leave blank to get current value without changing it"], 
                                        [name: "Size*", type:"ENUM", description: "8=uint8, 16=uint16, 1=bool",constraints: ["8", "16","1"]]]
     
        command "setZigbeeAttribute",  [[name: "Cluster*",type:"NUMBER", description: "Cluster (in decimal) ex. Inovelli Private Cluster=0xFC31 input 64561"], 
                                        [name: "Attribute*",type:"NUMBER", description: "Attribute (in decimal) ex. 0x000F input 15"], 
                                        [name: "Value", type:"NUMBER", description: "Enter the value (in decimal) Leave blank to get current value without changing it"], 
                                        [name: "Size*", type:"ENUM", description: "8=uint8, 16=uint16, 32=unint32, 1=bool",constraints: ["8", "16","32","1"]]]
        
        command "startLevelChange",    [[name: "Direction*",type:"ENUM", description: "Direction for level change", constraints: ["up","down"]], 
                                        [name: "Duration",type:"NUMBER", description: "Transition duration in seconds"]]
        
        command "toggle"
        
        command "updateFirmware"

        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,0008,0702,0B04,0B05,FC57,FC31", outClusters:"0003,0019",           model:"VZM31-SN", manufacturer:"Inovelli"
        fingerprint profileId:"0104", endpointId:"02", inClusters:"0000,0003",                                              outClusters:"0003,0019,0006,0008", model:"VZM31-SN", manufacturer:"Inovelli"
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,0008,0702,0B04,FC31",           outClusters:"0003,0019",           model:"VZM31-SN", manufacturer:"Inovelli"
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
                                title: "${i}. " + green(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name)), 
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
                            input "parameter${i}", "enum",
                                title: "${i}. " + green(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name)), 
                                description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description),
                                //defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                                options: configParams["parameter${i.toString().padLeft(3,"0")}"].range
                            break
                        case 22:    //Aux Type
                        case 52:    //Smart Bulb Mode
                        case 258:   //Switch Mode
                            input "parameter${i}", "enum",
                                title: "${i}. " + red(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name)), 
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
            
            if (i==23) {  //QuickStart is implemented in firmware for the fan, emulated in this driver for 2-in-1 Dimmer
                input "parameter${i}", "enum",
                    title: "${i}. " + darkSlateBlue(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name + " Duration")), 
                    description: italic(configParams["parameter${i.toString().padLeft(3,"0")}"].description),
                    //defaultValue: configParams["parameter${i.toString().padLeft(3,"0")}"].default,
                    options: configParams["parameter${i.toString().padLeft(3,"0")}"].range
                
                if (state.model?.substring(0,5)!="VZM35") {
                    input "parameter${i}level", "number",
                        title: darkSlateBlue(bold(configParams["parameter${i.toString().padLeft(3,"0")}"].name + " Level")), 
                        description: italic("Startup Level for LED bulbs to turn on before dropping to lower level.<br>Range=1..100 Default=100"),
                        defaultValue: "100",
                        range: "1..100"
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
                }
                else {
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
                        (hue((settings?."parameter${i}"!=null?settings?."parameter${i}":configParams["parameter${i.toString().padLeft(3,"0")}"].default)?.toInteger(), 
                            bold("Custom " + configParams["parameter${i.toString().padLeft(3,"0")}"].name))),
                    description: italic("Hue value to override " + configParams["parameter${i.toString().padLeft(3,"0")}"].name+".<br>Range: 0-360 chosen from a"+
                        underline(''' <a href="https://community-assets.home-assistant.io/original/3X/6/c/6c0d1ea7c96b382087b6a34dee6578ac4324edeb.png" target="_blank">'''+
                        fireBrick(" h")+crimson("u")+red("e")+orangeRed(" c")+darkOrange("o")+orange("l")+limeGreen("o")+green("r")+teal(" w")+blue("h")+steelBlue("e")+blueViolet("e")+magenta("l")+"</a>")),
                    required: false,
                    range: "0..360"
            }
        }
        input name: "infoEnable",          type: "bool",   title: bold("Enable Info Logging"),   defaultValue: true
        input name: "traceEnable",         type: "bool",   title: bold("Enable Trace Logging"),  defaultValue: false
        input name: "debugEnable",         type: "bool",   title: bold("Enable Debug Logging"),  defaultValue: false
        input name: "disableInfoLogging",  type: "number", title: bold("Disable Info Logging"),  description: italic("after this number of minutes<br>(0=Do not disable)"),  defaultValue: 20
        input name: "disableTraceLogging", type: "number", title: bold("Disable Trace Logging"), description: italic("after this number of minutes<br>(0=Do not disable)"),  defaultValue: 10
        input name: "disableDebugLogging", type: "number", title: bold("Disable Debug Logging"), description: italic("after this number of minutes<br>(0=Do not disable)"), defaultValue: 5
    }
}

def getParameterNumbers() {   //controls which options are available depending on whether the device is configured as a switch or a dimmer.
    if (parameter258 == "1")  //on/off mode
        return [258,22,52,10,11,12,17,18,19,20,21,50,51,95,96,97,98,256,257,259,260,261]
    else                      //dimmer mode
        return [258,22,52,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,17,18,19,20,21,23,50,51,53,95,96,97,98,256,257,260]
}

@Field static Map configParams = [
    parameter001 : [
        number: 1,
        name: "Dimming Speed - Up (Remote)",
        description: "This changes the speed that the light dims up when controlled from the hub. A setting of 'instant' turns the light immediately on. Increasing the value slows down the transition speed.<br>Default=2.5s",
        range: ["0":"instant", "5":"500ms", "6":"600ms", "7":"700ms", "8":"800ms", "9":"900ms", "10":"1.0s", "11":"1.1s", "12":"1.2s", "13":"1.3s", "14":"1.4s", "15":"1.5s", "16":"1.6s", "17":"1.7s", "18":"1.8s", "19":"1.9s", "20":"2.0s", "21":"2.1s", "22":"2.2s", "23":"2.3s", "24":"2.4s", "25":"2.5s (default)", "26":"2.6s", "27":"2.7s", "28":"2.8s", "29":"2.9s", "30":"3.0s", "31":"3.1s", "32":"3.2s", "33":"3.3s", "34":"3.4s", "35":"3.5s", "36":"3.6s", "37":"3.7s", "38":"3.8s", "39":"3.9s", "40":"4.0s", "41":"4.1s", "42":"4.2s", "43":"4.3s", "44":"4.4s", "45":"4.5s", "46":"4.6s", "47":"4.7s", "48":"4.8s", "49":"4.9s", "50":"5.0s", "51":"5.1s", "52":"5.2s", "53":"5.3s", "54":"5.4s", "55":"5.5s", "56":"5.6s", "57":"5.7s", "58":"5.8s", "59":"5.9s", "60":"6.0s", "61":"6.1s", "62":"6.2s", "63":"6.3s", "64":"6.4s", "65":"6.5s", "66":"6.6s", "67":"6.7s", "68":"6.8s", "69":"6.9s", "70":"7.0s", "71":"7.1s", "72":"7.2s", "73":"7.3s", "74":"7.4s", "75":"7.5s", "76":"7.6s", "77":"7.7s", "78":"7.8s", "79":"7.9s", "80":"8.0s", "81":"8.1s", "82":"8.2s", "83":"8.3s", "84":"8.4s", "85":"8.5s", "86":"8.6s", "87":"8.7s", "88":"8.8s", "89":"8.9s", "90":"9.0s", "91":"9.1s", "92":"9.2s", "93":"9.3s", "94":"9.4s", "95":"9.5s", "96":"9.6s", "97":"9.7s", "98":"9.8s", "99":"9.9s", "100":"10.0s", "101":"10.1s", "102":"10.2s", "103":"10.3s", "104":"10.4s", "105":"10.5s", "106":"10.6s", "107":"10.7s", "108":"10.8s", "109":"10.9s", "110":"11.0s", "111":"11.1s", "112":"11.2s", "113":"11.3s", "114":"11.4s", "115":"11.5s", "116":"11.6s", "117":"11.7s", "118":"11.8s", "119":"11.9s", "120":"12.0s", "121":"12.1s", "122":"12.2s", "123":"12.3s", "124":"12.4s", "125":"12.5s", "126":"12.6s"],
        default: 25,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter002 : [
        number: 2,
        name: "Dimming Speed - Up (Local)",
        description: "This changes the speed that the light dims up when controlled at the switch. A setting of 'instant' turns the light immediately on. Increasing the value slows down the transition speed.<br>Default=Sync with parameter1",
        range: ["0":"instant", "5":"500ms", "6":"600ms", "7":"700ms", "8":"800ms", "9":"900ms", "10":"1.0s", "11":"1.1s", "12":"1.2s", "13":"1.3s", "14":"1.4s", "15":"1.5s", "16":"1.6s", "17":"1.7s", "18":"1.8s", "19":"1.9s", "20":"2.0s", "21":"2.1s", "22":"2.2s", "23":"2.3s", "24":"2.4s", "25":"2.5s", "26":"2.6s", "27":"2.7s", "28":"2.8s", "29":"2.9s", "30":"3.0s", "31":"3.1s", "32":"3.2s", "33":"3.3s", "34":"3.4s", "35":"3.5s", "36":"3.6s", "37":"3.7s", "38":"3.8s", "39":"3.9s", "40":"4.0s", "41":"4.1s", "42":"4.2s", "43":"4.3s", "44":"4.4s", "45":"4.5s", "46":"4.6s", "47":"4.7s", "48":"4.8s", "49":"4.9s", "50":"5.0s", "51":"5.1s", "52":"5.2s", "53":"5.3s", "54":"5.4s", "55":"5.5s", "56":"5.6s", "57":"5.7s", "58":"5.8s", "59":"5.9s", "60":"6.0s", "61":"6.1s", "62":"6.2s", "63":"6.3s", "64":"6.4s", "65":"6.5s", "66":"6.6s", "67":"6.7s", "68":"6.8s", "69":"6.9s", "70":"7.0s", "71":"7.1s", "72":"7.2s", "73":"7.3s", "74":"7.4s", "75":"7.5s", "76":"7.6s", "77":"7.7s", "78":"7.8s", "79":"7.9s", "80":"8.0s", "81":"8.1s", "82":"8.2s", "83":"8.3s", "84":"8.4s", "85":"8.5s", "86":"8.6s", "87":"8.7s", "88":"8.8s", "89":"8.9s", "90":"9.0s", "91":"9.1s", "92":"9.2s", "93":"9.3s", "94":"9.4s", "95":"9.5s", "96":"9.6s", "97":"9.7s", "98":"9.8s", "99":"9.9s", "100":"10.0s", "101":"10.1s", "102":"10.2s", "103":"10.3s", "104":"10.4s", "105":"10.5s", "106":"10.6s", "107":"10.7s", "108":"10.8s", "109":"10.9s", "110":"11.0s", "111":"11.1s", "112":"11.2s", "113":"11.3s", "114":"11.4s", "115":"11.5s", "116":"11.6s", "117":"11.7s", "118":"11.8s", "119":"11.9s", "120":"12.0s", "121":"12.1s", "122":"12.2s", "123":"12.3s", "124":"12.4s", "125":"12.5s", "126":"12.6s", "127":"Sync with parameter1"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter003 : [
        number: 3,
        name: "Ramp Rate - Off to On (Remote)",
        description: "This changes the speed that the light turns on when controlled from the hub. A setting of 'instant' turns the light immediately on. Increasing the value slows down the transition speed.<br>Default=Sync with parameter1",
        range: ["0":"instant", "5":"500ms", "6":"600ms", "7":"700ms", "8":"800ms", "9":"900ms", "10":"1.0s", "11":"1.1s", "12":"1.2s", "13":"1.3s", "14":"1.4s", "15":"1.5s", "16":"1.6s", "17":"1.7s", "18":"1.8s", "19":"1.9s", "20":"2.0s", "21":"2.1s", "22":"2.2s", "23":"2.3s", "24":"2.4s", "25":"2.5s", "26":"2.6s", "27":"2.7s", "28":"2.8s", "29":"2.9s", "30":"3.0s", "31":"3.1s", "32":"3.2s", "33":"3.3s", "34":"3.4s", "35":"3.5s", "36":"3.6s", "37":"3.7s", "38":"3.8s", "39":"3.9s", "40":"4.0s", "41":"4.1s", "42":"4.2s", "43":"4.3s", "44":"4.4s", "45":"4.5s", "46":"4.6s", "47":"4.7s", "48":"4.8s", "49":"4.9s", "50":"5.0s", "51":"5.1s", "52":"5.2s", "53":"5.3s", "54":"5.4s", "55":"5.5s", "56":"5.6s", "57":"5.7s", "58":"5.8s", "59":"5.9s", "60":"6.0s", "61":"6.1s", "62":"6.2s", "63":"6.3s", "64":"6.4s", "65":"6.5s", "66":"6.6s", "67":"6.7s", "68":"6.8s", "69":"6.9s", "70":"7.0s", "71":"7.1s", "72":"7.2s", "73":"7.3s", "74":"7.4s", "75":"7.5s", "76":"7.6s", "77":"7.7s", "78":"7.8s", "79":"7.9s", "80":"8.0s", "81":"8.1s", "82":"8.2s", "83":"8.3s", "84":"8.4s", "85":"8.5s", "86":"8.6s", "87":"8.7s", "88":"8.8s", "89":"8.9s", "90":"9.0s", "91":"9.1s", "92":"9.2s", "93":"9.3s", "94":"9.4s", "95":"9.5s", "96":"9.6s", "97":"9.7s", "98":"9.8s", "99":"9.9s", "100":"10.0s", "101":"10.1s", "102":"10.2s", "103":"10.3s", "104":"10.4s", "105":"10.5s", "106":"10.6s", "107":"10.7s", "108":"10.8s", "109":"10.9s", "110":"11.0s", "111":"11.1s", "112":"11.2s", "113":"11.3s", "114":"11.4s", "115":"11.5s", "116":"11.6s", "117":"11.7s", "118":"11.8s", "119":"11.9s", "120":"12.0s", "121":"12.1s", "122":"12.2s", "123":"12.3s", "124":"12.4s", "125":"12.5s", "126":"12.6s", "127":"Sync with parameter1"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter004 : [
        number: 4,
        name: "Ramp Rate - Off to On (Local)",
        description: "This changes the speed that the light turns on when controlled at the switch. A setting of 'instant' turns the light immediately on. Increasing the value slows down the transition speed.<br>Default=Sync with parameter3",
        range: ["0":"instant", "5":"500ms", "6":"600ms", "7":"700ms", "8":"800ms", "9":"900ms", "10":"1.0s", "11":"1.1s", "12":"1.2s", "13":"1.3s", "14":"1.4s", "15":"1.5s", "16":"1.6s", "17":"1.7s", "18":"1.8s", "19":"1.9s", "20":"2.0s", "21":"2.1s", "22":"2.2s", "23":"2.3s", "24":"2.4s", "25":"2.5s", "26":"2.6s", "27":"2.7s", "28":"2.8s", "29":"2.9s", "30":"3.0s", "31":"3.1s", "32":"3.2s", "33":"3.3s", "34":"3.4s", "35":"3.5s", "36":"3.6s", "37":"3.7s", "38":"3.8s", "39":"3.9s", "40":"4.0s", "41":"4.1s", "42":"4.2s", "43":"4.3s", "44":"4.4s", "45":"4.5s", "46":"4.6s", "47":"4.7s", "48":"4.8s", "49":"4.9s", "50":"5.0s", "51":"5.1s", "52":"5.2s", "53":"5.3s", "54":"5.4s", "55":"5.5s", "56":"5.6s", "57":"5.7s", "58":"5.8s", "59":"5.9s", "60":"6.0s", "61":"6.1s", "62":"6.2s", "63":"6.3s", "64":"6.4s", "65":"6.5s", "66":"6.6s", "67":"6.7s", "68":"6.8s", "69":"6.9s", "70":"7.0s", "71":"7.1s", "72":"7.2s", "73":"7.3s", "74":"7.4s", "75":"7.5s", "76":"7.6s", "77":"7.7s", "78":"7.8s", "79":"7.9s", "80":"8.0s", "81":"8.1s", "82":"8.2s", "83":"8.3s", "84":"8.4s", "85":"8.5s", "86":"8.6s", "87":"8.7s", "88":"8.8s", "89":"8.9s", "90":"9.0s", "91":"9.1s", "92":"9.2s", "93":"9.3s", "94":"9.4s", "95":"9.5s", "96":"9.6s", "97":"9.7s", "98":"9.8s", "99":"9.9s", "100":"10.0s", "101":"10.1s", "102":"10.2s", "103":"10.3s", "104":"10.4s", "105":"10.5s", "106":"10.6s", "107":"10.7s", "108":"10.8s", "109":"10.9s", "110":"11.0s", "111":"11.1s", "112":"11.2s", "113":"11.3s", "114":"11.4s", "115":"11.5s", "116":"11.6s", "117":"11.7s", "118":"11.8s", "119":"11.9s", "120":"12.0s", "121":"12.1s", "122":"12.2s", "123":"12.3s", "124":"12.4s", "125":"12.5s", "126":"12.6s", "127":"Sync with parameter3"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter005 : [
        number: 5,
        name: "Dimming Speed - Down (Remote)",
        description: "This changes the speed that the light dims down when controlled from the hub. A setting of 'instant' turns the light immediately off. Increasing the value slows down the transition speed.<br>Default=Sync with parameter1",
        range: ["0":"instant", "5":"500ms", "6":"600ms", "7":"700ms", "8":"800ms", "9":"900ms", "10":"1.0s", "11":"1.1s", "12":"1.2s", "13":"1.3s", "14":"1.4s", "15":"1.5s", "16":"1.6s", "17":"1.7s", "18":"1.8s", "19":"1.9s", "20":"2.0s", "21":"2.1s", "22":"2.2s", "23":"2.3s", "24":"2.4s", "25":"2.5s", "26":"2.6s", "27":"2.7s", "28":"2.8s", "29":"2.9s", "30":"3.0s", "31":"3.1s", "32":"3.2s", "33":"3.3s", "34":"3.4s", "35":"3.5s", "36":"3.6s", "37":"3.7s", "38":"3.8s", "39":"3.9s", "40":"4.0s", "41":"4.1s", "42":"4.2s", "43":"4.3s", "44":"4.4s", "45":"4.5s", "46":"4.6s", "47":"4.7s", "48":"4.8s", "49":"4.9s", "50":"5.0s", "51":"5.1s", "52":"5.2s", "53":"5.3s", "54":"5.4s", "55":"5.5s", "56":"5.6s", "57":"5.7s", "58":"5.8s", "59":"5.9s", "60":"6.0s", "61":"6.1s", "62":"6.2s", "63":"6.3s", "64":"6.4s", "65":"6.5s", "66":"6.6s", "67":"6.7s", "68":"6.8s", "69":"6.9s", "70":"7.0s", "71":"7.1s", "72":"7.2s", "73":"7.3s", "74":"7.4s", "75":"7.5s", "76":"7.6s", "77":"7.7s", "78":"7.8s", "79":"7.9s", "80":"8.0s", "81":"8.1s", "82":"8.2s", "83":"8.3s", "84":"8.4s", "85":"8.5s", "86":"8.6s", "87":"8.7s", "88":"8.8s", "89":"8.9s", "90":"9.0s", "91":"9.1s", "92":"9.2s", "93":"9.3s", "94":"9.4s", "95":"9.5s", "96":"9.6s", "97":"9.7s", "98":"9.8s", "99":"9.9s", "100":"10.0s", "101":"10.1s", "102":"10.2s", "103":"10.3s", "104":"10.4s", "105":"10.5s", "106":"10.6s", "107":"10.7s", "108":"10.8s", "109":"10.9s", "110":"11.0s", "111":"11.1s", "112":"11.2s", "113":"11.3s", "114":"11.4s", "115":"11.5s", "116":"11.6s", "117":"11.7s", "118":"11.8s", "119":"11.9s", "120":"12.0s", "121":"12.1s", "122":"12.2s", "123":"12.3s", "124":"12.4s", "125":"12.5s", "126":"12.6s", "127":"Sync with parameter1"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter006 : [
        number: 6,
        name: "Dimming Speed - Down (Local)",
        description: "This changes the speed that the light dims down when controlled at the switch. A setting of 'instant' turns the light immediately off. Increasing the value slows down the transition speed.<br>Default=Sync with parameter2",
        range: ["0":"instant", "5":"500ms", "6":"600ms", "7":"700ms", "8":"800ms", "9":"900ms", "10":"1.0s", "11":"1.1s", "12":"1.2s", "13":"1.3s", "14":"1.4s", "15":"1.5s", "16":"1.6s", "17":"1.7s", "18":"1.8s", "19":"1.9s", "20":"2.0s", "21":"2.1s", "22":"2.2s", "23":"2.3s", "24":"2.4s", "25":"2.5s", "26":"2.6s", "27":"2.7s", "28":"2.8s", "29":"2.9s", "30":"3.0s", "31":"3.1s", "32":"3.2s", "33":"3.3s", "34":"3.4s", "35":"3.5s", "36":"3.6s", "37":"3.7s", "38":"3.8s", "39":"3.9s", "40":"4.0s", "41":"4.1s", "42":"4.2s", "43":"4.3s", "44":"4.4s", "45":"4.5s", "46":"4.6s", "47":"4.7s", "48":"4.8s", "49":"4.9s", "50":"5.0s", "51":"5.1s", "52":"5.2s", "53":"5.3s", "54":"5.4s", "55":"5.5s", "56":"5.6s", "57":"5.7s", "58":"5.8s", "59":"5.9s", "60":"6.0s", "61":"6.1s", "62":"6.2s", "63":"6.3s", "64":"6.4s", "65":"6.5s", "66":"6.6s", "67":"6.7s", "68":"6.8s", "69":"6.9s", "70":"7.0s", "71":"7.1s", "72":"7.2s", "73":"7.3s", "74":"7.4s", "75":"7.5s", "76":"7.6s", "77":"7.7s", "78":"7.8s", "79":"7.9s", "80":"8.0s", "81":"8.1s", "82":"8.2s", "83":"8.3s", "84":"8.4s", "85":"8.5s", "86":"8.6s", "87":"8.7s", "88":"8.8s", "89":"8.9s", "90":"9.0s", "91":"9.1s", "92":"9.2s", "93":"9.3s", "94":"9.4s", "95":"9.5s", "96":"9.6s", "97":"9.7s", "98":"9.8s", "99":"9.9s", "100":"10.0s", "101":"10.1s", "102":"10.2s", "103":"10.3s", "104":"10.4s", "105":"10.5s", "106":"10.6s", "107":"10.7s", "108":"10.8s", "109":"10.9s", "110":"11.0s", "111":"11.1s", "112":"11.2s", "113":"11.3s", "114":"11.4s", "115":"11.5s", "116":"11.6s", "117":"11.7s", "118":"11.8s", "119":"11.9s", "120":"12.0s", "121":"12.1s", "122":"12.2s", "123":"12.3s", "124":"12.4s", "125":"12.5s", "126":"12.6s", "127":"Sync with parameter2"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter007 : [
        number: 7,
        name: "Ramp Rate - On to Off (Remote)",
        description: "This changes the speed that the light turns off when controlled from the hub. A setting of 'instant' turns the light immediately off. Increasing the value slows down the transition speed.<br>Default=Sync with parameter3",
        range: ["0":"instant", "5":"500ms", "6":"600ms", "7":"700ms", "8":"800ms", "9":"900ms", "10":"1.0s", "11":"1.1s", "12":"1.2s", "13":"1.3s", "14":"1.4s", "15":"1.5s", "16":"1.6s", "17":"1.7s", "18":"1.8s", "19":"1.9s", "20":"2.0s", "21":"2.1s", "22":"2.2s", "23":"2.3s", "24":"2.4s", "25":"2.5s", "26":"2.6s", "27":"2.7s", "28":"2.8s", "29":"2.9s", "30":"3.0s", "31":"3.1s", "32":"3.2s", "33":"3.3s", "34":"3.4s", "35":"3.5s", "36":"3.6s", "37":"3.7s", "38":"3.8s", "39":"3.9s", "40":"4.0s", "41":"4.1s", "42":"4.2s", "43":"4.3s", "44":"4.4s", "45":"4.5s", "46":"4.6s", "47":"4.7s", "48":"4.8s", "49":"4.9s", "50":"5.0s", "51":"5.1s", "52":"5.2s", "53":"5.3s", "54":"5.4s", "55":"5.5s", "56":"5.6s", "57":"5.7s", "58":"5.8s", "59":"5.9s", "60":"6.0s", "61":"6.1s", "62":"6.2s", "63":"6.3s", "64":"6.4s", "65":"6.5s", "66":"6.6s", "67":"6.7s", "68":"6.8s", "69":"6.9s", "70":"7.0s", "71":"7.1s", "72":"7.2s", "73":"7.3s", "74":"7.4s", "75":"7.5s", "76":"7.6s", "77":"7.7s", "78":"7.8s", "79":"7.9s", "80":"8.0s", "81":"8.1s", "82":"8.2s", "83":"8.3s", "84":"8.4s", "85":"8.5s", "86":"8.6s", "87":"8.7s", "88":"8.8s", "89":"8.9s", "90":"9.0s", "91":"9.1s", "92":"9.2s", "93":"9.3s", "94":"9.4s", "95":"9.5s", "96":"9.6s", "97":"9.7s", "98":"9.8s", "99":"9.9s", "100":"10.0s", "101":"10.1s", "102":"10.2s", "103":"10.3s", "104":"10.4s", "105":"10.5s", "106":"10.6s", "107":"10.7s", "108":"10.8s", "109":"10.9s", "110":"11.0s", "111":"11.1s", "112":"11.2s", "113":"11.3s", "114":"11.4s", "115":"11.5s", "116":"11.6s", "117":"11.7s", "118":"11.8s", "119":"11.9s", "120":"12.0s", "121":"12.1s", "122":"12.2s", "123":"12.3s", "124":"12.4s", "125":"12.5s", "126":"12.6s", "127":"Sync with parameter3"],
        default: 127,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter008 : [
        number: 8,
        name: "Ramp Rate - On to Off (Local)",
        description: "This changes the speed that the light turns off when controlled at the switch. A setting of 'instant' turns the light immediately off. Increasing the value slows down the transition speed.<br>Default=Sync with parameter4",
        range: ["0":"instant", "5":"500ms", "6":"600ms", "7":"700ms", "8":"800ms", "9":"900ms", "10":"1.0s", "11":"1.1s", "12":"1.2s", "13":"1.3s", "14":"1.4s", "15":"1.5s", "16":"1.6s", "17":"1.7s", "18":"1.8s", "19":"1.9s", "20":"2.0s", "21":"2.1s", "22":"2.2s", "23":"2.3s", "24":"2.4s", "25":"2.5s", "26":"2.6s", "27":"2.7s", "28":"2.8s", "29":"2.9s", "30":"3.0s", "31":"3.1s", "32":"3.2s", "33":"3.3s", "34":"3.4s", "35":"3.5s", "36":"3.6s", "37":"3.7s", "38":"3.8s", "39":"3.9s", "40":"4.0s", "41":"4.1s", "42":"4.2s", "43":"4.3s", "44":"4.4s", "45":"4.5s", "46":"4.6s", "47":"4.7s", "48":"4.8s", "49":"4.9s", "50":"5.0s", "51":"5.1s", "52":"5.2s", "53":"5.3s", "54":"5.4s", "55":"5.5s", "56":"5.6s", "57":"5.7s", "58":"5.8s", "59":"5.9s", "60":"6.0s", "61":"6.1s", "62":"6.2s", "63":"6.3s", "64":"6.4s", "65":"6.5s", "66":"6.6s", "67":"6.7s", "68":"6.8s", "69":"6.9s", "70":"7.0s", "71":"7.1s", "72":"7.2s", "73":"7.3s", "74":"7.4s", "75":"7.5s", "76":"7.6s", "77":"7.7s", "78":"7.8s", "79":"7.9s", "80":"8.0s", "81":"8.1s", "82":"8.2s", "83":"8.3s", "84":"8.4s", "85":"8.5s", "86":"8.6s", "87":"8.7s", "88":"8.8s", "89":"8.9s", "90":"9.0s", "91":"9.1s", "92":"9.2s", "93":"9.3s", "94":"9.4s", "95":"9.5s", "96":"9.6s", "97":"9.7s", "98":"9.8s", "99":"9.9s", "100":"10.0s", "101":"10.1s", "102":"10.2s", "103":"10.3s", "104":"10.4s", "105":"10.5s", "106":"10.6s", "107":"10.7s", "108":"10.8s", "109":"10.9s", "110":"11.0s", "111":"11.1s", "112":"11.2s", "113":"11.3s", "114":"11.4s", "115":"11.5s", "116":"11.6s", "117":"11.7s", "118":"11.8s", "119":"11.9s", "120":"12.0s", "121":"12.1s", "122":"12.2s", "123":"12.3s", "124":"12.4s", "125":"12.5s", "126":"12.6s", "127":"Sync with parameter4"],
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
        description: "The level the switch will return to when power is restored after power failure.<br>0=Off<br>1-100=Set Level<br>101=Use previous level.",
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
        description: "Set the Aux switch type.",
        range: ["0":"None (default)", "1":"3-Way Dumb Switch", "2":"3-Way Aux Switch"],
        default: 0,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter023 : [ //implemented in firmware for the fan, emulated in this driver for 2-in-1 Dimmer
        number: 23,
        name: "Quick Start",
        description: "Duration of higher power when the light goes from OFF to ON (for LEDs that need higher power to turn on but can be dimmed lower) 0=Disabled",
        range: ["0":"disabled (default)","1":"100ms","2":"200ms","3":"300ms","4":"400ms","5":"500ms","6":"600ms","7":"700ms","8":"800ms","9":"900ms","10":"1000ms"],
        default: 0,
        size: 8,
        type: "enum",
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
        range: ["0":"Disabled (default)", "1":"Smart Bulb Mode"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter053 : [
        number: 53,
        name: "Double-Tap UP for full brightness",
        description: "Enable or Disable full brightness on double-tap up.",
        range: ["0":"Disabled (default)", "1":"Enabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter095 : [
        number: 95,
        name: "LED Indicator Color (when On)",
        description: "Set the color of the LED Indicator when the load is on.",
        range: ["0":"Red","7":"Orange","28":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","148":"Aqua","170":"Blue (default)","190":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 170,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter096 : [
        number: 96,
        name: "LED Indicator Color (when Off)",
        description: "Set the color of the LED Indicator when the load is off.",
        range: ["0":"Red","7":"Orange","28":"Lemon","64":"Lime","85":"Green","106":"Teal","127":"Cyan","148":"Aqua","170":"Blue (default)","190":"Violet","212":"Magenta","234":"Pink","255":"White"],
        default: 170,
        size: 8,
        type: "enum",
        value: null
        ],
    parameter097 : [
        number: 97,
        name: "LED Indicator Intensity (when On)",
        description: "Set the intensity of the LED Indicator when the load is on.",
        range: "0..100",
        default: 33,
        size: 8,
        type: "number",
        value: null
        ],
    parameter098 : [
        number: 98,
        name: "LED Indicator Intensity (when Off)",
        description: "Set the intensity of the LED Indicator when the load is off.",
        range: "0..100",
        default: 3,
        size: 8,
        type: "number",
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
        name: "Remote Protection",
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
        description: "Use as a Dimmer or an On/Off switch",
        range: ["0":"Dimmer", "1":"On/Off (default)"],
        default: 1,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter259 : [
        number: 259,
        name: "On/Off LED Mode",
        description: "When the device is in On/Off mode, use full LED bar or just one LED",
        range: ["0":"All (default)", "1":"One"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter260 : [
        number: 260,
        name: "Firmware Update-In-Progess Indicator",
        description: "Display firmware update progress on LED Indicator",
        range: ["1":"Enabled (default)", "0":"Disabled"],
        default: 1,
        size: 1,
        type: "enum",
        value: null
        ],
    parameter261 : [
        number: 261,
        name: "Relay Click",
        description: "Audible Click in On/Off mode",
        range: ["0":"Enabled (default)", "1":"Disabled"],
        default: 0,
        size: 1,
        type: "enum",
        value: null
        ]
]

@Field static Integer defaultDelay = 500    //default delay to use for zigbee commands (in milliseconds)

def bind(cmds=[] ) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: bind(${cmds})"
    state.lastCommand = "Bind"// (${cmds})"
    state.lastCommandTime = nowFormatted()
    return cmds
} 

def bindInitiator() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: BindInitiator()" 
    state.lastCommand = "BindInitiator" 
    state.lastCommandTime = nowFormatted()
    def cmds = zigbee.command(0xfc31,0x04,["mfgCode":"0x122F"],defaultDelay,"0") 
    if (traceEnable) log.trace "bindInit $cmds"
    return cmds
}

def bindTarget() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: BindTarget()"
    state.lastCommand = "BindTarget"
    state.lastCommandTime = nowFormatted()
    def cmds = zigbee.command(0x0003, 0x00, [:], defaultDelay, "20 00")
    if (traceEnable) log.trace "bindTarget $cmds"
    return cmds
}

def calculateDuration(direction) {
	if (parameter258=="1") duration=0  //IF switch mode is on/off THEN dim/ramp rates are 0
    else {                             //ElSE we are in dimmer/3-speed mode so calculate the dim/ramp rates
        switch (direction) {
            case "up":
                break
            case "down":
                break
            case "on":
                def rampRate = 0
                if ((parameter258=="0")&&(state.model?.substring(0,5)!="VZM35")) //if we are in dimmer mode and this is not the Fan Switch then use params 1-8 for rampRate
                    rampRate = (parameter3!=null?parameter3:(parameter1!=null?parameter1:configParams["parameter001"].default))?.toInteger()
                break
            case "off":
                def rampRate = 0
                if ((parameter258=="0")&&(state.model?.substring(0,5)!="VZM35"))  //if we are in dimmer mode and this is not the Fan Switch then use params 1-8 for rampRate
                    rampRate = (parameter7!=null?parameter7:(parameter3!=null?parameter3:(parameter1!=null?parameter1:configParams["parameter001"].default)))?.toInteger()
                break
        }
    }	
}

def calculateParameter(number) {
    //if (debugEnable) log.debug "${device.label?device.label:device.name}: calculateParameter(${number})"
    def value = (settings."parameter${number}"!=null?settings."parameter${number}":configParams["parameter${number.toString().padLeft(3,'0')}"].default).toInteger()
    switch (number){
        case 9:     //Min Level
        case 10:    //Max Level
        case 13:    //Default Level (local)
        case 14:    //Default Level (remote)
        case 15:    //Level after power restored
            value = convertPercentToByte(value)    //convert levels from percent to byte values before sending to the device
            break
        case 95:    //custom hue for LED Indicator (when On)
        case 96:    //custom hue for LED Indicator (when Off)
            //360-hue values need to be converted to byte values before sending to the device
            if (settings."parameter${number}custom" =~ /^([0-9]{1}|[0-9]{2}|[0-9]{3})$/) {
                value = Math.round(settings."parameter${number}custom"/360*255)
            }
            else {   //else custom hue is invalid format or not selected
                if(settings."parameter${number}custom"!=null) {
                    device.clearSetting("parameter${number}custom")
                    if (infoEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("Cleared invalid custom hue: ${settings."parameter${number}custom"}")
                }
            }
            break 
    }
    return value
}

def calculateSize(size=8) {
    //if (debugEnable) log.debug "${device.label?device.label:device.name}: calculateSize(${size})"
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

def clusterLookup(cluster) {
    return zigbee.clusterLookup(cluster)?:"PRIVATE_CLUSTER (${cluster})"
}

def configure(option) {    //THIS GETS CALLED AUTOMATICALLY WHEN NEW DEVICE IS DISCOVERED OR WHEN CONFIGURE BUTTON SELECTED ON DEVICE PAGE
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.label?device.label:device.name}: configure($option)" 
    state.lastCommand = "Configure " + option
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
    sendEvent(name: "numberOfButtons", value: 14, displayed:false)
    def cmds = []
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0000 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Basic Cluster
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0003 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Identify Cluster
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0003 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Identify Cluster ep2
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0004 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Group Cluster
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0005 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Scenes Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //On/Off Cluster
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //On/Off Cluster ep2
    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0008 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Level Control Cluster
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0008 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Level Control Cluster ep2
    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0019 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //OTA Upgrade Cluster
    if (state.model?.substring(0,5)!="VZM35") {  //Fan does not support power/energy reports
        cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0702 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Simple Metering - to get energy reports
        cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0B04 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Electrical Measurement - to get power reports
    }
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x8021 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Binding Cluster - to get binding reports
//  cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x8022 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //UnBinding Cluster - to get Unbinding reports
    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0xFC31 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Private Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0xFC31 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Private Cluster ep2
    //read back some key attributes
    cmds += ["he rattr ${device.deviceNetworkId} 0x01 0x0000 0x0004                    {}", "delay ${defaultDelay}"] //get manufacturer
    cmds += ["he rattr ${device.deviceNetworkId} 0x01 0x0000 0x0005                    {}", "delay ${defaultDelay}"] //get model
    cmds += ["he rattr ${device.deviceNetworkId} 0x01 0x0000 0x0006                    {}", "delay ${defaultDelay}"] //get firmware date
    cmds += ["he rattr ${device.deviceNetworkId} 0x01 0x0000 0x0007                    {}", "delay ${defaultDelay}"] //get power source
    cmds += ["he rattr ${device.deviceNetworkId} 0x01 0x0000 0x4000                    {}", "delay ${defaultDelay}"] //get firmware version
    cmds += ["he rattr ${device.deviceNetworkId} 0x01 0x0006 0x0000                    {}", "delay ${defaultDelay}"] //get on/off state
    cmds += ["he rattr ${device.deviceNetworkId} 0x01 0x0006 0x4003                    {}", "delay ${defaultDelay}"] //get Startup OnOff state
    cmds += ["he rattr ${device.deviceNetworkId} 0x01 0x0008 0x0000                    {}", "delay ${defaultDelay}"] //get current level
    if (state.model?.substring(0,5)!="VZM35")  //Fan does not support on_off transition time
        cmds += ["he rattr ${device.deviceNetworkId} 0x01 0x0008 0x0010                    {}", "delay ${defaultDelay}"] //get OnOff Transition Time
    cmds += ["he rattr ${device.deviceNetworkId} 0x01 0x0008 0x0011                    {}", "delay ${defaultDelay}"] //get Default Remote On Level
    cmds += ["he rattr ${device.deviceNetworkId} 0x01 0x0008 0x4000                    {}", "delay ${defaultDelay}"] //get Startup Level
    //if we didn't pick option "All" (so we don't read them twice) then preload the dimming/ramp rates so they are not null in calculations
    if (option!="All") for(int i = 1;i<=8;i++) cmds += getAttribute(0xfc31, i)
    //update local copies of the read-only parameters
    //cmds += getAttribute(0xfc31, 21)        //power source (neutral/non-neutral)
    cmds += getAttribute(0xfc31, 51)        //number of bindings
    if (option!="") cmds += updated(option) //if option was selected on Configure button, pass it on to update settings.
    return cmds
}

def convertByteToPercent(int value=0) {                  //Zigbee uses a 0-254 range where 254=100%.  255 is reserved for special meaning.
    //if (debugEnable) log.debug "${device.label?device.label:device.name}: convertByteToPercent(${value})"
    value = value==null?0:value                          //default to 0 if null
    value = Math.min(Math.max(value.toInteger(),0),255)  //make sure input byte value is in the 0-255 range
    value = value>=255?256:value                         //this ensures that byte values of 255 get rounded up to 101%
    value = Math.ceil(value/255*100)                     //convert to 0-100 where 254=100% and 255 becomes 101 for special meaning
    return value
}

def convertPercentToByte(int value=0) {                  //Zigbee uses a 0-254 range where 254=100%.  255 is reserved for special meaning.
    //if (debugEnable) log.debug "${device.label?device.label:device.name}: convertByteToPercent(${value})"
    value = value==null?0:value                          //default to 0 if null
    value = Math.min(Math.max(value.toInteger(),0),101)  //make sure input percent value is in the 0-101 range
    value = Math.floor(value/100*255)                    //convert to 0-255 where 100%=254 and 101 becomes 255 for special meaning
    value = value==255?254:value                         //this ensures that 100% rounds down to byte value 254
    value = value>255?255:value                          //this ensures that 101% rounds down to byte value 255
    return value
}

def cycleSpeed() {    // FOR FAN ONLY
    def cmds =[]
    if (parameter258=="1") cmds += toggle()    //if we are in on/off mode then do a toggle instead of cycle
    else {
        def currentLevel = device.currentValue("level")==null?0:device.currentValue("level").toInteger()
        if (device.currentValue("switch")=="off") currentLevel = 0
        def newLevel = 0
		def newSpeed =""
        if      (currentLevel <=0 ) { newLevel = 33;  newSpeed = "low" }
        else if (currentLevel <=33) { newLevel = 66;  newSpeed = "medium" }
        else if (currentLevel <=66) { newLevel = 100; newSpeed = "high" }
        else                        { newLevel = 0;   newSpeed = "off" }
        if (infoEnable) log.info "${device.label?device.label:device.name}: cycleSpeed(${device.currentValue("speed")}->${newSpeed})"
        state.lastCommand = "Cycle Speed ($newSpeed)"
        state.lastCommandTime = nowFormatted()
        cmds += zigbee.setLevel(newLevel)
        if (traceEnable) log.trace "cycleSpeed $cmds"
    }
    return cmds
}

def infoLogsOff() {
    log.warn "${device.label?device.label:device.name}: "+fireBrick("Disabling info logging after timeout")
    device.updateSetting("infoEnable",[value:"false",type:"bool"])
    device.updateSetting("disableInfoLogging",[value:"0",type:"number"])
}

def traceLogsOff() {
    log.warn "${device.label?device.label:device.name}: "+fireBrick("Disabling trace logging after timeout")
    device.updateSetting("traceEnable",[value:"false",type:"bool"])
    device.updateSetting("disableTraceLogging",[value:"0",type:"number"])
}

def debugLogsOff() {
    log.warn "${device.label?device.label:device.name}: "+fireBrick("Disabling debug logging after timeout")
    device.updateSetting("debugEnable",[value:"false",type:"bool"])
    device.updateSetting("disableDebugLogging",[value:"0",type:"number"])
}

def initialize() {    //CALLED DURING HUB BOOTUP IF "INITIALIZE" CAPABILITY IS DECLARED IN METADATA SECTION
    //Typically used for things that need refreshing or re-connecting at bootup (e.g. LAN integrations but not zigbee bindings)
    if (infoEnable) log.info "${device.label?device.label:device.name}: initialize()"
    state.clear()
    def cmds = []
    cmds = refresh()
    state.driverDate = getDriverDate()
    state.lastCommand = "Initialize"
    state.lastCommandTime = nowFormatted()
    return cmds
}

def installed() {    //THIS IS CALLED WHEN A DEVICE IS INSTALLED 
    log.info "${device.label?device.label:device.name}: installed()"
    state.lastCommand = "Installed"
    state.lastCommandTime = nowFormatted()
    initialize()
    //configure()     //I confirmed configure() gets called at Install time so this isn't needed here
    return
}

def intTo16bitUnsignedHex(value) {
    def hexStr = zigbee.convertToHexString(value.toInteger(),4)
    return new String(hexStr.substring(2,4) + hexStr.substring(0,2))
}

def intTo8bitUnsignedHex(value) {
    return zigbee.convertToHexString(value.toInteger(),2)
}

def ledEffectAll(effect=1, color=0, level=100, duration=255) {
    effect   = Math.min(Math.max((effect!=null?effect:1).toInteger(),0),255) 
    color    = Math.min(Math.max((color!=null?color:0).toInteger(),0),255) 
    level    = Math.min(Math.max((level!=null?level:100).toInteger(),0),100) 
    duration = Math.min(Math.max((duration!=null?duration:255).toInteger(),0),255) 
    if (infoEnable) log.info "${device.label?device.label:device.name}: ledEffectALL(${effect},${color},${level},${duration})"
    state.lastCommand = "Led Effect All"// (${effect},${color},${level},${duration})"
    state.lastCommandTime = nowFormatted()
    sendEvent(name:"ledEffect", value: (effect==255?"Stop":"Start")+" All", displayed:false)
    def cmds =[]
    Integer cmdEffect = effect.toInteger()
    Integer cmdColor = color.toInteger()
    Integer cmdLevel = level.toInteger()
    Integer cmdDuration = duration.toInteger()
    cmds += zigbee.command(0xfc31,0x01,["mfgCode":"0x122F"],defaultDelay,"${intTo8bitUnsignedHex(cmdEffect)} ${intTo8bitUnsignedHex(cmdColor)} ${intTo8bitUnsignedHex(cmdLevel)} ${intTo8bitUnsignedHex(cmdDuration)}")
    if (traceEnable) log.trace "ledEffectAll $cmds"
    return cmds
}
                                        
def ledEffectOne(lednum, effect=1, color=0, level=100, duration=255) { 
    effect   = Math.min(Math.max((effect!=null?effect:1).toInteger(),0),255) 
    color    = Math.min(Math.max((color!=null?color:0).toInteger(),0),255) 
    level    = Math.min(Math.max((level!=null?level:100).toInteger(),0),100) 
    duration = Math.min(Math.max((duration!=null?duration:255).toInteger(),0),255)
    if (infoEnable) log.info "${device.label?device.label:device.name}: ledEffectOne(${lednum},${effect},${color},${level},${duration})"
    state.lastCommand = "Led Effect Led${lednum}"// (${effect},${color},${level},${duration})"
    state.lastCommandTime = nowFormatted()
	sendEvent(name:"ledEffect", value: (effect==255?"Stop":"Start")+" LED${lednum}", displayed:false)
    def cmds = []
    lednum.each {
        it= Math.min(Math.max((it!=null?it:1).toInteger(),1),7)
        Integer cmdLedNum = it.toInteger()-1    //lednum is 0-based in firmware 
        Integer cmdEffect = effect.toInteger()
        Integer cmdColor = color.toInteger()
        Integer cmdLevel = level.toInteger()
        Integer cmdDuration = duration.toInteger()
        cmds = zigbee.command(0xfc31,0x03,["mfgCode":"0x122F"],defaultDelay,"${intTo8bitUnsignedHex(cmdLedNum)} ${intTo8bitUnsignedHex(cmdEffect)} ${intTo8bitUnsignedHex(cmdColor)} ${intTo8bitUnsignedHex(cmdLevel)} ${intTo8bitUnsignedHex(cmdDuration)}")
    }
    if (traceEnable) log.trace "ledEffectone $cmds"
    return cmds
}

def nowFormatted() {
    if(location.timeZone) return new Date().format("yyyy-MMM-dd h:mm:ss a", location.timeZone)
    else                  return new Date().format("yyyy MMM dd EEE h:mm:ss a")
}

def off() {
    //def rampRate = 0
    //if ((parameter258=="0")&&(state.model?.substring(0,5)!="VZM35"))  //if we are in dimmer mode and this is not the Fan Switch then use params 1-8 for rampRate
    //    rampRate = (parameter7!=null?parameter7:(parameter3!=null?parameter3:(parameter1!=null?parameter1:configParams["parameter001"].default)))?.toInteger()
    if (infoEnable) log.info "${device.label?device.label:device.name}: off()" //${device.currentValue('level')}%" + ", ${rampRate/10}s)"// (parameter258=="0"?", ${rampRate/10}s)":")")
    state.lastCommand = "Off"  // (${device.currentValue('level')}%" + ", ${rampRate/10}s)"// (parameter258=="0"?", ${rampRate/10}s)":")")
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zigbee.off(defaultDelay)
    if (traceEnable) log.trace "off $cmds"
    return cmds
}

def on() {
    //def rampRate = 0
    //if ((parameter258=="0")&&(state.model?.substring(0,5)!="VZM35")) //if we are in dimmer mode and this is not the Fan Switch then use params 1-8 for rampRate
    //    rampRate = (parameter3!=null?parameter3:(parameter1!=null?parameter1:configParams["parameter001"].default))?.toInteger()
    if (infoEnable) log.info "${device.label?device.label:device.name}: on()" //${device.currentValue('level')}%" + ", ${rampRate/10}s)"// (parameter258=="0"?", ${rampRate/10}s)":")")
    state.lastCommand = "On"  // (${device.currentValue('level')}%" + ", ${rampRate/10}s)"// (parameter258=="0"?", ${rampRate/10}s)":")")
    state.lastCommandTime = nowFormatted()
    def cmds = []
    if (state.model?.substring(0,5)!="VZM35") cmds += quickStartEmulation()  //if this is not the Fan Switch then emulate QuickStart
    cmds += zigbee.on(defaultDelay)
    if (traceEnable) log.trace "on $cmds"
    return cmds
}

def parse(String description) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: parse($description)"
    Map descMap = zigbee.parseDescriptionAsMap(description)
    if (debugEnable) log.debug "${device.label?device.label:device.name}: $descMap"
    try {
        if (debugEnable && (zigbee.getEvent(description)!=[:])) log.debug "${device.label?device.label:device.name}: zigbee.getEvent ${zigbee.getEvent(description)}"
    } catch (e) {
        if (debugEnable) log.debug "${device.label?device.label:device.name}: "+magenta(bold("There was an error while calling zigbee.getEvent: $description"))   
    }
    def attrHex =    descMap.attrInt==null?null:"0x${zigbee.convertToHexString(descMap.attrInt,4)}"
    def attrInt =    descMap.attrInt==null?null:descMap.attrInt.toInteger()
    def clusterHex = descMap.clusterInt==null?null:"0x${zigbee.convertToHexString(descMap.clusterInt,4)}"
    def clusterInt = descMap.clusterInt==null?null:descMap.clusterInt.toInteger()
    def valueStr =   descMap.value ?: "unknown"
    switch (clusterInt){
        case 0x0000:    //BASIC CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            switch (attrInt) {
                case 0x0004:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Mfg:\t\t$valueStr"
                    state.manufacturer = valueStr
                    break
                case 0x0005:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Model:\t$valueStr"
                    state.model = valueStr
                    break
                case 0x0006:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received FW Date:\t$valueStr"
					state.fwDate = valueStr
                    break
                case 0x0007:
                    def valueInt = Integer.parseInt(descMap['value'],16)
                    valueStr = valueInt==0?"Non-Neutral":"Neutral"
                    if (infoEnable) log.info "${device.label?device.label:device.name}: " + green("Report received Power Source:\t$valueInt ($valueStr)")
                    state.powerSource = valueStr
                    state.parameter21value = valueInt
                    device.updateSetting("parameter21",[value:"${valueInt}",type:"enum"]) 
                    break
                case 0x4000:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received FW Version:\t$valueStr"
					state.fwVersion = valueStr
                    break
                default:
                    if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN ATTRIBUTE")
                    break
            }
            break
        case 0x0003:    //IDENTIFY CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            switch (attrInt) {
                case 0x0000:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received IdentifyTime:\t$valueStr"
                    break
                default:
                    if ((infoEnable && attrInt!=null)||traceEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN ATTRIBUTE ${attrInt}")
                    break
            }
            break
        case 0x0004:    //GROUP CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            switch (attrInt) {
                case 0x0000:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Group Name Support:\t$valueStr"
                    break
                default:
                    if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN ATTRIBUTE")
                    break
            }
            break
        case 0x0005:    //SCENES CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            switch (attrInt) {
                case 0x0000:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Scene Count:\t$valueStr"
                    break
                case 0x0001:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Current Scene:\t$valueStr"
                    break
                case 0x0002:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Current Group:\t$valueStr"
                    break
                case 0x0003:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Scene Valid:\t$valueStr"
                    break
                case 0x0004:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Scene Name Support:\t$valueStr"
                    break
                default:
                    if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN ATTRIBUTE")
                    break
            }
            break
        case 0x0006:    //ON_OFF CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            switch (attrInt) {
                case 0x0000:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        def valueInt = Integer.parseInt(descMap['value'],16)
                        valueStr = valueInt == 0? "off": "on"
                        if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Switch:\t$valueInt\t($valueStr)"
                        sendEvent(name:"switch", value: valueStr)
                        if (state.model?.substring(0,5)=="VZM35") { //FOR FAN ONLY 
		                    def newSpeed =""
                            if      (valueStr=="off")                   newSpeed = "off"
                            else if (parameter258=="1")                 newSpeed = "high"
                            else if (device.currentValue("level")<=33)  newSpeed = "low"
                            else if (device.currentValue("level")<=66)  newSpeed = "medium"
                            else if (device.currentValue("level")<=100) newSpeed = "high"
                            if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Speed:\t${newSpeed}"
                            sendEvent(name:"speed", value: "${newSpeed}")   
                        }
                    }
                    else if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN COMMAND")
                    break
                case 0x4003:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        def valueInt = Integer.parseInt(descMap['value'],16)
                        valueStr = (valueInt==0?"Off":(valueInt==255?"Previous":"On")) 
                        if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Power-On State:\t$valueInt\t($valueStr)"
                        state.powerOnState = valueStr
                    }
                    else if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN COMMAND")																										  
                    break
                default:
                    if (attrInt==null && descMap.command=="00" && descMap.direction=="00") {
                        if (infoEnable||debugEnable) log.info "${device.label?device.label:device.name}: "+darkOrange("Cluster:$clusterHex Heartbeat")
                    }
                    else if (attrInt==null && descMap.command=="0B" && descMap.direction=="01") {
                        if (parameter$51>0) {    //not sure why the V-mark firmware sends these when there are no bindings
                            if (descMap.data[0]=="00" && infoEnable) log.info "${device.label?device.label:device.name}: Bind Command Sent:\tSwitch OFF"
                            if (descMap.data[0]=="01" && infoEnable) log.info "${device.label?device.label:device.name}: Bind Command Sent:\tSwitch ON"
                            if (descMap.data[0]=="02" && infoEnable) log.info "${device.label?device.label:device.name}: Bind Command Sent:\tToggle"
                        }
                    } 
                    else if (infoEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN ATTRIBUTE")
                    break
            }
            break
        case 0x0008:    //LEVEL CONTROL CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            switch (attrInt) {
                case 0x0000:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        def valueInt = Integer.parseInt(descMap['value'],16)
                        valueInt=Math.min(Math.max(valueInt.toInteger(),0),254)
                        def percentValue = convertByteToPercent(valueInt)
                        valueStr = percentValue.toString()+"%"
                        if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Level:\t$valueInt\t($valueStr)"
                        sendEvent(name:"level", value: percentValue, unit: "%")
                        if (state.model?.substring(0,5)=="VZM35") { //FOR FAN ONLY 
		                    def newSpeed =""
                            if (device.currentValue("switch")=="off") newSpeed = "off"
                            else if (parameter258=="1")               newSpeed = "high"
                            else if (percentValue<=33)                newSpeed = "low"
                            else if (percentValue<=66)                newSpeed = "medium"
                            else if (percentValue<=100)               newSpeed = "high"
                            if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Speed:\t${newSpeed}"
                            sendEvent(name:"speed", value: "${newSpeed}")   
                        }
                    }
                    else if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN COMMAND")
                    break
                case 0x0010:
                    if(descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        def valueInt = Integer.parseInt(descMap['value'],16)
                        if (infoEnable) log.info "${device.label?device.label:device.name}: Report received On/Off Transition:\t${valueInt/10}s"
                        state.parameter3value = valueInt
                        device.updateSetting("parameter3",[value:"${valueInt}",type:configParams["parameter003"].type.toString()])
                    }
                    else 
                        if (infoEnable || debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN COMMAND")
                    break
                case 0x0011:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        def valueInt = Integer.parseInt(descMap['value'],16)
                        valueStr = (valueInt==255?"Previous":convertByteToPercent(valueInt).toString()+"%")
                        if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Remote-On Level:\t$valueInt\t($valueStr)"
                    }
                    else if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN COMMAND")
                    break
                case 0x4000:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        def valueInt = Integer.parseInt(descMap['value'],16)
                        valueStr = (valueInt==255?"Previous":convertByteToPercent(valueInt).toString()+"%")
                        if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Power-On Level:\t$valueInt\t($valueStr)"
                        state.parameter15value = convertByteToPercent(valueInt)
                        device.updateSetting("parameter15",[value:"${convertByteToPercent(valueInt)}",type:configParams["parameter015"].type.toString()])
                    }
                    else if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN COMMAND")
                    break
                default:
                    if (attrInt==null && descMap.command=="0B" && descMap.direction=="01") {
                        if (parameter$51>0) {    //not sure why the V-mark firmware sends these when there are no bindings
                            if (descMap.data[0]=="00" && infoEnable) log.info "${device.label?device.label:device.name}: Bind Command Sent:\tMove To Level"
                            if (descMap.data[0]=="01" && infoEnable) log.info "${device.label?device.label:device.name}: Bind Command Sent:\tMove Up/Down"
                            if (descMap.data[0]=="02" && infoEnable) log.info "${device.label?device.label:device.name}: Bind Command Sent:\tStep"
                            if (descMap.data[0]=="03" && infoEnable) log.info "${device.label?device.label:device.name}: Bind Command Sent:\tStop Level Change"
                            if (descMap.data[0]=="04" && infoEnable) log.info "${device.label?device.label:device.name}: Bind Command Sent:\tMove To Level (with On/Off)"
                            if (descMap.data[0]=="05" && infoEnable) log.info "${device.label?device.label:device.name}: Bind Command Sent:\tMove Up/Down (with On/Off)"
                            if (descMap.data[0]=="06" && infoEnable) log.info "${device.label?device.label:device.name}: Bind Command Sent:\tStep (with On/Off)"
                        }
                    }
                    else if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN ATTRIBUTE")
                    break
            }
            break
        case 0x0013:    //ALEXA CLUSTER
            if (infoEnable||debugEnable) log.info "${device.label?device.label:device.name}: "+darkOrange("Alexa Heartbeat")
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            break
        case 0x0019:    //OTA CLUSTER
            if (infoEnable||debugEnable) log.info "${device.label?device.label:device.name}: "+darkOrange("OTA CLUSTER")
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            switch (attrInt) {
                case 0x0000:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Server ID:\t$valueStr"
                    break
                case 0x0001:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received File Offset:\t$valueStr"
                    break
                case 0x0006:
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Upgrade Status:\t$valueStr"
                    break
                default:
                    if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN ATTRIBUTE")
                    break
            }
            break
        case 0x0702:    //SIMPLE METERING CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            switch (attrInt) {
                case 0x0000:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        def valueInt = Integer.parseInt(descMap['value'],16)
                        float energy
                        energy = valueInt/100
                        if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Energy:\t${energy}kWh"
                        sendEvent(name:"energy",value:energy ,unit: "kWh")
                    }
                    else if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN COMMAND")		  													  
                    break
                default:
                    if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN ATTRIBUTE")
                    break
            }
            break
        case 0x0B04:    //ELECTRICAL MEASUREMENT CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            switch (attrInt) {
                case 0x0501:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        def valueInt = Integer.parseInt(descMap['value'],16)
                        float amps
                        amps = valueInt/100
                        if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Amps:\t${amps}A"
                        sendEvent(name:"amps",value:amps ,unit: "A")
                    }
                    else if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN COMMAND")
                    break
                case 0x050b:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        def valueInt = Integer.parseInt(descMap['value'],16)
                        float power 
                        power = valueInt/10
                        if (infoEnable) log.info "${device.label?device.label:device.name}: Report received Power:\t${power}W"
                        sendEvent(name: "power", value: power, unit: "W")
                    }
                    else if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN COMMAND")				  																																	  
                    break
                default:
                    if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN ATTRIBUTE")
                    break
            }
            break
        case 0x8021:    //BINDING CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            break
        case 0x8022:    //UNBINDING CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            break
        case 0x8032:    //ROUTING TABLE CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            break
        case 0xfc31:    //PRIVATE CLUSTER
            if (traceEnable) log.trace "${device.label?device.label:device.name}: ${clusterLookup(clusterHex)} (" +
                                       "clusterId:${descMap.cluster?:descMap.clusterId}" +
                                       (descMap.attrId==null?"":" attrId:${descMap.attrId}") +
                                       (descMap.value==null?"":" value:${descMap.value}") +
                                       (zigbee.getEvent(description)==[:]?(descMap.data==null?"":" data:${descMap.data}"):(" ${zigbee.getEvent(description)}")) + 
                                       ")"
            if (attrInt == null) {
                if (descMap.isClusterSpecific) {
                    if (descMap.command == "00") ZigbeePrivateCommandEvent(descMap.data)        //Button Events
                    if (descMap.command == "04") BindInitiator()                                //Start Binding
                    if (descMap.command == "24") ZigbeePrivateLEDeffectStopEvent(descMap.data)  //LED start/stop events
                }
            } 
            else if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                def valueInt = Integer.parseInt(descMap['value'],16)
				def infoDev = "${device.label?device.label:device.name}: "
                def infoTxt = "Receive  attribute ${attrInt.toString().padLeft(3," ")} value ${valueInt.toString().padLeft(3," ")}"
				def infoMsg = infoDev + infoTxt
                switch (attrInt){
                    case 1:
                        infoMsg += "\t(Remote Dim Rate Up:\t\t" + (valueInt<127?((valueInt/10).toString()+"s)"):"default)")
                        break
                    case 2:
                        infoMsg += "\t(Local Dim Rate Up:\t\t" + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 1)")
                        break
                    case 3:
                        infoMsg += "\t(Remote Ramp Rate On:\t" + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 1)")
                        break
                    case 4:
                        infoMsg += "\t(Local Ramp Rate On:\t\t" + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 3)")
                        break
                    case 5:
                        infoMsg += "\t(Remote Dim Rate Down:\t" + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 1)")
                        break
                    case 6:
                        infoMsg += "\t(Local Dim Rate Down:\t" + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 2)")
                        break
                    case 7:
                        infoMsg += "\t(Remote Ramp Rate Off:\t" + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 3)")
                        break
                    case 8:
                        infoMsg += "\t(Local Ramp Rate Off:\t\t" + (valueInt<127?((valueInt/10).toString()+"s)"):"sync with 4)")
                        break
                    case 9:     //Min Level
                        infoMsg += "\t(min level ${convertByteToPercent(valueInt)}%)"
                        break
                    case 10:    //Max Level
                        infoMsg += "\t(max level ${convertByteToPercent(valueInt)}%)" 
                        break
                    case 11:    //Invert Switch
                        infoMsg += valueInt==0?"\t(not Inverted)":"\t(Inverted)" 
                        break
                    case 12:    //Auto Off Timer
                        infoMsg += "\t(Auto Off Timer " + (valueInt==0?red("disabled"):"${valueInt}s") + ")"
                        break
                    case 13:    //Default Level (local)
                        infoMsg += "\t(default local level " + (valueInt==255?" = previous)":" ${convertByteToPercent(valueInt)}%)")
                        break
                    case 14:    //Default Level (remote)
                        infoMsg += "\t(default remote level " + (valueInt==255?" = previous)":"${convertByteToPercent(valueInt)}%)")
                        break
                    case 15:    //Level After Power Restored
                        infoMsg += "\t(power-on level " + (valueInt==255?" = previous)":"${convertByteToPercent(valueInt)}%)")
                        break
                    case 17:    //Load Level Timeout
                        infoMsg += (valueInt==0?"\t(do not display load level)":(valueInt==11?"\t(always display load level)":"s \tload level timeout"))
                        break
                    case 18: 
                        infoMsg += "\t(Active Power Report" + (valueInt==0?red(" disabled"):" ${valueInt}% change") + ")"
                        break
                    case 19:
                        infoMsg += "s\t(Periodic Power/Energy " + (valueInt==0?red(" disabled"):"") + ")"
                        break
                    case 20:
                        infoMsg += "\t(Active Energy Report " + (valueInt==0?red(" disabled"):" ${valueInt/100}kWh change") + ")"
                        break
                    case 21:    //Power Source
                        infoMsg = infoDev + green(infoTxt + (valueInt==0?"\t(Non-Neutral)":"\t(Neutral)"))
                        break
                    case 22:    //Aux Type
                        switch (state.model?.substring(0,5)){
                            case "VZM31":    //2-in-1 Dimmer
                                infoMsg = infoDev + red(infoTxt + (valueInt==0?"\t(No Aux)":valueInt==1?"\t(Dumb Aux)":"\t(Smart Aux)"))
                                sendEvent(name:"auxType", value:valueInt==0?"None":valueInt==1?"Dumb":"Smart", displayed:false )
                                break
                            case "VZM35":    //3-speed Fan
                                infoMsg = infoDev + red(infoTxt + (valueInt==0?"\t(No Aux)":"\t(Smart Aux)"))
                                sendEvent(name:"auxType", value:valueInt==0?"None":"Smart", displayed:false )
                                break
                            default:
                                infoMsg = infoDev + red(infoTxt + " unknown model $state.model")
                                sendEvent(name:"auxType", value:"unknown model", displayed:false )
                                break
                        }
                        break
                    case 23:    //Quick Start (in firmware on Fan, emulated in this driver for dimmer)
                        if  (state.model?.substring(0,5)!="VZM35") 
                            infoMsg += "\t(Quick Start " + (valueInt==0?red("disabled"):"${valueInt*100} milliseconds ") + ")"
                        else 
                            infoMsg += "\t(Quick Start " + (valueInt==0?red("disabled"):"${valueInt} seconds") + ")"
                        break
                    case 50:    //Button Press Delay
                        infoMsg += "\t(${valueInt*100}ms Button Delay)"
                        break
                    case 51:    //Device Bind Number
                        infoMsg = infoDev + green(infoTxt + "\t(Bindings)")
                        sendEvent(name:"numberOfBindings", value:valueInt, displayed:false )
                        break
                    case 52:    //Smart Bulb Mode
                        infoMsg = infoDev + red(infoTxt) + (valueInt==0?red("\t(SBM disabled)"):green("\t(SBM enabled)"))
                        sendEvent(name:"smartBulb", value:valueInt==0?"Disabled":"Enabled", displayed:false )
                        break
                    case 53:  //Double-Tap UP for full brightness
                        infoMsg += "\t(Double-Tap Up " + (valueInt==0?red("disabled"):green("enabled")) + ")"
                        break
                    case 95:
                    case 96:
                        infoMsg = infoDev + hue(valueInt,infoTxt + "\t(${Math.round(valueInt/255*360)}°)")
                        break
                    case 97:  //LED bar intensity when on
                    case 98:  //LED bar intensity when off
                        infoMsg += "%\t(LED bar intensity when " + (attrInt==97?"On)":"Off)")
                        break
                    case 256:    //Local Protection
                        infoMsg += "\t(Local Control " + (valueInt==0?green("enabled"):red("disabled")) + ")"
                        break
                    case 257:    //Remote Protection
                        infoMsg += "\t(Remote Control " + (valueInt==0?green("enabled"):red("disabled")) + ")"
                        break
                    case 258:    //Switch Mode
                        switch (state.model?.substring(0,5)){
                            case "VZM31":    //2-in-1 Dimmer
                                infoMsg = infoDev + red(infoTxt + (valueInt==0?"\t(Dimmer mode)":"\t(On/Off mode)"))
                                sendEvent(name:"switchMode", value:valueInt==0?"Dimmer":"On/Off", displayed:false )
                                break
                            case "VZM35":    //3-speed Fan
								infoMsg = infoDev + red(infoTxt + (valueInt==0?"\t(3-Speed mode)":"\t(On/Off mode)"))
								sendEvent(name:"switchMode", value:valueInt==0?"3-Speed":"On/Off", displayed:false )
								break
                            default:
                                infoMsg = infoDev + red(infoTxt + " unknown model $state.model")
                                sendEvent(name:"switchMode", value:"unknown model", displayed:false )
                                break
                        }
						break
                    case 259:    //On-Off LED
                        infoMsg += "\t(On-Off LED mode: " + (valueInt==0?"All)":"One)")
                        break
                    case 260:    //Firmware Update Indicator
                        infoMsg += "\t(Firmware Update Indicator " + (valueInt==0?red("disabled"):green("enabled")) + ")"
                        break
                    case 261:    //Relay Click
                        infoMsg += "\t(Relay Click " + (valueInt==0?green("enabled"):red("disabled")) + ")"
                        break
                    default:
                        infoMSg += orangeRed(" *** Undefined Parameter $attrInt ***")
                        break
                }
                if (infoEnable) log.info infoMsg
                if ((attrInt==9)||(attrInt==10)||(attrInt==13)||(attrInt==14)||(attrInt==15)) valueInt = convertByteToPercent(valueInt)    //these attributes are stored as bytes but presented as percentages
                if (attrInt>0) device.updateSetting("parameter${attrInt}",[value:"${valueInt}",type:configParams["parameter${attrInt.toString().padLeft(3,"0")}"].type.toString()]) //update local setting with value received from device                   
                state."parameter${attrInt}value" = valueInt  //update state variable with value received from device
                if ((attrInt==95 && parameter95custom!=null)||(attrInt==96 && parameter96custom!=null)) {   //if custom hue was set, update the custom state variable also
                    device.updateSetting("parameter${attrInt}custom",[value:"${Math.round(valueInt/255*360)}",type:configParams["parameter${attrInt.toString().padLeft(3,"0")}"].type.toString()])
                    state."parameter${attrInt}custom" = Math.round(valueInt/255*360)
                }
                if ((valueInt==configParams["parameter${attrInt.toString()?.padLeft(3,"0")}"]?.default?.toInteger())  //IF  setting is the default
                && (attrInt!=21)&&(attrInt!=22)&&(attrInt!=51)&&(attrInt!=52)&&(attrInt!=258)) {                     //AND  not read-only or primary config params
                    if (debugEnable) log.debug "${device.label?device.label:device.name}: parse() cleared parameter${attrInt}"
                    device.clearSetting("parameter${attrInt}")                                                       //THEN clear the setting (so only changed settings are displayed)
                }
            }
            else if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN COMMAND" + (debugEnable?"\t$descMap\t${zigbee.getEvent(description)}":""))
            break
        default:
            if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("Cluster:$clusterHex UNKNOWN CLUSTER" + (debugEnable?"\t$descMap\t${zigbee.getEvent(description)}":""))
            break
    }
    state.lastEventTime =      nowFormatted() 
    state.lastEventCluster =   clusterLookup(clusterHex)
    state.lastEventAttribute = attrInt
    state.lastEventValue =     descMap.value
}

def ping() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: ping()"
    refresh()
}

def poll() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: poll()"
    refresh()
}

def presetLevel(value) {    //possible future command
    if (infoEnable) log.info "${device.label?device.label:device.name}: presetLevel(${value})"
    state.lastCommand = "Preset Level (${value})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    Integer scaledValue = value==null?null:Math.min(Math.max(convertPercentToByte(value.toInteger()),1),255)  //ZigBee levels range from 0x01-0xfe with 00 and ff = 'use previous'
    cmds += setPrivateCluster(13, scaledValue, 8)
    //if (traceEnable) log.trace "preset $cmds"
    return cmds
}
  
def refresh(option) {
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.label?device.label:device.name}: refresh(${option})"
    state.lastCommand = "Refresh " + option
    state.lastCommandTime = nowFormatted()
    def cmds = []
    //cmds += zigbee.readAttribute(0x0000, 0x0000, [:], defaultDelay)    //CLUSTER_BASIC ZCL Version
    //cmds += zigbee.readAttribute(0x0000, 0x0001, [:], defaultDelay)    //CLUSTER_BASIC Application Version
    //cmds += zigbee.readAttribute(0x0000, 0x0002, [:], defaultDelay)    //CLUSTER_BASIC 
    //cmds += zigbee.readAttribute(0x0000, 0x0003, [:], defaultDelay)    //CLUSTER_BASIC 
    cmds += zigbee.readAttribute(0x0000, 0x0004, [:], defaultDelay)    //CLUSTER_BASIC Mfg
    cmds += zigbee.readAttribute(0x0000, 0x0005, [:], defaultDelay)    //CLUSTER_BASIC Model
    cmds += zigbee.readAttribute(0x0000, 0x0006, [:], defaultDelay)    //CLUSTER_BASIC SW Date Code
    cmds += zigbee.readAttribute(0x0000, 0x0007, [:], defaultDelay)    //CLUSTER_BASIC Power Source
    //cmds += zigbee.readAttribute(0x0000, 0x0008, [:], defaultDelay)    //CLUSTER_BASIC dev class
    //cmds += zigbee.readAttribute(0x0000, 0x0009, [:], defaultDelay)    //CLUSTER_BASIC dev type
    //cmds += zigbee.readAttribute(0x0000, 0x000A, [:], defaultDelay)    //CLUSTER_BASIC prod code
    //cmds += zigbee.readAttribute(0x0000, 0x000B, [:], defaultDelay)    //CLUSTER_BASIC prod url
    cmds += zigbee.readAttribute(0x0000, 0x4000, [:], defaultDelay)    //CLUSTER_BASIC SW Build ID
    //cmds += zigbee.readAttribute(0x0003, 0x0000, [:], defaultDelay)    //CLUSTER_IDENTIFY Identify Time
    //cmds += zigbee.readAttribute(0x0004, 0x0000, [:], defaultDelay)    //CLUSTER_GROUP Name Support
    //cmds += zigbee.readAttribute(0x0005, 0x0000, [:], defaultDelay)    //CLUSTER_SCENES Scene Count
    //cmds += zigbee.readAttribute(0x0005, 0x0001, [:], defaultDelay)    //CLUSTER_SCENES Current Scene
    //cmds += zigbee.readAttribute(0x0005, 0x0002, [:], defaultDelay)    //CLUSTER_SCENES Current Group
    //cmds += zigbee.readAttribute(0x0005, 0x0003, [:], defaultDelay)    //CLUSTER_SCENES Scene Valid
    //cmds += zigbee.readAttribute(0x0005, 0x0004, [:], defaultDelay)    //CLUSTER_SCENES Name Support
    cmds += zigbee.readAttribute(0x0006, 0x0000, [:], defaultDelay)    //CLUSTER_ON_OFF Current OnOff state
    cmds += zigbee.readAttribute(0x0006, 0x4003, [:], defaultDelay)    //CLUSTER_ON_OFF Startup OnOff state
    cmds += zigbee.readAttribute(0x0008, 0x0000, [:], defaultDelay)    //CLUSTER_LEVEL_CONTROL Current Level
    //cmds += zigbee.readAttribute(0x0008, 0x0001, [:], defaultDelay)    //CLUSTER_LEVEL_CONTROL Remaining Time
    //cmds += zigbee.readAttribute(0x0008, 0x000F, [:], defaultDelay)    //CLUSTER_LEVEL_CONTROL Options
    if (state.model?.substring(0,5)!="VZM35")  //Fan does not support on_off transition time
      cmds += zigbee.readAttribute(0x0008, 0x0010, [:], defaultDelay)    //CLUSTER_LEVEL_CONTROL OnOff Transition Time
    cmds += zigbee.readAttribute(0x0008, 0x0011, [:], defaultDelay)    //CLUSTER_LEVEL_CONTROL Default Remote On Level
    cmds += zigbee.readAttribute(0x0008, 0x4000, [:], defaultDelay)    //CLUSTER_LEVEL_CONTROL Startup Level
    //cmds += zigbee.readAttribute(0x0019, 0x0000, [:], defaultDelay)    //CLUSTER_OTA Upgrade Server ID
    //cmds += zigbee.readAttribute(0x0019, 0x0001, [:], defaultDelay)    //CLUSTER_OTA File Offset
    //cmds += zigbee.readAttribute(0x0019, 0x0006, [:], defaultDelay)    //CLUSTER_OTA Image Upgrade Status
    if (state.model?.substring(0,5)!="VZM35")  //Fan does not support power/energy reports
      cmds += zigbee.readAttribute(0x0702, 0x0000, [:], defaultDelay)    //CLUSTER_SIMPLE_METERING Energy Report
    //cmds += zigbee.readAttribute(0x0702, 0x0200, [:], defaultDelay)    //CLUSTER_SIMPLE_METERING Status
    //cmds += zigbee.readAttribute(0x0702, 0x0300, [:], defaultDelay)    //CLUSTER_SIMPLE_METERING Units
    //cmds += zigbee.readAttribute(0x0702, 0x0301, [:], defaultDelay)    //CLUSTER_SIMPLE_METERING AC Multiplier
    //cmds += zigbee.readAttribute(0x0702, 0x0302, [:], defaultDelay)    //CLUSTER_SIMPLE_METERING AC Divisor
    //cmds += zigbee.readAttribute(0x0702, 0x0303, [:], defaultDelay)    //CLUSTER_SIMPLE_METERING Formatting
    //cmds += zigbee.readAttribute(0x0702, 0x0306, [:], defaultDelay)    //CLUSTER_SIMPLE_METERING Metering Device Type
    //cmds += zigbee.readAttribute(0x0B04, 0x0501, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT Line Current
    //cmds += zigbee.readAttribute(0x0B04, 0x0502, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT Active Current
    //cmds += zigbee.readAttribute(0x0B04, 0x0503, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT Reactive Current
    //cmds += zigbee.readAttribute(0x0B04, 0x0505, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT RMS Voltage
    //cmds += zigbee.readAttribute(0x0B04, 0x0506, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT RMS Voltage min
    //cmds += zigbee.readAttribute(0x0B04, 0x0507, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT RMS Voltage max
    //cmds += zigbee.readAttribute(0x0B04, 0x0508, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT RMS Current
    //cmds += zigbee.readAttribute(0x0B04, 0x0509, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT RMS Current min
    //cmds += zigbee.readAttribute(0x0B04, 0x050A, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT RMS Current max
    if (state.model?.substring(0,5)!="VZM35")  //Fan does not support power/energy reports
      cmds += zigbee.readAttribute(0x0B04, 0x050B, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT Active Power
    //cmds += zigbee.readAttribute(0x0B04, 0x050C, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT Active Power min
    //cmds += zigbee.readAttribute(0x0B04, 0x050D, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT Active Power max
    //cmds += zigbee.readAttribute(0x0B04, 0x050E, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT Reactive Power
    //cmds += zigbee.readAttribute(0x0B04, 0x050F, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT Apparent Power
    //cmds += zigbee.readAttribute(0x0B04, 0x0510, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT Power Factor
    //cmds += zigbee.readAttribute(0x0B04, 0x0604, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT Power Multiplier
    //cmds += zigbee.readAttribute(0x0B04, 0x0605, [:], defaultDelay)    //CLUSTER_ELECTRICAL_MEASUREMENT Power Divisor
    //cmds += zigbee.readAttribute(0x8021, 0x0000, [:], defaultDelay)    //Binding Cluster
    //cmds += zigbee.readAttribute(0x8022, 0x0000, [:], defaultDelay)    //UnBinding Cluster
    getParameterNumbers().each{ i -> 
        if (i==23 && (state.model?.substring(0,5)!="VZM35")) {  //QuickStart is implemented in firmware for the fan, emulated in this driver for 2-in-1 Dimmer
            setQuickStartVariables()
        }
        switch (option) {
            case "":
            case " ":
            case null:
                if (((i>=1)&&(i<=8))||(i==22)||(i==51)||(i==52)||(i==258)) cmds += getAttribute(0xfc31, i) //if option is blank or null then refresh primary and read-only settings
                break
            case "User":                
                if (settings."parameter${i}"!=null) cmds += getAttribute(0xfc31, i) //if option is User then refresh settings that are non-blank
                break
            case "All":
                cmds += getAttribute(0xfc31, i) //if option is All then refresh all settings
                break
            
        }
    }
    return cmds
}

def resetEnergyMeter() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: resetEnergyMeter(" + device.currentValue("energy") + "kWh)"
    state.lastCommand = "Reset Energy Meter (" + device.currentValue("energy") + "kWh)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zigbee.command(0xfc31,0x02,["mfgCode":"0x122F"],defaultDelay,"0")
    cmds += zigbee.readAttribute(CLUSTER_SIMPLE_METERING, 0x0000)
    if (traceEnable) log.trace "resetEnergy $cmds"
    return cmds 
}
        
def setAttribute(Integer cluster, Integer attrInt, Integer dataType, Integer value, Map additionalParams = [:], Integer delay=defaultDelay) {
    if (cluster==0xfc31) additionalParams = ["mfgCode":"0x122F"]
    if ((delay==null)||(delay==0)) delay = defaultDelay
    if (debugEnable) log.debug "${device.label?device.label:device.name} setAttribute(" +
                             "0x${zigbee.convertToHexString(cluster,4)}, " +
                             "0x${zigbee.convertToHexString(attrInt,4)}, " +
                             "0x${zigbee.convertToHexString(dataType,2)}, " +
                               "${value}, ${additionalParams}, ${delay})"
    def infoMsg = "${device.label?device.label:device.name}: Sending "
    if (cluster==0xfc31) {
        infoMsg += " attribute ${attrInt.toString().padLeft(3," ")} value "
        switch (attrInt) {
            case 9:     //min level
            case 10:    //max level
            case 13:    //default local level
            case 14:    //default remote level
            case 15:    //level after power restored
                infoMsg += "${convertByteToPercent(value)}%\tconverted to ${value}\t(0..255 scale)"
                break
            case 23:
                setQuickStartVariables()
                infoMsg += "${value.toString().padLeft(3," ")}"
                break
            case 95:
            case 96:
                infoMsg += "${value.toString().padLeft(3," ")} (" + Math.round(value/255*360) + "°)"
                break
            default:
                infoMsg += "${value.toString().padLeft(3," ")}"
                break 
        }
    } 
    else {
        infoMsg += "" + (cluster==0xfc31?"":clusterLookup(cluster)) + " attribute 0x${zigbee.convertToHexString(attrInt,4)} value ${value}"
    }
    if (infoEnable) log.info infoMsg + (delay==defaultDelay?"":" [delay ${delay}]")
    def cmds = zigbee.writeAttribute(cluster, attrInt, dataType, value, additionalParams, delay)
    if (traceEnable) log.trace "setAttr $cmds"
    return cmds
}

def getAttribute(Integer cluster, Integer attrInt, Map additionalParams = [:], Integer delay=defaultDelay) {
    if (cluster==0xfc31) additionalParams = ["mfgCode":"0x122F"]
    if (delay==null||delay==0) delay = defaultDelay
    if (traceEnable) log.trace  "${device.label?device.label:device.name}: Getting "+(cluster==0xfc31?"":clusterLookup(cluster))+" attribute ${attrInt}"+(delay==defaultDelay?"":" [delay ${delay}]")
    if (debugEnable) log.debug  "${device.label?device.label:device.name} getAttribute(0x${zigbee.convertToHexString(cluster,4)}, 0x${zigbee.convertToHexString(attrInt,4)}, ${additionalParams}, ${delay})"
    if (cluster==0xfc31 && attrInt==23 && state.model?.substring(0,5)!="VZM35") {  //if not Fan, get the QuickStart values from state variables since dimmer does not store these
        if (infoEnable) log.info "${device.label?device.label:device.name}: Receive  attribute ${attrInt.toString().padLeft(3," ")} value ${state.parameter23value?.toString().padLeft(3," ")}\t(QuickStart " + 
            (state.parameter23value?.toInteger()==0?red("disabled"):"${state?.parameter23value.toInteger()*100} milliseconds ") + ")"
        if (infoEnable) log.info "${device.label?device.label:device.name}: Receive  attribute ${attrInt.toString().padLeft(3," ")} level ${state.parameter23level?.toString().padLeft(3," ")}\t(QuickStart startup level)"
    }
    def cmds = []
    //String mfgCode = "{}"
    //if(additionalParams.containsKey("mfgCode")) mfgCode = "{${additionalParams.get("mfgCode")}}"
    //String rattrArgs = "0x${device.deviceNetworkId} 0x01 0x${zigbee.convertToHexString(cluster,4)} " + 
    //                   "0x${zigbee.convertToHexString(attrInt,4)} " + 
    //                   "$mfgCode"
    //cmds += ["he rattr $rattrArgs", "delay $delay"] 
    cmds += zigbee.readAttribute(cluster, attrInt, additionalParams, delay)
    if (traceEnable) log.trace "getAttr $cmds"
    return cmds
}

//def setLevel(newLevel) {
//    if (infoEnable) log.info "${device.label?device.label:device.name}: setLevel(${newLevel})"
//    state.lastCommand = "Set Level (${newLevel})"
//    state.lastCommandTime = nowFormatted()
//    def cmds = []
//    cmds += zigbee.setLevel(newLevel)
//    return cmds
//}

def setLevel(newLevel,duration=null) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: setLevel(${newLevel}" + (duration==null?")":", ${duration}s)")
    state.lastCommand = "Set Level"
    state.lastCommandTime = nowFormatted()
    if (duration!=null) duration = duration.toInteger()*10  //firmware duration in 10ths
    def cmds = []
    if (state.model?.substring(0,5)!="VZM35") cmds += quickStartEmulation()  //if this is not the Fan Switch then emulate QuickStart
    cmds += duration==null?zigbee.setLevel(newLevel):zigbee.setLevel(newLevel,duration)
    if (traceEnable) log.trace "setLevel $cmds"
    return cmds
}

def quickStartEmulation() {
    setQuickStartVariables()
    def cmds= []
    if (settings.parameter23.toInteger()>0 && device.currentValue("switch")=="off") {  //don't QuickStart if switch is already on
        if (infoEnable) log.info "${device.label?device.label:device.name}: quickStartEmulation(${settings.parameter23.toInteger()*100}ms, ${settings.parameter23level})"
        def startLevel = device.currentValue("level")
        cmds += zigbee.setLevel(state.parameter23level.toInteger(),0,state.parameter23value.toInteger()*100)  //QuickStart should jump to level (0 duration) with brief delay after
        cmds += zigbee.setLevel(startLevel.toInteger(),0,defaultDelay) 
    }
    return cmds
}

def setQuickStartVariables() {
    if (state.model?.substring(0,5)!="VZM35") {  //IF not the Fan switch THEN set the QuickStart variables manually
        settings.parameter23 =  (settings.parameter23!=null?settings.parameter23:configParams["parameter023"].default).toInteger()
        state.parameter23value = settings.parameter23?:0
        state.parameter23level = settings.parameter23level?:100
    }
}

def setSpeed(value) {  // FOR FAN ONLY
    if (infoEnable) log.info "${device.label?device.label:device.name}: setSpeed(${value})"
    state.lastCommand = "Set Speed (${value})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    switch (value) {
        case "off":
            cmds += zigbee.setLevel(0) 
            break
        case "low": 
            cmds += zigbee.setLevel(25) 
            break
        case "medium-low": 
            cmds += zigbee.setLevel(33) 
            break
        case "medium": 
            cmds += zigbee.setLevel(50) 
            break
        case "medium-high": 
            cmds += zigbee.setLevel(66) 
            break
        case "high": 
            cmds += zigbee.setLevel(100) 
            break
        case "on":  
            cmds += zigbee.setLevel(100) 
            break
    }
    if (traceEnable) log.trace "setSpeed $cmds"
    return cmds
}

def setPrivateCluster(attributeId, value=null, size=8) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: setPrivateCluster(${attributeId}, ${value}, ${size})"
    state.lastCommand = "Set Private Cluster"// (${attributeId},${value},${size})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    Integer attId = attributeId.toInteger()
    Integer attValue = (value?:0).toInteger()
    Integer attSize = calculateSize(size).toInteger()
    if (value!=null) cmds += setAttribute(0xfc31,attId,attSize,attValue,[:],attId==258?2000:defaultDelay)
    cmds += getAttribute(0xfc31, attId)
    //if (traceEnable) log.trace "setPrivate $cmds"
    return cmds
}

def setZigbeeAttribute(cluster, attributeId, value=null, size=8) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: setZigbeeAttribute(${cluster}, ${attributeId}, ${value}, ${size})"
    state.lastCommand = "Set Zigbee Attribute"// ($cluster, $attributeId, $value, $size)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    Integer setCluster = cluster.toInteger()
    Integer attId = attributeId.toInteger()
    Integer attValue = (value?:0).toInteger()
    Integer attSize = calculateSize(size).toInteger()
    if (value!=null) cmds += setAttribute(setCluster,attId,attSize,attValue,[:],attId==258?2000:defaultDelay)
    cmds += getAttribute(setCluster, attId)
    //if (traceEnable) log.trace "setZigbee $cmds"
    return cmds
}

//def startLevelChange(direction) {
//    startLevelChange(direction, null)
//}

def startLevelChange(direction, duration=null) {
    def newLevel = direction=="up"?100:device.currentValue("switch")=="off"?0:1
	if (parameter258=="1") duration=0  //if switch mode is on/off then ramping is 0
    //if (duration==null){               //if we didn't pass in the duration then get it from parameters
    //    if (direction=="up")           //if direction is up use parameter1 dimming duration
    //        duration = (parameter1!=null?parameter1:configParams["parameter001"].default)?.toInteger()    //dimming up, use parameter1, if null use default
    //    else                           //else direction is down so use parameter5 dim duration unless default then use parameter1 dim duration
    //        duration = (parameter5!=null?parameter5:(parameter1!=null?parameter1:configParams["parameter001"].default))?.toInteger()
    //}
    //else {
    //    duration = duration*10          //we passed in seconds but calculations are based on 10ths of seconds
    //}
    //if (duration==null) duration = configParams["parameter001"].default.toInteger()	//catch-all just in case we still have a null then use parameter001 default
    if (infoEnable) log.info "${device.label?device.label:device.name}: startLevelChange(${direction}, ${duration}s)"
    state.lastCommand = "Start Level Change"// (${direction}" + (duration==0?")":", ${duration/10}s)")  //duration is in 10ths of seconds
    state.lastCommandTime = nowFormatted()
    if (duration!=null) duration = duration.toInteger()*10  //firmware duration in 10ths
    def cmds = []
    cmds += duration==null?zigbee.setLevel(newLevel):zigbee.setLevel(newLevel, duration)
    log.trace "startLevel $cmds"
    return cmds
}

def stopLevelChange() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: stopLevelChange()" // at level " + device.currentValue("level")
    state.lastCommand = "Stop Level Change"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += ["he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} ${CLUSTER_LEVEL_CONTROL} ${COMMAND_STOP} {}","delay $defaultDelay"]
    if (traceEnable) log.trace "stopLevel $cmds"
    return cmds
}

def toggle() {	
    def toggleDirection = device.currentValue("switch")=="off"?"off->on":"on->off"
    if (infoEnable) log.info "${device.label?device.label:device.name}: toggle(${toggleDirection})"
    state.lastCommand = "Toggle ($toggleDirection)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    if (state.model?.substring(0,5)!="VZM35") cmds += quickStartEmulation()  //if this is not the Fan Switch then emulate QuickStart
    //cmds += zigbee.command(CLUSTER_ON_OFF, COMMAND_TOGGLE)  //toggle is inconsistent with QiuckStart, so we emulate toggle with on/off instead
    //if having trouble keeping multiple bulbs in sync, use the below code to emulate toggle
    if (device.currentValue("switch")=="off")            //uncomment these lines to convert Toggle to On/Off commands 
       cmds += zigbee.on(defaultDelay)                   //
    else                                                 //
       cmds += zigbee.off(defaultDelay)                  //
    if (traceEnable) log.trace "toggle $cmds"
    return cmds
}

def updated(option) { // called when "Save Preferences" is requested
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.label?device.label:device.name}: updated(${option})"
    if (infoEnable   && disableInfoLogging)   runIn(disableInfoLogging*60,infoLogsOff)
    if (traceEnable  && disableTraceLogging)  runIn(disableTraceLogging*60,traceLogsOff)
    if (debugEnable  && disableDebugLogging)  runIn(disableDebugLogging*60,debugLogsOff) 
    def changedParams = []
    def cmds = []
    def nothingChanged = true
    int setAttrDelay = defaultDelay 
    int defaultValue
    int newValue
    int oldValue
    getParameterNumbers().each{ i ->
        defaultValue=configParams["parameter${i.toString().padLeft(3,'0')}"].default.toInteger()
        oldValue=state."parameter${i}value"!=null?state."parameter${i}value".toInteger():defaultValue
        if ((i==9)||(i==10)||(i==13)||(i==14)||(i==15)) {    //convert the percent preferences back to byte values before testing for changes
            defaultValue=convertPercentToByte(defaultValue)
            oldValue=convertPercentToByte(oldValue)
        }
        if ((i==95 && parameter95custom!=null)||(i==96 && parameter96custom!=null)) {                                         //IF  a custom hue value is set
            if ((Math.round(settings?."parameter${i}custom"?.toInteger()/360*255)==settings?."parameter${i}"?.toInteger())) { //AND custom setting is same as normal setting
                device.clearSetting("parameter${i}custom")                                                                    //THEN clear custom hue and use normal color 
                if (infoEnable) log.info "${device.label?device.label:device.name}: Cleared Custom Hue setting since it equals standard color setting"
            }
            oldvalue=state."parameter${i}custom"!=null?state."parameter${i}custom".toInteger():oldValue
        }
        newValue = calculateParameter(i)
        if ((option == "Default")&&(i!=21)&&(i!=22)&&(i!=51)&&(i!=52)&&(i!=258)){    //if DEFAULT option was selected then use the default value (but don't change switch modes)
            newValue = defaultValue
            if (debugEnable) log.debug "${device.label?device.label:device.name}: updated() has cleared parameter${attrInt}"
            device.clearSetting("parameter${i}")  //and clear the local settings so they go back to default values
            if ((i==95)||(i==96)) device.clearSetting("parameter${i}custom")    //clear the custom hue colors also
        }
        //If a setting changed OR we selected ALL then update parameters in the switch (but don't change switch modes when ALL is selected)
        //log.debug "Param:$i default:$defaultValue oldValue:$oldValue newValue:$newValue setting:${settings."parameter$i"} `$option`"
        if ((newValue!=oldValue) 
        || ((option=="User")&&(settings."parameter${i}"!=null)) 
        || ((option=="All")&&(i!=258))) {
            if ((i==52)||(i==258)) setAttrDelay = setAttrDelay!=2000?2000:defaultDelay  //IF   we're changing modes THEN delay longer
            else                   setAttrDelay = defaultDelay                          //ELSE set back to default delay if we already delayed previously
            cmds += setAttribute(0xfc31, i, calculateSize(configParams["parameter${i.toString().padLeft(3,'0')}"].size), newValue.toInteger(), ["mfgCode":"0x122F"], setAttrDelay)
            changedParams += i
            nothingChanged = false
        }
        else if ((i==23)&&(state.model?.substring(0,5)!="VZM35")) {  //IF not Fan switch THEN manually update the QuickStart state variables since Dimmer does not store these
            setQuickStartVariables()
        }
    }
    changedParams.each{ i ->     //read back the parameters we've changed so the state variables are updated 
        cmds += getAttribute(0xfc31, i)
    }
    if (nothingChanged && (infoEnable||debugEnable||traceEnable)) {
        log.info "${device.label?device.label:device.name}: No device settings were changed"
        log.info  "${device.label?device.label:device.name}: Info logging    "  + (infoEnable?green("Enabled"):red("Disabled"))
        log.trace "${device.label?device.label:device.name}: Trace logging  "   + (traceEnable?green("Enabled"):red("Disabled"))
        log.debug "${device.label?device.label:device.name}: Debug logging "    + (debugEnable?green("Enabled"):red("Disabled"))
    }
    return cmds
}

List updateFirmware() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: updateFirmware(switch's fwDate: ${state.fwDate}, switch's fwVersion: ${state.fwVersion})"
    state.lastCommand = "Update Firmware"
    state.lastCommandTime = nowFormatted()
    if (state.lastUpdate != null && now() - state.lastUpdate < 2000) {
        def cmds = []
        cmds += zigbee.updateFirmware()
        if (traceEnable) log.trace "updateFirmware $cmds"
        return cmds
    } else {
        log.info "Firmware in this channel may be \"beta\" quality. Please check https://community.inovelli.com/c/switches/switch-firmware/42 before proceeding. Double click \"Update Firmware\" to proceed"
    }
    state.lastUpdate = now()
    return []
}
//  ****  COMMENTED OUT BECAUSE I DON'T THINK THESE METHODS ARE USED ANYWHERE  ****
//
//ArrayList<String> zigbeeWriteAttribute(Integer cluster, Integer attributeId, Integer dataType, Integer value, Map additionalParams = [:], int delay = defaultDelay) {
//    if (debugEnable||traceEnable) log.trace "${device.label?device.label:device.name}: zigbeeWriteAttribute($cluster,$attributeId,$dataType,$value,$additionalParams,$delay)"
//    if (delay==null||delay==0) delay = defaultDelay
//    ArrayList<String> cmds = setAttribute(cluster, attributeId, dataType, value, additionalParams, delay)
//    //cmds[0] = cmds[0].replace('0xnull', '0x01')
//    if (debugEnable) log.debug "${device.label?device.label:device.name}: zigbeeWriteAttribute cmds=${cmds}"
//    return cmds
//}
//
//ArrayList<String> zigbeeWriteAttribute(Integer endpoint, Integer cluster, Integer attributeId, Integer dataType, Integer value, Map additionalParams = [:], int delay = defaultDelay) {
//    if (debugEnable||traceEnable) log.trace "${device.label?device.label:device.name}: zigbeeWriteAttribute($endpoint,$cluster,$attributeId,$dataType,$value,$additionalParams,$delay)"
//    if (delay==null||delay==0) delay = defaultDelay
//    String mfgCode = ""
//    if(additionalParams.containsKey("mfgCode"))
//        mfgCode = " ${zigbee.convertToHexString(zigbee.convertHexToInt(additionalParams.get("mfgCode")), 4)}"
//    //Integer size = dataType==0x21?4:2
//    //String wattrArgs = "0x${device.deviceNetworkId} " +
//    //                   "0x${zigbee.convertToHexString(endpoint, 2)} " +
//    //                   "0x${zigbee.convertToHexString(cluster, 4)} " + 
//    //                   "0x${zigbee.convertToHexString(attributeId, 4)} " + 
//    //                   "0x${zigbee.convertToHexString(dataType, 2)} " + 
//    //                   "0x${zigbee.convertToHexString(value, size)} " 
//    //                   "${mfgCode}"
//    //ArrayList<String> cmds = ["he wattr $wattrArgs", "delay $delay"]
//    ArrayList<String> cmds = setAttribute(cluster, attributeId, dataType, value, additionalParams, delay)
//    if (debugEnable) log.debug "${device.label?device.label:device.name}: zigbeeWriteAttribute cmds=${cmds}"
//    return cmds
//}

void ZigbeePrivateCommandEvent(data) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ButtonNumber: ${data[0]} ButtonAttributes: ${data[1]}"
    Integer ButtonNumber = Integer.parseInt(data[0],16)
    Integer ButtonAttributes = Integer.parseInt(data[1],16)
    switch(zigbee.convertToHexString(ButtonNumber,2) + zigbee.convertToHexString(ButtonAttributes,2)) {
        case "0200":    //Tap Up 1x
            if (state.model?.substring(0,5)!="VZM35") zigbee.on()  //If not Fan then emulate QuickStart for local button push
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
            log.warn "${device.label?device.label:device.name}: "+fireBrick("Undefined button function ButtonNumber: ${data[0]} ButtonAttributes: ${data[1]}")
            break
    }
}

void ZigbeePrivateLEDeffectStopEvent(data) {
    Integer ledNumber = Integer.parseInt(data[0],16)+1 //LED number is 0-based
    String  ledStatus = ledNumber==17?"Stop All":ledNumber==256?"User Cleared":"Stop LED${ledNumber}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: ledEffect: ${ledStatus}"
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
			sendEvent(name:"ledEffect", value: "${ledStatus}", displayed:false)
            break
        default:  
			log.warn "${device.label?device.label:device.name}: "+fireBrick("Undefined LEDeffectStopEvent: ${data[0]}")
            break
    }
}

void buttonEvent(button, action, type = "digital") {
    if (infoEnable) log.info "${device.label?device.label:device.name}: ${type} Button ${button} was ${action}"
    sendEvent(name: action, value: button, isStateChange: true, type: type)
    switch (button) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
            sendEvent(name:"lastButton", value: "${action=='pushed'?'Tap '.padRight(button+4, '▲'):'Tap '.padRight(button+4, '▼')}", displayed:false)
            break
        case 6:
            sendEvent(name:"lastButton", value: "${action=='pushed'?'Hold ▲':'Hold ▼'}", displayed:false)
            break
        case 7:
            sendEvent(name:"lastButton", value: "${action=='pushed'?'Release ▲':'Release ▼'}", displayed:false)
            break
        case 8:
        case 9:
        case 10:
        case 11:
        case 12:
            sendEvent(name:"lastButton", value: "Tap ".padRight(button-3, "►"), displayed:false)
            break
        case 13:
            sendEvent(name:"lastButton", value: "Hold ►", displayed:false)
            break
        case 14:
            sendEvent(name:"lastButton", value: "Release ►", displayed:false)
            break
    }
}

void hold(button) {
    buttonEvent(button, "held", "digital")
}

void push(button) {
    buttonEvent(button, "pushed", "digital")
}

void release(button) {
    buttonEvent(button, "released", "digital")
}

def pressUpX1() {
    buttonEvent(1, "pushed", "digital")
}

def pressDownX1() {
    buttonEvent(1, "held", "digital")
}

def pressUpX2() {
    buttonEvent(2, "pushed", "digital")
}

def pressDownX2() {
    buttonEvent(2, "held", "digital")
}

def pressUpX3() {
    buttonEvent(3, "pushed", "digital")
}

def pressDownX3() {
    buttonEvent(3, "held", "digital")
}

def pressUpX4() {
    buttonEvent(4, "pushed", "digital")
}

def pressDownX4() {
    buttonEvent(4, "held", "digital")
}

def pressUpX5() {
    buttonEvent(5, "pushed", "digital")
}

def pressDownX5() {
    buttonEvent(5, "held", "digital")
}

def holdUp() {
    buttonEvent(6, "pushed", "digital")
}

def holdDown() {
    buttonEvent(6, "held", "digital")
}

def releaseUp() {
    buttonEvent(7, "pushed", "digital")
}

def releaseDown() {
    buttonEvent(7, "held", "digital")
}

def pressConfigX1() {
    buttonEvent(8, "pushed", "digital")
}

def pressConfigX2() {
    buttonEvent(9, "pushed", "digital")
}

def pressConfigX3() {
    buttonEvent(10, "pushed", "digital")
}

def pressConfigX4() {
    buttonEvent(11, "pushed", "digital")
}

def pressConfigX5() {
    buttonEvent(12, "pushed", "digital")
}

def holdConfig() {
    buttonEvent(13, "held", "digital")
}

def releaseConfig() {
    buttonEvent(14, "released", "digital")
}

/**
 *  -----------------------------------------------------------------------------
 *  Everything below here are LIBRARY includes and should NOT be edited manually!
 *  -----------------------------------------------------------------------------
 *  --- Nothings to edit here, move along! --------------------------------------
 *  -----------------------------------------------------------------------------
 *  --- user by xiaomi ----------------------------------------------------------
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
String mark(s)      { return "<mark>$s</u>" }
String strike(s)    { return "<s>$s</s>" }
String underline(s) { return "<u>$s</u>" }

String hue(h,s) { 
    h = Math.min(Math.max((h!=null?h:170),1),255)    //170 is Inovelli factory default blue
    if (h==255) s = '<font style="background-color:Gray;color:White;"> ' + s + ' </font>'
    else        s = '<font color="' + hubitat.helper.ColorUtils.rgbToHEX(hubitat.helper.ColorUtils.hsvToRGB([(h/255*100), 100, 100])) + '">' + s + '</font>'
    return s
}

//Reds
String indianRed(s) { return '<font color = "IndianRed">' + s + '</font>'}
String lightCoral(s) { return '<font color = "LightCoral">' + s + '</font>'}
String crimson(s) { return '<font color = "Crimson">' + s + '</font>'}
String red(s) { return '<font color = "Red">' + s + '</font>'}
String fireBrick(s) { return '<font color = "FireBrick">' + s + '</font>'}
String coral(s) { return '<font color = "Coral">' + s + '</font>'}

//Oranges
String orangeRed(s) { return '<font color = "OrangeRed">' + s + '</font>'}
String darkOrange(s) { return '<font color = "DarkOrange">' + s + '</font>'}
String orange(s) { return '<font color = "Orange">' + s + '</font>'}

//Yellows
String gold(s) { return '<font color = "Gold">' + s + '</font>'}
String yellow(s) { return '<font color = "yellow">' + s + '</font>'}
String paleGoldenRod(s) { return '<font color = "PaleGoldenRod">' + s + '</font>'}
String peachPuff(s) { return '<font color = "PeachPuff">' + s + '</font>'}
String darkKhaki(s) { return '<font color = "DarkKhaki">' + s + '</font>'}

//Greens
String limeGreen(s) { return '<font color = "LimeGreen">' + s + '</font>'}
String green(s) { return '<font color = "green">' + s + '</font>'}
String darkGreen(s) { return '<font color = "DarkGreen">' + s + '</font>'}
String olive(s) { return '<font color = "Olive">' + s + '</font>'}
String darkOliveGreen(s) { return '<font color = "DarkOliveGreen">' + s + '</font>'}
String lightSeaGreen(s) { return '<font color = "LightSeaGreen">' + s + '</font>'}
String darkCyan(s) { return '<font color = "DarkCyan">' + s + '</font>'}
String teal(s) { return '<font color = "Teal">' + s + '</font>'}

//Blues
String cyan(s) { return '<font color = "Cyan">' + s + '</font>'}
String lightSteelBlue(s) { return '<font color = "LightSteelBlue">' + s + '</font>'}
String steelBlue(s) { return '<font color = "SteelBlue">' + s + '</font>'}
String lightSkyBlue(s) { return '<font color = "LightSkyBlue">' + s + '</font>'}
String deepSkyBlue(s) { return '<font color = "DeepSkyBlue">' + s + '</font>'}
String dodgerBlue(s) { return '<font color = "DodgerBlue">' + s + '</font>'}
String blue(s) { return '<font color = "blue">' + s + '</font>'}
String midnightBlue(s) { return '<font color = "midnightBlue">' + s + '</font>'}

//Purples
String magenta(s) { return '<font color = "Magenta">' + s + '</font>'}
String rebeccaPurple(s) { return '<font color = "RebeccaPurple">' + s + '</font>'}
String blueViolet(s) { return '<font color = "BlueViolet">' + s + '</font>'}
String slateBlue(s) { return '<font color = "SlateBlue">' + s + '</font>'}
String darkSlateBlue(s) { return '<font color = "DarkSlateBlue">' + s + '</font>'}

//Browns
String burlywood(s) { return '<font color = "Burlywood">' + s + '</font>'}
String goldenrod(s) { return '<font color = "Goldenrod">' + s + '</font>'}
String darkGoldenrod(s) { return '<font color = "DarkGoldenrod">' + s + '</font>'}
String sienna(s) { return '<font color = "Sienna">' + s + '</font>'}

//Grays
String lightGray(s) { return '<font color = "LightGray">' + s + '</font>'}
String gray(s) { return '<font color = "Gray">' + s + '</font>'}
String dimGray(s) { return '<font color = "DimGray">' + s + '</font>'}
String slateGray(s) { return '<font color = "SlateGray">' + s + '</font>'}
String black(s) { return '<font color = "Black">' + s + '</font>'}

//**********************************************************************************
//****** End of HTML enhancement functions.
//**********************************************************************************
