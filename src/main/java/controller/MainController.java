package controller;

import content.connection.ConnectionHandler;
import content.connection.WikiField;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

public class MainController {

    private ConnectionHandler connectionHandler;
    private LocalDate selectedDate;

    @FXML
    private ListView listViewImieniny;

    @FXML
    private TreeView treeViewSwieta;

    @FXML
    private TreeView treeViewPolandEvents;

    @FXML
    private TreeView treeViewBornDeath;

    @FXML
    private Button connectionButton;

    @FXML
    private DatePicker datePicker;

    @FXML
    public void initialize(){
        //w końcowej wersji powinno tu ładować połączenie dla aktualnego dnia

        selectedDate = LocalDateTime.now().toLocalDate();
        datePicker.setValue(selectedDate);


        //do tego wysyłam datę, która jest konwertowana przez ConnectionHandler
        connectionHandler = new ConnectionHandler(selectedDate);
        setViews();
    }


    @FXML
    public void connectionClick(){
        LocalDate datePickerValue = datePicker.getValue();

        //if dates differ
        if(!(datePickerValue == selectedDate )) {
            connectionHandler.downloadNewDocument(datePickerValue);
            setViews();
        }
    }

    public void setViews(){
        listViewImieniny.setItems(connectionHandler.extractImieniny());
        treeViewSwieta.setRoot(connectionHandler.extractContent(WikiField.Swieta));


        TreeItem<String> events = new TreeItem<String>("Święta");
        events.setExpanded(true);
        events.getChildren().add(connectionHandler.extractContent(WikiField.PolskaEvents));
        events.getChildren().add(connectionHandler.extractContent(WikiField.WorldEvents));
        treeViewPolandEvents.setRoot(events);

        treeViewBornDeath.setRoot(connectionHandler.extractBornDeath());

    }
}
