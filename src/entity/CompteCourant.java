package entity;

public final class CompteCourant extends Compte {
    double decouvert;

    CompteCourant(String code, double solde, int idClient, double decouvert){
        super(code, solde, idClient);
        this.decouvert = decouvert;
    }

    public double getDecouvert() {
        return decouvert;
    }

    public void setDecouvert(double decouvert) {
        this.decouvert = decouvert;
    }
}