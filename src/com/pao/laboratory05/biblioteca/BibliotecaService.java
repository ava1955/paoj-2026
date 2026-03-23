package com.pao.laboratory05.biblioteca;

import java.util.Arrays;
import java.util.Comparator;

public class BibliotecaService {
    private Carte[] carti = new Carte[0];

    private BibliotecaService() {}

    private static class BibliotecaHolder {
        private static final BibliotecaService INSTANCE = new BibliotecaService();
    }

    public static BibliotecaService getInstance() {
        return BibliotecaHolder.INSTANCE;
    }

    public void addCarte(Carte carte) {
        Carte[] noiCarti = new Carte[carti.length + 1];
        System.arraycopy(carti, 0, noiCarti, 0, carti.length);
        noiCarti[carti.length] = carte;
        this.carti = noiCarti;
        System.out.println("Adaugat: " + carte.getTitlu());
    }

    public void listSortedByRating() {
        Carte[] copy = carti.clone();
        Arrays.sort(copy);
        for (Carte c : copy) System.out.println(c);
    }

    public void listSortedBy(Comparator<Carte> comparator) {
        Carte[] copy = carti.clone();
        Arrays.sort(copy, comparator);
        for (Carte c : copy) System.out.println(c);
    }
}