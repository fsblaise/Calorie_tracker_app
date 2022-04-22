package hu.fsblaise.kcal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FoodListActivity extends AppCompatActivity {
    private static final String LOG_TAG = FoodListActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private FirebaseUser user;

    private FrameLayout redCircle;
    private TextView countTextView;
    private int cartItems = 0;
    private int gridNumber = 1;
    private int queryLimit = 1000;

    // Member variables.
    private RecyclerView mRecyclerView;
    private ArrayList<FoodItem> mItemsData;
    private ArrayList<FoodItem> mItems2Data;
    private FoodItemAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;
    private CollectionReference mItems2;

    private NotificationHandler mNotificationHandler;
    private AlarmManager mAlarmManager;
    private JobScheduler mJobScheduler;

    private SharedPreferences preferences;

    private boolean viewRow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);

        user = FirebaseAuth.getInstance().getCurrentUser();

        //Commented out, because it will block the guest login
        if(user == null){
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }
        if(user.getEmail().equals("null")){
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }
        Log.d(LOG_TAG, "Authenticated user!" + user.getEmail());

/*        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        if(preferences != null) {
            cartItems = preferences.getInt("cartItems", 0);
            gridNumber = preferences.getInt("gridNum", 1);
        }*/

        // recycle view
        mRecyclerView = findViewById(R.id.recyclerView);
        // Set the Layout Manager.
        mRecyclerView.setLayoutManager(new GridLayoutManager(
                this, gridNumber));
        // Initialize the ArrayList that will contain the data.
        mItemsData = new ArrayList<>();
        mItems2Data = new ArrayList<>();
        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new FoodItemAdapter(this, mItemsData);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection((user.getEmail() != null) ? user.getEmail() : "Items");
        mItems2 = mFirestore.collection((user.getEmail() != null) ? "Intake: " + user.getEmail() : "Intake: Default");
        // Get the data.
        queryData();

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

            queryData();
        }
    };

    private void initializeData() {
        // Get the resources from the XML file.
        String[] itemsList = getResources()
                .getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources()
                .getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources()
                .getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResources =
                getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        for (int i = 0; i < itemsList.length; i++)
            mItems.add(new FoodItem(
                    itemsList[i],
                    itemsInfo[i],
                    itemsPrice[i],
                    itemRate.getFloat(i, 0),
                    itemsImageResources.getResourceId(i, 0),
                    0));

        // Recycle the typed array.
        itemsImageResources.recycle();
    }

    private void queryData() {
        // Clear the existing data (to avoid duplication).
        mItemsData.clear();

//        mItems.whereEqualTo()...
        mItems.orderBy("cartedCount", Query.Direction.DESCENDING).limit(queryLimit).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FoodItem item = document.toObject(FoodItem.class);
                        item.setId(document.getId());
                        mItemsData.add(item);
                    }

                    if (mItemsData.size() == 0) {
                        initializeData();
                        queryData();
                    }

                    // Notify the adapter of the change.
                    mAdapter.notifyDataSetChanged();
                });
    }

    private void queryData2(){
        mItems2Data.clear();
        mItems2.orderBy("cartedCount", Query.Direction.DESCENDING).limit(queryLimit).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FoodItem item = document.toObject(FoodItem.class);
                        item.setId(document.getId());
                        mItems2Data.add(item);
                    }

