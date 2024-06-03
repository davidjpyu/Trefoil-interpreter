package trefoil2;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interprets expressions and bindings in the context of a dynamic environment
 * according to the semantics of Trefoil v2.
 */
public class Interpreter {
    /**
     * Evaluates e in the given environment. Returns the resulting value.
     *
     * Throws TrefoilError.RuntimeError when the Trefoil programmer makes a mistake.
     */
    public static Expression interpretExpression(Expression e, DynamicEnvironment environment) {
        if (e instanceof Expression.IntegerLiteral) {
            return e;
        } else if (e instanceof Expression.VariableReference) {
            Expression.VariableReference var = (Expression.VariableReference) e;
            return environment.getVariable(var.getVarname());
        } else if (e instanceof Expression.Plus) {
            Expression.Plus p = (Expression.Plus) e;
            Expression v1 = interpretExpression(p.getLeft(), environment);
            Expression v2 = interpretExpression(p.getRight(), environment);
            if (!((v1 instanceof Expression.IntegerLiteral) && (v2 instanceof Expression.IntegerLiteral))){
                throw new Trefoil2.TrefoilError.RuntimeError("two arguments of plus expression are not all integers");
            }

            return new Expression.IntegerLiteral(
                    ((Expression.IntegerLiteral) v1).getData() +
                            ((Expression.IntegerLiteral) v2).getData()
            );

        } else if (e instanceof Expression.Minus) {
            Expression.Minus p = (Expression.Minus) e;
            Expression v1 = interpretExpression(p.getLeft(), environment);
            Expression v2 = interpretExpression(p.getRight(), environment);

            if (!((v1 instanceof Expression.IntegerLiteral) && (v2 instanceof Expression.IntegerLiteral))){
                throw new Trefoil2.TrefoilError.RuntimeError("two arguments of minus expression are not all integers");
            }
            return new Expression.IntegerLiteral(
                    ((Expression.IntegerLiteral) v1).getData() -
                            ((Expression.IntegerLiteral) v2).getData()
            );
        } else if (e instanceof Expression.Times) {
            Expression.Times p = (Expression.Times) e;
            Expression v1 = interpretExpression(p.getLeft(), environment);
            Expression v2 = interpretExpression(p.getRight(), environment);

            if (!((v1 instanceof Expression.IntegerLiteral) && (v2 instanceof Expression.IntegerLiteral))){
                throw new Trefoil2.TrefoilError.RuntimeError("two arguments of minus expression are not all integers");
            }

            return new Expression.IntegerLiteral(
                    ((Expression.IntegerLiteral) v1).getData() *
                            ((Expression.IntegerLiteral) v2).getData()
            );
        } else if (e instanceof Expression.Equals) {
            Expression.Equals p = (Expression.Equals) e;
            Expression v1 = interpretExpression(p.getLeft(), environment);
            Expression v2 = interpretExpression(p.getRight(), environment);

            if (!((v1 instanceof Expression.IntegerLiteral) && (v2 instanceof Expression.IntegerLiteral))){
                throw new Trefoil2.TrefoilError.RuntimeError("two arguments of equal expression are not all integers");
            }
            if (((Expression.IntegerLiteral) v1).getData() == ((Expression.IntegerLiteral) v2).getData()) {
                return new Expression.BooleanLiteral(true);
            }
            return new Expression.BooleanLiteral(false);
        } else if (e instanceof Expression.ifStatement) {
            Expression.ifStatement p = (Expression.ifStatement) e;
            Expression condition = interpretExpression(p.getCondition(), environment);
            if (condition instanceof Expression.BooleanLiteral && !((Expression.BooleanLiteral) condition).isData()) {
                return interpretExpression(p.getFalseStatement(), environment);
            } else {
                return interpretExpression(p.getTrueStatement(), environment);
            }
        } else if (e instanceof Expression.let) {
            Expression.let p = (Expression.let) e;
            if (!(p.getTemporalEnvironment() instanceof Expression.letChildren1)) {
                throw new Trefoil2.TrefoilError.AbstractSyntaxError("first argument of let expression is not a definition");
            }
            Expression.letChildren1 f = (Expression.letChildren1)p.getTemporalEnvironment();
            String localVariable = f.getLocalVariable();
            Expression v1 = interpretExpression(f.getExpression(), environment);
            DynamicEnvironment newEnvironment = environment.extendVariable(localVariable, v1);

            return interpretExpression(p.getExpression(), newEnvironment);
        } else if (e instanceof Expression.BooleanLiteral) {
            return e;
        } else if (e instanceof Expression.Nil) {
            return e;
        } else if (e instanceof Expression.Isnil) {
            Expression.Isnil p = (Expression.Isnil) e;
            Expression val = interpretExpression(p.getExpression(), environment);
            if (val instanceof Expression.Nil) {
                return new Expression.BooleanLiteral(true);
            } else {
                return new Expression.BooleanLiteral(false);
            }
        } else if (e instanceof Expression.Cons) {
            Expression.Cons p = (Expression.Cons) e;
            Expression val1 = interpretExpression(p.getExpression1(), environment);
            Expression val2 = interpretExpression(p.getExpression2(), environment);
            return new Expression.Cons(val1, val2);
        } else if (e instanceof Expression.IsCons) {
            Expression.IsCons p = (Expression.IsCons) e;
            Expression val = interpretExpression(p.getExpression(), environment);
            if (val instanceof Expression.Cons) {
                return new Expression.BooleanLiteral(true);
            } else {
                return new Expression.BooleanLiteral(false);
            }
        } else if (e instanceof Expression.car) {
            Expression.car p = (Expression.car) e;
            Expression val = interpretExpression(p.getExpression(), environment);
            if (val instanceof Expression.Cons) {
                return ((Expression.Cons) val).getExpression1();
            } else {
                throw new Trefoil2.TrefoilError.RuntimeError("car argument is not (cons arg1 arg2)");
            }
        } else if (e instanceof Expression.cdr) {
            Expression.cdr p = (Expression.cdr) e;
            Expression val = interpretExpression(p.getExpression(), environment);
            if (val instanceof Expression.Cons) {
                return ((Expression.Cons) val).getExpression2();
            } else {
                throw new Trefoil2.TrefoilError.RuntimeError("cdr argument is not (cons arg1 arg2)");
            }
        } else if (e instanceof Expression.FunctionReference) {
            DynamicEnvironment callenv = environment;
            Expression.FunctionReference func = (Expression.FunctionReference) e;
            DynamicEnvironment defenv = environment.getFunction(func.getFunname()).definingEnvironment;
            Binding.FunctionBinding funcBind = environment.getFunction(func.getFunname()).functionBinding;
            if (funcBind.getArgnames().size() != func.getArgs().size()) {
                throw new Trefoil2.TrefoilError.RuntimeError("function " + func.getFunname() + "has incompatible number of parameters");
            }
            List<Expression> vals = new ArrayList<>();
            for (int i = 0; i < func.getArgs().size(); i++) {
                vals.add(interpretExpression(func.getArgs().get(i), callenv));
            }
            callenv = defenv.extendVariables(funcBind.getArgnames(), vals);
            return interpretExpression(funcBind.getBody(), callenv);
        } else if (e instanceof  Expression.Findmax) {
            Expression.Findmax p = (Expression.Findmax) e;

            Expression val = interpretExpression(p.getArgs().get(0));
            if (!(val instanceof Expression.IntegerLiteral)) {
                throw new Trefoil2.TrefoilError.RuntimeError("max arguments fail to be compiled as integers");
            }
            int maxVal = ((Expression.IntegerLiteral) val).getData();

            for (int i = 1; i < p.getArgs().size(); i++) {
                val = interpretExpression(p.getArgs().get(i));
                if (!(val instanceof Expression.IntegerLiteral)) {
                    throw new Trefoil2.TrefoilError.RuntimeError("max arguments fail to be compiled as integers");
                }
                if (maxVal < ((Expression.IntegerLiteral) val).getData()) {
                    maxVal = ((Expression.IntegerLiteral) val).getData();
                }
            }
            return Expression.ofInt(maxVal);
        } else {
            // Otherwise it's an expression AST node we don't recognize. Tell the interpreter implementor.
            throw new Trefoil2.InternalInterpreterError("\"impossible\" expression AST node " + e.getClass());
        }
    }

