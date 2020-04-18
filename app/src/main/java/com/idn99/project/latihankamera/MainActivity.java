package com.idn99.project.latihankamera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int AMBIL_FOTO_BESAR = 1;
    private static final int AMBIL_FOTO_KECIL = 2;
    private static final int AMBIL_VIDEO = 3;
    private static final int TAMPILKAN_GALLERY = 4;
    private static final int KIRIM_GAMBAR = 5;



    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private ImageView imageView;
    private Bitmap bitmap;
    private VideoView videoView;
    private Uri videoUri;
    private Uri gambarUriSend;
    private String mCurrentPhotoPath;
    private Intent ambilGambarIntent;
    private Intent ambilVideoIntent;
    private Intent tampilkanGalleryIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        findViewById(R.id.cameraBButton).setOnClickListener(this);
        findViewById(R.id.cameraSButton).setOnClickListener(this);
        findViewById(R.id.videoButton).setOnClickListener(this);
        findViewById(R.id.galleryButton).setOnClickListener(this);
        findViewById(R.id.xButton).setOnClickListener(this);
        findViewById(R.id.kirim).setOnClickListener(this);
    }

    private File getAlbumDir() {
        File storageDir = null;
        final String namaDirektori = "/DCIM/CameraSample";
        if
        (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStorageDirectory() + namaDirektori);
            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if
                    (!storageDir.exists()) {
                        Log.d("CameraSample", "Gagal membuat direktori " + storageDir);
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "Eksternal penyimpanan tidak diset READ/WRITE");
        }
        return storageDir;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp;

        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);

        return imageF;
    }

    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();

        mCurrentPhotoPath = f.getAbsolutePath();
        return f;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cameraBButton:
                ambilGambarIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = null;
                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    ambilGambarIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                startActivityForResult(ambilGambarIntent, AMBIL_FOTO_BESAR);
                break;
            case R.id.cameraSButton:
                ambilGambarIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(ambilGambarIntent, AMBIL_FOTO_KECIL);
                break;
            case R.id.videoButton:
                ambilVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(ambilVideoIntent, AMBIL_VIDEO);
                break;
            case R.id.galleryButton:
                tampilkanGalleryIntent = new Intent(Intent.ACTION_PICK);
                tampilkanGalleryIntent.setType("image/*");
                startActivityForResult(tampilkanGalleryIntent, TAMPILKAN_GALLERY);
                break;
            case R.id.kirim:
                Drawable mDrawable = imageView.getDrawable();
                Bitmap mBitmap = ((BitmapDrawable)mDrawable).getBitmap();

                String path = MediaStore.Images.Media.insertImage(this.getContentResolver(),
                        mBitmap, "Design", null);

                Uri uri = Uri.parse(path);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/*");
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.putExtra(Intent.EXTRA_TEXT, "I found something cool!");
                this.startActivity(Intent.createChooser(share, "Share Your Design!"));
            case R.id.xButton:
                finish();
                break;
            default:
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AMBIL_FOTO_BESAR:
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            case AMBIL_FOTO_KECIL:
                if (resultCode == RESULT_OK) {
                    handleSmallCameraPhoto(data);
                }
                break;
            case AMBIL_VIDEO:
                if (resultCode == RESULT_OK) {
                    handleCameraVideo(data);
                }
                break;
            case TAMPILKAN_GALLERY:
                if (resultCode == RESULT_OK) {
                    Uri photoUri = data.getData();
                    imageView.setImageURI(photoUri);

                    videoView.setVisibility(View.INVISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                }
                break;
//            case KIRIM_GAMBAR:
//                if (resultCode == RESULT_OK) {
//                    Uri photoUri = data.getData();
//                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, photoUri);
//                }
//                break;
        }
    }

    private void handleBigCameraPhoto() {
        if (mCurrentPhotoPath != null) {
            setPic();
            galleryAddPic();
            mCurrentPhotoPath = null;
        }
    }

    private void handleSmallCameraPhoto(Intent intent) {
        Bundle extras = intent.getExtras();
        bitmap = (Bitmap) extras.get("data");
        imageView.setImageBitmap(bitmap);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp;
        if (MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, imageFileName, null) != null) {
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
        }

        videoUri = null;
        videoView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.VISIBLE);
    }

    private void handleCameraVideo(Intent intent) {
        videoUri = intent.getData();
        videoView.setVideoURI(videoUri);
        videoView.start();
        videoView.seekTo(1);
        bitmap = null;
        imageView.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.VISIBLE);
    }

    private void setPic() {
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        imageView.setImageBitmap(bitmap);
        videoUri = null;
        imageView.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.INVISIBLE);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}