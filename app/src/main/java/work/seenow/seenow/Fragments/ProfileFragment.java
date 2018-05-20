package work.seenow.seenow.Fragments;


import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import work.seenow.seenow.R;
import work.seenow.seenow.Utils.GridSpacingItemDecoration;
import work.seenow.seenow.Utils.PostsAdapter;
import work.seenow.seenow.Utils.ProfileItem;
import work.seenow.seenow.Utils.User;
import work.seenow.seenow.databinding.*;


public class ProfileFragment extends Fragment implements PostsAdapter.PostsAdapterListener {
    private static final String TAG = ProfileFragment.class.getSimpleName();
    private MyClickHandlers handlers;
    private PostsAdapter mAdapter;
    private RecyclerView recyclerView;
    private ActivityGalleryBinding binding;
    private User user;


    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment newInstance(int page, String title) {
        ProfileFragment fragmentFirst = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    // Inflate the view for the fragment based on layout XML
    @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;
        binding = DataBindingUtil.setContentView(getActivity(), R.layout.activity_gallery);

//        Toolbar toolbar = binding.toolbar;
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle(R.string.toolbar_profile);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handlers = new MyClickHandlers(getContext());

        renderProfile();

        initRecyclerView();

        view = binding.getRoot();

        return view;
    }

    /**
     * Renders RecyclerView with Grid Images in 3 columns
     */
    private void initRecyclerView() {
        recyclerView = binding.content.recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(4), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        mAdapter = new PostsAdapter(getPosts(), this);
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * Renders user profile data
     */
    private void renderProfile() {
        user = new User();
        user.setName("David Attenborough");
        user.setEmail("david@natgeo.com");
        user.setProfileImage("https://api.androidhive.info/images/nature/david.jpg");
        user.setAbout("Naturalist");

        // ObservableField doesn't have setter method, instead will
        // be called using set() method
        user.numberofPhotosTaken.set(3400L);
        user.numberofAppereances.set(3050890L);
        user.numberofFriends.set(150L);


        // display user
        binding.setUser(user);

        // assign click handlers
        binding.content.setHandlers(handlers);
    }

    private ArrayList<ProfileItem> getPosts() {
        ArrayList<ProfileItem> posts = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            ProfileItem post = new ProfileItem();
            post.setImageUrl("https://api.androidhive.info/images/nature/" + i + ".jpg");

            posts.add(post);
        }

        return posts;
    }

    @Override
    public void onPostClicked(ProfileItem post) {
        Toast.makeText(getActivity().getApplicationContext(), "Post clicked! " + post.getImageUrl(), Toast.LENGTH_SHORT).show();
    }

    public class MyClickHandlers {

        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        /**
         * Demonstrating updating bind data
         * Profile name, number of posts and profile image
         * will be updated on Fab click
         */
        public void onProfileFabClicked(View view) {
            user.setName("Sir David Attenborough");
            user.setProfileImage("https://api.androidhive.info/images/nature/david1.jpg");

            // updating ObservableField
            user.numberofPhotosTaken.set(5500L);
            user.numberofAppereances.set(5050890L);
            user.numberofFriends.set(180L);
        }

        public boolean onProfileImageLongPressed(View view) {
            Toast.makeText(getActivity().getApplicationContext(), "Profile image long pressed!", Toast.LENGTH_LONG).show();
            return false;
        }


        public void onFollowersClicked(View view) {
            Toast.makeText(context, "Followers is clicked!", Toast.LENGTH_SHORT).show();
        }

        public void onFollowingClicked(View view) {
            Toast.makeText(context, "Following is clicked!", Toast.LENGTH_SHORT).show();
        }

        public void onPostsClicked(View view) {
            Toast.makeText(context, "Posts is clicked!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}