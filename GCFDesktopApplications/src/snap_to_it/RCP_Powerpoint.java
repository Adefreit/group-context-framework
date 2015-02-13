package snap_to_it;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import toolkits.ScreenshotToolkit;

import com.adefreitas.desktopframework.DesktopGroupContextManager;
import com.adefreitas.desktopframework.MessageProcessor;
import com.adefreitas.groupcontextframework.ContextSubscriptionInfo;
import com.adefreitas.groupcontextframework.GroupContextManager;
import com.adefreitas.messages.CommMessage;
import com.adefreitas.messages.ComputeInstruction;
import com.adefreitas.messages.ContextData;

public class RCP_Powerpoint extends RemoteControlProvider implements MessageProcessor
{
	// Provider Specific Variables Go Here
	public		  String PRIMARY_DEVICE		   = "";
	public 		  String PRESENTATION_FILE     = "";                 							// The File (ON THE COMPUTER)
	public static String PRESENTATION_LOCATION = "";											// The Path to the File (ON THE COMPUTER)
	public static String UPLOAD_FOLDER         = "/var/www/html/gcf/universalremote/Server/";	// The Folder Containing the Presentation (ON SERVER)
	
	private boolean accMode = false;
	
	/**
	 * Constructor
	 * @param groupContextManager
	 */
	public RCP_Powerpoint(DesktopGroupContextManager groupContextManager) 
	{
		super(groupContextManager);
		
		// Enables Screenshot Comparison
		//this.addPhoto(APP_DATA_FOLDER + "projector1.jpg");
		//this.addPhoto(APP_DATA_FOLDER + "projector2.jpg");
		//this.addPhoto(APP_DATA_FOLDER + "projector3.jpg");
		//this.enableScreenshots(10000, 3);
		
		groupContextManager.registerOnMessageProcessor(this);
	}

	private void runPresentation(boolean quitExisting)
	{		
		System.out.println("Running presentation: " + PRESENTATION_FILE);
		
		Robot robot = this.getRobot();
		
		if (quitExisting)
		{
			robot.keyPress(KeyEvent.VK_ESCAPE);
			robot.delay(40);
			robot.keyRelease(KeyEvent.VK_ESCAPE);
			robot.delay(1000);
			
			robot.keyPress(KeyEvent.VK_META);
			robot.delay(40);
			robot.keyPress(KeyEvent.VK_Q);
			robot.delay(40);
			robot.keyRelease(KeyEvent.VK_META);
			robot.delay(40);
			robot.keyRelease(KeyEvent.VK_Q);
			robot.delay(1000);
		}
		
		try
		{
			// Runs Powerpoint
			this.executeRuntimeCommand("open " + PRESENTATION_LOCATION);
			
			// Gives the Program Time to Do What it Needs to Do
			Thread.sleep(1500);
			
			// Tries to Enter Presentation Mode
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.delay(40);
			robot.keyPress(KeyEvent.VK_META);
			robot.delay(40);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.delay(40);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			robot.delay(40);
			robot.keyRelease(KeyEvent.VK_META);
			robot.delay(40);
			robot.keyRelease(KeyEvent.VK_ENTER);
		}
		catch (Exception ex)
		{
			System.out.println("A problem occurred while setting up the PowerPoint Presentation: " + ex.getMessage());
		}
	}
	
	protected void initializeUserInterfaces()
	{
		super.initializeUserInterfaces();
	}
	
	@Override
	public void onSubscription(ContextSubscriptionInfo newSubscription)
	{
		super.onSubscription(newSubscription);
		
		// Prints out Preferences
		ArrayList<String> preferences = CommMessage.getValues(newSubscription.getParameters(), "preferences");
		
		if (preferences != null)
		{
			for (String preference : preferences)
			{
				System.out.println(" *** PREFERENCE: " + preference + " ***");
			}
		}
	}
	
