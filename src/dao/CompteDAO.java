package dao;

import entity.Compte;
import entity.CompteCourant;
import entity.CompteEpargne;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompteDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public void saveCompteCourant(CompteCourant compte) throws SQLException {
        String sql = "INSERT INTO compte (code, solde, id_client, type_compte, decouvert) VALUES (?, ?, ?, 'COURANT', ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, compte.getCode());
            stmt.setDouble(2, compte.getSolde());
            stmt.setInt(3, compte.getIdClient());
            stmt.setDouble(4, compte.getDecouvert());
            stmt.executeUpdate();
        }
    }

    public void saveCompteEpargne(CompteEpargne compte) throws SQLException {
        String sql = "INSERT INTO compte (code, solde, id_client, type_compte, taux_interet) VALUES (?, ?, ?, 'EPARGNE', ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, compte.getCode());
            stmt.setDouble(2, compte.getSolde());
            stmt.setInt(3, compte.getIdClient());
            stmt.setDouble(4, compte.getTauxInteret());
            stmt.executeUpdate();
        }
    }

    public Optional<Compte> findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM compte WHERE code = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCompte(rs));
            }
            return Optional.empty();
        }
    }

    public List<Compte> findByClientId(int clientId) throws SQLException {
        String sql = "SELECT * FROM compte WHERE id_client = ?";
        List<Compte> comptes = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comptes.add(mapResultSetToCompte(rs));
            }
        }
        return comptes;
    }

    public List<Compte> findAll() throws SQLException {
        String sql = "SELECT * FROM compte";
        List<Compte> comptes = new ArrayList<>();

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                comptes.add(mapResultSetToCompte(rs));
            }
        }
        return comptes;
    }

    public void updateSolde(String code, double nouveauSolde) throws SQLException {
        String sql = "UPDATE compte SET solde = ? WHERE code = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, nouveauSolde);
            stmt.setString(2, code);
            stmt.executeUpdate();
        }
    }

    public void delete(String code) throws SQLException {
        String sql = "DELETE FROM compte WHERE code = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, code);
            stmt.executeUpdate();
        }
    }

    private Compte mapResultSetToCompte(ResultSet rs) throws SQLException {
        String code = rs.getString("code");
        double solde = rs.getDouble("solde");
        int idClient = rs.getInt("id_client");
        String typeCompte = rs.getString("type_compte");

        if ("COURANT".equals(typeCompte)) {
            double decouvert = rs.getDouble("decouvert");
            return new CompteCourant(code, solde, idClient, decouvert);
        } else if ("EPARGNE".equals(typeCompte)) {
            double tauxInteret = rs.getDouble("taux_interet");
            return new CompteEpargne(code, solde, idClient, tauxInteret);
        }

        throw new SQLException("Type de compte inconnu : " + typeCompte);
    }
}