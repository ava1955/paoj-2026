package com.pao.proiect.aplicatiebancara.model;
import java.util.List;

public class IstoricTranzactie {
    List<Tranzactie> listaTranzactii;
    public IstoricTranzactie(List<Tranzactie> listaTranzactii){
        this.listaTranzactii=listaTranzactii;
    }
    public void adaugaTranzactie(Tranzactie t){
        listaTranzactii.add(t);
    }
    public List<Tranzactie> getTranzactiiRecente(int n){
        return listaTranzactii.subList(Math.max(listaTranzactii.size() - n, 0), listaTranzactii.size());
    }
    public Tranzactie getUltimaTranzactie() {
        if (listaTranzactii.isEmpty()) return null;
        return listaTranzactii.get(listaTranzactii.size() - 1);
    }
}
