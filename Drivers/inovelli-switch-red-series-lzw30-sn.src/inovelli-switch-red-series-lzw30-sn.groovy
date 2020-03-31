/**
 *  Inovelli Switch Red Series
 *  Author: Eric Maycock (erocm123)
 *  Date: 2020-03-31
 *
 *  Copyright 2020 Eric Maycock / Inovelli
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
 *  2020-03-27: Adding additional duration options for Notifications. Also adding the commands  
 *              startNotification(value) and stopNotification() to be used in apps like
 *              WebCoRE or Rule Machine to directly control notifications instead of through child Devices.
 *              To determine "value": https://nathanfiscus.github.io/inovelli-notification-calc/
 *
 *  2020-02-25: Switch over to using Hubitat child device drivers. Should still be backwards compatible with
 *              Inovelli child drivers.
 * 
 *  2020-02-07: Update preferences when user changes parameter or disables relay from switch or from child device.
 *
 *  2020-02-06: Fix for remote control child device being created when it shouldn't be.
 *              Fix for local protection being updated via hub after being changed with config button.
 *
 *  2020-02-05: Fix for LED turning off after 3 seconds when LED intensity (when off) is set to 0.
 *              Extra button event added for those that want to distinguish held vs pushed. 
 *              Button 8 pushed = Up button held. Button 8 held = Down button held.
 *              Button 6 pushed = Up button released. Button 6 pushed = Down button released. 
 *
 *  2020-01-28: Update VersionReport parsing because of Hubitat change. Removing unnecessary reports.
 *
 *  2019-12-03: Specify central scene command class version for upcoming Hubitat update.
 *
 *  2019-10-15: Ability to create child devices for local & rf protection to use in various automations.
 *              Device label is now displayed in logging. 
 * 
 *  2019-10-01: Adding the ability to set a custom color for the RGB indicator. Use a hue 360 color wheel.
 *              Adding the ability to enable z-wave "rf protection" to disable control from z-wave commands.
 *
 */
 
metadata {
    definition (name: "Inovelli Switch Red Series LZW30-SN", namespace: "InovelliUSA", author: "Eric Maycock", vid: "generic-switch", importUrl: "https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-switch-red-series-lzw30-sn.src/inovelli-switch-red-series-lzw30-sn.groovy") {
        capability "Switch"
        capability "Refresh"
        capability "Actuator"
        capability "Sensor"
        capability "PushableButton"
        capability "HoldableButton"
        capability "Configuration"
        capability "Energy Meter"
        capability "Power Meter"
        
        attribute "lastActivity", "String"
        attribute "lastEvent", "String"
        attribute "firmware", "String"
        /*
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
        command "pressConfig"
        */
        command "childOn", ["string"]
        command "childOff", ["string"]
        command "childRefresh", ["string"]
        command "componentOn"
        command "componentOff"
        command "componentRefresh"
        
        command "reset"
        command "startNotification", ["number", "number"]
        command "stopNotification", ["number"]
        command "setAssociationGroup", ["number", "enum", "number", "number"] // group number, nodes, action (0 - remove, 1 - add), multi-channel endpoint (optional)

        fingerprint mfr: "031E", prod: "0002", model: "0001", deviceJoinName: "Inovelli Switch Red Series" 
        fingerprint deviceId: "0x1001", inClusters: "0x5E,0x6C,0x55,0x98,0x9F,0x22,0x70,0x85,0x59,0x86,0x32,0x72,0x5A,0x5B,0x73,0x75,0x7A"
        fingerprint deviceId: "0x1001", inClusters: "0x5E,0x70,0x85,0x59,0x55,0x86,0x72,0x5A,0x73,0x32,0x5B,0x98,0x9F,0x25,0x6C,0x75,0x22,0x7A"
    }

    simulator {
    }
    
    preferences {
        generate_preferences()
    }
}

