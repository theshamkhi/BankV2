package dao;

import entity.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public int save(Client client) throws SQLException {
        String sql = "INSERT INTO client (nom, email) VALUES (?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, client.nom());
            stmt.setString(2, client.email());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Échec de la création du client, aucun ID généré");
        }
    }

    public Optional<Client> findById(int id) throws SQLException {
        String sql = "SELECT nom, email FROM client WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(new Client(
                        rs.getString("nom"),
                        rs.getString("email")
                ));
            }
            return Optional.empty();
        }
    }

    public List<Client> findAll() throws SQLException {
        String sql = "SELECT nom, email FROM client";
        List<Client> clients = new ArrayList<>();

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clients.add(new Client(
                        rs.getString("nom"),
                        rs.getString("email")
                ));
            }
        }
        return clients;
    }

    public void update(int id, Client client) throws SQLException {
        String sql = "UPDATE client SET nom = ?, email = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, client.nom());
            stmt.setString(2, client.email());
            stmt.setInt(3, id);
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM client WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Client> findByNom(String nom) throws SQLException {
        String sql = "SELECT nom, email FROM client WHERE nom LIKE ?";
        List<Client> clients = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + nom + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                clients.add(new Client(
                        rs.getString("nom"),
                        rs.getString("email")
                ));
            }
        }
        return clients;
    }
}