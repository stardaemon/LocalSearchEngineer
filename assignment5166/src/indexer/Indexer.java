package indexer;
import indexer.Stemmer;
import indexer.Tokenization;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;



public class Indexer {
	
	String colDirection;
	String indexDirection;
	String stopText;
	private File[] listOfFiles;
	private List<String> stringInFile = new ArrayList<String>();
	//hash map to save different files' term and frequency
	private List<HashMap<String, Integer>> filesTermList = new ArrayList<HashMap<String,Integer>>();
	//the final result
	private List<String> result = new ArrayList<String>();
	//save all the terms, frequency in this HashMap is incorrect
	private HashMap<String,Integer> allTerms = new HashMap<String,Integer>();
	
	
	public Indexer(String colDirection, String indexDirection, String stopText)
	{
		this.colDirection = colDirection;
		this.indexDirection = indexDirection;
		this.stopText = stopText;
	}
	
	//plays as the main when call it.
	public void indexer() throws IOException
	{
		//read direction of collections
		File folder = new File(colDirection);
		//all files in the direction 
		listOfFiles = folder.listFiles();
		//save one file to one string
		for(File a : listOfFiles)
		{
			this.stringInFile.add(fileReader( a.toString() ) );
			//test read from direction
			//System.out.println(a.toString());
		}
		//the no. of files in this direction
		int number = stringInFile.size();
		// deal contents occured in each file independently
		for(String s : stringInFile)
		{
			//tokenization
			Tokenization tok = new Tokenization(s);
			List<String> tokenResult = tok.tokenDocument();
			
			//System.out.println("---------------------------------------            " + tokenResult.size());
			//convert List to ArrayList
			ArrayList<String> listOfStrings = new ArrayList<>(tokenResult.size());
			listOfStrings.addAll(tokenResult);
			//stop words
			List<String> delStopResult = delStopWords(listOfStrings, stopText);
			//System.out.println("---------------------------------------            " + delStopResult.size());
			//stemmer
			String[] stockArr = (String[]) delStopResult.toArray(new String[delStopResult.size()]);
			//System.out.println("---------------------------------------            " + stockArr.length);
			List<String> res = Stemmer.stemmer(stockArr);
			//System.out.println("---------------------------------------            " + res.size());
			//count term frequency
			HashMap<String, Integer> termFreq = countFrequency(res);
			filesTermList.add(termFreq);
			allTerms.putAll(termFreq);
			System.out.println("One File completed");
		}
		
		//combine all terms frequency together and calculate matched IDF
		for(String key : allTerms.keySet() )
		{
			
			String termOccure = key;
			//file frequency
			int count = 0;
			for(int i = 0; i < number; i++)
			{
				HashMap<String, Integer> hm = filesTermList.get(i);
				if(hm.containsKey(key))
				{
					termOccure += " ," + "d" + i +" ," + hm.get(key);
					count++;
				}
			}
			double idf = idf(count, number);
			termOccure += " ," + idf;
			result.add(termOccure);
		}
		
		//write to the index.txt
		writefile(indexDirection);
	}
	
	
	//read a file and return String type
	private String fileReader(String fileName) throws IOException
	{
		String everything = "";
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();
		    
		    while (line != null) {
		        sb.append(line);
		        //add a space into each line in order to divide them.
		        sb.append(" ");
		        line = br.readLine();
		    }
		    everything = sb.toString();
		} finally {
		    br.close();
		}		
		return everything;
	}
	
	//read all stop words
	private List<String> stopWords(String stopText) throws IOException
	{
		List<String> stopWords = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(stopText));
		try {
		    String line = br.readLine();
		    while (line != null) {
		        stopWords.add(line);
		        line = br.readLine();
		    }
		} finally {
		    br.close();
		}	
		return stopWords;
	}
	
	//delete stop words from the tokenization result.
	private List<String> delStopWords(ArrayList<String> tokenWords, String stopText ) throws IOException
	{
		//List<String> delStopList = new ArrayList<String>();
		List<String> stopList = new ArrayList<String>();
		stopList = stopWords(stopText);		
		List<String> copyOfToken = new ArrayList<String>(tokenWords);
		//test code
		//System.out.println(copyOfToken.size());
		for(int i = 0; i < tokenWords.size() - 1; i++)
		{
			if(tokenWords.get(i).length() == 1)
			{
				copyOfToken.remove(tokenWords.get(i));
			}
			else
			{
				for( int j = 0; j < stopList.size() - 1; j++)
				{
					if( tokenWords.get(i).equals( stopList.get(j)) )
					{
						copyOfToken.remove(tokenWords.get(i));
						//System.out.println("delete");
					}
				}
			}
		}
		//test code
		//System.out.println(copyOfToken.size());
		copyOfToken.removeAll(Collections.singleton(""));
		copyOfToken.removeAll(Collections.singleton(null));
		return copyOfToken;
	}
	
	//count the same term frequency in one document
	private HashMap<String, Integer> countFrequency(List<String> terms)
	{
		HashMap<String, Integer> wordFreq = new HashMap<String, Integer>();		
		for(String s : terms )
		{
			if(wordFreq.containsKey(s))
			{
				wordFreq.put(s, wordFreq.get(s) + 1);
			}
			else
			{
				wordFreq.put(s,1);
			}
			
		}		
		return wordFreq;
	}
	

	//calculate idf
	public static double idf(int docTermCount, int totalNumDocuments) 
	{ 
		//if used in MAc, the total number should minus 1 because there will be a hide document in each document direction.
		return Math.log( ((double)(totalNumDocuments - 1)) / (double)docTermCount ); 
	}
	
	//write into the file
    public void writefile(String dir)
    {
    	try
    	{
    		BufferedWriter output = null;
    		String fullAdd = dir + "/index.txt";
    		File file = new File(fullAdd);
    		output = new BufferedWriter(new FileWriter(file));

    		for(String str: result) 
    		{
    			output.write(str);
    			output.newLine();
    		}
    		output.close();
    		System.out.println("File has been written");

    	}
    	catch(Exception e)
    	{
        System.out.println("Could not create file");
    	}
    }
		
	
	public static void main(String[] args) throws IOException
	{
		Indexer ind = new Indexer("/Users/liyunhong/Documents/Monash/Semester2_2016/FIT5166/sampleFiles/collection", 
				"/Users/liyunhong/Documents/Monash/Semester2_2016/FIT5166/sampleFiles",
				"/Users/liyunhong/Documents/Monash/Semester2_2016/FIT5166/sampleFiles/stopwords.txt");
		ind.indexer();
		//ind.locationIndexer();
	}
	
	
	
	//HD part Get term location in the documents.
	public ArrayList<String> termLocation(ArrayList<String> locationString)
	{
		ArrayList<String> res = new ArrayList<String>();
		ArrayList<String> afterProcessList = locationString;
		//a hash map to save unique terms and their frequency
		HashMap<String, Integer> termFrequency = new HashMap<String, Integer>();
		//save the location of terms in the document.
		HashMap<String, String> termLocations = new HashMap<String, String>();
		//get frequency and unique terms in the documents
		for(String t : afterProcessList)
		{
			if(termFrequency.containsKey(t))
			{
				termFrequency.put(t, termFrequency.get(t) + 1);
			}
			else
			{
				termFrequency.put(t, 1);
			}
			
		}		
		//two loop to find out all locations.
		for(String term : termFrequency.keySet())
		{
			//new String to save location info.
			String s = " ";
			for(int i = 0; i < afterProcessList.size() - 1; i++)
			{
				if(term.equals(afterProcessList.get(i)))
				{
					 s = s + "[" + (i+1) + "]";
				}
			}
			termLocations.put(term, s);
		}
		//combine frequency and location hash map to one string list.
		for(String s : termFrequency.keySet())
		{
			String combineString = s + " ," + termFrequency.get(s) + "  " + termLocations.get(s) + " ,";
			res.add(combineString);
		}
		return res;
	}
	
	
	//the HD part main method when called.
	//plays as the main when call it.
	public void locationIndexer() throws IOException
	{
		//read direction of collections
		File folder = new File(colDirection);
		//all files in the direction 
		listOfFiles = folder.listFiles();
		//save one file to one string
		for(File a : listOfFiles)
		{
			this.stringInFile.add(fileReader( a.toString() ) );
			//test read from direction
			//System.out.println(a.toString());
		}
		//the no. of files in this direction
		int number = stringInFile.size();
		//list to store all files term independently.
		ArrayList<ArrayList<String>> fileTerms = new ArrayList<ArrayList<String>>();
		// deal contents occured in each file independently		
		for(String s : stringInFile)
		{
			//tokenization
			Tokenization tok = new Tokenization(s);
			List<String> tokenResult = tok.tokenDocument();			
			//convert List to ArrayList
			ArrayList<String> listOfStrings = new ArrayList<>(tokenResult.size());
			listOfStrings.addAll(tokenResult);
			//stop words
			List<String> delStopResult = delStopWords(listOfStrings, stopText);
			//stemmer
			String[] stockArr = (String[]) delStopResult.toArray(new String[delStopResult.size()]);
			List<String> res = Stemmer.stemmer(stockArr);
			HashMap<String, Integer> termFreq = countFrequency(res);
			//save all unique terms.
			allTerms.putAll(termFreq);
			
			//using location method to the res.
			ArrayList<String> locResult = termLocation((ArrayList<String>)res);
			//add result to the filTerms.
			fileTerms.add(locResult);
			
			System.out.println("One File completed");
		}
		
		for(String key : allTerms.keySet())
		{
			String termOccur = key;
			for(int i = 0; i < number; i++)
			{
				ArrayList<String> s = fileTerms.get(i);
				for(String inFile : s)
				{
					String[] termInFile = inFile.split(" ,");
					//get the term
					String term = termInFile[0];
					if( key.equals(term))
					{
						termOccur += " ," + "d" + i +" ," + termInFile[1];
					}
				}
			}
			result.add(termOccur);
		}
		//write to the index.txt
		writeNewFile(indexDirection);
	}
	
	
	//write a new file instead of index.txt
    public void writeNewFile(String dir)
    {
    	try
    	{
    		BufferedWriter output = null;
    		String fullAdd = dir + "/locationIndex.txt";
    		File file = new File(fullAdd);
    		output = new BufferedWriter(new FileWriter(file));

    		for(String str: result) 
    		{
    			output.write(str);
    			output.newLine();
    		}
    		output.close();
    		System.out.println("File has been written");

    	}
    	catch(Exception e)
    	{
        System.out.println("Could not create file");
    	}
    }
	
}
