package parser;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

public class Parser {

    private Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Node parseProgram() {
        Node result = new Node(TokenType.PROGRAM);
        try {
            Node curFunction;
            do {
                curFunction = parseFunction();
                result.setRight(curFunction);
            } while (!curFunction.match(TokenType.EMPTY));
        } catch (RuntimeException e) {
            Token<?> errorToken = lexer.getParentToken();
            System.out.printf((char) 27 + "[31m %s LOC<%d:%d>\n", e.getMessage(), errorToken.getRow(), errorToken.getCol());
            System.exit(0);
        }
        return result;
    }

    public Node parseFunction() {
        Node result;
        if (lexer.peekToken().match(TokenType.END)) {
            return new Node(TokenType.EMPTY);
        }

        Node typeNode = parseType();
        Token<?> nameToken = lexer.getToken();
        if (!nameToken.match(TokenType.NAME)) {
            throw new RuntimeException("P: Ошибка. Должно быть имя");
        }

        Token<?> openBracketToken = lexer.getToken();
        if (!openBracketToken.match(TokenType.BRACKET_OPEN)) {
            throw new RuntimeException("P: Ошибка. Должно быть (");
        }

        Node parlistNode = parseParlist();

        Token<?> closeBracketToken = lexer.getToken();
        if (!closeBracketToken.match(TokenType.BRACKET_CLOSE)) {
            throw new RuntimeException("P: Ошибка. Должно быть )");
        }

        Token<?> openBraceToken = lexer.getToken();

        if (!openBraceToken.match(TokenType.BRACE_OPEN)) {
            throw new RuntimeException("P: Ошибка. Должно быть  {");
        }

        Node bodyNode = parseBody("def");

        Token<?> closeBraceToken = lexer.getToken();
        if (!closeBraceToken.match(TokenType.BRACE_CLOSE)) {
            throw new RuntimeException("P: Ошибка. Должно быть }");
        }

        result = new Node(new Token<String>(TokenType.FUNCTION, (String) nameToken.getTokenValue()));
        result.setLeft(typeNode);
        result.setRight(new Node(nameToken));
        result.setRight(parlistNode);
        result.setRight(bodyNode);

        return result;
    }

    public Node parseParlist() {
        Token<?> closeBracketToken = lexer.peekToken();
        if (closeBracketToken.match(TokenType.BRACKET_CLOSE)) {
            Node emptyParList = new Node(TokenType.PARAMS_LIST);
            emptyParList.setLeft(new Node(TokenType.EMPTY));
            return emptyParList;
        }

        Node result = new Node(TokenType.PARAMS_LIST);
        Token<?> commaToken;
        do {
            Node typeNode = parseType();
            Token<?> nameToken = lexer.getToken();
            if (!nameToken.match(TokenType.NAME)) {
                throw new RuntimeException("P: переменная не имеет имени");
            }
            Node newParam = new Node(new Token<String>(TokenType.PARAM, (String) nameToken.getTokenValue()));
            newParam.setLeft(typeNode);
            result.setRight(newParam);
            commaToken = lexer.peekToken();
        } while (commaToken.match(TokenType.COMMA) && commaToken == lexer.getToken());

        if (!(commaToken.match(TokenType.COMMA) || commaToken.match(TokenType.BRACKET_CLOSE))) {
            throw new RuntimeException("P: ошибка закрытия аргументов функции");
        }
        return result;
    }

    public Node parseBody(String type) {

        Node result;

        switch (type){
            case "def":
                result = new Node(TokenType.BODY);
                break;
            case "then":
                result = new Node(TokenType.BODY_THEN);
                break;
            case "else":
                result = new Node(TokenType.BODY_ELSE);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }

        Token<?> closeBraceToken = lexer.peekToken();
        if (closeBraceToken.match(TokenType.BRACE_CLOSE)) {
            result.setLeft(new Node(TokenType.EMPTY));
            return result;
        }

        Node command = parseCommand();
        result.setLeft(command);
        do {
            if (checkExceptionSemicolon(command)) {
                Token<?> semicolonToken = lexer.getToken();
                if (!semicolonToken.match(TokenType.SEMICOLON)) {
                    throw new RuntimeException("P: после команды должна следовать \";\"");
                }
            }
            command = parseCommand();
            result.setRight(command);
        } while (!command.getFirstChildren().getValue().match(TokenType.EMPTY));
        result.setRight(new Node(TokenType.EMPTY));
        return result;
    }

