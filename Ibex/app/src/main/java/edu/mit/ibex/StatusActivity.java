package edu.mit.ibex;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class StatusActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    ImageButton mapsButton, friendsButton;
    EditText editStatus;
    Switch available;
    String curUser, toUser, targetFriend;
    boolean curAvailable;
    Firebase baseFire, myFire;
    HashMap<String, Object> data;
    TextView myStatus;

    String friendStatus;
    String fStatus;
    String location;
    String tempStatus;
    List<String> friendsInfo, friendsName;
    List<List<String>> friendLocation;
    ListView theListView;
    Set<String> userList; //List of ALL users in database. Used for checking if friend is valid
    LatLng center;

    // Google Api Client
    private GoogleApiClient mGoogleApiClient;
    //Request code to use when launching the resolutiona activity
    private static final int REQUEST_RESOLVE_ERROR=1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    //Bool to track whether the app is already resolving an error
    private boolean mResolvingError= false;
    
    private Location mCurrentLocation;

    //Keeping track of whether errors are being resolved

    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    
    private static final String REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
    private static final String LOCATION_KEY = "location_key";
    
    private LocationRequest mLocationRequest;
    
    private boolean mRequestingLocationUpdates;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateValuesFromBundle(savedInstanceState);

        Intent intent = getIntent();
        curUser = intent.getExtras().getString("curUser");
        curAvailable = intent.getExtras().getBoolean("curAvailable");

        setContentView(R.layout.activity_status);
        Firebase.setAndroidContext(this);
        baseFire = new Firebase("https://hangmonkey.firebaseio.com/");
        myFire = baseFire.child(curUser);
        myStatus = (TextView) findViewById(R.id.MyStatus);
        myStatus.setText(curUser+":");
        buildGoogleApiClient();
        mapsButton = (ImageButton) findViewById(R.id.mapsButton);
        friendsButton = (ImageButton) findViewById(R.id.friendsButton);
        editStatus = (EditText) findViewById(R.id.editStatus);
        available = (Switch) findViewById(R.id.available);
        if (curAvailable){
            available.setChecked(true);
        }

        //Set up Google API Client
        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        //End of setting up Google API Client

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
                if (msgPack.get("seen").equals("false")) {
                    Intent intent = new Intent(StatusActivity.this, Notifications.class);
                    intent.putExtra("user", curUser);
                    intent.putExtra("messageKey", snapshot.getKey());
//                    Intent msgIntent = new Intent(StatusActivity.this, LogInActivity.class);
//                    PendingIntent pIntent = PendingIntent.getActivity(StatusActivity.this, 0, msgIntent, 0);
                    PendingIntent pendIntent = PendingIntent.getActivity(StatusActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT  );

                    Notification notif = new Notification.Builder(StatusActivity.this)
                            .setContentTitle("New message from " + msgPack.get("name"))
                            .setContentIntent(pendIntent)
                            .setContentText(msgPack.get("message"))
                            .setAutoCancel(true)
                            .setSmallIcon(R.mipmap.ic_action_mail)
                            .build();
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(0, notif);
//                    myFire.child("messages").child(snapshot.getKey()).child("seen").setValue("true");
                    Log.d("key", snapshot.getKey());
                    Log.d("posting something", "done!");
                }
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

        myFire.child("requests").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, String> msgPack = (Map<String, String>) snapshot.getValue();
                if (msgPack.get("status").equals("neutral")) {
                    Intent intent1 = new Intent(StatusActivity.this, Friending.class)
                            .setAction("foo")
                            .putExtra("toUser", msgPack.get("name"))
                            .putExtra("accept", "true")
                            .putExtra("curUser", curUser)
                            .putExtra("requestKey", snapshot.getKey());
                    Intent intent2 = new Intent(StatusActivity.this, Friending.class)
                            .setAction("foo2")
                            .putExtra("toUser", msgPack.get("name"))
                            .putExtra("accept", "false")
                            .putExtra("curUser", curUser)
                            .putExtra("requestKey", snapshot.getKey());
//                    Intent msgIntent = new Intent(StatusActivity.this, LogInActivity.class);
//                    PendingIntent pIntent = PendingIntent.getActivity(StatusActivity.this, 0, msgIntent, 0);
                    //PendingIntent addIntent = PendingIntent.getActivity(StatusActivity.this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                    //PendingIntent denyIntent = PendingIntent.getActivity(StatusActivity.this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification notif = new Notification.Builder(StatusActivity.this)
                            .setContentTitle(msgPack.get("name") + " has sent you a friend request!")
                            .addAction(R.mipmap.ic_action_mail, "Accept Request", PendingIntent.getActivity(StatusActivity.this, 0, intent1, PendingIntent.FLAG_ONE_SHOT))
                            .addAction(R.mipmap.ic_action_mail, "Deny Request", PendingIntent.getActivity(StatusActivity.this, 0, intent2, PendingIntent.FLAG_ONE_SHOT))
                            .setAutoCancel(true)
                            .setSmallIcon(R.mipmap.ic_action_mail)
                            .build();
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(0, notif);
//                    myFire.child("messages").child(snapshot.getKey()).child("seen").setValue("true");
                    Log.d("key", snapshot.getKey());
                    Log.d("posting something", "done!");
                }
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

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if(savedInstanceState!=null){
            //Update the value of mRequestingLocationUpdates from the Bundle
            if(savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES)){
                mRequestingLocationUpdates=savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES);
            }
            //Update the value of mRequestingLocationUpdates from the Bundle
            if(savedInstanceState.keySet().contains(LOCATION_KEY)){
                mCurrentLocation=savedInstanceState.getParcelable(LOCATION_KEY);
            }
        }
    }

    //Build google Api Client
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
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
                Intent newIntent = new Intent(this, LogInActivity.class);
                Log.d("delete", "Back to Log In");
                startActivity(newIntent);
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
            myFire.child("available").setValue(true);
            showStatusList();
        }else{
            myFire.child("available").setValue(false);
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
                if (available.isChecked()) {
                    data = (HashMap<String, Object>) snapshot.getValue();
                    Log.d("data - (raw)", data.toString());
                    Log.d("data - Users", data.keySet().toString());
                    userList = data.keySet();
                    Log.d("showStatusList", "calling showFriendInfo");
                    showFriendInfo(curUser, data);
                }
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
                boolean friendAvailable = (boolean) friendInfo.get("available");
                if (friendAvailable) {
                    Log.d("Add Friend", friendName + " added to list");
                    friendsInfo.add(friendName + ": " + friendStatus);
                }

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
                Log.d("friendClick", selectedFromList);
                final String selectedFriend = selectedFromList.split(":")[0];
                final String selectedFriendStatus = selectedFromList.split(": ")[1];
                Log.d("friendClick", selectedFriend);
                Log.d("friendClick", selectedFriendStatus);
                clickFriend(selectedFriend,selectedFriendStatus );
            }
        });
    }

    /*
    This decides what happens when we click a friend in the Friends List
    Right now, clicking a friend opens notification of their status and long/lat coordinates.
    We can make it so when a friend is clicked we open maps tab up to their location
     */
    private void clickFriend(final String selectedFriend, final String selectedFriendStatus) {
        Log.d("clickFriend", selectedFriend + " clicked!");
        targetFriend = selectedFriend;
        //AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.dialog_title_style);
        AlertDialog.Builder alert = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        View clickLayout = getLayoutInflater().inflate(R.layout.layout_friend_click, null);
        alert.setView(clickLayout);
        alert.setTitle(selectedFriend);
        TextView friendStatus = (TextView)clickLayout.findViewById(R.id.friendStatus);
        friendStatus.setText(selectedFriendStatus);

        alert.setNeutralButton("Map", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                startMap(selectedFriend);
            }
        });
        alert.setNegativeButton("Message", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                messageFriend(selectedFriend);
            }
        });
        alert.setPositiveButton("Cancel", null);
        alert.show();
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
//    private void statusPop(String user, String status, String location) {
//        toUser = user;
//        AlertDialog.Builder alert = new AlertDialog.Builder(this);
//
//        alert.setMessage(status + "\n" + location);
//        alert.setTitle(toUser);
//        alert.setPositiveButton("Message", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                message(toUser);
//            }
//        });
//        alert.setNegativeButton("OK", null);
//        alert.show();
//    }

    /*
    Adds a friend to the database under current user's friends list
    Checks if friend exists in database and if friend is already in user's friends.
     */
    public void addFriend(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Add a Friend");

        View addFriendLayout = getLayoutInflater().inflate(R.layout.layout_add_friend, null);
        alert.setView(addFriendLayout);
        final EditText friendInput = (EditText) addFriendLayout.findViewById(R.id.friendInput);


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
                                    putName.put("name", curUser);
                                    putName.put("status", "neutral");

                                    //myFire.child("friends").push().setValue(putName);
                                    baseFire.child(friendName).child("requests").push().setValue(putName);

                                    Log.d("Add Friend", friendName+" added");

                                    Toast.makeText(getApplicationContext(),
                                            "Friend request sent to " + friendName + "!",
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

    public void deleteFriend(View v) {
        myFire.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List friends = new ArrayList();
                HashMap<String, Object> friendList = (HashMap<String, Object>) snapshot.getValue();
                System.out.println("friendsList");
                System.out.println(friendList);
                String timeMarker = null;

                if (friendList != null) {
                    Log.d("deleteFriend", "friendList not null");
                    for (Object timestamp : friendList.keySet()) {
                        HashMap name = (HashMap) friendList.get(timestamp);
                        String friendName = name.get(name.keySet().toArray()[0]).toString();
                        if (friendName.equals(targetFriend)) {
                            timeMarker = timestamp.toString();
                        }
                    }
                }
                if (timeMarker != null) {
                    Log.d("deleteFriend", "calling delete");
                    delete(timeMarker);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public void delete(String timestamp){
        Log.d("delete", targetFriend+" deleted");
        myFire.child("friends").child(timestamp).removeValue();
        Toast.makeText(getApplicationContext(),
                targetFriend + " deleted",
                Toast.LENGTH_LONG).show();

        Intent newIntent = new Intent(this, LogInActivity.class);
        Log.d("delete", "Back to home");
        startActivity(newIntent);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setAvailable(View v){
        if(!available.isChecked()){

            available.setChecked(true);
            availableClick(v);
        }
    }

    /**
     * Setup a map with current friends of the user and centers on its last saved location
     */

    public void startMap() {
        final List<String> ami = new ArrayList<String>();
        final Intent i = new Intent(this, MapsActivity.class);
        if (curUser != null) {
            i.putExtra("curUser", curUser);
        }
        myFire.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String, String>> fd = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                if(fd!=null){
                Collection<HashMap<String, String>> friendsNames = fd.values();
                for (HashMap<String, String> amiDic : friendsNames) {
                    String amigo = amiDic.get("name");
                    if (!ami.contains(amigo)) {
                        ami.add(amigo);
                    }
                }

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
                    if (lat != null && lon != null && available) {
                        friendInfo.add(name);
                        friendInfo.add(lat.toString());
                        friendInfo.add(lon.toString());
                        friendInfo.add((String) dataForFriend.get("status"));
                    }
                    allFriendsInfo.add((ArrayList<String>) friendInfo);

                Log.d("All f info", allFriendsInfo.toString());
                i.putExtra("friends", (java.io.Serializable) allFriendsInfo);}}
                startActivity(i);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    /**
     * Triggers a map activity intent and display all the currently available friends of the current logged in user
     * Also centers the map on the friend mentionned
     * @param friend
     */
    public void startMap(final String friend){
        final List<String> ami = new ArrayList<String>();
        final Intent i = new Intent(this, MapsActivity.class);
        if (curUser != null) {
            i.putExtra("curUser", curUser);
        }
        myFire.child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            /*
            Take a snapshot of current data and displays these friends on the map
             */
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("start map", dataSnapshot.toString());
                if (dataSnapshot.getValue()!=null){
                    HashMap<String, HashMap<String, String>> fd = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                    Log.d("start map", dataSnapshot.getValue().toString());
                    Collection<HashMap<String, String>> friendsNames = fd.values();
                    for (HashMap<String, String> amiDic : friendsNames) {
                        String amigo = amiDic.get("name");
                        if (!ami.contains(amigo)) {
                            ami.add(amigo);
                        }
                    }
                }
                Log.d("HHHHH", ami.toString());

                List<ArrayList<String>> allFriendsInfo = new ArrayList<ArrayList<String>>();
                HashMap<String, Object> friends = (HashMap<String, Object>) data.get(curUser);
                if(friends !=null){
                Log.d("DataforFriends ", friends.toString());
                for (String name : ami) {
                    List<String> friendInfo = new ArrayList<String>();
                    HashMap<String, Object> dataForFriend = (HashMap<String, Object>) data.get(name);
                    Log.d("Name", name);
                    Log.d("Data4Name", dataForFriend.toString());
                    Object lat = dataForFriend.get("lat");
                    Object lon = dataForFriend.get("long");
                    boolean available = (boolean) dataForFriend.get("available");
                    if(friend.equals(name)){
                        center = new LatLng((double)lat,(double)lon);
                        fStatus = dataForFriend.get("status").toString();
                    }
                    if (lat != null && lon != null && available == true) {
                        friendInfo.add(name);
                        friendInfo.add(lat.toString());
                        friendInfo.add(lon.toString());
                        friendInfo.add((String) dataForFriend.get("status"));
                    }
                    allFriendsInfo.add((ArrayList<String>) friendInfo);
                }}
                Log.d("All f info", allFriendsInfo.toString());
                i.putExtra("friends", (java.io.Serializable) allFriendsInfo);
                i.putExtra("center",center);
                i.putExtra("friend",friend);
                Log.d("SSSSSS",fStatus);
                i.putExtra("status",fStatus);
                startActivity(i);

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void mapsClick(View v) {
        startMap();
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
        // Getting Current Location
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
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
    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    // Update the location of the current user on the Firebase database
    public void UpdateNewLocation(Location location){
        mCurrentLocation=location;
        Log.d("Location Updated",location.toString());
        if(curAvailable){
        double latitude = location.getLatitude();
        // Getting longitude of the current location
        double longitude = location.getLongitude();
        //String latString = Double.toString(latitude);
        //String longString = Double.toString(longitude);
        myFire.child("lat").setValue(latitude);
        myFire.child("long").setValue(longitude);}

    }


    //Sends a message to a specified user
    public void messageFriend(String user) {
        toUser = user;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        View messageLayout = getLayoutInflater().inflate(R.layout.layout_message, null);
        alert.setView(messageLayout);
        alert.setTitle("Message to " + toUser);
        final EditText messageContent = (EditText) messageLayout.findViewById(R.id.messageContent);

        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String msg = messageContent.getText().toString();
                if(msg.equals("")){
                    Toast.makeText(getApplicationContext(),
                            "What are we sending to "+toUser+"?",
                            Toast.LENGTH_LONG).show();
                } else if (userList.contains(toUser)) {
                    if (toUser.equals(curUser)){
                        HashMap<String, String> putMessage = new HashMap<String, String>();
                        putMessage.put("name", curUser);
                        putMessage.put("message", msg);
                        putMessage.put("seen", "false");
                        baseFire.child(toUser).child("messages").push().setValue(putMessage);
                        Toast.makeText(getApplicationContext(),
                                "Don't worry, I talk to myself too.",
                                Toast.LENGTH_LONG).show();
                    }else {
                        HashMap<String, String> putMessage = new HashMap<String,String>();
                        putMessage.put("name", curUser);
                        putMessage.put("message", msg);
                        putMessage.put("seen", "false");
                        baseFire.child(toUser).child("messages").push().setValue(putMessage);
                        Toast.makeText(getApplicationContext(),
                                toUser+" messaged!",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    // Additional Google Location API Methods
    @Override
    protected void onPause(){
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates(){
        if(mGoogleApiClient.isConnected()){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        UpdateNewLocation(mCurrentLocation);
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((StatusActivity)getActivity()).onDialogDismissed();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        if(!mResolvingError){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
       
        Location currentLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(currentLoc==null){
            //Blank
        }
        else{
            UpdateNewLocation(currentLoc);
            mCurrentLocation=currentLoc;
            Log.d("Location obtained from Google Maps",currentLoc.toString());
        }
        startLocationUpdates();

    }

    private void startLocationUpdates() {
        Log.d("status","LocationUpdatesStarted");
        mRequestingLocationUpdates=true;
        createLocationRequest();
        if(mGoogleApiClient.isConnected()){
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);}
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        outState.putBoolean(REQUESTING_LOCATION_UPDATES,mRequestingLocationUpdates);
        outState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(outState);
    }
   
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }
    
    @Override
    public void onResume(){
        super.onResume();
        if(mGoogleApiClient.isConnected()&& !mRequestingLocationUpdates){
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}


