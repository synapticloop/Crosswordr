package com.synapticloop.crosswordr.util;

import javafx.scene.control.CheckBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public class Settings {
	private static boolean isInitialised = false;
	private static Properties settings = new Properties();

	public synchronized static void loadSettings() {
		if (isInitialised) {
			return;
		}

		File file = new File(Constants.APP_PROPERTIES_LOCATION);
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			settings.load(fileInputStream);
		} catch (IOException e) {
			// if we cannot find it, then just create a new one
			settings = new Properties();
		}


		try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
			settings.store(fileOutputStream, null);
		} catch (IOException e) {
			// We couldn't store the properties - look like we will have to use the
			// sensible defaults that we set
		}
		isInitialised = true;
	}

	public synchronized static void saveSettings() {
		if (!isInitialised) {
			loadSettings();
		}

		File file = new File(Constants.APP_PROPERTIES_LOCATION);

		try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
			settings.store(fileOutputStream, null);
		} catch (IOException e) {
			// We couldn't store the properties - look like we will have to use the
			// sensible defaults that we set
		}
	}

	public static void setSetting(Object classObject, String key, Object value) {
		if (!isInitialised) {
			loadSettings();
		}

		if (null != value) {
			if (value instanceof CheckBox) {
				settings.setProperty(getKey(classObject, key), ((CheckBox) value).isSelected() ? "true" : "false");
			} else {
				settings.setProperty(getKey(classObject, key), value.toString());
			}
		}
	}

	public static String getStringSetting(Object classObject, String key, String defaultValue) {
		if (!isInitialised) {
			loadSettings();
		}

		String property = settings.getProperty(getKey(classObject, key));
		if (null == property) {
			return (defaultValue);
		} else {
			return (property);
		}
	}

	public static Integer getIntegerSetting(Object classObject, String key, Integer defaultValue) {
		if (!isInitialised) {
			loadSettings();
		}

		String property = settings.getProperty(getKey(classObject, key));
		if (null == property) {
			return (defaultValue);
		} else {
			try {
				return (Integer.parseInt(property));
			} catch (NumberFormatException ex) {
				return (defaultValue);
			}
		}
	}

	public static boolean getBooleanSetting(Object classObject, String key, boolean defaultValue) {
		if (!isInitialised) {
			loadSettings();
		}

		String property = settings.getProperty(getKey(classObject, key));
		if (null == property) {
			return (defaultValue);
		}
		return (property.equalsIgnoreCase("true"));
	}

	public static Double getDoubleSetting(Object classObject, String key, Double defaultValue) {
		if (!isInitialised) {
			loadSettings();
		}

		String property = settings.getProperty(getKey(classObject, key));
		if (null == property) {
			return (defaultValue);
		} else {
			try {
				return (Double.parseDouble(property));
			} catch (NumberFormatException ex) {
				return (defaultValue);
			}
		}
	}

	private static String getKey(Object classObject, String key) {
		return (String.format("%s.%s", classObject.getClass().getCanonicalName(), key));
	}
}
