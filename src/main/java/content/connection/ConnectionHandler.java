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

//16_sierpnia WSZYSTKO SIĘ ROZJEŻDŻA, bo jest zakładka astronomia(trzeba to uwzględnić)

public class ConnectionHandler {
    Document docWiki;

    public Document getDocWiki() {
        return docWiki;
    }


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
        //getting rid of words and letters that aren't needed
        imieninyLine = imieninyLine.replace("Imieniny obchodzą: ", "").replace(" i", ",").replace(".", "");


        //przechowuje imiona na dany dzien
        //ArrayList<String> imieninyArray = new ArrayList<String>();
        ObservableList imieninyArray = FXCollections.observableArrayList();

        //lista imion obchodzących imieniny
        for (String imie : imieninyLine.split(", "))
            imieninyArray.add(imie);

        return imieninyArray;
    }

    public TreeItem<String> extractContent(WikiField whichField) {
        //create an item for the view
        TreeItem<String> rootItem = new TreeItem<String>(whichField.fieldName());
        rootItem.setExpanded(true);

        //extract webcontent
        Elements swieta = docWiki.select(".mw-parser-output > ul");

        boolean first_element = false;

        //if we want to check święta
        if (whichField.fieldNumber() == 0)
            first_element = true;

        // > li aby pobierać tylko "najwyższe" elementy, żeby pomijać powtórzenia przy zagnieżdżonej liście
        for (Element swieto : swieta.get(whichField.fieldNumber()).select("> li")) {
            //omit first part (with names)
            if (first_element) {
                first_element = false;
                continue;
            }

            TreeItem<String> countryItem = new TreeItem<String>();
            countryItem.setExpanded(true);

            //sprawdzam czy jest więcej niż jedno świeto
            Elements listaSwiat = swieto.select("ul");
            if (!listaSwiat.isEmpty()) {

                System.out.println(swieto.select("a").get(0).text());//Nazwa kraju/kategorii
                String country = swieto.select("a").get(0).text();
                countryItem.setValue(country);


                for (Element zListy : listaSwiat.get(0).select("li")) {
                    System.out.println(zListy.text());//kolejne święta z listy świąt
                    TreeItem<String> swietoItem = new TreeItem<String>(zListy.text());
                    countryItem.getChildren().add(swietoItem);
                }

                System.out.println("NEXT");//tak żebym miał pewność że przetwarzam kolejny element
            } else {
                //nie działa tak jak chce, chyba przelatuje też po dzieciach, w sensie główny selector swietaImieniny.get(0).select("li")
                System.out.println("Pojedynczy element:");
                String[] swietoString = swieto.text().split(" – ", 2);

                countryItem.setValue(swietoString[0]);
                TreeItem<String> swietoItem = new TreeItem<String>(swietoString[1]);
                countryItem.getChildren().add(swietoItem);
                System.out.println(swietoString[0]);
            }

            rootItem.getChildren().add(countryItem);
        }


        return rootItem;
    }

    public TreeItem<String> extractBornDeath() {
        TreeItem<String> rootItem = new TreeItem<String>("Urodzili się/zmarli");
        rootItem.setExpanded(true);

        //przechowuje dla danego roku listę zmarłych i narodzonych ( w formie tree item)
        TreeMap<Integer, ArrayList<TreeItem<String>>> treeItemMap = new TreeMap<Integer, ArrayList<TreeItem<String>>>();


        //get born/death and year
        extractTreeItem(WikiField.Born, treeItemMap);
        extractTreeItem(WikiField.Dead, treeItemMap);

        //z mapy wyciągam rok - tworzę z niego itema, do którego podpinam itemy z arraylisty

        for ( Integer year : treeItemMap.keySet() ){
            ArrayList<TreeItem<String>> mapValue = treeItemMap.get(year);

            TreeItem<String> yearTreeItem = new TreeItem<String>(year.toString()); //ZMIENIĆ YEAR NA STRING!!!!
            rootItem.getChildren().add(yearTreeItem);

            for ( TreeItem<String> treeItem : mapValue)
                yearTreeItem.getChildren().add(treeItem);
        }

        return rootItem;

    }

    private void extractTreeItem(WikiField whichField, TreeMap<Integer, ArrayList<TreeItem<String>>> treeItemMap) {

        //extract webcontent
        Elements extractedElements = docWiki.select(".mw-parser-output > ul");

        // > li aby pobierać tylko "najwyższe" elementy, żeby pomijać powtórzenia przy zagnieżdżonej liście
        for (Element element : extractedElements.get(whichField.fieldNumber()).select("> li")) {

            TreeItem<String> bornDeathItem = new TreeItem<String>(whichField.fieldName());
            bornDeathItem.setExpanded(true);

            Integer year;//przechowuje rok

            //sprawdzam czy jest więcej niż jedno świeto
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

                System.out.println( personString[0] + personString[1]);

                TreeItem<String> personItem = new TreeItem<String>(personString[1]);
                bornDeathItem.getChildren().add(personItem);

            }


            //tworzę mapę przechowującą itemy dla danego roku, potem ich zawartość dodaję do itemu dla danego roku, który jest dzieckiem root(ale to robi inna funkcja)

            //dołączanie przetworzonego itema
            ArrayList<TreeItem<String>> mapValue = treeItemMap.get(year);

            //jeżeli nie dodawano nic do wpisu dla tego roku
            if (mapValue == null) {
                mapValue = new ArrayList<TreeItem<String>>();
                treeItemMap.put(year, mapValue);
            }

            mapValue.add(bornDeathItem);//operuję właśnie na born deathItem - do tego dołączam jako dzieci kolejnych ludzi
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