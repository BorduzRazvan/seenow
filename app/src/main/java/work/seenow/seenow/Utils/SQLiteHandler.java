package work.seenow.seenow.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 6;

    // Database Name
    private static final String DATABASE_NAME = "seenow_db";

    // Login table name
    private static final String TABLE_USER = "user";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "fullname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_PIC = "profilePicture";
    private static final String KEY_COUNTRY  = "country";
    private static final String KEY_POINTS = "points";
    private static final String KEY_USE_REC = "use_Recognizer";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_NR_PICTURES = "nr_pictures";
    private static final String KEY_NR_friends = "nr_friends";
    private static final String KEY_NR_foundIn = "nr_foundIn";
    private static final String KEY_CREATED_AT = "created_at";


    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_PROFILE_PIC + " TEXT,"
                + KEY_BIRTHDAY + " TEXT," + KEY_COUNTRY + " TEXT, "
                + KEY_GENDER + " TEXT," + KEY_POINTS+ " INTEGER, "
                + KEY_USE_REC + " TEXT," + KEY_NR_PICTURES + " INTEGER, "
                + KEY_NR_foundIn +" INTEGER, "+ KEY_NR_friends + " INTEGER, "
                + KEY_CREATED_AT +" TEXT)";
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(User u) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, u.getName());
        values.put(KEY_EMAIL, u.getEmail());
        values.put(KEY_BIRTHDAY, u.getBirthday());
        values.put(KEY_PROFILE_PIC, u.getProfileImage());
        values.put(KEY_COUNTRY, u.getCountry());
        values.put(KEY_POINTS, u.getnumberofPoints().get());
        values.put(KEY_USE_REC, u.getUseRecognizer());
        values.put(KEY_GENDER, u.getGender());
        values.put(KEY_ID, u.getId());
        values.put(KEY_NR_foundIn, u.numberofAppereances.get());
        values.put(KEY_NR_PICTURES, u.numberofPhotosTaken.get());
        values.put(KEY_NR_friends, u.numberofFriends.get());

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public User getUserDetails() {
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {


            User user = new User(cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2),(cursor.getString(3)).substring(AppConfig.URL_SERVER.length() -1),
                    cursor.getString(4), cursor.getString(5),
                    cursor.getString(6), cursor.getInt(7),
                    cursor.getString(8),cursor.getString(12));
                user.numberofAppereances.set(cursor.getLong(10));
                user.numberofPhotosTaken.set(cursor.getLong(9));
                user.numberofFriends.set(cursor.getLong(11));
            return user;
        }else {
            return null;
        }
    }

    public User getUserDetails(int id) {
            String selectQuery = "SELECT  * FROM " + TABLE_USER +"WHERE "+KEY_ID +" = "+id;

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            // Move to first row
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                User user = new User(cursor.getInt(0), cursor.getString(1),
                        cursor.getString(2),(cursor.getString(3)).substring(AppConfig.URL_SERVER.length() -1),
                        cursor.getString(4), cursor.getString(5),
                        cursor.getString(6), cursor.getInt(7),
                        cursor.getString(8),cursor.getString(12));
                user.numberofAppereances.set(cursor.getLong(10));
                user.numberofPhotosTaken.set(cursor.getLong(9));
                user.numberofFriends.set(cursor.getLong(11));
                return user;
            }else {
                return null;
            }
        }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

}