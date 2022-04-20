package hu.fsblaise.kcal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static int SECRET_KEY;
//    private static final int RC_SIGN_IN = 123;
//    private static final int SECRET_KEY = 99;

//    EditText userNameET;
//    EditText passwordET;
//
//    private SharedPreferences preferences;
//    private FirebaseAuth mAuth;
//    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        SECRET_KEY = getIntent().getIntExtra("SECRET_KEY", 99);
//        userNameET = findViewById(R.id.editTextUserName);
//        passwordET = findViewById(R.id.editTextPassword);
//
//        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
//        mAuth = FirebaseAuth.getInstance();
//
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Random Async Task
//        Button button = findViewById(R.id.guestLoginButton);
//        new RandomAsyncTask(button).execute();

        // Random Async Loader
//        getSupportLoaderManager().restartLoader(0, null, this);
        Log.i(TAG, "onCreate" + "    " +SECRET_KEY);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//
//            try {
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());
//                firebaseAuthWithGoogle(account.getIdToken());
//            } catch (ApiException e) {
//                Log.w(TAG, "Google sign in failed", e);
//            }
//        }
//
//    }

//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    Log.d(TAG, "User logged in successfully!");
//                    startShopping();
//                } else {
//                    Log.d(TAG, "Something went wrong...");
//                    Toast.makeText(MainActivity.this, "User login fail: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//
//    }

//    public void login(View view) {
//        String userName = userNameET.getText().toString();
//        String password = passwordET.getText().toString();
//
////        Log.i(TAG, "login: " + userName + "passwd: " + password);
//
//        mAuth.signInWithEmailAndPassword(userName, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    Log.d(TAG, "User logged in successfully!");
//                    startShopping();
//                } else {
//                    Log.d(TAG, "Something went wrong...");
//                    Toast.makeText(MainActivity.this, "User login fail: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//    }

//    private void startShopping() {
//        Intent intent = new Intent(this, ShopListActivity.class);
//        startActivity(intent);
//    }
//
//    public void loginWithGoogle(View view) {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }
//
//    public void loginAsGuest(View view) {
//        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    Log.d(TAG, "Anonym user logged in successfully");
//                    startShopping();
//                } else {
//                    Log.d(TAG, "Anonym user login fail");
//                    Toast.makeText(MainActivity.this, "User login fail: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//    }

//    public void register(View view) {
//        Intent intent = new Intent(this, RegisterActivity.class);
//        intent.putExtra("SECRET_KEY", SECRET_KEY);
//        startActivity(intent);
//        // TODO
//    }

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
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString("userName", userNameET.getText().toString());
//        editor.putString("password", passwordET.getText().toString());
//        editor.apply();

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
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
        // TODO: putextra with the secret key, default 99, logged in 98,
        //  if default: only list, else ability to add to the intake collection, or remove from it
    }

    public void goToIntake(View view) {
        Intent intent = new Intent(this, ShopListActivity.class);
        if (SECRET_KEY == 98){
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "You have to log in to access the Daily Intake page!", Toast.LENGTH_SHORT).show();
        }
    }


//    @NonNull
//    @Override
//    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
//        return new RandomAsyncLoader(this);
//    }
//
//    @Override
//    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
//        Button button = findViewById(R.id.guestLoginButton);
//        button.setText(data);
//    }
//
//    @Override
//    public void onLoaderReset(@NonNull Loader<String> loader) {
//
//    }
}