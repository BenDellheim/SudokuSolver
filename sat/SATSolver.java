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
     * unit propagation. The returned environment binds literals of class
     * bool.Variable rather than the special literals used in clausification of
     * class Literal, so that clients can more readily use it.
     * 
     * @return an environment for which the problem evaluates to Bool.TRUE, or
     *         null if no such environment exists.
     */
    public static Environment solve(Formula formula) {
    	// If there are no clauses, the formula is trivially solvable
    	ImList<Clause> clauseList = formula.getClauses();
    	if(clauseList.size() == 0) return new Environment();

    	// If there is an empty clause, the list is unsatisfiable - fail and backtrack (return null)
    	Iterator<Clause> eCheck = clauseList.iterator();
    	while(eCheck.hasNext())
    	{
    		if(eCheck.next().isEmpty()) return null;
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
    		solution = solution.putTrue(var);
    		ImList<Clause> cList = substitute(clauseList, lit);
    		solution = solve(cList, solution);
    	}
    	else
    	{
    		// No unit Clause
    		// Take the smallest one, try setting it to True and recurse
    		Literal arbLit = smallest.chooseLiteral();
    		ImList<Clause> cList = substitute(clauseList, arbLit);
    		solution = solve(cList, solution);
    		if(solution == null)
    		{
    			// Dead end when setting to True
    			// Try setting it to False!
    			cList = substitute(clauseList, arbLit.getNegation());
    			solution = solve(cList, solution);
    			if(solution == null)
    			{
    				// False didn't work either
    				// Time to backtrack
    				return null;
    			}
    		}
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
    private static Environment solve(ImList<Clause> clauses, Environment env) {
        // TODO: implement this.
        throw new RuntimeException("not yet implemented.");
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
    	// Clauses are a list of "ORed" literals.
    	// Considering the boolean identity A OR TRUE = TRUE,
    	// any clause with this Literal l (being set to true) will become simply, TRUE.
    	
        Iterator<Clause> clauseItor = clauses.iterator();
        ImList<Clause> newList = new EmptyImList<Clause>();
        while(clauseItor.hasNext())
        {
        	// Clause.reduce(Literal) will return null if setting the literal to true makes the clause true.
        	// Considering the boolean identity A AND TRUE = A,
        	// and that the ImList<Clause> is an "ANDed" list,
        	// a clause with just TRUE can be omitted. Thus, a check for if newClause is null.
        	Clause newClause = clauseItor.next().reduce(l);
        	if(newClause != null)
            {
            	newList = newList.add(newClause);
            }
        	
        }
        System.out.println(newList);
        return newList;
    }

}
