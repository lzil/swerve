package edu.mit.ibex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MapsActivity extends FragmentActivity {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng myLocation;
    Firebase myFirebase;
    Map<String, Object> data;
    Map<String, Object> newData;
    ArrayList<ArrayList<String>> don;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }

        username = extras.getString("curUser");
        don = (ArrayList<ArrayList<String>>) extras.getSerializable("friends");
        Log.d("FRiends",don.toString());
        Log.d("user",username);
        System.out.println(username);
        setUpMapIfNeeded();
    }
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    @Override
    public  boolean onKeyDown(int keyCode, KeyEvent event){
        if((keyCode==KeyEvent.KEYCODE_BACK)){
            finish();
           Log.d("Message:","Activity Done YAY");
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        LatLng MIT = new LatLng(42.3598,-71.0921);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MIT, 15));
        mMap.setMyLocationEnabled(true);
        myFirebase = new Firebase("https://hangmonkey.firebaseio.com/");
        for (ArrayList<String> friend:don){
            if(friend.size()==4){
            String name = friend.get(0);
            String lat = friend.get(1);
            String lon = friend.get(2);
            String status = friend.get(3);
            Double x = Double.parseDouble(lat);
            Double y = Double.parseDouble(lon);
            addFriendsToMap(name,status,new LatLng(x,y));
            }
        }
        myFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {


            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });

    }

    public void addFriendsToMap(String friend, String status, LatLng location) {
        mMap.addMarker(new MarkerOptions().position(location).title(friend).snippet(status));
    }
    public void mapsClick(View v) {
        /*Intent i = new Intent(this, MapsActivity.class);
        if(curUser!=null){
            i.putExtra("curUser",curUser);}
        startActivity(i);*/
    }

    public void friendsClick(View v) {
        Intent i = new Intent(this, StatusActivity.class);
        startActivity(i);
    }

}


      /*  GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                Marker mMarker = mMap.addMarker(new MarkerOptions().position(loc));
                if(mMap != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                }
            }
        };
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);*/
