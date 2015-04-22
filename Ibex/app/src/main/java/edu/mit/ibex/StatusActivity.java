package edu.mit.ibex;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.List;
import java.util.Map;


public class StatusActivity extends ActionBarActivity {
    private String LOG_MESSAGE = "HangMonkey";

    ImageButton mapsButton, friendsButton;
    EditText editStatus, friendInput;
    Firebase myFirebase;
    Switch available;
    String username;
    Map<String, Object> data;

    String friendStatus;
    String location;
    String tempStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        username = intent.getExtras().getString("username");

        setContentView(R.layout.activity_status);
        Firebase.setAndroidContext(this);
        mapsButton = (ImageButton) findViewById(R.id.mapsButton);
        friendsButton = (ImageButton) findViewById(R.id.friendsButton);
        editStatus = (EditText) findViewById(R.id.editStatus);
        friendInput = (EditText) findViewById(R.id.friendInput);
        available = (Switch) findViewById(R.id.available);
        myFirebase = new Firebase("https://hangmonkey.firebaseio.com/");
        //Need to actually pull data
//        JSONArray info = new JSONArray();
//        showFriendInfo(info);

//        String data = "";
        myFirebase.child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
//                System.out.println(snapshot.getValue());
                data = (Map<String, Object>)snapshot.getValue();
                Log.d("Data : ", data.toString());
             //   showFriendInfo(snapshot.getKey(), data.get("status").toString(), data.get("friends").toString());
            }
            @Override public void onCancelled(FirebaseError error) { }
        });
//        Log.d("Data : ",data.toString());
//        showFriendInfo(data);
    }

    private void showFriendInfo(String user, String status, String friends) {


        final ListView theListView = (ListView) findViewById(R.id.listView);

        List<String> friendsInfo = new ArrayList<String>();

        ArrayAdapter<String> resultsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,friendsInfo);

        final TextView myStatus = (TextView) findViewById(R.id.MyStatus);
        myStatus.setText(user + ": " + status);

        String[] myFriends = friends.split("\\s+");

        for (String friend : myFriends){
            myFirebase.child(friend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
//                System.out.println(snapshot.getValue());
                    data = (Map<String, Object>)snapshot.getValue();
                    Log.d("Friend Data : ", data.toString());
                    if(!(data.get("status").equals(null))){
                        tempStatus = data.get("status").toString();
                        System.out.println("inside"+tempStatus);
                    }
                }
                @Override public void onCancelled(FirebaseError error) { }

            });
            System.out.println(tempStatus);
//            friendsInfo.add(friend + ": " + tempStatus);
            friendsInfo.add(friend);
        }


        theListView.setAdapter(resultsAdapter);

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

                        popup(selectedFriend, friendStatus, location);
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                    }
                });
            }
        });
    }

    private void popup(String user, String status, String location) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(status + "\n"+location);
        dlgAlert.setTitle(user);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
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

    public void friendsClick(View v) {
        //Intent i = new Intent(this, StatusActivity.class);
        //startActivity(i);
    }

    public void postStatus(View v) {
        System.out.println("wow");
        boolean on = available.isChecked();
        myFirebase.child(username + "/status").setValue(editStatus.getText().toString());
        myFirebase.child(username + "/available").setValue(on);
    }


    public void addFriend(View v) {
        myFirebase.child(username + "/friends");
    }
}
