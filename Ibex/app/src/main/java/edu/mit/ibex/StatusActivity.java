package edu.mit.ibex;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class StatusActivity extends ActionBarActivity {
    private String LOG_MESSAGE = "HangMonkey";

    ImageButton mapsButton, friendsButton;
    EditText editStatus;
    Firebase myFirebase;
    Switch available;
    String username, toUser;
    HashMap<String, Object> data;
    TextView myStatus;

    String friendStatus;
    String location;
    String tempStatus;
    List<String> friendsInfo;
    List<String> friendsName;
    List<List<String>> friendLocation;
    ListView theListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        username = intent.getExtras().getString("username");

        setContentView(R.layout.activity_status);
        Firebase.setAndroidContext(this);
        myStatus = (TextView) findViewById(R.id.MyStatus);
        mapsButton = (ImageButton) findViewById(R.id.mapsButton);
        friendsButton = (ImageButton) findViewById(R.id.friendsButton);
        editStatus = (EditText) findViewById(R.id.editStatus);
        available = (Switch) findViewById(R.id.available);
        myFirebase = new Firebase("https://hangmonkey.firebaseio.com/");

        friendsInfo = new ArrayList<String>();
      //  friendsName = new ArrayList<String>();
      //  friendLocation = new ArrayList<List<String>>();
        theListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> resultsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,friendsInfo);
        theListView.setAdapter(resultsAdapter);

        /**
         * Makes only one call to Firebase
         */
        myFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                data = (HashMap<String, Object>)snapshot.getValue();

                Log.d("Data : ", data.toString());
                showFriendInfo(username, data);
            }
            @Override public void onCancelled(FirebaseError error) { }
        });

        /**
         * Makes N calls to Firebase where N is total number of friends.
         */
