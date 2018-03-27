/**
 *  Copyright 2018 Eric Maycock
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
 *  2018-03-27: Adapted for Hubitat
 */
metadata {
	
    definition (name: "Simulated Switch", namespace: "erocm123", author: "Eric Maycock") {
		capability "Switch"
        capability "Actuator"

		command "onPhysical"
		command "offPhysical"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState:"turningOn"
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"	
			}
		}
        main "switch"
		details(["switch"])
	}
}

def parse(String description) {

}

def parse(Map description) {
    def eventMap
    if (description.type == null) eventMap = [name:"$description.name", value:"$description.value"]
    else eventMap = [name:"$description.name", value:"$description.value", type:"$description.type"]
    createEvent(eventMap)
}

def extSendEvent(Map description) {
	def eventMap
    if (description.type == null) eventMap = [name:"$description.name", value:"$description.value"]
    else eventMap = [name:"$description.name", value:"$description.value", type:"$description.type"]
    sendEvent(eventMap)
}

def on() {
	log.debug "$version on()"
	sendEvent(name: "switch", value: "on")
}

def off() {
	log.debug "$version off()"
	sendEvent(name: "switch", value: "off", displayed: true)
}

def onPhysical() {
	log.debug "$version onPhysical()"
	sendEvent(name: "switch", value: "on", type: "physical")
}

def offPhysical() {
	log.debug "$version offPhysical()"
	sendEvent(name: "switch", value: "off", type: "physical")
}

private getVersion() {
	"PUBLISHED"
}
