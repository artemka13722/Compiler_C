package semantic;

import idTable.Variable;
import lexer.Token;
import lexer.TokenType;
import parser.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sema {

    // придумать что нибудь по лучше
    private String nameFunction;
    private String nameVariable;
    private boolean assigment = false;

    private Node buffer;
    private Node tree;

    private Map<String, Integer> functionCount;

    private Map<String, List<Variable>> idTableSema;

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
                    buffer = child.clone();
                    name = childFunc.getTokenValue().toString();
                    String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();
                    TokenType type = getTokenType(lvl, name);
                    if (type == null) {
                        System.out.printf((char) 27 + "[31m SEMA: переменная не была объявлена LOC<%d:%d>", childFunc.getValue().getRow(), childFunc.getValue().getCol());
                        System.exit(0);
                    }
                    child.changeNode(type);
                    child.setLeft(getBuffer());
                    nameFunction = name;
                    functionCount.put(name, 0);
                    break;
                case PARAMS_LIST:

                    // счетчик параметров функции
                    int countParams = 0;
                    for (Node params : child.getListChild()) {
                        if (params.getTokenType() == TokenType.PARAM) {
                            countParams++;
                        }
                    }
                    functionCount.put(name, countParams);
                    break;
            }
        }
    }

    public void bodyRec(Node body) throws CloneNotSupportedException {
        setLevel(getLevel() + 1);
        addSubLevel(level);
        boolean returned = false;
        for (Node childBody : body.getListChild()) {
            switch (childBody.getTokenType()) {
                case COMMAND:
                    for (Node childCommand : childBody.getListChild()) {
                        switch (childCommand.getTokenType()) {
                            case BODY:
                                bodyRec(childCommand);
                                break;
                            case NAME:
                                //приведение типов
                                buffer = childCommand.clone();
                                String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();
                                if (assigment) {

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
                                    childCommand.changeNode(type);
                                }
                                childCommand.setLeft(getBuffer());
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
                    break;
                case CALL_FUNCTION:
                    function(childBody);
                    break;
                case EMPTY:
                    setLevel(getLevel() - 1);
                    break;
            }
        }
    }

    public TokenType typeCheckType(TokenType in, TokenType out) {

        if (in.equals(out)) {
            return out;
        }

        switch (in) {
            case INT:
                switch (out) {
                    case CHAR:
                        out = TokenType.INTTOCHAR;
                        break;
                    case DOUBLE:
                        out = TokenType.INTTODOUBLE;
                        break;
                }

            case DOUBLE:
                switch (out) {
                    case INT:
                        out = TokenType.DOUBLETOINT;
                        break;
                    case CHAR:
                        out = TokenType.DOUBLETOCHAR;
                        break;
                }

            case CHAR:
                switch (out) {
                    case INT:
                        out = TokenType.CHARTOINT;
                        break;
                    case DOUBLE:
                        out = TokenType.CHARTODOUBLE;
                        break;
                }
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

    public void function(Node function) throws CloneNotSupportedException {
        String name = null;
        for (Node fun : function.getListChild()) {
            switch (fun.getTokenType()) {
                case NAME:
                    buffer = fun.clone();
                    name = fun.getTokenValue().toString();
                    String lvl = "0a";
                    TokenType type = getTokenType(lvl, name);
                    if (type == null) {
                        System.out.printf((char) 27 + "[31m SEMA: переменная не была объявлена LOC<%d:%d>",
                                fun.getValue().getRow(), fun.getValue().getCol());
                        System.exit(0);
                    }
                    fun.changeNode(type);
                    fun.setLeft(getBuffer());
                    break;
                case ARG_LIST:
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
                    break;
            }
        }
    }

    // TODO: 23.04.2020 попробовать перевести перебор типов на switch
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

                                        value = convertType(TokenType.DOUBLE, TokenType.INT, value);
                                        Token<?> newValue = new Token<>(TokenType.NUMBER, Integer.valueOf(value));
                                        buffer.setValue(newValue);

                                    }
                                    command.changeNode(TokenType.INT);
                                    command.setLeft(getBuffer());

                                    break;
                                case DOUBLE:
                                    if (!(command.getTokenValue() instanceof Double)) {
                                        value = convertType(TokenType.INT, TokenType.DOUBLE, value);
                                        Token<?> newValue = new Token<>(TokenType.NUMBER, Double.valueOf(value));
                                        buffer.setValue(newValue);
                                    }
                                    command.changeNode(TokenType.DOUBLE);
                                    command.setLeft(getBuffer());
                            }
                        } else {
                            if (command.getTokenValue() instanceof Integer) {
                                command.changeNode(TokenType.INT);
                                command.setLeft(getBuffer());
                            }
                            if (command.getTokenValue() instanceof Double) {
                                command.changeNode(TokenType.DOUBLE);
                                command.setLeft(getBuffer());
                            }
                        }
                        break;
                    case CHAR:
                        buffer = command.clone();

                        if (assigment) {
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
                                    value = convertType(TokenType.CHAR, TokenType.INT, value);
                                    newValue = new Token<Integer>(TokenType.NUMBER, Integer.valueOf(value));
                                    buffer.setValue(newValue);
                                    command.changeNode(TokenType.INT);
                                    command.setLeft(getBuffer());
                                    break;
                                case DOUBLE:
                                    value = convertType(TokenType.CHAR, TokenType.DOUBLE, value);
                                    newValue = new Token<>(TokenType.NUMBER, Double.valueOf(value));
                                    buffer.setValue(newValue);
                                    command.changeNode(TokenType.DOUBLE);
                                    command.setLeft(getBuffer());
                                    break;
                            }
                        } else {
                            if (command.getTokenValue() instanceof Character) {
                                command.changeNode(TokenType.CHAR);
                                command.setLeft(getBuffer());
                            }
                        }

                        break;
                    case NAME:
                        buffer = command.clone();
                        String name = command.getTokenValue().toString();
                        String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();
                        TokenType type = getTokenType(lvl, name);
                        if (type == null) {
                            System.out.printf((char) 27 + "[31m SEMA: переменная не была объявлена LOC<%d:%d>",
                                    command.getValue().getRow(), command.getValue().getCol());
                            System.exit(0);
                        }
                        command.changeNode(type);
                        command.setLeft(getBuffer());
                        break;
                    case EMPTY:
                        assigment = false;
                        break;
                }
            }
        }

    }

    public String convertType(TokenType in, TokenType out, String value) {

        if (in.equals(out)) {
            return value;
        } else {
            switch (in) {
                case INT:
                    switch (out) {
                        case DOUBLE:
                            int input = Integer.parseInt(value);
                            value = String.valueOf((double) input);
                            break;
                    }
                    break;
                case DOUBLE:
                    switch (out) {
                        case INT:
                            double input = Double.parseDouble(value);
                            int output = (int) Math.round(input);
                            value = String.valueOf(output);
                            break;
                    }
                    break;
                case CHAR:
                    char input = value.charAt(0);
                    switch (out) {
                        case INT:
                            value = String.valueOf((int) input);
                            break;
                        case DOUBLE:
                            value = String.valueOf((double) input);
                            break;

                    }
                    break;
            }
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

}
