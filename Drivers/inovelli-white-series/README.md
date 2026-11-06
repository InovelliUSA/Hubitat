# Inovelli White Series Matter Driver for Hubitat

## Acknowledgments

This source is a continuation of the work by **jmhavon**, published under the GitHub account **jvmahon** in the original [Hubitat-matterTools Inovelli project](https://github.com/jvmahon/Hubitat-matterTools/tree/main/Inovelli).

We are grateful for all of his help with this and many other projects. His work and continued contributions to the community are greatly appreciated!

## Installation

Choose one of the methods below, then complete **Device setup and initialization**.

### Option 1: Import a bundle ZIP

Use this method when the release includes a ZIP packaged as a Hubitat bundle.

1. Download the driver bundle ZIP. Keep the ZIP intact.
2. Open your Hubitat hub's web interface.
3. Open **Bundles** under **Developer tools**. If these options are hidden, open **Settings** and enable **Show advanced/developer options**.
4. Select **Import ZIP**.
5. Choose the local-file upload option, select the bundle ZIP, and complete the upload if prompted. Click **Import** and wait for confirmation.
6. Check **Drivers Code** to confirm that the included drivers were installed.
7. Continue with **Device setup and initialization** below.

If you have a direct download URL for the bundle, you can use the URL-import option instead. Use a link to the ZIP itself, rather than its GitHub preview page.

**Use a Hubitat bundle ZIP for this method.** GitHub's **Download ZIP** repository archive, or an ordinary ZIP containing loose driver files, is not a substitute for a bundle. Use the manual method for loose source files.

### Option 2: Install driver source manually

1. Access each drivers .groovy source code in github and copy the conent (you will repeat this with each file). 
2. In Hubitat, open **Drivers Code** and select **New Driver**.
3. Paste the complete contents of one driver `.groovy` file into the editor and click **Save**.
4. Repeat for every supplied driver file, including the component drivers.
5. Continue with the setup steps below.

## Device setup and initialization

1. Pair your Inovelli device with Hubitat, or open its existing entry under **Devices**.
2. On the parent device's **Device info** tab, select **Inovelli White Series Matter Advanced Device Driver** as the **Type**, then save.
3. Open **Commands** and click **Initialize**.
4. Wait approximately **15 seconds**, then refresh the page. In Chrome, use **Ctrl + F5**.
5. Check **Child devices** and **Preferences**. For example, a VTM31 should expose **Load Control** and **LED Notification Bar** child devices.

Use the child devices for load and LED control. If preferences display refresh messages instead of configuration controls, run **Initialize** again, wait, and refresh.

## References

- [Original Inovelli source and manual installation instructions](https://github.com/jvmahon/Hubitat-matterTools/tree/main/Inovelli)
- [Hubitat Package Manager installation guide: locating Bundles and importing a ZIP](https://hubitatpackagemanager.hubitatcommunity.com/installing.html)
