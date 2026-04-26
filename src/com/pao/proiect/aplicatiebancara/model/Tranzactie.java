package com.pao.proiect.aplicatiebancara.model;

import java.time.LocalDateTime;

public record Tranzactie(String idTranzactie, double suma, LocalDateTime dataSiOra, String descriere, TipTranzactie tipTranzactie, ContBancar contSursa, ContBancar contDestinatar) {
    public void inregistreazaTranzactie() {
        System.out.println("Tranzactie inregistrata: " + idTranzactie() + " | " + suma + " RON | " + tipTranzactie());
    }

    public void genereazaComision() {
        double comision = 0;
        if(tipTranzactie() == TipTranzactie.TRANSFER && suma > 5000){
            comision = suma * 0.005;
        } else if(tipTranzactie() == TipTranzactie.RETRAGERE){
            comision = 2.5;
        }
        if(comision > 0){
            System.out.println("Comision aplicat: " + comision + " RON pentru tranzactia " + idTranzactie());
        }
    }

}