package com.pao.proiect.aplicatiebancara.model;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class IstoricTranzactie {
    private List<Tranzactie> listaTranzactii;
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
    public List<Tranzactie> getTranzactiiInPerioda(LocalDate start, LocalDate end) {
        return listaTranzactii.stream()
                .filter(t -> {
                    LocalDate data = t.dataSiOra().toLocalDate();
                    return !data.isBefore(start) && !data.isAfter(end);
                })
                .collect(Collectors.toList());
    }
}
