package service;

import dao.ClientDAO;
import dao.CompteDAO;
import entity.Client;
import entity.Compte;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ClientService {
    private final ClientDAO clientDAO;
    private final CompteDAO compteDAO;

    public ClientService() {
        this.clientDAO = new ClientDAO();
        this.compteDAO = new CompteDAO();
    }

    public int ajouterClient(String nom, String email) throws SQLException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email invalide");
        }

        Client client = new Client(nom, email);
        return clientDAO.save(client);
    }

    public void modifierClient(int id, String nom, String email) throws SQLException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email invalide");
        }

        Optional<Client> clientExist = clientDAO.findById(id);
        if (clientExist.isEmpty()) {
            throw new IllegalArgumentException("Client introuvable avec l'ID : " + id);
        }

        Client client = new Client(nom, email);
        clientDAO.update(id, client);
    }

    public void supprimerClient(int id) throws SQLException {
        Optional<Client> client = clientDAO.findById(id);
        if (client.isEmpty()) {
            throw new IllegalArgumentException("Client introuvable avec l'ID : " + id);
        }

        List<Compte> comptes = compteDAO.findByClientId(id);
        if (!comptes.isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer le client. Il possède encore des comptes.");
        }

        clientDAO.delete(id);
    }

    public Optional<Client> rechercherParId(int id) throws SQLException {
        return clientDAO.findById(id);
    }

    public List<Client> rechercherParNom(String nom) throws SQLException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de recherche ne peut pas être vide");
        }
        return clientDAO.findByNom(nom);
    }

    public List<Client> listerTousLesClients() throws SQLException {
        return clientDAO.findAll();
    }

    public double calculerSoldeTotal(int clientId) throws SQLException {
        List<Compte> comptes = compteDAO.findByClientId(clientId);
        return comptes.stream()
                .mapToDouble(Compte::getSolde)
                .sum();
    }

    public int compterComptes(int clientId) throws SQLException {
        return compteDAO.findByClientId(clientId).size();
    }

    public String genererRapportClient(int clientId) throws SQLException {
        Optional<Client> clientOpt = clientDAO.findById(clientId);
        if (clientOpt.isEmpty()) {
            return "Client introuvable";
        }

        Client client = clientOpt.get();
        List<Compte> comptes = compteDAO.findByClientId(clientId);
        double soldeTotal = calculerSoldeTotal(clientId);

        StringBuilder rapport = new StringBuilder();
        rapport.append("=== Rapport Client ===\n");
        rapport.append("Nom: ").append(client.nom()).append("\n");
        rapport.append("Email: ").append(client.email()).append("\n");
        rapport.append("Nombre de comptes: ").append(comptes.size()).append("\n");
        rapport.append("Solde total: ").append(String.format("%.2f", soldeTotal)).append(" MAD\n");
        rapport.append("\nDétail des comptes:\n");

        comptes.forEach(compte -> {
            rapport.append("  - Code: ").append(compte.getCode())
                    .append(", Solde: ").append(String.format("%.2f", compte.getSolde()))
                    .append(" MAD\n");
        });

        return rapport.toString();
    }
}