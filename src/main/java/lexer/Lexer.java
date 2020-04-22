package lexer;

import buffer.Buffer;

public class Lexer {

    private Buffer buffer;

    private Token<?> currentToken = null;
    private Token<?> parentToken = null;

    private boolean isEndSourceCode = false;

    public Lexer(Buffer buffer) {
        this.buffer = buffer;
    }

    public Token<?> getParentToken(){
        return parentToken;
    }

    public Token<?> peekToken() {
        if (currentToken == null) {
            makeToken();
        }
        return currentToken;
    }

    public Token<?> getToken() {
        if (currentToken == null) {
            makeToken();
        }
        parentToken = currentToken;
        Token<?> result = currentToken;
        makeToken();

        return result;
    }

    private void makeToken() {

        if (isEndSourceCode) {
            currentToken = new Token<String>(TokenType.END, "End", buffer.getRow(), buffer.getRow());
            return;
        }

        readThroughSpacesAndComments();

        if (isEndSourceCode) {
            currentToken = new Token<String>(TokenType.END, "End", buffer.getRow(), buffer.getRow());
            return;
        }

        char curChar = buffer.getChar();
        switch (curChar) {
            case '+':
                currentToken = new Token<Object>(TokenType.PLUS, buffer.getRow(), buffer.getCol());
                break;
            case '-':
                currentToken = new Token<Object>(TokenType.MINUS, buffer.getRow(), buffer.getCol());
                break;
            case '*':
                currentToken = new Token<Object>(TokenType.MULTIPLICATION, buffer.getRow(), buffer.getCol());
                break;
            case '/':
                currentToken = new Token<Object>(TokenType.DIVISION, buffer.getRow(), buffer.getCol());
                break;
            case '^':
                currentToken = new Token<Object>(TokenType.EXPONENTIATION, buffer.getRow(), buffer.getCol());
                break;
            case '(':
                currentToken = new Token<Object>(TokenType.BRACKET_OPEN, buffer.getRow(), buffer.getCol());
                break;
            case ')':
                currentToken = new Token<Object>(TokenType.BRACKET_CLOSE, buffer.getRow(), buffer.getCol());
                break;
            case '{':
                currentToken = new Token<Object>(TokenType.BRACE_OPEN, buffer.getRow(), buffer.getCol());
                break;
            case '}':
                currentToken = new Token<Object>(TokenType.BRACE_CLOSE, buffer.getRow(), buffer.getCol());
                break;
            case '[':
                currentToken = new Token<Object>(TokenType.BRACET_OPEN, buffer.getRow(), buffer.getCol());
                break;
            case ']':
                currentToken = new Token<Object>(TokenType.BRACET_CLOSE, buffer.getRow(), buffer.getCol());
                break;
            case ',':
                currentToken = new Token<Object>(TokenType.COMMA, buffer.getRow(), buffer.getCol());
                break;
            case ';':
                currentToken = new Token<Object>(TokenType.SEMICOLON, buffer.getRow(), buffer.getCol());
                break;

            default:
                if (Character.isDigit(curChar)) {
                    currentToken = getNumberFromBuffer(curChar);
                } else {
                    if (Character.isAlphabetic(curChar) || curChar == '_') {
                        currentToken = getIdentificatorFromBuffer(curChar);
                    } else {
                        currentToken = getConditionSignFromBuffer(curChar);
                    }
                }
                break;
        }
    }

