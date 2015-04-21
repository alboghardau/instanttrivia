package com.itmc.instanttrivia;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.LikeView;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.io.InputStream;
import java.util.List;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;


public class StartActivity extends MaterialNavigationDrawer implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static SharedPreferences settings;

    private BlankFragment frag;

    private DbOP db;

    public GoogleApiClient mGoogleApiClient;

    //FACEBOOK
    AppInviteDialog appInviteDialog;
    CallbackManager callbackManager;
    LikeView likeView;

    Typeface font;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    boolean mExplicitSignOut = false;
    boolean mInSignInFlow = false;

    private static int RC_SIGN_IN = 9001;

    int colorPrimary;
    int colorDark;


    @Override
    public void init(Bundle savedInstanceState) {

        settings = getSharedPreferences("InstantOptions", MODE_PRIVATE);
        Theme_Setter();

        //DATABASE UPDATE
        db = new DbOP(this);
        db.testnewdb();
        db.close();

        font = Typeface.createFromAsset(this.getAssets(), "typeface/bubblegum.otf");


        //INITIALIZE FB SDK AND SHARE DIALOG
        FacebookSdk.sdkInitialize(getApplicationContext());
        appInviteDialog = new AppInviteDialog(this);
        callbackManager = CallbackManager.Factory.create();
        //CALLBACK RESPONSE FOR INVITE FRIENDS
        appInviteDialog.registerCallback(callbackManager, new FacebookCallback<AppInviteDialog.Result>() {
            @Override
            public void onSuccess(AppInviteDialog.Result result) {
                Log.e("FB CallB","YES");
                //FB POLICY DOESNT WANT REWARDS FOR ACTIONS EXCEPT LIKE YOUR PAGE
            }

            @Override
            public void onCancel() {
                Log.e("FB CallB","Canceled");
            }

            @Override
            public void onError(FacebookException e) {
                Log.e("FB CallB","Error");
            }
        });
        //LIKE INTERFACE
        likeView = new LikeView(this);


        //add home fragment
        Intent options = new Intent(this, Options.class);
        frag = new BlankFragment();

        setTitle("TEST");
        MaterialSection section = newSection("Home",frag);
        section.setSectionColor(colorPrimary,colorDark);
        section.setTitle("");

        //first fragment section
        this.disableLearningPattern();
        this.addSubheader("Main Menu");
        this.addDivisor();
        this.addSection(section);

        //SECTION ACHIEVEMENTS
        MaterialSection section_achievements = newSection("Achievements",getResources().getDrawable(R.drawable.icon_trophy_award),new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {
                if(mGoogleApiClient.isConnected()){
                    startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),1);
                }else{
                    display_change_state(false);
                    Toast t = Toast.makeText(getApplicationContext(),"Internet not connected!",Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
        section_achievements.setTypeface(font);
        section_achievements.useRealColor();
        section_achievements.setIconColor(getResources().getColor(R.color.orange_500));
        this.addSection(section_achievements);

        //SECTION LEADERBOARDS
        MaterialSection section_leader = newSection("Leader-Boards",getResources().getDrawable(R.drawable.icon_trophy_leader),new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {
                if(mGoogleApiClient.isConnected()) {
                    startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), 1);
                }else{
                    display_change_state(false);
                    Toast t = Toast.makeText(getApplicationContext(),"Internet not connected!",Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
        section_leader.setTypeface(font);
        section_leader.useRealColor();
        section_leader.setIconColor(getResources().getColor(R.color.blue_500));
        this.addSection(section_leader);

        //SECTION GOOGLE SIGN IN
        MaterialSection sign_in = newSection("Sign In",getResources().getDrawable(R.drawable.icon_gplus_box),new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {
                if(isNetworkAvailable()){
                    // Signin button clicked
                    mSignInClicked = true;
                    mGoogleApiClient.connect();
                    options_signed_in(true);
                }else{
                    Toast t = Toast.makeText(getApplicationContext(),"Internet not connected!", Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
        sign_in.setTypeface(font);
        sign_in.useRealColor();
        sign_in.setIconColor(getResources().getColor(R.color.red_500));
        this.addSection(sign_in);

        //SIGN OUT SECTION
        MaterialSection section_signout = newSection("Sign Out",getResources().getDrawable(R.drawable.icon_logout), new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {
                // user explicitly signed out, so turn off auto sign in
                mExplicitSignOut = true;
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Games.signOut(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
                //registers signed out option
                options_signed_in(false);
                //show hide buttons
                display_change_state(false);
            }
        });
        section_signout.setTypeface(font);
        section_signout.useRealColor();
        section_signout.setIconColor(getResources().getColor(R.color.red_500));
        this.addSection(section_signout);

        //DIVIDER // SUB HEADER
        if(appInstalledOrNot("com.facebook.katana")){
            this.addSubheader("Facebook");
            //this.addDivisor();
        }

        //SETTINGS SECTION
        MaterialSection section_settings = newSection("Settings", getResources().getDrawable(R.drawable.icon_settings),new Intent(this, Options.class));
        section_settings.setTypeface(font);
        section_settings.useRealColor();
        section_settings.setIconColor(getResources().getColor(R.color.light_green_500));
        this.addBottomSection(section_settings);

        //LIKE OUR PAGE SECTION
        if(appInstalledOrNot("com.facebook.katana")) {
            MaterialSection section_like_page = newSection("Like", getResources().getDrawable(R.drawable.icon_like), new MaterialSectionListener() {
                @Override
                public void onClick(MaterialSection section){
                    if(isNetworkAvailable()){
                        launchFacebook();
                    }else{
                        Toast t = Toast.makeText(getApplicationContext(), "Internet not connected", Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            });
            section_like_page.setTypeface(font);
            section_like_page.useRealColor();
            section_like_page.setIconColor(getResources().getColor(R.color.facebook_blue));
            this.addSection(section_like_page);
        }

        //FACEBOOK INVITE FRIENDS SECTION
        if(appInstalledOrNot("com.facebook.katana")) {
            MaterialSection section_invite_friends = newSection("Invite Friends", getResources().getDrawable(R.drawable.icon_facebook), new MaterialSectionListener() {
                @Override
                public void onClick(MaterialSection section) {
                    String appLinkUrl, previewImageUrl;
                    appLinkUrl = "https://fb.me/1561478857410205";
                    previewImageUrl = "http://s17.postimg.org/t7g1erdf3/new_version_promo.png";

                    if (AppInviteDialog.canShow() && isNetworkAvailable()) {
                        AppInviteContent content = new AppInviteContent.Builder()
                                .setApplinkUrl(appLinkUrl)
                                .setPreviewImageUrl(previewImageUrl)
                                .build();
                        appInviteDialog.show(content);
                    } else {
                        Toast t = Toast.makeText(getApplicationContext(), "Internet not connected", Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            });
            section_invite_friends.setTypeface(font);
            section_invite_friends.useRealColor();
            section_invite_friends.setIconColor(getResources().getColor(R.color.facebook_blue));
            this.addSection(section_invite_friends);
        }

        //RATE US SECTION
        if(!settings.getBoolean("Rated", false)) {
            this.addDivisor();
            MaterialSection section_rate_us = newSection("Rate Us! +25 Coins", getResources().getDrawable(R.drawable.icon_star), new MaterialSectionListener() {
                @Override
                public void onClick(MaterialSection section) {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        startActivity(goToMarket);
                        settings_coins_update(coins_left() + 25);
                        settings_rated(true);
                    } catch (ActivityNotFoundException e) {

                    }
                }
            });
            section_rate_us.setTypeface(font);
            section_rate_us.useRealColor();
            section_rate_us.setIconColor(getResources().getColor(R.color.green_500));
            this.addSection(section_rate_us);
        }

        //GOOGLE API INITIALIZE
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).setViewForPopups(null)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES).build();

        if(!isNetworkAvailable()){
            options_signed_in(false);
        }

        //CHANGE DISPLAY FOR SIGN ON
        if(settings.getBoolean("SIGNED_IN", false) == false) {
            display_change_state(false);
        }else{
            display_change_state(true);
        }

        //hide first fragment
        section_hide("");

        //onGameFirstRun will initialize the difficulty
        initialize_question_diff();
    }

    //CHECK IF APP IS INSTALLED
    private boolean appInstalledOrNot(String uri)
    {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try
        {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            app_installed = false;
        }
        return app_installed ;
    }

    //initialize first run question diff setting
    private void initialize_question_diff(){
        if(settings.getInt("question_diff",0) == 0){
            SharedPreferences.Editor edit = settings.edit();
            edit.putInt("question_diff",5);
            edit.commit();
        }
    }

    //SETTING RATE US
    private void settings_rated(boolean set){
        SharedPreferences.Editor edit = settings.edit();
        edit.putBoolean("Rated", set);
        edit.commit();
    }

    private void get_pic_result(){

        //reads url or player photo
        Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
        String personPhotoUrl = p.getIconImageUrl();
        String name = p.getDisplayName();

        frag.ui_top_scores.setVisibility(View.VISIBLE);
        //request data from server for total score
        PendingResult<Leaderboards.LoadPlayerScoreResult> pendingResult = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,getString(R.string.leaderboard_total_score), LeaderboardVariant.TIME_SPAN_ALL_TIME,LeaderboardVariant.COLLECTION_SOCIAL);
        final ResultCallback<Leaderboards.LoadPlayerScoreResult> scoreCallback = new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                //gets player's score from server
                LeaderboardScore scoresBuffer = loadPlayerScoreResult.getScore();
                //test if player has any score
                if(scoresBuffer != null){
                    long score = scoresBuffer.getRawScore();
                    frag.ui_total_trophy.setVisibility(View.VISIBLE);
                    frag.ui_total_score.setText("" + score);
                }else{
                    frag.ui_total_trophy.setVisibility(View.VISIBLE);
                    frag.ui_total_score.setText("0");
                }
            }
        };
        pendingResult.setResultCallback(scoreCallback);

        //GET SCORE FROM SERVER - EASY LEVEL
        PendingResult<Leaderboards.LoadPlayerScoreResult> pending_easy = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,getString(R.string.leaderboard_time_trial__easy_level),LeaderboardVariant.TIME_SPAN_ALL_TIME,LeaderboardVariant.COLLECTION_SOCIAL);
        ResultCallback<Leaderboards.LoadPlayerScoreResult> easy_callback = new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                LeaderboardScore scoresBuffer = loadPlayerScoreResult.getScore();
                if(scoresBuffer != null){
                    long score = scoresBuffer.getRawScore();
                    frag.ui_score_easy.setText("" + score);
                    highest_score_set(1, (int) score);
                }else{
                    frag.ui_score_easy.setText("0");
                }
            }
        };
        pending_easy.setResultCallback(easy_callback);

        //GET SCORE FROM SERVER - MEDIUM LEVEL
        PendingResult<Leaderboards.LoadPlayerScoreResult> pending_medium = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,getString(R.string.leaderboard_time_trial__medium_level),LeaderboardVariant.TIME_SPAN_ALL_TIME,LeaderboardVariant.COLLECTION_SOCIAL);
        ResultCallback<Leaderboards.LoadPlayerScoreResult> medium_callback = new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                LeaderboardScore scoresBuffer = loadPlayerScoreResult.getScore();
                if(scoresBuffer != null){
                    long score = scoresBuffer.getRawScore();
                    frag.ui_score_med.setText("" + score);
                    highest_score_set(2, (int) score);
                }else{
                    frag.ui_score_med.setText("0");
                }
            }
        };
        pending_medium.setResultCallback(medium_callback);

        //GET SCORE FROM SERVER - HARD LEVEL
        PendingResult<Leaderboards.LoadPlayerScoreResult> pending_hard = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,getString(R.string.leaderboard_time_trial__hard_level),LeaderboardVariant.TIME_SPAN_ALL_TIME,LeaderboardVariant.COLLECTION_SOCIAL);
        ResultCallback<Leaderboards.LoadPlayerScoreResult> hard_callback = new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                LeaderboardScore scoresBuffer = loadPlayerScoreResult.getScore();
                if(scoresBuffer != null){
                    long score = scoresBuffer.getRawScore();
                    frag.ui_score_hard.setText("" + score);
                    highest_score_set(3, (int) score);
                }else{
                    frag.ui_score_hard.setText("0");
                }
            }
        };
        pending_hard.setResultCallback(hard_callback);

        PendingResult<Leaderboards.LoadPlayerScoreResult> pending_random = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getString(R.string.leaderboard_time_trial__random), LeaderboardVariant.TIME_SPAN_ALL_TIME,LeaderboardVariant.COLLECTION_SOCIAL);
        ResultCallback<Leaderboards.LoadPlayerScoreResult> random_callback = new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                LeaderboardScore scoresBuffer = loadPlayerScoreResult.getScore();
                if(scoresBuffer != null){
                    long score = scoresBuffer.getRawScore();
                    frag.ui_score_random.setText(""+ score);
                    highest_score_set(5,(int) score);
                }else{
                    frag.ui_score_random.setText("0");
                }
            }
        };
        pending_random.setResultCallback(random_callback);

        //loads profile picture
        if(personPhotoUrl != null) {
            new LoadProfileImage(frag.ui_profile).execute(personPhotoUrl);
        }else
        {
            frag.ui_profile.setImageResource(R.drawable.unknown_profile);
        }

        //display loged in as textview
        frag.ui_loged_as.setText("Loged in as " + name);
        frag.ui_loged_as.setPadding(0,dpToPx(5),0,dpToPx(5));
    }

    private static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void display_change_state(Boolean signed){
        if (signed == true){
            section_hide("Sign In");
            section_show("Achievements");
            section_show("Leader-Boards");
            section_show("Sign Out");
            Log.e("Connection:", "CONNECTED");
        }else{
            section_show("Sign In");
            section_hide("Achievements");
            section_hide("Leader-Boards");
            section_hide("Sign Out");
            Log.e("Connection:", "DISCONNECTED");
        }
    }

    private void section_hide(String title){
        List<MaterialSection> sections = getSectionList();
        for(int i =0 ; i<sections.size(); i++){
            if(sections.get(i).getTitle() == title){
                sections.get(i).getView().setVisibility(View.GONE);
            }
        }
    }
    private void section_show(String title){
        List<MaterialSection> sections = getSectionList();
        for(int i =0 ; i<sections.size(); i++){
            if(sections.get(i).getTitle() == title){
                sections.get(i).getView().setVisibility(View.VISIBLE);
            }
        }
    }

    private void options_signed_in(boolean state){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("SIGNED_IN",state);
        editor.commit();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void Theme_Setter(){
        String tester = settings.getString("Color_Theme","Purple");
        switch (tester){
            case "Red":
                colorPrimary = getResources().getColor(R.color.red_500);
                colorDark = getResources().getColor(R.color.red_700);
                break;
            case "Purple":
                colorPrimary = getResources().getColor(R.color.deep_purple_500);
                colorDark = getResources().getColor(R.color.deep_purple_700);
                break;
            case "Blue":
                colorPrimary = getResources().getColor(R.color.blue_500);
                colorDark = getResources().getColor(R.color.blue_700);
                break;
            case "LGreen":
                colorPrimary = getResources().getColor(R.color.light_green_500);
                colorDark = getResources().getColor(R.color.light_green_700);
                break;
            case "Orange":
                colorPrimary = getResources().getColor(R.color.orange_500);
                colorDark = getResources().getColor(R.color.orange_700);
                break;
        }
    }

    //OPEN FACEBOOK GAME PAGE
    public final void launchFacebook() {
        final String urlFb = "fb://page/"+"883932578295403";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(urlFb));

        // If a Facebook app is installed, use it. Otherwise, launch
        // a browser
        final PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://www.facebook.com/pages/883932578295403";
            intent.setData(Uri.parse(urlBrowser));
        }
        startActivity(intent);
    }

    //READS COINTS LEFT
    private int coins_left(){
        return settings.getInt("Coins", 5);
    }
    //UPDATES COINS
    private void settings_coins_update(int hints){
        SharedPreferences.Editor edit = settings.edit();
        edit.putInt("Coins",hints);
        edit.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Boolean sign_tester = settings.getBoolean("SIGNED_IN",false);
        Log.e("Signed IN onStart", sign_tester+"");
        if (sign_tester == true && isNetworkAvailable()) {
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

    @Override
    public void onConnected(Bundle bundle) {
        display_change_state(true);
        get_pic_result();           //display picture and stats
        scores_late_upload();       //uploads saved scores
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
        //HANDLE FB CALLBACKS
        callbackManager.onActivityResult(requestCode, resultCode, intent);

        //HANDLE GOOGLE LOG IN
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

    //GET HIGHEST SCORE
    private void highest_score_set(int difficulty, int new_score){
        //READ PREVIOUS SCORE
        int h_score = settings.getInt("highest_score"+difficulty, 0);
        //CHECK IF NEW SCORE IS BETTER
        if(new_score > h_score){
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("highest_score"+difficulty, new_score);
            editor.commit();
        }
    }

    //UPDATE SCORES SAVED LATER WHEN CONNECTED
    private void scores_late_upload(){
        final SharedPreferences.Editor edit = settings.edit();
        if(settings.getInt("saved_score_easy", 0) > 0){
            Games.Leaderboards.submitScore(mGoogleApiClient,getString(R.string.leaderboard_time_trial__easy_level),settings.getInt("saved_score_easy", 0));
            edit.putInt("saved_score_easy", 0);
        }
        if(settings.getInt("saved_score_medium", 0) > 0){
            Games.Leaderboards.submitScore(mGoogleApiClient,getString(R.string.leaderboard_time_trial__medium_level),settings.getInt("saved_score_medium", 0));
            edit.putInt("saved_score_medium", 0);
        }
        if(settings.getInt("saved_score_hard", 0) > 0){
            Games.Leaderboards.submitScore(mGoogleApiClient,getString(R.string.leaderboard_time_trial__hard_level),settings.getInt("saved_score_hard", 0));
            edit.putInt("saved_score_hard", 0);
        }
        if(settings.getInt("saved_total_score", 0) > 0){
            Log.e("SAVED TOTAL SCORE:", settings.getInt("saved_total_score",0)+"");
            //update total score
            //request data from server
            PendingResult<Leaderboards.LoadPlayerScoreResult> pendingResult = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getString(R.string.leaderboard_total_score), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_SOCIAL);
            ResultCallback<Leaderboards.LoadPlayerScoreResult> scoreCallback = new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                @Override
                public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                    //gets player's score from server
                    LeaderboardScore scoresBuffer = loadPlayerScoreResult.getScore();
                    long score_local = 0;
                    //test if player has any score
                    if(scoresBuffer != null){
                        score_local = scoresBuffer.getRawScore();
                        Log.e("Retrieved Total Score:",score_local+"");
                    }
                    Games.Leaderboards.submitScore(mGoogleApiClient,getString(R.string.leaderboard_total_score), settings.getInt("saved_total_score", 0)+score_local);
                    Log.e("Total Score Uploaded", "TRUE");
                    edit.putInt("saved_total_score", 0);
                    edit.commit();
                }
            };
            pendingResult.setResultCallback(scoreCallback);
        }
        edit.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isNetworkAvailable()) {
            AppEventsLogger.activateApp(this);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(isNetworkAvailable()) {
            AppEventsLogger.deactivateApp(this);
        }
    }

    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                //Log.e("Error", e.getMessage());
//                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {

            bmImage.setImageBitmap(getCroppedBitmap(result));
        }
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
}
