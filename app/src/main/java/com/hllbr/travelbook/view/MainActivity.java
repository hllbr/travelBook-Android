package com.hllbr.travelbook.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hllbr.travelbook.R;
import com.hllbr.travelbook.adapter.CustomAdapter;
import com.hllbr.travelbook.model.Place;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase database ;
    //buradaki verileri kaydedeceğim bir dizi olması gerekiyor
    ArrayList<Place> placeList  =new ArrayList<Place>();
    ListView listView ;
    CustomAdapter customAdapter ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        getData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_place,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_place_item){
            Intent intent = new Intent(MainActivity.this,MapsActivity.class);
            intent.putExtra("info","new");//new data
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    public void getData(){
        customAdapter = new CustomAdapter(this,placeList);
        try {
            database = this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM places",null);
            //Listede ne göstermem gerekiyorsa onu çekmem gerek diğerleri gereksiz olacaktir.
            //benim için name ve id yeterli name kullanıcıya gösterilirken id ile hangi elemnte gtıklandığını ayırt edebilirim

            int nameIx = cursor.getColumnIndex("name");
            int latitudeIx = cursor.getColumnIndex("latitude");
            int longitudeIx = cursor.getColumnIndex("longitude");

            while(cursor.moveToNext()){
                String nameFromDatabase = cursor.getString(nameIx);
                String latitudeFromDatabase = cursor.getString(latitudeIx);
                String longitudeFromDatabase = cursor.getString(longitudeIx);

               Double latitudeDouble = Double.parseDouble(latitudeFromDatabase);
               Double longitudeDouble = Double.parseDouble(longitudeFromDatabase);

               Place place = new Place(nameFromDatabase,latitudeDouble,longitudeDouble);

                System.out.println(place.name);

               placeList.add(place);

            }
            customAdapter.notifyDataSetChanged();
            cursor.close();

        }catch (Exception ex){
            ex.printStackTrace();
        }
        /*
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,placeList);
        Sadece adaptere bir string göndermediğimiz için bu yapıyı kullanamayız

         */


        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("place",placeList.get(position));
                startActivity(intent);
            }
        });
    }
}