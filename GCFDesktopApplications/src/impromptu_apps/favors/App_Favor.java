package impromptu_apps.favors;

import impromptu_apps.DesktopApplicationProvider;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.adefreitas.gcf.ContextSubscriptionInfo;
import com.adefreitas.gcf.GroupContextManager;
import com.adefreitas.gcf.CommManager.CommMode;
import com.adefreitas.gcf.desktop.toolkit.JSONContextParser;
import com.adefreitas.gcf.desktop.toolkit.SQLToolkit;
import com.adefreitas.gcf.messages.ComputeInstruction;
import com.google.gson.JsonObject;

public class App_Favor extends DesktopApplicationProvider
{	
	public static final int TIME_TO_COMPLETE = 3600;
	
	// Properties
	private String	   		  id;
	private FavorDispatcher   dispatcher;
	private int		   		  timestamp;
	private String			  deviceID;
	private String			  userName;
	private String 	   		  description;
	private String 	   		  desc_performance;
	private String 	   		  desc_turnin;
	private double	   		  latitude;
	private double	   		  longitude;
	private String			  submissionType;
	private ArrayList<String> tags;
	private ArrayList<String> sensors;
	private ArrayList<String> matches;
	private String	   		  status;
	
	// SQL Database Query Tool
	private SQLToolkit sqlToolkit;
	
	// Use Metrics
	private Date	  		  dateCreated;
	private Date	  		  dateCompleted;
	private int       		  views;
	private boolean   		  completed;
	private ArrayList<String> devicesOffered;
	
	// Performance Variables
	private boolean hasBeenAccepted  = false;
	private String  acceptedDeviceID = "";
	private Date    acceptedDate     = new Date(0);
	private Date	lastTransmission = new Date(0);
	
	/**
	 * Constructor
	 */
	public App_Favor(String id, FavorDispatcher dispatcher, int timestamp, String deviceID, String userName, String submissionType, String description, String desc_performance, 
			String desc_turnin, double latitude, double longitude, String[] tags, String[] sensors, String status, String[] matches, 
			GroupContextManager groupContextManager, CommMode commMode, String ipAddress, int port, SQLToolkit sqlToolkit)
	{
		super(	groupContextManager, 
				id, 
				"Favor Request", 
				"description", 
				"FAVORS",
				new String[] { }, 
				new String[] { }, 
				"http://25.media.tumblr.com/tumblr_kvewyajk5c1qzxzwwo1_500.jpg",
				30,
				commMode, 
				ipAddress, 
				port,
				"FAVOR");
		
		this.timestamp   	  = timestamp;
		this.id			 	  = id;
		this.dispatcher  	  = dispatcher;
		this.sqlToolkit  	  = sqlToolkit;
		this.sensors 	 	  = new ArrayList<String>();
		this.tags 			  = new ArrayList<String>();
		this.matches		  = new ArrayList<String>();
		this.dateCreated 	  = new Date();
		this.devicesOffered   = new ArrayList<String>();
		this.submissionType   = submissionType;
		
		update(deviceID, userName, description, desc_performance, desc_turnin, latitude, longitude, tags, sensors, status, matches);
	}
		
	/**
	 * Generates a Custom Interface for this Subscription
	 */
	@Override
	public String[] getInterface(ContextSubscriptionInfo subscription)
	{
		String parameter = "";
		
		if (completed)
		{
			if (hasBeenAccepted)
			{
				parameter = "mode=completed" + "&completedBy=" + acceptedDeviceID + "&dateCompleted=" + dateCompleted.getTime();
			}
			else
			{
				parameter = "mode=canceled";
			}
		}
		else if (subscription.getDeviceID().equals(this.deviceID))
		{
			parameter = "mode=status";
			
			if (hasBeenAccepted)
			{
				parameter += "&deviceID=" + acceptedDeviceID + "&acceptedDate=" + acceptedDate.getTime();
			}
			else
			{
				parameter += "&numOffers=" + devicesOffered.size();
			}
		}
		else if (hasBeenAccepted)
		{
			parameter = "mode=accepted" + "&thisDevice=" + subscription.getDeviceID().equals(acceptedDeviceID);
		}
		else
		{
			parameter = "mode=open";
		}
		
		// Determines if we should force a refresh on the user's device
		String forceMode = "FORCE=FALSE";
//		if ((System.currentTimeMillis() - lastTransmission.getTime()) > 60000)
//		{
//			lastTransmission = new Date();
//			forceMode = "FORCE=true";
//		}
		
		return new String[] { "WEBSITE=" + "http://gcf.cmu-tbank.com/apps/favors/viewFavor.php?timestamp=" + timestamp + "&" + parameter, forceMode};
	}

