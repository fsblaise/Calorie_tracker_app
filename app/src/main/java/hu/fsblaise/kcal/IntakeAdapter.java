package hu.fsblaise.kcal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class IntakeAdapter extends RecyclerView.Adapter<IntakeAdapter.ViewHolder> implements Filterable {
    private static final String LOG_TAG = MainActivity.class.getName();
    // Member variables.
    private ArrayList<FoodItem> mFoodItemData = new ArrayList<>();
    private ArrayList<FoodItem> mFoodItemDataAll = new ArrayList<>();
    private Context mContext;
    private int lastPosition = -1;

    IntakeAdapter(Context context, ArrayList<FoodItem> itemsData) {
        this.mFoodItemData = itemsData;
        this.mFoodItemDataAll = itemsData;
        this.mContext = context;
    }

    @Override
    public IntakeAdapter.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_intake, parent, false));
    }

    @Override
    public void onBindViewHolder(IntakeAdapter.ViewHolder holder, int position) {
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
        private TextView mPriceText;
        private TextView mCountText;

        ViewHolder(View itemView) {
            super(itemView);

            // Initialize the views.
            mTitleText = itemView.findViewById(R.id.itemTitle);
            mPriceText = itemView.findViewById(R.id.price);
            mCountText = itemView.findViewById(R.id.count);
        }

        void bindTo(FoodItem currentItem) {
            mTitleText.setText(currentItem.getName());
            mPriceText.setText(currentItem.getCalories());
            String count = "" + (currentItem.getCartedCount() + 1);
            mCountText.setText(count);

            Button removeButton = (Button) itemView.findViewById(R.id.remove);

            removeButton.setOnClickListener(view -> ((IntakeActivity) mContext).removeItem(currentItem));
        }
    }
}

