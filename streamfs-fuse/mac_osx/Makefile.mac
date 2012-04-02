CC = gcc -fPIC
CFLAGS_MACFUSE = -D__DARWIN_64_BIT_INO_T=1 -D_FILE_OFFSET_BITS=64 -I/usr/local/include/fuse -DFUSE_USE_VERSION=26# -D__FreeBSD__=10
#CFLAGS_EXTRA = -Wall -Werror -g -Wextra -ggdb -pg -DDEBUG 
CFLAGS_EXTRA = -g -Wextra -ggdb -pg -DDEBUG 
CURL_FLAGS = -lcurl
MATH_FLAGS = -lm
#ARCHS = -arch i386 -arch ppc
LIBS = -lfuse_ino64

streamfs : streamfs.o sfslib.o cJSON.o log.o hashtable.o
	$(CC) $(LIBS) $(CFLAGS_MACFUSE) $(CFLAGS_EXTRA) $(CURL_FLAGS) -o streamfs streamfs.o log.o sfslib/sfslib.o cJSON/cJSON.o strmap/hashtable.o $(MATH_FLAGS)

sfslib.o :
	gcc -c sfslib/sfslib.c -o sfslib/sfslib.o

streamfs.o :
	$(CC) $(CFLAGS_EXTRA) $(CFLAGS_MACFUSE) -c streamfs.c -o streamfs.o

cJSON.o : cJSON/cJSON.h
	$(CC) $(CFLAGS_EXTRA) -c cJSON/cJSON.c -o cJSON/cJSON.o

log.o : log.c log.h params.h
	gcc -g -Wall $(CFLAGS_MACFUSE) -c log.c

hashtable.o : 
	gcc -g -Wall -c strmap/strmap.c -o strmap/hashtable.o

clean:
	rm -f streamfs *.o sfslib/*.o cJSON/cJSON.o strmap/hashtable.o
