package com.tistory.devyongsik.analyzer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.AttributeSource.State;

import com.tistory.devyongsik.analyzer.dictionary.DictionaryFactory;
import com.tistory.devyongsik.analyzer.dictionary.DictionaryType;

public class KoreanCompoundNounEngine implements Engine {

	private Log logger = LogFactory.getLog(KoreanCompoundNounEngine.class);
	
	private Map<String,List<String>> compoundNouns = new HashMap<String,List<String>>();
	
	private static KoreanCompoundNounEngine koreanCompoundNounEngineInstance = new KoreanCompoundNounEngine();
	
	public static KoreanCompoundNounEngine getInstance() {
		return koreanCompoundNounEngineInstance;
	}
	
	private KoreanCompoundNounEngine() {
		if(logger.isInfoEnabled()) {
			logger.info("사전을 읽습니다.");
		}
		
		loadDictionary();
	}
	
	private void loadDictionary() {
		if(logger.isInfoEnabled()) {
			logger.info("어미-조사 사전을 로드합니다.");
		}
		
		DictionaryFactory dictionaryFactory = DictionaryFactory.getFactory();	
		List<String> dictionaryData = dictionaryFactory.create(DictionaryType.COMPOUND);
		
		if(logger.isInfoEnabled()) {
			logger.info("복합명사 사전 : [" + dictionaryData.size() + "]");
		}
		
		//Map형태로 변환
		String[] extractKey = null;
		String key = null;
		String[] nouns = null;
		
		for(String data : dictionaryData) {
			extractKey = data.split(":");
			key = extractKey[0];
			nouns = extractKey[1].split(",");
			
			compoundNouns.put(key, Arrays.asList(nouns));
		}
		
		if(logger.isInfoEnabled()) {
			logger.info("사전 생성 완료");
		}
	}
	
	@Override
	public Stack<State> getAttributeSources(AttributeSource attributeSource) throws Exception {
		CharTermAttribute termAttr = attributeSource.getAttribute(CharTermAttribute.class);
		TypeAttribute typeAttr = attributeSource.getAttribute(TypeAttribute.class);
		OffsetAttribute offSetAttr = attributeSource.getAttribute(OffsetAttribute.class);
		PositionIncrementAttribute positionAttr = attributeSource.getAttribute(PositionIncrementAttribute.class);

		Stack<State> nounsStack = new Stack<State>();
		
		String termString = termAttr.toString();

		//복합명사 사전에 있는 단어면
		List<String> matchedData = compoundNouns.get(termString);
		if(matchedData != null) {
			typeAttr.setType("compounds");

			for(String noun : matchedData) {
				
				if(logger.isDebugEnabled()) {
					logger.debug("복합명사추출 : " + noun);
				}
				
				termAttr.setEmpty();
				termAttr.append(noun);
			    
				positionAttr.setPositionIncrement(1);
			    
			    int startOffSet = termString.indexOf(noun);
			    int endOffSet = startOffSet + noun.length();
			    offSetAttr.setOffset(startOffSet , endOffSet);
			    
			    typeAttr.setType("compound");
			    nounsStack.add(attributeSource.captureState());
			}
		}
		
		return nounsStack;
	}
}
