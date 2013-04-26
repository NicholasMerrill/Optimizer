package solutions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import problems.OptimizationProblem;

public class CSSolutionSet extends SolutionSet {
    
    private ArrayList<CSSolution> solutions;
    private Random rand;
    private int numNests;
    
    public CSSolutionSet(int numNests, int numVars) {
        this.numNests = numNests;
        super.N_SOL = numNests;
        this.rand = new Random();
        this.solutions = new ArrayList<CSSolution>(numNests);
        
        for (int i = 0; i < numNests; i++) {
            this.solutions.add(i, new CSSolution(numVars));
        }
    }
    
    public void initializeWithRandomSols(OptimizationProblem optProb) {
        for (CSSolution sol : this.solutions) {
            sol.setAsRandSol(optProb);
        }
    }
    
    public int getNumSols() {
        return numNests;
    }
    
    public void replace(int j, CSSolution sol) {
        this.solutions.set(j, sol);
    }
    
    public CSSolution getRandSol() {
        return this.solutions.get(rand.nextInt(this.getNumSols()));
    }

    public void abandonWorstSols(OptimizationProblem optProb, double abandonmentRatio) {
        this.sortByFitness(optProb);
        int numToAbandon = (int) (abandonmentRatio * this.numNests);
        int numToKeep = this.numNests - numToAbandon;
        for (int i = numToKeep; i < numNests; i++) {
            solutions.get(i).setAsRandSol(optProb);
        }
    }
}
