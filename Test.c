#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <threads.h>

int main(int argc, char* argv[])
{
    __pid_t pid;
    pid = fork();
    if (pid == 0)
    {
        fork();
        printf("Hello there I am %d", getpid());
        printf("Thread");
    }
    fork();
}
