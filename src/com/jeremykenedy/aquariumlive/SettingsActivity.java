package com.jeremykenedy.aquariumlive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/** Options screen, opened from the app tile or the system screensaver settings. */
public final class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null) {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new Options()).commit();
        }
    }

    public static final class Options extends PreferenceFragment {
        @Override
        public void onCreate(Bundle state) {
            super.onCreate(state);
            addPreferencesFromResource(R.xml.settings);
            wire();
        }

        private void wire() {
            findPreference("preview").setOnPreferenceClickListener(p -> {
                startActivity(new Intent(getActivity(), PreviewActivity.class));
                return true;
            });
            findPreference("reset").setOnPreferenceClickListener(p -> {
                getPreferenceManager().getSharedPreferences().edit().clear().apply();
                getPreferenceScreen().removeAll();
                addPreferencesFromResource(R.xml.settings);
                wire();
                return true;
            });
        }
    }
}