def generate_preferences()
{
    getParameterNumbers().each { i ->
        
        switch(getParameterInfo(i, "type"))
        {   
            case "number":
                input "parameter${i}", "number",
                    title:getParameterInfo(i, "name") + "\n" + getParameterInfo(i, "description") + "\nRange: " + getParameterInfo(i, "options") + "\nDefault: " + getParameterInfo(i, "default"),
                    range: getParameterInfo(i, "options")
                    //defaultValue: getParameterInfo(i, "default")
            break
            case "enum":
                input "parameter${i}", "enum",
                    title:getParameterInfo(i, "name") + "\n" + getParameterInfo(i, "description"), 
                    //defaultValue: getParameterInfo(i, "default"),
                    options: getParameterInfo(i, "options")
            break
        }
        if (i == 5){
           input "parameter5custom", "number", 
               title: "Custom LED RGB Value", 
               description: "\nInput a custom value in this field to override the above setting. The value should be between 0 - 360 and can be determined by using the typical hue color wheel.", 
               required: false,
               range: "0..360"
        }
    }
    
    input description: "When each notification set (Color, Level, Duration, Type) is configured, a switch child device is created that can be used in SmartApps to activate that notification.", title: "LED Notifications", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    
    [1,2,3,4,5].each { i ->
                input "parameter8-${i}a", "enum", title: "LED Effect Color - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    0:"Red",
                    21:"Orange",
                    42:"Yellow",
                    85:"Green",
                    127:"Cyan",
                    170:"Blue",
                    212:"Violet",
                    234:"Pink"]
                input "parameter8-${i}b", "enum", title: "LED Effect Level - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    0:"0%",
                    1:"10%",
                    2:"20%",
                    3:"30%",
                    4:"40%",
                    5:"50%",
                    6:"60%",
                    7:"70%",
                    8:"80%",
                    9:"90%",
                    10:"100%"]
                input "parameter8-${i}c", "enum", title: "LED Effect Duration - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    255:"Indefinitely",
                    1:"1 Second",
                    2:"2 Seconds",
                    3:"3 Seconds",
                    4:"4 Seconds",
                    5:"5 Seconds",
                    6:"6 Seconds",
                    7:"7 Seconds",
                    8:"8 Seconds",
                    9:"9 Seconds",
                    10:"10 Seconds",
                    11:"11 Seconds",
                    12:"12 Seconds",
                    13:"13 Seconds",
                    14:"14 Seconds",
                    15:"15 Seconds",
                    16:"16 Seconds",
                    17:"17 Seconds",
                    18:"18 Seconds",
                    19:"19 Seconds",
                    20:"20 Seconds",
                    21:"21 Seconds",
                    22:"22 Seconds",
                    23:"23 Seconds",
                    24:"24 Seconds",
                    25:"25 Seconds",
                    26:"26 Seconds",
                    27:"27 Seconds",
                    28:"28 Seconds",
                    29:"29 Seconds",
                    30:"30 Seconds",
                    31:"31 Seconds",
                    32:"32 Seconds",
                    33:"33 Seconds",
                    34:"34 Seconds",
                    35:"35 Seconds",
                    36:"36 Seconds",
                    37:"37 Seconds",
                    38:"38 Seconds",
                    39:"39 Seconds",
                    40:"40 Seconds",
                    41:"41 Seconds",
                    42:"42 Seconds",
                    43:"43 Seconds",
                    44:"44 Seconds",
                    45:"45 Seconds",
                    46:"46 Seconds",
                    47:"47 Seconds",
                    48:"48 Seconds",
                    49:"49 Seconds",
                    50:"50 Seconds",
                    51:"51 Seconds",
                    52:"52 Seconds",
                    53:"53 Seconds",
                    54:"54 Seconds",
                    55:"55 Seconds",
                    56:"56 Seconds",
                    57:"57 Seconds",
                    58:"58 Seconds",
                    59:"59 Seconds",
                    61:"1 Minute",
                    62:"2 Minutes",
                    63:"3 Minutes",
                    64:"4 Minutes",
                    65:"5 Minutes",
                    66:"6 Minutes",
                    67:"7 Minutes",
                    68:"8 Minutes",
                    69:"9 Minutes",
                    70:"10 Minutes",
                    71:"11 Minutes",
                    72:"12 Minutes",
                    73:"13 Minutes",
                    74:"14 Minutes",
                    75:"15 Minutes",
                    76:"16 Minutes",
                    77:"17 Minutes",
                    78:"18 Minutes",
                    79:"19 Minutes",
                    80:"20 Minutes",
                    81:"21 Minutes",
                    82:"22 Minutes",
                    83:"23 Minutes",
                    84:"24 Minutes",
                    85:"25 Minutes",
                    86:"26 Minutes",
                    87:"27 Minutes",
                    88:"28 Minutes",
                    89:"29 Minutes",
                    90:"30 Minutes",
                    91:"31 Minutes",
                    92:"32 Minutes",
                    93:"33 Minutes",
                    94:"34 Minutes",
                    95:"35 Minutes",
                    96:"36 Minutes",
                    97:"37 Minutes",
                    98:"38 Minutes",
                    99:"39 Minutes",
                    100:"40 Minutes",
                    101:"41 Minutes",
                    102:"42 Minutes",
                    103:"43 Minutes",
                    104:"44 Minutes",
                    105:"45 Minutes",
                    106:"46 Minutes",
                    107:"47 Minutes",
                    108:"48 Minutes",
                    109:"49 Minutes",
                    110:"50 Minutes",
                    111:"51 Minutes",
                    112:"52 Minutes",
                    113:"53 Minutes",
                    114:"54 Minutes",
                    115:"55 Minutes",
                    116:"56 Minutes",
                    117:"57 Minutes",
                    118:"58 Minutes",
                    119:"59 Minutes",
                    121:"1 Hour",
                    122:"2 Hours",
                    123:"3 Hours",
                    124:"4 Hours",
                    125:"5 Hours",
                    126:"6 Hours",
                    127:"7 Hours",
                    128:"8 Hours",
                    129:"9 Hours",
                    130:"10 Hours",
                    131:"11 Hours",
                    132:"12 Hours",
                    133:"13 Hours",
                    134:"14 Hours",
                    135:"15 Hours",
                    136:"16 Hours",
                    137:"17 Hours",
                    138:"18 Hours",
                    139:"19 Hours",
                    140:"20 Hours",
                    141:"21 Hours",
                    142:"22 Hours",
                    143:"23 Hours",
                    144:"24 Hours",
                    145:"25 Hours",
                    146:"26 Hours",
                    147:"27 Hours",
                    148:"28 Hours",
                    149:"29 Hours",
                    150:"30 Hours",
                    151:"31 Hours",
                    152:"32 Hours",
                    153:"33 Hours",
                    154:"34 Hours",
                    155:"35 Hours",
                    156:"36 Hours",
                    157:"37 Hours",
                    158:"38 Hours",
                    159:"39 Hours",
                    160:"40 Hours",
                    161:"41 Hours",
                    162:"42 Hours",
                    163:"43 Hours",
                    164:"44 Hours",
                    165:"45 Hours",
                    166:"46 Hours",
                    167:"47 Hours",
                    168:"48 Hours",
                    169:"49 Hours",
                    170:"50 Hours",
                    171:"51 Hours",
                    172:"52 Hours",
                    173:"53 Hours",
                    174:"54 Hours",
                    175:"55 Hours",
                    176:"56 Hours",
                    177:"57 Hours",
                    178:"58 Hours",
                    179:"59 Hours",
                    180:"60 Hours",
                    181:"61 Hours",
                    182:"62 Hours",
                    183:"63 Hours",
                    184:"64 Hours",
                    185:"65 Hours",
                    186:"66 Hours",
                    187:"67 Hours",
                    188:"68 Hours",
                    189:"69 Hours",
                    190:"70 Hours",
                    191:"71 Hours",
                    192:"72 Hours",
                    193:"73 Hours",
                    194:"74 Hours",
                    195:"75 Hours",
                    196:"76 Hours",
                    197:"77 Hours",
                    198:"78 Hours",
                    199:"79 Hours",
                    200:"80 Hours",
                    201:"81 Hours",
                    202:"82 Hours",
                    203:"83 Hours",
                    204:"84 Hours",
                    205:"85 Hours",
                    206:"86 Hours",
                    207:"87 Hours",
                    208:"88 Hours",
                    209:"89 Hours",
                    210:"90 Hours",
                    211:"91 Hours",
                    212:"92 Hours",
                    213:"93 Hours",
                    214:"94 Hours",
                    215:"95 Hours",
                    216:"96 Hours",
                    217:"97 Hours",
                    218:"98 Hours",
                    219:"99 Hours",
                    220:"100 Hours",
                    221:"101 Hours",
                    222:"102 Hours",
                    223:"103 Hours",
                    224:"104 Hours",
                    225:"105 Hours",
                    226:"106 Hours",
                    227:"107 Hours",
                    228:"108 Hours",
                    229:"109 Hours",
                    230:"110 Hours",
                    231:"111 Hours",
                    232:"112 Hours",
                    233:"113 Hours",
                    234:"114 Hours",
                    235:"115 Hours",
                    236:"116 Hours",
                    237:"117 Hours",
                    238:"118 Hours",
                    239:"119 Hours",
                    240:"120 Hours",
                    241:"121 Hours",
                    242:"122 Hours",
                    243:"123 Hours",
                    244:"124 Hours",
                    245:"125 Hours",
                    246:"126 Hours",
                    247:"127 Hours",
                    248:"128 Hours",
                    249:"129 Hours",
                    250:"130 Hours",
                    251:"131 Hours",
                    252:"132 Hours",
                    253:"133 Hours",
                    254:"134 Hours"]
                input "parameter8-${i}d", "enum", title: "LED Effect Type - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    0:"Off",
                    1:"Solid",
                    //2:"Chase",
                    2:"Fast Blink",
                    3:"Slow Blink",
                    4:"Pulse"]
    
    }
    input "disableLocal", "enum", title: "Disable Local Control", description: "\nDisable ability to control switch from the wall", required: false, options:[["1": "Yes"], ["0": "No"]], defaultValue: "0"
    input "disableRemote", "enum", title: "Disable Remote Control", description: "\nDisable ability to control switch from inside Hubitat", required: false, options:[["1": "Yes"], ["0": "No"]], defaultValue: "0"
    input description: "Use the below options to enable child devices for the specified settings. This will allow you to adjust these settings using SmartApps such as Smart Lighting. If any of the options are enabled, make sure you have the appropriate child device handlers installed.\n(Firmware 1.02+)", title: "Child Devices", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    input "enableDisableLocalChild", "bool", title: "Disable Local Control", description: "", required: false, defaultValue: false
    input "enableDisableRemoteChild", "bool", title: "Disable Remote Control", description: "", required: false, defaultValue: false
    input name: "debugEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    input name: "infoEnable", type: "bool", title: "Enable informational logging", defaultValue: true
}

