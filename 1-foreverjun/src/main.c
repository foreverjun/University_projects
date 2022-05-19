#define _USE_MATH_DEFINES
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#define MAX_CELL_SIZE 30
#define error(...) (fprintf(stderr, __VA_ARGS__))
double calculate_by_simpson_method(int number_of_exp, double left_border, double right_border){
  double interval,result=0;
  for(int i=0;i<number_of_exp;i++){
      interval= (right_border-left_border) /number_of_exp;
      result+= interval / 6 * (sin(left_border + i * interval) +
              4 * sin((left_border + i * interval + right_border -(interval * (number_of_exp - 1 - i))) / 2)
              +sin(right_border - (interval * (number_of_exp - 1 - i))));
  }
  return result;
}
double calculate_by_rectangles_method(int number_of_exp, double left_border, double right_border) {
    int i;
    double interval, result = 0;
    interval = (right_border - left_border) / number_of_exp;
    for (i = 0; i < number_of_exp; i++) {
        result += sin(left_border + interval / 2 + interval * i);
    }
    result = result * interval;
    return result;
}
void free_string_array (char** result, int experiments_count){
    for (int i = 0; i <experiments_count ; ++i) {
        free(result[i]);
    }
    free(result);
}
char** fills_in_array(int *partition_counts, double left_border, double right_border, int experiment_number) {
    int i;
    char **result=(char**)malloc(sizeof(char*)*experiment_number);
    if(!result){
        error("Can not allocate memory for **result\n");
        return NULL;
    }
    for (i = 0; i < experiment_number; i++) {
        result[i]=(char*)malloc(sizeof(char)*MAX_CELL_SIZE);
        if(!result[i]){
            error("Can not allocate memory for result[%d]\n", i);
            free_string_array(result, i);
            return NULL;
        }

        if(!sprintf(result[i], "%d %.5f %.5f", *(partition_counts + i),
                    calculate_by_rectangles_method(*(partition_counts + i), left_border, right_border),
                    calculate_by_simpson_method(*(partition_counts + i), left_border, right_border))){
                    free_string_array(result,i+1);
            error("Can not write string to result[%d]\n", i);
            return NULL;
        }
    }
    return result;
}
int read_interval ( double *left_border, double *right_border){
    if (printf("Enter left border: ")<0){
        error("Cannot write to stdout");
        return -1;
    }
    if (scanf("%lf", left_border)!=1){
        error("Cannot read left border");
        return -1;
    }
    if (*left_border<0){
        error("Left border less than 0");
        return -1;
    }
    if (printf("Enter right border: ")<0){
        error("Cannot write to stdout");
        return -1;
    }
    if (scanf("%lf", right_border)!=1){
        error("Cannot read right border");
        return -1;
    }
    if (*right_border>M_PI){
        error("Right border greater than PI");
        return -1;
    }
    if (*left_border>*right_border){
        error("Right border less then left border");
        return -1;
    }
    return 0;
}
int main() {
    double left_border =0, right_border=0;
    if(read_interval(&left_border, &right_border)){
        return 1;
    }
    int partition_counts[] ={6,10,20,100,500,1000};
    int experiment_number=sizeof(partition_counts)/sizeof(partition_counts[0]);
    char** result = fills_in_array(partition_counts, left_border, right_border, experiment_number);
    if(!result) {
        return 1;
    }
    for (int i = 0; i < experiment_number; ++i) {
        if(printf("%s\n", result[i])<0){
            error("Can not write result[%d] to stdout", i);
            free_string_array(result, experiment_number);
            return 1;
        }
    }
    free_string_array(result, experiment_number);
    return 0;
}