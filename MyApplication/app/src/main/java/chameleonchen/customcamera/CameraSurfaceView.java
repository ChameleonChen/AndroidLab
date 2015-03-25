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
    ////////////////////////////////////////////////////////////////////////
    /// Camera
    ////////////////////////////////////////////////////////////////////////

    /** 相机对象 */
    private Camera mCamera;

    /**
     * 获取{@link android.hardware.Camera}实例。
     * @return
     */
    public Camera getCameraInstance() {
        Camera c = null;

        if (!checkCameraHardware(mContext)) {
            return null;    // 如果手机没有相机则返回null
        }

        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e("CameraSurfaceView", e.getMessage());
        }
        return c; // returns null if camera is unavailable
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
    ////////////////////////////////////////////////////////////////////////

    private SurfaceHolder mSurfaceHolder;

    private Context mContext;

    public CameraSurfaceView(Context context) {
        super(context);

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

    ////////////////////////////////////////////////////////////////////////
    /**
     * 实现{@link android.view.SurfaceHolder.Callback} 接口定义的回调函数
     */
    ////////////////////////////////////////////////////////////////////////
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
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.setDisplayOrientation(getPreviewDegree((Activity) mContext));
            mCamera.startPreview();
        } catch (Exception e) {
            // 失败
        }
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

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            // 失败
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.release();
    }

    ////////////////////////////////////////////////////////////////////////

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
}
