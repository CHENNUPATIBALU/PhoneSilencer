package com.phonesilencer;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LocationsFragment extends Fragment {

    LinearLayout rootLayout;
    StorageHelper storageHelper;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_locations, container, false);

        if(container!=null){
            container.removeAllViews();
        }

        rootLayout = root.findViewById(R.id.locationsLayout);
        storageHelper = new StorageHelper(getActivity(),"Silencer",null,1);

        populateLocations();

        return root;
    }

    public void populateLocations(){
        String allLocations = new StorageHelper(getActivity(),"Silencer",null,1).getData();
        if(!allLocations.equals("")){
            String[] locations = allLocations.split("\n");

            for(String location: locations){
                String[] loc = location.split(";");
                populateLocation(loc[0],loc[1],loc[3]);
            }
        }else{
            Toast.makeText(getActivity(), "No Locations added", Toast.LENGTH_SHORT).show();
        }
    }

    public void populateLocation(String name, String coordinates, String checked){
        FrameLayout frameLayout = new FrameLayout(getActivity());
        FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        frameLayout.setLayoutParams(fParams);

        CardView locationCard = new CardView(getActivity());
        locationCard.setElevation(10);
        locationCard.setRadius(5);
        FrameLayout.LayoutParams locationCardParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        locationCardParams.setMargins(20,20,20,20);
        locationCard.setLayoutParams(locationCardParams);

        LinearLayout verticalLayout = new LinearLayout(getActivity());
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        verticalParams.setMargins(20,20,20,20);
        verticalLayout.setLayoutParams(verticalParams);

        LinearLayout horizontalLayout = new LinearLayout(getActivity());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        horizontalLayout.setLayoutParams(horizontalParams);

        TextView locationNameTv = new TextView(getActivity());
        locationNameTv.setText(name);
        locationNameTv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams locationNameTvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        locationNameTv.setLayoutParams(locationNameTvParams);

        horizontalLayout.addView(locationNameTv);

        LinearLayout horizontalLayout1 = new LinearLayout(getActivity());
        horizontalLayout1.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout1.setGravity(Gravity.END|Gravity.TOP);
        LinearLayout.LayoutParams horizontalLayout1Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        horizontalLayout1.setLayoutParams(horizontalLayout1Params);

        SwitchCompat switchCompat = new SwitchCompat(getActivity());
        boolean check = (checked.equals("1"))?true:false;
        Log.d(TAG, "populateLocation: "+checked);

        switchCompat.setChecked(check);
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switchParams.setMargins(0,0,70,0);
        switchCompat.setLayoutParams(switchParams);

        horizontalLayout1.addView(switchCompat);
        horizontalLayout.addView(horizontalLayout1);
        verticalLayout.addView(horizontalLayout);

        TextView coordTv = new TextView(getActivity());
        coordTv.setText(coordinates);
        LinearLayout.LayoutParams coordParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        coordTv.setLayoutParams(coordParams);

        verticalLayout.addView(coordTv);
        locationCard.addView(verticalLayout);

        CardView deleteCard = new CardView(getActivity());
        deleteCard.setRadius(40);
        deleteCard.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.delete_bg)));
        LinearLayout.LayoutParams deleteCardParams = new LinearLayout.LayoutParams(80,80);
        deleteCardParams.setMargins(120,10,10,10);
        deleteCard.setLayoutParams(deleteCardParams);

        ImageView deleteImg = new ImageView(getActivity());
        deleteImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_delete_outline_24));
        deleteImg.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        deleteImg.setLayoutParams(flParams);

        deleteCard.addView(deleteImg);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);

        LinearLayout layout1 = new LinearLayout(getActivity());
        FrameLayout.LayoutParams layout1Params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout1Params.gravity = Gravity.END;
        layout1.setLayoutParams(layout1Params);

        layout1.addView(deleteCard);

        deleteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Deleting the location");
                progressDialog.setCancelable(false);
                progressDialog.show();
                storageHelper.deleteDataByLocationName(name);
                progressDialog.dismiss();
                layout.setVisibility(View.GONE);
                rootLayout.removeView(layout);
            }
        });

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                storageHelper.changeLocationStatus(name,(b)?1:0);
            }
        });

        locationCard.addView(layout1);
        frameLayout.addView(locationCard);
        layout.addView(frameLayout);
        rootLayout.addView(layout);
    }
}