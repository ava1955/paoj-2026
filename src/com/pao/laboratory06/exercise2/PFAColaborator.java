package com.pao.laboratory06.exercise2;

import java.util.Scanner;

public class PFAColaborator extends PersoanaFizica {
    private double cheltuieliLunare;

    private static final double SALARIU_MINIM_BRUT_ANUAL = 4050 * 12;

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
        double venitNet = (venitBrutLunar - cheltuieliLunare) * 12;

        double impozit = 0.10 * venitNet;

        double cass;
        if (venitNet < 6 * SALARIU_MINIM_BRUT_ANUAL) {
            cass = 0.10 * (6 * SALARIU_MINIM_BRUT_ANUAL);
        } else if (venitNet <= 72 * SALARIU_MINIM_BRUT_ANUAL) {
            cass = 0.10 * venitNet;
        } else {
            cass = 0.10 * (72 * SALARIU_MINIM_BRUT_ANUAL);
        }

        double cas = 0.0;
        if (venitNet >= 12 * SALARIU_MINIM_BRUT_ANUAL) {
            double bazaCas = (venitNet <= 24 * SALARIU_MINIM_BRUT_ANUAL)
                    ? 12 * SALARIU_MINIM_BRUT_ANUAL
                    : 24 * SALARIU_MINIM_BRUT_ANUAL;
            cas = 0.25 * bazaCas;
        }

        return venitNet - impozit - cass - cas;
    }

    @Override
    public TipColaborator getTipColaborator() {
        return TipColaborator.PFA;
    }
}