    public Node parseCommand() {
        Node result = new Node(TokenType.COMMAND);

        Token<?> whatEver = lexer.peekToken();
        switch (whatEver.getTokenType()) {
            case BRACE_CLOSE:
                break;
            case INT:
            //case DOUBLE:
            case CHAR:
                lexer.getToken();
                Token<?> nameToken = lexer.getToken();
                if (nameToken.match(TokenType.NAME)) {
                    Node typeNode = new Node(TokenType.TYPE);
                    typeNode.setLeft(new Node(whatEver));
                    result.setLeft(typeNode);
                    result.setRight(new Node(nameToken));

                    Token<?> openBracetToken = lexer.peekToken();

                    switch (openBracetToken.getTokenType()){
                        case BRACET_OPEN:
                            result.setRight(parseArray());
                            break;
                        case ASSIGNMENT:
                            Node assigment = new Node(openBracetToken);
                            //result.setLeft(new Node(whatEver));
                            lexer.getToken();
                            Token strstr = lexer.peekToken();
                            if(strstr.match(TokenType.STRSTR)){
                                assigment = new Node(lexer.peekToken());
                                parseStrStr(assigment);
                            } else {
                                assigment.setLeft(parseExpr());
                            }
                            result.setRight(assigment);
                           /* result.setRight(new Node(openBracetToken));
                            result.setRight(parseExpr());*/
                            break;
                    }
                } else {
                    throw new RuntimeException("P: отсутсвует имя у переменной");
                }
                break;
            case NAME:
                lexer.getToken();
                Token<?> assignToken = lexer.getToken();

                switch (assignToken.getTokenType()){

                    case ASSIGNMENT:
                        result.setLeft(new Node(whatEver));
                        Node assigment = new Node(assignToken);
                        if(lexer.peekToken().match(TokenType.STRSTR)){
                            assigment = new Node(lexer.peekToken());
                            parseStrStr(assigment);
                        } else {
                            assigment.setLeft(parseExpr());
                        }
                        result.setRight(assigment);
                        break;
                    case BRACET_OPEN:
                        result.setLeft(new Node(whatEver));
                        result.setRight(parseArrayAssigment());
                        break;
                    case BRACKET_OPEN:
                        result = new Node(TokenType.CALL_FUNCTION);
                        result.setLeft(new Node(whatEver));
                        result.setRight(parseArgList());
                        Token<?> nextToken = lexer.getToken();
                        if (!nextToken.match(TokenType.BRACKET_CLOSE)) {
                            throw new RuntimeException("P: отсвутствует закрывающая скобка");
                        }
                        break;
                    default:
                        throw new RuntimeException("P: отсутвует знак = после имени переменной");
                }
                break;
            case RETURN:
                lexer.getToken();
                result.setLeft(new Node(whatEver));
                result.setRight(parseExpr());
                break;
            case SCANF:
            case PRINTF:
                lexer.getToken();
                Token<?> openBracketToken = lexer.getToken();
                if (!openBracketToken.match(TokenType.BRACKET_OPEN)) {
                    throw new RuntimeException("P: ожидалась открывающаяся скобка после printf");
                }

                result.setLeft(new Node(whatEver));

                result.setRight(parseInOut(whatEver));

                Token<?> closeBracketToken = lexer.getToken();
                if (!closeBracketToken.match(TokenType.BRACKET_CLOSE)) {
                    throw new RuntimeException("P: ожидалась закрывающаяся скобка после printf");
                }
                break;
            case IF:
                lexer.getToken();
                Token<?> openBracketIfToken = lexer.getToken();
                if (!openBracketIfToken.match(TokenType.BRACKET_OPEN)) {
                    throw new RuntimeException("P: ожидалась открывающаяся скобка после if");
                }

                result.setLeft(new Node(whatEver));
                result.setRight(parseCondition());

                Token<?> closeBracketIfToken = lexer.getToken();
                if (!closeBracketIfToken.match(TokenType.BRACKET_CLOSE)) {
                    throw new RuntimeException("P: ожидалась закрывающаяся скобка после условия");
                }

                Token<?> openBracketThenToken = lexer.getToken();
                if (!openBracketThenToken.match(TokenType.BRACE_OPEN)) {
                    throw new RuntimeException("P: ожидалась открывающаяся фигурная скобка ветви if");
                }
                // ветвь then
                result.setRight(parseBody("then"));
                Token<?> closeBracketThenToken = lexer.getToken();
                if (!closeBracketThenToken.match(TokenType.BRACE_CLOSE)) {
                    throw new RuntimeException("P: ожидалась закрывающаяся фигурная скобка ветви if");
                }

                // проверка на else
                Token<?> elseToken = lexer.peekToken();
                if (elseToken.match(TokenType.ELSE)) {
                    // есть else ветвь
                    lexer.getToken();
                    Token<?> openBraceElseToken = lexer.getToken();
                    if (!openBraceElseToken.match(TokenType.BRACE_OPEN)) {
                        throw new RuntimeException("P: ожидалась открывающаяся фигурная скобка после else");
                    }
                    result.setRight(parseBody("else"));
                    Token<?> closeBraceElseToken = lexer.getToken();
                    if (!closeBraceElseToken.match(TokenType.BRACE_CLOSE)) {
                        throw new RuntimeException("P: ожидалась закрывающаяся фигурная скобка ветви else");
                    }
                }
                break;
            case WHILE:
                lexer.getToken();
                Token<?> openBracketWhileToken = lexer.getToken();
                if (!openBracketWhileToken.match(TokenType.BRACKET_OPEN)) {
                    throw new RuntimeException("P: ожидалась открывающаяся скобка после while");
                }

                result.setLeft(new Node(whatEver));
                result.setRight(parseCondition());

                Token<?> closeBracketWhileToken = lexer.getToken();
                if (!closeBracketWhileToken.match(TokenType.BRACKET_CLOSE)) {
                    throw new RuntimeException("P: ожидалась закрывающаяся скобка после условия");
                }

                Token<?> openBraceWhileToken = lexer.getToken();
                if (!openBraceWhileToken.match(TokenType.BRACE_OPEN)) {
                    throw new RuntimeException("P: ожидалась открывающаяся фигурная скобка тела цикла");
                }

                result.setRight(parseBody("def"));

                Token<?> closeBraceWhileToken = lexer.getToken();
                if (!closeBraceWhileToken.match(TokenType.BRACE_CLOSE)) {
                    throw new RuntimeException("P: ожидалась закрывающаяся фигурная скобка тела цикла");
                }
                break;
            default:
                throw new RuntimeException("P: неизвестная команда или не закрыто тело функции");
        }
        result.setRight(new Node(TokenType.EMPTY));
        return result;
    }


