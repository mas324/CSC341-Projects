/* Creates three threads.
Each thread will write the following two messages three times to the screen.
Message one is:   start processid, thread number, round number
Message two is:  end   processid, thread number, round number

compile with gcc -D_REENTERANT -lpthread ...
*/

#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <pthread.h>

int _THREAD_MAX = 4; // Create max allowable threads

char *thread_error(int error);
/*  thread_errlist - only works for thread_create and thread_join
 *                   and does not work on MacOS
 *  preconditions:   a pthread library call has failed and error is the
 *                   return value of the call.
 *  postcondition:   a local char * (an error message) is  return that.
 */

void *thread(void *arg);
/* write two messages to the screen (stdout).
 *    write the first message
 *    sleeps for some time (see nap function)
 *    writes the second message
 */

int verbose = 0; // do we output are sleeping habits ?
// set to 1 (true) if any command line options found

/*
 *  The sleep function
 */
#define SLEEPVALUE rand() % 1000000L + 100L
void nap(void *who)
{
    long sleepTime = SLEEPVALUE;
    if (verbose)
        fprintf(stderr, "ID %s sleeping for %ld milli seconds \n", (char *)who, sleepTime);
    usleep(sleepTime);
}

/*
 *  Creating semaphores
 */
key_t semkey;
int semid;

void semInit()
{
    if (-1 == (semkey = ftok(".", 34104)))
    {
        perror("ftok");
        exit(1);
    }

    if (-1 == (semid = semget(semkey, 1, IPC_CREAT | 0600)))
    {
        perror("semget");
        exit(2);
    }

    if (-1 == ((semctl(semid, 0, SETVAL, 1))))
    {
        perror("semctl SETVAL");
        exit(4);
    }
}

int main(int argc, char **argv)
{
    int i, retcode;
    pthread_t th[_THREAD_MAX];
    char *th_n[_THREAD_MAX];
    void *retval;

    if (argc > 1)    // if user put something extra on command line
        verbose = 1; // we will be chatty - lots of output

    srand(time(NULL)); // seed random number generator

    for (i = 0; i < _THREAD_MAX; i++)
    { // Fill out thread array
        th_n[i] = malloc(sizeof(char));
        sprintf(th_n[i], "%d", i + 1);
    }

    semInit(); // Create semaphors

    /* start X threads */
    for (i = 0; i < _THREAD_MAX; i++)
    {
        if (0 != (retcode = pthread_create(&th[i], NULL, thread, th_n[i])))
        {
            fprintf(stderr, "creating thread %s a failed %s\n",
                    th_n[i], thread_error(retcode));
            exit(-1);
        }
    }

    /* wait for the X threads to finish */
    for (i = 0; i < _THREAD_MAX; i++)
    {
        if (0 != (retcode = pthread_join(th[i], &retval)))
        {
            fprintf(stderr, "join a failed for thread %s %s\n",
                    th_n[i], thread_error(retcode));
            exit(-1);
        }
    }
}

static struct sembuf WAIT = {0, -1, SEM_UNDO};
static struct sembuf SIG = {0, 1, SEM_UNDO};
int ROUNDS = 3;

void *thread(void *arg)
{
    int i, retcode;

    for (i = 0; i < ROUNDS; i++)
    {
        /* Wait */
        if (-1 == semop(semid, &WAIT, 1))
        {
            perror("semopt WAIT");
            exit(-1);
        }

        fprintf(stderr, "\n\tSTART process id %d, thread # %s, round # %d \n",
                getpid(), (char *)arg, i + 1);

        // sleep for some time (random)
        if (rand() % 2)
        {
            nap(arg);
        }
        nap(arg);

        fprintf(stderr, "\tEND   process id %d, thread # %s, round # %d \n\n",
                getpid(), (char *)arg, i + 1);

        /* Signal */
        if (-1 == semop(semid, &SIG, 1))
        {
            perror("semopt SIG");
            exit(-1);
        }
    }
    return NULL;
}

//
//  Error numbers no longer supported under all POSIX systems (that is you Mac)
//
char *thread_error(int error)
{
    switch (error)
    {
    case 0:
        return "success.";
    default:
        return "unknown error.";
    }
}
