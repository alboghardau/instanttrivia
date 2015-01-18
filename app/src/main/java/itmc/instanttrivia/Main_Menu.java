package itmc.instanttrivia;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;


public class Main_Menu extends ActionBarActivity {

    private DbOP db;

    private LinearLayout lin;
    private ViewSwitcher play_vs;
    private TextView text_play;
    private TextView text_time;
    private ImageView play_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__menu);

        //variable declaration
        play_vs = (ViewSwitcher) findViewById(R.id.play_switcher);
        lin = (LinearLayout) findViewById(R.id.main_menu_linear);
        text_play = (TextView) findViewById(R.id.text_play);
        text_time = (TextView) findViewById(R.id.text_time);
        play_back = (ImageView) findViewById(R.id.play_back_icon);

        ImageView logo_1 = (ImageView) findViewById(R.id.logo_1);
        ImageView logo_2 = (ImageView) findViewById(R.id.logo_2);
        ImageView logo_3 = (ImageView) findViewById(R.id.logo_3);

        //set animation for view switcher
        final Animation right_in = AnimationUtils.loadAnimation(this,R.anim.anim_right_in);
        final Animation right_out = AnimationUtils.loadAnimation(this,R.anim.anim_right_out);
        final Animation left_in = AnimationUtils.loadAnimation(this,R.anim.anim_left_in);
        final Animation left_out = AnimationUtils.loadAnimation(this,R.anim.anim_left_out);
        final Animation left_trans = AnimationUtils.loadAnimation(this,R.anim.anim_left_in_translate);
        final Animation right_trans = AnimationUtils.loadAnimation(this,R.anim.anim_right_in_translate);

        logo_1.startAnimation(left_trans);
        logo_2.startAnimation(right_trans);
        logo_3.startAnimation(right_trans);

        //database
        db = new DbOP(this);
        db.testnewdb();
        db.close();

        //click listeners
        text_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_vs.setInAnimation(right_in);
                play_vs.setOutAnimation(right_out);
                play_vs.showNext();
            }
        });

        play_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_vs.setInAnimation(left_in);
                play_vs.setOutAnimation(left_out);
                play_vs.showPrevious();
            }
        });

        text_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_time_game();
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
