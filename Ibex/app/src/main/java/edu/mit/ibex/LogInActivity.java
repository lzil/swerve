package edu.mit.ibex;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

        Log.d("Login", "Login opened");
        Firebase.setAndroidContext(this);
        baseFire = new Firebase("https://hangmonkey.firebaseio.com/");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        logText = (TextView) findViewById(R.id.invalidText);
        username =  (EditText) findViewById(R.id.usernameInput);
        password =  (EditText) findViewById(R.id.passwordInput);

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
            intent.putExtra("curAvailable", true);
            Log.d("login", "Log in success");
            startActivity(intent);
            finish();
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
        menu.findItem(R.id.action_logout).setVisible(false);
        return true;
    }

    public void logIn(View view) throws InterruptedException {
        logText.setTypeface(null, Typeface.ITALIC);
        logText.setTextColor(Color.DKGRAY);
        logText.setText("Confirming user login...");

        usr = username.getText().toString();
        psw = password.getText().toString();

        //TODO need to check if user exists
        if(usr!=null){
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

    }}

    public void login(String psw2) {
        if (psw2.equals(psw)) {
            Intent intent = new Intent(this, StatusActivity.class);
            intent.putExtra("curUser", usr);
            intent.putExtra("curAvailable", true);
            Log.d("login", "Log in success");

            SharedPreferences.Editor ed = sp.edit();
            ed.putString("curUser", usr);
            //ed.putString("curPsw", psw2);
            ed.commit();
            Log.d("sharepref", "added shared pref user"+usr+" psw"+psw2);
            startActivity(intent);
            finish();
        }
        else {
            logText.setVisibility(View.VISIBLE);
            logText.setTextColor(Color.RED);
            logText.setText("Wrong username and password. Please try again.");
        }
    }

    public void signUp(View view){

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        View signUpLayout = getLayoutInflater().inflate(R.layout.layout_sign_up, null);
        final EditText username = (EditText) signUpLayout.findViewById(R.id.usernameInput);
        final EditText password = (EditText) signUpLayout.findViewById(R.id.passwordInput);

        alert.setView(signUpLayout);

        alert.setIcon(R.drawable.hangin);
        alert.setTitle("Register for Swerve!");

        alert.setPositiveButton("Sign up", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String usr = username.getText().toString();
                String psw = password.getText().toString();

                if(usr.equals("") || psw.equals("")){
                    Log.d("Sign Up", "User or pass is empty");
                    Toast.makeText(getApplicationContext(),
                            "Username or password is empty! Can't register q.q",
                                    Toast.LENGTH_LONG).show();
                } else if (userList.contains(usr)) {
                    Toast.makeText(getApplicationContext(),
                            "Username already taken :(",
                            Toast.LENGTH_LONG).show();
                } else {
                    myFire = baseFire.child(usr);
                    myFire.child("status").setValue("Hi everyone! I'm new to Swerve!");
                    myFire.child("pass").setValue(psw);
                    myFire.child("available").setValue(false);
                    myFire.child("long").setValue(studLong);
                    myFire.child("lat").setValue(studLat);
                    goToStatus(usr);
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
    }

    public void goToStatus(String usr){
        Intent intent = new Intent(this, StatusActivity.class);
        intent.putExtra("curUser", usr);
        intent.putExtra("curAvailable", true);
        startActivity(intent);
        finish();
    }
}
