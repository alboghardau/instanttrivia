package com.itmc.instanttrivia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;

import com.google.example.games.basegameutils.GameHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

public class Game_Timer extends Activity{

    TextView text_question, text_score, text_question_current, text_final_score, text_difficulty, text_category;
    ScrollView cats_scroll;
    Button btn_nextq;
    LinearLayout lin_cats, lin_answer, lin_bot, lin_gratz, lin_inter, lin_difficulty;
    RelativeLayout lin_top, rel_base;
    GridLayout btn_grid;
    ProgressBar prog_bar;
    RadioButton radio_easy, radio_medium, radio_hard, radio_random;

    int animation_time = 700;

    ArrayList<String> questions_array;

    //animation interpolator for all animations
    TimeInterpolator interpolator;

    String question;
    String answer;
    String category;
    String randomchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    SharedPreferences settings;

    ArrayList<Character> buttons;
    ArrayList<Character> ans_arr;
    ArrayList<Character> ans_pressed;
    ArrayList<String> categories;

    int color_extraDark;
    Typeface font;

    Boolean started = false;

    //TO DO INSPECT IF CAN DELETE THIS VARIABLE
    int game_difficulty = 0;

    //options and varaibles
    int back_pressed = 0;
    int score = 0;
    int pressed_correct = 0;
    int pressed_wrong = 0;

    //GAME OPTIONS THAT ARE UPDATED WHEN GAME IS SELECTED
    int question_counter = 0;
    int question_number = 0;
    int max_wrong = 0;
    int score_per_question = 0;
    int question_time = 0;

    //settings for difficulty and category
    int difficulty_setting = 0;
    int question_category = 0;

    boolean buttons_sort_alpha = true;

    int leaderboard_name = 0;

    //STATISTICS
    int question_correct = 0;
    int question_wrong = 0;
    int total_buttons_correct = 0;
    int total_buttons_wrong = 0;

    int coin_awarder = 0;
    int coin_awarder_limit = 0;

    //buffer variables for millis left to calc score
    int millis_buffer = 0;

    int total_time = 0;
    CountDownTimer timer = null;

    private DbOP db;

    public GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences("InstantOptions", MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game__timer);

        font = Typeface.createFromAsset(this.getAssets(), "typeface/bubblegum.otf");

        //define question text
        text_question = (TextView) findViewById(R.id.text_question);
        text_score = (TextView) findViewById(R.id.text_score);
        text_question_current = (TextView) findViewById(R.id.text_question_current);
        text_final_score = (TextView)findViewById(R.id.text_final_score);
        text_difficulty = (TextView) findViewById(R.id.text_difficulty);
        text_category = (TextView) findViewById(R.id.text_category);
        btn_nextq = (Button)findViewById(R.id.button_nextq);

        radio_easy = (RadioButton) findViewById(R.id.radio_easy);
        radio_medium = (RadioButton) findViewById(R.id.radio_medium);
        radio_hard = (RadioButton) findViewById(R.id.radio_hard);
        radio_random = (RadioButton) findViewById(R.id.radio_random);

        lin_answer = (LinearLayout) findViewById(R.id.linear_answer);
        lin_bot = (LinearLayout) findViewById(R.id.linear_bot);
        lin_cats = (LinearLayout) findViewById(R.id.linear_cats);
        lin_gratz = (LinearLayout) findViewById(R.id.linear_gratz);
        lin_inter = (LinearLayout) findViewById(R.id.linear_inter);
        lin_difficulty = (LinearLayout) findViewById(R.id.linear_difficulty);

        lin_top = (RelativeLayout) findViewById(R.id.linear_topbar);
        rel_base = (RelativeLayout) findViewById(R.id.relative_base);
        btn_grid = (GridLayout) findViewById(R.id.buttons_grid);

        cats_scroll = (ScrollView) findViewById(R.id.cats_scroll);
        prog_bar = (ProgressBar) findViewById(R.id.timer_bar);

        overrideFonts(this,rel_base);

        //set interpolator
        interpolator = new DecelerateInterpolator(1);

        //sets colors for layout
        Theme_Setter_Views();

        //set relative size for questions textview
        text_question.setTextSize((float) (DpHeight()/35.0));

        ViewGroup.LayoutParams p = text_question.getLayoutParams();
        p.height = dpToPx((int) (DpHeight()/6.0));

        //declare answer chars store , and store answer in array
        buttons = new ArrayList<Character>();
        ans_arr = new ArrayList<Character>();
        ans_pressed = new ArrayList<Character>();

        //start database operator
        db = new DbOP(this);
        db.startdb();

