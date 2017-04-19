package com.rygbee.simdoc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.util.Function;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.Tree;
//import edu.stanford.nlp.parser.lexparser.LexicalizedParserQuery;
//import edu.stanford.nlp.parser.lexparser.Options.wordFunction;

public class SemanticSimilarityComputer 
{
	private static Dictionary i_db = new Dictionary(new File("dict"));
	private Morphology morphology;
	private static LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");;
	
	private String _word1;
	private String _word2;
	private String[] _word1tokens;
	private String[] _word2tokens;
	private List<String> wordsToBeChecked1 = new ArrayList<>();
	private List<String> wordsToBeChecked2 = new ArrayList<>();
	
	private String _simtype;
	
	private ILexicalDatabase n_db = new NictWordNet();
	private RelatednessCalculator[] _WordNetsimilaritymeasures = {new HirstStOnge(n_db), new LeacockChodorow(n_db), new Lesk(n_db), new WuPalmer(n_db),
			new Resnik(n_db), new JiangConrath(n_db), new Lin(n_db), new Path(n_db) };
	
	private double _semanticscore;
	
	public SemanticSimilarityComputer(String word1, String word2, String simtype) 
	{
		WS4JConfiguration.getInstance().setMFS(true);
		_simtype = simtype;
		_semanticscore = -1;
		_word1 = word1;
		_word2 = word2;
		
		//System.out.println("The words to be computed are: " + _word1 + " and " + _word2);
		
		//i_db = new Dictionary(new File("dict"));
		try 
		{
			i_db.open();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		catch(NullPointerException e){
			System.out.println("I db could not open!!");
		}
	//	lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		morphology = new Morphology();
	}
	
	public double getSemanticSimilarity(String type)
	{
		//System.out.println("The word 2 to be split is: "+ _word2);
		_word1tokens = _word1.split(" ");
		_word2tokens = _word2.split(" ");
		//System.out.println("Words to be matched are: "+ _word1 + " and "+ _word2);
		if(_simtype == "S-Rel")
		{
			return this.computeSematicRelatedness(_word1tokens, _word2tokens);
		}
		
		else
		{
			return this.computeSematicSimilarity(_word1tokens, _word2tokens);
		}
	}
	
	public double getSemanticSimilarity()
	{
		
		String tag1 = getTag(_word1);
		//System.out.println("word 1 at get semantic similarity is "+ _word1 + " and the associated tag is " + tag1);
		
		if (tag1.charAt(0) == 'N') 
		{
			_word1 = morphology.lemma(_word1, "n") + "#n";
		} 
		
		else if(tag1.charAt(0) == 'V')
		{
			_word1 = morphology.lemma(_word1, "n") + "#n";

		}
		else
		{
			_word1 = morphology.lemma(_word1, "n") + "#n";
			tag1 = "NN";
		}
		String tag2 = getTag(_word2);
		//System.out.println("word 2 at get semantic similarity is "+ _word2 + " and the associated tag is " + tag2);
		if (tag2.charAt(0) == 'N') 
		{
			_word2 = morphology.lemma(_word2, "n") + "#n";
		} 
		
		else if(tag2.charAt(0) == 'V')
		{
			_word2 = morphology.lemma(_word2, "n") + "#n";
		}
		else
		{
			_word2 = morphology.lemma(_word2, "n") + "#n";
			tag2 = "NN";
			//System.out.println("The second word morph is: " + _word2 + " and its tag is: "+ tag2.charAt(0));
		}
			
		
		if(tag1.charAt(0) != tag2.charAt(0))
		{
			//System.out.println("AM I HERE?????");
			//System.out.println("Word 1 is: "+ _word1);
			//System.out.println("Word 2 is: "+ _word2);
			POS p;
			if (tag1.charAt(0) == 'V')
			{
				//System.out.println("AM I (1st WORD) HERE AS WELL!!!!");
				p = POS.VERB;
				wordsToBeChecked1 = this.fillTheList(_word1, p);
				if(wordsToBeChecked1.equals(_word1))
				{
					tag1 = "NN";
				}
				//System.out.println("Words to be checked are: "+ wordsToBeChecked1.size());
			}				
			else if (tag1.charAt(0) == 'J')
			{
				//System.out.println("AM I HERE AS WELL!!!!");
				p = POS.ADJECTIVE;
				wordsToBeChecked1 = this.fillTheList(_word1, p);
			}
			else if (tag1.charAt(0) == 'R')
			{
				//System.out.println("AM I HERE AS WELL!!!!");
				p = POS.ADVERB;
				wordsToBeChecked1 = this.fillTheList(_word1, p);
			}
			else
			{
				//System.out.println("AM I HERE AS WELL!!!!");
				p = null;
				wordsToBeChecked1.add(_word1);
			}
			//=====================================//
			
			if (tag2.charAt(0) == 'V')
			{
				//System.out.println("AM I (2nd word) HERE AS WELL!!!! Ok, tag is: " + tag2);
				p = POS.VERB;
				wordsToBeChecked2 = this.fillTheList(_word2, p);
				if(wordsToBeChecked2.equals(_word2))
				{
					tag2 = "NN";
				}
				//System.out.println("Words to be checked are: "+ wordsToBeChecked2.size());
			}				
			else if (tag2.charAt(0) == 'J')
			{
				//System.out.println("AM I (2nd word) HERE AS WELL!!!!");
				p = POS.ADJECTIVE;
				wordsToBeChecked2 = this.fillTheList(_word2, p);
			}
			else if (tag2.charAt(0) == 'R')
			{
				//System.out.println("AM I (2nd word) HERE AS WELL!!!!");
				p = POS.ADVERB;
				wordsToBeChecked2 = this.fillTheList(_word2, p);
			}
			else
			{
				//System.out.println("AM I (2nd word) HERE AS WELL!!!!");
				p = null;
				wordsToBeChecked2.add(_word2);
				//System.out.println("Word 2 in POS mismatch is: "+ wordsToBeChecked2.size());
			}
			
			//System.out.println("AM I HERE AS WELL!!!! OK, the 2nd words to checked is: " + wordsToBeChecked2.size());
			
			if (wordsToBeChecked1.size() > 0 && wordsToBeChecked2.size() > 0)
			{
				//System.out.println("AM I HERE FOR THE DEAL??");
				double finalAvg = -1;
				for (String c1 : wordsToBeChecked1) 
				{
					for (String c2 : wordsToBeChecked2) 
					{
						String[] temp1 = c1.split(" ");
						String[] temp2 = c2.split(" ");
						//System.out.println("Temp words to be compared are: "+ temp1[0]+" and "+ temp2[0]);
						double avg = this.computeSematicSimilarity(temp1, temp2);
						if (avg > finalAvg) 
						{
							finalAvg = avg;
						}
					}
				}
				
				_semanticscore = finalAvg;
				//System.out.println("the final semantic similarity by wordnet between " + _word1 + " and" + " word2 is " + _semanticscore);
				
				return _semanticscore;
			} 
			
			else 
			{
				//System.out.println("WHAT THE HELL!!!!");
				return -1;
			}
		}
		
		else
		{
			//System.out.println("The word 2 to be split is: "+ _word2);
			_word1tokens = _word1.split(" ");
			_word2tokens = _word2.split(" ");
			//System.out.println("Words to be matched are: "+ _word1 + " and "+ _word2);
			if(_simtype == "S-Rel")
			{
				return this.computeSematicRelatedness(_word1tokens, _word2tokens);
			}
			
			else
			{
				return this.computeSematicSimilarity(_word1tokens, _word2tokens);
			}
		}
	}
	
	public double getSemanticSimilarity(String _word1,String _word2)
	{
		String tag1 = getTag(_word1);
		String tag2 = getTag(_word2);
		
		if (tag1.charAt(0) == 'N') 
		{
			_word1 = morphology.lemma(_word1, "n") + "#n";
			//System.out.println("Word 1 is: "+ _word1);
		} 
		
		else if(tag1.charAt(0) == 'V')
		{
			_word1 = morphology.lemma(_word1, "v") + "#v";
		}
		
		if (tag2.charAt(0) == 'N') 
		{
			_word2 = morphology.lemma(_word2, "n") + "#n";
			//System.out.println("Word 2 is: "+ _word2);
		} 
		
		else if(tag2.charAt(0) == 'V')
		{
			_word2 = morphology.lemma(_word2, "v") + "#v";
		}
			
		
		if(tag1.charAt(0) != tag2.charAt(0))
		{
			
			POS p;
			if (tag1.charAt(0) == 'V')
			{
				p = POS.VERB;
				wordsToBeChecked1 = this.fillTheList(_word1, p);
			}				
			else if (tag1.charAt(0) == 'J')
			{
				p = POS.ADJECTIVE;
				wordsToBeChecked1 = this.fillTheList(_word1, p);
			}
			else if (tag1.charAt(0) == 'R')
			{
				p = POS.ADVERB;
				wordsToBeChecked1 = this.fillTheList(_word1, p);
			}
			else
			{
				p = null;
				wordsToBeChecked1.add(_word1);
			}
			
			if (wordsToBeChecked1.size() > 0 && wordsToBeChecked2.size() > 0)
			{
				double finalAvg = -1;
				for (String c1 : wordsToBeChecked1) 
				{
					for (String c2 : wordsToBeChecked2) 
					{
						String[] temp1 = c1.split(" ");
						String[] temp2 = c2.split(" ");
						
						double avg = this.computeSematicSimilarity(temp1, temp2);
						//System.out.println("The arguments given are: "+ temp1[0] + " and "+ temp2[0]);
						if (avg > finalAvg) 
						{
							finalAvg = avg;
						}
					}
				}
				
				_semanticscore = finalAvg;
				
				return _semanticscore;
			} 
			
			else 
			{
				return -1;
			}
		}
		
		else
		{
			_word1tokens = _word1.split(" ");
			_word2tokens = _word1.split(" ");
			
			if(_simtype == "S-Rel")
			{
				return this.computeSematicRelatedness(_word1tokens, _word2tokens);
			}
			
			else
			{
				//System.out.println("I am here!!");
				return this.computeSematicSimilarity(_word1tokens, _word2tokens);
			}
		}
	}	
	
	
	private List<String> fillTheList(String word, POS p) 
	{
		//System.out.println("The word POS to be derived is: " + p + " corresponding POS Tagger value is: " + POS.VERB);

		if (p == POS.ADJECTIVE || p == POS.VERB) 
		{
			List<String> ans = new ArrayList<>();
			IIndexWord rootIndexWord = i_db.getIndexWord(word, p);
			if (rootIndexWord != null) 
			{
				List<IWordID> rootIndexWordIds = rootIndexWord.getWordIDs();
				for (IWordID id : rootIndexWordIds) 
				{
					IWord w = i_db.getWord(id);
					List<IWordID> relatedWordIds = w.getRelatedWords(Pointer.DERIVATIONALLY_RELATED);
					for (IWordID relatedWordID : relatedWordIds) 
					{
						IWord w1 = i_db.getWord(relatedWordID);
						String temp = w1.getLemma();
						if (!ans.contains(temp))
							ans.add(temp + "#n");
					}
				}
			}
			else
			{
				
				ans.add(word);
				//System.out.println("TREAT WORD AS NOUN FORM");
			}
			return ans;
		} 
		else 
		{
           List<String> ans = new ArrayList<>();
           List<String> tempAns = fillTheList(word); 
           for(String adjString : tempAns)
           {
               List<String> listWithNounTags=fillTheList(adjString,POS.ADJECTIVE);
               for(String nounString:listWithNounTags)
               {
                   if(!ans.contains(nounString))
                   {
                       ans.add(nounString);
                   }
               }
           }                   
           return ans;
		}
	}
	
	private List<String> fillTheList(String word)
	{
		List<String> ans = new ArrayList<>();
		IIndexWord rootIndexWord = i_db.getIndexWord(word, POS.ADVERB);
		if (rootIndexWord != null) 
		{
			List<IWordID> rootIndexWordIds = rootIndexWord.getWordIDs();
			for (IWordID id : rootIndexWordIds) 
			{
				IWord w = i_db.getWord(id);
				List<IWordID> relatedWordIds = w.getRelatedWords(Pointer.DERIVED_FROM_ADJ);
				for (IWordID relatedWordID : relatedWordIds) 
				{
					IWord w1 = i_db.getWord(relatedWordID);
					String temp = w1.getLemma();
					if (!ans.contains(temp))
						ans.add(temp);
				}
			}
		}
		return ans;
	}
	
	public double computeSematicSimilarity(String[] word1, String[] word2)
	{
		//System.out.println("word1 is "+ word1[0] + " word 2 is "+ word2[0]);
		//double wuM[][] = _WordNetsimilaritymeasures[3].getNormalizedSimilarityMatrix(word1, word2);
		//System.out.println("WORDNET: Similarity is: " + _semanticscore);
		//double rM[][] = _WordNetsimilaritymeasures[4].getNormalizedSimilarityMatrix(word1, word2);
		//double jM[][] = _WordNetsimilaritymeasures[5].getNormalizedSimilarityMatrix(word1, word2);
		//_semanticscore = wuM[0][0];
		//_semanticscore = (wuM[0][0] + rM[0][0] + jM[0][0]) / 3; // why [0][0] ??
		//_semanticscore = jM[0][0]; // why [0][0] ??
		//_semanticscore = (Math.max(_WordNetsimilaritymeasures[1].getNormalizedSimilarityMatrix(word1, word2)[0][0], _WordNetsimilaritymeasures[3].getNormalizedSimilarityMatrix(word1, word2)[0][0])); // why [0][0] ??
		//_semanticscore = (_WordNetsimilaritymeasures[1].getNormalizedSimilarityMatrix(word1, word2)[0][0]+_WordNetsimilaritymeasures[3].getNormalizedSimilarityMatrix(word1, word2)[0][0])/2; // why [0][0] ??
		//_semanticscore = 0.0*_WordNetsimilaritymeasures[6].getNormalizedSimilarityMatrix(word1, word2)[0][0] + 1.0 * _WordNetsimilaritymeasures[3].getNormalizedSimilarityMatrix(word1, word2)[0][0]; // why [0][0] ??
		_semanticscore = 0.6 * _WordNetsimilaritymeasures[3].getNormalizedSimilarityMatrix(word1, word2)[0][0] + 0.4 * _WordNetsimilaritymeasures[5].getNormalizedSimilarityMatrix(word1, word2)[0][0];
		//_semanticscore = _WordNetsimilaritymeasures[3].getNormalizedSimilarityMatrix(word1, word2)[0][0];
		//System.out.println("WORDNET: Similarity is: " + _semanticscore);
		return _semanticscore; //TO-DO: need to save this semantic score
	}
	
	public double computeSematicRelatedness(String[] word1, String[] word2)
	{
		//double hirststongeM[][] = _WordNetsimilaritymeasures[5].getNormalizedSimilarityMatrix(word1, word2);
		_semanticscore = _WordNetsimilaritymeasures[0].getNormalizedSimilarityMatrix(word1, word2)[0][0]; 
		//System.out.println("WORDNET: Relatedness is: " + _semanticscore);
		return _semanticscore; //TO-DO: need to save this semantic score
	}
	
	/* Is this the best possible way to get POS tag? */
	private String getTag(String s) 
	{
		String[] sArr = s.split(" ");
		List<CoreLabel> rawWords = Sentence.toCoreLabelList(sArr);
		//System.out.println("@rawwords");
		//System.out.println(rawWords);
		Tree parse = lp.apply(rawWords);
		List<Tree> leaves = parse.getLeaves();
		String tag = leaves.get(0).parent(parse).nodeString();
		return tag;
	}
}