private channelNumber(String dni) {
    dni.split("-ep")[-1] as Integer
}

private sendAlert(data) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: Error while creating child device"
    sendEvent(
        descriptionText: data.message,
        eventType: "ALERT",
        name: "failedOperation",
        value: "failed",
        displayed: true,
    )
}

private toggleTiles(number, value) {
   for (int i = 1; i <= 5; i++){
       if ("${i}" != number){
           def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep$i"}
           if (childDevice) {         
                childDevice.sendEvent(name: "switch", value: "off")
           }
       } else {
           def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep$i"}
           if (childDevice) {         
                childDevice.sendEvent(name: "switch", value: value)
           }
       }
   }
}

def startNotification(value, ep = null){
    if (infoEnable) log.info "${device.label?device.label:device.name}: startNotification($value)"
    def parameterNumbers = [8]
    def cmds = []
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(value.toInteger(),4), parameterNumber: parameterNumbers[(ep == null)? 0:ep?.toInteger()-1], size: 4)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterNumbers[(ep == null)? 0:ep?.toInteger()-1])
    return commands(cmds)
}

def stopNotification(ep = null){
    if (infoEnable) log.info "${device.label?device.label:device.name}: stopNotification()"
    def parameterNumbers = [8]
    def cmds = []
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(0,4), parameterNumber: parameterNumbers[(ep == null)? 0:ep?.toInteger()-1], size: 4)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterNumbers[(ep == null)? 0:ep?.toInteger()-1])
    return commands(cmds)
}

