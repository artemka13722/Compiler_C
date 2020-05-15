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
    private Map<String, List<String>> arrays;
    private List<String> assembler;

    private Integer varBytes = 0;
    private Integer node = 0;
    private Integer bodyCounter = 0;
    private String nameVariable = null;
    private boolean returned = false;
    private String nameFunc = null;

    public CodeGen(Node tree) {
        this.subLevel = new HashMap<>();
        this.level = 0;
        this.tree = tree;
        this.addressVar = new HashMap<>();
        this.arrays = new HashMap<>();
        this.assembler = new ArrayList<>();

        generator();
    }

    public void setVar(String name, TokenType type) {

        switch (type){
            case INT:
                varBytes = varBytes + 4;
                break;
            case CHAR:
                varBytes = varBytes + 12;
                break;
        }
        addressVar.put(name, varBytes);
    }

    public String getNameLC() {
        return "LC" + numberLC.toString();
    }

    public void setNameLC() {
        if (numberLC == null) {
            numberLC = 0;
        } else {
            numberLC++;
        }
    }

    public String getNameIf(Integer bodyCounter, Integer numberIF) {
        String val = bodyCounter.toString() + numberIF;
        return ".if" + val;
    }

    public String getNameIfNext(Integer bodyCounter, Integer numberIF) {
        String nextVal = bodyCounter.toString() + (numberIF + 1);
        return ".if" + nextVal;
    }

    public String getNameWhile(Integer bodyCounter, Integer numberWhile) {
        String val = bodyCounter.toString() + numberWhile;
        return ".while" + val;
    }

    public String getNameWhilenext(Integer bodyCounter, Integer numberWhile) {
        String nextVal = bodyCounter.toString() + (numberWhile + 1);
        return ".while" + nextVal;
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
                    returned = false;
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
                    setAssemblerFunctionParam(functionParam);
                    break;
                case BODY:
                    bodyCounter++;
                    functionBody(functionParam);
                    break;
            }
        }
    }

    public void setAssemblerFunctionParam(Node functionParam){

        List<String> names = new ArrayList<>();

        for(Node param : functionParam.getListChild()){
            if(param.getTokenType().equals(TokenType.PARAM)){
                switch (param.getFirstChildren().getFirstChildren().getTokenType()){
                    case INT:
                        String name = param.getTokenValue().toString();
                        setVar(name, TokenType.INT);
                        names.add(name);
                        break;
                    case CHAR:
                        System.out.println("Нельзя принимать char");
                        System.exit(0);
                        break;
                }
            }
        }

        if(names.size() > 0){
            for(int i = 0; i < names.size(); i++){
                switch (i){
                    case 0:
                        assembler.add("\tmovl\t%edi,\t-"+addressVar.get(names.get(i)) + "(%rbp)");
                        break;
                    case 1:
                        assembler.add("\tmovl\t%esi,\t-"+addressVar.get(names.get(i)) + "(%rbp)");
                        break;
                    case 2:
                        assembler.add("\tmovl\t%edx,\t-"+addressVar.get(names.get(i)) + "(%rbp)");
                        break;
                    default:
                        System.out.println("Входных параметров функции должно быть не более 3");
                        System.exit(0);
                }
            }
        }
    }

    public void assemblerFunctionNmae(Node functionName) {
        nameFunc = functionName.getTokenValue().toString();

        if (nameFunc.equals("main")) {
            assembler.add(".global main");
            assembler.add("\t.text");
            assembler.add("\t.type main, @function");
        }

        assembler.add(nameFunc + ":");
        assembler.add("\tpushq\t%rbp");
        assembler.add("\tmovq\t%rsp, %rbp");

       /* if (nameFunc.equals("main")) {
            assembler.add("subq    $2048, %rsp");
        }*/

        assembler.add("\tsubq\t$2048, %rsp");
    }

    public void functionBody(Node functionBody) {
        setLevel(getLevel() + 1);
        addSubLevel(level);
        for (Node body : functionBody.getListChild()) {
            switch (body.getTokenType()) {
                case COMMAND:
                    assembler.addAll(bodyCommand(body));
                    break;
                case EMPTY:

/*                    if(!"main".equals(nameFunc)){
                        if(!returned){
                            assembler.add("nop");
                        }
                        assembler.add("popq\t%rbp");
                        assembler.add("ret");
                    } else {
                        assembler.add("leave");
                        assembler.add("ret");
                    }*/

                    assembler.add("\tleave");
                    assembler.add("\tret");
            }
        }
    }

    public List<String> bodyRec(Node body){

        bodyCounter++;
        List<String> asm = new ArrayList<>();
        List<String> acm;

        for (Node bodyRec : body.getListChild()) {
            switch (bodyRec.getTokenType()) {
                case COMMAND:
                    acm = bodyCommand(bodyRec);
                    if(acm != null){
                        asm.addAll(acm);
                    }
                    break;
            }
        }
        return asm;
    }

    public List<String> bodyCommand(Node bodyCommand) {

        List<String> assemblerThen = new ArrayList<>();
        List<String> assemblerElse = new ArrayList<>();

        boolean literalCheck = false;
        boolean announcementVar = false;
        boolean whileCommand = false;
        boolean ifCommand = false;

        Integer randForCommand = bodyCounter;
        Integer numberWhile = 0;
        Integer numberIf = 0;

        List<String> commandAssembler = new ArrayList<>();
        List<String> literal = new ArrayList<>();

        for (Node command : bodyCommand.getListChild()) {
            switch (command.getTokenType()) {
                case STRSTR:
                    strstr(command, commandAssembler);
                    break;
                case TYPE:
                    announcementVar = true;
                    break;
                case CHAR:
                    nameVariable = command.getFirstChildren().getTokenValue().toString();
                    if(announcementVar){
                        setVar(nameVariable, TokenType.CHAR);
                    } else if(returned){
                        System.out.println("Возкращать можно только int");
                        System.exit(0);
                    }
                    break;
                case INT:
                    nameVariable = command.getFirstChildren().getTokenValue().toString();
                    if (announcementVar) {
                        setVar(nameVariable, TokenType.INT);
                    } else if(returned){
                        // возврат
                        if(isNumeric(nameVariable)){
                            commandAssembler.add("\tmovl\t$" + nameVariable + ", %eax");
                        } else {
                            commandAssembler.add("\tmovl\t-"+addressVar.get(nameVariable) +"(%rbp),\t%eax");
                        }
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
                case RETURN:
                    returned = true;
                    break;
                case PRINTF:
                    literalCheck = true;
                    setNameLC();
                    commandAssembler.add("\tcall\tprintf");
                    break;
                case PRINTF_BODY:
                    printfBody(command, literal, commandAssembler);
                    break;
                case SCANF_BODY:
                    scanfBody(command, literal, commandAssembler);
                    break;
                case SCANF:
                    literalCheck = true;
                    setNameLC();
                    commandAssembler.add("\tcall\tscanf");
                    break;
                case WHILE:
                    whileCommand = true;
                    commandAssembler.add("\tjmp\t" + getNameWhile(randForCommand, numberWhile));
                    break;
                case IF:
                    ifCommand = true;
                    break;
                case CONDITION:
                    if(whileCommand){
                        condition(command, commandAssembler, randForCommand, numberIf, TokenType.WHILE);
                    }
                    if(ifCommand){
                        condition(command, commandAssembler, randForCommand, numberIf, TokenType.IF);
                    }
                    break;
                case BODY_THEN:
                    if(ifCommand){
                        numberIf++;
                        assemblerThen = bodyRec(command);
                    }
                    break;
                case BODY_ELSE:
                    if(ifCommand){
                        assemblerElse = bodyRec(command);
                    }
                    break;
                case BODY:
                    if(whileCommand){
                        numberWhile++;
                        commandAssembler.add(1, getNameWhile(randForCommand, numberWhile) + ":");

                        assemblerThen = bodyRec(command);

                        commandAssembler.addAll(2, assemblerThen);
                        numberWhile++;
                    }
                    break;
                case EMPTY:
                    if (literalCheck) {
                        assembler.addAll(0, literal);
                    }
                    if(ifCommand){
                        if(assemblerElse.size() == 0){
                            commandAssembler.addAll(assemblerThen);
                            commandAssembler.add( getNameIf(randForCommand,numberIf) + ":");
                            numberIf++;
                        } else {
                            commandAssembler.addAll(assemblerThen);
                            commandAssembler.add( "\tjmp\t" + getNameIf(randForCommand,(numberIf + 1)));
                            commandAssembler.add( getNameIf(randForCommand,numberIf) + ":");
                            commandAssembler.addAll(assemblerElse);
                            commandAssembler.add( getNameIf(randForCommand,(numberIf + 1)) + ":");
                            numberIf = numberIf + 2;
                        }
                    }
                    return  commandAssembler;
            }
        }
        return null;
    }

    public void strstr(Node str, List<String> assembler){

        String name1;
        String name2;

        name1 = str.getListChild().get(0).getFirstChildren().getTokenValue().toString();
        name2 = str.getListChild().get(1).getFirstChildren().getTokenValue().toString();

        assembler.add("\tleaq\t-" + addressVar.get(name2) + "(%rbp),\t%rdx");
        assembler.add("\tleaq\t-" + addressVar.get(name1) + "(%rbp),\t%rax");
        assembler.add("\tmovq\t%rdx, %rsi");
        assembler.add("\tmovq\t%rax, %rdi");
        assembler.add("\tcall\tstrstr");
        assembler.add("\tmovq\t%rax, -" + addressVar.get(nameVariable) + "(%rbp)");
    }


    public void condition(Node cond, List<String> assembler, Integer randForCommand, Integer numberIfWhile, TokenType type){

        String signType = null;

        String val1 = null;
        String val2 = null;

        String lit1 = null;
        String lit2 = null;

        String nameArray1 = null;
        String nameArray2 = null;

        boolean literal = false;

        int count = 0;

        for(Node condition : cond.getListChild()){
            switch (condition.getTokenType()){
                case LITERAL:
                    literal = true;
                    if(count == 0){
                        lit1 = condition.getTokenValue().toString();
                        count++;
                    } else {
                        lit2 = condition.getTokenValue().toString();
                    }
                    break;
                case NAME:
                    if(count == 0){
                        nameArray1 = condition.getTokenValue().toString();
                        val1 = condition.getFirstChildren().getFirstChildren().getFirstChildren().getTokenValue().toString();
                        count++;
                    } else {
                        nameArray2 = condition.getTokenValue().toString();
                        val2 = condition.getFirstChildren().getFirstChildren().getFirstChildren().getTokenValue().toString();
                    }
                    break;
                case CHAR:
                case INT:
                    if(count == 0){
                        val1 = condition.getFirstChildren().getTokenValue().toString();
                        count++;
                    } else {
                        val2 = condition.getFirstChildren().getTokenValue().toString();
                    }
                    break;
                case SIGN:
                    signType = condition.getTokenValue().toString();
                    break;
                case EMPTY:
                    break;
            }
        }

        if(type.equals(TokenType.WHILE)){
            assembler.add(getNameWhile(randForCommand, numberIfWhile) + ":");
        }

        if(literal){

            if(lit1 != null){
                if(lit1.equals("null")){
                    lit1 = "$0";
                    assembler.add("\tcmp\t"+lit1 + ",\t-" + addressVar.get(val2) + "(%rbp)");
                } else {
                    StringBuilder str = new StringBuilder();
                    for(int i = lit1.length(); i > 0; i--){
                        str.append(Integer.toHexString(lit1.charAt(i-1)));
                    }
                    assembler.add("\tcmp\t0x"+str + ",\t-" + addressVar.get(val2) + "(%rbp)");
                }
            }
            if(lit2 != null){
                if(lit2.equals("null")){
                    lit2 = "$0";
                }
                assembler.add("\tcmp\t"+lit2 + ",\t-" + addressVar.get(val1) + "(%rbp)");
            }

        } else {

            if(nameArray1 == null){
                if(isNumeric(val1)){
                    assembler.add("\tmovl\t$" + val1 + ",\t%eax");
                } else {
                    assembler.add("\tmovl\t-" + addressVar.get(val1) + "(%rbp),\t%eax");
                }
            } else {
                if(isNumeric(val1)){
                    String arValue = nameArray1 + val1;
                    assembler.add("\tmovl\t-" + addressVar.get(arValue) + "(%rbp),\t%eax");
                } else {
                    arrayToAsm(val1, nameArray1, assembler);
                }
            }

            if(nameArray2 == null){
                if(isNumeric(val2)){
                    assembler.add("\tcmpl\t$" +val2 + ",\t%eax");
                } else {
                    assembler.add("\tcmpl\t-"+ addressVar.get(val2)+ "(%rbp),\t%eax");
                }
            } else {
                if(isNumeric(val2)){
                    String arValue = nameArray2+ val2;
                    assembler.add("\tcmpl\t-" + addressVar.get(arValue) + "(%rbp),\t%eax");
                } else {
                   arrayToAsm(val2, nameArray2, assembler);
                    assembler.add("\tcmpl\t%eax,\t-" + addressVar.get(val2) + "(%rbp)"); // val1
                }
            }
        }
        assert signType != null;
        singType(signType, assembler, randForCommand, numberIfWhile, type);
    }


    public void arrayToAsm(String val, String nameArray, List<String> assembler){
        assembler.add("\tmovl    -"+ addressVar.get(val)+"(%rbp), %eax");
        assembler.add("\tcltd");

        List<String> addresArray = arrays.get(nameArray);
        nameArray = nameArray + (addresArray.size()-1);

        assembler.add("\tmovl    -"+ addressVar.get(nameArray) +"(%rbp,%rax,4), %eax");
    }

    public void singType(String signType, List<String> assembler, Integer randForCommand, Integer numberIfWhile, TokenType type){

        switch (signType){
            case ">":
                switch (type){
                    case IF:
                        assembler.add("\tjle\t" + getNameIfNext(randForCommand, numberIfWhile));
                        break;
                    case WHILE:
                        assembler.add("\tjg\t" + getNameWhilenext(randForCommand, numberIfWhile));
                        break;
                }
                break;
            case "<":
                switch (type){
                    case IF:
                        assembler.add("\tjge\t" + getNameIfNext(randForCommand, numberIfWhile));
                        break;
                    case WHILE:
                        assembler.add("\tjl\t" + getNameWhilenext(randForCommand, numberIfWhile));
                        break;
                }
                break;
            case "<=":
                switch (type){
                    case IF:
                        assembler.add("\tjg\t" + getNameIfNext(randForCommand, numberIfWhile));
                        break;
                    case WHILE:
                        assembler.add("\tjle\t" + getNameWhilenext(randForCommand, numberIfWhile));
                        break;
                }
                break;
            case "==":
                switch (type){
                    case IF:
                        assembler.add("\tjne\t" + getNameIfNext(randForCommand, numberIfWhile));
                        break;
                    case WHILE:
                        assembler.add("\tje\t" + getNameWhilenext(randForCommand, numberIfWhile));
                        break;
                }
                break;
            case "!=":
                switch (type){
                    case IF:
                        assembler.add("\tje\t" + getNameIfNext(randForCommand, numberIfWhile));
                        break;
                    case WHILE:
                        assembler.add("\tjne\t" + getNameWhilenext(randForCommand, numberIfWhile));
                        break;
                }
                break;
            case ">=":
                switch (type){
                    case IF:
                        assembler.add("\tjl\t" + getNameIfNext(randForCommand, numberIfWhile));
                        break;
                    case WHILE:
                        assembler.add("\tjge\t" + getNameWhilenext(randForCommand, numberIfWhile));
                        break;
                }
                break;
        }
    }

    // TODO: 06.05.2020 возможно заблокировать объявление массива буквой
    // todo сделать объявление массивов с нуля
    public List<String> createArray(Node array){

        int countArray = 0;

        List<String> body = new ArrayList<>();

        for(Node arChild : array.getListChild()){
            switch (arChild.getTokenType()){
                case INT:
                    try {
                        countArray = (int) arChild.getFirstChildren().getTokenValue();
                    } catch (ClassCastException e){
                        System.out.println("Индекс массива не может быть переменной");
                        System.exit(0);
                    }

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
            setVar(nameVar, TokenType.INT);
            asArray.add("\tmovl    $"+ body.get(i) + " , -" + addressVar.get(nameVar) + "(%rbp)");
        }
        return asArray;
    }

    public List<String> arrayBody(Node body, int arCounter){

        List<String> value = new ArrayList<>();
        int realCounter = 0;

        for(Node arBody : body.getListChild()){

            if(arCounter + 1 > realCounter){
                switch (arBody.getTokenType()){
                    case INT:
                        value.add(arBody.getFirstChildren().getTokenValue().toString());
                        realCounter++;
                        break;
                }
            } else {
                System.out.println("Количество объявленных элементов больше величины массива");
                System.exit(0);
            }
        }
        return value;
    }

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
                        commandAssembler.add("\tmovl    $"+ value + ", -" + addressVar.get(nameAsVar) +"(%rbp)");
                    }
                    break;
            }
        }
    }
    
    public void assigment(Node number, List<String> commandAssembler){

        List<String> arguments = new ArrayList<>();
        String nameFunction = null;

        try {
            if(number.getFirstChildren().getTokenType().equals(TokenType.CALL_FUNCTION)){
                for(Node args : number.getFirstChildren().getListChild()){
                    switch (args.getTokenType()){
                        case NAME:
                            nameFunction = args.getTokenValue().toString();
                            break;
                        // TODO: 06.05.2020 почему то нет типов аргументов в семантике, если починю то ошибка тут
                        case ARG_LIST:
                            arguments = argList(args);
                            break;
                    }
                }

                switch (arguments.size()){
                    case 0:
                        commandAssembler.add("\tmovl\t$0,\t%eax");
                        commandAssembler.add("\tcall\t"+ nameFunction);
                        commandAssembler.add("\tmovl\t%eax,\t-"+ addressVar.get(nameVariable) +"(%rbp)");
                        break;
                    case 1:
                        if(isNumeric(arguments.get(0))){
                            commandAssembler.add("\tmovl\t$"+ arguments.get(0)+  ",\t%edi");
                            commandAssembler.add("\tcall\t"+ nameFunction);
                            commandAssembler.add("\tmovl\t%eax,\t-"+ addressVar.get(nameVariable) +"(%rbp)");
                        } else {
                            commandAssembler.add("\tmovl\t-"+ addressVar.get(arguments.get(0)) + "(%rbp), %eax");
                            commandAssembler.add("\tmovl\t%eax, %edi");
                            commandAssembler.add("\tcall\t"+ nameFunction);
                            commandAssembler.add("\tmovl\t%eax,\t-"+ addressVar.get(nameVariable) +"(%rbp)");
                        }
                        break;
                    case 2:
                        if(isNumeric(arguments.get(0))){
                            commandAssembler.add("\tmovl\t$" + arguments.get(0) + ", %edi");
                        } else {
                            commandAssembler.add("\tmovl\t-"+ addressVar.get(arguments.get(0)) + "(%rbp), %eax");
                            commandAssembler.add("\tmovl\t%eax, %edi");
                        }

                        if(isNumeric(arguments.get(1))){
                            commandAssembler.add("\tmovl\t$" + arguments.get(1) + ", %esi");
                        } else {
                            commandAssembler.add("\tmovl\t-"+ addressVar.get(arguments.get(1)) + "(%rbp), %eax");
                            commandAssembler.add("\tmovl\t%eax, %esi");
                        }
                        commandAssembler.add("\tcall\t"+ nameFunction);
                        commandAssembler.add("\tmovl\t%eax,\t-"+ addressVar.get(nameVariable) +"(%rbp)");
                        break;
                    default:
                        System.out.println("Передввать можно не более двух аргументов");
                }

            } else {
                assigmentElse(number,commandAssembler);
            }
        } catch (IndexOutOfBoundsException e){
            assigmentElse(number,commandAssembler);
        }
    }

    public List<String> argList(Node argList){

        List<String> arguments = new ArrayList<>();

        for(Node arg :argList.getListChild()){
            switch (arg.getTokenType()){
                case EMPTY:
                    return null;
                case NUMBER:
                case NAME:
                    arguments.add(arg.getTokenValue().toString());
                    break;
            }
        }
        return arguments;
    }

    public void assigmentElse(Node number, List<String> commandAssembler) {
        if (number.getListChild().size() > 0) {
            for (Node numRec : number.getListChild()) {
                assigment(numRec, commandAssembler);
            }
        } else {

            if(number.getTokenType().equals(TokenType.LITERAL)){

                StringBuilder str = new StringBuilder();

                String literal = number.getTokenValue().toString();

                for(int i = literal.length(); i > 0; i--){
                    str.append(Integer.toHexString(literal.charAt(i-1)));
                }
                commandAssembler.add("\tmovabsq\t$0x" + str + ", %rax");
                commandAssembler.add("\tmovq\t%rax, -" + addressVar.get(nameVariable) +"(%rbp)");
            }

            switch (number.getParent().getParent().getTokenType()) {
                case ARRAY:
                    switch (number.getTokenType()){
                            //a[3]
                        case NUMBER:
                            String arNum = number.getTokenValue().toString();
                            String varName = number.getParent().getParent().getParent().getTokenValue().toString();
                            String value = varName + arNum;
                            commandAssembler.add("\tmovl\t-" + addressVar.get(value) + "(%rbp), %eax");
                            commandAssembler.add("\tmovl\t%eax, -" +  addressVar.get(nameVariable) + "(%rbp)");
                            break;
                            //a[b]
                        case NAME:
                            String varNameAssign = number.getTokenValue().toString();

                            commandAssembler.add("\tmovl\t-"+ addressVar.get(varNameAssign)+"(%rbp), %eax");
                            commandAssembler.add("\tcltd");

                            String nameArray = number.getParent().getParent().getParent().getTokenValue().toString();
                            List<String> addresArray = arrays.get(nameArray);
                            nameArray = nameArray + (addresArray.size()-1);

                            commandAssembler.add("\tmovl\t-"+ addressVar.get(nameArray) +"(%rbp,%rax,4), %eax");
                            commandAssembler.add("\tmovl\t%eax, -" +  addressVar.get(nameVariable) + "(%rbp)");

                            break;
                    }
                    break;
                case ASSIGNMENT:

                    switch (number.getTokenType()){
                        case NUMBER:
                            commandAssembler.add("\tmovl\t$" + number.getTokenValue() + ", -" + addressVar.get(nameVariable) + "(%rbp)");
                            break;
                        case NAME:
                            String valueAsName = number.getTokenValue().toString();
                            commandAssembler.add("\tmovl\t-" + addressVar.get(valueAsName) + "(%rbp), %eax");
                            commandAssembler.add("\tmovl\t%eax,\t-" + addressVar.get(nameVariable) + "(%rbp)");
                            break;
                    }

                    break;
                case DIVISION:
                case MINUS:
                case PLUS:
                case MULTIPLICATION:
                    assemblerMath(number, commandAssembler, number.getParent().getParent().getTokenType());
                    break;

                    // разобраться с делением

                    // деление переменной на переменную - изи
                    // деление переменной на число или наоборот - число превращаем в переменную и делим
                    // деление числа на число - поделить на джаве
            }
        }
    }

    public void assemblerMath(Node number, List<String> commandAssembler, TokenType type){
        if (node != 1) {

            String num1 = number.getParent().getParent().getListChild().get(0).getFirstChildren().getTokenValue().toString();
            String num2 = number.getParent().getParent().getListChild().get(1).getFirstChildren().getTokenValue().toString();

            if(type.equals(TokenType.DIVISION)){

                if(isNumeric(num1) && isNumeric(num2)){

                    int one = Integer.parseInt(num1);
                    int two = Integer.parseInt(num2);

                    String result = String.valueOf(one / two);
                    assembler.add("\tmovl\t$" + result + ", -" + addressVar.get(nameVariable) + "(%rbp)");
                } else {
                    if(isNumeric(num1)){
                        setVar("numDiv1", TokenType.INT);
                        assembler.add("\tmovl\t$" + num1 + ", -" + addressVar.get("numDiv1") + "(%rbp)");
                        assembler.add("\tmovl\t-" + addressVar.get("numDiv1") + "(%rbp), %eax");

                    } else {
                        assembler.add("\tmovl\t-" + addressVar.get(num1) + "(%rbp), %eax");
                        assembler.add("\tcltd");
                    }

                    if(isNumeric(num2)){
                        setVar("numDiv2", TokenType.INT);
                        assembler.add("\tmovl\t$" + num2 + ", -" + addressVar.get("numDiv2") + "(%rbp)");
                        assembler.add("\tidivl\t-" + addressVar.get("numDiv2") + "(%rbp)");
                    } else {
                        assembler.add("\tcltd");
                        assembler.add("\tidivl\t-" + addressVar.get(num2) + "(%rbp)");
                    }
                    assembler.add("\tmovl\t%eax, -" +addressVar.get(nameVariable) + "(%rbp)");
                }

            } else

            if (isNumeric(num1)) {
                commandAssembler.add("\tmovl\t$" + num1 + ", %edx");
            } else {
                if (!num1.equals(nameVariable)) {
                    commandAssembler.add("\tmovl\t-" + addressVar.get(num1) + "(%rbp), %edx");
                }
            }

            switch (type){
                case MINUS:
                    if (isNumeric(num2)) {
                        commandAssembler.add("\tsubl\t$" + num2 + ", %edx");
                    } else {
                        if (!num2.equals(nameVariable)) {
                            commandAssembler.add("\tsubl\t-" + addressVar.get(num2) + "(%rbp), %edx");
                        }
                    }
                    commandAssembler.add("\tmovl\t%edx, -" + addressVar.get(nameVariable) + "(%rbp)");
                    break;
                case PLUS:
                    if (isNumeric(num2)) {
                        commandAssembler.add("\taddl\t$" + num2 + ", %edx");
                    } else {
                        if (!num2.equals(nameVariable)) {
                            commandAssembler.add("\taddl\t-" + addressVar.get(num2) + "(%rbp), %edx");
                        }
                    }
                    commandAssembler.add("\tmovl\t%edx, -" + addressVar.get(nameVariable) + "(%rbp)");
                    break;
                case MULTIPLICATION:
                    if (isNumeric(num2)) {
                        commandAssembler.add("\tmovl\t$" + num2 + ", %eax");
                    } else {
                        if (!num2.equals(nameVariable)) {
                            commandAssembler.add("\tmovl\t-" + addressVar.get(num2) + "(%rbp), %eax");
                        }
                    }
                    commandAssembler.add("\tmull\t%edx");
                    commandAssembler.add("\tmovl\t%eax, -" + addressVar.get(nameVariable) + "(%rbp)");
                    break;
            }
            node++;
        }
    }

    public void scanfBody(Node command, List<String> literal, List<String> commandAsm) {

        literal.add("." + getNameLC() + ":");
        literal.add("\t.string \"" + command.getFirstChildren().getTokenValue().toString() + "\"");

        List<String> names = new ArrayList<>();

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
            commandAsm.add(0,"\txorl\t%eax,\t%eax");
            commandAsm.add(1,"\tmovq\t$." + getNameLC() + ",\t%rdi");
            commandAsm.add( 2,"\tleaq\t-" + addressVar.get(names.get(0)) + "(%rbp),\t%rsi");
        } else if (names.size() > 1) {

            /*for (int i = 0; i < names.size(); i++) {
                asm.add("movl    -" + addressVar.get(names.get(i)) + "(%rbp), %esi");
                if (i + 1 != names.size()) {
                    asm.add("$." + getNameLC() + ", %edi");
                    asm.add("movl    $0, %eax");
                    asm.add("call    printf");
                }
            }*/

            System.out.println("принимать можно не более одного значения");
        }
    }

    public void printfBody(Node command, List<String> literal, List<String> commandAsm) {

        literal.add("." + getNameLC() + ":");
        literal.add("\t.string \"" + command.getFirstChildren().getTokenValue().toString() + "\"");

        List<String> names = new ArrayList<>();
        
        List<TokenType> type = new ArrayList<>();


        if (command.getListChild().size() > 1) {
            for (Node printfBody : command.getListChild()) {
                switch (printfBody.getTokenType()) {

                    case INT:
                    case CHAR:
                    case DOUBLE:
                        type.add(printfBody.getTokenType());
                        names.add(printfBody.getFirstChildren().getTokenValue().toString());
                        break;
                }
            }
        }

        switch (names.size()){
            case 0:
                commandAsm.add(0,"\tmovl\t$." + getNameLC() + ",\t%edi");
                break;
            case 1:
                switch (type.get(0)){
                    case INT:
                        commandAsm.add(0, "\tmovl\t-" + addressVar.get(names.get(0)) + "(%rbp),\t%eax");
                        commandAsm.add(1,"\tmovl\t%eax,\t%esi");
                        commandAsm.add(2,"\tmovl\t$." + getNameLC() + ",\t%edi");
                        break;
                    case CHAR:
                        commandAsm.add(0, "\tleaq\t-" + addressVar.get(names.get(0)) + "(%rbp),\t%rax");
                        commandAsm.add(1,"\tmovq\t%rax,\t%rsi");
                        commandAsm.add(2,"\tmovl\t$." + getNameLC() + ",\t%edi");
                        break;
                }
                break;
            case 2:
                if(type.get(0).equals(type.get(0)) && type.get(0).equals(TokenType.INT) ){
                    commandAsm.add(0, "\tmovl\t-" + addressVar.get(names.get(1)) + "(%rbp),\t%edx");
                    commandAsm.add(1, "\tmovl\t-" + addressVar.get(names.get(0)) + "(%rbp),\t%eax");
                    commandAsm.add(2,"\tmovl\t%eax,\t%esi");
                    commandAsm.add(3,"\tmovl\t$." + getNameLC() + ",\t%edi");
                } else {
                    System.out.println("Можно вывести только два INT");
                }
                break;
            default:
                System.out.println("Такое количество параметров вывода не завезли");
        }
    }
}
