package hu.fsblaise.kcal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class IntakeActivity extends AppCompatActivity {
    private static final String LOG_TAG = IntakeActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private final String healthy = "You are living a healthy life!";
    private final String tooMuch = "You are eating too much calorie!";
    private final String tooFew = "You should consume more calories!";

    private FirebaseUser user;

    private int gridNumber = 1;
    private int queryLimit = 1000;
    private int hour;
    private int minute;
    private long timeinmillis;
    private static String time = "";
    private String msg;

    // Member variables.
    private RecyclerView mRecyclerView;
    private ArrayList<FoodItem> mItems2Data;
    private IntakeAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems2;

    private NotificationHandler mNotificationHandler;
    private AlarmManager mAlarmManager;
    private JobScheduler mJobScheduler;

    private SharedPreferences preferences;

    private boolean viewRow = true;

    private int[] getScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        return new int[]{width, height};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intake);

        View v = findViewById(R.id.topCard);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        v.startAnimation(animation);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && !user.getEmail().equals("null")) {
            Log.d(LOG_TAG, "Authenticated user!" + user.getEmail());
        } else {
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }

        int[] size = getScreenSize();
        if (size[0] < 1260) gridNumber = 1;
        else if (size[0] < 2000) gridNumber = 2;
        else gridNumber = 3;

        // recycle view
        mRecyclerView = findViewById(R.id.recyclerView2);
        // Set the Layout Manager.
        mRecyclerView.setLayoutManager(new GridLayoutManager(
                this, gridNumber));
        // Initialize the ArrayList that will contain the data.
        mItems2Data = new ArrayList<>();
        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new IntakeAdapter(this, mItems2Data);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems2 = mFirestore.collection((user.getEmail() != null) ? "Intake: " + user.getEmail() : "Intake: Default");
        // Get the data.
        loadKcalSum();
        queryData2();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver, filter);

        mNotificationHandler = new NotificationHandler(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mJobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        //setAlarmManager();
        //setJobScheduler();

    }

    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null)
                return;

            switch (action) {
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 1000;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 5;
                    break;
            }

            queryData2();
        }
    };

    private void loadKcalSum() {
        int kcalSum = 0;
        for (int i = 0; i < mItems2Data.size(); i++) {
            kcalSum += (mItems2Data.get(i).getCartedCount() + 1) * Integer.parseInt(mItems2Data.get(i).getCalories().replace(" Kcal", ""));
        }
        TextView calText = (TextView) findViewById(R.id.calSum);
        TextView tipText = (TextView) findViewById(R.id.tips);
        calText.setText("" + kcalSum + " Kcal");
        if (kcalSum < 1500) {
            tipText.setText(tooFew);
            msg = tooFew;
        } else if (kcalSum < 2000) {
            tipText.setText(healthy);
            msg = healthy;
        } else if (kcalSum > 2000) {
            tipText.setText(tooMuch);
            msg = tooMuch;
        }
    }

    private void queryData2() {
        mItems2Data.clear();
        mItems2.orderBy("cartedCount", Query.Direction.DESCENDING).limit(queryLimit).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FoodItem item = document.toObject(FoodItem.class);
                        item.setId(document.getId());
                        mItems2Data.add(item);
                    }

                    if (mItems2Data.size() == 0) {
                        return;
                    }

                    // Notify the adapter of the change.
                    mAdapter.notifyDataSetChanged();
                    loadKcalSum();
                });
    }

    public void removeItem(FoodItem item) {
        for (int i = 0; i < mItems2Data.size(); i++) {
            if (mItems2Data.get(i).getName().equals(item.getName())) {
                if (mItems2Data.get(i).getCartedCount() > 0) {
                    String id = mItems2Data.get(i)._getId();
                    mItems2.document(id).update("cartedCount", mItems2Data.get(i).getCartedCount() - 1).addOnFailureListener(failure -> {
                        Toast.makeText(this, "Item " + id + " cannot be changed.", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    String id = mItems2Data.get(i)._getId();
                    DocumentReference ref = mItems2.document(mItems2Data.get(i)._getId());
                    ref.delete().addOnSuccessListener(succes -> {
                        Log.d(LOG_TAG, "Item is successfully deleted: " + id);
                    })
                            .addOnFailureListener(failure -> {
                                Toast.makeText(this, "Item  " + id + " cannot be deleted.", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        }
        queryData2();
        mNotificationHandler.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.intake_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG, s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out_button:
                Log.d(LOG_TAG, "Logout clicked!");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.settings_button:
                Log.d(LOG_TAG, "Setting clicked!");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = ((Activity) this).getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.settings_popup,
                        null);
                final AlertDialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                dialog.setView(dialogLayout, 0, 30, 0, 0);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                if (!time.equals("")){
                    Button b = (Button)dialogLayout.findViewById(R.id.timepicker);
                    b.setText(time);
                }
                Button confirm = (Button) dialogLayout.findViewById(R.id.confirm);
                confirm.setOnClickListener(view -> {
                    if(!time.equals("")){
                        setAlarmManager();
                        dialog.hide();
                    }
                });
                WindowManager.LayoutParams wlmp = dialog.getWindow()
                        .getAttributes();
                wlmp.gravity = Gravity.TOP;

                builder.setView(dialogLayout);

                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showTimePicker(View view) {
        Button b = (Button) view;
        Calendar mCurrentTime = Calendar.getInstance();
        hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                time = ((selectedHour < 10) ? ("0" + selectedHour) : selectedHour) + ":" + ((selectedMinute < 10) ? ("0" + selectedMinute) : selectedMinute);
                b.setText(time);
                final Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                timeinmillis = calendar.getTimeInMillis();
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select the time of the day!");
        mTimePicker.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }

    private void setAlarmManager() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("msg", msg);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d(LOG_TAG,"" + timeinmillis);

        mAlarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                timeinmillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );

//        mAlarmManager.cancel(pendingIntent);
    }
}
