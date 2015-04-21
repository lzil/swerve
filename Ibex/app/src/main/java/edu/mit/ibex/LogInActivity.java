package edu.mit.ibex;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.Firebase;


public class LogInActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        TextView logText = (TextView) findViewById(R.id.logText);
        logText.setTypeface(null, Typeface.ITALIC);
        logText.setTextColor(Color.GRAY);
        logText.setText("Confirming user login...");

        EditText username =  (EditText) findViewById(R.id.usernameEditText);
        EditText password =  (EditText) findViewById(R.id.passwordEditText);

        String usr = username.getText().toString();
        String psw = password.getText().toString();

        //Passes usr and psw to some server
        //if pass:
        Intent intent = new Intent(this, StatusActivity.class);
        intent.putExtra("username", usr);
        startActivity(intent);
        //if fail: Failure message. Same screen. Retry.
        logText.setTextColor(Color.RED);
        logText.setText("Wrong username and password. Please try again.");
    }

    public void reg(View view){
        TextView logText = (TextView) findViewById(R.id.logText);
        logText.setTypeface(null, Typeface.ITALIC);
        logText.setTextColor(Color.GRAY);
        logText.setText("Register...");

        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
