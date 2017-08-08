package com.applozic.mobicomkit.uiwidgets.people.contact;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.RegisteredUsersAsyncTask;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicomkit.feed.RegisteredUsersApiResponse;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.applozic.mobicomkit.uiwidgets.people.activity.MobiComKitPeopleActivity;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.people.OnContactsInteractionListener;
import com.applozic.mobicommons.people.SearchListFragment;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by adarsh on 1/8/15.
 */

/**
 * Created by adarsh on 30/5/15.
 */
@SuppressLint("ValidFragment")
public class AppContactFragment extends ListFragment implements SearchListFragment,
        AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    static final String AL_CUSTOMIZATION_SETTINGS = "alCustomizationSettings";
    // Defines a tag for identifying log entries
    private static final String TAG = "AppContactFragment";
    private static final String SHARE_TEXT = "share_text";
    // Bundle key for saving previously selected search result item
    private static final String STATE_PREVIOUSLY_SELECTED_KEY =
            "net.mobitexter.mobiframework.contact.ui.SELECTED_ITEM";
    private static String inviteMessage;
    AlCustomizationSettings alCustomizationSettings;
    RefreshContactsScreenBroadcast refreshContactsScreenBroadcast;
    private ContactsAdapter mAdapter; // The main query adapter
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread
    private String mSearchTerm; // Stores the current search query term
    // Contact selected listener that allows the activity holding this fragment to be notified of
// a contact being selected
    private OnContactsInteractionListener mOnContactSelectedListener;
    // Stores the previously selected search item so that on a configuration change the same item
