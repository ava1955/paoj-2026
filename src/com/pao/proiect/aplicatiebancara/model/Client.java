package com.pao.proiect.aplicatiebancara.model;
import com.pao.proiect.aplicatiebancara.exception.InvalidCNPException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Client extends Persoana {
    protected int IDClient;
    protected LocalDate dataInregistrare;
    protected List<ContBancar> listaConturi;
    protected String statusClient;

    public Client(String CNP, String nume, String prenume, String adresa,
                  LocalDate dataNastere, int IDClient, LocalDate dataInregistrare) throws InvalidCNPException {
        super(CNP, nume, prenume, adresa, dataNastere);
        this.IDClient = IDClient;
        this.dataInregistrare = dataInregistrare;
        this.listaConturi = new ArrayList<>();
        this.statusClient = "ACTIV";
    }

    public void adaugaCont(ContBancar cont) {
        if (cont != null) listaConturi.add(cont);
    }

    public List<ContBancar> getListaConturi() {
        return List.copyOf(listaConturi);
    }

    public String getStatusClient() {
        return statusClient;
    }

    public void setStatusClient(String status) {
        this.statusClient = status;
    }

    @Override
    public String getRol() {
        return "Client";
    }

    @Override
    public String toString() {
        return "Client{" + "IDClient='" + IDClient + '\'' + ", numeComplet='" + getNume() + " " + getPrenume() + '\'' + ", status=" + statusClient + ", conturi=" + listaConturi.size() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client client)) return false;
        return Objects.equals(CNP, client.CNP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CNP);
    }
}