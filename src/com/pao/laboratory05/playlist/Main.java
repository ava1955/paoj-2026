package com.pao.laboratory05.playlist;

public class Main {
    public static void main(String[] args) {
        Playlist playlist = new Playlist("Road Trip");
        playlist.addSong(new Song("Waterloo", "ABBA", 174));
        playlist.addSong(new Song("Bohemian Rhapsody", "Queen", 354));
        playlist.addSong(new Song("Imagine", "John Lennon", 187));
        playlist.addSong(new Song("Smells Like Teen Spirit", "Nirvana", 301));

        System.out.println("=== " + playlist.getName() + " ===");
        System.out.println("Durata totală: " + playlist.getTotalDuration() + "s\n");

        System.out.println("--- Sortate după titlu ---");
        playlist.printSortedByTitle();

        System.out.println("\n--- Sortate după durată ---");
        playlist.printSortedByDuration();
    }
}
