package impromptu_apps.desktop;

import impromptu_apps.DesktopApplicationProvider;

import java.awt.geom.Point2D;
import java.util.HashMap;


import com.adefreitas.gcf.ContextSubscriptionInfo;
import com.adefreitas.gcf.GroupContextManager;
import com.adefreitas.gcf.CommManager.CommMode;
import com.adefreitas.gcf.desktop.toolkit.JSONContextParser;
import com.adefreitas.gcf.messages.CommMessage;
import com.adefreitas.gcf.messages.ComputeInstruction;

public class App_Starbucks extends DesktopApplicationProvider
{
	// Stores Coordinates
	private HashMap<String, Point2D.Double> coordinates = new HashMap<String, Point2D.Double>();
	
	public App_Starbucks(GroupContextManager groupContextManager, CommMode commMode, String ipAddress, int port)
	{
		super(groupContextManager, 
				"STARB",
				"Starbucks App",
				"Launches the Starbucks App (or installs it) to let you pay for your coffee.",
				"SHOPPING",
				new String[] { },  // Contexts
				new String[] { },  // Preferences
				"http://2.bp.blogspot.com/-gUEhBpnLdCU/UJibeAya_8I/AAAAAAAAAb4/PXEpLCxJsuI/s1600/Starbucks_Corporation_Logo_2011.svg.png", // LOGO
				120,
				commMode,
				ipAddress,
				port);
		
		// Populates coordinates
		// NOTE:  I am using X, Y for Longitude, Latitude (the reverse of what you normally think)
		coordinates.put("Mall", new Point2D.Double(40.430981,-79.794758));
		coordinates.put("Waterfront", new Point2D.Double(40.407435, -79.917222));
		coordinates.put("Monroeville West", new Point2D.Double(40.429615, -79.810339));
		coordinates.put("Monroeville (Miracle Mile)", new Point2D.Double(40.437900, -79.768467));
		coordinates.put("Forbes/Craig", new Point2D.Double(40.44456700, -79.9485571));
		coordinates.put("Forbes Tower", new Point2D.Double(40.440932, -79.957643));
		coordinates.put("UPMC Shadyside", new Point2D.Double(40.455807, -79.939505));
		coordinates.put("Copeland St", new Point2D.Double(40.451680, -79.934961));
		coordinates.put("Squirrel Hill", new Point2D.Double(40.430686, -79.923124));
		coordinates.put("Penn Ave (Inside Target)", new Point2D.Double(40.460810, -79.921170));
		coordinates.put("East Side", new Point2D.Double(40.459022, -79.927600));
		coordinates.put("Liberty Ave", new Point2D.Double(40.461134, -79.947555));
		coordinates.put("Murray Ave", new Point2D.Double(40.430662, -79.923028));
		coordinates.put("East Carson St", new Point2D.Double(40.428656, -79.983987));
		coordinates.put("Park Manor", new Point2D.Double(40.452584, -80.164393));
		coordinates.put("Robinson Mall", new Point2D.Double(40.454441, -80.157002));
		coordinates.put("Summit Park", new Point2D.Double(40.448245, -80.179330));
		coordinates.put("Carnot-Moon", new Point2D.Double(40.516437, -80.219894));
		coordinates.put("McKnight Rd (Inside Target)", new Point2D.Double(40.526168, -80.008014));
		coordinates.put("Ross Park Mall", new Point2D.Double(40.543624, -80.007592));
		coordinates.put("Siebert Rd", new Point2D.Double(40.530281, -80.010725));
		coordinates.put("McKnight Rd", new Point2D.Double(40.546535, -80.017905));
		coordinates.put("Wexford (Near Sorgels)", new Point2D.Double(40.616908, -80.092646));
		coordinates.put("Wexford (Perry Hwy)", new Point2D.Double(40.622571, -80.053383));
	}
	
	private boolean nearLocation(JSONContextParser parser, double km)
	{
		double bestDistance = Double.MAX_VALUE;
		
		for (String locationName : coordinates.keySet())
		{
			Point2D.Double location = coordinates.get(locationName);
			double 		   distance = this.getDistance(parser, location.x, location.y);
			
			if (distance < bestDistance)
			{
				bestDistance = distance;
			}
		}
		
		return bestDistance <= km;
	}
	
	private String getNearestLocation(JSONContextParser parser)
	{
		String bestName     = "";
		double bestDistance = Double.MAX_VALUE;
		
		for (String locationName : coordinates.keySet())
		{
			Point2D.Double location = coordinates.get(locationName);
			double 		   distance = this.getDistance(parser, location.x, location.y);
			
			if (distance < bestDistance)
			{
				bestName     = locationName;
				bestDistance = distance;
			}
		}
		
		return bestName;
	}

	@Override
	public String[] getInterface(ContextSubscriptionInfo subscription)
	{
		String  		  context 		  = CommMessage.getValue(subscription.getParameters(), "context");
		JSONContextParser parser  		  = new JSONContextParser(JSONContextParser.JSON_TEXT, context);
		String 			  closestLocation = getNearestLocation(parser);
		
		return new String[] { "PACKAGE=com.starbucks.mobilecard" };
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
		return nearLocation(parser, 0.10) && !this.inVehicle(parser);
	}
}