	/**
	 * Logic for when a User Subscribes
	 */
	@Override
	public void onSubscription(ContextSubscriptionInfo newSubscription)
	{
		super.onSubscription(newSubscription);
		
		ResultSet viewerInfo = sqlToolkit.runQuery("SELECT * FROM favors_profile WHERE device_id='" + newSubscription.getDeviceID() + "'");
		
		try
		{
			if (viewerInfo.next())
			{
				String sensor   = viewerInfo.getString("last_sensor") != null ? viewerInfo.getString("last_sensor") : "";	
				String contents = String.format("favorID=%s,creatorDeviceID=%s,timestamp=%d,submissionType=request,viewerDeviceID=%s,viewerLat=%f,viewerLon=%f,viewerSensor=%s,sensorTimestamp=%d",
						this.getAppID(), this.deviceID, System.currentTimeMillis(), newSubscription.getDeviceID(), viewerInfo.getDouble("latitude"), viewerInfo.getDouble("longitude"), sensor, viewerInfo.getInt("last_sensor_date"));
				this.log("FAVOR_VIEWED", contents);
				
				views++;
			}
		}
		catch (Exception ex)
		{
			System.out.println("Error Occurred While Logging FAVOR_VIEWED: " + ex.getMessage());
		}
	}
	
	/**
	 * Logic for when a User Unsubscribes
	 */
	@Override
	public void onSubscriptionCancelation(ContextSubscriptionInfo subscription)
	{
		super.onSubscriptionCancelation(subscription);
		
		System.out.println(subscription.getDeviceID() + " has unsubscribed.");
	}
	
	/**
	 * Determines Whether or Not to Send Application Data
	 */
	@Override
	public boolean sendAppData(String bluewaveContext)
	{        
		JSONContextParser parser   = new JSONContextParser(JSONContextParser.JSON_TEXT, bluewaveContext);
		String 			  deviceID = this.getDeviceID(parser);
		
		// This will make the text output pretty.  Trust me.
		System.out.print("\n    Matches: " + matches + ": ");
		
		boolean result = !completed && (this.deviceID.equals(deviceID) || canViewAll(parser) || isMatch(parser) || deviceID.equals(acceptedDeviceID) || (isMatch(parser) && submissionType.equalsIgnoreCase("offer")));
		
		// Creates a Log Entry
		if (result && !this.deviceID.equals(deviceID) && !devicesOffered.contains(deviceID))
		{
			devicesOffered.add(deviceID);
			double latitude  = parser.getJSONObject("location").has("LATITUDE")  ?  parser.getJSONObject("location").get("LATITUDE").getAsDouble() : 0.0;
			double longitude = parser.getJSONObject("location").has("LONGITUDE") ? parser.getJSONObject("location").get("LONGITUDE").getAsDouble() : 0.0;
			String sensors   = parser.getJSONObject("location").has("SENSOR")    ? parser.getJSONObject("location").get("SENSOR").getAsString()    : "";
			String contents  = String.format("favorID=%s,creatorDeviceID=%s,timestamp=%d,submissionType=request,matchedDeviceID=%s,matchedLatitude=%f,matchedLongitude=%f,matchedSensor=%s",
					this.getAppID(), this.deviceID, System.currentTimeMillis(), deviceID, latitude, longitude, sensors);
			this.log("FAVOR_SENT", contents);
		}
		
		return result;
	}

