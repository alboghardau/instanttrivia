package com.itmc.instanttrivia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;


public class Options extends ActionBarActivity {

    SharedPreferences settings;

    RadioButton radio_red, radio_blue, radio_purple, radio_lgreen, radio_orange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set theme based on prefferences
        settings = getSharedPreferences("InstantOptions", MODE_PRIVATE);
        Theme_Setter();

        //create layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        radio_red = (RadioButton)findViewById(R.id.radio_red);
        radio_blue = (RadioButton)findViewById(R.id.radio_blue);
        radio_purple = (RadioButton)findViewById(R.id.radion_purple);
        radio_lgreen = (RadioButton)findViewById(R.id.radio_lgreen);
        radio_orange = (RadioButton)findViewById(R.id.radio_orange);
        radio_check(settings.getString("Color_Theme", "Purple"));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
    }

    private void radio_check(String tester){
        switch (tester){
            case "Red":
                radio_red.setChecked(true);
                break;
            case "Blue":
                radio_blue.setChecked(true);
                break;
            case "Purple":
                radio_purple.setChecked(true);
                break;
            case "LGreen":
                radio_lgreen.setChecked(true);
                break;
            case "Orange":
                radio_orange.setChecked(true);
                break;
        }
    }

    private void Theme_Setter(){
        String tester = settings.getString("Color_Theme","Purple");

        switch (tester){
            case "Red":
                setTheme(R.style.ActionTheme_Options_Style_Red);
                break;
            case "Purple":
                setTheme(R.style.ActionTheme_Options_Style_Purple);
                break;
            case "Blue":
                setTheme(R.style.ActionTheme_Options_Style_Blue);
                break;
            case "LGreen":
                setTheme(R.style.ActionTheme_Options_Style_LGreen);
                break;
            case "Orange":
                setTheme(R.style.ActionTheme_Options_Style_Orange);
                break;
        }
    }



    private void update_options(String option){
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Color_Theme", option);
        editor.commit();

        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void onRadioButtonClicked(View view){
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()){

            //RED BUTTON
            case R.id.radio_red:
                update_options("Red");
                radio_blue.setChecked(false);
                radio_lgreen.setChecked(false);
                radio_purple.setChecked(false);
                break;

            //BLUE BUTTOn
            case R.id.radio_blue:
                update_options("Blue");
                radio_red.setChecked(false);
                radio_lgreen.setChecked(false);
                radio_purple.setChecked(false);
                break;

            //LGREEN BUTTON
            case R.id.radio_lgreen:
                update_options("LGreen");
                radio_red.setChecked(false);
                radio_blue.setChecked(false);
                radio_purple.setChecked(false);
                break;

            //PURPLE BUTTON
            case R.id.radion_purple:
                update_options("Purple");
                radio_blue.setChecked(false);
                radio_lgreen.setChecked(false);
                radio_red.setChecked(false);

                break;
            case R.id.radio_orange:
                update_options("Orange");
                radio_blue.setChecked(false);
                radio_red.setChecked(false);
                radio_lgreen.setChecked(false);
                radio_purple.setChecked(false);
        }
    }
}
