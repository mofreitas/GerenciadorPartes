package com.example.mathe.gerenciadorpartes.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by mathe on 17/12/2017.
 */

//Ao implementar Parcelable, possibilita passar objetos dessa classe pelas activities por meio de intents
public class Publicador implements Parcelable{
    private int id;
    private String nome;
    private Boolean ativo;
    private Character sexo;

    private Calendar lastData;
    private int ponto_sugerido;
    private int numeroPartes;

    public Publicador(int id, String nome, int sexo) {
        this.id = id;
        this.nome = nome;
        this.ativo = true;

        switch (sexo){
            case 0:
                this.sexo = 'm';
                break;
            case 1:
                this.sexo = 'f';
                break;
            default:
                break;
        }
    }

    public Publicador(int id, String nome, String sexo) {
        this.id = id;
        this.nome = nome;
        this.sexo = sexo.charAt(0);  //obtem apenas o primeira caractere da string "sexo"
        this.ativo = true;
    }

    public Publicador(int id, String nome, int numeroPartes, String lastdata, String sexo, int sugerido){
        this.id = id;
        this.nome = nome;
        this.numeroPartes = numeroPartes;
        this.ponto_sugerido = sugerido;
        this.sexo = sexo.charAt(0);  //obtem apenas o primeira caractere da string "sexo"

        //Transforma string de Data no formato "yyyy-MM-dd" em variavel Calendario
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            this.lastData = Calendar.getInstance();
            this.lastData.setTime(sdf.parse(lastdata));
        }
        catch (ParseException | NullPointerException pe){
            this.lastData = null;
        }
    }

    //Gerado automaticamente por uma classe que implementa Parcelable(alt+enter)
    protected Publicador(Parcel in) {
        id = in.readInt();
        nome = in.readString();
        sexo = in.readString().charAt(0);
        numeroPartes = in.readInt();
        ponto_sugerido = in.readInt();
    }

    //Gerado automaticamente por uma classe que implementa Parcelable(alt+enter)
    public static final Creator<Publicador> CREATOR = new Creator<Publicador>() {
        @Override
        public Publicador createFromParcel(Parcel in) {
            return new Publicador(in);
        }

        @Override
        public Publicador[] newArray(int size) {
            return new Publicador[size];
        }
    };

    public Character getSexo_banco(){
        return this.sexo;
    }

    public int getAtivo() {
        return ativo ? 1 : 0;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    //Converte de Calendar para String no formato normal dd/MM/yyyy
    //Em Calendar, os meses são armazenados de 0 a 11 em vez de 1 a 12, por isso o valor do mês é somado com 1
    public String getData(){
        if(this.lastData != null) {
            return lastData.get(Calendar.DAY_OF_MONTH) + "/" +
                    (lastData.get(Calendar.MONTH) + 1) + "/" +
                    lastData.get(Calendar.YEAR);
        }else{
            return "";
        }
    }

    //Converte de Calendar para String no formato do banco de dados yyyy-MM-dd
    //Em Calendar, os meses são armazenados de 0 a 11 em vez de 1 a 12, por isso o valor do mês é somado com 1.
    public String getData_Banco(){
        if(this.lastData!=null) {
            return lastData.get(Calendar.YEAR) + "-" +
                    (lastData.get(Calendar.MONTH) + 1) + "-" +
                    lastData.get(Calendar.DAY_OF_MONTH);
        }
        else{
            return null;
        }

    }

    public int getNpartes(){
        return numeroPartes;
    }

    public int getSugerido(){
        return ponto_sugerido;
    }

    //Gerado automaticamente
    @Override
    public int describeContents() {
        return 0;
    }

    //Tem que escrever na mesma ordem que a leitura é feita no método autogerado "protected Publicador(Parcel in)"
    //Nem todos os atributos do objeto precisam ser escritos
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.nome);
        dest.writeString(String.valueOf(this.sexo));
        dest.writeInt(this.numeroPartes);
        dest.writeInt(this.ponto_sugerido);
    }

    //O método toString é sobrescrito para que esse objeto possa ser usado com ArrayAdapater
    //para preencher os AutoCompleteTextView's
    @Override
    public String toString() {
        return nome;
    }
}
