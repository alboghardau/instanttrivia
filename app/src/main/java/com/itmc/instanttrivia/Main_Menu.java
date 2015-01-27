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
import com.itmc.instanttrivia.R;

import java.io.InputStream;


public class Main_Menu extends BaseGameActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    // Logcat tag
    private static final String TAG = "MainActivity";

    // Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 400;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    private boolean mSignInClicked = false;

    private ConnectionResult mConnectionResult;

    private SignInButton btnSignIn;
    private Button btn_high_scores;
    private Button btn_play;
    private Button btn_singout;
    private TextView text_loged;
    private ImageView imgProfilePic;

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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Games.API)
                .addScope(Games.SCOPE_GAMES).build();

        //test if app is connected on start
        if(mSignInClicked == false) updateUI(false);
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

        // Get user's information
        //getProfileInformation();

        // Update the UI after signin
        updateUI(true);

    }

    /**
     * Updating the UI, showing/hiding buttons and profile layout
     * */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            btnSignIn.setVisibility(View.GONE);
            btn_singout.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            btn_singout.setVisibility(View.GONE);
        }
    }

//    /**
//     * Fetching user's information name, email, profile pic
//     * */
//    private void getProfileInformation() {
//        try {
//            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
//                Person currentPerson = Plus.PeopleApi
//                        .getCurrentPerson(mGoogleApiClient);
//                String personName = currentPerson.getDisplayName();
//                String personPhotoUrl = currentPerson.getImage().getUrl();
//                String personGooglePlusProfile = currentPerson.getUrl();
//                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
//
//                Log.e(TAG, "Name: " + personName + ", plusProfile: "
//                        + personGooglePlusProfile + ", email: " + email
//                        + ", Image: " + personPhotoUrl);
//
//                text_loged.setText("Logged in as "+personName);
//                //txtEmail.setText(email);
//
//                // by default the profile url gives 50x50 px image only
//                // we can replace the value with whatever dimension we want by
//                // replacing sz=X
//                personPhotoUrl = personPhotoUrl.substring(0,
//                        personPhotoUrl.length() - 2)
//                        + PROFILE_PIC_SIZE;
//
//                new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);
//
//            } else {
//                Toast.makeText(getApplicationContext(),
//                        "Person information is null", Toast.LENGTH_LONG).show();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
        updateUI(false);
    }



    /**
     * Button on click listener
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                // Signin button clicked
                signInWithGplus();
                break;
            case R.id.button_signout:
                // Signout button clicked
                signOutFromGplus();
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
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient , "CgkIyc6Y-6gaEAIQAQ") , 1);
                Log.e("HS test press","TRUE");
                break;
        }
    }

    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Sign-out from google
     * */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Games.signOut(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            updateUI(false);
        }
    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }


    /**
     * Background Async task to load user profile picture from url
     * */
//    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
//        ImageView bmImage;
//
//        public LoadProfileImage(ImageView bmImage) {
//            this.bmImage = bmImage;
//        }
//
//        protected Bitmap doInBackground(String... urls) {
//            String urldisplay = urls[0];
//            Bitmap mIcon11 = null;
//            try {
//                InputStream in = new java.net.URL(urldisplay).openStream();
//                mIcon11 = BitmapFactory.decodeStream(in);
//            } catch (Exception e) {
//                Log.e("Error", e.getMessage());
//                e.printStackTrace();
//            }
//            return mIcon11;
//        }
//
//        protected void onPostExecute(Bitmap result) {
//            bmImage.setImageBitmap(result);
//
//
//        }
//    }

}


