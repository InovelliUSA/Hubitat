import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field
//import hubitat.helper.HexUtils
import java.security.MessageDigest

metadata {
    definition (name: "Inovelli VZM36-SN Zigbee Canopy Fan", namespace: "InovelliUSA", author: "Sven") { 
        
        capability "Actuator"
        capability "Bulb"
        capability "ChangeLevel"
        capability "Configuration"
        //capability "EnergyMeter"


        capability "Switch"
        capability "SwitchLevel"

        // Uncomment these lines if you would like to test your scenes with digital button presses.

        
        command "BindTarget"

        command "configure",           [[name: "Option", type: "ENUM", description: "User=user changed settings only, All=configure all settings, Default=set all settings to default", constraints: [" ","User","All","Default"]]]

        command "initialize"
        
       
        command "refresh",             [[name: "Option", type: "ENUM", description: "blank=current states only, User=user changed settings only, All=refresh all settings",constraints: [" ","User","All"]]]

        //command "resetEnergyMeter"

        
        command "setRFControl",            [[name: "RFControl*", type: "NUMBER"]]
        
        command "updateFirmware"
        
      //  command "GetRssiLQI"
        
        command "setSpeed",            [[name: "FanSpeed*", type: "ENUM", constraints: ["off","low","medium","high"]]]
        
        command "setPrivateCluster",   [[name: "Attribute*",type:"NUMBER", description: "Attribute (in decimal) ex. 0x000F input 15"], 
                                        [name: "Value", type:"NUMBER", description: "Enter the value (in decimal) Leave blank to get current value without changing it"], 
                                        [name: "Size*", type:"ENUM", description: "8=uint8, 16=uint16, 1=bool",constraints: ["8", "16","1"]]]
        
       // fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,0008,0B05,FC31,FC57", outClusters:"0019",                model:"VZM38-SN", manufacturer:"Inovelli"
    }
	
    
}



@Field static Integer defaultDelay = 300    //default delay for zigbee commands (in milliseconds)
 
void parse(String description) { log.inf "parse(String description) not implemented" }

void parse(List<Map> description) {
    description.each {
        if (it.name in ["switch","level"]) {
            log.info it.descriptionText
            sendEvent(it)
        }
    }
}




def configure(option) {    //THIS GETS CALLED AUTOMATICALLY WHEN NEW DEVICE IS DISCOVERED OR WHEN CONFIGURE BUTTON SELECTED ON DEVICE PAGE
    option = (option==null||option==" ")?"":option
    if (infoEnable) log.info "${device.label?device.label:device.name}: configure($option)"
    state.lastCommand = "Configure " + option
    sendEvent(name: "numberOfButtons", value: 14, displayed:false)
    def cmds = []

    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //On/Off Cluster

    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0008 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Level Control Cluster

    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0xFC31 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Private Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0xFC31 {${device.zigbeeId}} {}", "delay ${defaultDelay}"] //Private Cluster ep2
 
    if (option!="All") for(int i = 1;i<=8;i++) cmds += getAttribute(0xfc31, i)
    //update local copies of the read-only parameters
    cmds += getAttribute(0xfc31, 21)        //power type (neutral / non-neutral
    cmds += getAttribute(0xfc31, 51)        //number of bindings
    if (option!="") cmds += updated(option) //if option was selected on Configure button, pass it on to update settings.
    return cmds
}


def BindTarget() {
    parent?.componentTarget(this.device)
}

void on() {
    parent?.componentOn(this.device)
}

void off() {
    parent?.componentOff(this.device)
}

void setLevel(level) {
    parent?.componentSetLevel(this.device,level)

}

void setLevel(level, ramp) {
      parent?.componentSetLevel(this.device,level,ramp)

}

void startLevelChange(direction) {
    parent?.componentStartLevelChange(this.device,direction)
}

void stopLevelChange() {
    parent?.componentStopLevelChange(this.device)
}

def GetRssiLQI()
{
    def cmd = parent?.componentGetRSSILQI(this.device)
    return cmd
}

def setPrivateCluster(attributeId, value=null, size=8)
{
    parent?.componentsetPrivateCluster(this.device, attributeId, value, size)
}

def setRFControl(Control)
{
    parent?.componentSetRFControl(this.device, Control)
}

def setSpeed(value) {  // FOR FAN ONLY
    if (infoEnable) log.info "${device.label?device.label:device.name}: setSpeed(${value})"
    state.lastCommand = "Set Speed (${value})"
    switch (value) {
        case "off":
            setLevel(0) 
            break
        case "low": 
            setLevel(25) 
            break
        case "medium": 
            setLevel(50) 
            break
        case "high": 
            setLevel(100) 
            break
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

//**********************************************************************************
