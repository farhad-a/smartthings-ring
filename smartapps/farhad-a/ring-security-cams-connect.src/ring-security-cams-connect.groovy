/**
 * Ring Security Cams (Connect)
 *
 * Copyright 2018 Farhad Alaghband
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Ring Security Cams (Connect)",
    namespace: "farhad-a",
    author: "Farhad Alaghband",
    description: "Adds the ability to control the light on Ring Floodlight and Spotlight Cams.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
  page(name: "prefLogIn", title: "Ring")
  page(name: "prefListDevice", title: "Ring Security Lights")
}

/* Preferences */
def prefLogIn() {
  def showUninstall = username != null && password != null
  return dynamicPage(name: "prefLogIn", title: "Connect to Ring", nextPage: "prefListDevice", uninstall: showUninstall, install: false) {
    section("Login Credentials") {
      input("username", "email", title: "Email", description: "Ring email")
      input("password", "password", title: "Password", description: "Ring password")
    }
    section("Advanced Options"){
      input(name: "polling", title: "Server Polling (in Minutes)", type: "int", description: "in minutes", defaultValue: "5" )
    }
  }
}

def prefListDevice() {
  if (login()) {
    def lightList = getLightList()
    if (lightList) {
      return dynamicPage(name: "prefListDevice", title: "Devices", install: true, uninstall: true) {
        section("Select which devices to include"){
          input(name: getDeviceType(), type: "enum", required: false, multiple: true, metadata: [values: lightList])
        }
      }
    } else {
      return dynamicPage(name: "prefListDevice", title: "Error!", install: false, uninstall: true) {
        section(""){ paragraph "Could not find any devices." }
      }
    }
  } else {
    return dynamicPage(name: "prefListDevice", title: "Error!", install: false, uninstall: true) {
      section(""){ paragraph "The username or password you entered is incorrect. Try again." }
    }
  }
}

def installed() {
  log.debug "Installed with settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
  unsubscribe()
  initialize()
}

def uninstalled() {
  log.debug "Uninstalled"
  unschedule()
  getAllChildDevices().each { deleteChildDevice(it) }
}

def initialize() {
  log.debug "Initialize"
  // Set initial states
  state.polling = [ last: 0, rescheduler: now() ]
  
  // Create selected devices
  def lightList = getLightList()
  def selectedDevices = []+ getSelectedDevices(getDeviceType())
  selectedDevices.each {
    def dev = getChildDevice(it)
    def name = lightList[it]
    if (dev == null) {
      try {
        addChildDevice("Ring Security Cam", it, null, ["name": "Ring: " + name])
      } catch (e) {
        log.debug "addChildDevice Error: $e"
      }
    }
  }
  
  //Subscribes to sunrise and sunset event to trigger refreshes
  subscribe(location, "sunrise", runRefresh)
  subscribe(location, "sunset", runRefresh)
  subscribe(location, "mode", runRefresh)
  subscribe(location, "sunriseTime", runRefresh)
  subscribe(location, "sunsetTime", runRefresh)
  
  //Refresh devices
  runRefresh()
}

def getSelectedDevices( settingsName ) {
  def selectedDevices = []
  (!settings.get(settingsName))?:((settings.get(settingsName)?.getAt(0)?.size() > 1) ? settings.get(settingsName)?.each {   selectedDevices.add(it)  } : selectedDevices.add(settings.get(settingsName)))
  return selectedDevices
}

private getLightList() {
  def deviceList = [:]
  apiGet("/clients_api/ring_devices", []) { response ->
    if (response.status == 200) {
      response.data.stickup_cams?.each {
        if (it.led_status) {
          deviceList["" + it.id]= it.description
        }
      }
    }
  }
  return deviceList
}

