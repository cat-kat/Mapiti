package com.example.mapiti;

import static com.example.mapiti.MainActivity.ANDROID_ID;
import static com.example.mapiti.MapsActivity.newAddress;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class AddPlaceActivity extends AppCompatActivity {
    ImageView loadedImage;
    Button chooseButton;
    StorageReference mStorageRef;
    Uri uploadUri;
    EditText nameInput;
    EditText aboutInput;
    EditText routeInput;
    EditText addressInput;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        mStorageRef = FirebaseStorage.getInstance().getReference("ImageGrafitti");
        loadedImage = (ImageView) findViewById(R.id.loadedImage);
        chooseButton = (Button) findViewById(R.id.chooseButton);
        nameInput = (EditText) findViewById(R.id.nameInput);
        aboutInput = (EditText) findViewById(R.id.aboutInput);
        routeInput = (EditText) findViewById(R.id.routeInput);
        addressInput = (EditText) findViewById(R.id.yInput);
        text = findViewById(R.id.textToChoose);
        text.setVisibility(View.VISIBLE);

        if (newAddress != null) {
            addressInput.setText("Адрес выбран на карте");
            addressInput.setFocusable(false);
            addressInput.setLongClickable(false);
        } else {
            addressInput.setFocusable(true);
            addressInput.setLongClickable(true);
            addressInput.setText("");
        }
    }

    public void onClickChooseImage(View view) {
        getImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null && data.getData() != null) {
            if (resultCode == RESULT_OK) {
                loadedImage.setImageURI(data.getData());
                //upload();
            }
        }
    }

    public void upload() {
        Bitmap bitmap = ((BitmapDrawable) loadedImage.getDrawable()).getBitmap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteImage = baos.toByteArray();
        final StorageReference mRef = mStorageRef.child(String.valueOf(System.currentTimeMillis()));
        UploadTask ut = mRef.putBytes(byteImage);
        Task<Uri> task = ut.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                try {
                    saveUser();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getImage() {
        Intent intentChooser = new Intent();
        intentChooser.setType("image/");
        intentChooser.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentChooser, 1);
        text.setVisibility(View.INVISIBLE);
    }

    public void onClickSave(View v) {
        upload();
    }

    public void saveUser() throws IOException {
        String name = nameInput.getText().toString();
        String route = routeInput.getText().toString();
        String about = aboutInput.getText().toString();
        Double first_coord, second_coord;

        if (newAddress == null) {
            String address = "Санкт-Петербург" + addressInput.getText().toString();
            Geocoder geocoder = new Geocoder(getApplicationContext());
            List<Address> res = geocoder.getFromLocationName(address, 1);
            first_coord = res.get(0).getLatitude();
            second_coord = res.get(0).getLongitude();
        } else {
            first_coord = newAddress.latitude;
            second_coord = newAddress.longitude;
        }

        Place newGraffiti = new Place(name, route, about, uploadUri.toString(), first_coord, second_coord, (String.valueOf(first_coord) + "!" + String.valueOf(second_coord)).replaceAll("\\.", "-"));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("places").push();
        ref.setValue(newGraffiti);

        FirebaseDatabase.getInstance().getReference().child("user").child(ANDROID_ID).child(newGraffiti.ID).setValue(new Like(0));
        myMap.addPlaceToLocal(newGraffiti);

        newAddress = null;

        startActivity(new Intent(AddPlaceActivity.this, MapsActivity.class));
        overridePendingTransition(0, 0);

    }

    public void onClickHomeButton(View v) {
        startActivity(new Intent(AddPlaceActivity.this, MapsActivity.class));
        overridePendingTransition(0, 0);


    }

    public void onClickFavButton(View v) {

        startActivity(new Intent(AddPlaceActivity.this, FavoriteActivity.class));
        overridePendingTransition(0, 0);
    }

    public void onClickProfile(View v) {
        startActivity(new Intent(AddPlaceActivity.this, ProfileActivity.class));
        overridePendingTransition(0, 0);

    }

}
