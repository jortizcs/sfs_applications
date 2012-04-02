#ifndef SFSLIB_H
#define SFSLIB_H 


void init_sfslib(char*);
void shutdown_sfslib();
char* get(const char *);
int delete_(const char *);
int isdir(const char*, int* size);
int mkdefault(const char*);
int mkstream(const char*);


#endif
