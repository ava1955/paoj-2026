package com.pao.proiect.aplicatiebancara;

import com.pao.proiect.aplicatiebancara.exception.InsufficientFundsException;
import com.pao.proiect.aplicatiebancara.exception.InvalidCNPException;
import com.pao.proiect.aplicatiebancara.model.*;
import com.pao.proiect.aplicatiebancara.service.BancaService;
import com.pao.proiect.aplicatiebancara.service.CardService;
import com.pao.proiect.aplicatiebancara.service.ClientService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static BancaService bancaService;
    static ClientService clientService;
    static CardService cardService;

    public static void main(String[] args) {
        Banca banca = new Banca("Banca", null, null, null);
        bancaService = BancaService.getInstance(banca);
        clientService = ClientService.getInstance(banca);
        cardService = CardService.getInstance(banca);

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
                case "0"  -> { System.out.println("La revedere!"); return; }
                default   -> System.out.println("Optiune invalida.");
            }
        }
    }

    static void afiseazaMeniu() {
        System.out.println("""
                \n BANCA
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
                0.  Iesire
                Alege optiunea: """);
    }

    //  1 
    static void adaugaClient() {
        System.out.print("Tip client (1=Normal, 2=Premium): ");
        String tip = sc.nextLine().trim();
        try {
            System.out.print("CNP: ");        String cnp    = sc.nextLine().trim();
            System.out.print("Nume: ");       String nume   = sc.nextLine().trim();
            System.out.print("Prenume: ");    String pren   = sc.nextLine().trim();
            System.out.print("Adresa: ");     String adresa = sc.nextLine().trim();
            System.out.print("ID client: ");  int id = Integer.parseInt(sc.nextLine().trim());

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

    //  2 
    static void creeazaCont() {
        System.out.print("CNP client: ");
        String cnp = sc.nextLine().trim();
        try {
            Client client = clientService.cautaClientDupaCNP(cnp);
            if (client == null) { System.out.println("Clientul nu a fost gasit."); return; }
            bancaService.creeazaContNou(client);
            System.out.println("Cont creat pentru " + client.getNume() + " " + client.getPrenume());
            System.out.println("IBAN: " + client.getListaConturi().get(client.getListaConturi().size() - 1).getIBAN());
        } catch (InvalidCNPException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    //  3 
    static void emiteCard() {
        System.out.print("CNP client: ");
        String cnp = sc.nextLine().trim();
        System.out.print("IBAN cont: ");
        String iban = sc.nextLine().trim();
        try {
            Client client = clientService.cautaClientDupaCNP(cnp);
            if (client == null) { System.out.println("Clientul nu a fost gasit."); return; }
            ContBancar cont = bancaService.cautaContDupaIBAN(iban);
            if (cont == null) { System.out.println("Contul nu a fost gasit."); return; }
            bancaService.emiteCard(client, cont);
            System.out.println("Card emis cu succes.");
        } catch (InvalidCNPException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    //  4 
    static void efectueazaTranzactie() {
        System.out.print("IBAN cont sursa: ");
        String iban = sc.nextLine().trim();
        ContBancar cont = bancaService.cautaContDupaIBAN(iban);
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
                String ibanDest = sc.nextLine().trim();
                destinatar = bancaService.cautaContDupaIBAN(ibanDest);
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

    //  5 
    static void genereazaExtras() {
        System.out.print("IBAN cont: ");
        String iban = sc.nextLine().trim();
        ContBancar cont = bancaService.cautaContDupaIBAN(iban);
        if (cont == null) { System.out.println("Contul nu a fost gasit."); return; }
        bancaService.genereazaExtras(cont, LocalDate.now().minusMonths(1), LocalDate.now());
    }

    //  6 
    static void afiseazaIstoric() {
        System.out.print("IBAN cont: ");
        String iban = sc.nextLine().trim();
        ContBancar cont = bancaService.cautaContDupaIBAN(iban);
        if (cont == null) { System.out.println("Contul nu a fost gasit."); return; }
        bancaService.afiseazaIstoricTranzactii(cont);
    }

    //  7 
    static void cautaClient() {
        System.out.print("Cauta dupa (1=CNP, 2=Nume): ");
        String opt = sc.nextLine().trim();
        if (opt.equals("1")) {
            System.out.print("CNP: ");
            try {
                Client c = clientService.cautaClientDupaCNP(sc.nextLine().trim());
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

    //  8 
    static void cautaCont() {
        System.out.print("IBAN: ");
        ContBancar cont = bancaService.cautaContDupaIBAN(sc.nextLine().trim());
        System.out.println(cont != null ? cont : "Contul nu a fost gasit.");
    }

    //  9 
    static void listeazaClienti() {
        List<Client> clienti = clientService.listeazaTotiClientii();
        if (clienti.isEmpty()) { System.out.println("Nu exista clienti."); return; }
        clienti.forEach(System.out::println);
    }

    //  10 
    static void listeazaPremium() {
        List<ClientPremium> premium = clientService.listeazaClientiPremium();
        if (premium.isEmpty()) { System.out.println("Nu exista clienti premium."); return; }
        premium.forEach(c -> System.out.println(c.getNume() + " " + c.getPrenume()
                + " | Limita: " + c.getLimitaMaximaTranzactii()
                + " | Dobanda bonus: " + c.getDobandaBonus()));
    }

    //  11 
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

    //  12 
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

    //  13 
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

    //  14 
    static void listeazaCarduri() {
        List<CardBancar> carduri = cardService.listeazaToateCardurile();
        if (carduri.isEmpty()) { System.out.println("Nu exista carduri."); return; }
        carduri.forEach(c -> System.out.println("Card: " + (c.esteBlocat() ? "[BLOCAT]" : "[ACTIV]")
                + " | Tip: " + c.getTipCard()
                + " | PIN: " + c.getPIN()));
    }

    //  15
    static void blocheazaCard() {
        System.out.print("Numar card: ");
        String numar = sc.nextLine().trim();
        List<CardBancar> carduri = cardService.listeazaToateCardurile();
        CardBancar card = carduri.stream()
                .filter(c -> c.getNumarCard().equals(numar))
                .findFirst()
                .orElse(null);
        if (card == null) { System.out.println("Cardul nu a fost gasit."); return; }
        if (card.esteBlocat()) { System.out.println("Cardul este deja blocat."); return; }
        try {
            cardService.blocheazaCard(card);
            System.out.println("Cardul a fost blocat.");
        } catch (IllegalArgumentException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }
}