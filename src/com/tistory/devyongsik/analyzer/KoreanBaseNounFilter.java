package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class KoreanBaseNounFilter extends TokenFilter {
	private Log logger = LogFactory.getLog(KoreanBaseNounFilter.class);
	
	private Stack<State> nounsStack = new Stack<State>();
	private Engine engine;
	
	protected KoreanBaseNounFilter(TokenStream input) {
		super(input);
		this.engine = KoreanBaseNounEngine.getInstance();
	}

	@Override
	public boolean incrementToken() throws IOException {
		if(logger.isDebugEnabled())
			logger.debug("incrementToken KoreanBaseNounFilter");
		

		if (nounsStack.size() > 0) {
			if(logger.isDebugEnabled())
				logger.debug("명사 Stack에서 토큰 리턴함");

			State synState = nounsStack.pop();
			restoreState(synState);

			return true;
		}

		if (!input.incrementToken())
			return false;
		
		try {
			
			nounsStack = engine.getAttributeSources(input.cloneAttributes());
			
		} catch (Exception e) {
			logger.error("명사필터에서 목록 조회 오류");
			e.printStackTrace();
		}
		
		return true;
	}

}