package impromptu_apps.desktop;

import impromptu_apps.DesktopApplicationProvider;

import java.util.ArrayList;
import java.util.Calendar;

import com.adefreitas.gcf.ContextSubscriptionInfo;
import com.adefreitas.gcf.GroupContextManager;
import com.adefreitas.gcf.CommManager.CommMode;
import com.adefreitas.gcf.desktop.toolkit.JSONContextParser;
import com.adefreitas.gcf.messages.CommMessage;
import com.adefreitas.gcf.messages.ComputeInstruction;

public class App_CMU_Bus extends DesktopApplicationProvider
{
	public static final double MIN_DISTANCE_IN_KM = 0.10;
	
	// Stores Coordinates
	private ArrayList<Location> locations;
	
	/**
	 * Constructor
	 * @param groupContextManager
	 * @param commMode
	 * @param ipAddress
	 * @param port
	 */
	public App_CMU_Bus(GroupContextManager groupContextManager, CommMode commMode, String ipAddress, int port)
	{
		super(groupContextManager, 
				"CMUBUS",
				"CMU Shuttle Services",
				"Provides Real Time Information about the CMU Shuttle System.",
				"TRANSPORTATION",
				new String[] { },  // Contexts
				new String[] { },  // Preferences
				"http://st.depositphotos.com/3538103/5164/i/170/depositphotos_51646411-Bus-icon-design.jpg",				   // LOGO
				120,
				commMode,
				ipAddress,
				port);
		
		locations = new ArrayList<Location>();
		
		populateLocations();
	}
	
	private void populateLocations()
	{
		// Populates coordinates
		locations.add(new Location(false, false, false, true, 40.444568, -79.945951));
		locations.add(new Location(true, false, true, false, 40.444511, -79.948759));
		locations.add(new Location(true, false, true, false, 40.445702, -79.950432));
		locations.add(new Location(false, false, false, true, 40.443166, -79.953573));
		locations.add(new Location(true, false, false, false, 40.447063, -79.950681));
		locations.add(new Location(true, false, true, false, 40.448858, -79.951734));
		locations.add(new Location(true, false, true, false, 40.451472, -79.953276));
		locations.add(new Location(true, false, false, false, 40.452278, -79.950846));
		locations.add(new Location(true, false, false, false, 40.452560, -79.949969));
		locations.add(new Location(true, false, true, false, 40.453570, -79.946386));
		locations.add(new Location(true, false, true, false, 40.454165, -79.944464));
		locations.add(new Location(true, false, true, false, 40.455032, -79.941481));
		locations.add(new Location(true, false, true, false, 40.455900, -79.938582));
		locations.add(new Location(false, false, true, false, 40.453456, -79.935193));
		locations.add(new Location(false, false, true, false, 40.454225, -79.933572));
		locations.add(new Location(true, false, false, false, 40.457195, -79.934014));
		locations.add(new Location(false, true, true, false, 40.456570, -79.933629));
		locations.add(new Location(true, true, true, false, 40.454735, -79.932545));
		locations.add(new Location(true, true, false, false, 40.453423, -79.931773));
		locations.add(new Location(true, true, false, false, 40.452064, -79.930962));
		locations.add(new Location(true, true, false, false, 40.451201, -79.930478));
		locations.add(new Location(false, true, true, true, 40.449820, -79.929654));
		locations.add(new Location(true, true, true, false, 40.448921, -79.932547));
		locations.add(new Location(true, true, true, true, 40.448402, -79.934214));
		locations.add(new Location(true, true, true, true, 40.447811, -79.937131));
		locations.add(new Location(true, true, true, false, 40.445099, -79.942770));
		locations.add(new Location(false, false, false, true, 40.444459, -79.942039));
		locations.add(new Location(false, false, false, true, 40.437843, -79.930714));
		locations.add(new Location(false, false, false, true, 40.435752, -79.927686));
		locations.add(new Location(false, false, false, true, 40.434123, -79.927623));
		locations.add(new Location(false, false, false, true, 40.434763, -79.922824));
		locations.add(new Location(false, false, false, true, 40.435329, -79.918996));
		locations.add(new Location(false, true, true, true, 40.452878, -79.920712));
		locations.add(new Location(false, false, false, true, 40.452582, -79.922368));
		locations.add(new Location(false, true, true, true, 40.451957, -79.923993));
		locations.add(new Location(false, true, true, true, 40.451217, -79.925804));
		locations.add(new Location(false, true, true, true, 40.450520, -79.927507));
		locations.add(new Location(false, true, true, true, 40.454532, -79.921740));
		locations.add(new Location(false, true, true, true, 40.455884, -79.922566));
		locations.add(new Location(false, true, true, true, 40.457222, -79.922384));
		locations.add(new Location(false, true, true, false, 40.459166, -79.922034));
		locations.add(new Location(false, true, true, false, 40.460314, -79.921870));
		locations.add(new Location(false, true, true, false, 40.460261, -79.923945));
		locations.add(new Location(false, true, true, false, 40.459551, -79.926387));
		locations.add(new Location(false, true, true, false, 40.458791, -79.928915));
		locations.add(new Location(false, false, false, true, 40.456095, -79.915414));
		locations.add(new Location(false, false, false, true, 40.453613, -79.915326));
	}
	
	private Location getNearestLocation(JSONContextParser parser, double threshold)
	{
		Location bestLocation = null;
		double   bestDistance = Double.MAX_VALUE;
		
		for (Location location : locations)
		{
			double distance = this.getDistance(parser, location.latitude, location.longitude);
			
			if (distance < threshold && distance < bestDistance)
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
		return new String[] { "WEBSITE=http://www.andysbuses.com" };
	}
	
	@Override
	public void onComputeInstruction(ComputeInstruction instruction)
	{
		super.onComputeInstruction(instruction);
	}

	@Override
	public boolean sendAppData(String json)
	{
		JSONContextParser parser 		  = new JSONContextParser(JSONContextParser.JSON_TEXT, json);
		Location 		  nearestLocation = getNearestLocation(parser, MIN_DISTANCE_IN_KM);
		
		return (nearestLocation != null && !this.inVehicle(parser) && this.hasEmailDomain(parser, new String[] { "cs.cmu.edu", "andrew.cmu.edu" }));
	}
	
	public String getDescription(String userContextJSON)
	{
		JSONContextParser parser 		  = new JSONContextParser(JSONContextParser.JSON_TEXT, userContextJSON);
		Location 		  nearestLocation = getNearestLocation(parser, MIN_DISTANCE_IN_KM);
		
		if (nearestLocation != null)
		{
			return "Provides Real Time Information about the CMU Shuttle System.\n" + nearestLocation.getDescription();
		}
		else
		{
			return description;
		}
	}
	
	private class Location
	{
		public double  latitude, longitude;
		public boolean a, b, ab, bs;
		
		public Location(boolean a, boolean b, boolean ab, boolean bs, double latitude, double longitude)
		{
			this.latitude  = latitude;
			this.longitude = longitude;
			this.a 		   = a;
			this.b 		   = b;
			this.ab 	   = ab;
			this.bs 	   = bs;
		}
		
		public String getDescription()
		{
			String description = "Available Routes: ";
			
			if (a)
			{
				description += "A ";
			}
			
			if (b)
			{
				description += "B ";
			}
			
			if (ab)
			{
				description += "AB ";
			}
			
			if (bs)
			{
				description += "Bakery Square";
			}
			return description;
		}
	}
}
