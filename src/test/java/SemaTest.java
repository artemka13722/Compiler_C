import buffer.Buffer;
import idTable.IdTable;
import idTable.Variable;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import org.junit.Assert;
import org.junit.Test;
import parser.Node;
import parser.Parser;
import semantic.Sema;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemaTest {


    @Test
    public void testParamsFunction(){

        Sema sema = new Sema();

        Node params = new Node(TokenType.PARAMS_LIST);

        Node param1 = new Node(TokenType.PARAM);
        Node name1 = new Node(new Token<>(TokenType.NAME, "b"));
        name1.setLeft(new Node(new Token<>(TokenType.INT)));
        param1.setRight(name1);

        Node param2 = new Node(TokenType.PARAM);
        Node name2 = new Node(new Token<>(TokenType.NAME, "a"));
        name2.setLeft(new Node(new Token<>(TokenType.CHAR)));
        param2.setRight(name2);

        params.setLeft(param1);
        params.setRight(param2);

        sema.paramsCounter(params, "test");

        int testCount = sema.getFunctionCount().get("test");

        List<TokenType> listType = sema.getFunctionParams().get("test");
        List<TokenType> testType = new ArrayList<>();
        testType.add(TokenType.INT);
        testType.add(TokenType.CHAR);

        Assert.assertEquals(testType, listType);
        Assert.assertEquals(testCount, 2);
    }

    @Test
    public void testConvertType(){

        Sema sema = new Sema();
        TokenType testType = sema.typeCheckType(TokenType.INT, TokenType.DOUBLE);
        Assert.assertEquals(testType, TokenType.INTTODOUBLE);
    }

    @Test
    public void testArray() throws CloneNotSupportedException {
        Sema sema = new Sema();

        Node array = new Node(TokenType.ARRAY);
        array.setLeft(new Node(new Token<>(TokenType.NUMBER, 10)));

        sema.setNameVariable("a");

        Map<String, String> testArray = new HashMap<>();
        testArray.put("a", "10");

        sema.array(array);

        Assert.assertEquals(testArray, sema.getArrays());
    }

    @Test
    public void testNameFunction() throws CloneNotSupportedException {

        Sema sema = new Sema();

        Node fun = new Node(new Token<>(TokenType.NAME, "test"));

        Map<String, List<Variable>> testIdTable = new HashMap<>();

        List<Variable> testList = new ArrayList<>();
        testList.add(new Variable("0a", TokenType.INT));
        testIdTable.put("test", testList);

        sema.setIdTableSema(testIdTable);

        String nameFunc = sema.nameFunction(fun);
        Assert.assertEquals(nameFunc, "test");
    }

    @Test
    public void testGetNameType1(){

        Sema sema = new Sema();

        Map<String, List<Variable>> testIdTable = new HashMap<>();

        List<Variable> testList = new ArrayList<>();
        testList.add(new Variable("1a", TokenType.INT));
        testIdTable.put("test", testList);

        sema.setIdTableSema(testIdTable);

        TokenType testType = sema.getTokenType("1a", "test");
        Assert.assertEquals(testType, TokenType.INT);
    }

    @Test
    public void testGetNameType2(){

        Sema sema = new Sema();

        Map<String, List<Variable>> testIdTable = new HashMap<>();

        List<Variable> testList = new ArrayList<>();
        testList.add(new Variable("1a", TokenType.VOID));
        testIdTable.put("test", testList);

        sema.setIdTableSema(testIdTable);

        TokenType testType = sema.getTokenType("1a", "test");
        Assert.assertEquals(testType, TokenType.VOID);
    }

    @Test
    public void testGetNameType3(){

        Sema sema = new Sema();

        Map<String, List<Variable>> testIdTable = new HashMap<>();

        List<Variable> testList = new ArrayList<>();
        testList.add(new Variable("1a", TokenType.CHAR));
        testIdTable.put("test", testList);

        sema.setIdTableSema(testIdTable);

        TokenType testType = sema.getTokenType("1a", "test");
        Assert.assertEquals(testType, TokenType.CHAR);
    }

    @Test
    public void testConvertValue1(){

        Sema sema = new Sema();
        String testValue = sema.convertTypeValue(TokenType.DOUBLE, TokenType.INT, "10.2");
        Assert.assertEquals(testValue, "10");
    }

    @Test
    public void testConvertValue2(){

        Sema sema = new Sema();
        String testValue = sema.convertTypeValue(TokenType.INT, TokenType.DOUBLE, "10");
        Assert.assertEquals(testValue, "10.0");
    }

    @Test
    public void testConvertValue3(){

        Sema sema = new Sema();
        String testValue = sema.convertTypeValue(TokenType.CHAR, TokenType.INT, "a");
        Assert.assertEquals(testValue, "97");
    }

    @Test
    public void testConvertValue4(){

        Sema sema = new Sema();
        String testValue = sema.convertTypeValue(TokenType.CHAR, TokenType.DOUBLE, "a");
        Assert.assertEquals(testValue, "97.0");
    }

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
        body.setRight(empty);

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
        body.setRight(empty);

        function.setLeft(typeFunction);
        function.setRight(typeName);
        function.setRight(paramsList);
        function.setRight(body);

        program.setLeft(function);
        program.setRight(empty);

        Assert.assertEquals(program, semaTree);
    }

    @Test
    public void testFunction() throws CloneNotSupportedException {

        Buffer buffer = new Buffer(new StringReader("" +
                "void test(int a){}" +
                "int main(){" +
                "test(42);" +
                "}"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);
        Node programTree = parser.parseProgram();

        IdTable idTable = new IdTable(programTree);

        Sema sema = new Sema(programTree, idTable.getIdTable());
        Node semaTree = sema.getTree();


        Node program = new Node(TokenType.PROGRAM);
        Node empty = new Node(TokenType.EMPTY);

        Node function1 = new Node(new Token<>(TokenType.FUNCTION, "test"));
        Node typeFunction1 = new Node(TokenType.TYPE);
        typeFunction1.setLeft(new Node(TokenType.VOID));

        Node typeName1 = new Node(TokenType.VOID);
        Node nameFunction1 = new Node(new Token<>(TokenType.NAME, "test"));
        typeName1.setLeft(nameFunction1);

        Node paramsList1 = new Node(TokenType.PARAMS_LIST);
        Node paramType = new Node(new Token<>(TokenType.PARAM, "a"));

        Node typeparam1 = new Node(TokenType.TYPE);
        typeparam1.setLeft(new Node(TokenType.INT));

        paramType.setLeft(typeparam1);
        paramsList1.setLeft(paramType);

        Node body1 = new Node(TokenType.BODY);
        body1.setLeft(empty);

        function1.setRight(typeFunction1);
        function1.setRight(typeName1);
        function1.setRight(paramsList1);
        function1.setRight(body1);


        Node function2 = new Node(new Token<>(TokenType.FUNCTION, "main"));

        Node typeFunction2 = new Node(TokenType.TYPE);
        typeFunction2.setLeft(new Node(TokenType.INT));

        Node typeName2 = new Node(TokenType.INT);
        Node nameFunction2 = new Node(new Token<>(TokenType.NAME, "main"));
        typeName2.setLeft(nameFunction2);

        Node paramsList2 = new Node(TokenType.PARAMS_LIST);
        paramsList2.setLeft(empty);

        Node body2 = new Node(TokenType.BODY);

        Node callFucntion = new Node(TokenType.CALL_FUNCTION);
        callFucntion.setLeft(typeName1);

        Node argList = new Node(TokenType.ARG_LIST);
        Node nubmer = new Node(TokenType.INT);
        nubmer.setLeft(new Node(new Token<>(TokenType.NUMBER, 42)));
        argList.setLeft(nubmer);

        Node commandFunction = new Node(TokenType.COMMAND);
        commandFunction.setLeft(empty);

        callFucntion.setRight(argList);
        callFucntion.setRight(empty);

        body2.setLeft(callFucntion);
        body2.setRight(commandFunction);
        body2.setRight(empty);


        function2.setLeft(typeFunction2);
        function2.setRight(typeName2);
        function2.setRight(paramsList2);
        function2.setRight(body2);

        program.setLeft(function1);
        program.setRight(function2);
        program.setRight(empty);


        Assert.assertEquals(program, semaTree);
    }

}
