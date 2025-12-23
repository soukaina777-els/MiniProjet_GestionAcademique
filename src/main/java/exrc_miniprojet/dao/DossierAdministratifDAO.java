package exrc_miniprojet.dao;

import exrc_miniprojet.model.DossierAdministratif;
import exrc_miniprojet.util.DBConnection;
import exrc_miniprojet.util.DaoException;
import exrc_miniprojet.util.Validator;

import java.sql.*;
import java.time.LocalDate;

public class DossierAdministratifDAO {

    public DossierAdministratif findByEleveId(int eleveId) {
        String sql = "SELECT id, numero_inscription, date_creation, eleve_id FROM dossier_administratif WHERE eleve_id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, eleveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                DossierAdministratif d = new DossierAdministratif();
                d.setId(rs.getInt("id"));
                d.setNumeroInscription(rs.getString("numero_inscription"));
                d.setDateCreation(rs.getDate("date_creation").toLocalDate());
                d.setEleveId(rs.getInt("eleve_id"));
                return d;
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur chargement dossier.", e);
        }
    }

    public void insert(DossierAdministratif d) {
        Validator.notBlank(d.getNumeroInscription(), "Numéro d'inscription");
        if (d.getDateCreation() == null) d.setDateCreation(LocalDate.now());

        if (findByEleveId(d.getEleveId()) != null) {
            throw new DaoException("Cet élève possède déjà un dossier administratif.");
        }

        String sql = "INSERT INTO dossier_administratif(numero_inscription, date_creation, eleve_id) VALUES(?,?,?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getNumeroInscription().trim());
            ps.setDate(2, Date.valueOf(d.getDateCreation()));
            ps.setInt(3, d.getEleveId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) d.setId(keys.getInt(1));
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DaoException("Numéro d'inscription déjà utilisé (unicité).");
        } catch (SQLException e) {
            throw new DaoException("Erreur insertion dossier.", e);
        }
    }

    public void update(DossierAdministratif d) {
        Validator.notBlank(d.getNumeroInscription(), "Numéro d'inscription");
        if (d.getDateCreation() == null) d.setDateCreation(LocalDate.now());

        String sql = "UPDATE dossier_administratif SET numero_inscription=?, date_creation=? WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, d.getNumeroInscription().trim());
            ps.setDate(2, Date.valueOf(d.getDateCreation()));
            ps.setInt(3, d.getId());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DaoException("Numéro d'inscription déjà utilisé (unicité).");
        } catch (SQLException e) {
            throw new DaoException("Erreur modification dossier.", e);
        }
    }
}