	/**
	 * Handles Compute Instructions Received from Clients
	 */
	@Override
	public void onComputeInstruction(ComputeInstruction instruction)
	{
		System.out.println("Received Instruction: " + instruction.toString());
		
		if (instruction.getCommand().equalsIgnoreCase("MARK_COMPLETE"))
		{
			System.out.println(instruction.getPayload("completed"));
			
			// Updates the Database
			completed     = true;
			dateCompleted = new Date();
			
			// Logs the Completion
			if (instruction.getPayload("completed").equalsIgnoreCase("false"))
			{
				String logEntry = "favorID=" + this.getAppID() + ",";
				logEntry	   += "deviceID=" + deviceID + ",";
				logEntry	   += "timestamp=" + System.currentTimeMillis() + ",";
				logEntry       += "submissionType=" + "requested" + ",";
				
				dispatcher.markComplete(timestamp, false);
				this.log("FAVOR_CANCELED", logEntry);
			}
			else if (acceptedDeviceID.length() > 0)
			{
				String logEntry = "favorID=" + this.getAppID() + ",";
				logEntry	   += "creatorDeviceID=" + deviceID + ",";
				logEntry	   += "timestamp=" + System.currentTimeMillis()+ ",";
				logEntry       += "submissionType=" + "requested" + ",";
				logEntry       += "completedByDeviceID=" + acceptedDeviceID + "";
				
				dispatcher.markComplete(timestamp, true);
				this.log("FAVOR_COMPLETED",  logEntry);
			}
		}
		else if (instruction.getCommand().equalsIgnoreCase("ACCEPT_FAVOR"))
		{
			hasBeenAccepted  = true;
			acceptedDeviceID = instruction.getDeviceID();
			acceptedDate     = new Date();
			
			// Generates the Log Entry
			ResultSet viewerInfo = sqlToolkit.runQuery("SELECT * FROM favors_profile WHERE device_id='" + instruction.getDeviceID() + "'");
			
			try
			{
				if (viewerInfo.next())
				{
					String sensor   = viewerInfo.getString("last_sensor") != null ? viewerInfo.getString("last_sensor") : "";	
					String contents = String.format("favorID=%s,creatorDeviceID=%s,timestamp=%d,submissionType=request,viewerDeviceID=%s,viewerLat=%f,viewerLon=%f,viewerSensor=%s,sensorTimestamp=%d",
							this.getAppID(), this.deviceID, System.currentTimeMillis(), instruction.getDeviceID(), viewerInfo.getDouble("latitude"), viewerInfo.getDouble("longitude"), sensor, viewerInfo.getInt("last_sensor_date"));
					this.log("FAVOR_ACCEPTED", contents);
				}
			}
			catch (Exception ex)
			{
				System.out.println("Error Occurred While Logging FAVOR_ACCEPTED: " + ex.getMessage());
			}
		}
		else if (instruction.getCommand().equalsIgnoreCase("BACK_OUT"))
		{
			hasBeenAccepted = false;
			acceptedDeviceID = "";
			acceptedDate = new Date(0);
			
			// Generates the Log Entry
			ResultSet viewerInfo = sqlToolkit.runQuery("SELECT * FROM favors_profile WHERE device_id='" + instruction.getDeviceID() + "'");
			
			try
			{
				if (viewerInfo.next())
				{
					String sensor   = viewerInfo.getString("last_sensor") != null ? viewerInfo.getString("last_sensor") : "";	
					String contents = String.format("favorID=%s,timestamp=%d,submissionType=request,viewerDeviceID=%s,viewerLat=%f,viewerLon=%f,viewerSensor=%s,sensorTimestamp=%d",
							this.getAppID(), this.timestamp, instruction.getDeviceID(), viewerInfo.getDouble("latitude"), viewerInfo.getDouble("longitude"), sensor, viewerInfo.getInt("last_sensor_date"));
					this.log("FAVOR_BACKEDOUT", contents);
				}
			}
			catch (Exception ex)
			{
				System.out.println("Error Occurred While Logging FAVOR_BACKEDOUT: " + ex.getMessage());
			}
		}
		else if (instruction.getCommand().equalsIgnoreCase("SEND_MESSAGE"))
		{
			String contents = String.format("favorID=%s,creatorDeviceID=%s,timestamp=%d,messengerDeviceID=%s", 
					this.getAppID(), this.deviceID, System.currentTimeMillis(), instruction.getDeviceID());
			this.log("MESSAGE_EXCHANGE", contents);
		}
		
		this.sendContext();
	}
	
