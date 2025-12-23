package exrc_miniprojet.controller;

import exrc_miniprojet.dao.CoursDAO;
import exrc_miniprojet.dao.EleveDAO;
import exrc_miniprojet.dao.FiliereDAO;
import exrc_miniprojet.dao.InscriptionDAO;
import exrc_miniprojet.model.Cours;
import exrc_miniprojet.model.Eleve;
import exrc_miniprojet.model.Filiere;
import exrc_miniprojet.model.StatutEleve;
import exrc_miniprojet.util.AlertUtils;
import exrc_miniprojet.util.DaoException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EleveController {

    @FXML private TableView<Eleve> table;
    @FXML private TableColumn<Eleve, Number> colId;
    @FXML private TableColumn<Eleve, String> colMat;
    @FXML private TableColumn<Eleve, String> colNom;
    @FXML private TableColumn<Eleve, String> colPrenom;
    @FXML private TableColumn<Eleve, String> colEmail;
    @FXML private TableColumn<Eleve, String> colStatut;
    @FXML private TableColumn<Eleve, String> colFiliere;

    @FXML private TextField txtMat;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<StatutEleve> cbStatut;
    @FXML private ComboBox<Filiere> cbFiliere;

    @FXML private ListView<Cours> listCoursFiliere;

    // ✅ bouton "Enregistrer inscription" (ajoute fx:id dans FXML)
    @FXML private Button btnSaveInscription;

    private final EleveDAO eleveDAO = new EleveDAO();
    private final FiliereDAO filiereDAO = new FiliereDAO();
    private final CoursDAO coursDAO = new CoursDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    /* ===================== RULE PDF ===================== */

    private boolean isSuspended() {
        return cbStatut.getValue() == StatutEleve.SUSPENDU;
    }

    private void applyStatutRule() {
        boolean suspended = isSuspended();

        // ✅ SUSPENDU -> pas d'accès à l'inscription
        listCoursFiliere.setDisable(suspended);

        if (btnSaveInscription != null) {
            btnSaveInscription.setDisable(suspended);
        }

        if (suspended) {
            listCoursFiliere.getSelectionModel().clearSelection();
        }
    }

    /* ===================== INIT ===================== */

    @FXML
    public void initialize() {

        colId.setCellValueFactory(e -> new javafx.beans.property.SimpleIntegerProperty(e.getValue().getId()));
        colMat.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getMatricule()));
        colNom.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getNom()));
        colPrenom.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getPrenom()));
        colEmail.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getEmail()));
        colStatut.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getStatut().name()));
        colFiliere.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(
                e.getValue().getFiliereCode() + " - " + e.getValue().getFiliereNom()
        ));

        cbStatut.setItems(FXCollections.observableArrayList(StatutEleve.values()));
        cbStatut.setValue(StatutEleve.ACTIF);

        listCoursFiliere.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // ✅ Quand le statut change -> appliquer règle + éventuellement vider cours
        cbStatut.valueProperty().addListener((obs, oldV, newV) -> {
            applyStatutRule();
            // si suspendu => vider la liste (optionnel, mais propre)
            if (newV == StatutEleve.SUSPENDU) {
                listCoursFiliere.setItems(FXCollections.observableArrayList());
            } else {
                loadCoursForSelectedFiliere();
            }
        });

        // Quand filière change -> charger cours seulement si ACTIF
        cbFiliere.setOnAction(ev -> loadCoursForSelectedFiliere());

        // Sélection élève dans table -> remplir formulaire + règle statut
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected == null) return;

            txtMat.setText(selected.getMatricule());
            txtNom.setText(selected.getNom());
            txtPrenom.setText(selected.getPrenom());
            txtEmail.setText(selected.getEmail());
            cbStatut.setValue(selected.getStatut());

            // sélectionner filière
            for (Filiere f : cbFiliere.getItems()) {
                if (f.getId() == selected.getFiliereId()) {
                    cbFiliere.setValue(f);
                    break;
                }
            }

            applyStatutRule();

            // Charger cours de filière (seulement si actif)
            loadCoursForSelectedFiliere();

            // ✅ si suspendu => pas de pré-sélection
            if (selected.getStatut() == StatutEleve.SUSPENDU) {
                listCoursFiliere.getSelectionModel().clearSelection();
                return;
            }

            // pré-sélectionner les cours déjà inscrits
            List<Integer> eleveCours = inscriptionDAO.getCoursIdsByEleve(selected.getId());
            listCoursFiliere.getSelectionModel().clearSelection();
            for (int i = 0; i < listCoursFiliere.getItems().size(); i++) {
                if (eleveCours.contains(listCoursFiliere.getItems().get(i).getId())) {
                    listCoursFiliere.getSelectionModel().select(i);
                }
            }
        });

        refresh();
        clear();
        applyStatutRule();
    }

    /* ===================== LOAD COURSES ===================== */

    private void loadCoursForSelectedFiliere() {
        // ✅ règle PDF : suspendu => aucun cours
        if (isSuspended()) {
            listCoursFiliere.setItems(FXCollections.observableArrayList());
            applyStatutRule();
            return;
        }

        Filiere f = cbFiliere.getValue();
        if (f == null) {
            listCoursFiliere.setItems(FXCollections.observableArrayList());
            return;
        }

        listCoursFiliere.setItems(FXCollections.observableArrayList(
                coursDAO.findCoursByFiliere(f.getId())
        ));
    }

    /* ===================== CRUD ÉLÈVES ===================== */

    @FXML
    public void refresh() {
        table.setItems(FXCollections.observableArrayList(eleveDAO.findAllWithFiliere()));
        cbFiliere.setItems(FXCollections.observableArrayList(filiereDAO.findAllWithCount()));
    }

    @FXML
    public void clear() {
        table.getSelectionModel().clearSelection();
        txtMat.clear();
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        cbStatut.setValue(StatutEleve.ACTIF);
        cbFiliere.setValue(null);
        listCoursFiliere.setItems(FXCollections.observableArrayList());
        applyStatutRule();
    }

    @FXML
    public void add() {
        try {
            Filiere f = cbFiliere.getValue();
            if (f == null) throw new DaoException("Sélectionne une filière.");

            Eleve e = new Eleve();
            e.setMatricule(txtMat.getText());
            e.setNom(txtNom.getText());
            e.setPrenom(txtPrenom.getText());
            e.setEmail(txtEmail.getText());
            e.setStatut(cbStatut.getValue() == null ? StatutEleve.ACTIF : cbStatut.getValue());
            e.setFiliereId(f.getId());

            eleveDAO.insert(e);

            // ✅ si ajouté en SUSPENDU => pas d'inscriptions (sécurité)
            if (e.getStatut() == StatutEleve.SUSPENDU) {
                inscriptionDAO.inscrireEleveAuxCours(e.getId(), List.of());
            }

            AlertUtils.info("Succès", "Élève ajouté.");
            refresh();
            clear();

        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }

    @FXML
    public void update() {
        Eleve selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.error("Erreur", "Sélectionne un élève.");
            return;
        }

        try {
            Filiere f = cbFiliere.getValue();
            if (f == null) throw new DaoException("Sélectionne une filière.");

            selected.setMatricule(txtMat.getText());
            selected.setNom(txtNom.getText());
            selected.setPrenom(txtPrenom.getText());
            selected.setEmail(txtEmail.getText());
            selected.setStatut(cbStatut.getValue() == null ? StatutEleve.ACTIF : cbStatut.getValue());
            selected.setFiliereId(f.getId());

            eleveDAO.update(selected);

            // ✅ si devient SUSPENDU => annuler toutes les inscriptions
            if (selected.getStatut() == StatutEleve.SUSPENDU) {
                inscriptionDAO.inscrireEleveAuxCours(selected.getId(), List.of());
                listCoursFiliere.getSelectionModel().clearSelection();
                listCoursFiliere.setItems(FXCollections.observableArrayList());
                AlertUtils.info("Info", "Statut SUSPENDU : inscriptions aux cours annulées.");
            }

            applyStatutRule();

            AlertUtils.info("Succès", "Élève modifié.");
            refresh();

        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }

    @FXML
    public void delete() {
        Eleve selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.error("Erreur", "Sélectionne un élève.");
            return;
        }
        if (!AlertUtils.confirm("Confirmation", "Supprimer cet élève ?")) return;

        try {
            eleveDAO.delete(selected.getId());
            AlertUtils.info("Succès", "Élève supprimé.");
            refresh();
            clear();

        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }

    /* ===================== INSCRIPTION COURS ===================== */

    @FXML
    public void saveInscription() {
        Eleve selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.error("Erreur", "Sélectionne un élève.");
            return;
        }

        // ✅ règle PDF : suspendu => interdit
        if (selected.getStatut() == StatutEleve.SUSPENDU || isSuspended()) {
            AlertUtils.error("Règle métier", "Un élève SUSPENDU ne peut pas être inscrit à un cours.");
            return;
        }

        List<Integer> ids = new ArrayList<>();
        for (Cours c : listCoursFiliere.getSelectionModel().getSelectedItems()) {
            ids.add(c.getId());
        }

        try {
            inscriptionDAO.inscrireEleveAuxCours(selected.getId(), ids);
            AlertUtils.info("Succès", "Inscription enregistrée (transaction).");
        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }

    /* ===================== DOSSIER ADMIN ===================== */

    @FXML
    public void openDossier() {
        Eleve selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.error("Erreur", "Sélectionne un élève.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/exrc_miniprojet/view/dossier.fxml"));
            Scene scene = new Scene(loader.load(), 420, 280);
            scene.getStylesheets().add(getClass().getResource("/exrc_miniprojet/css/app.css").toExternalForm());

            DossierController ctrl = loader.getController();
            ctrl.setEleve(selected);

            Stage st = new Stage();
            st.setTitle("Dossier administratif");
            st.setScene(scene);
            st.initModality(Modality.APPLICATION_MODAL);
            st.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.error("Erreur", "Impossible d'ouvrir la fenêtre dossier.");
        }
    }
}