// Refresh data
def refresh() {
  if (!login()) {
    return
  }
  
  log.info "Refreshing data..."
  // update last refresh
  state.polling?.last = now()
  
  // get all the children and send updates
  apiGet("/clients_api/ring_devices", []) { response ->
    if (response.status == 200) {
      log.debug "Got data: $response.data"
      getAllChildDevices().each { c ->
        response.data.stickup_cams?.each { l ->
          if (l.led_status && "${l.id}" == "${c.deviceNetworkId}") {
            log.debug "Updating device data: " + c.deviceNetworkId
            c.updateDeviceData(l)
          }
        }
      }
    }
  }
  
  //schedule the rescheduler to schedule refresh ;)
  if ((state.polling?.rescheduler?:0) + 2400000 < now()) {
    log.info "Scheduling Auto Rescheduler.."
    runEvery30Minutes(runRefresh)
    state.polling?.rescheduler = now()
  }
}

// Schedule refresh
def runRefresh(evt) {
  log.info "Last refresh was "  + ((now() - state.polling?.last?:0)/60000) + " minutes ago"
  // Reschedule if  didn't update for more than 5 minutes plus specified polling
  if ((((state.polling?.last?:0) + (((settings.polling?.toInteger()?:1>0)?:1) * 60000) + 300000) < now()) && canSchedule()) {
    log.info "Scheduling Auto Refresh.."
    schedule("* */" + ((settings.polling?.toInteger()?:1>0)?:1) + " * * * ?", refresh)
  }
  
  // Force Refresh NOWWW!!!!
  refresh()
  
  //Update rescheduler's last run
  if (!evt) state.polling?.rescheduler = now()
}

def setDeviceEnabled(childDevice, enabled) {
  log.info "setDeviceEnabled: $childDevice.deviceNetworkId $enabled"
  if (login()) {
    def cmd = enable ? "on" : "off"
    apiPut("/clients_api/doorbots/$childDevice.deviceNetworkId/floodlight_light_" + cmd)
  }
}

private login() {
  log.debug "Authenticating..."
  def s = "${settings.username}:${settings.password}"
  String encodedUandP = s.bytes.encodeBase64()
  def apiParams = [
    uri: getApiURL(),
    path: "/clients_api/session",
    headers: [
      Authorization: "Basic ${encodedUandP}",
      "User-Agent": "iOS"
    ],
    requestContentType: "application/x-www-form-urlencoded",
    body: "device%5Bos%5D=ios&device%5Bhardware_id%5D=a565187537a28e5cc26819e594e28213&api_version=9"
  ]
  
  if (state.session?.expiration < now()) {
    try {
      httpPost(apiParams) { response ->
        log.debug "POST response code: ${response.status}"
        if (response.status == 201) {
          log.debug "Login good!"
          state.session = [
            authenticationToken: response.data.profile.authentication_token,
            expiration: now() + 150000
          ]
          return true
        } else {
          return false
        }
      }
    } catch (e) {
      log.error "API Error: $e"
      return false
    }
  } else {
    // TODO: do a refresh
    return true
  }
}

/* API Management */
// HTTP GET call
private apiGet(apiPath, apiParams = [], callback = {}) {
  // set up parameters
  apiParams = [
    uri: getApiURL(),
    path: apiPath,
    query: [
      "api_version": "9",
      "auth_token": state.session?.authenticationToken
    ],
    requestContentType: "application/json",
  ] + apiParams
  log.debug "GET: $apiParams"
  try {
    httpGet(apiParams) { response -> callback(response) }
  } catch (e) {
    log.debug "API Error: $e"
  }
}

// HTTP PUT call
private apiPut(apiPath, apiParams = [], callback = {}) {
  // set up parameters
  apiParams = [
    uri: getApiURL(),
    path: apiPath,
    query: [
      "api_version": "9",
      "auth_token": state.session?.authenticationToken
    ],
    requestContentType: "application/json",
  ] + apiParams
  
  try {
    httpPut(apiParams) { response -> callback(response) }
  } catch (e) {
    log.debug "API Error: $e"
  }
}

private static getApiURL() {
  return "https://api.ring.com"
}

private static getDeviceType() {
  return "security-light"
}