	/**
	 * This method returns the name of the application
	 * You can return a flat string, or have a dynamic name based on the user
	 */
	public String getName(String userContextJSON)
	{
		JSONContextParser parser   = new JSONContextParser(JSONContextParser.JSON_TEXT, userContextJSON);
		String 			  deviceID = this.getDeviceID(parser);
		String 			  result   = "";
		
		if (deviceID.equals(this.deviceID))
		{
			result = (submissionType.equalsIgnoreCase("request")) ? "Your Request" : "Your Offer";
		}
		else
		{
			result = (submissionType.equalsIgnoreCase("request")) ? "Favor for " + userName : "Offer from " + userName;
		}
		
		long timeInMinutes = (System.currentTimeMillis() - this.dateCreated.getTime()) / 1000 / 60;
		long timeInHours   = timeInMinutes / 60;
		
		if (timeInHours > 0)
		{
			result += " (" + timeInHours + " hrs old)";	
		}
		else
		{
			result += " (" + timeInMinutes + " min old)";
		}
		 
		
		return result;
	}
	
	/**
	 * This method returns the category of this application
	 */
	public String getCategory(String userContextJSON)
	{
		JSONContextParser parser   = new JSONContextParser(JSONContextParser.JSON_TEXT, userContextJSON);
		String 			  deviceID = this.getDeviceID(parser);
		
		if (deviceID.equals(this.deviceID))
		{
			return "FAVORS (Yours)";
		}
		else
		{
			return "FAVORS (Others)";	
		}
	}
	
	/**
	 * This method returns the description of the application
	 * You can return a 
	 */
	public String getDescription(String userContextJSON)
	{
		JSONContextParser parser   = new JSONContextParser(JSONContextParser.JSON_TEXT, userContextJSON);
		String 			  deviceID = this.getDeviceID(parser);
		String 			  result   = "";
		
		result += this.description + "\n\nStatus: ";
		
		if (completed)
		{
			result += "Completed";
		}
		else if (hasBeenAccepted)
		{
			result += "Accepted";
		}
		else
		{
			result += "Available";
		}
		
		return result;
	}
	
	/**
	 * This method returns the amount of time that this app is visible at once
	 */
	public int getLifetime(String userContextJSON)
	{
		if (completed)
		{
			return 0;
		}
		else
		{
			return 60;
		}
	}
	
	/**
	 * This method returns TRUE if the favor was reported (by the requester) to be completed
	 * @return
	 */
	public boolean isCompleted()
	{
		return completed;
	}
	
	/**
	 * Returns the Device ID that Requested/Offered the Favor
	 * @return
	 */
	public String getDeviceID()
	{
		return deviceID;
	}
	
	/**
	 * This method updates the contents
	 * @param description
	 * @param desc_performance
	 * @param desc_turnin
	 * @param latitude
	 * @param longitude
	 * @param tags
	 * @param sensors
	 * @param status
	 */
	public void update(String deviceID, String userName, String description, String desc_performance, String desc_turnin, double latitude, double longitude, String[] tags, String[] sensors, String status, String matches[])
	{
		this.deviceID		  = deviceID;
		this.userName		  = userName;
		this.description 	  = description;
		this.desc_performance = desc_performance;
		this.desc_turnin 	  = desc_turnin;
		this.latitude    	  = latitude;
		this.longitude   	  = longitude;
		this.status		 	  = status;
		
		// Clears the List of Sensors and Tags for New Additions
		this.tags.clear();
		this.sensors.clear();
		this.matches.clear();
		
		for (String tag : tags)
		{
			if (tag.length() > 0)
			{
				this.tags.add(tag);
			}
		}
		
		for (String sensor : sensors)
		{
			if (sensor.length() > 0)
			{
				this.sensors.add(sensor);
			}
		}
		
		for (String match : matches)
		{
			if (match.length() > 0)
			{
				this.matches.add(match);
			}
		}
		
		if (System.currentTimeMillis() - acceptedDate.getTime() > 1000 * TIME_TO_COMPLETE)
		{
			hasBeenAccepted = false;
			acceptedDeviceID = "";
			acceptedDate = new Date(0);
			this.sendContext();
		}
	}

