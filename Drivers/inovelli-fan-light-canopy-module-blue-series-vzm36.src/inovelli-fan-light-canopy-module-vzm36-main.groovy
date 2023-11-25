import hubitat.device.HubAction
import hubitat.device.Protocol

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

import java.security.MessageDigest
import hubitat.helper.HexUtils
import groovy.transform.Field


metadata {
    definition (name: "Inovelli VZM36-SN Zigbee Canopy", namespace: "InovelliUSA", author: "Sven") {
        capability "Initialize"
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Health Check"
        command "GetTemperature"
        command "bindTarget"
         command "GetRssiLQI"
        command "SetFanControl",            [[name: "FanSpeed*", type: "ENUM", constraints: ["off","low","medium","high"]]]
        attribute "lastCheckin", "string"    
        
        fingerprint profileId:"0101", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,0008,       0B05,1000, FC31,FC57", outClusters:"0019",                model:"VZM36", manufacturer:"Inovelli"
        fingerprint profileId:"0101", endpointId:"02", inClusters:"0000,0003,0004,0005,0006,0008,0202,  0B05,1000, FC31", outClusters:"0019",                           model:"VZM36", manufacturer:"Inovelli"
    }
 
}

def configure() {    //THIS GETS CALLED AUTOMATICALLY WHEN NEW DEVICE IS DISCOVERED OR WHEN CONFIGURE BUTTON SELECTED ON DEVICE PAGE
    def cmds = []
    
    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}", "delay ${50}"] //On/Off Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}", "delay ${50}"]
    
    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0008 {${device.zigbeeId}} {}", "delay ${50}"] //Level Control Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0008 {${device.zigbeeId}} {}", "delay ${50}"]
    
    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0xFC31 {${device.zigbeeId}} {}", "delay ${50}"] //Private Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0xFC31 {${device.zigbeeId}} {}", "delay ${50}"] //Private Cluster ep2
 
    return cmds
}


def initialize() {
    logDebug "Initializing..."
 
    setupChildDevices()
}

def installed() {
    logDebug "Parent installed"
}

def updated() {
    logDebug "Parent updated"
}

def bindTarget() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: BindTarget()"
    def cmds = zigbee.command(0x0003, 0x00, [:], "20 00")
    return cmds
}

def convertByteToPercent(int value=0) {                  //Zigbee uses a 0-254 range where 254=100%.  255 is reserved for special meaning.
    value = value==null?0:value                          //default to 0 if null
    value = Math.min(Math.max(value.toInteger(),0),255)  //make sure input byte value is in the 0-255 range
    value = value>=255?256:value                         //this ensures that byte values of 255 get rounded up to 101%
    value = Math.ceil(value/255*100)                     //convert to 0-100 where 254=100% and 255 becomes 101 for special meaning
    return value
}

def convertPercentToByte(int value=0) {                  //Zigbee uses a 0-254 range where 254=100%.  255 is reserved for special meaning.
    value = value==null?0:value                          //default to 0 if null
    value = Math.min(Math.max(value.toInteger(),0),101)  //make sure input percent value is in the 0-101 range
    value = Math.floor(value/100*255)                    //convert to 0-255 where 100%=254 and 101 becomes 255 for special meaning
    value = value==255?254:value                         //this ensures that 100% rounds down to byte value 254
    value = value>255?255:value                          //this ensures that 101% rounds down to byte value 255
    return value
}



// Parse incoming device messages to generate events

