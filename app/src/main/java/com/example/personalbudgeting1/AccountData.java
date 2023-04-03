package com.example.personalbudgeting1;

public class AccountData {
    private double suma;
    private String dataTranzactie;
    private String detalii;

    public AccountData(double suma,String dataTranzactie,String detalii) {
        this.suma = suma;
        this.dataTranzactie = dataTranzactie;
        this.detalii = detalii;
    }

    public AccountData(double suma, String dataTranzactie) {
        this.suma = suma;
        this.dataTranzactie = dataTranzactie;
    }


    public String getDetalii() {
        return detalii;
    }


    public double getSuma() {
        return suma;
    }

    public String getData() {
        return dataTranzactie;
    }
}
