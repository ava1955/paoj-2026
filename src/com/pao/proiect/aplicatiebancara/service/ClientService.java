package com.pao.proiect.aplicatiebancara.service;

import com.pao.proiect.aplicatiebancara.model.Banca;
import com.pao.proiect.aplicatiebancara.model.Client;
import com.pao.proiect.aplicatiebancara.model.ClientPremium;
import com.pao.proiect.aplicatiebancara.exception.InvalidCNPException;
import com.pao.proiect.aplicatiebancara.model.ContBancar;

import java.util.ArrayList;
import java.util.List;

public class ClientService {
    private static ClientService instance;
    private final Banca banca;

    private ClientService(Banca banca) {
        this.banca = banca;
    }

    public static ClientService getInstance(Banca banca) {
        if (instance == null) instance = new ClientService(banca);
        return instance;
    }

    public void adaugaClientNou(Client client) {
        if (client == null) throw new IllegalArgumentException("Clientul nu poate fi null");
        banca.adaugaClient(client);
    }

    public Client cautaClientDupaCNP(String cnp) throws InvalidCNPException {
        if (cnp == null || cnp.isBlank()) {
            throw new InvalidCNPException("CNP-ul nu poate fi gol.");
        }

        return banca.getClientByCNP(cnp);
    }

    public List<Client> cautaClientDupaNume(String nume) {
        if (nume == null || nume.isBlank()) return List.of();
        List<Client> rezultat = new ArrayList<>();
        for (Client c : banca.getListaClienti()) {
            String numeComplet = (c.getNume() + " " + c.getPrenume()).toLowerCase();
            if (numeComplet.contains(nume.toLowerCase())) {
                rezultat.add(c);
            }
        }
        return rezultat;
    }
    public List<Client> listeazaTotiClientii() {
        return banca.getListaClienti();
    }

    public List<ClientPremium> listeazaClientiPremium() {
        return banca.getClientiPremium();
    }

    public double calculeazaSoldTotalClient(Client client) {
        if (client == null) return 0;

        return client.getListaConturi().stream()
                .mapToDouble(ContBancar::getSold)
                .sum();
    }

    public void eliminaClient(Client client) {
        if (client == null) return;
        List<ContBancar> listaConturi =client.getListaConturi();
        for (ContBancar c : listaConturi){
            if(c == null || c.getSold() > 0){
                throw new IllegalArgumentException("Nu se poate sterge client cu cont cu sold pozitiv sau inexistent.");
            }
        }
        banca.stergeClient(client);
    }
}
