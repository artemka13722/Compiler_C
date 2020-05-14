int main(){
    int i = 0;
    int n = 10;
    int A[10] = { 13,7,3,4,5,6,22,8,1,10};
    int min = A[0];
    int max = A[0];

    while(i < n){
        if ( A[i] < min ){
            min = A[i];
        }
        i = i + 1;
    }

    i = 0;

    while(i < n){
        if ( A[i] > max ){
            max = A[i];
        }
        i = i + 1;
    }

    printf("\n Минимальный элемент %d\n", min);
    printf("\n Максимальный элемент %d\n", max);
}