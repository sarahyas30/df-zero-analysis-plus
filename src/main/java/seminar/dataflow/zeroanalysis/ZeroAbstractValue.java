package seminar.dataflow.zeroanalysis;

/**
 * This enum represents the possible values of the zero analysis for a variable.
 * This is the extended lattice: besides -, 0 and +, it can say that a variable is <= 0, >= 0 or != 0.
 */
public enum ZeroAbstractValue {

    /**
     * We don't have information about the variable.
     */
    BOTTOM("bottom"),

    /**
     * The variable is negative (< 0).
     */
    NEGATIVE("negative"),

    /**
     * The variable is zero.
     */
    ZERO("zero"),

    /**
     * The variable is positive (> 0).
     */
    POSITIVE("positive"),

    /**
     * The variable is negative or zero (<= 0).
     */
    NON_POSITIVE("non-positive"),

    /**
     * The variable is positive or zero (>= 0).
     */
    NON_NEGATIVE("non-negative"),

    /**
     * The variable is not zero (< 0 or > 0).
     */
    NOT_ZERO("not-zero"),

    /**
     * The variable is top (can be any value).
     */
    TOP("top");


    /**
     * The name of the ZeroAbstractValue.
     */
    private final String name;

    @Override
    public String toString() {
        return this.name;
    }

    ZeroAbstractValue(String name) {
        this.name = name;
    }

    /**
     * Returns the result of the addition between this ZeroAbstractValue and another.
     * @param another the other ZeroAbstractValue.
     * @return the result of the addition.
     */
    public ZeroAbstractValue add(ZeroAbstractValue another) {
        if (this == BOTTOM || another == BOTTOM) { return BOTTOM; }
        if (this == TOP || another == TOP) { return TOP; }
        if (this == ZERO) return another;
        if (another == ZERO) return this;
        if (this == another && this != NOT_ZERO) return this;
        if (this == NEGATIVE && another == NON_POSITIVE || this == NON_POSITIVE && another == NEGATIVE) { return NEGATIVE; }
        if (this == POSITIVE && another == NON_NEGATIVE || this == NON_NEGATIVE && another == POSITIVE) { return POSITIVE ;}
        return TOP;
    }

    /**
     * Returns the result of the division between this ZeroAbstractValue and another.
     * @param another the other ZeroAbstractValue.
     * @return the result of the division.
     */
    public ZeroAbstractValue divideBy(ZeroAbstractValue another) {
        if (this == BOTTOM || another == BOTTOM || another == ZERO) { return BOTTOM; }
        if (this == ZERO) return ZERO;
        if (this == TOP || another == TOP || this == NOT_ZERO || another == NOT_ZERO) { return TOP; }
        boolean thisNeg = this == NEGATIVE || this == NON_POSITIVE;
        boolean anotherNeg = another == NEGATIVE || another == NON_POSITIVE;
        if (thisNeg == anotherNeg) { return NON_NEGATIVE; }
        return NON_POSITIVE;
    }

    /**
     * Returns the result of the multiplication between this ZeroAbstractValue and another.
     * @param another the other ZeroAbstractValue.
     * @return the result of the multiplication.
     */
    public ZeroAbstractValue multiplyBy(ZeroAbstractValue another) {
        if (this == BOTTOM || another == BOTTOM) return BOTTOM;
        if (this == ZERO || another == ZERO) return ZERO;
        if (this == TOP || another == TOP) return TOP;
        if (this == another){
            if (this == NEGATIVE || this == POSITIVE) return POSITIVE;
            if (this == NOT_ZERO) return NOT_ZERO;
            return NON_NEGATIVE;
        }
        if (this == POSITIVE) return another;
        if (another == POSITIVE) return this;
        if ((this == NOT_ZERO && another == NEGATIVE) || (this == NEGATIVE && another == NOT_ZERO)) return NOT_ZERO;
        if ((this == NON_NEGATIVE && another != NOT_ZERO) || (this != NOT_ZERO && another == NON_NEGATIVE)) return NON_POSITIVE;
        if (this == NEGATIVE || another == NEGATIVE) return NON_NEGATIVE;
        return TOP;
    }

    /**
     * Returns the result of the subtraction between this ZeroAbstractValue and another.
     * @param another the other ZeroAbstractValue.
     * @return the result of the subtraction.
     */
    public ZeroAbstractValue substract(ZeroAbstractValue another) {
        if (this == BOTTOM || another == BOTTOM) { return BOTTOM; }
        if (this == TOP || another == TOP) { return TOP; }
        if (another == ZERO) { return this; }
        if (this == ZERO) {
            if (another == NEGATIVE) return POSITIVE;
            if (another == POSITIVE) return NEGATIVE;
            if (another == NON_NEGATIVE) return NON_POSITIVE;
            if (another == NON_POSITIVE) return NON_NEGATIVE;
            return NOT_ZERO;
        }
        if (this == POSITIVE && (another == NEGATIVE || another == NON_POSITIVE)) return POSITIVE;
        if (this == NEGATIVE && (another == POSITIVE || another == NON_NEGATIVE)) return NEGATIVE;
        if (this == NON_POSITIVE) {
            if (another == POSITIVE) return NEGATIVE;
            if (another == NON_NEGATIVE) return NON_POSITIVE;
            return TOP;
        }
        if (this == NON_NEGATIVE) {
            if (another == NEGATIVE) return POSITIVE;
            if (another == NON_POSITIVE) return NON_NEGATIVE;
            return TOP;
        }
        return TOP;
    }

    /**
     * Returns the result of the merge between this ZeroAbstractValue and another.
     * @param another the other ZeroAbstractValue.
     * @return the result of the merge.
     */
    public ZeroAbstractValue merge(ZeroAbstractValue another) {
        if (this == TOP || another == TOP) { return TOP; }
        if (this == BOTTOM) { return another; }
        if (another == BOTTOM) { return this; }
        if (this == another) { return this; }
        if (this == NOT_ZERO) {
            if (another == NEGATIVE || another == POSITIVE){ return NOT_ZERO; }
            else { return TOP; }
        }
        if (another == NOT_ZERO) {
            if (this == NEGATIVE || this == POSITIVE){ return NOT_ZERO; }
            else { return TOP; }
        }
        if (this == NEGATIVE && another == POSITIVE || this == POSITIVE && another == NEGATIVE) { return NOT_ZERO; }
        if (this == ZERO && (another == NEGATIVE || another == NON_POSITIVE)) { return NON_POSITIVE; }
        if (another == ZERO && (this == NEGATIVE || this == NON_POSITIVE)) { return NON_POSITIVE; }
        if (this == ZERO && (another == POSITIVE || another == NON_NEGATIVE)) { return NON_NEGATIVE; }
        if (another == ZERO && (this == POSITIVE || this == NON_NEGATIVE)) { return NON_NEGATIVE; }
        if ((this == NEGATIVE && another == NON_POSITIVE) || (another == NEGATIVE && this == NON_POSITIVE)) { return NON_POSITIVE; }
        if ((this == POSITIVE && another == NON_NEGATIVE) || (another == POSITIVE && this == NON_NEGATIVE)) { return NON_NEGATIVE; }
        return TOP;
    }
}
