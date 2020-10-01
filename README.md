Android Chat SDK Project

### Overview         

<img align="right" src="https://raw.githubusercontent.com/AppLozic/Applozic-Android-SDK/master/img/android.png" />


Open source Android Chat SDK / Messaging SDK that lets you add real time chat and in-app messaging in your mobile (android, iOS) applications and website.

Signup at [https://www.applozic.com/signup.html](https://www.applozic.com/signup.html?utm_source=github&utm_medium=readme&utm_campaign=android) to get the App ID.

Applozic One to One and Group Chat SDK



### Getting Started       


To integrate android chat library into your android app, signup at [Applozic](https://www.applozic.com/signup.html?utm_source=github&utm_medium=readme&utm_campaign=android) to get the App ID.

Documentation: [Applozic Android Chat & Messaging SDK Documentation](https://www.applozic.com/docs/android-chat-sdk.html?utm_source=github&utm_medium=readme&utm_campaign=android)



#### Step 1: Add the following in your build.gradle dependency:      

`implementation 'com.applozic.communication.uiwidget:mobicomkitui:5.80' `

Add the following in gradle android target:      


```
android {

        packagingOptions {           
           exclude 'META-INF/DEPENDENCIES'      
           exclude 'META-INF/NOTICE'         
           exclude 'META-INF/LICENSE'      
           exclude 'META-INF/LICENSE.txt'    
           exclude 'META-INF/NOTICE.txt' 
           exclude 'META-INF/ECLIPSE_.SF'
           exclude 'META-INF/ECLIPSE_.RSA'
         }    
    }               
```


#### Step 2: Addition of Permissions,Activities, Services and Receivers in androidmanifest.xml:
        
**Note**: Add meta-data, Activities, Services and Receivers within application Tag ``` <application> </application> ```

**Note**: Add Permissions outside the application Tag ``` <application>  ```
```

<meta-data android:name="com.applozic.application.key"
           android:value="<YOUR_APPLOZIC_APP_ID" /> <!-- Applozic App ID -->

<meta-data android:name="com.applozic.mobicomkit.notification.smallIcon"
           android:resource="YOUR_LAUNCHER_SMALL_ICON" /> <!-- Launcher white Icon -->
           
<meta-data android:name="com.google.android.geo.API_KEY"
           android:value="YOUR_GEO_API_KEY" />  <!--Replace with your geo api key from google developer console  --> 
<!-- For testing purpose use AIzaSyAYB1vPc4cpn_FJv68eS_ZGe1UasBNwxLI
To disable the location sharing via map add this line ApplozicSetting.getInstance(context).disableLocationSharingViaMap(); in onSuccess of Applozic UserLoginTask -->   
            
<meta-data android:name="com.package.name" 
           android:value="${applicationId}" /> <!-- NOTE: Do NOT change this, it should remain same i.e 'com.package.name' -->
                     
```
   **Note**: If you are **not using gradle build** you need to replace ${applicationId}  with your Android app package name

  
  Define Attachment Folder Name in your string.xml.          
     
```
<string name="default_media_location_folder">YOUR_APP_NAME</string> 
```

Paste the following in your androidmanifest.xml:        
```
<activity android:name="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity"
           android:configChanges="keyboardHidden|screenSize|smallestScreenSize|screenLayout|orientation"
           android:label="@string/app_name"
           android:parentActivityName="<APP_PARENT_ACTIVITY>"
           android:theme="@style/ApplozicTheme"
           android:launchMode="singleTask"
           tools:node="replace">
      <!-- Parent activity meta-data to support API level 7+ -->
<meta-data
           android:name="android.support.PARENT_ACTIVITY"
           android:value="<APP_PARENT_ACTIVITY>" />
 </activity>               
```

Replace APP_PARENT_ACTIVITY with your app's parent activity.        

#### Step 3: Register user account:     



     
```
User user = new User();          
user.setUserId(userId); //userId it can be any unique user identifier
user.setDisplayName(displayName); //displayName is the name of the user which will be shown in chat messages
user.setEmail(email); //optional  
user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());  //User.AuthenticationType.APPLOZIC.getValue() for password verification from Applozic server and User.AuthenticationType.CLIENT.getValue() for access Token verification from your server set access token as password
user.setPassword(""); //optional, leave it blank for testing purpose, read this if you want to add additional security by verifying password from your server https://www.applozic.com/docs/configuration.html#access-token-url
user.setImageLink("");//optional,pass your image link

 Applozic.connectUser(context, user, new AlLoginHandler() {
                @Override
                public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                    // After successful registration with Applozic server the callback will come here 
                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                    // If any failure in registration the callback  will come here 
             }
   });                                      
```

If it is a new user, new user account will get created else existing user will be logged in to the application.
You can check if user is logged in to applozic or not by using ``` Applozic.isConnected(context) ```



#### Step 4: Push Notification Setup

***Go to Applozic Dashboard, Edit Application -> Push Notification -> Android -> GCM/FCM Server Key.***

#### Firebase Cloud Messaging (FCM)  is already enabled in my app

  Add the below code and pass the FCM registration token:
  
 **1.** In UserLoginTask "onSuccess" (refer Step 3)
  

```
if(MobiComUserPreference.getInstance(context).isRegistered()) {
  Applozic.registerForPushNotification(context, registrationToken, new AlPushNotificationHandler() {
                @Override
                public void onSuccess(RegistrationResponse registrationResponse) {
                   
                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                }
    });
}
```

 **2.** In your FcmListenerService  onNewToken(Token registrationToken) method

 ```
 if (MobiComUserPreference.getInstance(this).isRegistered()) {
      new RegisterUserClientService(this).updatePushNotificationId(registrationToken);
 }
```

##### For Receiving Notifications in FCM

Add the following in your FcmListenerService  in onMessageReceived(RemoteMessage remoteMessage) 

```
 if (MobiComPushReceiver.isMobiComPushNotification(remoteMessage.getData())) {
           MobiComPushReceiver.processMessageAsync(this, remoteMessage.getData());
           return;
   }
```



#### GCM is already enabled in my app

If you already have GCM enabled in your app, add the below code and pass the GCM registration token:
  
 **1.** In UserLoginTask "onSuccess" (refer Step 3)
  

```
if(MobiComUserPreference.getInstance(context).isRegistered()) {
  Applozic.registerForPushNotification(context, registrationToken, new AlPushNotificationHandler() {
                @Override
                public void onSuccess(RegistrationResponse registrationResponse) {
                   
                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                }
     });
}
```

 **2.** At the place where you are getting the GCM registration token in your app.       

 ```
 if (MobiComUserPreference.getInstance(this).isRegistered()) {
      new RegisterUserClientService(this).updatePushNotificationId(registrationToken);
 }
```

##### For Receiving Notifications In GCM


Add the following in your GcmListenerService  in onMessageReceived 

```
if(MobiComPushReceiver.isMobiComPushNotification(data)) {            
        MobiComPushReceiver.processMessageAsync(this, data);               
        return;          
}                                          
```




#### Don't have Android Push Notification code ?

To Enable Android Push Notification using Firebase Cloud Messaging (FCM) visit the [Firebase console](https://console.firebase.google.com) and create new project, add the google service json to your app, configure the build.gradle files in your app ,finally get server key from project settings and update in  
***[Applozic Dashboard](https://console.applozic.com/settings/pushnotification) under Push Notification -> Android -> GCM/FCM Server Key.***


In case, if you don't have the existing FCM related code, then copy the push notification related files from Applozic sample app to your project from the below github link

[Github push notification code link](https://github.com/AppLozic/Applozic-Android-SDK/tree/master/app/src/main/java/com/applozic/mobicomkit/sample/pushnotification)


And add below code in your androidmanifest.xml file

``` 
<service android:name="<CLASS_PACKAGE>.FcmListenerService"
android:stopWithTask="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
</service>
  ``` 
#### Setup PushNotificationTask in UserLoginTask "onSuccess" (refer Step 3).

```
Applozic.registerForPushNotification(context, Applozic.getInstance(context).getDeviceRegistrationId(), new   AlPushNotificationHandler() {
                @Override
                public void onSuccess(RegistrationResponse registrationResponse) {
                   
                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                }
    });
```


#### Step 5: For starting the messaging activity:        

      
```
Intent intent = new Intent(this, ConversationActivity.class);            
startActivity(intent);                               
``` 
 
 
 For starting individual conversation thread, set "userId" in intent:        
 
           
```
Intent intent = new Intent(this, ConversationActivity.class);            
intent.putExtra(ConversationUIService.USER_ID, "receiveruserid123");             
intent.putExtra(ConversationUIService.DISPLAY_NAME, "Receiver display name"); //put it for displaying the title.  
intent.putExtra(ConversationUIService.TAKE_ORDER,true); //Skip chat list for showing on back press 
startActivity(intent);

```

#### Step 6: On logout, call the following:       



```
Applozic.logoutUser(context, new AlLogoutHandler() {
                @Override
                public void onSuccess(Context context) {
                    
                }

                @Override
                public void onFailure(Exception exception) {

                }
        });     
 ```
 
 
 Note: If you are running ProGuard, please add following lines:        
 
 
 
 
 
```
 #keep json classes                
 -keepclassmembernames class * extends com.applozic.mobicommons.json.JsonMarker {
 	!static !transient <fields>;
 }

 -keepclassmembernames class * extends com.applozic.mobicommons.json.JsonParcelableMarker {
 	!static !transient <fields>;
 }
 #GSON Config          
-keepattributes Signature          
-keep class sun.misc.Unsafe { *; }           
-keep class com.google.gson.examples.android.model.** { *; }            
-keep class org.eclipse.paho.client.mqttv3.logging.JSR47Logger { *; } 
-keep class android.support.** { *; }
-keep interface android.support.** { *; }
-dontwarn android.support.v4.**
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**
-keep class com.google.gson.** { *; }

 ``` 
   
**Trying out the demo app:**

Open project in Android Studio to run the sample app in your device. Send messages between multiple devices. 


Display name for users:
You can either choose to handle display name from your app or have Applozic handle it.
From your app's first activity, set the following to disable display name feature:
ApplozicClient.getInstance(this).setHandleDisplayName(false);
By default, the display name feature is enabled.


### Documentation:
For advanced options and customization, visit [Applozic Android Chat & Messaging SDK Documentation](https://www.applozic.com/docs/android-chat-sdk.html?utm_source=github&utm_medium=readme&utm_campaign=android)


### Changelog
[Changelog](https://github.com/AppLozic/Applozic-Android-SDK/blob/master/CHANGELOG.md)


#### Features:


 One to one and Group Chat
 
 Image capture
 
 Photo sharing
 
 File attachment
 
 Location sharing
 
 Push notifications
 
 In-App notifications
 
 Online presence
 
 Last seen at 
 
 Unread message count
 
 Typing indicator
 
 Message sent, Read Recipients and Delivery report
 
 Offline messaging
 
 User block / unblock
 
 Multi Device sync
 
 Application to user messaging
 
 Customized chat bubble
 
 UI Customization Toolkit
 
 Cross Platform Support (iOS, Android & Web)


### Sample source code to build messenger and chat app
https://github.com/AppLozic/Applozic-Android-SDK/tree/master/app


## Help

We provide support over at [StackOverflow] (http://stackoverflow.com/questions/tagged/applozic) when you tag using applozic, ask us anything.

Applozic is the best android chat sdk for instant messaging, still not convinced? Write to us at github@applozic.com and we will be happy to schedule a demo for you.


### Free Android Chat SDK
Special plans for startup and open source contributors, write to us at github@applozic.com 


## Github projects

Android Chat SDK https://github.com/AppLozic/Applozic-Android-SDK

Web Chat Plugin https://github.com/AppLozic/Applozic-Web-Plugin

iOS Chat SDK https://github.com/AppLozic/Applozic-iOS-SDK
