package com.itmc.instanttrivia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Game_Timer extends ActionBarActivity {

    TextView text_question;
    TextView text_score;
    LinearLayout lin_answer;
    LinearLayout lin_bot;
    ProgressBar prog_bar;

    String question;
    String answer;
    String randomchars;
    ArrayList<Character> buttons;
    ArrayList<Character> ans_arr;
    ArrayList<Character> ans_pressed;

    Typeface font_regular;

    //options and varaibles
    int score = 0;
    int question_counter = 0;
    long milis_timer = 30000;
    long milis_add = 2000;
    long milis_sub = 1000;
    CountDownTimer timer;

    private DbOP db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game__timer);

        //random chars used to generated buttons
        randomchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        font_regular = Typeface.createFromAsset(getAssets(),"typeface/RobotoRegular.ttf");

        //define question text
        text_question = (TextView) findViewById(R.id.text_question);
        text_question.setTypeface(font_regular);

        //define variables
        text_score = (TextView) findViewById(R.id.text_score);
        text_score.setTypeface(font_regular);
        lin_answer = (LinearLayout) findViewById(R.id.linear_answer);
        lin_bot = (LinearLayout) findViewById(R.id.linear_bot);
        prog_bar = (ProgressBar) findViewById(R.id.timer_bar);

        final Button btn_start = (Button) findViewById(R.id.btn_start);

        //declare answer chars store , and store answer in array
        buttons = new ArrayList<Character>();
        ans_arr = new ArrayList<Character>();
        ans_pressed = new ArrayList<Character>();

        //start database operator
        db = new DbOP(this);
        db.startdb();

        new_question(); // reads question on game start

        //dispaly animation on start
        animate_start();

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setOnClickListener(null);

                text_question.setText(question);
                answer_display_hidden();

                //porneste animatia pentru questions
                animate_quest();

                //genereza si porneste timer
                timer_create();
            }
        });
    }

    //animates question textview
    private void animate_quest() {

        final TextView txt2 = (TextView) findViewById(R.id.text_question);
        final int old_pad = txt2.getPaddingTop();

        //animatie padding
        ValueAnimator val = ValueAnimator.ofInt(txt2.getPaddingTop(), 50);
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
        final Button btn_start = (Button) findViewById(R.id.btn_start);

        anim_start_back.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup parent = (ViewGroup) btn_start.getParent();
                parent.removeView(btn_start);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {            }
        });
        btn_start.startAnimation(anim_start_back);
    }

    private void question_update(final String text){

        final TextView question = (TextView) findViewById(R.id.text_question);

        int init_height = question.getHeight();
        ValueAnimator val = ValueAnimator.ofInt(init_height,0);
        final ValueAnimator val2 = ValueAnimator.ofInt(0,dpToPx(112));
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

    //conversie dp to pixels, util pentru animatii cu layoutparams
    private static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    private float DpWidth(){
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();

        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return dpWidth;
    }

    //animation function for the start of activity
    private void animate_start() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        Animation anim_logo = AnimationUtils.loadAnimation(this, R.anim.anim_left_in_translate);
        Animation anim_score = AnimationUtils.loadAnimation(this, R.anim.anim_right_in_translate);


        Button start = (Button) findViewById(R.id.btn_start);
        ImageView logo = (ImageView) findViewById(R.id.image_logo);
        TextView score = (TextView) findViewById(R.id.text_score);

        start.startAnimation(anim);
        logo.startAnimation(anim_logo);
        score.startAnimation(anim_score);
    }

    //function handles animation for answer display connected to answer_display_hidden2();
    private void answer_display_hidden() {

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

        //contor used for answers id starting with 200
        Integer cont_id = 200;

        for (Character ch : answer.toCharArray()) {
            TextView t = new TextView(this);

            t.setGravity(Gravity.CENTER);
            t.setTypeface(Typeface.MONOSPACE);
            t.setTextSize(20);
            t.setTextColor(Color.WHITE);
            t.setElevation(5);
            t.setId(cont_id);
            t.setBackgroundDrawable(getResources().getDrawable(R.drawable.transition_answer));
            t.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));

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

        int bonus = 1;
        ValueAnimator val = ValueAnimator.ofInt(score,score+bonus);
        val.setDuration(bonus*100);
        val.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                text_score.setText("Score: "+ (Integer) animation.getAnimatedValue());
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

        TransitionDrawable trans = (TransitionDrawable) text.getBackground();
        trans.startTransition(1000);
        text.setText(change.toString());


    }

    //enabble disables click event for buttons, used for fade in and out animation
    private void buttons_enabler(boolean enable){

        for ( int i = 0; i < 8; i++){
            TextView t = (TextView) findViewById(i+100);
            t.setEnabled(enable);
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
                    click_btn(100 + finalI);
                }
            });

            btn_grid.addView(t);
        }
    }

    //genereaza timerul initial
    private void timer_create() {
        prog_bar.setMax(100);
        final long timer_buffer = milis_timer;
        timer = new CountDownTimer(milis_timer, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                prog_bar.setProgress((int) (((double)millisUntilFinished / timer_buffer) * 100));
                milis_timer = millisUntilFinished;
                //Log.e("testmil",(int) (((double)millisUntilFinished / timer_buffer) * 100) +"");
            }

            @Override
            public void onFinish() {
                timer_end();
            }
        };
        timer.start();
    }

    //mareste timpul alocat pentru timer cu 2 secunde
    private void timer_change(String action) {

        timer.cancel();
        long total_time = 0;

        //action selector for timer   increase/decrease
        if (action == "increase") {
            total_time = milis_timer + milis_add;
        }
        if (action == "decrease") {
            total_time = milis_timer - milis_sub;
        }
        prog_bar.setMax(100);
        final long timer_buffer = total_time;
        timer = new CountDownTimer(total_time, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                prog_bar.setProgress((int) (((double)millisUntilFinished / timer_buffer) * 100));
                milis_timer = millisUntilFinished;
                //Log.e("testmil",(int) (((double)millisUntilFinished / timer_buffer) * 100) +"");
            }

            @Override
            public void onFinish() {
                timer_end();
            }
        };
        timer.start();
    }

    private void timer_end(){
        text_question.setText("Congratulations! Your score is"+score);
        lin_answer.setVisibility(View.GONE);
        lin_bot.setVisibility(View.GONE);
    }

    private void buttons_after_press(int pressed_id, boolean correct) {

        Button pressed = (Button) findViewById(pressed_id);
        pressed.setEnabled(false);

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
    private void click_btn(int id){

        boolean correct_press = false;
        TextView t = (TextView) findViewById(id);
        Character c_btn = t.getText().charAt(0);

        //testeaza daca litera face parte din raspuns
        if(ans_arr.contains(t.getText().charAt(0))){
            ans_pressed.add(t.getText().charAt(0));
            timer_change("increase");
            score_update();
            correct_press = true;
        }else{
            timer_change("decrease");
        }

        //updates answer display
        answer_display_refresh(ans_arr, c_btn);
        //update pressed buttons
        buttons_after_press(id, correct_press);

        //action for word completion
        if(check_completion() == true){
            new_question();                 //read new questions form database
            answer_display_hidden();        //display answer
            question_update(question);      //update text with animation
            buttons_generate(answer);
            buttons_display();
            buttons_enabler(false);         //click disables after word completion, reactivated in buttons display animation end
        }

        if(ans_arr.contains(c_btn) == true ){
           // Log.e("text char press", "TRUE");
        }
    }

    //reads new questions from database and sets variables
    private void new_question(){

        String[] questy;
        questy = db.read_rand_question_difficulty(1);

        question = questy[0];
        answer = questy[1];
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
    private boolean check_completion()
    {
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

        Collections.shuffle(buttons);

        Log.e("Answer", a.toString());
        Log.e("Answer Chars:", buttons.toString());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        db.close();
    }
}