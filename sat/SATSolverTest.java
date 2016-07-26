package sat;

import static org.junit.Assert.*;

import org.junit.Test;

import sat.env.Bool;
import sat.env.Environment;
import sat.env.Variable;
import sat.formula.Clause;
import sat.formula.Formula;
import sat.formula.Literal;
import sat.formula.PosLiteral;

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
    public void testSolve() {
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
		Literal lit = c5.chooseLiteral();
		Variable var = lit.getVariable();
		System.out.println(f3);
		SATSolver.solve(f3);
    }
    

    
}