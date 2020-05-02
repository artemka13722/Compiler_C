package codeGen;

import lexer.TokenType;
import parser.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGen {

    boolean anyNames = false;

    Node tree;
    String nameLC = "LC";
    Integer numberLC;
    List<String> assembler = new ArrayList<>();
    private Map<Integer, Character> subLevel;
    private Integer level;

    private Map<String, Integer> addressVar;
    private Integer varBytes = 0;


    public CodeGen(Node tree) {
        this.subLevel = new HashMap<>();
        this.level = 0;
        this.tree = tree;
        addressVar = new HashMap<>();
        generator();
    }

    public void setVar(String name){
        varBytes = varBytes + 4;
        addressVar.put(name, varBytes);
    }

    public String getNameLC() {
        return this.nameLC + numberLC.toString();
    }

    public void setNameLC() {
        if (numberLC == null) {
            numberLC = 0;
        } else {
            numberLC++;
        }
    }

    public List<String> getAssembler() {
        return assembler;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void addSubLevel(Integer level) {
        Character subLvl = this.subLevel.get(level);

        if (subLvl == null) {
            subLevel.put(level, 'a');
        } else if (level == 0) {
            subLevel.put(level, 'a');
        } else {
            subLvl = (char) (subLvl + 1);
            subLevel.put(level, subLvl);
        }
    }

    public void generator() {
        if (tree != null) {
            for (Node child : tree.getListChild()) {
                if (child.getTokenType() == TokenType.FUNCTION) {
                    functionParam(child);
                }
            }
        }
    }

    public void functionParam(Node body) {
        addSubLevel(level);

        for (Node functionParam : body.getListChild()) {

            switch (functionParam.getTokenType()) {

                // тип функции, разобраться почему сдесь анотированное AST а не обычное

                case INT:
                    assemblerFunctionNmae(functionParam.getFirstChildren());
                    break;
                case PARAMS_LIST:

                    // полсе имени идёт добавление параметров

                    break;
                case BODY:
                    functionBody(functionParam);
                    break;
            }

        }
    }

    public void assemblerFunctionNmae(Node functionName) {
        String name = functionName.getTokenValue().toString();
        assembler.add(name + ":");
        assembler.add("pushq   %rbp");
        assembler.add("movq    %rsp, %rbp");

    }

    public void functionBody(Node functionBody) {
        setLevel(getLevel() + 1);
        addSubLevel(level);
        for (Node body : functionBody.getListChild()) {
            switch (body.getTokenType()) {
                case COMMAND:
                    bodyCommand(body);
                    break;
                case EMPTY:
                    assembler.add("leave");
                    assembler.add("ret");

            }
        }
    }

    public void bodyCommand(Node bodyCommand) {

        setNameLC();

        TokenType com = null;
        boolean assigment = false;
        boolean literalCheck = false;
        boolean announcementVar = false;

        String nameVariable = null;

        List<String> commandAssembler = new ArrayList<>();
        List<String> literal = new ArrayList<>();

        for (Node command : bodyCommand.getListChild()) {
            switch (command.getTokenType()) {

                // объявление переменной
                case TYPE:
                    announcementVar = true;
                    break;
                    // разобраться с деревьями
                case INT:
                    nameVariable = command.getFirstChildren().getTokenValue().toString();
                    if(announcementVar){
                        setVar(nameVariable);
                    }
                    break;
                case ASSIGNMENT:
                    assigment = true;
                    break;
                case NUMBER:

                    if(assigment){
                        commandAssembler.add("$" + command.getTokenValue() + ", -" + addressVar.get(nameVariable) + "(%rbp)");
                    }

                    break;
                case PRINTF:
                    literalCheck = true;

                    commandAssembler.add("$." + getNameLC() + ", %edi");
                    commandAssembler.add("movl    $0, %eax");
                    commandAssembler.add("call    printf");

                    break;
                case PRINTF_BODY:
                   commandAssembler.addAll(0,printfBody(command, literal, commandAssembler));
                    break;
                case EMPTY:
                    // проверка на то что был принт/скан
                    if (literalCheck) {
                        assembler.addAll(0, literal);
                    }
                    assembler.addAll(commandAssembler);

                    break;
            }
        }
    }

    public List<String> printfBody(Node command, List<String> literal, List<String> commandAsm){

        literal.add("." + getNameLC() + ":");
        literal.add(".string \"" + command.getFirstChildren().getTokenValue().toString() + "\"");

        List<String> names = new ArrayList<>();
        List<String> asm = new ArrayList<>();

        if(command.getListChild().size() > 1){
            for(Node printfBody : command.getListChild()){
                switch (printfBody.getTokenType()){
                    case INT:
                    case CHAR:
                    case DOUBLE:
                        String name = printfBody.getFirstChildren().getTokenValue().toString();
                        names.add(name);
                        break;
                }
            }

        }

        if(names.size() == 1){
            commandAsm.add(0,"movl    -" + addressVar.get(names.get(0)) + "(%rbp), %esi");
        } else if(names.size() > 1){

            for(int i = 0; i < names.size(); i++){
                asm.add("movl    -" + addressVar.get(names.get(i)) + "(%rbp), %esi");
                if(i+1 != names.size()){
                    asm.add("$." + getNameLC() + ", %edi");
                    asm.add("movl    $0, %eax");
                    asm.add("call    printf");
                }
            }
        }
        return asm;
    }

    public int typeToByte(TokenType type){
        int bytes = 0;

        switch (type){
            case INT:
                bytes = 4;
        }
        return bytes;
    }
}
