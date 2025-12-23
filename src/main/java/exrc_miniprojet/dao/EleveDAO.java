package exrc_miniprojet.dao;

import exrc_miniprojet.model.Eleve;
import exrc_miniprojet.model.StatutEleve;
import exrc_miniprojet.util.DBConnection;
import exrc_miniprojet.util.DaoException;
import exrc_miniprojet.util.Validator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EleveDAO {

    public List<Eleve> findAllWithFiliere() {
        String sql = """
            SELECT e.id, e.matricule, e.nom, e.prenom, e.email, e.statut,
                   f.id AS filiere_id, f.code AS filiere_code, f.nom AS filiere_nom
            FROM eleve e
            JOIN filiere f ON f.id = e.filiere_id
            ORDER BY e.id DESC
        """;
        List<Eleve> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Eleve e = new Eleve();
                e.setId(rs.getInt("id"));
                e.setMatricule(rs.getString("matricule"));
                e.setNom(rs.getString("nom"));
                e.setPrenom(rs.getString("prenom"));
                e.setEmail(rs.getString("email"));
                e.setStatut(StatutEleve.valueOf(rs.getString("statut")));

                e.setFiliereId(rs.getInt("filiere_id"));
                e.setFiliereCode(rs.getString("filiere_code"));
                e.setFiliereNom(rs.getString("filiere_nom"));
                list.add(e);
            }
            return list;
        } catch (SQLException ex) {
            throw new DaoException("Erreur chargement élèves (JOIN).", ex);
        }
    }

    public void insert(Eleve e) {
        Validator.notBlank(e.getMatricule(), "Matricule");
        Validator.notBlank(e.getNom(), "Nom");
        Validator.notBlank(e.getPrenom(), "Prénom");
        Validator.email(e.getEmail());

        String sql = "INSERT INTO eleve(matricule, nom, prenom, email, statut, filiere_id) VALUES(?,?,?,?,?,?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getMatricule().trim());
            ps.setString(2, e.getNom().trim());
            ps.setString(3, e.getPrenom().trim());
            ps.setString(4, e.getEmail().trim());
            ps.setString(5, e.getStatut().name());
            ps.setInt(6, e.getFiliereId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.setId(keys.getInt(1));
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new DaoException("Matricule déjà utilisé (unicité).");
        } catch (SQLException ex) {
            throw new DaoException("Erreur insertion élève.", ex);
        }
    }

    public void update(Eleve e) {
        Validator.notBlank(e.getMatricule(), "Matricule");
        Validator.notBlank(e.getNom(), "Nom");
        Validator.notBlank(e.getPrenom(), "Prénom");
        Validator.email(e.getEmail());

        String sql = "UPDATE eleve SET matricule=?, nom=?, prenom=?, email=?, statut=?, filiere_id=? WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, e.getMatricule().trim());
            ps.setString(2, e.getNom().trim());
            ps.setString(3, e.getPrenom().trim());
            ps.setString(4, e.getEmail().trim());
            ps.setString(5, e.getStatut().name());
            ps.setInt(6, e.getFiliereId());
            ps.setInt(7, e.getId());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new DaoException("Matricule déjà utilisé (unicité).");
        } catch (SQLException ex) {
            throw new DaoException("Erreur modification élève.", ex);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM eleve WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DaoException("Erreur suppression élève.", ex);
        }
    }

    public StatutEleve getStatut(int eleveId) {
        String sql = "SELECT statut FROM eleve WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, eleveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new DaoException("Élève introuvable.");
                return StatutEleve.valueOf(rs.getString(1));
            }
        } catch (SQLException ex) {
            throw new DaoException("Erreur statut élève.", ex);
        }
    }

    public int getFiliereId(int eleveId) {
        String sql = "SELECT filiere_id FROM eleve WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, eleveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new DaoException("Élève introuvable.");
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw new DaoException("Erreur filière élève.", ex);
        }
    }
}
