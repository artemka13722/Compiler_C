int square(int a){
    a = a * a;
    return a;
}

int sum(int i, int j){
    int sum =  i + j;
    return sum;
}

int main(){
    int a = 5;
    a = square(a);
    printf("Квадрат %d\n", a);
    int b = sum(a, 10);
    printf("Квадрат %d + 10 = %d\n",a, b);
}