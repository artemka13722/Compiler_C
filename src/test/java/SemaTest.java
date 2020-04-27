import buffer.Buffer;
import idTable.IdTable;
import lexer.Lexer;
import org.junit.Test;
import parser.Node;
import parser.Parser;
import semantic.Sema;

import java.io.StringReader;

public class SemaTest {

    @Test
    public void testSema() throws CloneNotSupportedException {

        Buffer buffer = new Buffer(new StringReader("int main(){}"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);
        Node programTree = parser.parseProgram();

        IdTable idTable = new IdTable(programTree);

        Sema sema = new Sema(programTree, idTable.getIdTable());
        Node semaTree = sema.getTree();

        System.out.println(semaTree);

    }
}
