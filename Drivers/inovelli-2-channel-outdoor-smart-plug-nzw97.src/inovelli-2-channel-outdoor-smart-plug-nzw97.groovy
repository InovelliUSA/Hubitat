/**
 *  Inovelli 2-Channel Outdoor Smart Plug NZW97
 *  Author: Eric Maycock (erocm123)
 *  Date: 2020-08-25
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
 *  2020-08-25: Cleanup SwitchBinaryReport for root endpoint.
 *
 *  2020-06-26: Specify which command class versions to use. Remove extra commands. Switch to using Hubitat built in child drivers.
 *
 *  2019-11-20: Fixed Association Group management.
 *
 *  2018-05-02: Added support for Z-Wave Association Tool SmartApp. Associations require firmware 1.02+.
 *              https://github.com/erocm123/SmartThingsPublic/tree/master/smartapps/erocm123/z-waveat
 */
 
metadata {
    definition(
        name: "Inovelli 2-Channel Outdoor Smart Plug NZW97", 
        namespace: "InovelliUSA", 
        author: "Eric Maycock",
        importUrl: "https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-2-channel-outdoor-smart-plug-nzw97.src/inovelli-2-channel-outdoor-smart-plug-nzw97.groovy"
    ) {
        capability "Actuator"
        capability "Switch"
        capability "Refresh"
        capability "Configuration"
        
        attribute "lastActivity", "String"
        attribute "lastEvent", "String"
        
        command "setAssociationGroup", [[name: "Group Number*",type:"NUMBER", description: "Provide the association group number to edit"], 
                                        [name: "Z-Wave Node*", type:"STRING", description: "Enter the node number (in hex) associated with the node"], 
                                        [name: "Action*", type:"ENUM", constraints: ["Add", "Remove"]],
                                        [name:"Multi-channel Endpoint", type:"NUMBER", description: "Currently not implemented"]] 
                
        fingerprint mfr: "015D", prod: "6100", deviceId: "6100", deviceJoinName: "Inovelli 2-Channel Outdoor Smart Plug"
        fingerprint mfr: "0312", prod: "6100", deviceId: "6100", inClusters: "0x5E,0x25,0x85,0x8E,0x59,0x55,0x86,0x72,0x5A,0x73,0x70,0x71,0x9F,0x60,0x6C,0x7A", deviceJoinName: "Inovelli 2-Channel Outdoor Smart Plug"
        fingerprint mfr: "015D", prod: "0221", deviceId: "611C", deviceJoinName: "Inovelli 2-Channel Outdoor Smart Plug"
        fingerprint mfr: "0312", prod: "0221", deviceId: "611C", deviceJoinName: "Inovelli 2-Channel Outdoor Smart Plug"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x25,0x27,0x85,0x8E,0x59,0x55,0x86,0x72,0x5A,0x73,0x70,0x71,0x60,0x6C,0x7A"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x25,0x85,0x8E,0x59,0x55,0x86,0x72,0x5A,0x73,0x70,0x71,0x60,0x6C,0x7A"
    }
    
    simulator {}
    
    // TODO:
    // Association Group 2 - Left outlet
    // Association Group 3 - Right Outlet
    
    preferences {
        input "autoOff1", "number", title: "Auto Off Channel 1", description: "Automatically turn switch off after this number of seconds.\nRange: 0 to 32767", required: false, range: "0..32767"
        input "autoOff2", "number", title: "Auto Off Channel 2", description: "Automatically turn switch off after this number of seconds.\nRange: 0 to 32767", required: false, range: "0..32767"
        input "ledIndicator", "enum", title: "LED Indicator", description: "Turn LED indicator on when switch is:", required: false, options:[["0": "On"], ["1": "Off"], ["2": "Disable"]], defaultValue: "0"
        input description: "Use the \"Z-Wave Association Tool\" SmartApp to set device associations. (Firmware 1.02+)\n\nGroup 2: Sends on/off commands to associated devices when switch is pressed (BASIC_SET).", title: "Associations", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input name: "debugEnable", type: "bool", title: "Enable debug logging", defaultValue: false
        input name: "infoEnable", type: "bool", title: "Enable informational logging", defaultValue: true
    }
}

private getCommandClassVersions() {
	[
     0x20: 1, // Basic
     0x25: 1, // Switch Binary
     0x70: 2, // Configuration
     0x60: 3, // Multi Channel
     0x8E: 2, // Multi Channel Association
     0x72: 2, // Manufacturer Specific
     0x85: 2, // Association
     0x86: 1, // Version
        
     0x5A: 1, // DeviceResetLocally        
     0x5E: 1, // ZwaveplusInfo  
     0x7A: 4, // Firmware Update Meta Data
     0x59: 1, // AssociationGrpInfo   
     0x9F: 1, // Security 2 
    ]
}


