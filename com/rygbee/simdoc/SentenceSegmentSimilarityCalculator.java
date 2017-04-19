package com.rygbee.simdoc;

import com.rygbee.simdoc.SmithWatermanEditPenalizer;

public class SentenceSegmentSimilarityCalculator
{		
	private String _document1;
	private String _document2;
	private String[] _document1sentencelist;
	private String[] _document2sentencelist;
	private double[][] _SmithWaterman_sentence_matrix;
	private double _upperboundcumulativescore;
	private SmithWatermanEditPenalizer _editpenalizer;
	
	//private OrientGraph graph = new OrientGraph ("remote:localhost/test2", "root", "rishi98245" );
	private double _gappenalty;
	private double _globalmatrixmatchscore;
	private double _lastbestglobalmatrixmatchscore;
	private TopicSegmentSimilarityCalculator _topicSegmentSimilarityCalculator;
	//private WordSegmentSimilarityCalculator _wordSegmentSimilarityCalculator;
	private boolean _simflag;

	public SentenceSegmentSimilarityCalculator(String document1, String document2, TopicSegmentSimilarityCalculator topicSegmentSimilarityCalculator) 
	{		
		_editpenalizer = new SmithWatermanEditPenalizer("", "", "W-Sim", );
		_simflag = true;
		//System.out.println("Documents are: " + document1 + " & " + document2);
		if(document1.contains(document2) || document1.contains(document2))
		{
			_globalmatrixmatchscore = 1; //TODO: should be it symmetrical? Issues with extra white-spaces
			
		}
		else
		{
			
			_upperboundcumulativescore = 0;
			_globalmatrixmatchscore = 0.0;
			_lastbestglobalmatrixmatchscore = 0.0;
			
			_topicSegmentSimilarityCalculator = topicSegmentSimilarityCalculator;
			
			_document1 = document1;
			_document2 = document2;
			
			//System.out.println("The sentences to be compared are: \"" + _segment1 + "\" and \"" + _segment2 + "\"");
			_document1sentencelist =  _document1.split("END");
	    	_document2sentencelist = _document2.split("END");
	    	
	        _SmithWaterman_sentence_matrix = new double[_document1sentencelist.length + 1][_document2sentencelist.length + 1]; //NOTE: i-th position of _SmithWatermanmatrix corresponds to (i-1)th position in segment*/
	        
	        /* Initialize the SmithWaterman Matrix for every segment-pair matching:*/
	        
	        _globalmatrixmatchscore = this.computeSegmentSimilarity();
		}
		
	}

	
	/*
	public SentenceSegmentSimilarityCalculator(String document1, String document2, WordSegmentSimilarityCalculator wordSegmentSimilarityCalculator) 
	{		
		_editpenalizer = new SmithWatermanEditPenalizer("", "", "S-Sim");
		_simflag = false;
		if(document1.contains(document2))
		{
			//System.out.println("I am here!");
			_globalmatrixmatchscore = 1; //TODO: should be it symmetrical? Issues with extra white-spaces
		}
		else
		{
			_upperboundcumulativescore = 0;
			_globalmatrixmatchscore = 0.0;
			_lastbestglobalmatrixmatchscore = 0.0;
			
			//_wordSegmentSimilarityCalculator = wordSegmentSimilarityCalculator;
			
			_document1 = document1;
			_document2 = document2;
			
			//System.out.println("The sentences to be compared are: \"" + _segment1 + "\" and \"" + _segment2 + "\"");
			_document1sentencelist =  _document1.split("END");
	    	_document2sentencelist = _document2.split("END");
	        _SmithWaterman_sentence_matrix = new double[_document1sentencelist.length + 1][_document2sentencelist.length + 1]; //NOTE: i-th position of _SmithWatermanmatrix corresponds to (i-1)th position in segment
	        
	         // Initialize the SmithWaterman Matrix for every segment-pair matching:
	        
	        _globalmatrixmatchscore = this.computeSegmentSimilarity();
		}
	}

	*/


	
	public void resetSegmentSimilarityComputer()
	{
		//	System.out.println("I am resetting at sentence-level");
		
		_upperboundcumulativescore = 0;
		_globalmatrixmatchscore = 0.0;
		_lastbestglobalmatrixmatchscore = 0.0;
	}

	//NOTE: sentencesegment represents one document modeled as one sentence-sequence (topic-sequence as sentence)
	
