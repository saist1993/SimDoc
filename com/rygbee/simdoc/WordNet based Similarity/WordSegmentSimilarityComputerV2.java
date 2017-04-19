package com.rygbee.similarity_algorithms;

import com.rygbee.similarity_algorithms.utils.SmithWatermanEditPenalizer;
//import com.tinkerpop.blueprints.impls.orient.OrientGraph;

public class WordSegmentSimilarityComputerV2 extends SimilarityCalculator
{		
	private String _segment1;
	private String _segment2;
	private String[] _segment1tokenlist;
	private String[] _segment2tokenlist;
	private double[][] _SmithWatermanmatrix;
	
	//private double _mismatchcount;
	private SmithWatermanEditPenalizer _editpenalizer;
	//private OrientGraph graph = new OrientGraph ("remote:localhost/test2", "root", "rishi98245" );
	private double _gappenalty;

	private double _lastbestcumulativescore;
	//private double _maxpossiblescore;

	public WordSegmentSimilarityComputerV2(String similaritymeasure, String simtype) 
	{		
		if(similaritymeasure == "Smith-Waterman")
		{
			//_similaritymeasure = new SemanticSmithWatermanSimilarity();
			_editpenalizer = new SmithWatermanEditPenalizer("", "", simtype);
			//_segmenttype = segmenttype;
		}
	}
	
	public void resetSegmentSimilarityComputer()
	{
		//System.out.println("I am resetting matchcount and cumulative score");
		_lastbestcumulativescore = 0.0;
	}

	
	public double computeSegmentSimilarity(String segment1, String segment2)
	{
		if(_segment1.contains(_segment2) || _segment2.contains(_segment1))
        {
        	return 1;
        }
		 
		 _lastbestcumulativescore = 0;
		
		_segment1 = segment1;
		_segment2 = segment2;
		
		//System.out.println("The sentences to be compared are: \"" + _segment1 + "\" and \"" + _segment2 + "\"");
		_segment1tokenlist =  _segment1.toLowerCase().split(" ");
    	_segment2tokenlist = _segment2.toLowerCase().split(" ");
    	
        _SmithWatermanmatrix = new double[_segment1tokenlist.length + 1][_segment2tokenlist.length + 1]; //NOTE: i-th position of _SmithWatermanmatrix corresponds to (i-1)th position in segment*/
        
        					/* Initialize the SmithWaterman Matrix for every segment pair matching */
											/* Start Dynamic Programming Algorithm */
								//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX//
		//TODO: Start with [1][1]
		for (int segment1tokenindexinmatrix = 0; segment1tokenindexinmatrix <= _segment1tokenlist.length; segment1tokenindexinmatrix++) 
		{
			for (int segment2tokenindexinmatrix = 0; segment2tokenindexinmatrix <= _segment2tokenlist.length; segment2tokenindexinmatrix++) 
			{
				if (segment1tokenindexinmatrix != 0 && segment2tokenindexinmatrix != 0)
                {
                	_gappenalty = _editpenalizer.getOriginalGapValue("DEFAULT"); // initalize gap-penalty value
                	
                	if (_segment1tokenlist[segment1tokenindexinmatrix - 1].equals(_segment2tokenlist[segment2tokenindexinmatrix - 1])) 
                    {
                		if(_segment1tokenlist[segment1tokenindexinmatrix - 1].equals(" ") || _segment2tokenlist[segment2tokenindexinmatrix-1].equals(" "))
                        {
                            continue;
                        }
                    	//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX//
                														// LITERAL STRING-MATCH //
                		//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX	
                		_editpenalizer.setLValue(0); // A (literal only ??) match should reset the l-value
                		
                		/* Tackle the boundary corner case of [0][0] initialized as 0 (STRING-MATCH)*/
                    	if(_SmithWatermanmatrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] == 0 && _SmithWatermanmatrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix] == 0 && _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix - 1] == 0)
                        {
                    		//_editpenalizer.setWords(_segment1tokenlist[segment1tokenindexinmatrix - 1], _segment2tokenlist[segment2tokenindexinmatrix - 1]);
                    		_SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = 1;
                    		
                    		//System.out.println("I am resetting l-value. There has been a perfect string-match!!");
                     		
                     		_editpenalizer.setLValue(0); // A semantic match should reset the l-value
                     		
                     		//System.out.println("The CUMULATIVE SCORE (BOUNDARY) for STRING-MATCH so far is: " + _globalmatrixmatchscore);	
	                		//System.out.println("The UB CUMULATIVE SCORE (BOUNDARY) for STRING-MATCH so far is: " + _upperboundcumulativescore);
                     	
	                       	_lastbestcumulativescore = _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix]; 
                        	/*
                        	System.out.println("The CUMULATIVE SCORE for STRING-MATCH (BOUNDARY CASE) so far is: " + _globalmatrixmatchscore);
                        	System.out.println("Final SmithWaterman value of " + segment1tokenindexinmatrix +" and " + segment2tokenindexinmatrix+ " is "+ _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix]);
                        	System.out.println("MISMATCH!! The count is: " + _upperboundcumulativescore);
                        	System.out.println("Difference between cumulative and match count is: " + (_globalmatrixmatchscore - _upperboundcumulativescore));
                        	*/
                        }
                    	/* Tackle the non-boundary case */
                    	else
                    	{
                    		//_gappenalty = 1; //Since there has been a string match at word level, hence we regard it as a reward.
	                		_SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = Math.max(0, _SmithWatermanmatrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] + 1);
	                		
	                		_editpenalizer.setLValue(0); // A semantic match should reset the l-value
	                		_lastbestcumulativescore = _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix]; 
	                		
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
                    	if(_segment1tokenlist[segment1tokenindexinmatrix - 1].equals(" ") || _segment2tokenlist[segment2tokenindexinmatrix-1].equals(" "))
                        {
                            continue;
                        }
                    	_editpenalizer.setWords(_segment1tokenlist[segment1tokenindexinmatrix - 1], _segment2tokenlist[segment2tokenindexinmatrix - 1]);
                    	