def parse(String description) {
    def result = []
    def cmd = zwave.parse(description, commandClassVersions)
    if (cmd) {
        result += zwaveEvent(cmd)
        logDebug "Parsed ${cmd} to ${result.inspect()}"
    } else {
        logDebug "Non-parsed event: ${description}"
    }
    
    def now
    if(location.timeZone)
         now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    else
         now = new Date().format("yyyy MMM dd EEE h:mm:ss a")
    sendEvent(name: "lastActivity", value: now, displayed:false)
    
    return result
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd, ep = null) {
    logDebug "BasicReport ${cmd} - ep ${ep}"
    def value = cmd.value ? "on" : "off"
    if (ep) {
        def event
        childDevices.each {
            childDevice ->
                if (childDevice.deviceNetworkId == "$device.deviceNetworkId-ep$ep") {
                    // Send the event to the child device for processing/logging
                    childDevice.parse([[name:"switch", value:value,  descriptionText:"${childDevice.displayName} was turned ${value}"]])
                }
        }
        if (cmd.value) {
            event = [createEvent([name: "switch", value: "on"])]
        } else {
            def allOff = true
            childDevices.each {
                n ->
                    if (n.currentState("switch").value != "off") allOff = false
            }
            if (allOff) {
                event = [createEvent([name: "switch", value: "off"])]
            } else {
                event = [createEvent([name: "switch", value: "on"])]
            }
        }
        return event
    }
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep = null) {
    logDebug "SwitchBinaryReport ${cmd} - ep ${ep}"
    def value = cmd.value ? "on" : "off"
    if (ep) {
        def event
        def childDevice = childDevices.find {
            it.deviceNetworkId == "$device.deviceNetworkId-ep$ep"
        }
        if (childDevice) {
            // Send the event to the child device for processing/logging
            childDevice.parse([[name:"switch", value:value,  descriptionText:"${childDevice.displayName} was turned ${value}"]])
        }
        else
        {
            log.error("Unable to find child device $device.deviceNetworkId-ep$ep")
        }
    } else {
        logInfo("${device.label?device.label:device.name} is ${value}")
        def result = createEvent(name: "switch", value: value, type: "digital")
        return [result] // returns the result of createEvent()
    }
}

def zwaveEvent(hubitat.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    logDebug "MultiChannelCmdEncap ${cmd}"
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
    }
}

def zwaveEvent(hubitat.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    logDebug "ManufacturerSpecificReport ${cmd}"
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    logDebug "msr: $msr"
    updateDataValue("MSR", msr)
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    logDebug "Unhandled Event: ${cmd}"
}

def on() {
    logDebug "on()"
    commands([
            // Send a single command to the root device to turn both relays on/off.  This prevents
            // the device from sending toggling information for each endpoint back as 
            zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF)
    ])
}

def off() {
    logDebug "off()"
    return commands([
            zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00)
    ])
}

// Called from child component devices
def componentOn(cd) {
    logDebug "${device.label?device.label:device.name}: componentOn($cd)"
    sendHubCommandForChild( zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF), channelNumber(cd.deviceNetworkId) )
}

def componentOff(cd) {
    logDebug "${device.label?device.label:device.name}: componentOff($cd)"
    sendHubCommandForChild( zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00), channelNumber(cd.deviceNetworkId) )
}

def componentRefresh(cd) {
    logDebug "${device.label?device.label:device.name}: componentRefresh($cd)"
    sendHubCommandForChild( zwave.switchBinaryV1.switchBinaryGet(), channelNumber(cd.deviceNetworkId) )
}

def sendHubCommandForChild(cmd, ep) {
    if (ep && ( ep == 1 || ep == 2 )) {
        def cmds = []
        cmds << new hubitat.device.HubAction(command(encap(cmd, ep)), hubitat.device.Protocol.ZWAVE)
        logDebug("sendHubCommandForChild() sending cmds: ${cmds}")
        sendHubCommand(cmds)
    }
    else
    {
        log.error("sendHubCommandForChild() invalid endpoint ${ep}")
    }
}

def refresh() {
    logDebug "refresh()"
    commands([
            // Update each relay individually plus the root device
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
            zwave.switchBinaryV1.switchBinaryGet()
    ])
}

def ping() {
    logDebug "ping()"
    refresh()
}

def installed() {
    refresh()
}

def configure() {
    logDebug "configure()"
    def cmds = initialize()
    commands(cmds)
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
        logDebug "Error: integer2Cmd $e Value: $value"
    }
}

def updated() {
    if (!state.lastRan || now() >= state.lastRan + 2000) {
        logDebug "updated()"
        state.lastRan = now()
        def cmds = initialize()
        commands(cmds)
    } else {
        logDebug "updated() ran within the last 2 seconds. Skipping execution."
    }
}

