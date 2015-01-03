package itmc.instanttrivia;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game__timer);

        question = "Who is the biggest mother fucker out there?";
        answer = "SAURON";
        randomchars =  "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        //define veriables
        text_question = (TextView) findViewById(R.id.text_question);
        lin_answer = (LinearLayout) findViewById(R.id.linear_answer);

        text_question.setText(question);
        clear_answer(answer);

        //declare answer chars store
        c = new ArrayList<Character>();

        fill_answer_chars(answer);
    }

    private void clear_answer(String ans)
    {
        for( Character ch: ans.toCharArray()){
            TextView t = new TextView(this);
            t.setPadding(10,15,10,15);
            t.setTextSize(20);
            t.setTextColor(Color.WHITE);
            t.setText("_");
            lin_answer.addView(t);

        }
    }

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
        ArrayList<int> rand_index_ans = new ArrayList<int>();
        for(int i = 0; i < ans.length(); i++){
            rand_index_ans.add(i);
        }
        ArrayList<int> rand_index_8 = new ArrayList<int>();


        //shuffle random numbers
        Collections.shuffle(rand_index_ans);

        for(int i = 0; i < 3; i++){

        }



        Log.e("Answer", a.toString());
        Log.e("Answer Chars:", c.toString());
    }


}
