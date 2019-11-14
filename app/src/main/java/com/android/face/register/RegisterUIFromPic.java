package com.android.face.register;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.face.IFaceTrackUI;
import com.android.face.faceapi.readsense.YMFaceTrackManager;
import com.android.rgk.common.Constant;
import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.db.bean.User;
import com.android.rgk.common.util.BitmapUtil;

import java.util.ArrayList;
import java.util.List;

import com.ragentek.face.R;

import readsense.api.core.RSDeepFace;
import readsense.api.core.RSDetect;
import readsense.api.core.RSFaceQuality;
import readsense.api.core.RSFaceRecognition;
import readsense.api.enity.YMFace;
import readsense.api.enity.YMPerson;
import readsense.api.info.RSImageRotation;

public class RegisterUIFromPic implements IFaceTrackUI,
        PicAdapter.OnItemCheckListener, View.OnClickListener {
    private static final String DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    private Activity mActivity;

    private RecyclerView mRecyclerView;
    private PicAdapter mAdapter;
    private CheckBox mSelectAllView;
    private List<ItemObject> mCurrentSelects = new ArrayList<>();
    private View mView;
    private Button mRegisterBtn;

    private final Object lock = new Object();
    private Bitmap head;
    private int mName = 1;

    private RSDetect rsDetect;
    private RSFaceQuality rsFaceQuality;
    private RSDeepFace rsDeepFace;
    private RSFaceRecognition rsFaceRecognition;

    public RegisterUIFromPic(Activity activity) {
        mActivity = activity;
        rsDetect = YMFaceTrackManager.getRSDetect();
        rsFaceQuality = YMFaceTrackManager.getRSFaceQuality();
        rsDeepFace = YMFaceTrackManager.getRSDeepFace();
        rsFaceRecognition = YMFaceTrackManager.getRSFaceRecognition();
    }

    @Override
    public View createView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.register_from_pic, null);
        if (container != null) {
            container.addView(view);
        }
        mSelectAllView = (CheckBox) view.findViewById(R.id.select_all_checkbox);
        mSelectAllView.setOnClickListener(this);
        mRegisterBtn = (Button) view.findViewById(R.id.register);
        mRegisterBtn.setText(mActivity.getResources().getString(R.string.ok, mCurrentSelects.size()));
        mRegisterBtn.setEnabled(false);
        mRegisterBtn.setOnClickListener(this);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 3,
                GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new PicAdapter(mActivity);
        mAdapter.setOnItemCheckListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mView = view;

        return view;
    }

    @Override
    public void destroyView(ViewGroup container) {
        if (mView != null) {
            container.removeView(mView);
            mView = null;
        }
    }

    @Override
    public void onResume() {
        mAdapter.load(DIR_PATH);
        synchronized (lock) {
            startTrack();
        }
    }

    @Override
    public void onPause() {
        synchronized (lock) {
            stopTrack();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mAdapter.onBackPressed()) {
            return true;
        }
        return false;
    }

    @Override
    public void onItemCheck(ItemObject itemObject, int position) {
        Log.d("sqm", "onItemCheck " + position);
        if (itemObject.checked) {
            mCurrentSelects.add(itemObject);
        } else {
            mCurrentSelects.remove(itemObject);
        }
        Log.d("sqm", "onItemCheck count=" + mAdapter.getPicFileCount() + ", " + mCurrentSelects.size());
        if (mCurrentSelects.size() < mAdapter.getPicFileCount()) {
            mSelectAllView.setChecked(false);
        } else {
            mSelectAllView.setChecked(true);
        }
        refreshSelectCount();
    }

    private void refreshSelectCount() {
        if (mCurrentSelects.size() == 0) {
            mRegisterBtn.setEnabled(false);
        } else {
            mRegisterBtn.setEnabled(true);
        }
        mRegisterBtn.setText(mActivity.getResources().getString(R.string.ok, mCurrentSelects.size()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_all_checkbox: {
                CheckBox checkBox = (CheckBox) v;
                List<ItemObject> itemObjects = mAdapter.getPicFiles();
                if (checkBox.isChecked()) {
                    for (ItemObject itemObject : itemObjects) {
                        if (!itemObject.isDirectory()) {
                            itemObject.checked = true;
                            mCurrentSelects.add(itemObject);
                        }
                    }
                } else {
                    for (ItemObject itemObject : itemObjects) {
                        if (!itemObject.isDirectory()) {
                            itemObject.checked = false;
                        }
                    }
                    mCurrentSelects.clear();
                }
                mAdapter.notifyItemChanged(0, itemObjects.size());
                refreshSelectCount();
                break;
            }

            case R.id.register: {
                new RegisterTask(mActivity, mCurrentSelects).execute();
            }
        }
    }

    private void doEnd(final int personId) {
        String name = generateName();
        User user = new User("" + personId, name, "", "");
        DataOperator dataOperator = DataOperator.getInstance();
        dataOperator.insert(user);
        BitmapUtil.saveBitmap(head, Constant.ImagePath + personId + ".jpg");
    }

    private String generateName() {
        String name = String.valueOf(mName++);
        if (name.length() == 1) {
            name = "0000" + name;
        } else if (name.length() == 2) {
            name = "000" + name;
        } else if (name.length() == 3) {
            name = "00" + name;
        } else if (name.length() == 4) {
            name = "0" + name;
        }
        return name;
    }

    /**
     * 初始化
     */
    public void startTrack() {
    }

    public void stopTrack() {
    }

    private void showShortToast(final Context context, final String content) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
            }
        });
    }

    class RegisterTask extends AsyncTask<Void, Integer, Integer> {
        private ProgressDialog progressDialog;
        private Activity activity;
        private List<ItemObject> itemObjects;

        public RegisterTask(Activity activity, List<ItemObject> itemObjects) {
            this.activity = activity;
            this.itemObjects = itemObjects;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return bulkRegister(itemObjects);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMax(itemObjects.size());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        private int bulkRegister(List<ItemObject> itemObjects) {
            if (itemObjects == null || itemObjects.size() == 0) {
                Toast.makeText(mActivity, "未选择照片", Toast.LENGTH_SHORT).show();
                return 0;
            }
            int successCount = 0;
            for (ItemObject itemObject : itemObjects) {
                Log.d("sqm", "item file path===" + itemObject.getFilePath());
                final Bitmap bitmap = BitmapUtil.decodeScaleImage(itemObject.getFilePath(), 1000, 1000);
                startTrack();

                List<YMFace> ymFaces = rsDetect.runDetect(bitmap, RSImageRotation.RS_IMG_CLOCKWISE_ROTATE_0);
                if (ymFaces == null || ymFaces.size() == 0) {
                    showShortToast(mActivity, "未检测到人脸");
                    bitmap.recycle();
                    continue;
                }
                //找到最大人脸框
                int maxIndex = 0;
                for (int i = 1; i < ymFaces.size(); i++) {
                    if (ymFaces.get(maxIndex).getRect()[2] <= ymFaces.get(i).getRect()[2]) {
                        maxIndex = i;
                    }
                }
                float[] rect = ymFaces.get(maxIndex).getRect();
                float[] feature = rsDeepFace.getDeepFaceFeature(bitmap, RSImageRotation.RS_IMG_CLOCKWISE_ROTATE_0, rect);
                YMPerson ymPerson = rsFaceRecognition.faceIdentification(feature);

                if (null != ymPerson && ymPerson.getConfidence() >= 75) {
                    //已经认识，不不能再添加，可以选择删除之前的重新添加。
                    showShortToast(mActivity, "已经认识，不不能再添加");
                } else {
                    //还不不认识，可以添加
                    int personId = rsFaceRecognition.personCreate(feature);
                    if (personId > 0) {
                        //添加成功，此返回值即为数据库对当前⼈人脸的中唯⼀一标识
                        head = Bitmap.createBitmap(bitmap, (int) rect[0], (int) rect[1], (int) rect[2], (int) rect[3], null, true);
                        doEnd(personId);
                        successCount++;
                        publishProgress(successCount);
                    } else {
                        //personId < 0 //添加失败
                        showShortToast(mActivity, "添加失败");
                    }
                }
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
            return successCount;
        }
    }
}
