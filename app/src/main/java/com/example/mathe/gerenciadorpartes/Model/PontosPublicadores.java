package com.example.mathe.gerenciadorpartes.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by mathe on 25/12/2017.
 */

public class PontosPublicadores {
    private int id;
    private int public_id;
    private int comp_id;
    private int ponto_id;
    private Calendar data_conc;
    private int tipo_parte;
    private Boolean passou;
    private int ponto_sug;
    private Boolean notificado;

    private String public_nome;
    private String comp_nome;
    private String ponto_nome;

    public PontosPublicadores(int id, int publicador, int ponto_id, int ajudante, String data_conc, int tipo_parte){
        this.id = id;
        this.public_id = publicador;
        this.ponto_id = ponto_id;
        this.comp_id = ajudante;
        this.data_conc = this.String_Calendar(data_conc, 1);
        this.tipo_parte = tipo_parte;
        this.notificado = false;            //Ao ser criada, nenhuma parte é notificada de cara
    }

    public PontosPublicadores(int id, String public_nome, int ponto_id, String comp_nome, String data_concl, int tipo_parte, int pass, int ponto_sug, int public_id) {
        this.id = id;
        this.public_id = public_id;
        this.public_nome = public_nome;
        this.comp_nome = comp_nome;
        this.ponto_id = ponto_id;
        this.tipo_parte = tipo_parte;
        this.data_conc = Calendar.getInstance();
        this.ponto_sug = ponto_sug;
        this.data_conc = this.String_Calendar(data_concl, 1);
        this.passou = pass == 1 ? true : false;
    }

    public PontosPublicadores(int id, String publicador, int ponto_id, String ajudante, String data_conc, int tipo_parte, String ponto_nome, int notificado){
        this.id = id;
        this.public_nome = publicador;
        this.ponto_id = ponto_id;
        this.comp_nome = ajudante;
        this.data_conc = this.String_Calendar(data_conc, 1);
        this.tipo_parte = tipo_parte;
        this.ponto_nome = ponto_nome;
        this.notificado = notificado == 1 ? true : false;
    }

    //Metodo que tranforma de String (formatos yyyy-MM-dd ou dd/MM/yyyy) para Calendar
    private Calendar String_Calendar(String data, int tipo){
        Calendar c = Calendar.getInstance();
        try {
            if(tipo == 1) {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
                c.setTime(f.parse(data));
            }
            else if(tipo == 2){
                SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
                c.setTime(f.parse(data));
            }
            else{
                throw new IllegalArgumentException("Tipo de data inválido");
            }
        }
        catch (ParseException | NullPointerException E){
            //Se data = null ou se a string não estiver no formato correto, então c = null
            c = null;
        }
        return c;
    }

    //Converte de Calendar para String no formato normal dd/MM/yyyy
    //Em Calendar, os meses são armazenados de 0 a 11 em vez de 1 a 12, por isso o valor do mês é somado com 1
    public String getData_concl() {
        if(data_conc!=null) {
            return data_conc.get(Calendar.DAY_OF_MONTH) + "/" + (data_conc.get(Calendar.MONTH) + 1) + "/" + data_conc.get(Calendar.YEAR);
        }
        else{
            return null;
        }
    }

    //Converte de Calendar para String no formato do banco de dados yyyy-MM-dd
    //Em Calendar, os meses são armazenados de 0 a 11 em vez de 1 a 12, por isso o valor do mês é somado com 1.
    //Como a data obtida do banco está em forma de string, precisamos usar o método String_Calendar nele também.
    //Não adiantaria guardar os meses tal como no Calendar (0 - 11), pois ao recuperar esses dados,
    //o mês iria diminuir, já que na entrada da conversão para calendar, ele recebe os meses de 1-12
    public String getData_conc_banco(){
        return data_conc.get(Calendar.YEAR) + "-" + (data_conc.get(Calendar.MONTH)+1) + "-" + data_conc.get(Calendar.DAY_OF_MONTH);
    }

    public void setData_conc(String data){
        this.data_conc = this.String_Calendar(data, 2);
    }

    public void setData_conc_banco(String data){
        this.data_conc = this.String_Calendar(data, 1);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPublic_id() {
        return public_id;
    }

    public void setPublic_id(int public_id) {
        this.public_id = public_id;
    }

    public String getPublic_nome() {
        return public_nome;
    }

    public void setPublic_nome(String public_nome) {
        this.public_nome = public_nome;
    }

    public int getComp_id() {
        return comp_id;
    }

    public void setComp_id(int comp_id) {
        this.comp_id = comp_id;
    }

    public String getComp_nome() {
        return comp_nome;
    }

    public void setComp_nome(String comp_nome) {
        this.comp_nome = comp_nome;
    }

    public int getPonto_id() {
        return ponto_id;
    }

    public void setPonto_id(int ponto_id) {
        this.ponto_id = ponto_id;
    }

    public String getPonto_nome() {
        return ponto_nome;
    }

    public void setPonto_nome(String ponto_nome) {
        this.ponto_nome = ponto_nome;
    }

    public int getTipo_parte() {
        return tipo_parte;
    }

    public void setTipo_parte(int tipo_parte){
        this.tipo_parte = tipo_parte;
    }

    public Boolean getPassou() {
        return passou;
    }

    public void setPassou(int passou) {
        if(passou==1){
            this.passou = true;
        }
        else {
            this.passou = false;
        }
    }

    public String getTipo_partenome(){
        switch(tipo_parte){
            case 0:
                return("Leitura");
            case 1:
                return("Primeira conversa");
            case 2:
                return("Primeira revisita");
            case 3:
                return("Segunda revisita");
            case 4:
                return("Terceira revisita");
            case 5:
                return("Estudo bíblico");
            case 6:
                return("Discurso");
            default:
                throw new IllegalArgumentException("Esse tipo de parte não existe");
        }
    }

    public int getPonto_sug() {
        return ponto_sug;
    }

    public void setPonto_sug(int ponto_sug) {
        this.ponto_sug = ponto_sug;
    }

    public Calendar getData_conc_Calendar(){
        return this.data_conc;
    }

    public Boolean getNotificado() {
        return notificado;
    }

    public void setNotificado(Boolean notificado) {
        this.notificado = notificado;
    }
}
