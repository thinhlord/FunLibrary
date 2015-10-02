package com.teamx.funlibrary;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddBookActivity extends BaseActivity {

    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int FILE_SELECT_CODE = 0;
    Book book;
    TextView name, year, pub, des, auth;
    boolean edit;
    String mPhotoPath;
    File file;
    ImageView thumb;

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                switch (type) {
                    case "image":
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "video":
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "audio":
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        break;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        book = (Book) getIntent().getSerializableExtra(GlobalConst.EXTRA_BOOK);
        if (book == null) {
            book = new Book();
            edit = false;
        } else edit = true;

        name = (TextView) findViewById(R.id.name);
        year = (TextView) findViewById(R.id.year);
        pub = (TextView) findViewById(R.id.publisher);
        des = (TextView) findViewById(R.id.description);
        auth = (TextView) findViewById(R.id.author);
        thumb = (ImageView) findViewById(R.id.thumb);

        if (edit) {
            name.setText(book.name);
            year.setText("" + book.year);
            pub.setText(book.publisher);
            des.setText(book.description);
            auth.setText(book.author);
            Picasso.with(this).load(book.imageUrl).placeholder(R.drawable.ic_book)
                    .into(thumb);
            setTitle("Edit book");
        }
        
        findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(AddBookActivity.this)
                        .title("Pick a photo")
                        .items(R.array.items)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (which == 0) showFileChooser();
                                else takePicture();
                            }
                        })
                        .show();
            }
        });
    }

    private void saveBook() {
        if (name.getText().length() == 0) {
            toast("Name is empty");
            return;
        }
        if (year.getText().length() == 0) {
            toast("Year is empty");
            return;
        }
        if (pub.getText().length() == 0) {
            toast("Name is empty");
            return;
        }
        if (des.getText().length() == 0) {
            toast("Description is empty");
            return;
        }
        if (auth.getText().length() == 0) {
            toast("Author is empty");
            return;
        }
        book.name = name.getText().toString();
        book.year = Integer.parseInt(year.getText().toString());
        book.publisher = pub.getText().toString();
        book.description = des.getText().toString();
        book.author = auth.getText().toString();
        final MaterialDialog wait = new MaterialDialog.Builder(this)
                .content("Saving...")
                .progress(true, 0)
                .cancelable(false)
                .show();
        FunHttpClient.put(book, file, new FunHttpClient.CommonCallback() {
            @Override
            public void onSuccess() {
                AddBookActivity.this.toast("Done");
                wait.dismiss();
                AddBookActivity.this.finish();
            }

            @Override
            public void onFailure(String error) {
                AddBookActivity.this.toast(error);
                wait.dismiss();
                AddBookActivity.this.finish();
            }
        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_add_book;
    }

    private void showFileChooser() {
        //Toast.makeText(getApplicationContext(), "Hiện tại ứng dụng chỉ hỗ trợ phần mềm đọc file từ thẻ nhớ.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Choose picture"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            toast("NOOB PHONE REPLACE PLZ");
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            file = null;
            try {
                file = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Can't save. NOOB PHONE", Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (file != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(file));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String path = getPath(this, uri);
                    if (path != null) {
                        file = new File(path);
                        Picasso.with(this)
                                .load(file)
                                .placeholder(R.drawable.ic_book)
                                .centerInside()
                                .resize(1000, 1000)
                                .onlyScaleDown()
                                .into(thumb);
                    } else {
                        Toast.makeText(getApplicationContext(), "NOOB APP", Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Picasso.with(this)
                            .load(file)
                            .placeholder(R.drawable.ic_book)
                            .centerInside()
                            .resize(1000, 1000)
                            .onlyScaleDown()
                            .into(thumb);
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveBook();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
