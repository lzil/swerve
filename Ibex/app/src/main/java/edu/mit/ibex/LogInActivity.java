package edu.mit.ibex;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class LogInActivity extends ActionBarActivity {
    public static String psw2;
    public static boolean isPwdEntered = false;
    public static String usr, psw;
    Firebase baseFire, myFire;
    TextView logText;
    EditText username, password;
    HashMap<String, Object> data;
    Set<String> userList;
    double studLong = -71.094659;
    double studLat = 42.358991;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Firebase.setAndroidContext(this);
        baseFire = new Firebase("https://hangmonkey.firebaseio.com/");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        logText = (TextView) findViewById(R.id.invalidText);
        username =  (EditText) findViewById(R.id.usernameEditText);
        password =  (EditText) findViewById(R.id.passwordEditText);

        sp = getSharedPreferences("Login", 0);

        Map<String,?> keys = sp.getAll();
        Log.d("sharepref", "testing : map made");

        if(keys.entrySet().size()==0){
            Log.d("sharepref", "empty map");
        }
        else {
            Log.d("sharepref", "Map is populated");
            Intent intent = new Intent(this, StatusActivity.class);

            intent.putExtra("curUser", keys.get("curUser").toString());
            Log.d("login", "Log in success");
            startActivity(intent);
        }
        /*
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d("sharepref",entry.getKey() + ": " +
                    entry.getValue().toString());
        }*/


        baseFire.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                data = (HashMap<String, Object>) snapshot.getValue();
                Log.d("data", data.toString());
                Log.d("data", data.keySet().toString());
                userList = data.keySet();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
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

        //TODO need to check if user exists
        System.out.println(userList);
        if (userList.contains(usr)) {
            psw2 = "test"; //Can we get rid of this? looks like throw away code
            baseFire.child(usr).child("pass").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    LogInActivity.psw2 = snapshot.getValue().toString();
                    login(LogInActivity.psw2);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        } else{
            logText.setVisibility(View.VISIBLE);
            logText.setTextColor(Color.RED);
            logText.setText("User does not exist");
        }

    }

    public void login(String psw2) {
        if (psw2.equals(psw)) {
            Intent intent = new Intent(this, StatusActivity.class);
            intent.putExtra("curUser", usr);
            Log.d("login", "Log in success");

            SharedPreferences.Editor ed = sp.edit();
            ed.putString("curUser", usr);
            //ed.putString("curPsw", psw2);
            ed.commit();
            Log.d("sharepref", "added shared pref user"+usr+" psw"+psw2);

            startActivity(intent);
        }
        else {
            logText.setVisibility(View.VISIBLE);
            logText.setTextColor(Color.RED);
            logText.setText("Wrong curUser and password. Please try again.");
        }
    }

    public void signUp(View view){
        /*
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        LinearLayout lila1= new LinearLayout(this);
        lila1.setOrientation(LinearLayout.VERTICAL); //1 is for vertical orientation
        final EditText username = new EditText(this);
        username.setHint("Username");
        final EditText password = new EditText(this);
        password.setHint("Password");

        lila1.addView(username);
        lila1.addView(password);
        alert.setView(lila1);

        alert.setIcon(R.drawable.hangin);
        alert.setTitle("Register for Swerve!");

        alert.setPositiveButton("Sign up", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String usr = username.getText().toString();
                String psw = password.getText().toString();

                if(usr.equals("") || psw.equals("")){
                    Log.d("Sign Up", "User or pass is empty");
                    logText.setVisibility(View.VISIBLE);
                    logText.setTypeface(null, Typeface.ITALIC);
                    logText.setTextColor(Color.RED);
                    logText.setText("Username or password empty. Can't register.");
                    alert.show();
                }
                else {
                    //User/Pass are valid
                    //Check if user already exists
                    if (userList.contains(usr)) {
                        logText.setVisibility(View.VISIBLE);
                        logText.setText("User already taken");
                        alert.show();
                    } else {
                        Firebase myFirebase = new Firebase("https://hangmonkey.firebaseio.com/" + usr);
                        myFirebase.child("/status").setValue("");
                        myFirebase.child("/pass").setValue(psw);
                        myFirebase.child("/available").setValue("false");
                        myFirebase.child("/long").setValue(studLong);
                        myFirebase.child("/lat").setValue(studLat);
                        goToStatus();
                    }
                }
                //Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        alert.show();
        */

        logText.setTypeface(null, Typeface.ITALIC);
        logText.setTextColor(Color.DKGRAY);
        logText.setText("Signing up...");

        String usr = username.getText().toString();
        String psw = password.getText().toString();

        //Passes usr and psw to some server
        //if pass:
        if(usr.equals("") || psw.equals("")){
            Log.d("Sign Up", "User or pass is empty");
            logText.setVisibility(View.VISIBLE);
            logText.setTypeface(null, Typeface.ITALIC);
            logText.setTextColor(Color.RED);
            logText.setText("Username or password empty. Can't register.");
        }
        else {
            //User/Pass are valid
            //Check if user already exists
            if (userList.contains(usr)) {
                logText.setVisibility(View.VISIBLE);
                logText.setText("User already taken");
            } else{
                Firebase myFire = baseFire.child(usr);
                myFire.child("status").setValue("");
                myFire.child("pass").setValue(psw);
                myFire.child("available").setValue("false");
                myFire.child("long").setValue(studLong);
                myFire.child("lat").setValue(studLat);
                Intent intent = new Intent(this, StatusActivity.class);
                intent.putExtra("curUser", usr);
                startActivity(intent);
            }
        }
    }

    public void goToStatus(){
        Intent intent = new Intent(this, StatusActivity.class);
        intent.putExtra("curUser", usr);
        startActivity(intent);
    }
}
