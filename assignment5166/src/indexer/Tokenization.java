/**
 * this class includes methods used in tokenization.
 */
package indexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author liyunhong
 *
 */
public class Tokenization {

	private String input;
	List<String> original;
	List<String> result ;
	

	
	//initial
	public Tokenization(String in)
	{
		this.input = in;
		this.original = new ArrayList<String>();
		this.result = new ArrayList<String>();
	}
	
	//main method to tokenization
	public List<String> tokenDocument()
	{
		//split white space
		spliteWhiteSpace();
		//extract ' ' words
		List<String> secondStep = extracSingalQuaMark();
		result.addAll(secondStep);
		
		//extract start with capital
		List<String> thirdStep = capitalWords();
		result.addAll(thirdStep);
		
		//delete last character if it is a signal and delete hyphens in terms
		List<String> fourthStep = delHyphen();		
		result.addAll(fourthStep);

		return result;
	}
	
	//split the full input with white space at first
	private List<String> spliteWhiteSpace(){
		this.original = new ArrayList<String>(Arrays.asList(input.trim().split(" ")));
		//delete null
		original.removeAll(Collections.singleton(null));
		original.removeAll(Collections.singleton(""));
		return original;
	}
	
	//determine whether there are some terms included into the ''
	private List<String> extracSingalQuaMark()
	{
		List<String> withoutQuas = new ArrayList<String>();
		//position of input
		int i = 0;
		//position of output
		int j = 0;
		//all positions in documents include single quotation Mark 
		List<String> position = new ArrayList<String>();
		
		//test code
		/*for(String hhh : original)
		{
			System.out.println(hhh);
		}*/		
		//System.out.println(original.contains(""));
		
		while( i < original.size() )
		{
			int prePosition = i;
			if( original.get(i) != "" && original.get(i).charAt(0) == '\'' )
			{
				position.add(original.get(i) );
				String quotationMark = original.get(i);
				//the maximum length of single quotation is 8
				while( (i < original.size() - 1) && ((i - prePosition) < 8) && (original.get(i).charAt( original.get(i).length() - 1) != '\'') )
				{
					quotationMark += " " + original.get(++i);
					position.add(original.get(i));
				}
				//delete quotation mark in string
				String delQuotationMark = quotationMark.substring(1, quotationMark.length() - 1);
				withoutQuas.add(j, delQuotationMark);
				j++;
				i++;
			}
			else
			{
				i++;
			}
		}
		
		//delete single quotation marks from the original list.
		this.original.removeAll(position);

		return withoutQuas;
	}
	
