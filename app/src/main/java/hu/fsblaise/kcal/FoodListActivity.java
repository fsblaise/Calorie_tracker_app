package hu.fsblaise.kcal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
import java.util.Random;

import static android.os.Build.VERSION.SDK_INT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FoodListActivity extends AppCompatActivity {
    private static final String LOG_TAG = FoodListActivity.class.getName();
    public static final int PICK_IMAGE = 1;
    private FirebaseUser user;

    private FrameLayout redCircle;
    private TextView countTextView;
    private int cartItems = 0;
    private int gridNumber;
    private int queryLimit = 1000;
    private String picturePath = "";

    // Member variables.
    private RecyclerView mRecyclerView;
    private ArrayList<FoodItem> mItemsData;
    private ArrayList<FoodItem> mItems2Data;
    private FoodItemAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;
    private CollectionReference mItems2;

    private NotificationHandler mNotificationHandler;

    private boolean viewRow = true;

    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        }
    }

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
        setContentView(R.layout.activity_shop_list);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }
        if(user.getEmail().equals("null")){
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }
        Log.d(LOG_TAG, "Authenticated user!" + user.getEmail());

        int[] size = getScreenSize();
        if(size[0] < 900) gridNumber = 1;
        else if (size[0] < 1260) gridNumber = 2;
        else gridNumber = 3;

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

        mNotificationHandler = new NotificationHandler(this);
    }

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

                    // Notify the adapter of the change.
                    mAdapter.notifyDataSetChanged();
                });
    }

    public void deleteItem(FoodItem item) {
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
        // Setting up the popup window
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = ((Activity) this).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.update_popup,
                null);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setView(dialogLayout, 0, 30, 0, 0);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        WindowManager.LayoutParams wlmp = dialog.getWindow()
                .getAttributes();
        wlmp.gravity = Gravity.TOP;

        builder.setView(dialogLayout);

        dialog.show();

        ImageButton button = (ImageButton) dialogLayout.findViewById(R.id.confirm);
        button.setOnClickListener(view -> {

            // Getting the text out of the edittext
            String kcal;
            EditText kcalEdit = (EditText) dialogLayout.findViewById(R.id.kcalEditText);
            kcal = kcalEdit.getText().toString();

            // Setting the item's calorie count
            for (int i = 0; i < mItemsData.size(); i++) {
                if (mItemsData.get(i).getName().equals(item.getName())){
                    if (!kcal.equals("")){
                        kcal += " Kcal";
                        String id = mItemsData.get(i)._getId();
                        mItems.document(id).update("calories", kcal).addOnFailureListener(failure -> {
                            Toast.makeText(this, "Item " + id + " cannot be changed.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
            queryData();
            mNotificationHandler.cancel();
        });
    }

    public void addItem(FoodItem item) {
        // Get the resources from the XML file.
        String itemName = item.getName();
        Log.d(LOG_TAG, item.getName());
        String itemInfo = item.getInfo();
        String itemKcal = item.getCalories();
        int itemImage = item.getImageResource();
        float itemRate = item.getRatedInfo();

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
                settingsWindow();
                return true;
            case R.id.cart:
                Log.d(LOG_TAG, "Cart clicked!");
                Intent intent = new Intent(this, IntakeActivity.class);
                startActivity(intent);
                return true;
            case R.id.view_selector:
                if (viewRow) {
                    changeSpanCount(item, R.drawable.ic_view_row, (gridNumber==1) ? gridNumber : gridNumber-1);
                } else {
                    changeSpanCount(item, R.drawable.ic_view_grid, (gridNumber==1) ? gridNumber+1 : gridNumber);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void settingsWindow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = ((Activity) this).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.add_item_popup,
                null);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setView(dialogLayout, 0, 30, 0, 0);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        WindowManager.LayoutParams wlmp = dialog.getWindow()
                .getAttributes();
        wlmp.gravity = Gravity.TOP;

        builder.setView(dialogLayout);

        dialog.show();

        Button confirm = (Button) dialogLayout.findViewById(R.id.confirm);
        confirm.setOnClickListener(view -> {
            EditText nameET = (EditText) dialogLayout.findViewById(R.id.nameEditText);
            EditText infoET = (EditText) dialogLayout.findViewById(R.id.infoEditText);
            EditText kcalET = (EditText) dialogLayout.findViewById(R.id.caloriesEditText);
            if (nameET != null && infoET != null && kcalET != null && !picturePath.equals("")){
                String name = nameET.getText().toString();
                String info = infoET.getText().toString();
                String kcal = kcalET.getText().toString();
                if (!name.equals("") && !info.equals("") && !kcal.equals("")){
                    kcal += " Kcal";

                    float[] numbers = {0,0.5f,1,1.5f,2,2.5f,3,3.5f,4,4.5f,5};
                    Random r = new Random();
                    int rnd = r.nextInt(numbers.length);



                    mItems.add(new FoodItem(
                            name,
                            info,
                            kcal,
                            numbers[rnd],
                            0,
                            0,
                            picturePath));
                    Log.d(LOG_TAG, name + "                    name");
                    queryData();
                }
                if(name.equals("")) Log.e(LOG_TAG,"rossz a nev");
                if(name.equals("")) Log.e(LOG_TAG,"rossz az info");
                if(name.equals("")) Log.e(LOG_TAG,"rossz a kcal");
                if(picturePath.equals("")) Log.e(LOG_TAG,"rossz a path");
            }
            if(nameET == null) Log.e(LOG_TAG,"nincs nev");
            if(nameET == null) Log.e(LOG_TAG,"nincs info");
            if(nameET == null) Log.e(LOG_TAG,"nincs kcal");
            if(picturePath.equals("")) Log.e(LOG_TAG,"rossz a path");
        });
    }

    public void pickimage(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Uri selectedImageUri = data.getData();
                    picturePath = getRealPathFromURI(selectedImageUri);
                    Log.d(LOG_TAG, picturePath);
                } else {
                    requestPermission();
                }
            }
            else if (SDK_INT >= Build.VERSION_CODES.M){
                String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};

                int permsRequestCode = 200;

                requestPermissions(perms, permsRequestCode);

                Uri selectedImageUri = data.getData();
                picturePath = getRealPathFromURI(selectedImageUri);
                Log.d(LOG_TAG, picturePath);
            }else {
                Uri selectedImageUri = data.getData();
                picturePath = getRealPathFromURI(selectedImageUri);
                Log.d(LOG_TAG, picturePath);
            }
        }
    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = this.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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
    }

    private boolean shouldAskPermission(){

        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);

    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
        switch (permsRequestCode) {

            case 200:

                boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                break;

        }

    }
}
