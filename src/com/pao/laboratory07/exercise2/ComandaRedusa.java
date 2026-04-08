package com.pao.laboratory07.exercise2;

public final class ComandaRedusa extends Comanda {
    private final double pretInitial;
    private final int discountProcent;

    public ComandaRedusa(String nume, double pretInitial, int discountProcent) {
        super(nume);
        this.pretInitial = pretInitial;
        this.discountProcent = discountProcent;
    }

    @Override
    public double pretFinal() {
        return pretInitial * (1 - discountProcent / 100.0);
    }

    @Override
    public String descriere() {
        return String.format("DISCOUNTED: %s, pret: %.2f lei (-%d%%) [%s]",
                nume, pretFinal(), discountProcent, stare);
    }
}