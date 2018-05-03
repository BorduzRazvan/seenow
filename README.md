# seenow


Resources used into project: 

Register and Login
https://www.androidhive.info/2012/01/android-login-and-registration-with-php-mysql-and-sqlite/


Country Picker
https://github.com/hbb20/CountryCodePickerProject

Password strength
https://stackoverflow.com/questions/5142103/regex-to-validate-password-strength/5142164#5142164

Email regex
http://www.ex-parrot.com/pdw/Mail-RFC822-Address.html
https://stackoverflow.com/questions/201323/how-to-validate-an-email-address-using-a-regular-expression

Fragments, TabLayout, ViewPager
https://github.com/codepath/android_guides/wiki/ViewPager-with-FragmentPagerAdapter
https://android.jlelse.eu/tablayout-and-viewpager-in-your-android-app-738b8840c38a

Feed using Volley and ListView
https://www.androidhive.info/2014/06/android-facebook-like-custom-listview-feed-using-volley/
https://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
https://stackoverflow.com/questions/28259138/how-to-use-listview-in-android-fragment


MySql
_____


/** Creating Database **/
create database SeenowDB  
/** Select the Database **/
use android_api




 create table users (
    id int(11) primary key auto_increment,
    fullname varchar(50) not null,
    email varchar(100) not null unique,
    encrypted_password varchar(80) not null,
    salt varchar(10) not null,
    created_at datetime,
    gender varchar(1) not null,
    country varchar(30) not null,
    socialLoggedIn int(11) null
    );
 
 
 
create table feeds (
    id int(11) primary key auto_increment,
    author_id int(11) not null,
    foundUser_id int(11),
    picture_id int(11) not null,
    description varchar(255),
    posted_at datetime,
    FOREIGN KEY (author_id) REFERENCES users(id)
);
 
 
 
create table likedFeed (
    id int(11) not null,
    user_id int(11) not null,
    FOREIGN KEY (id) REFERENCES feeds(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);