#include <sys/types.h>
#include <sys/dir.h>
#include <sys/stat.h>
#include <time.h>
#include <stdio.h>

int main(int argc, char* argv[]) {
	DIR *dirp;
	struct direct* dp;
	struct stat buf;
	char* psDir = ".";
	
	/* print out directory with times */
	
}