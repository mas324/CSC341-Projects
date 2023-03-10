// My File Copy 
//
// copies the contents of one file to a destination file. 
// works by prompting user for name of source and destination files
// then reading the contents of source and writing it to destination 
//
// malcolm mccullough
// Jan 27 2023
// for csc 341 Operationg systems 

#include <stdio.h>
#include <stdlib.h>                                             // exit()

int main() {
    FILE *fp1, *fp2;
    char filename[512], c;
        
    printf("Enter filename of source: ");
    scanf("%s", filename);
        
    fp1 = fopen(filename, "r");                 // Open one file for reading
    if (fp1 == NULL) {
        printf("Open failed for %s \n", filename);
        exit(0);
    }

    printf("Enter filename of destination ");
    scanf("%s", filename);

    fp2 = fopen(filename, "w");             // Open another file for writing
    if (fp2 == NULL) {
        printf("Open faile for %s \n", filename);
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
