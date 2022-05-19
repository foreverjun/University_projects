#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define  MAX_INPUT_STRING_SIZE 1000
#define error(...) fprintf(stderr,__VA_ARGS__)

int comparator(const void  *a, const void *b)
{
    return -strcmp(*(char**)a,*(char**)b);
}
void free_str_arr (size_t arr_size, char **array)
{
    for (unsigned int i = 0; i < arr_size; ++i) {
        free(array[i]);
    }
    free(array);
}
int check_number_of_strings(size_t *string_count, FILE *input_file)
{
    char *string;
    if (!(string = malloc(sizeof(char) * (MAX_INPUT_STRING_SIZE+1)))){
        error("Memory allocation error");
        return -1;
    }
    while(!feof(input_file)){
        if(fgets(string, MAX_INPUT_STRING_SIZE, input_file) != NULL){
            *string_count += 1;
        }
    }
    free(string);
    if (ferror(input_file)){
        error("Reading error");
        return -1;
    }

    return 0;
}
void strings_output (char **strings_arr, size_t strings_count)
{
    unsigned int m = 0, j = 0;
    char *buffer = malloc(sizeof(char)*MAX_INPUT_STRING_SIZE);
    for (unsigned int i = 0; i < strings_count && i < 100; i++){
        while (j < MAX_INPUT_STRING_SIZE){
            if (strings_arr[i][j] == '.' || strings_arr[i][j] == ',' || strings_arr[i][j] == ';' ||
                strings_arr[i][j] == ':' ||strings_arr[i][j] == '!' || strings_arr[i][j] == '?' ){
                j++;
            }
            buffer[m] = strings_arr[i][j];
            j++;
            m++;
        }
        j = 0;
        m = 0;
        printf("%s", buffer);
    }
    free(buffer);
}
void get_strings(size_t strings_count, char** strings_arr,FILE *input_file)
{
    char *p;
    fseek(input_file, 0, SEEK_SET);
    for (unsigned int i = 0; i < strings_count; i++){
        p = fgets(strings_arr[i], MAX_INPUT_STRING_SIZE, input_file);
        if (p == 0)
            if (ferror(input_file))
                error("Reading error");
    }
}

int main(int argc,char *argv[])
{
    int result = 0;
    size_t strings_count = 0;
    char **strings_arr;
    FILE *input_file;
    if (argc != 2){
        error("You only have to enter the file name.");
        return -1;
    }
    if ((input_file = fopen(argv[1], "rb")) == NULL){
        error("Cannot open the file %s.", argv[1]);
        return -1;
    }
    if ((result = check_number_of_strings(&strings_count,input_file)) != 0){
        return result;
    }
    strings_arr = (char**)malloc(sizeof(char*) * strings_count);
    if(!strings_arr){
        error("Can not allocate memory for **strings_arr\n");
        return -1;
    }
    for (unsigned int i = 0; i < strings_count; i++) {
        strings_arr[i] = (char *) malloc(sizeof(char) * (MAX_INPUT_STRING_SIZE + 1));
        if (!strings_arr[i]) {
            error("Can not allocate memory for result[%d]\n", i);
            free_str_arr(i, strings_arr);
            return -1;
        }
    }
    get_strings(strings_count, strings_arr, input_file);
    qsort(strings_arr, strings_count, sizeof(char*), &comparator);
    strings_output(strings_arr,strings_count);
    free_str_arr(strings_count, strings_arr);
    fclose(input_file);
    return 0;
}