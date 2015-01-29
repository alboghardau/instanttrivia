package com.itmc.instanttrivia;

import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import static com.itmc.instanttrivia.R.id.fragment_container;

public class HS_Tab_Listener implements ActionBar.TabListener{

    Fragment fragment;

    public HS_Tab_Listener(Fragment fragment) {

        this.fragment = fragment;
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.replace(fragment_container,fragment);
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.remove(fragment);
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {

    }
}
