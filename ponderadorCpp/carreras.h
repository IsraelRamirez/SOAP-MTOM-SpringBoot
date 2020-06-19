#ifndef CARRERAS_H
#define CARRERAS_H
#include <string>
#include <vector>
#include <utility>
using namespace std;

class carreras {
public:
    carreras();
    
    vector<pair<string,double>> personas;
    void setCod(int );
    void setVacant(int );
    int getVacant();
    int getCod();
    
    virtual ~carreras();
private:
    int codCarrera;
    int vacant;
};

#endif /* CARRERAS_H */

