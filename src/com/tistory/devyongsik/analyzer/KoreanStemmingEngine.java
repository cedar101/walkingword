package com.tistory.devyongsik.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.AttributeSource.State;

import com.tistory.devyongsik.analyzer.dictionary.DictionaryFactory;
import com.tistory.devyongsik.analyzer.dictionary.DictionaryType;

public class KoreanStemmingEngine implements Engine {

	private Log logger = LogFactory.getLog(KoreanStemmingEngine.class);
	
	private List<String> eomisJosaList = new ArrayList<String>();
	private static KoreanStemmingEngine stemmingEngineInstance = new KoreanStemmingEngine();
	
	public static KoreanStemmingEngine getInstance() {
		return stemmingEngineInstance;
	}
	
	private KoreanStemmingEngine() {
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
		eomisJosaList = dictionaryFactory.create(DictionaryType.EOMI);
		
		if(logger.isInfoEnabled()) {
			logger.info("어미-조사 사전 : [" + eomisJosaList.size() + "]");
		}
		
		//가장 긴 순으로 정렬한다.
		Collections.sort(eomisJosaList, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int o1Length = o1.length();
				int o2Length = o2.length();
				
				return o2Length - o1Length;
			}
		});
		
		if(logger.isInfoEnabled()) {
			logger.info("사전 정렬 완료");
		}
	}
	
	@Override
	public Stack<State> getAttributeSources(AttributeSource attributeSource) throws Exception {
		CharTermAttribute characterAttr = attributeSource.getAttribute(CharTermAttribute.class);
		String tokenTerm = characterAttr.toString();
		
		int removedWordLength = 0;
		
		Stack<State> wordsStack = new Stack<State>();
		
		for(String eomiJosa : eomisJosaList) {
			
			if(tokenTerm.endsWith(eomiJosa)) {
				//뒤에서부터 매칭되는 가장 긴 어미를 찾아 substring함.
				String stemmedWord = tokenTerm.substring(0, tokenTerm.length() - eomiJosa.length());
				removedWordLength = eomiJosa.length();
				
				if("".equals(stemmedWord)) {
					if(logger.isDebugEnabled())
						logger.debug("..in Stem : " + tokenTerm);
					
					return wordsStack; //스테밍 할 것이 없음 Token=어미/조사인 경우임
				}
				
				CharTermAttribute termAttrResult = attributeSource.getAttribute(CharTermAttribute.class);
				termAttrResult.setEmpty();
				termAttrResult.append(stemmedWord);
			    
				//offset 재조정
				OffsetAttribute offSetAttrResult = attributeSource.getAttribute(OffsetAttribute.class);
			    offSetAttrResult.setOffset(offSetAttrResult.startOffset(), offSetAttrResult.endOffset() - removedWordLength);
			    
			    TypeAttribute typeAttrResult = attributeSource.getAttribute(TypeAttribute.class);
			    typeAttrResult.setType("word");

			    wordsStack.push(attributeSource.captureState());
			    
				if(logger.isDebugEnabled()) {
					logger.debug("tokenTerm : " + tokenTerm);
					logger.debug("stemming된 term : " + tokenTerm.substring(0, tokenTerm.length() - eomiJosa.length()));
				}
				
				return wordsStack;
			}
		}

		return wordsStack;
	}
}
