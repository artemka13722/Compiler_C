import buffer.Buffer;
import codeGen.CodeGen;
import idTable.IdTable;
import lexer.Lexer;
import parser.Node;
import parser.Parser;
import semantic.Sema;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class Main {
    public static void main(String[] args) throws CloneNotSupportedException {
        String file;
        if (args.length > 0) {
            file = args[0];
        } else {
            file = "./examples/min_array.c";
        }

        Reader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (IOException e) {
            System.out.println("Не смогли открыть файл: " + file);
            return;
        }

        Buffer buffer = new Buffer(fileReader);
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);
        Node programTree = parser.parseProgram();

        programTree.writeGraph("./tmp/graph1.dot");

        IdTable idTable = new IdTable(programTree);
        System.out.println(idTable.getIdTable());

        Sema sema = new Sema(programTree, idTable.getIdTable());
        sema.getTreeSema().writeGraph("./tmp/graph2.dot");

        getAstParent(sema.getTreeSema());

        CodeGen codeGen = new CodeGen(programTree);

        for (int i = 0; i < codeGen.getAssembler().size(); i++) {
            System.out.println(codeGen.getAssembler().get(i));
        }
    }

    public static void getAstParent(Node tree) {
        if (tree.getListChild().size() != 0) {
            for (Node children : tree.getListChild()) {
                children.setParent(tree);
                getAstParent(children);
            }
        }
    }
}
