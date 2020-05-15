int main () {
    char q = "acaa";
    char r = "caa";
    char c = strstr(q,r);
    if( c == "null") {
        printf("FALSE\n");
    } else {
        printf("TRUE\n");
    }
}
