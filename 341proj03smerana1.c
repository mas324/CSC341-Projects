#include <string.h>
#include <stdio.h>
#define WHITE "\t \n"
#define MAXARG 20
#define MAXLINE 80

extern char* sys_errlist[];
extern int errno;

char* myargv[MAXARG];


void parse(char* cmd) {
    int i = 0;
    // fill myargv
    myargv[i++] = strtok(cmd, WHITE); //call 1st with whitespace
    printf("%s\n", myargv[i-1]);
    //then nulls
    while (i < MAXARG && (myargv[i++] = strtok(NULL, WHITE)) != NULL) {
        printf("%s\n", myargv[i-1]);
    }
}

int main(int argc, char const *argv[])
{
    char cmd[MAXLINE];
    printf("Enter a string to parse: ");
    fgets(cmd,MAXLINE,stdin);
    parse(cmd);
}
