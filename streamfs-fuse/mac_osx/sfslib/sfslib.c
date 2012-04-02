#include <string.h> 
#include <stdlib.h>
#include <errno.h>
#include <stdlib.h>
#include <curl/curl.h>
#include "../cJSON/cJSON.h"
#include "sfslib.h"

//int main(int, char*[]);
void test();
void test2();
void test3();
void test4();
void test5();
void test6();
//char* fmt_ts_query_result(cJSON* queryres_cache, const char* path);
static char* fmt_ts_query_result(cJSON* prev_reply_json, const char* path);
size_t get_writer( void *buffer, size_t size, size_t nmemb, void *userp );
size_t read_cr_callback(void *ptr, size_t size, size_t nmemb, void *stream);
size_t read_cs_callback(void *ptr, size_t size, size_t nmemb, void *stream);
char* split_parent_child(const char* path, int parent_child);
int last_index_of(const char* source, char c);

#define MAX_URL_BUF 2000
#define MAX_RES_BUF 1048576//65536

//GLOBALS
static CURL* get_curl=NULL;
static CURL* put_curl=NULL;
static CURL* post_curl=NULL;
static CURL* delete_curl=NULL;
char get_resp[MAX_RES_BUF];
int bytes_rcvd=0;
static char sfs_server[MAX_URL_BUF];// = "http://jortiz81.homelinux.com:8081";
char fpath[MAX_URL_BUF];

static char* new_node_name;
static int CR_BASE_SIZE;
static int CS_BASE_SIZE;

static int globals_set=0;

#define CREATE_DEFAULT_SIZE CR_BASE_SIZE*sizeof(char)
#define CREATE_STREAM_SIZE CS_BASE_SIZE*sizeof(char)

inline static void set_globals(){
    if(globals_set==0){
        //set up global constants
        //create default
        char* out;
        cJSON* json = cJSON_CreateObject();
        cJSON_AddStringToObject(json, "operation", "create_resource");
        cJSON_AddStringToObject(json, "resourceName", "");
        cJSON_AddStringToObject(json, "resourceType", "default");
        out = cJSON_Print(json);
        CR_BASE_SIZE = strlen(out);
        cJSON_Delete(json);
        free(out);

        //create stream
        json = cJSON_CreateObject();
        cJSON_AddStringToObject(json, "operation", "create_generic_publiser");
        cJSON_AddStringToObject(json, "resourceName", "");
        out = cJSON_Print(json);
        CS_BASE_SIZE = strlen(out);
        cJSON_Delete(json);
        free(out);

        memset(fpath, 0, MAX_URL_BUF);
        strcpy(fpath, sfs_server);
    
        globals_set=1;
    }
}

void init_sfslib(char* host_port){
    if(host_port != NULL && strlen(host_port)<MAX_URL_BUF){
        sprintf(sfs_server, "http://%s", host_port);
        fprintf(stdout, "Set host:port=%s\n", host_port);
    }
    else
        sprintf(sfs_server, "http://jortiz81.homelinux.com:8081");
    set_globals();
    get_curl = curl_easy_init();
    put_curl = curl_easy_init();
    post_curl = curl_easy_init();
    delete_curl = curl_easy_init();
}

void shutdown_sfslib(){
    curl_easy_cleanup(get_curl); 
    curl_easy_cleanup(put_curl);
    curl_easy_cleanup(post_curl);
    curl_easy_cleanup(delete_curl);
}

size_t get_writer( void *buffer, size_t size, size_t nmemb, void *userp )
{
    int segsize = size * nmemb;
    /*int space_left = sizeof(*get_resp)- strlen(get_resp);
    fprintf(stdout, "\tsfslib.get_writer:: space_left=%d\t%d < %d?\n", space_left, (int)(1048576-strlen(get_resp)), segsize);
    if((1048576-strlen(get_resp)) < segsize){
        fprintf(stdout, "CALLING REALLOC\n");
        get_resp = realloc(get_resp, sizeof(get_resp) + (size*nmemb));
    }*/
    //get_resp =(char*)calloc(segsize+1);
    //memset(get_resp, 0, segsize+1);
    //fprintf(stdout, "\nbuffer=%s\n", (char*)buffer);
    //memcpy( (void *)get_resp, buffer, (size_t)segsize );

    bytes_rcvd += segsize;
    if(bytes_rcvd < MAX_RES_BUF)
        strncat(get_resp, buffer, segsize);
    else{
        memset(get_resp, 0, MAX_RES_BUF);
        strcat(get_resp, "Too many bytes in results\n\tre-query with smaller constraints\n");
        strcat(get_resp, "\tMAX_SIZE=%d bytes, BYTES_RETURNED=%d bytes\n");
    }
        

    //get_resp[segsize]='\0';
    return segsize;
}

