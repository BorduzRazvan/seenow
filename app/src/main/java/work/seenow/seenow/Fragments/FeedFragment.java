package work.seenow.seenow.Fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;


import work.seenow.seenow.LoginActivity;
import work.seenow.seenow.MainActivity;
import work.seenow.seenow.R;
import work.seenow.seenow.Utils.AppConfig;
import work.seenow.seenow.Utils.AppController;
import work.seenow.seenow.Utils.FeedItem;
import work.seenow.seenow.Utils.FeedListAdapter;
import work.seenow.seenow.Utils.User;

import static work.seenow.seenow.Utils.AppConfig.URL_FEED;

public class FeedFragment extends Fragment  {
    private static final String TAG = FeedFragment.class.getSimpleName();
    private static ListView listView;
    private static FeedListAdapter listAdapter;
    private static List<FeedItem> feedItems;
    private static User user;
    public FeedFragment() {
        // Required empty public constructor
    }

    public static FeedFragment newInstance(User user) {
        FeedFragment fragment = new FeedFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",user);
        fragment.setArguments(bundle);

        return fragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    /**
     * Parsing json reponse and passing the data to feed view list adapter
     * */
    private static void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");
            Log.d(TAG, "AICI: "+response.toString());
            for (int i = 0; i < feedArray.length(); i++) {

                JSONObject feedObj = (JSONObject) feedArray.get(i);
                FeedItem item;
                if(feedObj.getString("fullname").isEmpty()) {
                    item = new FeedItem(feedObj.getInt("id"), feedObj.getString("author_name"),
                            feedObj.getString(" description"),
                            "none", feedObj.getString("picture_name"),
                            feedObj.getString("profile_pic"),
                            feedObj.getString("posted_at"), feedObj.getInt("LIKES"),
                            -1, feedObj.getInt("LIKED"), feedObj.getInt("author_id"),-1);
                }
                else
                {
                    item = new FeedItem(feedObj.getInt("id"), feedObj.getString("author_name"),
                                feedObj.getString("description"),
                                feedObj.getString("fullname"), feedObj.getString("picture_name"),
                                feedObj.getString("profile_pic"),
                                feedObj.getString("posted_at"), feedObj.getInt("LIKES"),
                                feedObj.getInt("trust"), feedObj.getInt("LIKED"),feedObj.getInt("author_id"),feedObj.getInt("founduser_id"));
                }
                Log.d(TAG,"Sunt aici, post_pic: "+item.getPicture_url()+" si profile_pic:"+item.getProfile_picture_url());
                feedItems.add(item);
            }

            // notify data changes to list adapater
            listAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        user = (User) getArguments().getSerializable("user");
        View view = inflater.inflate(R.layout.activity_feed, container, false);

        listView = (ListView) view.findViewById(R.id.list);
        feedItems = new ArrayList<FeedItem>();

        listAdapter = new FeedListAdapter(getActivity(), feedItems, user);
        listView.setAdapter(listAdapter);


        // These two lines not needed,
        // just to get the look of facebook (changing background color & hiding the icon)

        // We first check for cached request
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Entry entry = cache.get(URL_FEED);
        if (entry != null) {
            // fetch the data from cache
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    parseJsonFeed(new JSONObject(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else {
            getFeeds();
        }

        return view;
    }


    public static void getFeeds(){
        {
            feedItems.clear();
            StringRequest strReq = new StringRequest(Method.POST,
                    AppConfig.URL_FEED, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Login Response: " + response.toString());

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

//                     Check for error node in json
                        if (!error) {
                            parseJsonFeed(jObj);

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
                    params.put("id", ((Integer)user.getId()).toString());

                    return params;
                }

            };


            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, "get_feed");

        }
    }
}