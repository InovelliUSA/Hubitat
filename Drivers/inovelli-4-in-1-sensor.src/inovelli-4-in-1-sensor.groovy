/**
 *
 *  Inovelli 4-in-1 Sensor
 *
 *	github: InovelliUSA
 *	Date: 2020-01-28
 *	Copyright Inovelli / Eric Maycock
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
 *  2020-01-28: Update VersionReport parsing because of Hubitat change. Removing unnecessary reports.
 *
 *  2020-01-16: Support for all device configuration parameters.
 *              Offset options for temperature, humidity, and illuminance.
 *              Fix illuminance scale.
 *              Options to enable & disable logging.
 *              Association support added for use with the Inovelli Z-Wave association tool.
 *  2020-04-09: - bcopeland - Re-engineer of driver using current coding standards
 *                            Reduce un-necessary event log chatter (there was a lot of this)
 *                            Add TamperAlert capability
 *                            Standardize device info and add serialnumber, firmware version, protocol version, hardware version
 *                            Got rid of double / redundant motion events
 */

import groovy.transform.Field

metadata {
    definition (name: "Inovelli 4-in-1 Sensor", namespace: "InovelliUSA", author: "Eric Maycock", importUrl: "https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-4-in-1-sensor.src/inovelli-4-in-1-sensor.groovy") {
        capability "Actuator"
        capability "MotionSensor"
        capability "TemperatureMeasurement"
        capability "RelativeHumidityMeasurement"
        capability "IlluminanceMeasurement"
        capability "TamperAlert"
        capability "Refresh"
        capability "Configuration"
        capability "Sensor"
        capability "Battery"

        fingerprint mfr: "0072", prod: "0503", model: "0002", deviceJoinName: "Inovelli 4-in-1 Sensor"
        fingerprint mfr: "0072", prod: "0503", model: "1E00", deviceJoinName: "Inovelli 4-in-1 Sensor"
        fingerprint mfr: "031E", prod: "000D", model: "0001", deviceJoinName: "Inovelli 4-in-1 Sensor"

    }
    preferences {
        input description: "If battery powered, the configuration options (aside from temp, humidity, & lux offsets) will not be updated until the sensor wakes up (once every 24-Hours). To manually wake up the sensor, press the button on the back 3 times quickly.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        configParams.each { input it.value.input }
        input name: "temperatureOffset", type: "number", title: "Temperature Offset Adjust the reported temperature by this positive or negative value Range: -10.0..10.0 Default: 0.0", range: "-10.0..10.0", defaultValue: 0
        input name: "humidityOffset", type: "number", title: "Humidity Offset", Description: "Adjust the reported humidity percentage by this positive or negative value Range: -10 ..10 Default: 0", range: "-10..10", defaultValue: 0
        input name: "luminanceOffset", type: "number", title: "Luminance Offset", Description: "Adjust the reported luminance by this positive or negative value Range: -100..100 Default: 0", range: "-100..100", defaultValue: 0
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
        input name: "txtEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

@Field static Map CMD_CLASS_VERS=[0x31:5, 0x84:2, 0x20:1, 0x70:1]
@Field static Map configParams = [
        10: [input: [name: "configParam10", type: "number", title: "Low Battery Alert Level", description: "At what battery level should the sensor send a low battery alert", defaultValue: 10, range: "10..50"], parameterSize: 1],
        12: [input: [name: "configParam12", type: "number", title: "Motion Sensor Sensitivity", description: "Sensitivity level of the motion sensor. 0=Disabled 1=Low 10=High", defaultValue: 8, range: "0..10"], parameterSize:1],
        13: [input: [name: "configParam13", type: "number", title: "Motion Sensor Reset Time", description: "How long after motion stops should the sensor wait before sending a no-motion report", defaultValue: 30, range: "5..15300"], parameterSize:2],
        14: [input: [name: "configParam14", type: "enum", title: "Send Basic Set on Motion", description: "Send a Basic Set report to devices in association group 2", defaultValue: 0, options:["1":"Yes", "0":"No"]],parameterSize:1],
        15: [input: [name: "configParam15", type: "enum", title: "Send OFF to devices in association group 2 when motion is triggered and ON when motion stop", defaultValue: 0, options:["1":"Yes", "0":"No"]],parameterSize:1],
        100: [input: [name: "configParam100", type: "enum", title: "Reverse Basic Set ON / OFF", description: "Send a Basic Set report to devices in association group 2", defaultValue: 0, options:["1":"Yes", "0":"No"]],parameterSize:1],
        101: [input: [name: "configParam101", type: "number", title: "Temperature Reporting Interval", description: "Interval, in seconds, in which temperature reports should be sent. 0=Disabled", defaultValue:7200, range: "0..2678400"],parameterSize:4],
        102: [input: [name: "configParam102", type: "number", title: "Humidity Reporting Interval", description: "Interval, in seconds, in which humidity reports should be sent. 0=Disabled", defaultValue:7200, range: "0..2678400"],parameterSize:4],
        103: [input: [name: "configParam103", type: "number", title: "Luminance Reporting Interval", description: "Interval, in seconds, in which luminance reports should be sent. 0=Disabled", defaultValue:7200, range: "0..2678400"],parameterSize:4],
        104: [input: [name: "configParam104", type: "number", title: "Battery Reporting Interval", description: "Interval, in seconds, in which battery reports should be sent. 0=Disabled", defaultValue:7200, range: "0..2678400"],parameterSize:4],
        110: [input: [name: "configParam110", type: "enum", title: "Send Reports According to Threshold", description: "Only send sensor reports if the below thresholds are met", defaultValue: 0, options:["1":"Yes", "0":"No"]],parameterSize:1],
        111: [input: [name: "configParam111", type: "number", title: "Temperature Threshold", description: "Threshold for temperature reports to be sent", defaultValue:10, range: "1..500"],parameterSize:2],
        112: [input: [name: "configParam112", type: "number", title: "Humidity Threshold", description: "Threshold for humidity reports to be sent", defaultValue:5, range: "1..32"],parameterSize:1],
        113: [input: [name: "configParam113", type: "number", title: "Luminance Threshold", description: "Threshold for luminance reports to be sent", defaultValue:150, range: "1..65528"],parameterSize:2],
        114: [input: [name: "configParam114", type: "number", title: "Battery Threshold", description: "Threshold for battery reports to be sent", defaultValue:10, range: "1..100"],parameterSize:1]
]

void logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

void configure() {
    if (!state.initialized) initializeVars()
    runIn(5,pollDeviceData)
}

void initializeVars() {
    // first run only
    state.initialized=true
    runIn(5, refresh)
}

void updated() {
    log.info "updated..."
    log.warn "debug logging is: ${logEnable == true}"
    unschedule()
    if (logEnable) runIn(1800,logsOff)
    if (state.realTemperature != null) sendEvent(name:"temperature", value: getAdjustedTemp(state.realTemperature))
    if (state.realHumidity != null) sendEvent(name:"humidity", value: getAdjustedHumidity(state.realHumidity))
    if (state.realLuminance != null) sendEvent(name:"illuminance", value: getAdjustedLuminance(state.realLuminance))
    runConfigs()
}

List<hubitat.zwave.Command> runConfigs() {
    List<hubitat.zwave.Command> cmds=[]
    configParams.each { param, data ->
        if (settings[data.input.name]) {
            cmds.addAll(configCmd(param, data.parameterSize, settings[data.input.name]))
        }
    }
    return cmds
}

List<hubitat.zwave.Command> configCmd(parameterNumber, size, scaledConfigurationValue) {
    List<hubitat.zwave.Command> cmds = []
    cmds.add(zwave.configurationV1.configurationSet(parameterNumber: parameterNumber.toInteger(), size: size.toInteger(), scaledConfigurationValue: scaledConfigurationValue.toInteger()))
    cmds.add(zwave.configurationV1.configurationGet(parameterNumber: parameterNumber.toInteger()))
    return cmds
}

void zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
    if(configParams[cmd.parameterNumber.toInteger()]) {
        Map configParam=configParams[cmd.parameterNumber.toInteger()]
        int scaledValue
        cmd.configurationValue.reverse().eachWithIndex { v, index -> scaledValue=scaledValue | v << (8*index) }
        device.updateSetting(configParam.input.name, [value: "${scaledValue}", type: configParam.input.type])
    }
}

void pollDeviceData() {
    List<hubitat.zwave.Command> cmds = []
    cmds.add(zwave.versionV2.versionGet())
    cmds.add(zwave.manufacturerSpecificV2.deviceSpecificGet(deviceIdType: 1))
    cmds.add(zwave.notificationV8.notificationGet(notificationType: 7, event: 0))
    cmds.add(zwave.notificationV8.notificationGet(notificationType: 8, event: 0))
    cmds.add(zwave.notificationV8.notificationGet(notificationType: 20, event: 0))
    cmds.add(zwave.wakeUpV1.wakeUpIntervalSet(seconds: 43200, nodeid:zwaveHubNodeId))
    cmds.add(zwave.wakeUpV1.wakeUpIntervalGet())
    cmds.addAll(processAssociations())
    sendToDevice(cmds)
}

void refresh() {
    List<hubitat.zwave.Command> cmds=[]
    log.info "${device.displayName}: refresh()"
    log.debug "Refresh Double Press"
    // get configs
    cmds.add(zwave.batteryV1.batteryGet())
    cmds.add(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1))
    cmds.add(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:1))
    cmds.add(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5, scale:1))
    // do some stuff here
    sendToDevice(cmds)
}

