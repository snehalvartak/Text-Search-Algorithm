/**
 * @author Snehal Vartak
 * Dependency: easySearch.java 
 * Set the path for the index and output files 
 * 
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;

public class searchTRECtopics {

	// Set path variables
	private static final String topics = "D:/IU/Search_workspace/Assignment_2/input/topics.51-100";
	private static final String shortQuery = "D:/IU/Search_workspace/Assignment_2/output/mySearch_shortQuery.txt";
	private static final String longQuery = "D:/IU/Search_workspace/Assignment_2/output/mySearch_longQuery.txt";
	
	public static void main(String[] args) throws ParseException{
		
		System.out.println("Parsing Topics to generate queries...");		
		try {
			// Output file for Short query result
			FileWriter shortQueryOut = new FileWriter(shortQuery);
			FileWriter longQueryOut = new FileWriter(longQuery);
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
			
			
			for(int i =0; i<docArray.length-1;i++) {
				// Checking the null values in array to avoid IllegalException error
				if(docArray[i]!=null) {
					
					//From each topic get the substring of topic number, title, and description 
					String doc = docArray[i].trim() ;
					String num = StringUtils.substringBetween(doc, "Number:", "<dom>");
					String title = StringUtils.substringBetween(doc, "Topic:", "<desc>");
					String desc = StringUtils.substringBetween(doc, "Description:", "<smry>");
					//System.out.println(num);
					
					Map<String, Double>shortQueryResult = easySearch.relevanceScore(title.trim());
					// Counter to just get the top 1000 results for the short query
					int shortCnt = 1;
				    for(String key : shortQueryResult.keySet()) {
				        if (shortCnt <=1000) {
				        	shortQueryOut.write(Integer.parseInt(num.trim())+ " Q0 " + key + " " + shortCnt + " " + shortQueryResult.get(key) + " mySearchShort");
				        	shortQueryOut.write("\n");
				        }
				        shortCnt++;
				    }
					Map<String, Double> longQueryResult = easySearch.relevanceScore(desc.trim());
					// Counter to just get the top 1000 results for the short query
					int longCnt = 1;
				    for(String key : longQueryResult.keySet()) {
				        if (longCnt <=1000) {
				        	longQueryOut.write(Integer.parseInt(num.trim())+ " Q0 " + key + " " + longCnt + " " + longQueryResult.get(key) + " mySearchLong");
				        	longQueryOut.write("\n");
				        }
				        longCnt++;
				    }
				}
			}
			shortQueryOut.close();
			longQueryOut.close();
			//System.out.println(longQ.size());
			
					
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}


}

	

