package exrc_miniprojet.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Objects;

public class DashboardController {

    @FXML private StackPane contentPane;
    @FXML private Label lblHint;


    @FXML private VBox homePane;

    @FXML
    public void initialize() {

        lblHint.setText("Accueil");
        contentPane.getChildren().setAll(homePane);
    }

    @FXML
    private void showHome() {

        lblHint.setText("Accueil");
        contentPane.getChildren().setAll(homePane);
    }

    @FXML
    private void openFiliere() {
        loadView("/exrc_miniprojet/view/filiere.fxml", "Module Filières");
    }

    @FXML
    private void openCours() {
        loadView("/exrc_miniprojet/view/cours.fxml", "Module Cours");
    }

    @FXML
    private void openEleve() {
        loadView("/exrc_miniprojet/view/eleve.fxml", "Module Étudiants");
    }

    private void loadView(String fxmlPath, String hint) {
        try {
            URL url = Objects.requireNonNull(getClass().getResource(fxmlPath),
                    "FXML introuvable : " + fxmlPath);

            Node view = FXMLLoader.load(url);
            contentPane.getChildren().setAll(view);
            lblHint.setText(hint);

        } catch (Exception e) {
            lblHint.setText("Erreur chargement vue.");
            e.printStackTrace();
        }
    }
}
