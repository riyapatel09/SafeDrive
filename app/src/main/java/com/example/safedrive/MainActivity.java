package com.example.safedrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.safedrive.Helper.SDHelper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    RelativeLayout activityMain;
    EditText email,firstName,lastName,password,confirmPassword,mobileNo,vehicleType;
    String userFirstName,userLastName,userEmail,userPassword,userConfirmPassword,userMobileNo,userVehicleType;
    JsonObject user;
    String responseEmail;
    String result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bindViews();
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    private void bindViews()
    {
        firstName = (EditText) findViewById(R.id.first_name);
        lastName = (EditText) findViewById(R.id.last_name);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirm_password);
        mobileNo = (EditText) findViewById(R.id.mobile_no);
        vehicleType = (EditText) findViewById(R.id.vehicle_type);
    }
    public boolean validateInputData()
    {
        userFirstName = firstName.getText().toString();
        userLastName= lastName.getText().toString();
        userEmail=email.getText().toString();
        userPassword=password.getText().toString();
        userConfirmPassword=confirmPassword.getText().toString();
        userMobileNo=mobileNo.getText().toString();
        userVehicleType=vehicleType.getText().toString();

        boolean isAllOK = true;
        boolean isTemp = true;

        //First name can not be blank
        if (userFirstName.length() == 0)
        {
            isAllOK = false;
            firstName.setError(getString(R.string.blankerror));
        }

        //email
        if (userEmail.length() > 0)
        {
            String emailPattern = "[a-zA-z0-9.-_]+@[a-z]+\\.+[a-z]+";
            if(userEmail.matches(emailPattern)) {
                isTemp=true;
            } else
            {
                email.setError(getString(R.string.emailerror));
                isAllOK = false;
            }
        }
        else
        {
            isAllOK = false;
            email.setError(getString(R.string.blankerror));
        }

        //password
        if(userPassword.length()>0)
        {
            if (userPassword.length() >= 6 && userPassword.length() <= 20) {
                isTemp = true;
            } else {
                isAllOK = false;
                password.setError("Password length must be between 6 to 20");
            }
        }
        else
        {
            password.setError(getString(R.string.blankerror));
        }

        //confirm password
        if(userPassword.equals(userConfirmPassword) )
        {
            isTemp=true;
        }
        else
        {
            isAllOK=false;
            confirmPassword.setError(getString(R.string.confirmPassworderror));
        }

        if (userMobileNo.length() > 0)
        {
            String mobilePattern = "[1-9]{1}[0-9]{9}";
            if(userMobileNo.matches(mobilePattern))
            {
                isTemp=true;
            }
            else
            {
                mobileNo.setError(getString(R.string.mobileNoerror));
                isAllOK = false;
            }
        }
        else
        {
            isAllOK = false;
            mobileNo.setError(getString(R.string.blankerror));
        }

        return isAllOK;
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

    public void onRegisterClick(View view) {

        if(validateInputData())
        {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://"+ SDHelper.getNetworkIp()+"/signUp";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equals("false")) {
                                email.setError("Email already registered");
                            }
                            else {
//                                user = (new JsonParser()).parse(response).getAsJsonObject();
                                result = (new JsonParser()).parse(response).getAsString();
//                                responseEmail = user.get("email").getAsString();
                                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
//                                responseId = user.get("id").getAsInt();
//                                isLoggedIn = true;
                                //Toast.makeText(RegistrationActivity.this, email, Toast.LENGTH_LONG).show();
//                                AppStorageAgent.setSharedStoreString("responseEmail", responseEmail, getApplicationContext());
//                                AppStorageAgent.setSharedStoreInt("responseId", responseId, getApplicationContext());
//                                AppStorageAgent.setSharedStoreBoolean("isLoggedIn", isLoggedIn, getApplicationContext());
//                                RegistrationActivity.this.finish();
//                                Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
                                // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                startActivity(intent);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("user[firstName]", userFirstName);
                    params.put("user[lastName]", userLastName);
                    params.put("user[emailID]", userEmail);
                    params.put("user[password]", userPassword);
                    params.put("user[mobileNumber]", userMobileNo);
                    params.put("user[vehicleType]", userVehicleType);
                    return params;
                }
            };

            queue.add(stringRequest);
        }
    }

    public void onGoToMapClick(View view)
    {
        Intent intent = new Intent(this,Main2Activity.class);
        startActivity(intent);
    }

}
