package lexer;

public enum TokenType {

        //преобразования
        INTTOCHAR,
        INTTODOUBLE,

        CHARTOINT,
        CHARTODOUBLE,

        DOUBLETOINT,
        DOUBLETOCHAR,

        LITERAL,

        BODY_FINCTION,
        BODY_THEN,
        BODY_ELSE,
        BODY_WHILE,

        ARRAY,
        ARRAY_BODY,
        ARRAYASSIGMENT,
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
        CHAR,

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
