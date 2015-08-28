package com.adefreitas.gcfimpromptu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adefreitas.gcfmagicapp.R;

@SuppressLint("SetJavaScriptEnabled")
public class PermissionActivity extends ActionBarActivity {

	private static final String URL = "http://gcf.cmu-tbank.com/bluewave/managePermissions.php?deviceID=";
	
	// The application
	private GCFApplication application;
	
	// Controls
	private Toolbar toolbar;
	private WebView webView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permission);
		
		// Saves a Link to the Application
		application = (GCFApplication)this.getApplication();
				
		// Saves Controls
		toolbar = (Toolbar)this.findViewById(R.id.toolbar);
		webView = (WebView)this.findViewById(R.id.webView);
		//webView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.getSettings().setLoadsImagesAutomatically(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setGeolocationDatabasePath(application.getFilesDir().getPath());
		webView.setWebViewClient(new CustomBrowser());	
		
		// Sets Up the Toolbar
		this.setSupportActionBar(toolbar);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setTitle("Permissions");
		
		// Sets Default Webpage
		webView.loadUrl(URL + application.getGroupContextManager().getDeviceID().replace(" ", "%20"));
	}

	/**
	 * Android Method:  Called to Create the Options Menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.permission, menu);
		return true;
	}

	/**
	 * Android Method:  Called when a Menu Item is Selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Android Method:  Used to Save the Activity's State
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		//Toast.makeText(this, "Save Instance State Called", Toast.LENGTH_SHORT).show();
		super.onSaveInstanceState(outState);
		webView.saveState(outState);
	}
	
	/**
	 * Android Method:  Used to Restore the Activity's State
	 */
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		webView.restoreState(savedInstanceState);
		
	}

	// Custom Browser Classes ---------------------------------------------------------------
	private class CustomBrowser extends WebViewClient 
	{
	   @Override
	   public boolean shouldOverrideUrlLoading(WebView view, String url) 
	   {
		  view.loadUrl(url);
	      return true;
	   }
	
	   @Override
       public void onPageFinished(WebView view, String url) 
	   {
           toolbar.setTitle(view.getTitle());
       }
	}
}
