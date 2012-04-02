sfslib : cJSON.o sfslib.h
	gcc -g -Wall -lcurl sfslib.c ../cJSON/cJSON.o -o sfslib -I.:../cJSON -lm

cJSON.o : ../cJSON/cJSON.h
	gcc -g -Wall -c ../cJSON/cJSON.c -o ../cJSON/cJSON.o
    
clean:
	#rm -f *.o
	rm -f sfslib *.o ../cJSON/cJSON.o
