package org.arrowhead.wp5.core.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FOConfig {
	private static final Logger logger = LoggerFactory.getLogger(FOConfig.class);
	private Properties properties;

	public FOConfig(String key, String def) {
		String filename = System.getProperty(key, def);
		properties = new Properties();
		try {
			properties.load(new FileInputStream(filename));
		} catch (FileNotFoundException e) {
			logger.info("Unable to locate configuration file, using defaults.");
		} catch (IOException e) {
			logger.error("Failed to read property file {}.", filename, e);
		}
	}
	
	public String getString(String key, String def) {
		return properties.getProperty(key, def).trim();
	}

	public int getInt(String key, int def) {
		int val = def;
        try {
        	val = Integer.parseInt(properties.getProperty(key, Integer.toString(def)));
        } catch (NumberFormatException e) {
            logger.warn("Unable to parse Integer value for key: {}", key);
        }
        
		return val;
	}
	
	public boolean getBoolean(String key, String def) {
		return Boolean.parseBoolean(properties.getProperty(key, def));
	}
}
