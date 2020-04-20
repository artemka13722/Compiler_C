int main(){
    int i;
    int n;
    int A[10];
    int min;

    min = A[0];
    n = 10;

    while(i < n){
        if ( A[i] < min ){
            min = A[i];
        }
    }
    printf("\n Минимальный элемент %d", min);
}