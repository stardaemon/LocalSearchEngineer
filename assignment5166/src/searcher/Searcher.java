package searcher;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;



public class Searcher {

	private String indexDir;
	private String locIndexDir;
	private int docNum;
	private String query = "";
	//store all strings read form index.txt
	private ArrayList<String> list = new ArrayList<String>();
	//store all weights instead of tf in each line
	private ArrayList<String> docs = new ArrayList<String>();

	
	public Searcher(String indexDir,  int docNum, String[] input)
	{
		this.indexDir = indexDir + "/index.txt" ;
		this.locIndexDir = indexDir + "/locationIndex.txt";
		this.docNum = docNum;
		for(String s : input)
		{
			this.query += s + " ";
		}
	}
	
	public void searcher() throws IOException
	{
		readIndexFile(indexDir);
		//test code
		//System.out.println(list.get(0));
		//System.out.println(list.get(list.size() - 1));
		replaceTfInWeight();
		HashMap<String, Double> res = cosSim();
		Map<String,Double> orderRes = orderRes(res);
		ArrayList<String> sortedResult = new ArrayList<String>();
		for(String treeString : orderRes.keySet())
		{
			sortedResult.add(treeString);
		}
		//compare number of documents and user wanted number of results
		int smallNum = res.size();
		if(res.size() > docNum)
			smallNum = docNum;
		//declare format of 3 dicemal
		DecimalFormat df = new DecimalFormat("0.000");
		for(int i = 0; i < smallNum; i++)
		{			
			//if(sortedResult.get(i).charAt(1) != '0')
			{
				System.out.println(sortedResult.get(i) + " ," + df.format(res.get(sortedResult.get(i))) );
			}
		}
	}
	
	//read index.txt
	private ArrayList<String> readIndexFile(String dir) throws IOException
	{
		Scanner s = new Scanner(new File(dir));
		while (s.hasNextLine()){
		    list.add(s.nextLine());
		}
		s.close();
		return list;
	}
	 
	//recreate each string in documents, using weight replaces tf. 
	private ArrayList<String> replaceTfInWeight()
	{
		for(String s : list)
		{
			String[] splite = s.split(" ,");
			//calculate no. of documents per term
			int numDocPerLine = (splite.length - 2) / 2 ;
			//using weight of this term in documents replace term weight.
			for( int j = 1; j < numDocPerLine + 1; j++)
			{
				double tf = Double.parseDouble(splite[j*2]);
				double idf = Double.parseDouble(splite[splite.length - 1]);
				splite[j*2] = tf*idf + "";
			}
			//recreate the string
			String newLine = "";
			for(int h = 0; h < splite.length; h++)
			{
				newLine = newLine + splite[h] + " ,";
			}
			docs.add(newLine);
			//test code
			/*if(splite[0].equals("mistreat"))
			{
				System.out.println(newLine);
			}*/
		}
		return docs;
	}
	
	//calculate each documents weight.
	private HashMap<String, Double> calDocWeight()
	{
		HashMap<String, Double> docsWeight = new HashMap<String,Double>();
		ArrayList<String> weightDocs = docs;
		for( String s : weightDocs)
		{
			String[] splite = s.split(" ,");
			//calculate no. of documents per term
			int numDocPerLine = (splite.length - 2) / 2 ;
			for( int j = 0; j < numDocPerLine; j++)
			{
				String docNum = splite[1 + j*2];
				//convert weight of doc to double
				double weightInDoc = Double.parseDouble(splite [2 + j*2]);
				//calculate power of weight.
				double docValue = Math.pow(weightInDoc, 2);
				//determine whether the file first occurs in the list
				if(docsWeight.containsKey(docNum))
				{
					//sum up all weights in documents.
					docsWeight.put(docNum, docsWeight.get(docNum) + docValue);
				}
				else
				{
					docsWeight.put(docNum, docValue);
				}
			}
		}
		//calculate the final weight of each document.
		for(String h : docsWeight.keySet())
		{
			docsWeight.put(h, Math.pow(docsWeight.get(h), 0.5));
		}		
		return docsWeight;
	}
	
	//calculate term frequency in the query
	private HashMap<String, Double> calQueryTf()
	{
		HashMap<String,Double> queryTf = new HashMap<String, Double>();
		String[] splite = this.query.split(" ");
		for(String s : splite)
		{
			if(queryTf.containsKey(s))
			{
				queryTf.put(s, queryTf.get(s) + 1);
			}
			else
			{
				queryTf.put(s,  1.0);
			}
		}		
		return queryTf;
	}
	
