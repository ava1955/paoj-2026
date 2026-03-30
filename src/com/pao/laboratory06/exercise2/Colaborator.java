package com.pao.laboratory06.exercise2;

public abstract class Colaborator implements IOperatiiCitireScriere {
    protected String nume;
    protected String prenume;
    protected double venitBrutLunar;

    public abstract double calculeazaVenitNetAnual();

    public abstract TipColaborator getTipColaborator();

    @Override
    public String tipContract() {
        return getTipColaborator().name();
    }

    protected String getNumeComplet() {
        return nume + " " + prenume;
    }

    protected String getDescriereCuVenit() {
        return tipContract() + ": " + getNumeComplet() + ", venit net anual: " +
                String.format("%.2f lei", calculeazaVenitNetAnual());
    }
}