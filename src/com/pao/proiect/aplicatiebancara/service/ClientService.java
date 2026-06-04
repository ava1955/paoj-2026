package com.pao.proiect.aplicatiebancara.service;

import com.pao.proiect.aplicatiebancara.model.Banca;
import com.pao.proiect.aplicatiebancara.model.Client;
import com.pao.proiect.aplicatiebancara.model.ClientPremium;
import com.pao.proiect.aplicatiebancara.model.ContBancar;
import com.pao.proiect.aplicatiebancara.exception.InvalidCNPException;
import com.pao.proiect.aplicatiebancara.repository.ClientRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientService {

    private static ClientService instance;
    private final Banca banca;
    private final AuditService audit = AuditService.getInstance();
    private final ClientRepository clientRepo = new ClientRepository();

    private ClientService(Banca banca) { this.banca = banca; }

    public static ClientService getInstance(Banca banca) {
        if (instance == null) instance = new ClientService(banca);
        return instance;
    }

    public void adaugaClientNou(Client client) {
        audit.logAction(AuditService.ADAUGA_CLIENT);
        if (client == null) throw new IllegalArgumentException("Clientul nu poate fi null");
        banca.adaugaClient(client);
        try {
            clientRepo.save(client);
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la salvarea clientului: " + e.getMessage());
        }
    }

    public Client cautaClientDupaCNP(String cnp) throws InvalidCNPException {
        if (cnp == null || cnp.isBlank()) throw new InvalidCNPException("CNP-ul nu poate fi gol.");
        return banca.getClientByCNP(cnp);
    }

    public Client cautaClientPublicDupaCNP(String cnp) throws InvalidCNPException {
        audit.logAction(AuditService.CAUTA_CLIENT);
        return cautaClientDupaCNP(cnp);
    }

    public List<Client> cautaClientDupaNume(String nume) {
        audit.logAction(AuditService.CAUTA_CLIENT);
        if (nume == null || nume.isBlank()) return List.of();
        List<Client> rezultat = new ArrayList<>();
        for (Client c : banca.getListaClienti()) {
            String numeComplet = (c.getNume() + " " + c.getPrenume()).toLowerCase();
            if (numeComplet.contains(nume.toLowerCase())) rezultat.add(c);
        }
        return rezultat;
    }

    public List<Client> listeazaTotiClientii() {
        audit.logAction(AuditService.LISTEAZA_CLIENTI);
        return banca.getListaClienti();
    }

    public List<ClientPremium> listeazaClientiPremium() {
        audit.logAction(AuditService.LISTEAZA_PREMIUM);
        return banca.getClientiPremium();
    }

    public double calculeazaSoldTotalClient(Client client) {
        audit.logAction(AuditService.SOLD_TOTAL);
        if (client == null) return 0;
        return client.getListaConturi().stream().mapToDouble(ContBancar::getSold).sum();
    }

    public void eliminaClient(Client client) {
        audit.logAction(AuditService.ELIMINA_CLIENT);
        if (client == null) return;
        for (ContBancar c : client.getListaConturi()) {
            if (c.getSold() > 0)
                throw new IllegalArgumentException(
                        "Nu se poate sterge client cu cont cu sold pozitiv.");
        }
        banca.stergeClient(client);
        try {
            clientRepo.delete(client.getIDClient());
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la stergerea clientului din DB: " + e.getMessage());
        }
    }

    public void afiseazaClientiCuNrConturi() {
        try {
            List<Map<String, Object>> rows = clientRepo.findClientiCuNrConturi();
            if (rows.isEmpty()) { System.out.println("Nu exista clienti in baza de date."); return; }
            System.out.printf("%-5s %-13s %-30s %-10s%n", "ID", "CNP", "Nume complet", "Nr. Conturi");
            System.out.println("-".repeat(62));
            for (var row : rows) {
                System.out.printf("%-5s %-13s %-30s %-10s%n",
                        row.get("idClient"), row.get("cnp"),
                        row.get("nume") + " " + row.get("prenume"),
                        row.get("nrConturi"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la interogarea JOIN clienti+conturi: " + e.getMessage());
        }
    }
}