package com.hllbr.travelbook.view;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hllbr.travelbook.R;
import com.hllbr.travelbook.model.Place;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager ;
    LocationListener locationListener ;
    SQLiteDatabase database ;

    @Override
    public void onBackPressed() {//geri tuşuna basıldığında ne olacak
        super.onBackPressed();
        Intent intenttoMain = new Intent(this,MainActivity.class);
        startActivity(intenttoMain);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if(info.matches("new")){
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    //Move Camera bizden LatLng istiyor bu sebeple önce LatLong oluşturmam gerek burada marker oluşturmamı gerektirecek bir drum yok

                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.hllbr.travelbook",MODE_PRIVATE);
                    boolean track = sharedPreferences.getBoolean("trackBoolean",false);
                    if(track == false){
                        LatLng userlocation = new LatLng(location.getAltitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation,15));
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply();
                    }

                }
            };
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                //onLocationChange sürekli çalıştırmak istemiyoruz

                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastLocation != null){
                    LatLng lastUserLocation = new LatLng(lastLocation.getAltitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));

                }
            }
        }else{
            //SQLite Data && IntentData
            //SQLite üzerinden çekilmiş datayı buraya aktarıp burada kullanacağız
            mMap.clear();
            Place place1 =(Place) intent.getSerializableExtra("place");
            //marker oluşturabilmek için mutlaka bir latlong ihtiyacım bulunuyor
            LatLng latLng = new LatLng(place1.latitude,place1.longitude);
            String placeName = place1.name ;

            mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length >0){
            if(requestCode == 1){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Intent intent = getIntent();

                String info1 = intent.getStringExtra("new");
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(lastLocation != null){
                        LatLng lastUserLocation = new LatLng(lastLocation.getAltitude(),lastLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));

                    }else{
                        //SQLite Data
                        mMap.clear();
                        Place place1 =(Place) intent.getSerializableExtra("place");
                        //marker oluşturabilmek için mutlaka bir latlong ihtiyacım bulunuyor
                        LatLng latLng = new LatLng(place1.latitude,place1.longitude);
                        String placeName = place1.name ;

                        mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                    }


                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addressList != null && addressList.size() > 0){
                if(addressList.get(0).getSubThoroughfare() != null){
                    address += addressList.get(0).getThoroughfare();

                    if(addressList.get(0).getSubThoroughfare() != null){
                        address += " ";
                        address+= addressList.get(0).getSubThoroughfare();

                    }
                }
            }else{
                address = "New Place";

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions().title(address).position(latLng));

        Double latitude = latLng.latitude;
        Double longitude = latLng.longitude;

        final Place place = new Place(address,latitude,longitude);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setCancelable(false);//yes / no diye soruyorsak illa seçmek zorunda bu yapı kullanıldığında

        alertDialog.setTitle("Are you sure ?");
        alertDialog.setMessage(place.name);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //DataBase operation area
                try{
                    database = MapsActivity.this.openOrCreateDatabase("Places",MODE_PRIVATE,null);

                    database.execSQL("CREATE TABLE IF NOT EXISTS places (id INTEGER PRIMARY KEY,name VARCHAR,latitude VARCHAR,longitude VARCHAR)");

                    String toCompile = "INSERT INTO places (name,latitude,longitude) VALUES (?,?,?)";

                    SQLiteStatement sqLiteDatabase = database.compileStatement(toCompile);
                    sqLiteDatabase.bindString(1,place.name);
                    sqLiteDatabase.bindString(2,String.valueOf(place.latitude));
                    sqLiteDatabase.bindString(3,String.valueOf(place.longitude));
                    sqLiteDatabase.execute();

                    Toast.makeText(getApplicationContext(),"Saved!",Toast.LENGTH_LONG).show();

                }catch (Exception ex){
                    ex.printStackTrace();
                }


            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"Canceled!",Toast.LENGTH_LONG).show();

            }
        });
        alertDialog.show();
    }
}