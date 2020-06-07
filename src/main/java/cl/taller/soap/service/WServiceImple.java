package cl.taller.soap.service;

import cl.taller.soap.localhost.soap.File;
import cl.taller.soap.localhost.soap.GetDataRequest;
import cl.taller.soap.localhost.soap.GetDataResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class WServiceImple {
    
    public GetDataResponse puntajes(GetDataRequest request) throws UnsupportedEncodingException, IOException{
        GetDataResponse getData = new GetDataResponse();
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String fileName= "puntajes.xlsx";
                
        if(request.getToken().equals("permitir")){
            int codCarrera = Integer.parseInt(request.getCodCarrera());
            Workbook wb = new SXSSFWorkbook();
            Sheet sheet = wb.createSheet();
            String fullData = new String(Base64.getDecoder().decode(request.getContent()),"UTF-8");
            String[] datas = fullData.split("\n");
            for(int i = 0; i< datas.length;i++){
                List<String> full = operador(datas[i],codCarrera);
                System.out.println(full);
                Row row = sheet.createRow(i);
                for(int j=0; j <2;j++){
                    Cell cell = row.createCell(j);
                    cell.setCellValue(full.get(j));
                }
            }
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            
            getData.setFile(setData(bos,fileName,mimeType,codCarrera));
        }
        return getData;
    }
    /**
     * Función para setear los datos del objeto File
     * @param bos Array de bytes con el binario del archvio
     * @param fileName Nombre del archivo
     * @param mimeType Tipo mime del archivo
     * @param carrera código de la carrera seleccionada
     * @return devuelve el objeto con File con los datos aplicados
     */
    public File setData(ByteArrayOutputStream bos, String fileName, String mimeType, int carrera){
        File file = new File();
        file.setFilename(String.valueOf(carrera)+"_"+fileName);
        file.setContent(bos.toByteArray());
        file.setMimetype(mimeType);
        return file;
    }
    /**
     * Función para operar sobre los datos de un alumno 
     * @param data datos de un alumno
     * @param carrera código de la carrera perteneciente 
     * @return La información de un alumno, según la carrera elegida
     */
    public List<String> operador(String data, int carrera) throws IOException{
        String[] datos = data.split(";");
        List<String> info = new ArrayList<>();
        if(datos.length>1){
            info.add(datos[0]);
            info.add(String.valueOf(ponderador(datos, carrera)));
        }
        return info;
    }
    /**
     * Función que pondera los puntajes psu de un alumno
     * @param data Array de strings con los puntajes de un alumno
     * @param carrera Codigo de la carrera perteneciente
     * @return La ponderación
     */
    public double ponderador(String[] data, int carrera) throws FileNotFoundException, IOException{
        FileInputStream fis = new FileInputStream(new java.io.File("./src/main/java/cl/taller/soap/staticfiles/puntajescarrera.xlsx"));
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet =  wb.getSheetAt(0);
        int thisRow = thisRow(wb,carrera);
        if(thisRow!=-1){
            XSSFRow row = sheet.getRow(thisRow(wb,carrera));
            double ponderado = (Double.parseDouble(data[1])* row.getCell(3).getNumericCellValue())+
                                (Double.parseDouble(data[2])* row.getCell(4).getNumericCellValue())+
                                (Double.parseDouble(data[3])* row.getCell(5).getNumericCellValue())+
                                (Double.parseDouble(data[4])* row.getCell(6).getNumericCellValue());
            if(Double.parseDouble(data[5])>Double.parseDouble(data[6])){
                ponderado += (Double.parseDouble(data[5])* row.getCell(7).getNumericCellValue());
            }
            else{
                ponderado += (Double.parseDouble(data[6])* row.getCell(7).getNumericCellValue());
            }
            
            return ponderado;
        }
        return 0;
        
    }
    /**
     * Función que obtiene la fila donde se encuentra los ponderadores de la carrera
     * @param wb WoorkBook del archivo excel con los ponderadores 
     * @param carrera Codigo de la carrera
     * @return La fila donde se encuentra los ponderadores de una carrera
     */
    public int thisRow(XSSFWorkbook wb,int carrera){
        int thisRow=-1;
        FormulaEvaluator formulaEvaluator=wb.getCreationHelper().createFormulaEvaluator();
        
        XSSFSheet sheet = wb.getSheetAt(0);
        for(int i =3; i<32;i++){
            Row row = sheet.getRow(i);
            Cell cell = row.getCell(2);
            if(formulaEvaluator.evaluateInCell(cell).getCellType() == CellType.NUMERIC){
                if((int) (cell.getNumericCellValue())==carrera){
                    return row.getRowNum();
                }
            }
        }
        return thisRow;
    }
    
}