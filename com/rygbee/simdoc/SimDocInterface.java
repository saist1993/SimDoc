package com.rygbee.simdoc;

// import java.io.BufferedReader;
// import java.io.File;
// import java.util.Scanner;
// import java.io.FileNotFoundException;
// import java.io.FileReader;
// import java.io.IOException;
// import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rygbee.simdoc.*;

public class SimDocInterface 
{
	private static SimDoc simdoc_STS;
	private static String _simtype;	
    //private static double[][] _topicvecmatrix = new double[450][450];

	public static double main(String document1, String document2, int version, String simtype) 
	{
		
		//===========================================================================================================================
		
		
																		// SemEval STS Testing
		//===============================================================================================================================//
		double score_STS = 0.0;

		_simtype = simtype;
		//System.out.println("The similarity type is: " + _simtype);

		if(_simtype.equals("W-Rel"))
		{
			// System.out.println("I am computing ...... !!");
			simdoc_STS = new SimDoc(_simtype);
		}
		// else if(simtype == "S-Rel")
		// {
		// 	SimDoc simdoc_STS = new SimDoc(simtype, "USE VEC-SEGMENT");
		// }

		else
		{
			simdoc_STS = new SimDoc(_simtype);
		}
		
		
		if(version == 1)
		{
								//	SimDocV1 (RMSD based) Testing ; NOTE: documents need to be formatted as arraylist
			//=========================================================================================================//

			//System.out.println("I am here!!");
			String[] _document1sentencelist =  document1.toLowerCase().split("a");
			String[] _document2sentencelist =  document2.toLowerCase().split("a");
			simdoc_STS.setDocumentPairV1(_document1sentencelist, _document2sentencelist);
			score_STS = simdoc_STS.getScoreV1();	
		}

		else
		{
	 	// 						//	SimDocV2 (Smith-Waterman based) Testing
			// //==============================================================================//
			// simdoc_STS.setDocumentPairV2(document1, document2);
			// score_STS = simdoc_STS.getScoreV2();
		}
		
		//System.out.print("The final score is: " + score_STS);
		return (float)score_STS;			
		//return 0;			
	}
}
