package trefoil2;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * An expression AST. See LANGUAGE.md for a list of possibilities.
 */
@Data
public abstract class Expression {
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class IntegerLiteral extends Expression {
        private final int data;

        @Override
        public String toString() {
            return Integer.toString(data);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class BooleanLiteral extends Expression {
        private final boolean data;

        @Override
        public String toString() {
            return Boolean.toString(data);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class VariableReference extends Expression {
        private final String varname;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Plus extends Expression {
        private final Expression left, right;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Minus extends Expression {
        private final Expression left, right;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Times extends Expression {
        private final Expression left, right;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Equals extends Expression {
        private final Expression left, right;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ifStatement extends Expression {
        private final Expression condition, trueStatement, falseStatement;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class let extends Expression {
        private final Expression temporalEnvironment, expression;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class letChildren1 extends Expression {
        private final String localVariable;
        private final Expression expression;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Nil extends Expression {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Isnil extends Expression {
        private final Expression expression;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Cons extends Expression {
        private final Expression expression1, expression2;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class IsCons extends Expression {
        private final Expression expression;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class car extends Expression {
        private final Expression expression;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class cdr extends Expression {
        private final Expression expression;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class FunctionReference extends Expression {
        private final String funname;
        private final List<Expression> args;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Findmax extends Expression {
        private final List<Expression> args;
    }

    // Convenience factory methods
    public static IntegerLiteral ofInt(int x) {
        return new IntegerLiteral(x);
    }
    public static BooleanLiteral ofBoolean(boolean b) {
        return new BooleanLiteral(b);
    }
    public static Expression nil() {
        return new Nil();
    }
    public static Expression cons(Expression e1, Expression e2) {
        return new Cons(e1, e2);
    }

    /**
     * Tries to convert a PST to an Expression.
     *
     * See LANGUAGE.md for a description of how this should work at a high level.
     *
     * If conversion fails, throws TrefoilError.AbstractSyntaxError with a nice message.
     */
    public static Expression parsePST(ParenthesizedSymbolTree pst) {
        // Either the PST is a Symbol or a Node
        if (pst instanceof ParenthesizedSymbolTree.Symbol) {
            // If it is a symbol, it is either a number, a symbol keyword, or a variable reference.
            ParenthesizedSymbolTree.Symbol symbol = (ParenthesizedSymbolTree.Symbol) pst;
            String s = symbol.getSymbol();
            try {
                return Expression.ofInt(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                switch (s) {
                    case "true":
                        return new BooleanLiteral(true);
                    case "false":
                        return new BooleanLiteral(false);
                    case "nil":
                        return new Nil();

                    // if the symbol is not a symbol keyword, then it represents a variable reference
                    default:
                        return new VariableReference(s);
                }
            }
        } else {
            // Otherwise it is a Node, in which case it might be a built-in form with a node keyword,
            // or if not, then it is a function call.
            ParenthesizedSymbolTree.Node n = (ParenthesizedSymbolTree.Node) pst;
            List<ParenthesizedSymbolTree> children = n.getChildren();
            if (children.size() == 0) {
                throw new Trefoil2.TrefoilError.AbstractSyntaxError("Unexpected empty pair of parentheses.");
            }
            String head = ((ParenthesizedSymbolTree.Symbol) children.get(0)).getSymbol();
            switch (head) {
                case "+":
                    if (children.size() - 1 /* -1 for head */ != 2) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects 2 arguments");
                    }
                    return new Plus(parsePST(children.get(1)), parsePST(children.get(2)));
                case "-":
                    if (children.size() - 1 /* -1 for head */ != 2) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects 2 arguments");
                    }
                    return new Minus(parsePST(children.get(1)), parsePST(children.get(2)));
                case "*":
                    if (children.size() - 1 /* -1 for head */ != 2) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects 2 arguments");
                    }
                    return new Times(parsePST(children.get(1)), parsePST(children.get(2)));
                case "=":
                    if (children.size() - 1 /* -1 for head */ != 2) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects 2 arguments");
                    }
                    return new Equals(parsePST(children.get(1)), parsePST(children.get(2)));
                case "if":
                    if (children.size() - 1 /* -1 for head */ != 3) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects 3 arguments");
                    }
                    return new ifStatement(parsePST(children.get(1)), parsePST(children.get(2)), parsePST(children.get(3)));
                case "let":
                    if (children.size() - 1 /* -1 for head */ != 2) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects 2 arguments");
                    }
                    List<ParenthesizedSymbolTree> letchildren_unprocessed = ((ParenthesizedSymbolTree.Node) children.get(1)).getChildren();
                    if (letchildren_unprocessed.size() != 1) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator () expects 1 arguments");
                    }
                    List<ParenthesizedSymbolTree> letchildren_processed = ((ParenthesizedSymbolTree.Node) letchildren_unprocessed.get(0)).getChildren();
                    if (letchildren_processed.size() != 2) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator () expects 2 arguments");
                    }
                    String localvar = ((ParenthesizedSymbolTree.Symbol) (letchildren_processed.get(0))).getSymbol();

                    return new let(new letChildren1(localvar, parsePST(letchildren_processed.get(1))), parsePST(children.get(2)));
                case "nil?":
                    if (children.size() - 1 /* -1 for head */ != 1) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects 1 arguments");
                    }
                    return new Isnil(parsePST(children.get(1)));
                case "cons":
                    if (children.size() - 1 /* -1 for head */ != 2) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects 2 arguments");
                    }
                    return new Cons(parsePST(children.get(1)), parsePST(children.get(2)));
                case "cons?":
                    if (children.size() - 1 /* -1 for head */ != 1) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects 1 arguments");
                    }
                    return new IsCons(parsePST(children.get(1)));
                case "car":
                    if (children.size() - 1 /* -1 for head */ != 1) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects 1 arguments");
                    }
                    return new car(parsePST(children.get(1)));
                case "cdr":
                    if (children.size() - 1 /* -1 for head */ != 1) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects 1 arguments");
                    }
                    return new cdr(parsePST(children.get(1)));
                case "max":
                    if (children.size() - 1 /* -1 for head */ == 0) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError("Operator " + head + " expects at least 1 arguments");
                    }
                    List<Expression> list = new ArrayList<>();
                    for (int i = 1; i < children.size(); i++) {
                        list.add(parsePST(children.get(i)));
                    }
                    return new Findmax(list);

                // if the symbol is not a node keyword, then it represents a function call
                default:
                    // eventually we will add function calls here
//                    throw new Trefoil2.TrefoilError.AbstractSyntaxError("Unrecognized operator " + head);
                    if (!(parsePST(children.get(0)) instanceof VariableReference)) {
                        throw new Trefoil2.TrefoilError.AbstractSyntaxError(head + " should be a function type but not");
                    }
                    String funname = ((VariableReference)parsePST(children.get(0))).getVarname();
                    List<Expression> args = new ArrayList<>();
                    for (int i = 1; i < children.size(); i++) {
                        args.add(parsePST(children.get(i)));
                    }
                    return new FunctionReference(funname, args);
            }
        }
    }

    // Convenience factory method for unit tests.
    public static Expression parseString(String s) {
        return parsePST(ParenthesizedSymbolTree.parseString(s));
    }
}

