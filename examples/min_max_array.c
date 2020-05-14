int main () {
    int i = 0;
    int j = 4;
    int max = 0;
    int min = 0;
    int temp = 0;
    int a[5] = {18, 2, 0, 11 , 5};
    max = 0;
    min = 100;
    while(i <= j){
        temp = a[i];
        if(temp > max){
            max = a[i];
        }
        i = i + 1;
    }
    i = 0;
    while(i <= j){
        temp = a[i];
        if(temp < min){
            min = a[i];
        }
        i = i + 1;
    }
    printf("min %d\n", min);
    printf("max %d\n", max);
}
