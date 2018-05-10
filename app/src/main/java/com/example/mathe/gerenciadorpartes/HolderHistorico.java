package com.example.mathe.gerenciadorpartes;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by mathe on 22/12/2017.
 */

//Em uma recycleview, esta classe Holder é necessária para relacionar os componentes de cada linha com seus
//respectivos valores da lista
//Tutorial:
//  http://www.luiztools.com.br/post/tutorial-crud-em-android-com-sqlite-e-recyclerview-2/
public class HolderHistorico extends RecyclerView.ViewHolder {

    private final TextView nome_pub;
    private final TextView nome_ajud;
    private final TextView data_parte;
    private final TextView numero_ponto;
    private final TextView tipo_parte;
    private final TextView ponto_suger;
    private final LinearLayout passou;

    //Construtor padrão requerido: Precisa receber uma view já inflada a partir de um layout (o layout da linha)
    //associando as variáveis aos id's que representam os componentes da linha
    public HolderHistorico(View itemView) {
        super(itemView);
        nome_pub = (TextView) itemView.findViewById(R.id.textolinhah);
        nome_ajud = (TextView) itemView.findViewById(R.id.textolinhah3);
        data_parte = (TextView) itemView.findViewById(R.id.textolinhah4);
        numero_ponto = (TextView) itemView.findViewById(R.id.textolinhah2);
        tipo_parte = (TextView) itemView.findViewById(R.id.textolinhah5);
        ponto_suger = (TextView) itemView.findViewById(R.id.textolinhah6);
        //Armazena o LinearLayout da linha para que possamos mudar seu atributo background, definindo se passou ou não
        passou = (LinearLayout) itemView.findViewById(R.id.linha_historico);
    }

    //############## Atribui a cada componente seu valor #################

    public void setNome_pub(String t){
        nome_pub.setText(t);
    }

    //Se t == null, essa parte não tem ajudante, porta
    public void setNome_ajud(String t){
        if(t==null){
            nome_ajud.setText("");
        }else{
            nome_ajud.setText(t);
        }
    }

    public void setData_parte(String t){
        data_parte.setText(t);
    }

    //Ponto avaliado == 0 indica que foi uma substituição
    public void setNumero_ponto(String t){
        if(t.equals("0")) {
            numero_ponto.setText("Substituição");
        }else{
            numero_ponto.setText("Ponto: " + t);
        }
    }

    public void setTipo_parte(String t){
        tipo_parte.setText(t);
    }

    //Ponto sugerido == 0 indica que não houve ponto sugerido
    public void setPonto_suger(String t){
        if(!t.equals("0")) {
            ponto_suger.setText("Ponto sug. " + t);
        }else{
            ponto_suger.setText("Ponto sug. -");
        }
    }

    //Alpha define a opacidade da cor, onde 255 é totalmente opaco e 0 é transparente
    public void setPassou(Boolean aprovado, int ponto_id){
        //Se o ponto_id == 0, então a parte é uma substituição. Dessa forma, ela não tem avaliação, portanto a cor dela
        //permanece branca. Se for aprovado, então é verde, e se for desaprovado, vermelho
        if(ponto_id != 0) {
            if (aprovado) {
                passou.setBackgroundColor(Color.argb(255, 214, 227, 181));
            }
            else {
                passou.setBackgroundColor(Color.argb(255, 255, 173, 99));
            }
        }
    }
}
