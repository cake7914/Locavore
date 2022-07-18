package com.example.locavore.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.example.locavore.DataManager;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateEventDialogFragment extends DialogFragment {

    private EditText etEventName;
    private EditText etEventDescription;
    private DatePicker dpStartDate;
    private DatePicker dpEndDate;
    private TimePicker tpStartTime;
    private TimePicker tpEndTime;
    private CheckBox cbMonday;
    private CheckBox cbTuesday;
    private CheckBox cbWednesday;
    private CheckBox cbThursday;
    private CheckBox cbFriday;
    private CheckBox cbSaturday;
    private CheckBox cbSunday;
    int[] eventDays = new int[7];

    DataManager dataManager = DataManager.getInstance(null);

    public static final String TAG = "CreateEventDialogFragment";

    public CreateEventDialogFragment() {
        // Required empty public constructor
    }

    public static CreateEventDialogFragment newInstance(String title) {
        CreateEventDialogFragment fragment = new CreateEventDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    public void adjustDays(boolean checked, int id) {
        if (checked)
            eventDays[id] = 1;
        else
            eventDays[id] = 0;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialog = inflater.inflate(R.layout.fragment_create_event_dialog, null);

        etEventName = dialog.findViewById(R.id.etEventName);
        etEventDescription = dialog.findViewById(R.id.etEventDescription);
        dpStartDate = dialog.findViewById(R.id.dpStartDate);
        dpEndDate = dialog.findViewById(R.id.dpEndDate);
        tpStartTime = dialog.findViewById(R.id.tpStartTime);
        tpEndTime = dialog.findViewById(R.id.tpEndTime);
        cbMonday = dialog.findViewById(R.id.cbMonday);
        cbTuesday = dialog.findViewById(R.id.cbTuesday);
        cbWednesday = dialog.findViewById(R.id.cbWednesday);
        cbThursday = dialog.findViewById(R.id.cbThursday);
        cbFriday = dialog.findViewById(R.id.cbFriday);
        cbSaturday = dialog.findViewById(R.id.cbSaturday);
        cbSunday = dialog.findViewById(R.id.cbSunday);

        dpStartDate.setMinDate(new Date().getTime());

        cbMonday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adjustDays(isChecked, 0);
        });
        cbTuesday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adjustDays(isChecked, 1);
        });
        cbWednesday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adjustDays(isChecked, 2);
        });
        cbThursday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adjustDays(isChecked, 3);
        });
        cbFriday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adjustDays(isChecked, 4);
        });
        cbSaturday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adjustDays(isChecked, 5);
        });
        cbSunday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adjustDays(isChecked, 6);
        });

        String title = getArguments().getString("title", "");
        AlertDialog.Builder alertDialogBuilder=  new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialog);
        alertDialogBuilder.setTitle(title);

        //alertDialogBuilder.setMessage("create a new event");
        alertDialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // add the new event to the database

                ParseObject event = ParseObject.create("Event");
                event.put(Event.KEY_NAME, etEventName.getText().toString());
                event.put(Event.KEY_DESCRIPTION, etEventDescription.getText().toString());
                event.put(Event.KEY_FARM, ParseUser.getCurrentUser().getObjectId());
                event.put(Event.KEY_LOCATION, ParseUser.getCurrentUser().getParseGeoPoint(User.KEY_LOCATION));
                event.put(Event.KEY_LOCATION_STR, ParseUser.getCurrentUser().getString(User.KEY_ADDRESS));
                event.put(Event.KEY_START_DATE, new Date(dpStartDate.getYear()-1900, dpStartDate.getMonth(), dpStartDate.getDayOfMonth(), tpStartTime.getHour(), tpStartTime.getMinute()));
                event.put(Event.KEY_END_DATE, new Date(dpEndDate.getYear()-1900, dpEndDate.getMonth(), dpEndDate.getDayOfMonth(), tpEndTime.getHour(), tpEndTime.getMinute()));
                for(int i = 0; i < 7; i++)
                    event.add(Event.KEY_DAYS_OF_WEEK, eventDays[i]);

                event.saveInBackground(e -> {
                    // assign the event to the farm creating it
                    ParseUser user = ParseUser.getCurrentUser();
                    if (user != null) {
                        user.add(User.KEY_EVENTS, event.getObjectId());
                        user.saveInBackground(e1 -> dialog.dismiss());
                    }
                });
            }
        });
        //gKOW9W_kQAg8hg07gKS9Fg

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        return alertDialogBuilder.create();
    }
}