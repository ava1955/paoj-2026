package com.pao.proiect.aplicatiebancara.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

public class AuditService {

    private static volatile AuditService instance;
    private static final String AUDIT_FILE = "audit.csv";
    private final ReentrantLock lock = new ReentrantLock();

    public static final String ADAUGA_CLIENT           = "adauga_client";
    public static final String CREEAZA_CONT            = "creeaza_cont";
    public static final String EMITE_CARD              = "emite_card";
    public static final String EFECTUEAZA_TRANZACTIE   = "efectueaza_tranzactie";
    public static final String GENEREAZA_EXTRAS        = "genereaza_extras";
    public static final String AFISEAZA_ISTORIC        = "afiseaza_istoric";
    public static final String CAUTA_CLIENT            = "cauta_client";
    public static final String CAUTA_CONT              = "cauta_cont";
    public static final String LISTEAZA_CLIENTI        = "listeaza_clienti";
    public static final String LISTEAZA_PREMIUM        = "listeaza_clienti_premium";
    public static final String SOLD_TOTAL              = "calculeaza_sold_total";
    public static final String STERGE_CONT             = "sterge_cont";
    public static final String ELIMINA_CLIENT          = "elimina_client";
    public static final String LISTEAZA_CARDURI        = "listeaza_carduri";
    public static final String BLOCHEAZA_CARD          = "blocheaza_card";
    public static final String LISTEAZA_CONTURI        = "listeaza_conturi";

    private AuditService() {
    }

    public static AuditService getInstance() {
        if (instance == null) {
            synchronized (AuditService.class) {
                if (instance == null) {
                    instance = new AuditService();
                }
            }
        }
        return instance;
    }

    public void logAction(String actionName) {
        lock.lock();
        try (PrintWriter pw = new PrintWriter(new FileWriter(AUDIT_FILE, true))) {
            pw.println(actionName + "," + LocalDateTime.now());
        }
        catch (IOException e) {
            System.err.println("[AUDIT] Eroare la scriere in " + AUDIT_FILE + ": " + e.getMessage());
        }
        finally {
            lock.unlock();
        }
    }
}
