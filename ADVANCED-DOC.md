Refer to the below documentation for a deeper integration if you wish to perform chat operation directly from your app's interface without using the Applozic UI toolkit:


#### User Login/Register   
   
Import
```
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;      
```

Code
```
new RegisterUserClientService(activity).createAccount
(USER_EMAIL, USER_ID, USER_PHONE_NUMBER, GCM_REGISTRATION_ID);         
 ``` 

#### Send message   

Import
```
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;         
```

Code
```
 public void sendMessage(Message message) {             
   ...        
 }                
```

Example
```
new MobiComConversationService(activity).sendMessage(new Message("contact@applozic.com", "hello test"));         
```

#### Send message with metadata

You can send extra information along with message text as meta-data. These key value pair can be used to do some extra processing or keep information about messages.

```
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        Message message = new Message();
        //Note:This is only for sending a message to Group then pass the channelKey
        message.setGroupId(channelKey);  
        
        //Note:This is only for sending a message to User then pass the receiver UserId 
        message.setTo(receiverUserId); 
        message.setContactIds(receiverUserId); 
        
        message.setRead(Boolean.TRUE);
        message.setStoreOnDevice(Boolean.TRUE);
        message.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
        message.setSendToDevice(Boolean.FALSE);
        message.setType(Message.MessageType.MT_OUTBOX.getValue());
        message.setMessage(messageToSend); //Message to send
        message.setDeviceKeyString(userPreferences.getDeviceKeyString());
        message.setSource(Message.Source.MT_MOBILE_APP.getValue());
        
       //Messsage metadata map 
        Map<String,String> messageMetaDataMap = new HashMap<>();
        messageMetaDataMap.put("key1","value1");
        messageMetaDataMap.put("key2","value2");
        message.setMetadata(messageMetaDataMap);
        
        //Method for sending a message 
        new MobiComConversationService(context).sendMessage(message);

```




#### Messages list 

Import
```
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
```

  
i) Get single latest message from each conversation    

Code
```
 public synchronized List<Message> getLatestMessagesGroupByPeople() {            
  ...         
 }                              
```


ii) Get messages of logged in user with another user by passing userId, startTime and endTime. 
startTime and endTime are considered in time in milliseconds from 1970.       

Code
```
 public List<Message> getMessages(String userId, Long startTime, Long endTime) {            
  ...           
 }                           
```

#### Unread Count

i)To get the unread count of individual contact pass the userId

Code

```
int contactUnreadCount = new MessageDatabaseService(context).getUnreadMessageCountForContact(userId);
```

ii)To get the unread count of individual channel/group pass the channelKey

Code

```
int channelUnreadCount = new MessageDatabaseService(context).getUnreadMessageCountForChannel(channelKey);
```

iii)To get the total unread count

Code

```
int totalUnreadCount = new MessageDatabaseService(context).getTotalUnreadCount();
```


#### Custom Message
Send an automated custom message to connect 2 users. The message layout will be same for both users so that it doesn't look like a sent message.


Code
```
Message message = new Message("contact@applozic.com", "hey! here's a match <3");
new MobiComMessageService(this, MessageIntentService.class).sendCustomMessage(message);
```

Customize the background color for the custom message:
Code
```
ApplozicSetting.getInstance(this).setColor(ApplozicSetting.CUSTOM_MESSAGE_BACKGROUND_COLOR, Color.parseColor("#FFB3E5FC"));
```



###  Contacts           

The below methods are for creating contacts to be stored locally on the user's device.


You can create the contact list in two easy steps by using AppContactService.java api. 
Sample method **buildContactData()** for adding contacts is present in sample app MainActivity.java.

##### Step 1: Creating contact   

Create
```
    Contact contact = new Contact();            
    contact.setUserId(<userId>);           
    (Unique ID to identify contact )                 
    contact.setFullName(<full name of contact>);                 
    contact.setEmailId(<EmailId>);               
    contact.setImageURL(<image http URL OR android resource drawable  >);               
    (in case of drawable use R.drawable.<resource_name>)                         
```

Example :        

```
    Contact contact = new Contact();                 
    contact.setUserId("adarshk");           
    contact.setFullName("Adarsh");               
    contact.setImageURL("R.drawable.applozic_ic_contact_picture_holo_light");           
    contact.setEmailId("github@applozic.com");                
```

##### Step 2: Save contact

