package com.pao.proiect.aplicatiebancara.service;

import com.pao.proiect.aplicatiebancara.model.Banca;
import com.pao.proiect.aplicatiebancara.model.CardBancar;

import java.util.List;

public class CardService {

    private static CardService instance;
    private final Banca banca;

    private CardService(Banca banca) {
        this.banca = banca;
    }

    public static CardService getInstance(Banca banca) {
        if (instance == null) instance = new CardService(banca);
        return instance;
    }

    public void blocheazaCard(CardBancar card) {
        if (card == null) throw new IllegalArgumentException("Card invalid");
        card.blocheazaCard();
    }

    public List<CardBancar> listeazaToateCardurile() {
        return banca.getListaCarduri();
    }
}