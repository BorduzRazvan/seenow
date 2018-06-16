package work.seenow.seenow.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import work.seenow.seenow.Fragments.ProfileFragment;
import work.seenow.seenow.R;

public class FeedListAdapter extends BaseAdapter {

    private static final String TAG = FeedListAdapter.class.getSimpleName();
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    private User user;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public FeedListAdapter(Activity activity, List<FeedItem> feedItems, User user) {
        this.activity = activity;
        this.feedItems = feedItems;
        this.user = user;
    }

    @Override
    public int getCount() {
        return feedItems.size();
    }

    @Override
    public Object getItem(int location) {
        return feedItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.feed_item, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        TextView author_name = (TextView) convertView.findViewById(R.id.author_name);
        TextView posted_at = (TextView) convertView.findViewById(R.id.posted_at);
        TextView founduser_name = (TextView) convertView.findViewById(R.id.fountuser_name);
        TextView trustLevel = (TextView) convertView.findViewById(R.id.trustLevel);
        TextView description = (TextView) convertView.findViewById(R.id.description);
        final TextView likes = (TextView) convertView.findViewById(R.id.numberLikes);
        NetworkImageView profilePic = (NetworkImageView) convertView.findViewById(R.id.profilePic);
        FeedImageView feedImageView = (FeedImageView) convertView
                .findViewById(R.id.feedImage1);
        final Button likeButton = (Button) convertView.findViewById(R.id.likeButton);
        Button shareButton = (Button) convertView.findViewById(R.id.likeButton);
        final FeedItem item = feedItems.get(position);
        FeedImageView feedImageView1= (FeedImageView) convertView.findViewById(R.id.feedImage1);
        /** Define if like buttons is pressed */
        if(item.isLiked())
        {
            likeButton.setTextColor(Color.BLUE);
        }
        else
        {
            likeButton.setTextColor(Color.BLACK);
        }

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"ID_USER: "+user.getId()+" si targetuser_id: "+((Integer)item.getAuthor_id()).toString());
                Intent i = new Intent();
                i.putExtra("target_user", item.getAuthor_id());
                Intent intent = new Intent();
                intent.setAction("profile");
                intent.putExtra("targetuser_id",item.getAuthor_id());
                activity.sendBroadcast(intent);
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"ID_USER: "+user.getId()+" si id_feed: "+item.getId());
                item.toggleLike();
                /** Define if like buttons is pressed */
                if(item.isLiked())
                {
                    action("addLike",user.getId(),item.getId());
                    item.setLikes(item.getLikes() +1);
                    likeButton.setTextColor(Color.BLUE);
                }
                else
                {
                    action("removeLike",user.getId(),item.getId());
                    item.setLikes(item.getLikes() -1);
                    likeButton.setTextColor(Color.BLACK);
                }
                likes.setText(((Integer)item.getLikes()).toString()+" likes");
            }
        });

        author_name.setText(item.getAuthor_name());
        posted_at.setText(item.getPosted_at());
        if(item.getFound_user().equals("none")){
            founduser_name.setVisibility(View.INVISIBLE);
            trustLevel.setVisibility(View.INVISIBLE);
        }
        else {
            founduser_name.setVisibility(View.VISIBLE);
            trustLevel.setVisibility(View.VISIBLE);
            founduser_name.setText("Found: "+item.getFound_user());
            trustLevel.setText("TrustLevel: "+((Integer)item.getTrustLevel()).toString());
        }

        description.setText(item.getDescription());
        likes.setText(((Integer)item.getLikes()).toString()+" likes");

        // user profile pic
        profilePic.setImageUrl(item.getProfile_picture_url(), imageLoader);

        // Feed image
            feedImageView.setImageUrl(item.getPicture_url(), imageLoader);
            feedImageView.setVisibility(View.VISIBLE);
            feedImageView
                    .setResponseObserver(new FeedImageView.ResponseObserver() {
                        @Override
                        public void onError() {
                        }

                        @Override
                        public void onSuccess() {
                        }
                    });

        return convertView;
    }


    private void action(final String action_type, final int userId, final int feedId)
    {
        {
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_ACTIONS, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Login Response: " + response.toString());

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

//                     Check for error node in json
                        if (!error) {

                        } else {

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
                    params.put("userId", ((Integer)userId).toString());
                    params.put("feedId", ((Integer)feedId).toString());
                    params.put("action_type", action_type);

                    return params;
                }

            };


            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, "action");

        }
    }



}