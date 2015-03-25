package chameleonchen.customcamera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by ChameleonChen on 15/3/25.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

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
    }

    private void previewCamera() {
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

    ////////////////////////////////////////////////////////////////////////
}