	//calculate term weight in the query
	private HashMap<String, Double> calQueryWeight()
	{
		HashMap<String, Double> queryWeight = calQueryTf();
		//combine query terms with them weights together.
		for(String s : docs)
		{
			String[] divideS = s.split(" ,");
			if( queryWeight.containsKey(divideS[0]))
			{
				double idf = Double.parseDouble(divideS[divideS.length - 1]);
				queryWeight.put(divideS[0], queryWeight.get(divideS[0]) * idf );
			}
		}
		
		return queryWeight;
	}
	
	//calculate cosine with query and documents
	private HashMap<String, Double> cosSim()
	{
		//get docs weight
		HashMap<String,Double> docsWeigh = calDocWeight();
		//get query weight
		HashMap<String,Double> queryWeigh = calQueryWeight();
		//initial to get results
		HashMap<String, Double> result = calDocWeight();
		for(String ini : result.keySet())
		{
			result.put(ini, 0.0);
		}
		
		for(String s : docs)
		{
			String[] divideS = s.split(" ,");
			if( queryWeigh.containsKey(divideS[0]))
			{
				//calculate no. of documents in the term
				int numDocPerLine = (divideS.length - 2) / 2 ;
				for(int i = 0; i < numDocPerLine; i++)
				{
					double dcWei = Double.parseDouble(divideS[2 + i*2]);
					// sum up weight of doc * weight of query, install to hash map.
					result.put(divideS[1 + i*2], result.get(divideS[1 + i*2] ) + dcWei * queryWeigh.get(divideS[0]));
				}
			}
		}
		//calculate  square of query power 2
		double queryOfSquare = 0.0;
		for(String q : queryWeigh.keySet())
		{
			queryOfSquare += Math.pow(queryWeigh.get(q), 2);
		}
		//final query weight 
		queryOfSquare = Math.pow(queryOfSquare, 0.5);
		
		//get the final result
		for(String fin : result.keySet())
		{
			double res = result.get(fin) / (queryOfSquare * docsWeigh.get(fin));
			result.put(fin, res);
		}		

		return result;
	}
	
	
	//sort list
	private Map<String, Double> orderRes(HashMap<String, Double> res)
	{
		
		Map<String,Double> resOfOrder = new HashMap<String,Double>();		
		for(String s1 : res.keySet())
		{
			resOfOrder.put(s1, res.get(s1));
		}
		resOfOrder = sortByValues(res);
	
		return resOfOrder;
	}
	
