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


public class Game_Timer extends ActionBarActivity {

    TextView text_question;
    LinearLayout lin_answer;

    String question;
    String answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game__timer);

        question = "Who is the biggest mother fucker out there?";
        answer = "SAURON";

        //define veriables
        text_question = (TextView) findViewById(R.id.text_question);
        lin_answer = (LinearLayout) findViewById(R.id.linear_answer);

        text_question.setText(question);
        fill_answer(answer);

    }

    private void fill_answer(String ans)
    {


        for( Character ch: ans.toCharArray()){
            TextView t = new TextView(this);
            t.setPadding(10,15,10,15);
            t.setTextSize(20);
            t.setTextColor(Color.WHITE);
            t.setText(ch.toString());
            lin_answer.addView(t);

        }

    }


}
