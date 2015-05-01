package edu.mit.ibex;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


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
    Set<String> userList; //List of ALL users in database. Used for checking if friend is valid

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

        friendsInfo = new ArrayList<String>();
        theListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> resultsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,friendsInfo);
        theListView.setAdapter(resultsAdapter);

        /**
         * Makes only one call to Firebase
         */
        Log.d("onCreate", "call showStatusList");
        showStatusList();


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

    private void showStatusList(){
        myFirebase = new Firebase("https://hangmonkey.firebaseio.com/");
        Log.d("made link to Firebase","good");
        myFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                data = (HashMap<String, Object>)snapshot.getValue();
                Log.d("data - (raw)", data.toString());
                Log.d("data - Users", data.keySet().toString());
                userList = data.keySet();
                if (available.isChecked()){
                    Log.d("showStatusList", "calling showFriendInfo");
                    showFriendInfo(username, data);
                }
            }
            @Override public void onCancelled(FirebaseError error) { }
        });
    }

    /*
     Makes only one call to Firebase
     */
    private void showFriendInfo(String currentUser, HashMap data) {
        Log.d("inside showFriendInfo", "inside");
        HashMap currentUserInfo = (HashMap) data.get(currentUser);
//        System.out.println(currentUserInfo);
        String status = currentUserInfo.get("status").toString();
//        System.out.println(status);

        myStatus.setTextSize(20);
        myStatus.setTypeface(null, Typeface.ITALIC);
        myStatus.setTextColor(Color.rgb(3, 171, 244));
        //BOLD_ITALIC
        myStatus.setText(status);

        HashMap friendsDict = (HashMap) currentUserInfo.get("friends");
        System.out.println(friendsDict);
        if (friendsDict != null) {
            friendsInfo = new ArrayList<String>();
            Log.d("data", "friendsDict not null");
            for (Object timestamp : friendsDict.keySet()) {
                HashMap name = (HashMap) friendsDict.get(timestamp);
//                System.out.println(name.keySet().toArray()[0]);
                String friendName = name.get(name.keySet().toArray()[0]).toString();
//                System.out.println(friendName);

                HashMap friendInfo = (HashMap) data.get(friendName);
                String friendStatus = friendInfo.get("status").toString();
                Log.d("Add Friend", friendName+ " added to list");
                friendsInfo.add(friendName + ": " + friendStatus);
               // friendsName.add(friendName);

              //  Double lat = Double.parseDouble(friendsDict.get(friendName).get(lat));
//                System.out.println("FriendsInfo " + friendsInfo);
                addFriendList(friendsInfo);

            }
        }else{
            Log.d("Error","No Friends");
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
        myStatus.setTextColor(Color.rgb(3, 171, 244));
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

    /*
    This decides what happens when we click a friend in the Friends List
    Right now, clicking a friend opens notification of their status and long/lat coordinates.
    We can make it so when a friend is clicked we open maps tab up to their location
     */
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

    /*
    Not sure the point of this.....it literally just calls another function. Who did this?
    Liang: I did this because various variable access trouble issues. pls keep it i need it for it to work
     */
    public void addFriend(View view) {
        friendPop(username);
    }

    /*
    Adds a friend to the database under current user's friends list
    Checks if friend exists in database and if friend is already in user's friends.
     */
    private void friendPop(String user) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext= new EditText(StatusActivity.this);
        //only allows user to input max 24 chars (limit of username length)
        edittext.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(24) });
        alert.setMessage("Add a Friend");
        alert.setView(edittext);

        alert.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final Firebase frands = new Firebase("https://hangmonkey.firebaseio.com/" + username + "/friends");
                final String friendName = edittext.getText().toString();
                frands.addListenerForSingleValueEvent(new ValueEventListener() {
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
//                                System.out.println(name.keySet().toArray()[0]);
                                String friendName = name.get(name.keySet().toArray()[0]).toString();
//                                System.out.println(friendName);
                                friends.add(friendName);                            }
                        }else{
                            //No friends yet
                            //Check if friend is in database
//                            if(userList.contains(friendName)){
//                                //Add friend
//                                friendsInfo = new ArrayList<String>();
//                                HashMap<String, String> putName = new HashMap<String, String>();
//                                putName.put("name", friendName);
//                                frands.push().setValue(putName);
//                                Log.d("Add Friend", friendName+" added");
//                                Toast.makeText(getApplicationContext(),
//                                        friendName+" added",
//                                        Toast.LENGTH_LONG).show();
//                            }
                        }

                        if (friends.contains(friendName)) {
                            //Check if friend is already in friends list
                            Log.d("Add Friend", friendName+" already added");
                            Toast.makeText(getApplicationContext(),
                                    friendName+" already added",
                                    Toast.LENGTH_LONG).show();
                        }else{
                            //Check if friend exists in database
                            if(userList.contains(friendName)){
                                if (friendName.equals(username)){
                                    Log.d("Add Friend", "Tried to add self. Motivational message sent");
                                    Toast.makeText(getApplicationContext(),
                                            "Are you lonely? You are always you're own friend! :D",
                                            Toast.LENGTH_LONG).show();
                                }else{
                                    //Add friend
                                    friendsInfo = new ArrayList<String>();
                                    HashMap<String, String> putName = new HashMap<String, String>();
                                    putName.put("name", friendName);
                                    frands.push().setValue(putName);
                                    Log.d("Add Friend", friendName+"added");
                                    Toast.makeText(getApplicationContext(),
                                            friendName+" added",
                                            Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Log.d("Add Friend", friendName+" does not exist");
                                Toast.makeText(getApplicationContext(),
                                        friendName+" does not use Swerve",
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void edittingStatus(View v){
        if(!available.isChecked()){
            available.setChecked(true);
            availableClick(v);
        }
    }

    public void mapsClick(View v) {
        final List<String> ami = new ArrayList<String>();
        final Intent i = new Intent(this, MapsActivity.class);
        if(username!=null){
            i.putExtra("username",username);
        }
        Firebase friendsForMap = new Firebase("https://hangmonkey.firebaseio.com/" + username + "/friends");
        friendsForMap.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String,HashMap<String,String>> fd = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                Collection<HashMap<String, String>> friendsNames =  fd.values();
                for(HashMap<String,String> amiDic : friendsNames){
                    String amigo = amiDic.get("name");
                    if(!ami.contains(amigo)){
                        ami.add(amigo);
                    }
                }
                Log.d("HHHHH",ami.toString());

                List<ArrayList<String>> allFriendsInfo = new ArrayList<ArrayList<String>>();
                HashMap<String,Object> friends = (HashMap<String, Object>) data.get(username);

                Log.d("DataforFriends ", friends.toString());
                for(String name : ami){
                    List<String> friendInfo = new ArrayList<String>();
                    HashMap<String,Object> dataForFriend = (HashMap<String, Object>) data.get(name);
                    Log.d("Name",name);
                    Log.d("Data4Name",dataForFriend.toString());
                    Object lat =  dataForFriend.get("lat");
                    Object lon =  dataForFriend.get("long");
                    boolean available = (boolean) dataForFriend.get("available");
                    if(lat!=null && lon!=null && available==true){
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
        if(!available.isChecked()) {
            available.setChecked(true);
            availableClick(v);
        }
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

        if (location != null) {
            // Getting latitude of the current location
            double latitude = location.getLatitude();
            // Getting longitude of the current location
            double longitude = location.getLongitude();
            String latString = Double.toString(latitude);
            String longString = Double.toString(longitude);
            myFirebase.child(username + "/lat").setValue(latitude);
            myFirebase.child(username + "/long").setValue(longitude);
        }
        Notification noti = new Notification.Builder(this)
                .setContentTitle("New mail from " + username.toString())
                .setContentText("something message")
                .setSmallIcon(R.drawable.maps)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, noti);
    }

    public void message(String user) {
        final Firebase notifs = new Firebase("https://hangmonkey.firebaseio.com/" + user + "/notifications");
        Log.d("testing", user);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage("Message to " + user);
        alert.setTitle(user);
        final EditText edittext= new EditText(StatusActivity.this);
        alert.setView(edittext);

        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String msg = edittext.getText().toString();
                Log.d("message here", msg);
                HashMap<String, String> putMessage = new HashMap<String, String>();
                putMessage.put(username, msg);
                notifs.push().setValue(putMessage);
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }
}


