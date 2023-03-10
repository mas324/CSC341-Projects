#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <time.h>
#include <string.h>
#include <unistd.h>

int main(int argc, char *argv[]) {
    int sockfd, client_sockfd;
    struct sockaddr_in serv_addr, cli_addr;
    time_t current_time;
    char* date_string;
    int portno = 6013;

    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        perror("ERROR opening socket");
        exit(1);
    }

    memset((char *) &serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(portno);

    if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
        perror("ERROR on binding");
        exit(1);
    }

    listen(sockfd, 5);

    while (1) {
        socklen_t clilen = sizeof(cli_addr);
        client_sockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
        if (client_sockfd < 0) {
            perror("ERROR on accept");
            exit(1);
        }

        current_time = time(NULL);
        date_string = ctime(&current_time);
        printf("%s", date_string);
        write(client_sockfd, date_string, strlen(date_string));
        close(client_sockfd);
    }

    close(sockfd);
    return 0;
}