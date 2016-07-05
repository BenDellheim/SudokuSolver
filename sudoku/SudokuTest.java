package sudoku;

import static org.junit.Assert.*;

import org.junit.Test;


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
    	System.out.println(puzzle1.toString());
    	Sudoku puzzle2 = new Sudoku(dim, square2);
    	System.out.println(puzzle2.toString());
    }
    
}