def childSetLevel(String dni, value) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childSetLevel($dni, $value)"
    def valueaux = value as Integer
    def level = Math.max(Math.min(valueaux, 99), 0)    
    def cmds = []
    switch (channelNumber(dni)) {
        case 101:
            cmds << zwave.protectionV2.protectionSet(localProtectionState : level > 0 ? 1 : 0, rfProtectionState: state.rfProtectionState? state.rfProtectionState:0)
            cmds << zwave.protectionV2.protectionGet()
        break
        case 102:
            cmds << zwave.protectionV2.protectionSet(localProtectionState : state.localProtectionState? state.localProtectionState:0, rfProtectionState : level > 0 ? 1 : 0)
            cmds << zwave.protectionV2.protectionGet()
        break
    }
	return commands(cmds)
}

def childOn(String dni) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childOn($dni)"
    def cmds = []
    if(channelNumber(dni).toInteger() <= 5) {
        toggleTiles("${channelNumber(dni)}", "on")
        cmds << new hubitat.device.HubAction(command(setParameter(8, calculateParameter("8-${channelNumber(dni)}"), 4)), hubitat.device.Protocol.ZWAVE )
        return cmds
    } else {
        childSetLevel(dni, 99)
    }
}

def childOff(String dni) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childOff($dni)"
    def cmds = []
    if(channelNumber(dni).toInteger() <= 5) {
        toggleTiles("${channelNumber(dni)}", "off")
        cmds << new hubitat.device.HubAction(command(setParameter(8, 0, 4)), hubitat.device.Protocol.ZWAVE )
        return cmds
    } else {
        childSetLevel(dni, 0)
    }
}

def componentOn(cd) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: componentOn($cd)"
    return childOn(cd.deviceNetworkId)
}

def componentOff(cd) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: componentOff($cd)"
    return childOff(cd.deviceNetworkId)
}

void componentRefresh(cd) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: componentRefresh($cd)"
}

void childRefresh(String dni) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childRefresh($dni)"
}

def childExists(ep) {
    def children = childDevices
    def childDevice = children.find{it.deviceNetworkId.endsWith(ep)}
    if (childDevice) 
        return true
    else
        return false
}

def installed() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: installed()"
    refresh()
}

def configure() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: configure()"
    def cmds = initialize()
    commands(cmds)
}

def updated() {
    if (!state.lastRan || now() >= state.lastRan + 2000) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: updated()"
        state.lastRan = now()
        def cmds = initialize()
        if (cmds != [])
            commands(cmds, 1000)
        else 
            return null
    } else {
        if (infoEnable) log.info "${device.label?device.label:device.name}: updated() ran within the last 2 seconds. Skipping execution."
    }
}

