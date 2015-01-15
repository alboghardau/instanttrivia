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
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Game_Timer extends ActionBarActivity {

    TextView text_question;
    LinearLayout lin_answer;
    ProgressBar prog_bar;

    String question;
    String answer;
    String randomchars;
    ArrayList<Character> buttons;
    ArrayList<Character> ans_arr;
    ArrayList<Character> ans_pressed;

    //timer options
    long milis_timer = 30000;
    long milis_add = 2000;
    long milis_sub = 1000;
    CountDownTimer timer ;

    private DbOP db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game__timer);

        //random chars used to generated buttons
        randomchars =  "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        //define veriables
        text_question = (TextView) findViewById(R.id.text_question);
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

                ViewGroup parent = (ViewGroup) text_start.getParent();
                parent.removeView(text_start);

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
    private void animate_quest(){

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
    }

    //conversie dp to pixels, util pentru animatii cu layoutparams
    public static int dpToPx(int dp){
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    //animation function for the start of activity
    private void animate_start(){
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_top_down);
        Animation anim_logo = AnimationUtils.loadAnimation(this,R.anim.anim_left_in_translate);
        Animation anim_score = AnimationUtils.loadAnimation(this,R.anim.anim_right_in_translate);

        LinearLayout lin = (LinearLayout) findViewById(R.id.linear_answer);
        ImageView logo = (ImageView) findViewById(R.id.image_logo);
        TextView score = (TextView) findViewById(R.id.text_score);

        lin.startAnimation(anim);
        logo.startAnimation(anim_logo);
        score.startAnimation(anim_score);
    }

    //generate textview for answer chars and fill them in linear layout
    private void answer_display_hidden(){
        lin_answer.removeAllViews();
        for( Character ch: answer.toCharArray()){
            TextView t = new TextView(this);
            t.setPadding(10,15,10,15);
            t.setTextSize(25);
            t.setTextColor(Color.WHITE);

            if(ch.compareTo(" ".charAt(0)) == 0){
                t.setText(" ");
            }else {
                t.setText("_");
            }

            lin_answer.addView(t);
        }
    }

    //display random chars at start
    private void buttons_display(){

        LinearLayout line1 = (LinearLayout) findViewById(R.id.linear_ans_first);
        LinearLayout line2 = (LinearLayout) findViewById(R.id.lineage_ans_second);
        line1.removeAllViews();
        line2.removeAllViews();

        for(int i = 0; i < 8; i++){
            TextView t = new TextView(this);
            t.setText(buttons.get(i).toString());
            t.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_view_all_indi500));
            t.setTextColor(getResources().getColor(R.color.white));
            t.setTypeface(Typeface.MONOSPACE);
            t.setPadding(80,30,80,30);
            t.setTextSize(50);
            t.setGravity(Gravity.CENTER);
            //generate id starting with 100
            t.setId(100+i);

            //set clicker
            final int finalI = i;
            t.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   click_btn(100+ finalI);
                }
            });

            //display text view
            if( i < 4)
            {
                line1.addView(t);
            }else{
                line2.addView(t);
            }
        }
    }

    private void answer_display_refresh(ArrayList<Character> answer, ArrayList<Character> pressed){

        int answer_lenght = answer.size();
        lin_answer.removeAllViews();

        //add revealed word to layout
        for(int i = 0; i < answer_lenght; i++){
            TextView t = new TextView(this);
            t.setPadding(10,15,10,15);
            t.setTextSize(25);
            t.setTextColor(Color.WHITE);

            if(pressed.contains(answer.get(i)) == true)
            {
                t.setText(answer.get(i).toString());
            //    Log.e("testchar", "contains");
            }else{
                t.setText("_");
            }

            lin_answer.addView(t);
        }
    }

    //genereaza timerul initial
    private void timer_create(){

        timer = new CountDownTimer(milis_timer, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                prog_bar.setProgress((int) millisUntilFinished/100 * 100 /30);
                milis_timer = millisUntilFinished;
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();
    }

    //mareste timpul alocat pentru timer cu 2 secunde
    private void timer_change(String action){

        timer.cancel();
        long total_time = 0;

        //action selector for timer   increase/decrease
        if(action == "increase"){
            total_time = milis_timer+milis_add;
        }
        if( action == "decrease"){
            total_time = milis_timer-milis_sub;
        }

        timer = new CountDownTimer(total_time,100) {
            @Override
            public void onTick(long millisUntilFinished) {
                prog_bar.setProgress((int) millisUntilFinished/100 * 100 /30);
                milis_timer = millisUntilFinished;
            }

            @Override
            public void onFinish() {            }
        };
        timer.start();
    }

    private void buttons_update_chars(int pressed_id){
        ArrayList<Integer> changed = new ArrayList<Integer>();
        ArrayList<Integer> buffer = new ArrayList<Integer>();
        ArrayList<Character> chars = new ArrayList<Character>();
        Character cha = null;
        TextView t = (TextView) findViewById(pressed_id);
        changed.add(pressed_id);

        //generate all ids array and shuffle it
        for(int i = 100; i < 108; i++){
            buffer.add(i);
        }
        Collections.shuffle(buffer);
        //add first 3 shuffled to array
        for(int i = 0; i < 3; i++){
            changed.add(buffer.get(i));
        }

        //genereaza 4 caractere la intamplare care nu sunt afisate & in array & apasate corecte
        while(chars.size() < 4)
        {
            cha = random_letter();
            if( ans_pressed.contains(cha) == false && buttons.contains(cha) == false && chars.contains(cha) == false && t.getText().charAt(0) != cha){
                chars.add(cha);
            }
        }

        Log.e("Chars Generated 1:", chars.toString());

        for ( int i = 0; i < changed.size(); i++){

        }

//        //updateaza array cu litere afisate clickers si inlocuieste butonul apasat
//        for( int i = 0; i < changed.size(); i++){
//            TextView t = (TextView) findViewById(changed.get(i));
//            if(ans_arr.contains(t.getText().charAt(0)) == false || i == 0) {
//                Log.e("test", t.getText().toString());
//                t.setText(chars.get(i).toString());
//                buttons.set(changed.get(i) - 100, chars.get(i));
//            }
//        }

//        //verifica daca exista macar o litere ce apartine raspunului
//        if(check_letter_exist() == false){
//            for(int i = 0; i < ans_arr.size(); i++){
//                if(ans_pressed.contains(ans_arr.get(i)) == false){
//                    TextView t = (TextView) findViewById(changed.get(1));
//                    t.setText(ans_arr.get(i).toString());
//                    break;
//                }
//            }
//        }
        //Log.e("Change: id", changed.toString());
        //Log.e("C array", buttons.toString());
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
            text_question.setText(question);
        }

        if(ans_arr.contains(c_btn) == true ){
            Log.e("text char press", "TRUE");
        }
    }

    //reads new questions from database and sets variables
    private void new_question(){
        String[] questy;
        questy = db.read_rand_question_difficulty(1);

        question = questy[0];
        answer = questy[1];

        //delete previously pressed chars
        ans_pressed.clear();
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


