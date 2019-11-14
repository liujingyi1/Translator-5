package com.android.face;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.android.face.faceapi.readsense.YMFaceTrackManager;
import com.android.face.util.SoundEffectsUtil;
import com.android.face.util.TrackDrawUtil;
import com.android.rgk.common.Constant;
import com.android.rgk.common.camera.CameraConfig;
import com.android.rgk.common.camera.CameraController;
import com.android.rgk.common.db.DataColumns;
import com.android.rgk.common.db.DataExecutor;
import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.db.UserDataUtil;
import com.android.rgk.common.db.bean.Image;
import com.android.rgk.common.db.bean.User;
import com.android.rgk.common.lock.LockManager;
import com.android.rgk.common.util.BitmapUtil;
import com.android.rgk.common.util.ImageSaver;
import com.android.rgk.common.util.LogUtil;
import com.ragentek.face.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import readsense.api.core.RSDeepFace;
import readsense.api.core.RSFaceQuality;
import readsense.api.core.RSFaceRecognition;
import readsense.api.core.RSLivenessDetect;
import readsense.api.core.RSTrack;
import readsense.api.enity.YMFace;
import readsense.api.enity.YMPerson;
import readsense.api.info.RSImageRotation;

public class FaceManager extends ViewManager {
    private static final String TAG = "FaceManager";
    private Activity mActivity;
    private CameraController mCameraController;

    private SurfaceView mDrawView;

    private FaceRecognizer mFaceRecognizer;
    private Future<SurfaceView> future;

    private int faceCount = 0;
    private int failedCount = 0;

    public FaceManager(IParentView iParentView, Activity activity, CameraController cameraController) {
        super(iParentView);
        mActivity = activity;
        mCameraController = cameraController;
        future = new Future<>();
    }

    public void onCreate() {
        if (YMFaceTrackManager.isInitialized()) {
            mFaceRecognizer = new FaceRecognizer(mActivity, mCameraController, future);
            if (DataOperator.getInstance() != null) {
                DataOperator.getInstance().addListener(mFaceRecognizer);
            }
        }
    }

    public void onYmFaceTrackInit() {
        mFaceRecognizer = new FaceRecognizer(mActivity, mCameraController, future);
        if (DataOperator.getInstance() != null) {
            DataOperator.getInstance().addListener(mFaceRecognizer);
        }
        mFaceRecognizer.onResume();
    }

    public void onStart() {
        show(ViewManager.VIEW_LAYER_2);
        if (mFaceRecognizer != null) {
            mFaceRecognizer.onResume();
        }
    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onStop() {
        if (mFaceRecognizer != null) {
            mFaceRecognizer.onPause();
        }
        hide();
    }

    public void onDestroy() {
        if (DataOperator.getInstance() != null) {
            DataOperator.getInstance().removeListener(mFaceRecognizer);
        }
        if (mFaceRecognizer != null) {
            mFaceRecognizer.onDestroy();
            mFaceRecognizer = null;
        }
    }

    public SurfaceView getSurfaceView() {
        return mDrawView;
    }

    @Override
    public View getView(int layer) {
        if (layer == ViewManager.VIEW_LAYER_DIALPAD) {
            return null;
        }
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View v = inflater.inflate(R.layout.face_reconize, null);
        mDrawView = (SurfaceView) v.findViewById(R.id.draw_view);
        mDrawView.setZOrderOnTop(true);
        mDrawView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        future.set(mDrawView);
        return v;
    }

    class FaceRecognizer extends BaseFaceTracker implements DataOperator.DataChangeListener {
        private RSTrack mRSTrack;
        private RSFaceQuality mRSFaceQuality;
        private RSDeepFace mRSDeepFace;
        private RSFaceRecognition mRSFaceRecognition;
        private RSLivenessDetect mRSLivenessDetect;

        private Future<SurfaceView> drawView;
        private SimpleArrayMap<Integer, YMFace> trackingMap;
        private Map<Integer, User> userMap;
        private static final int FRAME_COUNT = 10;
        private int frame = 0;//标识回调上来的数据帧的个数，累积到10帧，重置为0.
        boolean threadBusy = false;//标志人脸分析线程是否正在工作。
        private Thread thread;
        private boolean saveImage = false;//标志是否存储图片

        private Point size = null;

        private List<Integer> trackIdLivenessList;

        private CameraController.PreviewCallback previewCallback = new CameraController.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data) {
                if (size == null) {
                    size = CameraConfig.getInstance(CameraController.CAMERA_ID_INFRARED).getPreviewSize();
                }
                synchronized (trackIdLivenessList) {
                    int count = trackIdLivenessList.size();
                    if (count == 0) {
                        return;
                    }
                    YMFace ymFace;
                    float[] rect;
                    for (int i = 0; i < count; i++) {
                        ymFace = trackingMap.get(trackIdLivenessList.get(i));
                        if (ymFace != null) {
                            rect = ymFace.getRect();
                            if (rect != null && ymFace.getLiveness() != 1) {
                                // rect[0] = 1280 - rect[0] - rect[2];//可见光和红外预览反向
                                livenessDetect(data, size.x, size.y, rect, ymFace);
                            }
                        }
                    }
                    trackIdLivenessList.clear();
                }
            }
        };