void installed() {
    if (logEnable) log.debug "installed()..."
    initializeVars()
}

void zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
    // this is redundant
    if (logEnable) log.debug "Basic Report: ${cmd}"
}

void zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(CMD_CLASS_VERS)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand)
    }
}

void parse(String description) {
    if (logEnable) log.debug "parse:${description}"
    hubitat.zwave.Command cmd = zwave.parse(description, CMD_CLASS_VERS)
    if (cmd) {
        zwaveEvent(cmd)
    }
}

void zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd) {
    if (logEnable) log.debug "Supervision get: ${cmd}"
    hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(CMD_CLASS_VERS)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand)
    }
    sendToDevice(new hubitat.zwave.commands.supervisionv1.SupervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0))
}

void zwaveEvent(hubitat.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
    if (logEnable) log.debug "Device Specific Report: ${cmd}"
    switch (cmd.deviceIdType) {
        case 1:
            // serial number
            def serialNumber=""
            if (cmd.deviceIdDataFormat==1) {
                cmd.deviceIdData.each { serialNumber += hubitat.helper.HexUtils.integerToHexString(it & 0xff,1).padLeft(2, '0')}
            } else {
                cmd.deviceIdData.each { serialNumber += (char) it }
            }
            device.updateDataValue("serialNumber", serialNumber)
            break
    }
}

