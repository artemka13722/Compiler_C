package codeGen;

import lexer.TokenType;
import parser.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGen {


    private Integer numberLC;
    private Node tree;
    private Map<Integer, Character> subLevel;
    private Integer level;
    private Map<String, Integer> addressVar;

    private Integer varBytes = 0;
    private Integer node = 0;
    Map<String, List<String>> arrays = new HashMap<>();
    private List<String> assembler = new ArrayList<>();
    String nameVariable = null;
    private String nameLC = "LC";
    private boolean anyNames = false;

    public CodeGen(Node tree) {
        this.subLevel = new HashMap<>();
        this.level = 0;
        this.tree = tree;
        addressVar = new HashMap<>();
        generator();
    }

    public void setVar(String name) {
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

    public static boolean isNumeric(String strNum) {
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
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

        if (name.equals("main")) {
            assembler.add(".global main");
            assembler.add(".text");
            assembler.add(".type main, @function");
        }

        assembler.add(name + ":");
        assembler.add("pushq   %rbp");
        assembler.add("movq    %rsp, %rbp");
        assembler.add("subq    $2048, %rsp");

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
                    if (announcementVar) {
                        setVar(nameVariable);
                        //announcementVar = false; нужно ли? так как в команде макс 1 объявление
                    }
                    break;
                case ARRAY:
                    if(announcementVar){
                        commandAssembler = createArray(command);
                    }
                    break;
                case ASSIGNMENT:
                    assigment(command, commandAssembler);
                    node = 0;
                    break;
                case ARRAYASSIGMENT:
                    arrayAssigment(command,commandAssembler);
                    break;
                case PRINTF:
                    literalCheck = true;

                    commandAssembler.add("movl    $." + getNameLC() + ", %edi");
                    commandAssembler.add("movl    $0, %eax");
                    commandAssembler.add("call    printf");

                    break;
                case PRINTF_BODY:
                case SCANF_BODY:
                    commandAssembler.addAll(0, printfBody(command, literal, commandAssembler));
                    break;
                case SCANF:
                    literalCheck = true;
                    commandAssembler.add("movl    $." + getNameLC() + ", %edi");
                    commandAssembler.add("movl    $0, %eax");
                    commandAssembler.add("call    scanf");
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

    // TODO: 06.05.2020 возможно заблокировать объявление массива буквой
    public List<String> createArray(Node array){

        int countArray = 0;

        List<String> body = new ArrayList<>();

        for(Node arChild : array.getListChild()){
            switch (arChild.getTokenType()){
                case INT:
                    countArray = (int) arChild.getFirstChildren().getTokenValue();
                    break;
                case ARRAY_BODY:
                    body = arrayBody(arChild, countArray);
                    break;
            }
        }
        arrays.put(nameVariable, body);

        List<String> asArray = new ArrayList<>();

        // TODO: 06.05.2020 придумать как перезаписать выделение под имя массива в тело массива
        for(int i = 0; i < body.size(); i++){

            String nameVar = nameVariable + i;
            setVar(nameVar);
            asArray.add("movl    $"+ body.get(i) + " , -" + addressVar.get(nameVar) + "(%rbp)");
        }
        return asArray;
    }

    public List<String> arrayBody(Node body, int arCounter){

        List<String> value = new ArrayList<>();
        int realCounter = 0;

        for(Node arBody : body.getListChild()){

            if(arCounter > realCounter){
                switch (arBody.getTokenType()){
                    case INT:
                        value.add(arBody.getFirstChildren().getTokenValue().toString());
                        realCounter++;
                        break;
                }
            } else {
                // TODO: 06.05.2020 оформить как ошибку
                System.out.println("Количество объявленных элементов больше величины массива");
            }
        }
        return value;
    }


    // TODO: 06.05.2020 пока что умеет присваивать только числа
    public void arrayAssigment(Node assigment, List<String> commandAssembler){

        String nameAsVar = null;

        for(Node arAssigment : assigment.getListChild()){
            switch (arAssigment.getTokenType()){
                case NUMBER:
                    String num = arAssigment.getTokenValue().toString();
                    nameAsVar = nameVariable + num;
                    break;
                case ASSIGNMENT:
                    if(arAssigment.getFirstChildren().getTokenType().equals(TokenType.NUMBER)){
                        String value =  arAssigment.getFirstChildren().getTokenValue().toString();
                        commandAssembler.add("movl    $"+ value + ", -" + addressVar.get(nameAsVar) +"(%rbp)");
                    }
                    break;
            }
        }
    }

    public void assigment(Node number, List<String> commandAssembler) {
        if (number.getListChild().size() > 0) {
            for (Node numRec : number.getListChild()) {
                assigment(numRec, commandAssembler);
            }
        } else {
            switch (number.getParent().getParent().getTokenType()) {
                case ARRAY:

                    switch (number.getTokenType()){
                        //a[3]
                        case NUMBER:
                            String arNum = number.getTokenValue().toString();
                            String varName = number.getParent().getParent().getParent().getTokenValue().toString();
                            String value = varName + arNum;
                            commandAssembler.add("movl    -" + addressVar.get(value) + "(%rbp), %eax");
                            commandAssembler.add("movl    %eax, -" +  addressVar.get(nameVariable) + "(%rbp)");
                            break;
                            //a[b]
                        case NAME:
                            String varNameAssign = number.getTokenValue().toString();

                            commandAssembler.add("movl    -"+ addressVar.get(varNameAssign)+"(%rbp), %eax");
                            commandAssembler.add("cltd");

                            String nameArray = number.getParent().getParent().getParent().getTokenValue().toString();
                            List<String> addresArray = arrays.get(nameArray);
                            nameArray = nameArray + (addresArray.size()-1);

                            commandAssembler.add("movl    -"+ addressVar.get(nameArray) +"(%rbp,%rax,4), %eax");
                            commandAssembler.add("movl    %eax, -" +  addressVar.get(nameVariable) + "(%rbp)");

                            break;
                    }
                    break;
                case ASSIGNMENT:
                    commandAssembler.add("movl    $" + number.getTokenValue() + ", -" + addressVar.get(nameVariable) + "(%rbp)");
                    break;
                case PLUS:
                    if (node != 1) {

                        String num1 = number.getParent().getParent().getListChild().get(0).getFirstChildren().getTokenValue().toString();
                        String num2 = number.getParent().getParent().getListChild().get(1).getFirstChildren().getTokenValue().toString();


                        if (isNumeric(num1)) {
                            commandAssembler.add("movl    $" + num1 + ", %edx");
                        } else {
                            if (num1 != nameVariable) {
                                commandAssembler.add("movl    -" + addressVar.get(num1) + "(%rbp), %edx");
                            }
                        }

                        if (isNumeric(num2)) {
                            commandAssembler.add("movl    $" + num2 + ", %edx");
                        } else {
                            if (num2 != nameVariable) {
                                commandAssembler.add("movl    -" + addressVar.get(num2) + "(%rbp), %edx");
                            }
                        }
                        commandAssembler.add("movl    %edx, -" + addressVar.get(nameVariable) + "(%rbp)");
                        node++;
                    }
                    break;
                case MULTIPLICATION:
                    if (node != 1) {

                        String num1 = number.getParent().getParent().getListChild().get(0).getFirstChildren().getTokenValue().toString();
                        String num2 = number.getParent().getParent().getListChild().get(1).getFirstChildren().getTokenValue().toString();


                        if (isNumeric(num1)) {
                            commandAssembler.add("movl    $" + num1 + ", %edx");
                        } else {
                            if (num1 != nameVariable) {
                                commandAssembler.add("movl    -" + addressVar.get(num1) + "(%rbp), %edx");
                            }
                        }

                        if (isNumeric(num2)) {
                            commandAssembler.add("movl    $" + num2 + ", %eax");
                        } else {
                            if (num2 != nameVariable) {
                                commandAssembler.add("movl    -" + addressVar.get(num2) + "(%rbp), %eax");
                            }
                        }
                        commandAssembler.add("mull    %edx");
                        commandAssembler.add("movl    %eax, -" + addressVar.get(nameVariable) + "(%rbp)");
                        node++;
                    }
                    break;
                case DIVISION:
                    // разобраться с делением
                    break;
            }
        }
    }

    public List<String> printfBody(Node command, List<String> literal, List<String> commandAsm) {

        literal.add("." + getNameLC() + ":");
        literal.add(".string \"" + command.getFirstChildren().getTokenValue().toString() + "\"");

        List<String> names = new ArrayList<>();
        List<String> asm = new ArrayList<>();

        if (command.getListChild().size() > 1) {
            for (Node printfBody : command.getListChild()) {
                switch (printfBody.getTokenType()) {
                    case INT:
                    case CHAR:
                    case DOUBLE:
                        String name = printfBody.getFirstChildren().getTokenValue().toString();
                        names.add(name);
                        break;
                }
            }

        }

        if (names.size() == 1) {
            commandAsm.add(0, "movl    -" + addressVar.get(names.get(0)) + "(%rbp), %eax");
            commandAsm.add(1, "movl    %eax, %esi");
        } else if (names.size() > 1) {

            for (int i = 0; i < names.size(); i++) {
                asm.add("movl    -" + addressVar.get(names.get(i)) + "(%rbp), %esi");
                if (i + 1 != names.size()) {
                    asm.add("$." + getNameLC() + ", %edi");
                    asm.add("movl    $0, %eax");
                    asm.add("call    printf");
                }
            }
        }
        return asm;
    }

    public int typeToByte(TokenType type) {
        int bytes = 0;

        switch (type) {
            case INT:
                bytes = 4;
        }
        return bytes;
    }
}
