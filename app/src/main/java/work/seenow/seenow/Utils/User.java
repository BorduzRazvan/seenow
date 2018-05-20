package work.seenow.seenow.Utils;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import work.seenow.seenow.BR;

public class User extends BaseObservable {
    String name;
    String email;
    String profileImage;
    String about;
    String birthday;


    // profile meta fields are ObservableField, will update the UI
    // whenever a new value is set
    public ObservableField<Long> numberofPhotosTaken = new ObservableField<>();
    public ObservableField<Long> numberofAppereances = new ObservableField<>();
    public ObservableField<Long> numberofFriends = new ObservableField<>();

    public User() {
    }

    @Bindable
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }


    @Bindable
    public String getBirthday(){
        return birthday;
    }

    public void setBirthday(String birthday)
    {
        this.birthday = birthday;
        notifyPropertyChanged(BR.birthday);
    }


    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);
    }

    @BindingAdapter({"profileImage"})
    public static void loadImage(ImageView view, String imageUrl) {
        Glide.with(view.getContext())
                .load(imageUrl)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(view.getContext()))
                .into(view);

        // If you consider Picasso, follow the below
        // Picasso.with(view.getContext()).load(imageUrl).placeholder(R.drawable.placeholder).into(view);
    }

    @Bindable
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
        notifyPropertyChanged(BR.profileImage);
    }

    @Bindable
    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
        notifyPropertyChanged(BR.about);
    }

    public ObservableField<Long> getNumberofFriends() {
        return numberofFriends;
    }

    public ObservableField<Long> getnumberofPhotosTaken() {
        return numberofPhotosTaken;
    }

    public ObservableField<Long> getnumberofAppereances() {
        return numberofAppereances;
    }
}