Save the contact using AppContactService.java add() method.
 
```
    Context context = getApplicationContext();           
    AppContactService appContactService = new AppContactService(context);            
    appContactService.add(contact);                           
```



##### AppContactService.Java at a glance


AppContactService.java provides methods you need to add, delete and update contacts.

Add single contact
```
add(Contact contact)
```

Add multiple contacts
```
addAll(List<Contact> contactList)
```

Delete contact
```
deleteContact(Contact contact)
```

Delete contact by Id
```
deleteContactById(String contactId)
```

Read contact by Id
```
getContactById(String contactId )
```

Update contact
```
updateContact(Contact contact)
```

Update or Insert contact
```
upsert(Contact contact)
```



#### Group Chat

Open Group Chat

For opening group conversation thread, set "groupdId" in intent:

```
Intent intent = new Intent(this, ConversationActivity.class);            
intent.putExtra(ConversationUIService.GROUP_ID, 12);      //Pass group id here.       
startActivity(intent);
```


##### 1) Create Group

 Create a group with a specific group type 
 
 Private group with type : Channel.GroupType.PRIVATE.getValue().intValue()
 
 Public group with type : Channel.GroupType.PUBLIC.getValue().intValue()
 
 Open group with type : Channel.GroupType.OPEN.getValue().intValue()
 
 
 
 **Note:** Group meta data is optional 
 
 Setting group meta data for messages like created group, left group, removed from group, group deleted, group icon changed and group name changed.
  
  **Note:** If the channel meta data  is set as empty String, no notification is sent to other users in the group/channel.

   ```
 ChannelMetadata channelMetadata = new ChannelMetadata();
 channelMetadata.setCreateGroupMessage(ChannelMetadata.ADMIN_NAME + " created " + ChannelMetadata.GROUP_NAME);
 channelMetadata.setAddMemberMessage(ChannelMetadata.ADMIN_NAME + " added " + ChannelMetadata.USER_NAME);
 channelMetadata.setRemoveMemberMessage(ChannelMetadata.ADMIN_NAME + " removed " + ChannelMetadata.USER_NAME);
 channelMetadata.setGroupNameChangeMessage(ChannelMetadata.USER_NAME + " changed group name " + ChannelMetadata.GROUP_NAME);
 channelMetadata.setJoinMemberMessage(ChannelMetadata.USER_NAME + " joined");
 channelMetadata.setGroupLeftMessage(ChannelMetadata.USER_NAME + " left group " + ChannelMetadata.GROUP_NAME);
 channelMetadata.setGroupIconChangeMessage(ChannelMetadata.USER_NAME + " changed icon");
 channelMetadata.setDeletedGroupMessage(ChannelMetadata.ADMIN_NAME + " deleted group " + ChannelMetadata.GROUP_NAME);
 ```
 
  Following place holders will be replaced

  ChannelMetadata.ADMIN_NAME : admin name of the group
  
  ChannelMetadata.USER_NAME  : user name of the user
  
  ChannelMetadata.GROUP_NAME : group name

 

Import

```
import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.channel.service.ChannelService;
```

Code
 ```
       List<String> channelMembersList =  new ArrayList<String>();
       channelMembersList.add("user1");
       channelMembersList.add("user2");
       channelMembersList.add("user3");//Note:while creating group exclude logged in userId from list
       ChannelInfo channelInfo  = new ChannelInfo("Group name",channelMembersList);
       channelInfo.setType(Channel.GroupType.PUBLIC.getValue().intValue()); //group type
       //channelInfo.setImageUrl(""); //pass group image link URL
       //channelInfo.setChannelMetadata(channelMetadata); //Optional option for setting group meta data 
       //channelInfo.setClientGroupId(clientGroupId); //Optional if you have your own groupId then you can pass here
        Channel channel = ChannelService.getInstance(context).createChannel(channelInfo);

 ```

#####2) Add Member to Group
  
Import
```
  import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelAddMemberTask;
```

Code
 ``` 
 ApplozicChannelAddMemberTask.ChannelAddMemberListener channelAddMemberListener =  new ApplozicChannelAddMemberTask.ChannelAddMemberListener() {
            @Override
            public void onSuccess(String response, Context context) {
                //Response will be "success" if user is added successfully
                Log.i("ApplozicChannelMember","Add Response:" + response);
            }

            @Override
            public void onFailure(String response, Exception e, Context context) {

            }
        };

        ApplozicChannelAddMemberTask applozicChannelAddMemberTask =  new ApplozicChannelAddMemberTask(context,channelKey,userId,channelAddMemberListener);//pass channel key and userId whom you want to add to channel
        applozicChannelAddMemberTask.execute((Void)null);

 ```
   
   
