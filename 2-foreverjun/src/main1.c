#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#define FIRST_ARGUMENT_LENGTH 7
#define SECOND_ARGUMENT_LENGTH 5

void array_sorting(int size_array, long long* array);
struct arguments{
    long long from;
    long long to;
    _Bool to_flag;
    _Bool from_flag;
};
int analysis_argv(struct arguments* arg, char** argv, int argc){
    char *end = NULL;
    if(argc < 2)
        return -1;
    if(argc > 3)
        return -2;
    for(int i = 1; i < argc; i++){
        if(strncmp(argv[i], "--from=", FIRST_ARGUMENT_LENGTH) == 0){
            arg -> from = strtol(argv[i] + FIRST_ARGUMENT_LENGTH, &end, 10);
            arg -> from_flag = 1;
        }
        if(strncmp(argv[i], "--to=", SECOND_ARGUMENT_LENGTH) == 0){
            arg -> to = strtol(argv[i] + SECOND_ARGUMENT_LENGTH, &end, 10);
            arg -> to_flag = 1;
        }
        end = NULL;
    }
    if (arg -> to_flag == 0 && arg -> from_flag == 0 && argc == 3)
        return -4;
    if ((argc == 3 && !strncmp(argv[1], argv[2], SECOND_ARGUMENT_LENGTH)) ||
    (arg -> to == arg -> from && (arg -> to_flag == 1 && arg -> from ==1)))
        return -3;
    return 0;
}
int main(int argc, char* argv[]) {
    char character_after_number;
    int result_analysis_argv, size_array = 0, count_changes = 0;
    long long array[100], array_copy_for_counting[100];
    struct arguments arg = {0, 0, 0, 0};
    result_analysis_argv = analysis_argv(&arg,argv,argc);
    if (result_analysis_argv){
        return result_analysis_argv;}
    do{
        scanf("%lld%c", &array[size_array], &character_after_number);
        if ((arg.to_flag && (array[size_array] >= arg.to)) || (arg.from_flag && (array[size_array] <= arg.from))) {
            if (arg.from_flag && (array[size_array] <= arg.from))
                fprintf(stdout, "%lld", array[size_array]);
            if (arg.to_flag && (array[size_array] >= arg.to))
                fprintf(stderr, "%lld", array[size_array]);
        }
        else
            size_array++;
    }while (character_after_number == ' ');
    for(int i = 0; i <size_array; i++)
        array_copy_for_counting[i] = array[i];
    array_sorting(size_array, array);
    for(int i = 0; i <size_array; i++)
        if (array[i] != array_copy_for_counting[i])
            count_changes++;
    return count_changes;
}