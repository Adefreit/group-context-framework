package com.adefreitas.groupcontextframework;
/**
 * This Data Structure Tracks Context Subscriptions to this Device
 */
import java.util.Date;

public class ContextSubscriptionInfo 
{
	private String   deviceID;
	private String   contextType;
	private Date     dateSubscribed;
	private Date     lastContact;
	private int      refreshRate;
	private int      subscriptionTimeout;
	private String[] parameters;
	
	public ContextSubscriptionInfo(String deviceID, String contextType, int refreshRate, int subscriptionTimeout, String[] parameters)
	{
		this.deviceID 			 = deviceID;
		this.contextType 		 = contextType;
		this.dateSubscribed 	 = new Date();
		this.lastContact 		 = dateSubscribed;
		this.refreshRate 		 = refreshRate;
		this.subscriptionTimeout = subscriptionTimeout;
		this.parameters 		 = parameters;
	}
	
	public String getDeviceID()
	{
		return deviceID;
	}
	
	public String getContextType()
	{
		return contextType;
	}
		
	public Date getDateSubscribed()
	{
		return dateSubscribed;
	}
	
	public int getRefreshRate()
	{
		return refreshRate;
	}
	
	public boolean isTimeout()
	{
		Date currentDate = new Date();
		Date timeoutDate = new Date(lastContact.getTime() + subscriptionTimeout);
		
		return (timeoutDate.getTime() < currentDate.getTime());
	}
	
	public Date getLastContact()
	{
		return lastContact;
	}
	
	public void setLastContact(Date newDate)
	{
		lastContact = newDate;
	}
	
	public String[] getParameters()
	{
		return parameters;
	}
	
	public void setParameters(String[] parameters)
	{
		this.parameters = parameters;
	}
}
