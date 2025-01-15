package backend.javy;

/**
 * The {@code JavyExpression} interface defines the structure for different types of expressions
 * in the interpreter. Expressions can be binary (operations between two values), unary (single operand),
 * literals, identifiers, or logical operations. Each expression is capable of being evaluated, which
 * is its main operation.
 *
 * @author Albert Beaupre
 */
public interface JavyExpression {

    /**
     * Evaluates the expression and returns the result.
     *
     * @return The result of the expression.
     */
    Object evaluate();

    /**
     * A {@code Binary} expression involves two operands and an operator.
     * For example, arithmetic operations like addition and subtraction.
     */
    class Binary implements JavyExpression {
        final JavyExpression left;
        final JavyTokenBinder binder;
        final JavyExpression right;

        /**
         * Constructs a binary expression.
         *
         * @param left   The left-hand operand.
         * @param binder The token binder containing the operator.
         * @param right  The right-hand operand.
         */
        Binary(JavyExpression left, JavyTokenBinder binder, JavyExpression right) {
            this.left = left;
            this.binder = binder;
            this.right = right;
        }

        @Override
        public Object evaluate() {
            Object left = this.left.evaluate();
            Object right = this.right.evaluate();
            JavyToken operator = this.binder.token();

            if (left instanceof Number && right instanceof Number) {
                double leftValue = ((Number) left).doubleValue();
                double rightValue = ((Number) right).doubleValue();

                return switch (operator) {
                    case Plus -> leftValue + rightValue;
                    case Minus -> leftValue - rightValue;
                    case Star -> leftValue * rightValue;
                    case Slash -> leftValue / rightValue;
                    case GreaterThan -> leftValue > rightValue;
                    case LessThan -> leftValue < rightValue;
                    case GreaterThanOrEqualTo -> leftValue >= rightValue;
                    case LessThanOrEqualTo -> leftValue <= rightValue;
                    case Is -> leftValue == rightValue;
                    default -> throw new IllegalArgumentException(STR."\{binder.location()}Invalid operator: \{operator}");
                };
            } else if (left instanceof String || right instanceof String) {
                return switch (operator) {
                    case Plus -> String.valueOf(left) + right;
                    case Minus -> String.valueOf(left).replace(String.valueOf(right), "");
                    case Star -> {
                        if (right instanceof Number) {
                            yield String.valueOf(left).repeat(((Number) right).intValue());
                        } else if (left instanceof Number) {
                            yield String.valueOf(right).repeat(((Number) left).intValue());
                        } else {
                            throw new RuntimeException("Invalid multiplication operation");
                        }
                    }
                    case Is -> left.equals(right);
                    default ->
                            throw new IllegalArgumentException(STR."\{binder.location()}Invalid operator or operand types: \{operator}");
                };
            }
            throw new RuntimeException(STR."\{binder.location()}Invalid operation");
        }
    }

    /**
     * Represents a literal value expression.
     */
    class Literal implements JavyExpression {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        public Object evaluate() {
            return value;
        }
    }

    /**
     * Represents an identifier (variable) in the source code.
     */
    class Identifier implements JavyExpression {
        final JavyScope scope;
        final String name;

        Identifier(JavyScope scope, String name) {
            this.scope = scope;
            this.name = name;
        }

        @Override
        public Object evaluate() {
            return scope.getVariable(name).getValue();
        }
    }

    /**
     * Represents a unary expression, which applies an operator to a single operand.
     */
    class Unary implements JavyExpression {
        final JavyToken operator;
        final JavyExpression right;

        Unary(JavyToken operator, JavyExpression right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public Object evaluate() {
            Object right = this.right.evaluate();

            if (operator == JavyToken.Minus) {
                if (right instanceof Number) {
                    return -((Number) right).doubleValue();
                } else {
                    throw new RuntimeException("Unary minus can only be applied to numbers.");
                }
            }

            throw new RuntimeException("Unknown operator.");
        }
    }

    /**
     * Represents a logical expression, such as 'and' or 'or' operations.
     */
    class Logical implements JavyExpression {
        final JavyExpression left;
        final JavyTokenBinder binder;
        final JavyExpression right;

