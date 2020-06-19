#ifndef CONNECT_H
#define CONNECT_H

#include <iostream>
#include <string>
#include <postgresql/libpq-fe.h>

using namespace std;

class connect {
public:
    connect();
    virtual ~connect();
    
    /**
     * Función para conectar a la base de datos
     * @param ipserver Ip del servidor de postgresql
     * @param port Puerto del servidor de postgresql
     * @param dbname Nombre de la base de datos
     * @param user Usuario para conectar a la base de datos
     * @param password Contraseña del usuario
    */
    void dbconnect(char* ipserver, char* port, char* dbname, char* user, char* password);

    /**
     * Función para desconectar la base de datos
    */
    void dbclose();

    /** 
     * Funcion para consultar a la base de datos
     * @param query es la consulta en SQL para la base de datos
    */
    void dbquery(char* query);

    /** 
     * Entrega el número de filas afectadas por una consulta a la base de datos
     * @return Entrega la cantidad de filas afectadas por el resultado outCome
    */
    long dbnumrows();

    /**
     * 
     * @param row Fila a consultar
     * @param column Columna a consultar
     * @return Devuelve el resultado de la fila y la columna seleccionada
    */
    char* dbOutCome(int row, int column);

    /**
     * Funcion para liberar la memoria de un resultado
    */
    void dbfree();

private:
    PGconn* _connection;
    PGresult* _outCome;
};

#endif /* CONNECT_H */