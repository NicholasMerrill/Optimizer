package problems;
/* Guided by the wise words of http://www.math.cmu.edu/~af1p/Teaching/OR2/Projects/P23/ORProject_Final_Copy.pdf */
import java.util.ArrayList;

import exceptions.InputException;
import exceptions.PositiveNumberInputException;

import solutions.Solution;

public class NurseSchedProb extends OptimizationProblem {
	private int numEmployees;
	private int numDays; 
	private int numShifts;
	private int maxShiftsInRow;
//	private int maxShiftsADay;
//	private int minShifts;
	private ArrayList<ArrayList<Integer>> shiftReqs;
	private ArrayList<ArrayList<Integer>> preferences;

	/**
	 * Constructs a Nurse Scheduling Problem
	 */
	public NurseSchedProb(int numEmployees, int numDays, int numShifts, int maxShiftsInRow, /*int maxShiftsADay, int minShifts, */
			ArrayList<ArrayList<Integer>> shiftReqs, ArrayList<ArrayList<Integer>> preferences) 
					throws InputException, PositiveNumberInputException {
		this.numEmployees = numEmployees;
		this.numDays = numDays;
		this.numShifts = numShifts;
		this.maxShiftsInRow = maxShiftsInRow;
//		this.maxShiftsADay = maxShiftsADay;
//		this.minShifts = minShifts;
		this.shiftReqs = shiftReqs;
		this.preferences = preferences;
		for (int i = 0; i < numEmployees * numDays * numShifts; i++)
			this.constraints.add(new Constraint(i,0,1));
		
		// checking user input
		if (numEmployees <= 0) throw new PositiveNumberInputException("number of employees");
		if (numDays <= 0) throw new PositiveNumberInputException("number of days in scheduling cycle");
		if (numShifts <= 0) throw new PositiveNumberInputException("number of shifts per day");
		if (maxShiftsInRow <= 0) throw new PositiveNumberInputException("maximum number of shifts in a row");
//		if (maxShiftsADay <= 0) throw new PositiveNumberInputException("maximum number of shifts per 24 hours");
//		if (minShifts <= 0) throw new PositiveNumberInputException("minimum number of shifts for an employee per scheduling period");
		if (shiftReqs.size() != numDays || shiftReqs.get(0).size() != numShifts) {
			throw new InputException("shift requirements","does not have the right dimension",
					"the matrix should have dimensions [number of days] by [number of shifts]");
		}
		for (int i = 0; i < numDays; i++){
			if (shiftReqs.get(i).get(numShifts-1) != 0){
				throw new InputException("the last shift","must have a requirement of zero employees");
			}
		}
		if (preferences.size() != numEmployees || preferences.get(0).size() != numDays * numShifts){
			throw new InputException("employee preferences","does not have the right dimension",
					"the matrix should have dimensions [number of employees] by [number of days times number of shifts]");
		}
/*		if (minShifts > numDays * numShifts) {
			throw new InputException("the minimum number of shifts for an employee per scheduling period",
					"must not exceed the total number of shifts in the scheduling period");
		}
*/
	}
	
	/** 
	 * The fitness of the solution is based on how well you can satisfy 
	 * workers' preferences while still meeting the requirement of
	 * workers on duty per shift.
	 * 
	 * If an employee has more than maxShiftsInRow shifts in a row, 
	 * then fitness decreases by a significant amount.
	 */
	private double preferencesMet(Solution sol) {
		ArrayList<Integer> intSol = integerVarsOfSolution(sol);
		ArrayList<Integer> listPref = preferencesList(preferences);
		double totalHappiness = 0;
		for (int i = 0; i<numEmployees; i++){
			ArrayList<Integer> employeePref = row(listPref,i);
			ArrayList<Integer> employeeSched = row(intSol,i);
			double happiness = 0;
			for (int j = 0; j < numDays*numShifts; j++) {
				happiness += employeePref.get(j) * employeeSched.get(j);
			}
			totalHappiness += happiness;
		}
		return totalHappiness;
	}
	private double obeyMaxShifts(Solution sol) {
		ArrayList<Integer> intSol = integerVarsOfSolution(sol);
		int length = numDays * numShifts;
		double penalty = Math.pow(numEmployees * length,3);
		double violations = 0;
		for (int i = 0; i < numEmployees; i++) { // for each employee
			ArrayList<Integer> schedule = row(intSol,i);
			for (int j = 0; j < length; j++) { // for each shift
				int counter = 0;
				// check to make sure there are NO MORE THAN n shifts in a row, 
				// including wrapping around to the beginning of the schedule
				for (int k = 0; k < maxShiftsInRow + 1; k++){
					if (schedule.get((j+k) % length) == 0) break;
					else counter++;
				}
				if (counter > maxShiftsInRow) return violations + penalty;
			}
		}
		return violations;
	}
//	private double obeyMinShifts(Solution sol){
//		ArrayList<Integer> intSol = integerVarsOfSolution(sol);
//		int length = numDays * numShifts;
//		
//	}
	private double extraCost(Solution sol){
		ArrayList<Integer> intSol = integerVarsOfSolution(sol);
		ArrayList<Integer> shiftReqsList = shiftReqsList(shiftReqs);
		double cost = 0; 
		for (int i = 0; i < shiftReqsList.size(); i++){
			ArrayList<Integer> col = col(intSol,i);
			double difference = sumArrayList(col) - shiftReqsList.get(i);
			cost += Math.pow(difference, 2);
		}
		return cost;
	}
	
