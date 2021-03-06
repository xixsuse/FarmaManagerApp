package com.klugesoftware.farmamanager.controller;

import com.klugesoftware.FarmaManagerUpdating.ftp.FTPConnector;
import com.klugesoftware.farmamanager.DTO.ElencoTotaliGiornalieriRowData;
import com.klugesoftware.farmamanager.db.ElencoTotaliGiornalieriRowDataManager;
import com.klugesoftware.farmamanager.db.ImportazioniDAOManager;
import com.klugesoftware.farmamanager.model.Importazioni;
import com.klugesoftware.farmamanager.utility.DateUtility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SituazioneVenditeEProfittiController extends VenditeEProfittiController implements Initializable {

    private final Logger logger = LogManager.getLogger(SituazioneVenditeEProfittiController.class.getName());
    private final String FTP_PROPERTIES_FILE = "./resources/config/configFtp.properties";
    @FXML private Button btnVenditeLibere;
    @FXML private TableView<ElencoTotaliGiornalieriRowData> tableVenditeEProfittiTotali;
    @FXML private TableColumn<ElencoTotaliGiornalieriRowData,String> colData;
    @FXML private TableColumn<ElencoTotaliGiornalieriRowData,BigDecimal> colTotaleVendite;
    @FXML private TableColumn<ElencoTotaliGiornalieriRowData,BigDecimal> colTotaleProfitti;
    @FXML private AreaChart<String,Number> graficoVenditeEProfitti;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private DatePicker txtFldDataFrom;
    @FXML private DatePicker txtFldDataTo;
    @FXML private Label lblTotVendite;
    @FXML private Label lblTotProfitti;
    @FXML private Label lblTitleTotProfitti;
    @FXML private Label lblTitleTotVendite;
    @FXML private Button btnBack;
    @FXML private Button btnForward;
    @FXML private Label lblPeriodo;
    @FXML private Label lblTitle;
    @FXML private RadioButton rdtBtnVistaSettimanale;
    @FXML private RadioButton rdtBtnVistaMensile;
    @FXML private Button btnAggMov;
    @FXML private Button btnUpdate;
    private ChangePeriodListener changePeriodListenerBack;
    private ChangePeriodListener changePeriodListenerNext;
    private ChangeDateAndViewListener changeDateListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        btnUpdate.setVisible(false);
        rdtBtnVistaMensile.setUserData("vistaMensile");
        rdtBtnVistaSettimanale.setUserData("vistaSettimanale");
        txtFldDataFrom.setUserData("dataFrom");
        txtFldDataTo.setUserData("dataTo");
        changeDateListener = new ChangeDateAndViewListener(this);
        txtFldDataFrom.setOnAction(changeDateListener);
        txtFldDataTo.setOnAction(changeDateListener);
        rdtBtnVistaSettimanale.setOnAction(changeDateListener);
        rdtBtnVistaMensile.setOnAction(changeDateListener);
        changePeriodListenerBack = new ChangePeriodListener(PeriodToShow.SETTIMANA,PeriodDirection.BACK,this);
        changePeriodListenerNext = new ChangePeriodListener(PeriodToShow.SETTIMANA,PeriodDirection.FORWARD,this);
        btnBack.setOnAction(changePeriodListenerBack);
        btnForward.setOnAction(changePeriodListenerNext);
        colData.setCellValueFactory(new PropertyValueFactory<ElencoTotaliGiornalieriRowData,String>("data"));
        colTotaleVendite.setCellValueFactory(new PropertyValueFactory<ElencoTotaliGiornalieriRowData,BigDecimal>("totaleVenditeLorde"));
        colTotaleProfitti.setCellValueFactory(new PropertyValueFactory<ElencoTotaliGiornalieriRowData,BigDecimal>("totaleProfitti"));

        colData.setCellFactory(e ->{ return new TableCell<ElencoTotaliGiornalieriRowData,String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(DateUtility.converteGUIStringDDMMYYYYToNameDayOfWeek(item));
                }
            }
        };
        });

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.ITALY);
        DecimalFormat df = (DecimalFormat)nf;

        colTotaleProfitti.setCellFactory(new Callback<TableColumn<ElencoTotaliGiornalieriRowData, BigDecimal>, TableCell<ElencoTotaliGiornalieriRowData, BigDecimal>>() {
            @Override
            public TableCell<ElencoTotaliGiornalieriRowData, BigDecimal> call(TableColumn<ElencoTotaliGiornalieriRowData, BigDecimal> e) {
                return new TableCell<ElencoTotaliGiornalieriRowData, BigDecimal>() {
                    @Override
                    public void updateItem(BigDecimal item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(df.format(item));
                        }
                    }
                };
            }
        });

        colTotaleVendite.setCellFactory(new Callback<TableColumn<ElencoTotaliGiornalieriRowData, BigDecimal>, TableCell<ElencoTotaliGiornalieriRowData, BigDecimal>>() {
            @Override
            public TableCell<ElencoTotaliGiornalieriRowData, BigDecimal> call(TableColumn<ElencoTotaliGiornalieriRowData, BigDecimal> e) {
                return new TableCell<ElencoTotaliGiornalieriRowData, BigDecimal>() {
                    @Override
                    public void updateItem(BigDecimal item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(nf.format(item));
                        }
                    }
                };
            }
        });

        ObservableList<ElencoTotaliGiornalieriRowData> elencoRighe = itemList();
        tableVenditeEProfittiTotali.getItems().setAll(elencoRighe);

        aggiornaGrafico(graficoVenditeEProfitti,elencoRighe);
        //aggiornaMovimenti();
        verifyUpdate();

    }

    private void verifyUpdate(){
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(FTP_PROPERTIES_FILE));

            if ( prop.getProperty("updating").equals("true")){
                btnUpdate.setVisible(true);
                btnUpdate.setTooltip(new Tooltip("C'è un'aggiornamento versione. Clicca per aggiornare."));
            }
        }catch (IOException ex){
            logger.error(ex.getMessage());
        }
    }

    private void aggiornaGrafico(AreaChart<String,Number> graficoVenditeEProfitti,ObservableList<ElencoTotaliGiornalieriRowData> elencoRighe){

        xAxis.setLabel("Giorno");
        yAxis.setLabel("Valore (in €)");
        List<String> xLabels = new ArrayList<String>();

        XYChart.Series seriesProfitti = new XYChart.Series();
        seriesProfitti.setName("Profitti");
        XYChart.Series seriesVendite = new XYChart.Series();
        seriesVendite.setName("Vendite");
        Iterator<ElencoTotaliGiornalieriRowData> iter = elencoRighe.iterator();
        ElencoTotaliGiornalieriRowData totale;
        String toDay;
        BigDecimal totVendite = new BigDecimal(0);
        BigDecimal totProfitti = new BigDecimal(0);
        while(iter.hasNext()){
            totale = (ElencoTotaliGiornalieriRowData)iter.next();
            toDay = Integer.toString(DateUtility.getGiornoDelMese(DateUtility.converteGUIStringDDMMYYYYToDate(totale.getData())));
            xLabels.add(toDay);
            totVendite = totVendite.add(totale.getTotaleVenditeLorde());
            totProfitti = totProfitti.add(totale.getTotaleProfitti());
            seriesProfitti.getData().add(new XYChart.Data(toDay,totale.getTotaleProfitti()));
            seriesVendite.getData().add(new XYChart.Data(toDay,totale.getTotaleVenditeLorde()));
        }
        ObservableList<String> xxLabels = FXCollections.observableArrayList(xLabels);
        xAxis.getCategories().clear();
        xAxis.setCategories(xxLabels);
        graficoVenditeEProfitti.getData().clear();
        graficoVenditeEProfitti.getData().addAll(seriesProfitti,seriesVendite);

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.ITALY);
        DecimalFormat df = (DecimalFormat)nf;
        lblTotVendite.setText(df.format(totVendite.doubleValue()));
        lblTotProfitti.setText(df.format(totProfitti.doubleValue()));
    }

    private ObservableList<ElencoTotaliGiornalieriRowData> itemList(){

        Date toDate;
        Date fromDate;

        Importazioni importazione = ImportazioniDAOManager.findUltimoInsert();
        if (importazione.getIdImportazione() != null) {
            toDate = importazione.getDataUltimoMovImportato();
            Calendar myCal = Calendar.getInstance(Locale.ITALY);
            myCal.setTime(toDate);

            int diff = 0;
            if (myCal.getFirstDayOfWeek() < myCal.get(Calendar.DAY_OF_WEEK))
                diff = myCal.get(Calendar.DAY_OF_WEEK) - myCal.getFirstDayOfWeek();

            if (myCal.get(Calendar.DAY_OF_WEEK) == 1) {
                myCal.add(Calendar.DAY_OF_YEAR, -6);
                fromDate = myCal.getTime();
            } else {
                if (diff > 0)
                    diff = diff * (-1);
                myCal.add(Calendar.DAY_OF_YEAR, diff);
                fromDate = myCal.getTime();
            }


            txtFldDataFrom.getEditor().setText(DateUtility.converteDateToGUIStringDDMMYYYY(fromDate));
            txtFldDataTo.getEditor().setText(DateUtility.converteDateToGUIStringDDMMYYYY(toDate));

            myCal.setTime(fromDate);
            txtFldDataFrom.setValue(LocalDate.of(myCal.get(Calendar.YEAR), myCal.get(Calendar.MONTH) + 1, myCal.get(Calendar.DAY_OF_MONTH)));
            myCal.setTime(toDate);
            txtFldDataTo.setValue(LocalDate.of(myCal.get(Calendar.YEAR), myCal.get(Calendar.MONTH) + 1, myCal.get(Calendar.DAY_OF_MONTH)));


            return FXCollections.observableArrayList(ElencoTotaliGiornalieriRowDataManager.lookUpElencoTotaliGiornalieriBetweenDate((fromDate), (toDate)));
        }else
            return FXCollections.observableArrayList();

    }

    private void aggiornaMovimenti(){
        btnAggMov.setVisible(false);
        Importazioni importazioni = ImportazioniDAOManager.findUltimoInsert();
        if (importazioni.getIdImportazione()!= null) {
            Calendar dateTo = Calendar.getInstance(Locale.ITALY);
            Calendar dateFrom = Calendar.getInstance(Locale.ITALY);
            dateFrom.setTime(importazioni.getDataUltimoMovImportato());
            dateFrom.set(Calendar.DAY_OF_MONTH,dateFrom.get(Calendar.DAY_OF_MONTH)+1);
            dateTo.set(Calendar.DAY_OF_MONTH, dateTo.get(Calendar.DAY_OF_MONTH) - 1);
            if (dateFrom.before(dateTo)) {
                btnAggMov.setVisible(true);
            }
        }
    }

    @FXML
    private void aggMovimentiClicked(ActionEvent event){
        Calendar dateFrom = Calendar.getInstance(Locale.ITALY);
        Calendar dateTo = Calendar.getInstance(Locale.ITALY);
        dateFrom.setTime((ImportazioniDAOManager.findUltimoInsert().getDataUltimoMovImportato()));
        dateFrom.set(Calendar.DAY_OF_MONTH,dateFrom.get(Calendar.DAY_OF_MONTH)+1);
        dateTo.set(Calendar.DAY_OF_MONTH,dateTo.get(Calendar.DAY_OF_MONTH)-1);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Importazione");
        alert.setHeaderText("L'ultimo movimento importato risale al "+DateUtility.converteDateToGUIStringDDMMYYYY(ImportazioniDAOManager.findUltimoInsert().getDataUltimoMovImportato()));
        alert.setContentText("Vuoi aggiornare i movimenti dal "+DateUtility.converteDateToGUIStringDDMMYYYY(dateFrom.getTime())+" al "+DateUtility.converteDateToGUIStringDDMMYYYY(dateTo.getTime())+" ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/klugesoftware/farmamanager/view/Settings.fxml"));
                Parent parent = (Parent)fxmlLoader.load();
                SettingsController controller = fxmlLoader.getController();
                controller.fireAggiornaMovimentiButton();
                Scene scene = new Scene(parent);
                Stage app_stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                app_stage.setScene(scene);
                app_stage.show();
            }catch(Exception ex){
                logger.error(ex.getMessage());
            }
        } else {
            ;
        }
    }

    @FXML
    private void mostraProdottiClicked(ActionEvent event){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/klugesoftware/farmamanager/view/ElencoProdottiEProfitti.fxml"));
            Parent parent = (Parent)fxmlLoader.load();
            ElencoProdottiEProfittiController controller = fxmlLoader.getController();
            Scene scene = new Scene(parent);
            Stage app_stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            app_stage.setScene(scene);
            app_stage.show();
        }catch(Exception ex){
            logger.error(ex.getMessage());
        }
    }

    @FXML
    private void settingsClicked(ActionEvent event){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/klugesoftware/farmamanager/view/Settings.fxml"));
            Parent parent = (Parent)fxmlLoader.load();
            SettingsController controller = fxmlLoader.getController();
            Scene scene = new Scene(parent);
            Stage app_stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            app_stage.setScene(scene);
            app_stage.show();
        }catch(Exception ex){
            logger.error(ex.getMessage());
        }
    }

    @FXML
    private void listenerEsciButton(ActionEvent event){
        System.exit(0);

    }

    @FXML
    private void btnUpdateClicked(ActionEvent event){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Aggiornamento Versione FarmaManager");
        alert.setHeaderText("C'è una nuova versione di FarmaManager.");
        alert.setContentText("Vuoi aggiornare?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            try {
                    /*
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/klugesoftware/farmamanager/view/Settings.fxml"));
                    Parent parent = (Parent)fxmlLoader.load();
                    SettingsController controller = fxmlLoader.getController();
                    controller.fireButton();
                    Scene scene = new Scene(parent);
                    Stage app_stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                    app_stage.setScene(scene);
                    app_stage.show();
                    */
            }catch(Exception ex){
                logger.error(ex.getMessage());
            }
        } else {
            ;
        }
    }

    @FXML
    private void analisiLibereClicked(ActionEvent event){

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/klugesoftware/farmamanager/view/SituazioneVenditeEProfittiLibere.fxml"));
            Parent parent = (Parent) fxmlLoader.load();
            SituazioneVenditeEProfittiLibereController controller = fxmlLoader.getController();
            boolean vistaSettimanale = false;
            if (rdtBtnVistaSettimanale.isSelected())
                vistaSettimanale = true;
            controller.aggiornaTableAndScene(DateUtility.converteGUIStringDDMMYYYYToDate(txtFldDataFrom.getEditor().getText()), DateUtility.converteGUIStringDDMMYYYYToDate(txtFldDataTo.getEditor().getText()), vistaSettimanale);
            Scene scene = new Scene(parent);
            Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            app_stage.setScene(scene);
            app_stage.show();
        }catch(Exception ex){
            logger.error(ex);
        }

    }

    @FXML
    private void versionInfoClicked(MouseEvent event){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Questa è una versione beta, quindi potrebbero esserci errori o funzionalità dell'applicazione che non sono stati ancora perfezionati." +
                "\nSi tratta di una versione preliminare. Se ci sono funzioni che vorresti aggiungere faccelo sapere." +
                "\nGrazie.\nBuon Lavoro");

        alert.showAndWait();
    }

    @FXML
    private void DettaglioVenditeClicked(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/klugesoftware/farmamanager/view/DettagliVenditeEProfitti.fxml"));
        Parent parent = (Parent)fxmlLoader.load();
        DettaglioVenditeEProfittiController dettaglioController = fxmlLoader.getController();
        boolean vistaSettimanale = true;
        if (rdtBtnVistaMensile.isSelected())
            vistaSettimanale = false;
        dettaglioController.aggiornaTableAndScene(DateUtility.converteGUIStringDDMMYYYYToDate(txtFldDataFrom.getEditor().getText()),DateUtility.converteGUIStringDDMMYYYYToDate(txtFldDataTo.getEditor().getText()),vistaSettimanale);
        Scene scene = new Scene(parent);
        Stage app_stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        app_stage.setScene(scene);
        app_stage.show();
    }

    /*
    aggiorna il contenuto della tableview in funzione dell'intervallo di date; inoltre aggirna  anche il valore dei
    campi date picker, rispettando al corrispondenza fra il contenuto della tabella ed il valore dei campi datePicker
     */
    private void aggiornaTable(ObservableList<ElencoTotaliGiornalieriRowData> elencoRighe,Date fromDate,Date toDate){
        Calendar myCal = Calendar.getInstance(Locale.ITALY);
        myCal.setTime(fromDate);
        txtFldDataFrom.setValue(LocalDate.of(myCal.get(Calendar.YEAR),myCal.get(Calendar.MONTH)+1,myCal.get(Calendar.DAY_OF_MONTH)));
        myCal.setTime(toDate);
        txtFldDataTo.setValue(LocalDate.of(myCal.get(Calendar.YEAR),myCal.get(Calendar.MONTH)+1,myCal.get(Calendar.DAY_OF_MONTH)));

        txtFldDataFrom.getEditor().setText(DateUtility.converteDateToGUIStringDDMMYYYY(fromDate));
        txtFldDataTo.getEditor().setText(DateUtility.converteDateToGUIStringDDMMYYYY(toDate));
        tableVenditeEProfittiTotali.getItems().clear();
        tableVenditeEProfittiTotali.getItems().setAll(elencoRighe);
        tableVenditeEProfittiTotali.refresh();
        aggiornaGrafico(graficoVenditeEProfitti, elencoRighe);

    }

    /*
    aggiorna il periodo di riferimento della tabella e le varie label in funzione del periodo di riferimento( settimana o mese)
     */
    @Override
    public void aggiornaTableAndScene(Date dateFrom, Date dateTo, boolean vistaSettimanale){

        ObservableList<ElencoTotaliGiornalieriRowData> elencoRighe = FXCollections.observableArrayList(ElencoTotaliGiornalieriRowDataManager.lookUpElencoTotaliGiornalieriBetweenDate((dateFrom),(dateTo)));
        aggiornaTable(elencoRighe,dateFrom,dateTo);
        if (vistaSettimanale){
            changePeriodListenerBack.setPeriod(PeriodToShow.SETTIMANA);
            changePeriodListenerNext.setPeriod(PeriodToShow.SETTIMANA);
            lblPeriodo.setText(" Settimana");
            rdtBtnVistaSettimanale.setSelected(true);
        }else{
            changePeriodListenerBack.setPeriod(PeriodToShow.MESE);
            changePeriodListenerNext.setPeriod(PeriodToShow.MESE);
            lblPeriodo.setText("    Mese  ");
            rdtBtnVistaMensile.setSelected(true);
        }

    }

    @Override
    public void aggiornaTable(Date dateFrom,Date dateTo){

        ObservableList<ElencoTotaliGiornalieriRowData> elencoRighe = FXCollections.observableArrayList(ElencoTotaliGiornalieriRowDataManager.lookUpElencoTotaliGiornalieriBetweenDate((dateFrom),(dateTo)));
        if(!elencoRighe.isEmpty()) {
            //TODO: messsaggio se movimenti mancanti....
            aggiornaTable(elencoRighe, dateFrom, dateTo);
        }
    }

    public Date getDateFrom(){
        return DateUtility.converteGUIStringDDMMYYYYToDate(txtFldDataFrom.getEditor().getText());
    }

    public Date getDateTo(){
        return DateUtility.converteGUIStringDDMMYYYYToDate(txtFldDataTo.getEditor().getText());
    }


    public RadioButton getRdbtVistaMensile(){
        return rdtBtnVistaMensile;
    }
    public RadioButton getRdbtVistaSettimanale(){
        return rdtBtnVistaSettimanale;
    }

}
