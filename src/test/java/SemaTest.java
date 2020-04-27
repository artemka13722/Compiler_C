import buffer.Buffer;
import idTable.IdTable;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import org.junit.Assert;
import org.junit.Test;
import parser.Node;
import parser.Parser;
import semantic.Sema;

import java.io.StringReader;

public class SemaTest {

    @Test
    public void testSimpleProgram() throws CloneNotSupportedException {

        Buffer buffer = new Buffer(new StringReader("int main(){}"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);
        Node programTree = parser.parseProgram();

        IdTable idTable = new IdTable(programTree);

        Sema sema = new Sema(programTree, idTable.getIdTable());
        Node semaTree = sema.getTree();


        Node program = new Node(TokenType.PROGRAM);
        Node empty = new Node(TokenType.EMPTY);

        Node function = new Node(new Token<>(TokenType.FUNCTION, "main"));

        Node typeFunction = new Node(TokenType.TYPE);
        typeFunction.setLeft(new Node(TokenType.INT));

        Node typeName = new Node(TokenType.INT);
        Node nameFunction = new Node(new Token<>(TokenType.NAME, "main"));
        typeName.setLeft(nameFunction);

        Node paramsList = new Node(TokenType.PARAMS_LIST);
        paramsList.setLeft(empty);

        Node body = new Node(TokenType.BODY);
        body.setLeft(empty);


        function.setLeft(typeFunction);
        function.setRight(typeName);
        function.setRight(paramsList);
        function.setRight(body);

        program.setLeft(function);
        program.setRight(empty);


        Assert.assertEquals(program, semaTree);
    }

    @Test
    public void testAssigmentNameNumber() throws CloneNotSupportedException {

        Buffer buffer = new Buffer(new StringReader("int main(){" +
                "int a = 3 + 2.2;" +
                "}"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);
        Node programTree = parser.parseProgram();

        IdTable idTable = new IdTable(programTree);

        Sema sema = new Sema(programTree, idTable.getIdTable());
        Node semaTree = sema.getTree();


        Node program = new Node(TokenType.PROGRAM);
        Node empty = new Node(TokenType.EMPTY);

        Node function = new Node(new Token<>(TokenType.FUNCTION, "main"));

        Node typeINT = new Node(TokenType.TYPE);
        typeINT.setLeft(new Node(TokenType.INT));

        Node typeName = new Node(TokenType.INT);
        Node nameFunction = new Node(new Token<>(TokenType.NAME, "main"));
        typeName.setLeft(nameFunction);

        Node paramsList = new Node(TokenType.PARAMS_LIST);
        paramsList.setLeft(empty);

        Node body = new Node(TokenType.BODY);

        Node commandBody1 = new Node(TokenType.COMMAND);
        commandBody1.setLeft(typeINT);

        Node commandInt = new Node(TokenType.INT);
        commandInt.setLeft(new Node(new Token<>(TokenType.NAME, "a")));
        commandBody1.setRight(commandInt);

        commandBody1.setRight(new Node(TokenType.ASSIGNMENT));

        Node plus = new Node(TokenType.PLUS);
        Node num1Type = new Node(TokenType.INT);
        num1Type.setLeft(new Node(new Token<>(TokenType.NUMBER, 3)));
        Node num2Type = new Node(TokenType.INT);
        num2Type.setLeft(new Node(new Token<>(TokenType.NUMBER, 2)));

        plus.setLeft(num1Type);
        plus.setRight(num2Type);

        commandBody1.setRight(plus);
        commandBody1.setRight(empty);

        Node commandBody2 = new Node(TokenType.COMMAND);
        commandBody2.setLeft(empty);

        body.setRight(commandBody1);
        body.setRight(commandBody2);

        function.setLeft(typeINT);
        function.setRight(typeName);
        function.setRight(paramsList);
        function.setRight(body);

        program.setLeft(function);
        program.setRight(empty);

        Assert.assertEquals(program, semaTree);
    }


    @Test
    public void testAssigmentName() throws CloneNotSupportedException {
        Buffer buffer = new Buffer(new StringReader("int main(){" +
                "int a; " +
                "char b; " +
                "a = b;" +
                "}"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);
        Node programTree = parser.parseProgram();

        IdTable idTable = new IdTable(programTree);

        Sema sema = new Sema(programTree, idTable.getIdTable());
        Node semaTree = sema.getTree();


        Node program = new Node(TokenType.PROGRAM);
        Node empty = new Node(TokenType.EMPTY);

        Node function = new Node(new Token<>(TokenType.FUNCTION, "main"));

        Node typeFunction = new Node(TokenType.TYPE);
        typeFunction.setLeft(new Node(TokenType.INT));

        Node typeName = new Node(TokenType.INT);
        Node nameFunction = new Node(new Token<>(TokenType.NAME, "main"));
        typeName.setLeft(nameFunction);

        Node paramsList = new Node(TokenType.PARAMS_LIST);
        paramsList.setLeft(empty);

        Node body = new Node(TokenType.BODY);

        Node command1 = new Node(TokenType.COMMAND);

        Node typeINT = new Node(TokenType.TYPE);
        typeINT.setLeft(new Node(TokenType.INT));
        command1.setLeft(typeINT);

        Node commandInt = new Node(TokenType.INT);
        commandInt.setLeft(new Node(new Token<>(TokenType.NAME, "a")));
        command1.setRight(commandInt);

        command1.setRight(empty);

        Node command2 = new Node(TokenType.COMMAND);

        Node typeChar = new Node(TokenType.TYPE);
        typeChar.setLeft(new Node(TokenType.CHAR));
        command2.setLeft(typeChar);

        Node commandChar = new Node(TokenType.CHAR);
        commandChar.setLeft(new Node(new Token<>(TokenType.NAME, "b")));
        command2.setRight(commandChar);
        command2.setRight(empty);

        Node command3 = new Node(TokenType.COMMAND);

        command3.setLeft(commandInt);
        command3.setRight(new Node(TokenType.ASSIGNMENT));

        Node convert = new Node(TokenType.INTTOCHAR);
        convert.setLeft(new Node(new Token<>(TokenType.NAME, "b")));
        command3.setRight(convert);

        command3.setRight(empty);

        Node command4 = new Node(TokenType.COMMAND);
        command4.setRight(empty);

        body.setRight(command1);
        body.setRight(command2);
        body.setRight(command3);
        body.setRight(command4);

        function.setLeft(typeFunction);
        function.setRight(typeName);
        function.setRight(paramsList);
        function.setRight(body);

        program.setLeft(function);
        program.setRight(empty);

        Assert.assertEquals(program, semaTree);
    }

}