void parse(String description) { 
    Map descMap = zigbee.parseDescriptionAsMap(description)
    log.info "${device.label?device.label:device.name}: parse($descMap)"
    try {
        if (debugEnable && (zigbee.getEvent(description)!=[:])) log.debug "${device.label?device.label:device.name}: zigbee.getEvent ${zigbee.getEvent(description)}"
    } catch (e) {
        if (debugEnable) log.debug "${device.label?device.label:device.name}: "+magenta(bold("There was an error while calling zigbee.getEvent: $description"))   
    }
    def attrInt =    descMap.attrInt==null?null:descMap.attrInt.toInteger()
    def attrHex =    descMap.attrInt==null?null:"0x${zigbee.convertToHexString(descMap.attrInt,4)}"
    def clusterHex = descMap.clusterInt==null?null:"0x${zigbee.convertToHexString(descMap.clusterInt,4)}"
    
    def endpointHex = descMap.Endpoint
    
    def strValue =   descMap.value ?: "unknown"
    log.info "endpointInt :${descMap.endpoint}"
    
    if(descMap.endpoint != null)
    {
        switch (descMap.clusterInt){
        case 0x0006:
            if (debugEnable) log.debug "${device.label?device.label:device.name}: ON_OFF CLUSTER $descMap"
            switch (attrInt) {
                case 0x0000:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        def intValue = Integer.parseInt(descMap['value'],16)
                        strValue = intValue == 0? "off": "on"
                        if (infoEnable /*&& (strValue!=device.currentValue("switch"))*/) log.info "${device.label?device.label:device.name}: Report received Switch:\t$intValue ($strValue)"
                        
                        
                        
                        if(descMap.endpoint == "01")
                        {
                          sendEvent(name:"Light", value: strValue)
                            
                            def childId = "${device.id}-0${1}"
                            def existingChild = getChildDevices()?.find { it.deviceNetworkId == childId}
    
                            if (existingChild) {
                               log.info "Child device ${childId} already exists (${existingChild})"
                                existingChild.sendEvent(name:"Light", value: strValue) 
                            } 
                        }
                        else if(descMap.endpoint == "02")
                        {
                             sendEvent(name:"Fan", value: strValue)
                            def childId = "${device.id}-0${2}"
                            def existingChild = getChildDevices()?.find { it.deviceNetworkId == childId}
    
                            if (existingChild) {
                                log.info "Child device ${childId} already exists (${existingChild.currentValue("level")})"
                                existingChild.currentValue("level")
                                existingChild.sendEvent(name:"Fan", value: strValue)
                            } 
                        }
                    }
                    else if (infoEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("Cluster:$clusterHex Attribute:$attrHex UNKNOWN COMMAND:${descMap.command} Value:${descMap.value} Data:${descMap.data}")
                    break
            }
            break
        case 0x0008:
            if (debugEnable) log.debug "${device.label?device.label:device.name}: LEVEL CONTROL CLUSTER $descMap"
            switch (attrInt) {
                case 0x0000:
                    if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                        def intValue = Integer.parseInt(descMap['value'],16)
                        intValue=Math.min(Math.max(intValue.toInteger(),0),254)
                        def percentValue = convertByteToPercent(intValue)
                        strValue = percentValue.toString()+"%"
                       log.info "${device.label?device.label:device.name}:Report received Level:\t${percentValue}"
                        if(descMap.endpoint == "01")
                        {
                            sendEvent(name:"LightLevel", value: percentValue, unit: "%")
                            
                            def childId = "${device.id}-0${1}"
                            def existingChild = getChildDevices()?.find { it.deviceNetworkId == childId}
    
                            if (existingChild) {
                               log.info "Child device ${childId} already exists (${existingChild})"
                                existingChild.sendEvent(name:"level", value: percentValue, unit: "%")
                            } 
                        }
                        else if(descMap.endpoint == "02")
                        {
                            
                            sendEvent(name:"FanLevel", value: percentValue, unit: "%")
                            def childId = "${device.id}-0${2}"
                            def existingChild = getChildDevices()?.find { it.deviceNetworkId == childId}
                            log.info "${device.label?device.label:device.name}: Report received Speed:\t${percentValue}"
                            if (existingChild) {
                                existingChild.sendEvent(name:"level", value: percentValue, unit: "%")
                            } 
                        }
                    }
                    else if (infoEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("Cluster:$clusterHex Attribute:$attrHex UNKNOWN COMMAND:${descMap.command} Value:${descMap.value} Data:${descMap.data}")
                    break
          
                case null:													 
                    if (debugEnable) log.debug "${device.label?device.label:device.name}: Cluster:$clusterHex NULL ATTRIBUTE:$attrHex Command:${descMap.command} Value:${descMap.value} Data:${descMap.data}" 
                    break
                default:
                    if (infoEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("Cluster:$clusterHex UNKNOWN ATTRIBUTE:$attrHex Command:${descMap.command} Value:${descMap.value} Data:${descMap.data}")
                    break
            }
            break
            case 0x0B05:    //ROUTING TABLE CLUSTER
            if (descMap.command == "01" || descMap.command == "0A" || descMap.command == "0B"){
                switch (attrInt) {
                    case 0x011C:
                    def valueInt = Integer.parseInt(descMap['value'],16)
                    sendEvent(name:"LQI", value: valueInt, unit: "%")
                    break
                    case 0x011D:
                    def valueInt = Integer.parseInt(descMap['value'],16)
                    sendEvent(name:"RSSI", value: valueInt - 255, unit: "%")																															  
                    break
                    default:
                        if (infoEnable||debugEnable) log.warn "${device.label?device.label:device.name}: "+fireBrick("${clusterLookup(clusterHex)}(${clusterHex}) UNKNOWN ATTRIBUTE")
                    break  
                }
            }
            
            case 0xfc31:
            log.info "descMap.clusterInt :${descMap.clusterInt}"
            def valueInt = Integer.parseInt(descMap['value'],16)
            def infoDev = "${device.label?device.label:device.name}: "
            def infoTxt = "Receive  attribute ${attrInt.toString().padLeft(3," ")} value ${valueInt.toString().padLeft(3," ")}"
            def infoMsg = infoDev + infoTxt

            switch (attrInt){
                case 0x0020:    
                valueInt = Integer.parseInt(descMap['value'],16)
                sendEvent(name:"Temperature", value: valueInt, unit: "C")
                break

                case 0x0021:  
                
                sendEvent(name:"Overheat", value:valueInt==0?"NO":"YES", displayed:false )
                break

            }
            break
        }

    }
}


def off() {
    log.info "Turn all child switches off"	
    "he cmd 0x${device.deviceNetworkId} 0xFF 0x0006 0x0 {}"
}

def on() {
    log.info "Turn all child switches on"
    "he cmd 0x${device.deviceNetworkId} 0xFF 0x0006 0x1 {}"
}

def refresh() {
	logDebug "refreshing"
    "he rattr 0x${device.deviceNetworkId} 0xFF 0x0006 0x0"
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)d
}

