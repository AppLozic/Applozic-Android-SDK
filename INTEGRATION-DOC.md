### Building a New App

If you are building a new app, you can download the sample app with chat integrated from Github:
[https://github.com/Applozic/Applozic-Android-SDK/](https://github.com/Applozic/Applozic-Android-SDK/)

Open the downloaded project in Android Studio, replace Applozic Application Key in androidmanifest.xml with your [Application Key](https://www.applozic.com/docs/android-chat-sdk.html#first-level) in the following meta data:

```
<meta-data android:name="com.applozic.application.key"
           android:value="YOUR_APPLOZIC_APPLICATION_KEY" />
```


### Add Applozic Chat to existing App




#### Step 1: Gradle Dependency

Add the following in your build.gradle dependency  
a) Chat SDK (Without audio/video)

```
compile 'com.applozic.communication.uiwidget:mobicomkitui:4.96'
```

b) Chat SDK with Audio/Video.

```
compile 'com.applozic.communication.uiwidget:audiovideo:1.2'
```
**NOTE** : To enable Audio/Video you need to follow extra steps:
https://www.applozic.com/docs/android-chat-sdk.html#audio-video-call-setup

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


#### Step 2: AndroidManifest
Add the following Permissions, Activities, Services and Receivers in androidmanifest.xml
       
**Note**: Add meta-data, Activities, Services and Receivers within application Tag ``` <application> </application>  ``` 

**Note**: Add Permissions outside the application Tag ```  <application> ```  
``` 

<meta-data android:name="com.applozic.application.key"
           android:value="YOUR_APPLOZIC_APPLICATION_KEY" /> <!-- Applozic Application Key -->

<meta-data android:name="com.applozic.mobicomkit.notification.smallIcon"
           android:resource="YOUR_LAUNCHER_SMALL_ICON" /> <!-- Launcher white Icon -->
           
<meta-data android:name="main_folder_name"
           android:value="@string/default_media_location_folder" /> <!-- Attachment Folder Name -->
           
<meta-data android:name="com.google.android.geo.API_KEY"
           android:value="YOUR_GEO_API_KEY" />  <!--Replace with your geo api key from google developer console  --> 
<!-- For testing purpose use AIzaSyAYB1vPc4cpn_FJv68eS_ZGe1UasBNwxLI
To disable the location sharing via map add this line ApplozicSetting.getInstance(context).disableLocationSharingViaMap(); in onSuccess of Applozic UserLoginTask -->
           
 <meta-data android:name="activity.open.on.notification"
            android:value="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity" /> <!-- NOTE : Do NOT change this value -->    
            
 <meta-data android:name="com.package.name" 
           android:value="${applicationId}" /> <!-- NOTE: Do NOT change this, it should remain same i.e 'com.package.name' -->
           
<provider android:name="android.support.v4.content.FileProvider"
           android:authorities="${applicationId}.provider"
           android:exported="false"
           android:grantUriPermissions="true">
<meta-data android:name="android.support.FILE_PROVIDER_PATHS"
           android:resource="@xml/applozic_provider_paths"/>
 </provider>         
         
```
   **Note**: If you are **not using gradle build** you need to replace ${applicationId}  with your Android app package name

  
  Define Attachment Folder Name in your string.xml.          
     
```
<string name="default_media_location_folder">YOUR_APP_NAME</string> 
```

Permissions:          




```
<uses-permission android:name="<APP_PKG_NAME>.permission.MAPS_RECEIVE" />
<permission android:name="<APP_PKG_NAME>.permission.MAPS_RECEIVE" android:protectionLevel="signature" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"  />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.VIBRATE" />
  ```


Broadcast Registration For PushNotification:        


   
```
<receiver android:name="com.applozic.mobicomkit.uiwidgets.notification.MTNotificationBroadcastReceiver">
         <intent-filter>            
             <action android:name="${applicationId}.send.notification"/>                    
        </intent-filter>           
</receiver>                  
```

**Note**: If you are **not using gradle build** you need to replace ${applicationId}  with your Android app package name



