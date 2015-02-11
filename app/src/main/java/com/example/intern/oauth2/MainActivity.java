package com.example.intern.oauth2;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    Button select,api_call;
    String[] avail_accounts;
    private AccountManager mAccountManager;
    ListView list;
    ArrayAdapter<String> adapter;
    SharedPreferences pref;

    URLShortener jParser;
    JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        select = (Button)findViewById(R.id.select_button);
        api_call = (Button)findViewById(R.id.apicall_button);
        avail_accounts = getAccountNames();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,avail_accounts );
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        select.setOnClickListener(new View.OnClickListener() {
            Dialog accountDialog;
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (avail_accounts.length != 0){
                    accountDialog = new Dialog(MainActivity.this);
                    accountDialog.setContentView(R.layout.accounts_dialog);
                    accountDialog.setTitle("Select Google Account");
                    list = (ListView)accountDialog.findViewById(R.id.list);
                    list.setAdapter(adapter);
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            SharedPreferences.Editor edit = pref.edit();
                            //Storing Data using SharedPreferences
                            edit.putString("Email", avail_accounts[position]);
                            edit.commit();
                            new Authenticate().execute();
                            accountDialog.cancel();
                        }
                    });
                    accountDialog.show();
                }else{
                    Toast.makeText(getApplicationContext(), "No accounts found, Add a Account and Continue.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        api_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                new URLShort().execute();
            }
        });
    }
    private String[] getAccountNames() {
        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }
    private class Authenticate extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;
        String mEmail;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Authenticating....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            mEmail= pref.getString("Email", "");
            pDialog.show();
        }
        @Override
        protected void onPostExecute(String token) {
            pDialog.dismiss();
            if(token != null){
                SharedPreferences.Editor edit = pref.edit();
                //Storing Access Token using SharedPreferences
                edit.putString("Access Token", token);
                edit.commit();
                Log.i("Token", "Access Token retrieved:" + token);
                Toast.makeText(getApplicationContext(),"Access Token is " +token, Toast.LENGTH_SHORT).show();
                select.setText(pref.getString("Email", "")+" is Authenticated");
            }
        }
        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(
                        MainActivity.this,
                        mEmail,
                        "oauth2:https://www.googleapis.com/auth/urlshortener");
            } catch (IOException transientEx) {
                // Network or server error, try later
                Log.e("IOException", transientEx.toString());
            } catch (UserRecoverableAuthException e) {
                // Recover (with e.getIntent())
                startActivityForResult(e.getIntent(), 1001);
                Log.e("AuthException", e.toString());
            } catch (GoogleAuthException authEx) {
                // The call is not ever expected to succeed
                // assuming you have already verified that
                // Google Play services is installed.
                Log.e("GoogleAuthException", authEx.toString());
            }
            return token;
        }
    };
    private class URLShort extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        String Token,LongUrl;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Contacting Google Servers ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Token = pref.getString("Access Token", "");
            Toast.makeText(getApplicationContext(),"token   "+Token,Toast.LENGTH_SHORT).show();
            LongUrl = "128.199.224.11";
            pDialog.show();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            jParser = new URLShortener();
            json = jParser.getJSONFromUrl("https://www.googleapis.com/urlshortener/v1/url?access_token=" + Token,LongUrl);
            return json;
        }
        String shortUrl;
        @Override
        protected void onPostExecute(JSONObject j) {
            pDialog.dismiss();
            //try {
                if (json != null){
                    //shortUrl = json.getString("id");
                    //shortUrl = json.toString();
                    Toast.makeText(getApplicationContext(), ""+json.toString(), Toast.LENGTH_LONG).show();
                    pDialog.dismiss();
                }else{
                    pDialog.dismiss();
                }
            /*} catch (JSONException e) {
                e.printStackTrace();
            }*/
        }
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

        return super.onOptionsItemSelected(item);
    }
}
