package idTable;

import lexer.TokenType;

public class Variable {
    String value;
    TokenType tokenType;

    public Variable(String value, TokenType tokenType) {
        this.value = value;
        this.tokenType = tokenType;
    }

    @Override
    public String toString() {
        return "Varible{" +
                "value='" + value + '\'' +
                ", tokenType=" + tokenType +
                '}';
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }
}
