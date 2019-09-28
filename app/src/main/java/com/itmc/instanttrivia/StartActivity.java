package com.itmc.instanttrivia;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;

import com.facebook.appevents.AppEventsLogger;

import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.LikeView;





import java.util.List;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;


public class StartActivity extends MaterialNavigationDrawer{

    public static SharedPreferences settings;

    private BlankFragment frag;

    private DbOP db;



    //FACEBOOK
    AppInviteDialog appInviteDialog;
    CallbackManager callbackManager;
    LikeView likeView;

    Typeface font;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    boolean mExplicitSignOut = false;
    //boolean mInSignInFlow = false;

    private static int RC_SIGN_IN = 9001;

    int colorPrimary;
    int colorDark;

    @Override
    public void init(Bundle savedInstanceState) {
        settings = getSharedPreferences("InstantOptions", MODE_PRIVATE);
        Theme_Setter();

        db = new DbOP(this);
        db.testnewdb();


        font = Typeface.createFromAsset(this.getAssets(), "typeface/bubblegum.otf");




        //add home fragment
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


        //DIVIDER // SUB HEADER
        if(appInstalledOrNot("com.facebook.katana")){
            this.addSubheader("Facebook");
            //this.addDivisor();
        }

        //SETTINGS SECTION
        MaterialSection section_settings = newSection("Settings", ContextCompat.getDrawable(this, R.drawable.icon_settings),new Intent(this, Options.class));
        section_settings.setTypeface(font);
        section_settings.useRealColor();
        section_settings.setIconColor(getResources().getColor(R.color.light_green_500));
        this.addBottomSection(section_settings);

        //LIKE OUR PAGE SECTION
        if(appInstalledOrNot("com.facebook.katana")) {
            MaterialSection section_like_page = newSection("Like", ContextCompat.getDrawable(this, R.drawable.icon_like), new MaterialSectionListener() {
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
    }

    //CHECK IF APP IS INSTALLED
    private boolean appInstalledOrNot(String uri)
    {
        PackageManager pm = getPackageManager();
        boolean app_installed;
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
            edit.apply();
        }
    }

    //SETTING RATE US
    private void settings_rated(boolean set){
        SharedPreferences.Editor edit = settings.edit();
        edit.putBoolean("Rated", set);
        edit.apply();
    }

    private static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void display_change_state(Boolean signed){
        if (signed){
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
            if(sections.get(i).getTitle().equals(title)){
                sections.get(i).getView().setVisibility(View.GONE);
            }
        }
    }
    private void section_show(String title){
        List<MaterialSection> sections = getSectionList();
        for(int i =0 ; i<sections.size(); i++){
            if(sections.get(i).getTitle().equals(title)){
                sections.get(i).getView().setVisibility(View.VISIBLE);
            }
        }
    }

    private void options_signed_in(boolean state){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("SIGNED_IN",state);
        editor.apply();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void Theme_Setter(){
        String tester = settings.getString("Color_Theme","Purple");
        assert tester != null;
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
        edit.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Boolean sign_tester = settings.getBoolean("SIGNED_IN",false);
        Log.e("Signed IN onStart", sign_tester+"");
        if (sign_tester && isNetworkAvailable()) {

            Log.e("Sing in on start","Apelat");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    //GET HIGHEST SCORE
    private void highest_score_set(int difficulty, int new_score){
        //READ PREVIOUS SCORE
        int h_score = settings.getInt("highest_score"+difficulty, 0);
        //CHECK IF NEW SCORE IS BETTER
        if(new_score > h_score){
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("highest_score"+difficulty, new_score);
            editor.apply();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if(isNetworkAvailable()) {
            AppEventsLogger.activateApp(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(isNetworkAvailable()) {
            AppEventsLogger.deactivateApp(this);
        }
    }





}
