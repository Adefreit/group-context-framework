package impromptu_apps.favors;


import java.util.Calendar;

import impromptu_apps.DesktopApplicationProvider;

import com.adefreitas.desktopframework.toolkit.JSONContextParser;
import com.adefreitas.groupcontextframework.CommManager.CommMode;
import com.adefreitas.groupcontextframework.ContextSubscriptionInfo;
import com.adefreitas.groupcontextframework.GroupContextManager;
import com.adefreitas.messages.ComputeInstruction;

public class App_FavorViewAll extends DesktopApplicationProvider
{	
	public App_FavorViewAll(GroupContextManager groupContextManager, CommMode commMode, String ipAddress, int port)
	{
		super(groupContextManager, 
				"FAVORS_VIEWALL",
				"View Favors",
				"View reported problems (based on the events categories specified in your profile)",
				"FAVOR BANK",
				new String[] { },  // Contexts
				new String[] { },  // Preferences
				"http://www.iconsdb.com/icons/preview/soylent-red/view-details-xxl.png", // LOGO
				3600,
				commMode,
				ipAddress,
				port);
	}

	@Override
	public String[] getInterface(ContextSubscriptionInfo subscription)
	{		
		return new String[] { "WEBSITE=http://gcf.cmu-tbank.com/apps/favors/viewAllFavors.php?deviceID=" + subscription.getDeviceID()};
	}

	@Override
	public void onComputeInstruction(ComputeInstruction instruction)
	{
		super.onComputeInstruction(instruction);
	}

	@Override
	public boolean sendAppData(String json)
	{
		JSONContextParser parser = new JSONContextParser(JSONContextParser.JSON_TEXT, json);
		
		// Must have SOME roles
		if (parser.getJSONObject("preferences").has("roles"))
		{
			if (parser.getJSONObject("preferences").get("roles").getAsString().length() == 0)
			{
				return false;
			}
		}
		
		return this.hasEmailAddress(parser, new String[] {"adrian.defreitas@gmail.com", "gcf.user.1@gmail.com"});
	}
	
}
