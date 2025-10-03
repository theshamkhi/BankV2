package service;

import dao.ClientDAO;
import dao.CompteDAO;
import dao.TransactionDAO;
import entity.Client;
import entity.Compte;
import entity.Transaction;
import entity.TypeTransaction;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RapportService {
    private final ClientDAO clientDAO;
    private final CompteDAO compteDAO;
    private final TransactionDAO transactionDAO;
    private final TransactionService transactionService;

    public RapportService() {
        this.clientDAO = new ClientDAO();
        this.compteDAO = new CompteDAO();
        this.transactionDAO = new TransactionDAO();
        this.transactionService = new TransactionService();
    }

    public String genererTop5ClientsParSolde() throws SQLException {
        List<Client> clients = clientDAO.findAll();
        Map<String, Double> clientsSoldes = new HashMap<>();

        for (Client client : clients) {
            List<Compte> comptes = compteDAO.findByClientId(getClientId(client));
            double soldeTotal = comptes.stream()
                    .mapToDouble(Compte::getSolde)
                    .sum();
            clientsSoldes.put(client.nom(), soldeTotal);
        }

        List<Map.Entry<String, Double>> top5 = clientsSoldes.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        StringBuilder rapport = new StringBuilder();
        rapport.append("\n╔═══════════════════════════════════════════╗\n");
        rapport.append("║   TOP 5 DES CLIENTS PAR SOLDE TOTAL      ║\n");
        rapport.append("╚═══════════════════════════════════════════╝\n\n");

        int rang = 1;
        for (Map.Entry<String, Double> entry : top5) {
            rapport.append(String.format("%d. %-30s : %,.2f MAD\n",
                    rang++, entry.getKey(), entry.getValue()));
        }

        return rapport.toString();
    }

    public String genererRapportMensuel(int mois, int annee) throws SQLException {
        LocalDateTime debut = LocalDateTime.of(annee, mois, 1, 0, 0);
        LocalDateTime fin = debut.plusMonths(1).minusSeconds(1);

        List<Transaction> transactions = transactionDAO.findByDateRange(debut, fin);

        Map<TypeTransaction, Long> comptesParType = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::type, Collectors.counting()));

        Map<TypeTransaction, Double> volumesParType = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::type,
                        Collectors.summingDouble(Transaction::montant)
                ));

        StringBuilder rapport = new StringBuilder();
        rapport.append("\n╔═══════════════════════════════════════════╗\n");
        rapport.append(String.format("║   RAPPORT MENSUEL - %02d/%d              ║\n", mois, annee));
        rapport.append("╚═══════════════════════════════════════════╝\n\n");

        rapport.append("Nombre total de transactions : ").append(transactions.size()).append("\n\n");
        rapport.append("Détails par type :\n");
        rapport.append("─────────────────────────────────────────────\n");

        for (TypeTransaction type : TypeTransaction.values()) {
            long count = comptesParType.getOrDefault(type, 0L);
            double volume = volumesParType.getOrDefault(type, 0.0);
            rapport.append(String.format("%-15s : %3d transactions | Volume: %,.2f MAD\n",
                    type, count, volume));
        }

        double volumeTotal = volumesParType.values().stream().mapToDouble(Double::doubleValue).sum();
        rapport.append("─────────────────────────────────────────────\n");
        rapport.append(String.format("Volume total   : %,.2f MAD\n", volumeTotal));

        return rapport.toString();
    }

    public String detecterTransactionsSuspectes(double seuil) throws SQLException {
        List<Transaction> suspectes = transactionService.detecterTransactionsSuspectes(seuil, "Maroc");

        StringBuilder rapport = new StringBuilder();
        rapport.append("\n╔═══════════════════════════════════════════╗\n");
        rapport.append("║   DÉTECTION DES TRANSACTIONS SUSPECTES   ║\n");
        rapport.append("╚═══════════════════════════════════════════╝\n\n");

        if (suspectes.isEmpty()) {
            rapport.append("Aucune transaction suspecte détectée.\n");
        } else {
            rapport.append(String.format("Nombre de transactions suspectes : %d\n\n", suspectes.size()));

            for (Transaction t : suspectes) {
                rapport.append(String.format("• Date: %s | Montant: %,.2f MAD | Type: %s | Lieu: %s\n",
                        t.date().toString(), t.montant(), t.type(), t.lieu()));
            }
        }

        return rapport.toString();
    }

    public String identifierComptesInactifs(int joursInactivite) throws SQLException {
        LocalDateTime seuil = LocalDateTime.now().minusDays(joursInactivite);
        List<Compte> tousLesComptes = compteDAO.findAll();
        List<Compte> comptesInactifs = new ArrayList<>();

        for (Compte compte : tousLesComptes) {
            List<Transaction> transactions = transactionDAO.findByCompteId(compte.getIdClient());

            if (transactions.isEmpty()) {
                comptesInactifs.add(compte);
                continue;
            }

            Optional<LocalDateTime> derniereTransaction = transactions.stream()
                    .map(Transaction::date)
                    .max(LocalDateTime::compareTo);

            if (derniereTransaction.isPresent() && derniereTransaction.get().isBefore(seuil)) {
                comptesInactifs.add(compte);
            }
        }

        StringBuilder rapport = new StringBuilder();
        rapport.append("\n╔═══════════════════════════════════════════╗\n");
        rapport.append("║   COMPTES INACTIFS                       ║\n");
        rapport.append("╚═══════════════════════════════════════════╝\n\n");

        rapport.append(String.format("Seuil d'inactivité : %d jours\n", joursInactivite));
        rapport.append(String.format("Nombre de comptes inactifs : %d\n\n", comptesInactifs.size()));

        if (!comptesInactifs.isEmpty()) {
            for (Compte compte : comptesInactifs) {
                rapport.append(String.format("• Code: %s | Solde: %,.2f MAD\n",
                        compte.getCode(), compte.getSolde()));
            }
        }

        return rapport.toString();
    }

    public String genererRapportComplet() throws SQLException {
        StringBuilder rapport = new StringBuilder();
        rapport.append("\n╔═══════════════════════════════════════════════════════╗\n");
        rapport.append("║          RAPPORT COMPLET DU SYSTÈME BANCAIRE         ║\n");
        rapport.append("╚═══════════════════════════════════════════════════════╝\n");

        int nbClients = clientDAO.findAll().size();
        int nbComptes = compteDAO.findAll().size();
        int nbTransactions = transactionDAO.findAll().size();

        rapport.append("\n📊 STATISTIQUES GÉNÉRALES\n");
        rapport.append("─────────────────────────────────────────────────────────\n");
        rapport.append(String.format("Nombre de clients       : %d\n", nbClients));
        rapport.append(String.format("Nombre de comptes       : %d\n", nbComptes));
        rapport.append(String.format("Nombre de transactions  : %d\n", nbTransactions));

        double soldeTotalBanque = compteDAO.findAll().stream()
                .mapToDouble(Compte::getSolde)
                .sum();
        rapport.append(String.format("Solde total de la banque: %,.2f MAD\n", soldeTotalBanque));

        return rapport.toString();
    }

    public String genererAlertesSoldesBas(double seuil) throws SQLException {
        List<Compte> comptes = compteDAO.findAll();
        List<Compte> comptesBas = comptes.stream()
                .filter(c -> c.getSolde() < seuil)
                .collect(Collectors.toList());

        StringBuilder rapport = new StringBuilder();
        rapport.append("\n╔═══════════════════════════════════════════╗\n");
        rapport.append("║    ALERTES SOLDES BAS                     ║\n");
        rapport.append("╚═══════════════════════════════════════════╝\n\n");

        rapport.append(String.format("Seuil d'alerte : %,.2f MAD\n", seuil));
        rapport.append(String.format("Comptes concernés : %d\n\n", comptesBas.size()));

        for (Compte compte : comptesBas) {
            rapport.append(String.format("⚠️  Code: %s | Solde: %,.2f MAD\n",
                    compte.getCode(), compte.getSolde()));
        }

        return rapport.toString();
    }

    private int getClientId(Client client) throws SQLException {
        List<Client> clients = clientDAO.findAll();
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).equals(client)) {
                return i + 1;
            }
        }
        return -1;
    }
}