	@Override
	public void onSubscriptionCancelation(ContextSubscriptionInfo subscription)
	{
		super.onSubscriptionCancelation(subscription);
		
		if (PRIMARY_DEVICE.equals(subscription.getDeviceID()))
		{
			PRIMARY_DEVICE = "";
			
			if (PRESENTATION_FILE.length() > 0)
			{
				PRESENTATION_FILE = "";
				
				Robot robot = this.getRobot();
				
				robot.keyPress(KeyEvent.VK_ESCAPE);
				robot.delay(40);
				robot.keyRelease(KeyEvent.VK_ESCAPE);
				robot.delay(1000);
				
				robot.keyPress(KeyEvent.VK_META);
				robot.delay(40);
				robot.keyPress(KeyEvent.VK_Q);
				robot.delay(40);
				robot.keyRelease(KeyEvent.VK_META);
				robot.delay(40);
				robot.keyRelease(KeyEvent.VK_Q);
				robot.delay(1000);
			}
		}
		
		this.getGroupContextManager().cancelRequest("ACC", subscription.getDeviceID());
		
		sendMostRecentReading();
	}
	
	// THIS IS THE METHOD THAT DELIVERS THE USER INTERFACE
	public void sendMostRecentReading()
	{
		for (ContextSubscriptionInfo subscription : this.getSubscriptions())
		{
			String ui = "<html><title>Digital Projector</title>";
			
//			// TODO:  Hack for Comm Talk
//			ui += "<div><h4>Slide Controls</h4><input value=\"left\" type=\"button\" style=\"height:75px; width:75px\" onclick=\"device.sendComputeInstruction('KEYPRESS', ['keycode=left']);\"/>" +
//			  "<input value=\"right\" type=\"button\" style=\"height:75px; width:75px\" onclick=\"device.sendComputeInstruction('KEYPRESS', ['keycode=right']);\"/></div>" +
//			  "<div><h4>When You Are Finished . . .</h4>" +
//			  "<input value=\"Quit Presentation\" type=\"button\" onclick=\"device.sendComputeInstruction('QUIT', []);\"/></div>" +
//			  "</html>";
			
			System.out.println("Sending User Interface to: " + subscription.getDeviceID());
			
			if (PRESENTATION_FILE.length() == 0)
			{
				ui += "<div><input value=\"Upload Presentation\" type=\"button\" style=\"height:50px; width:300px; font-size:25px\" onclick=\"device.uploadFile('PP_UPLOADED', '" + UPLOAD_FOLDER + "', ['.pptx']);\"/></div>";
				ui += "</html>";
			}
			else
			{
				if (PRIMARY_DEVICE.length() == 0 || subscription.getDeviceID().equals(PRIMARY_DEVICE))
				{
					ui += "<div><h4>Slide Controls</h4><input value=\"left\" type=\"button\" style=\"height:100px; width:100px; font-size:25px\" onclick=\"device.sendComputeInstruction('KEYPRESS', ['keycode=left']);\"/>" +
						  "<input value=\"right\" type=\"button\" style=\"height:100px; width:100px; font-size:25px\" onclick=\"device.sendComputeInstruction('KEYPRESS', ['keycode=right']);\"/></div>" +
						  "<div><h4>Switch Control Modes</h4>" +
						  "<input value=\"" + (accMode ? "Turn OFF Accelerometer" : "Turn ON Accelerometer") +"\" style=\"height:50px; font-size:25px\" type=\"button\" onclick=\"device.sendComputeInstruction('ACC_TOGGLE', []);\"/></div>" +
						  "<div><h4>When You Are Finished . . .</h4>" +
						  "<input value=\"Quit Presentation\" type=\"button\" style=\"height:50px; font-size:25px\" onclick=\"device.sendComputeInstruction('QUIT', []);\"/></div>" +
						  "</html>";
				}
				else
				{
					ui += "<div>" + 
							"<h5>Last Updated: " + new Date().toString() + "</h5><h5>Latest Screenshot</h5>"+
							"<img border=\"0\" src=\"http://71.182.231.215/gcf/universalremote/Server/screen.jpeg\" alt=\"Screenshot\" width=\"304\" height=\"228\">" +
							"<input value=\"Download Presentation\" type=\"button\" style=\"height:50px; font-size:25px\" onclick=\"device.downloadFile('" + UPLOAD_FOLDER + PRESENTATION_FILE + "');\"/>" + 
						  "</div>" +
						  "</html>";
				}
			}
			
			this.getGroupContextManager().sendContext(
					this.getContextType(), 
					"", 
					new String[] { subscription.getDeviceID() }, 
					new String[] { "UI=" + ui  });
		}
	}
	
