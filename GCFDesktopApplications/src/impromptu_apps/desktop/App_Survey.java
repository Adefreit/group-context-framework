package impromptu_apps.desktop;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import impromptu_apps.DesktopApplicationProvider;

import com.adefreitas.gcf.ContextSubscriptionInfo;
import com.adefreitas.gcf.GroupContextManager;
import com.adefreitas.gcf.CommManager.CommMode;
import com.adefreitas.gcf.desktop.toolkit.JSONContextParser;
import com.adefreitas.gcf.desktop.toolkit.SQLToolkit;
import com.adefreitas.gcf.messages.ComputeInstruction;

public class App_Survey extends DesktopApplicationProvider
{	
	public static final String   CONTEXT_TYPE  	      = "IMP_SURVEY";
	public static final String   DEFAULT_TITLE 	      = "Impromptu Survey";
	public static final String   DEFAULT_DESCRIPTION  = "This survey asks questions about your experience with Impromptu. It only takes 5 minutes to complete!";
	public static final String   DEFAULT_CATEGORY     = "SURVEY";
	public static final String[] CONTEXTS_REQUIRED    = new String[] { };
	public static final String[] PREFERENCES_REQUIRED = new String[] { };
	public static final String   DEFAULT_LOGO_PATH    = "https://panorama-www.s3.amazonaws.com/sites/53d29249e511304c2f000002/theme/images/icon-admin.png";
	public static final int      DEFAULT_LIFETIME	  = 300;
	
	private String	   surveyID	   = "";
	private SQLToolkit sqlToolkit;
	
	/**
	 * Constructor
	 * @param application
	 * @param groupContextManager
	 * @param contextType
	 * @param name
	 * @param description
	 * @param contextsRequired
	 * @param preferencesToRequest
	 * @param logoPath
	 * @param commMode
	 * @param ipAddress
	 * @param port
	 */
	public App_Survey(String surveyID, GroupContextManager groupContextManager, CommMode commMode, String ipAddress, int port, SQLToolkit sqlToolkit)
	{
		super(groupContextManager, 
				CONTEXT_TYPE, 
				DEFAULT_TITLE, 
				DEFAULT_DESCRIPTION, 
				DEFAULT_CATEGORY, 
				CONTEXTS_REQUIRED, 
				PREFERENCES_REQUIRED, 
				DEFAULT_LOGO_PATH, 
				DEFAULT_LIFETIME,				
				commMode, ipAddress, port);
		
		this.surveyID   = surveyID;
		this.sqlToolkit = sqlToolkit;
	}
		
	/**
	 * Generates a Custom Interface for this Subscription
	 */
	@Override
	public String[] getInterface(ContextSubscriptionInfo subscription)
	{
		return new String[] { "WEBSITE=https://docs.google.com/forms/d/1pTR6DRaNYU2cR1yQNGCTiOP5KTCln2danN216HJcc5k/viewform" };
	}

	/**
	 * Event Called When a Client Connects
	 */
	@Override
	public void onSubscription(ContextSubscriptionInfo newSubscription)
	{
		super.onSubscription(newSubscription);
		
		try
		{
			sqlToolkit.runUpdateQuery(String.format("INSERT INTO impromptu_survey (deviceID, surveyID, date) VALUES ('%s', '%s', CURRENT_TIMESTAMP)",newSubscription.getDeviceID(), surveyID));	
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * Event Called with a Client Disconnects
	 */
	@Override
	public void onSubscriptionCancelation(ContextSubscriptionInfo subscription)
	{
		super.onSubscriptionCancelation(subscription);
		
		//Toast.makeText(this.getApplication(), subscription.getDeviceID() + " has unsubscribed.", Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Determines Whether or Not to Send Application Data
	 */
	@Override
	public boolean sendAppData(String bluewaveContext)
	{
		JSONContextParser parser   = new JSONContextParser(JSONContextParser.JSON_TEXT, bluewaveContext);
		String 			  deviceID = this.getDeviceID(parser);
		
		try
		{
			String    query  = "SELECT COUNT(*) FROM impromptu_survey WHERE deviceID='" + deviceID + "' AND surveyID = '" + surveyID + "';";
			ResultSet result = sqlToolkit.runQuery(query);
			
			if (result != null && result.next())
			{
				int count = result.getInt(1);
				return count == 0;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return false;
	}

	/**
	 * Handles Compute Instructions Received from Clients
	 */
	@Override
	public void onComputeInstruction(ComputeInstruction instruction)
	{
		System.out.println("Received Instruction: " + instruction.toString());
	}

	/**
	 * OPTIONAL:  Allows you to Customize the Name of the App on a Per User Basis
	 */
	public String getName(String userContextJSON)
	{
		return name;
	}
	
	/**
	 * OPTIONAL:  Allows you to Customize the Category of the App on a Per User Basis
	 */
	public String getCategory(String userContextJSON)
	{
		return category;
	}
	
	/**
	 * OPTIONAL:  Allows you to Customize the Description of the App on a Per User Basis
	 */
	public String getDescription(String userContextJSON)
	{
		return description;
	}
	
	/**
	 * OPTIONAL:  Allows you to Customize the Lifetime of an App on a Per User Basis
	 */
	public int getLifetime(String userContextJSON)
	{
		return this.lifetime;
	}

	/**
	 * OPTIONAL:  Allows you to Customize the Logo of an App on a Per User Basis
	 */
	public String getLogoPath(String userContextJSON)
	{
		return logoPath;
	}
}
