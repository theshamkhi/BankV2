package dao;

import entity.Transaction;
import entity.TypeTransaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public void save(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transaction (date, montant, type, lieu, id_compte) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(transaction.date()));
            stmt.setDouble(2, transaction.montant());
            stmt.setString(3, transaction.type().name());
            stmt.setString(4, transaction.lieu());
            stmt.setInt(5, transaction.idCompte());
            stmt.executeUpdate();
        }
    }

    public List<Transaction> findByCompteId(int compteId) throws SQLException {
        String sql = "SELECT * FROM transaction WHERE id_compte = ? ORDER BY date DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, compteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    public List<Transaction> findAll() throws SQLException {
        String sql = "SELECT * FROM transaction ORDER BY date DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    public List<Transaction> findByType(TypeTransaction type) throws SQLException {
        String sql = "SELECT * FROM transaction WHERE type = ? ORDER BY date DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    public List<Transaction> findByDateRange(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        String sql = "SELECT * FROM transaction WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(debut));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    public List<Transaction> findByMontantGreaterThan(double montant) throws SQLException {
        String sql = "SELECT * FROM transaction WHERE montant > ? ORDER BY montant DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, montant);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    public List<Transaction> findByLieu(String lieu) throws SQLException {
        String sql = "SELECT * FROM transaction WHERE lieu = ? ORDER BY date DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, lieu);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM transaction WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
        double montant = rs.getDouble("montant");
        TypeTransaction type = TypeTransaction.valueOf(rs.getString("type"));
        String lieu = rs.getString("lieu");
        int idCompte = rs.getInt("id_compte");

        return new Transaction(date, montant, type, lieu, idCompte);
    }
}