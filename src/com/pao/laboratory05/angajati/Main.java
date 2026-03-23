package com.pao.laboratory05.angajati;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        AngajatService service = AngajatService.getInstance();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== Gestionare Angajați =====");
            System.out.println("1. Adaugă angajat");
            System.out.println("2. Listare după salariu");
            System.out.println("3. Caută după departament");
            System.out.println("0. Ieșire");
            System.out.print("Opțiune: ");

            int optiune = scanner.nextInt();
            scanner.nextLine();

            if (optiune == 0) break;

            switch (optiune) {
                case 1:
                    System.out.print("Nume angajat: ");
                    String nume = scanner.nextLine();
                    System.out.print("Nume departament: ");
                    String deptNume = scanner.nextLine();
                    System.out.print("Locatie departament: ");
                    String deptLoc = scanner.nextLine();
                    System.out.print("Salariu: ");
                    double salariu = scanner.nextDouble();

                    service.addAngajat(new Angajat(nume, new Departament(deptNume, deptLoc), salariu));
                    break;
                case 2:
                    System.out.println("Angajati sortati dupa salariu");
                    service.listBySalary();
                    break;
                case 3:
                    System.out.print("Introduceti departamentul: ");
                    String cautat = scanner.nextLine();
                    service.findByDepartament(cautat);
                    break;
                default:
                    System.out.println("Optiune invalida!");
            }
        }
        scanner.close();
    }
}