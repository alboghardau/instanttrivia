package itmc.instanttrivia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.Typeface;
import android.support.annotation.IntegerRes;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;


public class Game_Timer extends ActionBarActivity {

    TextView text_question;
    LinearLayout lin_answer;

    String question;
    String answer;
    String randomchars;
    ArrayList<Character> c;
    ArrayList<Character> ans_arr;
    ArrayList<Character> ans_pressed;

    private DbOP db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game__timer);

        //random chars used to generated buttons
        randomchars =  "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        //start database operator
        db = new DbOP(this);
        db.startdb();

        //read random questions first
        String[] questy;
        questy = db.read_rand_question_difficulty(1);

        question = questy[0];
        answer = questy[1];

        //define veriables
        text_question = (TextView) findViewById(R.id.text_question);
        lin_answer = (LinearLayout) findViewById(R.id.linear_answer);
        final TextView text_start = (TextView) findViewById(R.id.text_start);

        //declare answer chars store , and store answer in array
        c = new ArrayList<Character>();
        ans_arr = new ArrayList<Character>();
        ans_pressed = new ArrayList<Character>();

        //fil answer array from word
        fill_answer_array(answer);

        //gen random chars
        fill_answer_chars(answer);
        animate_start();

        text_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_start.setOnClickListener(null);

                ViewGroup parent = (ViewGroup) text_start.getParent();
                parent.removeView(text_start);

                clear_answer(answer);

                text_question.setText(question);

                animate_quest();
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

        //animatie top margin
        ValueAnimator val2 = ValueAnimator.ofInt(dpToPx(55),dpToPx(100));
        val2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation2) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(0,(int) animation2.getAnimatedValue() ,0,0);
                txt2.setLayoutParams(params);
            }
        });

        val.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //cand termina animatia arata litere
                display_start_randoms();
            }
        });

        val.setDuration(750);
        val2.setDuration(750);
        val.start();
        val2.start();

    }

    //conversie dp to pixels
    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    //animation function for the start of activity
    private void animate_start()
    {
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

    //clear answer array and upddate it with answer word
    private void fill_answer_array(String s){
        ans_arr.clear();
        for(Character ch: s.toCharArray()){
            ans_arr.add(ch);
        }
    }

    //generate textview for answer chars and fill them in linear layout
    private void clear_answer(String ans)
    {
        for( Character ch: ans.toCharArray()){
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
    private void display_start_randoms(){

        LinearLayout line1 = (LinearLayout) findViewById(R.id.linear_ans_first);
        LinearLayout line2 = (LinearLayout) findViewById(R.id.lineage_ans_second);

        for(int i = 0; i < 8; i++){
            TextView t = new TextView(this);
            t.setText(c.get(i).toString());
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

    private void test_word_completion(ArrayList<Character> answer, ArrayList<Character> pressed){

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

    private void update_ans_chars(int pressed_id){

        ArrayList<Integer> changed = new ArrayList<Integer>();
        ArrayList<Integer> buffer = new ArrayList<Integer>();
        ArrayList<Character> chars = new ArrayList<Character>();
        Character cha = null;
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

        //genereaza 4 caractere la intamplare care nu sunt afisate & in array cu apasate corecte
        while(chars.size() < 4)
        {
            cha = random_letter();
            if( ans_pressed.contains(cha) == false && c.contains(cha) == false && chars.contains(cha) == false){
                chars.add(cha);
            }
        }

        //updateaza array cu litere afisate clickers
        for( int i = 0; i<changed.size(); i++){
            TextView t = (TextView) findViewById(changed.get(i));
            t.setText(chars.get(i).toString());
            c.set(changed.get(i)-100 ,chars.get(i));
        }

        Log.e("Change: id", changed.toString());
        Log.e("C array", c.toString());
    }



    private Character random_letter(){
        Random rnd = new Random();
        Character cha = (Character) randomchars.charAt(rnd.nextInt(randomchars.length()));
        return cha;
    }

    private void click_btn(int id){

        TextView t = (TextView) findViewById(id);
        Character c_btn = t.getText().charAt(0);

        if(ans_arr.contains(t.getText().charAt(0))){
            ans_pressed.add(t.getText().charAt(0));
        }

        test_word_completion(ans_arr,ans_pressed);
        update_ans_chars(id);



        if(ans_arr.contains(c_btn) == true ){
            Log.e("text char press", "TRUE");
        }


    }

    //generate 8 random chars containing minimum 3 answer chars at beggining
    private void fill_answer_chars(String ans) {
        ArrayList<Character> a = new ArrayList<Character>();
        Character cha = null;
        Random rnd = new Random();
        //set answer to array
        for (Character ch : ans.toCharArray()) {
            a.add(ch);
        }

        //generate 8 random chars
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < randomchars.length(); j++) {
                cha = randomchars.charAt(rnd.nextInt(randomchars.length()));
                if (c.contains(cha) == false) {
                    c.add(cha);
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
            if(( c.contains(a.get(rand_index_ans.get(i))) == false) && (a.get(rand_index_ans.get(i)).compareTo(" ".charAt(0)) != 0) )
            {
                c.set(rand_index_8.get(i),a.get(rand_index_ans.get(i)));
            }
        }

        Log.e("Answ Index", rand_index_ans.toString());
        Log.e("8 Index", rand_index_8.toString());
        Log.e("Answer", a.toString());
        Log.e("Answer Chars:", c.toString());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        db.close();
    }

}
