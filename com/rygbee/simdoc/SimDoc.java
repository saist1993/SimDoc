package com.rygbee.simdoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import com.rygbee.simdoc.TopicSegmentSimilarityCalculator;
// import com.rygbee.simdoc.WordSegmentSimilarityCalculator; //TODO: need to be put into package


public class SimDoc
{
	private TopicSegmentSimilarityCalculator _topicsegsimilaritycalculator;
	//private VecSegmentSimilarityCalculator _vecsegsimilaritycalculator;
	
	private double _simdocscore;
	private double _UpperBound = 1.0;
	//private double[][] _topicvecmatrix; 
	
	private HashMap<Integer, Integer> _matcheddocuments = new HashMap<Integer, Integer>(); //key: segment id; value: matchedsegment id
	private HashMap<Integer, Double> _maxsegmentscore = new	 HashMap<Integer, Double>(); //key: segment id; value: maxscore
	private ArrayList<Double> segmentscorelist = new ArrayList<Double>();
	//private SentenceSegmentSimilarityCalculator _simdocscorecomputer;
	//private String _segmenttype;
	private String _simtype;
	
		
	public SimDoc(String simtype)
	{	
		//_simdocscore = - 1.0;

		//System.out.println("I am here!!");
		//_topicvecmatrix = topicvecmatrix;
		//_segmenttype = segmenttype;
		_simtype = simtype;

		if(_simtype.equals("W-Rel"))
		{
			_topicsegsimilaritycalculator = new TopicSegmentSimilarityCalculator("Smith-Waterman");
		}
	}

	public void setDocumentPairV1(String[] document1, String[] document2)
	{
		//System.out.println("Total number of sentence entries: " + (document1.length + document2.length));
		//System.out.println("Sentence entries: " + document1.toString() + " and " + document2.toString());

		_topicsegsimilaritycalculator.resetSegmentSimilarityComputer();
		
		for(int documentsegmententry = 0; documentsegmententry < document1.length + document2.length; documentsegmententry++)
		{
			_maxsegmentscore.put(documentsegmententry, -1.0); // initialize all segment maxscores to zero
			_matcheddocuments.put(documentsegmententry, 0);
		}

		ArrayList<String> document1list = new ArrayList<String>(Arrays.asList(document1));
		ArrayList<String> document2list = new ArrayList<String>(Arrays.asList(document2));
		//System.out.println("I am computing ...... !!");
		this.fillSimDocScoreNaive(document1list, document2list);
	}
	
	public void setDocumentPairV2(String document1, String document2) 
	{
		if(_simtype.equals("W-Rel"))
		{
			//_simdocscorecomputer = new SentenceSegmentSimilarityCalculator(document1, document2, new TopicSegmentSimilarityCalculator("Smith-Waterman"));
		}
		else
		{
			//_simdocscorecomputer = new SentenceSegmentSimilarityCalculator(document1, document2, new WordSegmentSimilarityCalculator("Smith-Waterman", _simtype));
			
		}	
	}

