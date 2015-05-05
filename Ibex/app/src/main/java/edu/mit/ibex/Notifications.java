package edu.mit.ibex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.Firebase;

/**
 * Created by Kevin on 5/5/2015.
 */
public class Notifications extends ActionBarActivity{

    Firebase baseFire, myFire;

    String curUser;
    String messageKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Notifications", "Class called");

        Intent intent = getIntent();
        Log.d("Notifications", intent.toString());
        Log.d("Notifications", intent.getExtras().toString());
        curUser = intent.getExtras().getString("user");
        Log.d("Notifications", "curUser: "+curUser);
        messageKey = intent.getExtras().getString("messageKey");
        baseFire = new Firebase("https://hangmonkey.firebaseio.com/");
        myFire = baseFire.child(curUser);
        myFire.child("messages").child(messageKey).child("seen").setValue("true");

        Toast.makeText(getApplicationContext(),
                "Message Read!",
                Toast.LENGTH_LONG).show();

        Log.d("Notifications", "Message successfully read");
        Intent newIntent = new Intent(this, LogInActivity.class);
        Log.d("Notifications", "Back to Log In");
        startActivity(newIntent);
    }

}
