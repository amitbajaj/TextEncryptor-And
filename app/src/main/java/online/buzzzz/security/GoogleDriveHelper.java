package online.buzzzz.security;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import online.buzzzz.security.textencryptor.R;

public class GoogleDriveHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    public static final int READ_MODE = 0;
    public static final int WRITE_MODE = 1;
    private GoogleApiClient mGoogleApiClient;
    private GoogleDriveHelperListener googleDriveHelperListener;
    public boolean isClientConnected = false;
    private Activity activity;
    private Context context;
    private String fileName;
    private String fileContents;
    private boolean isNewFile;
    private int mode;
    private String error_label ;
    private String ok_button ;
    private String unable_to_connect ;
    private String TAG;
    private boolean workInProgress = false;
    public void init(Activity caller, GoogleDriveHelperListener googleDriveHelperListenerP, String tag){
        activity = caller;
        googleDriveHelperListener = googleDriveHelperListenerP;
        context = activity.getApplicationContext();
        TAG = tag;
        workInProgress = true;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        try{
            error_label = activity.getString(R.string.error_label);
        }catch(Exception e){
            error_label = "Error";
        }
        try{
            ok_button = activity.getString(R.string.ok_button);
        }catch(Exception e){
            ok_button = "Ok";
        }
        try{
            unable_to_connect = activity.getString(R.string.unable_to_connect);
        }catch(Exception e){
            unable_to_connect = "Unable to connect to Google Drive";
        }
    }
    public void connect(){
        if(!(isClientConnected||workInProgress)){
            mGoogleApiClient.connect();
        }
    }

    public void disconnect(){
        if(isClientConnected && !workInProgress){
            isClientConnected = false;
            mGoogleApiClient.disconnect();
        }
    }
    /*
    public boolean createFile(String fileName, String sData){
        if (isClientConnected){
            openOrCreateFile(fileName,sData);
            return true;
        }else{
            return false;
        }
    }

    final ResultCallback<DriveApi.DriveContentsResult> resultCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                    if(driveContentsResult.getStatus().isSuccess()){
                        createDummyFile(driveContentsResult);
                    }
                }
            };
    */
    public void readFile(String fileNameParameter){
        if(!workInProgress){
            mode = READ_MODE;
            workInProgress = true;
            fileName = fileNameParameter;
            //Log.d(TAG,"Trying to query for file named : "+fileName);
            Query query = new Query.Builder()
                    .addFilter(Filters.and(Filters.eq(SearchableField.TITLE,fileName), Filters.eq(SearchableField.TRASHED,false)))
                    .build();
            DriveFolder driveFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
            driveFolder.queryChildren(mGoogleApiClient,query)
                    .setResultCallback(metadataBufferResultResultCallback);
        }else{
            googleDriveHelperListener.onFileOpenFailure();
            //Log.d(TAG,"Already busy with some work!!");
        }
    }

    public void writeFile(String fileNameParameter, String fileContentsParameter){
        if(!workInProgress){
            mode = WRITE_MODE;
            workInProgress = true;
            fileName = fileNameParameter;
            fileContents = fileContentsParameter;
            //Log.d(TAG,"Trying to query for file named : "+fileName);
            Query query = new Query.Builder()
                    .addFilter(Filters.and(Filters.eq(SearchableField.TITLE,fileName), Filters.eq(SearchableField.TRASHED,false)))
                    .build();
            DriveFolder driveFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
            driveFolder.queryChildren(mGoogleApiClient,query)
                    .setResultCallback(metadataBufferResultResultCallback);
        }else{
            googleDriveHelperListener.onFileOpenFailure();
            //Log.d(TAG,"Already busy with some work!!");
        }
    }

    final private ResultCallback<DriveApi.MetadataBufferResult> metadataBufferResultResultCallback =
            new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                    if(metadataBufferResult.getStatus().isSuccess()){
                        MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();
                        if(metadataBuffer.getCount()>0){
                            DriveFile driveFile = metadataBuffer.get(0).getDriveId().asDriveFile();
                            //Log.d(TAG,"Found file with Id: "+driveFile.getDriveId().toString());
                            if (mode == READ_MODE){
                                driveFile.open(mGoogleApiClient,DriveFile.MODE_READ_ONLY,null)
                                        .setResultCallback(driveContentsResultResultCallback);
                            }else{
                                isNewFile = false;
                                driveFile.open(mGoogleApiClient,DriveFile.MODE_WRITE_ONLY,null)
                                        .setResultCallback(driveContentsResultResultCallback);
                            }
                        }else{
                            if(mode == READ_MODE){
                                workInProgress = false;
                                googleDriveHelperListener.onFileOpenFailure();
                            }else{
                                isNewFile = true;
                                Drive.DriveApi.newDriveContents(mGoogleApiClient)
                                        .setResultCallback(driveContentsResultResultCallback);
                            }
                        }
                        metadataBuffer.release();
                    }else{
                        workInProgress=false;
                        googleDriveHelperListener.onFileOpenFailure();
                    }
                }
            };

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsResultResultCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                    if(driveContentsResult.getStatus().isSuccess()){
                        DriveContents contents = driveContentsResult.getDriveContents();
                        if(mode==READ_MODE){
                            //Log.d(TAG,"Reading file....");
                            InputStream inputStream = contents.getInputStream();
                            ByteArrayOutputStream result = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            int length;
                            String fileContents;
                            try{
                                while ((length = inputStream.read(buffer)) != -1) {
                                    result.write(buffer, 0, length);
                                }
                                fileContents = result.toString("UTF-8");
                                workInProgress=false;
                                googleDriveHelperListener.onSuccessfulFileOpened(fileContents);
                            }catch(IOException e){
                                workInProgress=false;
                                googleDriveHelperListener.onFileOpenFailure();
                            }
                            //Log.d(TAG,"Read Completed!");

                        }else{
                            OutputStream os = contents.getOutputStream();
                            Writer writer = new OutputStreamWriter(os);
                            try{
                                writer.write(fileContents);
                                writer.close();
                            }catch (IOException e){
                                //Log.e(TAG,e.getMessage());
                            }
                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle(fileName)
                                    .setMimeType("text/plain")
                                    .build();
                            if(isNewFile){
                                Drive.DriveApi.getRootFolder(mGoogleApiClient).createFile(mGoogleApiClient,changeSet,contents)
                                        .setResultCallback(driveFileResultResultCallback);

                            }else{
                                contents.commit(mGoogleApiClient,changeSet)
                                        .setResultCallback(statusResultCallback);
                            }
                        }
                    }
                }
            };

    final private ResultCallback<Status> statusResultCallback =
            new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    workInProgress=false;
                    if(status.getStatus().isSuccess()){
                        googleDriveHelperListener.onSuccessfulFileSaved();
                    }else{
                        googleDriveHelperListener.onFileSaveFailure();
                    }
                }
            };

    final private ResultCallback<DriveFolder.DriveFileResult> driveFileResultResultCallback =
            new ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                    workInProgress=false;
                    if(driveFileResult.getStatus().isSuccess()){
                        googleDriveHelperListener.onSuccessfulFileCreated();
                    }else{
                        googleDriveHelperListener.onFileCreateFailure();
                    }
                }
            };
    /*
    void readFileContents(DriveApi.DriveContentsResult driveContentsResult){

    }

    void openOrCreateFile(String fileNameParameter, String sourceData){
        final String sData = sourceData;
        final String fileName = fileNameParameter;
        new Thread(){
            @Override
            public void run(){
                Query query = new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE,fileName))
                        .build();
                final DriveFolder driveFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
                PendingResult<DriveApi.MetadataBufferResult> metadataBufferResultPendingResult = driveFolder.queryChildren(mGoogleApiClient,query);
                if(metadataBufferResultPendingResult.await().getStatus().isSuccess()){
                    MetadataBuffer mb = metadataBufferResultPendingResult.await().getMetadataBuffer();
                    if (mb.getCount()>0){
                        DriveFile driveFile = mb.get(0).getDriveId().asDriveFile();
                        PendingResult<DriveApi.DriveContentsResult> driveContentsResultPendingResult =
                                driveFile.open(mGoogleApiClient,DriveFile.MODE_READ_ONLY,null);
                        if(driveContentsResultPendingResult.await().getStatus().isSuccess()){
                            DriveContents contents = driveContentsResultPendingResult.await().getDriveContents();
                            InputStream inputStream = contents.getInputStream();
                            ByteArrayOutputStream result = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            int length;
                            String fileContents;
                            try{
                                while ((length = inputStream.read(buffer)) != -1) {
                                    result.write(buffer, 0, length);
                                }
                                fileContents = result.toString("UTF-8");
                                googleDriveHelperListener.onSuccessfulFileOpened(fileContents);
                            }catch(IOException e){
                                googleDriveHelperListener.onFileOpenFailure();
                            }
                        }else{
                            googleDriveHelperListener.onFileOpenFailure();
                        }
                    }
                }else{
                    PendingResult<DriveApi.DriveContentsResult> driveContentsResultPendingResult = Drive.DriveApi.newDriveContents(mGoogleApiClient);
                    if (driveContentsResultPendingResult.await().getStatus().isSuccess()){
                        DriveContents contents = driveContentsResultPendingResult.await().getDriveContents();
                        OutputStream os = contents.getOutputStream();
                        Writer writer = new OutputStreamWriter(os);
                        try{
                            writer.write(sData);
                            writer.close();
                        }catch (IOException e){
                            Log.e(TAG,e.getMessage());
                        }
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("TextEncryptor")
                                .setMimeType("text/plain")
                                .build();
                        PendingResult<DriveFolder.DriveFileResult> driveFileResultPendingResult = driveFolder.createFile(mGoogleApiClient,changeSet,contents);
                        if(driveFileResultPendingResult.await().getStatus().isSuccess()){
                            googleDriveHelperListener.onSuccessfulFileOpened(sData);
                        }else{
                            googleDriveHelperListener.onFileOpenFailure();
                        }
                    }else{
                        googleDriveHelperListener.onFileOpenFailure();
                    }
                }
            }
        }.start();
    }


    void createDummyFile(DriveApi.DriveContentsResult result){
        final DriveContents contents = result.getDriveContents();
        new Thread(){
            @Override
            public void run(){
                Query query = new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE,"TextEncryptor"))
                        .build();
                final DriveFolder driveFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
                driveFolder.queryChildren(mGoogleApiClient,query)
                        .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                            @Override
                            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                                if(metadataBufferResult==null){
                                    String sData = "This is a simple text file";
                                    OutputStream os = contents.getOutputStream();
                                    Writer writer = new OutputStreamWriter(os);
                                    try{
                                        writer.write(sData);
                                        writer.close();
                                    }catch (IOException e){
                                        Log.e(TAG,e.getMessage());
                                    }
                                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                            .setTitle("TextEncryptor")
                                            .setMimeType("text/plain")
                                            .build();
                                    PendingResult<DriveFolder.DriveFileResult> driveFileResultPendingResult = driveFolder.createFile(mGoogleApiClient,changeSet,contents);
                                    if(driveFileResultPendingResult.await().getStatus().isSuccess()){
                                        googleDriveHelperListener.onSuccessfulFileOpened(sData);
                                    }else{
                                        googleDriveHelperListener.onFileOpenFailure();
                                    }
                                }else{
                                    if(metadataBufferResult.getStatus().isSuccess()){
                                        MetadataBuffer mb = metadataBufferResult.getMetadataBuffer();
                                        if (mb.getCount()>0){
                                            DriveFile driveFile = mb.get(0).getDriveId().asDriveFile();
                                            PendingResult<DriveApi.DriveContentsResult> driveContentsResultPendingResult =
                                                    driveFile.open(mGoogleApiClient,DriveFile.MODE_READ_ONLY,null);
                                            if(driveContentsResultPendingResult.await().getStatus().isSuccess()){
                                                driveContentsResultPendingResult.await().getDriveContents();
                                            }else{
                                                googleDriveHelperListener.onFileOpenFailure();
                                            }


                                        }
                                    }
                                }
                            }
                        });
            }
        }.start();
    }

    final ResultCallback<DriveFolder.DriveFileResult> fileCallBack = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                    if(driveFileResult.getStatus().isSuccess()){
                        googleDriveHelperListener.onSuccessfulFileOpened("");
                    }else{
                        googleDriveHelperListener.onFileOpenFailure();
                    }
                }
            };
    */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        isClientConnected = true;
        workInProgress=false;
        googleDriveHelperListener.onSuccessfulConnection();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()){
            try{
                connectionResult.startResolutionForResult(activity,1);
            }catch (IntentSender.SendIntentException e){
                googleDriveHelperListener.onConnectionFailure();
            }
        }else{
            GoogleApiAvailability.getInstance().getErrorDialog(activity, connectionResult.getErrorCode(), 0).show();
        }
        workInProgress=false;
    }
}
