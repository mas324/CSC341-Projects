#include <stdio.h>

#define MAXSIZE 255

//void signal_handler(int value);

extern int errno;

struct userbuf
{
    long mtype;
    char mText[];
};

int main(int argc, char const *argv[])
{
    //key_t myKey;
    int myKey;
    int next = 1;
    struct userbuf data;
    
    
    
}

