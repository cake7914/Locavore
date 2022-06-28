package com.example.locavore.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.locavore.R;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class CreateEventDialogFragment extends DialogFragment {

    private EditText etEventName;
    private EditText etEventDescription;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //inflate the layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialog = inflater.inflate(R.layout.fragment_create_event_dialog, null);

        //find the text inputs
        etEventName = dialog.findViewById(R.id.etEventName);
        etEventDescription = dialog.findViewById(R.id.etEventDescription);

        String title = getArguments().getString("title", "");
        AlertDialog.Builder alertDialogBuilder=  new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialog);
        alertDialogBuilder.setTitle(title);

        alertDialogBuilder.setMessage("create a new event");
        alertDialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // add the new event to the database
                String eventName = etEventName.getText().toString();
                String eventDescription = etEventDescription.getText().toString();

                ParseObject event = ParseObject.create("Event");
                event.put("name", eventName);
                event.put("description", eventDescription);
                event.put("farm", ParseUser.getCurrentUser().getObjectId());
                event.saveInBackground();

                // assign the event to the farm creating it
                ParseUser user = ParseUser.getCurrentUser();
                if (user != null) {
                    user.add("events", event);
                    user.saveInBackground();
                }
                dialog.dismiss();
            }
        });

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