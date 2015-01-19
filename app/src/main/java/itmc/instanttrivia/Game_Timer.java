package itmc.instanttrivia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Game_Timer extends ActionBarActivity {

    TextSwitcher text_question;
    TextView text_score;
    LinearLayout lin_answer;
    ProgressBar prog_bar;

    String question;
    String answer;
    String randomchars;
    ArrayList<Character> buttons;
    ArrayList<Character> ans_arr;
    ArrayList<Character> ans_pressed;

    Typeface font_regular;

    //timer options
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

        //define question text_witcher
        text_question = (TextSwitcher) findViewById(R.id.text_question);
        text_question.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView myText = new TextView(Game_Timer.this);
                myText.setTypeface(font_regular);
                myText.setTextSize(20);
                myText.setTextColor(Color.WHITE);
                myText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                myText.setGravity(Gravity.CENTER);
                return myText;
            }
        });
        text_question.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.anim_fade_in));
        text_question.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.anim_fade_out));

        //define variables
        text_score = (TextView) findViewById(R.id.text_score);
        text_score.setTypeface(font_regular);
        lin_answer = (LinearLayout) findViewById(R.id.linear_answer);
        final TextView text_start = (TextView) findViewById(R.id.text_start);
        prog_bar = (ProgressBar) findViewById(R.id.timer_bar);

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

        text_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_start.setOnClickListener(null);

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

        final TextSwitcher txt2 = (TextSwitcher) findViewById(R.id.text_question);
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
        final TextView text_start = (TextView) findViewById(R.id.text_start);

        anim_start_back.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup parent = (ViewGroup) text_start.getParent();
                parent.removeView(text_start);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        text_start.startAnimation(anim_start_back);

    }

    //conversie dp to pixels, util pentru animatii cu layoutparams
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    //animation function for the start of activity
    private void animate_start() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_top_down);
        Animation anim_logo = AnimationUtils.loadAnimation(this, R.anim.anim_left_in_translate);
        Animation anim_score = AnimationUtils.loadAnimation(this, R.anim.anim_right_in_translate);


        TextView start = (TextView) findViewById(R.id.text_start);
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
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        //animation action listener on ending fade in new answer
        fade_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lin_answer.removeAllViews();
                answer_display_hidden2();
                lin_answer.startAnimation(anim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        //check to display only fade in for first question
        if(question_counter > 1) {
            lin_answer.startAnimation(fade_out);
        }else{
            lin_answer.startAnimation(anim);
            answer_display_hidden2();
        }
    }

    //function handles text view generation for answer letters
    private void answer_display_hidden2(){
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER);
        lin_answer.addView(line);

        Integer cont_id = 200;

        for (Character ch : answer.toCharArray()) {
            TextView t = new TextView(this);

            t.setGravity(Gravity.CENTER);
            t.setTypeface(Typeface.MONOSPACE);
            t.setTextSize(20);
            t.setTextColor(Color.WHITE);
            t.setId(cont_id);
            t.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_view_all_small_orange500));
            t.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));

            if (ch.compareTo(" ".charAt(0)) == 0) {
                line = new LinearLayout(this);
                line.setOrientation(LinearLayout.HORIZONTAL);
                line.setGravity(Gravity.CENTER);
                lin_answer.addView(line);
                cont_id++;
            } else {
                t.setText(" ");
                line.addView(t);
                cont_id++;
            }
        }
    }

    private void answer_display_refresh(ArrayList<Character> answer, ArrayList<Character> pressed) {

        //add revealed word to layout
        for (int i = 0; i < answer.size(); i++) {
            if (pressed.contains(answer.get(i)) == true && answer.get(i) != " ".charAt(0)) {
                answer_replace_char(i + 200, answer.get(i));
            }
        }
    }

    //function will replace answer chars view from linear layout with animation
    public void answer_replace_char(final Integer char_id, Character change) {

        final TextView text = (TextView) findViewById(char_id);
        final LinearLayout lin = (LinearLayout) text.getParent();
        final int index = lin.indexOfChild(text);

        Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);

        final TextView t = new TextView(this);

        t.setTypeface(Typeface.MONOSPACE);
        t.setTextSize(20);
        t.setTextColor(Color.WHITE);
        t.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_view_all_lightgreen500));
        t.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
        t.setGravity(Gravity.CENTER);
        t.setId(char_id);
        t.setText(change.toString());

        text.startAnimation(fade_out);
        lin.addView(t,index);
        lin.removeView(text);
    }

    //display random chars at start
    private void buttons_display() {

        LinearLayout line1 = (LinearLayout) findViewById(R.id.linear_ans_first);
        LinearLayout line2 = (LinearLayout) findViewById(R.id.lineage_ans_second);
        line1.removeAllViews();
        line2.removeAllViews();

        for (int i = 0; i < 8; i++) {
            TextView t = new TextView(this);
            t.setText(buttons.get(i).toString());
            t.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_view_all_orange500));
            t.setTextColor(getResources().getColor(R.color.white));
            t.setTypeface(Typeface.MONOSPACE);
            t.setPadding(dpToPx(25), dpToPx(10), dpToPx(25), dpToPx(10));
            t.setTextSize(50);
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

            //display text view
            if (i < 4) {
                line1.addView(t);
            } else {
                line2.addView(t);
            }
        }
    }

    //genereaza timerul initial
    private void timer_create() {

        timer = new CountDownTimer(milis_timer, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                prog_bar.setProgress((int) millisUntilFinished / 100 * 100 / 30);
                milis_timer = millisUntilFinished;
            }

            @Override
            public void onFinish() {

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

        timer = new CountDownTimer(total_time, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                prog_bar.setProgress((int) millisUntilFinished / 100 * 100 / 30);
                milis_timer = millisUntilFinished;
            }

            @Override
            public void onFinish() {
            }
        };
        timer.start();
    }

    private void buttons_update_chars(int pressed_id) {
        ArrayList<Integer> changed = new ArrayList<Integer>();
        ArrayList<Integer> buffer = new ArrayList<Integer>();
        ArrayList<Character> chars = new ArrayList<Character>();
        ArrayList<Character> answer_unused = new ArrayList<Character>();
        Character cha = null;
        TextView t = (TextView) findViewById(pressed_id);
        changed.add(pressed_id);

        //generate all ids array and shuffle it
        for (int i = 100; i < 108; i++) {
            buffer.add(i);
        }
        Collections.shuffle(buffer);
        //add first 3 shuffled to array
        for (int i = 0; i < 3; i++) {
            changed.add(buffer.get(i));
        }

        //genereaza 4 caractere la intamplare care nu sunt afisate & in array & apasate corecte
        while (chars.size() < 4) {
            cha = random_letter();
            if (ans_pressed.contains(cha) == false && buttons.contains(cha) == false && chars.contains(cha) == false && t.getText().charAt(0) != cha) {
                chars.add(cha);
            }
        }

        Log.e("Chars Generated 1:", chars.toString());

        //generates an array with the chars not found from answer
        for (int i = 0; i < ans_arr.size(); i++) {
            if (ans_pressed.contains(ans_arr.get(i)) == false && answer_unused.contains(ans_arr.get(i)) == false) {
                answer_unused.add(ans_arr.get(i));
            }
        }
        //Log.e("Answer Unused:", answer_unused.toString());

        //randomize unused chars
        Collections.shuffle(answer_unused);

        if (answer_unused.size() > 0) {
            for (int i = 0; i < 2; i++) {
                if (changed.contains(answer_unused.get(i)) == false && buttons.contains(answer_unused.get(i)) == false ) {
                    chars.add(answer_unused.get(i));
                    Log.e("added", answer_unused.get(i).toString());
                }
                if (answer_unused.size() == 1) break;
            }
        }

        //reverse generated chars to bring the ones from answer foreward
        Collections.reverse(chars);

        Log.e("Chars Generated 2:", chars.toString());

        //updates buttons chars array and displays them
        for (int i = 0; i < changed.size(); i++) {

            TextView text_changer = (TextView) findViewById(changed.get(i));

            if(answer_unused.contains(text_changer.getText().charAt(0)) == false){
                text_changer.setText(chars.get(i).toString());
                buttons.set(changed.get(i) - 100, chars.get(i));
            }
        }

        Log.e("Change: id", changed.toString());
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

        TextView t = (TextView) findViewById(id);
        Character c_btn = t.getText().charAt(0);

        //testeaza daca litera face parte din raspuns
        if(ans_arr.contains(t.getText().charAt(0))){
            ans_pressed.add(t.getText().charAt(0));
            timer_change("increase");
        }else{
            timer_change("decrease");
        }

        //updates answer display
        answer_display_refresh(ans_arr, ans_pressed);
        buttons_update_chars(id);

        //action for word completion
        if(check_completion() == true){
            new_question();
            answer_display_hidden();
            View old = (View) text_question.getNextView();
            
            text_question.setText(question);
            text_question.removeView(old);
            buttons_generate(answer);
            buttons_display();
        }

        if(ans_arr.contains(c_btn) == true ){
           // Log.e("text char press", "TRUE");
        }
    }

    private void answer_complete_hide(){

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
        //set answer to array
        for (Character ch : ans.toCharArray()) {
            a.add(ch);
        }

        //generate 8 random chars
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < randomchars.length(); j++) {
                cha = randomchars.charAt(rnd.nextInt(randomchars.length()));
                if (buttons.contains(cha) == false) {
                    buttons.add(cha);
                    break;
                }
            }
        }

        //generate three random index
        ArrayList<Integer> rand_index_ans = new ArrayList<>();
        for(int i = 0; i < ans.length(); i++){
            rand_index_ans.add(i);
        }
        ArrayList<Integer> rand_index_8 = new ArrayList<>();
        for( int i = 0; i < 8; i++) {
            rand_index_8.add(i);
        }

        //shuffle random numbers
        Collections.shuffle(rand_index_ans);
        Collections.shuffle(rand_index_8);

        int max = rand_index_ans.size()/2;
        if (max > 6) max = 5;
        if (max < 1) max = 1;

        //replace three chars
        for(int i = 0; i < max; i++){
            if(( buttons.contains(a.get(rand_index_ans.get(i))) == false) && (a.get(rand_index_ans.get(i)).compareTo(" ".charAt(0)) != 0) )
            {
                buttons.set(rand_index_8.get(i),a.get(rand_index_ans.get(i)));
            }
        }

        Log.e("Answ Index", rand_index_ans.toString());
        Log.e("8 Index", rand_index_8.toString());
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