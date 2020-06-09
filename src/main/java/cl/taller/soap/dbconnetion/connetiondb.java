package cl.taller.soap.dbconnetion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connetiondb {
    static Connection connection = null;
    static String url = "jdbc:ucanaccess://./src/main/java/cl/taller/soap/staticfiles/ponderadosDB.accdb";
    
    public static Connection connectDB(){
        try{
            if(connection==null){
                connection = DriverManager.getConnection(url);
            }
        }catch(SQLException ex){
            connection =null;
        }
        return connection;
    }
}
