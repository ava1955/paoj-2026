package com.pao.laboratory05.angajati;

import java.util.Arrays;

public class AngajatService {
    private Angajat[] angajati = new Angajat[0];

    private AngajatService() {}

    private static class Holder {
        private static final AngajatService INSTANCE = new AngajatService();
    }

    public static AngajatService getInstance() {
        return Holder.INSTANCE;
    }

    public void addAngajat(Angajat a) {
        Angajat[] noi = new Angajat[angajati.length + 1];
        System.arraycopy(angajati, 0, noi, 0, angajati.length);
        noi[angajati.length] = a;
        this.angajati = noi;
        System.out.println("Angajat adaugat cu succes: " + a.getNume());
    }

    public void printAll() {
        for (Angajat a : angajati) System.out.println(a);
    }

    public void listBySalary() {
        Angajat[] copy = angajati.clone();
        Arrays.sort(copy);
        for (Angajat a : copy) System.out.println(a);
    }

    public void findByDepartament(String numeDept) {
        boolean gasit = false;
        for (Angajat a : angajati) {
            if (a.getDepartament().nume().equalsIgnoreCase(numeDept)) {
                System.out.println(a);
                gasit = true;
            }
        }
        if (!gasit) {
            System.out.println("Niciun angajat in departamentul: " + numeDept);
        }
    }
}