package com.android.face.ota;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RecoverySystem;
import android.util.Log;
import android.widget.Toast;

import com.android.face.ota.utils.Constants;
import com.android.face.ota.utils.MD5;
import com.android.face.ota.utils.UpdateInfo;
import com.android.rgk.common.lock.LockManager;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import com.ragentek.face.R;

public class UpdateActivity extends Activity{

    private static final String TAG = "UpdateActivity";
    private CompleteReceiver mReceiver;
    private DownloadManager mDownloadManager;
    private long mDownloadId;
    private ProgressDialog mDialog;
    private UpdateInfo mOTAInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota);
        mOTAInfo = getIntent().getParcelableExtra(Constants.OTA__UPDATE_IFNO);
        String url  = mOTAInfo.getUrl();
        mDownloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        if (mDownloadId != 0) {
            mDownloadManager.remove(mDownloadId);
        }
        File file = new File(Environment.getDataDirectory().getPath()+"/update.zip");
        Log.i(TAG,"update file:="+file.getAbsolutePath());
        if(file.exists()){
            file.delete();
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//        request.setDestinationInExternalPublicDir("", "update.zip");
        request.setDestinationInExternalFilesDir(this,Environment.getDataDirectory().getPath(),"update.zip");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setVisibleInDownloadsUi(false);
        mDownloadId = mDownloadManager.enqueue(request);
        mReceiver = new CompleteReceiver();
        registerReceiver(mReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        showProgressDialog();
        new Thread() {
            @Override
            public void run() {
                int progress = getOTADownloadPro(mDownloadId,mDownloadManager);
                while (progress <= 100){
                    progress = getOTADownloadPro(mDownloadId,mDownloadManager);
                    mDialog.setProgress(progress);
                }
                try{
                    Thread.sleep(100);
                }catch(Exception e){

                }
                super.run();
            }
        }.start();
    }

    private boolean checkMD5(String md5,File file){
        if (file.exists()
                && MD5.checkMd5(md5,file.getPath())){
            return true;
        }
        return false;
    }
    private void showProgressDialog(){
        mDialog = new ProgressDialog(this);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setTitle(getResources().getString(R.string.ota_tile));
        mDialog.setMessage(getResources().getString(R.string.ota_loading));
        mDialog.setMax(100);
        mDialog.setIndeterminate(false);
        mDialog.setCancelable(true);
        mDialog.show();
    }
    private void showCheckMD5Dialog(){
        mDialog.dismiss();
        Toast.makeText(this, "MD5 check failure", Toast.LENGTH_LONG).show();
    }
    private void showRebootDialog(){
        mDialog.dismiss();
        Toast.makeText(this, "The device will reboot and enter upgrade mode", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                DownloadManager.Query query = new DownloadManager.Query();
                long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                query.setFilterById(completeDownloadId);
                Cursor c = mDownloadManager.query(query);
                if (completeDownloadId == mDownloadId && c.moveToFirst()) {
                    // download successful
                    String msg="";
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    switch (status) {
                        case DownloadManager.STATUS_FAILED:
                            msg = "Download failed!";
                            break;

                        case DownloadManager.STATUS_PAUSED:
                            msg = "Download paused!";
                            break;

                        case DownloadManager.STATUS_PENDING:
                            msg = "Download pending!";
                            break;

                        case DownloadManager.STATUS_RUNNING:
                            msg = "Download in progress!";
                            break;

                        case DownloadManager.STATUS_SUCCESSFUL:
                            String localFilename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                            Boolean md5 = checkMD5(mOTAInfo.getMd5(),new File(localFilename));
                            if(md5){
                                showRebootDialog();
                                installOtaPackageAuto(context,localFilename);
                            }else{
                                showCheckMD5Dialog();
                            }

                            break;

                        default:
                            msg = "Download is nowhere in sight";
                            break;
                    }

                    Log.i(TAG, "CompleteReceiver onReceive...." + msg);
                }
            }


        }
    }

    public static int getOTADownloadPro(long id, DownloadManager downloadManager) {
        double progress = 0.0;
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(id);

        Cursor c = downloadManager.query(q);
        if (c.moveToFirst()) {
            int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            long size = c.getInt(sizeIndex);
            long downloaded = c.getInt(downloadedIndex);

            if (size != -1) {
                progress = downloaded * 100.0 / size;
            }
        }
        c.close();

        return (int)progress;
    }

    public  boolean installOtaPackageAuto(final Context context, String file) {
        Log.i(TAG,"file="+file);
        File otaPackageFile = new File(file);
        try {
            RecoverySystem.verifyPackage(otaPackageFile , null , null);
        }
        catch ( IOException e ) {
            Log.i(TAG,"e:"+e.getMessage());
            UpdateActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "RecoverySystem verifyPackage failed, file doesn't exist", Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace( );
            return false;
        }
        catch ( GeneralSecurityException e ) {
            Log.i(TAG,"e:"+e.getMessage());
            UpdateActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "RecoverySystem verifyPackage failed, invalid package ", Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace( );
            return false;
        }

        try {
//            RecoverySystem.installPackage(context , otaPackageFile);
            LockManager.getInstance().installPackage(file);
        }
        catch ( Exception e ) {
            Log.i(TAG,"installPackage e:"+e.getMessage());
           UpdateActivity.this.runOnUiThread(new Runnable() {
               @Override
                public void run() {
                    Toast.makeText(context, "RecoverySystem installPackage error, failed to install", Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace( );
            return false;
        }
        return true;
    }

}