Paste the following in your androidmanifest.xml:       




   
```
 <activity android:name="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity"
           android:configChanges="keyboardHidden|screenSize|smallestScreenSize|screenLayout|orientation"
           android:label="@string/app_name"
           android:parentActivityName="<APP_PARENT_ACTIVITY>"
           android:theme="@style/ApplozicTheme"
           android:launchMode="singleTask" >
      <!-- Parent activity meta-data to support API level 7+ -->
<meta-data
           android:name="android.support.PARENT_ACTIVITY"
           android:value="<APP_PARENT_ACTIVITY>" />
 </activity>
                   
 <activity android:name="com.applozic.mobicomkit.uiwidgets.people.activity.MobiComKitPeopleActivity"
           android:configChanges="keyboardHidden|screenSize|smallestScreenSize|screenLayout|orientation"
           android:label="@string/app_name"
           android:parentActivityName="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity"
           android:theme="@style/ApplozicTheme"
           android:windowSoftInputMode="adjustResize">

            <!-- Parent activity meta-data to support API level 7+ -->
<meta-data android:name="android.support.PARENT_ACTIVITY"
           android:value="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity" />

         <intent-filter>
                <action android:name="android.intent.action.SEARCH" />
         </intent-filter>

         <intent-filter>
               <action android:name="android.intent.action.SEND" />             
               <category android:name="android.intent.category.DEFAULT" />
               <data android:mimeType="image/*" />
               <data android:mimeType="audio/*" />
               <data android:mimeType="video/*" />
               <data android:mimeType="text/plain"/>
         </intent-filter>
<meta-data android:name="android.app.searchable"
           android:resource="@xml/searchable_contacts" />
</activity>

<activity android:name="com.applozic.mobicomkit.uiwidgets.conversation.activity.FullScreenImageActivity"
          android:configChanges="keyboardHidden|orientation|screenSize"
          android:label="Image"
 android:parentActivityName="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity"
          android:theme="@style/Applozic_FullScreen_Theme">
    <!-- Parent activity meta-data to support API level 7+ -->
<meta-data
          android:name="android.support.PARENT_ACTIVITY"
          android:value="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity" />
</activity>

<activity android:name="com.applozic.mobicomkit.uiwidgets.conversation.activity.ContactSelectionActivity"
          android:configChanges="keyboardHidden|screenSize|smallestScreenSize|screenLayout|orientation"
          android:launchMode="singleTop"
          android:parentActivityName="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity"
          android:theme="@style/ApplozicTheme">
<meta-data
           android:name="android.support.PARENT_ACTIVITY"
           android:value="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity" />
</activity>

<activity android:name="com.applozic.mobicomkit.uiwidgets.conversation.activity.ChannelCreateActivity"
          android:configChanges="keyboardHidden|screenSize|smallestScreenSize|screenLayout|orientation"
          android:launchMode="singleTop"
          android:parentActivityName="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity"
          android:theme="@style/ApplozicTheme">
<meta-data
          android:name="android.support.PARENT_ACTIVITY"
          android:value="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity" />
</activity>

<activity android:name="com.applozic.mobicomkit.uiwidgets.conversation.activity.ChannelNameActivity"
          android:configChanges="keyboardHidden|screenSize|smallestScreenSize|screenLayout|orientation"
          android:launchMode="singleTop"
          android:parentActivityName="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity"
          android:theme="@style/ApplozicTheme">
</activity>

<activity android:name="com.applozic.mobicomkit.uiwidgets.conversation.activity.ChannelInfoActivity"
          android:configChanges="keyboardHidden|screenSize|smallestScreenSize|screenLayout|orientation"
          android:launchMode="singleTop"
          android:parentActivityName="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity"
          android:theme="@style/ApplozicTheme">
<meta-data
           android:name="android.support.PARENT_ACTIVITY"
           android:value="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity" />
 </activity>

<activity android:name="com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComAttachmentSelectorActivity"
          android:configChanges="keyboardHidden|screenSize|smallestScreenSize|screenLayout|orientation"
          android:launchMode="singleTop"
          android:parentActivityName="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity"
          android:theme="@style/ApplozicTheme"
          android:windowSoftInputMode="stateHidden|adjustResize">
<meta-data 
           android:name="android.support.PARENT_ACTIVITY"
           android:value="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity" />
</activity>
  
<activity android:name="com.applozic.mobicomkit.uiwidgets.conversation.activity.MobicomLocationActivity"
          android:configChanges="keyboardHidden|screenSize|smallestScreenSize|screenLayout|orientation"
          android:parentActivityName="com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity"
          android:theme="@style/ApplozicTheme"
          android:windowSoftInputMode="adjustResize">
 </activity>
 
<activity android:name="com.theartofdev.edmodo.cropper.CropImageActivity"
           android:theme="@style/Base.Theme.AppCompat"/>
           
<service android:name="com.applozic.mobicomkit.api.conversation.MessageIntentService"
         android:exported="false" />
              
<service android:name="org.eclipse.paho.android.service.MqttService"/>

<service android:name="com.applozic.mobicomkit.api.conversation.ApplozicIntentService"
         android:exported="false" />
             
<service android:name="com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService"
         android:exported="false" />
         
<service android:name="com.applozic.mobicomkit.api.people.UserIntentService"
         android:exported="false" />

<service android:name="com.applozic.mobicomkit.api.conversation.ConversationIntentService"
         android:exported="false" />
            
<service android:name="com.applozic.mobicomkit.api.conversation.ConversationReadService"
         android:exported="false" />
         
<service android:name="com.applozic.mobicomkit.uiwidgets.notification.NotificationIntentService"
         android:exported="false" />

<receiver android:name="com.applozic.mobicomkit.broadcast.TimeChangeBroadcastReceiver">
         <intent-filter>
             <action android:name="android.intent.action.TIME_SET" />
             <action android:name="android.intent.action.TIMEZONE_CHANGED" />
         </intent-filter>
</receiver>

<receiver android:name="com.applozic.mobicomkit.broadcast.ConnectivityReceiver"
          android:exported="true" android:enabled="true">
          <intent-filter>
              <action android:name="android.intent.action.BOOT_COMPLETED" />
          </intent-filter>
</receiver>                  
```


