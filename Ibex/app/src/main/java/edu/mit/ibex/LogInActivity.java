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
    public static String psw2;
    public static boolean isPwdEntered = false;
    public static String usr, psw;
    TextView logText;
    EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Firebase.setAndroidContext(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        logText = (TextView) findViewById(R.id.invalidText);
        username =  (EditText) findViewById(R.id.usernameEditText);
        password =  (EditText) findViewById(R.id.passwordEditText);
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

    public void logIn(View view) throws InterruptedException {
        logText.setTypeface(null, Typeface.ITALIC);
        logText.setTextColor(Color.DKGRAY);
        logText.setText("Confirming user login...");

        usr = username.getText().toString();
        psw = password.getText().toString();
        Firebase ref = new Firebase("https://hangmonkey.firebaseio.com/" + usr + "/pass");
        psw2 = "test";
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                LogInActivity.psw2 = snapshot.getValue().toString();
                login(LogInActivity.psw2);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

    }

    public void login(String psw2) {
        if (psw2.equals(psw)) {
            Intent intent = new Intent(this, StatusActivity.class);
            intent.putExtra("username", usr);
            startActivity(intent);
        }
        else {
            logText.setVisibility(View.VISIBLE);
            logText.setTextColor(Color.RED);
            logText.setText("Wrong username and password. Please try again.");
        }
    }

    public void signUp(View view){
        //logText.setTypeface(null, Typeface.ITALIC);
        //logText.setTextColor(Color.GRAY);
        //logText.setText("Signing up...");
        String usr = username.getText().toString();
        String psw = password.getText().toString();

        //Passes usr and psw to some server
        //if pass:
        Firebase myFirebase = new Firebase("https://hangmonkey.firebaseio.com/" + usr);
        myFirebase.child("/status").setValue("");
        myFirebase.child("/password").setValue(psw);
        myFirebase.child("/available").setValue("false");
        Intent intent = new Intent(this, StatusActivity.class);
        intent.putExtra("username", usr);
        startActivity(intent);
    }
}
