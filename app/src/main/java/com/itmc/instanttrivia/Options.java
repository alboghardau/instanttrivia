package com.itmc.instanttrivia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class Options extends ActionBarActivity {

    SharedPreferences settings;

    RadioButton radio_red, radio_blue, radio_purple, radio_lgreen, radio_orange;
    RadioButton opt_radio_easy, opt_radio_medium, opt_radio_hard, opt_radio_random;
    TextView opt_color_header, opt_question_diff;
    RelativeLayout opt_rel_easy, opt_rel_medium, opt_rel_hard, opt_rel_random;

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

        opt_radio_easy = (RadioButton)findViewById(R.id.opt_radio_easy);
        opt_radio_medium = (RadioButton)findViewById(R.id.opt_radio_medium);
        opt_radio_hard = (RadioButton)findViewById(R.id.opt_radio_hard);
        opt_radio_random = (RadioButton)findViewById(R.id.opt_radio_random);

        opt_color_header = (TextView) findViewById(R.id.opt_color_header);
        opt_question_diff = (TextView) findViewById(R.id.opt_question_diff);
        opt_rel_easy = (RelativeLayout) findViewById(R.id.opt_rel_easy);
        opt_rel_medium = (RelativeLayout) findViewById(R.id.opt_rel_medium);
        opt_rel_hard = (RelativeLayout) findViewById(R.id.opt_rel_hard);
        opt_rel_random = (RelativeLayout) findViewById(R.id.opt_rel_random);

        radio_check(settings.getString("Color_Theme", "Purple"));
        diff_check(settings.getInt("question_diff", 5));

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

    //set on start the difficulty
    private void diff_check(int diff){
        switch (diff){
            case 1:
                opt_radio_easy.setChecked(true);
                break;
            case 2:
                opt_radio_medium.setChecked(true);
                break;
            case 3:
                opt_radio_hard.setChecked(true);
                break;
            case 5:
                opt_radio_random.setChecked(true);
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

    //sets the color setting in preferences and restarts app
    private void update_options_color(String option){
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Color_Theme", option);
        editor.commit();

        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    //sets the question difficulty setting in preferences
    private void update_options_difficulty(int difficulty){
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("question_diff", difficulty);
        editor.commit();
    }

    //question difficulty radio clicker
    public void onDifficultyClicked(View view){
        switch (view.getId()){
            case R.id.opt_rel_easy:
                update_options_difficulty(1);
                radio_diff_reset();
                opt_radio_easy.setChecked(true);
                break;
            case R.id.opt_rel_medium:
                update_options_difficulty(2);
                radio_diff_reset();
                opt_radio_medium.setChecked(true);
                break;
            case R.id.opt_rel_hard:
                update_options_difficulty(3);
                radio_diff_reset();
                opt_radio_hard.setChecked(true);
                break;
            case R.id.opt_rel_random:
                update_options_difficulty(5);
                radio_diff_reset();
                opt_radio_random.setChecked(true);
                break;
        }
    }

    private void radio_diff_reset(){
        opt_radio_easy.setChecked(false);
        opt_radio_medium.setChecked(false);
        opt_radio_hard.setChecked(false);
        opt_radio_random.setChecked(false);
    }

    //resets radios for color selection
    public void onRadioButtonClicked(View view){

        switch (view.getId()){

            //RED BUTTON
            case R.id.linear_opt_red:
                update_options_color("Red");
                radio_color_reset();
                radio_red.setChecked(true);
                break;

            //BLUE BUTTOn
            case R.id.linear_opt_blue:
                update_options_color("Blue");
                radio_color_reset();
                radio_blue.setChecked(true);
                break;

            //LGREEN BUTTON
            case R.id.linear_opt_lgreen:
                update_options_color("LGreen");
                radio_color_reset();
                radio_lgreen.setChecked(true);
                break;

            //PURPLE BUTTON
            case R.id.linear_opt_purple:
                update_options_color("Purple");
                radio_color_reset();
                radio_purple.setChecked(true);
                break;
            case R.id.linear_opt_orange:
                update_options_color("Orange");
                radio_color_reset();
                radio_orange.setChecked(true);
        }
    }

    private void radio_color_reset(){
        radio_red.setChecked(false);
        radio_blue.setChecked(false);
        radio_lgreen.setChecked(false);
        radio_purple.setChecked(false);
        radio_orange.setChecked(false);
    }
}
