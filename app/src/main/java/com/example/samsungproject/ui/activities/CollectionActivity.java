package com.example.samsungproject.ui.activities;

import static com.example.samsungproject.core.classes.Constants.REQUEST_CODE;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoeditor.R;
import com.example.samsungproject.core.adapters.CollectionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("unused")
public class CollectionActivity extends AppCompatActivity {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    //    private ArrayList<String> paths = new ArrayList<>();
    private List<StorageReference> files = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        ButterKnife.bind(this);

        getListOfImages();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));

            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back);
            upArrow.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            Spannable title = new SpannableString(getString(R.string.my_collection));
            title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Retrieves the list of images from Firebase Storage.
     * Clears the files list and adds the retrieved items to it.
     * Calls the loadItems() method to load the items in the RecyclerView.
     */
    private void getListOfImages() {
        StorageReference listRef = storage.getReference().child(user.getUid());

        listRef.listAll()
                .addOnSuccessListener(listResult -> {
                    files.clear();
                    files.addAll(listResult.getItems());
                    loadItems();
                })
                .addOnFailureListener(e -> {
                    // Uh-oh, an error occurred!
                });
    }

    /**
     * Loads the items in the RecyclerView using the CollectionAdapter.
     * Launches the ImageViewerActivity when an item is clicked.
     */
    private void loadItems() {
        CollectionAdapter adapter = new CollectionAdapter(this, files, position -> {

            startActivityForResult(
                    new Intent(CollectionActivity.this, ImageViewerActivity.class)
                            .putExtra("position", position), REQUEST_CODE);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        });

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.collection_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_refresh) {
            getListOfImages();
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            getListOfImages();
        }
    }
}