    private void readThroughSpacesAndComments() {
        while (true) {
            while (Character.isWhitespace(peekCharFromBuffer(0))) {
                buffer.getChar();
            }

            int nextChar = peekCharFromBuffer(0);
            if (nextChar == '/') {

                nextChar = peekCharFromBuffer(1);

                switch (nextChar) {
                    case '/':
                        while (!isEndSourceCode) {
                            if ('\n' != peekCharFromBuffer(0)) {
                                buffer.getChar();
                            } else {
                                buffer.getChar();
                                break;
                            }
                        }
                        break;
                    case '*':
                        buffer.getChar();
                        buffer.getChar();
                        boolean isEndMultilineComment = false;
                        while (!isEndMultilineComment) {
                            if (isEndSourceCode) {
                                return;
                            } else {
                                char nextCommentChar = buffer.getChar();
                                if (nextCommentChar == '*') {
                                    if (peekCharFromBuffer(0) == '/') {
                                        if (!isEndSourceCode) {
                                            isEndMultilineComment = true;
                                            buffer.getChar();
                                        } else {
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        if (isEndSourceCode) {
                            isEndSourceCode = false;
                        }
                        return;
                }
            } else {
                return;
            }
        }
    }

    private Token<?> getNumberFromBuffer(char curChar) {
        int number = Character.getNumericValue(curChar);

        int shiftComma = -1;

        while (Character.isDigit(peekCharFromBuffer(0)) || (peekCharFromBuffer(0) == '.')) {
            if ((shiftComma > -1) || (peekCharFromBuffer(0) == '.')) {
                shiftComma++;
            }
            if (shiftComma != 0) {
                number = 10 * number + Character.getNumericValue(buffer.getChar());
            } else {
                buffer.getChar();
            }
        }

        switch (shiftComma) {
            case -1:
                return new Token<Integer>(TokenType.NUMBER, number, buffer.getRow(), buffer.getCol());
            case 0:
                throw new RuntimeException("L: число не может заканчиваться на точку");
            default:
                double doubleNumber = number;
                double tenPower = Math.pow(10, shiftComma);
                doubleNumber /= tenPower;
                return new Token<Double>(TokenType.NUMBER, doubleNumber, buffer.getRow(), buffer.getCol());
        }
    }

    private Token<?> getIdentificatorFromBuffer(char curChar) {
        String ident = Character.toString(curChar);

        while (Character.isAlphabetic(peekCharFromBuffer(0))) {
            ident += buffer.getChar();
        }
        Token<?> result = null;

        switch (ident) {
            case "return":
                result = new Token<Object>(TokenType.RETURN, buffer.getRow(), buffer.getCol());
                break;
            case "int":
                result = new Token<String>(TokenType.INT, buffer.getRow(), buffer.getCol());
                break;
            case "double":
                result = new Token<String>(TokenType.DOUBLE, buffer.getRow(), buffer.getCol());
                break;
            case "char":
                result = new Token<String>(TokenType.CHAR, buffer.getRow(), buffer.getCol());
                break;
            case "printf":
                result = new Token<Object>(TokenType.PRINTF, buffer.getRow(), buffer.getCol());
                break;
            case "scanf":
                result = new Token<Object>(TokenType.SCANF, buffer.getRow(), buffer.getCol());
                break;
            case "void":
                result = new Token<Object>(TokenType.VOID, buffer.getRow(), buffer.getCol());
                break;
            case "if":
                result = new Token<Object>(TokenType.IF, buffer.getRow(), buffer.getCol());
                break;
            case "else":
                result = new Token<Object>(TokenType.ELSE, buffer.getRow(), buffer.getCol());
                break;
            case "while":
                result = new Token<Object>(TokenType.WHILE, buffer.getRow(), buffer.getCol());
                break;
            default:
                result = new Token<String>(TokenType.NAME, ident, buffer.getRow(), buffer.getCol());
                break;
        }
        return result;
    }

    private Token<?> getConditionSignFromBuffer(char curChar) {
        String sign = Character.toString(curChar);

        if (peekCharFromBuffer(0) == '=') {
            sign += buffer.getChar();
        }

        Token<?> result = null;

        switch (sign) {
            case "=":
                result = new Token<Object>(TokenType.ASSIGNMENT, buffer.getRow(), buffer.getCol());
                break;
            case "<":
                result = new Token<String>(TokenType.SIGN, "<", buffer.getRow(), buffer.getCol());
                break;
            case "<=":
                result = new Token<String>(TokenType.SIGN, "<=", buffer.getRow(), buffer.getCol());
                break;
            case "==":
                result = new Token<String>(TokenType.SIGN, "==", buffer.getRow(), buffer.getCol());
                break;
            case "!=":
                result = new Token<String>(TokenType.SIGN, "!=", buffer.getRow(), buffer.getCol());
                break;
            case ">":
                result = new Token<String>(TokenType.SIGN, ">", buffer.getRow(), buffer.getCol());
                break;
            case ">=":
                result = new Token<String>(TokenType.SIGN, ">=", buffer.getRow(), buffer.getCol());
                break;
            case "\"":
                sign = "";
                while (peekCharFromBuffer(0) != '"') {
                    if(peekCharFromBuffer(0) != '\n'){
                        sign += buffer.getChar();
                    } else {
                        throw new RuntimeException("L: незакрытый литерал");
                    }
                }
                buffer.getChar();
                result = new Token<String>(TokenType.LITERAL, sign, buffer.getRow(), buffer.getCol());
                break;
            case "'":
                sign = "";
                sign += buffer.getChar();
                if (peekCharFromBuffer(0) == '\'') {
                    buffer.getChar();
                } else {
                    throw new RuntimeException("L: ошибка символов");
                }
                result = new Token<Character>(TokenType.CHAR, sign.charAt(0), buffer.getRow(), buffer.getCol());
                break;
            default:
                throw new RuntimeException("L: неопределённая лексема");
        }
        return result;
    }

    private int peekCharFromBuffer(int serialIndex) {
        int ch = buffer.peekChar();

        if (ch == -1) {
            isEndSourceCode = true;
        } else {
            if (serialIndex == 1) {
                ch = buffer.peekSecondChar();
                if (ch == -1) {
                    isEndSourceCode = true;
                }
            }
        }
        return ch;
    }
}
