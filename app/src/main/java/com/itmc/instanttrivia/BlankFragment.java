package com.itmc.instanttrivia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlankFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;

    ImageView logo_1, logo_2, logo_3, ui_profile, ui_total_trophy, ui_dice_1, ui_dice_2, ui_dice_3, ui_dice_4, ui_coins;
    TextView ui_loged_as, ui_total_score, ui_score_easy, ui_score_med, ui_score_hard, ui_total_coins, ui_top_scores, ui_score_random;
    RelativeLayout ui_frag_back;
    RelativeLayout ui_bot_section;
    Button btn_play;

    public static BlankFragment newInstance() {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public BlankFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_blank,container,false);

        //define variables
        logo_1 = (ImageView) rootView.findViewById(R.id.ui_logo_1);
        logo_2 = (ImageView) rootView.findViewById(R.id.ui_logo_2);
        logo_3 = (ImageView) rootView.findViewById(R.id.ui_logo_3);
        ui_dice_1 = (ImageView) rootView.findViewById(R.id.ui_dice_1);
        ui_dice_2 = (ImageView) rootView.findViewById(R.id.ui_dice_2);
        ui_dice_3 = (ImageView) rootView.findViewById(R.id.ui_dice_3);
        ui_dice_4 = (ImageView) rootView.findViewById(R.id.ui_dice_4);
        ui_total_trophy = (ImageView) rootView.findViewById(R.id.ui_total_trophy);
        ui_profile = (ImageView) rootView.findViewById(R.id.ui_profile_img);
        ui_coins = (ImageView) rootView.findViewById(R.id.ui_coins);
        ui_loged_as = (TextView) rootView.findViewById(R.id.ui_loged_as);
        ui_total_score = (TextView) rootView.findViewById(R.id.ui_total_score);
        ui_score_easy = (TextView) rootView.findViewById(R.id.ui_score_easy);
        ui_score_med = (TextView) rootView.findViewById(R.id.ui_score_med);
        ui_score_hard = (TextView) rootView.findViewById(R.id.ui_score_hard);
        ui_score_random = (TextView) rootView.findViewById(R.id.ui_score_random);
        ui_total_coins = (TextView) rootView.findViewById(R.id.ui_total_coins);
        ui_top_scores = (TextView) rootView.findViewById(R.id.ui_top_scores);
        btn_play = (Button) rootView.findViewById(R.id.ui_button_play);
        ui_frag_back = (RelativeLayout) rootView.findViewById(R.id.ui_frag_back);
        ui_bot_section = (RelativeLayout) rootView.findViewById(R.id.ui_bot_section);

        overrideFonts(getActivity(),ui_frag_back);

        //set animation for view switcher
        final Animation left_trans = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_left_in_translate);
        final Animation right_trans = AnimationUtils.loadAnimation(getActivity(),R.anim.anim_right_in_translate);

        logo_1.startAnimation(left_trans);
        logo_2.startAnimation(right_trans);
        logo_3.startAnimation(right_trans);

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),Game_Timer.class);
                getActivity().startActivity(intent);
            }
        });
        Theme_Setter_Views();

        show_coins();

        return rootView;
    }

    //overrides all fonts at start , later generated need recall the method or static setting of typeface
    private void overrideFonts(final Context context, final View v) {
        Typeface new_font = Typeface.createFromAsset(context.getAssets(), "typeface/bubblegum.otf");
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof TextView ) {
                ((TextView) v).setTypeface(new_font);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //sets colors for internal views of layout
    private void Theme_Setter_Views(){
        String tester = StartActivity.settings.getString("Color_Theme","Purple");
        switch (tester){
            case "Red":
                ui_frag_back.setBackgroundColor(getResources().getColor(R.color.red_500));
                ui_top_scores.setTextColor(getResources().getColor(R.color.red_700));
                //ui_bot_section.setBackgroundColor(getResources().getColor(R.color.red_700));
                break;
            case "Purple":
                ui_frag_back.setBackgroundColor(getResources().getColor(R.color.deep_purple_500));
                ui_top_scores.setTextColor(getResources().getColor(R.color.deep_purple_700));
                //ui_bot_section.setBackgroundColor(getResources().getColor(R.color.purple_700));
                break;
            case "Blue":
                ui_frag_back.setBackgroundColor(getResources().getColor(R.color.blue_500));
                ui_top_scores.setTextColor(getResources().getColor(R.color.blue_700));
                //ui_bot_section.setBackgroundColor(getResources().getColor(R.color.blue_700));
                break;
            case "LGreen":
                ui_frag_back.setBackgroundColor(getResources().getColor(R.color.light_green_500));
                ui_top_scores.setTextColor(getResources().getColor(R.color.light_green_700));
                //ui_bot_section.setBackgroundColor(getResources().getColor(R.color.light_green_700));
                break;
            case "Orange":
                ui_frag_back.setBackgroundColor(getResources().getColor(R.color.orange_500));
                ui_top_scores.setTextColor(getResources().getColor(R.color.orange_700));
                //ui_bot_section.setBackgroundColor(getResources().getColor(R.color.orange_700));
                break;
        }
    }

    private void show_coins(){
        ui_coins.setVisibility(View.VISIBLE);
        ui_total_coins.setText(""+ StartActivity.settings.getInt("Coins", 5));
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ui_button_play:

                break;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
