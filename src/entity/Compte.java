package entity;

public sealed class Compte permits CompteCourant, CompteEpargne {
    protected String code;
    protected double solde;
    protected int idClient;






}
