package sat;

import static org.junit.Assert.*;

import org.junit.Test;

import sat.env.*;
import sat.formula.*;
import sudoku.*;
import sudoku.Sudoku.ParseException;

public class SATSolverTest {
    Literal a = PosLiteral.make("a");
    Literal b = PosLiteral.make("b");
    Literal c = PosLiteral.make("c");
    Literal na = a.getNegation();
    Literal nb = b.getNegation();
    Literal nc = c.getNegation();

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false;
    }

    @Test
    public void testSolve() throws ParseException {
    	Clause c1 = new Clause(a).add(nb);
    	Clause c2 = new Clause(a).add(b);
    	Clause c3 = new Clause(a);
    	Clause c4 = new Clause(b);
    	Clause c5 = new Clause(nb);
    	Clause c6 = new Clause(c);
    	//(a | ~b) & (a | b)
    	//Should return a->TRUE
    	Formula f1 = new Formula(c1).addClause(c2);
    	
    	//(a & b) & (a & ~b)
    	//Should return: null
    	Formula f2 = new Formula(c3).addClause(c4).and(
    			     new Formula(c3).addClause(c5));

    	//(a & b) & (~b | c)
    	//Should return: a->TRUE, b->TRUE, c->TRUE (order arbitrary)
    	Formula f3 = new Formula(c3).addClause(c4).and(
    			     new Formula(c5).or(new Formula(c6)));

    	
/*    	
    	Environment solution1 = SATSolver.solve(f1);
    	System.out.println("Solution 1:" + solution1);
    	Environment solution2 = SATSolver.solve(f2);
    	System.out.println("Solution 2:" + solution2);
    	Environment solution3 = SATSolver.solve(f3);
    	System.out.println("Solution 3:" + solution3);
  */  	
    	Environment solution = new Environment();
    	Environment solutionn = new Environment();
    	//        timedSolve (new Sudoku(2, new int[][] {{ 0, 1, 0, 4 }, { 0, 0, 0, 0 }, { 2, 0, 3, 0 }, { 0, 0, 0, 0 }}));
    	solution = solution.putTrue(Sudoku.literalVar(3, 1, 2));
    	solution = solution.putTrue(Sudoku.literalVar(1, 2, 1));
    	solution = solution.putTrue(Sudoku.literalVar(1, 4, 4));
    	solution = solution.putTrue(Sudoku.literalVar(3, 3, 3));
    	solution = solution.putTrue(Sudoku.literalVar(1, 1, 3));

    	int[][] butts = {{0,0,0,0,0},
    					 {0,0,2,3,4},
    			 		 {0,3,4,1,0},
    			 		 {0,2,1,4,0},
    			 		 {0,0,3,2,1}};

//		Sudoku s = new Sudoku(2);
		Sudoku s2 = new Sudoku(2, butts);
		System.out.println(SATSolver.solve(s2.getProblem()));
		s2 = s2.interpretSolution(SATSolver.solve(s2.getProblem()));
		System.out.println(s2);
		
    }
    

    
}