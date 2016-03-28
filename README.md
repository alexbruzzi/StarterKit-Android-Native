# Android, OCTO Starter Kit #

This kit covers an app published on Android platform and uses GCM for push notifications.


[TOC]


# Get Started #

## Download ##

Download the starter kit from here. This starter kit contains a working sample of code that takes all permissions from users, and sends appropriate API calls at appropriate times.

If you already have an app, chances are most of the steps would have been already done. However, it is advised to go through the document and remove any inconsistencies.

The code snippets mentioned here can be found in the starter kit. Should you have any difficulty understaning the flow, the starter kit code should help you out.

### Libraries ###

Should you want to download the native library, here is the link:

- [Android Native Libraries](downloads/AndroidLibNative.zip)

## Setup Capabilities ##

### GeoLocation ###

- Open AndroidManifest.xml and add `ACCESS_FINE_LOCATION` (Which includes both `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`).
- If you are getting network-based location then you need to add `INTERNET` permission too.
- Copy the `GPSTracker.java` class file from android starter kit and place it in along with other class files. This file having all the code setup for fetching user location.
- Create an object of GPSTracker class `GPSTracker object = new GPSTracker(this);` and start using gps services like
    * Check GPS services `object.canGetLocation()`
    * Fetch Latitude `object.getLatitude()`
    * Fetch Longitude `object.getLongitude()`
    * Show GPS Settings Alert Box `object.showSettingsAlert()`
    * Stop using GPS services `object.stopUsingGPS()`

---

### GCM Activation ###

