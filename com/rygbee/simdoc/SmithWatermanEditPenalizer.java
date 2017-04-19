package com.rygbee.simdoc;

//import com.rygbee.simdoc.SemanticSimilarityComputer;
import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SmithWatermanEditPenalizer 
{
	// -1.4,-0.5,-0.5,0.65
	// opic_delete,topic_ins,topic_sub,topic_match
	private double match_value;
	// private double o_value_SUB = -0.025;
	private double o_value_SUB = -0.65;
	// private double o_value_INS = -0.012;
	private double o_value_INS = -0.5;
	// private double o_value_DEL = -0.0025;
	private double o_value_DEL = -1.4;
	private double e_value = -0.90; //-0.75//-0.85
	private static int l_value;
	private double _ogapvalueSUB;
	private Integer _vec1;
	private Integer _vec2;
	//private SemanticSimilarityComputer _semanticsimilaritycomputer;
	private double _semanticsimilarityscore;
	private String _simtype;
	private int _original_lvalue;
	private double _ogapvalueINS;
	private double _ogapvalueDEL;
	private double o_value_DEL_SENTENCE = -0.5;
	private double o_value_SUB_SENTENCE = -1.0;
	private double o_value_INS_SENTENCE = -0.5;

	// 	private double o_value_DEL_SENTENCE = -0.5;
	// // private double o_value_DEL_SENTENCE = -0.5;
	// private double o_value_SUB_SENTENCE = -0.1;
	// private double o_value_INS_SENTENCE = -0.5;

	private static double[][] _topicvecmatrix = new double[100][100];
	
	// public SmithWatermanEditPenalizer(int matchvalue, int ovalue, int lvalue, int evalue, String word1, String word2, String simtype)
	// {
	// 	match_value = matchvalue;
	// 	o_value_SUB = ovalue;
	// 	l_value = lvalue;
	// 	_original_lvalue = lvalue;
	// 	e_value = evalue;
	// 	_ogapvalueSUB = (o_value_SUB + (l_value - 1) * e_value); // initialize the gap value
	// 	_word1 = word1;
	// 	_word2 =  word2;
	// }
	
	// public SmithWatermanEditPenalizer(String word1, String word2, String simtype)
	// {
	// 	match_value = 1;
	// 	l_value = 0;
	// 	_original_lvalue = 0;
	// 	_word1 = word1;
	// 	_word2 =  word2;
	// 	_simtype = simtype;
	// 	//this.computeSemanticGapValue();
	// }

	public SmithWatermanEditPenalizer(String vec1, String vec2, String simtype)
	{
		match_value = 1;
		l_value = 0;
		_original_lvalue = 0;
		//System.out.println("Vec 1 is: " + vec1);
		try{
			_vec1 = Integer.parseInt(vec1);
			_vec2 =  Integer.parseInt(vec2);
		}
		catch(NumberFormatException ex){ // handle your exception
   				System.out.print("NumberFormatException");
		}
		_simtype = simtype;

		// System.out.println("@SmithWatermanEditPenalizer: computing similarity!!");	

		if(_simtype.equals("W-Rel"))
		{
			// System.out.println("@SmithWatermanEditPenalizer: computing similarity!!");
			try
			{
				//File file = new File("/home/gaurav/Rygbee/SimDoc/com/rygbee/simdoc/vector_matrix.txt");
				Scanner input = new Scanner(new File("/home/gaurav/Rygbee/SimDoc/com/rygbee/simdoc/vector_matrix.txt"));
				int i = 0;
				while (input.hasNextLine()) 
				{
		            String line = input.nextLine();
		            String[] row = line.split(" ");
		            for(int j = 0; j < row.length; j++)
		            {
		            	_topicvecmatrix[i][j] = Double.parseDouble(row[j]);
		            	//System.out.println("topic-vec similarity is: " + _topicvecmatrix[i][j]);
		            }
		            i++; 
		        }
		        input.close();


			}
			catch(FileNotFoundException exception)
			{
				System.out.println("File not found!");
			}
			catch(Exception e)
			{
				System.out.println("@simdoc interface setting doc list");

			} 

			//_topicvecmatrix = topicvecmatrix;
		}

		
		//this.computeSemanticGapValue();
	}

	
	
	private void computeSemanticGapValue()
	{
		//System.out.println("I have started computing similarity!!");
		_semanticsimilarityscore = 0;
		//_semanticsimilaritycomputer = new SemanticSimilarityComputer(_word1, _word2, _simtype);		
		//_semanticsimilarityscore = _semanticsimilaritycomputer.getSemanticSimilarity();
		//_semanticsimilarityscore = _semanticsimilaritycomputer.getSemanticSimilarity("DEFAULT");
		//System.out.println("The semantic similarity score computed for " + _word1 + " and " + _word2 + " is: "+ _semanticsimilarityscore);
		
		if(_vec1 > 450 || _vec2 > 450)
		{
			_semanticsimilarityscore = 0;
		}
		else
		{
			_semanticsimilarityscore = _topicvecmatrix[_vec1][_vec2]; //TODO: check if +1 is needed (out of ArrayBound cond check)
			// System.out.println("The semantic vec similarity is: " + _semanticsimilarityscore);
			_ogapvalueSUB = o_value_SUB + (l_value - 1) * e_value;
			_ogapvalueINS = o_value_INS + (l_value - 1) * e_value;
			_ogapvalueDEL = o_value_DEL + (l_value - 1) * e_value;
				
		}
	}

	public double getTopicVecGapValue(String ovaluetype)
	{
		
		if(ovaluetype == "DEFAULT")
		{
			//System.out.println("I have started computing similarity!!");
			this.computeSemanticGapValue();
		}
		//System.out.println("The semantic similarity score computed for " + _word1 + " and " + _word2 + " is: "+ _semanticsimilarityscore);
		double _currentgapvalue = 0;
		
		if(_semanticsimilarityscore == -1)
		{
			_semanticsimilarityscore = 1;
			//System.out.println("ERROR IN SIMILARITY MEASURE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		
		if(_semanticsimilarityscore >= 0.90)
		{
			_currentgapvalue = 1;
			//System.out.println("The gap value for complete semantic match is calculated is: "+ _currentgapvalue);
		}
		else if(_semanticsimilarityscore == 0)
		{
			if(ovaluetype == "DEFAULT")
			{
				//System.out.println("The original SUB gap-value is: " + _ogapvalueSUB);
				_currentgapvalue = _ogapvalueSUB;
			}
			else if(ovaluetype == "INS")
			{
				//System.out.println("The original INS gap-value is: " + _ogapvalueINS);
				_currentgapvalue = _ogapvalueINS;
			}
			else
			{
				//System.out.println("The original DEL gap-value is: " + _ogapvalueDEL);
				_currentgapvalue = _ogapvalueDEL;
			}
			//System.out.println("The SEMANTIC gap-value when similarity is ZERO is: " + _currentgapvalue + " for the case: "+ ovaluetype);
		}
		else
		{
			if(ovaluetype == "DEFAULT")
			{
				//System.out.println("The original SUB gap-value is: " + _ogapvalueSUB);
				_currentgapvalue = _ogapvalueSUB + _semanticsimilarityscore*.60;
			}
			else if(ovaluetype == "INS")
			{
				//System.out.println("The original INS gap-value is: " + _ogapvalueINS);
				_currentgapvalue = _ogapvalueINS + _semanticsimilarityscore;
			}
			else
			{
				//System.out.println("The original DEL gap-value is: " + _ogapvalueDEL);
				_currentgapvalue = _ogapvalueDEL + _semanticsimilarityscore;
			}
			//System.out.println("The SEMANTIC gap-value is: " + _currentgapvalue + " for the case: "+ ovaluetype);
		}
		_semanticsimilarityscore = 0;
		return _currentgapvalue;
	}

	public double getTopicVecGapValue(String ovaluetype, int length)
	{
		double gapvalue = 0;
		
		if(length == 0)
		{
			return 0;
		}
		
		if(ovaluetype == "DEL")
		{
			gapvalue = o_value_DEL + (length - 1) * e_value;
		}
		else
		{
			gapvalue = o_value_SUB + (length - 1) * e_value;
		}
		
		return gapvalue;
	}
	
	// public double getSemanticGapValue(String ovaluetype)
	// {
		
	// 	if(ovaluetype == "DEFAULT")
	// 	{
	// 		this.computeSemanticGapValue();
	// 	}
	// 	//System.out.println("The semantic similarity score computed for " + _word1 + " and " + _word2 + " is: "+ _semanticsimilarityscore);
	// 	double _currentgapvalue = 0;
		
	// 	if(_semanticsimilarityscore == -1)
	// 	{
	// 		_semanticsimilarityscore = 1;
	// 		//System.out.println("ERROR IN SIMILARITY MEASURE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	// 	}
		
	// 	if(_semanticsimilarityscore >= 0.90)
	// 	{
	// 		_currentgapvalue = 1;
	// 		//System.out.println("The gap value for complete semantic match is calculated is: "+ _currentgapvalue);
	// 	}
	// 	else if(_semanticsimilarityscore == 0)
	// 	{
	// 		if(ovaluetype == "DEFAULT")
	// 		{
	// 			//System.out.println("The original SUB gap-value is: " + _ogapvalueSUB);
	// 			_currentgapvalue = _ogapvalueSUB;
	// 		}
	// 		else if(ovaluetype == "INS")
	// 		{
	// 			//System.out.println("The original INS gap-value is: " + _ogapvalueINS);
	// 			_currentgapvalue = _ogapvalueINS;
	// 		}
	// 		else
	// 		{
	// 			//System.out.println("The original DEL gap-value is: " + _ogapvalueDEL);
	// 			_currentgapvalue = _ogapvalueDEL;
	// 		}
	// 		//System.out.println("The SEMANTIC gap-value when similarity is ZERO is: " + _currentgapvalue + " for the case: "+ ovaluetype);
	// 	}
	// 	else
	// 	{
	// 		if(ovaluetype == "DEFAULT")
	// 		{
	// 			//System.out.println("The original SUB gap-value is: " + _ogapvalueSUB);
	// 			_currentgapvalue = _ogapvalueSUB + _semanticsimilarityscore;
	// 		}
	// 		else if(ovaluetype == "INS")
	// 		{
	// 			//System.out.println("The original INS gap-value is: " + _ogapvalueINS);
	// 			_currentgapvalue = _ogapvalueINS + _semanticsimilarityscore;
	// 		}
	// 		else
	// 		{
	// 			//System.out.println("The original DEL gap-value is: " + _ogapvalueDEL);
	// 			_currentgapvalue = _ogapvalueDEL + _semanticsimilarityscore;
	// 		}
	// 		//System.out.println("The SEMANTIC gap-value is: " + _currentgapvalue + " for the case: "+ ovaluetype);
	// 	}
	// 	_semanticsimilarityscore = 0;
	// 	return _currentgapvalue;
	// }
	
	public double getSemanticSimilarityValue()
	{
		return _semanticsimilarityscore;
	}

	
	
	// public double getSemanticMatchValue()
	// {
	// 	//System.out.println("I have started computing similarity!!");
	// 	_semanticsimilarityscore = 0;
	// 	_semanticsimilaritycomputer = new SemanticSimilarityComputer(_word1, _word2, _simtype);
	// 	//System.out.println("I am here!!");
	// 	//_semanticsimilarityscore = _semanticsimilaritycomputer.getSemanticSimilarity();
	// 	_semanticsimilarityscore = _semanticsimilaritycomputer.getSemanticSimilarity("DEFAULT");
	// 	if(_semanticsimilarityscore == 0)
	// 	{
	// 		match_value = 0; //TODO: should it be 0 or a little more?
	// 	}
	// 	else
	// 	{
	// 		match_value = match_value * _semanticsimilarityscore; //Used for matching of topic (special case: S-Sim/S-Rel)
	// 	}
	// 	//System.out.println("similarity is: " + _semanticsimilarityscore);
	// 	_semanticsimilarityscore = 0;
	// 	return match_value;
	// }


	
	// public double getSemanticRelatedValue()
	// {
	// 	_semanticsimilaritycomputer = new SemanticSimilarityComputer(_word1, _word2, _simtype);
	// 	//_semanticsimilarityscore = _semanticsimilaritycomputer.getSemanticSimilarity();
	// 	_semanticsimilarityscore = _semanticsimilaritycomputer.getSemanticSimilarity("DEFAULT");
	// 	//System.out.println("@getSemnticMatchValue" + _semanticsimilarityscore);
	// 	if(_semanticsimilarityscore == -1)
	// 	{
	// 		_semanticsimilarityscore = 1;
	// 	}
	// 	match_value = match_value * _semanticsimilarityscore; //WHY??
	// 	return match_value;
	// }

	
	
	// public double getOriginalGapValue(String ovaluetype)
	// {
	// 	double gapvalue = 0;
	// 	//System.out.println("L Value is: " + l_value);
	// 	switch (ovaluetype)
	// 	{
	// 		case "DEL": gapvalue = o_value_DEL + (l_value - 1) * e_value;
	// 		break;
			
	// 		case "INS": gapvalue = o_value_INS + (l_value - 1) * e_value;
	// 		break;
			
	// 		case "DEL_SENTENCE": gapvalue = o_value_DEL_SENTENCE + (l_value - 1) * e_value;
	// 		//System.out.println("sentence deletion penalty: " + gapvalue + " for l-value of: " + l_value);
	// 		break;
			
	// 		case "INS_SENTENCE": gapvalue = o_value_INS_SENTENCE + (l_value - 1) * e_value;
			
	// 		break;
			
	// 		case "SUB_SENTENCE": gapvalue = o_value_SUB_SENTENCE  + (l_value - 1) * e_value;
	// 		break;
			
	// 		default: gapvalue = o_value_SUB + (l_value - 1) * e_value;
	// 		break;
			
	// 	}
			
	// 	return gapvalue;
	// }
	
	// public double getOriginalGapValue(String ovaluetype, int length)
	// {
	// 	double gapvalue = 0;
		
	// 	if(length == 0)
	// 	{
	// 		return 0;
	// 	}
		
	// 	if(ovaluetype == "DEL")
	// 	{
	// 		gapvalue = o_value_DEL + (length - 1) * e_value;
	// 	}
	// 	else
	// 	{
	// 		gapvalue = o_value_SUB + (length - 1) * e_value;
	// 	}
		
	// 	return gapvalue;
	// }
	
	public void setLValue(int lvalue)
	{
		l_value = lvalue;
	}
	
	public int getLValue()
	{
		return l_value;
	}
	
	public void resetLValue()
	{
		l_value = _original_lvalue;
	}

	public double getMatchValue() 
	{
		// TODO Auto-generated method stub
		return match_value;
	}
	
	public void setOValue(double d)
	{
		o_value_SUB = d;
	}
	
	public double getOValue() 
	{
		// TODO Auto-generated method stub
		return o_value_SUB;
	}
	
	public double getEValue() 
	{
		// TODO Auto-generated method stub
		return e_value;
	}
	
	public void incrementLValue()
	{
		l_value++;
		//System.out.println("L-value has increased to: " + l_value);
	}
	public void setVecs(String vec1, String vec2)
	{
		// System.out.println("I am starting!!! The words are: " + vec1 + " and " + vec2);
		// _vec1 = Integer.parseInt(vec1);
		// _vec2 =  Integer.parseInt(vec2);
		try{
			_vec1 = Integer.parseInt(vec1);
			_vec2 =  Integer.parseInt(vec2);
		}
		catch(NumberFormatException ex){ // handle your exception
   				System.out.print("NumberFormatException");
		}
	}
	public String getSimType()
	{
		return _simtype;
	}
}