//                    if (mItems2Data.size() == 0) {
//                        return;
//                    }

                    // Notify the adapter of the change.
                    mAdapter.notifyDataSetChanged();
                });
    }

    public void deleteItem(FoodItem item) {
        // Todo: make it delete from the other collection
        DocumentReference ref = mItems.document(item._getId());

        ref.delete().addOnSuccessListener(succes -> {
            Log.d(LOG_TAG, "Item is successfully deleted: " + item._getId());
        })
                .addOnFailureListener(failure -> {
                    Toast.makeText(this, "Item  " + item._getId() + " cannot be deleted.", Toast.LENGTH_SHORT).show();
                });

        queryData();
        mNotificationHandler.cancel();
    }

    public void updateItem(FoodItem item) {

    }

    public void addItem(FoodItem item) {
        // Get the resources from the XML file.
        String itemName = item.getName();
        Log.d(LOG_TAG, item.getName() + "     aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        String itemInfo = item.getInfo();
        String itemKcal = item.getCalories();
        int itemImage = item.getImageResource();
        float itemRate = item.getRatedInfo();


//        String itemsList = getResources()
//                .getStringArray(R.array.shopping_item_names);
//        String itemsInfo = getResources()
//                .getStringArray(R.array.shopping_item_desc);
//        String itemsPrice = getResources()
//                .getStringArray(R.array.shopping_item_price);
//        TypedArray itemsImageResources =
//                getResources().obtainTypedArray(R.array.shopping_item_images);
//        TypedArray itemRate = getResources().obtainTypedArray(R.array.shopping_item_rates);
        boolean contains = false;
        for (int i = 0; i < mItems2Data.size(); i++) {
            if (mItems2Data.get(i).getName().equals(item.getName())){
                String id = mItems2Data.get(i)._getId();
                mItems2.document(id).update("cartedCount", mItems2Data.get(i).getCartedCount() + 1).addOnFailureListener(failure -> {
                    Toast.makeText(this, "Item " + id + " cannot be changed.", Toast.LENGTH_SHORT).show();
                });
                contains = true;
                break;
            }
        }
        if(!contains)
            mItems2.add(new FoodItem(
                itemName,
                itemInfo,
                itemKcal,
                itemRate,
                itemImage,
                0));

        queryData2();

        // Recycle the typed array.
//        itemsImageResources.recycle();
    }

    public void removeItem(FoodItem item) {
        for (int i = 0; i < mItems2Data.size(); i++) {
            if (mItems2Data.get(i).getName().equals(item.getName())){
                if (mItems2Data.get(i).getCartedCount() > 0){
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

    public void updateAlertIcon(FoodItem item) {
        addItem(item);
        cartItems = (cartItems + 1);
        if (0 < cartItems) {
            countTextView.setText(String.valueOf(cartItems));
        } else {
            countTextView.setText("");
        }

        redCircle.setVisibility((cartItems > 0) ? VISIBLE : GONE);

        mItems.document(item._getId()).update("cartedCount", item.getCartedCount() + 1).addOnFailureListener(failure -> {
            Toast.makeText(this, "Item " + item._getId() + " cannot be changed.", Toast.LENGTH_SHORT).show();
        });

        mNotificationHandler.send(item.getName());
        queryData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
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
//                FirebaseAuth.getInstance().signOut();
//                finish();
                return true;
            case R.id.cart:
                Log.d(LOG_TAG, "Cart clicked!");
                Intent intent = new Intent(this, IntakeActivity.class);
                startActivity(intent);
                return true;
            case R.id.view_selector:
                if (viewRow) {
                    changeSpanCount(item, R.drawable.ic_view_grid, 1);
                } else {
                    changeSpanCount(item, R.drawable.ic_view_row, 2);

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        countTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(alertMenuItem);
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }

    private void setAlarmManager() {
        long repeatInterval = 1 * 60 * 1000;//AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mAlarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                repeatInterval,
                pendingIntent
        );

        // mAlarmManager.cancel(pendingIntent);
    }

    private void setJobScheduler() {
        int networkType = JobInfo.NETWORK_TYPE_UNMETERED;
        int hardDeadLine = 5000;

        ComponentName name = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(0, name)
                .setRequiredNetworkType(networkType)
                .setRequiresCharging(true)
                .setOverrideDeadline(hardDeadLine);

        mJobScheduler.schedule(builder.build());
//        mJobScheduler.cancel(0);
    }
}
