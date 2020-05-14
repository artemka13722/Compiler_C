int square(int a){
    a = a * a;
    return a;
}

int main(){
    int a = 5;
    a = square(a);
    printf("%d", a);
}