	@Override
	public void onComputeInstruction(ComputeInstruction instruction)
	{
		System.out.println("Received Instruction: " + instruction.toString());
		
		Robot robot = this.getRobot();
		
		if (instruction.getCommand().equals("KEYPRESS"))
		{
			String key = CommMessage.getValue(instruction.getParameters(), "keycode");
			this.pressKey(key);
		}
		else if (instruction.getCommand().equals("PP_UPLOADED"))
		{		
			PRIMARY_DEVICE = instruction.getDeviceID();
			System.out.println("PRIMARY_DEVICE: " + PRIMARY_DEVICE);
			
			String filePath = CommMessage.getValue(instruction.getParameters(), "uploadPath");
			System.out.println("I got notified of an upload at: " + filePath);
			
			// TODO:  Remove me:  I test preferences Code
			this.setUserPreference(PRIMARY_DEVICE, "presentation", filePath);
			
			String folder      = filePath.substring(0, filePath.lastIndexOf("/") + 1);
			String filename    = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
			String destination = APP_DATA_FOLDER + instruction.getDeviceID().replace(" ", "") + "/" + filename;
			
			System.out.println("Folder: " + folder);
			System.out.println("File:   " + filename);
			System.out.println("Dest:   " + destination);
			
			// Tries to Download the File
			this.getCloudStorageToolkit().downloadFile(folder + filename, destination);
			
			File file = new File(destination);
			
			if (file.exists())
			{
				PRESENTATION_FILE     = filename;
				PRESENTATION_LOCATION = destination;
				
				// Uploads the Presentation to Dropbox
				this.getCloudStorageToolkit().uploadFile(UPLOAD_FOLDER, new File(PRESENTATION_LOCATION));
								
				this.sendMostRecentReading();
				
				runPresentation(false);
			}
		}
		else if (instruction.getCommand().equals("QUIT"))
		{
			PRESENTATION_FILE     = "";
			PRESENTATION_LOCATION = "";
					
			robot.keyPress(KeyEvent.VK_ESCAPE);
			robot.delay(40);
			robot.keyRelease(KeyEvent.VK_ESCAPE);
			robot.delay(500);
			
			robot.keyPress(KeyEvent.VK_META);
			robot.delay(40);
			robot.keyPress(KeyEvent.VK_Q);
			robot.delay(40);
			robot.keyRelease(KeyEvent.VK_META);
			robot.delay(40);
			robot.keyRelease(KeyEvent.VK_Q);
			robot.delay(500);
			
			this.sendMostRecentReading();
		}
		else if (instruction.getCommand().equals("ACC_TOGGLE"))
		{
			// Flips the Switch
			accMode = !accMode;
			
			if (accMode && PRIMARY_DEVICE != null && PRIMARY_DEVICE.length() > 0)
			{
				// Subscribes to their light provider
				this.getGroupContextManager().sendRequest("ACC", new String[] { PRIMARY_DEVICE }, 250, new String[0]);
			}
			else
			{
				this.getGroupContextManager().cancelRequest("ACC");
			}
			
			this.sendMostRecentReading();
		}
	}
	
	private void pressKey(String key)
	{
		int keycode = -1;
		
		if (key.equals("left"))
		{
			keycode = KeyEvent.VK_LEFT;
		}
		else if (key.equals("right"))
		{
			keycode = KeyEvent.VK_RIGHT;
		}
		
		System.out.println("Pressing " + keycode);
					
		Robot robot = this.getRobot();
	    robot.delay(20);
	    robot.keyPress(keycode);
	    robot.delay(20);
	    robot.keyRelease(keycode);
	
	    // TODO:  Experimental . . .
	    File screenshot = ScreenshotToolkit.takeScreenshot(640, 480, APP_DATA_FOLDER + "screen");
	    this.getCloudStorageToolkit().uploadFile(UPLOAD_FOLDER, screenshot);
	    
	    System.out.println("Uploaded New Screenshot");
	    this.sendMostRecentReading();
	}
	
	@Override
	public void onMessage(CommMessage message)
	{
		if (message instanceof ContextData)
		{
			ContextData data = (ContextData)message;
			Double[] values = data.getValuesAsDoubles();
			
			double magnitude = Math.sqrt(Math.pow(values[3], 2.0) + Math.pow(values[4], 2.0) + Math.pow(values[5], 2.0));
			
			if (magnitude > 25.0)
			{
				if (values[5] <= 0)
				{
					pressKey("right");	
				}
				else
				{
					pressKey("right");
				}				
			}
			
			System.out.println("Magnitude: " + magnitude + "   " + data);
		}
	}
}