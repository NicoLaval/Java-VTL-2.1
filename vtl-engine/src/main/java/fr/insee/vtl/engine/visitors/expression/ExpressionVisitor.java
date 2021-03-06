package fr.insee.vtl.engine.visitors.expression;

import fr.insee.vtl.engine.visitors.ClauseVisitor;
import fr.insee.vtl.engine.visitors.expression.functions.*;
import fr.insee.vtl.model.DatasetExpression;
import fr.insee.vtl.model.ProcessingEngine;
import fr.insee.vtl.model.ResolvableExpression;
import fr.insee.vtl.parser.VtlBaseVisitor;
import fr.insee.vtl.parser.VtlParser;

import java.util.Map;
import java.util.Objects;

/**
 * <code>ExpressionVisitor</code> is the base visitor for expressions.
 * It essentially passes the expressions to the more specialized visitors defined in the package.
 */
public class ExpressionVisitor extends VtlBaseVisitor<ResolvableExpression> {

    private static final ConstantVisitor CONSTANT_VISITOR = new ConstantVisitor();
    private final VarIdVisitor varIdVisitor;
    private final BooleanVisitor booleanVisitor;
    private final ArithmeticVisitor arithmeticVisitor;
    private final ArithmeticExprOrConcatVisitor arithmeticExprOrConcatVisitor;
    private final UnaryVisitor unaryVisitor;
    private final ComparisonVisitor comparisonVisitor;
    private final IfVisitor ifVisitor;
    private final StringFunctionsVisitor stringFunctionsVisitor;
    private final ComparisonFunctionsVisitor comparisonFunctionsVisitor;
    private final NumericFunctionsVisitor numericFunctionsVisitor;
    private final SetFunctionsVisitor setFunctionsVisitor;
    private final JoinFunctionsVisitor joinFunctionsVisitor;
    private final DistanceFunctionsVisitor distanceFunctionsVisitor;
    private final ProcessingEngine processingEngine;

    /**
     * Constructor taking a scripting context.
     *
     * @param context The map
     */
    public ExpressionVisitor(Map<String, Object> context, ProcessingEngine processingEngine) {
        Objects.requireNonNull(context);
        varIdVisitor = new VarIdVisitor(context);
        booleanVisitor = new BooleanVisitor(this);
        arithmeticVisitor = new ArithmeticVisitor(this);
        arithmeticExprOrConcatVisitor = new ArithmeticExprOrConcatVisitor(this);
        unaryVisitor = new UnaryVisitor(this);
        comparisonVisitor = new ComparisonVisitor(this);
        ifVisitor = new IfVisitor(this);
        stringFunctionsVisitor = new StringFunctionsVisitor(this);
        comparisonFunctionsVisitor = new ComparisonFunctionsVisitor(this);
        numericFunctionsVisitor = new NumericFunctionsVisitor(this);
        setFunctionsVisitor = new SetFunctionsVisitor(this, processingEngine);
        joinFunctionsVisitor = new JoinFunctionsVisitor(this, processingEngine);
        distanceFunctionsVisitor = new DistanceFunctionsVisitor(this);
        this.processingEngine = Objects.requireNonNull(processingEngine);
    }

    /**
     * Visits constants expressions.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the constant value with the expected type.
     * @see ConstantVisitor#visitConstant(VtlParser.ConstantContext)
     */
    @Override
    public ResolvableExpression visitConstant(VtlParser.ConstantContext ctx) {
        return CONSTANT_VISITOR.visit(ctx);
    }

    /**
     * Visits expressions with variable identifiers.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> or more specialized child resolving to the value of the variable.
     * @see VarIdVisitor#visitVarIdExpr(VtlParser.VarIdExprContext)
     */
    @Override
    public ResolvableExpression visitVarIdExpr(VtlParser.VarIdExprContext ctx) {
        return varIdVisitor.visit(ctx);
    }

    /**
     * Visits expressions with boolean operators.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the result of the boolean operation.
     * @see BooleanVisitor#visitBooleanExpr(VtlParser.BooleanExprContext)
     */
    @Override
    public ResolvableExpression visitBooleanExpr(VtlParser.BooleanExprContext ctx) {
        return booleanVisitor.visit(ctx);
    }

    /**
     * Visits expressions with multiplication or division operators.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the result of the multiplication or division operation.
     * @see ArithmeticVisitor#visitArithmeticExpr(VtlParser.ArithmeticExprContext)
     */
    @Override
    public ResolvableExpression visitArithmeticExpr(VtlParser.ArithmeticExprContext ctx) {
        return arithmeticVisitor.visit(ctx);
    }