private String getChildId(childDevice) {
    return childDevice.deviceNetworkId.substring(childDevice.deviceNetworkId.length() - 2)
}

def componentOn(childDevice) {
    logDebug "componentOn ${childDevice.deviceNetworkId}"
    sendHubCommand(new HubAction("he cmd 0x${device.deviceNetworkId} 0x${getChildId(childDevice)} 0x0006 0x1 {}", Protocol.ZIGBEE))
}

def componentOff(childDevice) {
    logDebug "componentOff ${childDevice.deviceNetworkId}"
    sendHubCommand(new HubAction("he cmd 0x${device.deviceNetworkId} 0x${getChildId(childDevice)} 0x0006 0x0 {}", Protocol.ZIGBEE))
}

def myintTo8bitUnsignedHex(def value) {  
    def hexString = String.format("%02X", value & 0xFF)  

    return hexString  
}

def myintTo16bitUnsignedHex(int value) {  
    int c = (value<<8) + (value>>8)
    def hexString = String.format("%04X", c & 0xFFFF)  
    return hexString  
}

def GetRssiLQI()
{
    def cmds = []
    cmds += zigbee.readAttribute(0x0b05, 0x011c, [destEndpoint: 0x01], 50)    //CLUSTER_BASIC Mfg
    cmds += zigbee.readAttribute(0x0b05, 0x011d, [destEndpoint: 0x01], 50)
    return cmds
}

