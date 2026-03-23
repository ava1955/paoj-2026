package com.pao.laboratory05.playlist;

public record Song(String title, String artist, int durationSeconds)
        implements Comparable<Song> {

    @Override
    public int compareTo(Song otherSong) {
        return this.title.compareTo(otherSong.title);
    }
}