char* get( const char* path)
{
    CURLcode ret;
    int wr_error;

    wr_error = 0;
    //get_curl = curl_easy_init();
    //fprintf(stdout, "sfslib::GET %s\n", path);
    if (!get_curl) {
        printf("couldn't init curl\n");
        return 0;
    }

    memset(get_resp, 0, MAX_RES_BUF);
    bytes_rcvd=0;
    memset(fpath, 0, MAX_URL_BUF);
    strcpy(fpath, sfs_server);
    strcat(fpath, path);
    fprintf(stdout, "sfslib.get::GET %s\n", fpath);

    /* Tell curl the URL of the file we're going to retrieve */
    curl_easy_setopt( get_curl, CURLOPT_URL, fpath );

    /* Tell curl that we'll receive data to the function write_data, and
    * also provide it with a context pointer for our error return.
    */
    curl_easy_setopt( get_curl, CURLOPT_WRITEDATA, (void *)&wr_error );
    //fprintf(stdout, "sfslib.get:: CURLOPT_WRITEDATA set\n");
    curl_easy_setopt( get_curl, CURLOPT_WRITEFUNCTION, get_writer );
    //fprintf(stdout, "sfslib.get:: CURLOPT_WRITEFUNCTION set\n");
    curl_easy_setopt(get_curl, CURLOPT_FAILONERROR, 1);
    //fprintf(stdout, "sfslib.get:: CURLOPT_FAILONERROR set\n");
    curl_easy_setopt(get_curl, CURLOPT_HEADER, 0);
    //fprintf(stdout, "sfslib.get:: CURLOPT_HEADER set\n");

    /* Allow curl to perform the action */
    //fprintf(stdout, "sfslib.get::calling easy_perform");
    ret = curl_easy_perform( get_curl );
    //fprintf(stdout, "sfslib.get::easy_perform called");
    //curl_easy_cleanup(get_curl);

    //printf( "ret = %d (write_error = %d)\n", ret, wr_error );

    /* Emit the page if curl indicates that no errors occurred */
    if ( ret != 0 )
        memset(get_resp, 0, MAX_RES_BUF);
    else if(get_resp[0] == '{')
        get_resp[last_index_of(get_resp, '}')+1]='\0';
    return get_resp;
}

int delete_(const char * path){
	CURLcode res;
    long http_code=200L;

    fprintf(stdout, "input_path=%s\n", path);
	if(delete_curl && path!=NULL) {
        memset(fpath, 0, MAX_URL_BUF);
        strcpy(fpath, sfs_server);
        strcat(fpath, path);
        fprintf(stdout, "DELETE fpath=%s\n", fpath);

		curl_easy_setopt(delete_curl, CURLOPT_URL, fpath);
		curl_easy_setopt(delete_curl, CURLOPT_HEADER, 0);
        curl_easy_setopt(delete_curl, CURLOPT_FAILONERROR, 1);
        curl_easy_setopt(delete_curl, CURLOPT_CUSTOMREQUEST, "DELETE");

        res = curl_easy_perform(delete_curl);
        curl_easy_getinfo (delete_curl, CURLINFO_RESPONSE_CODE, &http_code);
	}

    if(http_code != 200L)
        return -1;
    return 0;
}

int mkdefault(const char * path){
	CURLcode res;
    char* parent;       //the directory to create the sub-directory in
    char* hd_src;
    long http_code;
    int send_size=0;

    fprintf(stdout, "mkdefault::input_path=%s\n", path);
	if(path!=NULL) {
        parent = split_parent_child(path, 0);
        memset(fpath, 0, MAX_URL_BUF);
        strcpy(fpath, sfs_server);
        strcat(fpath, parent);
        fprintf(stdout, "PUT fpath=%s\n", fpath);

        new_node_name = split_parent_child(path,1);
        send_size = CREATE_DEFAULT_SIZE + strlen(new_node_name);
        hd_src =(char*)malloc(send_size + 5);
		curl_easy_setopt(put_curl, CURLOPT_URL, fpath);
		curl_easy_setopt(put_curl, CURLOPT_HEADER, 1);
        curl_easy_setopt(put_curl, CURLOPT_FAILONERROR, 1);
        curl_easy_setopt(put_curl, CURLOPT_READFUNCTION, read_cr_callback);
        curl_easy_setopt(put_curl, CURLOPT_UPLOAD, 1L);
        curl_easy_setopt(put_curl, CURLOPT_PUT, 1L);
        curl_easy_setopt(put_curl, CURLOPT_READDATA, hd_src);
        curl_easy_setopt(put_curl, CURLOPT_INFILESIZE_LARGE,(curl_off_t)send_size);

        //We lock here for safety, bit this is a major performance
        //bottlebeck at scale; must be re-designed
		res = curl_easy_perform(put_curl);
        curl_easy_getinfo (put_curl, CURLINFO_RESPONSE_CODE, &http_code);

        free(parent);
        free(hd_src);
        free(new_node_name);

        if(http_code==201L)
            return 0;
        else
            fprintf(stdout, "\thttp_code=%ld\n", http_code);
	}

    errno = ENOENT;
    return -1;
}

