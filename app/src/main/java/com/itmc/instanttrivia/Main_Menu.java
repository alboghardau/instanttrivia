package com.itmc.instanttrivia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.BaseImplementation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.d;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.example.games.basegameutils.GameHelper;

import java.util.concurrent.TimeUnit;


public class Main_Menu extends Activity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private SignInButton btnSignIn;
    private Button btn_high_scores;
    private Button btn_play;
    private Button btn_singout;
    private TextView text_loged;
    private ImageView imgProfilePic;

    GoogleApiClient mGoogleApiClient;

    private DbOP db;

    private static int RC_SIGN_IN = 9001;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    boolean mExplicitSignOut = false;
    boolean mInSignInFlow = false;


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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES).build();

        Log.e("OnCreate", "Apelat");



    }

    private void display_change_state(Boolean signed){
        if (signed == true){
            btnSignIn.setVisibility(View.GONE);
            btn_high_scores.setVisibility(View.VISIBLE);
            btn_singout.setVisibility(View.VISIBLE);
            Log.e("Connection:", "CONNECTED");
        }else{
            btnSignIn.setVisibility(View.VISIBLE);
            btn_high_scores.setVisibility(View.GONE);
            btn_singout.setVisibility(View.GONE);
            Log.e("Connection:", "DISCONNECTED");
        }
    }

    /**
     * Button on click listener
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                // Signin button clicked
                mSignInClicked = true;
                mGoogleApiClient.connect();
                break;
            case R.id.button_signout:
                // user explicitly signed out, so turn off auto sign in
                mExplicitSignOut = true;
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Games.signOut(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }

                //show hide buttons
                display_change_state(false);
                break;
            case R.id.button_play:
                // Start Game Activity
                Intent start = new Intent(this, Game_Timer.class);
                startActivity(start);
                break;
            case R.id.button_high_scores:;
                startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient),1);
                break;
        }
    }



    @Override
    public void onConnected(Bundle bundle) {

        display_change_state(true);
        String name = Games.getCurrentAccountName(mGoogleApiClient);
        text_loged.setText("Loged in as "+name);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut) {
            // auto sign in
            mGoogleApiClient.connect();
            Log.e("Sing in on start","Apelat");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    //resolves connection problems
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, "Can't Sign In")) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button
        display_change_state(false);
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.connection_problems);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();

    }
}