def GetTemperature(){
    def cmds = []
     cmds += zigbee.readAttribute(0xfc31, 0x0021, ["mfgCode": "0x122f"], 100)
     cmds += zigbee.readAttribute(0xfc31, 0x0020, ["mfgCode": "0x122f"], 100)
    return cmds
}

def componentGetRSSILQI(childDevice){
 
    def cmds = []
     Integer chilid = getChildId(childDevice).toInteger()
    sendHubCommand(new hubitat.device.HubMultiAction(zigbee.readAttribute(0x0b05, 0x011c, [destEndpoint: chilid], 50), hubitat.device.Protocol.ZIGBEE))
    sendHubCommand(new hubitat.device.HubMultiAction(zigbee.readAttribute(0x0b05, 0x011d, [destEndpoint: chilid], 50), hubitat.device.Protocol.ZIGBEE))
    return cmds
}

def componentTarget(childDevice){
 
    def cmds = []
     Integer chilid = getChildId(childDevice).toInteger()
    sendHubCommand(new HubAction("he cmd 0x${device.deviceNetworkId} 0x${getChildId(childDevice)} 0x0003 0x0 {20 00 }", Protocol.ZIGBEE))
   // sendHubCommand(new hubitat.device.HubMultiAction(zigbee.readAttribute(0x0003, 0x00, [destEndpoint: chilid], "20 00", 50), hubitat.device.Protocol.ZIGBEE))
    return cmds
}

def SetFanControl(FanMode)
{
    def cmds = []
    Integer FanModeInt
    
    state.lastCommand = "Set Speed (${value})"
    switch (FanMode) {
        case "off":
             FanModeInt  = 0x00
            break
        case "low": 
            FanModeInt  = 0x01
            break
        case "medium": 
            FanModeInt  = 0x02
            break
        case "high": 
            FanModeInt  = 0x03
            break
    }
   cmds = zigbeeWriteAttribute(0x02,0x0202,0x0000,0x30,FanModeInt,[:], 50)
    return cmds
}

void componentsetPrivateCluster(childDevice, attributeId, value=null, size=8) {
    Integer attId = attributeId.toInteger()
    Integer setValue = value.toInteger()
    Integer attsize
    if(size.toInteger() == 1){
        attsize = 0x10
    }else if(size.toInteger() == 8){
        attsize = 0x20
    }
    else if(size.toInteger() == 16){
        attsize = 0x21
    }
    //zigbee.writeAttribute(0x0000, 0xffde, 0x20, 0x0d, [destEndpoint: 0x01], delay = 50)
    Integer chilid = getChildId(childDevice).toInteger()
    sendHubCommand(new hubitat.device.HubMultiAction(zigbeeWriteAttribute(chilid,0xfc31,attId,attsize,setValue,["mfgCode": "0x122f"]), hubitat.device.Protocol.ZIGBEE))
}

void componentSetRFControl(childDevice, Control) {
    
    Integer chilid = getChildId(childDevice).toInteger()
    sendHubCommand(new hubitat.device.HubMultiAction(zigbee.command(0xfc31,0x10,["mfgCode":"0x122f", destEndpoint: chilid],10,"${Control}"), hubitat.device.Protocol.ZIGBEE))

}
def componentSetLevel(childDevice, value, rate=null) {
    log.info "componentSetLevel (${value}, ${rate}) request from ${childDevice.deviceNetworkId}"
    
    def scaledRate
    if(rate != null)
    {
        rate = rate.toBigDecimal()
        scaledRate = (rate * 1).toInteger()
    }else
    {
        scaledRate = 0xffff
    }
    value = (value.toInteger() * 2.55).toInteger()
    sendHubCommand(new HubAction("he cmd 0x${device.deviceNetworkId} 0x${getChildId(childDevice)} 0x0008 0x4 {0x${myintTo8bitUnsignedHex(value)} 0x${myintTo16bitUnsignedHex(scaledRate)} }", Protocol.ZIGBEE))
}