	private void fillSimDocScoreNaive(ArrayList<String> sourcedocument, ArrayList<String> targetdocument)
	{		
		//System.out.println("I am computing ...... !!");
		if(sourcedocument.containsAll(targetdocument) || targetdocument.containsAll(sourcedocument))
		{
			_simdocscore = 1; //TODO: should be it symmetrical? Issues with extra white-spaces
		}
		else
		{
			for(int sourcedocumentindex = 0; sourcedocumentindex < sourcedocument.size(); sourcedocumentindex++)
			{
				
				//System.out.println("----------------------------------------------------------------");
				//System.out.println("----------------------------------------------------------------");
				double temporarysegmentscore = -1;
				
				for(int targetdocumentindex = 0; targetdocumentindex < targetdocument.size(); targetdocumentindex++)
				{
					//System.out.println("IT IS A MATCH BETWEEN SENTENCE " + sourcedocumentindex + " AND SENTENCE " + targetdocumentindex);
					double temp = 0;
					
					if(_simtype.equals("W-Rel"))
					{
						
						//System.out.println("I am computing ...... !!");
						//System.out.println("1st source document is: " + sourcedocument.get(sourcedocumentindex));
						//System.out.println("1st target document is: " + targetdocument.get(targetdocumentindex));
						temp = _topicsegsimilaritycalculator.computeSegmentSimilarity(sourcedocument.get(sourcedocumentindex), targetdocument.get(targetdocumentindex));
						// System.out.println("The source temp score between \"" + sourcedocument.get(sourcedocumentindex) + "\" and \"" + targetdocument.get(targetdocumentindex) + "\" is: " + temp);
					}
					else
					{
						//System.out.println("I am computing ...... !!");
						//temp = _topicsegsimilaritycalculator.computeSegmentSimilarity(sourcedocument.get(sourcedocumentindex), targetdocument.get(targetdocumentindex));
						// System.out.println("The source temp score between \"" + sourcedocument.get(sourcedocumentindex) + "\" and \"" + targetdocument.get(targetdocumentindex) + "\" is: " + temp);
						//temp = _wordsegsimilaritycalculator.computeSegmentSimilarity(sourcedocument.get(sourcedocumentindex), targetdocument.get(targetdocumentindex));

					}
					
					//System.out.println("The starting recorded score is "+ temporarysegmentscore);
					//System.out.println(sourcedocument.size()+targetdocumentindex);
					if(temp > temporarysegmentscore)
					{
						//TODO: better process to be done.
						//System.out.println("Hey!! the source is improving!!");
						//System.out.println("Similarity score between " + sourcedocumentindex + " and " + targetdocumentindex + " is: " + temp);
						_maxsegmentscore.remove(sourcedocumentindex); //delete earlier source maxscore
						_maxsegmentscore.put(sourcedocumentindex, temp);
						
						_matcheddocuments.remove(sourcedocumentindex);
						_matcheddocuments.put(sourcedocumentindex, targetdocumentindex + sourcedocument.size());
						temporarysegmentscore = temp;
						//System.out.println("Source ("+ sourcedocumentindex +") improved max score is: " + _maxsegmentscore.get(sourcedocumentindex));
						
						//System.out.println("The current target (" + targetdocumentindex + ") max score is: " + _maxsegmentscore.get(sourcedocument.size()+targetdocumentindex));
						if(temp > _maxsegmentscore.get(sourcedocument.size()+targetdocumentindex))
						{
							//System.out.println("Hey!! the target is improving!!");
							//System.out.println("Similarity score between " + sourcedocumentindex + " and " + targetdocumentindex + " is: " + temp);
							_maxsegmentscore.remove(sourcedocument.size()+targetdocumentindex); //delete earlier target maxscore
							_maxsegmentscore.put(sourcedocument.size()+targetdocumentindex, temp);
							//System.out.println("Target ("+ targetdocumentindex +") is getting an improved score of: " + temp);
							
							_matcheddocuments.remove(sourcedocument.size()+targetdocumentindex);
							_matcheddocuments.put(sourcedocument.size() + targetdocumentindex, sourcedocumentindex);
							//System.out.println("Target ("+ targetdocumentindex +") improved max score is: " + _maxsegmentscore.get(targetdocument.size()+targetdocumentindex));
						}
					}
					else
					{
						//System.out.println("Source ("+ sourcedocumentindex +") still retains previous value of: " + _maxsegmentscore.get(sourcedocumentindex));
						
						//System.out.println("The current target (" + targetdocumentindex + ") max score is: " + _maxsegmentscore.get(sourcedocument.size()+targetdocumentindex));
						if(temp > _maxsegmentscore.get(sourcedocument.size()+targetdocumentindex))
						{
							//System.out.println("Hey!! the target is improving!!");
							//System.out.println("Similarity score between " + sourcedocumentindex + " and " + targetdocumentindex + " is: " + temp);
							_maxsegmentscore.remove(sourcedocument.size()+targetdocumentindex); //delete earlier target maxscore
							_maxsegmentscore.put(sourcedocument.size()+targetdocumentindex, temp);
							//System.out.println("Target ("+ targetdocumentindex +") is getting an improved score of: " + temp);
							
							_matcheddocuments.remove(sourcedocument.size()+targetdocumentindex);
							_matcheddocuments.put(sourcedocument.size() + targetdocumentindex, sourcedocumentindex);
							//System.out.println("Target ("+ targetdocumentindex +") improved max score is: " + _maxsegmentscore.get(targetdocument.size()+targetdocumentindex));
						}
					}
					
				}
				
				/*
				System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
				System.out.println("                                                                             ");
				System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
				*/
			}
		}
	}
	
	
	private void computeSimDocV1Similarity()
	{
		//System.out.println("The total number of max scores are: "+ _maxsegmentscore.size());
		if(_simdocscore != 1)
		{
			int K = _maxsegmentscore.size();
			//	System.out.println("The total number of segments to be compared is: "+ K);
				double sumofdifference = 0.0;
				
				for(int segmentscoreindex = 0; segmentscoreindex < _maxsegmentscore.size(); segmentscoreindex++)
				{
					//System.out.println("Maximum segment score of "+ segmentscoreindex + " is: "+ _maxsegmentscore.get(segmentscoreindex));
					//System.out.println("Its's match is sentence: " + _matcheddocuments.get(segmentscoreindex));
					sumofdifference = sumofdifference + Math.pow(_UpperBound - _maxsegmentscore.get(segmentscoreindex), 2);
				}
				
				double rootmeansquaredeviation = Math.sqrt(sumofdifference/K);
				_simdocscore = 1 - rootmeansquaredeviation;
		}
		
	}
	
	// public double getScoreV2()
	// {
	// 	return _simdocscorecomputer.getSimDocScore();
	// }
	
	public double getScoreV1()
	{
		computeSimDocV1Similarity();
		return _simdocscore;
	}

