package com.pao.laboratory06.exercise2;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int N = scanner.nextInt();
        List<Colaborator> colaboratori = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            String tipStr = scanner.next();
            TipColaborator tip = TipColaborator.valueOf(tipStr);

            Colaborator c = switch (tip) {
                case CIM -> new CIMColaborator();
                case PFA -> new PFAColaborator();
                case SRL -> new SRLColaborator();
            };

            c.citeste(scanner);
            colaboratori.add(c);
        }

        for (Colaborator c : colaboratori) {
            c.afiseaza();
        }
        System.out.println();

        Colaborator maxColaborator = colaboratori.stream()
                .max(Comparator.comparingDouble(Colaborator::calculeazaVenitNetAnual))
                .orElse(null);

        if (maxColaborator != null) {
            System.out.println("Colaborator cu venit net maxim: " + maxColaborator.getDescriereCuVenit());
        }
        System.out.println();

        System.out.println("Colaboratori persoane juridice:");
        for (Colaborator c : colaboratori) {
            if (c instanceof PersoanaJuridica) {
                c.afiseaza();
            }
        }
        System.out.println();

        Map<TipColaborator, Double> sumaPeTip = new HashMap<>();
        Map<TipColaborator, Integer> numarPeTip = new HashMap<>();

        for (Colaborator c : colaboratori) {
            TipColaborator tip = c.getTipColaborator();
            double venit = c.calculeazaVenitNetAnual();
            sumaPeTip.put(tip, sumaPeTip.getOrDefault(tip, 0.0) + venit);
            numarPeTip.put(tip, numarPeTip.getOrDefault(tip, 0) + 1);
        }

        System.out.println("Sume și număr colaboratori pe tip:");
        for (TipColaborator tip : TipColaborator.values()) {
            double suma = sumaPeTip.getOrDefault(tip, 0.0);
            int nr = numarPeTip.getOrDefault(tip, 0);
            if (nr > 0) {
                System.out.printf("%s: suma = %.2f lei, număr = %d%n", tip, suma, nr);
            }
        }

        scanner.close();
    }
}