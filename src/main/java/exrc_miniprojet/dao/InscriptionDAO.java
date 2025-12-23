package exrc_miniprojet.dao;



import exrc_miniprojet.model.StatutEleve;
import exrc_miniprojet.util.DBConnection;
import exrc_miniprojet.util.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class InscriptionDAO {
    private final EleveDAO eleveDAO = new EleveDAO();

    private boolean coursAppartientAFiliere(Connection cn, int filiereId, int coursId) throws SQLException {
        String sql = "SELECT 1 FROM filiere_cours WHERE filiere_id=? AND cours_id=? LIMIT 1";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, filiereId);
            ps.setInt(2, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void inscrireEleveAuxCours(int eleveId, List<Integer> coursIds) {
        if (coursIds == null || coursIds.isEmpty()) {
            throw new DaoException("Sélectionne au moins un cours.");
        }

        // Bonus: élève suspendu => pas d'inscription
        StatutEleve statut = eleveDAO.getStatut(eleveId);
        if (statut == StatutEleve.SUSPENDU) {
            throw new DaoException("Inscription impossible : élève SUSPENDU.");
        }

        int filiereId = eleveDAO.getFiliereId(eleveId);

        String deleteSql = "DELETE FROM eleve_cours WHERE eleve_id=?";
        String insertSql = "INSERT INTO eleve_cours(eleve_id, cours_id) VALUES(?,?)";

        try (Connection cn = DBConnection.getConnection()) {
            cn.setAutoCommit(false);

            // 1) on reset l'inscription
            try (PreparedStatement del = cn.prepareStatement(deleteSql)) {
                del.setInt(1, eleveId);
                del.executeUpdate();
            }

            // 2) règles métier: cours doit appartenir à la filière
            for (Integer cid : coursIds) {
                if (!coursAppartientAFiliere(cn, filiereId, cid)) {
                    cn.rollback();
                    throw new DaoException("Cours invalide : l'élève ne peut suivre que les cours de sa filière.");
                }
            }

            // 3) insertion batch
            try (PreparedStatement ins = cn.prepareStatement(insertSql)) {
                for (Integer cid : coursIds) {
                    ins.setInt(1, eleveId);
                    ins.setInt(2, cid);
                    ins.addBatch();
                }
                ins.executeBatch();
            }

            cn.commit();
        } catch (DaoException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new DaoException("Erreur inscription (transaction).", ex);
        }
    }

    public List<Integer> getCoursIdsByEleve(int eleveId) {
        String sql = "SELECT cours_id FROM eleve_cours WHERE eleve_id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, eleveId);
            try (ResultSet rs = ps.executeQuery()) {
                java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
                while (rs.next()) ids.add(rs.getInt(1));
                return ids;
            }
        } catch (SQLException ex) {
            throw new DaoException("Erreur chargement cours élève.", ex);
        }
    }
}