	public double fitness(Solution sol) {
		return -(preferencesMet(sol) + obeyMaxShifts(sol)) ;
		// return lambda * preferencesMet(sol) + (1 - lambda) * extraCost(sol);
	}

	/**
	 * A solution is within constraints if its number of workers
	 * per shift match or exceed the requirement.
	 * The solution must also not have one worker on duty for 
	 * more than maxShifts shifts in a row.
	 */
	public boolean withinCustomConstraints(Solution sol) {
		ArrayList<Integer> intSol = integerVarsOfSolution(sol);
		int length = numDays * numShifts;
		
		// minimum coverage
		ArrayList<Integer> shiftReqsList = shiftReqsList(shiftReqs);		
		for (int j = 0; j < length; j++) {
			if (sumArrayList(col(intSol,j)) < shiftReqsList.get(j))
				return false;
		}
		
		

		
		return true;
		// TODO max shifts per 24 hrs
		// TODO max back-to-back shifts
	}
	
	public int getNumVar() {
		return numEmployees * numDays * numShifts;
	}
	
	
	

	/* ********************** Helper Functions **************************/
	
	/*
	 * Converts the solution array list of doubles to an array list of integers.
	 * Needed for use throughout the rest of this file.
	 */
	private ArrayList<Integer> integerVarsOfSolution(Solution sol) {
		ArrayList<Double> vars = sol.getVars();
		return doubleListToIntegerList(vars);
	}
	
	private ArrayList<Integer> doubleListToIntegerList(ArrayList<Double> doubleList) {
	    int length = doubleList.size();
		ArrayList<Integer> integerList = new ArrayList<Integer>(length);
		for (int i = 0; i < length; i++){
			int x;
			if (doubleList.get(i)<.5) {x = 0;} else {x = 1;};
			integerList.add(x); 
		}
		return integerList;
	}
	
	/*
	 * Converts the inputed preferences matrix into a single array list,
	 * mimicking the appearance of the solutions array list.
	 */
	private ArrayList<Integer> preferencesList(ArrayList<ArrayList<Integer>> preferences) {
		int length = numDays * numShifts;
		ArrayList<Integer> prefList = new ArrayList<Integer>(length);
		for (int i = 0; i < numEmployees; i++){
			prefList.addAll(preferences.get(i));
		}
		return prefList;
	}
	/*
	 * Converts the shiftReqs matrix into a single array list
	 */
	private ArrayList<Integer> shiftReqsList(ArrayList<ArrayList<Integer>> shiftReqs){
		int length = numDays * numShifts;
		ArrayList<Integer> shiftReqsList = new ArrayList<Integer>(length);
		for (int i = 0; i < numDays; i++) 
			shiftReqsList.addAll(shiftReqs.get(i));
		return shiftReqsList;
	}
	
	private ArrayList<Integer> row(ArrayList<Integer> matrix, int index){
		int length = numDays * numShifts;
		ArrayList<Integer> row = new ArrayList<Integer>(length);
		for (int i = index*length; i < (index+1)*length; i++) {
			row.add(matrix.get(i));
		}
		return row;
	}
	
	private ArrayList<Integer> col(ArrayList<Integer> matrix, int index) {
		ArrayList<Integer> col = new ArrayList<Integer>(numEmployees);
		int skiplength = numDays * numShifts;
		for (int i = index; i < (numEmployees-1)*skiplength+index; i += skiplength) {
			col.add(matrix.get(i));
		}
		return col;
	}
	
	private int sumArrayList(ArrayList<Integer> list) {
		int length = list.size(); 
		int sum = 0;
		for (int i = 0; i < length; i++){
			sum += list.get(i);
		}
		return sum;
	}
	
	public boolean solsAreEqual(Solution s1, Solution s2) {
		ArrayList<Integer> s1Arr = this.integerVarsOfSolution(s1);
		ArrayList<Integer> s2Arr = this.integerVarsOfSolution(s2);
		return s1Arr.equals(s2Arr);
	}
	
	public void printSol(Solution s) {
	    ArrayList<Integer> vars = this.integerVarsOfSolution(s);
	    
		for (int i = 0; i < vars.size(); i++) {
		    if (i % (numDays * numShifts) == 0) System.out.printf("\n");
		    System.out.printf("%d ", vars.get(i));
		}
		System.out.printf("\n");
	    
	    
	}

}
