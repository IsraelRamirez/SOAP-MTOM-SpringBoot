#ifndef RUTS_H
#define RUTS_H
#include <string>
#include <vector>
using namespace std;
class ruts {
public:
    ruts();
    ~ruts();
    vector<double> puntajes;
    vector<int> codCarreras;
    void setRut(string );
    string getRut();
private:
    string rut;
    

};

#endif /* RUTS_H */

