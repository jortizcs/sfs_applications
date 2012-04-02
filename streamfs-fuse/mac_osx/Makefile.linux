streamfs : streamfs.o log.o sfslib.o cJSON.o
	gcc -g -lcurl `pkg-config fuse --libs` -D_FILE_OFFSET_BITS=64 -o streamfs streamfs.o log.o sfslib/sfslib.o cJSON/cJSON.o -lm

sfslib.o : log.o
	gcc -lcurl -c sfslib/sfslib.c -o sfslib/sfslib.o

streamfs.o : streamfs.c log.h params.h
	gcc -g -Wall `pkg-config fuse --cflags` -c streamfs.c

log.o : log.c log.h params.h
	gcc -g -Wall `pkg-config fuse --cflags` -c log.c

cJSON.o : cJSON/cJSON.h
	gcc -g -Wall -c cJSON/cJSON.c -o cJSON/cJSON.o

clean:
	rm -f streamfs *.o sfslib/*.o