	//sort by values, references from http://stackoverflow.com/questions/1448369/how-to-sort-a-treemap-based-on-its-values
	public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
	    Comparator<K> valueComparator =  new Comparator<K>() {
	        public int compare(K k1, K k2) {
	            int compare = map.get(k2).compareTo(map.get(k1));
	            if (compare == 0) return 1;
	            else return compare;
	        }
	    };
	    Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
	    sortedByValues.putAll(map);
	    return sortedByValues;
	}

	
	
	public static void main(String[] args) throws Exception
	{
		String[] par = new String[2];
		par[0] = "animas";
		par[1] = "History";
		Searcher ind = new Searcher("/Users/liyunhong/Documents/Monash/Semester2_2016/FIT5166/sampleFiles", 10, par);
		//ind.searcher();
		//ind.locSearcher();
		ind.checkInput();
	}
	
	
	
	/**
	 * 
	 * HD part search function using the location of files.
	 * 
	 */
	public void locSearcher() throws IOException
	{
		readIndexFile(locIndexDir);
		//store all files occur in query with their frequency and locations. e.g. d1 ,2 [1][4] ,d3 ,2 [3][4].
		String fileOccur = "";
		String[] queryTerms = this.query.split(" ");
		for(int i = 0; i < this.list.size(); i++)
		{
			//split term, document name and frequency.
			String[] splitEachLine = this.list.get(i).split(" ,");
			for(int j = 0; j < queryTerms.length; j++)
			{
				if( queryTerms[j].equals(splitEachLine[0]))
				{
					for( int h = 1 ; h < splitEachLine.length; h ++ )
					{
						fileOccur += splitEachLine[h] + " ,";
					}
				}
			}
		}		
		//System.out.println(fileOccur);
		
		//split results to separate documents and numbers.
		String[] spFiles = fileOccur.split(" ,");
		//number of terms occur in documents instead of frequency, the key is the doc name.
		HashMap<String, Integer> queryNum = new HashMap<String, Integer>();
		//store file and all its other info(term frequency and locations)
		HashMap<String, String> locTerms = new HashMap<String, String>();
		for(int k = 0; k < spFiles.length; k += 2)
		{
			if(queryNum.containsKey(spFiles[k]))
			{
				queryNum.put(spFiles[k], queryNum.get(spFiles[k]) + 1);
				//info looks like 2 [1][4] 2 [3][4]
				locTerms.put(spFiles[k], locTerms.get(spFiles[k]) + "   " + spFiles[k + 1]);
				//test code
				//System.out.println(spFiles[k]);
				//System.out.println(queryNum.get(spFiles[k]));
				//System.out.println("======" + locTerms.get(spFiles[k]));
			}
			else
			{
				queryNum.put(spFiles[k], 1);
				locTerms.put(spFiles[k], spFiles[k + 1]);
				//System.out.println("----------" + spFiles[k] + "  " + locTerms.get(spFiles[k]));
			}
		}
		
		//get all locations out.
		HashMap<String, ArrayList<Integer>> locOut = new HashMap<String, ArrayList<Integer>>();
		for(String fileName : locTerms.keySet())
		{
			String[] delTf =  locTerms.get(fileName).split("   ");
			/*ArrayList<String> delSpace = new ArrayList<String>();
			for(int m = 0; m < delTf.length; m ++)
			{
					String newStr = delTf[m].trim();
					if(!(newStr.equals("")))
					{
						delSpace.add(newStr);
					}
			}
			delSpace.removeAll(Collections.singleton(null));
			delSpace.removeAll(Collections.singleton(""));		*/
			//test code
			//System.out.println("\\\\\\\\" + locTerms.get(fileName));	
			String locOneFile = "";
			for( int p = 0; p < (delTf.length)  ; p=p+2)
			{
				//looks like  [1][4][3][8]
				locOneFile += delTf[p + 1];
			}

			//System.out.println("+++++++" + locOneFile);
			//process [] in the string.			
			String[] delLeftSquareBrackets = locOneFile.split("\\[");
			ArrayList<Integer> location = new ArrayList<Integer>();
			for(int u = 1; u < delLeftSquareBrackets.length; u++)
			{
				//System.out.println(delLeftSquareBrackets[u] + "  . " + delLeftSquareBrackets[u].length());
				int convertInt = Integer.parseInt(delLeftSquareBrackets[u].substring(0, delLeftSquareBrackets[u].length() - 1));
				location.add(convertInt);
				//System.out.print(convertInt + " ");
			}
						
			locOut.put(fileName, location);
		}
		
		//check whether the location in one file is close to others.
		HashMap<String,Double> markDoc = new HashMap<String,Double>();
		//single documents
		for(String fileName: locOut.keySet())
		{
			ArrayList<Integer> locations = locOut.get(fileName);
			//single location
			for(int y = 0; y < locations.size() - 1; y++)
			{
				//single location to compare
				for(int w = y + 1; w < locations.size(); w++)
				{
					if((locations.get(y) == locations.get(w) - 1) ||(locations.get(y) == locations.get(w) + 1))
					{
						//System.out.println(fileName);
						//System.out.println(locations.get(y) + " =============" +locations.get(w));
						if(markDoc.containsKey(fileName))
						{
							markDoc.put(fileName, markDoc.get(fileName) + 1);
						}
						else
						{
							markDoc.put(fileName, 1.0);
						}						
					}
				}
			}
			
			//after compare, if there is no neighbour terms in doc
			if (!(markDoc.containsKey(fileName)))
			{
				markDoc.put(fileName, 0.0);
			}
		}
		
		Map<String,Double> orderRes = orderRes(markDoc);
		for(String file: orderRes.keySet())
		{
			System.out.println( file + ":  " +  markDoc.get(file) + " terms closing to each other; " + queryNum.get(file) + " terms in query occurs."  ); 
		}
		
	}
	
	
	
	//auto check query. references from https://www.javacodegeeks.com/2010/05/did-you-mean-feature-lucene-spell.html.
	public void checkInput() throws Exception
	{
		File dir = new File("/Users/liyunhong/Documents/Monash/Semester2_2016/FIT5166/sampleFiles/collection1/");
		Directory directory = FSDirectory. getDirectory(dir);
        SpellChecker spellChecker = new SpellChecker(directory);
        spellChecker.indexDictionary(
        new PlainTextDictionary(new File("/Users/liyunhong/Documents/Monash/Semester2_2016/FIT5166/sampleFiles/CollectionTopics.txt")));
		int suggestionsNumber = 1;
        String[] termsInQuery = this.query.split(" ");
        System.out.print("Suggested Query Is: ");
        for(int i = 0; i < termsInQuery.length; i++)
        {
	        String[] suggestions = spellChecker.suggestSimilar(termsInQuery[i], suggestionsNumber);
		    if (suggestions!=null && suggestions.length>0) 
		    {
		        for (String word : suggestions)
		        {
			       System.out.print(" " + word);
			    }
			}
		    else 
		    {
			    System.out.println(" "+ termsInQuery[i]);
			}
        }
	}
	
	
}
