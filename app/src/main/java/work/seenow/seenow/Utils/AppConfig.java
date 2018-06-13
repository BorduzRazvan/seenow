package work.seenow.seenow.Utils;

public class AppConfig {
    // Server user login url
    public static String URL_LOGIN = "http://7f071333.ngrok.io/login.php";

    // Server user register url
    public static String URL_REGISTER = "http://7f071333.ngrok.io/register.php";

    // Server feed url
    public static String URL_FEED = "https://api.androidhive.info/feed/feed.json";


    // Place for SeeNow Picture
    public static final String IMAGE_DIRECTORY_NAME = "SeenowPictures";

    // Server upload url
    private static final String ROOT_URL = "http://192.168.0.102/upload.php?apicall=";
    public static final String UPLOAD_URL = ROOT_URL + "uploadpic";
    public static final String GET_PICS_URL = ROOT_URL + "getpics";
}