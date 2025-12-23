package exrc_miniprojet.dao;

import exrc_miniprojet.model.Filiere;
import exrc_miniprojet.util.DBConnection;
import exrc_miniprojet.util.DaoException;
import exrc_miniprojet.util.Validator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FiliereDAO {

    public List<Filiere> findAllWithCount() {
        String sql = """
            SELECT f.id, f.code, f.nom, f.description, COUNT(e.id) AS nb
            FROM filiere f
            LEFT JOIN eleve e ON e.filiere_id = f.id
            GROUP BY f.id, f.code, f.nom, f.description
            ORDER BY f.id DESC
        """;
        List<Filiere> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Filiere f = new Filiere();
                f.setId(rs.getInt("id"));
                f.setCode(rs.getString("code"));
                f.setNom(rs.getString("nom"));
                f.setDescription(rs.getString("description"));
                f.setNbEleves(rs.getInt("nb"));
                list.add(f);
            }
            return list;

        } catch (SQLException e) {
            throw new DaoException("Erreur chargement filières.", e);
        }
    }

    public void insert(Filiere f) {
        Validator.notBlank(f.getCode(), "Code filière");
        Validator.notBlank(f.getNom(), "Nom filière");

        String sql = "INSERT INTO filiere(code, nom, description) VALUES(?,?,?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, f.getCode().trim());
            ps.setString(2, f.getNom().trim());
            ps.setString(3, f.getDescription());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) f.setId(keys.getInt(1));
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DaoException("Code filière déjà utilisé (unicité).");
        } catch (SQLException e) {
            throw new DaoException("Erreur insertion filière.", e);
        }
    }

    public void update(Filiere f) {
        Validator.notBlank(f.getCode(), "Code filière");
        Validator.notBlank(f.getNom(), "Nom filière");

        String sql = "UPDATE filiere SET code=?, nom=?, description=? WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, f.getCode().trim());
            ps.setString(2, f.getNom().trim());
            ps.setString(3, f.getDescription());
            ps.setInt(4, f.getId());

            ps.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DaoException("Code filière déjà utilisé (unicité).");
        } catch (SQLException e) {
            throw new DaoException("Erreur modification filière.", e);
        }
    }

    public boolean hasEleves(int filiereId) {
        String sql = "SELECT COUNT(*) FROM eleve WHERE filiere_id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, filiereId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur vérification filière/élèves.", e);
        }
    }

    public void delete(int filiereId) {
        // Règle métier: suppression interdite si filière contient des élèves
        if (hasEleves(filiereId)) {
            throw new DaoException("Suppression impossible : la filière contient des élèves.");
        }

        // Bonne pratique: nettoyer les associations filiere_cours avant suppression
        String deleteLinks = "DELETE FROM filiere_cours WHERE filiere_id=?";
        String deleteFiliere = "DELETE FROM filiere WHERE id=?";

        try (Connection cn = DBConnection.getConnection()) {
            cn.setAutoCommit(false);

            try (PreparedStatement ps1 = cn.prepareStatement(deleteLinks)) {
                ps1.setInt(1, filiereId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = cn.prepareStatement(deleteFiliere)) {
                ps2.setInt(1, filiereId);
                ps2.executeUpdate();
            }

            cn.commit();

        } catch (SQLException e) {
            throw new DaoException("Erreur suppression filière.", e);
        }
    }

    /**
     * Association filière ↔ cours (transaction)
     * Stratégie simple: supprimer toutes les associations puis réinsérer celles cochées.
     */
    public void setCoursForFiliere(int filiereId, List<Integer> coursIds) {
        String deleteSql = "DELETE FROM filiere_cours WHERE filiere_id=?";
        String insertSql = "INSERT INTO filiere_cours(filiere_id, cours_id) VALUES(?,?)";

        try (Connection cn = DBConnection.getConnection()) {
            boolean oldAuto = cn.getAutoCommit();
            cn.setAutoCommit(false);

            try {
                // 1) delete old
                try (PreparedStatement del = cn.prepareStatement(deleteSql)) {
                    del.setInt(1, filiereId);
                    del.executeUpdate();
                }

                // 2) insert selected (si liste vide → on garde juste la suppression)
                if (coursIds != null && !coursIds.isEmpty()) {
                    try (PreparedStatement ins = cn.prepareStatement(insertSql)) {
                        for (Integer cid : coursIds) {
                            ins.setInt(1, filiereId);
                            ins.setInt(2, cid);
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }

                cn.commit();

            } catch (SQLException ex) {
                try { cn.rollback(); } catch (SQLException ignore) {}
                throw ex;

            } finally {
                try { cn.setAutoCommit(oldAuto); } catch (SQLException ignore) {}
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur affectation cours à filière (transaction).", e);
        }
    }

    public List<Integer> getCoursIdsForFiliere(int filiereId) {
        String sql = "SELECT cours_id FROM filiere_cours WHERE filiere_id=?";
        List<Integer> ids = new ArrayList<>();

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, filiereId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
            return ids;

        } catch (SQLException e) {
            throw new DaoException("Erreur chargement cours filière.", e);
        }
    }
}
