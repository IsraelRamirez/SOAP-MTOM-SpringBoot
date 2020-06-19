#include "connect.h"

connect::connect() {
    _connection = NULL;
    _outCome = NULL;
}

connect::~connect() {
}

void connect::dbconnect(char* ipserver, char* port, char* dbname, char* user, char* password){
    _connection = NULL;
    char* uri = new char[250];
    snprintf(uri, 250, "host='%s' port='%s' dbname='%s' user='%s' password='%s'",ipserver,port,dbname,user,password);
    
    _connection = PQconnectdb(uri);

    if(PQstatus(_connection) == CONNECTION_BAD){
        cout<< stderr << endl << "Error al conectar con el servidor: "<< PQerrorMessage(_connection) <<endl;
        _connection = NULL;
    }
}

void connect::dbclose(){
    PQfinish(_connection);
}

void connect::dbquery(char* query){
    _outCome = NULL;
    _outCome = PQexec(_connection, query);
}

long connect::dbnumrows(){

    long count = 0;
    char* tuplas = NULL;

    if(_outCome != NULL){
        tuplas = PQcmdTuples(_outCome);
        count = atol(tuplas);
    }

    return count;
}

char* connect::dbOutCome(int row, int column){
    char* outCome = NULL;
    if(_outCome != NULL){
        outCome = PQgetvalue(_outCome, row, column);
    }
    return outCome;
}

void connect::dbfree(){
    PQclear(_outCome);
    _outCome = NULL;
}