package itmc.instanttrivia;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.w3c.dom.Text;


public class Main_Menu extends ActionBarActivity {

    LinearLayout lin;
    ViewSwitcher vs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__menu);

        lin = (LinearLayout) findViewById(R.id.main_menu_linear);

        final TextView play = (TextView) findViewById(R.id.text_play);
        final LinearLayout lin = (LinearLayout) findViewById(R.id.main_menu_linear);


        final Animation anim1;
        anim1 = AnimationUtils.loadAnimation(this, R.anim.anim1);


        //test area
        Animation in = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);

        vs = (ViewSwitcher) findViewById(R.id.viewSwitcher);

        vs.setInAnimation(anim1);
        vs.setOutAnimation(out);



        TextView t = (TextView) findViewById(R.id.textView2);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vs.showPrevious();
            }
        });
        TextView t2 = (TextView) findViewById(R.id.textView);
        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vs.showNext();
            }
        });


        anim1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                removeview(0);
                createtv(0);createtv(0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play.startAnimation(anim1);
            }
        });
    }

    //remove view from LinearLayout
    private void removeview(final int id)
    {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                lin.removeViewAt(id);
            }
        });
    }

    private void createtv(int id)
    {
        TextView t = new TextView(this);
        t.setText("Test");
        t.setTextSize(15);

        lin.addView(t);
    }



    //start timer game activity
    private void start_time_game()
    {
        Intent start = new Intent(this, Game_Timer.class);
        startActivity(start);
    }


}
