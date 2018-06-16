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
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import work.seenow.seenow.R;
import work.seenow.seenow.Utils.AppConfig;
import work.seenow.seenow.Utils.AppController;
import work.seenow.seenow.Utils.FeedItem;
import work.seenow.seenow.Utils.GridSpacingItemDecoration;
import work.seenow.seenow.Utils.PostsAdapter;
import work.seenow.seenow.Utils.ProfileItem;
import work.seenow.seenow.Utils.SQLiteHandler;
import work.seenow.seenow.Utils.User;
import work.seenow.seenow.databinding.*;


public class ProfileFragment extends Fragment implements PostsAdapter.PostsAdapterListener {
    private static final String TAG = ProfileFragment.class.getSimpleName();
    private MyClickHandlers handlers;
    private PostsAdapter mAdapter;
    private RecyclerView recyclerView;
    private ActivityGalleryBinding binding;
    private User user;
    private static ArrayList<ProfileItem> posts;
    private int targetuser_id;

    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment newInstance(User user, int id) {
        ProfileFragment fragmentFirst = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("user",user);
        args.putInt("userId",id);
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
        binding = DataBindingUtil.inflate(
                inflater, R.layout.activity_gallery, container, false);
        user = (User)getArguments().getSerializable("user");
        targetuser_id = (int)getArguments().getInt("userId");
        posts = new ArrayList<ProfileItem>();
        handlers = new MyClickHandlers(getContext());
        getPosts(user.getId());
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
        mAdapter = new PostsAdapter(posts, this);
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * Renders user profile data
     */
    private void renderProfile() {

        // display user
        binding.setUser(user);

        // assign click handlers
        binding.content.setHandlers(handlers);
    }

    private void getPosts(final int id) {
       posts.clear();
       StringRequest strReq = new StringRequest(Request.Method.POST,
        AppConfig.URL_GALLERY, new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.d(TAG, "Login Response: " + response.toString());
            try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

//                  Check for error node in json
                    if (!error) {
                        parseJsonProfile(jObj, id);
                        JSONObject jSUser = jObj.getJSONObject("user");
                        user.setName(jSUser.getString("fullname"));
                        user.setBirthday(jSUser.getString("birthday"));
                        user.setCountry(jSUser.getString("country"));
                        user.setProfileImage(jSUser.getString("profilePicture"));
                        user.numberofFriends.set(jSUser.getLong("nr_friends"));
                        user.numberofAppereances.set(jSUser.getLong("nr_foundIn"));
                        user.numberofPhotosTaken.set(jSUser.getLong("nr_pictures"));

                    } else {
                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                        }
            } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
            }

        }
        }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Login Error: " + error.getMessage());
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to login url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("id", ((Integer)targetuser_id).toString());

                    return params;
                }

            };


            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, "get_pictures");
    }


    /**
     * Parsing json reponse and passing the data to feed view list adapter
     * */
    private void parseJsonProfile(JSONObject response,int id) {
        try {
            JSONArray gArray = response.getJSONArray("pics");
            Log.d(TAG, "AICI: "+response.toString());
            for (int i = 0; i < gArray.length(); i++) {

                JSONObject feedObj = (JSONObject) gArray.get(i);
                if(user.getId() != id)
                {
                    if(feedObj.getString("visibility").equals("v"))
                    {
                        ProfileItem item = new ProfileItem();
                        item.setImageUrl(AppConfig.URL_SERVER+feedObj.getString("pic_name"));
                        Log.d(TAG, "Am link:"+AppConfig.URL_SERVER+feedObj.getString("pic_name"));
                        posts.add(item);
                    }
                }
                else
                {
                    ProfileItem item = new ProfileItem();
                    item.setImageUrl(AppConfig.URL_SERVER+feedObj.getString("pic_name"));
                    Log.d(TAG, "Am link:"+AppConfig.URL_SERVER+feedObj.getString("pic_name"));
                    posts.add(item);
                }
            }

            mAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            user.setName(user.getName());
            Log.d(TAG,user.getProfileImage());
            user.setProfileImage(user.getProfileImage());
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