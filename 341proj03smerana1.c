#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
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
    //printf("%s\n", myargv[i-1]);
    //then nulls
    while (i < MAXARG && (myargv[i++] = strtok(NULL, WHITE)) != NULL) {
        //printf("%s\n", myargv[i-1]);
    }
}

int main() {
    __pid_t pid;
    int pid_status = -1;
    char cmd[MAXLINE];

    while (1) {
        printf("Enter string to parse: ");
        fgets(cmd, MAXLINE, stdin);
        parse(cmd);

        if (!(strcmp(myargv[0], "logout") && strcmp(myargv[0], "exit"))) {
            printf("Terminating shell...\n");
            exit(EXIT_SUCCESS);
            break;
        }

        pid = fork();
        int bg = 0;
        for (size_t i = 0; myargv[i] != NULL; i++) {
            bg = strcmp("&", myargv[i]) ? 0 : 1;
            if (bg) {
                myargv[i] = NULL;
                break;
            }
        }
        
        if (pid == 0) { // Child process
            if (execvp(myargv[0], myargv) != 0) {
                perror("Process error: ");
                _exit(EXIT_FAILURE);
            } else {
                _exit(EXIT_SUCCESS);
            }
        } else if (pid < 0) { // Process error
            fprintf(stderr, "Fork failed\n");
            exit(EXIT_FAILURE);
        } else { // Parent process
            printf("Parent %d\n", getpid());
            if (bg) {
                sleep(1);
            } else {
                waitpid(pid, &pid_status, 0);
            }
        }        
    }
}
