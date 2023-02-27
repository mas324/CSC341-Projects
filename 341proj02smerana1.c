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
        } else if (!strcmp(argv[1], "-la") || !strcmp(argv[1], "-al")) {
                exTime = 1;
                exFile = 1;
                if (argc == 3) {
                    psDir = argv[2];
                }
        } else if (argc <= 4) {
            int i;
            for (i = 1; i < argc; i++) {
                if ((strlen(argv[i]) == 2) && (argv[i][0] == '-')) {
                    switch (argv[i][1]) {
                        case 'l':
                            exTime = 1;
                            break;
                        case 'a':
                            exFile = 1;
                            break;
                        default:
                            printf("usage: ls [-l|-a] location\n");
                            return 1;
                    }
                } else {
                    psDir = argv[i];
                    break;
                }
            }
        } else {
            printf("usage: ls [-l|-a] location\n");
            return 1;
        }
    }

    dirp = opendir(psDir);

    if (dirp == NULL) {
        stat(psDir, &buf);
        if (exTime) {
            printf("%s\t%s", psDir, ctime(&(buf.st_mtime)));
        } else {
            printf("%s\n", psDir);
        }
    } else {
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
    }
    closedir(dirp);
    return 0;
}