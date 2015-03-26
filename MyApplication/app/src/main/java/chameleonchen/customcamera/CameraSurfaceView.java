package chameleonchen.customcamera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ChameleonChen on 15/3/25.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String LOG_TAG = "CameraSurfaceView";

    private SurfaceHolder mSurfaceHolder;

    private Context mContext;

    public CameraSurfaceView(Context context) {
        super(context);

        getCameraId();

        mContext = context;

        if (mCamera == null) {
            mCamera = getCameraInstance();
        }

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public CameraSurfaceView(Context context, Camera camera) {
        this(context);
        mCamera = camera;
    }

    /* 【SurfaceView 绘图的原理】
     *  我们都知道Android是在应用程序的主线程更新UI的，用主线程渲染UI适合UI更新频率不是很高的情况下，如果需要更新
     *  的频率很高，那么利用主线程来完成这件事情显然是不科学的。
     *  对于更新频率高的绘图需求，我们希望在后台线程进行绘图。
     *  SurfaceView 就是为了满足后台绘图而设置的。
     *  我们可以通过实现 SurfaceHolder.Callback 接口绘制图形。
     *  SurfaceView只有在创建之后才能够绘图，也就是说 surfaceCreated函数和surfaceDestroyed 函数是绘图的边界。
     */

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        previewCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mSurfaceHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {

        }

        previewCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.release();
//        pictureData = null;     // 清空照片缓存区
    }

    /**
     * 预览摄像头内容
     */
    public void previewCamera() {
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.setDisplayOrientation(getPreviewDegree((Activity) mContext));
            mCamera.startPreview();
        } catch (Exception e) {
            // 失败
        }
    }

    /**
     * 获取相机与手机方向匹配时的角度
     * @param activity
     * @return
     */
    public static int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    ////////////////////////////////////////////////////////////////////////
    /// Camera
    ////////////////////////////////////////////////////////////////////////

    /** 相机对象 */
    private Camera mCamera;

    /**
     * @see #getCameraInstance(int)
     * @return
     */
    private Camera getCameraInstance() {
        return getCameraInstance(idOfFacingFront);      // 获取前置摄像头
    }

    /**
     * 获取对应ID的Camera
     * @param cameraId
     * @return
     */
    private Camera getCameraInstance(int cameraId) {

        Camera c = null;

        if (!checkCameraHardware(mContext)) {
            return null;    // 如果手机没有相机则返回null
        }

        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e("CameraSurfaceView", e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * 调用此函数将操作的摄像头切换成前置摄像头，可以预览摄像头内容，以及拍照。
     */
    public void openFrontCamera() {
        if (mCamera != null) {
            mCamera.release();      // 释放相机资源
            mCamera = null;
        }

        mCamera = getCameraInstance(idOfFacingFront);

        previewCamera();
    }

    /**
     * 调用此函数将操作的摄像头切换成后置摄像头，可以预览摄像头内容，以及拍照。
     */
    public void openBackCamera() {
        if (mCamera != null) {
            mCamera.release();      // 释放相机资源
            mCamera = null;
        }

        mCamera = getCameraInstance(idOfFacingBack);

        previewCamera();
    }

    /**
     * 检查手机设备是否有相机
     * @param context
     * @return
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private int idOfFacingFront;    // 前置摄像头的ID
    private int idOfFacingBack;     // 后置摄像头的IO

    /**
     * 获取前者摄像头和后置摄像头的ID，
     * 前置摄像头ID的值保存到 {@link #idOfFacingFront}
     * 后置摄像头ID的值保存到 {@link #idOfFacingBack}
     */
    private void getCameraId() {
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                idOfFacingFront = i;
            }
            else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                idOfFacingBack = i;
            }
        }
    }

    private Camera.PictureCallback mPictureCallback;

    public void setPictureCallback(Camera.PictureCallback callback) {
        mPictureCallback = callback;
    }

    /**
     * 拍照
     */
    public void takePicture() {
        if (mPictureCallback == null) {
            throw new NullPointerException();
        }
        mCamera.takePicture(null, null ,mPictureCallback);
    }


}

