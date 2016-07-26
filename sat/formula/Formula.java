/**
 * Author: dnj, Hank Huang
 * Date: March 7, 2009
 * 6.005 Elements of Software Construction
 * (c) 2007-2009, MIT 6.005 Staff
 */
package sat.formula;

import immutable.*;
import java.util.Iterator;
import sat.env.Variable;

/**
 * Formula represents an immutable boolean formula in conjunctive normal form,
 * intended to be solved by an SAT solver.
 * 
 * Formula contains an ImList of Clause, which are considered ANDed together.
 * Clause  contains an ImList of Literal, which are considered ORed together.
 * Literal is abstracted into PosLiteral and NegLiteral, and is akin to variables like x1' or x2.
 * It's similar to a "system of equations" in algebra, where we're solving for the variables.
 * Only, the variables are all either true or false.
 */
public class Formula {
    private final ImList<Clause> clauses;
    // Rep invariant:
    //      clauses != null
    //      clauses contains no null elements (ensured by spec of ImList)
    //
    // Note: although a formula is intended to be a set,  
    // the list may include duplicate clauses without any problems. 
    // The cost of ensuring that the list has no duplicates is not worth paying.
    //
    //    
    //    Abstraction function:
    //        The list of clauses c1,c2,...,cn represents 
    //        the boolean formula (c1 and c2 and ... and cn)
    //        
    //        For example, if the list contains the two clauses (a,b) and (!c,d), then the
    //        corresponding formula is (a or b) and (!c or d).

    void checkRep() {
        assert this.clauses != null : "SATProblem, Rep invariant: clauses non-null";
    }

    /**
     * Create a new problem for solving that contains no clauses (that is the
     * vacuously true problem)
     * 
     * @return the true problem
     */
    public Formula() {
    	clauses = new EmptyImList<Clause>();
    	checkRep();
    }

    /**
     * Create a new problem for solving that contains a single clause with a
     * single literal
     * 
     * @return the problem with a single clause containing the literal l
     */
    public Formula(Variable l) {
    	clauses = new NonEmptyImList<Clause>(new Clause(PosLiteral.make(l)));
    	checkRep();
    }

    /**
     * Create a new problem for solving that contains a single clause
     * 
     * @return the problem with a single clause c
     */
    public Formula(Clause c) {
    	clauses = new NonEmptyImList<Clause>(c);
    	checkRep();
    }

    public Formula(ImList<Clause> c) {
        clauses = c;
        checkRep();
    }

    
    /**
     * Add a clause to this problem
     * 
     * @return a new problem with the clauses of this, but c added
     */
    public Formula addClause(Clause c) {
    	return new Formula(clauses.add(c));
    }

	/**
     * Get the clauses of the formula.
     * 
     * @return list of clauses
     */
    public ImList<Clause> getClauses() {
    	return clauses;
    }

    /**
     * Iterator over clauses
     * 
     * @return an iterator that yields each clause of this in some arbitrary
     *         order
     */
    public Iterator<Clause> iterator() {
    	return clauses.iterator();
    }

    /**
     * ANDs two formulas and returns the result.
     * Both formulas' clause lists are concatenated, since they are implicitly ANDed already.
     * @return a new problem corresponding to the conjunction of this and p
     */
    public Formula and(Formula p) {
    	ImList<Clause> resultList = clauses;
    	Iterator<Clause> list = p.clauses.iterator();
    	while(list.hasNext())
    	{
    		resultList = resultList.add(list.next());
    	}
    	
    	return new Formula(resultList);
    }

    /**
     * ORs two formulas and returns the result.
     * This makes the formula no longer in Conjunctive Normal Form, so we
     * have to use the distributive law to convert back to CNF.
     * I.E. for Formula1 (x1 & x2) OR Formula2 (y1 & y2),
     * result Formula3 will be (x1 | y1) & (x1 | y2) & (x2 | y1) & (x2 | y2)
	 * or in terms of Clauses, (x1,y1) & (x1,y2) & (x2,y1) & (x2,y2).
	 * NOTE: I corrected this comment after some research. They were WRONG! =)
     * @return a new problem corresponding to the disjunction of this and p
     */
    public Formula or(Formula p) {
    	ImList<Clause> returnList = new EmptyImList<Clause>();
    	Clause left;
    	Iterator<Clause> list1 = this.clauses.iterator();
    	while(list1.hasNext())
    	{
    		left = list1.next();
        	Iterator<Clause> list2 = p.clauses.iterator();
    		while(list2.hasNext())
    		{
    			Clause right = list2.next();
    			Clause tempClause;
    			tempClause = left.merge(right);
    			if(tempClause.isEmpty()) return null;
    			else returnList = returnList.add(tempClause);
    		}
    		
    	}
    	return new Formula(returnList);
    }

    /**
     * @return a new problem corresponding to the negation of this
     */
    public Formula not() {
        // Hint: you'll need to apply DeMorgan's Laws (http://en.wikipedia.org/wiki/De_Morgan's_laws)
        // to move the negation down to the literals, and the distributive law to preserve 
        // conjunctive normal form, i.e.:
        //   if you start with (a | b) & c,
        //   you'll need to make !((a | b) & c) 
        //                       => (!a & !b) | !c            (moving negation down to the literals)
        //                       => (!a | !c) & (!b | !c)    (conjunctive normal form)
    	checkRep();
    	Iterator<Clause> list1 = this.clauses.iterator();
    	Formula result = new Formula();

    	// Priming the loop
   		Clause clause1 = list1.next();
   		Iterator<Literal> literator = clause1.iterator();
   		while(literator.hasNext())
   		{
   			result = result.addClause(new Clause(literator.next().getNegation()));
   		}

   		// Main loop: calls result.or() with new Formula each time
   		while(list1.hasNext())
       	{
   			Formula nextFormula = new Formula();
   			Clause clause2 = list1.next();
   			Iterator<Literal> literator2 = clause2.iterator();
   			while(literator2.hasNext())
   			{
   				nextFormula = nextFormula.addClause(new Clause(literator2.next().getNegation()));
   			}
   			result = result.or(nextFormula);
    	}
   		return result;    	
    }

    /**
     * 
     * @return number of clauses in this
     */
    public int getSize() {
    	return clauses.size();
    }

    /**
     * @return string representation of this formula
     */
    public String toString() {
        String result = "Problem[";
        for (Clause c : clauses)
            result += "\n" + c;
        return result + "]";
    }
}
