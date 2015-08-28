package impromptu_apps.favors;

import impromptu_apps.DesktopApplicationProvider;

import java.sql.ResultSet;
import java.util.Calendar;


import com.adefreitas.gcf.ContextSubscriptionInfo;
import com.adefreitas.gcf.GroupContextManager;
import com.adefreitas.gcf.CommManager.CommMode;
import com.adefreitas.gcf.desktop.toolkit.JSONContextParser;
import com.adefreitas.gcf.desktop.toolkit.SQLToolkit;
import com.adefreitas.gcf.messages.ComputeInstruction;

public class App_FavorOfferer extends DesktopApplicationProvider
{
	private SQLToolkit toolkit;
	
	public App_FavorOfferer(GroupContextManager groupContextManager, CommMode commMode, String ipAddress, int port, SQLToolkit toolkit)
	{
		// Creates App with Default Settings
		super(groupContextManager, 
				"FAVOR_OFFER",
				"Offer a Favor",
				"This app lets you offer a favor to other users.",
				"FAVORS",
				new String[] { },  // Contexts
				new String[] { },  // Preferences
				"http://static.squarespace.com/static/52f2345de4b09aa670972fad/t/52f6b456e4b05d1230c1fde0/1391899736686/Icons%20transparent%20update%20grey-09.png", // LOGO
				3600,
				commMode,
				ipAddress,
				port);
		
		this.toolkit = toolkit;
	}

	@Override
	public String[] getInterface(ContextSubscriptionInfo subscription)
	{
		return new String[] { "WEBSITE=http://gcf.cmu-tbank.com/apps/favors/submitFavor.php?deviceID=" + subscription.getDeviceID() + "&mode=offer"};
	}

	@Override
	public void onComputeInstruction(ComputeInstruction instruction)
	{
		super.onComputeInstruction(instruction);
	}

	public int getLifetime(String userContextJSON)
	{
		return this.lifetime;
	}
	
	@Override
	public boolean sendAppData(String json)
	{
		JSONContextParser parser = new JSONContextParser(JSONContextParser.JSON_TEXT, json);
		
		try
		{
			String    query   = String.format("SELECT device_id FROM favors_profile WHERE device_id='%s' AND status=1;", this.getDeviceID(parser)); 
			ResultSet results = toolkit.runQuery(query);
			return results.next();
		}
		catch (Exception ex)
		{
			System.out.println("PRoblem Occurred Checking DeviceID Against Database: " + ex.getMessage());
		}
		
		return false;
	}
}
