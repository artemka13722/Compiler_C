package application;

import buffer.Buffer;
import idTable.IdTable;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import parser.Node;
import parser.Parser;
import semantic.Sema;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class NewMain {

    public static String option;
    public static String inputFile;

    public static void main(String[] args) {

        try {
            processingArguments(args);
            compiler();
        }catch (ExceptionCommand | CloneNotSupportedException e){
            e.printStackTrace();
        }
    }

    public static void compiler() throws CloneNotSupportedException {

        Reader fileReader;
        try {
            fileReader = new FileReader(inputFile);
        } catch (IOException e) {
            System.out.println("Не смогли открыть файл: " + inputFile);
            return;
        }

        if(option == null){
           compile(fileReader);
        } else {
            switch (option){
                case "--dump-tokens":
                    dumpTokens(fileReader);
                    break;
                case "--dump-ast":
                    dumpAst(fileReader);
                    break;
                case "--dump-asm":
                    dumpAsm(fileReader);
                    break;
            }
        }
    }

    // в процессе
    public static void compile(Reader fileReader) throws CloneNotSupportedException {
        Buffer buffer = new Buffer(fileReader);
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);
        Node programTree = parser.parseProgram();

        IdTable idTable = new IdTable(programTree);

        Sema sema = new Sema(programTree, idTable.getIdTable());
    }

    public static void dumpAsm(Reader fileReader){
        // в процессе
    }

    public static void dumpAst(Reader fileReader){
        Buffer buffer = new Buffer(fileReader);
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node programTree = parser.parseProgram();

        programTree.writeGraph("./tmp/graph1.dot");
    }

    public static void dumpTokens(Reader fileReader){


        Buffer buffer = new Buffer(fileReader);
        Lexer lexer = new Lexer(buffer);


        List<Token> tokenList = new ArrayList<>();
        while(!lexer.peekToken().match(TokenType.END)){
            tokenList.add(lexer.getToken());
        }

        for(int i = 0; i < tokenList.size(); i++){
            System.out.println(tokenList.get(i));
        }
    }

    public static void processingArguments(String[] args) throws ExceptionCommand {
        if (args.length == 2) {
            option = args[0];
            inputFile = args[1];
            if (!option.equals("--dump-tokens") && !option.equals("--dump-ast") && !option.equals("--dump-asm")) {
                throw new ExceptionCommand();
            }
        } else {
            if (args.length == 1) {
                inputFile = args[0];
            } else {
                throw new ExceptionCommand();
            }
        }
    }

}