	//add near capital words together
	private List<String> capitalWords()
	{
		List<String> capitalWordsList = new ArrayList<String>();
		//position of input
		int i = 0;
		//position of output
		int j = 0;
		//all positions in documents include Capital Words.
		List<String> position = new ArrayList<String>();
		
		//using ++i therefore -2
		while( i < ( original.size() -2 ) )
		{
			if( Character.isUpperCase(original.get(i).charAt(0)))
			{
				//add position to the list.
				position.add(original.get(i));
				String capitalWords = original.get(i);
				//start with capital, not end with dot and next word start with capital.
				while( (original.get(i).charAt( original.get(i).length() - 1) != '.') && 
						(original.get(i).charAt( original.get(i).length() - 1) != ',')&&
						Character.isUpperCase(original.get(i + 1).charAt(0)) )
				{
					capitalWords += " " + original.get(++i);
					position.add(original.get(i));
				}				
				
				//delete first and last special character
				int l = 0;
				while(l < capitalWords.length() )
				{
					if( (capitalWords.charAt(capitalWords.length() - 1) == '.' )  || (capitalWords.charAt(capitalWords.length() - 1) == '!' )
							|| (capitalWords.charAt(capitalWords.length() - 1) == ',' ) || (capitalWords.charAt(capitalWords.length() - 1) == '?' )
							|| (capitalWords.charAt(capitalWords.length() - 1) == '\"' ) || (capitalWords.charAt(capitalWords.length() - 1) == '(' )
							|| (capitalWords.charAt(capitalWords.length() - 1) == ')' ) || (capitalWords.charAt(capitalWords.length() - 1) == '\'' )
							|| (capitalWords.charAt(capitalWords.length() - 1) == ':' ) || (capitalWords.charAt(capitalWords.length() - 1) == ';' )
							|| (capitalWords.charAt(capitalWords.length() - 1) == '+' ) || (capitalWords.charAt(capitalWords.length() - 1) == '_' )
							|| (capitalWords.charAt(capitalWords.length() - 1) == ']' ) || (capitalWords.charAt(capitalWords.length() - 1) == '[' ) )
					{
						capitalWords = capitalWords.substring(0, original.get(i).length() - 1);
					}
					if( (capitalWords.charAt(0) == '.' )  || (capitalWords.charAt(0) == '!' )
							|| (capitalWords.charAt(0) == ',' ) || (capitalWords.charAt(0) == '?' )
							|| (capitalWords.charAt(0) == '\"' ) || (capitalWords.charAt(0) == '(' )
							|| (capitalWords.charAt(0) == ')' ) || (capitalWords.charAt(0) == '\'' )
							|| (capitalWords.charAt(0) == ':' ) || (capitalWords.charAt(0) == ';' )
							|| (capitalWords.charAt(0) == '-' ) ||  (capitalWords.charAt(0) == '+' )
							|| (capitalWords.charAt(0) == '_' ) || (capitalWords.charAt(0) == '[' )
							|| (capitalWords.charAt(0) == ']' ))
					{
						original.set(i, (original.get(i).substring(1, original.get(i).length() )));
					}
					l++;
				}
				
				String delLastMark = capitalWords;

				
				capitalWordsList.add(j, delLastMark);
				j++;
				i++;
			}
			else
			{
				i++;
			}
		}
		
		//delete words start with capital characters from the original list.
		this.original.removeAll(position);	
		
		return capitalWordsList;
	}
	
	
	//delete first and last signals in terms
	private List<String> delLastSignal()
	{
		List<String> withoutLastSignalList = new ArrayList<String>();
		int i = 0;
		while ((i < original.size()) )
		{
			String delLastMark = original.get(i);
			int l = 0;
			while((original.get(i).length() > 1 ) && (l < original.get(i).length()))
			{
				if( (original.get(i).charAt(original.get(i).length() - 1) == '.' )  || (original.get(i).charAt(original.get(i).length() - 1) == '!' )
						|| (original.get(i).charAt(original.get(i).length() - 1) == ',' ) || (original.get(i).charAt(original.get(i).length() - 1) == '?' )
						|| (original.get(i).charAt(original.get(i).length() - 1) == '\"' ) || (original.get(i).charAt(original.get(i).length() - 1) == '(' )
						|| (original.get(i).charAt(original.get(i).length() - 1) == ')' ) || (original.get(i).charAt(original.get(i).length() - 1) == '\'' )
						|| (original.get(i).charAt(original.get(i).length() - 1) == ':' ) || (original.get(i).charAt(original.get(i).length() - 1) == ';' )
						|| (original.get(i).charAt(original.get(i).length() - 1) == '_' ) || (original.get(i).charAt(original.get(i).length() - 1) == '+' )
						|| (original.get(i).charAt(original.get(i).length() - 1) == '&' ) )
				{
					original.set(i, (original.get(i).substring(0, original.get(i).length() - 1)));
					delLastMark = original.get(i);
				}
				if (((original.get(i).charAt(0) == '.' )  || (original.get(i).charAt(0) == '!' )
						|| (original.get(i).charAt(0) == ',' ) || (original.get(i).charAt(0) == '?' )
						|| (original.get(i).charAt(0) == '\"' ) || (original.get(i).charAt(0) == '(' )
						|| (original.get(i).charAt(0) == ')' ) || (original.get(i).charAt(0) == '\'' )
						|| (original.get(i).charAt(0) == ':' ) || (original.get(i).charAt(0) == ';' )
						|| (original.get(i).charAt(0) == '-' ) || (original.get(i).charAt(0) == '_' )
						|| (original.get(i).charAt(0) == '+' ) || (original.get(i).charAt(0) == '&' )) && (original.get(i).length() > 1) )
				{
					original.set(i, (original.get(i).substring(1, original.get(i).length() )));
					delLastMark = original.get(i);
				}
				l++;
			}
			withoutLastSignalList.add(i,delLastMark);
			i++;
		}
		
		return withoutLastSignalList;
	}
	
	//delete hyphen in term
	private List<String> delHyphen()
	{
		List<String> withoutHyphen = new ArrayList<String>();
		List<String> afetrSignal = delLastSignal();
		int i = 0;
		while( i < afetrSignal.size() )
		{
			String delHyphen = afetrSignal.get(i);
			StringBuilder sb = new StringBuilder(delHyphen);
			for(int j = 0; j < delHyphen.length(); j++)
			{
				if(delHyphen.charAt(j) == '-')
				{
					delHyphen = sb.deleteCharAt(j).toString();
				}
			}
			
			withoutHyphen.add(i, delHyphen);
			i++;
		}
		
		return withoutHyphen;
	}
	
	
	
	//test methods	
	public static void main(String[] args) {
		String a ="This is 'document list' test, A doctor New York male, 'as' you know, A, BCD, a-b, Gwilt’s _Encyclopædia asd.jh.qw.com as-in 12.23.34.5, (asb as-asd) as  asw! 143;123 Engilshman's Ch.";
		String b = "No special word exist,  a test hy-a, a  a asd@gmail.com, https://www.google.com.au, 192.168.1.1, 'single mark' is used, New York done, C.A.T";
		String c = "(includ, a lot) of \"special. character; with: !specialist? asd'  ";
		Tokenization tok = new Tokenization(b);
		List<String> res = tok.termLocation();
		
		for( int i = 0; i < res.size(); i++)
		{
			System.out.println(res.get(i));
		}
	}
	
	
	
	//HD part Get term location in the documents. Just a test method because these terms are not deleted stop words and not use stemmer.
	public ArrayList<String> termLocation()
	{
		ArrayList<String> res = new ArrayList<String>();
		ArrayList<String> afterProcessList = (ArrayList<String>)tokenDocument();
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
}


