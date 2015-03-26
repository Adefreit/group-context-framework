package com.adefreitas.gcfmagicapp;

import java.util.Date;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adefreitas.androidbluewave.JSONContextParser;
import com.adefreitas.androidframework.AndroidCommManager;
import com.adefreitas.androidframework.ContextReceiver;
import com.adefreitas.gcfmagicapp.lists.AppInfo;
import com.adefreitas.gcfmagicapp.lists.AppInfoListAdapter;
import com.adefreitas.gcfmagicapp.lists.BitmapManager;
import com.adefreitas.messages.ContextData;
import com.adefreitas.messages.ContextRequest;

public class MainActivity extends ActionBarActivity implements ContextReceiver
{	
	// Constants
	private static final String TITLEBAR_CONTENTS = "TITLE";
	private static final String APP_ID			  = "APP_ID";
	private static final double CONFIDENT_MATCH   = 40.0;
	
	// Link to the Application
	private GCFApplication application;
	
	// Android Controls
	private Toolbar	     toolbar;
	private ListView     lstApps;
	private LinearLayout layoutInstructions;
	private ImageView    imgCameraSmall;
	private ImageView    imgCameraBig;
	private TextView	 txtInstructionTitle;
	private TextView	 txtInstructionDescription;
	
	// List Adapters
	private AppInfoListAdapter adapter; 
	
	// Intent Filters
	private ContextReceiver contextReceiver;
	private IntentFilter    filter;
	private IntentReceiver  intentReceiver;
	
	// Temporary
	private AppInfo selectedApp;
	
	/**
	 * Android Method:  Used when the Activity is Created
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Creates a Link to the Application
		this.application = (GCFApplication)this.getApplication();
	
		// Saves Controls
		this.toolbar   	   			   = (Toolbar)this.findViewById(R.id.toolbar);
		this.lstApps  	    		   = (ListView)this.findViewById(R.id.lstApps);
		this.layoutInstructions 	   = (LinearLayout)this.findViewById(R.id.layoutInstructions);
		this.imgCameraSmall 		   = (ImageView)this.findViewById(R.id.imgCameraSmall);
		this.imgCameraBig   		   = (ImageView)this.findViewById(R.id.imgCameraBig);
		this.txtInstructionTitle 	   = (TextView)this.findViewById(R.id.txtInstructionTitle);
		this.txtInstructionDescription = (TextView)this.findViewById(R.id.txtInstructionDescription);
				
		// Sets Up Adapter
		adapter = new AppInfoListAdapter(this, R.layout.app_info_single, application.getApplicationCatalog(), BitmapManager.INSTANCE);
		this.lstApps.setOnItemClickListener(onItemClickListener);
		this.lstApps.setAdapter(adapter);
		
		// Create Intent Filter and Receiver
		this.intentReceiver = new IntentReceiver();
		this.filter = new IntentFilter();
		this.filter.addAction(GCFApplication.ACTION_APP_UPDATE);
		this.filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.filter.addAction(AndroidCommManager.ACTION_COMMTHREAD_CONNECTED);
		this.filter.addAction(AndroidCommManager.ACTION_CHANNEL_SUBSCRIBED);
		
		// Creates Event Handlers
		this.imgCameraSmall.setOnClickListener(onCameraClickListener);
		this.imgCameraBig.setOnClickListener(onCameraClickListener);
		
		// Generates the Execution Alert for an App
		if (savedInstanceState != null && savedInstanceState.containsKey(APP_ID))
		{
			selectedApp = this.application.getApplicationFromCatalog(savedInstanceState.getString(APP_ID));
			this.connectToApp(selectedApp, false);
		}
		
		// Sets Up Toolbar
		this.setSupportActionBar(toolbar);
		
		// Sets the Name of the Toolbar
		if (savedInstanceState != null && savedInstanceState.containsKey(TITLEBAR_CONTENTS))
		{
			toolbar.setTitle(savedInstanceState.getString(TITLEBAR_CONTENTS));
		}
		
		// Updates the Application List
		updateViewContents();
	}

	/**
	 * Android Method:  Used when the Menu is Created
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Android Method:  Used when a Menu Item is Selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		if (item.toString().equalsIgnoreCase(this.getString(R.string.title_activity_settings)))
	    {
	    	Intent intent = new Intent(this, SettingsActivity.class);
	    	this.startActivity(intent);
	    }
		else if (item.toString().equalsIgnoreCase(this.getString(R.string.title_activity_quit)))
		{
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		
		return true;
	}
	
	/**
	 * Android Method:  Used when an Activity is Resumed
	 */
	protected void onResume()
	{
		super.onResume();
			
		// Sets Up the Intent Listener
		this.registerReceiver(intentReceiver, filter);
		
		// Notifies the Application to Forward GCF Messages to this View
		this.application.setContextReceiver(this);
				
		// Clears All Existing UIs
		for (AppInfo app : application.getApplicationCatalog())
		{
			app.setUI(null);
		}
		
		// Cleans Up Running Applications
		if (application.getActiveApplications().size() > 0)
		{			
			// Terminates Active Subscriptions
			for (ContextRequest request : application.getGroupContextManager().getRequests())
			{
				application.getGroupContextManager().cancelRequest(request.getContextType());
			}
			
			
			for (AppInfo app : application.getActiveApplications())
			{
				if (application.getGroupContextManager().isConnected(app.getCommMode(), app.getIPAddress(), app.getPort()))
				{
					String connectionKey = application.getGroupContextManager().getConnectionKey(app.getCommMode(), app.getIPAddress(), app.getPort());
					
					if (app.getCommMode() == GCFApplication.COMM_MODE && app.getIPAddress().equals(GCFApplication.IP_ADDRESS) && app.getPort() == GCFApplication.PORT)
					{
						application.getGroupContextManager().unsubscribe(connectionKey, app.getChannel());
					}
					else
					{
						application.getGroupContextManager().disconnect(connectionKey);
					}
				}
			}
			
			// Removes all Active Applications
			application.getActiveApplications().clear();
		}
		
		// Updates the Application List
		updateViewContents();
	}
	
