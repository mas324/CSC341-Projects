/*
Merana Shawn
Project 03
smerana1
341proj03smerana1.c
*/

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <wait.h>
#include <errno.h>

#define WHITE "\t \n"
#define MAXARG 20
#define MAXLINE 80

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
    int terminate;
    char cmd[MAXLINE];
    printf("New shell started\ntype 'exit' or 'logout' to terminate\n\n");
    

    while (!(terminate = 0)) {
        printf("user@myshell:$ ");
        if (fgets(cmd, MAXLINE, stdin) == NULL) { // If somehow reading in fails report
            printf("an error has occured: %s", strerror(errno));
            continue;
        }
        parse(cmd);

        if (!(strcmp(myargv[0], "logout") && strcmp(myargv[0], "exit"))) { // Exit shell
            printf("Terminating shell...\n");
            terminate = 1;
            exit(EXIT_SUCCESS);
            continue;
            break; // Really making sure that the program exits the loop
        }

        pid = fork();
        int bg = 0;
        for (size_t i = 0; myargv[i] != NULL; i++) { // Check if '&' is an argument
            bg = strcmp("&", myargv[i]) ? 0 : 1;
            if (bg) {
                myargv[i] = NULL; // Replace '&' with NULL so that exec can accept
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
            fprintf(stderr, "Fork failed reason: %s\n", strerror(errno));
            exit(EXIT_FAILURE);
        } else { // Parent process
            //printf("Parent %d\n", getpid()); // Used for debugging
            if (bg) { // If '&' was used create process into background
                printf("Process %d created\n", pid); // Tells pid of child without waiting to finish
            } else {
                waitpid(pid, &pid_status, 0); // Wait for child process to finish
            }
        }        
    }
    exit(EXIT_SUCCESS);
}
