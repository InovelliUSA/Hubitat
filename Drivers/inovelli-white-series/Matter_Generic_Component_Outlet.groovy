metadata {
    definition(name: "Matter Generic Component Outlet", namespace: "matterTools", author: "jvm", component: true) {
        capability "Refresh"
        capability "Switch"
        capability "VoltageMeasurement"
        capability "CurrentMeter"
        capability "PowerMeter"
        

        command "on"  , [[name: "Remain on for (seconds)", type:"NUMBER", description:"Turn off the device after the specified number of seconds"]]
        command "toggleOnOff" 
       
   
        // Identify Cluster
        attribute "IdentifyTime", "number"
        attribute "IdentifyType", "string"
        
        // Switch Cluster
        attribute "OnTime", "number"
        attribute "OffWaitTime", "number"
        attribute "StartUpOnOff", "string"
        
    }
    preferences {
        input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: false
    }
}

void updated() {
    log.info "Updated..."
    log.warn "description logging is: ${txtEnable == true}"
}

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
     List updateLocalStateOnlyAttributes = ["OffWaitTime", "OnTime", "StartUpOnOff"]
    sendEventTypeOfEvents.each {
        if (device.hasAttribute (it.name)) {
            if (txtEnable) {
                if(device.currentValue(it.name) == it.value) {
                    log.info ((it.descriptionText) ? (it.descriptionText) : ("${device.displayName}: ${it.name} set to ${it.value}") )+" (unchanged)" // Log if txtEnable and the value is the same
                } else {
                    log.info ((it.descriptionText) ? (it.descriptionText) : ("${device.displayName}: ${it.name} set to ${it.value}") ) // Log if txtEnable and the value is the same
                }
            }
            if (updateLocalStateOnlyAttributes.contains(it.name)) {
                device.updateDataValue(it.name, "${it.value}")
            } else {
                sendEvent(it)
            }
        }
    }
}

void refresh() { parent?.refreshMatter(ep: getEndpoint() ) }

Integer getEndpoint() { Integer.parseInt(getDataValue("endpointId") ) }

void on() { parent?.on(ep: getEndpoint() ) }
void on(turnOffAfterSeconds) {   parent?.onWithTimedOff(ep:getEndpoint(), onTime10ths:(turnOffAfterSeconds * 10 as Integer) )}

void off() { parent?.off(ep: getEndpoint() ) }

void toggleOnOff() { parent?.toggleOnOff(ep: getEndpoint()) }

void OnWithTimedOff(timeInSeconds, guardTime = 0) {
    parent?.onWithTimedOff(ep: getEndpoint(), 
                           onTime10ths:(timeInSeconds * 10) as Integer, 
                           ((timeInSeconds + guardTime) * 10) as Integer)
}
                                         
                                         
void clearLeftoverStates() {
	// Can't modify state from within state.each{}, so first collect what is unwanted, then remove in a separate unwanted.each
	state.keySet().each{state.remove( it ) }
}

void removeAllSettings() {
    if (logEnable) log.debug "settings before clearing: " + settings
    // Copy keys set first to avoid any chance of concurrent modification
    def keys = new HashSet(settings.keySet())
    keys.each{ key -> device.removeSetting(key) }
     if (logEnable) log.debug "settings after clearing: " + settings
}

