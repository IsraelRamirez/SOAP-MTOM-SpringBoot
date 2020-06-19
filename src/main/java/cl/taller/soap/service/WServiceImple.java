package cl.taller.soap.service;

import static cl.taller.soap.dbconnetion.connetiondb.connectDB;
import cl.taller.soap.models.File;
import cl.taller.soap.models.GetDataRequest;
import cl.taller.soap.models.GetDataResponse;
import cl.taller.soap.models.carreras;
import cl.taller.soap.models.ruts;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class WServiceImple{
    
    private static final String token = "fk2x6rpw6fDCkXqDlqeeR22u8jpN6qGa";
    private static final String error = "Token ingresado no valido.";
    private static final String queryPonderador = "SELECT nem, ranking,lenguaje,matematica,histociencia FROM ponderado WHERE codCarrera=";
    private static final String queryPonderadorMultiple = "SELECT nem, ranking,lenguaje,matematica,histociencia,codCarrera,first FROM ponderado WHERE ";
    private static final String queryInit = "SELECT codCarrera, vacant FROM ponderado";
    private static final String queryFirst = "SELECT first FROM ponderado WHERE codCarrera=";
    /**
     * Función que valida la llave token ingresada por el cliente
     * @param request Objecto con los puntajes y datos de los alumnos
     * @return Los puntajes ponderados en caso de que el token esté bien ingresado
     * @throws java.io.UnsupportedEncodingException Excepción con respecto a la encodificación de datos
     * @throws java.io.IOException Excepción respectiva a un error de input/output
     * @throws java.io.FileNotFoundException Excepción respectiva a que no se encuentra un archivo
     * @throws java.sql.SQLException Excepción respectiva a un error en la conexión a la base de datos o en la sentencia SQL
     */
    public GetDataResponse auth(GetDataRequest request) throws IOException, UnsupportedEncodingException, FileNotFoundException, SQLException{
        
        if(request.getToken().equals(token)){
            return puntajes(request);
        }
        File invalidToken = new File();
        
        GetDataResponse data = new GetDataResponse();
        invalidToken.setContent(error.getBytes());
        invalidToken.setFilename("error.txt");
        invalidToken.setMimetype("text/plain");
        data.setFile(invalidToken);
        return data;
             
    }
    /**
     * Función principal del WS soap, calcula las ponderaciones de los de acuerdo a sus puntajes PSU y la carrera que eligen
     * @param request Requerimiento del cliente en forma de objeto con los datos asociados
     * @return devuelve las ponderaciones de los puntajes de cada alumno dependiendo de la carrera
     * @throws java.io.UnsupportedEncodingException Excepción con respecto a la encodificación de datos
     * @throws java.io.IOException Excepción respectiva a un error de input/output
     * @throws java.io.FileNotFoundException Excepción respectiva a que no se encuentra un archivo
     * @throws java.sql.SQLException Excepción respectiva a un error en la conexión a la base de datos o en la sentencia SQL
     */
    private GetDataResponse puntajes(GetDataRequest request) throws UnsupportedEncodingException, IOException, FileNotFoundException, SQLException{
        GetDataResponse getData = new GetDataResponse();
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String fileName= "puntajes.xlsx";
        
        List<carreras> carreras = initCarreras();
        List<ruts> ruts = new ArrayList<ruts>();
        
        String fullData = new String(Base64.getDecoder().decode(request.getContent()),"UTF-8");
        String[] datas = fullData.split("\n");
        
        operador(datas,carreras,ruts);
        
        while(!isEmpty(ruts)){

            int rutLength = ruts.size();
            for(int i = 0; i<rutLength;i++){
                int rutCodLength = ruts.get(i).codCarreras.size();
                
                if(rutCodLength>0){
                    
                    if(ruts.get(i).codCarreras.get(rutCodLength-1)!=-1){ //Si el último código ingresado es -1, significa que está en un carrera
                        Pair<Integer, Double> pair = ponderadorMultiple(ruts.get(i));
                        if(pair.getValue()!=0.0){ //Si la ponderación es 0, significa que no existe una carrera en la que este rut pueda ingresar, por lo tanto se le asigna -1 como código de carrera
                            for(int j=0;j<carreras.size();j++){
                                if(carreras.get(j).getCod()==pair.getKey()){
                                    int index = indexador(carreras.get(j).personas,pair.getValue(),carreras.get(j).getVacant());
                                    if(index>=0){
                                        carreras.get(j).personas.add(index,new Pair<>(ruts.get(i).getRut(),pair.getValue()));
                                        ruts.get(i).codCarreras.add(-1);
                                    }
                                    else if(index == -2){
                                        ruts.get(i).codCarreras.add(carreras.get(j).getCod());
                                    }
                                    else if(index == -1){
                                        carreras.get(j).personas.add(new Pair<>(ruts.get(i).getRut(),pair.getValue()));
                                        ruts.get(i).codCarreras.add(-1);
                                    }
                                    int carPersonasLength = carreras.get(j).personas.size();
                                    if(carPersonasLength>carreras.get(j).getVacant()){
                                        String tmpRut = carreras.get(j).personas.get(carPersonasLength-1).getKey();
                                        carreras.get(j).personas.remove(carPersonasLength-1);
                                        for(int k=0;k<rutLength;k++){
                                            if(ruts.get(k).getRut().equals(tmpRut)){
                                                ruts.get(k).codCarreras.add(carreras.get(j).getCod());
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        else{
                            ruts.get(i).codCarreras.add(-1);
                        }
                    }
                    
                }
            }
        }
        getData.setFile(setData(getExcel(carreras),fileName,mimeType));
        return getData;
    }
    /**
     * Función que genera, según los ruts ingresados en la carrera, un excel con los datos obtenidos
     * @param carreras Lista con las carreras, sus ruts ingresados y sus ponderaciones
     * @return Devuelve un Array de byte con el formato de excel "xlsx"
     * @throws IOException Excepción relacionada con algún fallo generado por el ingreso de los datos en el formato excel     
     */
    private ByteArrayOutputStream getExcel(List<carreras> carreras) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Workbook wb = new SXSSFWorkbook();
        
        for(int i=0;i< carreras.size();i++){
            Sheet sheet = wb.createSheet(String.valueOf(carreras.get(i).getCod()));
            for(int j=0;j<carreras.get(i).personas.size();j++){
                Row row = sheet.createRow(j);
                
                Cell cell = row.createCell(0);
                cell.setCellValue(carreras.get(i).personas.get(j).getKey());
                cell = row.createCell(1);
                cell.setCellValue(carreras.get(i).personas.get(j).getValue());
            }
        }
        wb.write(bos);
        return bos;
    }
    /**
     * Función que genera una lista de carreras, con el codigo de carrera correspondiente.
     * @return Devuelve una lista de las carreras inicialiazadas con el código de carrera.
     * @throws SQLException Excepción relacionada con algún problema con el motor de base de datos usados.
     */
    private List<carreras> initCarreras() throws SQLException{
        List<carreras> carreras=new ArrayList<carreras>();
        Connection connectionDB = connectDB();
        
        if(connectionDB!=null){
            Statement st = connectionDB.createStatement();
            ResultSet rs = st.executeQuery(queryInit);
            while(rs.next()){
                if(rs.getInt(1)!=0){
                    carreras carrera = new carreras();
                    carrera.setCod(rs.getInt(1));
                    carrera.setVacant(rs.getInt(2));
                    carreras.add(carrera);
                }
                
            }
        }
        return carreras;
    }
    /**
     * Función que verifica si todos los ruts están en alguna carrera
     * @param carreras Lista con todas las carreras
     * @return Devuelve si todas las carreras están al máximo en los cupos o si almenos hay una que le sobre cupos
     */
    private boolean isEmpty(List<ruts> ruts){
        boolean isEmpty = true;
        for(int i=0;i<ruts.size();i++){
            if(ruts.get(i).codCarreras.size()>0){
                if(ruts.get(i).codCarreras.get(ruts.get(i).codCarreras.size()-1)!=-1){
                    return false;
                }
            }
        }
        return isEmpty;
    }
    
    /**
     * Función que verifica cual de las dos carreras ingresadas es la que tiene el puntaje del primero mas alto
     * @param carreraA Codigo de carrera de la carrera A
     * @param carreraB Codigo de carrera de la carrera B
     * @return Devuelve si la carrera A tiene el puntajes primero más que la carrera B
     * @throws SQLException Excepción relacionada con algún problema con el motor de base de datos usados.
     */
    private boolean primeroCarrera(int carreraA, int carreraB) throws SQLException{
        boolean isA=true;
        Connection connectionDB = connectDB();
        List<Double> firts = new ArrayList<Double>();
        List<Integer> codCarreras = new ArrayList<Integer>();
        codCarreras.add(carreraA);
        codCarreras.add(carreraB);
        if(connectionDB!=null){
            for(int i = 0; i<2;i++){
                Statement st = connectionDB.createStatement();
                ResultSet rs = st.executeQuery(queryFirst+codCarreras.get(i));
                rs.next();
                firts.add(rs.getDouble(1));
            }
        }
        if(firts.get(0)<firts.get(1)){
            return false;
        }
        return isA;
    }
    
    /**
     * Función para setear los datos del objeto File
     * @param bos Array de bytes con el binario del archvio
     * @param fileName Nombre del archivo
     * @param mimeType Tipo mime del archivo
     * @param carrera código de la carrera seleccionada
     * @return devuelve el objeto con File con los datos aplicados
     */
    private File setData(ByteArrayOutputStream bos, String fileName, String mimeType){
        File file = new File();
        file.setFilename(fileName);
        file.setContent(bos.toByteArray());
        file.setMimetype(mimeType);
        return file;
    }
    /**
     * Función que opera sobre los datos de cada rut y coloca a dicho rut en la carrera en la que consigue mayor ponderación
     * @param data Array con todos los ruts y sus ponderaciones ingresadas
     * @param carrera Lista de todas las carreras en el sistema
     * @param ruts Lista de ruts, donde se guardaran la información de los ruts del array data
     * @throws FileNotFoundException Excepción respectiva a que no se encuentra un archivo
     * @throws IOException Excepción respectiva a un error de input/output
     * @throws SQLException Excepción respectiva a un error en la conexión a la base de datos o en la sentencia SQL
     */
    private void operador(String[] data, List<carreras> carrera, List<ruts> ruts) throws IOException, FileNotFoundException, SQLException{
        
        for(int i=0;i<data.length;i++){
            String[] datos = data[i].split(";");
            if((Double.parseDouble(datos[3])+Double.parseDouble(datos[4]))/2>=450){

                Double mayor = 0.0;
                int codCarrera = carrera.get(0).getCod();
                for(int j=0;j<carrera.size();j++){
                    if(datos.length>1){
                        Double ponderado = ponderador(datos,carrera.get(j).getCod());
                        if(ponderado > mayor){
                            codCarrera = carrera.get(j).getCod();
                            mayor = ponderado;
                        }
                        else if(ponderado == mayor){
                            if(!primeroCarrera(codCarrera,carrera.get(j).getCod())){
                                mayor=ponderado;
                                codCarrera = carrera.get(j).getCod();
                            }
                        }
                        
                    }
                }
                for(int j=0;j<carrera.size();j++){
                    if(carrera.get(j).getCod()==codCarrera){

                        ruts persona = new ruts();
                        persona.setRut(datos[0]);
                        persona.puntajes.add(Double.parseDouble(datos[1]));
                        persona.puntajes.add(Double.parseDouble(datos[2]));
                        persona.puntajes.add(Double.parseDouble(datos[3]));
                        persona.puntajes.add(Double.parseDouble(datos[4]));
                        persona.puntajes.add(Double.parseDouble(datos[5]));
                        persona.puntajes.add(Double.parseDouble(datos[6]));
                        ruts.add(persona);

                        int index = indexador(carrera.get(j).personas,mayor,carrera.get(j).getVacant());
                        if(index>=0){
                            carrera.get(j).personas.add(index,new Pair<>(datos[0],mayor));
                        }
                        else if(index==-2){
                            
                            int length = ruts.size();
                            ruts.get(length-1).codCarreras.add(codCarrera);
                        }
                        else if(index==-1){
                            carrera.get(j).personas.add(new Pair<>(datos[0],mayor));
                        }
                        int carPersonasLength = carrera.get(j).personas.size();
                        if(carPersonasLength>carrera.get(j).getVacant()){
                            String tmpRut = carrera.get(j).personas.get(carPersonasLength-1).getKey();
                            carrera.get(j).personas.remove(carPersonasLength-1);
                            for(int k=0;k<ruts.size();k++){
                                if(ruts.get(k).getRut().equals(tmpRut)){
                                    ruts.get(k).codCarreras.add(carrera.get(j).getCod());
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
    /**
     * Función que pondera los puntajes psu de un alumno
     * @param data Array de strings con los puntajes de un alumno
     * @param codCarrera codigo de la carrera perteneciente
     * @return La ponderación del alumno si es que el promedio simple entre el puntaje de lenguaje y de matematica supera los 450 puntos
     * @throws java.io.FileNotFoundException Excepción respectiva a que no se encuentra un archivo
     * @throws java.io.IOException Excepción respectiva a un error de input/output
     * @throws java.sql.SQLException Excepción respectiva a un error en la conexión a la base de datos o en la sentencia SQL
     */
    private double ponderador(String[] data,int codCarrera) throws FileNotFoundException, IOException, SQLException{
        
        Connection connectionDB = connectDB();
        
        if(connectionDB!=null){
            Statement st = connectionDB.createStatement();
            ResultSet rs = st.executeQuery(queryPonderador+ String.valueOf(codCarrera));
            while(rs.next()){
                double ponderado = 0.0;
                ponderado = (Double.parseDouble(data[1]) * rs.getDouble(1) )+
                        (Double.parseDouble(data[2]) * rs.getDouble(2) )+
                        (Double.parseDouble(data[3]) * rs.getDouble(3) )+
                        (Double.parseDouble(data[4]) * rs.getDouble(4) );
                if(Double.parseDouble(data[5])>Double.parseDouble(data[6])){
                    ponderado += (Double.parseDouble(data[5]) * rs.getDouble(5));
                }
                else{
                    ponderado += (Double.parseDouble(data[6]) * rs.getDouble(5));
                }
                if((Double.parseDouble(data[3])+Double.parseDouble(data[4]))/2 >=450){
                    return ponderado;
                }
                
            }
            
        }
        return 0.0;
    }
    /**
     * Función que al ingresar el rut de un postulante verifica la n-sima mejor ponderación de varias carreras en las que no ha postulado
     * @param rut Clase ruts, se encuentra la información de un postulante, entre ellas, a todas las carreras a las que se le ha ingresa y ha salido por sobre cupo
     * @return Devuelve un par de el código de la siguiente carrera a la que se ingresara y la ponderación que alcanzó en esa carrera
     * @throws SQLException Excepción respectiva a un error en la conexión a la base de datos o en la sentencia SQL
     */
    private Pair<Integer,Double> ponderadorMultiple(ruts rut) throws SQLException{
        Connection connectionDB = connectDB();
        
        if(connectionDB!=null){
            Statement st = connectionDB.createStatement();
            if(rut.codCarreras.get(0)!=-1){
                String fullQuery = queryPonderadorMultiple +" codCarrera <> "+rut.codCarreras.get(0);
                for(int i=1;i<rut.codCarreras.size();i++){
                    if(rut.codCarreras.get(i)!=-1){
                        fullQuery += " AND codCarrera <> "+rut.codCarreras.get(i);
                    }
                }
                ResultSet rs = st.executeQuery(fullQuery);
                int cod = 0;
                double mayor = 0.0;
                double firts = 0.0;

                while(rs.next()){
                    double ponderado = 0.0;
                    ponderado = (rut.puntajes.get(0) * rs.getDouble(1) )+
                            (rut.puntajes.get(1)  * rs.getDouble(2) )+
                            (rut.puntajes.get(2)  * rs.getDouble(3) )+
                            (rut.puntajes.get(3)  * rs.getDouble(4) );
                    if(rut.puntajes.get(4)>rut.puntajes.get(5)){
                        ponderado += (rut.puntajes.get(4)  * rs.getDouble(5));
                    }
                    else{
                        ponderado += (rut.puntajes.get(5)  * rs.getDouble(5));
                    }
                    if((rut.puntajes.get(2) +rut.puntajes.get(3) )/2 >=450){
                        if(ponderado > mayor){
                            mayor=ponderado;
                            cod = rs.getInt(6);
                            firts = rs.getDouble(7);
                        }
                        else if(ponderado == mayor){
                            if(firts<rs.getDouble(7)){
                                mayor=ponderado;
                                cod = rs.getInt(6);
                                firts = rs.getDouble(7);
                            }
                        }
                    }

                }
                return new Pair<>(cod,mayor);
            }
        }
        return null;
    }
    /**
     * Función que devuelve el indice o posicion en el ranking que la ponderación ingresada sobre pasa a la de alguno que se encuentre en el ranking
     * @param carrera Lista de los pontulantes ingresados en una carrera
     * @param ponderado Ponderación obtenida en dicha carrera del nuevo postulante
     * @return Devuelve el índice del primero al que sobre pasa en puntaje en dicha carrera solicitada
     */
    private int indexador(List<Pair<String,Double>> carrera,double ponderado,int vacant){
        int index=-1;
        int length = carrera.size();
        if(length < vacant){
            for(int i =0; i<length;i++){
                if(ponderado > carrera.get(i).getValue()){
                    return i;
                }
            }
        }
        else{
            if(ponderado>carrera.get(length-1).getValue()){
                for(int i =0; i<length;i++){
                    if(ponderado > carrera.get(i).getValue()){
                        return i;
                    }
                }
            }
            else{
                return -2;
            }
        }
        
        return index;
    }
}