    /**
     * Visits expressions with plus, minus or concatenation operators.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the result of the plus, minus or concatenation operation.
     * @see ArithmeticExprOrConcatVisitor#visitArithmeticExprOrConcat(VtlParser.ArithmeticExprOrConcatContext)
     */
    @Override
    public ResolvableExpression visitArithmeticExprOrConcat(VtlParser.ArithmeticExprOrConcatContext ctx) {
        return arithmeticExprOrConcatVisitor.visit(ctx);
    }

    /**
     * Visits unary expressions.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the result of the unary operation.
     * @see UnaryVisitor#visitUnaryExpr(VtlParser.UnaryExprContext)
     */
    @Override
    public ResolvableExpression visitUnaryExpr(VtlParser.UnaryExprContext ctx) {
        return unaryVisitor.visit(ctx);
    }

    /**
     * Visits expressions between parentheses (just passes the expression down the tree).
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> corresponding to the expression between parentheses.
     */
    @Override
    public ResolvableExpression visitParenthesisExpr(VtlParser.ParenthesisExprContext ctx) {
        return visit(ctx.expr());
    }

    /**
     * Visits expressions with comparisons.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the boolean result of the comparison.
     * @see ComparisonVisitor#visitComparisonExpr(VtlParser.ComparisonExprContext)
     */
    @Override
    public ResolvableExpression visitComparisonExpr(VtlParser.ComparisonExprContext ctx) {
        return comparisonVisitor.visit(ctx);
    }

    /**
     * Visits 'element of' ('In' or 'Not in') expressions.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the boolean result of the 'element of' expression.
     * @see ComparisonVisitor#visitInNotInExpr(VtlParser.InNotInExprContext)
     */
    @Override
    public ResolvableExpression visitInNotInExpr(VtlParser.InNotInExprContext ctx) {
        return comparisonVisitor.visit(ctx);
    }

    /**
     * Visits if-then-else expressions.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the if or else clause resolution depending on the condition resolution.
     * @see IfVisitor#visitIfExpr(VtlParser.IfExprContext)
     */
    @Override
    public ResolvableExpression visitIfExpr(VtlParser.IfExprContext ctx) {
        return ifVisitor.visit(ctx);
    }

    /*
    Functions
     */

    /**
     * Visits expressions involving string functions.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the result of the string function.
     * @see StringFunctionsVisitor
     */
    @Override
    public ResolvableExpression visitStringFunctions(VtlParser.StringFunctionsContext ctx) {
        return stringFunctionsVisitor.visit(ctx.stringOperators());
    }

    /**
     * Visits expressions involving comparison functions.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the result of the comparison function.
     * @see ComparisonFunctionsVisitor
     */
    @Override
    public ResolvableExpression visitComparisonFunctions(VtlParser.ComparisonFunctionsContext ctx) {
        return comparisonFunctionsVisitor.visit(ctx.comparisonOperators());
    }

    /**
     * Visits set function expressions.
     *
     * @param ctx The scripting context for the function expression.
     * @return A <code>ResolvableExpression</code> resolving to the result of the function expression.
     * @see SetFunctionsVisitor
     */
    @Override
    public ResolvableExpression visitSetFunctions(VtlParser.SetFunctionsContext ctx) {
        return setFunctionsVisitor.visit(ctx.setOperators());
    }

    @Override
    public ResolvableExpression visitJoinFunctions(VtlParser.JoinFunctionsContext ctx) {
        return joinFunctionsVisitor.visitJoinFunctions(ctx);
    }

    @Override
    public ResolvableExpression visitNumericFunctions(VtlParser.NumericFunctionsContext ctx) {
        return numericFunctionsVisitor.visit(ctx.numericOperators());
    }

    /**
     * Visits expressions involving distance functions.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the result of the distance function.
     * @see DistanceFunctionsVisitor
     */
    @Override
    public ResolvableExpression visitDistanceFunctions(VtlParser.DistanceFunctionsContext ctx) {
        return distanceFunctionsVisitor.visit(ctx.distanceOperators());
    }

    /**
     * Visits clause expressions.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the result of the close expression.
     * @see ClauseVisitor
     */
    @Override
    public ResolvableExpression visitClauseExpr(VtlParser.ClauseExprContext ctx) {
        DatasetExpression datasetExpression = (DatasetExpression) visit(ctx.dataset);
        ClauseVisitor clauseVisitor = new ClauseVisitor(datasetExpression, processingEngine);
        return clauseVisitor.visit(ctx.clause);
    }
}
