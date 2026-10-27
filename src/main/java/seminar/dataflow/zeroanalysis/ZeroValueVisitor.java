package seminar.dataflow.zeroanalysis;


import seminar.dataflow.util.visitor.AbstractValueVisitor;
import seminar.dataflow.util.visitor.ValueVisitor;
import pascal.taie.ir.exp.Var;

/**
 * This class implements a visitor that tries to deduce if the ZeroAbstractValue for an expression.
 */
public class ZeroValueVisitor extends AbstractValueVisitor<ZeroAbstractValue> {
    /**
     * The IN state that affect the expression being analyzed.
     */
    private final ZeroAbstractState state;

    /**
     * The resolved ZeroAbstractValue for the expression being analyzed.
     */
    private ZeroAbstractValue resolvedValue;

    /**
     * Indicates if there is a possible division by zero in the expression being analyzed.
     */
    private Boolean possibleDivisionByZero;

    public ZeroValueVisitor(ZeroAbstractState state) {
        this.state = state;
        this.possibleDivisionByZero = false;
    }

    /**
     * This method is called once per method parameter, to compute the value it holds when the method starts.
     * @param parameter the parameter.
     */
    @Override
    protected void visitParameter(Var parameter) {
        resolvedValue = ZeroAbstractValue.TOP;
    }

    /**
     * This method is called when the expression being analyzed is a local variable.
     * @param variable the local variable.
     */
    @Override
    // x = y 
    public void visitLocal(Var variable) {
        String name = variable.getName();
        if (state.hasValue(name)) {
            resolvedValue = state.getValue(name);
        } else {
            resolvedValue = ZeroAbstractValue.BOTTOM;
        }
    }

    /**
     * This method is called when the expression being analyzed is a division.
     * @param leftOperand the left operand of the division.
     * @param rightOperand the right operand of the division.
     */
    @Override
    public void visitDivExpression(ZeroAbstractValue leftOperand, ZeroAbstractValue rightOperand) {
        if (maybeZeroOperand(rightOperand)) {
            this.possibleDivisionByZero = true;
        }
        resolvedValue = leftOperand.divideBy(rightOperand);
    }

    /***
     * Returns true if the operand may be ZERO
     ***/
    private boolean maybeZeroOperand(ZeroAbstractValue operand)
    {
        return operand == ZeroAbstractValue.ZERO || operand == ZeroAbstractValue.TOP ||
                operand == ZeroAbstractValue.NON_NEGATIVE || operand == ZeroAbstractValue.NON_POSITIVE;
    }
    /**
     * This method is called when the expression being analyzed is a multiplication.
     * @param leftOperand the left operand of the multiplication.
     * @param rightOperand the right operand of the multiplication.
     */
    @Override
    public void visitMulExpression(ZeroAbstractValue leftOperand, ZeroAbstractValue rightOperand) {
        resolvedValue = leftOperand.multiplyBy(rightOperand);
    }


    /**
     * This method is called when the expression being analyzed is a subtraction.
     * @param leftOperand the left operand of the subtraction.
     * @param rightOperand the right operand of the subtraction.
     */
    @Override
    public void visitSubExpression(ZeroAbstractValue leftOperand, ZeroAbstractValue rightOperand) {
        resolvedValue = leftOperand.substract(rightOperand);
    }

    /**
     * This method is called when the expression being analyzed is an addition.
     * @param leftOperand the left operand of the addition.
     * @param rightOperand the right operand of the addition.
     */
    @Override
    public void visitAddExpression(ZeroAbstractValue leftOperand, ZeroAbstractValue rightOperand) {
        resolvedValue = leftOperand.add(rightOperand);
    }

    /**
     * This method is called when the expression being analyzed is an integer constant.
     * @param value the integer constant.
     */
    @Override
    public void visitIntegerConstant(int value) {
        if (value == 0){
            resolvedValue = ZeroAbstractValue.ZERO;
        } else if (value > 0) {
            resolvedValue = ZeroAbstractValue.POSITIVE;
        } else {
            resolvedValue = ZeroAbstractValue.NEGATIVE;
        }
    }

    /**
     * Method called after this visitor has finished analyzing the expression.
     * @return the ZeroAbstractValue for the expression.
     */
    @Override
    public ZeroAbstractValue done() {
        return resolvedValue;
    }

    @Override
    public ValueVisitor<ZeroAbstractValue> cloneVisitor() {
        return new ZeroValueVisitor(state);
    }

    /**
     * This method returns true if there is a possible division by zero in the expression being analyzed.
     * @return true if there is a possible division by zero in the expression being analyzed.
     */
    public Boolean getPossibleDivisionByZero() {
        return possibleDivisionByZero;
    }
}
