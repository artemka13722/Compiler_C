import buffer.Buffer;
import idTable.IdTable;
import lexer.Lexer;
import parser.Node;
import parser.Parser;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
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

        IdTable idTable = new IdTable();
        //idTable.getAstParent(programTree);
        idTable.formATablel(programTree);

        System.out.println(idTable.getIdTable());

        List<IdTable> list = new ArrayList<>();

        System.out.println(idTable.getIdTable().get("a"));
        programTree.writeGraph("./tmp/graph1.dot");
    }
}
