package idTable;

import lexer.TokenType;
import parser.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdTable {

    private boolean isAnnounced = false;

    private Map<String, List<Variable>> idTable;
    private Map<Integer, Character> subLevel;
    private Integer level;
    private TokenType type;
    private Node tree;

    public IdTable(Node tree) {
        this.idTable = new HashMap<>();
        this.subLevel = new HashMap<>();
        this.level = 0;
        this.tree = tree;
        formATablel();
    }

    public Map<String, List<Variable>> getIdTable() {
        return idTable;
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
        } else if(level == 0) {
            subLevel.put(level, 'a');
        } else {
            subLvl = (char) (subLvl.charValue() + 1);
            subLevel.put(level, subLvl);
        }
    }

    public void remSubLevel(Integer level) {
        Character subLvl = this.subLevel.get(level);
        subLvl = (char) (subLvl - 1);
        this.subLevel.put(level, subLvl);
    }

    public void tableParams(Node child) {
        setLevel(getLevel() + 1);
        addSubLevel(level);
        for (Node childParam : child.getListChild()) {

            switch (childParam.getTokenType()) {
                case PARAM:
                    String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();

                    List<Variable> testList = idTable.get(childParam.getTokenValue().toString());
                    if (testList == null) {
                        testList = new ArrayList<>();
                    }

                    testList.add(new Variable(lvl, childParam.getFirstChildren().getFirstChildren().getTokenType())); // поменять тип
                    idTable.put(childParam.getTokenValue().toString(), testList);
                    break;
            }
        }
        remSubLevel(level);
        setLevel(getLevel() - 1);
    }

    public void body(Node child) {
        addSubLevel(level);
        for (Node childFunc : child.getListChild()) {
            switch (childFunc.getTokenType()) {
                case BODY:
                    adding(childFunc);
                    break;
                case NAME:
                    checkName(childFunc);
                    break;
                case TYPE:
                    type = childFunc.getFirstChildren().getTokenType();
                    isAnnounced = true;
                    break;
                case PARAMS_LIST:
                    tableParams(childFunc);
                    break;

            }
        }
    }

    private void checkName(Node childFunc) {
        if (isAnnounced) {
            String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();

            List<Variable> testList = idTable.get(childFunc.getTokenValue().toString());
            if (testList == null) {
                testList = new ArrayList<>();
            } else {
                for (Variable check : testList){
                    if(check.getValue().equals(lvl)){
                        System.out.printf((char) 27 + "[31m ID: повторное объявление переменной на одном уровне LOC<%d:%d>",
                                childFunc.getValue().getRow(), childFunc.getValue().getCol());
                        System.exit(0);
                    }
                }
            }

            testList.add(new Variable(lvl, type)); // поменять тип
            idTable.put(childFunc.getTokenValue().toString(), testList);
            isAnnounced = false;
        }
    }

    private void adding(Node childFunc) {
        setLevel(getLevel() + 1);
        addSubLevel(level);
        for (Node childBody : childFunc.getListChild()) {
            if (childBody.getTokenType() == TokenType.COMMAND) {
                for (Node childCommand : childBody.getListChild()) {
                    switch (childCommand.getTokenType()) {
                        case BODY:
                            adding(childCommand);
                            break;
                        case NAME:
                            checkName(childCommand);
                            break;
                        case TYPE:
                            type = childCommand.getFirstChildren().getTokenType();
                            isAnnounced = true;
                            break;
                    }
                }
            } else if (childBody.getTokenType() == TokenType.EMPTY) {
                setLevel(getLevel() - 1);
            }
        }
    }

    public void formATablel() {
        List<Node> listChild;
        if (tree != null) {
            listChild = tree.getListChild();
            for (Node child : listChild) {
                if (child.getTokenType() == TokenType.FUNCTION) {
                    if (child.getListChild().size() > 0) {
                        body(child);
                    }
                }
            }
        }
    }
}


