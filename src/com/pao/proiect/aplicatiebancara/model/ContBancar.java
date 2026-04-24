package com.pao.proiect.aplicatiebancara.model;
import com.pao.proiect.aplicatiebancara.exception.InsufficientFundsException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class ContBancar {
    private String IBAN;
    private double sold;
    private String valuta;
    private LocalDate dataDeschidere;
    private Client titular;
    private IstoricTranzactie istoricTranzactii;

    public ContBancar(String IBAN, double sold, String valuta, LocalDate dataDeschidere, Client titular) {
        this.IBAN = IBAN;
        this.sold = sold;
        this.valuta = valuta;
        this.dataDeschidere = dataDeschidere;
        this.titular = titular;
        this.istoricTranzactii = new IstoricTranzactie(new ArrayList<>());
    }

    private void fundchecker(double suma) throws InsufficientFundsException {
        if (sold < suma) {
            throw new InsufficientFundsException("Fonduri insuficiente! IBAN: " + IBAN + ", sold: " + sold + ", suma ceruta: " + suma);
        }
    }

    public void depunere(double suma) {
        if (suma <= 0) return;
        sold += suma;
        Tranzactie t = new Tranzactie("DEP-" + System.currentTimeMillis(), suma, LocalDateTime.now(),
                "Depunere numerar", TipTranzactie.DEPUNERE, this, null);
        istoricTranzactii.adaugaTranzactie(t);
    }

    public void retragere(double suma) throws InsufficientFundsException {
        fundchecker(suma);
        sold -= suma;
        Tranzactie t = new Tranzactie("RET-" + System.currentTimeMillis(), suma, LocalDateTime.now(),
                "Retragere numerar", TipTranzactie.RETRAGERE, this, null);
        istoricTranzactii.adaugaTranzactie(t);
    }

    public void transfer(ContBancar destinatar, double suma) throws InsufficientFundsException {
        if (destinatar == null) return;
        fundchecker(suma);
        sold -= suma;
        destinatar.sold += suma;
        Tranzactie t = new Tranzactie("TR-" + System.currentTimeMillis(), suma, LocalDateTime.now(),
                "Transfer catre IBAN " + destinatar.getIBAN(), TipTranzactie.TRANSFER, this, destinatar);
        istoricTranzactii.adaugaTranzactie(t);
        destinatar.istoricTranzactii.adaugaTranzactie(t);
    }

    public IstoricTranzactie getIstoricTranzactii() {
        return istoricTranzactii;
    }

    public double getSold() {
        return sold;
    }

    public String getIBAN() {
        return IBAN;
    }

    public Client getTitular() {
        return titular;
    }

    @Override
    public String toString() {
        return "ContBancar{" + "IBAN=" + IBAN + ", sold=" + sold + ", valuta='" + valuta + '\'' + ", titular=" + titular.getNume() + " " + titular.getPrenume() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContBancar that)) return false;
        return IBAN.equals(that.IBAN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IBAN);
    }
}