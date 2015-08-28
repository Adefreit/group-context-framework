package com.adefreit.openimajtoolkit;

public class OpenImajTest 
{
	public static void main(String[] args)
	{
		System.out.println("OpenImajToolkit");
		
		OpenImajToolkit toolkit = new OpenImajToolkit();
		
		if (args.length == 2)
		{
			String imageFilename = args[0];
			String dataFilename  = args[1];
			
			System.out.print("Serializing features . . . ");
			toolkit.serializeFeatures(imageFilename, dataFilename);
			System.out.println("DONE!");
		}
		
//		System.out.println("Comparison Results: " + toolkit.compareImages("APP_STI_PROJECTS_0.jpeg", "Nexus5.jpeg"));

//		toolkit.showResults("APP_STI_PROJECTS_1.jpeg", "Device1.jpeg");
		
//		System.out.print("Serializing features . . . ");
//		String json = toolkit.serializeFeatures("Nexus5.jpeg");
//		System.out.println("DONE!");
//		
//		System.out.print("Deserializing features . . .");
//		toolkit.deserializeFeatures("blah");
//		System.out.println("DONE");
	}
}
