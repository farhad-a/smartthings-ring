/**
 *  Ring Security Cam
 *
 *  Copyright 2018 Farhad Alaghband
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
  definition (name: "Ring Security Cam", namespace: "farhad-a", author: "Farhad Alaghband") {
    capability "Actuator"
    capability "Alarm"
    capability "Refresh"
    capability "Switch"
    capability "Sensor"
    
    command "updateDeviceData", ["string"]
  }
  
  simulator {}
  
  tiles(scale: 2) {
    multiAttributeTile(name:"light", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
      tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        attributeState "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light15", backgroundColor:"#00a0dc", nextState:"turningOff"
        attributeState "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light15", backgroundColor:"#ffffff", nextState:"turningOn"
        attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.Lighting.light15", backgroundColor:"#00a0dc", nextState:"turningOff"
        attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.Lighting.light15", backgroundColor:"#ffffff", nextState:"turningOn"
      }
    }
    
    standardTile("siren", "device.alarm", inactiveLabel: false, decoration: "flat") {
      state "off", label:'', action:"alarm.siren", icon:"st.secondary.siren", backgroundColor:"#cccccc"
      state "strobe", label:'', action:"alarm.siren", icon:"st.secondary.siren", backgroundColor:"#cccccc"
      state "siren", label:'', action:'alarm.siren', icon:"st.secondary.siren", backgroundColor:"#e86d13"
      state "both", label:'', action:'alarm.siren', icon:"st.secondary.siren", backgroundColor:"#e86d13"
    }
    
    standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
      state("default", action: "refresh.refresh", icon: "st.secondary.refresh")
    }
    
    main("light")
    
    details([
      "light",
      //"siren",
      "refresh"
    ])
  }
}

// parse events into attributes
def parse(String description) {
  log.debug "Parsing '${description}'"
}

def refresh() {
  log.debug "Executing 'refresh'"
  parent.refresh()
}

// handle commands
def off() {
  log.debug "Executing 'off'"
  parent.setDeviceEnabled(this.device, false)
  sendEvent(name: "switch", value: "off")
}

def on() {
  log.debug "Executing 'on'"
  parent.setDeviceEnabled(this.device, true)
  sendEvent(name: "switch", value: "on")
}

def updateDeviceData(data) {
  sendEvent(name: "switch", value: data.led_status)
}