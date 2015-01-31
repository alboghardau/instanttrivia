package com.itmc.instanttrivia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;


public class Game_Timer extends Main_Menu {

    TextView text_question;
    TextView text_score;
    TextView text_question_current;

    ImageView icon_question;

    LinearLayout lin_start_btn;
    LinearLayout lin_answer;
    LinearLayout lin_bot;
    GridLayout btn_grid;

    ProgressBar prog_bar;

    Button btn_start_easy;
    Button btn_start_med;
    Button btn_start_hard;

    String question;
    String answer;
    String category;
    String randomchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    //int leaderboard_level = null;

    ArrayList<Character> buttons;
    ArrayList<Character> ans_arr;
    ArrayList<Character> ans_pressed;

    Typeface font_regular;
    Typeface font_thin;
    Typeface font_bold;

    Boolean started = false;

    String game_difficulty = null;

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
    boolean buttons_sort_alpha = true;

    int leaderboard_name = 0;

    //STATISTICS
    int question_correct = 0;
    int question_wrong = 0;
    int total_buttons_correct = 0;
    int total_buttons_wrong = 0;

    //buffer variables for millis left to calc score
    int millis_buffer = 0;

    int total_time = 0;
    CountDownTimer timer = null;

    private DbOP db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        icon_question = (ImageView) findViewById(R.id.image_icon_question);
        icon_question.setAlpha(0f);

        icon_question = (ImageView) findViewById(R.id.image_icon_question);

        lin_answer = (LinearLayout) findViewById(R.id.linear_answer);
        lin_bot = (LinearLayout) findViewById(R.id.linear_bot);
        lin_start_btn = (LinearLayout) findViewById(R.id.linear_start_btn);
        btn_grid = (GridLayout) findViewById(R.id.buttons_grid);

        prog_bar = (ProgressBar) findViewById(R.id.timer_bar);

        btn_start_easy = (Button) findViewById(R.id.btn_start_easy);
        btn_start_med = (Button) findViewById(R.id.btn_start_med);
        btn_start_hard = (Button) findViewById(R.id.btn_start_hard);

        //set relative size for questions textview
        text_question.setTextSize(DpHeight()/38);
        text_question.setPadding(0,dpToPx((int)DpHeight()/26),0,dpToPx((int)DpHeight()/26));

        //declare answer chars store , and store answer in array
        buttons = new ArrayList<Character>();
        ans_arr = new ArrayList<Character>();
        ans_pressed = new ArrayList<Character>();

        //start database operator
        db = new DbOP(this);
        db.startdb();

        //dispaly animation on start
        animate_start();

        btn_start_easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_disable();
                btn_start_easy.setOnClickListener(null);
                difficulty_set("Easy");
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
        btn_start_med.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_disable();
                difficulty_set("Medium");
                question_read_db_rand(); // reads question on game start
                text_question.setText(question);
                answer_display_hidden();
                //porneste animatia pentru questions
                animate_quest();
                //genereza si porneste timer
                timer_create();