int mkstream(const char * path){
	CURLcode res;
    char* parent;       //the directory to create the sub-directory in
    char* hd_src;
    long http_code;
    int send_size=0;

    fprintf(stdout, "mkstream::input_path=%s\n", path);
	if(put_curl && path!=NULL) {
        set_globals();
        parent = split_parent_child(path, 0);
        memset(fpath, 0, MAX_URL_BUF);
        strcpy(fpath, sfs_server);
        strcat(fpath, parent);
        fprintf(stdout, "PUT fpath=%s\n", fpath);

        new_node_name = split_parent_child(path,1);
        send_size = CREATE_DEFAULT_SIZE + strlen(new_node_name);
        hd_src =(char*)malloc(send_size + 5);
		curl_easy_setopt(put_curl, CURLOPT_URL, fpath);
		curl_easy_setopt(put_curl, CURLOPT_HEADER, 1);
        curl_easy_setopt(put_curl, CURLOPT_FAILONERROR, 1);
        curl_easy_setopt(put_curl, CURLOPT_READFUNCTION, read_cs_callback);
        curl_easy_setopt(put_curl, CURLOPT_UPLOAD, 1L);
        curl_easy_setopt(put_curl, CURLOPT_PUT, 1L);
        curl_easy_setopt(put_curl, CURLOPT_READDATA, hd_src);
        curl_easy_setopt(put_curl, CURLOPT_INFILESIZE_LARGE,(curl_off_t)send_size);
                     
		res = curl_easy_perform(put_curl);
        curl_easy_getinfo (put_curl, CURLINFO_RESPONSE_CODE, &http_code);

        free(parent);
        free(hd_src);
        free(new_node_name);

        if(http_code==201L)
            return 0;
        else
            fprintf(stdout, "\thttp_code=%ld\n", http_code);
	}

    errno = ENOENT;
    return -1;
}

/**
 * Return 1 if it is a "default" file, 0 if we received a response but 
 * the resource is a stream, -1 if not response was received.
 */
int isdir(const char * path, int *size){
    char* resp;
    cJSON* obj;
    char lpath[MAX_URL_BUF];
    memset(lpath, 0, MAX_URL_BUF);
    strcpy(lpath, path);
    fprintf(stdout, "sfslib.isdir::lpath=%s\n", lpath);
    resp=get(lpath);
    fprintf(stdout, "resp=%s\n", resp);
    if(resp !=NULL && (*size=strlen(resp))>0){
        if(strcmp(path, "/")==0)
            return 1;
        //convert resp to json object, return true if "type=DEFAULT" is set in the response
        obj = cJSON_Parse(resp);
        if(obj != NULL && cJSON_GetObjectItem(obj, "type") != NULL &&
            strcmp(cJSON_GetObjectItem(obj, "type")->valuestring, "DEFAULT")==0){
            cJSON_Delete(obj);
            return 1;
        }
    }

    if(*size==0)
        return -1;

    return 0;
}

size_t read_cr_callback(void *ptr, size_t size, size_t nmemb, void *stream){
    if(new_node_name != NULL && ptr != NULL){
        cJSON * json = cJSON_CreateObject();
        cJSON_AddStringToObject(json, "operation", "create_resource");
        cJSON_AddStringToObject(json, "resourceName", new_node_name);
        cJSON_AddStringToObject(json, "resourceType", "default");
        strncpy((char*)ptr, cJSON_Print(json), size*nmemb);
        cJSON_Delete(json);
        return strlen(ptr)*sizeof(char);
    }
    return 0;
}

