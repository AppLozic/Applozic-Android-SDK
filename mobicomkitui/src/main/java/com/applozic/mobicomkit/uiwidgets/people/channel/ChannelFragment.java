package com.applozic.mobicomkit.uiwidgets.people.channel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.Button;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.applozic.mobicomkit.channel.database.ChannelDatabaseService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.people.OnContactsInteractionListener;
import com.applozic.mobicommons.people.SearchListFragment;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sunil on 28/1/16.
 */
public class ChannelFragment extends ListFragment implements
        AdapterView.OnItemClickListener, SearchListFragment, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String SHARE_TEXT = "share_text";
    static int QUERY_ID = 1;
    private static String inviteMessage;
    String mSearchTerm;
    ImageLoader mChannelImageLoader;
    BaseContactService baseContactService;
    private ChannelAdapter mAdapter; // The main query adapter
    // Contact selected listener that allows the activity holding this fragment to be notified of
// a contact being selected
    private OnContactsInteractionListener mOnChannelSelectedListener;
    private Button shareButton;
    private TextView resultTextView;
    private boolean syncStatus = true;
    private int mPreviouslySelectedSearchItem = 0;

    public ChannelFragment() {

    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inviteMessage = Utils.getMetaDataValue(getActivity().getApplicationContext(), SHARE_TEXT);
        baseContactService = new AppContactService(getActivity());
        mAdapter = new ChannelAdapter(getActivity().getApplicationContext());

        if (savedInstanceState != null) {
            mSearchTerm = savedInstanceState.getString(SearchManager.QUERY);
        }
        final Context context = getActivity().getApplicationContext();
        mChannelImageLoader = new ImageLoader(context, getListPreferredItemHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return baseContactService.downloadGroupImage(context, (Channel) data);
            }
        };
        // Set a placeholder loading image for the image loader
        mChannelImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_holo_light);
        // Add a cache to the image loader
        mChannelImageLoader.addImageCache(getActivity().getSupportFragmentManager(), 0.1f);
        mChannelImageLoader.setImageFadeIn(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        final Cursor cursor = mAdapter.getCursor();

        // Moves to the Cursor row corresponding to the ListView item that was clicked
        cursor.moveToPosition(position);
        Channel channel = ChannelDatabaseService.getInstance(getContext()).getChannel(cursor);
        mOnChannelSelectedListener.onGroupSelected(channel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the list fragment layout
        View view = inflater.inflate(R.layout.contact_list_fragment, container, false);
        shareButton = (Button) view.findViewById(R.id.actionButton);
        shareButton.setVisibility(View.GONE);
        resultTextView = (TextView) view.findViewById(R.id.result);
        resultTextView.setText(getString(R.string.no_groups));
        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mOnChannelSelectedListener = (OnContactsInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnContactsInteractionListener");
        }
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
                mChannelImageLoader.setPauseWork(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING);
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });

        // the action bar search view (see onQueryTextChange() in onCreateOptionsMenu()).
        if (mPreviouslySelectedSearchItem == 0) {
            // Initialize the loader, and create a loader identified by ContactsQuery.QUERY_ID
            getLoaderManager().initLoader(QUERY_ID, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = ChannelDatabaseService.getInstance(getActivity()).getSearchCursorForGroupsLoader(mSearchTerm);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // This swaps the new cursor into the adapter.
        if (loader.getId() == QUERY_ID) {
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if (loader.getId() == QUERY_ID) {
            // When the loader is being reset, clear the cursor from the adapter. This allows the
            // cursor resources to be freed.
            mAdapter.swapCursor(null);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(mSearchTerm)) {
            // Saves the current search string
            outState.putString(SearchManager.QUERY, mSearchTerm);
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Updates
        // the search filter, and restarts the loader to do a new query
        // using the new search string.
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;

        // Updates current filter to new filter
        mSearchTerm = newFilter;
        if(mAdapter != null){
            mAdapter.indexOfSearchQuery(newFilter);
        }
        getLoaderManager().restartLoader(
                QUERY_ID, null, ChannelFragment.this);

        return true;
    }

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
    public void onPause() {
        super.onPause();
        if (mChannelImageLoader != null) {
            mChannelImageLoader.setPauseWork(false);
        }
    }

    private class ChannelAdapter extends CursorAdapter implements SectionIndexer {
        Context context;
        private LayoutInflater mInflater; // Stores the layout inflater
        private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance
        private TextAppearanceSpan highlightTextSpan; // Stores the highlight text appearance style

        /**
         * Instantiates a new Contacts Adapter.
         *
         * @param context A context that has access to the app's layout.
         */
        public ChannelAdapter(Context context) {
            super(context, null, 0);
            this.context = context;
            // Stores inflater for use later
            mInflater = LayoutInflater.from(context);
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
                    mInflater.inflate(R.layout.applozic_groups_list_layout, parent, false);

            final ViewHolder holder = new ViewHolder();
            holder.groupName = (TextView) itemLayout.findViewById(R.id.applozic_group_name);
            holder.totalmembers = (TextView) itemLayout.findViewById(R.id.applozic_group_members);
            holder.groupIcon = (CircleImageView) itemLayout.findViewById(R.id.contactImage);
            itemLayout.setTag(holder);
            return itemLayout;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            // Gets handles to individual view resources
            final ViewHolder holder = (ViewHolder) view.getTag();

            //////////////////////////
            Channel channel = ChannelDatabaseService.getInstance(getContext()).getChannel(cursor);

            ///////////////////


            if (!TextUtils.isEmpty(channel.getImageUrl())) {
                mChannelImageLoader.loadImage(channel, holder.groupIcon);
            } else if (channel.isBroadcastMessage()) {
                holder.groupIcon.setImageResource(R.drawable.applozic_ic_applozic_broadcast);
            } else {
                holder.groupIcon.setImageResource(R.drawable.applozic_group_icon);
            }

            // Returns the item layout view

            ///////////////////////
            final int startIndex = indexOfSearchQuery(channel.getName());

            if (startIndex == -1) {
                // If the user didn't do a search, or the search string didn't match a display
                // name, show the display name without highlighting
                holder.groupName.setText(channel.getName());

                if (TextUtils.isEmpty(mSearchTerm)) {
                    // If the search search is empty, hide the second line of text
                    holder.totalmembers.setVisibility(View.GONE);
                } else {
                    // Shows a second line of text that indicates the search string matched
                    // something other than the display name
                    holder.totalmembers.setVisibility(View.VISIBLE);
                }
            } else {
                // If the search string matched the display name, applies a SpannableString to
                // highlight the search string with the displayed display name

                // Wraps the display name in the SpannableString
                final SpannableString highlightedName = new SpannableString(channel.getName());

                // Sets the span to start at the starting point of the match and end at "length"
                // characters beyond the starting point
                highlightedName.setSpan(highlightTextSpan, startIndex,
                        startIndex + mSearchTerm.length(), 0);

                // Binds the SpannableString to the display name View object
                holder.groupName.setText(highlightedName);

                // Since the search string matched the name, this hides the secondary message
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
            TextView groupName;
            TextView totalmembers;
            CircleImageView groupIcon;
        }
    }

}
