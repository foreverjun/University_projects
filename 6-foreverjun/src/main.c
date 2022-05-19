#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include "sortings.h"


#define error(...) fprintf(stderr,__VA_ARGS__)

typedef void (*sort_funk_t)(strings_array_t, array_size_t, comparator_func_t);

int asc (const char* a, const char* b)
{
    return 0;
}

int des (const char* a, const char* b)
{
    return 0;
}

int parsing_args (char **argv, int argc, long *number_of_lines, comparator_func_t *comp, sort_funk_t *sort)
{
    *comp = 0;
    *sort = 0;
    char **end = 0;
    if (argc != 6){
        error("You must enter 5 arguments:\n"
              "1. number of lines to be sorted\n"
              "2. the name of the file from which to read the strings\n"
              "3. the name of the file in which to output the sorted lines\n"
              "4. algorithm name\n"
              "5. comparator name\n");
        return -1;
    }
    *number_of_lines = strtol(argv[1], end, 10);
    printf("%d",1);
    if (end == 0){
        error("The number of rows is not a number. Enter a number in the argument 1.");
        return -1;
    }
    printf("%d",2);
    if (*number_of_lines == LONG_MAX || *number_of_lines < 0){
        error("The number of lines is either less than zero or exceeds the size of long type.");
        return -1;
    }
    if (strlen(argv[2])< 4 || strlen(argv[2])< 4 ||
    strcmp((argv[2] + strlen(argv[2]) - 3), ".txt") || strcmp(argv[3] + strlen(argv[3]) - 3, ".txt")){
        error("The input and output file should have the extension .txt");
        return -1;
    }
    if (!strcmp(argv[4], "bubble"))
        *sort = bubble;
    if (!strcmp(argv[4], "insertion"))
        *sort = insertion;
    if (!strcmp(argv[4], "merge"))
        *sort = merge;
    if (!strcmp(argv[4], "quick"))
        *sort = quick;
    if (!strcmp(argv[4], "radix"))
        *sort = radix;
    if (*sort == 0){
        error("The wrong sorting algorithm. You can only use these algorithms:"
              "\n1.bubble\n2.insertion\n3.merge\n4.quick\n5.radix");
        return -1;
    }
    if (!strcmp(argv[5], "asc"))
        *comp = asc;
    if (!strcmp(argv[5], "des"))
        *comp = des;
    if (*comp == 0){
        error("Wrong comparator name. You can only use these comparators:\n1.asc\n2.des");
        return -1;
    }
    return 0;
}
int main(int argc,char *argv[])
{
    comparator_func_t comp_f;
    sort_funk_t sort_f;
    long number_of_lines = 0;
    return parsing_args(argv, argc, &number_of_lines, &comp_f, &sort_f);
}