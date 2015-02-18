package com.itmc.instanttrivia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;

import com.google.example.games.basegameutils.GameHelper;
import com.tapfortap.Banner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

public class Game_Timer extends Activity{

    TextView text_question;
    TextView text_score;
    TextView text_question_current;
    TextView text_final_score;

    LinearLayout lin_cats;
    LinearLayout lin_answer;
    LinearLayout lin_bot;
    LinearLayout lin_gratz;
    RelativeLayout lin_top;
    RelativeLayout rel_base;
    GridLayout btn_grid;

    ProgressBar prog_bar;

    String question;
    String answer;
    String category;
    String randomchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    SharedPreferences settings;

    ArrayList<Character> buttons;
    ArrayList<Character> ans_arr;
    ArrayList<Character> ans_pressed;

    int color_extraDark;

    Typeface font_regular;
    Typeface font_thin;
    Typeface font_bold;

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

        //initialize fonts
        font_regular = Typeface.createFromAsset(getAssets(), "typeface/RobotoRegular.ttf");
        font_bold = Typeface.createFromAsset(getAssets(), "typeface/RobotoBold.ttf");
        font_thin = Typeface.createFromAsset(getAssets(), "typeface/RobotoThin.ttf");

        //define question text
        text_question = (TextView) findViewById(R.id.text_question);
        text_question.setTypeface(font_regular);
        text_score = (TextView) findViewById(R.id.text_score);
        text_score.setTypeface(font_bold);
        text_question_current = (TextView) findViewById(R.id.text_question_current);
        text_question_current.setTypeface(font_bold);
        text_final_score = (TextView)findViewById(R.id.text_final_score);

        lin_answer = (LinearLayout) findViewById(R.id.linear_answer);
        lin_bot = (LinearLayout) findViewById(R.id.linear_bot);
        lin_cats = (LinearLayout) findViewById(R.id.linear_cats);
        lin_gratz = (LinearLayout) findViewById(R.id.linear_gratz);
        lin_top = (RelativeLayout) findViewById(R.id.linear_topbar);
        rel_base = (RelativeLayout) findViewById(R.id.relative_base);
        btn_grid = (GridLayout) findViewById(R.id.buttons_grid);

        prog_bar = (ProgressBar) findViewById(R.id.timer_bar);

        //sets colors for layout
        Theme_Setter_Views();

        //set relative size for questions textview
        text_question.setTextSize((float) (DpHeight()/40.0));

        ViewGroup.LayoutParams p = text_question.getLayoutParams();
        p.height = dpToPx((int) (DpHeight()/5.0));

        //declare answer chars store , and store answer in array
        buttons = new ArrayList<Character>();
        ans_arr = new ArrayList<Character>();
        ans_pressed = new ArrayList<Character>();

        //ads
        Banner banner = Banner.create(this, new Banner.BannerListener() {
            @Override
            public void bannerOnReceive(Banner banner) {

            }

            @Override
            public void bannerOnFail(Banner banner, String s, Throwable throwable) {

            }

            @Override
            public void bannerOnTap(Banner banner) {

            }
        });
        RelativeLayout.LayoutParams para= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        para.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        para.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        para.width = (int)dpToPx((int)(DpWidth()*0.75));
        para.height = (int)(para.width/6.4);

        banner.setLayoutParams(para);
        lin_top.addView(banner);

        //start database operator
        db = new DbOP(this);
        db.startdb();

