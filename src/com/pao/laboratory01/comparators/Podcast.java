package com.pao.laboratory01.comparators;
import java.util.Comparator;

// EXERCITIU 1: cream in pachetul comparators o clasa Podcast cu durata (secunde, int) si titlu (string)
// dupa modelul AudioBook.java si Book.java, implementati:
// 1. toString — pentru afisare frumoasa
// 2. Comparable<Podcast> cu compareTo — sortare dupa titlu
// 3. un Comparator extern (PodcastLengthComparator) — sortare dupa durata
// 4. o metoda main in care cream cateva podcast-uri si le sortam in ambele moduri
public class Podcast implements Comparable<Podcast> {

    private String title;
    private int durationInSeconds;

    public Podcast(String title, int durationInSeconds) {
        this.title = title;
        this.durationInSeconds = durationInSeconds;
    }
    @Override
    public String toString() {
        return "Podcast{title='" + title + "', durationInSeconds=" + durationInSeconds + "}";
    }

    @Override
    public int compareTo(Podcast other) {
        return this.title.compareTo(other.title);
    }
    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public static void main(String[] args) {
        // cream cateva podcast-uri
        Podcast[] podcasts = {
                new Podcast("Tech Talk", 2400),
                new Podcast("Arta Conversatiei", 3600),
                new Podcast("Mindset", 1800)
        };

        java.util.Arrays.sort(podcasts);
        System.out.println("Sortate dupa titlu:");
        System.out.println(java.util.Arrays.toString(podcasts));

        java.util.Arrays.sort(podcasts, new PodcastLengthComparator());
        System.out.println("Sortate dupa durata (crescator):");
        System.out.println(java.util.Arrays.toString(podcasts));

        java.util.Arrays.sort(podcasts,
                (p1, p2) -> Integer.compare(p2.getDurationInSeconds(), p1.getDurationInSeconds())
        );
        System.out.println("Sortate dupa durata (descrescator, lambda):");
        System.out.println(java.util.Arrays.toString(podcasts));
    }
}


class PodcastLengthComparator implements Comparator<Podcast> {

    @Override
    public int compare(Podcast p1, Podcast p2) {
        // comparam direct valorile int (mai sigur decat p1.duration - p2.duration)
        return Integer.compare(p1.getDurationInSeconds(), p2.getDurationInSeconds());
    }
}