package hu.fsblaise.kcal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = RegisterActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 98;

    EditText emailEditText;
    EditText passwordEditText;
    EditText confirmPasswordEditText;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.userEmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.passwordAgainEditText);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String email = preferences.getString("email", "");
        String password = preferences.getString("password", "");

        emailEditText.setText(email);
        passwordEditText.setText(password);

        mAuth = FirebaseAuth.getInstance();

        Log.i(TAG, "onCreate");
    }

    public void register(View view) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (!password.equals(confirmPassword)) {
            Log.e(TAG, "Nem egyeznek a jelszavak.");
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User created successfully");
                    startShopping();
                } else {
                    Log.d(TAG, "Something went wrong");
                    Toast.makeText(RegisterActivity.this, "User was not created successfully:" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void cancel(View view) {
        finish();
    }

    private void startShopping() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("SECRET_KEY",SECRET_KEY);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        // It puts the email and password back, after rotating the device,
        // or when onPause gets called in someway,
        // instead of having to type in again
        editor.putString("email", emailEditText.getText().toString());
        editor.putString("password", passwordEditText.getText().toString());
        editor.apply();
        Log.i(TAG, "onPause");
    }
}