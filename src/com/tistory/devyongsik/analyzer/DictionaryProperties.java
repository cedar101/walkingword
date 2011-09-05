package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DictionaryProperties {
	private Log logger = LogFactory.getLog(DictionaryProperties.class);
	
	private static DictionaryProperties instance = new DictionaryProperties();

	private Properties prop;
	private String resourceName = "com/tistory/devyongsik/analyzer/dictionary.properties";

	private DictionaryProperties() {
		loadProperties();
	}

	private void loadProperties() {
		if(logger.isDebugEnabled())
			logger.debug("load analyzer properties.....");

		Class<DictionaryProperties> clazz = DictionaryProperties.class;
		InputStream in = clazz.getClassLoader().getResourceAsStream(resourceName);
		
		if(in == null) {
			logger.error(resourceName + " was not found!!! ");
		}

		prop = new Properties();

		try {
			prop.load(in);
			in.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}
	}

	public static DictionaryProperties getInstance() {
		return instance;
	}

	public String getProperty(String key) {
		return prop.getProperty(key).trim();
	}
}
