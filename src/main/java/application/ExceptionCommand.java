package application;

public class ExceptionCommand extends Exception {

    public static String message = "формат команды не соответствует шаблону:\n" +
            "\t[операция] файл_с_исходным_кодом\n\n" +
            "операции:\n" +
            "\tбез заполнения - без остановок на этапах компиляции\n" +
            "\t--dump-tokens — вывести результат работы лексического анализатора\n" +
            "\t--dump-ast — вывести AST\n" +
            "\t--dump-asm — вывести ассемблер\n";

    public ExceptionCommand() {
        super(message);
    }

}
