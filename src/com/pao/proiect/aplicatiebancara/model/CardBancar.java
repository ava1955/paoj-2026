package com.pao.proiect.aplicatiebancara.model;
import com.pao.proiect.aplicatiebancara.exception.InsufficientFundsException;
import com.pao.proiect.aplicatiebancara.exception.InvalidPINException;

import java.time.LocalDate;
public class CardBancar {
    private String numarCard;
    private String PIN;
    private LocalDate dataExpirare;
    public enum TipCard{
        DEBIT,
        CREDIT
    }
    private TipCard tipCard;
    private ContBancar contAsociat;
    private boolean blocat;
    public CardBancar(String numarCard, String PIN, LocalDate dataExpirare, TipCard tipCard, ContBancar contAsociat, boolean blocat) throws InvalidPINException {
        if(PIN.length() != 4) {
            throw new InvalidPINException("PIN invalid. Incercati din nou.");
        }
        this.numarCard = numarCard;
        this.PIN = PIN;
        this.dataExpirare = dataExpirare;
        this.tipCard = tipCard;
        this.contAsociat = contAsociat;
        this.blocat = blocat;
    }
    public TipCard getTipCard() {
        return tipCard;
    }
    public void blocheazaCard() {
        this.blocat = true;
    }
    public boolean esteBlocat() {
        return blocat;
    }
    public void efectueazaPlata(double suma) throws InsufficientFundsException {
        if (blocat) throw new IllegalStateException("Cardul este blocat!");
        contAsociat.retragere(suma);
    }
    public String getPIN() {
        return PIN;
    }

    public String getNumarCard() {
        return numarCard;
    }
    public ContBancar getContAsociat() {
        return contAsociat;
    }
}
