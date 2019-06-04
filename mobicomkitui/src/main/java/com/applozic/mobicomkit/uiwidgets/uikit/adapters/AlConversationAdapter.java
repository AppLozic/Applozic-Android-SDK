package com.applozic.mobicomkit.uiwidgets.uikit.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.uikit.AlMessageProperties;
import com.applozic.mobicomkit.uiwidgets.uikit.AlUIService;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ashish on 28/05/18.
 */

public class AlConversationAdapter extends AlFooterAdapter implements AdapterView.OnItemClickListener {
    public Context context;
    private AlMessageProperties messageProperties;

    public AlConversationAdapter(Context context, List<Message> mList) {
        super(context, mList);
        this.context = context;
        messageProperties = new AlMessageProperties(context);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        messageProperties.handleConversationClick(mItems.get(position));
    }

    @Override
    public RecyclerView.ViewHolder getConversationViewHolder(ViewGroup parent) {
        return new ConversationViewHolder(mInflater.inflate(R.layout.al_conversation_item_layout, parent, false));
    }

    @Override
    public void bindConversationViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ConversationViewHolder) {
            Message message = mItems.get(position);
            messageProperties.setMessage(message);

            ConversationViewHolder mHolder = (ConversationViewHolder) holder;

            mHolder.receiverName.setText(messageProperties.getReceiver());
            mHolder.createdAtTime.setText(messageProperties.getCreatedAtTime());
            messageProperties.setMessageAndAttchmentIcon(mHolder.messageTv, mHolder.attachmentIcon);
            messageProperties.setUnreadCount(mHolder.unreadCount);
            messageProperties.loadProfileImage(mHolder.profileImage, mHolder.alphabeticImage);
        }
    }

    public class ConversationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

        TextView alphabeticImage;
        CircleImageView profileImage;
        TextView receiverName;
        TextView messageTv;
        TextView unreadCount;
        TextView createdAtTime;
        ImageView attachmentIcon;
        AlMessageProperties properties;
        AlUIService uiService;

        public ConversationViewHolder(View view) {
            super(view);
            alphabeticImage = view.findViewById(R.id.alphabeticImage);
            profileImage = view.findViewById(R.id.contactImage);
            receiverName = view.findViewById(R.id.smReceivers);
            messageTv = view.findViewById(R.id.message);
            unreadCount = view.findViewById(R.id.unreadSmsCount);
            createdAtTime = view.findViewById(R.id.createdAtTime);
            attachmentIcon = view.findViewById(R.id.attachmentIcon);

            properties = new AlMessageProperties(context);
            uiService = new AlUIService(context);

            view.setOnClickListener(this);
            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            int itemPosition = getLayoutPosition();

            if (itemPosition != -1 && !mItems.isEmpty()) {
                Message message = mItems.get(itemPosition);

                if (message != null) {
                    messageProperties.handleConversationClick(message);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = this.getLayoutPosition();
            if (mItems.size() <= position) {
                return;
            }
            Message message = mItems.get(position);
            menu.setHeaderTitle(R.string.conversation_options);

            String[] menuItems = context.getResources().getStringArray(R.array.conversation_options_menu);

            properties.setMessage(message);

            boolean isChannelDeleted = properties.getChannel() != null && properties.getChannel().isDeleted();
            boolean isUserPresentInGroup = properties.getChannel() != null && ChannelService.getInstance(context).processIsUserPresentInChannel(properties.getChannel().getKey());

            for (int i = 0; i < menuItems.length; i++) {

                if ((message.getGroupId() == null || (properties.getChannel() != null && (Channel.GroupType.GROUPOFTWO.getValue().equals(properties.getChannel().getType()) || Channel.GroupType.SUPPORT_GROUP.getValue().equals(properties.getChannel().getType())))) && (menuItems[i].equals(context.getResources().getString(R.string.delete_group)) ||
                        menuItems[i].equals(context.getResources().getString(R.string.exit_group)))) {
                    continue;
                }

                if (menuItems[i].equals(context.getResources().getString(R.string.exit_group)) && (isChannelDeleted || !isUserPresentInGroup)) {
                    continue;
                }

                if (menuItems[i].equals(context.getResources().getString(R.string.delete_group)) && (isUserPresentInGroup || !isChannelDeleted)) {
                    continue;
                }
                if (menuItems[i].equals(context.getResources().getString(R.string.delete_conversation))) {
                    continue;
                }


                MenuItem item = menu.add(Menu.NONE, i, i, menuItems[i]);
                item.setOnMenuItemClickListener(onEditMenu);
            }
        }

        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 0:
                        if (properties.getChannel() != null && properties.getChannel().isDeleted()) {
                            uiService.deleteGroupConversation(properties.getChannel());
                        } else {
                            uiService.deleteConversationThread(properties.getContact(), properties.getChannel());
                        }
                        break;
                    case 1:
                        uiService.deleteGroupConversation(properties.getChannel());
                        break;
                    case 2:
                        uiService.channelLeaveProcess(properties.getChannel());
                        break;

                }
                return true;
            }
        };
    }


}
