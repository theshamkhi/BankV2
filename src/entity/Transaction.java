package entity;

import java.time.LocalDateTime;

public record Transaction(LocalDateTime date, double montant, TypeTransaction type, String lieu, int idCompte) {}
