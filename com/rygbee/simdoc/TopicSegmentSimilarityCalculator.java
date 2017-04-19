package com.rygbee.simdoc;

import com.rygbee.simdoc.SmithWatermanEditPenalizer;
//import com.tinkerpop.blueprints.impls.orient.OrientGraph;

public class TopicSegmentSimilarityCalculator
{		
	private String _segment1;
	private String _segment2;
	private String[] _segment1tokenlist;
	private String[] _segment2tokenlist;
	private double[][] _SmithWaterman_topic_matrix;
	private double _upperboundcumulativescore;
	private SmithWatermanEditPenalizer _editpenalizer;
	//private double[][] _topicvecmatrix;
	
	//private OrientGraph graph = new OrientGraph ("remote:localhost/test2", "root", "rishi98245" );
	private double _gappenalty;
	private double _globalmatrixmatchscore;
	private double _lastbestglobalmatrixmatchscore = 0.0;

	public TopicSegmentSimilarityCalculator(String similaritymeasure) 
	{		
		//_topicvecmatrix = topicvecmatrix;

		if(similaritymeasure == "Smith-Waterman")
		{
			_editpenalizer = new SmithWatermanEditPenalizer("456", "457", "W-Rel");
			// System.out.println("Sim type is: " + _editpenalizer.getSimType());

		}
	}
	
	public void resetSegmentSimilarityComputer()
	{
		// System.out.println("I am here!!");
		_upperboundcumulativescore = 0;
		_globalmatrixmatchscore = 0.0;
		_lastbestglobalmatrixmatchscore = 0.0;
	}
	
	//NOTE: topicsegment represents one sentence (in each document) modeled as one topic-sequence
	
	public double computeSegmentSimilarity(String topicsegment1, String topicsegment2)
	{
		_segment1 = topicsegment1;
		_segment2 = topicsegment2;
		
		//System.out.println("Sentence received: " + _segment1 + " & " + _segment2);
		
		//System.out.println("I am here too!!");
		_upperboundcumulativescore = 0;
		_globalmatrixmatchscore = 0.0;
		_lastbestglobalmatrixmatchscore = 0.0;
		
		_segment1 = topicsegment1;
		_segment2 = topicsegment2;
		
		//System.out.println("The sentences to be compared are: \"" + _segment1 + "\" and \"" + _segment2 + "\"");
		_segment1tokenlist =  _segment1.toLowerCase().split(" ");
    	_segment2tokenlist = _segment2.toLowerCase().split(" ");
    	
        _SmithWaterman_topic_matrix = new double[_segment1tokenlist.length + 1][_segment2tokenlist.length + 1]; //NOTE: i-th position of _SmithWatermanmatrix corresponds to (i-1)th position in segment*/
        
        
        /* Initialize the SmithWaterman Matrix for every segment pair matching: Auto-done in Java */
				
												/* Start Dynamic Programming Algorithm */
								//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX//
		//TODO: Start with [1][1]
		
		for (int segment1tokenindexinmatrix = 0; segment1tokenindexinmatrix <= _segment1tokenlist.length; segment1tokenindexinmatrix++) 
		{
			for (int segment2tokenindexinmatrix = 0; segment2tokenindexinmatrix <= _segment2tokenlist.length; segment2tokenindexinmatrix++) 
			{
				if (segment1tokenindexinmatrix != 0 && segment2tokenindexinmatrix != 0)
                {
					
                	
					//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX//
														// TOKEN-ID MATCH //
					//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
					
                	if (_segment1tokenlist[segment1tokenindexinmatrix - 1].equals(_segment2tokenlist[segment2tokenindexinmatrix - 1])) 
                    {
                    	// System.out.println("Topic id matched");
                		if(_segment1tokenlist[segment1tokenindexinmatrix - 1].equals(" ") || _segment2tokenlist[segment2tokenindexinmatrix-1].equals(" "))
                        {
                            continue;
                        }
                    	
                		//System.out.println("I am here!!!");
                		
                		_editpenalizer.setLValue(0); // A (literal only ??) match should reset the l-value
                		
                    	
                								/* Tackle the boundary corner case of [0][0] initialized as 0 (TOKEN-ID MATCH)*/
                    	if(_SmithWaterman_topic_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] == 0 && _SmithWaterman_topic_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix] == 0 && _SmithWaterman_topic_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix - 1] == 0)
                        {
                    		//_editpenalizer.setVecs(_segment1tokenlist[segment1tokenindexinmatrix - 1], _segment2tokenlist[segment2tokenindexinmatrix - 1]);
                    		_SmithWaterman_topic_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = 1;
                    		
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
                    		//_gappenalty = 1; //Since there has been a string match at word level, hence we regard it as a reward.
	                		_SmithWaterman_topic_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = Math.max(0, _SmithWaterman_topic_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] + 1);
	                		
	                			                		
	                		_globalmatrixmatchscore = _SmithWaterman_topic_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] + _globalmatrixmatchscore;

	                		//System.out.println("The CUMULATIVE SCORE for TOPIC-MATCH so far is: " + _globalmatrixmatchscore);

	                		_upperboundcumulativescore = _SmithWaterman_topic_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] + _upperboundcumulativescore;
	                	