| Parameter | Description  |
| --------- | ------------ |
| channelKey | Unique identifier of the group/channel |
| userId | Unique identifier of the user |

 
##### 3) Remove Member from Group
 
Import  
```
import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelRemoveMemberTask;
```

Code
  ```
  ApplozicChannelRemoveMemberTask.ChannelRemoveMemberListener channelRemoveMemberListener = new ApplozicChannelRemoveMemberTask.ChannelRemoveMemberListener() {
            @Override
            public void onSuccess(String response, Context context) {
                //Response will be "success" if user is removed successfully
                Log.i("ApplozicChannel","remove member response:"+response);
            }

            @Override
            public void onFailure(String response, Exception e, Context context) {

            }
        };

  ApplozicChannelRemoveMemberTask applozicChannelRemoveMemberTask =  new ApplozicChannelRemoveMemberTask(context,channelKey,userId,channelRemoveMemberListener);//pass channelKey and userId whom you want to remove from channel
  applozicChannelRemoveMemberTask.execute((Void)null);
 ```
  
| Parameter | Description  |
| --------- | ------------ |
| channelKey | Unique identifier of the group/channel |
| userId | Unique identifier of the user |
 
 
  __NOTE:__ Only admin can remove member from the group/channel.
  
 
##### 4) Leave Group
 
Import
```
import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelLeaveMember;
```
  
Code
  ```
  ApplozicChannelLeaveMember.ChannelLeaveMemberListener  channelLeaveMemberListener  = new ApplozicChannelLeaveMember.ChannelLeaveMemberListener() {
            @Override
            public void onSuccess(String response, Context context) {
               //Response will be "success" if user is left successfully
                Log.i("ApplozicChannel","Leave member respone:"+response);
            }

            @Override
            public void onFailure(String response, Exception e, Context context) {

            }
        };

  ApplozicChannelLeaveMember applozicChannelLeaveMember = new ApplozicChannelLeaveMember(context,channelKey,userId,channelLeaveMemberListener);//pass channelKey and userId
  applozicChannelLeaveMember.execute((Void)null);
  ```
 
| Parameter | Description  |
| --------- | ------------ |
| channelKey | Unique identifier of the group/channel |
| userId | Unique identifier of the user |
 
 __Note:__ This is only for logged in user who want's to leave from group
 
##### 5) Change Group Name

Import

```
  
import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelNameUpdateTask;
```
  
Code

```
  ApplozicChannelNameUpdateTask.ChannelNameUpdateListener channelNameUpdateListener = new ApplozicChannelNameUpdateTask.ChannelNameUpdateListener() {
            @Override
            public void onSuccess(String response, Context context) {
                //Response will be "success" if Channel/Group name is changed successfully
                Log.i("ApplozicChannel", "Name update:" + response);
            }
            @Override
            public void onFailure(String response, Exception e, Context context) {

            }
        };

  ApplozicChannelNameUpdateTask channelNameUpdateTask = new ApplozicChannelNameUpdateTask(context, channelKey, channelName, channelNameUpdateListener);//pass context ,channelKey,chnanel new name 
  channelNameUpdateTask.execute((Void) null);

 ```
| Parameter | Description  |
| --------- | ------------ |
| channelKey | Unique identifier of the group/channel |
| userId | Unique identifier of the user




  
  
##### Open Activity on tap of toolbar in Chat Screen

  Add the following in your androidmanifest.xml
  
  Code
  ```
  <meta-data
             android:name="com.applozic.mobicomkit.uiwidgets.toolbar.tap.activity"
             android:value="PUT_ACTIVITY_CLASS_HERE" />
 ```

  This activity will receive the userId of the selected chat in intent.
  
  
#### Context Based Chat 



Setting need to be added in UserLoginTask onSuccess method


``` 

ApplozicClient.getInstance(context).setContextBasedChat(true);

```


When Starting ConversationActivity add  CONTEXT_BASED_CHAT flag in intent: 


```

Intent intent = new Intent(this, ConversationActivity.class);
intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT,true);
startActivity(intent);
 
```
 
 
 
