package com.pao.laboratory06.exercise2;

import java.util.Scanner;

public class SRLColaborator extends PersoanaJuridica {
    private double cheltuieliLunare;

    @Override
    public void citeste(Scanner in) {
        this.nume = in.next();
        this.prenume = in.next();
        this.venitBrutLunar = in.nextDouble();
        this.cheltuieliLunare = in.nextDouble();
    }

    @Override
    public void afiseaza() {
        System.out.println(getDescriereCuVenit());
    }

    @Override
    public double calculeazaVenitNetAnual() {
        double profitAnual = (venitBrutLunar - cheltuieliLunare) * 12;
        return profitAnual * 0.84;
    }

    @Override
    public TipColaborator getTipColaborator() {
        return TipColaborator.SRL;
    }
}