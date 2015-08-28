package template;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.adefreitas.gcf.CommManager;
import com.adefreitas.gcf.Settings;
import com.adefreitas.gcf.desktop.DesktopGroupContextManager;
import com.adefreitas.gcf.desktop.EventReceiver;
import com.adefreitas.gcf.desktop.providers.PhillipsHueProvider;
import com.adefreitas.gcf.desktop.toolkit.HttpToolkit;
import com.adefreitas.gcf.messages.ContextData;
import com.adefreitas.gcf.messages.ContextRequest;
import com.google.gson.Gson;

/**
 * This is a simple example of how to use GCF in a desktop application
 * @author adefreit
 *
 */
public class GCFDesktopApplication implements EventReceiver
{
	// Creates a Unique Device ID (Needed for GCM to Operate, But You Can Change it to Anything Unique)
	public String deviceID;
	
	// GCF Communication Settings
	public static final CommManager.CommMode COMM_MODE  = CommManager.CommMode.MQTT;
	public static final String 				 IP_ADDRESS = Settings.DEV_MQTT_IP;
	public static final int    				 PORT 	    = Settings.DEV_MQTT_PORT;
	
	// GCF Variables
	public DesktopGroupContextManager gcm;
	
	// Gson
	public Gson gson = new Gson();
	
	/**
	 * Constructor:  Initializes the GCM
	 */
	public GCFDesktopApplication(String[] args)
	{
		// Assigns the Desktop Application's Name
		deviceID  = (args.length >= 1) ? args[0] : "DESKTOP_APP_" + (System.currentTimeMillis() % 1000);
		
		// Creates the Group Context Manager
		gcm = new DesktopGroupContextManager(deviceID, false);
		String connectionKey = gcm.connect(COMM_MODE, IP_ADDRESS, PORT);
		gcm.subscribe(connectionKey, "TEST_CHANNEL");

		// GCM Settings
		gcm.registerEventReceiver(this);
		//gcm.setDebugMode(true);
			
		// TODO:  Create/Register Context Providers
		//gcm.registerContextProvider(new ContextProvider());
	}
	
	/**
	 * This Method is Called Whenever the GCM Receives Data
	 */
	@Override
	public void onContextData(ContextData data) 
	{
		System.out.println(data);
	}

	/**
	 * This is the Main Application
	 * @param args
	 */
	public static void main(String[] args) 
	{			
		new GCFDesktopApplication(args);
	}
}
