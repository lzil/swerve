package edu.mit.ibex;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
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
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class StatusActivity extends ActionBarActivity {
    ImageButton mapsButton, friendsButton;
    EditText editStatus;
    Switch available;
    String curUser, toUser;
    Firebase baseFire, myFire;
    HashMap<String, Object> data;
    TextView myStatus;

    String friendStatus;
    String location;
    String tempStatus;
    List<String> friendsInfo, friendsName;
    List<List<String>> friendLocation;
    ListView theListView;
    Set<String> userList; //List of ALL users in database. Used for checking if friend is valid


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        curUser = intent.getExtras().getString("curUser");

        setContentView(R.layout.activity_status);
        Firebase.setAndroidContext(this);
        baseFire = new Firebase("https://hangmonkey.firebaseio.com/");
        myFire = baseFire.child(curUser);
        myStatus = (TextView) findViewById(R.id.MyStatus);
        mapsButton = (ImageButton) findViewById(R.id.mapsButton);
        friendsButton = (ImageButton) findViewById(R.id.friendsButton);
        editStatus = (EditText) findViewById(R.id.editStatus);
        available = (Switch) findViewById(R.id.available);

        friendsInfo = new ArrayList<String>();
        theListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> resultsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,friendsInfo);
        theListView.setAdapter(resultsAdapter);

        /**
         * Makes only one call to Firebase
         */
        showStatusList();

        //notifications from Firebase

        myFire.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, String> msgPack = (Map<String, String>) snapshot.getValue();
                Notification notif = new Notification.Builder(StatusActivity.this)
                        .setContentTitle("New message from " + msgPack.get("name"))
                        .setContentText(msgPack.get("message"))
                        .setSmallIcon(R.mipmap.ic_action_mail)
                        .build();
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(0, notif);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_settings:
//                //Toast.makeText(getApplicationContext(),"Item 1 Selected",Toast.LENGTH_LONG).show();
//                //return true;
//                return super.onOptionsItemSelected(item);
            case R.id.action_logout:
                /*SharedPreferences sp = this.getSharedPreferences("Login", 0);

                String user = sp.getString("curUser", null);
                String pass = sp.getString("curPsw", null);*/
                SharedPreferences sp = getSharedPreferences("Login", 0);
                sp.edit().clear().commit();
                Log.d("sharepref", "deleted shared pref");
                super.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //availability
    public void availableClick(View v){
        if (available.isChecked()) {
            Log.d("availableClicked", "calling showStatusList");
            showStatusList();
        }else{
            Log.d("availableClicked", "clearing everything!");
            friendsInfo = new ArrayList<String>();
            ArrayAdapter<String> resultsAdapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,friendsInfo);
            theListView.setAdapter(resultsAdapter);
        }
    }


    //show the status list
    private void showStatusList(){
        Log.d("made link to Firebase","good");
        baseFire.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                data = (HashMap<String, Object>) snapshot.getValue();
                Log.d("data - (raw)", data.toString());
                Log.d("data - Users", data.keySet().toString());
                userList = data.keySet();
                Log.d("showStatusList", "calling showFriendInfo");
                showFriendInfo(curUser, data);
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    /*
     Makes only one call to Firebase
     */
    private void showFriendInfo(String currentUser, HashMap data) {
        HashMap currentUserInfo = (HashMap) data.get(currentUser);
        String status = currentUserInfo.get("status").toString();
        myStatus.setText(curUser + ": " + status);

        HashMap friendsDict = (HashMap) currentUserInfo.get("friends");
        System.out.println(friendsDict);
        if (friendsDict != null) {
            friendsInfo = new ArrayList<String>();
            Log.d("data", "friendsDict not null");
            for (Object timestamp : friendsDict.keySet()) {
                HashMap name = (HashMap) friendsDict.get(timestamp);
                String friendName = name.get(name.keySet().toArray()[0]).toString();

                HashMap friendInfo = (HashMap) data.get(friendName);
                String friendStatus = friendInfo.get("status").toString();
                Log.d("Add Friend", friendName+ " added to list");
                friendsInfo.add(friendName + ": " + friendStatus);

              //  Double lat = Double.parseDouble(friendsDict.get(friendName).get(lat));
                addFriendList(friendsInfo);

            }
        }else{
            Log.d("Error","No Friends");
        }

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long myLong) {
                Log.d("friendClick", "clicked!");
                String selectedFromList = (String) (theListView.getItemAtPosition(myItemInt));
                final String selectedFriend = selectedFromList.split(":")[0];
                Log.d("friendClick", selectedFriend);
                clickFriend(selectedFriend);
            }
        });
    }

    /*
    This decides what happens when we click a friend in the Friends List
    Right now, clicking a friend opens notification of their status and long/lat coordinates.
    We can make it so when a friend is clicked we open maps tab up to their location
     */
    private void clickFriend(String selectedFriend) {
        baseFire.child(selectedFriend).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
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

    /*
    Helper function to add a friend to the Friends List
     */
    private void addFriendList(List<String> friendsInfo) {
        ArrayAdapter<String> resultsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,friendsInfo);
        theListView.setAdapter(resultsAdapter);
    }

    /*
    Notification popup with more information about a friend
     */
    private void statusPop(String user, String status, String location) {
        toUser = user;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage(status + "\n" + location);
        alert.setTitle(toUser);
        alert.setPositiveButton("Message", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                message(toUser);
            }
        });
        alert.setNegativeButton("OK", null);
        alert.show();
    }

    /*
    Adds a friend to the database under current user's friends list
    Checks if friend exists in database and if friend is already in user's friends.
     */
    public void addFriend(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText friendInput = new EditText(StatusActivity.this);
        //only allows user to input max 24 chars (limit of curUser length)
        friendInput.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(24)});
        alert.setMessage("Add a Friend");
        alert.setView(friendInput);

        alert.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String friendName = friendInput.getText().toString();
                myFire.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List friends = new ArrayList();
                        HashMap<String,Object> friendList = (HashMap<String,Object> )snapshot.getValue();
                        System.out.println("friendsList");
                        System.out.println(friendList);


                        if (friendList != null) {
                            Log.d("data", "friendList not null");
                            for (Object timestamp : friendList.keySet()) {
                                HashMap name = (HashMap) friendList.get(timestamp);
                                String friendName = name.get(name.keySet().toArray()[0]).toString();
                                friends.add(friendName);
                            }
                        }

                        if (friends.contains(friendName)) {
                            //Check if friend is already in friends list
                            Log.d("Add Friend", friendName+" already added");
                            Toast.makeText(getApplicationContext(),
                                    friendName+" is already your friend!",
                                    Toast.LENGTH_LONG).show();
                        }else{
                            //Check if friend exists in database
                            if(userList.contains(friendName)){
                                if (friendName.equals(curUser)){
                                    Log.d("Add Friend", "Tried to add self. Motivational message sent");
                                    Toast.makeText(getApplicationContext(),
                                            "Are you lonely? You're always your own friend! :D",
                                            Toast.LENGTH_LONG).show();
                                }else{
                                    //Add friend
                                    friendsInfo = new ArrayList<String>();
                                    HashMap<String, String> putName = new HashMap<String, String>();
                                    putName.put("name", friendName);

                                    myFire.child("friends").push().setValue(putName);

                                    Log.d("Add Friend", friendName+"added");

                                    Toast.makeText(getApplicationContext(),
                                            friendName+" added!",
                                            Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Log.d("Add Friend", friendName+" does not exist");
                                Toast.makeText(getApplicationContext(),
                                        friendName+" does not use Swerve :(",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }
        });
        alert.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setAvailable(View v){
        if(!available.isChecked()){
            available.setChecked(true);
            availableClick(v);
        }
    }

    public void mapsClick(View v) {
        final List<String> ami = new ArrayList<String>();
        final Intent i = new Intent(this, MapsActivity.class);
        if (curUser != null) {
            i.putExtra("curUser", curUser);
        }
        myFire.child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String,String>> fd = (HashMap<String, HashMap<String,String>>) dataSnapshot.getValue();
                Collection<HashMap<String, String>> friendsNames = fd.values();
                for (HashMap<String, String> amiDic : friendsNames) {
                    String amigo = amiDic.get("name");
                    if (!ami.contains(amigo)) {
                        ami.add(amigo);
                    }
                }
                Log.d("HHHHH", ami.toString());

                List<ArrayList<String>> allFriendsInfo = new ArrayList<ArrayList<String>>();
                HashMap<String, Object> friends = (HashMap<String, Object>) data.get(curUser);

                Log.d("DataforFriends ", friends.toString());
                for (String name : ami) {
                    List<String> friendInfo = new ArrayList<String>();
                    HashMap<String, Object> dataForFriend = (HashMap<String, Object>) data.get(name);
                    Log.d("Name", name);
                    Log.d("Data4Name", dataForFriend.toString());
                    Object lat = dataForFriend.get("lat");
                    Object lon = dataForFriend.get("long");
                    boolean available = (boolean) dataForFriend.get("available");
                    if (lat != null && lon != null && available == true) {
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

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void postStatus(View v) {
        setAvailable(v);

        friendsInfo = new ArrayList<String>();
        tempStatus = editStatus.getText().toString();
        boolean on = available.isChecked();
        if (tempStatus.equals("")) {
            Toast.makeText(getApplicationContext(),
                    "Post a (nonempty) status!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        myFire.child("status").setValue(editStatus.getText().toString());
        editStatus.setText("");
        myFire.child("available").setValue(on);
        Toast.makeText(getApplicationContext(),
                "Status posted!",
                Toast.LENGTH_LONG).show();
        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);


        if (location != null) {
            // Getting latitude of the current location
            double latitude = location.getLatitude();
            // Getting longitude of the current location
            double longitude = location.getLongitude();
            //String latString = Double.toString(latitude);
            //String longString = Double.toString(longitude);
            myFire.child("lat").setValue(latitude);
            myFire.child("long").setValue(longitude);
        }
    }


    //Sends a message to a specified user
    public void message(String user) {
        toUser = user;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage("Message to " + toUser);
        alert.setTitle(toUser);
        final EditText edittext= new EditText(StatusActivity.this);
        alert.setView(edittext);

        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String msg = edittext.getText().toString();
                HashMap<String, String> putMessage = new HashMap<String, String>();
                putMessage.put("name", curUser);
                putMessage.put("message", msg);
                baseFire.child(toUser).child("messages").push().setValue(putMessage);
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }
}