	/**
	 * Android Method:  Used when an Activity is Paused
	 */
	@Override
	protected void onPause() 
	{
	    super.onPause();
	    this.unregisterReceiver(intentReceiver);
	    this.application.setContextReceiver(null);
	}

	/**
	 * Android Method:  Used when an Activity is About to Close to Save Important Information
	 */
	@Override
	protected void onSaveInstanceState(Bundle bundle)
	{
		if (selectedApp != null)
		{
			bundle.putString(APP_ID, selectedApp.getAppID());
			bundle.putString(TITLEBAR_CONTENTS, toolbar.getTitle().toString());
		}
	}
	
	// GCF Logic Goes Here ----------------------------------------------------------------------
	public void onContextData(ContextData data)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onGCFOutput(String output)
	{
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void onBluewaveContext(JSONContextParser parser)
	{
		// Do Something?
	}

	// Custom Application Logic Goes Here -------------------------------------------------------
	/**
	 * Creates an Alert to Run an Application
	 * @param app
	 */
	public void connectToApp(final AppInfo app, final boolean forceConfirmation)
	{	
		if (app.getPreferences().size() == 0 && app.getContextsRequired().size() == 0 && !forceConfirmation)
		{
			selectedApp = null;
			
		  	// Immediately Connects if No Context/Preferences are Needed
			Intent i = new Intent(application, AppEngine.class);
			i.putExtra("APP_ID", app.getAppID());
    		startActivity(i); 
		}
		else
		{
			// Creates a Confirmation Dialog if there Are Contexts
			AlertDialog.Builder alert = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
			
			final FrameLayout frameView = new FrameLayout(this);
			alert.setView(frameView);
			
			final AlertDialog alertDialog = alert.create();
			LayoutInflater inflater = alertDialog.getLayoutInflater();
			View dialoglayout = inflater.inflate(R.layout.dialog_app_alert, frameView);

			// Grabs Controls
			TextView 	 txtAppName 	   = (TextView)dialoglayout.findViewById(R.id.txtAppName);
			TextView 	 txtAppDescription = (TextView)dialoglayout.findViewById(R.id.txtAppDescription);
			TextView 	 txtAppContexts    = (TextView)dialoglayout.findViewById(R.id.txtAppContexts);
			TextView 	 txtAppPreferences = (TextView)dialoglayout.findViewById(R.id.txtAppPreferences);
			LinearLayout btnRun			   = (LinearLayout)dialoglayout.findViewById(R.id.btnRun);
			
			txtAppName.setText(app.getAppName());
			txtAppDescription.setText("ID: " + app.getAppID() + "\n" + app.getDescription());
			
			// Shows Contexts Required
			if (app.getContextsRequired().size() > 0)
			{
				LinearLayout layoutContexts = (LinearLayout)dialoglayout.findViewById(R.id.layoutContexts);
				layoutContexts.setVisibility(View.VISIBLE);
				txtAppContexts.setText(app.getContextsRequired().toString());
			}
			
			// Shows Preferences
			if (app.getPreferences().size() > 0)
			{
				LinearLayout layoutPreferences = (LinearLayout)dialoglayout.findViewById(R.id.layoutPreferences);
				layoutPreferences.setVisibility(View.VISIBLE);
				txtAppPreferences.setText(app.getPreferences().toString());
			}
			
			final OnClickListener onButtonRunPressed = new OnClickListener() 
			{
				@Override
		        public void onClick(final View v)
		        {
		    		alertDialog.dismiss();
		    		
		    		selectedApp = null;
		    		
				  	// Opens Up the App Engine
					Intent i = new Intent(application, AppEngine.class);
					i.putExtra("APP_ID", app.getAppID());
		    		startActivity(i); 
		        }
		    };
		    
		    final OnDismissListener onDialogDismissed = new OnDismissListener()
		    {
				@Override
				public void onDismiss(DialogInterface dialog)
				{
					selectedApp = null;
				}
		    };
		    
		    // Sets Events
		    btnRun.setOnClickListener(onButtonRunPressed);
		    alertDialog.setOnDismissListener(onDialogDismissed);
			
		    // Shows the Finished Dialog
			alertDialog.show();	
		}
	}
	
	/**
	 * Updates the Visual Look of the Entire Activity
	 */
	public void updateViewContents()
	{						
		// Determines What Icon to Display
		if (application.getApplicationCatalog().size() == 0)
		{
			imgCameraSmall.setVisibility(View.GONE);
			lstApps.setVisibility(View.GONE);
			layoutInstructions.setVisibility(View.VISIBLE);
		}
		else
		{
			adapter.notifyDataSetChanged();
			
			// Runs a Process to Auto Launch the Best App
			runAutoLaunch();
			
			imgCameraSmall.setVisibility(View.VISIBLE);
			lstApps.setVisibility(View.VISIBLE);
			layoutInstructions.setVisibility(View.GONE);
		}
		
		// Determines What Message to Display
		if (application.getBluewaveManager().getPersonalContextProvider().getContext() != null && 
				!application.getBluewaveManager().getPersonalContextProvider().getContext().getJSONObject(GCFApplication.SNAP_TO_IT_TAG).isNull("PHOTO"))
		{
			txtInstructionTitle.setText("Searching For Your Device");
			txtInstructionDescription.setText("Snap-To-It is looking for your device.  This may take a few seconds.");
			imgCameraBig.setBackground(this.getResources().getDrawable(R.drawable.camera_searching));
		}
		else
		{
			txtInstructionTitle.setText("All Is Well");
			txtInstructionDescription.setText("Impromptu is waiting for apps.  Check to make sure your network connections are good.  If they are, then sit back and wait!");
			//txtInstructionDescription.setText("Welcome to Snap-To-It!  To get started, press the above button and take a photograph of the appliance you want to control!");
			imgCameraBig.setBackground(this.getResources().getDrawable(R.drawable.camera_focused));
		}
		
		// Determines if the Snap To It Button is Visible
		if (new Date().getTime() - application.getLastSnapToItDeviceContact().getTime() > 120000)
		{
			imgCameraSmall.setBackground(this.getResources().getDrawable(R.drawable.camera_unfocused));
		}
		else
		{
			imgCameraSmall.setBackground(this.getResources().getDrawable(R.drawable.camera_focused));
		}
	}
	
	public void runAutoLaunch()
	{
		// Determines if AutoLaunch is Enabled
		final boolean autoMode = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("sti_auto", true);
		
		if (autoMode)
		{
			Thread t = new Thread()
			{
				public void run()
				{
					try
					{
						sleep(1000);
						
						// Runs the First App with Over X Matches
						int     count    = 0;
						AppInfo appToRun = null;

						// Looks for App
						for (AppInfo app : application.getApplicationCatalog())
						{
							if (app.getPhotoMatches() >= CONFIDENT_MATCH)
							{
								count++;
								appToRun = app;
							}
						}
						
						if (appToRun != null && count == 1 && application.shouldAutoLaunch())
						{
							application.setAutoLaunch();
							connectToApp(appToRun, false);
						}
					}
					catch (Exception ex)
					{
						Toast.makeText(application, "Auto Launch Failure: " + ex.getMessage(), Toast.LENGTH_LONG).show();
						ex.printStackTrace();
					}
				}
			};
			
			t.start();
		}

//		// Runs the First App with Over X Matches
//		int     count    = 0;
//		AppInfo appToRun = null;
//		
//		if (autoMode)
//		{
//			// Looks for App
//			for (AppInfo app : application.getApplicationCatalog())
//			{
//				if (app.getPhotoMatches() >= CONFIDENT_MATCH && application.shouldAutoLaunch())
//				{
//					count++;
//					appToRun = app;
//				}
//			}
//			
//			if (appToRun != null && count == 1)
//			{
//				application.setAutoLaunch();
//				connectToApp(appToRun, false);
//			}
//		}
	}
	
	// Event Handlers Go Here -------------------------------------------------------------------
	final OnItemClickListener onItemClickListener = new OnItemClickListener() 
    {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			try
			{
				selectedApp = application.getApplicationCatalog().get((int)id);
				
				if (selectedApp != null)
				{
					connectToApp(selectedApp, false);	
				}
			}
			catch (Exception ex)
			{
				Toast.makeText(application, "Problem Occurred While Selecting App: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
				ex.printStackTrace();
			}
		}
    };

    final OnClickListener onCameraClickListener = new OnClickListener() 
    {
		@Override
		public void onClick(View v) 
		{
		  	// Opens Up the Camera Application
			Intent i = new Intent(application, CameraActivity.class);
    		startActivity(i); 
		}
    };
	
	// Intent Receiver --------------------------------------------------------------------------
    private class IntentReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{	
			if (intent.getAction().equals(GCFApplication.ACTION_APP_UPDATE))
			{
				updateViewContents();
			}
			else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
			{
				updateViewContents();
			}
			else if (intent.getAction().equals(AndroidCommManager.ACTION_COMMTHREAD_CONNECTED))
			{
				toolbar.setTitle(getString(R.string.app_name) + " (Subscribing . . .)");
			}
			else if (intent.getAction().equals(AndroidCommManager.ACTION_CHANNEL_SUBSCRIBED))
			{
				toolbar.setTitle(getString(R.string.app_name));
			}
			else
			{
				Log.e("", "Unknown Action: " + intent.getAction());
			}
		}
	}
}
