package com.pao.laboratory08.exercise1;

public class Adresa implements Cloneable {
    private String oras;
    private String strada;

    public Adresa(String oras, String strada) {
        this.oras = oras;
        this.strada = strada;
    }

    public void setOras(String oras) {
        this.oras = oras;
    }

    @Override
    public String toString() {
        return "Adresa{" +
                "oras='" + oras + '\'' +
                ", strada='" + strada + '\'' +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}