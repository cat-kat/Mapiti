package com.example.mapiti;

import static com.example.mapiti.MainActivity.ANDROID_ID;
import static com.example.mapiti.MainActivity.markers;
import static com.example.mapiti.MapsActivity.mMap;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

public class myMap {
    public static ArrayList<Pair<LatLng, Place>> places;

    public myMap() {
        places = new ArrayList<>();
        getPlaces();
    }


    public void getPlaces() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("places");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnap : snapshot.getChildren()) {

                    String name = childSnap.child("name").getValue().toString();
                    String route = childSnap.child("route").getValue().toString();
                    String about = childSnap.child("about").getValue().toString();
                    String photo = childSnap.child("photo").getValue().toString();
                    Double first_coord = (Double) childSnap.child("first_coord").getValue();
                    Double second_coord = (Double) childSnap.child("second_coord").getValue();
                    Place newGraffiti = new Place(name, route, about, photo, first_coord, second_coord, (String.valueOf(first_coord) + "!" + String.valueOf(second_coord)).replaceAll("\\.", "-"));

                    addPlaceToLocal(newGraffiti);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public static void addPlaceToLocal(Place place) {
        LatLng mapPoint = new LatLng(place.first_coord, place.second_coord);
        MarkerOptions marker = new MarkerOptions().position(mapPoint).title(place.name);
        markers.add(marker);
        places.add(new Pair(mapPoint, place));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user").child(ANDROID_ID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Like newLike;
                int likeValue = Integer.parseInt(dataSnapshot.child(place.ID).child("like").getValue().toString());
                if (likeValue == 1) {
                    newLike = new Like(1);
                } else newLike = new Like(0);

                ref.child(place.ID).setValue(newLike);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
