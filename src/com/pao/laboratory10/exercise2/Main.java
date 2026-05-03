package com.pao.laboratory10.exercise2;

import com.pao.laboratory10.exercise1.Tranzactie;
import com.pao.laboratory10.exercise1.TipTranzactie;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Tranzactie> listaTranzactii = new ArrayList<>();

        if (!scanner.hasNextInt()) return;
        int n = scanner.nextInt();

        for (int i = 0; i < n; i++) {
            int id = scanner.nextInt();
            double suma = Double.parseDouble(scanner.next());
            String data = scanner.next();
            TipTranzactie tip = TipTranzactie.valueOf(scanner.next());
            listaTranzactii.add(new Tranzactie(id, suma, data, tip));
        }

        while (scanner.hasNext()) {
            String comanda = scanner.next();

            switch (comanda) {
                case "UNIQUE_IDS":
                    Set<Integer> ids = new LinkedHashSet<>();
                    for (Tranzactie t : listaTranzactii) {
                        ids.add(t.getId());
                    }
                    System.out.println("IDs unice (" + ids.size() + "): " + ids);
                    break;

                case "MONTHLY_REPORT":
                    Map<String, double[]> report = new TreeMap<>();
                    for (Tranzactie t : listaTranzactii) {
                        String luna = t.getData().substring(0, 7);
                        report.putIfAbsent(luna, new double[2]);
                        if (t.getTip() == TipTranzactie.CREDIT) {
                            report.get(luna)[0] += t.getSuma();
                        } else {
                            report.get(luna)[1] += t.getSuma();
                        }
                    }
                    report.forEach((luna, sume) ->
                            System.out.printf("%s: CREDIT %.2f RON, DEBIT %.2f RON%n", luna, sume[0], sume[1]));
                    break;

                case "TOP":
                    int topN = scanner.nextInt();
                    List<Tranzactie> copieTop = new ArrayList<>(listaTranzactii);
                    copieTop.sort((t1, t2) -> Double.compare(t2.getSuma(), t1.getSuma()));
                    System.out.println("Top " + topN + ":");
                    int limita = Math.min(topN, copieTop.size());
                    for (int i = 0; i < limita; i++) {
                        System.out.println(copieTop.get(i));
                    }
                    break;

                case "SORT_ASC":
                    listaTranzactii.sort(Comparator.comparingDouble(Tranzactie::getSuma));
                    afiseazaLista(listaTranzactii);
                    break;

                case "SORT_DESC":
                    listaTranzactii.sort((t1, t2) -> Double.compare(t2.getSuma(), t1.getSuma()));
                    afiseazaLista(listaTranzactii);
                    break;

                case "REVERSE":
                    Collections.reverse(listaTranzactii);
                    afiseazaLista(listaTranzactii);
                    break;

                case "MIN_MAX":
                    Tranzactie min = Collections.min(listaTranzactii, Comparator.comparingDouble(Tranzactie::getSuma));
                    Tranzactie max = Collections.max(listaTranzactii, Comparator.comparingDouble(Tranzactie::getSuma));
                    System.out.println("MIN: " + min);
                    System.out.println("MAX: " + max);
                    break;

                case "CME_DEMO":
                    try {
                        for (Tranzactie t : listaTranzactii) {
                            listaTranzactii.remove(t);
                        }
                    } catch (ConcurrentModificationException e) {
                        System.out.println("ConcurrentModificationException prins: modificare in iteratie detectata.");
                    }
                    break;
            }
        }
    }

    private static void afiseazaLista(List<Tranzactie> lista) {
        for (Tranzactie t : lista) {
            System.out.println(t);
        }
    }
}