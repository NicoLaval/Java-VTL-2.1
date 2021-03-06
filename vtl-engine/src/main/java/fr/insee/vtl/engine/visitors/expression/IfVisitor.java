package fr.insee.vtl.engine.visitors.expression;

import fr.insee.vtl.engine.exceptions.InvalidTypeException;
import fr.insee.vtl.engine.exceptions.VtlRuntimeException;
import fr.insee.vtl.model.BooleanExpression;
import fr.insee.vtl.model.ResolvableExpression;
import fr.insee.vtl.parser.VtlBaseVisitor;
import fr.insee.vtl.parser.VtlParser;

import java.util.Objects;

import static fr.insee.vtl.engine.utils.TypeChecking.assertTypeExpression;
import static fr.insee.vtl.engine.utils.TypeChecking.isNull;

/**
 * <code>IfVisitor</code> is the base visitor for if-then-else expressions.
 */
public class IfVisitor extends VtlBaseVisitor<ResolvableExpression> {

    private final ExpressionVisitor exprVisitor;

    /**
     * Constructor taking a scripting context.
     *
     * @param expressionVisitor The expression visitor.
     */
    public IfVisitor(ExpressionVisitor expressionVisitor) {
        exprVisitor = Objects.requireNonNull(expressionVisitor);
    }

    /**
     * Visits if-then-else expressions.
     *
     * @param ctx The scripting context for the expression.
     * @return A <code>ResolvableExpression</code> resolving to the if or else clause resolution depending on the condition resolution.
     */
    @Override
    public ResolvableExpression visitIfExpr(VtlParser.IfExprContext ctx) {

        ResolvableExpression nullableExpression = exprVisitor.visit(ctx.conditionalExpr);
        if (isNull(nullableExpression)) {
            return BooleanExpression.of((Boolean) null);
        }

        ResolvableExpression conditionalExpression = assertTypeExpression(
                exprVisitor.visit(ctx.conditionalExpr),
                Boolean.class,
                ctx.conditionalExpr
        );

        // Find the common non null type.
        ResolvableExpression thenExpression = exprVisitor.visit(ctx.thenExpr);
        ResolvableExpression elseExpression = exprVisitor.visit(ctx.elseExpr);

        // Normalize the type if we have nulls.
        if (isNull(elseExpression) && !isNull(thenExpression)) {
            elseExpression = assertTypeExpression(elseExpression, thenExpression.getType(),
                    ctx.elseExpr);
        } else if (isNull(thenExpression)) {
            thenExpression = assertTypeExpression(thenExpression, elseExpression.getType(),
                    ctx.thenExpr);
        }

        if (!thenExpression.getType().equals(elseExpression.getType())) {
            throw new VtlRuntimeException(
                    new InvalidTypeException(thenExpression.getType(), elseExpression.getType(), ctx.elseExpr)
            );
        }

        ResolvableExpression finalThenExpression = thenExpression;
        ResolvableExpression finalElseExpression = elseExpression;
        return ResolvableExpression.withTypeCasting(thenExpression.getType(), (clazz, context) -> {
            Boolean conditionalValue = (Boolean) conditionalExpression.resolve(context);
            return Boolean.TRUE.equals(conditionalValue) ?
                    clazz.cast(finalThenExpression.resolve(context)) :
                    clazz.cast(finalElseExpression.resolve(context));
        });
    }
}
