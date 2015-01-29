package com.itmc.instanttrivia;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.FrameLayout;
import android.widget.TextView;


public class High_Scores extends ActionBarActivity {

    ActionBar.Tab Tab1, Tab2;

    ActionBar actionBar;

    Fragment frag_easy = new HS_Frag_Easy();
    Fragment frag_medium = new Fragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_high__scores);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Leaderboards");

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Tab1 = actionBar.newTab().setText("HS_Frag_Easy");
        Tab2 = actionBar.newTab().setText("Medium");

        Tab1.setTabListener(new HS_Tab_Listener(frag_easy));
        Tab2.setTabListener(new HS_Tab_Listener(frag_medium));

        actionBar.addTab(Tab1);
        actionBar.addTab(Tab2);


    }

}
