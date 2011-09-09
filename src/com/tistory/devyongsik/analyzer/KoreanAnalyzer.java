package com.tistory.devyongsik.analyzer;
 
import java.io.Reader;
 
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
 
/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 8. 31.
 *
 */
public class KoreanAnalyzer extends ReusableAnalyzerBase {
 
           @Override
           protected TokenStreamComponents createComponents(final String fieldName,
                                final Reader reader) {
                     Tokenizer tokenizer = new KoreanCharacterTokenizer(reader);
                     TokenStream tok = new KoreanStemFilter(tokenizer);
                     tok = new KoreanStopFilter(tok);
                     tok = new KoreanCompoundNounFilter(tok);
                     tok = new KoreanBaseNounFilter(tok);
                     tok = new KoreanLongestNounFilter(tok);
                     tok = new KoreanSynonymFilter(tok);
                    
                     return new TokenStreamComponents(tokenizer, tok);
           }
 
}