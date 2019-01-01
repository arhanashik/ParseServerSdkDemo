package com.blackspider.parseserversdkdemo.ui.signup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blackspider.parseserversdkdemo.R;
import com.blackspider.parseserversdkdemo.data.local.appconst.AppConst;
import com.blackspider.parseserversdkdemo.ui.main.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.RequestToken;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;


    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    private static final String PUBLIC_PROFILE = "public_profile ";
    private static final String EMAIL = "email";

    // UI references.
    private EditText mUsernameView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    //private TwitterLoginButton twitterLoginButton;
    private Twitter twitter;
    private RequestToken twitterRequestToken;

    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton googleSignInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ParseUser user = ParseUser.getCurrentUser();

        //session check
        if(user != null) goToNextActivity(user);

        // Set up the login form.
        mUsernameView = findViewById(R.id.username);

        mEmailView = findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptSignIn();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignIn();
            }
        });

        Button mSignUpButton = findViewById(R.id.email_sign_up_button);
        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        findViewById(R.id.anonymous_sign_in_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                useAnonymously();
            }
        });

        findViewById(R.id.facebook_sign_in_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLogin();
            }
        });

        findViewById(R.id.twitter_sign_in_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //twitterLogin();
                new GetTwitterTokenTask(LoginActivity.this).execute();
            }
        });

        configGoogleSignIn();
        googleSignInBtn = findViewById(R.id.sign_in_button);
        googleSignInBtn.setSize(SignInButton.SIZE_WIDE);

        googleSignInBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        printHashKey();

        //initDefaultTwitterLoginBtn();
    }

    public void callBackDataFromAsyncTask(User user) {
        Toast.makeText(this, "Id: " + user.getId()  + ", Name: " + user.getName(), Toast.LENGTH_SHORT).show();
        parseContinueWithTwitter(user);

        //loading User Avatar by Picasso
//        Picasso.with(this)
//                .load(user.getBiggerProfileImageURL())
//                .placeholder(R.mipmap.ic_launcher)
//                .error(R.mipmap.ic_launcher)
//                .into(avatar);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == AppConst.REQUEST_CODE_GOOGLE_LOGIN){
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }else {
            ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        }
        //if(requestCode == AppConst.REQUEST_CODE_FACEBOOK_LOGIN)

        //else twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    private void useAnonymously(){
        showProgress(true);
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                showProgress(false);
                if (e != null) {
                    Log.d("MyApp", "Anonymous login failed.");
                    Toast.makeText(LoginActivity.this, "Anonymous login failed.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("MyApp", "Anonymous user logged in.");
                    goToNextActivity(user);
                }
            }
        });
    }

    private void attemptSignUp() {
        // Reset errors.
        mUsernameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            parseSignUp(username, password, email);
        }
    }

    private void attemptSignIn() {
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            parseSignIn(username, password);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    private void facebookLogin(){
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, Arrays.asList(PUBLIC_PROFILE, EMAIL), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                    Toast.makeText(LoginActivity.this, "Failed to authenticate using facebook!", Toast.LENGTH_SHORT).show();
                } else if (user.isNew()) {
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                    requestAdditionalFacebookPermissions(user);
                } else {
                    Log.d("MyApp", "User logged in through Facebook!");
                    //Toast.makeText(LoginActivity.this, "Sign in successful using facebook!", Toast.LENGTH_SHORT).show();
                    //goToNextActivity(user);
                    requestAdditionalFacebookPermissions(user);
                }
            }
        });
    }

    private void requestAdditionalFacebookPermissions(final ParseUser user){
        ParseFacebookUtils.linkWithReadPermissionsInBackground(user, this, Arrays.asList(EMAIL), new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Toast.makeText(LoginActivity.this, "Sign up successful using facebook!", Toast.LENGTH_SHORT).show();
                    goToNextActivity(user);
                } else {
                    Toast.makeText(LoginActivity.this, "Additional permission not granted!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void linkOldUserToFacebook(final ParseUser user){
        if (!ParseFacebookUtils.isLinked(user)) {
            ParseFacebookUtils.linkWithReadPermissionsInBackground(user, this, null, new SaveCallback() {
                @Override
                public void done(ParseException ex) {
                    if (ParseFacebookUtils.isLinked(user)) {
                        Log.d("MyApp", "Woohoo, user logged in with Facebook!");
                        Toast.makeText(LoginActivity.this, "Woohoo, user logged in with Facebook!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void unlinkUserFromFacebook(ParseUser user){
        ParseFacebookUtils.unlinkInBackground(user, new SaveCallback() {
            @Override
            public void done(ParseException ex) {
                if (ex == null) {
                    Log.d("MyApp", "The user is no longer associated with their Facebook account.");
                    Toast.makeText(LoginActivity.this, "The user is no longer associated with their Facebook account.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    private void initDefaultTwitterLoginBtn(){
//        twitterLoginButton = findViewById(R.id.default_twitter_login_button);
//        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
//            @Override
//            public void success(Result<TwitterSession> result) {
//                // Do something with result, which provides a TwitterSession for making API calls
//                Toast.makeText(LoginActivity.this, "Twitter login: " + result.response, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void failure(TwitterException exception) {
//                // Do something on failure
//                Toast.makeText(LoginActivity.this, "Twitter login: " + exception, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void requestTwitterEmail(TwitterSession session){
//        TwitterAuthClient authClient = new TwitterAuthClient();
//        authClient.requestEmail(session, new Callback<String>() {
//            @Override
//            public void success(Result<String> result) {
//                // Do something with the result, which provides the email address
//            }
//
//            @Override
//            public void failure(TwitterException exception) {
//                // Do something on failure
//            }
//        });
//    }
//
//    private void twitterLogin(){
//        ParseTwitterUtils.logIn(this, new LogInCallback() {
//            @Override
//            public void done(ParseUser user, ParseException err) {
//                if (user == null) {
//                    Log.d("MyApp", "Uh oh. The user cancelled the Twitter login. " + err.toString());
//                    Toast.makeText(LoginActivity.this, err.getMessage(), Toast.LENGTH_SHORT).show();
//                } else if (user.isNew()) {
//                    Log.d("MyApp", "User signed up and logged in through Twitter!");
//                    Toast.makeText(LoginActivity.this, "User signed up and logged in through Twitter!", Toast.LENGTH_SHORT).show();
//                    goToNextActivity(user);
//                } else {
//                    Log.d("MyApp", "User logged in through Twitter!");
//                    Toast.makeText(LoginActivity.this, "User logged in through Twitter!", Toast.LENGTH_SHORT).show();
//                    goToNextActivity(user);
//                }
//            }
//        });
//    }
//
//    private void linkUserTOTwitter(final ParseUser user){
//        if (!ParseTwitterUtils.isLinked(user)) {
//            ParseTwitterUtils.link(user, this, new SaveCallback() {
//                @Override
//                public void done(ParseException ex) {
//                    if (ParseTwitterUtils.isLinked(user)) {
//                        Log.d("MyApp", "Woohoo, user logged in with Twitter!");
//                    }
//                }
//            });
//        }
//    }
//
//    private void unlinkUserFromTwitter(final ParseUser user){
//        ParseTwitterUtils.unlinkInBackground(user, new SaveCallback() {
//            @Override
//            public void done(ParseException ex) {
//                if (ex == null) {
//                    Log.d("MyApp", "The user is no longer associated with their Twitter account.");
//                }
//            }
//        });
//    }

    private void parseContinueWithTwitter(final User twitterUser) {
        showProgress(true);
        ParseUser.logInInBackground(String.valueOf(twitterUser.getId()), String.valueOf(twitterUser.getId()), new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    showProgress(false);
                    goToNextActivity(user);
                } else {
                    parseSignUp(String.valueOf(twitterUser.getId()), String.valueOf(twitterUser.getId()), "");
                }
            }
        });
    }

    private void configGoogleSignIn(){
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, AppConst.REQUEST_CODE_GOOGLE_LOGIN);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Toast.makeText(this, "Welcome " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
            parseContinueWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(LoginActivity.class.getSimpleName(), "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToNextActivity(ParseUser user){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }

    private void parseSignUp(String username, String password, String email) {
        final ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        if(!TextUtils.isEmpty(email)) user.setEmail(email);

        // other fields can be set just like with ParseObject
        user.put("phone", "650-253-0000");

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                showProgress(false);
                if (e == null) {
                    Toast.makeText(LoginActivity.this, "Sign up successful!" , Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "Something's wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void parseSignIn(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    goToNextActivity(user);
                } else {
                    Toast.makeText(LoginActivity.this, "Something's wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void parseContinueWithGoogle(final GoogleSignInAccount account) {
        showProgress(true);
        ParseUser.logInInBackground(account.getEmail(), account.getId(), new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    showProgress(false);
                    goToNextActivity(user);
                } else {
                    parseSignUp(account.getEmail(), account.getId(), "");
                }
            }
        });
    }

    public void printHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.d("Facebook hash key", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.d("Facebook hash key", "printHashKey()", e);
        } catch (Exception e) {
            Log.d("Facebook hash key", "printHashKey()", e);
        }
    }
}

