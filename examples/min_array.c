int main(){

    int n = 10;
    int A[10] = {23,1,2,3,4,5,6,7,8,9};
    int min;

    min = A[0];

    int i = 0;
    while(i < n){
        if ( A[i] < min ){
            min = A[i];
        }
        i = i + 1;
    }
    printf("\n Минимальный элемент %d", min);
}