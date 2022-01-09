package com.example.mapiti;

import static com.example.mapiti.MainActivity.ANDROID_ID;
import static com.example.mapiti.MapsActivity.likes;
import static com.example.mapiti.R.layout.activity_favorite;
import static com.example.mapiti.myMap.places;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {
    Button infoFav;
    LinearLayout layoutCardsFav;
    int favNow;
    FrameLayout addWarn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_favorite);

        MapsActivity.newAddress = null;
        favNow = -1;

        layoutCardsFav = (LinearLayout) findViewById(R.id.image_container);
        drawFav();

        infoFav = findViewById(R.id.info_fav);
        infoFav.setVisibility(View.INVISIBLE);
        addWarn = findViewById(R.id.addWarningNoRoute);
        addWarn.setVisibility(View.INVISIBLE);

    }



    public void drawFav() {
        int id = 0;
        for (Place place : likes) {
            CardView cardView;
            cardView = new CardView(getApplicationContext());
            cardView.setRadius(40);
            cardView.setId(id);
            id++;
            cardView.setOnClickListener(this::onClickFav);
            cardView.setCardElevation(20);
            LinearLayout.LayoutParams cardViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardView.setLayoutParams(cardViewParams);
            ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
            if (likes.indexOf(place) == likes.size() - 1)
                cardViewMarginParams.setMargins(30, 0, 30, 0);
            else cardViewMarginParams.setMargins(30, 0, 0, 0);
            cardView.requestLayout();

            TextView textView = new TextView(getApplicationContext());
            ViewGroup.LayoutParams paramsText = new ViewGroup.LayoutParams(270, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(paramsText);
            textView.setPadding(20, 350, 20, 10);
            textView.setTextColor(Color.WHITE);
            textView.setText(place.name);

            ImageView imageView;
            imageView = new ImageView(getApplicationContext());
            Picasso.get().load(place.photo).into(imageView);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(270, 400);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            cardView.addView(imageView);
            cardView.addView(textView);
            textView.setGravity(Gravity.BOTTOM);
            layoutCardsFav.addView(cardView);
        }
    }

    public void onClickHomeButton(View v) {
                startActivity(new Intent(FavoriteActivity.this, MapsActivity.class));
                overridePendingTransition(0, 0);
    }

    public void onClickFav(View v) {
        for (int i = 0; i < likes.size(); i++)
            if (v.getId() == i) {

                TextView nameFav, routeFav;
                ImageView imageFav;
                nameFav = findViewById(R.id.name_fav);
                routeFav = findViewById(R.id.route_fav);

                imageFav = (ImageView) findViewById(R.id.image_fav);
                Picasso.get().load(likes.get(i).photo).into(imageFav);
                imageFav.setScaleType(ImageView.ScaleType.CENTER_CROP);

                nameFav.setText(likes.get(i).name);
                routeFav.setText(likes.get(i).route);
                infoFav.setVisibility(View.VISIBLE);
                infoFav.setOnClickListener(this::onClickFavInfo);
                favNow = i;
                break;
            }

    }

    public void onClickFavInfo(View v) {
        showBottomSheetDialogFav(likes.get(favNow));
    }

    private void showBottomSheetDialogFav(Place place) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout_fav);
        bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();
        TextView textName = (TextView) bottomSheetDialog.findViewById(R.id.text_name);
        TextView textAbout = (TextView) bottomSheetDialog.findViewById(R.id.text_about);
        TextView textRoute = (TextView) bottomSheetDialog.findViewById(R.id.text_route);
        textName.setText(place.name);
        textAbout.setText(place.about);
        textRoute.setText(place.route);

    }

    public void onClickDeleteFav(View v) {
        layoutCardsFav.removeView(layoutCardsFav.getChildAt(favNow));
        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference().child("user").child(ANDROID_ID);
        ref.child(likes.get(favNow).ID).setValue(new Like(0));

        likes.remove(likes.get(favNow));
        for (int i=0; i<likes.size(); i++) {
            layoutCardsFav.getChildAt(i).setId(i);
        }
        infoFav.setVisibility(View.INVISIBLE);

    }

    public void onClickMakeRoute(View v) {
        addWarn.setVisibility(View.VISIBLE);

    }
    public void onClickNoRoute (View v) {
        addWarn.setVisibility(View.INVISIBLE);
    }

    public void onClickProfile(View v) {
        startActivity(new Intent(FavoriteActivity.this, ProfileActivity.class));
        overridePendingTransition(0, 0);
    }
}
