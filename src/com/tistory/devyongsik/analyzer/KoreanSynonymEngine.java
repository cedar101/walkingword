package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;
import org.apache.lucene.util.AttributeSource.State;

import com.tistory.devyongsik.analyzer.dictionary.DictionaryFactory;
import com.tistory.devyongsik.analyzer.dictionary.DictionaryType;

public class KoreanSynonymEngine implements Engine {

	private Log logger = LogFactory.getLog(KoreanSynonymEngine.class);

	private RAMDirectory directory;
	private IndexSearcher searcher;
	private List<String> synonyms = new ArrayList<String>();
	private static KoreanSynonymEngine synonymEngineInstance = new KoreanSynonymEngine();
	
	public static KoreanSynonymEngine getInstance() {
		return synonymEngineInstance;
	}

	private KoreanSynonymEngine() {
		if(logger.isInfoEnabled()) {
			logger.info("사전을 읽습니다.");
		}
		
		loadDictionary();
		
		if(logger.isInfoEnabled()) {
			logger.info("동의어 색인을 실시합니다.");
		}
		
		createSynonymIndex();

		if(logger.isInfoEnabled()) {
			logger.info("동의어 색인 완료");
		}
		
		try {
			searcher = new IndexSearcher(directory);
		} catch (CorruptIndexException e) {
			logger.error("동의어 색인에 대한 Searcher 생성 중 에러 발생함 : " + e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("동의어 색인에 대한 Searcher 생성 중 에러 발생함 : " + e);
			e.printStackTrace();
		}
	}
	
	private void loadDictionary() {
		DictionaryFactory dictionaryFactory = DictionaryFactory.getFactory();	
		synonyms = dictionaryFactory.create(DictionaryType.SYNONYM);
	}

	private void createSynonymIndex() {
		directory = new RAMDirectory();

		try {

			Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_31); //문서 내용을 분석 할 때 사용 될 Analyzer
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);

			IndexWriter ramWriter = new IndexWriter(directory, iwc);
			
			int recordCnt = 0;
			//동의어들을 ,로 잘라내어 색인합니다.
			//하나의 document에 syn이라는 이름의 필드를 여러개 추가합니다.
			//나중에 syn=노트북 으로 검색한다면 그때 나온 결과 Document로부터 
			//모든 동의어 리스트를 얻을 수 있습니다.
			for(String syn : synonyms) {
				String[] synonymWords = syn.split(",");
				Document doc = new Document();
				for(int i = 0, size = synonymWords.length; i < size ; i++) {
					
	
					String fieldValue = synonymWords[i];
					Field field = new Field("syn",fieldValue,Store.YES,Index.NOT_ANALYZED_NO_NORMS, TermVector.NO);
					doc.add(field);
	
					recordCnt++;
				}//end inner for
				ramWriter.addDocument(doc);
			}//end outer for
			
			ramWriter.optimize();
			ramWriter.close();


			if(logger.isInfoEnabled())
				logger.info("동의어 색인 단어 갯수 : " + recordCnt);

		} catch (CorruptIndexException e) {
			logger.error("동의어 색인 중 에러 발생함 : " + e);
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			logger.error("동의어 색인 중 에러 발생함 : " + e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("동의어 색인 중 에러 발생함 : " + e);
			e.printStackTrace();
		}
	}

	private List<String> getWords(String word) throws Exception {
		List<String> synWordList = new ArrayList<String>();
		if(logger.isDebugEnabled()) {
			logger.debug("동의어 탐색 : " + word);
		}

		Query query = new TermQuery(new Term("syn",word));
		
		if(logger.isDebugEnabled()) {
			logger.debug("query : " + query);
		}
		
		TopScoreDocCollector collector = TopScoreDocCollector.create(5 * 5, false);
		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		if(logger.isDebugEnabled()) {
			logger.debug("대상 word : " + word);
			//검색된 document는 하나이므로..
			logger.debug("동의어 갯수 : " + hits.length);
		}

		for(int i = 0; i < hits.length; i++) {
			Document doc = searcher.doc(hits[i].doc);

			String[] values = doc.getValues("syn");

			for(int j = 0; j < values.length; j++) {
				if(logger.isDebugEnabled())
					logger.debug("대상 word : " + "["+word+"]" + " 추출된 동의어 : " + values[j]);

				if(!word.equals(values[j])) {
					synWordList.add(values[j]);
				}
			}
		}
		return synWordList;
	}

	@Override
	public void collectNounState(AttributeSource attributeSource, Stack<State> nounsStack, Map<String, String> returnedTokens) throws Exception {
		CharTermAttribute charTermAttr = attributeSource.getAttribute(CharTermAttribute.class);
		OffsetAttribute offSetAttr = attributeSource.getAttribute(OffsetAttribute.class);
		
		returnedTokens.put(charTermAttr.toString()+"_"+offSetAttr.startOffset()+"_"+offSetAttr.endOffset(), "");
		
		if(logger.isDebugEnabled())
			logger.debug("넘어온 Term : " + charTermAttr.toString());
		
		List<String> synonyms = getWords(charTermAttr.toString());

		if (synonyms.size() == 0) new Stack<State>(); //동의어 없음

		for (int i = 0; i < synonyms.size(); i++) {
			
			String synonymWord = synonyms.get(i);
			String makeKeyForCheck = synonymWord + "_" + offSetAttr.startOffset() + "_" + offSetAttr.endOffset();
			
			if(returnedTokens.containsKey(makeKeyForCheck)) {
				
				if(logger.isDebugEnabled()) {
					logger.debug("["+makeKeyForCheck+"] 는 이미 추출된 Token입니다. Skip");
				}
				
				continue;
				
			} else {
				returnedTokens.put(makeKeyForCheck, "");
			}
			
			//#1. 동의어는 키워드 정보와 Type정보, 위치증가정보만 변경되고 나머지 속성들은 원본과 동일하기 때문에
			//attributeSource로부터 변경이 필요한 정보만 가져와서 필요한 정보를 변경한다.
			//offset은 원본과 동일하기 때문에 건드리지 않는다.
			CharTermAttribute attr = attributeSource.getAttribute(CharTermAttribute.class); //원본을 복사한 AttributeSource의 Attribute를 받아옴
			attr.setEmpty();
			attr.append(synonyms.get(i));
			PositionIncrementAttribute positionAttr = attributeSource.getAttribute(PositionIncrementAttribute.class); //원본 AttributeSource의 Attribute를 받아옴
			positionAttr.setPositionIncrement(0);  //동의어이기 때문에 위치정보 변하지 않음
			TypeAttribute typeAtt = attributeSource.getAttribute(TypeAttribute.class); //원본 AttributeSource의 Attribute를 받아옴
			//타입을 synonym으로 설정한다. 나중에 명사추출 시 동의어 타입은 건너뛰기 위함
			typeAtt.setType("synonym"); 
			
			nounsStack.push(attributeSource.captureState()); //추출된 동의어에 대한 AttributeSource를 Stack에 저장
		}
		return;
	}
}
