package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class KoreanCompoundNounFilter extends TokenFilter {
	private Log logger = LogFactory.getLog(KoreanStemFilter.class);
	
	private Stack<State> compundNounsStack = new Stack<State>();
	private Engine engine;
	
	protected KoreanCompoundNounFilter(TokenStream input) {
		super(input);
		this.engine = KoreanCompoundNounEngine.getInstance();
	}

	@Override
	public boolean incrementToken() throws IOException {
		if(logger.isDebugEnabled())
			logger.debug("incrementToken KoreanCompoundNounFilter");


		if (compundNounsStack.size() > 0) {
			if(logger.isDebugEnabled())
				logger.debug("복합명사 Stack에서 토큰 리턴함");

			State synState = compundNounsStack.pop();
			restoreState(synState);

			return true;
		}

		if (!input.incrementToken())
			return false;
		
		try {
			
			compundNounsStack = engine.getAttributeSources(input.cloneAttributes());
			
		} catch (Exception e) {
			logger.error("복합명사필터에서 목록 조회 오류");
			e.printStackTrace();
		}

		//원본 Token 리턴
		if(logger.isDebugEnabled()) {
			CharTermAttribute charTermAttr = input.getAttribute(CharTermAttribute.class);
			logger.debug("원본 termAttr 리턴 : [" + charTermAttr.toString() + "]");
		}
		
		return true;
	}

}