//    /**
//     * 在拍照获取的图片还没有保存之前，可以调用此函数获取图片的Bitmap格式。
//     * @return
//     * @throws java.lang.IllegalStateException 图片保存之后调用此函数；
//     *              调用 setSavingWayAfterPictureToken(1) 后调用此函数。
//     */
//    public Bitmap getPictureBitmap() {
//        if (pictureData == null) {
//            throw new IllegalStateException("照片数据为空时不可调用 getPictureBitmap() 函数");
//        }
//
//        return BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
//    }

//    /** 是否在拍照成功之后立即保存图片 */
//    private boolean isSavingImmediatelyAfterPictureToken = false;

//    /**
//     * 设置拍照之后照片的保存方式。默认是立即保存图片文件。
//     * @param way 如果为1，表示拍照之后立即将文件保存起来；
//     *            如果为2，表示拍照之后没有立即保存图片，而是等到调用{@link #savingPicture()}后保存图片。
//     */
//    public void setSavingWayAfterPictureToken(int way) {
//        if (way == 1) {
//            isSavingImmediatelyAfterPictureToken = true;
//        }
//        else if (way == 2) {
//            isSavingImmediatelyAfterPictureToken = false;
//        }
//        else {
//            throw new IllegalArgumentException();
//        }
//    }

//    /** 照片数据的缓存区 */
//    private byte[] pictureData;

//    /**
//     * 保存图片文件
//     * @throws java.lang.IllegalStateException 调用 setSavingWayAfterPictureToken(1) 调用此函数；
//     *              如果调用了该函数还抛出此异常就是拍照失败。
//     */
//    public void savingPicture() {
//        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//        if (pictureFile == null){
//            throw new NullPointerException("保存图片文件名为空");
//        }
//        if (pictureData == null) {
//            throw new IllegalStateException("图片文件为空或者文件已经保存，无需再调用 savingPicture() 函数");
//        }
//
//        try {
//            FileOutputStream fos = new FileOutputStream(pictureFile);
//            fos.write(pictureData);
//            fos.close();
//            pictureData = null;
//        } catch (FileNotFoundException e) {
//            Log.d(LOG_TAG, "File not found: " + e.getMessage());
//        } catch (IOException e) {
//            Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
//        }
//    }

//    private static final int MEDIA_TYPE_IMAGE = 1;
//    private static final int MEDIA_TYPE_VIDEO = 2;
//
//    /** Create a file Uri for saving an image or video */
//    private Uri getOutputMediaFileUri(int type){
//        return Uri.fromFile(getOutputMediaFile(type));
//    }
//
//    private String pictureFileDirectoryName;
//
//    /**
//     * 在手机的{@link android.os.Environment#DIRECTORY_PICTURES} 目录下创建一个目录，保存拍照后的图片文件.
//     * <pre>
//     *     // 设置目录
//     *     setPictureFileDirectoryName(CustomCamera);
//     *     // 拍照
//     *     takePicture();
//     * </pre>
//     * 这样子的话，照片文件保存到 /Pictures/CustomCamera/yyyyMMdd_HHmmss.jpg 中
//     * @param pictureFileDirectoryName  创建的目录名
//     */
//    public void setPictureFileDirectoryName(String pictureFileDirectoryName) {
//        this.pictureFileDirectoryName = pictureFileDirectoryName;
//    }
//
//    /** Create a File for saving an image or video */
//    private File getOutputMediaFile(int type){
//        if (pictureFileDirectoryName == null) {
//            throw new IllegalStateException("情输入保存照片的文件夹名");
//        }
//
//        Log.i(LOG_TAG, pictureFileDirectoryName);
//
//        // To be safe, you should check that the SDCard is mounted
//        // using Environment.getExternalStorageState() before doing this.
//
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES), pictureFileDirectoryName);
//        // This location works best if you want the created images to be shared
//        // between applications and persist after your app has been uninstalled.
//
//        // Create the storage directory if it does not exist
//        if (! mediaStorageDir.exists()){
//            if (! mediaStorageDir.mkdirs()){
//                Log.d("MyCameraApp", "failed to create directory");
//                return null;
//            }
//        }
//
//        // Create a media file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        File mediaFile;
//        if (type == MEDIA_TYPE_IMAGE){
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "IMG_"+ timeStamp + ".jpg");
//        } else if(type == MEDIA_TYPE_VIDEO) {
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "VID_"+ timeStamp + ".mp4");
//        } else {
//            return null;
//        }
//
//        return mediaFile;
//    }

    ////////////////////////////////////////////////////////////////////////

