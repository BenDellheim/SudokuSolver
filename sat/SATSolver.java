package sat;

import immutable.*;
import sat.env.*;
import sat.formula.*;
import java.util.Iterator;

/**
 * A simple DPLL SAT solver. See http://en.wikipedia.org/wiki/DPLL_algorithm
 */
public class SATSolver {
    /**
     * Solve the problem using a simple version of DPLL with backtracking and
     * unit propagation. The returned environment uses a map of Variable->Bool
     * rather than PosLiteral/NegLiteral->Bool, so that clients can more readily use it.
     * 
     * @return an environment for which the problem evaluates to Bool.TRUE, or
     *         null if no such environment exists.
     */
    public static Environment solve(Formula formula) {
    	// Verify size > 0.
    	// If there are NO clauses, the formula is trivially solvable.
    	// Stop and return an empty Environment.
    	ImList<Clause> clauseList = formula.getClauses();
    	if(clauseList.size() == 0) return new Environment();

    	// Verify no empty clauses.
    	// If size>0 and there is an empty clause, the list is unsatisfiable.
    	// Fail and backtrack (return null).
    	Iterator<Clause> emptyCheck = clauseList.iterator();
    	while(emptyCheck.hasNext())
    	{
    		Clause ec = emptyCheck.next();
    		if(ec.isEmpty()) return null;
    	}

    	// There are clauses and they are not empty
    	// Find the smallest clause
    	Environment solution = new Environment();
    	Iterator<Clause> clauseItor = clauseList.iterator();
    	Clause smallest = clauseItor.next();
    	int smallestSize = smallest.size();
    	while(clauseItor.hasNext())
    	{
        	Clause current = clauseItor.next();
    		if(current.size() < smallestSize)
    		{
    			smallest = current;
    			smallestSize = smallest.size();
    		}
    	}
    	// smallest is now the smallest Clause, with size smallestSize (int)
    	if(smallest.isUnit())
    	{
    		// Bind its variable in the environment so the clause is satisfied.
    		// Call substitute() in all of the other clauses.
    		// Recursively call solve().
    		Literal lit = smallest.chooseLiteral();
    		Variable var = lit.getVariable();
    		if(lit instanceof NegLiteral) 
    		{
    			solution = solution.putFalse(var);
    		}
    		else
    		{
        		solution = solution.putTrue(var);
    		}
    		ImList<Clause> cList = substitute(clauseList, lit);
    		solution = solve(cList, solution);
    	}
    	else
    	{
    		// No unit Clause
    		// Take the smallest one, try setting it to True and recurse
    		Literal arbLit = smallest.chooseLiteral();
    		ImList<Clause> cList = substitute(clauseList, arbLit);
    		Environment tempSolution = solve(cList, solution);
    		if(tempSolution == null)
    		{
    			// Dead end when setting to True
    			// Try setting it to False!
    			cList = substitute(clauseList, arbLit.getNegation());
    			tempSolution = solve(cList, solution);
    			if(tempSolution == null)
    			{
    				// False didn't work either
    				// Formula is unsolvable!!
    				return null;
    			}
    		}
    		solution = tempSolution;
    	}
    	// Not sure about this, but it should come here after finishing recursion and return the solution.
    	// TODO: Verify the solution will be finalized by this point!
    	return solution;
    }

