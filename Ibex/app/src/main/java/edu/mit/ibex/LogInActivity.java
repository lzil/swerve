package edu.mit.ibex;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


public class LogInActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Firebase.setAndroidContext(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

    public void logIn(View view){
        TextView logText = (TextView) findViewById(R.id.invalidText);
        logText.setTypeface(null, Typeface.ITALIC);
        logText.setText("Confirming user login...");

        EditText username =  (EditText) findViewById(R.id.usernameEditText);
        EditText password =  (EditText) findViewById(R.id.passwordEditText);

        String usr = username.getText().toString();
        String psw = password.getText().toString();
        String psw2 = new String();
        //Passes usr and psw to some server
        //if pass:
        Firebase ref = new Firebase("https://hangmonkey.firebaseio.com/" + usr + "/pass");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //psw2 = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
        //Log.d("pass", psw2[0]);
        //Log.d("passs", psw);
        //if (psw.equals(psw2)) {
        Log.d("login", "preintent");
        Intent intent = new Intent(this, StatusActivity.class);
        Log.d("login", "postintent");
        intent.putExtra("username", usr);
        Log.d("login", "post intent putextra");
        startActivity(intent);
        Log.d("login", "post start status activity");
        //}
        //else {
        //    logText.setText("Fail");
        //}
        //if fail: Failure message. Same screen. Retry.
        //logText.setTextColor(Color.RED);
        //logText.setText("Wrong username and password. Please try again.");
    }

    public void reg(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
