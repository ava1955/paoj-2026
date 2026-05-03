package com.pao.laboratory09.exercise1;

import java.io.*;
import java.util.*;

public class Main {
    private static final String OUTPUT_FILE = "output/lab09_ex1.ser";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        File dir = new File("output");
        if (!dir.exists()) dir.mkdirs();

        if (!sc.hasNextInt()) return;
        int n = sc.nextInt();
        List<Tranzactie> listaInitiala = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            int id = sc.nextInt();
            double suma = sc.nextDouble();
            String data = sc.next();
            String contSursa = sc.next();
            String contDestinatie = sc.next();
            TipTranzactie tip = TipTranzactie.valueOf(sc.next());

            Tranzactie t = new Tranzactie(id, suma, data, contSursa, contDestinatie, tip);
            t.setNote("procesat");
            listaInitiala.add(t);
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(OUTPUT_FILE))) {
            oos.writeObject(listaInitiala);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Tranzactie> listaDeserializata = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(OUTPUT_FILE))) {
            listaDeserializata = (List<Tranzactie>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        while (sc.hasNext()) {
            String comanda = sc.next();
            switch (comanda) {
                case "LIST":
                    for (Tranzactie t : listaDeserializata) {
                        System.out.println(t);
                    }
                    break;

                case "FILTER":
                    String prefix = sc.next();
                    boolean found = false;
                    for (Tranzactie t : listaDeserializata) {
                        if (t.getData().startsWith(prefix)) {
                            System.out.println(t);
                            found = true;
                        }
                    }
                    if (!found) {
                        System.out.println("Niciun rezultat.");
                    }
                    break;

                case "NOTE":
                    int searchId = sc.nextInt();
                    Tranzactie gasita = null;
                    for (Tranzactie t : listaDeserializata) {
                        if (t.getId() == searchId) {
                            gasita = t;
                            break;
                        }
                    }
                    if (gasita != null) {
                        System.out.println("NOTE[" + searchId + "]: " + gasita.getNote());
                    } else {
                        System.out.println("NOTE[" + searchId + "]: not found");
                    }
                    break;
            }
        }
        sc.close();
    }
}