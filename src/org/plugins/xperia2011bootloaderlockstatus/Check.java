package org.plugins.xperia2011bootloaderlockstatus;

import org.logger.MyLogger;
import org.plugins.PluginDefaults;
import org.system.GlobalConfig;
import org.system.OS;

import flashsystem.Bundle;
import flashsystem.X10flash;
import gui.FlasherGUI;
import gui.WaitDeviceFlashmodeGUI;

public class Check extends PluginDefaults implements org.plugins.PluginInterface {


	public String getName() {
		return "Bootloader Lock Status Checker (for Xperia 2011 devices)";
	}

	public String getPhoneCert() {
		try {
			Bundle bundle = new Bundle();
			bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
			if (!OS.getName().equals("windows")) bundle.setSimulate(true);
			X10flash flash = new X10flash(bundle);
			if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(FlasherGUI._root)) {
				String hook = flash.getHook2(); //grab the long return string from loader [CMD01] 
				MyLogger.debug("hook2 returned: " + hook);
				String[] hookarray = hook.split(";");
				
				String bootloaderStatus = "ERROR";
				
				for (int i = 0; i < hookarray.length; i++)
				{
					String propertyName = hookarray[i].split("=")[0];
				
					if(propertyName.equals("ROOTING_STATUS")){
						MyLogger.debug("FOUND ROOTING_STATUS");
						String propertyValue = hookarray[i].split("=")[1].replace("\"", "");
						if(propertyValue.equals("ROOTED")){
							// bootloader IS already unlocked the OFFICIALLY way  
							bootloaderStatus = "UNLOCKED";
							MyLogger.debug("bootloaderStatus: " + bootloaderStatus);
						}
						if(propertyValue.equals("ROOTABLE")){
							// bootloader CAN BE unlocked the OFFICIALLY way
							// OR 
							// this can also happen if bootloader was unlocked the UNOFFICIALLY
							// way in which case we need to further implement code to check if its
							// unlocked the UNOFFICIAL way 
							bootloaderStatus = "LOCKED";
							MyLogger.debug("bootloaderStatus: " + bootloaderStatus);
						}
						if(propertyValue.equals("UNROOTABLE")){
							// bootloader CANNOT be unlocked the OFFICIALLY way  
							bootloaderStatus = "UNLOCKABLE";
							MyLogger.debug("bootloaderStatus: " + bootloaderStatus);
						}						
						break;
					}
					
				}
				MyLogger.initProgress(0);
				bundle.close();
				return bootloaderStatus;
			}
			MyLogger.initProgress(0);
			bundle.close();
			return "ERROR";
		}
		catch (Exception e) {
			MyLogger.initProgress(0);
			MyLogger.error(e.getMessage());
			return "ERROR";
		}
	}

	public void run() throws Exception {
		try {
			MyLogger.debug("Working directory : " + _workdir);
			String currentCert = getPhoneCert();
			MyLogger.debug("GETCERT RETURNED: " + currentCert);
			
			if(currentCert.equals("ERROR")){
				MyLogger.error("Unable to determine bootloader lock status");
			}
			else{
				MyLogger.info("BOOTLOADER STATUS: " + currentCert);
			}
			
		}
		catch (Exception e) {
			MyLogger.error(e.getMessage());
		}
	}

	public void showAbout() {
		About about = new About();
		about.setVisible(true);
	}

}