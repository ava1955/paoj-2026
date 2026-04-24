package com.pao.proiect.aplicatiebancara.model;

import com.pao.proiect.aplicatiebancara.exception.InvalidCNPException;

import java.time.LocalDate;
public abstract class Persoana {
    protected final String CNP;
    protected String nume, prenume, adresa;
    protected LocalDate dataNastere;

    protected Persoana(String CNP, String nume, String prenume, String adresa, LocalDate dataNastere) throws InvalidCNPException {
        if (CNP.isEmpty()) {
            throw new InvalidCNPException("CNP invalid. Incercati din nou.");
        }
        this.CNP = CNP;
        this.nume = nume;
        this.prenume = prenume;
        this.adresa = adresa;
        this.dataNastere = dataNastere;
    }

    public abstract String getRol();

    public String getCNP() {
        return CNP;
    }

    public LocalDate getDataNastere() {
        return dataNastere;
    }

    public String getAdresa() {
        return adresa;
    }

    public String getNume() {
        return nume;
    }

    public String getPrenume() {
        return prenume;
    }
}