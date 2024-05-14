package com.synapticloop.crosswordr.util;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import java.io.File;

public class Constants {
	public static String APP_DATA_DIRECTORY = AppDirsFactory.getInstance().getUserDataDir("Crosswordr", null, "Synapticloop");
	public static String APP_PROPERTIES_LOCATION = APP_DATA_DIRECTORY + File.separator + "crossword.properties";
	public static String APP_CACHE_DIRECTORY = APP_DATA_DIRECTORY + File.separator + "cached";
	public static File cacheDirectory;

	static {
		AppDirs appDirs = AppDirsFactory.getInstance();
		cacheDirectory = new File(APP_CACHE_DIRECTORY);
		cacheDirectory.mkdirs();
	}


}
