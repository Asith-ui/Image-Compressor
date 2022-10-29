package com.ps.android.imagecompressor;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nanchen.compresshelper.CompressHelper;
import com.nanchen.compresshelper.FileUtil;
import com.ps.android.imagecompressor.R;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements MaxAdListener, UpdateHelper.OnUpdateCheckListener {

    ImageView add_image, download_image, before_image, after_image, clear, list, imagesetting, defaultsetting, productImage, compress;
    TextView beforeSize, afterSize, productTitleTv, productDecTv, set, showQuality, compressQuality;
    ImageButton backBtn;
    EditText setQuality;
    LinearLayout lLayout;
    private MaxInterstitialAd interstitialAd;
    private int retryAttempt;

    private MaxAdView adView;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    private String imageName;
    private String imagesize;
    private String setquality;
    private String showQ;
    private String setQ;

    private Uri addImageUri, setImagUri;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    public File beforeProduct;
    public File afterProduct;

    private ProgressDialog progressDialog, styleProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UpdateHelper
                .with(this)
                .onUpdateCheck(this)
                .check();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        findViews();
        unloadImages();
        createInterstitialAd();


        add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unloadImages();
                Toast.makeText(MainActivity.this, "Upload next image..", Toast.LENGTH_SHORT).show();
            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Details.class);
                startActivity(intent);
            }
        });

        defaultsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog();
                set.setVisibility(View.GONE);
                setQuality.setVisibility(View.GONE);

            }
        });
        imagesetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog();
                set.setVisibility(View.VISIBLE);
                setQuality.setVisibility(View.VISIBLE);


            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        styleProgress = new ProgressDialog(this);
        styleProgress.setMax(100);
        styleProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        styleProgress.setCanceledOnTouchOutside(false);
        styleProgress.setTitle("Please wait");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (styleProgress.getProgress() <= styleProgress
                            .getMax()) {
                        Thread.sleep(200);
                        handle.sendMessage(handle.obtainMessage());
                        if (styleProgress.getProgress() == styleProgress
                                .getMax()) {
                            styleProgress.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        AppLovinSdk.getInstance(MainActivity.this).setMediationProvider("max");
        AppLovinSdk.initializeSdk(MainActivity.this, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {

            }
        });
        adView.loadAd();
    }

    @Override
    public void onUpdateCheckListener(final String urlApp) {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("New Features Available")
                .setMessage("Please Update To New Version Use To New Features")
                .setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        gotoUrl();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        alertDialog.show();

    }


    private void gotoUrl() {
        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.pixelstudio.android.imagecompressor");
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void findViews() {
        add_image = findViewById(R.id.add_image);
        download_image = findViewById(R.id.download_image);
        compress = findViewById(R.id.compress);
        before_image = findViewById(R.id.before_image);
        after_image = findViewById(R.id.after_image);
        beforeSize = findViewById(R.id.beforeSize);
        afterSize = findViewById(R.id.afterSize);
        clear = findViewById(R.id.clear);
        list = findViewById(R.id.list);
        imagesetting = findViewById(R.id.imagesetting);
        defaultsetting = findViewById(R.id.defaultsetting);
        compressQuality = findViewById(R.id.compressQuality);
        lLayout = findViewById(R.id.lLayout);
        adView = findViewById(R.id.adView);

    }

    private void createInterstitialAd() {
        interstitialAd = new MaxInterstitialAd("fc38f26dc7d90ea8", this);
        interstitialAd.setListener(this);

        // Load the first ad
        interstitialAd.loadAd();
    }

    private void unloadImages() {
        before_image.setVisibility(View.GONE);
        after_image.setVisibility(View.GONE);
        afterSize.setVisibility(View.GONE);
        clear.setVisibility(View.GONE);
        compress.setVisibility(View.GONE);
        download_image.setVisibility(View.GONE);
        imagesetting.setVisibility(View.GONE);
        defaultsetting.setVisibility(View.GONE);
        lLayout.setVisibility(View.GONE);

    }

    final Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            styleProgress.incrementProgressBy(1);
        }
    };

    //Pick Icon Image
    private void showImagePickDialog() {
        String[] option = {"Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (checkStoragePermissins()) {
                            pickFromGallery();
                        } else {
                            requestStoragePermissions();


                        }
                    }
                })
                .show();
    }

    private void pickFromGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), IMAGE_PICK_GALLERY_CODE);

    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        addImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, addImageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    // Storage Permission
    private Boolean checkStoragePermissins() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera Permission Are Necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage Permission is Necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == IMAGE_PICK_GALLERY_CODE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {

                try {
                    before_image.setVisibility(View.VISIBLE);
                    imagesetting.setVisibility(View.VISIBLE);
                    defaultsetting.setVisibility(View.VISIBLE);
                    compress.setVisibility(View.VISIBLE);
                    defaultsetting.setVisibility(View.VISIBLE);
                    lLayout.setVisibility(View.VISIBLE);
                    beforeProduct = FileUtil.getTempFile(this, data.getData());
                    imageName = beforeProduct.getName();
                    setImagUri = data.getData();
                    before_image.setImageURI(setImagUri);
                    beforeSize.setText(String.format(" %s", getReadableFileSize(beforeProduct.length())));
                    imagesize = beforeSize.getText().toString().trim();

                    Toast.makeText(MainActivity.this, "Image Uploaded..", Toast.LENGTH_SHORT).show();

                    compress.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            crompressProductImage();
                            styleProgress.setMessage("Compressing...");
                            styleProgress.show();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private void bottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.image_details, null);
        bottomSheetDialog.setContentView(view);

        productImage = view.findViewById(R.id.productImage);
        productTitleTv = view.findViewById(R.id.productTitleTv);
        productDecTv = view.findViewById(R.id.productDecTv);
        setQuality = view.findViewById(R.id.setQuality);
        set = view.findViewById(R.id.set);
        showQuality = view.findViewById(R.id.showQuality);

        productTitleTv.setText(imagesize);
        productDecTv.setText(imageName);
        productImage.setImageURI(setImagUri);

        bottomSheetDialog.show();

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showQ = setQuality.getText().toString();
                int set0 = Integer.parseInt(showQ);

                if (set0 > 100) {
                    Toast.makeText(MainActivity.this, "Enter maximum 100 only..", Toast.LENGTH_SHORT).show();
                } else if (set0 < 10) {
                    Toast.makeText(MainActivity.this, "Enter minimum 10 only..", Toast.LENGTH_SHORT).show();
                } else {

                    showQuality.setText(showQ);
                    setQuality.setText("0");
                    compressQuality.setText(showQ);
                    Toast.makeText(MainActivity.this, "Quality Saved", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                }

            }
        });

    }

    private void crompressProductImage() {
        after_image.setVisibility(View.VISIBLE);
        afterSize.setVisibility(View.VISIBLE);
        afterProduct = CompressHelper.getDefault(getApplicationContext()).compressToFile(beforeProduct);

        setQ = compressQuality.getText().toString();
        int set = Integer.parseInt(setQ);

        afterProduct = new CompressHelper.Builder(this)
                .setMaxWidth(720)
                .setMaxHeight(1024)
                .setQuality(set)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setFileName("imageCompressior." + imageName)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())
                .build()
                .compressToFile(beforeProduct);

        styleProgress.dismiss();
        after_image.setImageBitmap(BitmapFactory.decodeFile(afterProduct.getAbsolutePath()));
        Toast.makeText(this, "Image Compressed Succesfully.", Toast.LENGTH_SHORT).show();
        afterSize.setText(String.format("Image Size : %s", getReadableFileSize(afterProduct.length())));

        download_image.setVisibility(View.VISIBLE);
        clear.setVisibility(View.VISIBLE);
        download_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (interstitialAd.isReady()) {
                    interstitialAd.showAd();
                    interstitialAd.loadAd();
                }
                Toast.makeText(MainActivity.this, "Image Downloaded , Visit Download Folder", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onAdLoaded(final MaxAd maxAd) {
        // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'

        // Reset retry attempt
        retryAttempt = 0;
    }

    @Override
    public void onAdLoadFailed(final String adUnitId, final MaxError error) {
        // Interstitial ad failed to load
        // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)

        retryAttempt++;
        long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                interstitialAd.loadAd();
            }
        }, delayMillis);
    }

    @Override
    public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error) {
        // Interstitial ad failed to display. We recommend loading the next ad
        interstitialAd.loadAd();
    }

    @Override
    public void onAdDisplayed(final MaxAd maxAd) {
    }

    @Override
    public void onAdClicked(final MaxAd maxAd) {
    }

    @Override
    public void onAdHidden(final MaxAd maxAd) {
        // Interstitial ad is hidden. Pre-load the next ad
        interstitialAd.loadAd();
    }


}