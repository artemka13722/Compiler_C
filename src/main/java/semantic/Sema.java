package semantic;

import idTable.Varible;
import lexer.TokenType;
import parser.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sema {

    private Node buffer;
    private Node tree;

    private Map<String, Integer> functionCount;

    private Map<String, List<Varible>> idTableSema;

    private Map<Integer, Character> subLevel;
    private Integer level;

    public Sema(Node tree, Map<String, List<Varible>> idTableSema) throws CloneNotSupportedException {
        this.tree = tree.clone();
        this.idTableSema = idTableSema;
        this.subLevel = new HashMap<>();
        this.level = 0;
        this.functionCount = new HashMap<>();
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
            subLvl = (char) (subLvl.charValue() + 1);
            subLevel.put(level, subLvl);
        }
    }

    public void remSubLevel(Integer level) {
        Character subLvl = this.subLevel.get(level);
        subLvl = (char) (subLvl.charValue() - 1);
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
                    functionCount.put(name, 0);
                    break;
                case PARAMS_LIST:

                    // счетчик параметров функции
                    int countParams = 0;
                    for(Node params : child.getListChild()){
                        if(params.getTokenType() == TokenType.PARAM){
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
        for (Node childBody : body.getListChild()) {
            switch (childBody.getTokenType()) {
                case COMMAND:
                    for (Node childCommand : childBody.getListChild()) {
                        switch (childCommand.getTokenType()) {
                            case BODY:
                                bodyRec(childCommand);
                                break;
                            case NAME:
                                buffer = childCommand.clone();
                                String name = childCommand.getTokenValue().toString();
                                String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();
                                TokenType type = getTokenType(lvl, name);
                                if (type == null) {
                                    System.out.printf((char) 27 + "[31m SEMA: переменная не была объявлена LOC<%d:%d>",
                                            childCommand.getValue().getRow(), childCommand.getValue().getCol());
                                    System.exit(0);
                                }
                                childCommand.changeNode(type);
                                childCommand.setLeft(getBuffer());
                                break;
                            default:
                                commandRec(childCommand);
                        }
                    }
                    break;
                case CALL_FUNCTION:
                    System.out.println("FUNCTION");
                    function(childBody);
                    break;
                case EMPTY:
                    setLevel(getLevel() - 1);
                    break;
            }
        }
    }

    public void function(Node function) throws CloneNotSupportedException {
        String name = null;
        for(Node fun : function.getListChild()){
            switch (fun.getTokenType()){
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
                    for(Node args : fun.getListChild()){
                        countArgs++;
                    }
                    int count = 0;
                    try {
                        count = functionCount.get(name);
                    } catch (NullPointerException e){
                        System.out.printf((char) 27 + "[31m SEMA: функция должна быть описана выше main LOC<%d:%d>",
                                fun.getValue().getRow(), fun.getValue().getCol());
                        System.exit(0);
                    }
                    if(count != countArgs){
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
                        if (command.getTokenValue() instanceof Integer) {
                            command.changeNode(TokenType.INT);
                            command.setLeft(getBuffer());
                        } else if (command.getTokenValue() instanceof Double) {
                            command.changeNode(TokenType.DOUBLE);
                            command.setLeft(getBuffer());
                        }
                        break;
                    case CHAR:
                        buffer = command.clone();
                        if (command.getTokenValue() instanceof Character) {
                            command.changeNode(TokenType.CHAR);
                            command.setLeft(getBuffer());
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

                }
            }
        }

    }

    public TokenType getTokenType(String lvl, String name) {

        List<Varible> varibleList = idTableSema.get(name);

        if (varibleList == null) return null;

        for (Varible varible : varibleList) {
            if (varible.getValue().equals(lvl)) {
                return varible.getTokenType();
            }
        }

        boolean checkLvl = false;
        Character nameLvl = lvl.charAt(1);
        for(Varible varible : varibleList) {
            if (varible.getValue().charAt(1) == nameLvl) {
                checkLvl = true;
            }
        }
        if(checkLvl == false){
            return null;
        }

        int[] mass = new int[varibleList.size()];
        for (int i = 0; i < varibleList.size(); i++) {
            mass[i] = varibleList.get(i).getValue().compareTo(lvl);
        }

        int indexOfMax = 0;
        for (int i = 1; i < mass.length; i++) {
            if (mass[i] > mass[indexOfMax]) {
                indexOfMax = i;
            }
        }
        return varibleList.get(indexOfMax).getTokenType();

        //return null;
    }

}
