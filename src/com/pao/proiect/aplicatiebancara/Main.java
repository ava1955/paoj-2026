package com.pao.proiect.aplicatiebancara;

import com.pao.proiect.aplicatiebancara.exception.InsufficientFundsException;
import com.pao.proiect.aplicatiebancara.exception.InvalidCNPException;
import com.pao.proiect.aplicatiebancara.model.*;
import com.pao.proiect.aplicatiebancara.repository.*;
import com.pao.proiect.aplicatiebancara.service.BancaService;
import com.pao.proiect.aplicatiebancara.service.CardService;
import com.pao.proiect.aplicatiebancara.service.ClientService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.pao.proiect.aplicatiebancara.model.*;
import com.pao.proiect.aplicatiebancara.repository.ClientRepository;
import com.pao.proiect.aplicatiebancara.repository.ContBancarRepository;
import com.pao.proiect.aplicatiebancara.repository.CardBancarRepository;
public class Main {

    static Scanner       sc;
    static BancaService  bancaService;
    static ClientService clientService;
    static CardService   cardService;

    public static void main(String[] args) {
        sc = new Scanner(System.in);

        System.out.println("Se incarca datele din baza de date...");
        Banca banca = incarcaBanca("Banca");

        bancaService  = BancaService.getInstance(banca);
        clientService = ClientService.getInstance(banca);
        cardService   = CardService.getInstance(banca);

        while (true) {
            afiseazaMeniu();
            String optiune = sc.nextLine().trim();
            switch (optiune) {
                case "1"  -> adaugaClient();
                case "2"  -> creeazaCont();
                case "3"  -> emiteCard();
                case "4"  -> efectueazaTranzactie();
                case "5"  -> genereazaExtras();
                case "6"  -> afiseazaIstoric();
                case "7"  -> cautaClient();
                case "8"  -> cautaCont();
                case "9"  -> listeazaClienti();
                case "10" -> listeazaPremium();
                case "11" -> soldTotal();
                case "12" -> stergeCont();
                case "13" -> eliminaClient();
                case "14" -> listeazaCarduri();
                case "15" -> blocheazaCard();
                case "16" -> listeazaConturi();
                case "0"  -> { System.out.println("La revedere!"); return; }
                default   -> System.out.println("Optiune invalida.");
            }
        }
    }

    private static Banca incarcaBanca(String numeBanca) {
        try {
            ClientRepository clientRepo = new ClientRepository();
            ContBancarRepository contRepo = new ContBancarRepository();
            CardBancarRepository cardRepo = new CardBancarRepository();
            TranzactieRepository tranzactieRepo = new TranzactieRepository();

            List<Client> clienti = clientRepo.findAll();
            Map<String, Client> clientMap = new HashMap<>();
            for (Client c : clienti) clientMap.put(c.getCNP(), c);

            List<ContBancar> conturi = contRepo.findAll(clientMap);
            Map<String, ContBancar> contMap = new HashMap<>();
            for (ContBancar co : conturi) {
                contMap.put(co.getIBAN(), co);
                co.getTitular().adaugaCont(co);
            }

            List<CardBancar> carduri = cardRepo.findAll(contMap);
            List<Tranzactie> tranzactii = tranzactieRepo.findAll(contMap);

            for (Tranzactie t : tranzactii) {
                if (t.contSursa() != null) {
                    t.contSursa().getIstoricTranzactii().adaugaTranzactie(t);
                }
                if (t.contDestinatar() != null && t.contDestinatar() != t.contSursa()) {
                    t.contDestinatar().getIstoricTranzactii().adaugaTranzactie(t);
                }
            }

            System.out.println("[DB] Date incarcate: " + clienti.size() + " clienti, "
                    + conturi.size() + " conturi, " + carduri.size() + " carduri, "
                    + tranzactii.size() + " tranzactii.");

            return new Banca(numeBanca, clienti, conturi, carduri);

        } catch (SQLException e) {
            System.err.println("[DB] Eroare la incarcarea datelor din DB: " + e.getMessage());
            System.err.println("[DB] Se porneste cu date goale (in-memory only).");
            return new Banca(numeBanca, null, null, null);
        }
    }

    static void afiseazaMeniu() {
        System.out.println("""

                 BANCA
                1.  Adauga client
                2.  Creeaza cont bancar
                3.  Emite card bancar
                4.  Efectueaza tranzactie
                5.  Genereaza extras de cont
                6.  Afiseaza istoric tranzactii
                7.  Cauta client (CNP / nume)
                8.  Cauta cont dupa IBAN
                9.  Listeaza toti clientii
                10. Listeaza clienti premium
                11. Sold total client
                12. Sterge cont bancar
                13. Elimina client
                14. Listeaza toate cardurile
                15. Blocheaza card
                16. Listeaza toate conturile
                0.  Iesire
                Alege optiunea: """);
    }

