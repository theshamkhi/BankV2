package ui;

import entity.TypeTransaction;
import service.ClientService;
import service.CompteService;
import service.RapportService;
import service.TransactionService;

import java.sql.SQLException;
import java.util.Scanner;

public class Menu {
    private final Scanner scanner;
    private final ClientService clientService;
    private final CompteService compteService;
    private final TransactionService transactionService;
    private final RapportService rapportService;

    public Menu() {
        this.scanner = new Scanner(System.in);
        this.clientService = new ClientService();
        this.compteService = new CompteService();
        this.transactionService = new TransactionService();
        this.rapportService = new RapportService();
    }

    public void afficher() {
        boolean quitter = false;

        while (!quitter) {
            afficherMenuPrincipal();
            int choix = lireEntier("Votre choix: ");

            try {
                switch (choix) {
                    case 1 -> menuGestionClients();
                    case 2 -> menuGestionComptes();
                    case 3 -> menuTransactions();
                    case 4 -> menuRapportsEtAnalyses();
                    case 5 -> menuAlertes();
                    case 0 -> {
                        System.out.println("\n👋 Merci d'avoir utilisé le système bancaire!");
                        quitter = true;
                    }
                    default -> System.out.println("❌ Choix invalide. Veuillez réessayer.");
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private void afficherMenuPrincipal() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║                    MENU PRINCIPAL                    ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("  1. 👥 Gestion des Clients");
        System.out.println("  2. 💰 Gestion des Comptes");
        System.out.println("  3. 💳 Transactions");
        System.out.println("  4. 📊 Rapports et Analyses");
        System.out.println("  5. 🔔 Alertes et Surveillance");
        System.out.println("  0. 🚪 Quitter");
        System.out.println("──────────────────────────────────────────────────────");
    }

    private void menuGestionClients() throws SQLException {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              GESTION DES CLIENTS                     ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("  1. Ajouter un client");
        System.out.println("  2. Modifier un client");
        System.out.println("  3. Supprimer un client");
        System.out.println("  4. Rechercher un client par ID");
        System.out.println("  5. Rechercher un client par nom");
        System.out.println("  6. Lister tous les clients");
        System.out.println("  7. Rapport détaillé d'un client");
        System.out.println("  0. Retour");
        System.out.println("──────────────────────────────────────────────────────");

        int choix = lireEntier("Votre choix: ");

        switch (choix) {
            case 1 -> ajouterClient();
            case 2 -> modifierClient();
            case 3 -> supprimerClient();
            case 4 -> rechercherClientParId();
            case 5 -> rechercherClientParNom();
            case 6 -> listerClients();
            case 7 -> afficherRapportClient();
            case 0 -> {}
            default -> System.out.println("❌ Choix invalide.");
        }
    }

    private void ajouterClient() throws SQLException {
        System.out.println("\n➕ AJOUTER UN NOUVEAU CLIENT");
        System.out.println("────────────────────────────────────────");
        String nom = lireChaine("Nom du client: ");
        String email = lireChaine("Email: ");

        int id = clientService.ajouterClient(nom, email);
        System.out.println("✅ Client créé avec succès! ID: " + id);
    }

    private void modifierClient() throws SQLException {
        System.out.println("\n✏️  MODIFIER UN CLIENT");
        System.out.println("────────────────────────────────────────");
        int id = lireEntier("ID du client: ");
        String nom = lireChaine("Nouveau nom: ");
        String email = lireChaine("Nouvel email: ");

        clientService.modifierClient(id, nom, email);
        System.out.println("✅ Client modifié avec succès!");
    }

    private void supprimerClient() throws SQLException {
        System.out.println("\n🗑️  SUPPRIMER UN CLIENT");
        System.out.println("────────────────────────────────────────");
        int id = lireEntier("ID du client: ");

        String confirmation = lireChaine("Êtes-vous sûr? (oui/non): ");
        if (confirmation.equalsIgnoreCase("oui")) {
            clientService.supprimerClient(id);
            System.out.println("✅ Client supprimé avec succès!");
        } else {
            System.out.println("❌ Suppression annulée.");
        }
    }

    private void rechercherClientParId() throws SQLException {
        System.out.println("\n🔍 RECHERCHER UN CLIENT PAR ID");
        System.out.println("────────────────────────────────────────");
        int id = lireEntier("ID du client: ");

        var client = clientService.rechercherParId(id);
        if (client.isPresent()) {
            System.out.println("✅ Client trouvé:");
            System.out.println("   Nom: " + client.get().nom());
            System.out.println("   Email: " + client.get().email());
        } else {
            System.out.println("❌ Client introuvable.");
        }
    }

    private void rechercherClientParNom() throws SQLException {
        System.out.println("\n🔍 RECHERCHER UN CLIENT PAR NOM");
        System.out.println("────────────────────────────────────────");
        String nom = lireChaine("Nom à rechercher: ");

        var clients = clientService.rechercherParNom(nom);
        if (clients.isEmpty()) {
            System.out.println("❌ Aucun client trouvé.");
        } else {
            System.out.println("✅ " + clients.size() + " client(s) trouvé(s):");
            clients.forEach(c -> System.out.println("   • " + c.nom() + " - " + c.email()));
        }
    }

    private void listerClients() throws SQLException {
        System.out.println("\n📋 LISTE DE TOUS LES CLIENTS");
        System.out.println("────────────────────────────────────────");
        var clients = clientService.listerTousLesClients();

        if (clients.isEmpty()) {
            System.out.println("Aucun client enregistré.");
        } else {
            System.out.println("Total: " + clients.size() + " client(s)\n");
            clients.forEach(c -> System.out.println("   • " + c.nom() + " - " + c.email()));
        }
    }

    private void afficherRapportClient() throws SQLException {
        System.out.println("\n📄 RAPPORT DÉTAILLÉ D'UN CLIENT");
        System.out.println("────────────────────────────────────────");
        int id = lireEntier("ID du client: ");

        String rapport = clientService.genererRapportClient(id);
        System.out.println(rapport);
    }

    private void menuGestionComptes() throws SQLException {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              GESTION DES COMPTES                     ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("  1. Créer un compte courant");
        System.out.println("  2. Créer un compte épargne");
        System.out.println("  3. Rechercher un compte par code");
        System.out.println("  4. Lister les comptes d'un client");
        System.out.println("  5. Lister tous les comptes");
        System.out.println("  6. Afficher le compte avec le solde maximum");
        System.out.println("  7. Afficher le compte avec le solde minimum");
        System.out.println("  8. Supprimer un compte");
        System.out.println("  0. Retour");
        System.out.println("──────────────────────────────────────────────────────");

        int choix = lireEntier("Votre choix: ");

        switch (choix) {
            case 1 -> creerCompteCourant();
            case 2 -> creerCompteEpargne();
            case 3 -> rechercherCompteParCode();
            case 4 -> listerComptesClient();
            case 5 -> listerTousLesComptes();
            case 6 -> afficherCompteSoldeMax();
            case 7 -> afficherCompteSoldeMin();
            case 8 -> supprimerCompte();
            case 0 -> {}
            default -> System.out.println("❌ Choix invalide.");
        }
    }

    private void creerCompteCourant() throws SQLException {
        System.out.println("\n➕ CRÉER UN COMPTE COURANT");
        System.out.println("────────────────────────────────────────");
        int idClient = lireEntier("ID du client: ");
        double soldeInitial = lireDouble("Solde initial: ");
        double decouvert = lireDouble("Découvert autorisé: ");

        compteService.creerCompteCourant(idClient, soldeInitial, decouvert);
        System.out.println("✅ Compte courant créé avec succès!");
    }

    private void creerCompteEpargne() throws SQLException {
        System.out.println("\n➕ CRÉER UN COMPTE ÉPARGNE");
        System.out.println("────────────────────────────────────────");
        int idClient = lireEntier("ID du client: ");
        double soldeInitial = lireDouble("Solde initial: ");
        double tauxInteret = lireDouble("Taux d'intérêt (%): ");

        compteService.creerCompteEpargne(idClient, soldeInitial, tauxInteret);
        System.out.println("✅ Compte épargne créé avec succès!");
    }

    private void rechercherCompteParCode() throws SQLException {
        System.out.println("\n🔍 RECHERCHER UN COMPTE");
        System.out.println("────────────────────────────────────────");
        String code = lireChaine("Code du compte: ");

        var compte = compteService.rechercherParCode(code);
        if (compte.isPresent()) {
            System.out.println("✅ Compte trouvé:");
            System.out.println("   Code: " + compte.get().getCode());
            System.out.println("   Solde: " + String.format("%.2f", compte.get().getSolde()) + " MAD");
            System.out.println("   Type: " + compte.get().getClass().getSimpleName());
        } else {
            System.out.println("❌ Compte introuvable.");
        }
    }

    private void listerComptesClient() throws SQLException {
        System.out.println("\n📋 COMPTES D'UN CLIENT");
        System.out.println("────────────────────────────────────────");
        int idClient = lireEntier("ID du client: ");

        var comptes = compteService.rechercherParClient(idClient);
        if (comptes.isEmpty()) {
            System.out.println("❌ Aucun compte trouvé pour ce client.");
        } else {
            System.out.println("✅ " + comptes.size() + " compte(s) trouvé(s):\n");
            comptes.forEach(c -> System.out.println(
                    String.format("   • %s | Solde: %.2f MAD | Type: %s",
                            c.getCode(), c.getSolde(), c.getClass().getSimpleName())
            ));
        }
    }

    private void listerTousLesComptes() throws SQLException {
        System.out.println("\n📋 TOUS LES COMPTES");
        System.out.println("────────────────────────────────────────");
        var comptes = compteService.listerTousLesComptes();

        if (comptes.isEmpty()) {
            System.out.println("Aucun compte enregistré.");
        } else {
            System.out.println("Total: " + comptes.size() + " compte(s)\n");
            comptes.forEach(c -> System.out.println(
                    String.format("   • %s | Solde: %.2f MAD | Type: %s",
                            c.getCode(), c.getSolde(), c.getClass().getSimpleName())
            ));
        }
    }

    private void afficherCompteSoldeMax() throws SQLException {
        var compte = compteService.trouverCompteSoldeMax();
        if (compte.isPresent()) {
            System.out.println("\n💎 COMPTE AVEC LE SOLDE MAXIMUM:");
            System.out.println("   Code: " + compte.get().getCode());
            System.out.println("   Solde: " + String.format("%.2f", compte.get().getSolde()) + " MAD");
        } else {
            System.out.println("❌ Aucun compte trouvé.");
        }
    }

    private void afficherCompteSoldeMin() throws SQLException {
        var compte = compteService.trouverCompteSoldeMin();
        if (compte.isPresent()) {
            System.out.println("\n📉 COMPTE AVEC LE SOLDE MINIMUM:");
            System.out.println("   Code: " + compte.get().getCode());
            System.out.println("   Solde: " + String.format("%.2f", compte.get().getSolde()) + " MAD");
        } else {
            System.out.println("❌ Aucun compte trouvé.");
        }
    }

    private void supprimerCompte() throws SQLException {
        System.out.println("\n🗑️  SUPPRIMER UN COMPTE");
        System.out.println("────────────────────────────────────────");
        String code = lireChaine("Code du compte: ");

        String confirmation = lireChaine("Êtes-vous sûr? (oui/non): ");
        if (confirmation.equalsIgnoreCase("oui")) {
            compteService.supprimerCompte(code);
            System.out.println("✅ Compte supprimé avec succès!");
        } else {
            System.out.println("❌ Suppression annulée.");
        }
    }

    private void menuTransactions() throws SQLException {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║                  TRANSACTIONS                        ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("  1. Effectuer un versement");
        System.out.println("  2. Effectuer un retrait");
        System.out.println("  3. Effectuer un virement");
        System.out.println("  4. Consulter l'historique d'un compte");
        System.out.println("  5. Lister toutes les transactions");
        System.out.println("  6. Filtrer les transactions par type");
        System.out.println("  7. Filtrer les transactions par montant minimum");
        System.out.println("  8. Statistiques par type de transaction");
        System.out.println("  0. Retour");
        System.out.println("──────────────────────────────────────────────────────");

        int choix = lireEntier("Votre choix: ");

        switch (choix) {
            case 1 -> effectuerVersement();
            case 2 -> effectuerRetrait();
            case 3 -> effectuerVirement();
            case 4 -> consulterHistorique();
            case 5 -> listerToutesTransactions();
            case 6 -> filtrerParType();
            case 7 -> filtrerParMontant();
            case 8 -> afficherStatistiquesParType();
            case 0 -> {}
            default -> System.out.println("❌ Choix invalide.");
        }
    }

    private void effectuerVersement() throws SQLException {
        System.out.println("\n💵 EFFECTUER UN VERSEMENT");
        System.out.println("────────────────────────────────────────");
        String code = lireChaine("Code du compte: ");
        double montant = lireDouble("Montant: ");
        String lieu = lireChaine("Lieu (Agence par défaut): ");

        if (lieu.trim().isEmpty()) lieu = "Agence";

        transactionService.effectuerVersement(code, montant, lieu);
        System.out.println("✅ Versement effectué avec succès!");
    }

    private void effectuerRetrait() throws SQLException {
        System.out.println("\n💸 EFFECTUER UN RETRAIT");
        System.out.println("────────────────────────────────────────");
        String code = lireChaine("Code du compte: ");
        double montant = lireDouble("Montant: ");
        String lieu = lireChaine("Lieu (Agence par défaut): ");

        if (lieu.trim().isEmpty()) lieu = "Agence";

        transactionService.effectuerRetrait(code, montant, lieu);
        System.out.println("✅ Retrait effectué avec succès!");
    }

    private void effectuerVirement() throws SQLException {
        System.out.println("\n🔄 EFFECTUER UN VIREMENT");
        System.out.println("────────────────────────────────────────");
        String codeSource = lireChaine("Code du compte source: ");
        String codeDest = lireChaine("Code du compte destination: ");
        double montant = lireDouble("Montant: ");

        transactionService.effectuerVirement(codeSource, codeDest, montant);
        System.out.println("✅ Virement effectué avec succès!");
    }

    private void consulterHistorique() throws SQLException {
        System.out.println("\n📜 HISTORIQUE DES TRANSACTIONS");
        System.out.println("────────────────────────────────────────");
        int compteId = lireEntier("ID du compte: ");

        var transactions = transactionService.listerTransactionsParCompte(compteId);
        if (transactions.isEmpty()) {
            System.out.println("❌ Aucune transaction trouvée.");
        } else {
            System.out.println("✅ " + transactions.size() + " transaction(s):\n");
            transactions.forEach(t -> System.out.println(
                    String.format("   • %s | %.2f MAD | %s | %s",
                            t.date().toString(), t.montant(), t.type(), t.lieu())
            ));
        }
    }

    private void listerToutesTransactions() throws SQLException {
        System.out.println("\n📜 TOUTES LES TRANSACTIONS");
        System.out.println("────────────────────────────────────────");
        var transactions = transactionService.listerToutesLesTransactions();

        if (transactions.isEmpty()) {
            System.out.println("Aucune transaction enregistrée.");
        } else {
            System.out.println("Total: " + transactions.size() + " transaction(s)\n");
            transactions.stream().limit(20).forEach(t -> System.out.println(
                    String.format("   • %s | %.2f MAD | %s | %s",
                            t.date().toString(), t.montant(), t.type(), t.lieu())
            ));
            if (transactions.size() > 20) {
                System.out.println("\n   ... (" + (transactions.size() - 20) + " autres)");
            }
        }
    }

    private void filtrerParType() throws SQLException {
        System.out.println("\n🔍 FILTRER PAR TYPE");
        System.out.println("────────────────────────────────────────");
        System.out.println("Types disponibles:");
        System.out.println("  1. VERSEMENT");
        System.out.println("  2. RETRAIT");
        System.out.println("  3. VIREMENT");

        int choix = lireEntier("Votre choix: ");
        TypeTransaction type = switch (choix) {
            case 1 -> TypeTransaction.VERSEMENT;
            case 2 -> TypeTransaction.RETRAIT;
            case 3 -> TypeTransaction.VIREMENT;
            default -> null;
        };

        if (type != null) {
            var transactions = transactionService.filtrerParType(type);
            System.out.println("✅ " + transactions.size() + " transaction(s) de type " + type);
        } else {
            System.out.println("❌ Type invalide.");
        }
    }

    private void filtrerParMontant() throws SQLException {
        System.out.println("\n🔍 FILTRER PAR MONTANT MINIMUM");
        System.out.println("────────────────────────────────────────");
        double montant = lireDouble("Montant minimum: ");

        var transactions = transactionService.filtrerParMontant(montant);
        if (transactions.isEmpty()) {
            System.out.println("❌ Aucune transaction trouvée.");
        } else {
            System.out.println("✅ " + transactions.size() + " transaction(s) trouvée(s):\n");
            transactions.forEach(t -> System.out.println(
                    String.format("   • %s | %.2f MAD | %s",
                            t.date().toString(), t.montant(), t.type())
            ));
        }
    }

    private void afficherStatistiquesParType() throws SQLException {
        System.out.println("\n📊 STATISTIQUES PAR TYPE");
        System.out.println("────────────────────────────────────────");
        var stats = transactionService.calculerStatistiquesParType();

        stats.forEach((type, total) ->
                System.out.println(String.format("   %s: %.2f MAD", type, total))
        );
    }

    private void menuRapportsEtAnalyses() throws SQLException {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║            RAPPORTS ET ANALYSES                      ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("  1. Top 5 des clients par solde");
        System.out.println("  2. Rapport mensuel");
        System.out.println("  3. Détecter les transactions suspectes");
        System.out.println("  4. Identifier les comptes inactifs");
        System.out.println("  5. Rapport complet du système");
        System.out.println("  0. Retour");
        System.out.println("──────────────────────────────────────────────────────");

        int choix = lireEntier("Votre choix: ");

        switch (choix) {
            case 1 -> afficherTop5Clients();
            case 2 -> afficherRapportMensuel();
            case 3 -> detecterTransactionsSuspectes();
            case 4 -> identifierComptesInactifs();
            case 5 -> afficherRapportComplet();
            case 0 -> {}
            default -> System.out.println("❌ Choix invalide.");
        }
    }

    private void afficherTop5Clients() throws SQLException {
        String rapport = rapportService.genererTop5ClientsParSolde();
        System.out.println(rapport);
    }

    private void afficherRapportMensuel() throws SQLException {
        int mois = lireEntier("Mois (1-12): ");
        int annee = lireEntier("Année: ");

        String rapport = rapportService.genererRapportMensuel(mois, annee);
        System.out.println(rapport);
    }

    private void detecterTransactionsSuspectes() throws SQLException {
        double seuil = lireDouble("Seuil de montant (ex: 10000): ");

        String rapport = rapportService.detecterTransactionsSuspectes(seuil);
        System.out.println(rapport);
    }

    private void identifierComptesInactifs() throws SQLException {
        int jours = lireEntier("Nombre de jours d'inactivité: ");

        String rapport = rapportService.identifierComptesInactifs(jours);
        System.out.println(rapport);
    }

    private void afficherRapportComplet() throws SQLException {
        String rapport = rapportService.genererRapportComplet();
        System.out.println(rapport);
    }

    private void menuAlertes() throws SQLException {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║          ALERTES ET SURVEILLANCE                     ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("  1. Alertes soldes bas");
        System.out.println("  2. Comptes inactifs");
        System.out.println("  3. Transactions suspectes récentes");
        System.out.println("  0. Retour");
        System.out.println("──────────────────────────────────────────────────────");

        int choix = lireEntier("Votre choix: ");

        switch (choix) {
            case 1 -> {
                double seuil = lireDouble("Seuil d'alerte: ");
                String rapport = rapportService.genererAlertesSoldesBas(seuil);
                System.out.println(rapport);
            }
            case 2 -> identifierComptesInactifs();
            case 3 -> detecterTransactionsSuspectes();
            case 0 -> {}
            default -> System.out.println("❌ Choix invalide.");
        }
    }

    private int lireEntier(String message) {
        System.out.print(message);
        while (!scanner.hasNextInt()) {
            System.out.print("❌ Veuillez entrer un nombre entier: ");
            scanner.next();
        }
        int valeur = scanner.nextInt();
        scanner.nextLine();
        return valeur;
    }

    private double lireDouble(String message) {
        System.out.print(message);
        while (!scanner.hasNextDouble()) {
            System.out.print("❌ Veuillez entrer un nombre valide: ");
            scanner.next();
        }
        double valeur = scanner.nextDouble();
        scanner.nextLine();
        return valeur;
    }

    private String lireChaine(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }
}