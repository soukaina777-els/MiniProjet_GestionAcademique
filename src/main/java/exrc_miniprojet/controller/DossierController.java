package exrc_miniprojet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import exrc_miniprojet.dao.DossierAdministratifDAO;
import exrc_miniprojet.model.DossierAdministratif;
import exrc_miniprojet.model.Eleve;
import exrc_miniprojet.util.AlertUtils;
import exrc_miniprojet.util.DaoException;

import java.time.LocalDate;

public class DossierController {

    @FXML private Label lblEleve;
    @FXML private TextField txtNum;
    @FXML private DatePicker dpDate;
    @FXML private Label lblState;

    private final DossierAdministratifDAO dao = new DossierAdministratifDAO();

    private Eleve eleve;
    private DossierAdministratif current;

    public void setEleve(Eleve eleve) {
        this.eleve = eleve;
        lblEleve.setText("Dossier - " + eleve.getNomComplet() + " (" + eleve.getMatricule() + ")");

        current = dao.findByEleveId(eleve.getId());
        if (current == null) {
            lblState.setText("Aucun dossier trouvé. Vous pouvez le créer.");
            dpDate.setValue(LocalDate.now());
        } else {
            lblState.setText("Dossier existant (modifiable).");
            txtNum.setText(current.getNumeroInscription());
            dpDate.setValue(current.getDateCreation());
        }
    }

    @FXML
    public void create() {
        if (eleve == null) return;
        try {
            DossierAdministratif d = new DossierAdministratif();
            d.setEleveId(eleve.getId());
            d.setNumeroInscription(txtNum.getText());
            d.setDateCreation(dpDate.getValue());
            dao.insert(d);
            current = d;
            AlertUtils.info("Succès", "Dossier créé.");
            lblState.setText("Dossier créé.");
        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }

    @FXML
    public void update() {
        if (current == null) {
            AlertUtils.error("Erreur", "Aucun dossier à modifier. Cliquez sur Créer.");
            return;
        }
        try {
            current.setNumeroInscription(txtNum.getText());
            current.setDateCreation(dpDate.getValue());
            dao.update(current);
            AlertUtils.info("Succès", "Dossier modifié.");
            lblState.setText("Dossier modifié.");
        } catch (DaoException ex) {
            AlertUtils.error("Erreur", ex.getMessage());
        }
    }

    @FXML
    public void close() {
        Stage st = (Stage) txtNum.getScene().getWindow();
        st.close();
    }
}
