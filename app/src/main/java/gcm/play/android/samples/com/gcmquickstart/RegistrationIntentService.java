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

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import io.swagger.client.model.*;
import io.swagger.client.api.PushnotificationApi;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public String GCMToken = null;
    public GPSTracker gps;
    public float latitude = 0.0f;
    public float longitude = 0.0f;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        GCMToken = token;

        // create class object
        gps = new GPSTracker(RegistrationIntentService.this);

        // check if GPS enabled
        if(gps.canGetLocation()) {
            latitude = (float)gps.getLatitude();
            longitude = (float)gps.getLongitude();
        } else {
            gps.showSettingsAlert();
        }

        PushNotificationCall beaconcall = new PushNotificationCall();
        String[] param = new String[]{token};
        beaconcall.execute(param);
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

    class PushNotificationCall extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... data) {
            try {

                PushnotificationApi pushApi = new PushnotificationApi();
                pushApi.addHeader("Content-Type","application/json");
                pushApi.addHeader("Accept","application/json");
                pushApi.addHeader("apikey", "25745dd7d6754297a0883f4ee3201982");

                pushApi.setBasePath("http://192.168.0.106:8000");

                try {
                    UpdatePushToken message = new UpdatePushToken();
                    message.setUserId(2736482);

                    PhoneDetails phoneDetails = new PhoneDetails();
                    phoneDetails.setDeviceId(Build.ID);
                    phoneDetails.setLatitude(latitude);
                    phoneDetails.setLongitude(longitude);
                    phoneDetails.setManufacturer(Build.MANUFACTURER);
                    phoneDetails.setModel(Build.MODEL);
                    message.setPhoneDetails(phoneDetails);

                    message.setPushToken(GCMToken);
                    message.setPushKey("AIzaSyB7YgH_8XYPbHAvwbMt1l-9_BsKoYdlS20");

                    message.setNotificationType(1);

                    BeaconResponse response = pushApi.updatePushTokenPost(message);
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
            Log.d("GCM Token Updated",response);
        }
    }

}
