package com.adefreitas.desktoptoolkits;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import com.adefreitas.groupcontextframework.Settings;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.DbxWriteMode;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;

public class SftpToolkit extends CloudStorageToolkit
{		
	// User Credentials
	private static final String USERNAME = "gcfuser";
	private static final String PASSWORD = "1qaz2wsx!QAZ@WSX";
	private static final String SERVER   = Settings.DEV_SFTP_IP;
	private static final int    PORT     = Settings.DEV_SFTP_PORT;
	
	public SftpToolkit()
	{	
		
	}
	
	public void uploadFile(String path, File file)
	{
		Session session = null;
	    Channel channel = null;
		
		try
    	{
	        JSch ssh = new JSch();
        
	        session = ssh.getSession(USERNAME, SERVER, PORT);
	        session.setConfig("StrictHostKeyChecking", "no");
	        session.setPassword(PASSWORD);
	        session.connect();
	        
	        channel = session.openChannel("sftp");
	        channel.connect();
	        
	        ChannelSftp sftp = (ChannelSftp)channel;
	        sftp.put(file.getAbsolutePath(), path + "/" + file.getName());
    	}
    	catch (Exception ex)
    	{
    		System.out.println("Problem occurred while uploading " + file.getAbsolutePath() + " to " + path + "/" + file.getName());
    		ex.printStackTrace();
    	}
    	finally
    	{
    		if (channel != null) 
	        {
	            channel.disconnect();
	        }
	        if (session != null) 
	        {
	            session.disconnect();
	        }
    	}
	}
	
	public void downloadFile(String sourcePath, String destinationPath)
	{
		Session session = null;
	    Channel channel = null;
    	
    	try
    	{
	        JSch ssh = new JSch();
	        
	        session = ssh.getSession(USERNAME, SERVER, PORT);
	        session.setConfig("StrictHostKeyChecking", "no");
	        session.setPassword(PASSWORD);
	        session.connect();
	        
	        channel = session.openChannel("sftp");
	        channel.connect();
	        
	        File destination = new File(destinationPath.substring(0, destinationPath.lastIndexOf("/") + 1));
	        
	        if (!destination.exists())
	        {
	        	System.out.println("Creating Directories: " + destination.getAbsolutePath());
	        	destination.mkdirs();
	        }
	        
	        ChannelSftp sftp = (ChannelSftp)channel;
	        sftp.get(sourcePath, destinationPath);
    	}
    	catch (Exception ex)
    	{
    		System.out.println("Problem occurred while downloading " + sourcePath + " to " + destinationPath);
    		ex.printStackTrace();
    	}
    	finally
    	{
    		if (channel != null) 
	        {
	            channel.disconnect();
	        }
	        if (session != null) 
	        {
	            session.disconnect();
	        }
    	}
	}

	public String[] getFileContents(String path)
	{
		ArrayList<String> result = new ArrayList<String>();
		
		Session session = null;
	    Channel channel = null;
    	
    	try
    	{
	        JSch ssh = new JSch();
	        //ssh.setKnownHosts("/path/of/known_hosts/file");
	        
	        session = ssh.getSession(USERNAME, SERVER, PORT);
	        session.setConfig("StrictHostKeyChecking", "no");
	        session.setPassword(PASSWORD);
	        session.connect();
	        
	        channel = session.openChannel("sftp");
	        channel.connect();
	        
	        ChannelSftp sftp = (ChannelSftp)channel;
	        Vector fileList = sftp.ls(path);
	        
	        for (int i=0; i<fileList.size(); i++)
	        {
	        	result.add(fileList.get(i).toString());
	        }
    	}
    	catch (Exception ex)
    	{
    		System.out.println("Problem occurred while examining contents of " + path);
    		ex.printStackTrace();
    	}
    	finally
    	{
    		if (channel != null) 
	        {
	            channel.disconnect();
	        }
	        if (session != null) 
	        {
	            session.disconnect();
	        }
    	}
		
		return result.toArray(new String[0]);
	}
	
	public Date getLastModified(String path)
	{
		Session 	session = null;
	    Channel 	channel = null;
	    ChannelSftp sftp    = null;
    	try
    	{
	        JSch ssh = new JSch();
	        //ssh.setKnownHosts("/path/of/known_hosts/file");
	        
	        session = ssh.getSession(USERNAME, SERVER, PORT);
	        session.setConfig("StrictHostKeyChecking", "no");
	        session.setPassword(PASSWORD);
	        session.connect();
	        
	        channel = session.openChannel("sftp");
	        channel.connect();
	        
	        sftp  		    = (ChannelSftp)channel;
	        SftpATTRS stats = sftp.lstat(path);
	        
	        // According to the documentation, getMTime returns time in seconds
	        // However, date requires that time be measured in milliseconds
	        return new Date((long)stats.getMTime() * 1000L);	        
    	}
    	catch (Exception ex)
    	{
    		System.out.println("Problem occurred while examining contents of " + path);
    		ex.printStackTrace();
    	}
    	finally
    	{
    		if (channel != null) 
	        {
	            channel.disconnect();
	        }
	        if (session != null) 
	        {
	            session.disconnect();
	        }
	        if (sftp != null)
	        {
	        	sftp.disconnect();
	        }
    	}
    	
    	return null;
	}
}
