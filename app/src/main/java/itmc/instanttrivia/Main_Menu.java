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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.w3c.dom.Text;


public class Main_Menu extends ActionBarActivity {

    private LinearLayout lin;
    private ViewSwitcher play_vs;
    private TextView text_play;
    private ImageView play_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__menu);

        //variable declaration
        play_vs = (ViewSwitcher) findViewById(R.id.play_switcher);
        lin = (LinearLayout) findViewById(R.id.main_menu_linear);
        text_play = (TextView) findViewById(R.id.text_play);
        play_back = (ImageView) findViewById(R.id.play_back_icon);

        switch_play(); // set view switch for play

        text_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_vs.showNext();
            }
        });
        play_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_vs.showPrevious();
            }
        });







    }

    //set up switch view play details
    private void switch_play(){
        Animation in = AnimationUtils.loadAnimation(this,R.anim.anim2);
        Animation out = AnimationUtils.loadAnimation(this,R.anim.anim1);

        play_vs.setInAnimation(in);
        play_vs.setOutAnimation(out);
    }

    //start timer game activity
    private void start_time_game()
    {
        Intent start = new Intent(this, Game_Timer.class);
        startActivity(start);
    }


}
