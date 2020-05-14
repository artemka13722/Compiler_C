int main(){

    int a;
    int b;
    int tmp;

    printf("введите а\n");
    scanf("%d", a);
    printf("введите b\n");
    scanf("%d", b);

    while (a != b) {
        if (a > b) {
        tmp = a;
        a = b;
        b = tmp;
        }
        b = b - a;
    }
    printf("%d\n",a);
}