package edu.mit.ibex;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Liang on 5/9/15.
 */
public class Friending extends ActionBarActivity {
    Firebase baseFire, myFire, toFire;

    String curUser, toUser, requestKey;
    Boolean acceptFriend;
    ArrayList friendsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Log.d("Requests", "processed");
        curUser = intent.getExtras().getString("curUser");
        toUser = intent.getExtras().getString("toUser");
        requestKey = intent.getExtras().getString("requestKey");
        baseFire = new Firebase("https://hangmonkey.firebaseio.com");
        Log.d("curUserFriending", curUser);
        toFire = baseFire.child(toUser);
        myFire = baseFire.child(curUser);
        Log.d("hmm", intent.getExtras().getString("accept"));
        acceptFriend = Boolean.valueOf(intent.getExtras().getString("accept"));
        Log.d("truefalse", Boolean.toString(acceptFriend));
        if (acceptFriend) {
            myFire.child("requests").child(requestKey).child("status").setValue("accepted");
            friendsInfo = new ArrayList<String>();
            HashMap<String, String> putName = new HashMap<String, String>();
            putName.put("name", curUser);
            toFire.child("friends").push().setValue(putName);
            Toast.makeText(getApplicationContext(),
                    toUser + " accepted!",
                    Toast.LENGTH_LONG).show();
        }
        else {
            myFire.child("requests").child(requestKey).child("status").setValue("denied");
            Toast.makeText(getApplicationContext(),
                    toUser + " denied!",
                    Toast.LENGTH_LONG).show();
        }

        Log.d("Friending", "Message successfully read");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        finish();
    }
}