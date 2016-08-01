package sudoku;

import java.io.*;
import static org.junit.Assert.*;

import org.junit.Test;

import sat.formula.Formula;
import sat.env.*;

public class SudokuTest {
    
    @Test
    public void SudokuTester() {
    	int dim = 2;
    	int size = dim * dim;
    	int[][] square  = new int[size+1][size+1];
    	int[][] square2 = {{0,0,0,0,0},
    			           {0,2,3,4,2},
    			           {0,1,0,2,4},
    			           {0,1,3,2,0},
    			           {0,0,3,1,0}};
    	Sudoku emptyPuzzle = new Sudoku(dim);
    	Sudoku puzzle1 = new Sudoku(dim, square);
//    	System.out.println(puzzle1.toString());
    	Sudoku puzzle2 = new Sudoku(dim, square2);
//    	System.out.println(puzzle2.toString());
    	try{
    		Sudoku puzzle3 = Sudoku.fromFile(3, "samples\\sudoku_hard2.txt");
    		Formula problem3 = puzzle3.getProblem();
    		System.out.println(puzzle3);
    		    		
/*    		PrintWriter writer = new PrintWriter("sudoku.log", "UTF-8");
    		
    		writer.println("Problem size: " + problem3.getSize());
    		writer.println(problem3);
    		writer.close();
*/    	}
    	catch(IOException s){;}
    	catch(Sudoku.ParseException s){;}
    }
    
}