Replace APP_PARENT_ACTIVITY with your app's parent activity.        

#### Step 3: Login/Register User
Applozic will create a new user if the user doesn't exists. userId is the unique identifier for any user, it can be anything like email, phone number or uuid from your database.

     
```
UserLoginTask.TaskListener listener = new UserLoginTask.TaskListener() {                  

@Override          
public void onSuccess(RegistrationResponse registrationResponse, Context context) {           
     //After successful registration with Applozic server the callback will come here 
}                       

@Override             
public void onFailure(RegistrationResponse registrationResponse, Exception exception) {  
    //If any failure in registration the callback  will come here 
}};                      

User user = new User();          
user.setUserId(userId); //userId it can be any unique user identifier
user.setDisplayName(displayName); //displayName is the name of the user which will be shown in chat messages
user.setEmail(email); //optional
user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());  //User.AuthenticationType.APPLOZIC.getValue() for password verification from Applozic server and User.AuthenticationType.CLIENT.getValue() for access Token verification from your server set access token as password
user.setPassword(""); //optional, leave it blank for testing purpose, read this if you want to add additional security by verifying password from your server https://www.applozic.com/docs/configuration.html#access-token-url
user.setImageLink("");//optional,pass your image link
new UserLoginTask(user, listener, this).execute((Void) null);                                       
```

If it is a new user, new user account will get created else existing user will be logged in to the application.
You can check if user is logged in to applozic or not by using ``` MobiComUserPreference.getInstance(this).isLoggedIn() ```



#### Step 4: Push Notification Setup

For push notifications, you must have a Firebase account.
Signup to https://console.firebase.google.com/ and create your application and generate push notification services file.