def initialize() {
    sendEvent(name: "checkInterval", value: 3 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    sendEvent(name: "numberOfButtons", value: 8, displayed: true)
    
    if (enableDisableLocalChild && !childExists("ep101")) {
    try {
        addChildDevice("hubitat", "Generic Component Switch", "${device.deviceNetworkId}-ep101",
                [completedSetup: true, label: "${device.displayName} (Disable Local Control)",
                isComponent: false, componentName: "ep101", componentLabel: "Disable Local Control"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Level Child Device\" is installed"]])
    }
    } else if (!enableDisableLocalChild && childExists("ep101")) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: Trying to delete child device ep101. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
        try {
            if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            if (infoEnable) log.info "Hubitat may have issues trying to delete the child device when it is in use. Need to manually delete them."
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }
    if (enableDisableRemoteChild && !childExists("ep102")) {
    try {
        addChildDevice("hubitat", "Generic Component Switch", "${device.deviceNetworkId}-ep102", 
                [completedSetup: true, label: "${device.displayName} (Disable Remote Control)",
                isComponent: false, componentName: "ep102", componentLabel: "Disable Remote Control"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Level Child Device\" is installed"]])
    }
    } else if (!enableDisableRemoteChild && childExists("ep102")) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: Trying to delete child device ep102. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep102")}
        try {
            if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            if (infoEnable) log.info "Hubitat may have issues trying to delete the child device when it is in use. Need to manually delete them."
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }
    
    [1,2,3,4,5].each { i ->
    if ((settings."parameter8-${i}a"!=null && settings."parameter8-${i}b"!=null && settings."parameter8-${i}c"!=null && settings."parameter8-${i}d"!=null && settings."parameter8-${i}d"!="0") && !childExists("ep${i}")) {
    try {
        addChildDevice("hubitat", "Generic Component Switch", "${device.deviceNetworkId}-ep${i}",
                [completedSetup: true, label: "${device.displayName} (Notification ${i})",
                isComponent: false, componentName: "ep${i}", componentLabel: "Notification ${i}"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Child Device\" is installed"]])
    }
    } else if ((settings."parameter8-${i}a"==null || settings."parameter8-${i}b"==null || settings."parameter8-${i}c"==null || settings."parameter8-${i}d"==null) && childExists("ep${i}" && settings."parameter8-${i}d"=="0")) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: Trying to delete child device ep${i}. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep${i}")}
        try {
            if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            if (infoEnable) log.info "Hubitat may have issues trying to delete the child device when it is in use. Need to manually delete them."
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }}
    if (device.label != state.oldLabel) {
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep1")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 1)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep2")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 2)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep3")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 3)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep4")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 4)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep5")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 5)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Disable Local Control)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep102")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Disable Remote Control)")
        state.oldLabel = device.label
    }
    
    def cmds = processAssociations()
    
    getParameterNumbers().each{ i ->
      if ((state."parameter${i}value" != ((settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger()))){
          cmds << setParameter(i, (settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger(), getParameterInfo(i, "size").toInteger())
          cmds << getParameter(i)
      }
      else {
          //if (infoEnable) log.info "${device.label?device.label:device.name}: Parameter already set"
      }
    }
    
    if (state."parameter9value" != 0){
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 0, parameterNumber: 9, size: 1)
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 9)
    }
    
    cmds << zwave.versionV1.versionGet()
    
    if (state.localProtectionState?.toInteger() != settings.disableLocal?.toInteger() || state.rfProtectionState?.toInteger() != settings.disableRemote?.toInteger()) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: Protection command class settings need to be updated"
        cmds << zwave.protectionV2.protectionSet(localProtectionState : disableLocal!=null? disableLocal.toInteger() : 0, rfProtectionState: disableRemote!=null? disableRemote.toInteger() : 0)
        cmds << zwave.protectionV2.protectionGet()
    } else {
        if (infoEnable) log.info "${device.label?device.label:device.name}: No Protection command class settings to update"
    }

    if (cmds != []) return cmds else return []
}

def calculateParameter(number) {
    def value = 0
    switch (number){
      case "5":
          if (settings.parameter5custom =~ /^([0-9]{1}|[0-9]{2}|[0-9]{3})$/) value = settings.parameter5custom.toInteger() / 360 * 255
          else value = settings."parameter${number}"
      break
      case "8-1":
      case "8-2":
      case "8-3": 
      case "8-4":
      case "8-5":
         value += settings."parameter${number}a"!=null ? settings."parameter${number}a".toInteger() * 1 : 0
         value += settings."parameter${number}b"!=null ? settings."parameter${number}b".toInteger() * 256 : 0
         value += settings."parameter${number}c"!=null ? settings."parameter${number}c".toInteger() * 65536 : 0
         value += settings."parameter${number}d"!=null ? settings."parameter${number}d".toInteger() * 16777216 : 0
      break
      default:
          value = settings."parameter${number}"
      break
    }
    return value
}

def setParameter(number, value, size) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: Setting parameter $number with a size of $size bytes to $value"
    return zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(value.toInteger(),size), parameterNumber: number, size: size)
}

def getParameter(number) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: Retreiving value of parameter $number"
    return zwave.configurationV1.configurationGet(parameterNumber: number)
}

def getParameterNumbers(){
    return [1,2,3,4,5,6,7,10,11,12]
}

