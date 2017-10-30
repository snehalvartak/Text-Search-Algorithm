import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
/**
 * 
 */

/**
 * @author Snehal Vartak
 *
 */
public class compareAlgorithms {
	
	//Path Variables
	private static final String pathToIndex = "D:/IU/Search_workspace/Assignment_2/input/index/";
	private static final String topics = "D:/IU/Search_workspace/Assignment_2/input/topics.51-100";
	private static final String shortQuery = "D:/IU/Search_workspace/Assignment_2/output/";
	private static final String longQuery = "D:/IU/Search_workspace/Assignment_2/output/";
	
	public static void main(String args[]) throws ParseException, IOException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(pathToIndex)));
		for(int i=0; i < 4;i++) {
			// Calls method for each similarity
			calculateScore(i, reader);
		}
		reader.close();
	}

	private static void calculateScore(int i, IndexReader reader) throws IOException, ParseException{
		
		
		IndexSearcher searcher = new IndexSearcher(reader);
		String similarity_type = "";
		float lambda = 0.7f; // for Language Model with Jelinek Mercer Smoothing
	//	searcher.setSimilarity(new BM25Similarity());
		//You need to explicitly specify the ranking algorithm using the respective Similarity class	
		
		Similarity similarity =null;
		switch(i) {
			case 0:
				similarity = new BM25Similarity();
				similarity_type ="BM25";
				break;
			case 1:
				similarity = new ClassicSimilarity();
				similarity_type = "Classic";
				break;
			case 2:
				similarity = new LMDirichletSimilarity();
				similarity_type = "LMDirichlet";
				break;
			case 3:
				similarity = new LMJelinekMercerSimilarity(lambda);
				similarity_type = "LMJelinekMercer";
				break;
		}
		searcher.setSimilarity(similarity);
		Analyzer analyzer = new StandardAnalyzer();
		
		QueryParser parser = new QueryParser("TEXT", analyzer); 
		
		
		String queryString ="New York";
		
		// Files to output the top 1000 results
		FileWriter shortQueryOut = new FileWriter(shortQuery.concat(similarity_type+"shortQuery.txt"));
		FileWriter longQueryOut = new FileWriter(longQuery.concat(similarity_type+"longQuery.txt"));
		
		InputStream is = new FileInputStream(topics);
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = buf.readLine();
		while(line != null){
			sb.append(line).append("\n");
			line = buf.readLine();
		}
		buf.close();
		
		//Extract all topics from the file by splitting the file.
		String[] docArray= sb.toString().split("</top>");
		
		
		for(int j =0; j<docArray.length-1;j++) {
			// Checking the null values in array to avoid IllegalException error
			if(docArray[j]!=null) {
				
				//From each topic get the substring of topic number, title, and description 
				String doc = docArray[j].trim() ;
				String num = StringUtils.substringBetween(doc, "Number:", "<dom>");
				String title = StringUtils.substringBetween(doc, "Topic:", "<desc>");
				String desc = StringUtils.substringBetween(doc, "Description:", "<smry>");
				//System.out.println(num);
				
				// For Short query
				Query shortQuery = parser.parse(QueryParser.escape(title));
				TopScoreDocCollector shortCollector = TopScoreDocCollector.create(1000);
				searcher.search(shortQuery, shortCollector);
				ScoreDoc[] shortDocs = shortCollector.topDocs().scoreDocs;
				for (int k = 0; k < shortDocs.length; k++) {
					Document doc1 = searcher.doc(shortDocs[k].doc);
					//System.out.println(doc1.get("DOCNO")+" "+shortDocs[k].score);
					shortQueryOut.write(Integer.parseInt(num.trim())+ " Q0 " + doc1.get("DOCNO") + " " + (k+1) + " " + shortDocs[k].score + " "+similarity_type+ "short");
					shortQueryOut.write("\n");
				}		
				// For Long query
				Query longQuery = parser.parse(QueryParser.escape(desc));
				TopScoreDocCollector longCollector = TopScoreDocCollector.create(1000);
				searcher.search(longQuery, longCollector);
				ScoreDoc[] docs = longCollector.topDocs().scoreDocs;
				for (int k = 0; k < docs.length; k++) {
					Document doc2 = searcher.doc(docs[k].doc);
					//System.out.println(doc2.get("DOCNO")+" "+docs[k].score);
					longQueryOut.write(Integer.parseInt(num.trim())+ " Q0 " + doc2.get("DOCNO") + " " + (k+1) + " " + docs[k].score + " "+similarity_type+ "long");
		        	longQueryOut.write("\n");
				}		
			}
		}
		shortQueryOut.close();
		longQueryOut.close();
	}
}
