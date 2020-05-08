int main(){

    int a;
    int b;
    int tmp;

   scanf("введите а %d", a);
   scanf("введите b %d", b);

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