    private void parseStrStr(Node assigment){


        lexer.getToken();

        Token openBracketToken = lexer.getToken();
        if(!openBracketToken.match(TokenType.BRACKET_OPEN)){
            throw new RuntimeException("ошибка ( скобки");
        }
        Token name1Token = lexer.getToken();

        if(!name1Token.match(TokenType.NAME)){
            throw new RuntimeException("ошибка первого аргумента");
        }

        assigment.setRight(new Node(name1Token));

        Token commaToken = lexer.getToken();

        if(!commaToken.match(TokenType.COMMA)){
            throw new RuntimeException("ошибка запятая между аргументами аргумента");
        }

        Token name2Token = lexer.getToken();

        if(!name2Token.match(TokenType.NAME)){
            throw new RuntimeException("ошибка второго аргумента");
        }

        assigment.setRight(new Node(name2Token));

        Token closeBracketToken = lexer.getToken();
        if(!closeBracketToken.match(TokenType.BRACKET_CLOSE)){
            throw new RuntimeException("ошибка ) скобки");
        }
    }

    private Node parseInOut(Token out) {
        Node result;

        switch (out.getTokenType()){
            case PRINTF:
                result = new Node(TokenType.PRINTF_BODY);
                break;
            case SCANF:
                result = new Node(TokenType.SCANF_BODY);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + out.getTokenType());
        }

