package exrc_miniprojet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        var url = Objects.requireNonNull(
                MainApp.class.getResource("/exrc_miniprojet/view/dashboard.fxml"),
                "FXML introuvable: /exrc/miniprojet/view/dashboard.fxml"
        );

        var root = FXMLLoader.load(url);

        Scene scene = new Scene((Parent) root);
        scene.getStylesheets().add(Objects.requireNonNull(
                MainApp.class.getResource("/exrc_miniprojet/css/app.css")
        ).toExternalForm());

        stage.setTitle("Gestion Académique");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
