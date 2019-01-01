package com.blackspider;
/*
 *  ****************************************************************************
 *  * Created by : Arhan Ashik on 6/28/2018 at 6:42 PM.
 *  * Email : ashik.pstu.cse@gmail.com
 *  *
 *  * Last edited by : Arhan Ashik on 6/28/2018.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.blackspider.parseserversdkdemo.R;
import com.blackspider.parseserversdkdemo.data.local.appconst.AppConst;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class AppController extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //ParseObject.registerSubclass(AskMeshTeam.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .enableLocalDataStore() //Enable local data storage for parse if needed
                .applicationId("AskMeshServer")
                //.clientKey("YOUR_CLIENT_KEY")
                .server("http://192.168.2.181:1337/parse/")
                .build()
        );

        ParseFacebookUtils.initialize(this);
        //ParseTwitterUtils.initialize(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));

        //initTwitter();
    }

//    private void initTwitter(){
//        TwitterConfig config = new TwitterConfig.Builder(this)
//                .logger(new DefaultLogger(Log.DEBUG))//enable logging when app is in debug mode
//                .twitterAuthConfig(new TwitterAuthConfig(getResources().getString(R.string.twitter_consumer_key), getResources().getString(R.string.twitter_consumer_secret)))//pass the created app Consumer KEY and Secret also called API Key and Secret
//                .debug(true)//enable debug mode
//                .build();
//
//        //finally initialize twitter with created configs
//        Twitter.initialize(config);
//    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
