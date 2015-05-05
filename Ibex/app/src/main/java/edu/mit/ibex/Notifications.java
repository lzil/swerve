package edu.mit.ibex;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Kevin on 5/5/2015.
 */
public class Notifications extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Notifications", "Class called");
        super.onCreate(savedInstanceState);
        Toast.makeText(getApplicationContext(),
                "Do Something NOW",
                Toast.LENGTH_LONG).show();
    }

}