void zwaveEvent(hubitat.zwave.commands.versionv2.VersionReport cmd) {
    if (logEnable) log.debug "version2 report: ${cmd}"
    device.updateDataValue("firmwareVersion", "${cmd.firmware0Version}.${cmd.firmware0SubVersion}")
    device.updateDataValue("protocolVersion", "${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}")
    device.updateDataValue("hardwareVersion", "${cmd.hardwareVersion}")
}

void sendToDevice(List<hubitat.zwave.Command> cmds) {
    sendHubCommand(new hubitat.device.HubMultiAction(commands(cmds), hubitat.device.Protocol.ZWAVE))
}

void sendToDevice(hubitat.zwave.Command cmd) {
    sendHubCommand(new hubitat.device.HubAction(secureCommand(cmd), hubitat.device.Protocol.ZWAVE))
}

void sendToDevice(String cmd) {
    sendHubCommand(new hubitat.device.HubAction(secureCommand(cmd), hubitat.device.Protocol.ZWAVE))
}

List<String> commands(List<hubitat.zwave.Command> cmds, Long delay=200) {
    return delayBetween(cmds.collect{ secureCommand(it) }, delay)
}

String secureCommand(hubitat.zwave.Command cmd) {
    secureCommand(cmd.format())
}

String secureCommand(String cmd) {
    String encap = ""
    if (getDataValue("zwaveSecurePairingComplete") != "true") {
        return cmd
    } else {
        encap = "988100"
    }
    return "${encap}${cmd}"
}

