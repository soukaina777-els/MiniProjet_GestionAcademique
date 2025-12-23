package exrc_miniprojet.dao;

import exrc_miniprojet.model.Cours;
import exrc_miniprojet.util.DBConnection;
import exrc_miniprojet.util.DaoException;
import exrc_miniprojet.util.Validator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {

    public List<Cours> findAll() {
        String sql = "SELECT id, code, intitule FROM cours ORDER BY id DESC";
        List<Cours> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Cours(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("intitule")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Erreur chargement cours.", e);
        }
    }

    public void insert(Cours c) {
        Validator.notBlank(c.getCode(), "Code cours");
        Validator.notBlank(c.getIntitule(), "Intitulé");

        String sql = "INSERT INTO cours(code, intitule) VALUES(?,?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCode().trim());
            ps.setString(2, c.getIntitule().trim());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getInt(1));
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DaoException("Code cours déjà utilisé (unicité).");
        } catch (SQLException e) {
            throw new DaoException("Erreur insertion cours.", e);
        }
    }

    public void update(Cours c) {
        Validator.notBlank(c.getCode(), "Code cours");
        Validator.notBlank(c.getIntitule(), "Intitulé");

        String sql = "UPDATE cours SET code=?, intitule=? WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getCode().trim());
            ps.setString(2, c.getIntitule().trim());
            ps.setInt(3, c.getId());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DaoException("Code cours déjà utilisé (unicité).");
        } catch (SQLException e) {
            throw new DaoException("Erreur modification cours.", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM cours WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur suppression cours.", e);
        }
    }

    public List<Cours> findCoursByFiliere(int filiereId) {
        String sql = """
            SELECT c.id, c.code, c.intitule
            FROM filiere_cours fc
            JOIN cours c ON c.id = fc.cours_id
            WHERE fc.filiere_id=?
            ORDER BY c.code
        """;
        List<Cours> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, filiereId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Cours(rs.getInt("id"), rs.getString("code"), rs.getString("intitule")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Erreur cours par filière (JOIN).", e);
        }
    }
}
