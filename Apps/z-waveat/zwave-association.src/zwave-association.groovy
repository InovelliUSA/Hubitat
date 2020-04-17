/**
 *  Z-Wave Association
 *  Author: Eric Maycock (erocm123)
 *  Date: 2020-04-16
 *
 *  Copyright 2019 Eric Maycock / Inovelli
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
 *  2020-04-16: Adding option to reset association variables. Use this if you need to remove the source device from
 *              your network (to OTA update it) and add it back to the association later.
 *
 *  2019-10-10: Changing name of switch capability to switch / dimmer / bulb
 *
 */

definition(
    name: "Z-Wave Association",
    namespace: "erocm123",
    author: "Eric Maycock",
    description: "An app to create direct associations between two Z-Wave devices.",
    category: "My Apps",

    parent: "erocm123:Z-Wave Association Tool",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page name: "mainPage", title: "Associate Z-Wave Devices", install: false, uninstall: true, nextPage: "namePage"
    page name: "namePage", title: "Associate Z-Wave Devices", install: true, uninstall: true
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def uninstalled() {
    settings."s${settings.sCapability}".setAssociationGroup(groupNumber, settings."d${settings.dCapability}"? settings."d${settings.dCapability}".deviceNetworkId : [], 0, settings.endpoint)
    settings."s${settings.sCapability}".configure()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unschedule()
    initialize()
}

def initialize() {
    if (!overrideLabel) {
        app.updateLabel(defaultLabel())
    }
    
    if (resetAssoc == true) {
        state.previousNodes = []
        app.updateSetting("resetAssoc", false)
    }

    def addNodes = ((settings."d${settings.dCapability}"?.deviceNetworkId)?:[]) - (state.previousNodes? state.previousNodes : [])
    def delNodes = (state.previousNodes? state.previousNodes : []) - ((settings."d${settings.dCapability}"?.deviceNetworkId)?:[])
    if (addNodes)
        settings."s${settings.sCapability}".setAssociationGroup(groupNumber, addNodes, 1, settings.endpoint)
    if (delNodes)
        settings."s${settings.sCapability}".setAssociationGroup(groupNumber, delNodes, 0, settings.endpoint)
    settings."s${settings.sCapability}".configure()
        
    state.previousNodes = (settings."d${settings.dCapability}"?.deviceNetworkId)?:[]
}

def mainPage() {
    dynamicPage(name: "mainPage") {
        associationInputs()
    }
}

def namePage() {
    if (!overrideLabel) {
        def l = defaultLabel()
        log.debug "will set default label of $l"
        app.updateLabel(l)
    }
    //log.debug settings."s${settings.sCapability}".getDeviceDataByName("firmware")
    //log.debug getDeviceById(settings."s${settings.sCapability}".deviceNetworkId)
    //log.debug settings."s${settings.sCapability}".getDataValue("firmware")

    dynamicPage(name: "namePage") {
        if (overrideLabel) {
            section("Association Name") {
                label title: "Enter custom name", defaultValue: app.label, required: false
            }
        } else {
            section("Association Name") {
                paragraph app.label
            }
        }
        section {
            input "overrideLabel", "bool", title: "Edit association name", defaultValue: "false", required: "false", submitOnChange: true
        }
    }
}

def defaultLabel() {
    def associationLabel
    associationLabel = settings."s${settings.sCapability}".displayName + " Association Group ${groupNumber}"
    return associationLabel
}

def associationInputs() {
    section("Source Device") {
        input "sCapability", "enum", title: "Which capability?", multiple: false, required: true, submitOnChange: true, options: capabilities()
        if (settings.sCapability) {
            input "s${settings.sCapability}", "capability.${settings.sCapability.toLowerCase()}", title: "${capabilities()[settings.sCapability]}", multiple: false, required: false
        }
    }
    section("Destination Device") {
        input "dCapability", "enum", title: "Which capability?", multiple: false, required: true, submitOnChange: true, options: capabilities()
        if (settings.dCapability) {
            input "d${settings.dCapability}", "capability.${toCamelCase(settings.dCapability)}", title: "${capabilities()[settings.dCapability]}", multiple: true, required: false
        }
    }
    section("Options") {
        input "groupNumber", "enum", title: "Which group number?", multiple: false, required: true, options: returnGroups()
        //input "multiChannel", "bool", title: "MultiChannel Association?", required: false, submitOnChange: true
        //if (multiChannel) {
        //    input "endpoint", "number", title: "Endpoint ID", required: multiChannel
        //}
    }
    section("Advanced") {
        input "resetAssoc", "bool", title: "Reset the association variables (use only if you are changing the source device in this association)", multiple: false, required: false
    }
}

def capabilities() {
    return ["Actuator":"Actuator", "Sensor":"Sensor", "Switch":"Switch / Dimmer / Bulb", "Motion Sensor":"Motion Sensor", "Relative Humidity Measurement":"Relative Humidity Measurement", "Water Sensor":"Water Sensor", 
    "Thermostat":"Thermostat", "Temperature Measurement":"Temperature Measurement", "Contact Sensor":"Contact Sensor", "Lock":"Lock", "Alarm":"Alarm", "Presence Sensor":"Presence Sensor", "Smoke Detector":"Smoke Detector", "Valve":"Valve", "Button":"Button" ]
}

def returnGroups() {
    def groups = settings."s${settings.sCapability}"?.currentValue("groups")? settings."s${settings.sCapability}"?.currentValue("groups") : 5
    def groupings = []
    for (int i = 1; i <= groups.toInteger(); i++){
        groupings += i
    }
   return groupings
}

def toCamelCase( def text ) {
    def camelCase = ""
    def counter = 0
    text.split().each() {
        if (counter > 0) {
            camelCase += it.capitalize()
        } else {
            camelCase += it.toLowerCase()
        }
        counter += 1
    }
    return camelCase
}
