package edu.mit.ibex;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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
import java.util.Map;


public class StatusActivity extends ActionBarActivity {
    private String LOG_MESSAGE = "HangMonkey";

    ImageButton mapsButton, friendsButton;
    EditText editStatus;
    Firebase myFirebase;
    Switch available;
    String username;
    Map<String, Object> data;
    TextView myStatus;

    String friendStatus;
    String location;
    String tempStatus;
    List<String> friendsInfo;
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
        theListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> resultsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,friendsInfo);
        theListView.setAdapter(resultsAdapter);

        myFirebase.child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
//                System.out.println(snapshot.getValue());

                data = (Map<String, Object>)snapshot.getValue();

                Log.d("Data : ", data.toString());
                System.out.println(snapshot.getKey());
                System.out.println(data.get("status").toString());
                System.out.println(data.get("friends").toString());
                showFriendInfo(snapshot.getKey(), data.get("status").toString(), data.get("friends").toString());
            }
            @Override public void onCancelled(FirebaseError error) { }
        });
//        Log.d("Data : ",data.toString());
//        showFriendInfo(data);
    }

    private void showFriendInfo(String user, String status, String friends) {
        myStatus.setTextSize(20);
        myStatus.setTypeface(null, Typeface.ITALIC);
        //BOLD_ITALIC
        myStatus.setText("\""+status+"\"");

        String[] myFriends = friends.split("\\s+");

        for (String friend : myFriends){
            myFirebase.child(friend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    data = (Map<String, Object>)snapshot.getValue();
                    Log.d("Friend Data : ", data.toString());
                    if(!(data.get("status").equals(null))){
                        tempStatus = data.get("status").toString();
                        System.out.println("inside"+tempStatus);
                        friendsInfo.add(snapshot.getKey() + ": " + data.get("status"));

                        System.out.println("FriendsInfo "+friendsInfo);
                        addFriendList(friendsInfo);
                    }
                }
                @Override public void onCancelled(FirebaseError error) { }

            });
//            System.out.println(tempStatus);
//            friendsInfo.add(friend + ": " + tempStatus);
        }
//        System.out.println("FriendsInfo "+friendsInfo);
//        addFriendList(friendsInfo);

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long myLong) {
                String selectedFromList = (String) (theListView.getItemAtPosition(myItemInt));
//                do whatever here
                final String selectedFriend = selectedFromList.split(":")[0];
                myFirebase.child(selectedFriend).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
//                System.out.println(snapshot.getValue());
                        data = (Map<String, Object>) snapshot.getValue();
                        System.out.println(data);

                        friendStatus = data.get("status").toString();
                        location = data.get("lat").toString() + "," + data.get("long").toString();

                        statusPop(selectedFriend, friendStatus, location);
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                    }
                });
            }
        });
    }

    private void addFriendList(List<String> friendsInfo) {
        ArrayAdapter<String> resultsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,friendsInfo);
        theListView.setAdapter(resultsAdapter);
    }

    private void statusPop(String user, String status, String location) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(status + "\n"+location);
        dlgAlert.setTitle(user);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
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
                        Map<String, String> putName = new HashMap<String, String>();
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
        i.putExtra("username",username);}
        startActivity(i);
    }

    public void postStatus(View v) {
        friendsInfo = new ArrayList<String>();
        boolean on = available.isChecked();
        myFirebase.child(username + "/status").setValue(editStatus.getText().toString());
        myFirebase.child(username + "/available").setValue(on);
    }
}
