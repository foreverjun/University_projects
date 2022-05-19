#include <stdio.h>

void other ()
{
    printf("Congratulations. You've completed the buffer overflow.\n");
    fflush(stdout);
}

void input ()
{
    printf("Enter to 'input' function\n");
    char buf[10];
    scanf("%s", buf);
    printf("Was recorded in buffer: %s\n", buf);
}

int main ()
{
    input();
    printf("Return to 'main' after 'input'\n");
}