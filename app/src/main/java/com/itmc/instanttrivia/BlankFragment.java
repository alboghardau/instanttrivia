package com.itmc.instanttrivia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlankFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    ImageView logo_1, logo_2, logo_3;
    RelativeLayout ui_frag_back;
    RelativeLayout ui_bot_section;
    Button btn_play;

    private LinearLayout lin_top_logo;
    private RelativeLayout rel_logged;


    public static BlankFragment newInstance(String param1, String param2) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public BlankFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View rootView = inflater.inflate(R.layout.fragment_blank,container,false);

        //define variables
        logo_1 = (ImageView) rootView.findViewById(R.id.ui_logo_1);
        logo_2 = (ImageView) rootView.findViewById(R.id.ui_logo_2);
        logo_3 = (ImageView) rootView.findViewById(R.id.ui_logo_3);
        btn_play = (Button) rootView.findViewById(R.id.ui_button_play);
        ui_frag_back = (RelativeLayout) rootView.findViewById(R.id.ui_frag_back);
        ui_bot_section = (RelativeLayout) rootView.findViewById(R.id.ui_bot_section);

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

        return rootView;
    }

    //sets colors for internal views of layout
    private void Theme_Setter_Views(){
        String tester = StartActivity.settings.getString("Color_Theme","Purple");
        switch (tester){
            case "Red":
                ui_frag_back.setBackgroundColor(getResources().getColor(R.color.red_500));
                ui_bot_section.setBackgroundColor(getResources().getColor(R.color.red_800));
                break;
            case "Purple":
                ui_frag_back.setBackgroundColor(getResources().getColor(R.color.purple_500));
                ui_bot_section.setBackgroundColor(getResources().getColor(R.color.purple_800));
                break;
            case "Blue":
                ui_frag_back.setBackgroundColor(getResources().getColor(R.color.blue_500));
                ui_bot_section.setBackgroundColor(getResources().getColor(R.color.blue_800));
                break;
            case "LGreen":
                ui_frag_back.setBackgroundColor(getResources().getColor(R.color.light_green_500));
                ui_bot_section.setBackgroundColor(getResources().getColor(R.color.light_green_800));
                break;
            case "Orange":
                ui_frag_back.setBackgroundColor(getResources().getColor(R.color.orange_500));
                ui_bot_section.setBackgroundColor(getResources().getColor(R.color.orange_800));
                break;
        }
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
