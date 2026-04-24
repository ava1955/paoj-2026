package com.pao.proiect.aplicatiebancara.model;

import java.util.*;

public class Banca {

    private String numeBanca;
    private List<Client> listaClienti;
    private List<ContBancar> listaConturi;
    private List<CardBancar> listaCarduri;
    private Map<String, Client> clientiByCNP;
    private Map<String, ContBancar> conturiByIBAN;
    private Set<ClientPremium> clientiPremium;
    private Set<Integer> idsClienti;
    private Map<String, CardBancar> carduriByNumar;

    public Banca(String numeBanca, List<Client> listaClienti, List<ContBancar> listaConturi, List<CardBancar> listaCarduri) {
        this.numeBanca = numeBanca;
        this.listaClienti = listaClienti != null ? listaClienti : new ArrayList<>();
        this.listaConturi = listaConturi != null ? listaConturi : new ArrayList<>();
        this.listaCarduri = listaCarduri != null ? listaCarduri : new ArrayList<>();
        this.clientiByCNP = new HashMap<>();
        this.conturiByIBAN = new HashMap<>();
        this.clientiPremium = new TreeSet<>(Comparator.comparing(Client::getNume).thenComparing(Client::getPrenume));
        this.idsClienti = new HashSet<>();
        this.carduriByNumar = new HashMap<>();
        for (Client c : this.listaClienti) {
            clientiByCNP.put(c.getCNP(), c);
            idsClienti.add(c.IDClient);
            if (c instanceof ClientPremium cp) clientiPremium.add(cp);
        }
        for (ContBancar c : this.listaConturi) {
            conturiByIBAN.put(c.getIBAN(), c);
        }
    }

    public void adaugaClient(Client client) {
        if (client == null) return;
        if (clientiByCNP.containsKey(client.getCNP()))
            throw new IllegalArgumentException("Exista deja un client cu CNP-ul: " + client.getCNP());
        if (idsClienti.contains(client.IDClient))
            throw new IllegalArgumentException("Exista deja un client cu ID-ul: " + client.IDClient);
        listaClienti.add(client);
        clientiByCNP.put(client.getCNP(), client);
        idsClienti.add(client.IDClient);
        if (client instanceof ClientPremium cp) clientiPremium.add(cp);
    }

    public void adaugaCont(ContBancar cont) {
        if (cont == null) return;
        if (conturiByIBAN.containsKey(cont.getIBAN()))
            throw new IllegalArgumentException("Exista deja un cont cu IBAN-ul: " + cont.getIBAN());
        listaConturi.add(cont);
        conturiByIBAN.put(cont.getIBAN(), cont);
    }

    public void adaugaCard(CardBancar card) {
        if (card == null) return;
        if (carduriByNumar.containsKey(card.getNumarCard()))
            throw new IllegalArgumentException("Exista deja un card cu numarul: " + card.getNumarCard());
        listaCarduri.add(card);
        carduriByNumar.put(card.getNumarCard(), card);
    }

    public void stergeCont(ContBancar cont) {
        if (cont != null) {
            listaConturi.remove(cont);
            conturiByIBAN.remove(cont.getIBAN());
        }
    }
    public void stergeClient(Client client) {
        if (client == null) return;
        List<ContBancar> conturiClient = client.getListaConturi();
        listaCarduri.removeIf(card -> {
            if (conturiClient.contains(card.getContAsociat())) {
                carduriByNumar.remove(card.getNumarCard());
                return true;
            }
            return false;
        });

        listaClienti.remove(client);
        clientiByCNP.remove(client.getCNP());
        idsClienti.remove(client.IDClient);
        if (client instanceof ClientPremium cp) clientiPremium.remove(cp);
    }
    public Client cautaClientDupaCNP(String cnp) {
        return clientiByCNP.get(cnp);
    }

    public Client getClientByCNP(String cnp) {
        return clientiByCNP.get(cnp);
    }

    public List<ClientPremium> getClientiPremium() {
        return new ArrayList<>(clientiPremium);
    }

    public List<Client> getListaClienti() {
        return listaClienti;
    }

    public List<ContBancar> getListaConturi() {
        return listaConturi;
    }

    public List<CardBancar> getListaCarduri() {
        return listaCarduri;
    }

    public boolean cardExista(String numarCard) {
        return carduriByNumar.containsKey(numarCard);
    }

    public ContBancar getContByIBAN(String iban) {
        return conturiByIBAN.get(iban);
    }
}