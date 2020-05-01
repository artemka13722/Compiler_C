package codeGen;

import lexer.TokenType;
import parser.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGen {

    Node tree;
    private Map<Integer, Character> subLevel;
    private Integer level;


    String nameLC = "LC";
    Integer numberLC;

    List<String> assembler = new ArrayList<>();

    public CodeGen(Node tree) {
        this.subLevel = new HashMap<>();
        this.level = 0;
        this.tree = tree;
        generator();
    }

    public String getNameLC(){
        return this.nameLC + numberLC.toString();
    }

    public void setNameLC(){
        if(numberLC == null){
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

    public void generator(){
        if (tree != null) {
            for (Node child : tree.getListChild()) {
                if (child.getTokenType() == TokenType.FUNCTION) {
                    functionParam(child);
                }
            }
        }
    }

    public void functionParam(Node body){
        addSubLevel(level);

        for(Node functionParam : body.getListChild()){

            switch (functionParam.getTokenType()){
                case NAME:
                    assemblerFunctionNmae(functionParam);
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

    public void assemblerFunctionNmae(Node functionName){
        String name = functionName.getTokenValue().toString();
        assembler.add(name + ":");
        assembler.add("pushq   %rbp");
        assembler.add("movq    %rsp, %rbp");

    }

    public void functionBody(Node functionBody){
        setLevel(getLevel() + 1);
        addSubLevel(level);
        for (Node body : functionBody.getListChild()) {
            switch (body.getTokenType()){
                case COMMAND:
                    bodyCommand(body);
                    break;
                case EMPTY:
                    assembler.add("leave");
                    assembler.add("ret");

            }
        }
    }

    public void bodyCommand(Node bodyCommand){

        setNameLC();

        List<String> commandAssembler = new ArrayList<>();
        List<String> literal = new ArrayList<>();

        for(Node command : bodyCommand.getListChild()){
            switch (command.getTokenType()){
                case PRINTF:

                    commandAssembler.add("$." + getNameLC() + ", %edi");
                    commandAssembler.add("movl    $0, %eax");
                    commandAssembler.add("call    printf");

                    break;
                case PRINTF_BODY:

                    literal.add("." + getNameLC() + ":");
                    literal.add(".string \"" + command.getFirstChildren().getTokenValue().toString() + "\"");

                    break;
                case EMPTY:

                    // проверка на то что был принт/скан

                    assembler.addAll(0, literal);
                    assembler.addAll(commandAssembler);

            }
        }
    }

}
