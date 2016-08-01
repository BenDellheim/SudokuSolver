package sudoku;

import java.io.IOException;

import sat.SATSolver;
import sat.env.Environment;
import sat.formula.Formula;
import sudoku.Sudoku.ParseException;

public class Main {


    /*
     * Uncomment line(s) below to test your implementation! 
     */
    public static void main (String[] args) throws ParseException {
        timedSolve (new Sudoku(2));
//        timedSolve (new Sudoku(2, new int[][] { 
//                    new int[] { 0, 1, 0, 4 }, 
//                    new int[] { 0, 0, 0, 0 }, 
//                    new int[] { 2, 0, 3, 0 }, 
//                    new int[] { 0, 0, 0, 0 }, 
//        }));
//        timedSolveFromFile(3, "samples/sudoku_easy.txt");
//        timedSolveFromFile(3, "samples/sudoku_hard.txt");        
    }

    /**
     * Solve a puzzle and display the solution and the time it took.
     * @param sudoku
     * @throws ParseException 
     */
    private static void timedSolve (Sudoku sudoku) throws ParseException {
        long started = System.nanoTime();

        System.out.println ("Creating SAT formula...");
        Formula f = new Formula();
		try {
			f = sudoku.getProblem();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        System.out.println ("Solving...");
        Environment e = SATSolver.solve(f);
        
        System.out.println ("Interpreting solution...");
        Sudoku solution = sudoku.interpretSolution(e);
        
        System.out.println ("Solution is: \n" + solution);    

        long time = System.nanoTime();
        long timeTaken = (time - started);
        System.out.println ("Time:" + timeTaken/1000000 + "ms");
    }

    /**
     * Solve a puzzle loaded from a file and display the solution and the time it took.
     * @param dim  dimension of puzzle
     * @param filename  name of puzzle file to load
     */
    private static void timedSolveFromFile(int dim, String filename) {
        try {
            timedSolve (Sudoku.fromFile (dim, filename));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }        
    }
}