        public FaceRecognizer(Activity activity, CameraController cameraController, Future<SurfaceView> drawView) {
            super(activity, cameraController);
            this.drawView = drawView;
            size = CameraConfig.getInstance(CameraController.CAMERA_ID_INFRARED).getPreviewSize();

            mRSTrack = YMFaceTrackManager.getRSTrack();
            mRSFaceQuality = YMFaceTrackManager.getRSFaceQuality();
            mRSDeepFace = YMFaceTrackManager.getRSDeepFace();
            mRSFaceRecognition = YMFaceTrackManager.getRSFaceRecognition();
            mRSLivenessDetect = YMFaceTrackManager.getRSLivenessDetect();
        }

        @Override
        public void onResume() {
            super.onResume();
            mCameraController.addInfraredPreviewCallback(previewCallback);
            if (trackingMap != null) {
                trackingMap.clear();
            } else {
                trackingMap = new SimpleArrayMap<>();
            }
            if (trackIdLivenessList != null) {
                trackIdLivenessList.clear();
            } else {
                trackIdLivenessList = new ArrayList<>();
            }
            userMap = UserDataUtil.updateDataSource(true);
        }

        @Override
        public void onPause() {
            mCameraController.removeInfraredPreviewCallback(previewCallback);
            //等待线程结束再执行super中释放检测器
            //让父线程等待子线程结束之后才能继续运行。此处UI线程为父线程，thread为子线程。
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                thread = null;
            }
            super.onPause();
        }

        @Override
        public boolean onBackPressed() {
            return false;
        }

        public void onDestroy() {
            userMap.clear();
            userMap = null;
        }

        @Override
        protected void drawAnim(List<YMFace> faces, SurfaceView drawView, float scaleBit, int cameraId, String fps) {
            if (drawView == null) {
                return;
            }
            TrackDrawUtil.drawFaceRecognition(faces, drawView, scaleBit, cameraId, fps, userMap, mActivity);
        }

        @Override
        protected List<YMFace> analyse(final byte[] data, int iw, int ih) {
            final List<YMFace> ymFaces = mRSTrack.runTrack(data, iw, ih, Constant.SDKOrientation);
            frame++;
            if (ymFaces != null && ymFaces.size() > 0) {
                if (!threadBusy && !stop && frame >= FRAME_COUNT) {//十帧图片识别一次，每帧都
                    threadBusy = true;
                    final byte[] yuvData = new byte[data.length];
                    System.arraycopy(data, 0, yuvData, 0, data.length);
                    if (trackingMap.size() > 50) {
                        trackingMap.clear();
                    }
                    final int width = iw;
                    final int height = ih;
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                YMFace ymFace;
                                float[] rect;
                                boolean next;
                                int trackId;
                                float[] headposes;
                                for (int i = 0, size = ymFaces.size(); i < size; i++) {
                                    ymFace = ymFaces.get(i);
                                    rect = ymFace.getRect();
                                    next = true;
                                    trackId = ymFace.getTrackId();
                                    headposes = ymFace.getHeadpose();
                                    if ((Math.abs(headposes[0]) > 30 || Math.abs(headposes[1]) > 30 || Math.abs(headposes[2]) > 30)) {
                                        next = false;
                                        LogUtil.i(TAG, "角度不佳，不再识别");
                                    }
                                    if (next) {
                                        int faceQuality = mRSFaceQuality.getFaceQuality(yuvData, width, height, Constant.SDKOrientation, ymFace.getLandmarks());
                                        if (faceQuality < 6) {
                                            next = false;
                                            LogUtil.i(TAG, "人脸质量不佳，不再识别");
                                        }
                                    }
                                    if (next) {
                                        if (!trackingMap.containsKey(trackId)) {
                                            identifyPerson(yuvData, width, height, rect, ymFace);
                                            trackingMap.put(trackId, ymFace);
                                            synchronized (trackIdLivenessList) {
                                                trackIdLivenessList.add(trackId);
                                            }
                                        } else {
                                            YMFace face = trackingMap.get(trackId);
                                            face.setRect(rect);
                                            if (face.getPersonId() <= 0) {
                                                identifyPerson(yuvData, width, height, rect, face);
                                            }
                                            if (face.getLiveness() != 1) {
                                                synchronized (trackIdLivenessList) {
                                                    trackIdLivenessList.add(trackId);
                                                }
                                            }
                                        }
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                threadBusy = false;
                            }
                        }
                    });
                    thread.start();
                }
                int size = ymFaces.size();
                YMFace ymFace;
                int trackId;
                for (int i = 0; i < size; i++) {
                    ymFace = ymFaces.get(i);
                    trackId = ymFace.getTrackId();
                    if (trackingMap.containsKey(trackId)) {
                        YMFace face = trackingMap.get(trackId);
                        ymFace.setIdentifiedPerson(face.getPersonId(), face.getConfidence());
                        ymFace.setLiveness(face.getLiveness());
                    }
                }
            }
            return ymFaces;
        }

        private void identifyPerson(byte[] yuvData, int width, int height, float[] rect, YMFace ymFace) {
            float[] feature = mRSDeepFace.getDeepFaceFeature(yuvData, width, height,
                    RSImageRotation.RS_IMG_CLOCKWISE_ROTATE_0, rect);
            YMPerson ymPerson = mRSFaceRecognition.faceIdentification(feature);
            LogUtil.i(TAG, "[identifyPerson] personId=" + (ymPerson != null ? ymPerson.getPerson_id() : 0));
            if (null != ymPerson && ymPerson.getConfidence() >= 75) {
                ymFace.setIdentifiedPerson(ymPerson.getPerson_id(), (int) ymPerson.getConfidence());
            }
        }

        private void livenessDetect(byte[] data, int width, int height, float[] rect, YMFace ymFace) {
            int liveness = mRSLivenessDetect.runLivenessDetectInfrared(
                    data, width, height, Constant.SDKOrientation, rect);
            LogUtil.i(TAG, "[livenessDetect] liveness=" + liveness
                    + "; rect:" + rect[0] + "," + rect[1] + "," + rect[2] + "," + rect[3]);
            if (BuildConfig.BUILD_TYPE.equals("debug") && liveness != 1) {
                saveImage(data, rect);
            }
            ymFace.setLiveness(liveness);
        }

        /**
         * For testing
         *
         * @param data
         * @param rect
         */
        private void saveImage(byte[] data, float[] rect) {
            Bitmap bmp = BitmapUtil.getBitmapFromYuvByte(data, iw, ih, ImageFormat.NV21);
            Bitmap bitmap = bmp.copy(bmp.getConfig(), true);
            bmp.recycle();
            bmp = null;
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);
            canvas.drawRect(new RectF(rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3]), paint);
            File tmpFile = new File("/sdcard/img/ir/out");
            if (!tmpFile.exists()) {
                tmpFile.mkdirs();
            }
            String path = "/sdcard/img/ir/out" + "/img_" + System.currentTimeMillis() + ".jpg";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                bitmap.recycle();
                bitmap = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 存储图片
         *
         * @param personId
         * @param yuvBytes
         */
        public void saveImageFromCamera(int personId, byte[] yuvBytes) {
            if (!saveImage) {
                return;
            }
            File tmpFile = new File("/sdcard/img/fr/out");
            if (!tmpFile.exists()) {
                tmpFile.mkdirs();
            }
            tmpFile = new File("/sdcard/img/fr/out" + "/img_" + System.currentTimeMillis() + "_" + personId + ".jpg");
            saveImage(tmpFile, yuvBytes);
        }

        private void saveImage(File file, byte[] yuvBytes) {

            FileOutputStream fos = null;
            try {
                YuvImage image = new YuvImage(yuvBytes, ImageFormat.NV21, iw, ih, null);
                fos = new FileOutputStream(file);
                image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, fos);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    assert fos != null;
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected SurfaceView getDrawView() {
            return drawView.get();
        }

        @Override
        protected void afterAnalyse(byte[] data, int iw, int ih, List<YMFace> ymFaces) {
            if (ymFaces == null || ymFaces.size() == 0) {
                faceCount = 0;
                if (mCameraController.getCaptureRequestSize() > 0) {
                    Bitmap bmp = BitmapUtil.getBitmapFromYuvByte(data, iw, ih, ImageFormat.NV21);
                    mCameraController.capture(bmp, null, -1);
                }
                failedCount = 0;
                return;
            }
            if (frame < FRAME_COUNT) {
                return;
            }
            frame = 0;
            Bitmap bmp = BitmapUtil.getBitmapFromYuvByte(data, iw, ih, ImageFormat.NV21);
            int size = ymFaces.size();
            int unlockIndex = 0;
            YMFace ymFace;
            User user;
            boolean unlock = false;
            for (int i = 0; i < size; i++) {
                ymFace = ymFaces.get(i);
                user = userMap.get(ymFace.getPersonId());
                if (user != null && ymFace.getLiveness() == 1) {
                    float[] facePos = ymFace.getRect();
                    LockManager.getInstance().unlockByFace(user.serverPersonId,
                            "", ymFace.getConfidence(), bmp, new Rect((int) facePos[0], (int) facePos[1],
                                    (int) (facePos[0] + facePos[2]), (int) (facePos[1] + facePos[3])));
                    unlockIndex = i;
                    unlock = true;
                    break;
                }
            }
            if (!unlock) {
                if (failedCount != -1) {
                    failedCount++;
                }
                if (failedCount == 5) {
                    failedCount = -1;
                    SoundEffectsUtil.play(SoundEffectsUtil.FACE_FAILED_ID);
                }
            }
            ArrayList<Rect> rects = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                float[] facePos = ymFaces.get(i).getRect();
                rects.add(new Rect((int) facePos[0], (int) facePos[1],
                        (int) (facePos[0] + facePos[2]), (int) (facePos[1] + facePos[3])));
            }

            if (mCameraController.getCaptureRequestSize() > 0) {
                mCameraController.capture(bmp.copy(bmp.getConfig(), false),
                        rects, unlockIndex);
            }

            if (faceCount < size) {
                String dir = Constant.ImagePath;
                File file = new File(dir);
                long total = file.getTotalSpace();
                long usable = file.getUsableSpace();
                LogUtil.d(TAG, "total=" + total + ",usable=" + usable);
                if (usable <= 100 * 1024 * 1024) {
                    List<Image> images = DataOperator.getInstance().getImages(0, size + 1);
                    ArrayList<Long> ids = new ArrayList<>();
                    for (Image image : images) {
                        File f = new File(image.file);
                        boolean success = f.delete();
                        if (success) {
                            ids.add(image.id);
                        }
                    }
                    DataOperator.getInstance().deleteImageByIds(ids);
                    DataExecutor.getInstance().execute(new ImageSaver(bmp.copy(bmp.getConfig(), false),
                            rects, dir));
                } else {
                    DataExecutor.getInstance().execute(new ImageSaver(bmp.copy(bmp.getConfig(), false),
                            rects, dir));
                }
            }
            faceCount = size;
            bmp.recycle();
            bmp = null;
        }

        @Override
        public View createView(LayoutInflater inflater, @Nullable ViewGroup container) {
            return null;
        }

        @Override
        public void destroyView(ViewGroup container) {

        }

        @Override
        public void onChanged(String table) {
            LogUtil.i(TAG, "onChanged table:" + table);
            if (DataColumns.UserColumns.TABLE_USER.equals(table)) {
                userMap = UserDataUtil.updateDataSource(true);
            }
        }
    }

    class Future<T> {
        private T mT;

        void set(T t) {
            mT = t;
        }

        T get() {
            return mT;
        }
    }
}
