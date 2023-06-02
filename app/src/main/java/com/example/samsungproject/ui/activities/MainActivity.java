package com.example.samsungproject.ui.activities;


import static com.example.samsungproject.core.classes.Constants.TEMP_PICTURE_DIRECTORY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.photoeditor.R;

import java.io.File;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity {

    @BindString(R.string.market_dev_name)
    String market_dev_name;

    private final static int COLLECTION_REQUEST = 2;
    private final static int GALLERY_REQUEST = 1;
    private final static int CAMERA_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        requestPermissions();
        requestWorkingWithAllFilesAndroidRAndHigher();
    }

    /**
     * Requests necessary permissions for the application to function properly.
     */
    void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                },
                PackageManager.PERMISSION_GRANTED
        );
    }

    /**
     Requests permission to work with all files on devices running Android R and higher.
     Opens the system settings if permission is not granted.
     */
    void requestWorkingWithAllFilesAndroidRAndHigher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        }
    }

    @OnClick(R.id.collection)
    void onCollectionClick() {
        startActivity(new Intent(this, CollectionActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @OnClick(R.id.camera)
    void onCameraClick() {
        Uri cameraUri;
        boolean success = true;
        File storageDir = new File(TEMP_PICTURE_DIRECTORY);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        if (success) {
            File file = new File(storageDir, "temp-original.jpg");
            cameraUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    @OnClick(R.id.gallery)
    void onGalleryClick() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), GALLERY_REQUEST);
    }


    /**
     * Handles the result of permission requests.
     *
     * @param requestCode The request code of the permission request.
     * @param permissions The requested permissions.
     * @param grantResults The results of the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case CAMERA_REQUEST:
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri cameraUri;
                boolean success = true;
                File storageDir = new File(TEMP_PICTURE_DIRECTORY);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (!storageDir.exists()) {
                    success = storageDir.mkdirs();
                }

                if (success) {
                    File file = new File(storageDir, "temp-original.jpg");
                    cameraUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
                break;

            case GALLERY_REQUEST:
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), GALLERY_REQUEST);
                break;

            case COLLECTION_REQUEST:
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(this, CollectionActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

        }

    }

    /**
     * Handles the result of activities started for results.
     *
     * @param requestCode The request code of the activity result.
     * @param resultCode The result code of the activity result.
     * @param data The data returned by the activity result.
     */
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case CAMERA_REQUEST:
                    Uri cameraUri = null;
                    boolean success = true;
                    File storageDir = new File(TEMP_PICTURE_DIRECTORY);

                    if (!storageDir.exists()) {
                        success = storageDir.mkdirs();
                    }

                    if (success) {
                        File file = new File(storageDir, "temp-original.jpg");
                        cameraUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
                    }

                    if (cameraUri == null) {
                        return;
                    }

                    startActivity(new Intent(this, ImageEditorActivity.class).putExtra("uriStr", cameraUri.toString()));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    break;

                case GALLERY_REQUEST:
                    Uri galleryUri = data.getData();
                    if (galleryUri == null) return;
                    startActivity(new Intent(this, ImageEditorActivity.class).putExtra("uriStr", galleryUri.toString()));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    break;

            }
        }
    }
}
