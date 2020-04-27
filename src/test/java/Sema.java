import buffer.Buffer;
import lexer.Lexer;
import org.junit.Test;
import parser.Parser;

import java.io.StringReader;

public class Sema {

    @Test
    public void testSema(){

        Buffer buffer = new Buffer(new StringReader("code"));
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

    }
}