size_t read_cs_callback(void *ptr, size_t size, size_t nmemb, void *stream){
    if(new_node_name != NULL && ptr != NULL){
        cJSON * json = cJSON_CreateObject();
        cJSON_AddStringToObject(json, "operation", "create_generic_publisher");
        cJSON_AddStringToObject(json, "resourceName", new_node_name);
        strncpy((char*)ptr, cJSON_Print(json), size*nmemb);
        cJSON_Delete(json);
        return strlen(ptr)*sizeof(char);
    }
    return 0;
}

int last_index_of(const char* source, char c){
    int idx=-1;
    int i=0;
    int srclen = 0;
    if(source != NULL) {
        srclen = strlen(source);
        for(i=srclen-1; i>=0; i--){
            if(source[i] == c)
                return i;
        }
    }
    return idx;
}

char* split_parent_child(const char* path, int parent_child){
    char * fullpath;
    char* parent;
    char* last_tok;
    char* tok;
    int new_length=0;
    if(path != NULL){
        //root case
        if(strcmp(path, "/")==0 && parent_child==0){
            fullpath= (char*)malloc(sizeof(char)*strlen(path) + 1);
            memset(fullpath, 0, sizeof(char)*strlen(path) + 1);
            strcpy(fullpath,"/");
            return fullpath;
        } else if(strcmp(path, "/")==0 && parent_child!=0){
            last_tok = (char*)malloc(sizeof(char)*strlen(path) + 1);
            memset(last_tok, 0, sizeof(char)*strlen(path) + 1);
            last_tok = (char*)malloc(sizeof(char)*strlen(path) + 1);
            strcpy(last_tok, "");
            return last_tok;
        }

        //non-corner case
        fullpath= (char*)malloc(sizeof(char)*strlen(path) + 1);
        memset(fullpath, 0, sizeof(char)*strlen(path) + 1);
        last_tok = (char*)malloc(sizeof(char)*strlen(path) + 1);
        memset(last_tok, 0, sizeof(char)*strlen(path) + 1);
        strcpy(fullpath, path);
        tok = strtok(fullpath, "/");
        while(tok != NULL){
            memset(last_tok, 0, sizeof(char)*strlen(path) + 1);
            strcpy(last_tok,tok);
            last_tok[strlen(last_tok)]='\0';
            tok = strtok(NULL, "/");
        }
        strcpy(fullpath, path);
        parent = (char*) malloc(sizeof(char)*(strlen(fullpath)-strlen(last_tok)));
        new_length= strlen(fullpath)-(strlen(last_tok)+1);
        strncpy(parent, fullpath, new_length);
        parent[new_length]='\0';
        free(fullpath);
        if(parent_child==0){
            if(strcmp(parent, "")==0)
                strcpy(parent, "/\0");
            free(last_tok);
            return parent;
        } else {
            free(parent);
            return last_tok;
        }
    }
    return NULL;
}

/*int main(int argc, char* argv[]){
    char * resp;
    init_sfslib();
    resp = get("/homes/jorge/acmes/2224/power?query=true&ts_timestamp=lt:now,gt:now-86400");
    fprintf(stdout, "resp=%s\n", resp);
    shutdown_sfslib();
    return 0;
}*/

/*int main(int argc, char*argv[]){
    char* query;
    char* rep;
    int isdir_;
    init_sfslib();
    rep =get("/homes/jorge/acmes/2218/power");
    fprintf(stdout, "rep=%s\n", rep);
    isdir_ = isdir("/homes/jorge/acmes/2218/power");
    rep=get("/homes/jorge/acmes/2218/power");
    fprintf(stdout, "rep=%s\n", rep);
    query = "/homes/jorge/acmes/2218/power?query=true&ts_timestamp=lt:now,gt:now-800\n";
    rep = get(query);
    fprintf(stdout, "rep=%s\n", rep);
    shutdown_sfslib();
    return 0;
}*/