def initialize() {
    logDebug "initialize()"
    if (!childDevices) {
        createChildDevices()
    } else if (device.label != state.oldLabel) {
        childDevices.each {
            if (it.label == "${state.oldLabel} (CH${channelNumber(it.deviceNetworkId)})") {
                def newLabel = "${device.displayName} (CH${channelNumber(it.deviceNetworkId)})"
                it.setLabel(newLabel)
            }
        }
        state.oldLabel = device.label
    }

    def cmds = processAssociations()
    cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: ledIndicator!=null? ledIndicator.toInteger() : 0, parameterNumber: 1, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: autoOff1!=null? integer2Cmd(autoOff1.toInteger(), 2) : integer2Cmd(0,2), parameterNumber: 2, size: 2)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 2)
    cmds << zwave.configurationV1.configurationSet(configurationValue: autoOff2!=null? integer2Cmd(autoOff2.toInteger(), 2) : integer2Cmd(0,2), parameterNumber: 3, size: 2)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 3)
    
    cmds << zwave.versionV1.versionGet()
    cmds << zwave.firmwareUpdateMdV2.firmwareMdGet()   
    cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()

    return cmds
}

def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
    logDebug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
}

private encap(cmd, endpoint) {
    if (endpoint) {
        zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint).encapsulate(cmd)
    } else {
        cmd
    }
}

private command(hubitat.zwave.Command cmd) {
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay = 500) {
    delayBetween(commands.collect {
        command(it)
    }, delay)
}

private channelNumber(String dni) {
    dni.split("-ep")[-1] as Integer
}
private void createChildDevices() {
    state.oldLabel = device.label
    for (i in 1..2) {
        addChildDevice("hubitat", "Generic Component Switch", "${device.deviceNetworkId}-ep${i}", [completedSetup: true, label: "${device.displayName} (CH${i})",
            isComponent: false, componentName: "ep$i", componentLabel: "Channel $i"
        ])
    }
}

def setDefaultAssociations() {
    def smartThingsHubID = String.format('%02x', zwaveHubNodeId).toUpperCase()
    state.defaultG1 = [smartThingsHubID]
    state.defaultG2 = []
    state.defaultG3 = []
}

def setAssociationGroup(group, nodes, action, endpoint = null){
    // Normalize the arguments to be backwards compatible with the old method
    action = "${action}" == "1" ? "Add" : "${action}" == "0" ? "Remove" : "${action}" // convert 1/0 to Add/Remove
    group  = "${group}" =~ /\d+/ ? (group as int) : group                             // convert group to int (if possible)
    nodes  = [] + nodes ?: [nodes]                                                    // convert to collection if not already a collection

    if (! nodes.every { it =~ /[0-9A-F]+/ }) {
        log.error "invalid Nodes ${nodes}"
        return
    }

    if (group < 1 || group > maxAssociationGroup()) {
        log.error "Association group is invalid 1 <= ${group} <= ${maxAssociationGroup()}"
        return
    }
    
    def associations = state."desiredAssociation${group}"?:[]
    nodes.each { 
        node = "${it}"
        switch (action) {
            case "Remove":
            logDebug "Removing node ${node} from association group ${group}"
            associations = associations - node
            break
            case "Add":
            logDebug "Adding node ${node} to association group ${group}"
            associations << node
            break
        }
    }
    state."desiredAssociation${group}" = associations.unique()
    return
}

def maxAssociationGroup(){
   if (!state.associationGroups) {
       if (logEnable) logDebug "Getting supported association groups from device"
       zwave.associationV2.associationGroupingsGet() // execute the update immediately
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
                logDebug "Adding node $it to group $i"
                cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:hubitat.helper.HexUtils.hexStringToInt(it))
                refreshGroup = true
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                logDebug "Removing node $it from group $i"
                cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:hubitat.helper.HexUtils.hexStringToInt(it))
                refreshGroup = true
            }
            if (refreshGroup == true) cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
            else logDebug "There are no association actions to complete for group $i"
         }
      } else {
         logDebug "Association info not known for group $i. Requesting info from device."
         cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
      }
   }
   return cmds
}

void zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
    def temp = []
    if (cmd.nodeId != []) {
       cmd.nodeId.each {
          temp += it.toString().format( '%02x', it.toInteger() ).toUpperCase()
       }
    } 
    state."actualAssociation${cmd.groupingIdentifier}" = temp
    logDebug "Associations for Group ${cmd.groupingIdentifier}: ${temp}"
    updateDataValue("associationGroup${cmd.groupingIdentifier}", "$temp")
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    logDebug "Supported association groups: ${cmd.supportedGroupings}"
    state.associationGroups = cmd.supportedGroupings
    createEvent(name: "groups", value: cmd.supportedGroupings)
}

void zwaveEvent(hubitat.zwave.commands.versionv1.VersionReport cmd) {
    logDebug cmd
    if(cmd.applicationVersion && cmd.applicationSubVersion) {
	    def firmware = "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2,'0')}"
        updateDataValue("firmware", firmware)
    }
}

private logDebug(msg) {
    if (debugEnable) log.debug(msg)
}

private logInfo(msg) {
    if (infoEnable) log.info(msg)
}
