package edu.mit.ibex;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.io.File;


public class RegisterActivity extends ActionBarActivity {
    ImageView photo;
    Bitmap bitMap;
    Firebase myFirebase;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Firebase.setAndroidContext(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        myFirebase = new Firebase("https://hangmonkey.firebaseio.com/");
        photo = (ImageView) findViewById(R.id.photo);
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

    public void signUp(View view){
        TextView logText = (TextView) findViewById(R.id.logText);
        //logText.setTypeface(null, Typeface.ITALIC);
        //logText.setTextColor(Color.GRAY);
        //logText.setText("Signing up...");

        EditText username =  (EditText) findViewById(R.id.usernameEditText);
        EditText password =  (EditText) findViewById(R.id.passwordEditText);

        String usr = username.getText().toString();
        String psw = password.getText().toString();

        //Passes usr and psw to some server
        //if pass:
        myFirebase.child(usr + "/status").setValue("");
        myFirebase.child(usr + "/pass").setValue(psw);
        myFirebase.child(usr + "/available").setValue("false");
        Intent intent = new Intent(this, StatusActivity.class);
        intent.putExtra("username", usr);
        startActivity(intent);
        //if fail: Failure message. Same screen. Retry.
        //logText.setTextColor(Color.RED);
        //logText.setText("This username is taken. Please try again.");
    }

    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d("camcam", "requestCode: "+ requestCode + " resultCode: "+resultCode);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            // Calculate inSampleSize
            // Raw height and width of image
            final int h = options.outHeight;
            final int w = options.outWidth;

            photo.setImageBitmap(rotateBitmap(decodeSampledBitmapFromFile(file.getAbsolutePath(), w, h), -90));
        }
    }

    public static Bitmap decodeSampledBitmapFromFile(String path,
                                                     int reqWidth, int reqHeight) { // BEST QUALITY MATCH
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 1;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
