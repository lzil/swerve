package edu.mit.ibex;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.firebase.client.Firebase;


public class StatusActivity extends ActionBarActivity {
    Button mapsButton, friendsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Firebase.setAndroidContext(this);
        Firebase myFirebaseRef = new Firebase("https://hangmonkey.firebaseio.com/");
        mapsButton = (Button) findViewById(R.id.mapsButton);
        friendsButton = (Button) findViewById(R.id.friendsButton);
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
        Intent i = new Intent(this, FriendsActivity.class);
        //startActivity(i);
    }
}
