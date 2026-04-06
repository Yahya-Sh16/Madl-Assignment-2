package com.example.madlassignment_2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.example.madlassignment_2.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ActivityMainBinding binding;
    private DatabaseHelper databaseHelper;
    private String currentImagePath = "";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> selectPictureLauncher;
    private Uri photoURI;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        // Permissions
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // permission granted
            }
        });
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Setup WorkManager for Periodic Notification (15 mins minimum)
        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest.Builder(
                NotificationWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueue(notificationWork);

        // One-time request for testing (shows notification in 10 secs)
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest);

        // Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        setupLaunchers();

        binding.btnCaptureImage.setOnClickListener(v -> dispatchTakePictureIntent());
        binding.btnSelectImage.setOnClickListener(v -> selectPictureLauncher.launch("image/*"));

        binding.btnSaveNote.setOnClickListener(v -> saveNote());

        binding.btnViewNotes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotesListActivity.class);
            startActivity(intent);
        });
    }

    private void setupLaunchers() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        currentImagePath = photoURI.toString();
                        binding.cvThumbnail.setVisibility(View.VISIBLE);
                        binding.ivThumbnail.setVisibility(View.VISIBLE);
                        Glide.with(this).load(currentImagePath).into(binding.ivThumbnail);
                    }
                }
        );

        selectPictureLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        currentImagePath = uri.toString();
                        binding.cvThumbnail.setVisibility(View.VISIBLE);
                        binding.ivThumbnail.setVisibility(View.VISIBLE);
                        Glide.with(this).load(currentImagePath).into(binding.ivThumbnail);
                    }
                }
        );
    }

    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", photoFile);
            takePictureLauncher.launch(photoURI);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void saveNote() {
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String desc = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
        String noteType = binding.etNoteType.getText() != null ? binding.etNoteType.getText().toString().trim() : "";
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        if (title.isEmpty()) {
            binding.etTitle.setError("Title is required");
            return;
        }

        Note note = new Note(title, desc, currentImagePath, date, noteType);
        long id = databaseHelper.insertNote(note);

        if (id > -1) {
            Toast.makeText(this, "Note Saved Successfully", Toast.LENGTH_SHORT).show();
            binding.etTitle.setText("");
            binding.etDescription.setText("");
            binding.etNoteType.setText("");
            binding.cvThumbnail.setVisibility(View.GONE);
            binding.ivThumbnail.setVisibility(View.GONE);
            currentImagePath = "";
        } else {
            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometer != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
            if (acceleration > 5) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastShakeTime) > 2000) {
                    lastShakeTime = currentTime;
                    Toast.makeText(this, "Device motion detected", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}