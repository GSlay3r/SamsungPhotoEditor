package com.example.samsungproject.ui.activities;


import static com.example.samsungproject.core.classes.Constants.EMPTY;
import static com.example.samsungproject.core.classes.Constants.FINAL_PICTURE_DIRECTORY;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.photoeditor.R;
import com.example.samsungproject.core.adapters.ImageAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageViewerActivity extends AppCompatActivity {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private List<StorageReference> files = new ArrayList<>();

    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;

    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    ImageAdapter adapter;
    int currentPosition;
    int initPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(this);

        appBarLayout.bringToFront();
        appBarLayout.invalidate();

        setSupportActionBar(toolbar);

        if (getIntent() != null) {

            getListOfImages();
            currentPosition = initPosition = getIntent().getIntExtra("position", 0);

            adapter = new ImageAdapter(this, files);

            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(initPosition);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    currentPosition = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(EMPTY);

        }
    }

    /**
     * Retrieves the list of images from the Firebase storage.
     * Updates the image files list and sets up the adapter for the view pager.
     */
    private void getListOfImages() {
        StorageReference listRef = storage.getReference().child(user.getUid());

        listRef.listAll()
                .addOnSuccessListener(listResult -> {
                    files.clear();
                    files.addAll(listResult.getItems());
                    adapter = new ImageAdapter(this, files);
                    viewPager.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    viewPager.setCurrentItem(currentPosition);
                })
                .addOnFailureListener(e -> {
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_viewer_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Performs actions based on the selected menu item.
     *
     * @param item The selected menu item.
     * @return True if the menu item is handled, false otherwise.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_delete:

                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirmation)
                        .setMessage(R.string.delete_image)
                        .setPositiveButton(R.string.yes, (dialog, which) -> deleteItem())
                        .setNegativeButton(R.string.cancel, null)
                        .show();

                break;

            case R.id.action_share:

                StorageReference fileRef = files.get(currentPosition);
                File storageDir = new File(FINAL_PICTURE_DIRECTORY);
                boolean success = true;
                if (!storageDir.exists()) {
                    success = storageDir.mkdirs();
                }
                if (!success) break;
                File file = new File(storageDir, fileRef.getName());
                try {
                    if (!file.createNewFile()) break;
                } catch (IOException e) {
                    break;
                }
                fileRef.getFile(file)
                        .addOnSuccessListener(taskSnapshot -> {
                            Uri uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            shareIntent.setType("image/*");
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                        })
                        .addOnFailureListener(exception -> {
                            // Handle any errors
                        });


                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Deletes the current image.
     */
    private void deleteItem() {

        if (files.size() > 1) {
            files.get(currentPosition).delete().addOnSuccessListener(taskSnapshot -> {
                        viewPager.setAdapter(null);
                        files.remove(currentPosition);
                        adapter = new ImageAdapter(this, files);
                        viewPager.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        currentPosition -= 1;
                        viewPager.setCurrentItem(currentPosition);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show());
        } else {
            files.get(currentPosition).delete().addOnSuccessListener(taskSnapshot -> {
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onSupportNavigateUp() {

        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        return true;
    }

}