def getParameterInfo(number, type){
    def parameter = [:]

    parameter.parameter1default=0
    parameter.parameter2default=0
    parameter.parameter3default=0
    parameter.parameter4default=15
    parameter.parameter5default=170
    parameter.parameter6default=5
    parameter.parameter7default=1
    parameter.parameter8default=0
    parameter.parameter9default=0
    parameter.parameter10default=10
    parameter.parameter11default=3600
    parameter.parameter12default=10
    
    parameter.parameter1type="enum"
    parameter.parameter2type="enum"
    parameter.parameter3type="number"
    parameter.parameter4type="number"
    parameter.parameter5type="enum"
    parameter.parameter6type="enum"
    parameter.parameter7type="enum"
    parameter.parameter8type="enum"
    parameter.parameter9type="enum"
    parameter.parameter10type="number"
    parameter.parameter11type="number"
    parameter.parameter12type="number"
    
    parameter.parameter1size=1
    parameter.parameter2size=1
    parameter.parameter3size=2
    parameter.parameter4size=1
    parameter.parameter5size=2
    parameter.parameter6size=1
    parameter.parameter7size=1
    parameter.parameter8size=4
    parameter.parameter9size=1
    parameter.parameter10size=1
    parameter.parameter11size=2
    parameter.parameter12size=1
    
	parameter.parameter1options=["0":"Previous", "1":"On", "2":"Off"]
    parameter.parameter2options=["1":"Yes", "0":"No"]
    parameter.parameter3options="1..32767"
    parameter.parameter4options="0..15"
    parameter.parameter5options=["0":"Red","21":"Orange","42":"Yellow","85":"Green","127":"Cyan","170":"Blue","212":"Violet","234":"Pink"]
    parameter.parameter6options=["0":"0%","1":"10%","2":"20%","3":"30%","4":"40%","5":"50%","6":"60%","7":"70%","8":"80%","9":"90%","10":"100%"]
    parameter.parameter7options=["0":"0%","1":"10%","2":"20%","3":"30%","4":"40%","5":"50%","6":"60%","7":"70%","8":"80%","9":"90%","10":"100%"]
    parameter.parameter8options=["1":"Yes", "2":"No"]
    parameter.parameter9options=["0":"Stay Off","1":"1 Second","2":"2 Seconds","3":"3 Seconds","4":"4 Seconds","5":"5 Seconds","6":"6 Seconds","7":"7 Seconds","8":"8 Seconds","9":"9 Seconds","10":"10 Seconds"]
    parameter.parameter10options="0..100"
    parameter.parameter11options="0..32767"
    parameter.parameter12options="0..100"
    
    parameter.parameter1name="State After Power Restored"
    parameter.parameter2name="Invert Switch"
    parameter.parameter3name="Auto Off Timer"
    parameter.parameter4name="Association Behavior"
    parameter.parameter5name="LED Strip Color"
    parameter.parameter6name="LED Strip Intensity"
    parameter.parameter7name="LED Strip Intensity (When OFF)"
    parameter.parameter8name="LED Strip Effect"
    parameter.parameter9name="LED Strip Timeout"
    parameter.parameter10name="Active Power Reports"
    parameter.parameter11name="Periodic Power & Energy Reports"
    parameter.parameter12name="Energy Reports"
    
    
    parameter.parameter1description="The state the switch should return to once power is restored after power failure."
	parameter.parameter2description="Inverts the orientation of the switch. Useful when the switch is installed upside down. Essentially up becomes down and down becomes up."
    parameter.parameter3description="Automatically turns the switch off after this many seconds. When the switch is turned on a timer is started that is the duration of this setting. When the timer expires, the switch is turned off."
    parameter.parameter4description="When should the switch send commands to associated devices?\n\n01 - local\n02 - 3way\n03 - 3way & local\n04 - z-wave hub\n05 - z-wave hub & local\n06 - z-wave hub & 3-way\n07 - z-wave hub & local & 3way\n08 - timer\n09 - timer & local\n10 - timer & 3-way\n11 - timer & 3-way & local\n12 - timer & z-wave hub\n13 - timer & z-wave hub & local\n14 - timer & z-wave hub & 3-way\n15 - all"
    parameter.parameter5description="This is the color of the LED strip."
    parameter.parameter6description="This is the intensity of the LED strip."
    parameter.parameter7description="This is the intensity of the LED strip when the switch is off. This is useful for users to see the light switch location when the lights are off."
    parameter.parameter8description="LED Strip Effect"
    parameter.parameter9description="When the LED strip is disabled (LED Strip Intensity is set to 0), this setting allows the LED strip to turn on temporarily while being adjusted."
    parameter.parameter10description="The power level change that will result in a new power report being sent. The value is a percentage of the previous report. 0 = disabled."
    parameter.parameter11description="Time period between consecutive power & energy reports being sent (in seconds). The timer is reset after each report is sent."
    parameter.parameter12description="The energy level change that will result in a new energy report being sent. The value is a percentage of the previous report."
    
    return parameter."parameter${number}${type}"
}