        public Logical(JavyExpression left, JavyTokenBinder binder, JavyExpression right) {
            this.left = left;
            this.binder = binder;
            this.right = right;
        }

        @Override
        public Object evaluate() {
            Object left = this.left.evaluate();

            if (binder.token() == JavyToken.Or) {
                if (isTruthy(left)) return left;
            } else {
                if (!isTruthy(left)) return left;
            }

            return right.evaluate();
        }

        private boolean isTruthy(Object object) {
            if (object == null) return false;
            if (object instanceof Boolean) return (boolean) object;
            return true;
        }
    }

    /**
     * The {@code Parser} class is responsible for parsing expressions according to their precedence
     * and structure. It includes the logic for parsing binary, unary, logical, and literal expressions.
     */
    class Parser {

        public static JavyExpression parse(JavyScope scope, boolean option) {
            return parseExpression(scope, Precedence.ASSIGNMENT);
        }

        public static Object parse(JavyScope scope) {
            return parse(scope, true).evaluate();
        }

        private static JavyExpression parseExpression(JavyScope scope, Precedence precedence) {
            JavyExpression expr = parseUnary(scope);

            while (true) {
                JavyTokenBinder binder = scope.getBinders().peek();
                Precedence currentPrecedence = getPrecedence(binder.token());

                if (currentPrecedence.ordinal() <= precedence.ordinal()) {
                    break;
                }

                scope.getBinders().poll(); // Consume the operator

                JavyExpression right = parseExpression(scope, currentPrecedence);
                if (binder.token() == JavyToken.Or || binder.token() == JavyToken.And) {
                    expr = new Logical(expr, binder, right);
                } else {
                    expr = new Binary(expr, binder, right);
                }
            }

            return expr;
        }

        private static JavyExpression parseUnary(JavyScope scope) {
            if (scope.getBinders().peek().token() == JavyToken.Minus) {
                JavyToken operator = scope.getBinders().poll().token();
                JavyExpression right = parseUnary(scope);
                return new Unary(operator, right);
            }

            return parsePrimary(scope);
        }

        private static JavyExpression parsePrimary(JavyScope scope) {
            JavyTokenBinder binder = scope.getBinders().poll();
            return switch (binder.token()) {
                case LeftParameter -> {
                    JavyExpression expression = parse(scope, true);
                    scope.getBinders().poll(); // Consume the closing parenthesis
                    yield expression;
                }
                case Identifier -> {
                    String name = (String) binder.value();
                    if (name.equals("true"))
                        yield new Literal(true);
                    else if (name.equals("false"))
                        yield new Literal(false);

                    JavyVariable variable = scope.getVariable(name);
                    if (variable == null) {
                        throw new RuntimeException(STR."\{binder.location()}No such variable exists: \{binder.value()}");
                    } else {
                        yield new Identifier(scope, name);
                    }
                }
                case Number, String -> new Literal(binder.value());
                default -> throw new RuntimeException(STR."\{binder.location()}Expected expression.");
            };
        }

        private static Precedence getPrecedence(JavyToken token) {
            return switch (token) {
                case Or -> Precedence.OR;
                case And -> Precedence.AND;
                case Is, Not -> Precedence.EQUALITY;
                case GreaterThan, GreaterThanOrEqualTo, LessThan, LessThanOrEqualTo -> Precedence.COMPARISON;
                case Plus, Minus -> Precedence.TERM;
                case Star, Slash -> Precedence.FACTOR;
                default -> Precedence.NONE;
            };
        }

        /**
         * The {@code Precedence} enum defines the precedence of different operators,
         * which determines the order in which operations are performed.
         */
        private enum Precedence {
            NONE,           // Lowest precedence (used for unknown tokens)
            ASSIGNMENT,     // =
            OR,             // or
            AND,            // and
            EQUALITY,       // is not
            COMPARISON,     // < > <= >=
            TERM,           // + -
            FACTOR,         // * /
            UNARY,          // ! -
            PRIMARY         // Literals, grouping
        }
    }
}