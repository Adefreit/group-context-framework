package com.adefreit.openimajtoolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.*;
import org.openimaj.math.model.fit.RANSAC;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * This is a simple wrapper class that makes using OpenImaj's SIFT Comparison extremely simple
 * @author adefreit
 */
public class OpenImajToolkit 
{	
	// Debug Flag
	private final boolean DEBUG = false;
	
	// Used to Display Results
	private JFrame frame;
	
	// Used to Store Keypoint Values for Previously Calculated Files
	private HashMap<String, LocalFeatureList<Keypoint>> keypointDB;
	
	// SIFT Comparator
	private DoGSIFTEngine engine;
	
	// Matchers
	final HomographyModel 		   	    fittingModel;
	final RANSAC<Point2d, Point2d> 	    ransac;
	final LocalFeatureMatcher<Keypoint> matcher;
		
	/**
	 * Constructor
	 */
	public OpenImajToolkit()
	{
		keypointDB   = new HashMap<String, LocalFeatureList<Keypoint>>();
		engine       = new DoGSIFTEngine();
		fittingModel = new HomographyModel(5);
		ransac		 = new RANSAC<Point2d, Point2d>(fittingModel, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5), true);
		matcher		 = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8), ransac);
	}

	public synchronized void computeFeatures(String path)
	{
		File file 	   = new File(path);
		Date startDate = new Date();
		
		try
		{
			if (!keypointDB.containsKey(path))
			{
				if (path.endsWith("jpeg") || path.endsWith("jpg"))
				{
					keypointDB.put(path, engine.findFeatures(ImageUtilities.readMBF(file).flatten()));
					println("  [Computed Features for " + path + " in " + (new Date().getTime() - startDate.getTime()) + " ms]");
				}
				else if (path.endsWith("sift"))
				{
					keypointDB.put(path, deserializeFeatures(path));
					println("  [Imported Features for " + path + " in " + (new Date().getTime() - startDate.getTime()) + " ms]");
				}
				else
				{
					println("UNKNOWN FILE TYPE: " + path);
				}
			}
			else
			{
				println("  [Features Already Computed for " + path + "]");	
			}
		}
		catch (Exception ex)
		{
			println("Problem occurred while computing features for " + path);
			ex.printStackTrace();
		}
	}
	
	public void serializeFeatures(String imagePath, String filePath)
	{
		Gson   gson     = new Gson();
		File   image    = new File(imagePath);
		
		try
		{
			LocalFeatureList<Keypoint> list = engine.findFeatures(ImageUtilities.readMBF(image).flatten());
			System.out.println("List has: " + list.size() + " elements.");
			System.out.println(list.get(0).ori + ", " + list.get(0).x);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			org.openimaj.io.IOUtils.writeBinary(baos, list);
			byte[] bytes = baos.toByteArray();
			
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(bytes);
			fos.close();
		}
		catch (Exception ex)
		{
			println("Problem occurred while serializing " + imagePath + ": " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	public synchronized LocalFeatureList<Keypoint> deserializeFeatures(String path)
	{
		Type keypointType = new TypeToken<LocalFeatureList<Keypoint>>(){}.getType();
		
		try
		{
			Path p = Paths.get(path);
			byte[] bytes = Files.readAllBytes(p);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			LocalFeatureList<Keypoint> list = MemoryLocalFeatureList.read(bais, Keypoint.class);
			System.out.println("List has: " + list.size() + " elements.");
			
			return list;
		}
		catch (Exception ex)
		{
			println("Problem occurred while deserializing features: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public void forgetFeatures(String path)
	{
		println("Forgetting features for file: " + path);
		keypointDB.remove(path);
	}
	
	public int compareImages(String path1, String path2)
	{
		int numMatches = 0;
		
		try
		{
			Date startDate = new Date();
			
			// Computes Features for File (and stores the results for later use)
			computeFeatures(path1);
			computeFeatures(path2);
			
			// Performs the Comparison
			matcher.setModelFeatures(keypointDB.get(path1));
			matcher.findMatches(keypointDB.get(path2));
			numMatches = matcher.getMatches().size();
			
			// Calculates the Processing Time
			println("Image Comparison [" + path1 + ", " + path2 + "]:  " + numMatches + " matches.  (" + (new Date().getTime() - startDate.getTime()) + " ms)");
		}
		catch (Exception ex)
		{
			println("Error occurred during comparison: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		// Returns the Number of Matches
		return numMatches;
	}
	
	public void showResults(String path1, String path2)
	{
		try
		{
			// Closes the Frame if it Exists
			if (frame != null)
			{
				frame.dispose();
			}
			
			LocalFeatureMatcher<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8), ransac);
			matcher.setModelFeatures(keypointDB.get(path1));
			matcher.findMatches(keypointDB.get(path2));
			
			// Loads the Images
			MBFImage mbf1 = ImageUtilities.readMBF(new File(path1));
			MBFImage mbf2 = ImageUtilities.readMBF(new File(path2));
			final MBFImage consistentMatches = MatchingUtilities.drawMatches(mbf1, mbf2, matcher.getMatches(), RGBColour.GREEN);

			// Closes an Existing Window
			frame = DisplayUtilities.display(consistentMatches);
		}
		catch (Exception ex)
		{
			println("Problem occurred showing results of " + path1 + " and " + path2 + ": " + ex.getMessage());
		}
	}
	
	private void println(String s)
	{
		if (DEBUG)
		{
			System.out.println(s);
		}
	}
}