Steps to create Context based chat 
 
 
##### Step 1 : Bulid Context chat Conversation
 
```
     private Conversation buildConversation() {
        
       //Title and subtitles are required if you are enabling the view for particular context.
       
        TopicDetail topic = new TopicDetail();
        topic.setTitle("Hyundai i20");//Your Topic title
        topic.setSubtitle("May be your car model");//Put Your Topic subtitle
        topic.setLink("Topic Image link if any");

       //You can set two Custom key-value pair which will appear on context view .

        topic.setKey1("Mileage  : ");
        topic.setValue1("18 kmpl");
        topic.setKey2("Price :");
        topic.setValue2("RS. 5.7 lakh");

        //Create Conversation.
        
        Conversation conversation = new Conversation();

        //SET UserId for which you want to launch chat or conversation

        conversation.setTopicId("Your Topic Id //unique ");
        conversation.setUserId("RECEIVER USERID");
        conversation.setTopicDetail(topic.getJson());
        return conversation;

    }
 ```
    
##### Step 2 : Create Async task and Starting Conversation chat

```
   ApplozicConversationCreateTask applozicConversationCreateTask = null;

   ApplozicConversationCreateTask.ConversationCreateListener conversationCreateListener =  new ApplozicConversationCreateTask.ConversationCreateListener() {
            @Override
            public void onSuccess(Integer conversationId, Context context) {

                //For launching the  one to one  chat
                Intent intent = new Intent(context, ConversationActivity.class);
                intent.putExtra("takeOrder", true);
                intent.putExtra(ConversationUIService.USER_ID, "userId");//RECEIVER USERID
                intent.putExtra(ConversationUIService.DEFAULT_TEXT, "Hello I am interested in this car, Can we chat?");
                intent.putExtra(ConversationUIService.DISPLAY_NAME,"display name");
                intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT,true);
                intent.putExtra(ConversationUIService.CONVERSATION_ID,conversationId);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e, Context context) {

            }
        };
    Conversation conversation = buildConversation(); //From Step 1 
applozicConversationCreateTask = new ApplozicConversationCreateTask(context,conversationCreateListener,conversation);
   applozicConversationCreateTask.execute((Void)null);
 
```
#### Audio/Video Call setup

Once you are done with [normal chat integration](https://www.applozic.com/docs/android-chat-sdk.html#overview), below steps you need to follow to enable audio/video call.

a) Enable audio/video feature: 

while doing user registration, you should set audio/video feature in user detail.

```
List<String> featureList =  new ArrayList<>();
featureList.add(User.Features.IP_AUDIO_CALL.getValue());// FOR AUDIO
featureList.add(User.Features.IP_VIDEO_CALL.getValue());// FOR VIDEO
user.setFeatures(featureList); // ADD FEATURES
```

b) Add setting for audio/video handler class:

onSuccess of UserLoginTask, you need to set below handlers in settings.

```
  ApplozicClient.getInstance(context).setHandleDial(true).setIPCallEnabled(true);
  Map<ApplozicSetting.RequestCode, String> activityCallbacks = new HashMap<ApplozicSetting.RequestCode, String>();
  activityCallbacks.put(ApplozicSetting.RequestCode.AUDIO_CALL, AudioCallActivityV2.class.getName());
  activityCallbacks.put(ApplozicSetting.RequestCode.VIDEO_CALL, VideoActivity.class.getName());
  ApplozicSetting.getInstance(context).setActivityCallbacks(activityCallbacks);
  
```

c) Add these activity in your AndroidManifest.xml.

```
<activity android:name="com.applozic.audiovideo.activity.AudioCallActivityV2"
            android:configChanges="keyboardHidden|orientation|screenSize"
            android:exported="true"
            android:launchMode="singleTop"
            android:theme="@style/Applozic_FullScreen_Theme"/>

        <activity
            android:name="com.applozic.audiovideo.activity.CallActivity"
            android:configChanges="orientation|keyboardHidden|screenSize"
            android:label="@string/app_name"
            android:launchMode="singleTop"
            android:theme="@style/Applozic_FullScreen_Theme"/>

        <activity
            android:name="com.applozic.audiovideo.activity.VideoActivity"
            android:launchMode="singleTop"
            android:configChanges="keyboardHidden|orientation|screenSize"
            android:exported="true"
            android:theme="@style/Applozic_FullScreen_Theme">
        </activity>
```



