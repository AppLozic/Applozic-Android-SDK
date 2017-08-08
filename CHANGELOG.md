
## Changelog

**Version 4.78**  - Thursday, 2 March 2017
   
  - Group of two support 
  - Group member limit in group create and group member add
  - Launch one to one chat in group member name click or profile click 
  - Sending meta data in message by adding settings 
  - Group and profile image upload bug fix
  - Other bug fixes 
 

 **Version 4.76**  - Friday,10 February 2017
   
  - Android N contact sharing bug fix  .
 

 **Version 4.75**  - Wednesday,8 February 2017
   
  - Group Delete support: deleted group will be disabled.
 
 **Version 4.74**  - Sunday, 5 February 2017
   
  - Support for version 7.1 
  - Support for support-library version 25.1.0
  - Chat not syncing bug fix for android 7.1
  
 **Version 4.73**  - Sunday, 5 January 2017
 
 - Support for version 7.0 
 - Group mute
 
   
 **Version 4.71**  - Thursday, 5 January 2017
 
 - Attachments options settings 
 - Restricted words settings 
 
  
   
 **Version 4.64**  - Friday, 18 November 2016
   
  Group info members context menu
 
  Smart messaging with message meta data : Push notification,Archive
  
  Group silent notifications 

  Bug fixes and Improvements
  
### Steps for upgrading from 4.63 to 4.64


**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.64'`


**Version 4.63**  - Tuesday, 25 October 2016
   
  Broadcast messageing 
  
  Smart messaging with message meta data : Hidden messages

  Bug fixes and Improvements


### Steps for upgrading from 4.62 to 4.63


**Step 1: Add the following in your Top-level/Proejct level build.gradle file change the version according to your app**:   

 ```
ext.googlePlayServicesVersion = '9.0.2'
ext.supportLibraryVersion = '23.1.1'
 ```

**Step 2: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.63'`


 **Version 4.62**  - Tuesday, 11 October 2016

  
  Change in Settings config now added json file 
  
  Bug fixes and Improvements


### Steps for upgrading from 4.61 to 4.62


**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.62'`


 **Version 4.61**  - wednesday, 5 October 2016

   Contact list selection added search option ,UI change

   Bug fixes and Improvements


### Steps for upgrading from 4.60 to 4.61


**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.61'`


 **Version 4.60**  - Friday, 30 September 2016
 
 Bug fixes and Improvements
 
  
### Steps for upgrading from 4.59 to 4.60


**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.60'`

 
 **Version 4.59**  - Saturday, 17 September 2016
 
 Bug fixes and Improvements
 
 
###  Steps for upgrading from 4.58 to 4.59

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.59'`
 

 **Version 4.58**  - Thursday, 15 September 2016
 
 Unread count bug fix
 
 Open group issues
 
 Improvements  

 
###  Steps for upgrading from 4.57 to 4.58

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.58'`


 **Version 4.57**  - Wednesday, 7 September 2016
 
 Block and unblock fix
 
 Message list pagination
 
 Message Encryption
 
 Group add,remove,exit,delete group,group icon change meta data supports 
 
 Improvements and bug fixs
 

 
###  Steps for upgrading from 4.56 to 4.57

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.57'`
 
 **Version 4.56**  - Tuesday, 30 August 2016
 
 Improvements and bug fixs
 
 
###  Steps for upgrading from 4.55 to 4.56

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.56'`

 
**Version 4.55**  - Tuesday, 23 August 2016

User block and unblock bug fix

Unread message count fix

Code improvements 

###  Steps for upgrading from 4.53 to 4.55

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.55'`


**Step 2: In Your Android manifest add below code**

```
<service android:name="com.applozic.mobicomkit.api.conversation.ConversationReadService"
          android:exported="false" />

```



 
 
 
**Version 4.53**  - Monday,1 August 2016

Bug fixes and improvement

###  Steps for upgrading from 4.52 to 4.53

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.53'`


**Version 4.52**  - Wednesday,27 July 2016

User and Group image upload and change

Group typing status 

Typing status is moved from bottom to App Bar

User status message change

Bug fixes and performance improvement

###  Steps for upgrading from 4.51 to 4.52

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.52'`


**Step 2: In Your Android manifest add below code**

```
 <activity android:name="com.soundcloud.android.crop.CropImageActivity" />

 <service android:name="com.applozic.mobicomkit.api.people.UserIntentService"
          android:exported="false" />

 <service android:name="com.applozic.mobicomkit.api.conversation.ConversationIntentService"
           android:exported="false" />

```

**Version 3.31**

Bug fixes and improvements

**Version 3.30**

 Contact search bug fix
 
 Group name sync changes
 
 Read Count bug fix 
 
**Version 3.29**

User Block

Grid layout for attachment options

Contact Search

Group Change notification
