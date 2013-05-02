import java.util.ArrayList;
import java.util.Arrays;

import exceptions.InputException;
import exceptions.PositiveNumberInputException;

import UIs.LanternaGUI;
import algorithms.*;
import problems.*;
import solutions.*;

public class optimizer {

	public static void main(String[] args) {
	    
		CuckooSearchOpt csAlg = new CuckooSearchOpt();

    	int numEmployees = 3;
    	int numDays = 3;
    	int numShifts = 4;
    	int maxShiftsInRow = 5;
    	int maxShiftsADay = 4;
    	int minShifts = 4;
    	double lambdaPref = 0.5;
    	double lambdaMin = .5;
    	Integer[] shiftReqArr = new Integer[]{1,1,1,0};
    	ArrayList<Integer> shiftReq1 = new ArrayList<Integer>(Arrays.asList(shiftReqArr));
    	ArrayList<Integer> shiftReq2 = new ArrayList<Integer>(Arrays.asList(shiftReqArr));
    	ArrayList<Integer> shiftReq3 = new ArrayList<Integer>(Arrays.asList(shiftReqArr));
    	
    	ArrayList<ArrayList<Integer>> shiftReqs = new ArrayList<ArrayList<Integer>>();
    	shiftReqs.add(shiftReq1);
    	shiftReqs.add(shiftReq2);
    	shiftReqs.add(shiftReq3);
    	
    	Integer[] prefArr = new Integer[12];
    	int j = 1;
    	for (int i = 0; i < 12; i++) {
    	    if (j == 5) j = 1;
    	    prefArr[i] = j;
    	    j++;
    	}
    	ArrayList<Integer> pref1 = new ArrayList<Integer>(Arrays.asList(prefArr));
    	j = 4;
    	for (int i = 0; i < 12; i++) {
    	    if (j == 5) j = 1;
    	    prefArr[i] = j;
    	    j++;
    	}
    	ArrayList<Integer> pref2 = new ArrayList<Integer>(Arrays.asList(prefArr));
    	j = 3;
    	for (int i = 0; i < 12; i++) {
    	    if (j == 5) j = 1;
    	    prefArr[i] = j;
    	    j++;
    	}
    	ArrayList<Integer> pref3 = new ArrayList<Integer>(Arrays.asList(prefArr));
    	
    	ArrayList<ArrayList<Integer>> preferences = new ArrayList<ArrayList<Integer>>();
    	preferences.add(pref1);
    	preferences.add(pref2);
    	preferences.add(pref3);
    	

    	NurseSchedProb nProb;
		try {
			nProb = new NurseSchedProb(numEmployees, numDays, numShifts, maxShiftsInRow, maxShiftsADay, minShifts, lambdaPref, lambdaMin, shiftReqs, preferences);
			csAlg.solve(nProb);
	    	SolutionSet sols = csAlg.getSolutions(nProb);
	    	int n = 15;
	    	int counter = 0;
	    	nProb.printSol(sols.getSol(0));
	    	for (int i = 1; i < n; i++) {
	    		if (!nProb.solsAreEqual(sols.getSol(i), sols.getSol(i-1))){
	    			nProb.printSol(sols.getSol(i-1));
	    			System.out.println("("+counter+")");
	    			counter = 0;
	    			System.out.println(nProb.fitness(sols.getSol(i-1)));
	    		}
	       		else counter++;
	    	}
		} catch (InputException e) {
			e.printStackTrace();
		}
	}

}