                started = true;
            }
        });
        btn_start_hard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_disable();
                difficulty_set("Hard");
                question_read_db_rand(); // reads question on game start
                text_question.setText(question);
                answer_display_hidden();
                //porneste animatia pentru questions
                animate_quest();
                //genereza si porneste timer
                timer_create();

                started = true;
            }
        });
    }

    private void start_disable(){
        btn_start_easy.setEnabled(false);
        btn_start_med.setEnabled(false);
        btn_start_hard.setEnabled(false);
    }
      //sets difficulty variables
    private void difficulty_set(String difficulty){
        game_difficulty = difficulty;
        switch (difficulty){
            case "Easy":
                question_number = 10;
                max_wrong = 5;
                score_per_question = 50;
                question_time = 30000;
                leaderboard_name = R.string.leaderboard_time_trial__easy_level;
                break;
            case "Medium":
                question_number = 10;
                max_wrong = 4;
                score_per_question = 100;
                question_time = 25000;
                leaderboard_name = R.string.leaderboard_time_trial__medium_level;
                break;
            case "Hard":
                question_number = 10;
                max_wrong = 3;
                score_per_question = 150;
                question_time = 20000;
                leaderboard_name = R.string.leaderboard_time_trial__hard_level;
                buttons_sort_alpha = false;
                break;
        }
    }

    //animates question textview
    private void animate_quest() {

        final TextView txt2 = (TextView) findViewById(R.id.text_question);
        final int old_pad = txt2.getPaddingTop();

        //animatie padding
        ValueAnimator val = ValueAnimator.ofInt(txt2.getPaddingTop(), dpToPx((int)DpHeight()/26));
        val.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                txt2.setPadding(10, (Integer) animation.getAnimatedValue(), 10, (Integer) animation.getAnimatedValue());
            }
        });

        val.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //displays buttons at animation end
                buttons_generate(answer);
                buttons_display();

            }
        });
        val.setDuration(750);
        val.start();

        //animate start button after click to retract back under question
        Animation anim_start_back = AnimationUtils.loadAnimation(this, R.anim.anim_top_top);

        anim_start_back.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lin_start_btn.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {            }
        });
        lin_start_btn.startAnimation(anim_start_back);
    }

    private void question_update(final String text){

        final TextView question = (TextView) findViewById(R.id.text_question);

        int init_height = question.getHeight();
        ValueAnimator val = ValueAnimator.ofInt(init_height,0);
        final ValueAnimator val2 = ValueAnimator.ofInt(0,dpToPx((int)DpHeight()/3));
        val.setDuration(1000);
        val2.setDuration(1000);

        val.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                question.setMaxHeight((Integer) animation.getAnimatedValue());
            }
        });
        val2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation2) {
                question.setMaxHeight((Integer) animation2.getAnimatedValue());
            }
        });
        val.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                val2.start();
                question.setText(text);
            }
        });
        val.start();
    }

    private void question_hide_end(){
        //animation to hide the question
        ValueAnimator val = ValueAnimator.ofInt(text_question.getHeight(),0);
        val.setDuration(2000);
        val.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                text_question.setMaxHeight((Integer) animation.getAnimatedValue());
            }
        });
        val.start();

        //answer fade out
        lin_answer.setAlpha(1f);
        lin_answer.animate().setDuration(2000).alpha(0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                lin_answer.setVisibility(View.GONE);
            }
        }).start();

        buttons_enabler(false);
        //buttons fade out
        lin_bot.setAlpha(1f);
        lin_bot.animate().setDuration(2000).alpha(0f).setListener(new AnimatorListenerAdapter() {
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

    private void score_final_display(){

        //send scores to google server
        if(mGoogleApiClient.isConnected() == true) {
            Log.e("CONNECTED PROCEED TO UPLOAD SCORES", "TRUE");
            //update leaderboards total score
            score_total_update();
            //update achievements
            achievements_questions_update();
        }

        LinearLayout lin_score = (LinearLayout) findViewById(R.id.linear_finalscore);
        lin_score.setVisibility(View.VISIBLE);

        text_score.setVisibility(View.INVISIBLE);

        TextView text_final_score = (TextView)findViewById(R.id.text_final_score);
        text_final_score.setText(score + "");
        text_final_score.setTypeface(font_bold);

        Button btn_back = (Button) findViewById(R.id.button_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

        if(game_difficulty == "Easy" && score > 470) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_easy_level_expert));
        if(game_difficulty == "Medium" && score > 900) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_medium_level_expert));
        if(game_difficulty == "Hard" && score > 1350) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_hard_level_expert));

        if(game_difficulty == "Easy" && total_buttons_wrong == 0 && question_correct == 10) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_play__easy));
        if(game_difficulty == "Medium" && total_buttons_wrong == 0 && question_correct == 10) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_play__medium));
        if(game_difficulty == "Hard" && total_buttons_wrong == 0 && question_correct == 10) Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_play__hard));
    }

    //updates total score leadeboard
    private void score_total_update(){

        //update difficulty leaderboard score
        Games.Leaderboards.submitScore(mGoogleApiClient, getString(leaderboard_name),score);

        //update total score
        //request data from server
        PendingResult<Leaderboards.LoadPlayerScoreResult> pendingResult = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,getString(R.string.leaderboard_total_score), LeaderboardVariant.TIME_SPAN_ALL_TIME,LeaderboardVariant.COLLECTION_SOCIAL);
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
        Animation anim_logo = AnimationUtils.loadAnimation(this, R.anim.anim_left_in_translate);
        Animation anim_score = AnimationUtils.loadAnimation(this, R.anim.anim_right_in_translate);

        ImageView logo = (ImageView) findViewById(R.id.image_logo);
        TextView score = (TextView) findViewById(R.id.text_score);

        lin_start_btn.startAnimation(anim);
        logo.startAnimation(anim_logo);
        score.startAnimation(anim_score);
        icon_question.animate().alpha(1f).setDuration(1000).start();
    }

    //function handles animation for answer display connected to answer_display_hidden2();
    private void answer_display_hidden() {

        //display update question number on top
        text_question_current.setText(question_counter+"/"+question_number);
        icon_question.setVisibility(View.VISIBLE);

        //animation definition
        Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);
        fade_out.setStartOffset(500);
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
            Log.e("dp",dpToPx(max_width)+"");
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

    private void score_update(){

        int time_sub =(int) Math.ceil((1-(millis_buffer)/(float)(question_time)) * score_per_question/2.0);
        int wrong_sub =(int) Math.ceil((pressed_wrong/(float)max_wrong * 0.5 * score_per_question));
        int bonus = score_per_question - time_sub - wrong_sub;

        Log.e("time_sub", time_sub+"");
        Log.e("wrong_Sub", wrong_sub+"");
        ValueAnimator val = ValueAnimator.ofInt(score,score+bonus);
        val.setDuration(bonus*50);
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

        Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);

        //transition animation for the background drawable
        TransitionDrawable trans = (TransitionDrawable) text.getBackground();
        trans.startTransition(1000);
        text.setText(change.toString());
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
                    .setStartDelay(500)
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
            t.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttons_click(100 + finalI);
                }
            });

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
            pressed.setTextColor(getResources().getColor(R.color.white));
        }else{
            pressed.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_lollipop_false));
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

        //next question for pressed wrong
        if(pressed_wrong == max_wrong) {
            //reset pressed variables
            pressed_correct = 0;
            pressed_wrong = 0;
            question_wrong++;
            question_next();
        }

        //action for word completion
        if(check_completion() == true){

            score_update();
            //reset pressed variables
            pressed_correct = 0;
            pressed_wrong = 0;
            question_correct++;

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
        questy = db.read_rand_question_difficulty(5);  // 5 diff for random questions without difficulty

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

        Log.e("Answer", a.toString());
        Log.e("Answer Chars:", buttons.toString());
    }

    //overides back buttons pressed not to exit activity
    @Override
    public void onBackPressed(){

        if(game_difficulty == null) finish();

        back_pressed++;
        if(back_pressed == 1){
            timer_end();
            if(started == true){
                timer.cancel();                         //prevent timer from exception
                btn_grid.setVisibility(View.GONE);      //solves not fading out button grid after back button pressed bug
            }else{
                lin_start_btn.setVisibility(View.GONE);     //if back pressed imediatly hide start btn
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