        //temporary fix passing of mGoogleApiClient form start activity to this
        Log.e("SignIn Status b4", settings.getBoolean("SIGNED_IN",false)+"");
        if(settings.getBoolean("SIGNED_IN",false) == true){
            GameHelper gameHelper = new GameHelper(this,GameHelper.CLIENT_GAMES);
            gameHelper.setup(new GameHelper.GameHelperListener() {
                @Override public void onSignInFailed() {}
                @Override public void onSignInSucceeded() {}
            });
            mGoogleApiClient = gameHelper.getApiClient();
            mGoogleApiClient.connect();
        }else{
            mGoogleApiClient = null;
        }

        //sets difficulty
        difficulty_set(settings.getInt("question_diff",5));
        Log.e("GET DIFFICULTY", settings.getInt("question_diff",5)+"");

        //populate list of categories
        categories_generate_list();

        //dispaly animation on start
        animate_start();

        db.read_10_questions(2,2);

        btn_nextq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                question_resume();
            }
        });
    }

    public void onDifficultyRadioClick(View view){
        switch (view.getId()){
            case R.id.radio_easy:
                difficulty_radio_reset();
                update_options_difficulty(1);
                difficulty_set(1);
                break;
            case R.id.radio_medium:
                difficulty_radio_reset();
                update_options_difficulty(2);
                difficulty_set(2);
                break;
            case R.id.radio_hard:
                difficulty_radio_reset();
                update_options_difficulty(3);
                difficulty_set(3);
                break;
            case R.id.radio_random:
                difficulty_radio_reset();
                update_options_difficulty(5);
                difficulty_set(5);
                break;
        }
    }

    //sets the question difficulty setting in preferences
    private void update_options_difficulty(int difficulty){
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("question_diff", difficulty);
        editor.commit();
    }

    private void difficulty_radio_reset(){
        radio_easy.setChecked(false);
        radio_medium.setChecked(false);
        radio_hard.setChecked(false);
        radio_random.setChecked(false);
    }

    //sets colors for internal views of layout
    private void Theme_Setter_Views(){
        String tester = settings.getString("Color_Theme","Purple");
        switch (tester){
            case "Red":
                Views_Editor("red");
                color_extraDark = getResources().getColor(R.color.red_900);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    getWindow().setStatusBarColor(getResources().getColor(R.color.red_700));
                }
                break;
            case "Purple":
                Views_Editor("deep_purple");
                color_extraDark = getResources().getColor(R.color.deep_purple_900);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    getWindow().setStatusBarColor(getResources().getColor(R.color.deep_purple_700));
                }
                break;
            case "Blue":
                Views_Editor("blue");
                color_extraDark = getResources().getColor(R.color.blue_900);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    getWindow().setStatusBarColor(getResources().getColor(R.color.blue_700));
                }
                break;
            case "LGreen":
                Views_Editor("light_green");
                color_extraDark = getResources().getColor(R.color.light_green_900);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    getWindow().setStatusBarColor(getResources().getColor(R.color.light_green_700));
                }
                break;
            case "Orange":
                Views_Editor("orange");
                color_extraDark = getResources().getColor(R.color.orange_900);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    getWindow().setStatusBarColor(getResources().getColor(R.color.orange_700));
                }
                break;
        }
    }

    private void Views_Editor(String color){
        int primary_color = getResources().getIdentifier("color/"+color+"_500",null,getPackageName());
        int darker_color = getResources().getIdentifier("color/"+color+"_700",null,getPackageName());
        int progress_dwg = getResources().getIdentifier("drawable/custom_progressbar_"+color,null,getPackageName());

        prog_bar.setProgressDrawable(getResources().getDrawable(progress_dwg));
        text_question.setBackgroundColor(getResources().getColor(primary_color));
        lin_top.setBackgroundColor((getResources().getColor(darker_color)));
        lin_gratz.setBackgroundColor(getResources().getColor(primary_color));
        text_final_score.setBackgroundColor(getResources().getColor(darker_color));
        text_difficulty.setTextColor(getResources().getColor(darker_color));
        text_category.setTextColor(getResources().getColor(darker_color));
    }

    private LinearLayout categories_generate_view(final String name, final String id){

        LinearLayout lin = new LinearLayout(this);
        lin.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;

        lin.setLayoutParams(params);
        lin.setGravity(Gravity.CENTER_VERTICAL);
        lin.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_lollipop));

        ImageView img = new ImageView(this);
        img.setImageResource(getResources().getIdentifier("icon_cat_" +id, "drawable", "com.itmc.instanttrivia"));
        img.setPadding(dpToPx(5),0,dpToPx(5),0);
        img.setColorFilter(getResources().getColor(R.color.grey_700));

        TextView text = new TextView(this);
        text.setText((CharSequence) name);
        text.setGravity(Gravity.CENTER);
        text.setTextSize(15);
        text.setTypeface(font);

        lin.addView(img);
        lin.addView(text);

        lin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                question_category = Integer.parseInt(id);
                //porneste animatia pentru questions

                async_load_questions();
            }
        });

        return lin;
    }

    private void categories_generate_list(){
        final ArrayList<String> categories = db.read_cats(difficulty_setting);
        Log.e("Categories", categories.toString());

        for( int i = 0; i < categories.size(); i=i+4){

            LinearLayout orig_lin = new LinearLayout(this);
            orig_lin.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout lin = categories_generate_view(categories.get(i),categories.get(i+1));
            LinearLayout lin2 = categories_generate_view(categories.get(i+2),categories.get(i+3));

            final int i2 = i;


            orig_lin.addView(lin);
            orig_lin.addView(lin2);

            lin_cats.addView(orig_lin);
        }
    }

    private void async_load_questions(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Loading questions");
        pd.setMessage("Please wait.");
        pd.setCancelable(false);
        pd.setIndeterminate(true);

        AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    questions_array = db.read_10_questions(difficulty_setting, question_category);
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pd.dismiss();
                animate_game_start();
            }
        };
        task.execute((Void[])null);
    }

    //sets difficulty variables
    private void difficulty_set(int difficulty){
        game_difficulty = difficulty;
        switch (difficulty){
            case 1:                     //ease game settings
                radio_easy.setChecked(true);
                question_number = 10;
                max_wrong = 5;
                score_per_question = 50;
                question_time = 40000;
                leaderboard_name = R.string.leaderboard_time_trial__easy_level;
                difficulty_setting = 1;
                coin_awarder_limit = 20;
                break;
            case 2:                     //medium game settings
                radio_medium.setChecked(true);
                question_number = 10;
                max_wrong = 4;
                score_per_question = 100;
                question_time = 35000;
                leaderboard_name = R.string.leaderboard_time_trial__medium_level;
                difficulty_setting = 2;
                coin_awarder_limit = 15;
                break;
            case 3:                     //hard game settings
                radio_hard.setChecked(true);
                question_number = 10;
                max_wrong = 3;
                score_per_question = 150;
                question_time = 30000;
                leaderboard_name = R.string.leaderboard_time_trial__hard_level;
                difficulty_setting = 3;
                buttons_sort_alpha = false;
                coin_awarder_limit = 10;
                break;
            case 5:                     //random game settings
                radio_random.setChecked(true);
                question_number = 10;
                max_wrong = 4;
                score_per_question = 75;
                question_time = 30000;
                //leaderboard_name = R.string.leaderboard_time_trial__hard_level;
                difficulty_setting = 5;
                buttons_sort_alpha = false;
                coin_awarder_limit = 15;
                break;
        }
    }

    //ANIMATE AFTER CATEGORY IS CHOSEN, START OF GAMEPLAY
    private void animate_game_start() {
        question_animate("hide","");
        lin_difficulty.animate().alpha(0f).setDuration(animation_time).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                lin_difficulty.setVisibility(View.GONE);
            }
        }).start();
        cats_scroll.animate().alpha(0f).setDuration(animation_time).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cats_scroll.setVisibility(View.GONE);
                cats_scroll.removeAllViews();
                started = true;
                question_next();
            }
        }).start();
    }

    private void question_animate(String action, final String text){
        switch (action){
            case "hide":
                text_question.animate().setDuration(animation_time).setInterpolator(interpolator).scaleX(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        text_question.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        text_question.setText("");
                    }
                }).start();
                break;
            case "show":
                text_question.animate().setDuration(animation_time).setInterpolator(interpolator).scaleX(1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        text_question.setText(text);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        text_question.setVisibility(View.VISIBLE);
                    }
                }).start();
                break;
        }
    }

    private void buttons_display(String state){
        switch (state){
            case "show":
                btn_grid.setVisibility(View.VISIBLE);
                btn_grid.setAlpha(0);
                btn_grid.animate().setDuration(animation_time).setStartDelay(0).setInterpolator(interpolator).alpha(1f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        buttons_enabler(false);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        buttons_enabler(true);
                    }
                }).start();
                break;
            case "hide":
                btn_grid.animate().setDuration(animation_time).setStartDelay(0).setInterpolator(interpolator).alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        buttons_enabler(false);
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        btn_grid.setVisibility(View.GONE);
                    }
                }).start();
                break;
        }
    }

    private void inter_display(String state){
        switch (state){
            case "show":
                btn_nextq.setEnabled(false);
                //lin_inter.setAlpha(0);
                lin_inter.animate().setStartDelay(700).setDuration(animation_time).setInterpolator(interpolator).alpha(1f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        lin_inter.setVisibility(View.VISIBLE);
                        btn_nextq.setEnabled(true);
                    }
                }).start();
                break;
            case "hide":
                btn_nextq.setEnabled(false);
                lin_inter.animate().setDuration(animation_time).setInterpolator(interpolator).alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        btn_nextq.setEnabled(false);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        lin_inter.setVisibility(View.GONE);
                    }
                }).start();
                break;
        }
    }

    //function to activate / deactivate the buttons
    private void buttons_enabler(boolean state){
        //buttons disable
        for (int i = 0; i < btn_grid.getChildCount(); i++) {
            View child = btn_grid.getChildAt(i);
            child.setEnabled(state);
        }
    }

    private int coins_left(){
        return settings.getInt("Coins", 25);
    }

    private void settings_coins_update(int hints){
        SharedPreferences.Editor edit = settings.edit();
        edit.putInt("Coins",hints);
        edit.commit();
    }
    private void settings_rated(boolean set){
        SharedPreferences.Editor edit = settings.edit();
        edit.putBoolean("Rated", set);
        edit.commit();
    }

    private void score_final_display(){
        //UPDATE SCORES, ACUM CU TRIPLA PROTECTIE, if not SING IN setting is not true will not test, preventing NULL exception
        //send scores to google server
        if(mGoogleApiClient != null){
            Log.e("API ACTION:", "Api client is initialized");
            if(mGoogleApiClient.isConnected() == true && difficulty_setting != 0 && question_category!=0) {
                Log.e("API ACTION:", "Uploading Scores!");
                //update leaderboards total score
                score_total_update();
                //update achievements
                achievements_questions_update();
            }else{
                Log.e("API ACTION:", "Can not upload scores, not connected");
            }
        }else{
            score_save_later(); //saves scores in settings for later upload
            Log.e("API ACTION:", "Scores saved for later upload");
        }

        LinearLayout lin_score = (LinearLayout) findViewById(R.id.linear_finalscore);
        lin_score.setVisibility(View.VISIBLE);

        text_score.setVisibility(View.INVISIBLE);

        text_final_score.setText(score + "");
        text_final_score.setTypeface(font);

        Button btn_back = (Button) findViewById(R.id.button_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final Button btn_rate = (Button) findViewById(R.id.button_rate);

        if(settings.getBoolean("Rated", false) == true){
            btn_rate.setVisibility(View.GONE);
        }
        btn_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                    settings_coins_update(coins_left() + 25);
                    btn_rate.setVisibility(View.GONE);
                    settings_rated(true);
                } catch (ActivityNotFoundException e) {

                }
            }
        });

        //set round progress bars
        TextView text_correct_hits = (TextView) findViewById(R.id.text_correct_hits);
        TextView text_wrong_hits = (TextView)findViewById(R.id.text_wrong_hits);
        TextView text_accuracy = (TextView)findViewById(R.id.text_accuracy);
        ProgressBar prog_correct = (ProgressBar)findViewById(R.id.prog_correct);
        ProgressBar prog_wrong = (ProgressBar) findViewById(R.id.prog_wrong);
        ProgressBar prog_acc = (ProgressBar) findViewById(R.id.prog_accuracy);

        text_correct_hits.setText(total_buttons_correct+"");
        text_wrong_hits.setText(total_buttons_wrong+"");
        int total_hits = total_buttons_correct+total_buttons_wrong;
        double accuracy = Math.ceil(((total_buttons_correct)/(float)total_hits)*100);
        text_accuracy.setText((int)accuracy+"");

        prog_correct.setMax(total_hits);
        prog_correct.setProgress(total_buttons_correct);
        prog_wrong.setMax(total_hits);
        prog_wrong.setProgress(total_buttons_wrong);
        prog_acc.setProgress(Integer.valueOf((int) accuracy));


        lin_bot.animate().setDuration(animation_time).alpha(1f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                lin_bot.clearAnimation();
            }
        }).start();
    }

    private void achievements_questions_update(){
        if(question_correct > 0) { //increment must be greater than 0
            Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_trivia_newbie), question_correct);
            Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_trivia_begginer), question_correct);
            Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_trivia_enthusiast), question_correct);
            Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_trivia_master), question_correct);
            Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_trivia_hero), question_correct);
        }

        if(game_difficulty == 1 && score > 400) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_easy_level_expert));
        if(game_difficulty == 2 && score > 800) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_medium_level_expert));
        if(game_difficulty == 3 && score > 1200) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_hard_level_expert));

        if(game_difficulty == 1 && score > 430) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_easy_level_freak));
        if(game_difficulty == 2 && score > 860) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_medium_level_freak));
        if(game_difficulty == 3 && score > 1290) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_hard_level_freak));

        if(game_difficulty == 1 && question_wrong == 10) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_noob_fairy));
        if(game_difficulty == 2 && question_wrong == 10) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_noob_fairy));
        if(game_difficulty == 3 && question_wrong == 10) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_dont_give_up));

        if(game_difficulty == 1 && total_buttons_wrong == 0 && question_correct == 10) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_play__easy));
        if(game_difficulty == 2 && total_buttons_wrong == 0 && question_correct == 10) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_play__medium));
        if(game_difficulty == 3 && total_buttons_wrong == 0 && question_correct == 10) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_play__hard));
    }

    //updates total score leadeboard
    private void score_total_update(){

        //update difficulty leaderboard score
        if(difficulty_setting != 5) {   //temporary will not post score for random difficulty
            Games.Leaderboards.submitScore(mGoogleApiClient, getString(leaderboard_name), score);
        }

        //update total score
        //request data from server
        PendingResult<Leaderboards.LoadPlayerScoreResult> pendingResult = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getString(R.string.leaderboard_total_score), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_SOCIAL);
        ResultCallback<Leaderboards.LoadPlayerScoreResult> scoreCallback = new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                //gets player's score from server
                LeaderboardScore scoresBuffer = loadPlayerScoreResult.getScore();
                long score_local = 0;
                //test if player has any score
                if(scoresBuffer != null){
                    score_local = scoresBuffer.getRawScore();
                    Log.e("Retrieved Total Score\n",score_local+"");
                }
                Games.Leaderboards.submitScore(mGoogleApiClient,getString(R.string.leaderboard_total_score), score+score_local);
                Log.e("Total Score Uploaded", "TRUE");
            }
        };
        pendingResult.setResultCallback(scoreCallback);
    }

    //will save the scores for future upload
    private void score_save_later(){
        SharedPreferences.Editor edit = settings.edit();
        switch (difficulty_setting){
            case 1:
                edit.putInt("saved_score_easy",score);
                break;
            case 2:
                edit.putInt("saved_score_medium",score);
                break;
            case 3:
                edit.putInt("saved_score_hard",score);
                break;
        }
        edit.putInt("saved_total_score", score+settings.getInt("saved_total_score",0));
        edit.commit();
    }

    //animation function for the start of activity
    private void animate_start() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        Animation anim_score = AnimationUtils.loadAnimation(this, R.anim.anim_right_in_translate);
        lin_cats.startAnimation(anim);
        text_score.startAnimation(anim_score);
    }

    //CHANGE DISPLAY STATE OF ANSWER WITH FADE IN / OUT ANIM
    private void answer_display(String action){
        switch (action){
            case "show":
                lin_answer.setVisibility(View.VISIBLE);
                lin_answer.setAlpha(0);
                lin_answer.animate().setDuration(animation_time).setInterpolator(interpolator).alpha(1f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                }).start();
                break;
            case "hide":
                lin_answer.animate().setDuration(animation_time).setInterpolator(interpolator).alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        lin_answer.setVisibility(View.GONE);
                    }
                }).start();
                break;
        }
    }

    //function handles text view generation for answer letters
    private void answer_add_views(){
        lin_answer.removeAllViews();
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER);
        line.setDividerDrawable(getResources().getDrawable(R.drawable.vertical_divider));
        line.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        line.setMinimumHeight(dpToPx(36)); //SOLVES shadow clipping in 5.0+
        lin_answer.addView(line);

        float width =  DpWidth();
        int max_width = (int) Math.ceil(width/(float)12);

        //contor used for answers id starting with 200
        Integer cont_id = 200;

        for (Character ch : answer.toCharArray()) {
            TextView t = new TextView(this);

            t.setGravity(Gravity.CENTER);
            t.setTypeface(font);
            t.setTextSize(20);
            t.setMaxWidth(dpToPx(max_width));
            t.setTextColor(Color.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                t.setElevation(5);       //implement elevation for 5.0+
            }
            t.setId(cont_id);
            t.setBackgroundDrawable(getResources().getDrawable(R.drawable.transition_answer));
            t.setPadding(dpToPx(4),0, dpToPx(4),0);

            if (ch.compareTo(" ".charAt(0)) == 0) {
                line = new LinearLayout(this);
                line.setOrientation(LinearLayout.HORIZONTAL);
                line.setGravity(Gravity.CENTER);
                line.setDividerDrawable(getResources().getDrawable(R.drawable.vertical_divider));
                line.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                line.setMinimumHeight(dpToPx(36));
                lin_answer.addView(line);
                cont_id++;
            } else {
                t.setText(" ");
                line.addView(t);
                cont_id++;
            }
        }
    }

    private void coin_award(boolean pressed){
        if(pressed == true){
            coin_awarder++;
        }else{
            coin_awarder = 0;
        }
        if(coin_awarder == coin_awarder_limit){
            display_message(" +1");
            coin_awarder = 0;
            settings_coins_update(coins_left() + 1);
        }
    }

    //generates the scores number
    private void score_update(){
        int time_sub =(int) Math.ceil((1-(millis_buffer)/(float)(question_time)) * score_per_question/2.0);
        int wrong_sub =(int) Math.ceil((pressed_wrong / (float) max_wrong * 0.5 * score_per_question));
        int bonus = score_per_question - time_sub - wrong_sub;

        Log.e("time_sub", time_sub+"");
        Log.e("wrong_Sub", wrong_sub+"");
        ValueAnimator val = ValueAnimator.ofInt(score,score+bonus);
        val.setDuration(bonus * 15);
        val.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                text_score.setText("Score\n"+  animation.getAnimatedValue());
            }
        });

        val.start();
        score = score + bonus;
    }

    private void answer_display_refresh(ArrayList<Character> answer, Character pressed) {
        //add revealed word to layout
        for (int i = 0; i < answer.size(); i++) {
            if (answer.get(i) == pressed) {
                answer_replace_char(i + 200, answer.get(i));
            }
        }
    }

    //function will replace answer chars view from linear layout with animation
    private void answer_replace_char(final Integer char_id, Character change) {
        final TextView text = (TextView) findViewById(char_id);
        final LinearLayout lin = (LinearLayout) text.getParent();
        final int index = lin.indexOfChild(text);

        //transition animation for the background drawable
        TransitionDrawable trans = (TransitionDrawable) text.getBackground();
        trans.startTransition(1000);
        text.setText(change.toString());
    }

    //display answer if mistaken
    private void answer_replace_lost(){
        int cont_id = 200;
        for(int i = 0; i < ans_arr.size(); i++){
            if(ans_arr.get(i) != " ".charAt(0) && ans_pressed.contains(ans_arr.get(i)) == false) {
                TextView txt = (TextView) findViewById(cont_id + i);
                txt.setBackgroundDrawable(getResources().getDrawable(R.drawable.answer_red));
                txt.setText(ans_arr.get(i).toString());
            }
        }
    }

    //function to dispaly the generated buttons, connected to buttons_display()
    private void buttons_add_views(){
        btn_grid.removeAllViews();

        for (int i = 0; i < buttons.size(); i++) {
            Button t = new Button(this);

            t.setText(buttons.get(i).toString());
            t.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_lollipop));
            t.setTextColor(getResources().getColor(R.color.abc_primary_text_material_light));
            GridLayout.LayoutParams par = new GridLayout.LayoutParams();
            par.width = dpToPx((int)DpWidth()/6);
            par.height = dpToPx((int)DpWidth()/6);
            par.setMargins(5,5,5,5);
            t.setLayoutParams(par);

            t.setTextSize(30);
            t.setTypeface(font);
            t.setGravity(Gravity.CENTER);
            //generate id starting with 100
            t.setId(100 + i);

            //set clicker
            final int finalI = i;
            if(buttons.get(i) == "?".charAt(0)) {
                t.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_hints));
                t.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buttons_reveal_help(100+ finalI);
                        Log.e("CLICK","TRUE");
                    }
                });
            }else{
                t.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buttons_click(100 + finalI);
                    }
                });
            }
            btn_grid.addView(t);
        }
    }

    private void game_end(){
        answer_replace_lost();          //REVEAL ANSWER
        prog_bar.setProgress(0);        //hide any procees on bar

        //FADE OUT BUTTONS AND DISPLAY STATISTICS
        btn_grid.animate().alpha(0f).setDuration(animation_time).setInterpolator(interpolator).setStartDelay(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                buttons_enabler(false);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                btn_grid.setVisibility(View.GONE);
                score_final_display();
            }
        }).start();
    }

    private void buttons_after_press(int pressed_id, boolean correct) {
        Button pressed = (Button) findViewById(pressed_id);
        pressed.setEnabled(false);

        //change background for buttons after click
        if(correct == true){
            pressed.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_lollipop_true));
            AnimationDrawable ani = (AnimationDrawable) pressed.getBackground();
            ani.start();
            pressed.setTextColor(getResources().getColor(R.color.white));
        }else{
            pressed.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_lollipop_false));
            AnimationDrawable ani = (AnimationDrawable) pressed.getBackground();
            ani.start();
            pressed.setTextColor(getResources().getColor(R.color.white));
        }
    }

    //GENERATE RANDOM LETTER
    private Character random_letter(){
        Random rnd = new Random();
        Character cha = (Character) randomchars.charAt(rnd.nextInt(randomchars.length()));
        return cha;
    }

    //HELP METHOND TO REVEAL 30% OF ANSWER
    private void buttons_reveal_help(int id){
        TextView pressed = (TextView) findViewById(id);
        pressed.setVisibility(View.INVISIBLE);
        pressed.setEnabled(false);

        if(coins_left()-1 == 0){
            display_message(" 0");
        }else {
            display_message(" " + (coins_left() - 1));
        }

        //update hints left
        settings_coins_update(coins_left() - 1);

        HashSet uniq_ans = new HashSet();
        uniq_ans.addAll(ans_arr);
        ArrayList<Character> uniq = new ArrayList<Character>();
        uniq.addAll(uniq_ans);
        Collections.shuffle(uniq);

        int size = uniq_ans.size();
        for(int i = 0; i < uniq_ans.size()*0.3 ; i++)
        {
            for(int j = 100; j < 115; j++){
                TextView btn = (TextView) findViewById(j);
                if(btn.getText().charAt(0) == (uniq.get(i)) && !ans_pressed.contains(btn.getText().charAt(0))){
                    btn.performClick();
                }
            }
        }
    }

    //functie pentru litere.onclick
    private void buttons_click(int id){

        boolean correct_press = false;
        TextView t = (TextView) findViewById(id);
        Character c_btn = t.getText().charAt(0);

        //testeaza daca litera face parte din raspuns
        if(ans_arr.contains(t.getText().charAt(0))){
            ans_pressed.add(t.getText().charAt(0));
            pressed_correct++;
            total_buttons_correct++;
            correct_press = true;
        }else{
            pressed_wrong++;
            total_buttons_wrong++;
        }

        //updates answer display
        answer_display_refresh(ans_arr, c_btn);
        //update pressed buttons drawable
        buttons_after_press(id, correct_press);
        coin_award(correct_press);

        //next question for pressed wrong
        if(pressed_wrong == max_wrong) {
            //reset pressed variables
            question_wrong++;
        }

        if(answer_check_complete() == true){
            score_update();
            question_correct++;
        }

        //action for word completion
        if(answer_check_complete() == true || (pressed_wrong == max_wrong)){
            //test if the timer is gone when change the question
            if(question_counter < question_number){
                question_pause();
            }else{
                timer.cancel();             //solves some weird animation bug
                game_end();
            }
        }

        if(ans_arr.contains(c_btn) == true ){
           // Log.e("text char press", "TRUE");
        }
    }

    //PAUSE TEST AFTER ANSWER
    private void question_pause(){
        answer_replace_lost();
        buttons_display("hide");
        inter_display("show");
        timer.cancel();
        Log.e("FUNCTION","question_pause");
    }

    //RESUME TEST ON BUTTON PRESS
    private void question_resume(){
        question_animate("hide","");
        answer_display("hide");

        lin_inter.setAlpha(1f);
        lin_inter.animate().setStartDelay(0).setDuration(animation_time).setInterpolator(interpolator).alpha(0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                btn_nextq.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                question_next();
                lin_inter.setVisibility(View.GONE);
            }
        }).start();
    }

    //DISPLAY NEXT QUESTION
    private void question_next(){
        //reset pressed variables
        pressed_correct = 0;
        pressed_wrong = 0;
        question_read_db_rand();        //read new questions form database

        //generate new views
        answer_add_views();
        buttons_add_views();

        text_question_current.setText(question_counter+"/10");

        question_animate("show", category.toUpperCase() + "\n" + question);
        answer_display("show");
        buttons_display("show");

        timer_create();
    }

    //reads new questions from database and sets variables
    private void question_read_db_rand(){

        //0 questions 1 answer 2 categpry 3 difficulty
        question = questions_array.get(question_counter*3);
        answer = questions_array.get(question_counter*3+1);
        category = questions_array.get(question_counter*3+2);
        question_counter++;

        //delete previously pressed chars
        ans_pressed.clear();
        ans_pressed.add(" ".charAt(0));
        //complete answer character array
        ans_arr.clear();
        for(Character ch: answer.toCharArray()){
            ans_arr.add(ch);
        }
        buttons_generate(answer);   //generates the random chars for buttons
    }

    //CHECKS IF ANSWER IS COMPLETED
    private boolean answer_check_complete(){
        for( int i = 0; i < ans_arr.size(); i++){
            if( ans_pressed.contains(ans_arr.get(i)) != true){
                return false;
            }
        }
        Log.e("Word Completed", "TRUE");
        return true;
    }

    //GENERATE 16 RANDOM CHARS CONTAINING THE ANSWER ALSO
    private void buttons_generate(String ans) {
        ArrayList<Character> a = new ArrayList<Character>();
        Character cha = null;
        Random rnd = new Random();
        buttons.clear();
        //adds answer letters to array
        for(Character ch: ans.toCharArray()){
            if(buttons.contains(ch) == false && ch != " ".charAt(0)){
                buttons.add(ch);
            }
        }

        //completes the array with random letters up to 16
        while(buttons.size() < 16){
            Character c = random_letter();
            if(buttons.contains(c) == false){
                buttons.add(c);
            }
        }

        if(buttons_sort_alpha == true){
            Collections.sort(buttons, new Comparator<Character>() {
                @Override
                public int compare(Character lhs, Character rhs) {
                    return lhs.compareTo(rhs);
                }
            });
        }else {
            Collections.shuffle(buttons);
        }

        //adds the hints button if there are hints left
        if(coins_left() > 0 && ans_arr.size() > 5) {
            boolean tester = false;
            while (tester == false) {
                int ran = rnd.nextInt(15);
                if (!ans_arr.contains(buttons.get(ran))) {
                    buttons.set(ran, "?".charAt(0));
                    tester = true;
                }
            }
        }
        Log.e("Answer", a.toString());
        Log.e("Answer Chars:", buttons.toString());
    }

    //DISPLAY MESAGE ON TOP OF THE SCREEN, COULD BE USED FOR ANY TIME OF INFORMATION DURING GAMEPLAY
    private void display_message(String text){
        int[] locations = new int[2];
        lin_top.getLocationOnScreen(locations);
        int left = locations[0];
        int top = locations[1];
        Log.e("Location",left+top+"");

        final LinearLayout message = new LinearLayout(this);
        message.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, prog_bar.getId());
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        message.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            message.setElevation(dpToPx(7));       //implement elevation for 5.0+
            message.setZ(dpToPx(7));
        }
        message.setBackgroundColor(color_extraDark);
        message.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
        message.setGravity(Gravity.CENTER);
        message.bringToFront();

        final ImageView img = new ImageView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.height = dpToPx(24);
        lp.width = dpToPx(24);
        img.setLayoutParams(lp);
        img.setImageResource(R.drawable.icon_coin);
        img.setColorFilter(getResources().getColor(R.color.white));

        final TextView txt = new TextView(this);
        txt.setTextColor(getResources().getColor(R.color.white));
        txt.setTextSize(17);
        txt.setTypeface(font);
        txt.setText(text);

        message.addView(img);
        message.addView(txt);

        //adds message layout
        rel_base.addView(message);

        message.setAlpha(0.0f);
        message.animate().alpha(1.0f).setDuration(animation_time).setInterpolator(interpolator).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                message.animate().alpha(0.0f).setStartDelay(1000).setDuration(animation_time).setInterpolator(interpolator).start();
            }
        }).start();
    }

    //TIMER GENERATE
    private void timer_create() {
        if(timer != null) timer.cancel();
        timer = new CountDownTimer(question_time, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                prog_bar.setProgress((int) (((double)millisUntilFinished / question_time) * 100));
                millis_buffer = (int)millisUntilFinished;
            }
            @Override
            public void onFinish() {
                if(question_counter == question_number){
                    game_end();
                }else {
                    question_pause();
                }
            }
        };
        timer.start();
    }

    //CONVERT DIP to PX
    private static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
    //CONVERT PX TO DIP
    private static int pxToDp(int px){
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
    //MEASURE SCREEN WIDTH IN DP
    private float DpWidth(){
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return dpWidth;
    }
    //MEASURE SCREEN HEIGHT IN DP
    private float DpHeight(){
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.heightPixels / displayMetrics.density;
        return dpWidth;
    }

    //TEST INTERNET CONNECTION
    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //overrides all fonts at start , later generated textviews need recall the method or static setting of typeface
    private void overrideFonts(final Context context, final View v) {
        Typeface new_font = Typeface.createFromAsset(context.getAssets(), "typeface/bubblegum.otf");
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof TextView ) {
                ((TextView) v).setTypeface(new_font);
            }
        } catch (Exception e) {
        }
    }

//    private void top_ads(){
//        Banner banner = Banner.create(this, new Banner.BannerListener() {
//            @Override
//            public void bannerOnReceive(Banner banner) {
//
//            }
//
//            @Override
//            public void bannerOnFail(Banner banner, String s, Throwable throwable) {
//
//            }
//
//            @Override
//            public void bannerOnTap(Banner banner) {
//
//            }
//        });
//        RelativeLayout.LayoutParams para= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//        para.addRule(RelativeLayout.CENTER_IN_PARENT);
//        para.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        para.width = (int)dpToPx((int)(DpWidth()*0.65));
//        para.height = (int)(para.width/6.4);
//
//        banner.setLayoutParams(para);
//        lin_top.addView(banner);
//    }

    //overides back buttons pressed not to exit activity
    @Override
    public void onBackPressed(){
        if(!started) finish();
        back_pressed++;
        if(back_pressed == 1){
            game_end();
            if(started == true){
                timer.cancel();                         //prevent timer from exception
                btn_grid.setVisibility(View.GONE);      //solves not fading out button grid after back button pressed bug
            }else{
                lin_cats.setVisibility(View.GONE);     //if back pressed imediatly hide start btn
            }
        }else{
            finish();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        db.close();
    }
}