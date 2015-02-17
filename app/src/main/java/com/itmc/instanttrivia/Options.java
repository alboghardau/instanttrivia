package com.itmc.instanttrivia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;


public class Options extends ActionBarActivity {

    SharedPreferences settings;

    RadioButton radio_red, radio_blue, radio_purple, radio_lgreen, radio_orange;
    TextView opt_color_header, opt_question_diff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set theme based on prefferences
        settings = getSharedPreferences("InstantOptions", MODE_PRIVATE);
        //sets action bar theme
        Theme_Setter();

        //create layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        radio_red = (RadioButton)findViewById(R.id.radio_red);
        radio_blue = (RadioButton)findViewById(R.id.radio_blue);
        radio_purple = (RadioButton)findViewById(R.id.radion_purple);
        radio_lgreen = (RadioButton)findViewById(R.id.radio_lgreen);
        radio_orange = (RadioButton)findViewById(R.id.radio_orange);
        opt_color_header = (TextView) findViewById(R.id.opt_color_header);
        opt_question_diff = (TextView) findViewById(R.id.opt_question_diff);
        radio_check(settings.getString("Color_Theme", "Purple"));

        //sets rest of theme settings
        Theme_Setter2();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    //sets checking of the radios buttons
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

    //has to be called before inflating the layout
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

    //has to be called after inflating the layout
    private void Theme_Setter2(){
        String tester = settings.getString("Color_Theme","Purple");
        switch (tester){
            case "Red":
                opt_color_header.setTextColor(getResources().getColor(R.color.red_700));
                opt_question_diff.setTextColor(getResources().getColor(R.color.red_700));
                break;
            case "Purple":
                opt_color_header.setTextColor(getResources().getColor(R.color.purple_700));
                opt_question_diff.setTextColor(getResources().getColor(R.color.purple_700));
                break;
            case "Blue":
                opt_color_header.setTextColor(getResources().getColor(R.color.blue_700));
                opt_question_diff.setTextColor(getResources().getColor(R.color.blue_700));
                break;
            case "LGreen":
                opt_color_header.setTextColor(getResources().getColor(R.color.light_green_700));
                opt_question_diff.setTextColor(getResources().getColor(R.color.light_green_700));
                break;
            case "Orange":
                opt_color_header.setTextColor(getResources().getColor(R.color.orange_700));
                opt_question_diff.setTextColor(getResources().getColor(R.color.orange_700));
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

        switch (view.getId()){

            //RED BUTTON
            case R.id.linear_opt_red:
                update_options("Red");
                radio_red.setChecked(true);
                radio_blue.setChecked(false);
                radio_lgreen.setChecked(false);
                radio_purple.setChecked(false);
                radio_orange.setChecked(false);
                break;

            //BLUE BUTTOn
            case R.id.linear_opt_blue:
                update_options("Blue");
                radio_red.setChecked(false);
                radio_blue.setChecked(true);
                radio_lgreen.setChecked(false);
                radio_purple.setChecked(false);
                radio_orange.setChecked(false);
                break;

            //LGREEN BUTTON
            case R.id.linear_opt_lgreen:
                update_options("LGreen");
                radio_red.setChecked(false);
                radio_blue.setChecked(false);
                radio_lgreen.setChecked(true);
                radio_purple.setChecked(false);
                radio_orange.setChecked(false);
                break;

            //PURPLE BUTTON
            case R.id.linear_opt_purple:
                update_options("Purple");
                radio_red.setChecked(false);
                radio_blue.setChecked(false);
                radio_lgreen.setChecked(false);
                radio_purple.setChecked(true);
                radio_orange.setChecked(false);
                break;
            case R.id.linear_opt_orange:
                update_options("Orange");
                radio_red.setChecked(false);
                radio_blue.setChecked(false);
                radio_lgreen.setChecked(false);
                radio_purple.setChecked(false);
                radio_orange.setChecked(true);
        }
    }
}