    /**
     * Takes a partial assignment of variables to values, and recursively
     * searches for a complete satisfying assignment.
     * 
     * @param clauses
     *            formula in conjunctive normal form
     * @param env
     *            assignment of some or all variables in clauses to true or
     *            false values.
     * @return an environment for which all the clauses evaluate to Bool.TRUE,
     *         or null if no such environment exists.
     */
    private static Environment solve(ImList<Clause> clauseList, Environment solution) {
    	// Verify size > 0.
    	// If there are NO clauses, we are DONE.
    	// Stop and return the solution!
    	if(clauseList.size() == 0) return solution;

    	// Verify no empty clauses.
    	// If size>0 and there is an empty clause, the list is unsatisfiable.
    	// Fail and backtrack (return null).
    	Iterator<Clause> emptyCheck = clauseList.iterator();
    	while(emptyCheck.hasNext())
    	{
    		Clause ec = emptyCheck.next();
    		if(ec.isEmpty()) return null;
    	}

    	// There are clauses and they are not empty
    	// Find the smallest clause
    	Iterator<Clause> clauseItor = clauseList.iterator();
    	Clause smallest = clauseItor.next();
    	int smallestSize = smallest.size();
    	while(clauseItor.hasNext())
    	{
        	Clause current = clauseItor.next();
    		if(current.size() < smallestSize)
    		{
    			smallest = current;
    			smallestSize = smallest.size();
    			if(smallestSize == 1) break;
    		}
    	}
    	// smallest is now the smallest Clause, with size smallestSize (int)
    	if(smallest.isUnit())
    	{
    		// Bind its variable in the environment so the clause is satisfied.
    		// Call substitute() in all of the other clauses.
    		// Recursively call solve().
    		Literal lit = smallest.chooseLiteral();
    		Variable var = lit.getVariable();
    		if(lit instanceof NegLiteral) 
    		{
    			solution = solution.putFalse(var);
    		}
    		else
    		{
        		solution = solution.putTrue(var);
    		}
    		ImList<Clause> cList = substitute(clauseList, lit);
    		solution = solve(cList, solution);
    	}
    	else
    	{
    		// No unit Clause
    		// Take the smallest one, try setting it to True and recurse
    		Literal arbLit = smallest.chooseLiteral();
    		ImList<Clause> cList = substitute(clauseList, arbLit);
    		Environment tempSolution = solve(cList, solution);
    		if(tempSolution == null)
    		{
    			// Dead end when setting to True
    			// Try setting it to False!
    			cList = substitute(clauseList, arbLit.getNegation());
    			tempSolution = solve(cList, solution);
    			if(tempSolution == null)
    			{
    				// False didn't work either
    				// Formula is unsolvable!!
    				return null;
    			}
    		}
    		solution = tempSolution;
    	}
    	return solution;
    }

    /**
     * given a clause list and literal, produce a new list resulting from
     * setting that literal to true
     * 
     * @param clauses
     *            , a list of clauses
     * @param l
     *            , a literal to set to true
     * @return a new list of clauses resulting from setting l to true
     */
    private static ImList<Clause> substitute(ImList<Clause> clauses, Literal l) {
    	// We use the following two boolean identities:
    	// A OR TRUE  = TRUE
    	// A OR FALSE = A
    	// Clauses are a list of "ORed" literals.
    	//  So if Literal l is positive (i.e. v331), substituting makes it TRUE which makes the whole clause TRUE.
    	// And if Literal l is negative (i.e. ~v331),substituting makes it FALSE and it is simply removed from the clause.

    	// For example in the clause (v331, ~v344, ~v313),
    	// substituting  v331 (a positive literal) makes (TRUE, ~v344, ~v313) or just TRUE. (TRUE clauses can be ignored.)
    	// Substituting ~v344 (a negative literal) makes (FALSE, v331, ~v313) or simply (v331, ~v313).

        Iterator<Clause> clauseItor = clauses.iterator();
        ImList<Clause> newList = new EmptyImList<Clause>();
        while(clauseItor.hasNext())
        {
        	// Here we use another boolean identity:
        	// A AND TRUE = A
        	// Now, Formulas are a list of "ANDed" clauses.
        	// So if a substituted Clause becomes TRUE, it can be removed from the formula by that identity.
        	// For example in the formula (x1, x2, x3),
        	// If calling reduce() on x1 makes it TRUE, we can simplify the formula to (x2, x3).
        	// reduce() returns null if the clause is TRUE, so we only keep the clause (add it to newList) if newClause != null.
        	Clause newClause = clauseItor.next().reduce(l);
        	if(newClause != null)
            {
            	newList = newList.add(newClause);
            }
        	
        }
//        System.out.println(newList);
        return newList;
    }

}