        //temporary fix passing of mGoogleApiClient form start activity to this
        Log.e("SignIn Status before conection:", settings.getBoolean("SIGNED_IN",false)+"");
        if(settings.getBoolean("SIGNED_IN",false) == true){
            GameHelper gameHelper = new GameHelper(this,GameHelper.CLIENT_GAMES);
            gameHelper.setup(new GameHelper.GameHelperListener() {
                @Override
                public void onSignInFailed() {
                }

                @Override
                public void onSignInSucceeded() {
                }
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
                Views_Editor("purple");
                color_extraDark = getResources().getColor(R.color.purple_900);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    getWindow().setStatusBarColor(getResources().getColor(R.color.purple_700));
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
    }

    private void categories_generate_list(){
        final ArrayList<String> categories = db.read_cats(difficulty_setting);
        Log.e("Categories", categories.toString());

        for( int i = 0; i < categories.size(); i=i+2){

            LinearLayout lin = new LinearLayout(this);
            lin.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

            lin.setLayoutParams(params);
            lin.setGravity(Gravity.CENTER_VERTICAL);
            lin.setBackgroundDrawable(getResources().getDrawable(R.drawable.options_ripple));

            ImageView img = new ImageView(this);
            img.setImageResource(getResources().getIdentifier("icon_cat_"+categories.get(i+1),"drawable","com.itmc.instanttrivia"));
            img.setPadding(dpToPx(20),0,dpToPx(20),0);
            img.setColorFilter(getResources().getColor(R.color.grey_700));

            TextView text = new TextView(this);

            text.setText(categories.get(i));
            text.setTextSize(15);
            text.setPadding(dpToPx(25),dpToPx(15),dpToPx(25),dpToPx(15));

            final int i2 = i;

            lin.addView(img);
            lin.addView(text);
            lin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                question_category = Integer.parseInt(categories.get(i2+1));

                question_read_db_rand(); // reads question on game start
                text_question.setText(category.toUpperCase()+"\n"+question);
                answer_display_hidden();
                //porneste animatia pentru questions
                animate_quest();
                //genereza si porneste timer
                timer_create();
                started = true;
                }
            });

            lin_cats.addView(lin);
        }
    }


      //sets difficulty variables
    private void difficulty_set(int difficulty){
        game_difficulty = difficulty;
        switch (difficulty){
            case 1:
                question_number = 10;
                max_wrong = 5;
                score_per_question = 50;
                question_time = 40000;
                leaderboard_name = R.string.leaderboard_time_trial__easy_level;
                difficulty_setting = 1;
                coin_awarder_limit = 20;
                break;
            case 2:
                question_number = 10;
                max_wrong = 4;
                score_per_question = 100;
                question_time = 35000;
                leaderboard_name = R.string.leaderboard_time_trial__medium_level;
                difficulty_setting = 2;
                coin_awarder_limit = 15;
                break;
            case 3:
                question_number = 10;
                max_wrong = 3;
                score_per_question = 150;
                question_time = 30000;
                leaderboard_name = R.string.leaderboard_time_trial__hard_level;
                difficulty_setting = 3;
                buttons_sort_alpha = false;
                coin_awarder_limit = 10;
                break;
            case 5:
                question_number = 10;
                max_wrong = 4;
                score_per_question = 75;
                question_time = 35000;
                //leaderboard_name = R.string.leaderboard_time_trial__hard_level;
                difficulty_setting = 5;
                buttons_sort_alpha = false;
                coin_awarder_limit = 15;
                break;
        }
    }

    //animates question textview
    private void animate_quest() {

        buttons_generate(answer);
        buttons_display();

        //animate start button after click to retract back under question
        Animation anim_start_back = AnimationUtils.loadAnimation(this, R.anim.anim_top_top);

        anim_start_back.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lin_cats.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {            }
        });
        lin_cats.startAnimation(anim_start_back);
    }

    private void question_update(final String text){

            TextView txt = new TextView(this);
            txt.setLayoutParams(text_question.getLayoutParams());
            txt.setTextSize(text_question.getTextSize());

            txt.setText(text);

            final int Height = dpToPx((int) (DpHeight()/5.0));

            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if(interpolatedTime == 1){
                        text_question.setVisibility(View.GONE);
                    }else{
                        text_question.getLayoutParams().height = Height - (int)(Height * interpolatedTime);
                        text_question.requestLayout();
                    }
                }
                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            // 1dp/ms
            a.setDuration(750);
            a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                text_question.setText(text);

                text_question.getLayoutParams().height = 0;
                text_question.setVisibility(View.VISIBLE);
                Animation a = new Animation()
                {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        text_question.getLayoutParams().height =  (int)(Height * interpolatedTime);
                        text_question.requestLayout();
                    }

                    @Override
                    public boolean willChangeBounds() {
                        return true;
                    }
                };

                // 1dp/ms
                a.setDuration((int)(Height / text_question.getContext().getResources().getDisplayMetrics().density)*10);
                text_question.startAnimation(a);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
            text_question.startAnimation(a);
    }

    private void question_hide_end(){
        //animation to hide the question
        final int Height = dpToPx((int) (DpHeight()/5.0));
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    text_question.setVisibility(View.GONE);
                }else{
                    text_question.getLayoutParams().height = Height - (int)(Height * interpolatedTime);
                    text_question.requestLayout();
                }
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration(750);
        text_question.startAnimation(a);

        //answer fade out
        lin_answer.setAlpha(1f);
        lin_answer.animate().setDuration(1000).alpha(0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                lin_answer.setVisibility(View.GONE);
            }
        }).start();

        buttons_enabler(false);
        //buttons fade out
        lin_bot.setAlpha(1f);
        lin_bot.animate().setDuration(1000).alpha(0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                btn_grid.removeAllViews();
                score_final_display();
                lin_bot.clearAnimation();
            }
        }).start();
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
        text_final_score.setTypeface(font_bold);

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


        lin_bot.animate().setDuration(1000).alpha(1f).setListener(new AnimatorListenerAdapter() {
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
                    Log.e("Retrieved Total Score:",score_local+"");
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

    //conversie dp to pixels, util pentru animatii cu layoutparams
    private static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private static int pxToDp(int px){
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    private float DpWidth(){
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();

        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return dpWidth;
    }
    private float DpHeight(){
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();

        float dpWidth = displayMetrics.heightPixels / displayMetrics.density;
        return dpWidth;
    }

    //animation function for the start of activity
    private void animate_start() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        Animation anim_score = AnimationUtils.loadAnimation(this, R.anim.anim_right_in_translate);

        TextView score = (TextView) findViewById(R.id.text_score);

        lin_cats.startAnimation(anim);
        score.startAnimation(anim_score);
    }

    //function handles animation for answer display connected to answer_display_hidden2();
    private void answer_display_hidden() {

        //display update question number on top
        text_question_current.setText(question_counter+"/"+question_number);

        //animation definition
        Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);
        fade_out.setStartOffset(1000);
        final Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        //animation action listener on ending fade in new answer
        fade_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lin_answer.removeAllViews();
                answer_display_hidden2();
                lin_answer.startAnimation(fade_in);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        //displays only fade in for first questions , fade in out for other questions
        if(question_counter > 1) {
            lin_answer.startAnimation(fade_out);
        }else{
            lin_answer.startAnimation(fade_in);
            answer_display_hidden2();
        }
    }

    //function handles text view generation for answer letters
    private void answer_display_hidden2(){
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
            t.setTypeface(Typeface.MONOSPACE);
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
                text_score.setText("Score: "+  animation.getAnimatedValue());
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

    //display buttons with animation
    private void buttons_display() {

        final LinearLayout line_bot = (LinearLayout) findViewById(R.id.linear_bot);
        //if is not first questions do fade in fade out else do just fade in
        if(question_counter > 1){
            line_bot.setAlpha(1f);
            line_bot.animate()
                    .alpha(0f)
                    .setDuration(1000)
                    .setStartDelay(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            line_bot.animate()
                                    .alpha(1f)
                                    .setDuration(1000)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            buttons_enabler(true);      //click enables after fade in
                                            line_bot.clearAnimation();
                                        }
                                    })
                                    .start();
                            buttons_display2();
                            buttons_enabler(false);     //click disabled after generation
                        }
                    })
                    .start();
        }else {
            line_bot.setAlpha(0f);
            line_bot.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            buttons_enabler(true);      //click enabled at animation end
                            line_bot.clearAnimation();
                        }
                    })
                    .start();
            buttons_display2();
            buttons_enabler(false);         //click disabled on start
        }
    }

    //function to dispaly the generated buttons, connected to buttons_display()
    private void buttons_display2(){
        GridLayout btn_grid = (GridLayout) findViewById(R.id.buttons_grid);
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

    //genereaza timerul initial
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
                    timer_end();
                }else {
                    answer_replace_lost();  //reveal answer
                    question_next();
                }
            }
        };
        timer.start();
    }

    private void timer_end(){

        //start quesiton hide animation
        question_hide_end();

        //hide any procees on bar
        prog_bar.setProgress(0);

        //hide answer and questions
        text_question.setText("");
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

    //verifica daca literele afisate sunt in raspuns
    private boolean check_letter_exist(){
        for(int i = 0; i < 8; i++){
            if(ans_arr.contains(buttons.get(i))){
                return true;
            }
        }
        return false;
    }

    private Character random_letter(){
        Random rnd = new Random();
        Character cha = (Character) randomchars.charAt(rnd.nextInt(randomchars.length()));
        return cha;
    }

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
        txt.setTypeface(font_regular);
        txt.setText(text);

        message.addView(img);
        message.addView(txt);

        //adds message layout
        rel_base.addView(message);


        message.setAlpha(0.0f);
        message.animate().alpha(1.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                message.animate().alpha(0.0f).setStartDelay(1000).setDuration(500).start();
            }
        }).start();
    }

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
            answer_replace_lost();
        }
        if(check_completion() == true){
            score_update();
            question_correct++;
        }

        //action for word completion
        if(check_completion() == true || (pressed_wrong == max_wrong)){
            //test if the timer is gone when change the question
            if(question_counter < question_number){
                question_next();
            }else{
                timer.cancel();             //solves some weird animation bug
                timer_end();
            }
        }

        if(ans_arr.contains(c_btn) == true ){
           // Log.e("text char press", "TRUE");
        }
    }

    private void question_next(){
        //reset pressed variables
        pressed_correct = 0;
        pressed_wrong = 0;
        question_read_db_rand();                 //read new questions form database
        answer_display_hidden();        //display answer
        question_update(category.toUpperCase()+"\n"+question);      //update text with animation
        buttons_generate(answer);
        buttons_display();
        buttons_enabler(false);         //click disables after word completion, reactivated in buttons display animation end
        timer_create();
    }

    //reads new questions from database and sets variables
    private void question_read_db_rand(){

        String[] questy;
        questy = db.read_rand_question_difficulty(difficulty_setting,question_category);  // 5 diff for random questions without difficulty

        //0 questions 1 answer 2 categpry 3 difficulty
        question = questy[0];
        answer = questy[1];
        category = questy[2];
        question_counter++;

        //delete previously pressed chars
        ans_pressed.clear();
        ans_pressed.add(" ".charAt(0));
        //complete answer character array
        ans_arr.clear();
        for(Character ch: answer.toCharArray()){
            ans_arr.add(ch);
        }
    }

    //verifica daca cuvantul este completat
    private boolean check_completion(){
        for( int i = 0; i < ans_arr.size(); i++){
            if( ans_pressed.contains(ans_arr.get(i)) != true){
                return false;
            }
        }
        Log.e("Word Completed", "TRUE");
        return true;
    }

    //generate 8 random chars containing minimum 3 answer chars at beggining
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

    //overides back buttons pressed not to exit activity
    @Override
    public void onBackPressed(){

        if(game_difficulty == 0) finish();

        back_pressed++;
        if(back_pressed == 1){
            timer_end();
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
    public void onDestroy()
    {
        super.onDestroy();
        db.close();
    }
}

