package com.itmc.instanttrivia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.example.games.basegameutils.GameHelper;
import com.tapfortap.Banner;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Game_Timer extends Activity{

    TextView text_question, text_score, text_question_current, text_final_score, text_difficulty, text_category, text_view_category;
    ScrollView cats_scroll;
    Button btn_nextq;
    LinearLayout lin_answer, lin_bot, lin_gratz, lin_inter, lin_difficulty;
    RelativeLayout lin_top, rel_base;
    GridLayout btn_grid, grid_categories;
    ProgressBar prog_bar, prog_score;
    RadioButton radio_easy, radio_medium, radio_hard, radio_random;
    ImageView image_help, image_time;
    Banner banner;

    int animation_time = 700;

    ArrayList<String> questions_array;

    //animation interpolator for all animations
    TimeInterpolator interpolator;

    //FACEBOOK
    ShareDialog shareDialog;
    CallbackManager callbackManager;

    String question_id;
    String question;
    String answer;
    String category;
    String randomchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    SharedPreferences settings;

    ArrayList<Character> buttons;
    ArrayList<Character> ans_arr;
    ArrayList<Character> ans_pressed;

    int color_extraDark;
    Typeface font;

    Boolean started = false;
    Boolean game_paused = null;

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
    int helper_time_used = 0;       //will count how many times the helper is used

    int coin_awarder = 0;
    int coin_awarder_limit = 0;

    //buffer variables for millis left to calc score
    int millis_buffer = 0;

    CountDownTimer timer = null;

    private DbOP db;

    public GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences("InstantOptions", MODE_PRIVATE);
        Theme_Setter();

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
        text_view_category = (TextView) findViewById(R.id.text_view_category);

        btn_nextq = (Button)findViewById(R.id.button_nextq);

        radio_easy = (RadioButton) findViewById(R.id.radio_easy);
        radio_medium = (RadioButton) findViewById(R.id.radio_medium);
        radio_hard = (RadioButton) findViewById(R.id.radio_hard);
        radio_random = (RadioButton) findViewById(R.id.radio_random);

        image_help = (ImageView) findViewById(R.id.image_help);
        image_time = (ImageView) findViewById(R.id.image_time);

        lin_answer = (LinearLayout) findViewById(R.id.linear_answer);
        lin_bot = (LinearLayout) findViewById(R.id.linear_bot);
        lin_gratz = (LinearLayout) findViewById(R.id.linear_gratz);
        lin_inter = (LinearLayout) findViewById(R.id.linear_inter);
        lin_difficulty = (LinearLayout) findViewById(R.id.linear_difficulty);

        lin_top = (RelativeLayout) findViewById(R.id.linear_topbar);
        rel_base = (RelativeLayout) findViewById(R.id.relative_base);
        btn_grid = (GridLayout) findViewById(R.id.buttons_grid);
        grid_categories = (GridLayout) findViewById(R.id.grid_categories);

        cats_scroll = (ScrollView) findViewById(R.id.cats_scroll);
        prog_bar = (ProgressBar) findViewById(R.id.timer_bar);
        prog_score = (ProgressBar) findViewById(R.id.progress_score);

        overrideFonts(this,rel_base);

        //FACEBOOK INITIALIZE
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {

            }

            @Override
            public void onCancel() {
                Log.e("FB SHARE", "CANCEL");
            }

            @Override
            public void onError(FacebookException e) {
                Log.e("FB SHARE", "SHARING ERROR! - " + e.getMessage());
            }
        });

        //set interpolator
        interpolator = new DecelerateInterpolator(1);

        //sets colors for layout
        Theme_Setter_Views();

        //set relative size for questions textview
        text_question.setTextSize((float) (DpHeight()/35.0));

        ViewGroup.LayoutParams p = text_question.getLayoutParams();
        p.height = dpToPx((int) (DpHeight()/6.0));

        //declare answer chars store , and store answer in array
        buttons = new ArrayList<>();
        ans_arr = new ArrayList<>();
        ans_pressed = new ArrayList<>();

        //start database operator
        db = new DbOP(this);
        db.startdb();

        //temporary fix passing of mGoogleApiClient form start activity to this
        Log.e("SignIn Status b4", settings.getBoolean("SIGNED_IN",false)+"");
        if(settings.getBoolean("SIGNED_IN", false)){
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

        if(isNetworkAvailable()){
            ads_add();
        }

        btn_nextq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                question_resume();
            }
        });
        image_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hints_reveal_chars();
            }
        });
        image_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    helper_time_used++;
                    hints_extra_time();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        //HANDLE FB CALLBACKS
        callbackManager.onActivityResult(requestCode, resultCode, intent);

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

    //sets the question difficulty setting in preferences
    private void update_options_difficulty(int difficulty){
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("question_diff", difficulty);
        editor.apply();
    }

    private void difficulty_radio_reset(){
        radio_easy.setChecked(false);
        radio_medium.setChecked(false);
        radio_hard.setChecked(false);
        radio_random.setChecked(false);
    }

    //sets colors for internal views of layout
    private void Theme_Setter_Views(){
        String tester = settings.getString("Color_Theme", "Purple");
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
        int primary_color = getResources().getIdentifier("color/" + color + "_500", null, getPackageName());
        int darker_color = getResources().getIdentifier("color/"+color+"_700",null,getPackageName());
        int progress_dwg = getResources().getIdentifier("drawable/custom_progressbar_"+color,null,getPackageName());
        int left_helper = getResources().getIdentifier("drawable/hints_time_"+color+"_700",null,getPackageName());
        int right_helper = getResources().getIdentifier("drawable/hints_help_"+color+"_700",null,getPackageName());
        int final_score_helper = getResources().getIdentifier("drawable/card_inside_"+color+"_700",null,getPackageName());

        image_help.setBackground(getResources().getDrawable(right_helper));
        image_time.setBackground(getResources().getDrawable(left_helper));
        image_help.setColorFilter(getResources().getColor(R.color.white));
        image_time.setColorFilter(getResources().getColor(R.color.white));

        prog_bar.setProgressDrawable(getResources().getDrawable(progress_dwg));
        prog_score.setProgressDrawable(getResources().getDrawable(progress_dwg));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)        {
            text_question.setTextColor(getResources().getColor(darker_color));
        }else {
            text_question.setBackgroundColor(getResources().getColor(primary_color));
        }
        lin_top.setBackgroundColor((getResources().getColor(primary_color)));
        text_view_category.setTextColor(getResources().getColor(darker_color));
        text_final_score.setBackground(getResources().getDrawable(final_score_helper));
        text_difficulty.setTextColor(getResources().getColor(darker_color));
        text_category.setTextColor(getResources().getColor(darker_color));
    }

    private LinearLayout categories_generate_view(final String name, final String id){

        LinearLayout lin = new LinearLayout(this);
        lin.setOrientation(LinearLayout.VERTICAL);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = dpToPx((int) (DpWidth()*0.4));

        lin.setLayoutParams(params);
        lin.setGravity(Gravity.CENTER_VERTICAL);
        lin.setBackground(getResources().getDrawable(R.drawable.button_lollipop));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            lin.setElevation(dpToPx(2));
        }

        ImageView img = new ImageView(this);
        img.setImageResource(getResources().getIdentifier("icon_cat_" + id, "drawable", "com.itmc.instanttrivia"));
        img.setPadding(dpToPx(5), 0, dpToPx(5), 0);
        img.setColorFilter(getResources().getColor(R.color.grey_900));

        TextView text = new TextView(this);
        text.setText(name);
        text.setGravity(Gravity.CENTER);
        text.setTextSize(15);
        text.setTextColor(getResources().getColor(R.color.grey_900));
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

        for( int i = 0; i < categories.size(); i=i+2){
            LinearLayout lin = categories_generate_view(categories.get(i),categories.get(i+1));
            grid_categories.addView(lin);
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
                buttons_sort_alpha = true;
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
                buttons_sort_alpha = true;
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
                leaderboard_name = R.string.leaderboard_time_trial__random;
                difficulty_setting = 5;
                buttons_sort_alpha = true;
                coin_awarder_limit = 15;
                break;
        }
    }

    //ANIMATE AFTER CATEGORY IS CHOSEN, START OF GAMEPLAY
    private void animate_game_start() {
        question_animate("hide","");
        text_score.setAlpha(0);
        text_score.animate().alpha(1).setInterpolator(interpolator).setDuration(animation_time).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                text_score.setVisibility(View.VISIBLE);
            }
        }).start();
        text_question_current.setAlpha(0);
        text_question_current.animate().alpha(1).setInterpolator(interpolator).setDuration(animation_time).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                text_question_current.setVisibility(View.VISIBLE);
            }
        }).start();
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
                question_next();
                started = true;
            }
        }).start();
    }

    private void question_animate(String action, final String text){
        switch (action){
            case "hide":
                //QUESTION HIDE
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
                //CATEGORY HIDE
                text_view_category.animate().setDuration(animation_time).setInterpolator(interpolator).alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        text_view_category.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                    }
                }).start();
                break;
            case "show":
                //QUESTION SHOW
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
                        text_question.setText("");      //SOLVES WEIRD BUG ON ANDROID 4.0 WHERE IS SHOWS DIFFERENT QUESTION BEFORE THE ACTUAL ONE
                    }
                }).start();
                //QUESTION HIDE
                text_view_category.animate().setDuration(animation_time).setInterpolator(interpolator).alpha(1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        text_view_category.setVisibility(View.VISIBLE);
                        text_view_category.setText(category);      //SOLVES WEIRD BUG ON ANDROID 4.0 WHERE IS SHOWS DIFFERENT QUESTION BEFORE THE ACTUAL ONE
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
        return settings.getInt("Coins", 5);
    }

    private void settings_coins_update(int hints){
        SharedPreferences.Editor edit = settings.edit();
        edit.putInt("Coins", hints);
        edit.apply();
    }

    //DISPLAY FINAL SCORE DIALOG
    private void score_final_display(){
        //FACEBOOK SHARE BUTTON
        Button btn_share = (Button) findViewById(R.id.button_facebook);
        if(appInstalledOrNot("com.facebook.katana") && score > 100) {
            btn_share.setVisibility(View.VISIBLE);
            btn_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ShareDialog.canShow(ShareLinkContent.class)){
                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                .setContentTitle("Hey, I just got "+score+" points on Instant Trivia")
                                .setContentDescription("Download now from Google Play!")
                                .setContentUrl(Uri.parse("https://www.facebook.com/pages/Instant-Trivia/883932578295403"))
                                .build();
                        shareDialog.show(linkContent);
                    }
                }
            });
        }

        //UPDATE SCORES, ACUM CU TRIPLA PROTECTIE, if not SING IN setting is not true will not test, preventing NULL exception
        //send scores to google server
        if(mGoogleApiClient != null){
            Log.e("API ACTION:", "Api client is initialized");
            if(mGoogleApiClient.isConnected() && difficulty_setting != 0 && question_category!=0) {
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

        //FINAL SCORE
        LinearLayout lin_score = (LinearLayout) findViewById(R.id.linear_finalscore);
        lin_score.setVisibility(View.VISIBLE);
        text_score.setVisibility(View.INVISIBLE);
        text_final_score.setText(score + "");
        text_final_score.setTypeface(font);

        //BACK TO MAIN MENU BUTTON
        Button btn_back = (Button) findViewById(R.id.button_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //SET ROUND PROGRESS BARS
        TextView text_correct_hits = (TextView) findViewById(R.id.text_correct_hits);
        TextView text_wrong_hits = (TextView)findViewById(R.id.text_wrong_hits);
        TextView text_accuracy = (TextView)findViewById(R.id.text_accuracy);
        TextView text_stat_ans = (TextView)findViewById(R.id.text_stat_answers);
        TextView text_highest_score = (TextView)findViewById(R.id.text_highest_score);
        ProgressBar prog_correct = (ProgressBar)findViewById(R.id.prog_correct);
        ProgressBar prog_wrong = (ProgressBar) findViewById(R.id.prog_wrong);
        ProgressBar prog_acc = (ProgressBar) findViewById(R.id.prog_accuracy);

        //UPDATE STATISTICS DISPLAY
        text_correct_hits.setText(total_buttons_correct+"");
        text_wrong_hits.setText(total_buttons_wrong+"");
        int total_hits = total_buttons_correct+total_buttons_wrong;
        double accuracy = Math.ceil(((total_buttons_correct)/(float)total_hits)*100);
        text_accuracy.setText((int)accuracy+"");
        text_stat_ans.setText(question_correct+" / 10");
        text_highest_score.setText("Best score: "+highest_score_get(difficulty_setting,score));

        prog_correct.setMax(total_hits);
        prog_correct.setProgress(total_buttons_correct);
        prog_wrong.setMax(total_hits);
        prog_wrong.setProgress(total_buttons_wrong);
        prog_acc.setProgress((int) accuracy);
        prog_score.setMax(question_number);
        prog_score.setProgress(question_correct);


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

    private void achievements_categories(){
        //GEORGRAPHY ID 2
        if(category.equals("Geography")){
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_first_trip),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_beyond_borders),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_top_of_the_hill),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_cliff_jumper),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_around_the_world),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_climbing_everest),1);
        }
        //HISTORY ID 3
        if(category.equals("History")){
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_peasant),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_kings_servant),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_knight_in_armor),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_master_of_the_castle),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_ancient_pharaoh),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_worlds_conquerer),1);
        }
        //BILOGY ID 7
        if(category.equals("Biology")){
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_buzz_buzz),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_caterpillar_in_the_rain),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_survival_instinct),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_garden_of_life),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_the_beauty_of_evolution),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_king_of_the_jungle),1);
        }
        //SCIENTE AND TECH ID 11
        if(category.equals("Science & Tech")){
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_inventor_of_the_wheel),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_cant_live_without_wifi),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_dont_blow_the_lab),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_chain_reaction),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_mad_scientist),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_time_travel_discovered),1);
        }
        //CULTURE ID 13
        if(category.equals("Culture")){
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_know_yourself),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_worlds_citizen),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_mythical_beast),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_easy_to_adapt),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_true_humanity),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_pure_knowledge),1);
        }
        //ARTS ID 15
        if(category.equals("Arts")){
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_first_sketch),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_virtuoso),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_melody_maestro),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_classical_artist),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_own_art_gallery),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_best_seller),1);
        }
        //ENTERTAINMENT ID 15
        if(category.equals("Entertainment")){
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_enjoy_the_show),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_singing_in_the_rain),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_olympics_winner),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_hollywood_star),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_oscar_winner),1);
            Games.Achievements.increment(mGoogleApiClient,getString(R.string.achievement_hal_of_fame),1);
        }
    }

    //updates total score leadeboard
    private void score_total_update(){
        //update difficulty score
        Games.Leaderboards.submitScore(mGoogleApiClient, getString(leaderboard_name), score);

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
            case 5:
                edit.putInt("saved_score_random",score);
                break;
        }
        edit.putInt("saved_total_score", score+settings.getInt("saved_total_score",0));
        edit.apply();
    }

    //animation function for the start of activity
    private void animate_start() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        lin_difficulty.startAnimation(anim);
        grid_categories.startAnimation(anim);
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
                t.setElevation(3);       //implement elevation for 5.0+
            }
            t.setId(cont_id.intValue());        //PREVENTS IDE ERROR, SHOWS WARNING
            t.setBackground(getResources().getDrawable(R.drawable.transition_answer));
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
        if(pressed){
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

        //SEND TO SERVER THE RATIO
        if(isNetworkAvailable() && settings.getBoolean("stat_send",true)){
            double q_ratio = (double) bonus/score_per_question;
            new send_async_ratio().execute(question_id, q_ratio+"");
        }
        //SEND ACHIEVEMENT FOR CATEGORIES
        if(isNetworkAvailable()){
            achievements_categories();
        }
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

        //transition animation for the background drawable
        TransitionDrawable trans = (TransitionDrawable) text.getBackground();
        trans.startTransition(1000);
        text.setText(change.toString());
    }

    //display answer if mistaken
    private void answer_replace_lost(){
        int cont_id = 200;
        for(int i = 0; i < ans_arr.size(); i++){
            if(ans_arr.get(i) != " ".charAt(0) && !ans_pressed.contains(ans_arr.get(i))) {
                TextView txt = (TextView) findViewById(cont_id + i);
                txt.setBackground(getResources().getDrawable(R.drawable.answer_red));
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
            t.setBackground(getResources().getDrawable(R.drawable.button_lollipop));
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
                t.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buttons_click(100 + finalI);
                    }
                });
            btn_grid.addView(t);
        }
    }

    //TRIGGERS GAME END / SHOWS STATISTICS
    private void game_end(){
        if(game_paused){
            ads_display(false);             //hide ads

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
                    score_final_display();
                }
            }).start();
        }else {
            answer_replace_lost();          //REVEAL LAST ANSWER
            prog_bar.setProgress(0);        //hide any progres on bar
            hints_help_enable(false);       //hide right helper
            hints_time_enable(false);       //hide left helper

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
    }

    private void buttons_after_press(int pressed_id, boolean correct) {
        Button pressed = (Button) findViewById(pressed_id);
        pressed.setEnabled(false);

        //change background for buttons after click
        if(correct){
            pressed.setBackground(getResources().getDrawable(R.drawable.button_lollipop_true));
            AnimationDrawable ani = (AnimationDrawable) pressed.getBackground();
            ani.start();
            pressed.setTextColor(getResources().getColor(R.color.white));
        }else{
            pressed.setBackground(getResources().getDrawable(R.drawable.button_lollipop_false));
            AnimationDrawable ani = (AnimationDrawable) pressed.getBackground();
            ani.start();
            pressed.setTextColor(getResources().getColor(R.color.white));
        }
    }

    //GENERATE RANDOM LETTER
    private Character random_letter(){
        Random rnd = new Random();
        return randomchars.charAt(rnd.nextInt(randomchars.length()));
    }

    //GET HIGHEST SCORE
    private int highest_score_get(int difficulty, int new_score){
        //READ PREVIOUS SCORE
        int h_score = settings.getInt("highest_score"+difficulty, 0);
        //CHECK IF NEW SCORE IS BETTER
        if(new_score >= h_score){
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("highest_score"+difficulty, new_score);
            editor.apply();
            h_score = new_score;
        }
        return h_score;
    }

    //DISPLAY REVEAL ANSWER HELPER
    private void hints_help_enable(boolean state){
        if (state){
            if(coins_left()>=1){
                image_help.animate().alpha(1).setDuration(animation_time).setInterpolator(interpolator).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        image_help.setEnabled(true);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        image_help.setEnabled(false);
                        image_help.setVisibility(View.VISIBLE);
                    }
                }).start();

            }
        }else {
            image_help.animate().alpha(0).setDuration(animation_time).setInterpolator(interpolator).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    image_help.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    image_help.setEnabled(false);
                }
            }).start();
        }
    }

    //DISPLAY MORE TIME HELPER
    private void hints_time_enable(boolean state){
        if(state){
            image_time.animate().alpha(1).setDuration(animation_time).setInterpolator(interpolator).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    image_time.setEnabled(true);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    image_time.setEnabled(false);
                    image_time.setVisibility(View.VISIBLE);
                }
            }).start();
        }else {
            image_time.animate().alpha(0).setDuration(animation_time).setInterpolator(interpolator).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    image_time.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    image_time.setEnabled(false);
                }
            }).start();
        }
    }

    private void hints_extra_time(){
        timer.cancel();
        hints_time_enable(false);
    }

    //HELP METHOND TO REVEAL 30% OF ANSWER
    private void hints_reveal_chars(){

        if(coins_left()-1 == 0){
            display_message(" 0");
        }else {
            display_message(" " + (coins_left() - 1));
        }

        //update hints left
        settings_coins_update(coins_left() - 1);

        HashSet uniq_ans = new HashSet();
        uniq_ans.addAll(ans_arr);
        ArrayList<Character> uniq = new ArrayList<>();
        uniq.addAll(uniq_ans);
        Collections.shuffle(uniq);

        for(int i = 0; i < uniq_ans.size()*0.3 ; i++)
        {
            for(Integer j = 100; j < 115; j++){
                TextView btn = (TextView) findViewById(j.intValue());
                if(btn.getText().charAt(0) == (uniq.get(i)) && !ans_pressed.contains(btn.getText().charAt(0))){
                    btn.performClick();
                }
            }
        }
        hints_help_enable(false);
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
            if(isNetworkAvailable() && settings.getBoolean("stat_send",true)) {
                new send_async_ratio().execute(question_id, "0.0");
            }
        }

        if(answer_check_complete()){
            score_update();
            question_correct++;
        }

        //action for word completion
        if(answer_check_complete() || (pressed_wrong == max_wrong)){
            //test if the timer is gone when change the question
            if(question_counter < question_number){
                question_pause();
            }else{
                timer.cancel();             //solves some weird animation bug
                game_end();
            }
        }
    }

    //PAUSE TEST AFTER ANSWER
    private void question_pause(){
        game_paused = true;
        answer_replace_lost();
        buttons_display("hide");
        hints_help_enable(false);
        hints_time_enable(false);
        ads_display(true);
        inter_display("show");
        timer.cancel();
        Log.e("FUNCTION","question_pause");
    }

    //RESUME TEST ON BUTTON PRESS
    private void question_resume(){
        question_animate("hide","");
        answer_display("hide");
        ads_display(false);

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
        game_paused = false;
        //reset pressed variables
        pressed_correct = 0;
        pressed_wrong = 0;
        question_read_db_rand();        //read new questions form database

        //generate new views
        answer_add_views();
        buttons_add_views();

        text_question_current.setText(question_counter+"/10");

        question_animate("show", question);
        answer_display("show");
        buttons_display("show");

        //display helpers
        if(coins_left()>=1 && ans_arr.size()>6){
            hints_help_enable(true);
        }
        if(coins_left()>=1 && helper_time_used < 3){
            hints_time_enable(true);
        }

        timer_create();
    }

    //reads new questions from database and sets variables
    private void question_read_db_rand(){

        //0 questions 1 answer 2 categpry 3 difficulty
        question_id = questions_array.get(question_counter*4);
        question = questions_array.get(question_counter*4+1);
        answer = questions_array.get(question_counter*4+2);
        category = questions_array.get(question_counter*4+3);
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
            if(!ans_pressed.contains(ans_arr.get(i))){
                return false;
            }
        }
        Log.e("Word Completed", "TRUE");
        return true;
    }

    //GENERATE 16 RANDOM CHARS CONTAINING THE ANSWER ALSO
    private void buttons_generate(String ans) {
        buttons.clear();
        //adds answer letters to array
        for(Character ch: ans.toCharArray()){
            if(!buttons.contains(ch) && ch != " ".charAt(0)){
                buttons.add(ch);
            }
        }

        //completes the array with random letters up to 16
        while(buttons.size() < 16){
            Character c = random_letter();
            if(!buttons.contains(c)){
                buttons.add(c);
            }
        }

        if(buttons_sort_alpha){
            Collections.sort(buttons, new Comparator<Character>() {
                @Override
                public int compare(Character lhs, Character rhs) {
                    return lhs.compareTo(rhs);
                }
            });
        }else {
            Collections.shuffle(buttons);
        }

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
                    //SEND RATIO TO SERVER
                    if(isNetworkAvailable() && settings.getBoolean("stat_send", true)) {
                        new send_async_ratio().execute(question_id, "0.0");
                    }
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
        return displayMetrics.widthPixels / displayMetrics.density;
    }
    //MEASURE SCREEN HEIGHT IN DP
    private float DpHeight(){
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels / displayMetrics.density;
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
            Log.e("Font O Err",e.toString());
        }
    }

    private void ads_display(boolean state){
        if(isNetworkAvailable()) {
            if (state) {
                banner.setVisibility(View.VISIBLE);
            } else {
                banner.setVisibility(View.GONE);
            }
        }
    }

    private void ads_add(){
        banner = Banner.create(this, new Banner.BannerListener() {
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
        banner.setVisibility(View.GONE);
        RelativeLayout.LayoutParams para= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        para.addRule(RelativeLayout.CENTER_IN_PARENT);
        para.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        para.width = dpToPx((int)(DpWidth()));
        para.height = (int)(para.width/6.4);

        banner.setLayoutParams(para);
        rel_base.addView(banner);
    }

    //CHECK IF APP IS INSTALLED
    private boolean appInstalledOrNot(String uri)
    {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try
        {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            app_installed = false;
        }
        return app_installed ;
    }

    //SEND QUESTION RATIO TO SERVER
    public class send_async_ratio extends AsyncTask<String, Integer, Double> {

        @Override
        protected Double doInBackground(String... params) {
            server_send_ratio(params[0], params[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            super.onPostExecute(aDouble);
            Log.e("Server Rat","SCS");
        }

        private void server_send_ratio(String id, String ratio) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://instanttrivia.atwebpages.com/o_scripts/record_play.php");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("id", id));
                nameValuePairs.add(new BasicNameValuePair("ratio", ratio));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpClient.execute(httpPost);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e("Server Rat","FAILED");
            }
        }
    }

    //overides back buttons pressed not to exit activity
    @Override
    public void onBackPressed(){
        back_pressed++;
        Log.e("started", started.toString());
        if(!started) {
            finish();
        }else{
            if(back_pressed == 1){
                game_end();
                if(started){
                    timer.cancel();                         //prevent timer from exception
                    //btn_grid.setVisibility(View.GONE);      //solves not fading out button grid after back button pressed bug
                }else{
                    grid_categories.setVisibility(View.GONE);     //if back pressed imediatly hide start btn
                }
            }else{
                finish();
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        db.close();
    }
}