		/*
	
	public void setDocumentPairV1(ArrayList<String> document1, ArrayList<String> document2)
	{
		//System.out.println("Total number of sentence entries: " + (document1.size() + document2.size()));
		//System.out.println("Sentence entries: " + document1.toString() + " and " + document2.toString());
		
		if(_segmenttype == "USE TOPIC-SEGMENT")
		{
			_topicsegsimilaritycalculator = new TopicSegmentSimilarityCalculator("Smith-Waterman");
		}
		else
		{
			_wordsegsimilaritycalculator = new WordSegmentSimilarityCalculator("Smith-Waterman", _simtype);
		}
		
		for(int documentsegmententry = 0; documentsegmententry < document1.size() + document2.size(); documentsegmententry++)
		{
			_maxsegmentscore.put(documentsegmententry, -1.0); // initialize all segment maxscores to zero
			_matcheddocuments.put(documentsegmententry, 0);
		}
		
		this.fillSimDocScoreNaive(document1, document2);
	}

	*/

	/*

	public void setDocumentPair(ArrayList<String> arr, ArrayList<String> arr2) 
	{
		//System.out.println("I am here!");
		
		String document1 = "", document2 = "";
		
		for(String sentence : arr)
		{
			document1 = document1 + sentence + "END";
		}
		
		for(String sentence : arr2)
		{
			document2 = document2 + sentence + "END";
		}
				
		if(_segmenttype == "USE TOPIC-SEGMENT")
		{
			_simdocscorecomputer = new SentenceSegmentSimilarityCalculator(document1, document2, new TopicSegmentSimilarityCalculator("Smith-Waterman"));
			
		}
		else
		{
			_simdocscorecomputer = new SentenceSegmentSimilarityCalculator(document1, document2, new WordSegmentSimilarityCalculator("Smith-Waterman", _simtype));
			//System.out.println("I am here!");
		}	
	}	
	*/
	
	/*

	private void fillSimDocScore(ArrayList<String> sourcedocument, ArrayList<String> targetdocument, int sourceoffset, int targetoffset)
	{		
		int _sourceoffset = sourceoffset; // initialize the offset for sourcedocument
		int _targetoffset = targetoffset;
		//int limit = Math.max(targetdocument.size(),sourcedocument.size() );
		if(_sourceoffset == 0)
		{
			_targetoffset = _sourceoffset + sourcedocument.size(); // initialize the offset for targetdocument
		}		

		for(int sourcedocumentindex = 0; sourcedocumentindex < sourcedocument.size(); sourcedocumentindex++)
		{
			if(!_matcheddocuments.containsKey(sourcedocumentindex + _sourceoffset)) // sourcedocumentsegment is not yet matched
			{
				for(int targetdocumentindex = 0; targetdocumentindex < targetdocument.size(); targetdocumentindex++)
				{
					if(!_matcheddocuments.containsKey(targetdocumentindex + _targetoffset)) // targetdocumentsegment is not yet matched
					{
						double temporarysegmentscore = _segsimilaritycalculator.computeSegmentSimilarity(sourcedocument.get(sourcedocumentindex), targetdocument.get(targetdocumentindex));
						//System.out.println("Version 1 temp score: "+ temporarysegmentscore);
						if(_maxsegmentscore.get(sourcedocumentindex) < temporarysegmentscore)
						{
							_maxsegmentscore.remove(sourcedocumentindex + _sourceoffset); //delete earlier maxscore
							_maxsegmentscore.put(sourcedocumentindex + _sourceoffset, temporarysegmentscore);
							_maxsegmentscore.put(targetdocumentindex + _targetoffset, temporarysegmentscore);
							
							_matcheddocuments.remove(sourcedocumentindex + _sourceoffset);
							_matcheddocuments.put(sourcedocumentindex + _sourceoffset, targetdocumentindex + _targetoffset);
						}
					}
					else // targetdocument is already matched. Hence, skip it!
					{
						continue;
					}			
				}				
			}
			else // sourcedocument is already matched!
			{
				continue;	
			}			
			
		//	System.out.println("index"+sourcedocumentindex);
			
			// continue with the target document as the source document
			//Swap the offsets before restarting the matching process in the reverse direction			
			
			int tempoffset = _sourceoffset; 
			_sourceoffset = _targetoffset; 
			_targetoffset = tempoffset; 
			
			ArrayList<String> temp = sourcedocument; 
			sourcedocument = targetdocument;
			targetdocument = temp;
			//this.fillSimDocScore(sourcedocument, targetdocument, _sourceoffset, _targetoffset);
		}
		/*
		System.out.println("The total number of max scores in Version A are: "+ _maxsegmentscore.size());
		System.out.println("The total number of max score pairs in Version A are: "+ _matcheddocuments.size());
		System.out.println("The number max scores are: "+ _maxsegmentscore.get(0)+ " "+ _maxsegmentscore.get(1));
		System.out.println("The number of max score pairs are: "+ _matcheddocuments.get(0)+ " "+ _matcheddocuments.get(1));
		
	
	}

	*/

}
