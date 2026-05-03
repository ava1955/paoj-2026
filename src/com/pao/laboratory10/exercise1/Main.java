package com.pao.laboratory10.exercise1;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        LinkedList<Tranzactie> coada = new LinkedList<>();
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            String comanda = scanner.next();

            switch (comanda) {
                case "ENQUEUE":
                    coada.addLast(citesteTranzactie(scanner));
                    break;

                case "PUSH":
                    coada.addFirst(citesteTranzactie(scanner));
                    break;

                case "DEQUEUE":
                    if (coada.isEmpty()) {
                        System.out.println("Coada goala.");
                    } else {
                        System.out.println("Procesat: " + coada.removeFirst());
                    }
                    break;

                case "POP":
                    if (coada.isEmpty()) {
                        System.out.println("Coada goala.");
                    } else {
                        System.out.println("Extras: " + coada.removeFirst());
                    }
                    break;

                case "REMOVE_DEBIT": {
                    int count = 0;
                    Iterator<Tranzactie> it = coada.iterator();
                    while (it.hasNext()) {
                        if (it.next().getTip() == TipTranzactie.DEBIT) {
                            it.remove();
                            count++;
                        }
                    }
                    System.out.println("Eliminat " + count + " tranzactii DEBIT.");
                    break;
                }

                case "REMOVE_BELOW": {
                    double threshold = Double.parseDouble(scanner.next());
                    int count = 0;
                    Iterator<Tranzactie> it = coada.iterator();
                    while (it.hasNext()) {
                        if (it.next().getSuma() < threshold) {
                            it.remove();
                            count++;
                        }
                    }
                    System.out.printf("Eliminat %d tranzactii sub %.2f RON.%n", count, threshold);
                    break;
                }

                case "PRINT":
                    for (Tranzactie t : coada) {
                        System.out.println(t);
                    }
                    break;

                case "SIZE":
                    System.out.println("Dimensiune coada: " + coada.size());
                    break;
            }
        }
        scanner.close();
    }

    private static Tranzactie citesteTranzactie(Scanner sc) {
        int id = sc.nextInt();
        double suma = Double.parseDouble(sc.next());
        String data = sc.next();
        TipTranzactie tip = TipTranzactie.valueOf(sc.next());
        return new Tranzactie(id, suma, data, tip);
    }
}