	                		_lastbestglobalmatrixmatchscore = _globalmatrixmatchscore; 
	                		
	                		
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
                														// TOPIC-ID MISMATCH//
                	//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX//
             
                    else 
                    {
                    	//_gappenalty = _editpenalizer.getOriginalGapValue("DEFAULT");
                    	// System.out.println("I am trying to set vecs !!");
                    	_editpenalizer.setVecs(_segment1tokenlist[segment1tokenindexinmatrix - 1], _segment2tokenlist[segment2tokenindexinmatrix - 1]);
                		_gappenalty = _editpenalizer.getTopicVecGapValue("DEFAULT");

                    	if(_segment1tokenlist[segment1tokenindexinmatrix - 1].equals(" ") || _segment2tokenlist[segment2tokenindexinmatrix-1].equals(" "))
                        {
                            continue;
                        }
                    	//System.out.println("Topic level l-increment");
                    	_editpenalizer.incrementLValue();
                    	
                    	/* Tackle the boundary case of [0][0] initialized as 0 */
                    	if(_SmithWaterman_topic_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] == 0 && _SmithWaterman_topic_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix] == 0 && _SmithWaterman_topic_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix - 1] == 0)
                        {
                    		_SmithWaterman_topic_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = _gappenalty;
                    		_globalmatrixmatchscore = _globalmatrixmatchscore + _gappenalty;
                     		_upperboundcumulativescore++;
                        }
                    	/* Tackle the non-boundary case */
                    	else
                    	{
                    		_SmithWaterman_topic_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix] = Math.max(0, Math.max(_SmithWaterman_topic_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix - 1] + _gappenalty, Math.max(_SmithWaterman_topic_matrix[segment1tokenindexinmatrix - 1][segment2tokenindexinmatrix] + _editpenalizer.getTopicVecGapValue("INS"), _SmithWaterman_topic_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix - 1] + _editpenalizer.getTopicVecGapValue("DEL"))));
                    		
                    		_globalmatrixmatchscore = _globalmatrixmatchscore + _SmithWaterman_topic_matrix[segment1tokenindexinmatrix][segment2tokenindexinmatrix];	
                    		// System.out.println("Gappenalty: " + _gappenalty);
                    		
                    		//_globalmatrixmatchscore = _globalmatrixmatchscore + _gappenalty;	
                    		_upperboundcumulativescore++;
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
		
		//System.out.println("Topic level current l-value: "+ _editpenalizer.getLValue());
		
		if(_upperboundcumulativescore >= 1)
		{
			if(_lastbestglobalmatrixmatchscore < 0)
			{
				_lastbestglobalmatrixmatchscore = 0;
			}

			if(_globalmatrixmatchscore <= 0)
			{
				_globalmatrixmatchscore = _lastbestglobalmatrixmatchscore;
				
				//System.out.println("last CUMULATIVE SCORE:" + _globalmatrixmatchscore);
			}
			
			if(_upperboundcumulativescore == _globalmatrixmatchscore)
			{
				double result = 1 + 0.1 * _editpenalizer.getTopicVecGapValue("DEL", (Math.abs(_segment1tokenlist.length - _segment2tokenlist.length)));
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
				//System.out.println("The same length final similarity value is: " + _globalmatrixmatchscore/_matchcount);
				if((_upperboundcumulativescore - _globalmatrixmatchscore) < 0.5) // how lenient the system wants to be
				{
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
				//System.out.println("last CUMULATIVE SCORE:" + _globalmatrixmatchscore);
			}
			//System.out.println("The low final similarity value is: " + _globalmatrixmatchscore);
			return _globalmatrixmatchscore;
		}
	}
}
