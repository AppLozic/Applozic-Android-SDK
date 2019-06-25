package com.applozic.mobicomkit.uiwidgets;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Preference screen.
 */
public class SpellCheckerSettingsFragment extends PreferenceFragment {

    /**
     * Empty constructor for fragment generation.
     */
    public SpellCheckerSettingsFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.spell_checker_settings);
    }
}
