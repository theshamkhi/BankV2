package service;

import dao.CompteDAO;
import dao.ClientDAO;
import entity.Compte;
import entity.CompteCourant;
import entity.CompteEpargne;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CompteService {
    private final CompteDAO compteDAO;
    private final ClientDAO clientDAO;

    public CompteService() {
        this.compteDAO = new CompteDAO();
        this.clientDAO = new ClientDAO();
    }

    public void creerCompteCourant(int idClient, double soldeInitial, double decouvert) throws SQLException {
        if (clientDAO.findById(idClient).isEmpty()) {
            throw new IllegalArgumentException("Client introuvable avec l'ID : " + idClient);
        }
        if (soldeInitial < 0) {
            throw new IllegalArgumentException("Le solde initial ne peut pas être négatif");
        }
        if (decouvert < 0) {
            throw new IllegalArgumentException("Le découvert ne peut pas être négatif");
        }

        String code = genererCodeCompte();
        CompteCourant compte = new CompteCourant(code, soldeInitial, idClient, decouvert);
        compteDAO.saveCompteCourant(compte);
    }

    public void creerCompteEpargne(int idClient, double soldeInitial, double tauxInteret) throws SQLException {
        if (clientDAO.findById(idClient).isEmpty()) {
            throw new IllegalArgumentException("Client introuvable avec l'ID : " + idClient);
        }
        if (soldeInitial < 0) {
            throw new IllegalArgumentException("Le solde initial ne peut pas être négatif");
        }
        if (tauxInteret < 0 || tauxInteret > 100) {
            throw new IllegalArgumentException("Le taux d'intérêt doit être entre 0 et 100");
        }

        String code = genererCodeCompte();
        CompteEpargne compte = new CompteEpargne(code, soldeInitial, idClient, tauxInteret);
        compteDAO.saveCompteEpargne(compte);
    }

    public void mettreAJourSolde(String code, double nouveauSolde) throws SQLException {
        Optional<Compte> compteOpt = compteDAO.findByCode(code);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte introuvable avec le code : " + code);
        }

        Compte compte = compteOpt.get();

        if (compte instanceof CompteCourant cc) {
            if (nouveauSolde < -cc.getDecouvert()) {
                throw new IllegalArgumentException("Le solde ne peut pas être inférieur au découvert autorisé");
            }
        } else if (nouveauSolde < 0) {
            throw new IllegalArgumentException("Le solde d'un compte épargne ne peut pas être négatif");
        }

        compteDAO.updateSolde(code, nouveauSolde);
    }

    public Optional<Compte> rechercherParCode(String code) throws SQLException {
        return compteDAO.findByCode(code);
    }

    public List<Compte> rechercherParClient(int idClient) throws SQLException {
        return compteDAO.findByClientId(idClient);
    }

    public List<Compte> listerTousLesComptes() throws SQLException {
        return compteDAO.findAll();
    }

    public Optional<Compte> trouverCompteSoldeMax() throws SQLException {
        return compteDAO.findAll().stream()
                .max(Comparator.comparingDouble(Compte::getSolde));
    }

    public Optional<Compte> trouverCompteSoldeMin() throws SQLException {
        return compteDAO.findAll().stream()
                .min(Comparator.comparingDouble(Compte::getSolde));
    }

    public void supprimerCompte(String code) throws SQLException {
        Optional<Compte> compte = compteDAO.findByCode(code);
        if (compte.isEmpty()) {
            throw new IllegalArgumentException("Compte introuvable avec le code : " + code);
        }

        compteDAO.delete(code);
    }

    public void crediter(String code, double montant) throws SQLException {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant à créditer doit être positif");
        }

        Optional<Compte> compteOpt = compteDAO.findByCode(code);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte introuvable");
        }

        Compte compte = compteOpt.get();
        double nouveauSolde = compte.getSolde() + montant;
        compteDAO.updateSolde(code, nouveauSolde);
    }

    public void debiter(String code, double montant) throws SQLException {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant à débiter doit être positif");
        }

        Optional<Compte> compteOpt = compteDAO.findByCode(code);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte introuvable");
        }

        Compte compte = compteOpt.get();
        double nouveauSolde = compte.getSolde() - montant;

        if (compte instanceof CompteCourant cc) {
            if (nouveauSolde < -cc.getDecouvert()) {
                throw new IllegalArgumentException("Opération refusée : dépassement du découvert autorisé");
            }
        } else if (nouveauSolde < 0) {
            throw new IllegalArgumentException("Opération refusée : solde insuffisant");
        }

        compteDAO.updateSolde(code, nouveauSolde);
    }

    private String genererCodeCompte() {
        return "CPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}