package com.tistory.devyongsik.analyzer;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.tistory.devyongsik.analyzer.util.AnalyzerTestUtil;
import com.tistory.devyongsik.analyzer.util.TestToken;

public class KoreanCompoundNounFilterTest extends AnalyzerTestUtil {
	private Set<TestToken> compondNouns = new HashSet<TestToken>();
	StringReader reader = new StringReader("월드컵조직위원회분과위");

	@Before
	public void initDictionary() {
		compondNouns.add(getToken("분과위", 8, 11));
		compondNouns.add(getToken("위원회", 5, 8));
		compondNouns.add(getToken("조직", 3, 5));
		compondNouns.add(getToken("월드컵", 0, 3));
		compondNouns.add(getToken("월드컵조직위원회분과위", 0, 11));
	}

	@Test
	public void testStemCase() throws Exception {
		TokenStream stream = new KoreanCompoundNounFilter(new KoreanCharacterTokenizer(reader));
		CharTermAttribute charTermAtt = stream.getAttribute(CharTermAttribute.class);
		OffsetAttribute offSetAtt = stream.getAttribute(OffsetAttribute.class);

		while(stream.incrementToken()) {
			TestToken t = getToken(charTermAtt.toString(), offSetAtt.startOffset(), offSetAtt.endOffset());
			System.out.println("termAtt.term() : " + charTermAtt.toString());
			System.out.println("offSetAtt : " + offSetAtt.startOffset());
			System.out.println("offSetAtt : " + offSetAtt.endOffset());

			Assert.assertTrue(compondNouns.contains(t));
		}
	}
}
