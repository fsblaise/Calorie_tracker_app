package hu.fsblaise.kcal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    private static int SECRET_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Performing the animations on the buttons.
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

        SECRET_KEY = getIntent().getIntExtra("SECRET_KEY", 99);
        Log.i(TAG, "onCreate" + "    " +SECRET_KEY);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
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