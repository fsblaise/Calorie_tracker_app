package hu.fsblaise.kcal;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    private static int SECRET_KEY;

    public int[] getScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        return new int[]{width, height};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        int[] size = getScreenSize();
//
//        if(size[1] < 1600) setContentView(R.layout.activity_main_land);

        View v = findViewById(R.id.card1);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        v.startAnimation(animation);

        v = findViewById(R.id.card2);
        animation = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        v.startAnimation(animation);

        v = findViewById(R.id.card3);
        animation = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        v.startAnimation(animation);
        v = findViewById(R.id.card4);
        animation = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        v.startAnimation(animation);


        Bundle bundle = getIntent().getExtras();
        SECRET_KEY = getIntent().getIntExtra("SECRET_KEY", 99);
        Log.i(TAG, "onCreate" + "    " +SECRET_KEY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    public void goToRegist(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void goToFoodList(View view) {
        Intent intent = new Intent(this, FoodListActivity.class);
        if (SECRET_KEY == 98){
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "You have to log in to access the Food list page!", Toast.LENGTH_SHORT).show();
        }
    }

    public void goToIntake(View view) {
        Intent intent = new Intent(this, IntakeActivity.class);
        if (SECRET_KEY == 98){
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "You have to log in to access the Daily Intake page!", Toast.LENGTH_SHORT).show();
        }
    }

    public void goToGithub(View view) {
        TextView t = (TextView) view.findViewById(R.id.textView3);
        String url = t.getText().toString();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}