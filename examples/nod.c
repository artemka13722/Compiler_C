int main(){

    int a;
    int b;
    int tmp;

   scanf("%d\n%d\n", a,b);

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