	/**
	 * Returns TRUE if the device is on a list to see all favors; FALSE otherwise
	 * @param parser
	 * @return
	 */
	private boolean canViewAll(JSONContextParser parser)
	{
		boolean result = this.hasEmailAddress(
			parser, 
			new String[] 
			{
				"adoryab@gmail.com"
			}
		);
		return result;
	}
	
	/**
	 * This method returns TRUE if the device is within a specified distance of the user; returns FALSE otherwise
	 * @param parser
	 * @return
	 */
	private boolean distanceCheck(JSONContextParser parser)
	{
		if (latitude == 0.0 && longitude == 0.0)
		{
			System.out.println("    No Lat/Lon Coordinate Specified.  Returning TRUE");
			return true;
		}
		else
		{
			double radius   = 0.5;  // in KM
			double distance = this.getDistance(parser, latitude, longitude);
			
			System.out.println("    Distance: " + distance + " km (" + (distance < radius) + ")");
			
			return distance < radius;
		}
	}
	
	/**
	 * This method returns TRUE if the user's tag matches at least one favor tag; returns FALSE otherwise
	 * @param parser
	 * @return
	 */
	private boolean tagsMatch(JSONContextParser parser)
	{
		JsonObject preferencesObj = parser.getJSONObject("preferences");
		
		if (preferencesObj != null && preferencesObj.has("tags"))
		{
			String[] userTags = preferencesObj.get("tags").getAsString().split(",");
			
			//System.out.println("    User Tags:  " + Arrays.toString(userTags));
			//System.out.println("    Favor Tags: " + tags);
			
			for (String tag : userTags)
			{
				if (tags.contains(tag))
				{
					System.out.println("    MATCH TAG: " + tag);
					return true;
				}
			}
		}
		
		System.out.println("    No Tag Match");
		return false;
	}

	/**
	 * This method returns TRUE if the sensor that detected the favor is conta
	 * @param parser
	 * @return
	 */
	private boolean sensorsMatch(JSONContextParser parser)
	{
		if (sensors.size() == 0)
		{
			System.out.println("    No Sensors Specified by the Favor.  Returning TRUE");
			return true;
		}
		else
		{
			System.out.println("    Looking for Sensor: " + sensors + " (" + sensors.size() + ")");
			
			// Sensor Information is Automatically Embedded in the JSON by THIS APPLICATION
			JsonObject locationObj = parser.getJSONObject("location");
			
			// HINT:  ALWAYS CHECK TO SEE IF A DEVICE HAS THE JSON TAG
			// There are older GCF devices in the environment that have not received the new code
			if (locationObj != null)
			{
				String source = locationObj.has("SENSOR") ? locationObj.get("SENSOR").getAsString() : "IMPROMPTU_APP_DIRECTORY";
				System.out.println("    SENSOR: " + source);
				
				if (sensors.contains(source))
				{
					System.out.println("    SENSOR MATCH: " + source);
					return true;
				}
			}
			
			System.out.println("    No Sensor Match.");
			return false;	
		}
	}

	/**
	 * This method returns TRUE if the deviceID is in the list of matches; returns FALSE otherwise
	 * @param parser
	 * @return
	 */
	private boolean isMatch(JSONContextParser parser)
	{
		return matches.contains(this.getDeviceID(parser));
	}
}