def zwaveEvent(hubitat.zwave.commands.meterv3.MeterReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    def event
	if (cmd.scale == 0) {
    	if (cmd.meterType == 161) {
		    event = createEvent(name: "voltage", value: cmd.scaledMeterValue, unit: "V")
            if (infoEnable) log.info "${device.label?device.label:device.name}: Voltage report received with value of ${cmd.scaledMeterValue} V"
        } else if (cmd.meterType == 1) {
        	event = createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
            if (infoEnable) log.info "${device.label?device.label:device.name}: Energy report received with value of ${cmd.scaledMeterValue} kWh"
        }
	} else if (cmd.scale == 1) {
		event = createEvent(name: "amperage", value: cmd.scaledMeterValue, unit: "A")
        if (infoEnable) log.info "${device.label?device.label:device.name}: Amperage report received with value of ${cmd.scaledMeterValue} A"
	} else if (cmd.scale == 2) {
		event = createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
        if (infoEnable) log.info "${device.label?device.label:device.name}: Power report received with value of ${cmd.scaledMeterValue} W"
	}

    return event
}

def zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
    if (!state.lastRan || now() <= state.lastRan + 60000) {
        state."parameter${cmd.parameterNumber}value" = cmd2Integer(cmd.configurationValue)
    } else {
        if (infoEnable) log.debug "${device.label?device.label:device.name}: Configuration report received more than 60 seconds after running updated(). Possible configuration made at switch"
    }
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

def integer2Cmd(value, size) {
    try{
	switch(size) {
	case 1:
		[value]
    break
	case 2:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        [value2, value1]
    break
    case 3:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        [value3, value2, value1]
    break
	case 4:
    	def short value1 = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        def short value4 = (value >> 24) & 0xFF
		[value4, value3, value2, value1]
	break
	}
    } catch (e) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: Error: integer2Cmd $e Value: $value"
    }
}

private getCommandClassVersions() {
	[0x20: 1, 0x25: 1, 0x70: 1, 0x98: 1, 0x32: 3, 0x5B: 1]
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        state.sec = 1
        zwaveEvent(encapsulatedCommand)
    }
}

def parse(description) {
    def result = null
    if (description.startsWith("Err 106")) {
        state.sec = 0
        result = createEvent(descriptionText: description, isStateChange: true)
    } else if (description != "updated") {
        def cmd = zwave.parse(description, commandClassVersions)
        if (cmd) {
            result = zwaveEvent(cmd)
            //if (debugEnable) log.debug("'$cmd' parsed to $result")
        } else {
            if (debugEnable) log.debug("Couldn't zwave.parse '$description'")
        }
    }
    def now
    if(location.timeZone)
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    else
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a")
    sendEvent(name: "lastActivity", value: now, displayed:false)
    result
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Basic report received with value of ${cmd.value ? "on" : "off"}"
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Basic set received with value of ${cmd.value ? "on" : "off"}"
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Switch Binary report received with value of ${cmd.value ? "on" : "off"}"
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}

