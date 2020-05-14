int main () {
    char q = "acaa";
    char r = "caa";
    char c = strstr(q,r);
    if( c == "null") {
        printf("\nFALSE");
    } else {
        printf("\nTRUE");
    }
}
