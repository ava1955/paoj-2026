package com.pao.laboratory05.playlist;

import java.util.Arrays;

public class Playlist {
    private String name;
    private Song[] songs = new Song[0];

    public Playlist(String name) {
        this.name = name;
    }

    public void addSong(Song song) {
        Song[] noileMelodii = new Song[songs.length + 1];

        System.arraycopy(songs, 0, noileMelodii, 0, songs.length);

        noileMelodii[songs.length] = song;

        this.songs = noileMelodii;
    }

    public void printSortedByTitle() {
        System.out.println("Playlist '" + name + "' sortat dupa titlu:");
        Song[] copy = songs.clone();

        Arrays.sort(copy);

        for (Song song : copy) {
            System.out.println(song);
        }
        System.out.println();
    }

    public void printSortedByDuration() {
        System.out.println("Playlist '" + name + "' sortat după durată:");
        Song[] copy = songs.clone();

        Arrays.sort(copy, new SongDurationComparator());

        for (Song song : copy) {
            System.out.println(song);
        }
        System.out.println();
    }

    public int getTotalDuration() {
        int total = 0;
        for (Song song : songs) {
            total += song.durationSeconds();
        }
        return total;
    }

    public String getName() {
        return name;
    }
}
