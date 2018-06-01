# SmartThings Ring Security Cams

## Overview
Control the light on Ring Floodlight and Spotlight Cams via SmartThings.

## Installation Instructions:

### Manually:
1. Log in to the SmartThings IDE. If you don't have a login yet, create one.
1. Create the SmartApp:
   1. Go to **My SmartApps** &rarr; **New SmartApp** &rarr; **From Code**.
   1. Copy contents of [smartapps/farhad-a/ring-security-cams-connect.src/ring-security-cams-connect.groovy](smartapps/farhad-a/ring-security-cams-connect.src/ring-security-cams-connect.groovy) and paste into text area.
   1. Click **Create**. 
   1. Click **Publish** &rarr; **For Me**
1. Create the custom device handler for the lights:
   1. Go to **My Device Handlers** &rarr; **Create New Device Handler** &rarr; **From Code**.
   1. Copy contents of [devicetypes/farhad-a/ring-security-cam.src/ring-security-cam.groovy](devicetypes/farhad-a/ring-security-cam.src/ring-security-cam.groovy) and paste into text area.
   1. Click **Create**. 
   1. Click **Publish** &rarr; **For Me**
1. In your SmartThings mobile app: 
   1. Tap **Automation** &rarr; **SmartApps** &rarr; **Add a SmartApp**. 
   1. Scroll all the way down and tap **My Apps**, then **Ring Security Cams (Connect)**. 
   1. Enter your **_Ring_** login details and click **Next**.
   1. Select the security cameras you would like to include and click **Done**.
   1. In your list of **Things**, look for devices named "Ring: _[Device Name]_"

### SmartThings IDE GitHub Integration:
To use this installation method, you will need a [GitHub](https://www.github.com) account. 
If you have not set up GitHub integration for SmartThings IDE, take a look at the [SmartThings documentation](http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html).

1. Add a new GitHub Repository:
   1. From either the "My Device Handlers" or "My SmartApps" sections click on **Settings**.
   1. Under **GitHub Repositories**, click **Add new repository** and enter the following information:
      * Owner: farhad-a
      * Name: smartthings-ring
      * Branch: master
    1. Click **Save**.
1. Create the SmartApp:
   1. Go to **My SmartApps** and click **Update from Repo**.
   1. Select **smartthings-ring (master)**.
   1. In the **New (only in GitHub)** section, check the box next to `../ring-security-cams-connect.groovy`. 
   1. Check the **Publish** checkbox.
   1.  Click **Execute Update**.
1. Create the custom device handler for the lights:
   1. Go to **My Device Handlers** and click **Update from Repo**.
   1. Select **smartthings-ring (master)**.
   1. In the **New (only in GitHub)** section, check the box next to `../ring-security-cam.groovy`. 
   1. Check the **Publish** checkbox.
   1.  Click **Execute Update**.
1. In your SmartThings mobile app: 
   1. Tap **Automation** &rarr; **SmartApps** &rarr; **Add a SmartApp**. 
   1. Scroll all the way down and tap **My Apps**, then **Ring Security Cams (Connect)**. 
   1. Enter your **_Ring_** login details and click **Next**.
   1. Select the security cameras you would like to include and click **Done**.
   1. In your list of **Things**, look for devices named "Ring: _[Device Name]_"

If there is an update available, in the SmartApp/Device Handler list of SmartThings IDE, the SmartApp and/or device handler will turn ![#990099](https://placehold.it/15/990099/000000?text=+) <span style="color: #990099;">Magenta</span>. To update repeat steps 2 and 3. The only difference is the SmartApp/Device Handler displays in the **Obsolete (updated in GitHub)** section.