    /**
     * Executes the binding in the given environment, returning the new environment.
     *
     * The environment passed in as an argument is *not* mutated. Instead, it is copied
     * and any modifications are made on the copy and returned.
     *
     * Throws TrefoilError.RuntimeError when the Trefoil programmer makes a mistake.
     */
    public static DynamicEnvironment interpretBinding(Binding b, DynamicEnvironment environment) {
        if (b instanceof Binding.VariableBinding) {
            Binding.VariableBinding vb = (Binding.VariableBinding) b;
            Expression value = interpretExpression(vb.getVardef(), environment);
            System.out.println(vb.getVarname() + " = " + value);
            return environment.extendVariable(vb.getVarname(), value);
        } else if (b instanceof Binding.TopLevelExpression) {
            Binding.TopLevelExpression tle = (Binding.TopLevelExpression) b;
            System.out.println(interpretExpression(tle.getExpression(), environment));
            return environment;
        } else if (b instanceof Binding.FunctionBinding) {
            Binding.FunctionBinding fb = (Binding.FunctionBinding) b;
            DynamicEnvironment newEnvironment = environment.extendFunction(fb.getFunname(), fb);
            System.out.println(fb.getFunname() + " is defined");
            return newEnvironment;
        } else if (b instanceof Binding.TestBinding) {
            Binding.TestBinding tb = (Binding.TestBinding) b;
            if(interpretExpression(tb.getExpression(), environment) instanceof Expression.BooleanLiteral
                    && ((Expression.BooleanLiteral) interpretExpression(tb.getExpression(), environment)).isData()) {
                return environment;
            } else {
                throw new Trefoil2.TrefoilError.RuntimeError("test fails");
            }
        }


        // Otherwise it's a binding AST node we don't recognize. Tell the interpreter implementor.
        throw new Trefoil2.InternalInterpreterError("\"impossible\" binding AST node " + b.getClass());
    }


