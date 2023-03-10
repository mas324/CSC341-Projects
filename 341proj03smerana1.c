#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <wait.h>

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

int main() {
    int pid;

    while (1) {
        char cmd[MAXLINE];
        printf("Enter a string to parse: ");
        fgets(cmd,MAXLINE,stdin);
        parse(cmd);

        pid = fork();

        if (pid == 0) { //Am child
            printf("I am child %d of parent %d\n", getpid(), getppid());
            int status = execvp(myargv[0], myargv + 1);
            printf("Exiting\n");
            _exit(status);
        }
        if (pid < 0) { //Am parent
            fprintf(stderr, "Fork failed\n");
            exit(1);
        }
    }
    printf("Parent says my pid=%d and my parent's pid=%d\n", getpid(), getppid());
    exit(0);
}
