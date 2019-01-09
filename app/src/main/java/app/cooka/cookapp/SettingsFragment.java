package app.cooka.cookapp;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;


public class SettingsFragment extends PreferenceFragmentCompat {
    private CheckBoxPreference tutorialCheckbox;
    private CheckBoxPreference soundCheckBox;
    private Preference btnLogout;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        btnLogout = getPreferenceManager().findPreference("btnLogout");
        tutorialCheckbox = (CheckBoxPreference) getPreferenceManager().findPreference("tutorialCheckBox");
        soundCheckBox = (CheckBoxPreference) getPreferenceManager().findPreference("soundCheckBox");

        soundCheckBox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                return true;
            }
        });

        tutorialCheckbox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                return true;
            }
        });
    }
}