    // Convenience methods for interpreting in the empty environment.
    // Used for testing.
    public static Expression interpretExpression(Expression e) {
        return interpretExpression(e, new DynamicEnvironment());
    }
    public static DynamicEnvironment interpretBinding(Binding b) {
        return interpretBinding(b, DynamicEnvironment.empty());
    }

    /**
     * Represents the dynamic environment, which is a mapping from strings to "entries".
     * In the starter code, the string always represents a variable name and an entry is always a VariableEntry.
     * You will extend it to also support function names and FunctionEntries.
     */
    @Data
    public static class DynamicEnvironment {
        public static abstract class Entry {
            @EqualsAndHashCode(callSuper = false)
            @Data
            public static class VariableEntry extends Entry {
                private final Expression value;
            }

            @EqualsAndHashCode(callSuper = false)
            @Data
            public static class FunctionEntry extends Entry {
                private final Binding.FunctionBinding functionBinding;

                @ToString.Exclude
                private final DynamicEnvironment definingEnvironment;
            }

            // Convenience factory methods

            public static Entry variable(Expression value) {
                return new VariableEntry(value);
            }
            public static Entry function(Binding.FunctionBinding functionBinding, DynamicEnvironment definingEnvironment) {
                return new FunctionEntry(functionBinding, definingEnvironment);
            }
        }

