package lexer;

public class Token<T> {
    private TokenType type;
    private T value;

    public int row;
    public int col;

    public Token(TokenType type) {
        this.type = type;
        this.value = null;
    }

    public Token(TokenType function, T tokenValue) {
        this.type = function;
        this.value = tokenValue;
    }

    public T getTokenValue() {
        return value;
    }

    public TokenType getTokenType() {
        return type;
    }

    public Token(TokenType type, int row, int col ){
        this.type = type;
        this.value = null;
        this.row = row;
        this.col = col;
    }

    public Token(TokenType type, T value , int row, int col){
        this.type = type;
        this.value = value;
        this.row = row;
        this.col = col;
    }

    public int getRow(){
        return row;
    }

    public int getCol(){
        return col;
    }

    public boolean match(TokenType type) {
        return (this.getTokenType() == type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Token)) {
            return false;
        }

        Token<?> other = (Token<?>) obj;
        if (type != other.type) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