***Go to Applozic Dashboard, Edit Application -> Push Notification -> Android -> GCM/FCM Server Key.***


#### Firebase Cloud Messaging (FCM)  is already enabled in my app

  Add the below code and pass the FCM registration token:
  
 **1.** In UserLoginTask "onSuccess" (refer Step 3)
  

```
if(MobiComUserPreference.getInstance(context).isRegistered()) {

PushNotificationTask pushNotificationTask = null;         
PushNotificationTask.TaskListener listener = new PushNotificationTask.TaskListener() {                  
@Override           
public void onSuccess(RegistrationResponse registrationResponse) {   

}            
@Override          
public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

} 

};                    

pushNotificationTask = new PushNotificationTask(registrationToken, listener, mActivity);            
pushNotificationTask.execute((Void) null);  
}
```

 **2.** In your FcmInstanceIDListenerService  onTokenRefresh() method

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

PushNotificationTask pushNotificationTask = null;         
PushNotificationTask.TaskListener listener = new PushNotificationTask.TaskListener() {                  
@Override           
public void onSuccess(RegistrationResponse registrationResponse) {   

}            
@Override          
public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

} 

};                    

pushNotificationTask = new PushNotificationTask(registrationToken, listener, mActivity);            
pushNotificationTask.execute((Void) null);  
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

To Enable Android Push Notification using Firebase Cloud Messaging (FCM) visit the [Firebase console](https://console.firebase.google.com) and create new project, add the google service json to your app, configure the build.gradle files in your app ,finally get server key from project settings and update in  ***[Applozic Dashboard](https://dashboard.applozic.com/views/applozic/page/admin/dashboard.jsp) under Edit Application -> Push Notification -> Android -> GCM/FCM Server Key.***


In case, if you don't have the existing FCM related code, then copy the push notification related files from Applozic sample app to your project from the below github link

[Github push notification code link](https://github.com/AppLozic/Applozic-Android-SDK/tree/master/app/src/main/java/com/applozic/mobicomkit/sample/pushnotification)


And add below code in your androidmanifest.xml file

``` 
<service android:name="<CLASS_PACKAGE>.FcmListenerService">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
</service>

<service android:name="<CLASS_PACKAGE>.FcmInstanceIDListenerService"
       android:exported="false">
       <intent-filter>
           <action android:name="com.google.firebase.INSTANCE_ID_EVENT" />
       </intent-filter>
</service>
  ``` 
#### Setup PushNotificationTask in UserLoginTask "onSuccess" (refer Step 3).

```
 PushNotificationTask pushNotificationTask = null;
 PushNotificationTask.TaskListener listener=  new PushNotificationTask.TaskListener() {
 @Override
 public void onSuccess(RegistrationResponse registrationResponse) {

 }
 @Override
 public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

 }
  
 };
 pushNotificationTask = new PushNotificationTask(Applozic.getInstance(context).getDeviceRegistrationId(),listener,context);
 pushNotificationTask.execute((Void)null);
```

#### Step 5: Initiate Chat

For starting the messaging activity      
      
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

#### Step 6: Logout user       

Call the following when user logout from your app:

```
1)Async task call for logout:

 UserLogoutTask.TaskListener userLogoutTaskListener = new UserLogoutTask.TaskListener() {
 @Override
 public void onSuccess(Context context) {
   //Logout success
 } 
  @Override
 public void onFailure(Exception exception) {
  //Logout failure
 }
 };

 UserLogoutTask userLogoutTask = new UserLogoutTask(userLogoutTaskListener, context);
 userLogoutTask.execute((Void) null);     
 
 2)Logout Method call  
 
 ApiResponse apiResponse =  new UserClientService(this).logout();
 
 if(apiResponse != null && apiResponse.isSuccess()){
      //Logout success
  }else {
     //Logout failure 
 }
Note :Use async task or thread to call this logout method  
```
 

#### Step 7: ProGuard Setup
Add the following if you are using ProGuard:
 
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
