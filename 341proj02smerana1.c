#include <sys/types.h>
#include <sys/dir.h>
#include <sys/stat.h>
#include <time.h>
#include <stdio.h>
#include <string.h>

int main (int argc, char* argv[]) {
    DIR *dirp;
    struct direct *dp;
    struct stat buf;
    char* psDir = ".";
    int exTime = 0;
    int exFile = 0;

    if (argc > 1) {
        if ((argc == 2) && (argv[1][0] != '-')) {
            psDir = argv[1];
        } else {
            for (int i = 1; i < argc; i++) {
                if (argv[i][0] == '-') {
                    if (argv[i][1] == 'l') {
                        exTime++;
                    } else if (argv[i][1] == 'a') {
                        exFile++;
                    } else {
                        printf("usage: ls [-l|-a] location");
                        return 1;
                    }
                }
            }
        }
    }

    dirp = opendir(psDir);
    for (dp = readdir(dirp); dp != NULL; dp = readdir(dirp)) {
        stat(dp->d_name, &buf);

        if (!exFile && (dp->d_name[0] == '.')) {
            continue;
        } 

        if (exTime) {
            printf("%s\t%s", dp->d_name, ctime(&(buf.st_mtime)));
        } else {
            printf("%s\n", dp->d_name);
        }
    }
    closedir(dirp);
    return 0;
}