//        myFirebase.child(username).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
////                System.out.println(snapshot.getValue());
//
//                data = (HashMap<String, Object>)snapshot.getValue();
//
//                System.out.println(snapshot.getKey());
//                Log.d("Data : ", data.toString());
////                System.out.println(data.get("status").toString());
////                System.out.println(data.get("friends").toString());
//                showFriendInfo(snapshot.getKey(), data.get("status").toString(), data.get("friends"));
//            }
//            @Override public void onCancelled(FirebaseError error) { }
//        });
    }

    /*
     Makes only one call to Firebase
     */
    private void showFriendInfo(String currentUser, HashMap data) {
        HashMap currentUserInfo = (HashMap) data.get(currentUser);
        System.out.println(currentUserInfo);
        String status = currentUserInfo.get("status").toString();
        System.out.println(status);

        myStatus.setTextSize(20);
        myStatus.setTypeface(null, Typeface.ITALIC);
        //BOLD_ITALIC
        myStatus.setText("My Status: " + status);

        HashMap friendsDict = (HashMap) currentUserInfo.get("friends");
        if (friendsDict != null) {
            for (Object timestamp : friendsDict.keySet()) {
                HashMap name = (HashMap) friendsDict.get(timestamp);
                System.out.println(name.keySet().toArray()[0]);
                String friendName = name.get(name.keySet().toArray()[0]).toString();
                System.out.println(friendName);

                HashMap friendInfo = (HashMap) data.get(friendName);
                String friendStatus = friendInfo.get("status").toString();

                friendsInfo.add(friendName + ": " + friendStatus);
               // friendsName.add(friendName);

              //  Double lat = Double.parseDouble(friendsDict.get(friendName).get(lat));
                System.out.println("FriendsInfo " + friendsInfo);
                addFriendList(friendsInfo);

            }
        }

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long myLong) {
                Log.d("friendClick", "clicked!");
                String selectedFromList = (String) (theListView.getItemAtPosition(myItemInt));
//                do whatever here
                final String selectedFriend = selectedFromList.split(":")[0];
                Log.d("friendClick", selectedFriend);
                clickFriend(selectedFriend);
            }
        });
    }

    /*
    Makes N calls to Firebase where N is total number of friends
     */
    private void showFriendInfo(String user, String status, Object friends) {
        myStatus.setTextSize(20);
        myStatus.setTypeface(null, Typeface.ITALIC);
        //BOLD_ITALIC
        myStatus.setText("\"" + status + "\"");

        HashMap friendsDict = (HashMap) friends;
        System.out.println(friendsDict);
        System.out.println(friendsDict.keySet());

        if (friendsDict != null) {
            for (Object timestamp : friendsDict.keySet()) {
                HashMap name = (HashMap) friendsDict.get(timestamp);
                System.out.println(name);
                System.out.println(name.keySet().toArray()[0]);
                String friendName = name.get(name.keySet().toArray()[0]).toString();
                System.out.println(friendName);
                myFirebase.child(friendName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        data = (HashMap<String, Object>) snapshot.getValue();
                        Log.d("Friend Data : ", data.toString());
                        if (!(data.get("status").equals(null))) {
                            tempStatus = data.get("status").toString();
                            System.out.println("inside" + tempStatus);
                            friendsInfo.add(snapshot.getKey() + ": " + data.get("status"));

                            System.out.println("FriendsInfo " + friendsInfo);
                            addFriendList(friendsInfo);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                    }

                });
            }
        }

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long myLong) {
                Log.d("friendClick", "clicked!");
                String selectedFromList = (String) (theListView.getItemAtPosition(myItemInt));
//                do whatever here
                final String selectedFriend = selectedFromList.split(":")[0];
                Log.d("friendClick", selectedFriend);
                clickFriend(selectedFriend);
            }
        });
    }

    private void clickFriend(String selectedFriend) {
        myFirebase.child(selectedFriend).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
//                System.out.println(snapshot.getValue());
                data = (HashMap<String, Object>) snapshot.getValue();
                System.out.println(data);

                friendStatus = data.get("status").toString();
                Log.d("friendClick", friendStatus);
                location = data.get("lat").toString() + "," + data.get("long").toString();
                Log.d("friendClick", location);

                statusPop(snapshot.getKey(), friendStatus, location);
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });

    }
    private void addFriendList(List<String> friendsInfo) {
        ArrayAdapter<String> resultsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,friendsInfo);
        theListView.setAdapter(resultsAdapter);
    }

    private void statusPop(String user, String status, String location) {
//        Log.d("statusPop", "Inside Status Pop");
//        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
//        dlgAlert.setMessage(status + "\n"+location);
//        dlgAlert.setTitle(user);
//        dlgAlert.setPositiveButton("OK", null);
//        dlgAlert.setCancelable(true);
//        dlgAlert.create().show();
        toUser = user;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage(status + "\n" + location);
        alert.setTitle(user);
        alert.setPositiveButton("Message", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                message(toUser);
            }
        });
        alert.setNegativeButton("OK", null);
        alert.show();
    }

    public void addFriend(View view) {
        friendPop(username);
    }
    private void friendPop(String user) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext= new EditText(StatusActivity.this);
        alert.setMessage("Add a Friend");
        alert.setView(edittext);

        alert.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final Firebase frands = new Firebase("https://hangmonkey.firebaseio.com/" + username + "/friends");
                final String friendName = edittext.getText().toString();
                frands.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        friendsInfo = new ArrayList<String>();
                        HashMap<String, String> putName = new HashMap<String, String>();
                        putName.put("name", friendName);
                        frands.push().setValue(putName);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }
        });
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void mapsClick(View v) {
        Intent i = new Intent(this, MapsActivity.class);
        if(username!=null){
        i.putExtra("username",username);
        }

        List<ArrayList<String>> allFriendsInfo = new ArrayList<ArrayList<String>>();
        HashMap<String,Object> friends = (HashMap<String, Object>) data.get(username);

        for(String name : data.keySet()){
            List<String> friendInfo = new ArrayList<String>();
            HashMap<String,Object> dataForFriend = (HashMap<String, Object>) data.get(name);
            Object lat =  dataForFriend.get("lat");
            Object lon =  dataForFriend.get("long");
            if(lat!=null && lon!=null){
            friendInfo.add(name);
            friendInfo.add(lat.toString());
            friendInfo.add(lon.toString());
            friendInfo.add((String) dataForFriend.get("status"));
            }
            allFriendsInfo.add((ArrayList<String>) friendInfo);
        }
        Log.d("All f info", allFriendsInfo.toString());
        i.putExtra("friends", (java.io.Serializable) allFriendsInfo);
        startActivity(i);
    }
    public void postStatus(View v) {
        friendsInfo = new ArrayList<String>();
        boolean on = available.isChecked();
        myFirebase.child(username + "/status").setValue(editStatus.getText().toString());
        myFirebase.child(username + "/available").setValue(on);
        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);

        if(location!=null){
            // Getting latitude of the current location
            double latitude = location.getLatitude();
            // Getting longitude of the current location
            double longitude = location.getLongitude();
            String latString = Double.toString(latitude);
            String longString = Double.toString(longitude);
            myFirebase.child(username + "/lat").setValue(latitude);
            myFirebase.child(username + "/long").setValue(longitude);
        }

    public void message(String user) {
        Log.d("testing", "did it work?");
    }
}


