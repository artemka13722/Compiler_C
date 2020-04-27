package semantic;

import idTable.Variable;
import lexer.Token;
import lexer.TokenType;
import parser.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sema {

    // придумать что нибудь по лучше
    private String nameFunction;
    private String nameVariable;
    private boolean assigment = false;
    private boolean returned = false;

    private Node buffer;
    private Node tree;

    private Map<String, Integer> functionCount;

    private Map<String, List<Variable>> idTableSema;

    private Map<String, List<TokenType>> functionParams;

    private Map<String, String> arrays;

    private Map<Integer, Character> subLevel;
    private Integer level;

    public Sema(Node tree, Map<String, List<Variable>> idTableSema) throws CloneNotSupportedException {
        this.tree = tree.clone();
        this.idTableSema = idTableSema;
        this.subLevel = new HashMap<>();
        this.level = 0;
        this.functionCount = new HashMap<>();
        this.arrays = new HashMap<>();
        this.functionParams = new HashMap<>();
        analyze();
    }

    private static boolean checkDouble(String str) {
        try {
            double d = Integer.parseInt(str);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    public Node getTree() {
        return tree;
    }

    public Node getBuffer() {
        return buffer;
    }

    public void setBuffer(Node buffer) {
        this.buffer = buffer;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void addSubLevel(Integer level) {
        Character subLvl = this.subLevel.get(level);

        if (subLvl == null) {
            subLevel.put(level, 'a');
        } else if (level == 0) {
            subLevel.put(level, 'a');
        } else {
            subLvl = (char) (subLvl + 1);
            subLevel.put(level, subLvl);
        }
    }

    public void remSubLevel(Integer level) {
        Character subLvl = this.subLevel.get(level);
        subLvl = (char) (subLvl - 1);
        this.subLevel.put(level, subLvl);
    }

    // проверить что тут происходит
    public void analyze() throws CloneNotSupportedException {
        List<Node> listChild;
        if (tree != null) {
            listChild = tree.getListChild();
            for (Node child : listChild) {
                if (child.getTokenType() == TokenType.FUNCTION) {
                    body(child);
                }
            }
        }
    }

    public void body(Node childFunc) throws CloneNotSupportedException {
        addSubLevel(level);
        String name = null;
        for (Node child : childFunc.getListChild()) {
            switch (child.getTokenType()) {
                case BODY:
                    bodyRec(child);
                    break;
                case NAME:
                    name = bodyName(child, childFunc);
                    break;
                case PARAMS_LIST:
                    paramsCounter(child, name);
                    break;
            }
        }
    }

    public void paramsCounter(Node child, String name){
        if (!child.getFirstChildren().getTokenType().equals(TokenType.EMPTY)) {
            int countParams = 0;
            for (Node params : child.getListChild()) {
                if (params.getTokenType() == TokenType.PARAM) {
                    countParams++;
                }
            }
            functionCount.put(name, countParams);
            parasList(child, name);
        } else {
            functionCount.put(name, 0);
        }
    }

    public void parasList(Node params, String name) {

        ArrayList parameters = new ArrayList();

        for (Node param : params.getListChild()) {
            TokenType type = param.getFirstChildren().getFirstChildren().getTokenType();
            parameters.add(type);
        }
        functionParams.put(name, parameters);
    }

    public String bodyName(Node child, Node childFunc) throws CloneNotSupportedException {
        buffer = child.clone();
        String name = childFunc.getTokenValue().toString();
        String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();
        TokenType type = getTokenType(lvl, name);
        typeCheck(type, childFunc);
        child.changeNode(type);
        child.setLeft(getBuffer());
        nameFunction = name;
        functionCount.put(name, 0);
        return name;
    }

    public void bodyRec(Node body) throws CloneNotSupportedException {
        setLevel(getLevel() + 1);
        addSubLevel(level);
        for (Node childBody : body.getListChild()) {
            bodyCommand(childBody);
        }
    }

    public void bodyCommand(Node childBody) throws CloneNotSupportedException {
        switch (childBody.getTokenType()) {
            case COMMAND:
                for (Node childCommand : childBody.getListChild()) {
                    commands(childCommand);
                }
                break;
            case CALL_FUNCTION:
                function(childBody);
                break;
            case EMPTY:
                setLevel(getLevel() - 1);
                break;
        }
    }

    // проверить правильность returned
    public void commands(Node childCommand) throws CloneNotSupportedException {
        switch (childCommand.getTokenType()) {
            case BODY:
                bodyRec(childCommand);
                break;
            case NAME:
                commandName(childCommand);
                break;
            case NUMBER:
                if (returned) {
                    nameVariable = nameFunction;
                    commandRec(childCommand);
                }
                returned = false;
                break;
            case RETURN:
                returned = true;
                break;
            case ASSIGNMENT:
                assigment = true;
                break;
            case ARRAY:
                array(childCommand);
                break;
            case ARRAYASSIGMENT:
                arrayAssigment(childCommand);
                break;
                /*case CONDITION:
                condition(childCommand);
                break;*/
            default:
                commandRec(childCommand);
        }
    }

    public void commandName(Node childCommand) throws CloneNotSupportedException {
        //приведение типов
        buffer = childCommand.clone();
        String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();
        if (assigment || returned) {

            TokenType type1 = getTokenType(lvl, nameVariable);
            typeCheck(type1, childCommand);

            nameVariable = childCommand.getTokenValue().toString();
            TokenType type2 = getTokenType(lvl, nameVariable);

            typeCheck(type2, childCommand);

            TokenType type = typeCheckType(type1, type2);
            childCommand.changeNode(type);
        } else {
            nameVariable = childCommand.getTokenValue().toString();
            TokenType type = getTokenType(lvl, nameVariable);
            typeCheck(type, childCommand);

            // проверка типов
            if (returned) {
                TokenType typeFunction = getTokenType("0a", nameFunction);
                if (typeFunction == TokenType.VOID || type != typeFunction) {
                    System.out.printf((char) 27 + "[31m SEMA: void не может возвращать значения " +
                                    "или возвращаемый тип не равен типу функции LOC<%d:%d>",
                            childCommand.getValue().getRow(), childCommand.getValue().getCol());
                    System.exit(0);
                }
            }
            returned = false;
            childCommand.changeNode(type);
        }
        childCommand.setLeft(getBuffer());
    }

    public TokenType typeCheckType(TokenType in, TokenType out) {
        if (in.equals(out)) {
            return out;
        }
        switch (in) {
            case INT:
                out = convertInt(out);
                break;
            case DOUBLE:
                out = convertDouble(out);
                break;
            case CHAR:
                out = convertChar(out);
        }
        return out;
    }

    public TokenType convertInt(TokenType out) {
        switch (out) {
            case CHAR:
                out = TokenType.INTTOCHAR;
                break;
            case DOUBLE:
                out = TokenType.INTTODOUBLE;
                break;
        }
        return out;
    }

    public TokenType convertDouble(TokenType out) {
        switch (out) {
            case INT:
                out = TokenType.DOUBLETOINT;
                break;
            case CHAR:
                out = TokenType.DOUBLETOCHAR;
                break;
        }
        return out;
    }

    public TokenType convertChar(TokenType out) {
        switch (out) {
            case INT:
                out = TokenType.CHARTOINT;
                break;
            case DOUBLE:
                out = TokenType.CHARTODOUBLE;
                break;
        }
        return out;
    }

    public void typeCheck(TokenType type, Node childCommand) {
        if (type == null) {
            System.out.printf((char) 27 + "[31m SEMA: переменная не была объявлена LOC<%d:%d>",
                    childCommand.getValue().getRow(), childCommand.getValue().getCol());
            System.exit(0);
        }
    }

    // проверка на double не совершенна
    public void array(Node array) throws CloneNotSupportedException {
        String value = array.getFirstChildren().getTokenValue().toString();

        if (value.matches("[\\p{L}| ]+")) {
            arrays.put(nameVariable, value);

        } else if (checkDouble(value)) {
            arrays.put(nameVariable, value);
        } else {
            System.out.print((char) 27 + "[31m SEMA: индекс массива должен быть целочисленным");
            System.exit(0);
        }
        commandRec(array);
    }


    public void arrayAssigment(Node arrayAssigment) throws CloneNotSupportedException {

        for (Node array : arrayAssigment.getListChild()) {
            switch (array.getTokenType()) {
                case NUMBER:
                case NAME:
                    nameArrayAssigment(array);
                    break;
                case ASSIGNMENT:
                    assigment = true;
                    break;
                default:
                    if (assigment) {
                        commandRec(array);
                    }
            }
        }
    }

    public void nameArrayAssigment(Node array) throws CloneNotSupportedException {
        if (assigment) {
            commandRec(array);
        } else {
            int value = 0;
            try {
                value = (Integer) array.getTokenValue();
            } catch (ClassCastException e) {
                //System.out.println("SEMA: переменная ");
            }

            int count = Integer.parseInt(arrays.get(nameVariable));
            if (value > count) {
                System.out.printf((char) 27 + "[31m SEMA: выход за пределы массива LOC<%d:%d>",
                        array.getValue().getRow(), array.getValue().getCol());
                System.exit(0);
            }
        }
    }

    public void function(Node function) throws CloneNotSupportedException {
        String name = null;
        for (Node fun : function.getListChild()) {
            switch (fun.getTokenType()) {
                case NAME:
                    name = nameFunction(fun);
                    break;
                case ARG_LIST:
                    argListFunction(fun, name);
                    break;
            }
        }
    }

    public void argListFunction(Node fun, String name) throws CloneNotSupportedException {
        int countArgs = 0;
        for (Node args : fun.getListChild()) {
            if (args.getTokenType() == TokenType.EMPTY) {
                break;
            }
            countArgs++;
        }
        int count = 0;
        try {
            count = functionCount.get(name);
        } catch (NullPointerException e) {
            System.out.printf((char) 27 + "[31m SEMA: функция должна быть описана выше main LOC<%d:%d>",
                    fun.getValue().getRow(), fun.getValue().getCol());
            System.exit(0);
        }
        if (count != countArgs) {
            System.out.printf((char) 27 + "[31m SEMA: разное количество принимаемых и/или отправляемых аргументов LOC<%d:%d>",
                    fun.getValue().getRow(), fun.getValue().getCol());
            System.exit(0);
        }

        commandRec(fun);

        if (countArgs > 0) {
            List<TokenType> types = new ArrayList<>();
            for (Node typeARGS : fun.getListChild()) {
                types.add(typeARGS.getTokenType());
            }

            List<TokenType> types1 = functionParams.get(name);

            for(int i = 0; i < types1.size(); i++){
                if(!types.get(i).equals(types1.get(i))){
                    System.out.printf((char) 27 + "[31m SEMA: разные типы отправляемых значений в функцию. " +
                                    "Ожидался %s, а на входе %s LOC<%d:%d>",
                            types1.get(i), types.get(i), fun.getValue().getRow(), fun.getValue().getCol());
                    System.exit(0);
                }
            }
        }
    }

    public String nameFunction(Node fun) throws CloneNotSupportedException {
        buffer = fun.clone();
        String name = fun.getTokenValue().toString();
        String lvl = "0a";
        TokenType type = getTokenType(lvl, name);
        typeCheck(type, fun);
        fun.changeNode(type);
        fun.setLeft(getBuffer());
        return name;
    }

    public void commandRec(Node command) throws CloneNotSupportedException {
        if (command != null) {
            if (command.getListChild().size() > 0) {
                for (Node recCommand : command.getListChild()) {
                    commandRec(recCommand);
                }
            } else {
                switch (command.getTokenType()) {
                    case NUMBER:
                        buffer = command.clone();
                        if (assigment) {
                            convertNumber(command);
                        } else {
                            checkTypeNumber(command);
                        }
                        break;
                    case CHAR:
                        buffer = command.clone();
                        if (assigment) {
                            convertChar(command);
                        } else {
                            checkTypeChar(command);
                        }

                        break;
                    case NAME:
                        // добавить приведение типов переменных

                        buffer = command.clone();
                        String name = command.getTokenValue().toString();
                        String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();
                        TokenType type = getTokenType(lvl, name);
                        typeCheck(type, command);
                        command.changeNode(type);
                        command.setLeft(getBuffer());
                        break;
                    case SIGN:
                        assigment = true;
                    case EMPTY:
                        assigment = false;
                        break;
                }
            }
        }

    }

    public void checkTypeChar(Node command) {
        if (command.getTokenValue() instanceof Character) {
            command.changeNode(TokenType.CHAR);
            command.setLeft(getBuffer());
        }
    }

    public void convertChar(Node command) {
        String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();
        TokenType type = getTokenType(lvl, nameVariable);
        String value = command.getTokenValue().toString();
        Token<?> newValue;

        switch (type) {
            case CHAR:
                if (command.getTokenValue() instanceof Character) {
                    command.changeNode(TokenType.CHAR);
                    command.setLeft(getBuffer());
                }
                break;
            case INT:
                value = convertTypeValue(TokenType.CHAR, TokenType.INT, value);
                newValue = new Token<Integer>(TokenType.NUMBER, Integer.valueOf(value));
                buffer.setValue(newValue);
                command.changeNode(TokenType.INT);
                command.setLeft(getBuffer());
                break;
            case DOUBLE:
                value = convertTypeValue(TokenType.CHAR, TokenType.DOUBLE, value);
                newValue = new Token<>(TokenType.NUMBER, Double.valueOf(value));
                buffer.setValue(newValue);
                command.changeNode(TokenType.DOUBLE);
                command.setLeft(getBuffer());
                break;
        }
    }

    public void checkTypeNumber(Node command) {
        if (command.getTokenValue() instanceof Integer) {
            command.changeNode(TokenType.INT);
            command.setLeft(getBuffer());
        }
        if (command.getTokenValue() instanceof Double) {
            command.changeNode(TokenType.DOUBLE);
            command.setLeft(getBuffer());
        }
    }

    public void convertNumber(Node command) {
        String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();
        TokenType type = getTokenType(lvl, nameVariable);
        String value = command.getTokenValue().toString();

        switch (type) {
            case CHAR:
                System.out.println((char) 27 + "[31m SEMA: к char присваивать можно только char");
                System.exit(0);
                break;
            case INT:
                if (!(command.getTokenValue() instanceof Integer)) {
                    value = convertTypeValue(TokenType.DOUBLE, TokenType.INT, value);
                    Token<?> newValue = new Token<>(TokenType.NUMBER, Integer.valueOf(value));
                    buffer.setValue(newValue);
                }
                command.changeNode(TokenType.INT);
                command.setLeft(getBuffer());
                break;
            case DOUBLE:
                if (!(command.getTokenValue() instanceof Double)) {
                    value = convertTypeValue(TokenType.INT, TokenType.DOUBLE, value);
                    Token<?> newValue = new Token<>(TokenType.NUMBER, Double.valueOf(value));
                    buffer.setValue(newValue);
                }
                command.changeNode(TokenType.DOUBLE);
                command.setLeft(getBuffer());
        }
    }

    public String convertTypeValue(TokenType in, TokenType out, String value) {

        if (in.equals(out)) {
            return value;
        } else {
            switch (in) {
                case INT:
                    value = convertValueInt(out, value);
                    break;
                case DOUBLE:
                    value = convertValueDouble(out, value);
                    break;
                case CHAR:
                    value = convertValueChar(out, value);
                    break;
            }
        }
        return value;
    }

    public String convertValueChar(TokenType out, String value) {
        char input = value.charAt(0);
        switch (out) {
            case INT:
                value = String.valueOf((int) input);
                break;
            case DOUBLE:
                value = String.valueOf((double) input);
                break;

        }
        return value;
    }

    public String convertValueDouble(TokenType out, String value) {
        switch (out) {
            case INT:
                double input = Double.parseDouble(value);
                int output = (int) Math.round(input);
                value = String.valueOf(output);
                break;
        }
        return value;
    }

    public String convertValueInt(TokenType out, String value) {
        switch (out) {
            case DOUBLE:
                int input = Integer.parseInt(value);
                value = String.valueOf((double) input);
                break;
        }
        return value;
    }

    public TokenType getTokenType(String lvl, String name) {

        List<Variable> variableList = idTableSema.get(name);

        if (variableList == null) return null;

        for (Variable variable : variableList) {
            if (variable.getValue().equals(lvl)) {
                return variable.getTokenType();
            }
        }

        boolean checkLvl = false;
        char nameLvl = lvl.charAt(1);
        for (Variable variable : variableList) {
            if (variable.getValue().charAt(1) == nameLvl) {
                checkLvl = true;
                break;
            }
        }
        if (!checkLvl) {
            return null;
        }

        int[] mass = new int[variableList.size()];
        for (int i = 0; i < variableList.size(); i++) {
            mass[i] = variableList.get(i).getValue().compareTo(lvl);
        }

        int indexOfMax = 0;
        for (int i = 1; i < mass.length; i++) {
            if (mass[i] > mass[indexOfMax]) {
                indexOfMax = i;
            }
        }
        return variableList.get(indexOfMax).getTokenType();

        //return null;
    }

    public void condition(Node condition) throws CloneNotSupportedException {

        // TODO: 26.04.2020 проверка на тип массива и после какие то действия

        List types = new ArrayList();

        commandRec(condition);

        for (Node type : condition.getListChild()) {
            if (type.getTokenType() != TokenType.SIGN) {
                types.add(type.getTokenType());
            }
        }

        if (!types.get(0).equals(types.get(1))) {
            System.out.println("SEMA : переменные в условии должны быть одного типа");
            System.exit(0);
        }
    }

}
