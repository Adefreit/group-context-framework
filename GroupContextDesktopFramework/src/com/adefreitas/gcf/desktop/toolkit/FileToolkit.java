package com.adefreitas.gcf.desktop.toolkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;

public class FileToolkit 
{
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}

	public static void writeToFile(String filename, String text)
	{
		try
		{
			PrintWriter writer = new PrintWriter(filename, "UTF-8");
			writer.println(text);
			writer.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static void writeToZippedFile(String filename, String text)
	{
		
	}
}
