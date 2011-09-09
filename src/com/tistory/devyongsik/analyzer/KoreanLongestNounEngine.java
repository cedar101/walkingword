package com.tistory.devyongsik.analyzer;

import java.util.ArrayList;
import java.util.List;
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

public class KoreanLongestNounEngine implements Engine {

	private Log logger = LogFactory.getLog(KoreanLongestNounEngine.class);

	private List<String> nounsDic = new ArrayList<String>();
	private List<String> customNounsDic = new ArrayList<String>();
	
	private static KoreanLongestNounEngine koreanLongestNounEngine = new KoreanLongestNounEngine();

	public static KoreanLongestNounEngine getInstance() {
		return koreanLongestNounEngine;
	}
	
	private KoreanLongestNounEngine() {
		if(logger.isInfoEnabled()) {
			logger.info("사전을 읽습니다.");
		}

		loadDictionary();
	}

	private void loadDictionary() {
		if(logger.isInfoEnabled()) {
			logger.info("명사사전을 로드합니다.");
		}

		DictionaryFactory dictionaryFactory = DictionaryFactory.getFactory();	
		nounsDic = dictionaryFactory.create(DictionaryType.NOUN);
		customNounsDic = dictionaryFactory.create(DictionaryType.CUSTOM);

		if(logger.isInfoEnabled()) {
			logger.info("명사 사전 : [" + nounsDic.size() + "]");
			logger.info("사용자 명사 사전 : [" + customNounsDic.size() + "]");
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

		if(!typeAttr.type().equals("word")) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("명사 분석 대상이 아닙니다.");
			}
			
			return nounsStack;
		}
		
		String term = termAttr.toString();
		String comparedWord = null;
		
		//1. 사전과 매칭되는 가장 긴 단어를 추출한다.
		int startIndex = 0;
		int endIndex = startIndex + 1;
		
		int orgStartOffSet = offSetAttr.startOffset();
		
		int prevMatchedStartIndex = 0;
		int prevMatchedEndIndex = 0;
		
		String matchedTerm = "";
		
		while(true) {
			
			if(endIndex > term.length()) {
				
				if(matchedTerm.length() > 0 && !term.equals(matchedTerm)) { //endIndex가 끝까지 갔고, 매칭된 키워드가 있음
					termAttr.setEmpty();
					termAttr.append(matchedTerm);

					positionAttr.setPositionIncrement(1);  //추출된 명사이기 때문에 위치정보를 1로 셋팅
					//타입을 noun으로 설정한다.
					typeAttr.setType("noun"); 
					
					int startOffSet = orgStartOffSet + prevMatchedStartIndex;
					int endOffSet = orgStartOffSet + prevMatchedEndIndex;
					
					offSetAttr.setOffset(startOffSet , endOffSet);
					
					nounsStack.push(attributeSource.captureState()); //추출된 명사에 대한 AttributeSource를 Stack에 저장
					
					matchedTerm = "";
					
					startIndex = prevMatchedEndIndex;
					endIndex = startIndex + 1;
				} else {
					
					if(startIndex == prevMatchedEndIndex) {
						startIndex++;
						endIndex = startIndex + 1;
					} else {
						startIndex = endIndex;
						endIndex = startIndex + 1;
					}
				}
				
				
			}
			
			if(startIndex >= term.length()) {
				break;
			}
			
			comparedWord = term.substring(startIndex, endIndex);
			
			//매칭될 때 우선 matchedTerm에 저장
			if(nounsDic.contains(comparedWord) || customNounsDic.contains(comparedWord)) {
				matchedTerm = comparedWord;
				prevMatchedStartIndex = startIndex;
				prevMatchedEndIndex = endIndex;
			}
			
			endIndex++;
			
		}//end while

		return nounsStack;
	}

}
