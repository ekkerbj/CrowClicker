package com.example.clicker;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.clicker.databinding.ActivityPointBinding;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class PointActivity extends AppCompatActivity {

    private static final String TAG = "PointActivity";
    private Point point;
    private ActivityPointBinding binding;
    private PointsHelper helper;
    private SheetAccess sheets;
    private boolean shouldNotifyDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPointBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        helper = new PointsHelper(this);
        point = getIntent().getParcelableExtra("point");
        shouldNotifyDefault = getIntent().getBooleanExtra("shouldNotify", false);
        binding.setPoint(point);
        sheets = new SheetAccess(this);

        populateForm();
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("point", point);
        setResult(RESULT_OK, returnIntent);
        super.finish();
    }

    public void cancelButton(View v) {
        finish();
    }

    public void saveButton(View v) {
        boolean shouldNotify = updatePoint();
        helper.addOrUpdatePoint(point);
        if (shouldNotify) {
            sendMessage(point.getMessage(), point.getContactType());
        }
        finish();
    }

    public void deleteButton(View v) {
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(this);
        dialogDelete.setTitle("Warning!!");
        dialogDelete.setMessage("Are you sure to delete this point?");
        dialogDelete.setPositiveButton("Yes", (dialogInterface, i) -> {
            helper.deletePoint(point.getId());
            sheets.deletePoint(point);
            dialogInterface.dismiss();
            finish();
        });
        dialogDelete.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        dialogDelete.show();
    }

    public void pushButton(View v) {
        updatePoint();
        if (point.getSheetId() <= 0) {
            point.setSheetId(point.getId());
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        sheets.storePoint(point, prefs.getString("Lake", ""));
        finish();
    }

    public void weatherButton(View v) {
        updatePoint();
        try {
            final Weather weather = new Weather();
            weather.populate(point.getLat(), point.getLon(), point.getTimeStamp(), this, new VolleyCallBack() {
                @Override
                public void onSuccess() {
                    point.setAirTemp(weather.temperature);
                    point.setDewPoint(weather.dewPoint);
                    point.setWindSpeed(weather.windSpeed);
                    point.setHumidity(weather.humidity);
                    point.setPressure(weather.pressure);
                    point.setCloudCover(weather.cloudCover);
                    point.setWindDir(weather.windDir);
                    point.setWindGust(weather.windGust);
                    point.setPrecipProbability(weather.precipProbability);
                    helper.addOrUpdatePoint(point);
                    Toast.makeText(getApplicationContext(), "Weather data updated.", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(getApplicationContext(), "Unable to retrieve weather data.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failure to update weather", e);
        }
    }

    private void populateForm() {
        String[] contactTypes = ContactType.asStringArray();
        ArrayAdapter<String> contactAdapter = new ArrayAdapter<>(binding.getRoot().getContext(), android.R.layout.simple_spinner_item, contactTypes);
        contactAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.contactType.setAdapter(contactAdapter);
        binding.contactType.setSelection(Arrays.asList(contactTypes).indexOf(point.getContactType()));

        String[] baits = getResources().getStringArray(R.array.bait_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(binding.getRoot().getContext(), android.R.layout.simple_spinner_item, baits);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.bait.setAdapter(adapter);
        binding.bait.setSelection(Arrays.asList(baits).indexOf(point.getBait()));

        binding.notify.setChecked(shouldNotifyDefault);
    }

    private boolean updatePoint() {
        point.setName(binding.name.getText().toString().trim());
        point.setContactType(binding.contactType.getSelectedItem().toString().trim());
        String timeStampStr = binding.timeStamp.getText().toString().trim();

        try {
            Date timeStamp = timeStamp = new SimpleDateFormat("MM-dd-yyyy h:mm a").parse(timeStampStr);
            point.setTimeStamp(new Timestamp(timeStamp.getTime()));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date/time", e);
        }

        point.setBait(binding.bait.getSelectedItem().toString().trim());
        String fishSize = binding.fishSize.getText().toString().trim();
        if (!fishSize.isEmpty())
            point.setFishSize(String.format("%.2f", Double.parseDouble(fishSize)));
        point.setAirTemp(binding.airtemp.getText().toString().trim());
        point.setWaterTemp(binding.watertemp.getText().toString().trim());
        point.setWindSpeed(binding.windSpeed.getText().toString().trim());
        point.setWindDir(binding.windDir.getText().toString().trim());
        point.setCloudCover(binding.cloudCover.getText().toString().trim());
        point.setDewPoint(binding.dewPoint.getText().toString().trim());
        point.setPressure(binding.pressure.getText().toString().trim());
        point.setHumidity(binding.humidity.getText().toString().trim());
        point.setNotes(binding.notes.getText().toString().trim());
        return binding.notify.isChecked();
    }


    private void sendMessage(String message, String action) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String notification = "";
        if (action.equalsIgnoreCase("CATCH"))
            notification = prefs.getString("Catch Notification", "");
        if (action.equalsIgnoreCase("FOLLOW"))
            notification = prefs.getString("Follow Notification", "");
        if (action.equalsIgnoreCase("CONTACT"))
            notification = prefs.getString("Lost Notification", "");
        if (!notification.isEmpty()) {
            SmsManager smgr = SmsManager.getDefault();
            Map<String, String> contacts = SettingsActivity.GET_CONTACT_LIST(Integer.parseInt(notification), getContentResolver());
            contacts.forEach((name, number) -> {
                smgr.sendTextMessage(number, null, message, null, null);
                Log.d(TAG, String.format("%s message sent to %s ( %s )", action, name, number));
            });
        }
    }
}