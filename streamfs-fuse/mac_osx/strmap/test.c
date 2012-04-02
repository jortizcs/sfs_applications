#include <stdio.h>
#include "strmap.h"

int main(void);

int main(){
    StrMap *sm;
    char* buf;

    sm = sm_new(10);
    if (sm == NULL) {
        printf("Could not allocate string_map\n");
        return 1;
    }
    /* Insert a couple of string associations */
    HT_ADD(sm, "application name", "Test Application");
    HT_ADD(sm, "application version", "1.0.0");

    /* Retrieve a value */
    buf = HT_LOOKUP(sm, "application name");
    if (buf==NULL){
        printf("No result\n");
    }
    else{
        printf("value: %s\n", buf); free(buf); buf=NULL;
    }

    buf = HT_LOOKUP(sm, "application version");
    if (buf==NULL){
        printf("No result\n");
    }
    else {
        printf("value: %s\n", buf); free(buf); buf=NULL;
    }
    
    buf = HT_LOOKUP(sm, "blah");
    if (buf == NULL){
        printf("No result\n");
    }

    HT_REMOVE(sm, "application version");
    buf = sm_get(sm, "application version");
    if (buf == NULL){
        printf("No result for \"application version\"\n");
    }

    /* When done, destroy the StrMap object */
    sm_delete(sm);
    return 0;
}
