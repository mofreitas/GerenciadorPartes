package com.example.mathe.gerenciadorpartes.Model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mathe on 17/12/2017.
 */

//Ao implementar Parcelable, possibilita passar objetos dessa classe pelas activities por meio de intents
public class Ponto implements Parcelable{
    private int id;
    private String nome;
    private String tipo;

    public Ponto(int id, String nome, int tipo){
        this.nome = nome;
        this.id = id;
        switch(tipo){
            case 1:
                this.tipo = "Leitura";
                break;
            case 2:
                this.tipo = "Demonstração";
                break;
            case 3:
                this.tipo = "Discurso";
                break;
            case 4:
                this.tipo = "Demonstração/Discurso";
                break;
            default:
                this.tipo = "Leitura/Demonstração/Discurso";
                break;
        }
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    //Escrita de atributos que serão passado.
    //Nem todos precisam estar incluídos.
    //A ordem de escrita deve ser a mesma do protected Ponto(Parcel in) {...} (método abaixo).
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nome);
        dest.writeString(tipo);
    }

    //Gerado automaticamente por uma classe que implementa Parcelable(alt+enter).
    //Nem todos os atributos precisam estar incluídos ser passados. Apenas os necessários
    protected Ponto(Parcel in) {
        id = in.readInt();
        nome = in.readString();
        tipo = in.readString();
    }

    //Gerado automaticamente por uma classe que implementa Parcelable(alt+enter)
    public static final Creator<Ponto> CREATOR = new Creator<Ponto>() {
        @Override
        public Ponto createFromParcel(Parcel in) {
            return new Ponto(in);
        }

        @Override
        public Ponto[] newArray(int size) {
            return new Ponto[size];
        }
    };

    //Gerado automaticamente por uma classe que implementa Parcelable(alt+enter)
    //inteiro usado para identificar a classe (pode deixar como 0)
    //https://stackoverflow.com/questions/4076946/parcelable-where-when-is-describecontents-used
    @Override
    public int describeContents() {
        return 0;
    }
}
