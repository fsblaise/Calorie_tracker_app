package hu.fsblaise.kcal;

public class FoodItem {
    private String id;
    private String name;
    private String info;
    private String calories;
    private float ratedInfo;
    private int imageResource;
    private int cartedCount;

    public FoodItem() {
    }

    public FoodItem(String name, String info, String calories, float ratedInfo, int imageResource, int cartedCount) {
        this.name = name;
        this.info = info;
        this.calories = calories;
        this.ratedInfo = ratedInfo;
        this.imageResource = imageResource;
        this.cartedCount = cartedCount;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public String getCalories() {
        return calories;
    }

    public float getRatedInfo() {
        return ratedInfo;
    }

    public int getImageResource() {
        return imageResource;
    }

    public int getCartedCount() {
        return cartedCount;
    }

    public String _getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
