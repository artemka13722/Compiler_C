package idTable;

import lexer.TokenType;
import parser.Node;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class IdTable {

    private Map<String, String> idTable;

    public Map<String, String> getIdTable() {
        return idTable;
    }

    private Map<Integer, Character> subLevel;
    private Integer level;

    public IdTable() {
        this.idTable = new IdentityHashMap<>();
        this.subLevel = new HashMap<>();
        this.level = 0;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void addSubLeve(Integer level){
        Character subLvl = this.subLevel.get(level);

        if(subLvl == null){
            this.subLevel.put(level, 'a');
        } else {
            subLvl = (char) (subLvl.charValue() + 1);
            this.subLevel.put(level, subLvl);
        }
    }

    public void body(Node child) {
        for (Node childFunc : child.getListChild()) {
            if (childFunc.getTokenType() == TokenType.BODY) {
                adding(childFunc);
            }
        }
    }

    private void adding(Node childFunc) {
        setLevel(getLevel() + 1);
        addSubLeve(level);
        for (Node childBody : childFunc.getListChild()) {
            if (childBody.getTokenType() == TokenType.COMMAND) {
                for (Node childCommand : childBody.getListChild()) {
                    if (childCommand.getTokenType() == TokenType.NAME) {
                        String lvl = getLevel().toString() + subLevel.get(getLevel()).toString();
                        idTable.put(childCommand.getTokenValue().toString(), lvl);
                    } else if (childCommand.getTokenType() == TokenType.BODY) {
                        adding(childCommand);
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


