package com.pao.laboratory08.exercise1;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String commandLine = stdin.readLine().trim();
            List<Student> students = citesteStudenti();
            String[] parts = commandLine.split(" ", 2);
            String tipComanda = parts[0];
            if (tipComanda.equals("PRINT")) {
                for (Student s : students) {
                    System.out.println(s);
                }

            }
            else if (tipComanda.equals("SHALLOW") || tipComanda.equals("DEEP")) {
                String numeCautat = parts[1].trim();
                Student original = null;
                for (Student s : students) {
                    if (s.getNume().equals(numeCautat)) {
                        original = s;
                        break;
                    }
                }

                Object clonaObj;
                if (tipComanda.equals("SHALLOW")) {
                    clonaObj = original.shallowClone();
                }
                else {
                    clonaObj = original.deepClone();
                }

                Student clona = (Student) clonaObj;
                clona.getAdresa().setOras("MODIFICAT");
                System.out.println("Original: " + original);
                System.out.println("Clona: " + clona);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Student> citesteStudenti() throws IOException {
        List<Student> students = new ArrayList<>();
        String path = "src/com/pao/laboratory08/tests/studenti.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String linie;
            while ((linie = br.readLine()) != null) {
                linie = linie.trim();
                if (linie.isEmpty()) continue;

                String[] date = linie.split(",");
                String nume = date[0].trim();
                int varsta = Integer.parseInt(date[1].trim());
                String oras = date[2].trim();
                String strada = date[3].trim();

                Adresa adresa = new Adresa(oras, strada);
                Student student = new Student(adresa, nume, varsta);
                students.add(student);
            }
        }
        return students;
    }
}
