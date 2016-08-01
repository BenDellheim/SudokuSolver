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

    // make sure assertions are turned on!  
    // we don't want to run test cases without assertions too.
    // see the handout to find out how to turn them on.
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
    	//Should return a: True, b: anything
    	Formula f1 = new Formula(c1).addClause(c2);
    	
    	//(a & b) & (a & ~b)
    	//Should return: null
    	Formula f2 = new Formula(c3).addClause(c4).and(
    			     new Formula(c3).addClause(c5));

    	//(a & b) & (~b | c)
    	//Should return: a: True, b: True, c: True
    	Formula f3 = new Formula(c3).addClause(c4).and(
    			     new Formula(c5).or(new Formula(c6)));
    	
    	Environment solution = new Environment();
    	solution = solution.putTrue(Sudoku.literalVar(1, 1, 1));
    	solution = solution.putFalse(Sudoku.literalVar(1, 2, 2));
    	solution = solution.putTrue(Sudoku.literalVar(1, 3, 3));
    	solution = solution.putTrue(Sudoku.literalVar(1, 4, 4));
    	solution = solution.putTrue(Sudoku.literalVar(2, 1, 3));
    	solution = solution.putTrue(Sudoku.literalVar(2, 2, 2));
    	solution = solution.putTrue(Sudoku.literalVar(2, 3, 1));
    	solution = solution.putTrue(Sudoku.literalVar(2, 4, 4));
    	solution = solution.putTrue(Sudoku.literalVar(3, 1, 3));
    	solution = solution.putTrue(Sudoku.literalVar(3, 2, 1));
    	solution = solution.putTrue(Sudoku.literalVar(3, 3, 4));
    	solution = solution.putTrue(Sudoku.literalVar(3, 4, 2));
    	solution = solution.putTrue(Sudoku.literalVar(4, 1, 2));
    	solution = solution.putTrue(Sudoku.literalVar(4, 2, 1));
    	solution = solution.putTrue(Sudoku.literalVar(4, 3, 4));
    	solution = solution.putTrue(Sudoku.literalVar(4, 4, 3));
    	solution = solution.putTrue(Sudoku.literalVar(1, 2, 1));

/*		Literal lit = c5.chooseLiteral();
		Variable var = lit.getVariable();
*/		Sudoku s = new Sudoku(2);
		s = s.interpretSolution(solution);
		System.out.println(s);
		
//		SATSolver.solve(f3);
    }
    

    
}