package codeGen;

import lexer.TokenType;
import parser.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGen {




    private Integer numberLC;

    private Integer bodyCounter = 0;

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

    private boolean returned = false;
    private String nameFunc = null;

    public CodeGen(Node tree) {
        this.subLevel = new HashMap<>();
        this.level = 0;
        this.tree = tree;
        addressVar = new HashMap<>();
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
        return this.nameLC + numberLC.toString();
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

        for(Node param : functionParam.getListChild()){
            if(param.getTokenType().equals(TokenType.PARAM)){

                switch (param.getFirstChildren().getFirstChildren().getTokenType()){
                    case INT:
                        String name = param.getTokenValue().toString();
                        setVar(name, TokenType.INT);
                        // добавление принимаемых переменных
                        assembler.add("movl\t%edi,\t-"+addressVar.get(name) + "(%rbp)");
                        break;
                    case CHAR:
                        System.out.println("Нельзя принимать char");
                        System.exit(0);
                        break;
                }



            }
        }
    }

    public void assemblerFunctionNmae(Node functionName) {
        nameFunc = functionName.getTokenValue().toString();

        if (nameFunc.equals("main")) {
            assembler.add(".global main");
            assembler.add(".text");
            assembler.add(".type main, @function");
        }

        assembler.add(nameFunc + ":");
        assembler.add("pushq   %rbp");
        assembler.add("movq    %rsp, %rbp");

       /* if (nameFunc.equals("main")) {
            assembler.add("subq    $2048, %rsp");
        }*/

        assembler.add("subq    $2048, %rsp");

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

                    assembler.add("leave");
                    assembler.add("ret");
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


        //Integer randForCommand = (int )Math.random();

        Integer randForCommand = bodyCounter;
        Integer numberWhile = 0;
        Integer numberIf = 0;

        List<String> commandAssembler = new ArrayList<>();
        List<String> literal = new ArrayList<>();

        for (Node command : bodyCommand.getListChild()) {
            switch (command.getTokenType()) {

                // объявление переменной
                case TYPE:
                    announcementVar = true;
                    break;
                // разобраться с деревьями
                case CHAR:

                    nameVariable = command.getFirstChildren().getTokenValue().toString();
                    if(announcementVar){
                        setVar(nameVariable, TokenType.CHAR);
                    }

                    break;
                case INT:
                    nameVariable = command.getFirstChildren().getTokenValue().toString();
                    if (announcementVar) {
                        setVar(nameVariable, TokenType.INT);
                        //announcementVar = false; нужно ли? так как в команде макс 1 объявление
                    } else if(returned){
                        // TODO: 06.05.2020 пока возвращает ток переменные, возможно добавлю арифметику
                        commandAssembler.add("movl\t-"+addressVar.get(nameVariable) +"(%rbp),\t%eax");
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
                    commandAssembler.add("call\tprintf");
                    break;
                case PRINTF_BODY:
                    commandAssembler.addAll(0, printfBody(command, literal, commandAssembler));
                    break;
                case SCANF_BODY:
                    commandAssembler.addAll(0, scanfBody(command, literal, commandAssembler));
                    break;
                case SCANF:
                    literalCheck = true;
                    setNameLC();
                    commandAssembler.add("call\tscanf");
                    break;

                    // цикл while
                case WHILE:
                    whileCommand = true;
                    // разобраться с индексов
                    commandAssembler.add("jmp\t" + getNameWhile(randForCommand, numberWhile));
                    break;


                case IF:
                    ifCommand = true;
                    break;
                case CONDITION:
                    if(whileCommand){
                        conditionWhile(command, commandAssembler, randForCommand, numberWhile);
                    }
                    if(ifCommand){
                        conditionIf(command, commandAssembler, randForCommand, numberIf);
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
                        commandAssembler.add(1, getNameWhile(randForCommand, numberWhile) + ":"); // КОСТЫЛЬ

                        assemblerThen = bodyRec(command);

                        commandAssembler.addAll(2, assemblerThen);
                        numberWhile++;
                    }
                    break;

                case EMPTY:

                    // проверка на то что был принт/скан
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
                            commandAssembler.add( "jmp\t" + getNameIf(randForCommand,(numberIf + 1)));
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

    public void conditionIf(Node condIf, List<String> assembler, Integer randForCommand, Integer numberIf){
        String var1 = null;
        String var2 = null;
        String signType = null;
        int countCond = 0;


        boolean array1 = false;
        boolean arrayNumeric1 = false;

        String arrayName1 = null;
        String arrayVarName1 = null;
        String arrayValue1 = null;

        boolean array2 = false;
        boolean arrayNumeric2 = false;

        String arrayName2 = null;
        String arrayVarName2 = null;
        String arrayValue2 = null;

        for(Node condCommand : condIf.getListChild()) {

            if(condCommand.getListChild().size() > 0){

                if(countCond == 0){

                    if(condCommand.getFirstChildren().getTokenType().equals(TokenType.ARRAY)){

                        array1 = true;

                        String arName = condCommand.getTokenValue().toString();
                        String arValue = condCommand.getFirstChildren().getFirstChildren().getFirstChildren().getTokenValue().toString();


                        if(isNumeric(arValue)){
                            arrayNumeric1 = true;
                            arrayValue1 = arName + arValue;
                        } else {
                            arrayName1 = arName;
                            arrayVarName1 = arValue;
                        }
                    } else {
                        var1 = condCommand.getFirstChildren().getTokenValue().toString();
                    }
                    countCond++;

                } else {
                    if(condCommand.getFirstChildren().getTokenType().equals(TokenType.ARRAY)){

                        array2 = true;

                        String arName = condCommand.getTokenValue().toString();
                        String arValue = condCommand.getFirstChildren().getFirstChildren().getFirstChildren().getTokenValue().toString();


                        if(isNumeric(arValue)){
                            arrayNumeric2 = true;
                            arrayValue2 = arName + arValue;
                        } else {
                            arrayName2 = arName;
                            arrayVarName2 = arValue;
                        }
                    } else {
                        var2 = condCommand.getFirstChildren().getTokenValue().toString();
                    }
                }
            }
            if(condCommand.getTokenType().equals(TokenType.SIGN)){
                signType = condCommand.getTokenValue().toString();
            }
        }

        // TODO: 07.05.2020  проверки на null если будут баги

        if(isNumeric(var1)){

            //assembler.add("movl\t$" +var1 + ",\t%eax");

            if(!(array2 && !arrayNumeric2)){
                assembler.add("movl\t$" +var1 + ",\t%eax");
            }
        } else {
            if(array1){
                if(arrayNumeric1){
                    assembler.add("movl\t-" + addressVar.get(arrayValue1) + "(%rbp),\t%eax");
                } else {
                    assembler.add("movl    -"+ addressVar.get(arrayVarName1)+"(%rbp), %eax");
                    assembler.add("cltd");

                    List<String> addresArray = arrays.get(arrayName1);
                    arrayName1 = arrayName1 + (addresArray.size()-1);

                    assembler.add("movl    -"+ addressVar.get(arrayName1) +"(%rbp,%rax,4), %eax");
                }
            } else {

                if(!(array2 && !arrayNumeric2)){
                    assembler.add("movl\t-" + addressVar.get(var1) + "(%rbp),\t%eax");
                }

                //assembler.add("movl\t-" + addressVar.get(var1) + "(%rbp),\t%eax");
            }
        }
        if(isNumeric(var2)){
            assembler.add("cmpl\t$" +var2 + ",\t%eax");
        } else {

            if(array2){

                if(arrayNumeric2){
                    assembler.add("cmpl\t-" + addressVar.get(arrayValue2) + "(%rbp),\t%eax");
                } else {

                    assembler.add("movl    -"+ addressVar.get(arrayVarName2)+"(%rbp), %eax");
                    assembler.add("cltd");

                    List<String> addresArray = arrays.get(arrayName2);
                    arrayName2 = arrayName2 + (addresArray.size()-1);

                    assembler.add("movl    -"+ addressVar.get(arrayName2) +"(%rbp,%rax,4), %eax");

                    assembler.add("cmpl\t%eax,\t-" + addressVar.get(var1) + "(%rbp)");
                }
            } else {
                assembler.add("cmpl\t-"+ addressVar.get(var2)+ "(%rbp),\t%eax");
            }
        }


        // возможно не надо следующий

        switch (signType){
            case ">":
                assembler.add("jle\t" + getNameIfNext(randForCommand, numberIf));
                break;
            case "<":
                assembler.add("jge\t" + getNameIfNext(randForCommand, numberIf));
                break;
            case "<=":
                assembler.add("jg\t" + getNameIfNext(randForCommand, numberIf));
                break;
            case "==":
                assembler.add("jne\t" + getNameIfNext(randForCommand, numberIf));
                break;
            case "!=":
                assembler.add("je\t" + getNameIfNext(randForCommand, numberIf));
                break;
            case ">=":
                assembler.add("jl\t" + getNameIfNext(randForCommand, numberIf));
                break;
        }


    }


    // TODO: 07.05.2020 если будет время оптимизировать эту хрень, но вроде работает
    public void conditionWhile(Node cond, List<String> assembler, Integer randForCommand, Integer numberWhile){
        
        String var1 = null;
        String var2 = null;
        String signType = null;
        int countCond = 0;


        boolean array1 = false;
        boolean arrayNumeric1 = false;

        String arrayName1 = null;
        String arrayVarName1 = null;
        String arrayValue1 = null;

        boolean array2 = false;
        boolean arrayNumeric2 = false;

        String arrayName2 = null;
        String arrayVarName2 = null;
        String arrayValue2 = null;

        for(Node condCommand : cond.getListChild()){

            if(condCommand.getListChild().size() > 0){

                if(countCond == 0){

                    if(condCommand.getFirstChildren().getTokenType().equals(TokenType.ARRAY)){

                        array1 = true;

                        String arName = condCommand.getTokenValue().toString();
                        String arValue = condCommand.getFirstChildren().getFirstChildren().getFirstChildren().getTokenValue().toString();


                        if(isNumeric(arValue)){
                            arrayNumeric1 = true;
                            arrayValue1 = arName + arValue;
                        } else {
                            arrayName1 = arName;
                            arrayVarName1 = arValue;
                        }
                    } else {
                        var1 = condCommand.getFirstChildren().getTokenValue().toString();
                    }
                    countCond++;

                } else {
                    if(condCommand.getFirstChildren().getTokenType().equals(TokenType.ARRAY)){

                        array2 = true;

                        String arName = condCommand.getTokenValue().toString();
                        String arValue = condCommand.getFirstChildren().getFirstChildren().getFirstChildren().getTokenValue().toString();


                        if(isNumeric(arValue)){
                            arrayNumeric2 = true;
                            arrayValue2 = arName + arValue;
                        } else {
                            arrayName2 = arName;
                            arrayVarName2 = arValue;
                        }
                    } else {
                        var2 = condCommand.getFirstChildren().getTokenValue().toString();
                    }
                }
            }
            if(condCommand.getTokenType().equals(TokenType.SIGN)){
                signType = condCommand.getTokenValue().toString();
            }
        }

        // TODO: 07.05.2020  проверки на null если будут баги

        assembler.add(getNameWhile(randForCommand, numberWhile) + ":");

        if(isNumeric(var1)){

            //assembler.add("movl\t$" +var1 + ",\t%eax");

            if(!(array2 && !arrayNumeric2)){
                assembler.add("movl\t$" +var1 + ",\t%eax");
            }
        } else {
            if(array1){
                if(arrayNumeric1){
                    assembler.add("movl\t-" + addressVar.get(arrayValue1) + "(%rbp),\t%eax");
                } else {
                    assembler.add("movl    -"+ addressVar.get(arrayVarName1)+"(%rbp), %eax");
                    assembler.add("cltd");

                    List<String> addresArray = arrays.get(arrayName1);
                    arrayName1 = arrayName1 + (addresArray.size()-1);

                    assembler.add("movl    -"+ addressVar.get(arrayName1) +"(%rbp,%rax,4), %eax");
                }
            } else {

                if(!(array2 && !arrayNumeric2)){
                    assembler.add("movl\t-" + addressVar.get(var1) + "(%rbp),\t%eax");
                }

                //assembler.add("movl\t-" + addressVar.get(var1) + "(%rbp),\t%eax");
            }
        }
        if(isNumeric(var2)){
            assembler.add("cmpl\t$" +var2 + ",\t%eax");
        } else {

            if(array2){

                if(arrayNumeric2){
                    assembler.add("cmpl\t-" + addressVar.get(arrayValue2) + "(%rbp),\t%eax");
                } else {

                    assembler.add("movl    -"+ addressVar.get(arrayVarName2)+"(%rbp), %eax");
                    assembler.add("cltd");

                    List<String> addresArray = arrays.get(arrayName2);
                    arrayName2 = arrayName2 + (addresArray.size()-1);

                    assembler.add("movl    -"+ addressVar.get(arrayName2) +"(%rbp,%rax,4), %eax");

                    assembler.add("cmpl\t%eax,\t-" + addressVar.get(var1) + "(%rbp)");
                }
            } else {
                assembler.add("cmpl\t-"+ addressVar.get(var2)+ "(%rbp),\t%eax");
            }
        }


        switch (signType){
            case ">":
                assembler.add("jg\t" + getNameWhilenext(randForCommand, numberWhile));
                break;
            case "<":
                assembler.add("jl\t" + getNameWhilenext(randForCommand, numberWhile));
                break;
            case "<=":
                assembler.add("jle\t" + getNameWhilenext(randForCommand, numberWhile));
                break;
            case "==":
                assembler.add("je\t" + getNameWhilenext(randForCommand, numberWhile));
                break;
            case "!=":
                assembler.add("jne\t" + getNameWhilenext(randForCommand, numberWhile));
                break;
            case ">=":
                assembler.add("jge\t" + getNameWhilenext(randForCommand, numberWhile));
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
            setVar(nameVar, TokenType.INT);
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


                if(arguments == null){
                    commandAssembler.add("movl\t$0,\t%eax");
                    commandAssembler.add("call\t"+ nameFunction);
                    commandAssembler.add("movl\t%eax,\t-"+ addressVar.get(nameVariable) +"(%rbp)");


                    // TODO: 06.05.2020 не забыть про переменные
                } else if(arguments.size() == 1){
                    commandAssembler.add("movl\t$"+ arguments.get(0)+  ",\t%edi");
                    commandAssembler.add("call\t"+ nameFunction);
                    commandAssembler.add("movl\t%eax,\t-"+ addressVar.get(nameVariable) +"(%rbp)");
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
                    str.append(Integer.toHexString((int) literal.charAt(i-1)));
                }

                commandAssembler.add("movabsq\t$0x" + str + ", %rax");
                commandAssembler.add("movq\t%rax, -" + addressVar.get(nameVariable) +"(%rbp)");


                //movabsq	$0x

            }

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

                    switch (number.getTokenType()){
                        case NUMBER:
                            commandAssembler.add("movl    $" + number.getTokenValue() + ", -" + addressVar.get(nameVariable) + "(%rbp)");
                            break;
                        case NAME:
                            String valueAsName = number.getTokenValue().toString();
                            commandAssembler.add("movl\t-" + addressVar.get(valueAsName) + "(%rbp), %eax");
                            commandAssembler.add("movl\t%eax,\t-" + addressVar.get(nameVariable) + "(%rbp)");
                            break;
                    }

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
                            commandAssembler.add("addl    $" + num2 + ", %edx");
                        } else {
                            if (num2 != nameVariable) {
                                commandAssembler.add("addl    -" + addressVar.get(num2) + "(%rbp), %edx");
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

    public List<String> scanfBody(Node command, List<String> literal, List<String> commandAsm) {

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
            commandAsm.add(0,"xorl\t%eax,\t%eax");
            commandAsm.add(1,"movq\t$." + getNameLC() + ",\t%rdi");
            commandAsm.add( 2,"leaq\t-" + addressVar.get(names.get(0)) + "(%rbp),\t%rsi");
        } else if (names.size() > 1) {

            /*for (int i = 0; i < names.size(); i++) {
                asm.add("movl    -" + addressVar.get(names.get(i)) + "(%rbp), %esi");
                if (i + 1 != names.size()) {
                    asm.add("$." + getNameLC() + ", %edi");
                    asm.add("movl    $0, %eax");
                    asm.add("call    printf");
                }
            }*/
        }
        return asm;
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
            commandAsm.add(0, "movl\t-" + addressVar.get(names.get(0)) + "(%rbp),\t%eax");
            commandAsm.add(1,"movl\t%eax,\t%esi");
            commandAsm.add(2,"movl\t$." + getNameLC() + ",\t%edi");

        } else if (names.size() > 1) {

            /*for (int i = 0; i < names.size(); i++) {
                asm.add("movl    -" + addressVar.get(names.get(i)) + "(%rbp), %esi");
                if (i + 1 != names.size()) {
                    asm.add("$." + getNameLC() + ", %edi");
                    asm.add("movl    $0, %eax");
                    asm.add("call    printf");
                }
            }*/
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
