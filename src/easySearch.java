/*
 * @author Snehal Vartak
 *
 */
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;


public class easySearch {
	
	private static final String indexPath = "D:/IU/Search_workspace/Assignment_2/input/index/";
	
	public static void main(String[] args) throws ParseException, IOException {
		
		String queryText = "New York";
		Map<String, Double> result = relevanceScore(queryText);
		for(String id : result.keySet()) {
			System.out.println(id + " " + result.get(id));
		} 
	}
	
	// This function calculates the relevance score of the document given a query
	public static Map<String, Double> relevanceScore(String queryText) throws IOException, ArithmeticException, ParseException{
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);

		/**
		 * Get query terms from the query string
		 */
		//queryText = "New York";

		// Get the preprocessed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(QueryParser.escape(queryText));
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		
		// Store the TFIDF relevance score for each document
		HashMap<String, Double> relevance = new HashMap<>();
		
		// Store the relevance score for each term
		Map<String, HashMap<String, Double>> termRelevance = new HashMap<String,HashMap<String, Double>>();
		/**
		 * The term frequency calculation starts here
		 * Get the document length and term frequency 
		 */
		// Use DefaultSimilarity.decodeNormValue(…) to decode normalized
		// document length
		ClassicSimilarity dSimi = new ClassicSimilarity();
		// Get the segments of the index
		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
		

		/** To calculate the Inverse Document Frequency(IDF) we need -
		 * N  ==> total number of documents in AP89, 
		 * k(t) ==> the total number of documents that have the term t
		 */
		// Get N  ==> total number of documents in AP89
		int N = reader.maxDoc();
		
		// k(t) ==> the total number of documents that have the term t
		for (Term t : queryTerms) {
			// Document frequency 
			int df = reader.docFreq(new Term("TEXT", t.text()));
			System.out.println("Number of documents containing the term "+t.text()+" for field \"TEXT\": "+df);
			
			// The Inverse Document Frequency is calculated as below
			double inverseDF = 0.0;
			// Handle for 0 document freq division
			if (df !=0) {
				inverseDF = Math.log(1+ (N / df) );
			}
			System.out.println(inverseDF);
			
			// Processing each index segment
			for (int i = 0; i < leafContexts.size(); i++) {
				// Get document length
				LeafReaderContext leafContext = leafContexts.get(i);
				int startDocNo = leafContext.docBase; // Doc No for the corresponding field
				//int numberOfDoc = leafContext.reader().maxDoc();
				
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));
				
				// Get frequency of the term "police" from its postings
				if (de != null) {

					while ((de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
						int term_count = de.freq();
						int docID = de.docID() + startDocNo;
						String docNo = searcher.doc(docID).get("DOCNO");
//						System.out.println(t.text() + " occurs " + term_count + " time(s) in doc(" + (de.docID() + startDocNo)+ " "+ docNo+ ")");
						// Get normalized length (1/sqrt(numOfTokens)) of the document
						float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(de.docID()));
						// Get length of the document
						float docLeng = 1 / (normDocLeng * normDocLeng);
						
						float normalizedTermFreq = term_count/docLeng;
						HashMap<String, Double> temp;
						if(termRelevance.containsKey(docNo)){
							temp = termRelevance.get(docNo);
						}
						else {
							temp = new HashMap<String, Double>();
						}
						temp.put(t.text(), normalizedTermFreq * inverseDF );
						termRelevance.put(docNo, temp); // this map store the relevance score of each term for each document
						
						//Store the overall relevance score of each document for a given query
						if (relevance.containsKey(docNo)) {
							double tfIDFScore = (normalizedTermFreq * inverseDF)+ relevance.get(docNo);
							relevance.put(docNo, tfIDFScore );
						}
						else {
							double tfIDFScore = (normalizedTermFreq * inverseDF); 
							relevance.put(docNo, tfIDFScore );
						}
					}
				}
				
			}
		}
		return sortByRelevance(relevance);  /// return the documents based on relevance score
	}
	
	// to sort the relevance score based on query
	// Below function is taken from https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByRelevance(Map<K, V> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(
						Map.Entry::getKey, 
						Map.Entry::getValue, 
						(e1, e2) -> e1, 
						LinkedHashMap::new));
	}
}
