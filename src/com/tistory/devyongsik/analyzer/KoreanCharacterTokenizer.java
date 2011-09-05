package com.tistory.devyongsik.analyzer;


import java.io.IOException;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

/**
 * 입력되는 문장을 읽어 Token으로 만들어 return
 * split 기준은 스페이스, 특수문자, 한글 / 영문,숫자
 *
 * @author 장용석, 2011.07.16 need4spd@naver.com
 */

public class KoreanCharacterTokenizer extends Tokenizer {

	private Log logger = LogFactory.getLog(KoreanCharacterTokenizer.class);

	private CharTermAttribute charTermAtt;
	private OffsetAttribute offsetAtt;
	private TypeAttribute typeAtt;
	private PositionIncrementAttribute positionAtt;

	public KoreanCharacterTokenizer(Reader input) {
		super(input);
		offsetAtt = addAttribute(OffsetAttribute.class);
		charTermAtt = addAttribute(CharTermAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
		positionAtt = addAttribute(PositionIncrementAttribute.class);

		if(logger.isInfoEnabled()) {
			logger.info("KoreanTokenizer....constructor");
		}
	}

	public KoreanCharacterTokenizer(AttributeSource source, Reader input) {
		super(source, input);
		offsetAtt = addAttribute(OffsetAttribute.class);
		charTermAtt = addAttribute(CharTermAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
	}

	public KoreanCharacterTokenizer(AttributeFactory factory, Reader input) {
		super(factory, input);
		offsetAtt = addAttribute(OffsetAttribute.class);
		charTermAtt = addAttribute(CharTermAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
	}

	private int offset = 0, bufferIndex = 0, dataLen = 0;
	private static final int MAX_WORD_LEN = 255;
	private static final int IO_BUFFER_SIZE = 4096;
	private final char[] ioBuffer = new char[IO_BUFFER_SIZE];
	private char preChar = ' ';

	private int preCharType = 99;
	private int nowCharType = 99;

	private final int DIGIT = 0; //숫자
	private final int KOREAN = 1; //한글
	private final int ALPHA = 2; //영어

	protected boolean isTokenChar(char c) {
		return (Character.isLetter(c) || Character.isDigit(c));
	}

	protected char normalize(char c) {
		return Character.toLowerCase(c);
	}
	public void reset(Reader input) throws IOException {
		super.reset(input);
		bufferIndex = 0;
		offset = 0;
		dataLen = 0;
	}

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();

		if(logger.isInfoEnabled())
			logger.info("incrementToken");


		int length = 0;
		int start = bufferIndex;
		char[] buffer = charTermAtt.buffer();

		while (true) {

			if (bufferIndex >= dataLen) {
				offset += dataLen;
				dataLen = input.read(ioBuffer);
				if (dataLen == -1) {
					if (length > 0)
						break;
					else
						return false;
				}
				bufferIndex = 0;
			}

			final char c = ioBuffer[bufferIndex++];

			if (isTokenChar(c)) {               // if it's a token char

				//전 문자와 현재 문자를 비교해서 속성이 다르면  분리해낸다.
				if (length > 0) {
					//이전문자의 속성 set
					if(Character.isDigit(preChar)) preCharType = this.DIGIT;
					else if(preChar < 127) preCharType = this.ALPHA;
					else preCharType = this.KOREAN;

					//현재문자의 속성set
					if(Character.isDigit(c)) nowCharType = this.DIGIT;
					else if(c < 127) nowCharType = this.ALPHA;
					else nowCharType = this.KOREAN;

					if(preCharType != nowCharType) { //앞뒤 Character가 서로 다른 형식
						bufferIndex--;

						//여기서 토큰을 하나 끊어야 함
						charTermAtt.setLength(length);
					    offsetAtt.setOffset(correctOffset(start), correctOffset(start+length));
					    typeAtt.setType("word");
					    positionAtt.setPositionIncrement(1);

						return true;
					}
				}
				preChar = c;

				if (length == 0)			           // start of token
					start = offset + bufferIndex - 1;
				else if (length == buffer.length)
					buffer = charTermAtt.resizeBuffer(1+length);

				buffer[length++] = normalize(c); // buffer it, normalized

				if (length == MAX_WORD_LEN)		   // buffer overflow!
					break;

			} else if (length > 0)             // at non-Letter w/ chars
				break;                           // return 'em
		}

		if(logger.isInfoEnabled())
			logger.info("return Token");


		charTermAtt.setLength(length);
	    offsetAtt.setOffset(correctOffset(start), correctOffset(start+length));
	    typeAtt.setType("word");
	    positionAtt.setPositionIncrement(1);

		return true;
	}
}
