package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class KoreanStemFilter extends TokenFilter {
	private Log logger = LogFactory.getLog(KoreanStemFilter.class);
	
	private Stack<State> stemmedWords = new Stack<State>();
	private Engine engine;
	
	protected KoreanStemFilter(TokenStream input) {
		super(input);
		this.engine = KoreanStemmingEngine.getInstance();
	}

	@Override
	public boolean incrementToken() throws IOException {
		if(logger.isDebugEnabled())
			logger.debug("incrementToken KoreanStemFilter");


		if (stemmedWords.size() > 0) {
			if(logger.isDebugEnabled())
				logger.debug("스탬 Stack에서 토큰 리턴함");

			State synState = stemmedWords.pop();
			restoreState(synState); //#3. 현재의 stream 즉 AttributeSource를 저장해놨던 놈으로 바꿔치기한다.

			return true;
		}

		if (!input.incrementToken())
			return false;
		
		try {
			
			stemmedWords = engine.getAttributeSources(input.cloneAttributes());
			
		} catch (Exception e) {
			logger.error("스템필터에서 목록 조회 오류");
			e.printStackTrace();
		}
		
		return true;
	}

}
