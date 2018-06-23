package work.seenow.seenow.Utils;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.Serializable;

import work.seenow.seenow.BR;

public class User extends BaseObservable implements Serializable {
    private int id;
    private String name;
    private String email;
    private String profileImage;
    private String birthday;
    private String country;
    private String gender;
    private String useRecognizer;
    private String created_at;
    private String about;


    // profile meta fields are ObservableField, will update the UI
    // whenever a new value is set
    public ObservableField<Long> numberofPhotosTaken = new ObservableField<>();
    public ObservableField<Long> numberofAppereances = new ObservableField<>();
    public ObservableField<Long> numberofFriends = new ObservableField<>();
    public ObservableField<Integer> numberofPoints = new ObservableField<>();

    public User(int id, String name, String email, String profileImage, String birthday, String country, String gender, String userRecognizer, String created_at, String about) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profileImage = AppConfig.URL_SERVER+profileImage;
        Log.d("UserClass","Am profileLink: "+this.profileImage);
        this.birthday = birthday;
        this.country = country;
        this.gender = gender;
        this.useRecognizer= userRecognizer;
        this.created_at = created_at;
        this.about = about;
    }
    public User(int id, String name, String email, String profileImage, String birthday, String country, String gender, int points, String userRecognizer, String created_at, String about) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profileImage =  AppConfig.URL_SERVER+profileImage;
        Log.d("UserClass","Am profileLink: "+this.profileImage);
        this.birthday = birthday;
        this.country = country;
        this.gender = gender;
        numberofPoints.set(points);
        this.useRecognizer = userRecognizer;
        this.created_at = created_at;
        this.about = about;
    }

    @Bindable
    public String getCreated_at() { return this.created_at; }

    public String getGender() { return this.gender; }

    public void setUseRecognizer(String useRecognizer){ this.useRecognizer = useRecognizer; }

    public int getId() { return id; }

    @Bindable
    public String getAbout() { return this.about;}

    public void setAbout(String about) { this.about = about;
        notifyPropertyChanged(BR.about);
    }

    @Bindable
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    public String getUseRecognizer() { return useRecognizer; }

    @Bindable
    public String getCountry() { return this.country; }


    @Bindable
    public String getBirthday(){
        return birthday;
    }

    public void setBirthday(String birthday)
    {
        this.birthday = birthday;
        notifyPropertyChanged(BR.birthday);
    }

    public void setCountry(String country) { this.country = country; }


    @Bindable
    public String getEmail() {
        return email;
    }

    @BindingAdapter({"profileImage"})
    public static void loadImage(ImageView view, String imageUrl) {
        Glide.with(view.getContext())
                .load(imageUrl)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(view.getContext()))
                .into(view);
    }

    @Bindable
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = AppConfig.URL_SERVER + profileImage;
        notifyPropertyChanged(BR.profileImage);
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

    public ObservableField<Integer> getnumberofPoints() { return numberofPoints; }


}