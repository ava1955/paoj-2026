package com.pao.proiect.aplicatiebancara.service;

import com.pao.proiect.aplicatiebancara.exception.InvalidPINException;
import com.pao.proiect.aplicatiebancara.model.*;
import com.pao.proiect.aplicatiebancara.exception.InsufficientFundsException;
import com.pao.proiect.aplicatiebancara.repository.CardBancarRepository;
import com.pao.proiect.aplicatiebancara.repository.ContBancarRepository;
import com.pao.proiect.aplicatiebancara.repository.TranzactieRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BancaService {

    private static BancaService instance;
    private final Banca banca;
    private int contorConturi;
    private final AuditService audit = AuditService.getInstance();

    private final ContBancarRepository contRepo      = new ContBancarRepository();
    private final TranzactieRepository tranzactieRepo = new TranzactieRepository();
    private final CardBancarRepository  cardRepo      = new CardBancarRepository();

    private BancaService(Banca banca) {
        this.banca = banca;
        this.contorConturi = (int) banca.getListaConturi().stream()
                .map(ContBancar::getIBAN)
                .filter(iban -> iban.startsWith("RO"))
                .mapToLong(iban -> {
                    try { return Long.parseLong(iban.substring(2)) - 1_000_000_000L + 1; }
                    catch (NumberFormatException e) { return 0L; }
                })
                .max().orElse(0L);
    }

    public static BancaService getInstance(Banca banca) {
        if (instance == null) instance = new BancaService(banca);
        return instance;
    }

    public void creeazaContNou(Client client) {
        audit.logAction(AuditService.CREEAZA_CONT);
        if (client == null) throw new IllegalArgumentException("Client invalid");
        String iban = "RO" + (1_000_000_000L + contorConturi++);
        ContBancar cont = new ContBancar(iban, 0, "RON", LocalDate.now(), client);
        banca.adaugaCont(cont);
        client.adaugaCont(cont);
        try {
            contRepo.save(cont);
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la salvarea contului: " + e.getMessage());
        }
    }

    public void emiteCard(Client client, ContBancar cont, String tip) {
        audit.logAction(AuditService.EMITE_CARD);
        if (client == null || cont == null) throw new IllegalArgumentException("Date invalide");
        try {
            String numar;
            do {
                numar = String.valueOf(400_000_000L + (long)(Math.random() * 999_999_999L));
            } while (banca.cardExista(numar));

            CardBancar card;
            if ("debit".equalsIgnoreCase(tip)) {
                card = new CardBancar(numar, "1234", LocalDate.now().plusYears(4),
                        CardBancar.TipCard.DEBIT, cont, false);
            } else if ("credit".equalsIgnoreCase(tip)) {
                card = new CardBancar(numar, "1234", LocalDate.now().plusYears(4),
                        CardBancar.TipCard.CREDIT, cont, false);
            } else {
                throw new IllegalArgumentException("Tip card invalid. Folositi: debit / credit");
            }
            banca.adaugaCard(card);
            try {
                cardRepo.save(card);
            } catch (SQLException e) {
                System.err.println("[DB] Eroare la salvarea cardului: " + e.getMessage());
            }
        } catch (InvalidPINException e) {
            throw new IllegalStateException("Eroare la generarea cardului: " + e.getMessage());
        }
    }

    public void efectueazaTranzactie(ContBancar contSursa, double suma, String tip,
                                     ContBancar contDestinatar) throws InsufficientFundsException {
        audit.logAction(AuditService.EFECTUEAZA_TRANZACTIE);
        if (contSursa == null || suma <= 0) throw new IllegalArgumentException("Date invalide");

        Tranzactie t = null;

        if ("depunere".equalsIgnoreCase(tip)) {
            contSursa.depunere(suma);
            t = contSursa.getIstoricTranzactii().getUltimaTranzactie();
            persistSimplaTranzactie(t);
        } else if ("retragere".equalsIgnoreCase(tip)) {
            contSursa.retragere(suma);
            t = contSursa.getIstoricTranzactii().getUltimaTranzactie();
            persistSimplaTranzactie(t);
        } else if ("transfer".equalsIgnoreCase(tip) && contDestinatar != null) {
            contSursa.transfer(contDestinatar, suma);
            t = contSursa.getIstoricTranzactii().getUltimaTranzactie();
            if (t != null) {
                try {
                    tranzactieRepo.efectueazaTransferTranzactional(t);
                } catch (SQLException e) {
                    System.err.println("[DB] Eroare la persistarea transferului: " + e.getMessage());
                }
            }
        }
        if (t != null) t.genereazaComision();
    }

    private void persistSimplaTranzactie(Tranzactie t) {
        if (t == null) return;
        try {
            tranzactieRepo.salveazaSiActualizeazaSold(t);
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la persistarea tranzactiei: " + e.getMessage());
        }
    }

    public ExtrasDeCont genereazaExtras(ContBancar cont, LocalDate start, LocalDate end) {
        audit.logAction(AuditService.GENEREAZA_EXTRAS);
        if (cont == null) throw new IllegalArgumentException("Cont invalid");

        List<Tranzactie> tranzactii = cont.getIstoricTranzactii().getTranzactiiInPerioda(start, end);
        double soldSfarsit  = cont.getSold();
        double totalIntrari = tranzactii.stream()
                .filter(t -> t.tipTranzactie() == TipTranzactie.DEPUNERE
                        || (t.tipTranzactie() == TipTranzactie.TRANSFER && t.contDestinatar() == cont))
                .mapToDouble(Tranzactie::suma).sum();
        double totalIesiri = tranzactii.stream()
                .filter(t -> t.tipTranzactie() == TipTranzactie.RETRAGERE
                        || (t.tipTranzactie() == TipTranzactie.TRANSFER && t.contSursa() == cont))
                .mapToDouble(Tranzactie::suma).sum();
        double soldInceput = soldSfarsit - totalIntrari + totalIesiri;

        ExtrasDeCont extras = new ExtrasDeCont(
                new LocalDate[]{start, end}, tranzactii, cont, soldInceput, soldSfarsit);
        extras.genereazaExtrasPDF();
        return extras;
    }

    public void stergeContBancar(ContBancar cont) {
        audit.logAction(AuditService.STERGE_CONT);
        if (cont == null || cont.getSold() > 0)
            throw new IllegalArgumentException("Nu se poate sterge cont cu sold pozitiv sau inexistent.");
        banca.stergeCont(cont);
        try {
            contRepo.delete(cont.getIBAN());
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la stergerea contului din DB: " + e.getMessage());
        }
    }

    public void afiseazaIstoricTranzactii(ContBancar cont) {
        audit.logAction(AuditService.AFISEAZA_ISTORIC);
        if (cont == null) return;
        System.out.println(cont.getIstoricTranzactii().getTranzactiiRecente(10));
    }

    public ContBancar cautaContDupaIBAN(String iban) {
        if (iban == null || iban.isBlank()) return null;
        return banca.getContByIBAN(iban);
    }

    public ContBancar cautaContSiAudit(String iban) {
        audit.logAction(AuditService.CAUTA_CONT);
        return cautaContDupaIBAN(iban);
    }

    public List<ContBancar> listeazaToateConturile() {
        audit.logAction(AuditService.LISTEAZA_CONTURI);
        return banca.getListaConturi();
    }

    public void afiseazaConturiCuDateTitular() {
        try {
            List<java.util.Map<String, Object>> rows = contRepo.findConturiCuDateTitular();
            if (rows.isEmpty()) { System.out.println("Nu exista conturi in baza de date."); return; }
            System.out.printf("%-30s %-12s %-6s %-30s %-10s%n",
                    "IBAN", "Sold", "Valuta", "Titular", "Status");
            System.out.println("-".repeat(90));
            for (var row : rows) {
                System.out.printf("%-30s %-12.2f %-6s %-30s %-10s%n",
                        row.get("iban"), row.get("sold"), row.get("valuta"),
                        row.get("numeClient"), row.get("status"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la interogarea JOIN conturi+clienti: " + e.getMessage());
        }
    }
    public void afiseazaTranzactiiComplete() {
        try {
            List<java.util.Map<String, Object>> rows = tranzactieRepo.findTranzactiiCuDateConturi();
            if (rows.isEmpty()) { System.out.println("Nu exista tranzactii."); return; }
            System.out.printf("%-15s %-10s %-12s %-20s %-25s %-25s%n",
                    "ID", "Suma", "Tip", "Data", "Sursa", "Destinatar");
            System.out.println("-".repeat(110));
            for (var row : rows) {
                System.out.printf("%-15s %-10.2f %-12s %-20s %-25s %-25s%n",
                        row.get("idTranzactie"), row.get("suma"), row.get("tip"),
                        row.get("dataSiOra"), row.get("numeSursa"), row.get("numeDest"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Eroare: " + e.getMessage());
        }
    }
}