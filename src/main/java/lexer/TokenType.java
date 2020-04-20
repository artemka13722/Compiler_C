package lexer;

public enum TokenType {
        CHAR,
        LITERAL,

        BODY_FINCTION,
        BODY_IF,
        BODY_ELSE,
        BODY_WHILE,

        ARRAY,
        ARRAY_BODY,
        NUMBER,
        PLUS, MINUS,
        MULTIPLICATION, DIVISION,
        EXPONENTIATION,
        BRACKET_OPEN, BRACKET_CLOSE,

        EMPTY,

        PROGRAM,
        FUNCTION,
        NAME,
        BRACET_OPEN, BRACET_CLOSE,
        BRACE_OPEN, BRACE_CLOSE,
        PARAMS_LIST,
        PARAM,
        COMMA,
        BODY,
        SEMICOLON,
        COMMAND,
        ARG_LIST,

        TYPE,
        INT, DOUBLE, VOID,

        ASSIGNMENT,
        CONDITION,
        SIGN,

        RETURN,

        CALL_FUNCTION,
        PRINTF,
        SCANF,
        PRINTF_BODY,
        SCANF_BODY,

        IF, ELSE,
        WHILE,

        END,
}
