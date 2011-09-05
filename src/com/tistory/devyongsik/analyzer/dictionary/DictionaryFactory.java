package com.tistory.devyongsik.analyzer.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tistory.devyongsik.analyzer.DictionaryProperties;

public class DictionaryFactory {
	private Log logger = LogFactory.getLog(DictionaryFactory.class);

	private static DictionaryFactory factory = new DictionaryFactory();

	//TODO 사전 중복으로 읽지 않도록..
	//TODO 사전을 Map으로.. 초성 분리..맵 고민
	
	public static DictionaryFactory getFactory() {
		return factory;
	}

	public List<String> create(DictionaryType name) {
		List<String> dic = loadDictionary(name);
		return dic;
	}

	private List<String> loadDictionary(DictionaryType name) {
		if(logger.isInfoEnabled()) {
			logger.info("["+name.getDescription()+"] "+"create wordset from file");
		}

		BufferedReader in = null;
		String dictionaryFile = DictionaryProperties.getInstance().getProperty(name.getPropertiesKey());
		InputStream inputStream = DictionaryFactory.class.getClassLoader().getResourceAsStream(dictionaryFile);

		if(inputStream == null) {
			logger.error("couldn't find dictionary : " + dictionaryFile);
		}

		List<String> words = new ArrayList<String>();

		try {
			String readWord = "";
			in = new BufferedReader( new InputStreamReader(inputStream ,"utf-8"));
			
			
			while( (readWord = in.readLine()) != null ) {
				words.add(readWord);
			}

			if(logger.isInfoEnabled()) {
				logger.info(name.getDescription() + " : " + words.size());
			}

			if(logger.isInfoEnabled()) {
				logger.info("create wordset from file complete");
			}

		}catch(IOException e){
			logger.error(e.toString());
		}finally{
			try {
				in.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
		
		return words;
	}
}