def zwaveEvent(hubitat.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    switch (cmd.keyAttributes) {
       case 0:
       if (cmd.sceneNumber == 3) createEvent(buttonEvent(7, "pushed", "physical"))
       else createEvent(buttonEvent(cmd.keyAttributes + 1, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
       case 1:
       createEvent(buttonEvent(6, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
       case 2:
       createEvent(buttonEvent(8, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
       default:
       createEvent(buttonEvent(cmd.keyAttributes - 1, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
    }
}

def buttonEvent(button, value, type = "digital") {
    if(button != 6)
        sendEvent(name:"lastEvent", value: "${value != 'pushed'?' Tap '.padRight(button+5, '▼'):' Tap '.padRight(button+5, '▲')}", displayed:false)
    else
        sendEvent(name:"lastEvent", value: "${value != 'pushed'?' Hold ▼':' Hold ▲'}", displayed:false)
    if (infoEnable) log.info "${device.label?device.label:device.name}: Button ${button} was ${value}"
    [name: value, value: button, isStateChange:true]
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: Unhandled: $cmd"
    null
}

def reset() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: Resetting energy statistics"
	def cmds = []
    cmds << zwave.meterV2.meterReset()
    cmds << zwave.meterV2.meterGet(scale: 0)
    cmds << zwave.meterV2.meterGet(scale: 2)
	commands(cmds, 1000)
}

def on() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: on()"
    commands([
        zwave.basicV1.basicSet(value: 0xFF)//,
        //zwave.basicV1.basicGet()
    ])
}

def off() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: off()"
    commands([
        zwave.basicV1.basicSet(value: 0x00)//,
        //zwave.basicV1.basicGet()
    ])
}

def ping() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: ping()"
    //refresh()
}

def poll() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: poll()"
    //refresh()
}

def refresh() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: refresh()"
    def cmds = []
    cmds << zwave.basicV1.basicGet()
    cmds << zwave.meterV3.meterGet(scale: 0)
	cmds << zwave.meterV3.meterGet(scale: 2)
    cmds << zwave.protectionV2.protectionGet()
    return commands(cmds)
}

private command(hubitat.zwave.Command cmd) {
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay=500) {
    delayBetween(commands.collect{ command(it) }, delay)
}

def pressUpX1() {
    sendEvent(buttonEvent(1, "pushed"))
}

def pressDownX1() {
    sendEvent(buttonEvent(1, "held"))
}

def pressUpX2() {
    sendEvent(buttonEvent(2, "pushed"))
}

def pressDownX2() {
    sendEvent(buttonEvent(2, "held"))
}

def pressUpX3() {
    sendEvent(buttonEvent(3, "pushed"))
}

def pressDownX3() {
    sendEvent(buttonEvent(3, "held"))
}

def pressUpX4() {
    sendEvent(buttonEvent(4, "pushed"))
}

def pressDownX4() {
    sendEvent(buttonEvent(4, "held"))
}

def pressUpX5() {
    sendEvent(buttonEvent(5, "pushed"))
}

def pressDownX5() {
    sendEvent(buttonEvent(5, "held"))
}

def holdUp() {
    sendEvent(buttonEvent(6, "pushed"))
}

def holdDown() {
    sendEvent(buttonEvent(6, "held"))
}

def pressConfig() {
    sendEvent(buttonEvent(7, "pushed"))
}

def setDefaultAssociations() {
    def smartThingsHubID = (zwaveHubNodeId.toString().format( '%02x', zwaveHubNodeId )).toUpperCase()
    state.defaultG1 = [smartThingsHubID]
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
       if (infoEnable) log.info "${device.label?device.label:device.name}: Getting supported association groups from device"
       cmds <<  zwave.associationV2.associationGroupingsGet()
   }
   for (int i = 1; i <= associationGroups; i++){
      if(state."actualAssociation${i}" != null){
         if(state."desiredAssociation${i}" != null || state."defaultG${i}") {
            def refreshGroup = false
            ((state."desiredAssociation${i}"? state."desiredAssociation${i}" : [] + state."defaultG${i}") - state."actualAssociation${i}").each {
                if (it != null){
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Adding node $it to group $i"
                    cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                    refreshGroup = true
                }
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                if (it != null){
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Removing node $it from group $i"
                    cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                    refreshGroup = true
                }
            }
            if (refreshGroup == true) cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
            else if (infoEnable) log.info "${device.label?device.label:device.name}: There are no association actions to complete for group $i"
         }
      } else {
         if (infoEnable) log.info "${device.label?device.label:device.name}: Association info not known for group $i. Requesting info from device."
         cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
      }
   }
   return cmds
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    def temp = []
    if (cmd.nodeId != []) {
       cmd.nodeId.each {
          temp += it.toString().format( '%02x', it.toInteger() ).toUpperCase()
       }
    } 
    state."actualAssociation${cmd.groupingIdentifier}" = temp
    if (infoEnable) log.info "${device.label?device.label:device.name}: Associations for Group ${cmd.groupingIdentifier}: ${temp}"
    updateDataValue("associationGroup${cmd.groupingIdentifier}", "$temp")
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    sendEvent(name: "groups", value: cmd.supportedGroupings)
    if (infoEnable) log.info "${device.label?device.label:device.name}: Supported association groups: ${cmd.supportedGroupings}"
    state.associationGroups = cmd.supportedGroupings
}

def zwaveEvent(hubitat.zwave.commands.versionv1.VersionReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if(cmd.applicationVersion != null && cmd.applicationSubVersion != null) {
	    def firmware = "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2,'0')}"
        if (infoEnable) log.info "${device.label?device.label:device.name}: Firmware report received: ${firmware}"
        state.needfwUpdate = "false"
        createEvent(name: "firmware", value: "${firmware}")
    } else if(cmd.firmware0Version != null && cmd.firmware0SubVersion != null) {
	    def firmware = "${cmd.firmware0Version}.${cmd.firmware0SubVersion.toString().padLeft(2,'0')}"
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Firmware report received: ${firmware}"
        state.needfwUpdate = "false"
        createEvent(name: "firmware", value: "${firmware}")
    }
}

def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Protection report received: Local protection is ${cmd.localProtectionState > 0 ? "on" : "off"} & Remote protection is ${cmd.rfProtectionState > 0 ? "on" : "off"}"
    state.localProtectionState = cmd.localProtectionState
    state.rfProtectionState = cmd.rfProtectionState
    device.updateSetting("disableLocal",[value:cmd.localProtectionState?"1":"0",type:"enum"])
    device.updateSetting("disableRemote",[value:cmd.rfProtectionState?"1":"0",type:"enum"])
    def children = childDevices
    def childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
    if (childDevice) {
        childDevice.sendEvent(name: "switch", value: cmd.localProtectionState > 0 ? "on" : "off")        
    }
    childDevice = children.find{it.deviceNetworkId.endsWith("ep102")}
    if (childDevice) {
        childDevice.sendEvent(name: "switch", value: cmd.rfProtectionState > 0 ? "on" : "off")        
    }
}
