package edu.mit.ibex;

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
import android.widget.ListView;
import android.widget.Switch;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class StatusActivity extends ActionBarActivity {
    private String LOG_MESSAGE = "HangMonkey";

    Button mapsButton, friendsButton;
    EditText editStatus;
    Firebase myFirebase;
    Switch available;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        username = intent.getExtras().getString("username");

        setContentView(R.layout.activity_status);
        Firebase.setAndroidContext(this);
        mapsButton = (Button) findViewById(R.id.mapsButton);
        friendsButton = (Button) findViewById(R.id.friendsButton);
        editStatus = (EditText) findViewById(R.id.editStatus);
        available = (Switch) findViewById(R.id.available);
        myFirebase = new Firebase("https://hangmonkey.firebaseio.com/");
        //Need to actually pull data
//        JSONArray info = new JSONArray();
//        showFriendInfo(info);

        myFirebase.child("liang").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
            }
            @Override public void onCancelled(FirebaseError error) { }
        });
    }

    private void showFriendInfo(JSONArray info) {


        final ListView theListView = (ListView) findViewById(R.id.listView);

        List<String> friendsInfo = new ArrayList<String>();

        ArrayAdapter<String> resultsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,friendsInfo);

        String friend;
        String status;

        if (info != null) {
            for (int i = 0; i < info.length(); i++) {
                try {
                    JSONObject foodInfo = (JSONObject) info.get(i);
                    JSONObject tempJsonParse = foodInfo.getJSONObject("fields");
                    friend = tempJsonParse.getString("friend");
                    status = tempJsonParse.getString("status");
                    friendsInfo.add(friend + " : " + status);

                } catch (JSONException e) {
                    Log.e(LOG_MESSAGE, e.getMessage());
                    e.printStackTrace();
                }
            }  // end the for loop
            theListView.setAdapter(resultsAdapter);
            theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long myLong) {
                    String selectedFromList = (String) (theListView.getItemAtPosition(myItemInt));
                    //do whatever here
                    String newString = selectedFromList.replaceAll(" ", "&");
//                    getWebResultString(newString);
                }
            });
        }
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
        startActivity(i);
    }

    public void friendsClick(View v) {
        Intent i = new Intent(this, StatusActivity.class);
        //startActivity(i);
    }

    public void postStatus(View v) {
        System.out.println("wow");
        boolean on = available.isChecked();
        myFirebase.child("liang/status").setValue(editStatus.getText().toString());
        myFirebase.child("liang/available").setValue(on);
    }


}
