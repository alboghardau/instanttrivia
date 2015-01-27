package com.itmc.instanttrivia;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;
import com.itmc.instanttrivia.R;

import java.io.InputStream;


public class Main_Menu extends BaseGameActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private SignInButton btnSignIn;
    private Button btn_high_scores;
    private Button btn_play;
    private Button btn_singout;
    private TextView text_loged;
    private ImageView imgProfilePic;

    GameHelper gameHelper;

    private DbOP db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__menu);

        //variable declaration

        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btn_play = (Button) findViewById(R.id.button_play);
        btn_singout = (Button)findViewById(R.id.button_signout);
        btn_high_scores = (Button)findViewById(R.id.button_high_scores);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
        text_loged = (TextView) findViewById(R.id.text_loged);

        ImageView logo_1 = (ImageView) findViewById(R.id.logo_1);
        ImageView logo_2 = (ImageView) findViewById(R.id.logo_2);
        ImageView logo_3 = (ImageView) findViewById(R.id.logo_3);

        //set animation for view switcher
        final Animation left_trans = AnimationUtils.loadAnimation(this,R.anim.anim_left_in_translate);
        final Animation right_trans = AnimationUtils.loadAnimation(this,R.anim.anim_right_in_translate);

        logo_1.startAnimation(left_trans);
        logo_2.startAnimation(right_trans);
        logo_3.startAnimation(right_trans);

        //database
        db = new DbOP(this);
        db.testnewdb();
        db.close();

        // Button click listeners
        btnSignIn.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_singout.setOnClickListener(this);
        btn_high_scores.setOnClickListener(this);


        gameHelper = new GameHelper(this, GameHelper.CLIENT_GAMES);
        gameHelper.setup(this);

        gameHelper.enableDebugLog(true);   // add this (but only for debug builds)
    }


    /**
     * Button on click listener
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                // Signin button clicked
                beginUserInitiatedSignIn();
                break;
            case R.id.button_signout:
                // Signout button clicked
                signOut();
                break;
//            TO KEEP IN CASE OF FUTURE USE
//            case R.id.btn_revoke_access:
//                // Revoke access button clicked
//                revokeGplusAccess();
//                break;
            case R.id.button_play:
                // Start Game Activity
                Intent start = new Intent(this, Game_Timer.class);
                startActivity(start);
                break;
            case R.id.button_high_scores:
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient() , getString(R.string.leaderboard_test_leaderboard)) , 1);
                Log.e("HS test press","TRUE");
                break;
        }
    }



    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}