void componentStartLevelChange(childDevice, direction) {
     log.info "componentStartLevelChange (${direction}) request from ${childDevice.deviceNetworkId}"
    def upDown = direction == "down" ? 1 : 0
    def unitsPerSecond = 100
     sendHubCommand(new HubAction("he cmd 0x${device.deviceNetworkId} 0x${getChildId(childDevice)} 0x0008 5 { 0x${myintTo8bitUnsignedHex(upDown)}  0x${myintTo8bitUnsignedHex(unitsPerSecond)} }", Protocol.ZIGBEE))
}

void componentStopLevelChange(childDevice) {
    log.info "componentStopLevelChange request from ${childDevice.deviceNetworkId}"
    sendHubCommand(new HubAction("he cmd 0x${device.deviceNetworkId} 0x${getChildId(childDevice)} 0x0008 3 {}", Protocol.ZIGBEE))
}



def setupChildDevices() {
    log.debug "Parent setupChildDevices"
    deleteObsoleteChildren()    
    def buttons = 2

    logDebug "model: ${device.data.model} buttons: $buttons"
    createChildDevices((int)buttons)
}

def createChildDevices(int buttons) {
    logDebug "Parent createChildDevices"
    
    if (buttons == 0)
        return            
    
    for (i in 1..buttons) {
        def childId = "${device.id}-0${i}"
        def existingChild = getChildDevices()?.find { it.deviceNetworkId == childId}
    
        if (existingChild) {
            log.info "Child device ${childId} already exists (${existingChild})"
        } else {
            if(i == 1)
            {
             log.info "Creatung device ${childId}"
             addChildDevice("InovelliUSA", "Inovelli VZM36-SN Zigbee Canopy Light", childId, [isComponent: true, name: "Canopy Light EP0${i}", label: "${device.displayName} Light"])
            }else if(i == 2)
            {
             addChildDevice("InovelliUSA", "Inovelli VZM36-SN Zigbee Canopy Fan", childId, [isComponent: true, name: "Canopy Fan EP0${i}", label: "${device.displayName} Fan"])
            }
            
        }
    }
}



def deleteObsoleteChildren() {
	 log.info "Parent deleteChildren"
    
    getChildDevices().each {child->
        if (!child.deviceNetworkId.startsWith(device.id) || child.deviceNetworkId == "${device.id}-00") {
            log.info "Deleting ${child.deviceNetworkId}"
  		    deleteChildDevice(child.deviceNetworkId)
        }
    }
}


ArrayList<String> zigbeeWriteAttribute(Integer endpoint, Integer cluster, Integer attributeId, Integer dataType, Integer value, Map additionalParams = [:], int delay = 198) {

    String mfgCode = ""
    if(additionalParams.containsKey("mfgCode")) {
          mfgCode = " {${HexUtils.integerToHexString(HexUtils.hexStringToInt(additionalParams.get("mfgCode")), 2)}}"
    }
    Integer size = 1
    if(dataType == 0x21){
        size = 2
    }
    String wattrArgs = "0x${device.deviceNetworkId} $endpoint 0x${HexUtils.integerToHexString(cluster, 2)} " + 
                       "0x${HexUtils.integerToHexString(attributeId, 2)} " + 
                       "0x${HexUtils.integerToHexString(dataType, 1)} " + 
                       "{${HexUtils.integerToHexString(value, size)}}" + 
                       "$mfgCode"
    ArrayList<String> cmd = ["he wattr $wattrArgs", "delay $delay"]
    return cmd
}

def logDebug(msg) {
    // log.debug msg
}
