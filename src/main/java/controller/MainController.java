package controller;

import content.connection.ConnectionHandler;
import content.connection.WikiField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainController {

    private ConnectionHandler connectionHandler;
    private LocalDate selectedDate;
    private LinkedHashMap<Integer, TreeItem<String>> hiddenBornAndDead;//as tree nodes are in sorted order this is as well
    private LinkedHashMap<Integer, TreeItem<String>> hiddenPolandEvents;//as tree nodes are in sorted order this is as well
    private LinkedHashMap<Integer, TreeItem<String>> hiddenWorldEvents;//as tree nodes are in sorted order this is as well

    @FXML
    private ListView listViewImieniny;

    @FXML
    private ListView listViewChoosenEvents;

    @FXML
    private TreeView treeViewSwieta;

    @FXML
    private TreeView treeViewEvents;

    @FXML
    private TreeView treeViewBornDeath;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField textFieldFrom;

    @FXML
    private TextField textFieldTo;

    @FXML
    private TextField textFieldSearchPhrase;

    @FXML
    public void initialize(){

        hiddenBornAndDead = new LinkedHashMap<Integer, TreeItem<String>>();
        hiddenPolandEvents = new LinkedHashMap<Integer, TreeItem<String>>();
        hiddenWorldEvents = new LinkedHashMap<Integer, TreeItem<String>>();

        selectedDate = LocalDateTime.now().toLocalDate();
        datePicker.setValue(selectedDate);

        //send LocalDateTime it will be converter by the ConnectionHandler
        connectionHandler = new ConnectionHandler(selectedDate);
        setViewSelectionListeners();
        setViews();

        addTextValueChangeListener(textFieldFrom);
        addTextValueChangeListener(textFieldTo);
    }


    @FXML
    public void connectionClick(){
        LocalDate datePickerValue = datePicker.getValue();

        //if dates differ
        if(datePickerValue != selectedDate ) {
            //clear map with hidden elements so it won't be added during filtering years for the new date
            hiddenBornAndDead.clear();
            hiddenPolandEvents.clear();
            hiddenWorldEvents.clear();

            connectionHandler.downloadNewDocument(datePickerValue);
            setViews();
        }
    }

    @FXML
    public void filterYearsButtonClicked() {

        Integer filterFrom, filterTo;
        TreeItem<String> rootBornDeath = treeViewBornDeath.getRoot();
        TreeItem<String> rootEvents = treeViewEvents.getRoot();

        System.out.println("TO JEST TEKST" + textFieldFrom.getText());

        if (textFieldFrom.getText().trim().isEmpty())
            filterFrom = -99999;//first written historical sources are dated around 3500b.c. so i think it's a fine down limit
        else
            filterFrom = Integer.parseInt(textFieldFrom.getText());

        if (textFieldTo.getText().trim().isEmpty())
            filterTo = 99999;//i think it's fine upper limit
        else
            filterTo = Integer.parseInt(textFieldTo.getText());

        filterYears(rootBornDeath, hiddenBornAndDead, filterFrom, filterTo);
        filterYears(rootEvents.getChildren().get(0), hiddenPolandEvents, filterFrom, filterTo);
        filterYears(rootEvents.getChildren().get(1), hiddenWorldEvents, filterFrom, filterTo);
    }

    @FXML
    public void searchPhraseButtonClicked(){
        String searchPhrase = textFieldSearchPhrase.getText();

        //there is something to look for
        if (!searchPhrase.trim().isEmpty()){
            TreeItem<String> rootPeople = treeViewBornDeath.getRoot();

            for (TreeItem<String> itemYear : rootPeople.getChildren()) {
                for(TreeItem<String> itemBornDead: itemYear.getChildren())
                    if( doesChildrenContainPatter(itemBornDead, searchPhrase))
                    {
                        itemYear.setExpanded(true);//it's not perfect because it may expand 2 child nodes even if only 1 contains search phrase
                        break;
                    }
            }

        }

    }

    @FXML
    public void addSelectedItemClicked(){
        addSelectedItem(treeViewBornDeath);
        addSelectedItem(treeViewEvents);
        addSelectedItem(treeViewSwieta);
    }

    //only the one TreeView selected at the time
    private void setViewSelectionListeners(){
        TreeView[] allViews = {treeViewSwieta, treeViewEvents, treeViewBornDeath};

        for(int i = 0 ; i < 3 ; i++) {
            TreeView firstTmp = allViews[(i + 1) % 3];
            TreeView secondTmp = allViews[(i + 2) % 3];

            allViews[i].getSelectionModel().selectedItemProperty().addListener(((observable, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    firstTmp.getSelectionModel().clearSelection();
                    secondTmp.getSelectionModel().clearSelection();
                }
            }
            ));
        }

    }

    private void setViews(){
        listViewImieniny.setItems(connectionHandler.extractImieniny());
        treeViewSwieta.setRoot(connectionHandler.extractContent(WikiField.Swieta));


        TreeItem<String> events = new TreeItem<String>("Wydarzenia");
        events.setExpanded(true);
        events.getChildren().add(connectionHandler.extractContent(WikiField.PolskaEvents));
        events.getChildren().add(connectionHandler.extractContent(WikiField.WorldEvents));
        treeViewEvents.setRoot(events);

        treeViewBornDeath.setRoot(connectionHandler.extractBornDeath());

    }

    private void addTextValueChangeListener(TextField textField){
        textField.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue)
            {
                String newValueAltered= newValue;

                //it ensures that only 4 characters can be written into the TextField
                if(textField.getText().length() > 4) {
                    newValueAltered = textField.getText().substring(0, 4);
                }

                //regex to ensure that only numeric value will be passed
                newValueAltered = newValueAltered.replaceAll("[^\\d]", "");

                //ustawienie zmodyfikowanej wartości
                textField.setText(newValueAltered);
            }
        });
    }

    private void filterYears(TreeItem<String> root, LinkedHashMap<Integer, TreeItem<String>> hiddenMap, Integer filterFrom, Integer filterTo){
        //if there were some alternations before, then recover former state of the treeView
        if (!hiddenMap.isEmpty()) {
            ObservableList<TreeItem<String>> childrenList = root.getChildren();

            int offset = 0;//how many items were added

            int childrenListSize = childrenList.size();

            //SĄ BŁĘDY JEŻELI NA LIŚCIE ZOSTAJE TYLKO JEDEN ELEMENT, MOŻNA UWZGLĘDNIĆ PRZYPADEK TYLKO DLA JEDNEGO ELEMENTU, ALBO OGARNĄĆ LOGIKĘ

            for (int i = 0; i < childrenListSize - 1; i++) {
                System.out.println(childrenList.get(i).getValue());

                Iterator<Map.Entry<Integer, TreeItem<String>>> iter = hiddenMap.entrySet().iterator();
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

            Iterator<Map.Entry<Integer, TreeItem<String>>> iter = hiddenMap.entrySet().iterator();
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
                    hiddenMap.put(currentItemYear, item);//mapa jest chyba pusta - null pointer exception
                }
            }

        }


        //deleting this item from root so it doesn't exist in treeview anymore
        for (Integer key : hiddenMap.keySet()) {
            TreeItem<String> item = hiddenMap.get(key);
            item.getParent().getChildren().remove(item);
        }

        System.out.println(treeViewBornDeath.getRoot().getChildren());
    }

    private boolean doesChildrenContainPatter(TreeItem<String> node, String searchPhrase){
        for( TreeItem<String> child : node.getChildren() ){
            if(child.getValue().toLowerCase().contains(searchPhrase.toLowerCase()))
                return true;
        }

        return false;
    }

    private void addSelectedItem(TreeView treeView){
        MultipleSelectionModel selectionModel = treeView.getSelectionModel();

        //if there's a valid selection
        if(!selectionModel.isEmpty()) {
            TreeItem<String> selectedItem = (TreeItem<String>) selectionModel.getSelectedItem();

            //add it only if selected node doesn't have children so it's person or event selected not category or year
            if (selectedItem.getChildren().isEmpty()) {
                String parentValue = selectedItem.getParent().getValue();
                String parentParentValue =  selectedItem.getParent().getParent().getValue();
                listViewChoosenEvents.getItems().add( parentParentValue + " - " + parentValue + " - " + selectedItem.getValue());
            }
        }
    }
}
