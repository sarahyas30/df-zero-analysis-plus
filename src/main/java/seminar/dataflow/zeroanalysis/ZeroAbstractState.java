package seminar.dataflow.zeroanalysis;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents a mapping of ZeroAbstractValues (BOTTOM, -, 0, +,  or TOP) to variable names.
 */
public class ZeroAbstractState {

    /**
     * This map contains the mapping of variable names to ZeroAbstractValues.
     */
    private final HashMap<String, ZeroAbstractValue> map;

    public ZeroAbstractState() {
        this.map = new HashMap<>();
    }

    /**
     * This method checks if the given variable has a value in the state.
     * @param variable the variable name.
     * @return true if the variable has a value, false otherwise.
     */
    public Boolean hasValue(String variable) {
        return this.map.containsKey(variable);
    }

    /**
     * This method returns the value of the given variable.
     * @param variable the variable name.
     * @return the value of the variable.
     */
    public ZeroAbstractValue getValue(String variable) {
        return this.map.get(variable);
    }

    /**
     * This method sets the value of the given variable.
     * @param variable the variable name.
     * @param value the value of the variable.
     */
    public void setValue(String variable, ZeroAbstractValue value) {
        if (value != null) {
            this.map.put(variable, value);
        }
    }

    /**
     * This method returns the defined variables in this mapping.
     * @return the defined variables in this mapping.
     */
    public Set<String> getDefinedVariables() {
        return this.map.keySet();
    }

    /**
     * This method returns the union of this state with another state.
     * Recall: state: Var -> {BOTTOM, -, 0, +, TOP}
     * @param another the other state.
     * @return the union of this state with another state.
     */
    public ZeroAbstractState union(ZeroAbstractState another) {
        ZeroAbstractState result = new ZeroAbstractState();
        for (String variable : this.getDefinedVariables()) {
            result.setValue(variable, this.getValue(variable));
        }
        for (String variable : another.getDefinedVariables()) {
            if (result.hasValue(variable)) {
                result.setValue(variable, result.getValue(variable).merge(another.getValue(variable)));
            } else {
                result.setValue(variable, another.getValue(variable));
            }
        }
        return result;
    }

    /**
     * Clears the State.
     */
    public void clear() {
        this.map.clear();
    }

    /**
     * Copies the values of another state into this state.
     * @param another the other state.
     */
    /**
     * Copies the values of another set into this set.
     * @param another the other set.
     */
    public void copy(ZeroAbstractState another) {
        this.map.putAll(another.map);
    }


    @Override
    public String toString() {
        return "ZeroAbstractState{" + this.map + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ZeroAbstractState)) return false;
        ZeroAbstractState that = (ZeroAbstractState) o;
        return Objects.equals(this.map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.map);
    }
}