	private double computeSegmentSimilarity()
	{
		
														/* Start Dynamic Programming Algorithm */
								//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX//
		//TODO: Start with [1][1]
		for (int segment1tokenindexinmatrix = 0; segment1tokenindexinmatrix <= _document1sentencelist.length; segment1tokenindexinmatrix++) 
		{
			for (int segment2tokenindexinmatrix = 0; segment2tokenindexinmatrix <= _document2sentencelist.length; segment2tokenindexinmatrix++) 
			{
				if (segment1tokenindexinmatrix != 0 && segment2tokenindexinmatrix != 0)
                {
					
					_gappenalty = _editpenalizer.getOriginalGapValue("SUB_SENTENCE"); 
					//System.out.println("gapvalue is: " + _gappenalty);	
                	//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX//
                													// LITERAL STRING-MATCH //
                	//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                	
                	if (_document1sentencelist[segment1tokenindexinmatrix - 1].equals(_document2sentencelist[segment2tokenindexinmatrix - 1])) 
                    {
                		if(_document1sentencelist[segment1tokenindexinmatrix - 1].equals(" ") || _document2sentencelist[segment2tokenindexinmatrix-1].equals(" "))
                        {
                            continue;
                        }
                    		
                		_editpenalizer.setLValue(0); // A (literal only ??) match should reset the l-value
                		
                    	
                								/* Tackle the boundary corner case of [0][0] initialized as 0 (STRING-MATCH)*/
                    	if(_SmithWaterman_sentence_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] == 0 && _SmithWaterman_sentence_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix] == 0 && _SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix - 1] == 0)
                        {
                    		//_editpenalizer.setWords(_segment1tokenlist[segment1tokenindexinmatrix - 1], _segment2tokenlist[segment2tokenindexinmatrix - 1]);
                    		_SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = 1;
                    		
                    		//System.out.println("I am resetting l-value. There has been a perfect string-match!!");
                     		
                     		//System.out.println("The CUMULATIVE SCORE (BOUNDARY) for STRING-MATCH so far is: " + _globalmatrixmatchscore);	
	                		//System.out.println("The UB CUMULATIVE SCORE (BOUNDARY) for STRING-MATCH so far is: " + _upperboundcumulativescore);
                     		_globalmatrixmatchscore++;
                        	_upperboundcumulativescore++;
                        	
                        	//System.out.println("The CUMULATIVE SCORE (BOUNDARY) for STRING-MATCH so far is: " + _globalmatrixmatchscore);	
	                		//System.out.println("The UB CUMULATIVE SCORE (BOUNDARY) for STRING-MATCH so far is: " + _upperboundcumulativescore);
                        	//_fullmatchcount++;
                        	_lastbestglobalmatrixmatchscore = _globalmatrixmatchscore; 
                        	/*
                        	System.out.println("The CUMULATIVE SCORE for STRING-MATCH (BOUNDARY CASE) so far is: " + _globalmatrixmatchscore);
                        	System.out.println("Final SmithWaterman value of " + segment1tokenindexinmatrix +" and " + segment2tokenindexinmatrix+ " is "+ _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix]);
                        	System.out.println("MISMATCH!! The count is: " + _upperboundcumulativescore);
                        	System.out.println("Difference between cumulative and match count is: " + (_globalmatrixmatchscore - _upperboundcumulativescore));
                        	*/
                        }
                    										/* Tackle the non-boundary case */
                    									/*-------------------------------------*/
                    	else
                    	{
                    		//_gappenalty = 1; //Since there has been a string match at sentence level, hence we regard it as a reward.
	                		_SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = Math.max(0, _SmithWaterman_sentence_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] + 1);
	                		
	                		_globalmatrixmatchscore = _SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] + _globalmatrixmatchscore;
	                		_upperboundcumulativescore = _SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] + _upperboundcumulativescore;
	                	
	                		_lastbestglobalmatrixmatchscore = _globalmatrixmatchscore; 
	                		
