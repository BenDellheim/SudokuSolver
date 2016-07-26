package sat.formula;

import static org.junit.Assert.*;
import org.junit.Test;

public class FormulaTest {    
    Literal a = PosLiteral.make("a");
    Literal b = PosLiteral.make("b");
    Literal c = PosLiteral.make("c");
    Literal na = a.getNegation();
    Literal nb = b.getNegation();
    Literal nc = c.getNegation();
//    Clause x3 = new Clause(na).add(nc);

    // make sure assertions are turned on!  
    // we don't want to run test cases without assertions too.
    // see the handout to find out how to turn them on.
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false;
    }
    
    
    @Test
    public void testBooleans() {
        Clause x1 = new Clause(a);
        Clause x2 = new Clause(nb);
        Clause x3 = new Clause(nc);
        Clause x9 = new Clause(a).add(b).add(c);
        Formula f1 = new Formula(x1);
        Formula f2 = new Formula(x2).addClause(x3);
        Formula f5 = new Formula(x9);
    	System.out.println("f1 = " + f1);
    	System.out.println("f2 = " + f2);

    	Formula f3 = f1.and(f2);
    	if(f3 != null)
    	{
    		System.out.println("f1 and f2 = " + f3);
    		System.out.println("!(f1 and f2) = " + f3.not());
    	}
    	System.out.println("f5 = " + f5);
    	System.out.println("!f5 = " + f5.not());

    	Clause x4 = x1.merge(x2);
    	//    	Formula f4 = f1.or(f2);
    	if(x4 != null)
    	{System.out.println("x1.merge(x2) = " + x4);}
    	
    }

    
    
    // Helper function for constructing a clause.  Takes
    // a variable number of arguments, e.g.
    //  clause(a, b, c) will make the clause (a or b or c)
    // @param e,...   literals in the clause
    // @return clause containing e,...
    private Clause make(Literal... e) {
        Clause c = new Clause();
        for (int i = 0; i < e.length; ++i) {
            c = c.add(e[i]);
        }
        return c;
    }
}