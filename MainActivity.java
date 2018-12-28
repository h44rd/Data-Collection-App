package com.cvit.haard.attendance;

import android.Manifest;
//import android.content.Context;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
//import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
//import android.provider.SyncStateContract;
//import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

//import net.gotev.uploadservice.MultipartUploadRequest;
//import net.gotev.uploadservice.ServerResponse;
//import net.gotev.uploadservice.UploadInfo;
//import net.gotev.uploadservice.UploadNotificationConfig;
//import net.gotev.uploadservice.UploadService;
//import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;
    static final int REQUEST_TAKE_PHOTO = 3;
    String mCurrentPhotoPath;
    String mCurrentVidPath;
    Uri vidUri_glob;
    Uri photoPath_glob;
    String id_glob;
    LocationManager locationManager;
    String locationProvider;
    private FusedLocationProviderClient mFusedLocationClient;
    String photoName;

    int randn_glob;

    String lat = "None";
    String lon = "None";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        checkLocationPermission();

        setRandomTxt();

//        final VideoView myvid = (VideoView) findViewById(R.id.camVid);
//        myvid.setZOrderOnTop(true);
//        myvid.setBackgroundColor(Color.TRANSPARENT);

        final EditText idtext = (EditText) findViewById(R.id.id_textbox);

        final Button idbutton = (Button) findViewById(R.id.id_button);
        idbutton.setClickable(false);
        idbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idtext.getText().toString();
                if(id.length() > 0) {
                    id_glob = id;
                    try {
                        dispatchTakeVideoIntent();
//                        idbutton.setClickable(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    dispatchTakePictureIntent();

                }
                else {
                    Toast.makeText( getApplicationContext(), "Please enter ID in the text box", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            idbutton.setClickable(true);
                            lat = (String) String.valueOf(location.getLatitude());
                            lon = (String) String.valueOf(location.getLongitude());
                            Snackbar.make(findViewById(R.id.root), "Location: " + lat + " , " + lon, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }
                    }
                });


    }

    public void setRandomTxt() {
        Random r = new Random();
        int i1 = r.nextInt(10000 -1000) + 1000;
        randn_glob = i1;

        TextView rando = (TextView) findViewById(R.id.random_num);
        rando.setText(String.valueOf(i1));
    }

    private void dispatchTakeVideoIntent() throws IOException {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Uri uri = getOutputVideoUri();
        if(uri != null) {
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    public Uri getOutputVideoUri() throws IOException {
        final EditText idtext = (EditText) findViewById(R.id.id_textbox);

        String id = (String) idtext.getText().toString();
        id = id.replaceAll("\\s+","");

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String vidFileName = id + "_" + timeStamp + "_";
        File storageDir = null;
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File vid = File.createTempFile(vidFileName,".mp4", storageDir);

        if (vid != null) {
            vidUri_glob = FileProvider.getUriForFile(this,
                    "*removed for privacy reasons*",
                    vid);
        }

        mCurrentVidPath = vid.getAbsolutePath();
        return  vidUri_glob;
    }


    private File createImageFile() throws IOException {
        // Create an image file name

        final EditText idtext = (EditText) findViewById(R.id.id_textbox);

        String id = (String) idtext.getText().toString();
        id.replaceAll("\\s+","");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = id + "_" + timeStamp + "_";
        File storageDir = null;
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        photoName = imageFileName + ".jpg";
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.cvit.haard.attendance",
                        photoFile);
                photoPath_glob = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Location permission")
                        .setMessage("We need the permission to your location to send the attendance.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            final Button b = (Button) findViewById(R.id.id_button);
                                            b.setClickable(true);
                                            lat = (String) String.valueOf(location.getLatitude());
                                            lon = (String) String.valueOf(location.getLongitude());
                                            Snackbar.make(findViewById(R.id.root), "Location: " + lat + " , " + lon, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        }
                                    }
                                });


                    }


                } else {

                    TextView textView = (TextView) findViewById(R.id.text1);
                    textView.setText("No location permission, Restart the app to give permission");

                }
                return;
            }

        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            final Uri videoUri = (Uri) data.getData();
//            final VideoView myvid = (VideoView) findViewById(R.id.camVid);
//            myvid.setVideoURI(videoUri);
//            myvid.start();

//            Toast.makeText(this, videoUri.getPath(), Toast.LENGTH_LONG).show();

            final EditText idtext = (EditText) findViewById(R.id.id_textbox);

            String id = (String) idtext.getText().toString();

//            try {
//                String uploadId = UUID.randomUUID().toString();
//
//                String s = new MultipartUploadRequest(this, uploadId, "*removed for privacy reasons*")
//                        .addFileToUpload( mCurrentVidPath, "video")
//                        .addParameter("name", "hello.mp4")
//                        .addParameter("id", id)
//                        .addParameter("speech", String.valueOf(randn_glob))
//                        .addParameter("longitude", lon)
//                        .addParameter("latitude", lat)
//                        .setNotificationConfig(new UploadNotificationConfig())
//                        .setMaxRetries(2)
//                        .setDelegate(new UploadStatusDelegate() {
//                            @Override
//                            public void onProgress(Context context, UploadInfo uploadInfo) {
//                                final TextView t = findViewById(R.id.textlog_during);
//                                t.setText(uploadInfo.toString());
//                            }
//
//                            @Override
//                            public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
//                                String out = mCurrentVidPath + exception.toString();
//                                Toast.makeText(context, out, Toast.LENGTH_LONG).show();
//                                Log.d("Upload",  out);
//                            }
//
//                            @Override
//                            public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
//                                File file = new File(mCurrentVidPath);
//                                boolean deleted = file.delete();
//                                Toast.makeText(context,"Upload complete!", Toast.LENGTH_LONG).show();
//                            }
//
//                            @Override
//                            public void onCancelled(Context context, UploadInfo uploadInfo) {
//                                final TextView t = findViewById(R.id.textlog_during);
//                                t.setText(uploadInfo.toString());
//                            }
//                        })
//                        .startUpload();
//
//            } catch (Exception exc) {
////                Toast.makeText(this, exc.getMessage(), Toast.LENGTH_LONG).show();
//                Log.d("Upload", exc.getMessage());
//            }
            uploadFileIni(mCurrentVidPath);
