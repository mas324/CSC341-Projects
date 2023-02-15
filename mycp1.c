// My File Copy 
//
// copies the contents of one file to a destination file. 
// works by 
//     looking for the source and destination filenames on the commandline 
//     if not found prompt user for name of source and destination files
// then reading the contents of source and writing it to destination 
//
// malcolm mccullough
// Jan 27 2023
// for csc 341 Operationg systems 

#include <stdio.h>
#include <stdlib.h>                                             // exit()
#include <string.h>                                             // strncopy

#define NAMELENGH 512
int main(int argc, char * argv[]) {
    FILE *fp1, *fp2;
    char ofilename[NAMELENGH], ifilename[NAMELENGH], c;
    
    switch (argc) {
        case 3:  // if 3 then we have 2 arguments mycp inout output   
            strncpy(ifilename, argv[1], NAMELENGH);
            strncpy(ofilename, argv[2], NAMELENGH);
            break;
        case 1: 
            printf("Enter filename of source: ");
            scanf("%s", ifilename);
            printf("Enter filename of destination ");
            scanf("%s", ofilename);
            break;
        default:
            printf("useage: %s inputfile outputfile\n", argv[0]);
            exit(0);
    }    
        
    fp1 = fopen(ifilename, "r");                 // Open one file for reading
    if (fp1 == NULL) {
        printf("Open failed for %s \n", ifilename);
        exit(0);
    }
    fp2 = fopen(ofilename, "w");             // Open another file for writing
    if (fp2 == NULL) {
        printf("Open faile for %s \n", ofilename);
        exit(0);
    }
    // Read contents from file
    c = fgetc(fp1);
    while (c != EOF) {
        fputc(c, fp2);
        c = fgetc(fp1);
    }
    fclose(fp1);
    fclose(fp2);
    return 0;
}
