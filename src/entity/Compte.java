package entity;

public sealed abstract class Compte permits CompteCourant, CompteEpargne {
    String code;
    double solde;
    int idClient;

    Compte(String code, double solde, int idClient) {
        this.code = code;
        this.solde = solde;
        this.idClient = idClient;
    }

    public String getCode() {
        return code;
    }

    public double getSolde() {
        return solde;
    }

    public int getIdClient() {
        return idClient;
    }

}