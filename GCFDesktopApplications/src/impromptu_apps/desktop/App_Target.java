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

public class App_Target extends DesktopApplicationProvider
{
	// Stores Coordinates
	private HashMap<String, Point2D.Double> coordinates = new HashMap<String, Point2D.Double>();
	
	public App_Target(GroupContextManager groupContextManager, CommMode commMode, String ipAddress, int port)
	{
		super(groupContextManager, 
				"TARGET_AD",
				"Target Application",
				"Gives you the weekly ad for this store.",
				"SHOPPING",
				new String[] { },  // Contexts
				new String[] { },  // Preferences
				"http://www.mcm.org/uploads/images/logos/TargetLogo.jpg", // LOGO
				120,
				commMode,
				ipAddress,
				port);
		
		// Populates coordinates
		coordinates.put("Monroeville Target", new Point2D.Double(40.433955,-79.772115));
		coordinates.put("Penn Ave. Target", new Point2D.Double(40.461107,-79.921803));
		coordinates.put("Waterfront Target", new Point2D.Double(40.410604, -79.910367));
		coordinates.put("Airport", new Point2D.Double(40.446222, -80.183681));
		coordinates.put("North Hills", new Point2D.Double(40.526077, -80.007947));
		coordinates.put("Freeport", new Point2D.Double(40.538371, -79.836601));
		coordinates.put("West Mifflin", new Point2D.Double(40.348166, -79.953947));
		coordinates.put("Mt Lebanon", new Point2D.Double(40.343678, -80.057118));
		coordinates.put("Monaca", new Point2D.Double(40.680884, -80.312491));
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
		
		return new String[] { "WEBSITE=http://m.weeklyad.target.com/" };
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
