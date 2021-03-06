package impromptu_apps.desktop;

import impromptu_apps.DesktopApplicationProvider;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;


import com.adefreitas.gcf.ContextSubscriptionInfo;
import com.adefreitas.gcf.GroupContextManager;
import com.adefreitas.gcf.CommManager.CommMode;
import com.adefreitas.gcf.desktop.toolkit.JSONContextParser;
import com.adefreitas.gcf.messages.CommMessage;
import com.adefreitas.gcf.messages.ComputeInstruction;

public class App_LocationWebsite extends DesktopApplicationProvider
{
	private String					websiteURL;
	private double					minDistanceInKm;
	private ArrayList<LocationInfo> locations;
	
	/**
	 * Constructor
	 * @param groupContextManager
	 * @param name
	 * @param websiteURL
	 * @param description
	 * @param category
	 * @param logo
	 * @param commMode
	 * @param ipAddress
	 * @param port
	 */
	public App_LocationWebsite(GroupContextManager groupContextManager, String name, String websiteURL, String description, String category, String logo, double minDistanceInKm, CommMode commMode, String ipAddress, int port)
	{
		super(groupContextManager, 
				"LOC_" + name,
				name,
				description,
				category,
				new String[] { }, // Contexts
				new String[] { }, // Preferences
				logo, // LOGO
				120,
				commMode,
				ipAddress,
				port);

		this.websiteURL  	 = websiteURL;
		this.description 	 = description;
		this.minDistanceInKm = minDistanceInKm;
		this.logoPath 	 	 = logo;
		this.locations   	 = new ArrayList<LocationInfo>();
		
		// Populates coordinates
		// NOTE:  I am using X, Y for Longitude, Latitude (the reverse of what you normally think)
//		coordinates.put("Monroeville", new Point2D.Double(40.431253,-79.799495));
//		coordinates.put("Waterfront", new Point2D.Double(40.412222, -79.903049));
//		coordinates.put("Home", new Point2D.Double(40.434090, -79.853565));
//		coordinates.put("NSH", new Point2D.Double(40.443608, -79.945573));
	}
	
	public void addLocation(String locationName, double latitude, double longitude)
	{
		locations.add(new LocationInfo(locationName, websiteURL, description, category, logoPath, latitude, longitude));
	}
	
	public void addLocation(String locationName, double latitude, double longitude, String url)
	{
		locations.add(new LocationInfo(locationName, url, description, category, logoPath, latitude, longitude));
	}
		
	private LocationInfo getNearestLocation(JSONContextParser parser)
	{
		LocationInfo bestLocation = null;
		double 		 bestDistance = Double.MAX_VALUE;
		
		for (LocationInfo location : locations)
		{
			double distance = this.getDistance(parser, location.latitude, location.longitude);
			
			if (distance < bestDistance)
			{
				bestLocation = location;
				bestDistance = distance;
			}
		}
		
		return bestLocation;
	}

	@Override
	public String[] getInterface(ContextSubscriptionInfo subscription)
	{
		String  		  context 		  = CommMessage.getValue(subscription.getParameters(), "context");
		JSONContextParser parser  		  = new JSONContextParser(JSONContextParser.JSON_TEXT, context);
		LocationInfo	  closestLocation = getNearestLocation(parser);
		
		return new String[] { "WEBSITE=" + closestLocation.url };
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
	
		for (LocationInfo location : locations)
		{
			double distance = this.getDistance(parser, location.latitude, location.longitude);
			
			if (distance < minDistanceInKm)
			{
				return !this.inVehicle(parser);
			}
		}
		
		return false;
	}

	/**
	 * This class represents a single location
	 * @author adefreit
	 *
	 */
	private class LocationInfo
	{
		public String locationName;
		public String url;
		public String description;
		public String category;
		public String logo;
		public double latitude;
		public double longitude;
		
		public LocationInfo(String locationName, String url, String description, String category, String logo, double latitude, double longitude)
		{
			this.locationName = locationName;
			this.url 		  = url;
			this.description  = description;
			this.category 	  = category;
			this.logo 		  = logo;
			this.latitude 	  = latitude;
			this.longitude 	  = longitude;
		}
	}
}
