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
 */
metadata {
	definition (name: "Inovelli Fan Child Device", namespace: "InovelliUSA", author: "Eric Maycock") {
	capability "Switch Level"
	capability "Actuator"
	capability "Switch"
	capability "Refresh"
        capability "FanControl"

        command "low"
        command "medium"
        command "high"
	}
}

void on() {
	parent.childOn(device.deviceNetworkId)
}

void off() {
	parent.childOff(device.deviceNetworkId)
}

void refresh() {
	parent.childRefresh(device.deviceNetworkId)
}

def setLevel(value) {
	parent.childSetLevel(device.deviceNetworkId, value)
}

def low() {
	parent.childSetLevel(device.deviceNetworkId, 33)
    sendEvent(name: "speed", value: "low")
}
def medium() {
	parent.childSetLevel(device.deviceNetworkId, 66)
    sendEvent(name: "speed", value: "medium")
}
def high() {
	parent.childSetLevel(device.deviceNetworkId, 99)
    sendEvent(name: "speed", value: "high")
}

def setSpeed(fanspeed) {
    log.info "fan speed set to ${fanspeed}"
    
    if (fanspeed == low) {
        low()
    log.info "fan speed set to 33"
    }
    else if (fanspeed == medium-low) {
        medium()
    log.info "fan speed set to 66"
    }
    else if (fanspeed == medium) {
        medium()
    log.info "fan speed set to 66"
    }
    else if (fanspeed == medium-high) {
        medium()
    log.info "fan speed set to 66"
    }
    else if (fanspeed == high) {
        high()
    log.info "fan speed set to 99"
    }
    else if (fanspeed == on) {
        on()
    }
    else if (fanspeed == off) {
        off()
    }
}
