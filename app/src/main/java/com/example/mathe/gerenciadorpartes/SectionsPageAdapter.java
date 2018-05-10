package com.example.mathe.gerenciadorpartes;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by mathe on 18/12/2017.
 */

//Tutorial
//  https://www.youtube.com/watch?v=bNpWGI_hGGg&t=89s
public class SectionsPageAdapter extends FragmentPagerAdapter{

    private final ArrayList<Fragment> listaFrag;        //Lista com os fragmentos (telas)
    private final ArrayList<String> listaTituloFrag;    //Lista com os títulos dos fragmentos (que aparecem nas abas)

    public SectionsPageAdapter(FragmentManager fm) {
        super(fm);
        listaFrag = new ArrayList<>();
        listaTituloFrag = new ArrayList<>();
    }

    public void adcFragmento(Fragment f, String titulo){
        listaFrag.add(f);
        listaTituloFrag.add(titulo);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return listaTituloFrag.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return listaFrag.get(position);
    }

    @Override
    public int getCount() {
        return listaFrag.size();
    }
}