package controller;

import content.connection.ConnectionHandler;
import content.connection.WikiField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainController {

    private ConnectionHandler connectionHandler;
    private LocalDate selectedDate;
    private LinkedHashMap<Integer, TreeItem<String>> hiddenBornAndDead;//as tree nodes are in sorted order this is as well

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
    private TextField textFieldFrom;

    @FXML
    private TextField textFieldTo;

    @FXML
    public void initialize(){

        hiddenBornAndDead = new LinkedHashMap<Integer, TreeItem<String>>();

        selectedDate = LocalDateTime.now().toLocalDate();
        datePicker.setValue(selectedDate);


        //do tego wysyłam datę, która jest konwertowana przez ConnectionHandler
        connectionHandler = new ConnectionHandler(selectedDate);
        setViews();

        addTextValueChangeListener(textFieldFrom);
        addTextValueChangeListener(textFieldTo);

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

    @FXML
    public void textFieldFromAction(){

    }

    @FXML
    public void filterYearsButtonClicked() {

        Integer filterFrom, filterTo;
        TreeItem<String> root = treeViewBornDeath.getRoot();

        System.out.println("TO JEST TEKST" + textFieldFrom.getText());

        if (textFieldFrom.getText().trim().isEmpty())
            filterFrom = -99999;
        else
            filterFrom = Integer.parseInt(textFieldFrom.getText());

        if (textFieldTo.getText().trim().isEmpty())
            filterTo = 99999;
        else
            filterTo = Integer.parseInt(textFieldTo.getText());

        //if there were some alternations before, then recover former state of the treeView
        if (!hiddenBornAndDead.isEmpty()) {
            ObservableList<TreeItem<String>> childrenList = root.getChildren();

            int offset = 0;//how many items was added

            int childrenListSize = childrenList.size();

            for (int i = 0; i < childrenListSize - 1; i++) {
                System.out.println(childrenList.get(i).getValue());

                Iterator<Map.Entry<Integer, TreeItem<String>>> iter = hiddenBornAndDead.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Integer, TreeItem<String>> entry = iter.next();

                    System.out.println(entry);

                    if (entry.getKey() < Integer.parseInt(childrenList.get(i + offset).getValue())) {
                        childrenList.add(i + offset, entry.getValue());
                        offset++;
                        iter.remove();
                    }//dla ostatniego powinno dodawać inaczej, bo już mogą być tylko większe
                }

            }

            Iterator<Map.Entry<Integer, TreeItem<String>>> iter = hiddenBornAndDead.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Integer, TreeItem<String>> entry = iter.next();

                System.out.println(entry);

                    childrenList.add(childrenListSize + offset, entry.getValue());
                    offset++;
                    iter.remove();
                }

        }


        System.out.println("END OF RECOVERY");
    //if date from is bigger than date to then do not do anything
    if (filterFrom < filterTo) {
        for (TreeItem<String> item : root.getChildren()) {
            int currentItemYear = Integer.parseInt(item.getValue());


            if ((currentItemYear < filterFrom) || (currentItemYear > filterTo)) {
                hiddenBornAndDead.put(currentItemYear, item);//mapa jest chyba pusta - null pointer exception
            }
        }

    }


    //deleting this item from root so it doesn't exist in treeview anymore
    for (Integer key : hiddenBornAndDead.keySet()) {
        TreeItem<String> item = hiddenBornAndDead.get(key);
        item.getParent().getChildren().remove(item);
    }

    System.out.println(treeViewBornDeath.getRoot().getChildren());


    }

    private void setViews(){
        listViewImieniny.setItems(connectionHandler.extractImieniny());
        treeViewSwieta.setRoot(connectionHandler.extractContent(WikiField.Swieta));


        TreeItem<String> events = new TreeItem<String>("Święta");
        events.setExpanded(true);
        events.getChildren().add(connectionHandler.extractContent(WikiField.PolskaEvents));
        events.getChildren().add(connectionHandler.extractContent(WikiField.WorldEvents));
        treeViewPolandEvents.setRoot(events);

        treeViewBornDeath.setRoot(connectionHandler.extractBornDeath());

    }

    private void addTextValueChangeListener(TextField textField){
        //ustawia listener na polu tekstowym!!!
        textField.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue)
            {
                String newValueAltered= newValue;

                //jeżeli wpisano więcej niz 4 znaka
                if(textField.getText().length() > 4) {
                    newValueAltered = textField.getText().substring(0, 4);
                }

                //tylko cyfry!
                newValueAltered = newValueAltered.replaceAll("[^\\d]", "");

                //ustawienie zmodyfikowanej wartości
                textField.setText(newValueAltered);

                /*
                //zrobić żeby nie powtarzało wyszukiwania przy identycznych polach
                if (oldValue != newValueAltered)
                {
                    System.out.println(newValueAltered);

                }*/


            }
        });
    }

}
