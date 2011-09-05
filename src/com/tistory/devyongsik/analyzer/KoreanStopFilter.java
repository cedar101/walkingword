package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import com.tistory.devyongsik.analyzer.dictionary.DictionaryFactory;
import com.tistory.devyongsik.analyzer.dictionary.DictionaryType;

public class KoreanStopFilter extends TokenFilter {

	private Log logger = LogFactory.getLog(KoreanStopFilter.class);
	private boolean enablePositionIncrements = false;

	private CharTermAttribute charTermAtt;
	private PositionIncrementAttribute posIncrAtt;

	private List<String> stopWords = new ArrayList<String>();
	
	protected KoreanStopFilter(TokenStream input) {
		super(input);
		loadDictionary();
		charTermAtt = getAttribute(CharTermAttribute.class);
		posIncrAtt = getAttribute(PositionIncrementAttribute.class);
	}

	private void loadDictionary() {
		if(logger.isInfoEnabled()) {
			logger.info("불용어 사전을 로드합니다.");
		}
		DictionaryFactory dictionaryFactory = DictionaryFactory.getFactory();	
		stopWords = dictionaryFactory.create(DictionaryType.STOP);
		
		if(logger.isInfoEnabled()) {
			logger.info("불용어 사전 : [" + stopWords.size() + "]");
		}
	}
	
	public void setEnablePositionIncrements(boolean enable) {
		this.enablePositionIncrements = enable;
	}

	public boolean getEnablePositionIncrements() {
		return enablePositionIncrements;
	}
	
	@Override
	public boolean incrementToken() throws IOException {
		if(logger.isDebugEnabled())
			logger.debug("incrementToken KoreanStopFilter");


		// return the first non-stop word found
		int skippedPositions = 0;

		while(input.incrementToken()) {

			if(logger.isDebugEnabled())
				logger.debug("원래 리턴 될 TermAtt : " + charTermAtt.toString() + " , stopWordDic.isExist : " + stopWords.contains(charTermAtt.toString()));

			if(!stopWords.contains(charTermAtt.toString())) {
				if(enablePositionIncrements) {
					posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
				}

				return true;
			}

			skippedPositions += posIncrAtt.getPositionIncrement();
		}

		return false;
	}

}
