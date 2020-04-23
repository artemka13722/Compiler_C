import buffer.Buffer;
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
        if(args.length > 0){
            file = args[0];
        } else {
            throw new RuntimeException("Не указан путь к файлу в аргументах запуска");
        }

        Reader fileReader = null;
        try {
            fileReader = new FileReader( file );
        } catch (IOException e) {
            System.out.println( "Не смогли открыть файл: " + file );
            return;
        }

        Buffer buffer = new Buffer( fileReader );
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser( lexer );
        Node programTree = parser.parseProgram();

        programTree.writeGraph("./tmp/graph1.dot");

        IdTable idTable = new IdTable();
        //idTable.getAstParent(programTree);
        idTable.formATablel(programTree);

        System.out.println(idTable.getIdTable());

        Sema sema = new Sema(programTree, idTable.getIdTable());
        sema.analyze();
        sema.getTree().writeGraph("./tmp/graph2.dot");

        System.out.println(idTable.getIdTable().get("a"));
    }
}
