#include <sys/types.h>
#include <stdio.h>
#include <unistd.h>

int main() {
    pid_t pid;

    while (1) {
        pid = fork();

        sleep(5);

        if (pid < 0) {
            fprintf(stderr, "Fork Failed");
            return 1;
        }
        else if (pid == 0) {
            fprintf(stdout, "I am the main");
        }
        else {
            wait(NULL);
            printf("Child Complete");
        }
    }

    return 0;
}