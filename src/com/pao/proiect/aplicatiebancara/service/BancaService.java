package com.pao.proiect.aplicatiebancara.service;

import com.pao.proiect.aplicatiebancara.exception.InvalidPINException;
import com.pao.proiect.aplicatiebancara.model.*;
import com.pao.proiect.aplicatiebancara.exception.InsufficientFundsException;
import java.time.LocalDate;
import java.util.List;

public class BancaService {

    private static BancaService instance;
    private final Banca banca;
    private int contorConturi = 0;
    private BancaService(Banca banca) {
        this.banca = banca;
    }

    public static BancaService getInstance(Banca banca) {
        if (instance == null) instance = new BancaService(banca);
        return instance;
    }

    public void creeazaContNou(Client client) {
        if (client == null) throw new IllegalArgumentException("Client invalid");
        String iban = "RO" + (1000000000L + contorConturi++);
        ContBancar cont = new ContBancar(iban, 0, "RON", LocalDate.now(), client);
        banca.adaugaCont(cont);
        client.adaugaCont(cont);
    }

    public void emiteCard(Client client, ContBancar cont, String tip) {
        if (client == null || cont == null) throw new IllegalArgumentException("Date invalide");
        try {
            String numar;
            do {
                numar = String.valueOf(400000000L + (long) (Math.random() * 999999999L));
            } while (banca.cardExista(numar));
            if("debit".equalsIgnoreCase(tip)){
                CardBancar card = new CardBancar(numar, "1234", LocalDate.now().plusYears(4), CardBancar.TipCard.DEBIT, cont, false);
                banca.adaugaCard(card);
            }
            else if("credit".equalsIgnoreCase(tip)){
                CardBancar card = new CardBancar(numar, "1234", LocalDate.now().plusYears(4), CardBancar.TipCard.CREDIT, cont, false);
                banca.adaugaCard(card);
            }
            else throw new IllegalArgumentException("Tip card invalid. Folositi: debit / credit");
        } catch (InvalidPINException e) {
            throw new IllegalStateException("Eroare la generarea cardului: " + e.getMessage());
        }
    }

    public void efectueazaTranzactie(ContBancar contSursa, double suma, String tip, ContBancar contDestinatar) throws InsufficientFundsException {
        if (contSursa == null || suma <= 0) throw new IllegalArgumentException("Date invalide");

        Tranzactie t = null;
        if ("depunere".equalsIgnoreCase(tip)) {
            contSursa.depunere(suma);
            t = contSursa.getIstoricTranzactii().getUltimaTranzactie();
        } else if ("retragere".equalsIgnoreCase(tip)) {
            contSursa.retragere(suma);
            t = contSursa.getIstoricTranzactii().getUltimaTranzactie();
        } else if ("transfer".equalsIgnoreCase(tip) && contDestinatar != null) {
            contSursa.transfer(contDestinatar, suma);
            t = contSursa.getIstoricTranzactii().getUltimaTranzactie();
        }

        if (t != null) t.genereazaComision();
    }

    public ExtrasDeCont genereazaExtras(ContBancar cont, LocalDate start, LocalDate end) {
        if (cont == null) throw new IllegalArgumentException("Cont invalid");

        List<Tranzactie> tranzactii = cont.getIstoricTranzactii()
                .getTranzactiiInPerioda(start, end);

        double soldSfarsit = cont.getSold();

        double totalIntrari = tranzactii.stream()
                .filter(t -> t.tipTranzactie() == TipTranzactie.DEPUNERE
                        || (t.tipTranzactie() == TipTranzactie.TRANSFER
                        && t.contDestinatar() == cont))
                .mapToDouble(Tranzactie::suma)
                .sum();

        double totalIesiri = tranzactii.stream()
                .filter(t -> t.tipTranzactie() == TipTranzactie.RETRAGERE
                        || (t.tipTranzactie() == TipTranzactie.TRANSFER
                        && t.contSursa() == cont))
                .mapToDouble(Tranzactie::suma)
                .sum();

        double soldInceput = soldSfarsit - totalIntrari + totalIesiri;

        LocalDate[] perioada = {start, end};
        ExtrasDeCont extras = new ExtrasDeCont(perioada, tranzactii, cont, soldInceput, soldSfarsit);
        extras.genereazaExtrasPDF();
        return extras;
    }

    public void afiseazaIstoricTranzactii(ContBancar cont) {
        if (cont == null) return;
        System.out.println(cont.getIstoricTranzactii().getTranzactiiRecente(10));
    }

    public void stergeContBancar(ContBancar cont) {
        if (cont == null || cont.getSold() > 0) throw new IllegalArgumentException("Nu se poate sterge cont cu sold pozitiv sau inexistent.");
        banca.stergeCont(cont);
    }

    public ContBancar cautaContDupaIBAN(String iban) {
        if (iban == null || iban.isBlank()) return null;
        return banca.getContByIBAN(iban);
    }
    public List<ContBancar> listeazaToateConturile() {
        return banca.getListaConturi();
    }
}
