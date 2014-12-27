package itmc.instanttrivia;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Main_Menu extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__menu);

        final TextView play = (TextView) findViewById(R.id.text_play);

        final Animation anim1;


        anim1 = AnimationUtils.loadAnimation(this, R.anim.anim1);
        final LinearLayout lin = (LinearLayout) findViewById(R.id.main_menu_linear);

        anim1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lin.removeViewAt(0);
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

    //start timer game activity
    private void start_time_game()
    {
        Intent start = new Intent(this, Game_Timer.class);
        startActivity(start);
    }


}
