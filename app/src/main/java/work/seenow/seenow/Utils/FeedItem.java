package work.seenow.seenow.Utils;

public class FeedItem {
    private int id, likes, trustLevel;
    private boolean isLiked;
    private String author_name, description, found_user, picture_url, profile_picture_url, posted_at;


    public FeedItem(int id, String author_name, String description, String found_user, String picture_name,
                    String profile_picture_name, String posted_at, int likes, int trustLevel, int liked){
        this.id = id;
        this.author_name = author_name;
        this.description= description;
        this.found_user= found_user;
        this.picture_url= AppConfig.URL_SERVER + picture_name;
        this.profile_picture_url = AppConfig.URL_SERVER + profile_picture_name;
        this.posted_at = posted_at;
        this.likes = likes;
        this.isLiked = (liked == 1);
        this.trustLevel = trustLevel;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public int getLikes() { return likes; }

    public int getTrustLevel() { return trustLevel; }

    public void setTrustLevel(int trustLevel) { this.trustLevel = trustLevel; }

    public boolean isLiked() { return isLiked; }

    public void toggleLike() { this.isLiked = !this.isLiked; }

    public String getAuthor_name() { return author_name; }

    public void setAuthor_name(String author_name) { this.author_name = author_name;}

    public String getDescription(){ return description; }

    public void setDescription(String description) { this.description = description; }

    public String getFound_user() { return found_user;}

    public void setLikes(int likes){ this.likes = likes; }
    public String getPicture_url() { return picture_url; }


    public String getProfile_picture_url() { return profile_picture_url;  }


    public String getPosted_at() { return posted_at;}

    public void setPosted_at(String posted_at){ this.posted_at = posted_at;}

}