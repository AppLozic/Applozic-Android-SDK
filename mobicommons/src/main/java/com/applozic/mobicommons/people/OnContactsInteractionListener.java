package com.applozic.mobicommons.people;

import android.net.Uri;

import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.people.channel.Channel;

/**
 * This interface must be implemented by any activity that loads this fragment. When an
 * interaction occurs, such as touching an item from the ListView, these callbacks will
 * be invoked to communicate the event back to the activity.
 */
public interface OnContactsInteractionListener {
    /**
     * Called when a contact is selected from the ListView.
     *
     * @param contactUri The contact Uri.
     */
    void onContactSelected(Uri contactUri);

    void onGroupSelected(Channel channel);

    void onCustomContactSelected(Contact contact);

    /**
     * Called when the ListView selection is cleared like when
     * a contact search is taking place or is finishing.
     */
    void onSelectionCleared();
}