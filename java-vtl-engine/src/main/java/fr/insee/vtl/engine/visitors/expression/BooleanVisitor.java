package fr.insee.vtl.engine.visitors.expression;

import fr.insee.vtl.model.ResolvableExpression;
import fr.insee.vtl.parser.VtlBaseVisitor;
import fr.insee.vtl.parser.VtlParser;

public class BooleanVisitor extends VtlBaseVisitor<ResolvableExpression> {

    private final ExpressionVisitor exprVisitor = new ExpressionVisitor();

    @Override
    public ResolvableExpression visitBooleanExpr(VtlParser.BooleanExprContext ctx) {
        ResolvableExpression leftExpression = exprVisitor.visit(ctx.left);
        ResolvableExpression rightExpression = exprVisitor.visit(ctx.right);
        switch (ctx.op.getType()) {
            case VtlParser.AND:
                return ResolvableExpression.withType(Boolean.class, context -> {
                    Boolean leftValue = (Boolean) leftExpression.resolve(context);
                    Boolean rightValue = (Boolean) rightExpression.resolve(context);
                    return leftValue && rightValue;
                });
            case VtlParser.OR:
                return ResolvableExpression.withType(Boolean.class, context -> {
                    Boolean leftValue = (Boolean) leftExpression.resolve(context);
                    Boolean rightValue = (Boolean) rightExpression.resolve(context);
                    return leftValue || rightValue;
                });
            case VtlParser.XOR:
                return ResolvableExpression.withType(Boolean.class, context -> {
                    Boolean leftValue = (Boolean) leftExpression.resolve(context);
                    Boolean rightValue = (Boolean) rightExpression.resolve(context);
                    return leftValue ^ rightValue;
                });
            default:
                throw new UnsupportedOperationException("unknown operator " + ctx);
        }
    }
}