- Create an application from [Google Developers Console](https://console.developers.google.com).
- Activate GCM API.
- Then, generate a new server key.
- Generate Json file for downstream messages in android application. [Generate](https://developers.google.com/mobile/add?platform=android&cntapi=gcm&cnturl=https:%2F%2Fdevelopers.google.com%2Fcloud-messaging%2Fandroid%2Fclient&cntlbl=Continue%20Adding%20GCM%20Support&%3Fconfigured%3Dtrue).
- Place the Json file in '**app/**' directory of your android application.

### GCM Setup ###

Follow the link to get started with [Google Android GCM Client](https://developers.google.com/cloud-messaging/android/client).

---

### Phone Details ###

- Import `import android.os.Build;` to fetch device details as follows
	- `Build.MANUFACTURER` to find device manufacturer
	- `Build.ID` to find device id
	- `Build.MODEL` to find device model

---

### Octo Libraries ###

- Import all files from '**libs**' folder from starter kit. List of files :
	- commons-codec-1.6.jar
	- commons-logging-1.1.3.jar
	- gson-2.3.1.jar
	- junit-4.8.1.jar
	- swagger-android-client-1.0.0.jar
	- swagger-annotations-1.5.4.jar
- Setup dependencies in module app gradle

```

ext {
    swagger_annotations_version = "1.5.0"
    httpclient_version = "4.3.3"
}
dependencies {
	// Default Dependencies
    compile fileTree(include: ['*.jar'], dir: 'libs')
    compile 'com.google.android.gms:play-services-gcm:8.4.0'
    compile 'com.android.support:appcompat-v7:23.2.0'
    // Dependencies
    androidTestCompile 'com.android.support.test.espresso:espresso-core:2.2.1'
    androidTestCompile 'com.android.support.test:runner:0.4.1'
    androidTestCompile 'com.android.support:support-annotations:23.2.0'
    compile "org.apache.httpcomponents:httpcore:$httpclient_version"
    compile "org.apache.httpcomponents:httpclient:$httpclient_version"
    compile ("org.apache.httpcomponents:httpcore:$httpclient_version") {
        exclude(group: 'org.apache.httpcomponents', module: 'httpclient')
    }
    compile ("org.apache.httpcomponents:httpmime:$httpclient_version") {
        exclude(group: 'org.apache.httpcomponents', module: 'httpclient')
    }
}

```

## How to use ##
- Import swagger apis and models using statements

```

import io.swagger.client.api.*;
import io.swagger.client.model.*;


```

- Create a `PhoneDetails` class object & initialize its variables using predefined library methods (fetch location details from GPSTracker)

```

PhoneDetails phoneDetails = new PhoneDetails();
phoneDetails.setDeviceId(Build.ID);
phoneDetails.setLatitude(latitude);
phoneDetails.setLongitude(longitude);
phoneDetails.setManufacturer(Build.MANUFACTURER);
phoneDetails.setModel(Build.MODEL);

```

---

#### App Init Call ####

- Create an Async Task for Application Initilization Beacon Call

```

class AppinitCall extends AsyncTask<String, Void, String> {

    private Exception exception;

    protected String doInBackground(String... urls) {
        try {
            EventsApi eventsApi = new EventsApi();
            eventsApi.addHeader("Content-Type","application/json");
            eventsApi.addHeader("Accept","application/json");
            eventsApi.addHeader("apikey", "API_KEY"); // Set your own Api Key

            eventsApi.setBasePath("http://api.octomatic.in"); // Set Base Path

            try {
                Message message = new Message();
                message.setUserId(1234567); // Set your own User ID
                message.setPhoneDetails(phoneDetails); // Phone Details Object

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
    }
}

```

	- Set `Api Key`, `Base Path URL` shared with you.
	- Replace `User Id` with your own Users Id
	- Replace phoneDeatils with your own '**PhoneDetails**' Class object created above.

- Make Beacon Call when app is initilialized every time user starts application.

```

AppinitCall appinit = new AppinitCall();
String[] param = new String[]{};
appinit.execute(param);

```

#### User Login Call ####

- Create an Async Task for User Login Beacon Call

```

class ApploginCall extends AsyncTask<String, Void, String> {

    private Exception exception;

    protected String doInBackground(String... urls) {
        try {
            EventsApi eventsApi = new EventsApi();
            eventsApi.addHeader("Content-Type","application/json");
            eventsApi.addHeader("Accept","application/json");
            eventsApi.addHeader("apikey", "API_KEY"); // Set your own Api Key

            eventsApi.setBasePath("http://api.octomatic.in"); // Set Base Path
            try {
                Message message = new Message();
                message.setUserId(1234567); // Set Your own User Id
                message.setPhoneDetails(phoneDetails); // Phone Details Object

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
    }
}

```

	- Set `Api Key`, `Base Path URL` shared with you.
	- Replace `User Id` with your own Users Id
	- Replace phoneDeatils with your own PhoneDetails Class object created above.

- Make Beacon Call when user logged in.

```

ApploginCall applogin = new ApploginCall();
String[] param = new String[]{};
applogin.execute(param);

```

#### User Logout Call ####

- Create an Async Task for User Logout Beacon Call

```

class ApplogoutCall extends AsyncTask<String, Void, String> {
    private Exception exception;

    protected String doInBackground(String... urls) {
        try {
            EventsApi eventsApi = new EventsApi();
            eventsApi.addHeader("Content-Type","application/json");
            eventsApi.addHeader("Accept","application/json");
            eventsApi.addHeader("apikey", "API_KEY"); // Set your own Api Key

            eventsApi.setBasePath("http://api.octomatic.in"); // Set Base Path
            try {
                Message message = new Message();
                message.setUserId(1234567); // Set Your own User Id
                message.setPhoneDetails(phoneDetails); // Phone Details Object
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
    }
}

```

	- Set `Api Key`, `Base Path URL` shared with you.
	- Replace `User Id` with your own Users Id
	- Replace phoneDeatils with your own PhoneDetails Class object created above.

- Make Beacon Call when user logged out.

```

ApplogoutCall applogout = new ApplogoutCall();
String[] param = new String[]{};
applogout.execute(param);

```

#### User Pageview Call ####

- Create an Async Task for User Pageview Beacon Call
    	
```

class AppPageViewCall extends AsyncTask<String, Void, String> {
    private Exception exception;

    protected String doInBackground(String... urls) {
        try {
            EventsApi eventsApi = new EventsApi();
            eventsApi.addHeader("Content-Type","application/json");
            eventsApi.addHeader("Accept","application/json");
            eventsApi.addHeader("apikey", "API_KEY"); // Set your own Api Key

            eventsApi.setBasePath("http://api.octomatic.in"); // Set Base Path
            try {
                PageViewMessage message = new PageViewMessage();
                message.setUserId(1234567); // Set Your own User Id
                message.setPhoneDetails(phoneDetails); // Phone Details Object

                message.setRouteUrl("/Home/DealsOfTheDay/34"); // Page URL

                List<String> cat = new ArrayList<String>();
                cat.add("handbags"); // Multiple Page Category
                cat.add("indian"); // Multiple Page Category

                message.setCategories(cat);

                List<String> tags = new ArrayList<String>();
                tags.add("handbags"); // Multiple Page Tags
                tags.add("indian"); // Multiple Page Tags
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

```

	- Set `Api Key`, `Base Path URL` shared with you.
	- Replace `User Id` with your own Users Id
	- Replace `Page Url`, `categories`, `tags` with the page details
	- Replace phoneDeatils with your own PhoneDetails Class object created above.

- Make Beacon Call when user logged out.

```

AppPageViewCall pageviewCall = new AppPageViewCall();
String[] param = new String[]{};
pageviewCall.execute(param);

```

#### User Product Pageview Call ####

- Create an Async Task for User Product Pageview Beacon Call

```

class AppProductPageViewCall extends AsyncTask<String, Void, String> {
    private Exception exception;

    protected String doInBackground(String... urls) {
        try {
            EventsApi eventsApi = new EventsApi();
            eventsApi.addHeader("Content-Type","application/json");
            eventsApi.addHeader("Accept","application/json");
            eventsApi.addHeader("apikey", "API_KEY"); // Set your own Api Key

            eventsApi.setBasePath("http://api.octomatic.in"); // Set Base Path
            try {
                ProductPageViewMessage message = new ProductPageViewMessage();
                message.setUserId(1234567); // Set Your own User Id
                message.setPhoneDetails(phoneDetails); // Phone Details Object

                message.setRouteUrl("/Home/DealsOfTheDay/34"); // Page URL

                List<String> cat = new ArrayList<String>();
                cat.add("handbags"); // Multiple Page Category
                cat.add("indian"); // Multiple Page Category

                message.setCategories(cat);

                List<String> tags = new ArrayList<String>();
                tags.add("handbags"); // Multiple Page Tags
                tags.add("indian"); // Multiple Page Tags
                message.setTags(tags);
                message.setPrice(99.99f); // Set Product Price
                message.setProductId(12345); // Set Product ID
                message.setProductName("PRODUCT_NAME"); // Set Product Name

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

```

	- Set `Api Key`, `Base Path URL` shared with you.
	- Replace `User Id` with your own Users Id
	- Replace `Page Url`, `categories`, `tags` with the page details
	- Replace `Product ID`, `Product Name`, `Product Price` with product details
	- Replace phoneDeatils with your own PhoneDetails Class object created above.

- Make Beacon Call when user logged out.

```

AppProductPageViewCall beaconCall = new AppProductPageViewCall();
String[] param = new String[]{};
beaconCall.execute(param);

```

#### User GCM Token Update Call ####

- Create an Async Task for GCM Token Update Beacon Call

```

class PushNotificationCall extends AsyncTask<String, Void, String> {
    private Exception exception;

    protected String doInBackground(String... data) {
        try {

            PushnotificationApi pushApi = new PushnotificationApi();
            pushApi.addHeader("Content-Type","application/json");
            pushApi.addHeader("Accept","application/json");
            pushApi.addHeader("apikey", "API_KEY"); // Set your own Api Key

            pushApi.setBasePath("http://api.octomatic.in"); // Set Base Path

            try {
                UpdatePushToken message = new UpdatePushToken();
                message.setUserId(1234567); // Set Your own User Id
                message.setPhoneDetails(phoneDetails); // Phone Details Object

                message.setPushToken(GCMToken); // Replace with your Google GCM Registration Token
                message.setPushKey("AbCdEfGhIjKl0123-4_56789mnop"); // Replace with your Google GCM API Key

                message.setNotificationType(1); // Android - 1

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

```

	- Set `Api Key`, `Base Path URL` shared with you.
	- Replace `User Id` with your own Users Id
	- Replace `GCMToken`, `GCM API Key` with your Google GCM Registration Token & your Google api key
	- Replace phoneDeatils with your own PhoneDetails Class object created above.

- Make Beacon Call when GCM registered.

```

PushNotificationCall beaconcall = new PushNotificationCall();
String[] param = new String[]{};
beaconcall.execute(param);

```
