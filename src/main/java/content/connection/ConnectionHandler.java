package content.connection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class ConnectionHandler {
    Document docWiki;

    public ConnectionHandler(LocalDate selectedDate) {
        downloadNewDocument(selectedDate);
    }

    public void downloadNewDocument(LocalDate selectedDate) {
        int choosenDay = selectedDate.getDayOfMonth();
        String choosenMonth = MonthConversion.monthToString(selectedDate.getMonthValue());

        try {
            docWiki = Jsoup.connect("https://pl.wikipedia.org/wiki/" + choosenDay + "_" + choosenMonth).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //ArrayList<String>
    public ObservableList extractImieniny() {
        Elements imieniny = docWiki.select(".mw-parser-output > ul");

        Element imieninyElement = imieniny.get(0).select("li").first();
        String imieninyLine = imieninyElement.text();
        //gettingg rid of words and letters that aren't needed
        imieninyLine = imieninyLine.replace("Imieniny obchodzą: ", "").replace(" i", ",").replace(".", "");

        ObservableList imieninyArray = FXCollections.observableArrayList();

        //lista imion obchodzących imieniny
        for (String imie : imieninyLine.split(", "))
            imieninyArray.add(imie);

        return imieninyArray;
    }

    public TreeItem<String> extractContent(WikiField whichField) {
        //create an item for the view
        TreeItem<String> rootItem = new TreeItem<String>(whichField.fieldName());

        //extract webcontent
        Elements swieta = docWiki.select(".mw-parser-output > ul");

        //omit first element (Imieniny)
        boolean first_element = false;

        //if we want to check święta
        if (whichField.fieldNumber() == 0)
            first_element = true;

        // "> li" so it won't count nested list
        for (Element swieto : swieta.get(whichField.fieldNumber()).select("> li")) {
            //omit first part (with names)
            if (first_element) {
                first_element = false;
                continue;
            }

            TreeItem<String> countryItem = new TreeItem<String>();

            //check whether it's a list
            Elements listaSwiat = swieto.select("ul");
            if (!listaSwiat.isEmpty()) {
                String country = swieto.select("a").get(0).text();
                countryItem.setValue(country);

                for (Element zListy : listaSwiat.get(0).select("li")) {
                    TreeItem<String> swietoItem = new TreeItem<String>(zListy.text());
                    countryItem.getChildren().add(swietoItem);
                }

            } else {
                String[] swietoString = swieto.text().split(" – | ", 2);//sometimes " - " separates year and description and sometimes " "

                countryItem.setValue(swietoString[0]);
                TreeItem<String> swietoItem = new TreeItem<String>(swietoString[1]);
                countryItem.getChildren().add(swietoItem);
            }

            rootItem.getChildren().add(countryItem);
        }


        return rootItem;
    }

    public TreeItem<String> extractBornDeath() {
        TreeItem<String> rootItem = new TreeItem<String>("Urodzili się/zmarli");
        rootItem.setExpanded(true);

        //stores for year - key node with dead or born
        TreeMap<Integer, ArrayList<TreeItem<String>>> treeItemMap = new TreeMap<Integer, ArrayList<TreeItem<String>>>();

        //get born/death and year
        extractTreeItem(WikiField.Born, treeItemMap);
        extractTreeItem(WikiField.Dead, treeItemMap);

        //I take year from the map, create a node with child nodes taken from the arraylist
        for ( Integer year : treeItemMap.keySet() ){
            ArrayList<TreeItem<String>> mapValue = treeItemMap.get(year);

            TreeItem<String> yearTreeItem = new TreeItem<String>(year.toString());
            rootItem.getChildren().add(yearTreeItem);

            for ( TreeItem<String> treeItem : mapValue)
                yearTreeItem.getChildren().add(treeItem);
        }

        return rootItem;
    }

    private void extractTreeItem(WikiField whichField, TreeMap<Integer, ArrayList<TreeItem<String>>> treeItemMap) {

        //extract webcontent
        Elements extractedElements = docWiki.select(".mw-parser-output > ul");

        int offset = 0;//if there's astronomy field the offset for born and death fields is 1 (because of structure of wikipedia)

        if( (extractedElements.size() == 6) && (whichField == WikiField.Born || whichField == WikiField.Dead) ) //field check in case if some other elements would be called in future
            offset = 1;//set proper offset if there's an astronomy field

        //         // "> li" so it won't count nested list
        for (Element element : extractedElements.get(whichField.fieldNumber() + offset).select("> li")) {

            TreeItem<String> bornDeathItem = new TreeItem<String>(whichField.fieldName());
            bornDeathItem.setExpanded(true);

            Integer year;

            Elements peopleList = element.select("ul");
            if (!peopleList.isEmpty()) {
                year = Integer.valueOf(element.select("a").get(0).text().replaceAll("[^\\d]", ""));//replace all aby mieć tylko cyfry(niektóre wpisy mają dwukropek)


                for (Element person : peopleList.get(0).select("li")) {
                    TreeItem<String> personItem = new TreeItem<String>(person.text());
                    bornDeathItem.getChildren().add(personItem);
                }

            } else {
                String[] personString = element.text().split(" – ", 2);
                year = Integer.valueOf(personString[0].replaceAll("[^\\d]", ""));
                TreeItem<String> personItem = new TreeItem<String>(personString[1]);
                bornDeathItem.getChildren().add(personItem);
            }


            //I created map that stores node for related year, so I can add it to the item for item related to the year(this is done by a different function)

            //add proccesed item
            ArrayList<TreeItem<String>> mapValue = treeItemMap.get(year);

            //if mapItem wasnt yet created for this year
            if (mapValue == null) {
                mapValue = new ArrayList<TreeItem<String>>();
                treeItemMap.put(year, mapValue);
            }

            mapValue.add(bornDeathItem);
        }
    }

    private static class MonthConversion
    {
        private static final Map<Integer, String> monthMap;
        static {
            LinkedHashMap<Integer, String> aMap = new LinkedHashMap<Integer, String>();
            aMap.put(1, "stycznia");
            aMap.put(2, "lutego");
            aMap.put(3, "marca");
            aMap.put(4, "kwietnia");
            aMap.put(5, "maja");
            aMap.put(6, "czerwca");
            aMap.put(7, "lipca");
            aMap.put(8, "sierpnia");
            aMap.put(9, "września");
            aMap.put(10, "października");
            aMap.put(11, "listopada");
            aMap.put(12, "grudnia");

            monthMap = Collections.unmodifiableMap(aMap);
        }

        private static String monthToString ( int month )
        {
            return monthMap.get(month);
        }
    }
}