void zwaveEvent(hubitat.zwave.Command cmd) {
    if (logEnable) log.debug "skip:${cmd}"
}

List<hubitat.zwave.Command> setDefaultAssociation() {
    List<hubitat.zwave.Command> cmds=[]
    cmds.add(zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId))
    cmds.add(zwave.associationV2.associationGet(groupingIdentifier: 1))
    return cmds
}

List<hubitat.zwave.Command> processAssociations(){
    List<hubitat.zwave.Command> cmds = []
    cmds.addAll(setDefaultAssociation())
    cmds.add(zwave.associationV2.associationGet(groupingIdentifier: 2))
    return cmds
}

void zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
    if (logEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    List<String> temp = []
    if (cmd.nodeId != []) {
        cmd.nodeId.each {
            temp.add(it.toString().format( '%02x', it.toInteger() ).toUpperCase())
        }
    }
    updateDataValue("zwaveAssociationG${cmd.groupingIdentifier}", "$temp")
}

void zwaveEvent(hubitat.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    if (logEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    log.info "${device.label?device.label:device.name}: Supported association groups: ${cmd.supportedGroupings}"
    state.associationGroups = cmd.supportedGroupings
}

void zwaveEvent(hubitat.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
    state.wakeInterval = cmd.seconds
}

void zwaveEvent(hubitat.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    log.info "${device.displayName} Device wakeup notification"
    // let's do some wakeup stuff here
    List<hubitat.zwave.Command> cmds=[]
    cmds.add(zwave.batteryV1.batteryGet())
    cmds.addAll(runConfigs())
    cmds.add(zwave.wakeUpV1.wakeUpNoMoreInformation())
    sendToDevice(cmds)
}

void zwaveEvent(hubitat.zwave.commands.batteryv1.BatteryReport cmd) {
    Map evt = [name: "battery", unit: "%", isStateChange: true]
    if (cmd.batteryLevel == 0xFF) {
        evt.descriptionText = "${device.displayName} has a low battery"
        evt.value = "1"
    } else {
        evt.descriptionText = "${device.displayName} battery is ${cmd.batteryLevel}%"
        evt.value = "${cmd.batteryLevel}"
    }
    if (txtEnable) log.info evt.descriptionText
    sendEvent(evt)
}

void zwaveEvent(hubitat.zwave.commands.notificationv8.NotificationReport cmd) {
    if (logEnable) log.debug "${cmd}"
    Map evt = [isStateChange:false]
    if (cmd.notificationType==7) {
        // home security
        switch (cmd.event) {
            case 0:
                // state idle
                if (cmd.eventParametersLength > 0) {
                    switch (cmd.eventParameter[0]) {
                        case 3:
                            evt.name = "tamper"
                            evt.value = "clear"
                            evt.isStateChange = true
                            evt.descriptionText = "${device.displayName} tamper alert cover closed"
                            break
                        case 7:
                            evt.name = "motion"
                            evt.value = "inactive"
                            evt.isStateChange = true
                            evt.descriptionText = "${device.displayName} motion became ${evt.value}"
                            break
                        case 8:
                            evt.name = "motion"
                            evt.value = "inactive"
                            evt.isStateChange = true
                            evt.descriptionText = "${device.displayName} motion became ${evt.value}"
                            break
                    }
                } else {
                    log.debug "0 length event parameter"
                }
                break
            case 3:
                // Tampering cover removed
                evt.name = "tamper"
                evt.value = "detected"
                evt.isStateChange = true
                evt.descriptionText = "${device.displayName} tamper alert cover removed"
                //deviceWakeup()
                break
            case 7:
                // motion detected (location provided)
                evt.name = "motion"
                evt.value = "active"
                evt.isStateChange = true
                evt.descriptionText = "${device.displayName} motion became ${evt.value}"
                break
            case 8:
                // motion detected
                evt.name = "motion"
                evt.value = "active"
                evt.isStateChange = true
                evt.descriptionText = "${device.displayName} motion became ${evt.value}"
                break
            case 254:
                // unknown event/state
                log.warn "Device sent unknown event / state notification"
                break
        }
    } else if (cmd.notificationType==8) {
        // power management
        switch (cmd.event) {
            case 0:
                // idle
                break
            case 1:
                // Power has been applied
                log.info "${device.displayName} Power has been applied"
                break
            case 2:
                // AC mains disconnected
                evt.name = "powerSource"
                evt.isStateChange = true
                evt.value = "battery"
                evt.descriptionText = "${device.displayName} AC mains disconnected"
                break
            case 3:
                // AC mains re-connected
                evt.name = "powerSource"
                evt.isStateChange = true
                evt.value = "mains"
                evt.descriptionText = "${device.displayName} AC mains re-connected"
                break
        }
    } else if (cmd.notificationType==20) {
        // might do something more with this later
        switch (cmd.event) {
            case 1:
                log.info "Light detected"
                break
            case 2:
                log.info "Light color transition detected"
                break
        }
    }
    if (evt.isStateChange) {
        if (txtEnable) log.info evt.descriptionText
        sendEvent(evt)
    }
}

void zwaveEvent(hubitat.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    if (logEnable) log.debug "${cmd}"
    Map evt = [isStateChange:false]
    switch (cmd.sensorType) {
        case 1:
            evt.name="temperature"
            double realTemp=convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
            state.realTemperature = realTemp
            evt.value=getAdjustedTemp(realTemp)
            evt.unit=getTemperatureScale()
            evt.isStateChange=true
            evt.description="${device.displayName}: Temperature report received: ${evt.value}"
            break
        case 3:
            evt.name = "illuminance"
            state.realLuminance = cmd.scaledSensorValue.toInteger()
            evt.value = getAdjustedLuminance(cmd.scaledSensorValue.toInteger())
            evt.unit = "lux"
            evt.isStateChange=true
            evt.description="${device.displayName}: Illuminance report received: ${evt.value}"
            break
        case 5:
            evt.name = "humidity"
            state.realHumidity = cmd.scaledSensorValue.toInteger()
            evt.value = getAdjustedHumidity(cmd.scaledSensorValue.toInteger())
            evt.unit = "%"
            evt.description="${device.displayName}: Humidity report received: ${evt.value}"
            break;
    }
    if (evt.isStateChange) {
        if (txtEnable) log.info evt.descriptionText
        sendEvent(evt)
    }
}

private double getAdjustedTemp(value) {
    value = Math.round((value as Double) * 100) / 100
    if (settings.temperatureOffset) {
        return value =  value + Math.round(settings.temperatureOffset * 100) /100
    } else {
        return value
    }
}

private double getAdjustedHumidity(value) {
    value = Math.round((value as Double) * 100) / 100
    if (settings.humidityOffset) {
        return value =  value + Math.round(settings.humidityOffset * 100) /100
    } else {
        return value
    }
}

private double getAdjustedLuminance(value) {
    value = (Math.round((value as Double) * 100) / 100) as Integer
    if (settings.luminanceOffset) {
        return value =  value + Math.round(settings.luminanceOffset * 100) /100
    } else {
        return value
    }
}
