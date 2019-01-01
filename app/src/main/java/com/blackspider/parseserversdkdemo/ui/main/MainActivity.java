package com.blackspider.parseserversdkdemo.ui.main;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.blackspider.parseserversdkdemo.R;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvData;

    private ParseUser user;
    private List<String> objIDList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvData = findViewById(R.id.txt_msg);

        user = getIntent().getParcelableExtra("user");

        Toast.makeText(this, "Welcome " + user.getUsername() + " : " + user.getEmail() , Toast.LENGTH_SHORT).show();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeNewData();
            }
        });

        //if(objIDList.size()>0) getDataById();
        getDataList();

        //if(user != null) unlinkUserFromFacebook(user);
    }

    private void unlinkUserFromFacebook(ParseUser user){
        ParseFacebookUtils.unlinkInBackground(user, new SaveCallback() {
            @Override
            public void done(ParseException ex) {
                if (ex == null) {
                    Log.d("MyApp", "The user is no longer associated with their Facebook account.");
                    Toast.makeText(MainActivity.this, "The user is no longer associated with their Facebook account.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == R.id.action_logout){
            ParseUser.logOut();
            finish();
            return true;
        }

        if(id == R.id.action_reset_password){
            resetPassword(user.getEmail());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void storeNewData() {
        final ParseObject askMeshTeam = new ParseObject("AskMeshTeam");
        askMeshTeam.put("team_name", "AskMeshTeam");
        askMeshTeam.put("total_dev", 6);
        askMeshTeam.put("pm", "Syed Ekram Uddin Emon");
        askMeshTeam.put("team_leader", "Aly Arman");
        askMeshTeam.put("server_guy", "Sumon");
        askMeshTeam.put("client_guy", "Arhan");
        askMeshTeam.put("designer_guy", "Nazma");
        askMeshTeam.put("qa_guy", "Al Imran");
        askMeshTeam.put("dev_running", true);

        //for saving data in the server
        askMeshTeam.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                String msg = "Failed: ";
                if(e == null){
                    msg = "Stored successfully: " + askMeshTeam.getObjectId();
                    objIDList.add(askMeshTeam.getObjectId());

                    getDataList();
                }
                else {
                    msg += e.getMessage();
                }


                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
        /*//for saving the data in the local database
        askMeshTeam.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                String msg = "Failed: ";
                if(e == null){
                    msg = "Stored successfully: " + askMeshTeam.getObjectId();
                    objIDList.add(askMeshTeam.getObjectId());

                    //getDataById();
                    getDataList();
                }
                else {
                    msg += e.getMessage();
                }


                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private void getDataById(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("AskMeshTeam");
        //add this line to get data from local database
        query.fromLocalDatastore();
        query.getInBackground(objIDList.get(objIDList.size()-1), new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    // object will be your game score
                    String data = "Team: " + object.getString("team_name")
                            + "\nTotal developer: " + object.getInt("total_dev")
                            + "\nProduct manager: " + object.getString("pm")
                            + "\nTeam leader: " + object.getString("team_leader")
                            + "\nServer guy: " + object.getString("server_guy")
                            + "\nClient guy: " + object.getString("client_guy")
                            + "\nDesigner guy: " + object.getString("designer_guy")
                            + "\nQA guy: " + object.getString("qa_guy")
                            + "\nDevelopment going on: " + object.getBoolean("dev_running");

                    tvData.append(data);
                } else {
                    Log.d(MainActivity.class.getSimpleName(), e.toString());
                    Toast.makeText(MainActivity.this, "Something's went wrong! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getDataList(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("AskMeshTeam");
        //add this line to get data from local database
        //query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    Log.d(MainActivity.class.getSimpleName(), "Retrieved " + objects.size() + " objects");
                    StringBuilder data = new StringBuilder();

                    for (ParseObject object : objects) {
                        data.append("Data ID: ").append(object.getObjectId())
                                .append("\nTeam: ").append(object.getString("team_name"))
                                .append("\nTotal developer: ").append(object.getInt("total_dev"))
                                .append("\nProduct manager: ").append(object.getString("pm"))
                                .append("\nTeam leader: ").append(object.getString("team_leader"))
                                .append("\nServer guy: ").append(object.getString("server_guy"))
                                .append("\nClient guy: ").append(object.getString("client_guy"))
                                .append("\nDesigner guy: ").append(object.getString("designer_guy"))
                                .append("\nQA guy: ").append(object.getString("qa_guy"))
                                .append("\nDevelopment going on: ").append(object.getBoolean("dev_running"))
                                .append("\n\n");

                        final ParseObject lastRead = new ParseObject("LastRead");
                        lastRead.put("date", System.currentTimeMillis());
                        lastRead.put("parent", object);
                        lastRead.saveInBackground();
                    }

                    tvData.setText(data);
                } else {
                    Log.d(MainActivity.class.getSimpleName(), "Error: " + e.getMessage());
                }
            }
        });
    }

    private void deleteData(final ParseObject parseObject){
        parseObject.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                String msg = "Failed: ";
                if(e == null){
                    msg = "Deleted successfully: " + parseObject.getObjectId();
                    objIDList.remove(parseObject.getObjectId());

                    getDataById();
                }
                else {
                    msg += e.getMessage();
                }


                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteLocalData(final ParseObject parseObject){
        parseObject.unpinInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                String msg = "Failed: ";
                if(e == null){
                    msg = "Deleted successfully: " + parseObject.getObjectId();
                    objIDList.remove(parseObject.getObjectId());

                    getDataById();
                }
                else {
                    msg += e.getMessage();
                }


                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword(String email){
        ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // An email was successfully sent with reset instructions.
                    Toast.makeText(MainActivity.this, "An email was successfully sent with reset instructions", Toast.LENGTH_SHORT).show();
                } else {
                    // Something went wrong. Look at the ParseException to see what's up.
                    Toast.makeText(MainActivity.this, "Something went wrong.: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
