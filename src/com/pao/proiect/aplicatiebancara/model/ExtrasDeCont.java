package com.pao.proiect.aplicatiebancara.model;
import java.time.LocalDate;
import java.util.List;
public class ExtrasDeCont {
    private LocalDate[] perioada;
    private List<Tranzactie> listaTranzactii;
    private ContBancar contAsociat;
    private double soldInceputPerioada, soldSfarsitPerioada;
    public void genereazaExtrasPDF(){
        double totalIntrari = calculeazaTotalIntrari();
        double totalIesiri = calculeazaTotalIesiri();
        System.out.println("EXTRAS DE CONT PDF");
        System.out.println("Cont IBAN: " + contAsociat.getIBAN());
        System.out.println("Perioada: " + perioada[0] + " - " + perioada[1]);
        System.out.println("Sold inceput perioada: " + soldInceputPerioada);
        System.out.println("Sold sfarsit perioada: " + soldSfarsitPerioada);
        System.out.println("Total intrari: " + totalIntrari);
        System.out.println("Total iesiri: " + totalIesiri);
        System.out.println("Tranzactii efectuate: " + listaTranzactii.size());
        for(Tranzactie t : listaTranzactii){
            System.out.println(" - " + t.dataSiOra() + " | " + t.tipTranzactie() + " | " + t.suma() + " RON | " + t.descriere());
        }
        System.out.println("\nExtras generat cu succes.");
    }
    public ExtrasDeCont(LocalDate[] perioada, List<Tranzactie> listaTranzactii, ContBancar contasociat, double soldInceputPerioada, double soldSfarsitPerioada){
        this.perioada=perioada;
        this.listaTranzactii=listaTranzactii;
        this.contAsociat=contasociat;
        this.soldInceputPerioada=soldInceputPerioada;
        this.soldSfarsitPerioada=soldSfarsitPerioada;
    }
    private double calculeazaTotalIntrari(){
        double total = 0;
        for(Tranzactie t : listaTranzactii){
            if(t.tipTranzactie() == TipTranzactie.DEPUNERE || (t.tipTranzactie() == TipTranzactie.TRANSFER && t.contDestinatar() == contAsociat)){
                total += t.suma();
            }
        }
        return total;
    }
    private double calculeazaTotalIesiri(){
        double total = 0;
        for(Tranzactie t : listaTranzactii){
            if(t.tipTranzactie() == TipTranzactie.RETRAGERE || (t.tipTranzactie() == TipTranzactie.TRANSFER && t.contSursa() == contAsociat)){
                total += t.suma();
            }
        }
        return total;
    }
}