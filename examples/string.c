int main () {
    char a = "acaa";
    char b = "caa";

    printf("Строка  '%s'", a);
    printf(" содержит строку '%s'?\n", b);

    char c = strstr(a,b);
    if( c == "null") {
        printf("FALSE\n");
    } else {
        printf("TRUE\n");
    }
}
