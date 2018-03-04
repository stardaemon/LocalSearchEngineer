package retrieval;

import java.io.IOException;

import indexer.Indexer;
import searcher.Searcher;

public class MySearchEngine {

	public static void mySearchEnginer(String[] args) throws Exception
	{
		int num = args.length;
		if(num < 3)
		{
			System.out.println("Arguements are not enough");
		}
		else
		{
			if(args[0].equals("index"))
			{
				//System.out.println("Arguement is index");
				Indexer ind = new Indexer(args[1], args[2],args[3]);
				ind.indexer();
			}
			else if(args[0].equals("search"))
			{
				//System.out.println("Arguement is search");
				String[] par = new String[num - 2];
				for(int i = 0; i < num -2 ; i++)
				{
					par[i] = args[i + 2];
				}
				int resNum = Integer.parseInt(args[2]);
				Searcher ser = new Searcher(args[1],resNum,par);
				ser.searcher();
			}
			else if(args[0].equals("locindex"))
			{
				Indexer locIndex = new Indexer(args[1], args[2],args[3]);
				locIndex.locationIndexer();
			}
			else if(args[0].equals("searchLocation"))
			{
				String[] par = new String[num - 2];
				for(int i = 0; i < num - 3 ; i++)
				{
					par[i] = args[i + 3];
				}
				int resNum = Integer.parseInt(args[2]);
				Searcher searchLocation = new Searcher(args[1],resNum,par);
				searchLocation.locSearcher();
			}
			else if(args[0].equals("check"))
			{
				String[] par = new String[num - 2];
				for(int i = 0; i < num -3 ; i++)
				{
					par[i] = args[i + 3];
				}
				int resNum = Integer.parseInt(args[2]);
				Searcher searchCheck = new Searcher(args[1],resNum,par);
				searchCheck.checkInput();
			}
		}
	}
	
	public static void main(String[] args) throws Exception 
	{
		MySearchEngine.mySearchEnginer(args);
	}
}
