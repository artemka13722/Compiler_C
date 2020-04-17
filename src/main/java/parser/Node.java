package parser;

import lexer.Token;
import lexer.TokenType;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Node {

    private Node parent;

    private Token<?> value;

    private List<Node> listChild;

    public Node(Token<?> newValue) {
        value = newValue;
        listChild = new ArrayList<>();
    }

    public Node(TokenType typeValue) {
        value = new Token<Object>(typeValue);
        listChild = new ArrayList<>();
    }

    public Token<?> getValue() {
        return value;
    }

    public TokenType getTokenType() {
        return value.getTokenType();
    }

    public Object getTokenValue() {
        return value.getTokenValue();
    }

    public void setLeft(Node leftChild) {
        listChild.add(0, leftChild);
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public void setRight(Node rightChild) {
        listChild.add(rightChild);
    }

    public Node getFirstChildren() {
        if (listChild.size() > 0) {
            return listChild.get(0);
        } else {
            throw new RuntimeException("Parser.Node have more than one children");
        }
    }

    public Node getParent() {
        return parent;
    }

    public List<Node> getListChild() {
        return listChild;
    }

    public boolean match(TokenType type) {
        return value.match(type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Node other = (Node) obj;
        if (listChild == null) {
            if (other.listChild != null) {
                return false;
            }
        } else {
            if (!listChild.equals(other.listChild)) {
                return false;
            }
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public void writeGraph(String path) {
        try {
            PrintWriter writer = new PrintWriter(path);
            writer.println("digraph parseTree {");
            writer.println("\tordering=out;");
            writeGraph(writer, new VertexNumber());
            writer.println("}");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeGraph(PrintWriter writer, VertexNumber vertex) {

        if (listChild == null || (listChild.size() == 0)) {
            writer.println(String.format("\ta_%s [label=\"%s\"; style=filled; fillcolor=green;];", vertex.value, getTokenType()));
        } else {
            writer.println(String.format("\ta_%s [label=\"%s\"];", vertex.value, value.getTokenType()));
        }
        if (listChild != null) {
            List<Integer> successors = new ArrayList<>();
            int curVertex = vertex.value;
            for (Node t : listChild) {
                vertex.inc();
                successors.add(vertex.value);
                t.writeGraph(writer, vertex);
            }
            for (Integer i : successors) {
                writer.println(String.format("\ta_%s -> a_%s", curVertex, i));
            }
        }
    }

    private class VertexNumber {
        int value = 0;

        public void inc() {
            ++value;
        }
    }

}