    static void adaugaClient() {
        System.out.print("Tip client (1=Normal, 2=Premium): ");
        String tip = sc.nextLine().trim();
        try {
            System.out.print("CNP: ");       String cnp    = sc.nextLine().trim();
            System.out.print("Nume: ");      String nume   = sc.nextLine().trim();
            System.out.print("Prenume: ");   String pren   = sc.nextLine().trim();
            System.out.print("Adresa: ");    String adresa = sc.nextLine().trim();
            System.out.print("ID client: "); int id = Integer.parseInt(sc.nextLine().trim());

            if (cnp.isBlank() || nume.isBlank() || pren.isBlank()) {
                System.out.println("Eroare: CNP, nume si prenume nu pot fi goale.");
                return;
            }
            Client client = tip.equals("2")
                    ? new ClientPremium(cnp, nume, pren, adresa, LocalDate.now(), id, LocalDate.now())
                    : new Client(cnp, nume, pren, adresa, LocalDate.now(), id, LocalDate.now());
            clientService.adaugaClientNou(client);
            System.out.println("Client adaugat: " + client);
        } catch (InvalidCNPException e) {
            System.out.println("Eroare CNP: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("ID invalid.");
        } catch (IllegalArgumentException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    static void creeazaCont() {
        System.out.print("CNP client: ");
        String cnp = sc.nextLine().trim();
        try {
            Client client = clientService.cautaClientDupaCNP(cnp);
            if (client == null) { System.out.println("Clientul nu a fost gasit."); return; }
            bancaService.creeazaContNou(client);
            List<ContBancar> conturi = client.getListaConturi();
            System.out.println("Cont creat pentru " + client.getNume() + " " + client.getPrenume());
            System.out.println("IBAN: " + conturi.get(conturi.size() - 1).getIBAN());
        } catch (InvalidCNPException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    static void emiteCard() {
        System.out.print("CNP client: "); String cnp  = sc.nextLine().trim();
        System.out.print("IBAN cont: ");  String iban = sc.nextLine().trim();
        System.out.print("Tip card (debit/credit): "); String tip = sc.nextLine().trim();
        try {
            Client client = clientService.cautaClientDupaCNP(cnp);
            if (client == null) { System.out.println("Clientul nu a fost gasit."); return; }
            ContBancar cont = bancaService.cautaContDupaIBAN(iban);
            if (cont == null) { System.out.println("Contul nu a fost gasit."); return; }
            bancaService.emiteCard(client, cont, tip);
            System.out.println("Card emis cu succes.");
        } catch (InvalidCNPException | IllegalArgumentException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    static void efectueazaTranzactie() {
        System.out.print("IBAN cont sursa: ");
        ContBancar cont = bancaService.cautaContDupaIBAN(sc.nextLine().trim());
        if (cont == null) { System.out.println("Contul nu a fost gasit."); return; }

        System.out.print("Tip (depunere/retragere/transfer): ");
        String tip = sc.nextLine().trim();
        System.out.print("Suma: ");
        try {
            double suma = Double.parseDouble(sc.nextLine().trim());
            if (suma <= 0) { System.out.println("Suma trebuie sa fie pozitiva."); return; }

            ContBancar destinatar = null;
            if ("transfer".equalsIgnoreCase(tip)) {
                System.out.print("IBAN cont destinatar: ");
                destinatar = bancaService.cautaContDupaIBAN(sc.nextLine().trim());
                if (destinatar == null) { System.out.println("Contul destinatar nu a fost gasit."); return; }
            }
            bancaService.efectueazaTranzactie(cont, suma, tip, destinatar);
            System.out.println("Tranzactie efectuata. Sold nou: " + cont.getSold());
        } catch (NumberFormatException e) {
            System.out.println("Suma invalida.");
        } catch (InsufficientFundsException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    static void genereazaExtras() {
        System.out.print("IBAN cont: ");
        ContBancar cont = bancaService.cautaContDupaIBAN(sc.nextLine().trim());
        if (cont == null) { System.out.println("Contul nu a fost gasit."); return; }
        bancaService.genereazaExtras(cont, LocalDate.now().minusMonths(1), LocalDate.now());
    }

    static void afiseazaIstoric() {
        System.out.print("IBAN cont: ");
        ContBancar cont = bancaService.cautaContDupaIBAN(sc.nextLine().trim());
        if (cont == null) { System.out.println("Contul nu a fost gasit."); return; }
        bancaService.afiseazaIstoricTranzactii(cont);
        System.out.println("\n Raport DB: Tranzactii cu date conturi si titulari");
        bancaService.afiseazaTranzactiiComplete();
    }

    static void cautaClient() {
        System.out.print("Cauta dupa (1=CNP, 2=Nume): ");
        String opt = sc.nextLine().trim();
        if (opt.equals("1")) {
            System.out.print("CNP: ");
            try {
                Client c = clientService.cautaClientPublicDupaCNP(sc.nextLine().trim());
                System.out.println(c != null ? c : "Clientul nu a fost gasit.");
            } catch (InvalidCNPException e) {
                System.out.println("Eroare: " + e.getMessage());
            }
        } else if (opt.equals("2")) {
            System.out.print("Nume: ");
            List<Client> rezultat = clientService.cautaClientDupaNume(sc.nextLine().trim());
            if (rezultat.isEmpty()) System.out.println("Niciun client gasit.");
            else rezultat.forEach(System.out::println);
        } else {
            System.out.println("Optiune invalida.");
        }
    }

    static void cautaCont() {
        System.out.print("IBAN: ");
        ContBancar cont = bancaService.cautaContSiAudit(sc.nextLine().trim());
        System.out.println(cont != null ? cont : "Contul nu a fost gasit.");
    }

    static void listeazaClienti() {
        List<Client> clienti = clientService.listeazaTotiClientii();
        if (clienti.isEmpty()) { System.out.println("Nu exista clienti."); return; }
        clienti.forEach(System.out::println);
        System.out.println("\n Raport DB: Clienti cu numarul de conturi ");
        clientService.afiseazaClientiCuNrConturi();
    }

    static void listeazaPremium() {
        List<ClientPremium> premium = clientService.listeazaClientiPremium();
        if (premium.isEmpty()) { System.out.println("Nu exista clienti premium."); return; }
        premium.forEach(c -> System.out.println(c.getNume() + " " + c.getPrenume()
                + " | Limita: " + c.getLimitaMaximaTranzactii()
                + " | Dobanda bonus: " + c.getDobandaBonus()));
    }

    static void soldTotal() {
        System.out.print("CNP client: ");
        try {
            Client client = clientService.cautaClientDupaCNP(sc.nextLine().trim());
            if (client == null) { System.out.println("Clientul nu a fost gasit."); return; }
            System.out.println("Sold total: " + clientService.calculeazaSoldTotalClient(client) + " RON");
        } catch (InvalidCNPException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    static void stergeCont() {
        System.out.print("IBAN cont: ");
        ContBancar cont = bancaService.cautaContDupaIBAN(sc.nextLine().trim());
        if (cont == null) { System.out.println("Contul nu a fost gasit."); return; }
        try {
            bancaService.stergeContBancar(cont);
            System.out.println("Contul a fost sters.");
        } catch (IllegalArgumentException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    static void eliminaClient() {
        System.out.print("CNP client: ");
        try {
            Client client = clientService.cautaClientDupaCNP(sc.nextLine().trim());
            if (client == null) { System.out.println("Clientul nu a fost gasit."); return; }
            clientService.eliminaClient(client);
            System.out.println("Clientul a fost eliminat.");
        } catch (InvalidCNPException e) {
            System.out.println("Eroare CNP: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    static void listeazaCarduri() {
        List<CardBancar> carduri = cardService.listeazaToateCardurile();
        if (carduri.isEmpty()) { System.out.println("Nu exista carduri."); return; }
        carduri.forEach(c -> System.out.println("Card: " + (c.esteBlocat() ? "[BLOCAT]" : "[ACTIV]")
                + " | Tip: " + c.getTipCard()
                + " | Numar: " + c.getNumarCard()
                + " | PIN: " + c.getPIN()));
        System.out.println("\n Raport DB: Carduri cu detalii cont si titular ");
        cardService.afiseazaCarduriCuDateContSiClient();
    }

    static void blocheazaCard() {
        System.out.print("Numar card: ");
        String numar = sc.nextLine().trim();
        CardBancar card = cardService.getCardByNumar(numar);
        if (card == null) { System.out.println("Cardul nu a fost gasit."); return; }
        if (card.esteBlocat()) { System.out.println("Cardul este deja blocat."); return; }
        try {
            cardService.blocheazaCard(card);
            System.out.println("Cardul a fost blocat.");
        } catch (IllegalArgumentException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    static void listeazaConturi() {
        List<ContBancar> conturi = bancaService.listeazaToateConturile();
        if (conturi.isEmpty()) { System.out.println("Nu exista conturi."); return; }
        conturi.forEach(System.out::println);
        System.out.println("\n Raport DB: Conturi cu date titular ");
        bancaService.afiseazaConturiCuDateTitular();
    }
}