package idTable;

import lexer.TokenType;
import parser.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdTable {

    private boolean isAnnounced = false;

    private Map<String, List<Varible>> idTable;
    private Map<Integer, Character> subLevel;
    private Integer level;

    public IdTable() {
        this.idTable = new HashMap<>();
        this.subLevel = new HashMap<>();
        this.level = 0;
    }

    public Map<String, List<Varible>> getIdTable() {
        return idTable;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void addSubLeve(Integer level) {
        Character subLvl = this.subLevel.get(level);

        if (subLvl == null) {
            this.subLevel.put(level, 'a');
        } else {
            subLvl = (char) (subLvl.charValue() + 1);
            this.subLevel.put(level, subLvl);
        }
    }

    public void remSubLevel(Integer level) {
        Character subLvl = this.subLevel.get(level);
        subLvl = (char) (subLvl.charValue() - 1);
        this.subLevel.put(level, subLvl);
    }

    public void tableParams(Node child) {
        setLevel(getLevel() + 1);
        addSubLeve(level);
        for (Node childParam : child.getListChild()) {

            switch (childParam.getTokenType()) {
                case PARAM:
                    String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();

                    List<Varible> testList = idTable.get(childParam.getTokenValue().toString());
                    if (testList == null) {
                        testList = new ArrayList<>();
                    }

                    testList.add(new Varible(lvl, childParam.getTokenType())); // поменять тип
                    idTable.put(childParam.getTokenValue().toString(), testList);


                    //idTable.put(childParam.getTokenValue().toString(), lvl);
                    // idTable.put(lvl, childParam.getTokenValue().toString());
                    break;
            }
        }
        remSubLevel(level);
        setLevel(getLevel() - 1);
    }

    public void body(Node child) {
        addSubLeve(level);
        for (Node childFunc : child.getListChild()) {
            switch (childFunc.getTokenType()) {
                case BODY:
                    adding(childFunc);
                    break;
                case NAME:
                    checkName(childFunc);
                case TYPE:
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

            List<Varible> testList = idTable.get(childFunc.getTokenValue().toString());
            if (testList == null) {
                testList = new ArrayList<>();
            } else {
                for (Varible check : testList){
                    if(check.getValue().equals(lvl)){
                        System.out.println("ПЭЗДА");
                        System.exit(0);
                    }
                }
            }

            testList.add(new Varible(lvl, childFunc.getTokenType())); // поменять тип
            idTable.put(childFunc.getTokenValue().toString(), testList);

            // idTable.put(childFunc.getTokenValue().toString(), lvl);
            //idTable.put(lvl, childFunc.getTokenValue().toString());
            isAnnounced = false;
        }
    }

    private void adding(Node childFunc) {
        setLevel(getLevel() + 1);
        addSubLeve(level);
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
                            isAnnounced = true;
                            break;
                    }
                }
            } else if (childBody.getTokenType() == TokenType.EMPTY) {
                setLevel(getLevel() - 1);
            }
        }
    }

    public void formATablel(Node tree) {
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

    public void getAstParent(Node tree) {
        if (tree.getListChild().size() != 0) {
            Node parent = tree;
            for (Node children : tree.getListChild()) {
                children.setParent(parent);
                getAstParent(children);
            }
        }
    }
}


