package com.example.mapiti;

import static com.example.mapiti.MainActivity.ANDROID_ID;
import static com.example.mapiti.MainActivity.markers;
import static com.example.mapiti.myMap.places;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.example.mapiti.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import static com.example.mapiti.myMap.drawPlaces;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static GoogleMap mMap;
    private ActivityMapsBinding binding;
    public View like;
    public static LatLng newAddress;
    EditText search;
    FrameLayout addWarn;
    public static ArrayList<Place> likes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newAddress = null;
        likes = new ArrayList<>();


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findLikes();

        addWarn = findViewById(R.id.addWarning);
        addWarn.setVisibility(View.INVISIBLE);
    }

    public void drawPlaces(ArrayList<MarkerOptions> markers) {
        for (MarkerOptions marker : markers) {
            if (marker != null)
                mMap.addMarker(marker);
        }
    }

    public void findLikes() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user").child(ANDROID_ID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    int likeValue = Integer.parseInt(childSnapshot.child("like").getValue().toString());
                    if (likeValue == 1) {
                        Place place = findPlaceById(childSnapshot.getKey());
                        if (!likes.contains(place)) likes.add(place);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        drawPlaces(markers);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(59.950002, 30.316672), 10));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng clickerPoint = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(clickerPoint));
                Place place = find(clickerPoint);
                showBottomSheetDialog(place);
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                addWarn.setVisibility(View.VISIBLE);
                TextView textWarn = findViewById(R.id.textWarning);
                textWarn.setText("Вы действительно хотите добавить новую точку?");
                newAddress = latlng;
            }
        });
        search = findViewById(R.id.searchText);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    try {
                        performSearch();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void performSearch() throws IOException {
        String toFind = "Санкт-Петербург" + String.valueOf(search.getText());
        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> res = geocoder.getFromLocationName(toFind, 1);
        Double first_coord = res.get(0).getLatitude();
        Double second_coord = res.get(0).getLongitude();
        LatLng mapPoint = new LatLng(first_coord, second_coord);
        int triggerToAsk = -1;
        for (Pair<LatLng, Place> place : places) {
            if (mapPoint == place.first) {
                triggerToAsk = 1;
                showBottomSheetDialog(place.second);
                break;
            }
        }
        if (triggerToAsk == -1) {
            addWarn.setVisibility(View.VISIBLE);
            TextView textWarn = findViewById(R.id.textWarning);
            textWarn.setText("Вы действительно хотите добавить новую точку? По адресу: " + search.getText());
            newAddress = mapPoint;
        }
    }

    public void onClickOkey(View v) {
        startActivity(new Intent(MapsActivity.this, AddPlaceActivity.class));
    }

    public void onClickNo(View v) {
        addWarn.setVisibility(View.INVISIBLE);
    }


    public void showBottomSheetDialog(Place place) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout);
        bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();
        TextView textName = (TextView) bottomSheetDialog.findViewById(R.id.text_name);
        TextView textAbout = (TextView) bottomSheetDialog.findViewById(R.id.text_about);
        TextView textRoute = (TextView) bottomSheetDialog.findViewById(R.id.text_route);
        ImageView image = (ImageView) bottomSheetDialog.findViewById(R.id.imageGraffiti);
        like = (View) bottomSheetDialog.findViewById(R.id.like);
        TextView id = (TextView) bottomSheetDialog.findViewById(R.id.id_place);
        textName.setText(place.name);
        textAbout.setText(place.about);
        textRoute.setText(place.route);
        id.setText(place.ID);
        Picasso.get().load(place.photo).into(image);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user").child(ANDROID_ID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int likeValue = Integer.parseInt(dataSnapshot.child(place.ID).child("like").getValue().toString());
                if (likeValue == 1) {
                    like.setVisibility(View.INVISIBLE);
                    if (!likes.contains(place)) likes.add(place);
                }
                if (likeValue == 0) {
                    if (likes.contains(place)) likes.remove(place);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void onClickAddButton(View v) {
        startActivity(new Intent(MapsActivity.this, AddPlaceActivity.class));
    }

    public Place find(LatLng latlng) {
        for (int i = 0; i < places.size(); i++) {
            if ((places.get(i).first.latitude == latlng.latitude) && (places.get(i).first.longitude == latlng.longitude)) {
                return (Place) places.get(i).second;
            }
        }
        return null;
    }

    public void onClickFavButton(View v) {
        startActivity(new Intent(MapsActivity.this, FavoriteActivity.class));
        overridePendingTransition(0, 0);
    }

    public Place findPlaceById(String id) {
        for (Pair<LatLng, Place> place : places) {
            if (place.second.ID.equals(id)) {
                return place.second;
            }
        }
        return null;
    }

    public void onClickLike(View v) {
        View parent = (TextView) v.getRootView().findViewById(R.id.id_place);
        String id = ((TextView) parent).getText().toString();

        final String[] click = {"active"};

        Place place = findPlaceById(id);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user").child(ANDROID_ID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int likeValue = Integer.parseInt(dataSnapshot.child(id).child("like").getValue().toString());
                if (likeValue == 0 && click[0] == "active") {
                    v.setVisibility(View.INVISIBLE);
                    if (!likes.contains(place)) likes.add(place);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user").child(ANDROID_ID);
                    ref.child(id).setValue(new Like(1));
                    click[0] = "block";
                }
                if (likeValue == 1 && click[0] == "active") {
                    like.setVisibility(View.VISIBLE);
                    likes.remove(place);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user").child(ANDROID_ID);
                    ref.child(id).setValue(new Like(0));
                    click[0] = "block";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void onClickProfile(View v) {
        startActivity(new Intent(MapsActivity.this, ProfileActivity.class));

    }

}