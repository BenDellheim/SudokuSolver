/**
 * Author: dnj, Hank Huang
 * Date: March 7, 2009
 * 6.005 Elements of Software Construction
 * (c) 2007-2009, MIT 6.005 Staff
 */
package sudoku;

import java.io.IOException;

import sat.env.Environment;
import sat.env.Variable;
import sat.formula.Formula;

/**
 * Sudoku is an immutable abstract datatype representing instances of Sudoku.
 * Each object is a partially completed Sudoku puzzle.
 */
public class Sudoku {
    // dimension: standard puzzle has dim 3
    private final int dim;
    // number of rows and columns: standard puzzle has size 9
    private final int size;
    // known values: square[i][j] represents the square in the ith row and jth column,
    // contains -1 if the digit is not present, else k>=0 to represent the digit k+1
    // (digits are indexed from 0 and not 1 so that we can take the number k
    // from square[i][j] and use it to index into occupies[i][j][k])
    private final int[][] square;
    // occupies [i,j,k] means that kth symbol occupies entry in row i, column j
    private final Variable[][][] occupies;

    // Rep invariant
    // dim is positive and dim * dim == size
    // square[][] must have "size+1" dimensions     (Indexing STARTS FROM ONE)
    // occupies[][][] must have "size+1" dimensions (Indexing STARTS FROM ONE)

    private void checkRep() {
    	assert dim > 0: "Dim must be positive.";
    	assert dim * dim == size: "Size must be the square of Dim.";
    	assert square.length == size + 1: "Square must have size+1 length. Remember, indexing starts from 1.";
    	for( int i = 0; i < square.length; i++)
    	{
    		assert square[i].length == size + 1: "Square[" + i + "] must have size+1 length. Remember, indexing starts from 1.";
    		for( int j = 0; j < square.length; j++)
    		{
    			assert (0 <= square[i][j]) && (square[i][j] <= size):
    				"Square[" + i + "][" + j + "]'s value must be between 0 and " + size + ".";
    		}
    	}
    	assert occupies.length == size + 1: "Occupies must have size+1 length. Remember, indexing starts from 1.";
    	for( int i = 0; i < occupies.length; i++)
    	{
    		for( int j = 0; j < occupies.length; j++)
    		{
    			assert occupies[i][j].length == size + 1: "Occupies[" + i + "][" + j + "] must have size+1 length. Remember, indexing starts from 1.";
    		}
    	}
    	
    }

    /**
     * create an empty Sudoku puzzle of dimension dim.
     * 
     * @param dim
     *            size of one block of the puzzle. For example, new Sudoku(3)
     *            makes a standard Sudoku puzzle with a 9x9 grid.
     */
    public Sudoku(int dim) {
       this.dim = dim;
       this.size = dim * dim;
       this.square   = new int[size+1][size+1];
       this.occupies = new Variable[size+1][size+1][size+1];
   	   checkRep();
     }
    
    /**
     * create Sudoku puzzle
     * 
     * @param square
     *            digits or blanks of the Sudoku grid. square[i][j] represents
     *            the square in the ith row and jth column, contains 0 for a
     *            blank, else i to represent the digit i. 
     *            
     *            THE FIRST ROW + COLUMN ARE IGNORED! I made indexing start from 1, not 0.
     *            This makes indexing MUCH easier to read and troubleshoot later.
     *            
     *            So { { 0,0,0,0,0 }, 
     *                 { 0,0,0,0,1 },
     *                 { 0,2,3,0,4 },
     *                 { 0,0,0,0,3 },
     *                 { 0,4,1,0,2 } } <-----a 5x5 matrix
     *            represents the 4x4 Sudoku grid: 
     *            
     *            ...1 
     *            23.4 
     *            ...3
     *            41.2
     *            
     *            And this 4x4 is referenced by square[1][1] through square[4][4].
     * 
     * @param dim
     *            dimension (minor square size) of puzzle.
     *            Requires that dim*dim == square.length+1 == square[i].length+1 for all i such that 0<=i<dim.
     */
    public Sudoku(int dim, int[][] square) {
        this.dim = dim;
        this.size = dim * dim;
        this.square   = square;
        this.occupies = new Variable[size+1][size+1][size+1];
    	checkRep();
    }

    /**
     * Reads in a file containing a Sudoku puzzle.
     * 
     * @param dim
     *            Dimension of puzzle. Requires: at most dim of 3, because
     *            otherwise need different file format
     * @param filename
     *            of file containing puzzle. The file should contain one line
     *            per row, with each square in the row represented by a digit,
     *            if known, and a period otherwise. With dimension dim, the file
     *            should contain dim*dim rows, and each row should contain
     *            dim*dim characters.
     * @return Sudoku object corresponding to file contents
     * @throws IOException
     *             if file reading encounters an error
     * @throws ParseException
     *             if file has error in its format
     */
    public static Sudoku fromFile(int dim, String filename) throws IOException,
            ParseException {
        // TODO: implement this.
        throw new RuntimeException("not yet implemented.");
    }

    /**
     * Exception used for signaling grammatical errors in Sudoku puzzle files
     */
    @SuppressWarnings("serial")
    public static class ParseException extends Exception {
        public ParseException(String msg) {
            super(msg);
        }
    }

    /**
     * Produce readable string representation of this Sukoku grid, e.g. for a 4
     * x 4 sudoku problem: 
     *   12.4 
     *   3412 
     *   2.43 
     *   4321
     * 
     * @return a string corresponding to this grid
     */
    public String toString() {
    	checkRep();
    	String puzzle = "";
    	for(int i = 1; i <= size; i++)
    	{
    		for(int j = 1; j <= size; j++)
    		{
    			if(square[i][j] == 0)
    			{
    				puzzle += ".";
    			}
    			else puzzle += square[i][j];
    		}
    		puzzle += "\n";
    	}
    	checkRep();
    	return puzzle;
    }

    /**
     * @return a SAT problem corresponding to the puzzle, using variables with
     *         names of the form occupies(i,j,k) to indicate that the kth symbol
     *         occupies the entry in row i, column j
     */
    public Formula getProblem() {

        // TODO: implement this.
        throw new RuntimeException("not yet implemented.");
    }

    /**
     * Interpret the solved SAT problem as a filled-in grid.
     * 
     * @param e
     *            Assignment of variables to values that solves this puzzle.
     *            Requires that e came from a solution to this.getProblem().
     * @return a new Sudoku grid containing the solution to the puzzle, with no
     *         blank entries.
     */
    public Sudoku interpretSolution(Environment e) {

        // TODO: implement this.
        throw new RuntimeException("not yet implemented.");
    }

}