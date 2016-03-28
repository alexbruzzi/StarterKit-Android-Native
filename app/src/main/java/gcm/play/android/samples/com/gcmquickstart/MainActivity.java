/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gcm.play.android.samples.com.gcmquickstart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;

import io.swagger.client.api.*;
import io.swagger.client.model.*;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private boolean isReceiverRegistered;

    Button appInitButton, appLoginButton, appLogoutButton, pageviewButton, productPageviewButton;
    TextView responseText;

    PhoneDetails phoneDetails;
    // GPSTracker class
    GPSTracker gps;
    private float latitude = 0.0f;
    private float longitude = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);

        appInitButton = (Button) findViewById(R.id.app_init);
        appLoginButton = (Button) findViewById(R.id.app_login);
        appLogoutButton = (Button) findViewById(R.id.app_logout);
        pageviewButton = (Button) findViewById(R.id.page_view);
        productPageviewButton = (Button) findViewById(R.id.productpage_view);
        responseText = (TextView) findViewById(R.id.responseTextview);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };

        mInformationTextView = (TextView) findViewById(R.id.informationTextView);

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        // create class object
        gps = new GPSTracker(MainActivity.this);
        // check if GPS enabled
        if(gps.canGetLocation()) {
            latitude = (float)gps.getLatitude();
            longitude = (float)gps.getLongitude();
        } else {
            gps.showSettingsAlert();
        }

        phoneDetails = new PhoneDetails();
        phoneDetails.setDeviceId(Build.ID);
        phoneDetails.setLatitude(latitude);
        phoneDetails.setLongitude(longitude);
        phoneDetails.setManufacturer(Build.MANUFACTURER);
        phoneDetails.setModel(Build.MODEL);

        appInitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            // Perform action on click
            AppinitCall appinit = new AppinitCall();
            String[] param = new String[]{};
            appinit.execute(param);
            }
        });

        appLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            // Perform action on click
            ApploginCall beaconCall = new ApploginCall();
            String[] param = new String[]{};
            beaconCall.execute(param);
            }
        });

        appLogoutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            // Perform action on click
            ApplogoutCall beaconCall = new ApplogoutCall();
            String[] param = new String[]{};
            beaconCall.execute(param);
            }
        });

        pageviewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            // Perform action on click
            AppPageViewCall beaconCall = new AppPageViewCall();
            String[] param = new String[]{};
            beaconCall.execute(param);
            }
        });

        productPageviewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            // Perform action on click
            AppProductPageViewCall beaconCall = new AppProductPageViewCall();
            String[] param = new String[]{};
            beaconCall.execute(param);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        AppinitCall appinit = new AppinitCall();
        String[] param = new String[]{};
        appinit.execute(param);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    class AppinitCall extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                EventsApi eventsApi = new EventsApi();
                eventsApi.addHeader("Content-Type","application/json");
                eventsApi.addHeader("Accept","application/json");
                eventsApi.addHeader("apikey", "25745dd7d6754297a0883f4ee3201982");

                eventsApi.setBasePath("http://192.168.0.106:8000");

                try {
                    Message message = new Message();
                    message.setUserId(2736482);
                    message.setPhoneDetails(phoneDetails);

                    BeaconResponse response = eventsApi.eventsAppInitPost(message);
                    return response.getEventId().toString();
                } catch (Exception e) {
                    return e.toString();
                }
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String response) {
            Log.d("EventId",response);
            responseText.setText(response);
        }
    }

    class ApploginCall extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                EventsApi eventsApi = new EventsApi();
                eventsApi.addHeader("Content-Type","application/json");
                eventsApi.addHeader("Accept","application/json");
                eventsApi.addHeader("apikey", "25745dd7d6754297a0883f4ee3201982");

                eventsApi.setBasePath("http://192.168.0.106:8000");

                try {
                    Message message = new Message();
                    message.setUserId(2736482);
                    message.setPhoneDetails(phoneDetails);

                    BeaconResponse response = eventsApi.eventsAppLoginPost(message);
                    return response.getEventId().toString();
                } catch (Exception e) {
                    return e.toString();
                }
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String response) {
            Log.d("EventId",response);
            responseText.setText(response);
        }
    }

    class ApplogoutCall extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                EventsApi eventsApi = new EventsApi();
                eventsApi.addHeader("Content-Type","application/json");
                eventsApi.addHeader("Accept","application/json");
                eventsApi.addHeader("apikey", "25745dd7d6754297a0883f4ee3201982");

                eventsApi.setBasePath("http://192.168.0.106:8000");

                try {
                    Message message = new Message();
                    message.setUserId(2736482);
                    message.setPhoneDetails(phoneDetails);

                    BeaconResponse response = eventsApi.eventsAppLogoutPost(message);
                    return response.getEventId().toString();
                } catch (Exception e) {
                    return e.toString();
                }
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String response) {
            Log.d("EventId",response);
            responseText.setText(response);
        }
    }

    class AppPageViewCall extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                EventsApi eventsApi = new EventsApi();
                eventsApi.addHeader("Content-Type","application/json");
                eventsApi.addHeader("Accept","application/json");
                eventsApi.addHeader("apikey", "25745dd7d6754297a0883f4ee3201982");

                eventsApi.setBasePath("http://192.168.0.106:8000");

                try {
                    PageViewMessage message = new PageViewMessage();
                    message.setUserId(2736482);
                    message.setPhoneDetails(phoneDetails);

                    message.setRouteUrl("/Home/DealsOfTheDay/34");

                    List<String> cat = new ArrayList<String>();
                    cat.add("handbags");
                    cat.add("indian");

                    message.setCategories(cat);

                    List<String> tags = new ArrayList<String>();
                    tags.add("handbags");
                    tags.add("indian");
                    message.setTags(tags);

                    BeaconResponse response = eventsApi.eventsPageViewPost(message);
                    return response.getEventId().toString();
                } catch (Exception e) {
                    return e.toString();
                }
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String response) {
            Log.d("EventId",response);
            responseText.setText(response);
        }
    }

    class AppProductPageViewCall extends AsyncTask<String, Void, String> {
        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                EventsApi eventsApi = new EventsApi();
                eventsApi.addHeader("Content-Type","application/json");
                eventsApi.addHeader("Accept","application/json");
                eventsApi.addHeader("apikey", "25745dd7d6754297a0883f4ee3201982");

                eventsApi.setBasePath("http://192.168.0.106:8000");

                try {
                    ProductPageViewMessage message = new ProductPageViewMessage();
                    message.setUserId(2736482);
                    message.setPhoneDetails(phoneDetails);
                    message.setRouteUrl("/Home/DealsOfTheDay/34");

                    List<String> cat = new ArrayList<String>();
                    cat.add("handbags");
                    cat.add("indian");

                    message.setCategories(cat);

                    List<String> tags = new ArrayList<String>();
                    tags.add("handbags");
                    tags.add("indian");

                    message.setTags(tags);
                    message.setPrice(99.99f);
                    message.setProductId(637286);
                    message.setProductName("Testing Device");

                    BeaconResponse response = eventsApi.eventsProductpageViewPost(message);
                    return response.getEventId().toString();
                } catch (Exception e) {
                    return e.toString();
                }
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String response) {
            Log.d("EventId",response);
            responseText.setText(response);
        }
    }
}