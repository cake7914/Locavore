package com.example.locavore.Fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.locavore.Adapters.PhotosAdapter;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CreateEventDialogFragment extends DialogFragment {

    public static final int IMAGE_REQUEST = 200;

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
    private Button btnUpload;
    private RecyclerView rvPhotos;
    private PhotosAdapter photosAdapter;

    int[] eventDays = new int[8];
    List<ParseFile> photos = new ArrayList<>();

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
        btnUpload = dialog.findViewById(R.id.btnUpload);
        rvPhotos = dialog.findViewById(R.id.rvPhotos);

        photosAdapter = new PhotosAdapter(getContext(), photos);
        rvPhotos.setAdapter(photosAdapter);
        rvPhotos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        cbSunday.setOnCheckedChangeListener((buttonView, isChecked) -> adjustDays(isChecked, 1));
        cbMonday.setOnCheckedChangeListener((buttonView, isChecked) -> adjustDays(isChecked, 2));
        cbTuesday.setOnCheckedChangeListener((buttonView, isChecked) -> adjustDays(isChecked, 3));
        cbWednesday.setOnCheckedChangeListener((buttonView, isChecked) -> adjustDays(isChecked, 4));
        cbThursday.setOnCheckedChangeListener((buttonView, isChecked) -> adjustDays(isChecked, 5));
        cbFriday.setOnCheckedChangeListener((buttonView, isChecked) -> adjustDays(isChecked, 6));
        cbSaturday.setOnCheckedChangeListener((buttonView, isChecked) -> adjustDays(isChecked, 7));

        assert getArguments() != null;
        String title = getArguments().getString("title", "");
        AlertDialog.Builder alertDialogBuilder=  new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialog);
        alertDialogBuilder.setTitle(title);

        //alertDialogBuilder.setMessage("create a new event");
        alertDialogBuilder.setPositiveButton("Create", (dialog1, which) -> {
            // add the new event to the database
            ParseObject event = ParseObject.create("Event");
            event.put(Event.KEY_NAME, etEventName.getText().toString());
            event.put(Event.KEY_DESCRIPTION, etEventDescription.getText().toString());
            event.put(Event.KEY_FARM, Objects.requireNonNull(ParseUser.getCurrentUser().getString(User.KEY_YELP_ID)));
            event.put(Event.KEY_LOCATION, Objects.requireNonNull(ParseUser.getCurrentUser().getParseGeoPoint(User.KEY_LOCATION)));
            event.put(Event.KEY_LOCATION_STR, Objects.requireNonNull(ParseUser.getCurrentUser().getString(User.KEY_ADDRESS)));
            event.put(Event.KEY_START_DATE,  new Date(dpStartDate.getYear()-1900, dpStartDate.getMonth(), dpStartDate.getDayOfMonth(), tpStartTime.getHour(), tpStartTime.getMinute()));
            event.put(Event.KEY_END_DATE, new Date(dpEndDate.getYear()-1900, dpEndDate.getMonth(), dpEndDate.getDayOfMonth(), tpEndTime.getHour(), tpEndTime.getMinute()));

            for(int i = 0; i <= 7; i++)
                event.add(Event.KEY_DAYS_OF_WEEK, eventDays[i]);

            if(photos != null) {
                event.put(Event.KEY_PHOTOS, photos);
            }

            event.saveInBackground(e -> {
                // assign the event to the farm creating it
                if(e != null) {
                    Log.e(TAG, "Error", e);
                }
                else {
                    ParseUser user = ParseUser.getCurrentUser();
                    if (user != null) {
                        user.add(User.KEY_EVENTS, event.getObjectId());
                        user.saveInBackground(e1 -> dialog1.dismiss());
                    }
                }
            });
        });

        alertDialogBuilder.setNegativeButton("Cancel", (dialog12, which) -> {
            if (dialog12 != null) {
                dialog12.dismiss();
            }
        });

        btnUpload.setOnClickListener(v -> {
            if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 120);
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Open Gallery"), IMAGE_REQUEST);
        });

        return alertDialogBuilder.create();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED) { // neither permission granted
            Toast.makeText(requireContext(), getString(R.string.upload_permissions) , Toast.LENGTH_SHORT).show();
        } else if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Open Gallery"), IMAGE_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
            assert data != null;
            Uri imageUri = data.getData();

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // put the image in the array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            ParseFile file = new ParseFile("image.jpg", stream.toByteArray());
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    photos.add(file);
                    photosAdapter.notifyItemInserted(photos.size()-1);
                }
            });
        }
    }
}