void test(){
    char path[MAX_URL_BUF];
    char* childstr;
    cJSON* json;
    cJSON* children;
    cJSON* thischild_json;
    int i, csize;
    char* childstr_nq;      //child string with no quotes
    char* getresp = get("/");
	if(strlen(getresp)>0){
        //get("/", &data);
		fprintf(stdout,"get(\"/\")=%s",getresp);
        //fprintf(stdout, "\tlast_char=%c\n", data[strlen(data)-1]);
        json = cJSON_Parse(getresp);
        //free(data);
        children = cJSON_GetObjectItem(json, "children");
        csize = cJSON_GetArraySize(children);
        fprintf(stdout, "\ncsize=%s\n", cJSON_Print(children));
        for (i=0; i<csize; i++){
            thischild_json = cJSON_GetArrayItem(children, i);
            childstr = cJSON_PrintUnformatted(thischild_json);
            childstr_nq = (char*)malloc(strlen(childstr)-2);
            strncpy(childstr_nq, childstr+1, strlen(childstr)-2);
            fprintf(stdout, "\tchild[%d]=%s\n", i, childstr_nq);

            memset(path, 0, MAX_URL_BUF);
            strcat(&path[0], "/");  
            strcat(&path[strlen(path)], childstr_nq);
            fprintf(stdout, "main_path=%s\n", path);
            fprintf(stdout, "RESP::%s\n",get(path));
            free(childstr);
            free(childstr_nq);
        }
        cJSON_Delete(json);
    }
}

void test2(){
    int size;
    fprintf(stdout, " isdir(\"/pub\")?  %d\n", isdir("/pub", &size));
    fprintf(stdout, " isdir(\"/temp\")?  %d\n", isdir("/temp", &size));
    fprintf(stdout, " isdir(\"/temp\")?  %d\n", isdir("/temp/strm1", &size));
}

void test3(){
    mkdefault("/temp");
    //resp = get("/temp");
    //fprintf(stdout, "test3::get(\"temp\")=%s\n", resp);
}

void test4(){
    mkstream("/temp/strm1");
    //resp = get("/temp");
    //fprintf(stdout, "test3::get(\"temp\")=\%s\n", resp);
}

void test5(){
    delete_("/temp/strm1");
    delete_("/temp");
}

void test6(){
    char* resp;
    cJSON* cache;
    cJSON* resp_json;
    cJSON* resp_json_ref;
    char * prefix = "/homes/jorge/acmes/2218/power";
    resp=get("/homes/jorge/acmes/2218/power?query=true&ts_timestamp=lt:now,gte:now-1800");
    if(strlen(resp)>0){
        cache = cJSON_CreateObject();
        fprintf(stdout, "resp=%s\n\n\tldix=%d, strlen=%d\n", resp, last_index_of(resp, '}'), (int)strlen(resp));
        resp_json = cJSON_Parse(resp);
        if(resp_json!=NULL){
            cJSON_AddItemToObject(cache, prefix, resp_json);
            resp_json_ref = cJSON_GetObjectItem(cache, prefix);
            fmt_ts_query_result(resp_json_ref, prefix);
        } else{
            fprintf(stdout, "Couldn't parse the response\n");
        }
        cJSON_Delete(cache);
    }
}

static char* fmt_ts_query_result(cJSON* prev_reply_json, const char* path){
    char* resp_cpy_str;         //feed()'d locally
    char* fmtstr;               //free()'d by caller
    cJSON* data_obj;            //delete not needed
    cJSON* tsarray;             //delete not needed
    int i=0,dtptct=0;

    /*pthread_mutex_lock(&qres_lock);
    if((prev_reply_json=cJSON_GetObjectItem(queryres_cache, path)) != NULL){*/
    resp_cpy_str = cJSON_PrintUnformatted(prev_reply_json);
    fprintf(stdout, "resp_cpy_str=%s\n", resp_cpy_str);
    //resp_cpy = cJSON_Parse(resp_cpy_str);
    fmtstr = (char*)malloc(strlen(resp_cpy_str));
    memset(fmtstr, 0, sizeof(fmtstr));
    free(resp_cpy_str);
    /*}
    pthread_mutex_unlock(&qres_lock);*/
    tsarray=cJSON_GetObjectItem(prev_reply_json, "ts_query_results");
    if(tsarray!=NULL){
        fprintf(stdout, "tsarray NOT NULL\n");
    } else {
        fprintf(stdout, "tsarray NULL\n");
    }

    if(tsarray!=NULL){
        dtptct = cJSON_GetArraySize(tsarray);
        fprintf(stdout, "array_size=%d\n", dtptct);
        if(dtptct>0){
            for(i=0; i<dtptct; ++i){
                data_obj = cJSON_GetArrayItem(tsarray, i);
                sprintf(fmtstr + strlen(fmtstr), "%s %s\n", 
                    cJSON_Print(cJSON_GetObjectItem(data_obj,"ts")),
                    cJSON_Print(cJSON_GetObjectItem(data_obj, "value")));
            }
            
        }
        fprintf(stdout, "fmtstr=\n%s", fmtstr);
        //cJSON_Delete(resp_cpy);
    
    }
    return fmtstr;
}