//            dispatchTakePictureIntent();
            Snackbar.make(findViewById(R.id.root), "ID: " + id + " Location: " + lat + " , " + lon, Snackbar.LENGTH_LONG).setAction("Action", null).show();

        }

//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bitmap imageBitmap = null;
//            try {
//                imageBitmap = (Bitmap) getBitmapFromUri(photoPath_glob);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            final ImageView mImageView = (ImageView) findViewById(R.id.bitmapCam);
//            mImageView.setImageBitmap(imageBitmap);
//
//            final EditText idtext = (EditText) findViewById(R.id.id_textbox);
//
//            String id = (String) idtext.getText().toString();
//
//            try {
//                String uploadId = UUID.randomUUID().toString();
//
//                String s = new MultipartUploadRequest(this, uploadId, "*removed for privacy reasons*")
//                        .addFileToUpload( mCurrentPhotoPath, "image") //Adding file
//                        .addParameter("name", "hello1.jpg") //Adding text parameter to the request
//                        .addParameter("id", id)
//                        .addParameter("longitude", lon)
//                        .addParameter("latitude", lat)
//                        .setNotificationConfig(new UploadNotificationConfig())
//                        .setMaxRetries(2)
//                        .setDelegate(new UploadStatusDelegate() {
//                            @Override
//                            public void onProgress(Context context, UploadInfo uploadInfo) {
//
//                            }
//
//                            @Override
//                            public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
//                                String out = mCurrentPhotoPath + exception.toString();
////                                Toast.makeText(context, out, Toast.LENGTH_LONG).show();
//                                Log.d("Upload",  out);
//                            }
//
//                            @Override
//                            public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
//                                File file = new File(mCurrentPhotoPath);
//                                boolean deleted = file.delete();
//                                mImageView.setColorFilter(45);
//                                Toast.makeText(context,"Upload complete!", Toast.LENGTH_LONG).show();
//                            }
//
//                            @Override
//                            public void onCancelled(Context context, UploadInfo uploadInfo) {
//
//                            }
//                        })
//                        .startUpload();
//
//            } catch (Exception exc) {
//                Log.d("Upload", exc.getMessage());
//            }
//            final VideoView myvid = (VideoView) findViewById(R.id.camVid);
//            myvid.start();
//
//            setRandomTxt();
//
//            Snackbar.make(findViewById(R.id.root), "ID: " + id + " Location: " + lat + " , " + lon, Snackbar.LENGTH_LONG).setAction("Action", null).show();
//
//        }

    }

    public void uploadFileIni(final  String selectedFilePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                uploadFile(selectedFilePath);
            }
        }).start();
    }

    public int uploadFile(final String selectedFilePath){

        final EditText idtext = (EditText) findViewById(R.id.id_textbox);

        String id = (String) idtext.getText().toString();

        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String dateStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        final TextView log_dis = findViewById(R.id.textlog_during);

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];

        if (!selectedFile.isFile()){
//            dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    tvFileName.setText("Source File Doesn't Exist: " + selectedFilePath);
                }
            });
            return 0;
        }else{
            try{
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL("*removed for privacy reasons*");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file",selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);

                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"id\"" + lineEnd + lineEnd + id + lineEnd + twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"latitude\"" + lineEnd + lineEnd + lat + lineEnd + twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"date\"" + lineEnd + lineEnd + dateStamp + lineEnd + twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"time\"" + lineEnd + lineEnd + timeStamp + lineEnd + twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"longitude\"" + lineEnd + lineEnd + lon + lineEnd + twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"speech\"" + lineEnd + lineEnd + String.valueOf(randn_glob) + lineEnd + twoHyphens + boundary + lineEnd);



                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"video\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);



                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0){
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                    log_dis.setText(String.valueOf(bytesRead));
                }

                dataOutputStream.writeBytes(lineEnd);

                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                log_dis.setText("Uploading...");
                serverResponseCode = connection.getResponseCode();
//                log_dis.setText("Uploading2...");
                final String serverResponseMessage = connection.getResponseMessage();
//                log_dis.setText("Uploading3...");
                Log.i("Awesome", "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                log_dis.setText("Outside yo " + serverResponseMessage);
//                response code of 200 indicates the server status OK
                if(serverResponseCode == 200){
                    log_dis.setText("Uploaded Successfully!");
//                    Toast.makeText(MainActivity.this, "Uploaded Successfully!", Toast.LENGTH_LONG);
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();

                selectedFile.delete();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                log_dis.setText("File now found!");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this,"File Not Found",Toast.LENGTH_SHORT).show();
//                    }
//                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                log_dis.setText("URL error!");
//                Toast.makeText(MainActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                log_dis.setText("Cannot read file!");
//                Toast.makeText(MainActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }
//            dialog.dismiss();
            setRandomTxt();
            return serverResponseCode;
        }

    }




}
