import org.junit.Test;
import trefoil2.*;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class Trefoil2Test {
    // ---------------------------------------------------------------------------------------------
    // Expression tests
    // ---------------------------------------------------------------------------------------------

    @Test
    public void testIntLit() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("3")));
    }

    @Test
    public void testIntLitNegative() {
        assertEquals(Expression.ofInt(-10),
                Interpreter.interpretExpression(Expression.parseString("-10")));
    }

    @Test
    public void testBoolLitTrue() {
        assertEquals(new Expression.BooleanLiteral(true),
                Interpreter.interpretExpression(Expression.parseString("true")));
    }

    @Test
    public void testBoolLitFalseParsing() {
        assertEquals(Expression.ofBoolean(false), Expression.parseString("false"));
    }

    @Test
    public void testBoolLitFalse() {
        assertEquals(new Expression.BooleanLiteral(false),
                Interpreter.interpretExpression(Expression.parseString("false")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testEmptyParen() {
        Interpreter.interpretExpression(Expression.parseString("()"));
    }

    @Test
    public void testVar() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("x"),
                        Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3))));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testVarNotFound() {
        Interpreter.interpretExpression(Expression.parseString("x"));
    }

    @Test
    public void testPlus() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("(+ 1 2)")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testPlusMissingOneArg() {
        Interpreter.interpretExpression(Expression.parseString("(+ 1)"));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testPlusTypeError() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("(+ 1 true)")));
    }

    @Test
    public void testMinus() {
        assertEquals(Expression.ofInt(-1),
                Interpreter.interpretExpression(Expression.parseString("(- 1 2)")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testMinusMissingOneArg() {
        Interpreter.interpretExpression(Expression.parseString("(- 1)"));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testMinusTypeError() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("(- 1 true)")));
    }

    @Test
    public void testTimes() {
        assertEquals(Expression.ofInt(6),
                Interpreter.interpretExpression(Expression.parseString("(* 2 3)")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testTimesMissingOneArg() {
        Interpreter.interpretExpression(Expression.parseString("(* 1)"));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testTimesTypeError() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("(* 1 true)")));
    }

    @Test
    public void testEqualsIntTrue() {
        assertEquals(Expression.ofBoolean(true),
                Interpreter.interpretExpression(Expression.parseString("(= 3 (+ 1 2))")));
    }

    @Test
    public void testEqualsIntFalse() {
        assertEquals(Expression.ofBoolean(false),
                Interpreter.interpretExpression(Expression.parseString("(= 4 (+ 1 2))")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testEqualsIntWrongType() {
        Interpreter.interpretExpression(Expression.parseString("(= 4 true)"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testEqualsMissingOneArg() {
        Interpreter.interpretExpression(Expression.parseString("(= 1)"));
    }

    @Test
    public void testIfTrue() {
        assertEquals(Expression.ofInt(0),
                Interpreter.interpretExpression(Expression.parseString("(if true 0 1)")));
    }

    @Test
    public void testIfFalse() {
        assertEquals(Expression.ofInt(1),
                Interpreter.interpretExpression(Expression.parseString("(if false 0 1)")));
    }

    @Test
    public void testIfNonBool() {
        // anything not false is true
        // different from how (test ...) bindings are interpreted!!
        assertEquals(Expression.ofInt(0),
                Interpreter.interpretExpression(Expression.parseString("(if 5 0 1)")));
    }

    @Test
    public void testIfNoEval() {
        // since the condition is true, the interpreter should not even look at the else branch.
        assertEquals(Expression.ofInt(0),
                Interpreter.interpretExpression(Expression.parseString("(if true 0 x)")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testIfMissingOneArg() {
        Interpreter.interpretExpression(Expression.parseString("(if true 0)"));
    }

    @Test
    public void testPutAndGetVariable() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        env.putVariable("x", Expression.ofInt(42));

        assertEquals(Expression.ofInt(42), env.getVariable("x"));
    }

    @Test
    public void testPutAndGetVariableUnprocessed() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        env.putVariable("x", Expression.parseString("(+ 3 2)"));
        assertEquals(Expression.ofInt(5), env.getVariable("x"));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testPutVariableNotValue() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        env.putVariable("x", Expression.parseString("y"));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testGetVariableUndefined() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        env.getVariable("x");
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testVariableDefineMissingOneArg() {
        Interpreter.interpretBinding(Binding.parseString("(define x)"));
    }

    @Test
    public void testLet() {
        assertEquals(Expression.ofInt(4),
                Interpreter.interpretExpression(Expression.parseString("(let ((x 3)) (+ x 1))")));
    }

    @Test
    public void testLetShadow1() {
        assertEquals(Expression.ofInt(2),
                Interpreter.interpretExpression(Expression.parseString("(let ((x 1)) (let ((x 2)) x))")));
    }

    @Test
    public void testLetShadow2() {
        assertEquals(Expression.ofInt(21),
                Interpreter.interpretExpression(Expression.parseString("(let ((x 2)) (* (let ((x 3)) x) (+ x 5)))")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testLetMissingArgument1() {
        Interpreter.interpretExpression(Expression.parseString("(let ((x)) (+ x 1))"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testLetMissingArgument2() {
        Interpreter.interpretExpression(Expression.parseString("(let ((x 2)) (+ x))"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testLetMissingArgument3() {
        Interpreter.interpretExpression(Expression.parseString("(let ((x 2)) (+ x 1) 5)"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testLetIncorrectDefinition() {
        Interpreter.interpretExpression(Expression.parseString("(let ((cons 1 2)) (+ x 1))"));
    }

    @Test
    public void testComment() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("(+ ;asdf asdf asdf\n1 2)")));
    }

    @Test
    public void testNil() {
        assertEquals(Expression.nil(),
                Interpreter.interpretExpression(Expression.parseString("nil")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testNilWrongArg() {
        Interpreter.interpretExpression(Expression.parseString("nils"));
    }

    @Test
    public void testCons() {
        assertEquals(Expression.cons(Expression.ofInt(1), Expression.ofInt(2)),
                Interpreter.interpretExpression(Expression.parseString("(cons 1 2)")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testConsMissingOneArg() {
        Interpreter.interpretExpression(Expression.parseString("(cons 0)"));
    }

    @Test
    public void testIsnilTrue() {
        assertEquals(Expression.ofBoolean(true),
                Interpreter.interpretExpression(Expression.parseString("(nil? nil)")));
    }

    @Test
    public void testIsnilFalse() {
        assertEquals(Expression.ofBoolean(false),
                Interpreter.interpretExpression(Expression.parseString("(nil? 1)")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testIsnilMissingArgument() {
        Interpreter.interpretExpression(Expression.parseString("(nil?)"));
    }

    @Test
    public void testIsConsTrue() {
        assertEquals(Expression.ofBoolean(true),
                Interpreter.interpretExpression(Expression.parseString("(cons? (cons 1 2))")));
    }

    @Test
    public void testIsConsFalse() {
        assertEquals(Expression.ofBoolean(false),
                Interpreter.interpretExpression(Expression.parseString("(cons? 3)")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testIsConsMissingArgument() {
        Interpreter.interpretExpression(Expression.parseString("(cons?)"));
    }

    @Test
    public void testCar() {
        assertEquals(Expression.ofInt(1),
                Interpreter.interpretExpression(Expression.parseString("(car (cons 1 2))")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testCarFalse() {
        Interpreter.interpretExpression(Expression.parseString("(car 3)"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testCarMissingArgument() {
        Interpreter.interpretExpression(Expression.parseString("(car)"));
    }

    @Test
    public void testCdr() {
        assertEquals(Expression.ofInt(2),
                Interpreter.interpretExpression(Expression.parseString("(cdr (cons 1 2))")));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testCdrFalse() {
        Interpreter.interpretExpression(Expression.parseString("(cdr 3)"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testCdrMissingArgument() {
        Interpreter.interpretExpression(Expression.parseString("(cdr)"));
    }

    @Test
    public void testPutAndGetFunction() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        env.putFunction("f", (Binding.FunctionBinding) Binding.parseString("(define (f p1 p2) p1)"));
        List<String> listArgs = new ArrayList<>();
        listArgs.add("p1");
        listArgs.add("p2");
        assertEquals(new Binding.FunctionBinding("f", listArgs, Expression.parseString("p1")), env.getFunction("f").getFunctionBinding());
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testPutFunctionMissingArgs() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        env.putFunction("f", (Binding.FunctionBinding) Binding.parseString("(define (f p1 p2))"));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testPutFunctionDuplicateParams() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        env.putFunction("f", (Binding.FunctionBinding) Binding.parseString("(define (f p1 p1) (+ 3 p1))"));
    }

    // ---------------------------------------------------------------------------------------------
    // Binding tests
    // ---------------------------------------------------------------------------------------------

    @Test
    public void testVarBinding() {
        assertEquals(Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3)),
                Interpreter.interpretBinding(Binding.parseString("(define x (+ 1 2))")));
    }

    @Test
    public void testVarBindingLookup() {
        Interpreter.DynamicEnvironment env = Interpreter.interpretBinding(Binding.parseString("(define x (+ 1 2))"));

        assertEquals(Expression.ofInt(3), env.getVariable("x"));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testBindingEmptyParen() {
        Interpreter.interpretBinding(Binding.parseString("()"));
    }

    @Test
    public void testTopLevelExpr() {
        // We don't test anything about the answer, since the interpreter just prints it to stdout,
        // and it would be too much work to try to capture this output for testing.
        // Instead, we just check that the environment is unchanged.
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3));
        assertEquals(env, Interpreter.interpretBinding(Binding.parseString("(* 2 x)"), env));
    }

    @Test
    public void testTestBindingPass() {
        // Who tests the tests??
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3));

        // just check that no exception is thrown here
        Interpreter.interpretBinding(Binding.parseString("(test (= 3 x))"), env);
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testTestBindingFail() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3));
        Interpreter.interpretBinding(Binding.parseString("(test (= 2 x))"), env);
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testTestBindingBadData() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3));
        Interpreter.interpretBinding(Binding.parseString("(test x)"), env);
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testTestMissingArgument() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(3));
        Interpreter.interpretBinding(Binding.parseString("(test)"), env);
    }

    @Test
    public void testFunctionBinding() {
        Interpreter.DynamicEnvironment env =
                Interpreter.interpretBinding(
                        Binding.parseString("(define (f x) (+ x 1))"));
        env = Interpreter.interpretBinding(
                Binding.parseString("(define y (f 2))"),
                env
        );
        assertEquals(
                Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("y"), env)
        );
    }

    @Test
    public void testFunctionBindingNoArg() {
        Interpreter.DynamicEnvironment env =
                Interpreter.interpretBinding(
                        Binding.parseString("(define (f) (+ 2 1))"));
        assertEquals(
                Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("(f)"), env)
        );
    }

    @Test
    public void testFunctionBindingLexicalScope() {
        Interpreter.DynamicEnvironment env =
                Interpreter.interpretBinding(
                        Binding.parseString("(define (f y) (+ x y))"),
                        Interpreter.DynamicEnvironment.singleton("x", Expression.ofInt(1))
                );
        env = Interpreter.interpretBinding(
                Binding.parseString("(define z (let ((x 2)) (f 3)))"),
                env
        );
        assertEquals(
                Expression.ofInt(4),
                Interpreter.interpretExpression(Expression.parseString("z"), env)
        );
    }

    @Test
    public void testFunctionBindingRecursive() {
        String program =
                "(define (pow base exp) " +
                        "(if (= exp 0) " +
                        "    1 " +
                        "    (* base (pow base (- exp 1)))))";
        Interpreter.DynamicEnvironment env =
                Interpreter.interpretBinding(
                        Binding.parseString(program)
                );
        env = Interpreter.interpretBinding(
                Binding.parseString("(define x (pow 2 3))"),
                env
        );
        assertEquals(
                Expression.ofInt(8),
                Interpreter.interpretExpression(Expression.parseString("x"), env)
        );
    }

    public static String countdownBinding =
            "(define (countdown n) " +
                    "(if (= n 0) " +
                    "    nil " +
                    "    (cons n (countdown (- n 1)))))";
    @Test
    public void testFunctionBindingListGenerator() {
        Interpreter.DynamicEnvironment env =
                Interpreter.interpretBinding(
                        Binding.parseString(countdownBinding)
                );
        env = Interpreter.interpretBinding(
                Binding.parseString("(define x (car (cdr (countdown 10))))"),
                env
        );
        Expression ans = Interpreter.interpretExpression(Expression.parseString("x"), env);
        assertEquals(Expression.ofInt(9), ans);
    }

    @Test
    public void testFunctionBindingListConsumer() {
        String sumBinding =
                "(define (sum l) " +
                        "(if (nil? l) " +
                        "    0 " +
                        "    (+ (car l) (sum (cdr l)))))";
        System.out.println(sumBinding);
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        env = Interpreter.interpretBinding(Binding.parseString(countdownBinding), env);
        env = Interpreter.interpretBinding(Binding.parseString(sumBinding), env);
        env = Interpreter.interpretBinding(
                Binding.parseString("(define x (sum (countdown 10)))"),
                env
        );
        assertEquals(
                Expression.ofInt(55),
                Interpreter.interpretExpression(Expression.parseString("x"), env)
        );
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testFunctionBindingParamLengDiffError() {
        String program =
                "(define (pow base exp anotherparameter) " +
                        "(if (= exp 0) " +
                        "    1 " +
                        "    (* base (pow base (- exp 1)))))";
        Interpreter.DynamicEnvironment env =
                Interpreter.interpretBinding(
                        Binding.parseString(program)
                );
        Interpreter.interpretBinding(
                Binding.parseString("(define x (pow 2 3))"),
                env
        );
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testFunctionBindingUndefinedfunctionError() {
        Interpreter.DynamicEnvironment env = Interpreter.DynamicEnvironment.empty();
        Interpreter.interpretBinding(
                Binding.parseString("(define x (pow 2 3))"),
                env
        );
    }

    // My own feature tests
    @Test
    public void testMax() {
        assertEquals(Expression.ofInt(8),
                Interpreter.interpretExpression(Expression.parseString("(max 3 8 4)")));
    }

    @Test
    public void testMaxUnprocessed() {
        assertEquals(Expression.ofInt(7),
                Interpreter.interpretExpression(Expression.parseString("(max 3 4 (+ 2 5))")));
    }

    @Test(expected = Trefoil2.TrefoilError.AbstractSyntaxError.class)
    public void testMaxMissingArgs() {
        Interpreter.interpretExpression(Expression.parseString("(max)"));
    }

    @Test(expected = Trefoil2.TrefoilError.RuntimeError.class)
    public void testMaxTypeError() {
        assertEquals(Expression.ofInt(3),
                Interpreter.interpretExpression(Expression.parseString("(max 1 true)")));
    }

    // adversarial test
    @Test
    public void testVarFuncSameName() {
        Interpreter.DynamicEnvironment env = Interpreter.interpretBinding(Binding.parseString("(define (x y) (+ 1 y))"));
        env = Interpreter.interpretBinding(Binding.parseString("(define x 3)"), env);
        assertEquals(Expression.ofInt(3), Interpreter.interpretExpression(Expression.parseString("x"), env));
        assertEquals(Expression.ofInt(5), Interpreter.interpretExpression(Expression.parseString("(x 4)"), env));
    }
}
