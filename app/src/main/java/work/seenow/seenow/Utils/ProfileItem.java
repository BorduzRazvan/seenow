package work.seenow.seenow.Utils;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import work.seenow.seenow.R;

public class ProfileItem extends BaseObservable {
    String imageUrl;
    int imageId;


    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String imageUrl) {
        Glide.with(view.getContext())
                .load(imageUrl)
                .into(view);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setimageId(int imageId) { this.imageId = imageId ;    }

    public int getImageId(){ return imageId; }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
