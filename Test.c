#include <stdio.h>
#include <string.h>
#include <unistd.h>

int main(int argc, char* argv[])
{
    __pid_t pid;
    pid = fork();

    if (pid == 0) {
        char* vector[] = {"ls", "-la", "/home", NULL};
        execvp(vector[0], vector);
        perror("Errored out");
    } else if (pid < 0) {
        printf("Something went wrong\n");
    } else {
        sleep(2);
    }
    printf("This is the file end for pid=%d\n", getpid());
}
