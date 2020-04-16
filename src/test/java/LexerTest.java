import buffer.Buffer;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

public class LexerTest {

	@Test
	public void testEnd(){
		Buffer buffer = new Buffer( new StringReader( "" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.END, token1.getTokenType());
	}
	
	@Test
	public void testPlus(){
		Buffer buffer = new Buffer( new StringReader( "+" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.PLUS, token1.getTokenType());
	}
	
	@Test
	public void testMinus(){
		Buffer buffer = new Buffer( new StringReader( "-" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.MINUS, token1.getTokenType());
	}
	
	@Test
	public void testMultiplication(){
		Buffer buffer = new Buffer( new StringReader( "*" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.MULTIPLICATION, token1.getTokenType());
	}
	
	@Test
	public void testDivision(){
		Buffer buffer = new Buffer( new StringReader( "/" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.DIVISION, token1.getTokenType());
	}
	
	@Test
	public void testExponential(){
		Buffer buffer = new Buffer( new StringReader( "^" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.EXPONENTIATION, token1.getTokenType());
	}
	
	@Test
	public void testBracketOpen(){
		Buffer buffer = new Buffer( new StringReader( "(" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.BRACKET_OPEN, token1.getTokenType());
	}
	
	@Test
	public void testBracketClose(){
		Buffer buffer = new Buffer( new StringReader( ")" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.BRACKET_CLOSE, token1.getTokenType());
	}

	@Test
	public void testBraceOpen(){
		Buffer buffer = new Buffer( new StringReader( "{" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.BRACE_OPEN, token1.getTokenType());
	}

	@Test
	public void testBraceClose(){
		Buffer buffer = new Buffer( new StringReader( "}" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.BRACE_CLOSE, token1.getTokenType());
	}

	@Test
	public void testBracetOpen(){
		Buffer buffer = new Buffer( new StringReader( "[" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.BRACET_OPEN, token1.getTokenType());
	}

	@Test
	public void testBracetClose(){
		Buffer buffer = new Buffer( new StringReader( "]" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.BRACET_CLOSE, token1.getTokenType());
	}

	@Test
	public void testComma(){
		Buffer buffer = new Buffer( new StringReader( "," ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.COMMA, token1.getTokenType());
	}

	@Test
	public void testSemicolon(){
		Buffer buffer = new Buffer( new StringReader( ";" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.SEMICOLON, token1.getTokenType());
	}

	@Test
	public void testNumber(){
		Buffer buffer = new Buffer( new StringReader( "42" ));
		Lexer lexer = new Lexer( buffer );
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.NUMBER, token1.getTokenType());
		Assert.assertEquals(42, token1.getTokenValue());
	}

	@Test
	public void testNumberDouble(){
		Buffer buffer = new Buffer( new StringReader( "42.042" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.NUMBER, token1.getTokenType());
		Assert.assertEquals(42.042, token1.getTokenValue());
	}
	
	@Test
	public void testReturn(){
		Buffer buffer = new Buffer( new StringReader( "return" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.RETURN, token1.getTokenType());
	}
	
	@Test
	public void testInt(){
		Buffer buffer = new Buffer( new StringReader( "int" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.INT, token1.getTokenType());
	}
	
	@Test
	public void testDouble(){
		Buffer buffer = new Buffer( new StringReader( "double" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.DOUBLE, token1.getTokenType());
	}

	@Test
	public void testChar(){
		Buffer buffer = new Buffer( new StringReader( "char" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.CHAR, token1.getTokenType());
	}

	@Test
	public void testPrintf(){
		Buffer buffer = new Buffer( new StringReader( "printf" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.PRINTF, token1.getTokenType());
	}

	@Test
	public void testScanf(){
		Buffer buffer = new Buffer( new StringReader( "scanf" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.SCANF, token1.getTokenType());
	}

	@Test
	public void testVoid(){
		Buffer buffer = new Buffer( new StringReader( "void" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.VOID, token1.getTokenType());
	}

	@Test
	public void testIf(){
		Buffer buffer = new Buffer( new StringReader( "if" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.IF, token1.getTokenType());
	}

	@Test
	public void testElse(){
		Buffer buffer = new Buffer( new StringReader( "else" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.ELSE, token1.getTokenType());
	}

	@Test
	public void testWhile(){
		Buffer buffer = new Buffer( new StringReader( "while" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.WHILE, token1.getTokenType());
	}
	
	@Test
	public void testName(){
		Buffer buffer = new Buffer( new StringReader( "testname" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.NAME, token1.getTokenType());
		Assert.assertEquals("testname", token1.getTokenValue());
	}
	
	@Test
	public void testName2(){
		Buffer buffer = new Buffer( new StringReader( "_testname" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.NAME, token1.getTokenType());
		Assert.assertEquals("_testname", token1.getTokenValue());
	}
	
	@Test
	public void testCommentSingleLine(){
		Buffer buffer = new Buffer( new StringReader( "//test comment" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.END, token1.getTokenType());
	}
	
	@Test
	public void testCommentMultiplyLine(){
		Buffer buffer = new Buffer( new StringReader( "/*test comment*/" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.END, token1.getTokenType());
	}

	@Test
	public void testSing1(){
		Buffer buffer = new Buffer( new StringReader( "==" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.SIGN, token1.getTokenType());
	}

	@Test
	public void testSing2(){
		Buffer buffer = new Buffer( new StringReader( "<" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.SIGN, token1.getTokenType());
	}

	@Test
	public void testSing3(){
		Buffer buffer = new Buffer( new StringReader( "<=" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.SIGN, token1.getTokenType());
	}

	@Test
	public void testSing4(){
		Buffer buffer = new Buffer( new StringReader( "!=" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.SIGN, token1.getTokenType());
	}

	@Test
	public void testSing5(){
		Buffer buffer = new Buffer( new StringReader( ">" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.SIGN, token1.getTokenType());
	}

	@Test
	public void testSing6(){
		Buffer buffer = new Buffer( new StringReader( ">=" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.SIGN, token1.getTokenType());
	}

	@Test
	public void testAssigment(){
		Buffer buffer = new Buffer( new StringReader( "=" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.ASSIGNMENT, token1.getTokenType());
	}

	@Test
	public void testCharacter(){
		Buffer buffer = new Buffer( new StringReader( "'q'" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.CHAR, token1.getTokenType());
	}

	@Test
	public void testLiteral(){
		Buffer buffer = new Buffer( new StringReader( "\"test\"" ) );
		Lexer lexer = new Lexer( buffer );

		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.LITERAL, token1.getTokenType());
	}
	
	@Test
	public void testEmptyLine() {
		Buffer buffer = new Buffer( new StringReader( "  2  + 3  \n   " ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.NUMBER, token1.getTokenType());
		Assert.assertEquals(2, token1.getTokenValue());
		
		Token<?> token2 = lexer.getToken();
		Assert.assertEquals(TokenType.PLUS, token2.getTokenType());
		
		Token<?> token3 = lexer.getToken();
		Assert.assertEquals(TokenType.NUMBER, token3.getTokenType());
		Assert.assertEquals(3, token3.getTokenValue());
		
		Token<?> token4 = lexer.getToken();
		Assert.assertEquals(TokenType.END, token4.getTokenType());
		
		Token<?> token5 = lexer.getToken();
		Assert.assertEquals(TokenType.END, token5.getTokenType());
	}
	
	@Test
	public void testManyLines() {
		Buffer buffer = new Buffer( new StringReader( "  2 \n   +\n   3\n" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.NUMBER, token1.getTokenType());
		Assert.assertEquals(2, token1.getTokenValue());
		
		Token<?> token2 = lexer.getToken();
		Assert.assertEquals(TokenType.PLUS, token2.getTokenType());
		
		Token<?> token3 = lexer.getToken();
		Assert.assertEquals(TokenType.NUMBER, token3.getTokenType());
		Assert.assertEquals(3, token3.getTokenValue());
		
		Token<?> token4 = lexer.getToken();
		Assert.assertEquals(TokenType.END, token4.getTokenType());
		
		Token<?> token5 = lexer.getToken();
		Assert.assertEquals(TokenType.END, token5.getTokenType());
	}

	@Test
	public void testCommentSingleLine2() {
		Buffer buffer = new Buffer( new StringReader( " 3 //2\n   + 4 " ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.NUMBER, token1.getTokenType());
		Assert.assertEquals(3, token1.getTokenValue());
		
		Token<?> token2 = lexer.getToken();
		Assert.assertEquals(TokenType.PLUS, token2.getTokenType());
		
		Token<?> token3 = lexer.getToken();
		Assert.assertEquals(TokenType.NUMBER, token3.getTokenType());
		Assert.assertEquals(4, token3.getTokenValue());
		
		Token<?> token4 = lexer.getToken();
		Assert.assertEquals(TokenType.END, token4.getTokenType());
		
		Token<?> token5 = lexer.getToken();
		Assert.assertEquals(TokenType.END, token5.getTokenType());
	}
	
	@Test
	public void testCommentMultiplyLineComplex() {
		Buffer buffer = new Buffer( new StringReader( " /* 56  */  3  /* 123123 123123 1123 13123 1313 13131 13 131  3131 3123 112*/    /**//*2\n   */+ 4 /**/\n" ) );
		Lexer lexer = new Lexer( buffer );
		
		Token<?> token1 = lexer.getToken();
		Assert.assertEquals(TokenType.NUMBER, token1.getTokenType());
		Assert.assertEquals(3, token1.getTokenValue());
		
		Token<?> token2 = lexer.getToken();
		Assert.assertEquals(TokenType.PLUS, token2.getTokenType());
		
		Token<?> token3 = lexer.getToken();
		Assert.assertEquals(TokenType.NUMBER, token3.getTokenType());
		Assert.assertEquals(4, token3.getTokenValue());
		
		Token<?> token4 = lexer.getToken();
		Assert.assertEquals(TokenType.END, token4.getTokenType());
		
		Token<?> token5 = lexer.getToken();
		Assert.assertEquals(TokenType.END, token5.getTokenType());
	}	
}
