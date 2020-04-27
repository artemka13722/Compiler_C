import buffer.Buffer;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import org.junit.Assert;
import org.junit.Test;
import parser.Node;
import parser.Parser;

import java.io.StringReader;

public class ParserTest {

    @Test
    public void testEmptyProgram() {
        Buffer buffer = new Buffer(new StringReader(""));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Token<?> programToken = new Token<Object>(TokenType.PROGRAM);
        Node tree = new Node(programToken);
        tree.setLeft(new Node(TokenType.EMPTY));

        Assert.assertEquals(tree, parser.parseProgram());
    }

    @Test
    public void testWhile() {
        Buffer buffer = new Buffer(new StringReader("while(a > b){}"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node command = new Node(new Token<String>(TokenType.COMMAND));
        Node WHILE = new Node(new Token<String>(TokenType.WHILE));

        Node condition = new Node(new Token<String>(TokenType.CONDITION)); // три ответвления
        Node name1 = new Node(new Token<String>(TokenType.NAME, "a"));
        Node sing = new Node(new Token<String>(TokenType.SIGN, ">"));
        Node name2 = new Node(new Token<String>(TokenType.NAME, "b"));

        Node body = new Node(new Token<String>(TokenType.BODY));
        Node empty = new Node(new Token<String>(TokenType.EMPTY));


        body.setLeft(empty);

        condition.setLeft(name1);
        condition.setRight(sing);
        condition.setRight(name2);

        command.setLeft(WHILE);
        command.setRight(condition);
        command.setRight(body);
        command.setRight(empty);


        Assert.assertEquals(command, parser.parseCommand());
    }

    @Test
    public void testIfElse() {
        Buffer buffer = new Buffer(new StringReader("if (a > b)" +
                "{" +
                "}" +
                "else" +
                "{" +
                "}"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node command = new Node(TokenType.COMMAND);
        Node IF = new Node(TokenType.IF);

        Node condition = new Node(TokenType.CONDITION);
        Node name1 = new Node(new Token<String>(TokenType.NAME, "a"));
        Node sing = new Node(new Token<String>(TokenType.SIGN, ">"));
        Node name2 = new Node(new Token<String>(TokenType.NAME, "b"));

        Node body1 = new Node(TokenType.BODY);
        Node empty = new Node(TokenType.EMPTY);
        Node body2 = new Node(TokenType.BODY);

        body1.setLeft(empty);
        body2.setLeft(empty);

        condition.setLeft(name1);
        condition.setRight(sing);
        condition.setRight(name2);

        command.setLeft(IF);
        command.setRight(condition);
        command.setRight(body1);
        command.setRight(body2);
        command.setRight(empty);

        Assert.assertEquals(command, parser.parseCommand());
    }

    @Test
    public void testPrintf() {
        Buffer buffer = new Buffer(new StringReader("printf(\"test\");"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node command = new Node(new Token<String>(TokenType.COMMAND));
        Node printf = new Node(new Token<String>(TokenType.PRINTF));
        Node printfBody = new Node(new Token<String>(TokenType.PRINTF_BODY));
        Node literal = new Node(new Token<String>(TokenType.LITERAL, "test"));
        Node empty = new Node(TokenType.EMPTY);

        printfBody.setLeft(literal);
        command.setLeft(printf);
        command.setRight(printfBody);
        command.setRight(empty);

        Node realTree = parser.parseCommand();

        Assert.assertEquals(command, realTree);
    }

    @Test
    public void testPrintfArg() {
        Buffer buffer = new Buffer(new StringReader("printf(\"test\", a);"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node command = new Node(new Token<String>(TokenType.COMMAND));
        Node printf = new Node(new Token<String>(TokenType.PRINTF));
        Node printfBody = new Node(new Token<String>(TokenType.PRINTF_BODY));
        Node literal = new Node(new Token<String>(TokenType.LITERAL, "test"));
        Node name = new Node(new Token<String>(TokenType.NAME, "a"));
        Node empty = new Node(TokenType.EMPTY);

        printfBody.setLeft(literal);
        printfBody.setRight(name);
        command.setLeft(printf);
        command.setRight(printfBody);
        command.setRight(empty);

        Assert.assertEquals(command, parser.parseCommand());
    }

    @Test
    public void testScanf() {
        Buffer buffer = new Buffer(new StringReader("scanf(\"test\", a);"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node command = new Node(new Token<String>(TokenType.COMMAND));
        Node scanf = new Node(new Token<String>(TokenType.SCANF));
        Node scanfBody = new Node(new Token<String>(TokenType.SCANF_BODY));
        Node literal = new Node(new Token<String>(TokenType.LITERAL, "test"));
        Node name = new Node(new Token<String>(TokenType.NAME, "a"));
        Node empty = new Node(TokenType.EMPTY);

        scanfBody.setLeft(literal);
        scanfBody.setRight(name);
        command.setLeft(scanf);
        command.setRight(scanfBody);
        command.setRight(empty);;

        Node realTree = parser.parseCommand();

        Assert.assertEquals(command, realTree);
    }

    @Test
    public void testCharAssigment() {
        Buffer buffer = new Buffer(new StringReader("Char[10] = 't';"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node command = new Node(TokenType.COMMAND);
        Node num1 = new Node(new Token<Integer>(TokenType.NUMBER, 10));
        Node assigment = new Node(TokenType.ASSIGNMENT);
        Node character = new Node(new Token<Character>(TokenType.CHAR, 't'));
        Node empty = new Node(TokenType.EMPTY);
        Node arrayAssigment = new Node(TokenType.ARRAYASSIGMENT);
        Node name = new Node(new Token<>(TokenType.NAME, "Char"));

        arrayAssigment.setLeft(num1);
        arrayAssigment.setRight(assigment);
        arrayAssigment.setRight(character);

        command.setLeft(name);
        command.setRight(arrayAssigment);
        command.setRight(empty);

        Node realTree = parser.parseCommand();
        Assert.assertEquals(command, realTree);
    }

    @Test
    public void testArray() {
        Buffer buffer = new Buffer(new StringReader("[1] = {1};"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node tree = new Node(new Token<String>(TokenType.ARRAY));
        Node num1 = new Node(new Token<Integer>(TokenType.NUMBER, 1));
        Node arrayBody = new Node(new Token<String>(TokenType.ARRAY_BODY));
        Node num2 = new Node(new Token<Integer>(TokenType.NUMBER , 1));

        arrayBody.setLeft(num2);
        tree.setLeft(num1);
        tree.setRight(arrayBody);

        Assert.assertEquals(tree, parser.parseArray());
    }

    @Test
    public void testCharArray() {
        Buffer buffer = new Buffer(new StringReader("[10] = \"test\";"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node tree = new Node(new Token<String>(TokenType.ARRAY));
        Node num1 = new Node(new Token<Integer>(TokenType.NUMBER, 10));
        Node arrayBody = new Node(new Token<String>(TokenType.ARRAY_BODY));
        Node literal = new Node(new Token<String>(TokenType.LITERAL, "test"));

        arrayBody.setLeft(literal);
        tree.setLeft(num1);
        tree.setRight(arrayBody);

        Node realTree = parser.parseArray();

        Assert.assertEquals(tree, realTree);
    }

    @Test
    public void testNumber() {
        Buffer buffer = new Buffer(new StringReader("42"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node tree = new Node(new Token<Integer>(TokenType.NUMBER, 42));

        Assert.assertEquals(tree, parser.parseExpr());
    }

    @Test
    public void testNegativeNumber() {
        Buffer buffer = new Buffer(new StringReader("-42"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node numNode = new Node(new Token<Integer>(TokenType.NUMBER, 42));

        Node tree = new Node(new Token<String>(TokenType.MINUS));
        tree.setLeft(numNode);

        Assert.assertEquals(tree, parser.parseExpr());
    }

    @Test
    public void testMULTIPLICATION() {
        Buffer buffer = new Buffer(new StringReader("5*2"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node numNode1 = new Node(new Token<Integer>(TokenType.NUMBER, 5));
        Node numNode2 = new Node(new Token<Integer>(TokenType.NUMBER, 2));

        Node tree = new Node(new Token<String>(TokenType.MULTIPLICATION));
        tree.setLeft(numNode1);
        tree.setRight(numNode2);

        Assert.assertEquals(tree, parser.parseExpr());
    }

    @Test
    public void testDIVISION() {
        Buffer buffer = new Buffer(new StringReader("20/2"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node numNode1 = new Node(new Token<Integer>(TokenType.NUMBER, 20));
        Node numNode2 = new Node(new Token<Integer>(TokenType.NUMBER, 2));

        Node tree = new Node(new Token<String>(TokenType.DIVISION));
        tree.setLeft(numNode1);
        tree.setRight(numNode2);

        Assert.assertEquals(tree, parser.parseExpr());
    }

    @Test
    public void testEXPONENTIATION() {
        Buffer buffer = new Buffer(new StringReader("2^3"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node numNode1 = new Node(new Token<Integer>(TokenType.NUMBER, 2));
        Node numNode2 = new Node(new Token<Integer>(TokenType.NUMBER, 3));

        Node tree = new Node(new Token<String>(TokenType.EXPONENTIATION));
        tree.setLeft(numNode1);
        tree.setRight(numNode2);

        Assert.assertEquals(tree, parser.parseExpr());
    }

    @Test
    public void testBRACKET() {
        Buffer buffer = new Buffer(new StringReader("(2)"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node tree = new Node(new Token<Integer>(TokenType.NUMBER, 2));

        Assert.assertEquals(tree, parser.parseExpr());
    }

    @Test
    public void testManyBRACKET() {
        Buffer buffer = new Buffer(new StringReader("(((2)))"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node tree = new Node(new Token<Integer>(TokenType.NUMBER, 2));

        Assert.assertEquals(tree, parser.parseExpr());
    }

    @Test
    public void testDoubleNumber() {
        Buffer buffer = new Buffer(new StringReader("1.0001"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node tree = new Node(new Token<Double>(TokenType.NUMBER, 1.0001));

        Assert.assertEquals(tree, parser.parseExpr());
    }
}
