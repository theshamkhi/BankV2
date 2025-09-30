package entity;

public final class CompteEpargne extends Compte {
    private double tauxInteret;

    CompteEpargne(String code, double solde, int idClient, double tauxInteret){
        super(code, solde, idClient);
        this.tauxInteret = tauxInteret;
    }

    public double getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(double tauxInteret) {
        this.tauxInteret = tauxInteret;
    }
}
