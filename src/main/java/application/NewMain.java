package application;

import buffer.Buffer;
import codeGen.CodeGen;
import idTable.IdTable;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import parser.Node;
import parser.Parser;
import semantic.Sema;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class NewMain {

    public static String option;
    public static String inputFile;
    public static String astPath;
    private static String astDefaultPath = "./tmp/ast.dot";
    private static String semaAstDefaultPath = "./tmp/semaAst.dot";

    public static void main(String[] args) {

        try {
            processingArguments(args);
            compiler(args);
        }catch (ExceptionCommand | CloneNotSupportedException | IOException e){
            e.printStackTrace();
        }
    }

    public static void compiler(String[] args) throws CloneNotSupportedException, ExceptionCommand, IOException {
        Reader fileReader;
        try {

            if(!checkFile()){
                System.out.println("Расширение файла должно быть .c");
                System.exit(0);
            }



            fileReader = new FileReader(inputFile);
        } catch (IOException e) {
            System.out.println("Не смогли открыть файл: " + inputFile);
            return;
        }

        if (option == null) {
            compile(fileReader, inputFile);
        } else {
            switch (option) {
                case "--dump-tokens":
                    dumpTokens(fileReader);
                    break;
                case "--dump-ast":
                    if (astPath != null) {
                        dumpAst(fileReader, astPath, semaAstDefaultPath);
                    } else {
                        dumpAst(fileReader, astDefaultPath, semaAstDefaultPath);
                    }
                    break;
                case "--dump-asm":
                    dumpAsm(fileReader);
                    break;
            }
        }
    }

    // в процессе
    public static void compile(Reader fileReader, String inputFile) throws CloneNotSupportedException, IOException { Buffer buffer = new Buffer(fileReader);
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);
        Node programTree = parser.parseProgram();

        IdTable idTable = new IdTable(programTree);

        Sema sema = new Sema(programTree, idTable.getIdTable());
        getAstParent(sema.getTreeSema());

        CodeGen codeGen = new CodeGen(programTree);

        StringBuilder nameAsm = new StringBuilder(inputFile);
        nameAsm.setCharAt(inputFile.length()-1, 's');

        FileWriter writer = new FileWriter(String.valueOf(nameAsm));
        for(String str: codeGen.getAssembler()) {
            writer.write(str + System.lineSeparator());
        }
        writer.close();

        StringBuilder nameProgram = new StringBuilder(inputFile);
        nameProgram.setCharAt(inputFile.length()-1, ' ');
        nameProgram.setCharAt(inputFile.length()-2, ' ');

        String echo = "gcc -o " + nameProgram + " -no-pie " + nameAsm;
        Runtime.getRuntime().exec(echo);
    }

    public static void dumpAsm(Reader fileReader) throws CloneNotSupportedException {
        Buffer buffer = new Buffer(fileReader);
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);
        Node programTree = parser.parseProgram();

        IdTable idTable = new IdTable(programTree);

        Sema sema = new Sema(programTree, idTable.getIdTable());
        getAstParent(sema.getTreeSema());

        CodeGen codeGen = new CodeGen(programTree);

        for (int i = 0; i < codeGen.getAssembler().size(); i++) {
            System.out.println(codeGen.getAssembler().get(i));
        }
    }

    public static void dumpAst(Reader fileReader, String outAst, String outSemaAst) throws CloneNotSupportedException, IOException {
        Buffer buffer = new Buffer(fileReader);
        Lexer lexer = new Lexer(buffer);
        Parser parser = new Parser(lexer);

        Node programTree = parser.parseProgram();

        programTree.writeGraph(outAst);
        System.out.println("Файл дерева создан по пути"+ outAst);
        convertDotToUrl(outAst);

        IdTable idTable = new IdTable(programTree);

        Sema sema = new Sema(programTree, idTable.getIdTable());
        sema.getTreeSema().writeGraph(outSemaAst);

        System.out.println("Файл аннотированного дерева создан по пути"+ outSemaAst);
        convertDotToUrl(outSemaAst);
    }

    public static void dumpTokens(Reader fileReader) {


        Buffer buffer = new Buffer(fileReader);
        Lexer lexer = new Lexer(buffer);


        List<Token> tokenList = new ArrayList<>();
        while (!lexer.peekToken().match(TokenType.END)) {
            tokenList.add(lexer.getToken());
        }

        for (int i = 0; i < tokenList.size(); i++) {
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
        } else if (args.length == 3) {
            option = args[0];
            inputFile = args[1];
            astPath = args[2];
            if (!option.equals("--dump-tokens") && !option.equals("--dump-ast") && !option.equals("--dump-asm")) {
                throw new ExceptionCommand();
            }
        } else {
            if (args.length == 1) {
                inputFile = args[0];
            } else {
                inputFile = "./examples/min_array.c";
            }
        }
    }

    public static void convertDotToUrl(String dot) throws IOException {
        FileReader reader = new FileReader(dot);
        int c;
        String test = "";
        while ((c = reader.read()) != -1) {
            test += (char) c;
        }

        String encodedUrl = URLEncoder.encode(test, "UTF-8").replace("+", "%20");
        System.out.println("https://dreampuf.github.io/GraphvizOnline/#" + encodedUrl);
    }

    public static void getAstParent(Node tree) {
        if (tree.getListChild().size() != 0) {
            for (Node children : tree.getListChild()) {
                children.setParent(tree);
                getAstParent(children);
            }
        }
    }


    public static boolean checkFile(){
        return (inputFile.charAt(inputFile.length()-1) == 'c' && inputFile.charAt(inputFile.length()-2) == '.');
    }
}
