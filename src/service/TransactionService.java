package service;

import dao.TransactionDAO;
import dao.CompteDAO;
import entity.Transaction;
import entity.TypeTransaction;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionService {
    private final TransactionDAO transactionDAO;
    private final CompteDAO compteDAO;
    private final CompteService compteService;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.compteDAO = new CompteDAO();
        this.compteService = new CompteService();
    }

    public void effectuerVersement(String codeCompte, double montant, String lieu) throws SQLException {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }

        var compteOpt = compteDAO.findByCode(codeCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte introuvable");
        }

        compteService.crediter(codeCompte, montant);

        Transaction transaction = new Transaction(
                LocalDateTime.now(),
                montant,
                TypeTransaction.VERSEMENT,
                lieu != null ? lieu : "Agence",
                compteOpt.get().getIdClient()
        );
        transactionDAO.save(transaction);
    }

    public void effectuerRetrait(String codeCompte, double montant, String lieu) throws SQLException {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }

        var compteOpt = compteDAO.findByCode(codeCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte introuvable");
        }

        compteService.debiter(codeCompte, montant);

        Transaction transaction = new Transaction(
                LocalDateTime.now(),
                montant,
                TypeTransaction.RETRAIT,
                lieu != null ? lieu : "Agence",
                compteOpt.get().getIdClient()
        );
        transactionDAO.save(transaction);
    }

    public void effectuerVirement(String codeCompteSource, String codeCompteDest, double montant) throws SQLException {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }

        var compteSourceOpt = compteDAO.findByCode(codeCompteSource);
        var compteDestOpt = compteDAO.findByCode(codeCompteDest);

        if (compteSourceOpt.isEmpty() || compteDestOpt.isEmpty()) {
            throw new IllegalArgumentException("Un ou plusieurs comptes introuvables");
        }

        compteService.debiter(codeCompteSource, montant);
        compteService.crediter(codeCompteDest, montant);

        Transaction transactionDebit = new Transaction(
                LocalDateTime.now(),
                montant,
                TypeTransaction.VIREMENT,
                "Virement vers " + codeCompteDest,
                compteSourceOpt.get().getIdClient()
        );
        transactionDAO.save(transactionDebit);

        Transaction transactionCredit = new Transaction(
                LocalDateTime.now(),
                montant,
                TypeTransaction.VIREMENT,
                "Virement de " + codeCompteSource,
                compteDestOpt.get().getIdClient()
        );
        transactionDAO.save(transactionCredit);
    }

    public List<Transaction> listerTransactionsParCompte(int compteId) throws SQLException {
        return transactionDAO.findByCompteId(compteId);
    }

    public List<Transaction> listerToutesLesTransactions() throws SQLException {
        return transactionDAO.findAll();
    }

    public List<Transaction> filtrerParType(TypeTransaction type) throws SQLException {
        return transactionDAO.findByType(type);
    }

    public List<Transaction> filtrerParMontant(double montantMin) throws SQLException {
        return transactionDAO.findByMontantGreaterThan(montantMin);
    }

    public List<Transaction> filtrerParPeriode(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        return transactionDAO.findByDateRange(debut, fin);
    }

    public List<Transaction> filtrerParLieu(String lieu) throws SQLException {
        return transactionDAO.findByLieu(lieu);
    }

    public Map<TypeTransaction, List<Transaction>> regrouperParType() throws SQLException {
        return transactionDAO.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::type));
    }

    public Map<String, List<Transaction>> regrouperParMois() throws SQLException {
        return transactionDAO.findAll().stream()
                .collect(Collectors.groupingBy(t ->
                        t.date().getYear() + "-" + String.format("%02d", t.date().getMonthValue())
                ));
    }

    public double calculerMoyenneTransactions(int compteId) throws SQLException {
        List<Transaction> transactions = transactionDAO.findByCompteId(compteId);
        return transactions.stream()
                .mapToDouble(Transaction::montant)
                .average()
                .orElse(0.0);
    }

    public double calculerTotalTransactions(int compteId) throws SQLException {
        return transactionDAO.findByCompteId(compteId).stream()
                .mapToDouble(Transaction::montant)
                .sum();
    }

    public List<Transaction> detecterTransactionsSuspectes(double seuilMontant, String paysHabituel) throws SQLException {
        List<Transaction> toutes = transactionDAO.findAll();
        List<Transaction> suspectes = new ArrayList<>();

        for (Transaction t : toutes) {
            if (t.montant() > seuilMontant) {
                suspectes.add(t);
                continue;
            }

            if (paysHabituel != null && !t.lieu().contains(paysHabituel)) {
                suspectes.add(t);
                continue;
            }
        }

        suspectes.addAll(detecterFrequenceExcessive());

        return suspectes.stream().distinct().collect(Collectors.toList());
    }

    private List<Transaction> detecterFrequenceExcessive() throws SQLException {
        List<Transaction> toutes = transactionDAO.findAll();
        List<Transaction> suspectes = new ArrayList<>();

        Map<Integer, List<Transaction>> parCompte = toutes.stream()
                .collect(Collectors.groupingBy(Transaction::idCompte));

        for (List<Transaction> transactions : parCompte.values()) {
            transactions.sort(Comparator.comparing(Transaction::date));

            for (int i = 0; i < transactions.size() - 1; i++) {
                Transaction t1 = transactions.get(i);
                Transaction t2 = transactions.get(i + 1);

                long secondes = ChronoUnit.SECONDS.between(t1.date(), t2.date());
                if (secondes < 60) {
                    suspectes.add(t1);
                    suspectes.add(t2);
                }
            }
        }

        return suspectes;
    }

    public Map<TypeTransaction, Double> calculerStatistiquesParType() throws SQLException {
        return transactionDAO.findAll().stream()
                .collect(Collectors.groupingBy(
                        Transaction::type,
                        Collectors.summingDouble(Transaction::montant)
                ));
    }
}