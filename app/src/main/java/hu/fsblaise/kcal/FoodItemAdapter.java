package hu.fsblaise.kcal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder> implements Filterable {
    // Member variables.
    private ArrayList<FoodItem> mFoodItemData = new ArrayList<>();
    private ArrayList<FoodItem> mFoodItemDataAll = new ArrayList<>();
    private Context mContext;
    private int lastPosition = -1;

    FoodItemAdapter(Context context, ArrayList<FoodItem> itemsData) {
        this.mFoodItemData = itemsData;
        this.mFoodItemDataAll = itemsData;
        this.mContext = context;
    }

    @Override
    public FoodItemAdapter.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(FoodItemAdapter.ViewHolder holder, int position) {
        // Get current sport.
        FoodItem currentItem = mFoodItemData.get(position);

        // Populate the textviews with data.
        holder.bindTo(currentItem);


        if (holder.getAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mFoodItemData.size();
    }


    /**
     * RecycleView filter
     **/
    @Override
    public Filter getFilter() {
        return ShoppingFilter;
    }

    private Filter ShoppingFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<FoodItem> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if (charSequence == null || charSequence.length() == 0) {
                results.count = mFoodItemDataAll.size();
                results.values = mFoodItemDataAll;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (FoodItem item : mFoodItemDataAll) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }

                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mFoodItemData = (ArrayList) filterResults.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        // Member Variables for the TextViews
        private TextView mTitleText;
        private TextView mInfoText;
        private TextView mPriceText;
        private ImageView mItemImage;
        private RatingBar mRatingBar;

        ViewHolder(View itemView) {
            super(itemView);

            // Initialize the views.
            mTitleText = itemView.findViewById(R.id.itemTitle);
            mInfoText = itemView.findViewById(R.id.subTitle);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mRatingBar = itemView.findViewById(R.id.ratingBar);
            mPriceText = itemView.findViewById(R.id.price);
        }

        void bindTo(FoodItem currentItem) {
            mTitleText.setText(currentItem.getName());
            mInfoText.setText(currentItem.getInfo());
            mPriceText.setText(currentItem.getCalories());
            mRatingBar.setRating(currentItem.getRatedInfo());

            // Load the images into the ImageView using the Glide library.
            // If there is a locally stored picture with that item, load it
            if(currentItem.getLocalPath() != null && !currentItem.getLocalPath().equals("")){
                Glide.with(mContext).load(currentItem.getLocalPath()).into(mItemImage);
            }
            // Else check the firestore database for a resourceID, to load
            else{
                Glide.with(mContext).load(currentItem.getImageResource()).into(mItemImage);
            }
            itemView.findViewById(R.id.add_to_cart).setOnClickListener(view -> ((FoodListActivity) mContext).updateAlertIcon(currentItem));
            itemView.findViewById(R.id.delete).setOnClickListener(view -> ((FoodListActivity) mContext).deleteItem(currentItem));
            itemView.findViewById(R.id.remove).setOnClickListener(view -> ((FoodListActivity) mContext).removeItem(currentItem));
            itemView.findViewById(R.id.update).setOnClickListener(view -> ((FoodListActivity) mContext).updateItem(currentItem));
        }
    }
}

