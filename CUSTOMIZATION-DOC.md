
Applozic provide easy settings to customise your ui themes color, pop-up messages etc. You need to follow below steps to enable/change these settings:

 **1**.Download applozic-settings.json file from here [Json file](https://github.com/AppLozic/Applozic-Android-SDK/tree/master/app/src/main/assets/applozic-settings.json)
 
 **2**.Create an assets directory in app-->main and paste that applozic-settings.json file in assets directory
 
#### Applozic settings  Json Properites detail



  
| Properites | Sample Value | Description |
| ---------- | ------| ----------- |
| sentMessageBackgroundColor | Color hex (#FF03A9F4) |  Sent message chat bubble color |
| receivedMessageBackgroundColor| Color hex (#FFFFFFFF) | Received message chat bubble color |
| sendButtonBackgroundColor | Color hex (#FF03A9F4) | Send button background color |
| attachmentIconsBackgroundColor | Color hex (#FF03A9F4) |Attachment icons background color |
| channelCustomMessageBgColor | Color hex (#cccccc) | Group add,remove,left message background color |
| sentContactMessageTextColor | Color hex (#FFFFFFFF) | Sent contact message text color |
| receivedContactMessageTextColor| Color hex (#000000)|Received contact message text color |
| sentMessageTextColor | Color hex (#FFFFFFFF) | Sent message text color |
| receivedMessageTextColor | Color hex (#000000) |  Received message text color |
| messageEditTextTextColor | Color hex (#000000) | Edit text text color |
| sentMessageLinkTextColor | Color hex (#5fba7d) |  Sent message hyper link text color |
| receivedMessageLinkTextColor | Color hex (#FFFFFFFF) |  Received message hyper link text color |
| messageEditTextHintTextColor | Color hex (#bdbdbd) | Edit text hint text color |
| noConversationLabelTextColor | Color hex  (#000000) | No Conversation Label text color |
| conversationDateTextColor | Color hex (#333333) | Message data text color |
| conversationDayTextColor | Color hex  (#333333) | Message day text color |
| messageTimeTextColor | Color hex  (#838b83) |    Message time text color |
| channelCustomMessageTextColor | Color hex (#666666) |  Group add,remove,left message text color |
| sentMessageBorderColor | Color hex  (#FF03A9F4) |Sent Message chat bubble border color |
| receivedMessageBorderColor | Color hex  (#FFFFFFFF )| Received message chat bubble border color |
| channelCustomMessageBorderColor | Color hex  (#cccccc) |Group add,remove,left message border color |
| noConversationLabel| String | No  conversation text label |
| noSearchFoundForChatMessages | String | No search found text label |
| totalRegisteredUserToFetch | int (Prefered 100)| Total Registerer User to get from server for conatct list |
| maxAttachmentAllowed | int (Prefered 10) | Maximum attachment allowed to attch while sending Multiple attchemnets |
| locationShareViaMap | true/false | Location share activity |
| startNewFloatingButton | true/false |Start New Conversation Plus (+) FloatingActionButton |
| startNewButton | true/false | Start New Conversation Plus (+) Button |
| onlineStatusMasterList | true/false | Online status Green Dot in Chat list |
| startNewGroup | true/false | Create group  option | 
| inviteFriendsInContactActivity | true/false | Invite friends button in Conatct list when no contcats are there |
| registeredUserContactListCall | true/false | Registered users contact list call |
| createAnyContact | true/false | Launch a chat with any user in contact list | 
| userProfileFragment |  true/false | Show other user profile on click of App bar|
| messageSearchOption | true/false | Message search option |
| hideGroupAddMembersButton | true/false | Hide the add memeber option in group |
| hideGroupNameUpdateButton | true/false | Hide group name and group image change option  in group |
| hideGroupExitButton | true/false | Hide group exit option button in group |
| hideGroupRemoveMemberOption | true/false | Hide remove member option  from group |
| profileOption | true/false | Show and hide the profile option |
| restrictedWordMessage | String | Restricted words are not allowed |
| hideAttachmentButton | true/false | Show and hide media attachment button |

#### Customizing attachment options.

You can hide particular attachment options by setting value as **false** in attachmentOptions in applozic-settings.json file. 
```
"attachmentOptions": {
      ":location": true,
      ":camera": true,
      ":file": true,
      ":audio": true,
      ":video": true,
      ":contact": true
    }
```
Example : to hide location option in attachment set **":location": false**. 



#### Notifications

Enable/disable the notfications

```
ApplozicClient.getInstance(context).disableNotification();
ApplozicClient.getInstance(context).enableNotification();
```


#### Theme customization

  To customize the theme, copy paste the following style in your theme's  res file:
   ```
  <style name="ApplozicTheme" parent="Theme.AppCompat.Light.NoActionBar">
  
    <!--To change the toolbar color change the colorPrimary  -->
    <item name="colorPrimary">@color/applozic_theme_color_primary</item>
    
    <!-- To change the status bar  color change the color of colorPrimaryDark -->
    <item name="colorPrimaryDark">@color/applozic_theme_color_primary_dark</item>
    
    <!-- colorAccent is used as the default value for colorControlActivated which is used to tint widgets -->
    <item name="colorAccent">@color/applozic_theme_color_primary</item>
    
    <item name="windowActionModeOverlay">true</item>
  </style>
   ```
  
   Change the name of the style from name="ApplozicTheme"  to some new name and in your app androidmanifest.xml file find for ApplozicTheme and replace with your new theme style.
 
 
#### UI source code

For complete control over UI, you can also download open source chat UI toolkit and change it as per your designs :

[https://github.com/AppLozic/Applozic-Android-SDK](https://github.com/AppLozic/Applozic-Android-SDK)


Import [MobiComKitUI Library](https://github.com/AppLozic/Applozic-Android-SDK/tree/master/mobicomkitui) into your android project and add the following in the build.gradle file:

```
compile project(':mobicomkitui')
```

MobiComKitUI contains the UI related source code, icons, layouts and other resources which you can customize based on your design needs.

For your custom contact list, replace MobiComKitPeopleActivity with your contact list activity.

Sample app with integration is available under [app](https://github.com/AppLozic/Applozic-Android-SDK/tree/master/app)


#### Fragment support

You can add the chat fragments to any activity  below documentation links will explain how to add the chat fragments to your activity   

**1**. [Adding Chat fragments to Activity](https://docs.google.com/document/d/1qH1razXLCg_aCWgdhVp4ATCAZqU3APugWiUbSEJm0-A/edit?usp=sharing)

 **2**. [Adding Chat fragments to TabView Activity](https://docs.google.com/document/d/1-TbJWCUEIzwWGD8mBkV6y2PLrNAIWTmTI6jJuzgC7c4/edit?usp=sharing)
 
 
 #### Localization
 
  Steps to Add resources in your Application
  
 **Step 1**. Create the android resource directory for your locale
 
 **Step 2**. Create resource file inside android resource directory for your locale (i.e created from Step1)
 
 Download and override resource string for your locale from below
 
 
  [Array Options](https://github.com/AppLozic/Applozic-Android-SDK/blob/master/mobicomkitui/src/main/res/values/mobicom_array.xml)


  [Common Strings](https://github.com/AppLozic/Applozic-Android-SDK/blob/master/mobicomkitui/src/main/res/values/mobicom_strings.xml)


  [Simple Strings](https://github.com/AppLozic/Applozic-Android-SDK/blob/master/mobicomkitui/src/main/res/values/strings.xml)
  
  Ex. Change resource string (message) value to spanish locale
  
  
      <string name="message">Mensaje</string>


