/**
 * Author: dnj, Hank Huang
 * Date: March 7, 2009
 * 6.005 Elements of Software Construction
 * (c) 2007-2009, MIT 6.005 Staff
 */
package sudoku;

import java.io.*;

import sat.env.Bool;
import sat.env.Environment;
import sat.env.Variable;
import sat.formula.*;

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

    /**
     * Verifies the Sudoku instance was created properly.
     * Rep invariant:
     *  dim is positive and dim * dim == size,
     *  square[][] must have "size+1" dimensions     (Indexing STARTS FROM ONE),
     *  occupies[][][] must have "size+1" dimensions (Indexing STARTS FROM ONE)
     */
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
    				"Square[" + i + "][" + j + "]'s value must be between 0 and " + size + ". Value was " + square[i][j];
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
     *            This makes indexing MUCH easier to read and troubleshoot later, at the cost
     *            of a more confusing setup.
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
        this.occupies = new Variable[size+1][size+1][size+1];
        if(dim*dim == square.length)
        {
        	// We need to add a leading blank row/column for my internal format
        	int[][] sq = new int[dim*dim+1][dim*dim+1];
        	for( int i = 0; i < dim*dim; i++)
        	{
        		for( int j = 0; j < dim*dim; j++)
        		{
        			sq[i+1][j+1] = square[i][j];
        		}
        	}
        	this.square = sq;
        }
        else
        {
            this.square = square;
        }
    	checkRep();
    }

    /**
     * Reads in a file containing a Sudoku puzzle.
     * 
     * @param dim
     *            Dimension of puzzle. Requires: 2 for a 4x4, or 3 for a 9x9.
     *            These are the only two sizes supported.
     * @param filename
     *            of file containing puzzle. The file should contain one line
     *            per row, with each square in the row represented by a digit
     *            (if known) or a '.' otherwise. (The periods are converted to zeros.)
     *            The file should contain dim*dim rows, and each row should contain dim*dim characters.
     *            
     *            NOTE: There is support for puzzles with an extra row/column
     *            so their indexes can start at 1 not 0.
     *            I.E. if for a 9x9 puzzle the file has 10x10 entries, the first row/column will be ignored
     *            and the rest will be used for the 9x9 puzzle.
     * @return Sudoku object corresponding to file contents
     * @throws IOException
     *             if file reading encounters an error
     * @throws ParseException
     *             if file has error in its format
     */
    public static Sudoku fromFile(int dim, String filename) throws IOException, ParseException {
        if(!(dim == 2 || dim == 3)){throw new ParseException("Invalid dim. Only values 2 or 3 are compatible.");}
    	try(
    			FileReader fr = new FileReader(filename);
            	BufferedReader br = new BufferedReader(fr);)
        {
        	String line;
        	int puzzleLength, buffer;
        	int[][] newPuzzle = new int[dim*dim+1][dim*dim+1];
        	
        	/* Priming the loop with a check for file type.
        	 * This block initializes the flag "buffer", which tells me whether or not to
        	 * skip the first row/column. (I add a "0" to row/column 0 to make indexing easier.
        	 * This checks if the file was saved that way or not.)
        	 * 
        	 * If the later rows aren't the same length (this is actually valid for Java arrays),
        	 * I'll throw a ParseException.
        	 */
        	line = br.readLine();
        	puzzleLength = line.length();
        	if( puzzleLength == dim * dim)
        	{
        		buffer = 0;
        		// Loop once to add primed row (ROW 1) to newPuzzle
        		for(int col = 0; col < line.length(); col++)
        		{
        			char s = line.charAt(col);
        			if(Character.isDigit(s))
        			{
        				newPuzzle[1][col+1] = s - '0';
        			}
        			else if(s == '.')
        			{
        				newPuzzle[1][col+1] = 0;
        			}
        			else
        			{
        				throw new ParseException("Invalid character in " + filename);
        			}
            	}
        	}
        	else if( puzzleLength == dim * dim + 1){ buffer = 1;} // No need in adding a zero row
        	else throw new ParseException("Invalid row size in " + filename + "; must be " + dim*dim + " or " + dim*dim+1);

        	
        	// Main loop to read Sudoku from file.
        	// The first row of the puzzle is stored to index "1", so if buffer == 0 we're on row 2
        	// (row 1 was stored in the last step), and if buffer == 1 we're on row 1 (because it skipped a buffer row).
        	for(int row = 2-buffer; (line = br.readLine()) != null; row++)
        	{
        		if( line.length() != puzzleLength) throw new ParseException("Invalid row size.");
        		
        		// Start at 0 if no buffer column; start at 1 if there is one.
            	for(int col = buffer; col < line.length(); col++)
            	{
            		char s = line.charAt(col);
            		if(Character.isDigit(s))
            		{
            			newPuzzle[row][col+1-buffer] = s - '0';
            		}
            		else if( s == '.')
            		{
            			newPuzzle[row][col+1-buffer] = 0;
            		}
            		else
            		{
            			throw new ParseException("Invalid character in " + filename + " at (" + row + ", " + col + ").");
            		}
            	}
        		
        	}
        	return new Sudoku(dim, newPuzzle);
        	
        }
        catch(IOException s){System.out.println("Error opening file.");}
        catch(ParseException s){}
    	return new Sudoku(dim); // Returns empty puzzle if there's an error
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
     * Helper function for getProblem(); used as a parameter for PosLiteral.make().
     * @param i row
     * @param j column
     * @param k value
     * @return the variable name for the literal
     */
    private String literalString(int i, int j, int k) {
//		return "occupies(" + Integer.toString(i) + ", " + Integer.toString(j) + ", " + Integer.toString(k)+ ")";
		return "v" + Integer.toString(i) + Integer.toString(j) + Integer.toString(k);

    }

    /**
     * Public static version of literalString().
     * A testing function for making Variables quickly in the JUnit tests.
     * @param i row
     * @param j column
     * @param k value
     * @return a variable with the name returned by literalString: "occupies(i, j, k)"
     */
    public static Variable literalVar(int i, int j, int k) {
		return new Variable("v" + Integer.toString(i) + Integer.toString(j) + Integer.toString(k));
    }

    /**
     * @return a SAT problem corresponding to the puzzle, using variables with
     *         names of the form occupies(i,j,k) to indicate that the kth symbol
     *         occupies the entry in row i, column j. This one's a doozy.
     * @throws ParseException
     *             if a dim other than 2 or 3 is used
     */
    public Formula getProblem() throws ParseException {
    	// int dim, int size
    	// int square[][] (size+1)
    	// Variable occupies[][][] (size+1)
        if(!(dim == 2 || dim == 3)){throw new ParseException("Invalid dim. Only values 2 or 3 are compatible.");}

    	checkRep();
    	Formula newProblem = new Formula();
    	// 1. Solution must be consistent with the starting grid.
    	// For every entry (already-filled square) in square[][], produce a clause. (81 max for a 9x9)
    	for(int i = 1; i <= size; i++)
    	{
    		for(int j = 1; j <= size; j++)
    		{
    			int k = square[i][j];
    			if(k > 0)
    			{
    				PosLiteral l = PosLiteral.make(literalString(i,j,k));
    				newProblem = newProblem.addClause(new Clause(l));
    				occupies[i][j][k] = l.getVariable();
    			}
    		}
    	}
    	
    	// 2. At most ONE DIGIT per square! (2,754 clauses for a 9x9 puzzle!)
    	for(int i = 1; i <= size; i++)
    	{
    		for(int j = 1; j <= size; j++)
    		{
    			/* For EACH CELL in the puzzle (hence the above for() loops), we need to make a set of clauses.
    			 * We have to loop through every pair of numbers, i.e. (1,2), (1,3), (1,4), (2,3), (2,4).
    			 * For EVERY PAIR k1/k2, we add the clause (NOT v[i][j][k1] or NOT v[i][j][k2]).
    			 * This logic prevents both pairs from being true at the same time.
    			 * IOW: If there's a digit in a cell, this prevents any other digit from being there.
    			 */
    			for(int k1 = 1; k1 < size; k1++)
    			{
    				for(int k2 = k1+1; k2 <= size; k2++)
    				{
        				PosLiteral L1 = PosLiteral.make(literalString(i,j,k1));
        				PosLiteral L2 = PosLiteral.make(literalString(i,j,k2));
    					newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L2.getNegation()));
    				}
    			}
    		}
    	}
    	
    	// 3. In each ROW "i", each DIGIT "k" must appear exactly once.
    	for(int i = 1; i <= size; i++)
    	{
    		for(int k = 1; k <= size; k++)
    		{
    			// First, we add a clause that guarantees the digit to appear AT LEAST once in this row.
    			// For each row i and digit k: (v[i][1][k] or v[i][2][k] or v[i][3][k]...)
    			// IOW: 1... or .1.. or ..1. or ...1 for every row and every number. Yeah it's a lot of clauses x.x
				Clause tempClause = new Clause();
    			for(int j = 1; j <= size; j++)
    			{
    				PosLiteral l = PosLiteral.make(literalString(i,j,k));
    				tempClause = tempClause.add(l); // Remember, adding to a clause is the same as ORing the literals
    			}
    			newProblem = newProblem.addClause(tempClause);

    			
    			/* ALSO within the ROW, we get to add the tricky loop again! Phew, here goes...
    			 * Same as part 2, only this time we're iterating through the middle value (j).
    			 * 
    			 * We need to make sure the current number (k) only appears ONCE in this row.
    			 * To do this, we take every pair of cells (j1/j2) in this row,
    			 * and add the clause (NOT v[i][j1][k] or NOT v[i][j2][k]) -- see part 2 above for details.
    			 * For example, the number 2 can be in ONE of (1st or 2nd), (1st or 3rd), (1st or 4th), (2nd or 3rd), (2nd or 4th), etc.
    			 */
    			for(int j1 = 1; j1 < size; j1++)
    			{
    				for(int j2 = j1+1; j2 <= size; j2++)
    				{
        				PosLiteral L1 = PosLiteral.make(literalString(i,j1,k));
        				PosLiteral L2 = PosLiteral.make(literalString(i,j2,k));
    					newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L2.getNegation()));
    				}
    			}
    		}
    	}
    	
    	// 4. In each COLUMN "j", each DIGIT "k" must appear exactly once.
    	// Exactly the same as part 3, only checking vertically instead of horizontally. (So, i/j swap)
    	for(int j = 1; j <= size; j++)
    	{
    		for(int k = 1; k <= size; k++)
    		{
    			/* First, we add a clause that guarantees the digit to appear AT LEAST once in this column.
    			 * For each column j and digit k: (v[1][j][k] or v[2][j][k] or v[3][j][k]...)
    			 * IOW: 1  or  .  or  .  or  .
    			 *      .      1      .      .
    			 *      .      .      1      .
    			 *      .      .      .      1
    			 * for every column and every number. Yeah it's a lot of clauses x.x
    			 */
				Clause tempClause = new Clause();
    			for(int i = 1; i <= size; i++)
    			{
    				PosLiteral l = PosLiteral.make(literalString(i,j,k));
    				tempClause = tempClause.add(l); // Remember, adding to a clause is the same as ORing the literals
    			}
    			newProblem = newProblem.addClause(tempClause);

    			
    			/* ALSO within the COLUMN, we get to add the tricky loop again! Phew, here goes...
    			 * Same as part 2, only this time we're iterating through the first value (i).
    			 * 
    			 * We need to make sure the current number (k) only appears ONCE in this column.
    			 * To do this, we take every pair of cells (i1/i2) in this column,
    			 * and add the clause (NOT v[i1][j][k] or NOT v[i2][j][k]) -- see part 2 above for details.
    			 * For example, the number 2 can be in ONE of (1st or 2nd), (1st or 3rd), (1st or 4th), (2nd or 3rd), (2nd or 4th), etc.
    			 */
    			for(int i1 = 1; i1 < size; i1++)
    			{
    				for(int i2 = i1+1; i2 <= size; i2++)
    				{
        				PosLiteral L1 = PosLiteral.make(literalString(i1,j,k));
        				PosLiteral L2 = PosLiteral.make(literalString(i2,j,k));
    					newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L2.getNegation()));
    				}
    			}
    		}
    	}
    	
    	// 5. In each BLOCK, each digit must appear exactly once.
    	// 
    	// ...*sigh*
    	// Okay, this part is tricky enough that I'm just gonna hard-code it.
    	// If you want to implement this class for dim 4 or higher (16x16+ Sudokus), you'll have to modify this section.
    	// Looping through each block, and then each value in each block is NOT pretty in for() loops.
    	
    	if(dim == 3)
    	{
    		// Each digit must appear exactly once in its 3x3 block
    		// Add a clause for each digit being ORed in its respective block
			for(int a = 0; a < size; a+= 3){ // Adds 0, 3, 6
			for(int b = 0; b < size; b+= 3){ // Adds 0, 3, 6
			for(int k = 1; k <= size; k++){
	    		Clause tempClause = new Clause();
				tempClause = tempClause.add(PosLiteral.make(literalString(1+a,1+b,k)))
						               .add(PosLiteral.make(literalString(1+a,2+b,k)))
						               .add(PosLiteral.make(literalString(1+a,3+b,k)))
						               .add(PosLiteral.make(literalString(2+a,1+b,k)))
						               .add(PosLiteral.make(literalString(2+a,2+b,k)))
						               .add(PosLiteral.make(literalString(2+a,3+b,k)))
						               .add(PosLiteral.make(literalString(3+a,1+b,k)))
						               .add(PosLiteral.make(literalString(3+a,2+b,k)))
						               .add(PosLiteral.make(literalString(3+a,3+b,k)));
    			newProblem = newProblem.addClause(tempClause);
			}}}

			
			// For each PAIR of cells in each block, each can only have the digit once. (See part 2)
			// Add a clause for each pair: (NOT v[i1][j1][k] or NOT v[i2][j2][k])
			// There's... PROBABLY an easier way to write this, but whatever. It's only done once.
			for( int a = 0; a < size; a+= 3){ // Adds 0, 3, 6
			for( int b = 0; b < size; b+= 3){ // Adds 0, 3, 6
			for( int k = 1; k<= size; k++){
				PosLiteral L1 = PosLiteral.make(literalString(1+a,1+b,k));
				PosLiteral L2 = PosLiteral.make(literalString(1+a,2+b,k));
				PosLiteral L3 = PosLiteral.make(literalString(1+a,3+b,k));
				PosLiteral L4 = PosLiteral.make(literalString(2+a,1+b,k));
				PosLiteral L5 = PosLiteral.make(literalString(2+a,2+b,k));
				PosLiteral L6 = PosLiteral.make(literalString(2+a,3+b,k));
				PosLiteral L7 = PosLiteral.make(literalString(3+a,1+b,k));
				PosLiteral L8 = PosLiteral.make(literalString(3+a,2+b,k));
				PosLiteral L9 = PosLiteral.make(literalString(3+a,3+b,k));
				newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L2.getNegation()));
				newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L3.getNegation()));
				newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L4.getNegation()));
				newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L5.getNegation()));
				newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L6.getNegation()));
				newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L7.getNegation()));
				newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L8.getNegation()));
				newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L9.getNegation()));
				newProblem = newProblem.addClause(new Clause(L2.getNegation()).add(L3.getNegation()));
				newProblem = newProblem.addClause(new Clause(L2.getNegation()).add(L4.getNegation()));
				newProblem = newProblem.addClause(new Clause(L2.getNegation()).add(L5.getNegation()));
				newProblem = newProblem.addClause(new Clause(L2.getNegation()).add(L6.getNegation()));
				newProblem = newProblem.addClause(new Clause(L2.getNegation()).add(L7.getNegation()));
				newProblem = newProblem.addClause(new Clause(L2.getNegation()).add(L8.getNegation()));
				newProblem = newProblem.addClause(new Clause(L2.getNegation()).add(L9.getNegation()));
				newProblem = newProblem.addClause(new Clause(L3.getNegation()).add(L4.getNegation()));
				newProblem = newProblem.addClause(new Clause(L3.getNegation()).add(L5.getNegation()));
				newProblem = newProblem.addClause(new Clause(L3.getNegation()).add(L6.getNegation()));
				newProblem = newProblem.addClause(new Clause(L3.getNegation()).add(L7.getNegation()));
				newProblem = newProblem.addClause(new Clause(L3.getNegation()).add(L8.getNegation()));
				newProblem = newProblem.addClause(new Clause(L3.getNegation()).add(L9.getNegation()));
				newProblem = newProblem.addClause(new Clause(L4.getNegation()).add(L5.getNegation()));
				newProblem = newProblem.addClause(new Clause(L4.getNegation()).add(L6.getNegation()));
				newProblem = newProblem.addClause(new Clause(L4.getNegation()).add(L7.getNegation()));
				newProblem = newProblem.addClause(new Clause(L4.getNegation()).add(L8.getNegation()));
				newProblem = newProblem.addClause(new Clause(L4.getNegation()).add(L9.getNegation()));
				newProblem = newProblem.addClause(new Clause(L5.getNegation()).add(L6.getNegation()));
				newProblem = newProblem.addClause(new Clause(L5.getNegation()).add(L7.getNegation()));
				newProblem = newProblem.addClause(new Clause(L5.getNegation()).add(L8.getNegation()));
				newProblem = newProblem.addClause(new Clause(L5.getNegation()).add(L9.getNegation()));
				newProblem = newProblem.addClause(new Clause(L6.getNegation()).add(L7.getNegation()));
				newProblem = newProblem.addClause(new Clause(L6.getNegation()).add(L8.getNegation()));
				newProblem = newProblem.addClause(new Clause(L6.getNegation()).add(L9.getNegation()));
				newProblem = newProblem.addClause(new Clause(L7.getNegation()).add(L8.getNegation()));
				newProblem = newProblem.addClause(new Clause(L7.getNegation()).add(L9.getNegation()));
				newProblem = newProblem.addClause(new Clause(L8.getNegation()).add(L9.getNegation()));
			}}}
			
    	}
    	else
    	{
    		// dim == 2
    		// Each digit must appear exactly once in its 2x2 block
    		// Add a clause for each digit being ORed in its respective block
			for( int a = 0; a < size; a+= 2){ // Adds 0, 2
			for( int b = 0; b < size; b+= 2){ // Adds 0, 2
			for( int k = 1; k <= size; k++)
			{
				Clause tempClause = new Clause();
				tempClause = tempClause.add(PosLiteral.make(literalString(1+a,1+b,k)))
			                           .add(PosLiteral.make(literalString(1+a,2+b,k)))
  			                           .add(PosLiteral.make(literalString(2+a,1+b,k)))
			                           .add(PosLiteral.make(literalString(2+a,2+b,k)));
				newProblem = newProblem.addClause(tempClause);
			}}}
			
			// For each PAIR of cells in each 2x2 block, each digit must appear once.
			// Add a clause for each pair: (NOT v[i1][j1][k] or NOT v[i2][j2][k])
			for( int a = 0; a < size; a+= 2){ // Adds 0, 2
			for( int b = 0; b < size; b+= 2){ // Adds 0, 2
			for( int k = 1; k<= size; k++){
				PosLiteral L1 = PosLiteral.make(literalString(1+a,1+b,k));
				PosLiteral L2 = PosLiteral.make(literalString(1+a,2+b,k));
				PosLiteral L3 = PosLiteral.make(literalString(2+a,1+b,k));
				PosLiteral L4 = PosLiteral.make(literalString(2+a,2+b,k));
				newProblem = newProblem.addClause(new Clause(L1.getNegation()).add(L2.getNegation()))
				                       .addClause(new Clause(L1.getNegation()).add(L3.getNegation()))
				                       .addClause(new Clause(L1.getNegation()).add(L4.getNegation()))
				                       .addClause(new Clause(L2.getNegation()).add(L3.getNegation()))
				                       .addClause(new Clause(L2.getNegation()).add(L4.getNegation()))
				                       .addClause(new Clause(L3.getNegation()).add(L4.getNegation()));
			}}}
			
    	}

    	return newProblem;
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
    public Sudoku interpretSolution(Environment e) throws ParseException {
    	// Uses e.get(Variable) while looping through all values.
    	// Every TRUE value is added to the Sudoku solution.
    	// There SHOULD be only one True value per coordinate, but I check in case of an error.

    	if( e == null) throw new ParseException("Solution not found.");
    	
    	Sudoku solution = new Sudoku(dim);
    	for(int i = 1; i<= size; i++)
    	{
    		for(int j = 1; j<= size; j++)
    		{
    			for(int k = 1; k<= size; k++)
    			{
    				Variable v = literalVar(i,j,k);
    				if(e.get(v) == Bool.TRUE)
    				{
    					if(solution.square[i][j] != 0) throw new ParseException("Multiple values found at (" + i + ", " + j + ").");
    					solution.square[i][j] = k;
    					solution.occupies[i][j][k] = v;
    					break;
    				}
    			}
    		}
    	}
    	// Final check for any empty values. If found, throw ParseException.
    	solution.checkRep();
    	for(int i = 1; i<= size; i++)
    	{
    		for(int j = 1; j<= size; j++)
    		{
    			if(solution.square[i][j] == 0) throw new ParseException("Solution provided has empty value (" + i + ", " + j + ")");
    		}
    	}
    	return solution;
    }

}