        Token<?> literalToken = lexer.getToken();
        if (literalToken.match(TokenType.LITERAL)) {
            result.setRight(new Node(literalToken));
        }

        // TODO: 06.05.2020 возможно научить выводить массив по индексам
        Token<?> commaToken = lexer.peekToken();

        if (commaToken.match(TokenType.BRACKET_CLOSE)) {
            return result;
        } else if (commaToken.match(TokenType.COMMA)) {
            lexer.getToken();
            while (!lexer.peekToken().match(TokenType.BRACKET_CLOSE)) {

                Token<?> tokenName = lexer.getToken();
                if (tokenName.match(TokenType.NAME)) {
                    result.setRight(new Node(tokenName));
                }
            }
        }
        return result;
    }

    private Node parseArrayAssigment() {
        Node result = new Node(TokenType.ARRAYASSIGMENT);

        Token<?> numberToken = lexer.getToken();

        switch (numberToken.getTokenType()){
            case NUMBER:
            case NAME:
                result.setRight(new Node(numberToken));
                break;
            default:
                throw new RuntimeException("P: не указан индекс массива");
        }

        Token<?> bracketClose = lexer.getToken();
        if (!bracketClose.match(TokenType.BRACET_CLOSE)) {
            throw new RuntimeException("P: не закрыта фигурная скобка индекса массива");
        }

        Token<?> assignToken = lexer.getToken();

        if (assignToken.match(TokenType.ASSIGNMENT)) {

            Node assigment = new Node(assignToken);
            switch (lexer.peekToken().getTokenType()) {
                //case CHAR:
                case NUMBER:
                case NAME:
                    assigment.setRight(parseExpr());
                    break;
                default:
                    throw new RuntimeException("P: неизвестный тип");
            }
            result.setRight(assigment);
        } else {
            throw new RuntimeException("P: отсутсвует знак = у массива");
        }
        return result;
    }


    public Node parseArray() {
        Node result = new Node(TokenType.ARRAY);

        Token<?>  tet = lexer.getToken();
        Token<?> numberToken = lexer.getToken();

        switch (numberToken.getTokenType()){
            case NUMBER:
            case NAME:
                result.setLeft(new Node(numberToken));
                break;
            default:
                throw new RuntimeException("P: не указан индекс массива");
        }

        Token<?> closeBracetToken = lexer.getToken();
        if (closeBracetToken.match(TokenType.BRACET_CLOSE)) {

            Token<?> assignToken = lexer.peekToken();

            switch (assignToken.getTokenType()){
                case ASSIGNMENT:
                    result.setRight(parseArrayBody());
                    break;
                case BRACKET_CLOSE:
                case SEMICOLON:
                case SIGN:
                    return result;
                default:
                    throw new RuntimeException("P: Ошибка массива");
            }
        }
        return result;
    }

    private Node parseArrayBody() {
        Node result = new Node(TokenType.ARRAY_BODY);

        lexer.getToken();
        Token<?> openBraceToken = lexer.getToken();

        switch (openBraceToken.getTokenType()) {
            case BRACE_OPEN:
                while (!lexer.peekToken().match(TokenType.BRACE_CLOSE)) {

                    Token<?> numberToken = lexer.getToken();
                    if (numberToken.match(TokenType.NUMBER)) {
                        result.setRight(new Node(numberToken));
                    }
                    Token<?> commaToken = lexer.getToken();
                    if (commaToken.match(TokenType.BRACE_CLOSE)) {
                        break;
                    }
                }
                break;
            case LITERAL:
                result.setRight(new Node(openBraceToken));
                break;
            default:
                throw new RuntimeException("P: Ошибка тела массива");
        }
        return result;
    }

    private Node parseCondition() {
        Node result = new Node(TokenType.CONDITION);

        result.setLeft(parseExpr());
        Token<?> signToken = lexer.getToken();
        if (!signToken.match(TokenType.SIGN)) {
            throw new RuntimeException("P: ожидался знак условия");
        }
        result.setRight(new Node(signToken));
        result.setRight(parseExpr());
        result.setRight(new Node(TokenType.EMPTY));
        return result;
    }

    public Node parseArgList() {
        Token<?> closeBracketToken = lexer.peekToken();

        Node result = new Node(TokenType.ARG_LIST, closeBracketToken.getRow(), closeBracketToken.getCol() - 1);

        if (closeBracketToken.match(TokenType.BRACKET_CLOSE)) {
            result.setLeft(new Node(TokenType.EMPTY));
            return result;
        }

        Token<?> commaToken;
        do {
            result.setRight(parseExpr());
            commaToken = lexer.peekToken();
        } while (commaToken.match(TokenType.COMMA) && commaToken == lexer.getToken());

        if (!commaToken.match(TokenType.BRACKET_CLOSE)) {
            throw new RuntimeException("P: отсутствует )");
        }

        return result;
    }

    public Node parseType() {
        Node result;

        Token<?> typeToken = lexer.getToken();

        switch (typeToken.getTokenType()) {
            case INT:
            //case DOUBLE:
            case VOID:
            case CHAR:
                Node specificType = new Node(typeToken);
                result = new Node(TokenType.TYPE);
                result.setLeft(specificType);
                break;
            default:
                throw new RuntimeException("P: неизвестный тип");
        }
        return result;
    }

    public Node parseExpr() {
        Node result = parseTerm();

        Token<?> curToken = lexer.peekToken();
        while (curToken.match(TokenType.PLUS) ||
                curToken.match(TokenType.MINUS)) {

            lexer.getToken();

            Node sign = new Node(curToken);
            sign.setLeft(result);
            sign.setRight(parseTerm());

            result = sign;

            curToken = lexer.peekToken();
        }

        return result;
    }

    public Node parseTerm() {
        Node result = parseFactor();

        Token<?> curToken = lexer.peekToken();
        while (curToken.match(TokenType.MULTIPLICATION) ||
                curToken.match(TokenType.DIVISION)) {

            lexer.getToken();

            Node sign = new Node(curToken);
            sign.setLeft(result);
            sign.setRight(parseFactor());

            result = sign;

            curToken = lexer.peekToken();
        }
        return result;
    }

    public Node parseFactor() {
        Node result = parsePower();

        Token<?> curToken = lexer.peekToken();
        if (curToken.match(TokenType.EXPONENTIATION)) {
            lexer.getToken();

            Node exp = new Node(curToken);
            exp.setLeft(result);
            exp.setRight(parseFactor());

            result = exp;
        }
        return result;
    }

    public Node parsePower() {
        Token<?> curToken = lexer.peekToken();

        if (curToken.match(TokenType.MINUS)) {
            lexer.getToken();

            Node minus = new Node(curToken);
            minus.setLeft(parseAtom());
            return minus;
        }

        return parseAtom();
    }

    public Node parseAtom() {
        Node result;

        Token<?> token = lexer.getToken();
        switch (token.getTokenType()) {
            case BRACKET_OPEN:
                result = parseExpr();
                token = lexer.getToken();
                if (token.match(TokenType.BRACKET_CLOSE)) {
                    return result;
                } else {
                    throw new RuntimeException("P: отсутсвует закрывающая скобка");
                }
            case LITERAL:
            case CHAR:
            case NUMBER:
                result = new Node(token);
                break;
            case NAME:
                Token<?> nextToken = lexer.peekToken();
                
                switch (nextToken.getTokenType()){
                    case BRACKET_OPEN:
                        lexer.getToken();
                        result = new Node(TokenType.CALL_FUNCTION);
                        result.setLeft(new Node(token));
                        result.setRight(parseArgList());

                        nextToken = lexer.getToken();
                        if (!nextToken.match(TokenType.BRACKET_CLOSE)) {
                            throw new RuntimeException("P: отсвутствует закрывающая скобка");
                        }
                        break;
                    // TODO: 06.05.2020 возможно поменять
                    case BRACET_OPEN:
                        result = new Node(token);
                        result.setLeft(parseArray());
                        break;
                    default:
                        result = new Node(token);
                        break;
                }

                break;
            default:
                throw new RuntimeException("P: неизвестный тип");
        }
        return result;
    }

    private boolean checkExceptionSemicolon(Node command) {
        return (!(command.getFirstChildren().getTokenType() == TokenType.IF ||
                command.getFirstChildren().getTokenType() == TokenType.WHILE));
    }
}