	                		//System.out.println("The CUMULATIVE SCORE for STRING-MATCH so far is: " + _globalmatrixmatchscore);	
	                		//System.out.println("The UB CUMULATIVE SCORE for STRING-MATCH so far is: " + _upperboundcumulativescore);	
	                		/*
	                		System.out.println("The CUMULATIVE SCORE for STRING-MATCH so far is: " + _globalmatrixmatchscore);	
    						System.out.println("Final SmithWaterman value of " + segment1tokenindexinmatrix +" and " + segment2tokenindexinmatrix+ " is "+ _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix]);
    						System.out.println("LITERAL SHORT-TEXT MATCH COUNT. The number of matches so far is: " + _upperboundcumulativescore);
    						System.out.println("Difference between cumulative and match count is: " + (_globalmatrixmatchscore - _upperboundcumulativescore));
	                		*/
	                	}
                    } 
                	//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX//
                														// NON LITERAL SEMANTIC-MATCH (or MISMATCH)//
                	//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX//
             
                    else 
                    {
                    	if(_document1sentencelist[segment1tokenindexinmatrix - 1].equals(" ") || _document2sentencelist[segment2tokenindexinmatrix-1].equals(" "))
                        {
                            continue;
                        }
                    	//System.out.println("Sentence level l-increment for current l-value: "+ _editpenalizer.getLValue());
                    	_editpenalizer.incrementLValue();
                    	
                    	double tempsim = 0;
                    	
                    	/* Tackle the boundary case of [0][0] initialized as 0 */
                    	if(_SmithWaterman_sentence_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] == 0 && _SmithWaterman_sentence_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix] == 0 && _SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix - 1] == 0)
                        {
                    		if(_simflag == true)
                    		{
                    			tempsim = _topicSegmentSimilarityCalculator.computeSegmentSimilarity(_document1sentencelist[segment1tokenindexinmatrix - 1], _document2sentencelist[segment2tokenindexinmatrix - 1]);
                    			
                    			_SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = _gappenalty + tempsim;
                    			
                        		//_topicSegmentSimilarityCalculator.resetSegmentSimilarityComputer();
                    		}
                    		else
                    		{
                    			//tempsim = _wordSegmentSimilarityCalculator.computeSegmentSimilarity(_document1sentencelist[segment1tokenindexinmatrix - 1], _document2sentencelist[segment2tokenindexinmatrix - 1]);
                    			_SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = _gappenalty + tempsim;
                    			//System.out.println("Gap penalty is: " + _gappenalty);
		                		//_wordSegmentSimilarityCalculator.resetSegmentSimilarityComputer();
                    		}
		             		
		             		_globalmatrixmatchscore =  _globalmatrixmatchscore + _SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix];
                    		_upperboundcumulativescore++;
                    		//System.out.println("CB boundary case: " + _globalmatrixmatchscore);
                        }
                    	/* Tackle the non-boundary case */
                    	else
                    	{
                    		_SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = Math.max(0, Math.max(_SmithWaterman_sentence_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] + _gappenalty + tempsim, Math.max(_SmithWaterman_sentence_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix] + _editpenalizer.getOriginalGapValue("INS_SENTENCE"), _SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix - 1] + _editpenalizer.getOriginalGapValue("DEL_SENTENCE"))));
                    		_globalmatrixmatchscore = _globalmatrixmatchscore + _SmithWaterman_sentence_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix];	
                    		//System.out.println("CB: " + _globalmatrixmatchscore);
                    		
                    		//_globalmatrixmatchscore = _globalmatrixmatchscore + _gappenalty;	
                    		_upperboundcumulativescore++;
                    		//System.out.println("UB: " + _upperboundcumulativescore);
                      	   //_upperboundcumulativescore = _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] + _upperboundcumulativescore;
                      	   	
                      	   	//_lastbestglobalmatrixmatchscore = _globalmatrixmatchscore; 
                    	}
                    }
            	}
                else
                {
                	continue;
                }
			}
		}
		
		//System.out.println("CB value is: " + _globalmatrixmatchscore);
		
		if(_upperboundcumulativescore >= 1)
		{
			
			if(_globalmatrixmatchscore <= 0)
			{
				_globalmatrixmatchscore = _lastbestglobalmatrixmatchscore;
				//System.out.println("last CUMULATIVE SCORE:" + _lastbestglobalmatrixmatchscore);
			}
			
			if(_upperboundcumulativescore == _globalmatrixmatchscore)
			{
				double result = 1 + 0.1 * _editpenalizer.getOriginalGapValue("DEL", (Math.abs(_document1sentencelist.length - _document2sentencelist.length)));
				//System.out.println("The CB value is: " + _globalmatrixmatchscore);
				if(result >= 0)
				{
					return result;
				}
				else
				{
					return 0;
				}
				
			}
			else
			{
				
				if((_upperboundcumulativescore - _globalmatrixmatchscore) < 0.05)
				{
					//System.out.println("CB value is very high: " + _globalmatrixmatchscore);
					//System.out.println("UB is: " + _upperboundcumulativescore);
					return 1;
				}
				else
				{
					//System.out.println("Similarity: "+_globalmatrixmatchscore/_upperboundcumulativescore);
					//System.out.println("Last best Similarity: "+_lastbestglobalmatrixmatchscore);
					return _globalmatrixmatchscore/_upperboundcumulativescore;
				}
			}
		}
		else
		{
			if(_globalmatrixmatchscore <= 0)
			{
				_globalmatrixmatchscore = _lastbestglobalmatrixmatchscore;
				System.out.println("last CUMULATIVE SCORE:" + _lastbestglobalmatrixmatchscore);
			}
			//System.out.println("The low final similarity value is: " + _globalmatrixmatchscore);
			return _globalmatrixmatchscore;
		}
	}
	
	public double getSimDocScore()
	{
		return _globalmatrixmatchscore;
	}
}