        // The backing map of this dynamic environment.
        private final Map<String, Entry> map;

        public DynamicEnvironment() {
            this.map = new HashMap<>();
        }

        public DynamicEnvironment(DynamicEnvironment other) {
            this.map = new HashMap<>(other.getMap());
        }

        private boolean containsVariable(String varname) {
            return map.containsKey(varname) && map.get(varname) instanceof Entry.VariableEntry;
        }

        public Expression getVariable(String varname) {
            if (!containsVariable(varname)) {
                throw new Trefoil2.TrefoilError.RuntimeError("the variable " + varname + " is unbounded");
            }

            // Hint: first, read the code for containsVariable().
            // Hint: you will likely need the value field from Entry.VariableEntr
            return ((Entry.VariableEntry) map.get(varname)).getValue();
        }

        public void putVariable(String varname, Expression value) {
            // Hint: map.put
            // Hint: either call new Entry.VariableEntry or the factory Entry.variable
            Expression val = Interpreter.interpretExpression(value, this);
            System.out.println(varname + " = " + val.toString());
            map.put(varname, Entry.variable(val));
        }

        /**
         * Returns a *new* DynamicEnvironment extended by the binding varname -> value.
         *
         * Does not change this! Creates a copy.
         */
        public DynamicEnvironment extendVariable(String varname, Expression value) {
            DynamicEnvironment newEnv = new DynamicEnvironment(this);  // create a copy
            newEnv.putVariable(varname, value);  // mutate the copy
            return newEnv;  // return the mutated copy (this remains unchanged!)
        }

        /**
         * Returns a *new* Dynamic environment extended by the given mappings.
         *
         * Does not change this! Creates a copy.
         *
         * varnames and values must have the same length
         *
         * @param varnames variable names to bind
         * @param values values to bind the variables to
         */
        public DynamicEnvironment extendVariables(List<String> varnames, List<Expression> values) {
            DynamicEnvironment newEnv = new DynamicEnvironment(this);
            assert varnames.size() == values.size();
            for (int i = 0; i < varnames.size(); i++) {
                newEnv.putVariable(varnames.get(i), values.get(i));
            }
            return newEnv;
        }

        private boolean containsFunction(String funname) {
            return map.containsKey(funname) && map.get(funname) instanceof Entry.FunctionEntry;
        }

        public Entry.FunctionEntry getFunction(String funname) {
            if (!containsFunction(funname)) {
                throw new Trefoil2.TrefoilError.RuntimeError("function " + funname + " is unbounded");
            }

            // Hint: first, read the code for containsFunction().
            return (Entry.FunctionEntry) map.get(funname);
        }

        public void putFunction(String funname, Binding.FunctionBinding functionBinding) {
            // Be careful to set up recursion correctly!
            // Hint: Pass definingEnvironment=this to the Entry.function factory, and then call map.put.
            //       That way, by the time Trefoil calls the function, everything points to
            //       the right place. Tricky!
            List<String>list = functionBinding.getArgnames();
            for (int i = 0; i < list.size(); i++) {
                for (int j = i+1; j < list.size(); j++) {
                    if (list.get(i).equals(list.get(j))) {
                        throw new Trefoil2.TrefoilError.RuntimeError("parameter name "+ list.get(i)+" duplicated");
                    }
                }
            }
            map.put(funname, Entry.function(functionBinding, this));
        }

        public DynamicEnvironment extendFunction(String funname, Binding.FunctionBinding functionBinding) {
            DynamicEnvironment newEnv = new DynamicEnvironment(this);  // create a copy of this
            newEnv.putFunction(funname, functionBinding);  // mutate the copy
            return newEnv;  // return the copy
        }

        // Convenience factory methods

        public static DynamicEnvironment empty() {
            return new DynamicEnvironment();
        }

        public static DynamicEnvironment singleton(String varname, Expression value) {
            return empty().extendVariable(varname, value);
        }
    }
}

