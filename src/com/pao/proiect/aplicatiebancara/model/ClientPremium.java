package com.pao.proiect.aplicatiebancara.model;

import com.pao.proiect.aplicatiebancara.exception.InvalidCNPException;

import java.time.LocalDate;
import java.util.ArrayList;

public class ClientPremium extends Client{
    private double limitaMaximaTranzactii, dobandaBonus;
    private boolean accesLaCreditRapid;
    public ClientPremium(String CNP, String nume, String prenume, String adresa,
                         LocalDate dataNastere, int IDClient, LocalDate dataInregistrare) throws InvalidCNPException {
        super(CNP, nume, prenume, adresa, dataNastere, IDClient, dataInregistrare);
        aplicaBeneficiiPremium();
    }
    @Override
    public String getRol() {
        return "Client Premium";
    }

    public double getLimitaMaximaTranzactii() {
        return limitaMaximaTranzactii;
    }
    public double getDobandaBonus() {
        return dobandaBonus;
    }
    public boolean areAccesLaCreditRapid(){
        return accesLaCreditRapid;
    }

    public void setLimitaMaximaTranzactii(double limitaMaximaTranzactii) {
        this.limitaMaximaTranzactii = limitaMaximaTranzactii;
    }

    public void setDobandaBonus(double dobandaBonus) {
        this.dobandaBonus = dobandaBonus;
    }

    public void setAccesLaCreditRapid(boolean acces){
        this.accesLaCreditRapid = acces;
    }

    public void aplicaBeneficiiPremium(){
        this.limitaMaximaTranzactii = 50000.0f;
        this.dobandaBonus = 0.75f;
        this.accesLaCreditRapid = true;
    }
}