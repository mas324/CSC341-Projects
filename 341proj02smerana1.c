#include <sys/types.h>
#include <sys/dir.h>
#include <sys/stat.h>
#include <time.h>
#include <stdio.h>

int main (int argc, char* argv[]) {
    DIR *dirp;
    struct direct *dp;
    struct stat buf;
    char* psDir = ".";

    if (argc > 1) {

        for (int i = 0; i < argc; i++)
        {
            printf("%s", argv[i]);
        }
        
        if (argc > 2){
            if (argv[2] == "a") {
                
            }
        }
        return 1;
    }

    dirp = opendir(psDir);
    for (dp = readdir(dirp); dp != NULL; dp = readdir(dirp)) {
        stat(dp->d_name, &buf);
        printf("%s\t%s", dp->d_name, ctime(&(buf.st_mtime)));
    }
    closedir(dirp);
}