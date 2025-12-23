package exrc_miniprojet.controller;

import exrc_miniprojet.dao.CoursDAO;
import exrc_miniprojet.dao.FiliereDAO;
import exrc_miniprojet.model.Cours;
import exrc_miniprojet.model.CoursItem;
import exrc_miniprojet.model.Filiere;
import exrc_miniprojet.util.AlertUtils;
import exrc_miniprojet.util.DaoException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FiliereController {

    @FXML private TableView<Filiere> table;
    @FXML private TableColumn<Filiere, Number> colId;
    @FXML private TableColumn<Filiere, String> colCode;
    @FXML private TableColumn<Filiere, String> colNom;
    @FXML private TableColumn<Filiere, Number> colNb;
    @FXML private TableColumn<Filiere, String> colDesc;

    @FXML private TextField txtCode;
    @FXML private TextField txtNom;
    @FXML private TextArea txtDesc;

    // ✅ IMPORTANT: ListView de CoursItem (pas Cours)
    @FXML private ListView<CoursItem> listCours;

    private final FiliereDAO filiereDAO = new FiliereDAO();
    private final CoursDAO coursDAO = new CoursDAO();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(f -> new javafx.beans.property.SimpleIntegerProperty(f.getValue().getId()));
        colCode.setCellValueFactory(f -> new javafx.beans.property.SimpleStringProperty(f.getValue().getCode()));
        colNom.setCellValueFactory(f -> new javafx.beans.property.SimpleStringProperty(f.getValue().getNom()));
        colDesc.setCellValueFactory(f -> new javafx.beans.property.SimpleStringProperty(f.getValue().getDescription()));
        colNb.setCellValueFactory(f -> new javafx.beans.property.SimpleIntegerProperty(f.getValue().getNbEleves()));

        // ✅ Affichage avec Checkbox
        listCours.setCellFactory(CheckBoxListCell.forListView(CoursItem::selectedProperty));

        // Quand on sélectionne une filière → remplir formulaire + pré-cocher cours associés
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected == null) return;

            txtCode.setText(selected.getCode());
            txtNom.setText(selected.getNom());
            txtDesc.setText(selected.getDescription());

            // cours associés en DB
            List<Integer> ids = filiereDAO.getCoursIdsForFiliere(selected.getId());
            Set<Integer> setIds = new HashSet<>(ids);

            // pré-cocher
            for (CoursItem item : listCours.getItems()) {
                item.setSelected(setIds.contains(item.getCours().getId()));
            }
        });

        refresh();
    }

    @FXML
    public void refresh() {
        table.setItems(FXCollections.observableArrayList(filiereDAO.findAllWithCount()));

        // Charger tous les cours et les transformer en CoursItem cochables
        List<Cours> allCours = coursDAO.findAll();
        List<CoursItem> items = new ArrayList<>();
        for (Cours c : allCours) {
            items.add(new CoursItem(c));
        }
        listCours.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    public void clear() {
        table.getSelectionModel().clearSelection();
        txtCode.clear();
        txtNom.clear();
        txtDesc.clear();
        for (CoursItem item : listCours.getItems()) {
            item.setSelected(false);
        }
    }

    private List<Integer> selectedCoursIds() {
        List<Integer> ids = new ArrayList<>();
        for (CoursItem item : listCours.getItems()) {
            if (item.isSelected()) {
                ids.add(item.getCours().getId());
            }
        }
        return ids;
    }

    @FXML
    public void add() {
        try {
            Filiere f = new Filiere();
            f.setCode(txtCode.getText().trim());
            f.setNom(txtNom.getText().trim());
            f.setDescription(txtDesc.getText().trim());

            filiereDAO.insert(f);

            // ✅ associer cours cochés
            filiereDAO.setCoursForFiliere(f.getId(), selectedCoursIds());

            AlertUtils.info("Succès", "Filière ajoutée.");
            refresh();
            clear();

        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }

    @FXML
    public void update() {
        Filiere selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.error("Erreur", "Sélectionne une filière.");
            return;
        }

        try {
            selected.setCode(txtCode.getText().trim());
            selected.setNom(txtNom.getText().trim());
            selected.setDescription(txtDesc.getText().trim());

            filiereDAO.update(selected);

            // ✅ re-associer cours cochés
            filiereDAO.setCoursForFiliere(selected.getId(), selectedCoursIds());

            AlertUtils.info("Succès", "Filière modifiée.");
            refresh();

        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }

    @FXML
    public void delete() {
        Filiere selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.error("Erreur", "Sélectionne une filière.");
            return;
        }
        if (!AlertUtils.confirm("Confirmation", "Supprimer cette filière ?")) return;

        try {
            filiereDAO.delete(selected.getId());
            AlertUtils.info("Succès", "Filière supprimée.");
            refresh();
            clear();

        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }
}
