/*
Copyright James V. Mahon
Distribution requires permission / Not for Commercial distribution
*/
import groovy.json.JsonOutput
import groovy.transform.Field
import hubitat.helper.HexUtils
import hubitat.matter.DataType

@Field static Map supportedFanSpeeds = ["off": 0, "low": 1, "medium": 2, "high": 3, "on": 4, "auto": 5]

metadata {
    definition(name: "Matter Generic Component Tapable Fan", namespace: "matterTools", author: "jvm", component: true) {
        capability "Actuator"
        capability "Refresh"
        capability "Switch"
        capability "SwitchLevel"
        capability "FanControl"
        capability "Initialize"
        capability "PushableButton"
        capability "DoubleTapableButton"
        capability "HoldableButton"
        capability "ReleasableButton"

        // Override the FanControl capability's default setSpeed input so only device-supported speeds are offered
        // (the device does not support "medium-low" or "medium-high").
        command "setSpeed", [[name: "Fan speed*", type: "ENUM", description: "Fan speed to set", constraints: ["low", "medium", "high", "on", "off", "auto"]]]

        attribute "IdentifyTime", "number"
        attribute "SpeedMax", "number"
        attribute "SpeedSetting", "number"
    }
    preferences {
        input(name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: false)
        input(name: "logEnable", type: "bool", title: "<b>Enable debug logging</b>", defaultValue: false)
    }
}

void installed() {
    log.info "Installed..."
    device.updateSetting("txtEnable", [type: "bool", value: false])
    initialize()
    refresh()
}

void initialize() {
    sendEvent(name: "supportedFanSpeeds", value: JsonOutput.toJson(["low", "medium", "high", "on", "off", "auto"]))
}

void updated() {
    if (logEnable) {
        log.info "${device.displayName}: Debug logging enabled for 30 minutes"
        runIn(1800, logsOff)
    }
}

void logsOff() {
    if (txtEnable) log.info "${device.displayName}: Turning off debug logging."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

void parse(List sendEventTypeOfEvents) {
    if (logEnable) log.debug "${device.displayName}: ${sendEventTypeOfEvents}"
    try {
        List updateLocalStateOnlyAttributes = ["OnOffTransitionTime", "OnTransitionTime", "OffTransitionTime", "MinLevel", "MaxLevel",
                                               "DefaultMoveRate", "OffWaitTime", "OnLevel", "Binding", "UserLabelList", "FixedLabelList",
                                               "DeviceTypeList", "ServerList", "ClientList", "PartsList", "TagList", "SpeedMax"]
        sendEventTypeOfEvents.each {
            // VTM35 firmware 1.0.0 reported Level Control CurrentLevel incorrectly. Fan speed comes from the Fan Control cluster.
            if ((it.clusterInt == 8) && (it.attrInt == 0)) return
            if (device.hasAttribute(it.name)) {
                if (txtEnable) {
                    String text = it.descriptionText ?: "${device.displayName}: ${it.name} set to ${it.value}"
                    if (device.currentValue(it.name) == it.value) text = text + " (unchanged)"
                    log.info text
                }
                sendEvent(it)
            } else if (updateLocalStateOnlyAttributes.contains(it.name)) {
                device.updateDataValue(it.name, "${it.value}")
            }
        }
    } catch (AssertionError e) {
        log.error "<pre>${e}<br><br>Stack trace:<br>${getStackTrace(e) }"
    } catch (e) {
        log.error "<pre>${e}<br><br>when processing parse with inputs ${sendEventTypeOfEvents}<br><br>Stack trace:<br>${getStackTrace(e) }"
    }
}

Integer getEndpoint() {
    String raw = getDataValue("endpointId")
    if (!raw?.trim()) {
        log.error "${device.displayName} is missing endpointId, so commands cannot be sent."
        return null
    }
    raw = raw.trim()
    if (raw.startsWith("0x") || raw.startsWith("0X")) raw = raw.substring(2)
    return Integer.parseInt(raw, 16)
}

void refresh() { parent?.refreshMatter(ep: getEndpoint()) }

void on() { parent?.on(ep: getEndpoint()) }

void off() { parent?.off(ep: getEndpoint()) }

void setSpeed(fanSpeed) {
    Integer fanSpeedAsInteger = supportedFanSpeeds.get(fanSpeed?.toString())
    if (fanSpeedAsInteger == null) {
        log.error "${device.displayName}: ${fanSpeed} is not a supported fan speed."
        return
    }
    List<Map<String, String>> attrWriteRequests = []
    attrWriteRequests.add(matter.attributeWriteRequest(getEndpoint(), 0x0202, 0x0000, DataType.UINT8, HexUtils.integerToHexString(fanSpeedAsInteger, 1)))
    parent?.sendHubCommand(new hubitat.device.HubAction(matter.writeAttributes(attrWriteRequests), hubitat.device.Protocol.MATTER))
}

void cycleSpeed() {
    switch (device.currentValue("speed")) {
        case "off":
            setSpeed("low")
            break
        case "low":
            setSpeed("medium")
            break
        case "medium":
            setSpeed("high")
            break
        default:
            setSpeed("off")
            break
    }
}

void setLevel(level, ramp = null) {
    if (level == null || level == "") return
    Integer percent = Math.max(0, Math.min(100, level as Integer))
    List<Map<String, String>> attrWriteRequests = []
    attrWriteRequests.add(matter.attributeWriteRequest(getEndpoint(), 0x0202, 0x0002, DataType.UINT8, HexUtils.integerToHexString(percent, 1)))
    parent?.sendHubCommand(new hubitat.device.HubAction(matter.writeAttributes(attrWriteRequests), hubitat.device.Protocol.MATTER))
}