                    	/* Tackle the boundary case of [0][0] initialized as 0 */
                    	if(_SmithWatermanmatrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] == 0 && _SmithWatermanmatrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix] == 0 && _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix - 1] == 0)
                        {
                    		//System.out.println("I am here because of STRING-MISMATCH (BOUNDARY CASE)!!");
                    		
                    		//_editpenalizer.setWords(_segment1tokenlist[segment1tokenindexinmatrix - 1], _segment2tokenlist[segment2tokenindexinmatrix - 1]);
                    		_SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = _gappenalty = _editpenalizer.getSemanticMatchValue();
                    		//_SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = _gappenalty;
                    		
                    		// The "approximately complete" semantic-match boundary case
                    		if(_SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] >= 0.9)
                         	{
                         		//System.out.println("I am resetting l-value. There has been a perfect semantic-match!!");
                         		_editpenalizer.setLValue(0); // A semantic match should reset the l-value
                         		_lastbestcumulativescore = _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix]; 
                            	
                         		/*
                         		System.out.println("The CUMULATIVE SCORE for MATCH (BOUNDARY) so far is: " + _globalmatrixmatchscore);
                            	System.out.println("Final SmithWaterman value of " + segment1tokenindexinmatrix +" and " + segment2tokenindexinmatrix+ " is "+ _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix]);
                            	System.out.println("MATCH!! The count is: " + _upperboundcumulativescore);
                            	System.out.println("Difference between cumulative and match count is: " + (_globalmatrixmatchscore - _upperboundcumulativescore));
                         		*/
                         	}
                    		else
                         	{
                         		//System.out.println("I am incrementing l-value in the boundary case. Sorry, no perfect semantic-match!!");
                         		_editpenalizer.incrementLValue();
                         	}
                        }
                    	/* Tackle the non-boundary case */
                    	else
                    	{
                    		//System.out.println("I am here because of STRING-MISMATCH!!");
							_gappenalty = _editpenalizer.getSemanticGapValue("DEFAULT"); // since it is an "apparent mismatch"
							
							/* The case when _gappenalty is a reward */
                        	if(_gappenalty >= - 0.10 && _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] < 1)
                            {
                        		if(_gappenalty < 0)
                        		{
                        			_gappenalty = 0;
                        		}
                        		
                        		_SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = Math.max(0, Math.max(_SmithWatermanmatrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] + _gappenalty, Math.max(_SmithWatermanmatrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix] + _editpenalizer.getSemanticGapValue("INS"), _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix - 1] + _editpenalizer.getSemanticGapValue("DEL"))));
                        		
                        		_editpenalizer.incrementLValue();
                          	   	_lastbestcumulativescore = _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix]; 
                          	   	
                          	   	/*
                          	   	System.out.println("The CUMULATIVE SCORE for OKAY SEMANTIC-MATCH so far is: " + _globalmatrixmatchscore);	
                          	   	System.out.println("Final SmithWaterman value of " + segment1tokenindexinmatrix +" and " + segment2tokenindexinmatrix+ " is "+ _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix]);
                          	   	System.out.println("SEMANTIC MATCH COUNT = " + _upperboundcumulativescore);
                          	   	System.out.println("Difference between cumulative and match count is: " + (_globalmatrixmatchscore - _upperboundcumulativescore));
                      	    	*/
                      	    }
                        	/* The excellent case when _gappenalty is a max reward - Case of complete semantic match*/
                            else if(_gappenalty == 1)
                            {
                            	_SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = Math.max(0, _SmithWatermanmatrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] + _gappenalty);
                           
                            	_editpenalizer.setLValue(0); // A semantic match should reset the l-value
    	                		_lastbestcumulativescore = _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix]; 
                 				/*
                         	   	System.out.println("The CUMULATIVE SCORE PERFECT SEMANTIC-MATCH so far is: " + _globalmatrixmatchscore);	
                         	   	System.out.println("Final SmithWaterman value of " + segment1tokenindexinmatrix +" and " + segment2tokenindexinmatrix+ " is "+ _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix]);
                         	   	System.out.println("STRONG SEMANTIC MATCH COUNT = " + _upperboundcumulativescore);
                         	   	System.out.println("Difference between cumulative and match count is: " + (_globalmatrixmatchscore - _upperboundcumulativescore));
                            	*/
                            }
                        	/* The poor case when _gappenalty is a penalty (the MISMATCH case) */
                            else if(_gappenalty < - 0.10)
                            {
                            	_SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = Math.max(0, Math.max(_SmithWatermanmatrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] + _gappenalty, Math.max(_SmithWatermanmatrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix] + _editpenalizer.getSemanticGapValue("INS"), _SmithWatermanmatrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix - 1] + _editpenalizer.getSemanticGapValue("DEL"))));
                        		
                            	_editpenalizer.incrementLValue();
                            }
                    	}
                    }
            	}
                else
                {
                	//System.out.println("Not yet fully started. Continuing!!");
                	//System.out.println("The CUMULATIVE SCORE for CARRY-OVER STRING-MATCH so far is: " + _globalmatrixmatchscore);	
            		//System.out.println("The UB CUMULATIVE SCORE for CARRY-OVER STRING-MATCH so far is: " + _upperboundcumulativescore);
                	continue;
                }
			}
		}
		
		/*
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		System.out.println("Total number of PARTIAL matches are: " + _matchcount);
		System.out.println("Total number of FULL matches are: " + _fullmatchcount);
		System.out.println("Total number of max matches: " + _maxscorecount);
		
		System.out.println("Final max score:" + _maxscoresofar);
		System.out.println("Final cumulative score:" + _globalmatrixmatchscore);
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		*/
		//_maxpossiblescore = Math.min(_segment1tokenlist.length, _segment2tokenlist.length) - (Math.max(_segment1tokenlist.length, _segment2tokenlist.length) - Math.min(_segment1tokenlist.length, _segment2tokenlist.length))*0.4;
		//System.out.println("Maximum possible score:" + _maxpossiblescore);
		
		/*
		if(_fullmatchcount >= 1)
		{
			System.out.println("The final similarity value is: " + _maxscoresofar/_maxpossiblescore);
			return _maxscoresofar/_maxpossiblescore;
		}
		else
		{
			System.out.println("The final similarity value is: " + _matchcount);
			return _maxscoresofar;
		} */
		
		
		System.out.println("Final cumulative score: " + _SmithWatermanmatrix[_segment1tokenlist.length][_segment2tokenlist.length]);
		System.out.println("Last best cumulative score:" + _lastbestcumulativescore);
		System.out.println("Upper Bound: " + _segment1tokenlist.length);
		
		/*
		if(_upperboundcumulativescore >= 1)
		{
			
			if(_globalmatrixmatchscore <= 0)
			{
				_globalmatrixmatchscore = _lastbestglobalmatrixmatchscore;
				//System.out.println("last CUMULATIVE SCORE:" + _lastbestglobalmatrixmatchscore);
			}
			
			if(_upperboundcumulativescore == _globalmatrixmatchscore)
			{
				double result = 1 + 0.5 * _editpenalizer.getOriginalGapValue("DEL", (Math.abs(_segment1tokenlist.length - _segment2tokenlist.length)));
				//System.out.println("The final similarity value is: " + result);
				return result;
			}
			else
			{
				//System.out.println("The same length final similarity value is: " + _globalmatrixmatchscore/_matchcount);
				if((_upperboundcumulativescore - _globalmatrixmatchscore) < 0.5)
				{
					return 1;
				}
				else
				{
					return _globalmatrixmatchscore/_upperboundcumulativescore;
				}
			}
		}
		else
		{
			if(_globalmatrixmatchscore <= 0)
			{
				_globalmatrixmatchscore = _lastbestglobalmatrixmatchscore;
				//System.out.println("last CUMULATIVE SCORE:" + _lastbestglobalmatrixmatchscore);
			}
			//System.out.println("The low final similarity value is: " + _globalmatrixmatchscore);
			return _globalmatrixmatchscore;
		}
		*/
		if(_SmithWatermanmatrix[_segment1tokenlist.length][_segment2tokenlist.length] == 0)
		{
			//System.out.println("The final similarity value is: " + (_lastbestcumulativescore/_segment1tokenlist.length));
			return Math.ceil(_lastbestcumulativescore)/_segment1tokenlist.length;
		}
		else if(_SmithWatermanmatrix[_segment1tokenlist.length][_segment2tokenlist.length] == _segment1tokenlist.length)
		{
			double result = 1 + 0.5 * _editpenalizer.getOriginalGapValue("DEL", (Math.abs(_segment1tokenlist.length - _segment2tokenlist.length)));
			//System.out.println("The final similarity value is: " + result);
			return result;
		}
		else
		{
			//System.out.println("The same length final similarity value is: " + _globalmatrixmatchscore/_matchcount);
			if((_segment1tokenlist.length - _SmithWatermanmatrix[_segment1tokenlist.length][_segment2tokenlist.length]) < 1)
			{
				return 1;
			}
			else
			{
				return Math.ceil(_SmithWatermanmatrix[_segment1tokenlist.length][_segment2tokenlist.length])/_segment1tokenlist.length;
			}
			//System.out.println("The final SW similarity value is: " + (_SmithWatermanmatrix[_segment1tokenlist.length][_segment2tokenlist.length]/_segment1tokenlist.length));
		}
		
	}
}
