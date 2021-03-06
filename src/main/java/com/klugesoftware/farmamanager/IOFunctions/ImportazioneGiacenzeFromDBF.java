package com.klugesoftware.farmamanager.IOFunctions;

import com.klugesoftware.farmamanager.db.GiacenzeDAOManager;
import com.klugesoftware.farmamanager.model.Giacenze;
import com.klugesoftware.farmamanager.utility.GetDBFFileName;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class ImportazioneGiacenzeFromDBF extends Task {

    private final Logger logger = LogManager.getLogger(ImportazioneGiacenzeFromDBF.class.getName());
    private Connection connection;
    private final String PROPERTIES_FILE_NAME = "./resources/config/config.properties";
    private final Properties propsFarmaManager = new Properties();
    private String tabella_name;
    private String queryForGiacenDBF;

    public ImportazioneGiacenzeFromDBF(){
        try{
            InputStream propertiesFile = new FileInputStream(PROPERTIES_FILE_NAME);
            propsFarmaManager.load(propertiesFile);
            GetDBFFileName dbfFileName = new GetDBFFileName();
            tabella_name =  dbfFileName.getGiacenzeFileNameAnnoCorrente();
            Class.forName(propsFarmaManager.getProperty("dbfDriverName"));
            connection = DriverManager.getConnection(propsFarmaManager.getProperty("dbfUrlName"));
            queryForGiacenDBF = "select * from "+tabella_name+"  ";
        }catch(Exception ex){
            logger.error("I can't create Connection",ex);
        }

    }

    @Override
    protected Object call() throws Exception {

        //svuota la tabella Giacenze
        updateMessage("Sto cancellando la tabella Giacenze...");
        logger.info("cancellazione giacenze");
        GiacenzeDAOManager.deleteTable();

        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = connection.createStatement();
            rs = stmt.executeQuery(queryForGiacenDBF);
            Giacenze giacenza = null;
            logger.info("inizio importazione giacenze");
            while (rs.next() && !isCancelled()){
                giacenza = new Giacenze();
                giacenza.setMinsan(rs.getString("MINSAN10"));
                giacenza.setDescrizione(rs.getString("DESCRIZ"));
                if (rs.getBigDecimal("COSTOULT") != null)
                    giacenza.setCostoUltimoDeivato(rs.getBigDecimal("COSTOULT"));
                giacenza.setDataCostoUltimo(rs.getDate("DTULTACQ"));
                giacenza.setGiacenza(rs.getInt("GIACTOT"));
                giacenza.setVenditeAnnoInCorso(rs.getInt("VENANNO"));
                insertGiacenza(giacenza);
            }
            logger.info("fine importazione giacenze");
        }catch(SQLException ex){
            logger.error("call(): I can't create record...");
        }finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            }catch(SQLException ex){
                logger.error("I can't close db connestions");

            }
        }

        if(isCancelled()) {
            updateMessage("Importazione giacenze interrotta");
            logger.info("importazione giacenze interrotta");

        }
        else
            updateMessage("Importazione giacenze terminata");

        return null;
    }

    private void insertGiacenza(Giacenze giacenza){

        giacenza = GiacenzeDAOManager.insert(giacenza);
        updateMessage("Ho inserito il minsan "+giacenza.getMinsan());
    }

}
