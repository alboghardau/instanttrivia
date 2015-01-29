package com.itmc.instanttrivia;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;

import org.w3c.dom.Text;


public class HS_Frag_Easy extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;

    private ViewGroup mContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_easy, container, false);
        mContainer = container;

        mGoogleApiClient = new GoogleApiClient.Builder(mContainer.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES).build();
        mGoogleApiClient.connect();

        return rootView;
    }

    @Override
    public void onConnected(Bundle bundle) {

        final RelativeLayout rel = (RelativeLayout) mContainer.findViewById(R.id.frag_easy_frame);
        final LinearLayout linear_easy = (LinearLayout) mContainer.findViewById(R.id.frag_easy_linear);

        Log.e("Easy Frament", "CONNECTED");

        Intent i = Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,getString(R.string.leaderboard_time_trial__easy_level));




        PendingResult<Leaderboards.LoadScoresResult> p = Games.Leaderboards.loadTopScores(mGoogleApiClient, getString(R.string.leaderboard_time_trial__easy_level), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_SOCIAL, 25, true);
        ResultCallback<Leaderboards.LoadScoresResult> pResultCallback = new ResultCallback<Leaderboards.LoadScoresResult>() {
            @Override
            public void onResult(Leaderboards.LoadScoresResult loadScoresResult) {

                LeaderboardScoreBuffer score_buffer = loadScoresResult.getScores();
                SparseArray<Bundle> scores = new SparseArray<Bundle>();

                for(int i = 0; i < score_buffer.getCount();i++){
                    LeaderboardScore l = score_buffer.get(i);
                    Bundle map = new Bundle();
                    map.putString("rank", l.getDisplayRank());
                    map.putString("formatScore", l.getDisplayScore());
                    map.putLong("score", l.getRawScore());
                    map.putString("name", l.getScoreHolderDisplayName());
                    map.putString("playerId", l.getScoreHolder().getPlayerId());
                    map.putInt("timestamp", (int)(l.getTimestampMillis()/1000));
                    scores.put(i, map);
                }

                for(int i = 0 ; i <scores.size(); i++){
                    Bundle map = scores.get(i);

                    LinearLayout lin = new LinearLayout(mContainer.getContext());
                    lin.setOrientation(LinearLayout.HORIZONTAL);
                    linear_easy.addView(lin);

                    TextView t = new TextView(mContainer.getContext());
                    t.setText(map.getString("name"));
                    lin.addView(t);

                    TextView t2 = new TextView(mContainer.getContext());
                    t.setText(map.getLong("score")+"");
                    lin.addView(t2);


                }


                Log.e("text", scores.toString());
            }
        };
        p.setResultCallback(pResultCallback);



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