// can be reselected again
    private int mPreviouslySelectedSearchItem = 0;
    private BaseContactService contactService;
    private Button shareButton;
    private TextView resultTextView;
    private List<Contact> contactList;
    private boolean syncStatus = true;
    private String[] userIdArray;
    private MobiComUserPreference userPreference;
    private boolean isScrolling = false;
    private int visibleThreshold = 0;
    private int currentPage = 0;
    private int previousTotalItemCount = 0;
    private boolean loading = true;
    private int startingPageIndex = 0;
    private ContactDatabase contactDatabase;

    /**
     * Fragments require an empty constructor.
     */
    public AppContactFragment() {

    }

    public AppContactFragment(String[] userIdArray) {
        this.userIdArray = userIdArray;
    }


    public void setAlCustomizationSettings(AlCustomizationSettings alCustomizationSettings) {
        this.alCustomizationSettings = alCustomizationSettings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactDatabase = new ContactDatabase(getContext());
        contactService = new AppContactService(getActivity());
        mAdapter = new ContactsAdapter(getActivity().getApplicationContext());
        userPreference = MobiComUserPreference.getInstance(getContext());
        inviteMessage = Utils.getMetaDataValue(getActivity().getApplicationContext(), SHARE_TEXT);
        if (savedInstanceState != null) {
            mSearchTerm = savedInstanceState.getString(SearchManager.QUERY);
            mPreviouslySelectedSearchItem =
                    savedInstanceState.getInt(STATE_PREVIOUSLY_SELECTED_KEY, 0);
            alCustomizationSettings = (AlCustomizationSettings) savedInstanceState.getSerializable(AL_CUSTOMIZATION_SETTINGS);
        }
        refreshContactsScreenBroadcast = new RefreshContactsScreenBroadcast();
        final Context context = getActivity().getApplicationContext();
        mImageLoader = new ImageLoader(context, getListPreferredItemHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage(context, (Contact) data);
            }
        };
        // Set a placeholder loading image for the image loader
        mImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_holo_light);
        // Add a cache to the image loader
        mImageLoader.addImageCache(getActivity().getSupportFragmentManager(), 0.1f);
        mImageLoader.setImageFadeIn(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the list fragment layout
        View view = inflater.inflate(R.layout.contact_list_fragment, container, false);
        shareButton = (Button) view.findViewById(R.id.actionButton);
        shareButton.setVisibility(alCustomizationSettings.isInviteFriendsInContactActivity() ? View.VISIBLE : View.GONE);
        resultTextView = (TextView) view.findViewById(R.id.result);
        return view;
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        shareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND)
                        .setType("text/plain").putExtra(Intent.EXTRA_TEXT, inviteMessage);

                List<Intent> targetedShareIntents = new ArrayList<Intent>();

                List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentActivities(intent, 0);
                if (!resInfo.isEmpty()) {
                    for (ResolveInfo resolveInfo : resInfo) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        Intent targetedShareIntent = new Intent(Intent.ACTION_SEND);
                        targetedShareIntent.setType("text/plain")
                                .setAction(Intent.ACTION_SEND)
                                .putExtra(Intent.EXTRA_TEXT, inviteMessage)
                                .setPackage(packageName);
                        targetedShareIntents.add(targetedShareIntent);
                    }
                    Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Share Via");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                    startActivity(chooserIntent);
                }
            }
        });

        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause image loader to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageLoader.setPauseWork(true);
                    Utils.toggleSoftKeyBoard(getActivity(), true);
                } else {
                    mImageLoader.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemsCount) {
                if ((alCustomizationSettings.isRegisteredUserContactListCall() || ApplozicSetting.getInstance(getActivity()).isRegisteredUsersContactCall()) && Utils.isInternetAvailable(getActivity().getApplicationContext())) {
                    if (totalItemsCount < previousTotalItemCount) {
                        currentPage = startingPageIndex;
                        previousTotalItemCount = totalItemsCount;
                        if (totalItemsCount == 0) {
                            loading = true;
                        } else {
                            loading = false;

                        }
                    }

                    if (loading && (totalItemsCount > previousTotalItemCount)) {
                        loading = false;
                        previousTotalItemCount = totalItemsCount;
                        currentPage++;
                    }

                    if (totalItemsCount - visibleItemCount == 0) {
                        return;
                    }

                    if (totalItemsCount <= 5) {
                        return;
                    }

                    if (!loading && (totalItemsCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                        if (!MobiComKitPeopleActivity.isSearching) {
                            loading = true;
                            processLoadRegisteredUsers();
                        }
                    }
                }
            }
        });

        // If there's a previously selected search item from a saved state then don't bother
        // initializing the loader as it will be restarted later when the query is populated into
        // the action bar search view (see onQueryTextChange() in onCreateOptionsMenu()).
        if (mPreviouslySelectedSearchItem == 0) {
            // Initialize the loader, and create a loader identified by ContactsQuery.QUERY_ID
            getLoaderManager().initLoader(ContactsQuery.QUERY_ID, null, this);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            // Assign callback listener which the holding activity must implement. This is used
            // so that when a contact item is interacted with (selected by the user) the holding
            // activity will be notified and can take further action such as populating the contact
            // detail pane (if in multi-pane layout) or starting a new activity with the contact
            // details (single pane layout).
            mOnContactSelectedListener = (OnContactsInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnContactsInteractionListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // In the case onPause() is called during a fling the image loader is
        // un-paused to let any remaining background work complete.
        mImageLoader.setPauseWork(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        final Cursor cursor = mAdapter.getCursor();

        // Moves to the Cursor row corresponding to the ListView item that was clicked
        cursor.moveToPosition(position);
        Contact contact = contactDatabase.getContact(cursor, "_id");
        mOnContactSelectedListener.onCustomContactSelected(contact);
    }

    /**
     * Called when ListView selection is cleared, for example
     * when search mode is finished and the currently selected
     * contact should no longer be selected.
     */
    @SuppressLint("NewApi")
    private void onSelectionCleared() {
        // Uses callback to notify activity this contains this fragment
        mOnContactSelectedListener.onSelectionCleared();
        // Clears currently checked item
        getListView().clearChoices();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(mSearchTerm)) {
            // Saves the current search string
            outState.putString(SearchManager.QUERY, mSearchTerm);
        }
        if (alCustomizationSettings != null) {
            outState.putSerializable(AL_CUSTOMIZATION_SETTINGS, alCustomizationSettings);
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Updates
        // the search filter, and restarts the loader to do a new query
        // using the new search string.
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;

        // Don't do anything if the filter is empty

        // Updates current filter to new filter
        mSearchTerm = newFilter;
        mAdapter.indexOfSearchQuery(newFilter);

        getLoaderManager().restartLoader(
                AppContactFragment.ContactsQuery.QUERY_ID, null, AppContactFragment.this);

        return true;
    }

    /**
     * Gets the preferred height for each item in the ListView, in pixels, after accounting for
     * screen density. ImageLoader uses this value to resize thumbnail images to match the ListView
     * item height.
     *
     * @return The preferred height in pixels, based on the current theme.
     */
    private int getListPreferredItemHeight() {
        final TypedValue typedValue = new TypedValue();

        // Resolve list item preferred height theme attribute into typedValue
        getActivity().getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, typedValue, true);

// Create a new DisplayMetrics object
        final DisplayMetrics metrics = new DisplayMetrics();

        // Populate the DisplayMetrics
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Return theme value based on DisplayMetrics
        return (int) typedValue.getDimension(metrics);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Loader<Cursor> loader = contactDatabase.getSearchCursorLoader(mSearchTerm, userIdArray);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // This swaps the new cursor into the adapter.
        if (loader.getId() == ContactsQuery.QUERY_ID) {
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if (loader.getId() == ContactsQuery.QUERY_ID) {
            // When the loader is being reset, clear the cursor from the adapter. This allows the
            // cursor resources to be freed.
            mAdapter.swapCursor(null);
        }
    }

    public void processLoadRegisteredUsers() {

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "",
                getActivity().getString(R.string.applozic_contacts_loading_info), true);

        RegisteredUsersAsyncTask.TaskListener usersAsyncTaskTaskListener = new RegisteredUsersAsyncTask.TaskListener() {
            @Override
            public void onSuccess(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                try {
                    if (registeredUsersApiResponse != null) {
                        getLoaderManager().restartLoader(
                                AppContactFragment.ContactsQuery.QUERY_ID, null, AppContactFragment.this);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray, Exception exception) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                String error = getString(Utils.isInternetAvailable(getActivity()) ? R.string.applozic_server_error : R.string.you_need_network_access_for_block_or_unblock);
                Toast toast = Toast.makeText(getActivity(), error, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            @Override
            public void onCompletion() {

            }
        };
        RegisteredUsersAsyncTask usersAsyncTask = new RegisteredUsersAsyncTask(getActivity(), usersAsyncTaskTaskListener, alCustomizationSettings.getTotalRegisteredUserToFetch(), userPreference.getRegisteredUsersLastFetchTime(), null, null, true);
        usersAsyncTask.execute((Void) null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (refreshContactsScreenBroadcast != null) {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshContactsScreenBroadcast, new IntentFilter(BroadcastService.INTENT_ACTIONS.UPDATE_USER_DETAIL.toString()));
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (refreshContactsScreenBroadcast != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(refreshContactsScreenBroadcast);
        }
    }


    /**
     * This interface defines constants for the Cursor and CursorLoader, based on constants defined
     * in the {@link android.provider.ContactsContract.Contacts} class.
     */
    public interface ContactsQuery {
        // An identifier for the loader
        int QUERY_ID = 1;

    }

    /**
     * This is a subclass of CursorAdapter that supports binding Cursor columns to a view layout.
     * If those items are part of search results, the search string is marked by highlighting the
     * query text. An {@link android.widget.AlphabetIndexer} is used to allow quicker navigation up and down the
     * ListView.
     */
    private class ContactsAdapter extends CursorAdapter implements SectionIndexer {
        Context context;
        private LayoutInflater mInflater; // Stores the layout inflater
        private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance
        private TextAppearanceSpan highlightTextSpan; // Stores the highlight text appearance style

        /**
         * Instantiates a new Contacts Adapter.
         *
         * @param context A context that has access to the app's layout.
         */
        public ContactsAdapter(Context context) {
            super(context, null, 0);
            this.context = context;
            // Stores inflater for use later
            mInflater = LayoutInflater.from(context);
            // Loads a string containing the English alphabet. To fully localize the app, provide a
            // strings.xml file in res/values-<x> directories, where <x> is a locale. In the file,
            // define a string with android:name="alphabet" and contents set to all of the
            // alphabetic characters in the language in their proper sort order, in upper case if
            // applicable.
            final String alphabet = context.getString(R.string.alphabet);

            // Instantiates a new AlphabetIndexer bound to the column used to sort contact names.
            // The cursor is left null, because it has not yet been retrieved.
            mAlphabetIndexer = new AlphabetIndexer(null, 1, alphabet);

            // Defines a span for highlighting the part of a display name that matches the search
            // string
            highlightTextSpan = new TextAppearanceSpan(getActivity(), R.style.searchTextHiglight);
        }

        /**
         * Identifies the start of the search string in the display name column of a Cursor row.
         * E.g. If displayName was "Adam" and search query (mSearchTerm) was "da" this would
         * return 1.
         *
         * @param displayName The contact display name.
         * @return The starting position of the search string in the display name, 0-based. The
         * method returns -1 if the string is not found in the display name, or if the search
         * string is empty or null.
         */
        private int indexOfSearchQuery(String displayName) {
            if (!TextUtils.isEmpty(mSearchTerm)) {
                return displayName.toLowerCase(Locale.getDefault()).indexOf(
                        mSearchTerm.toLowerCase(Locale.getDefault()));
            }
            return -1;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View itemLayout =
                    mInflater.inflate(R.layout.contact_list_item, parent, false);

            final ViewHolder holder = new ViewHolder();
            holder.text1 = (TextView) itemLayout.findViewById(R.id.text1);
            holder.text2 = (TextView) itemLayout.findViewById(R.id.text2);
            holder.contactNumberTextView = (TextView) itemLayout.findViewById(R.id.contactNumberTextView);
            holder.icon = (CircleImageView) itemLayout.findViewById(R.id.contactImage);
            holder.contactIcon = (TextView) itemLayout.findViewById(R.id.contactIcon);
            itemLayout.setTag(holder);
            return itemLayout;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            RelativeLayout.LayoutParams layoutParams;
            String contactNumber;
            char firstLetter = 0;

            // Gets handles to individual view resources
            final ViewHolder holder = (ViewHolder) view.getTag();

            //////////////////////////
            Contact contact = contactDatabase.getContact(cursor, "_id");
            ///////////////////

            holder.text1.setText(contact.getDisplayName() == null ? contact.getUserId() : contact.getDisplayName());
            holder.text2.setText(contact.getUserId());
            if (contact != null && !TextUtils.isEmpty(contact.getDisplayName())) {
                contactNumber = contact.getDisplayName().toUpperCase();
                firstLetter = contact.getDisplayName().toUpperCase().charAt(0);
                if (firstLetter != '+') {
                    holder.contactIcon.setText(String.valueOf(firstLetter));
                } else if (contactNumber.length() >= 2) {
                    holder.contactIcon.setText(String.valueOf(contactNumber.charAt(1)));
                }
                Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
                GradientDrawable bgShape = (GradientDrawable) holder.contactIcon.getBackground();
                bgShape.setColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
            }
            holder.contactIcon.setVisibility(View.GONE);
            holder.icon.setVisibility(View.VISIBLE);
            if (contact != null) {
                if (contact.isDrawableResources()) {
                    int drawableResourceId = context.getResources().getIdentifier(contact.getrDrawableName(), "drawable", context.getPackageName());
                    holder.icon.setImageResource(drawableResourceId);
                } else {
                    mImageLoader.loadImage(contact, holder.icon, holder.contactIcon);
                }
            }
            if (!TextUtils.isEmpty(contact.getContactNumber())) {
                layoutParams = (RelativeLayout.LayoutParams) holder.text1.getLayoutParams();
                layoutParams.setMargins(0, 20, 0, 0);
                holder.text1.setLayoutParams(layoutParams);
                holder.contactNumberTextView.setVisibility(View.VISIBLE);
                holder.contactNumberTextView.setText(contact.getContactNumber());

            } else {
                holder.text2.setVisibility(View.GONE);
                holder.contactNumberTextView.setVisibility(View.GONE);
                layoutParams = (RelativeLayout.LayoutParams) holder.text1.getLayoutParams();
                layoutParams.setMargins(0, 50, 0, 0);
                holder.text1.setLayoutParams(layoutParams);
            }
            // Returns the item layout view


            ///////////////////////
            final int startIndex = indexOfSearchQuery(contact.getDisplayName());

            if (startIndex == -1) {
                // If the user didn't do a search, or the search string didn't match a display
                // name, show the display name without highlighting
                holder.text1.setText(contact.getDisplayName());

                if (TextUtils.isEmpty(mSearchTerm)) {
                    // If the search search is empty, hide the second line of text
                    holder.text2.setVisibility(View.GONE);
                } else {
                    // Shows a second line of text that indicates the search string matched
                    // something other than the display name
                    holder.text2.setVisibility(View.VISIBLE);
                }
            } else {
                // If the search string matched the display name, applies a SpannableString to
                // highlight the search string with the displayed display name

                // Wraps the display name in the SpannableString
                final SpannableString highlightedName = new SpannableString(contact.getDisplayName());

                // Sets the span to start at the starting point of the match and end at "length"
                // characters beyond the starting point
                highlightedName.setSpan(highlightTextSpan, startIndex,
                        startIndex + mSearchTerm.length(), 0);

                // Binds the SpannableString to the display name View object
                holder.text1.setText(highlightedName);

                // Since the search string matched the name, this hides the secondary message
                holder.text2.setVisibility(View.GONE);
            }

        }

        /**
         * Overrides swapCursor to move the new Cursor into the AlphabetIndex as well as the
         * CursorAdapter.
         */
        @Override
        public Cursor swapCursor(Cursor newCursor) {
            // Update the AlphabetIndexer with new cursor as well
            mAlphabetIndexer.setCursor(newCursor);
            return super.swapCursor(newCursor);
        }

        /**
         * An override of getCount that simplifies accessing the Cursor. If the Cursor is null,
         * getCount returns zero. As a result, no test for Cursor == null is needed.
         */
        @Override
        public int getCount() {
            if (getCursor() == null) {
                return 0;
            }
            return super.getCount();
        }

        /**
         * Defines the SectionIndexer.getSections() interface.
         */
        @Override
        public Object[] getSections() {
            return mAlphabetIndexer.getSections();
        }

        /**
         * Defines the SectionIndexer.getPositionForSection() interface.
         */
        @Override
        public int getPositionForSection(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getPositionForSection(i);
        }

        /**
         * Defines the SectionIndexer.getSectionForPosition() interface.
         */
        @Override
        public int getSectionForPosition(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getSectionForPosition(i);
        }

        /**
         * A class that defines fields for each resource ID in the list item layout. This allows
         * ContactsAdapter.newView() to store the IDs once, when it inflates the layout, instead of
         * calling findViewById in each iteration of bindView.
         */
        private class ViewHolder {
            TextView text1;
            TextView text2;
            CircleImageView icon;
            TextView contactIcon;
            TextView contactNumberTextView;
        }
    }

    private final class RefreshContactsScreenBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && BroadcastService.INTENT_ACTIONS.UPDATE_USER_DETAIL.toString().equals(intent.getAction())) {
                try {
                    if (getLoaderManager() != null && userIdArray == null) {
                        getLoaderManager().restartLoader(
                                AppContactFragment.ContactsQuery.QUERY_ID, null, AppContactFragment.this);
                    }
                } catch (Exception e) {

                }
            }
        }
    }

}

