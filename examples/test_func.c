int square(int a){
    a = a * a;
    return a;
}

int sum(int i, int j){
    int summ = 0;
    summ =  i + j;
    return summ;
}

int main(){
    int a = 5;
    a = square(a);
    printf("Квадрат %d\n", a);
    int b = sum(a, 10);
    printf("Квадрат %d + 10 = %d\n", b);
}