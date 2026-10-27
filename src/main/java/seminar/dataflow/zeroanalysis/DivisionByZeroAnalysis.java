package seminar.dataflow.zeroanalysis;

import pascal.taie.analysis.dataflow.analysis.AbstractDataflowAnalysis;
import pascal.taie.analysis.dataflow.fact.DataflowResult;
import pascal.taie.analysis.dataflow.solver.Solver;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.DefinitionStmt;
import pascal.taie.ir.stmt.Stmt;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a may forward dataflow analysis that detects if a division by zero is possible in a given
 * control flow graph.
 */
public class DivisionByZeroAnalysis extends AbstractDataflowAnalysis<Stmt, ZeroAbstractState> {

    /**
     * This map contains the statements that are possibly offending (i.e., that may contain a division by zero).
     */
    private final HashMap<Stmt, Boolean> possibleDivisionByZero = new HashMap<>();

    /**
     * The IN and OUT states computed for each statement.
     */
    private final DataflowResult<Stmt, ZeroAbstractState> result;

    public DivisionByZeroAnalysis(CFG<Stmt> cfg) {
        super(cfg);
        // Performs the analysis and populates the possibleDivisionByZero map
        this.result = Solver.<Stmt, ZeroAbstractState>getSolver().solve(this);
    }

    @Override
    public boolean isForward() {
        return true;
    }

    /**
     * The state at the entry of the method: the value each parameter holds when the method starts.
     * @return the state at the entry of the method.
     */
    @Override
    public ZeroAbstractState newBoundaryFact() {
        ZeroAbstractState state = new ZeroAbstractState();
        for (Var parameter : cfg.getIR().getParams()) {
            ZeroValueVisitor visitor = new ZeroValueVisitor(state);
            state.setValue(parameter.getName(), visitor.visitParameterDefinition(parameter).done());
        }
        return state;
    }

    /**
     * The initial state of every other statement: no variable has a value yet (BOTTOM).
     * @return an empty state.
     */
    @Override
    public ZeroAbstractState newInitialFact() {
        return new ZeroAbstractState();
    }

    /**
     * This method is called for each statement in the control flow graph.
     * @param stmt the current node
     * @param in the input state
     * @param out the output state, updated by this method
     * @return true if the output state changed
     */
    @Override
    public boolean transferNode(Stmt stmt, ZeroAbstractState in, ZeroAbstractState out) {
        // Load all values from IN state into the new OUT state.
        ZeroAbstractState newOut = new ZeroAbstractState();
        newOut.copy(in);

        // Check if the statement assigns a value to a variable.
        if (stmt instanceof DefinitionStmt && ((DefinitionStmt<?, ?>) stmt).getLValue() instanceof Var) {
            DefinitionStmt<?, ?> definition = (DefinitionStmt<?, ?>) stmt;
            Var variable = (Var) definition.getLValue();

            // Create a visitor for the right-hand side of the assignment.
            ZeroValueVisitor visitor = new ZeroValueVisitor(in);
            ZeroAbstractValue resolvedValue = visitor.visit(definition.getRValue()).done();

            if (visitor.getPossibleDivisionByZero()) {
                possibleDivisionByZero.put(stmt, true);
            }

            // Set the ZeroAbstractValue in the OUT state for the variable being assigned.
            newOut.setValue(variable.getName(), resolvedValue);
        }

        boolean changed = !newOut.equals(out);
        out.clear();
        out.copy(newOut);
        return changed;
    }

    /**
     * This method merges a predecessor's OUT state into the IN state being built for a statement.
     * @param fact the OUT state of a predecessor
     * @param target the IN state being built, updated by this method
     */
    @Override
    public void meetInto(ZeroAbstractState fact, ZeroAbstractState target) {
        ZeroAbstractState merged = target.union(fact);
        target.clear();
        target.copy(merged);
    }

    /**
     * This method returns true if the given statement is possibly offending (i.e., that may contain a division by zero).
     * @param stmt the statement to check.
     * @return true if the given statement is possibly offending (i.e., that may contain a division by zero).
     */
    public boolean stmtIsOffending(Stmt stmt) {
        return possibleDivisionByZero.getOrDefault(stmt, false);
    }

    /**
     * This method returns a dictionary with all expressions that have a possible division by zero.
     * The key for each entry is the line number of the expression.
     * @return a dictionary with all expressions that have a possible division by zero.
     */
    public Map<Integer, String> getPossibleDivisionByZeroExpressions() {
        Map<Integer, String> expressions = new HashMap<>();

        for (Stmt stmt : this.possibleDivisionByZero.keySet()) {
            if (this.possibleDivisionByZero.get(stmt)) {
                expressions.put(stmt.getLineNumber(), stmt.toString());
            }
        }

        return expressions;
    }

    /**
     * This method returns the IN state of the first statement of the given line.
     * @param lineNumber the line number to check.
     * @return the IN state for the given line number, or null if no statement is on that line.
     */
    public ZeroAbstractState getINSetForLineNumber(int lineNumber) {
        for (Stmt stmt : cfg.getIR()) {
            if (stmt.getLineNumber() == lineNumber) {
                return result.getInFact(stmt);
            }
        }

        return null;
    }

    /**
     * This method returns the IN state of the given statement.
     * @param stmt the statement.
     * @return the IN state of the statement.
     */
    public ZeroAbstractState getINSet(Stmt stmt) {
        return result.getInFact(stmt);
    }
}
