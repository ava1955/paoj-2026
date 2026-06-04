package com.pao.proiect.aplicatiebancara.service;

import com.pao.proiect.aplicatiebancara.model.Banca;
import com.pao.proiect.aplicatiebancara.model.CardBancar;
import com.pao.proiect.aplicatiebancara.repository.CardBancarRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CardService {

    private static CardService instance;
    private final Banca banca;
    private final AuditService audit = AuditService.getInstance();
    private final CardBancarRepository cardRepo = new CardBancarRepository();

    private CardService(Banca banca) { this.banca = banca; }

    public static CardService getInstance(Banca banca) {
        if (instance == null) instance = new CardService(banca);
        return instance;
    }

    public void blocheazaCard(CardBancar card) {
        audit.logAction(AuditService.BLOCHEAZA_CARD);
        if (card == null) throw new IllegalArgumentException("Card invalid");
        card.blocheazaCard();
        try {
            cardRepo.update(card);
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la actualizarea cardului in DB: " + e.getMessage());
        }
    }

    public CardBancar getCardByNumar(String numarCard) {
        if (numarCard == null || numarCard.isBlank()) return null;
        return banca.getCardByNumar(numarCard);
    }

    public List<CardBancar> listeazaToateCardurile() {
        audit.logAction(AuditService.LISTEAZA_CARDURI);
        return banca.getListaCarduri();
    }

    public void afiseazaCarduriCuDateContSiClient() {
        try {
            List<Map<String, Object>> rows = cardRepo.findCarduriCuDateContSiClient();
            if (rows.isEmpty()) { System.out.println("Nu exista carduri in baza de date."); return; }
            System.out.printf("%-15s %-8s %-7s %-30s %-12s %-30s%n",
                    "Numar Card", "Tip", "Blocat", "IBAN Cont", "Sold", "Titular");
            System.out.println("-".repeat(105));
            for (var row : rows) {
                System.out.printf("%-15s %-8s %-7s %-30s %-12.2f %-30s%n",
                        row.get("numarCard"), row.get("tipCard"),
                        Boolean.TRUE.equals(row.get("blocat")) ? "DA" : "NU",
                        row.get("iban"), row.get("sold"), row.get("numeClient"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la interogarea JOIN carduri+conturi+clienti: " + e.getMessage());
        }
    }
}