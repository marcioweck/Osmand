package net.osmand.plus.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;

/**
 * Created by Alexey Pelykh on 02.02.2015.
 */
public class PluginActivity extends OsmandActionBarActivity {
	private static final String TAG = "PluginActivity";
	public static final String EXTRA_PLUGIN_ID = "plugin_id";

	private OsmandPlugin plugin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent == null || !intent.hasExtra(EXTRA_PLUGIN_ID)) {
			Log.e(TAG, "Required extra '" + EXTRA_PLUGIN_ID + "' is missing");
			finish();
			return;
		}
		String pluginId = intent.getStringExtra(EXTRA_PLUGIN_ID);
		if (pluginId == null) {
			Log.e(TAG, "Extra '" + EXTRA_PLUGIN_ID + "' is null");
			finish();
			return;
		}
		for (OsmandPlugin plugin : OsmandPlugin.getAvailablePlugins()) {
			if (!plugin.getId().equals(pluginId))
				continue;

			this.plugin = plugin;
			break;
		}
		if (plugin == null) {
			Log.e(TAG, "Plugin '" + EXTRA_PLUGIN_ID + "' not found");
			finish();
			return;
		}

		setContentView(R.layout.plugin);
		getSupportActionBar().setTitle(plugin.getName());

		TextView descriptionView = (TextView)findViewById(R.id.plugin_description);
		descriptionView.setText(plugin.getDescription());

		Button settingsButton = (Button)findViewById(R.id.plugin_settings);
		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(PluginActivity.this, plugin.getSettingsActivity()));
			}
		});

		CompoundButton enableDisableButton = (CompoundButton)findViewById(
				R.id.plugin_enable_disable);
		enableDisableButton.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (plugin.isActive() == isChecked) {
							return;
						}

						boolean ok = OsmandPlugin.enablePlugin((OsmandApplication)getApplication(),
								plugin, isChecked);
						if (!ok) {
							return;
						}
						updateState();
					}
				});
		Button getButton = (Button)findViewById(R.id.plugin_get);
		getButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(plugin.getInstallURL())));
			}
		});

		updateState();
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case android.R.id.home:
				finish();
				return true;

		}
		return false;
	}

	private void updateState() {
		CompoundButton enableDisableButton = (CompoundButton)findViewById(
				R.id.plugin_enable_disable);
		Button getButton = (Button)findViewById(R.id.plugin_get);
		Button settingsButton = (Button)findViewById(R.id.plugin_settings);
		View installHeader = findViewById(R.id.plugin_install_header);

		if (plugin.needsInstallation()) {
			getButton.setVisibility(View.VISIBLE);
			enableDisableButton.setVisibility(View.GONE);
			settingsButton.setVisibility(View.GONE);
			installHeader.setVisibility(View.VISIBLE);
		} else {
			getButton.setVisibility(View.GONE);
			enableDisableButton.setVisibility(View.VISIBLE);
			enableDisableButton.setChecked(plugin.isActive());

			final Class<? extends Activity> settingsActivity = plugin.getSettingsActivity();
			if (settingsActivity == null) {
				settingsButton.setVisibility(View.GONE);
			} else {
				settingsButton.setEnabled(plugin.isActive());
				settingsButton.setVisibility(View.VISIBLE);
			}

			installHeader.setVisibility(View.GONE);
		}
	}
}