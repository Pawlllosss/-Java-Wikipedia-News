package mainwindow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/MainWindow.fxml"));
        GridPane borderPaneMainWindow = loader.load();
        Scene sceneMainWindow = new Scene(borderPaneMainWindow);
        primaryStage.setScene(sceneMainWindow);
        primaryStage.setTitle("Wikipedia News");
        primaryStage.show();
    }
}
