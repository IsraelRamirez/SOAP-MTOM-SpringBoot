#include "carreras.h"

carreras::carreras() {
}

carreras::~carreras() {
}

void carreras::setCod(int cod){
    codCarrera = cod;
}
void carreras::setVacant(int v){
    vacant=v;
}

int carreras::getCod(){
    return codCarrera;
}

int carreras::getVacant(){
    return vacant;
}

