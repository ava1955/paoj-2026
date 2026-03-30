package com.pao.laboratory06.exercise2;

import java.util.Scanner;

public class CIMColaborator extends PersoanaFizica {
    private boolean bonus = false;

    @Override
    public void citeste(Scanner in) {
        this.nume = in.next();
        this.prenume = in.next();
        this.venitBrutLunar = in.nextDouble();

        if (in.hasNext() && !in.hasNextDouble()) {
            String b = in.next();
            this.bonus = "DA".equalsIgnoreCase(b);
        }
    }

    @Override
    public void afiseaza() {
        System.out.println(getDescriereCuVenit());
    }

    @Override
    public boolean areBonus() {
        return bonus;
    }

    @Override
    public double calculeazaVenitNetAnual() {
        double brutAnual = venitBrutLunar * 12;
        double net = brutAnual * 0.55;
        if (areBonus()) {
            net *= 1.10;
        }
        return net;
    }

    @Override
    public TipColaborator getTipColaborator() {
        return TipColaborator.CIM;
    }
}