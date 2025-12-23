package exrc_miniprojet.controller;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import exrc_miniprojet.dao.CoursDAO;
import exrc_miniprojet.model.Cours;
import exrc_miniprojet.util.AlertUtils;
import exrc_miniprojet.util.DaoException;

public class CoursController {

    @FXML private TableView<Cours> table;
    @FXML private TableColumn<Cours, Number> colId;
    @FXML private TableColumn<Cours, String> colCode;
    @FXML private TableColumn<Cours, String> colIntitule;

    @FXML private TextField txtCode;
    @FXML private TextField txtIntitule;

    private final CoursDAO dao = new CoursDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colCode.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCode()));
        colIntitule.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getIntitule()));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                txtCode.setText(selected.getCode());
                txtIntitule.setText(selected.getIntitule());
            }
        });

        refresh();
    }

    @FXML public void refresh() {
        table.setItems(FXCollections.observableArrayList(dao.findAll()));
    }

    @FXML public void clear() {
        table.getSelectionModel().clearSelection();
        txtCode.clear();
        txtIntitule.clear();
    }

    @FXML public void add() {
        try {
            Cours c = new Cours();
            c.setCode(txtCode.getText());
            c.setIntitule(txtIntitule.getText());
            dao.insert(c);
            AlertUtils.info("Succès", "Cours ajouté.");
            refresh();
            clear();
        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }

    @FXML public void update() {
        Cours selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.error("Erreur", "Sélectionne un cours.");
            return;
        }
        try {
            selected.setCode(txtCode.getText());
            selected.setIntitule(txtIntitule.getText());
            dao.update(selected);
            AlertUtils.info("Succès", "Cours modifié.");
            refresh();
        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }

    @FXML public void delete() {
        Cours selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.error("Erreur", "Sélectionne un cours.");
            return;
        }
        if (!AlertUtils.confirm("Confirmation", "Supprimer ce cours ?")) return;

        try {
            dao.delete(selected.getId());
            AlertUtils.info("Succès", "Cours supprimé.");
            refresh();
            clear();
        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }
}
