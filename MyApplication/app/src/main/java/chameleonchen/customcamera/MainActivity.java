package chameleonchen.customcamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;


public class MainActivity extends ActionBarActivity {

    private CameraSurfaceView  mCameraSurfaceView;

    private ImageView picturePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    private void initView() {
        picturePreview = (ImageView) findViewById(R.id.iv_picture_preview);

        mCameraSurfaceView = new CameraSurfaceView(this);
        mCameraSurfaceView.setPictureCallback(mPicture);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout.addView(mCameraSurfaceView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void capturePicture(View v) {
        mCameraSurfaceView.takePicture();
    }

    private int mode = 1;

    public void changeCamera(View v) {
        if (mode == 1) {
            mCameraSurfaceView.openBackCamera();
            mode = 2;
        }
        else {
            mCameraSurfaceView.openFrontCamera();
            mode = 1;
        }
    }


    //////////////////////////////////////////////////////////////////
    /// 照片处理
    //////////////////////////////////////////////////////////////////
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        /*
         * 拍照成功之后，Camera会回调onPictureTaken函数，并且停止预览相机，表现就是相机预览就是拍照的照片，
         * 不发生变化。
         * Camera 内部创建了一个Handler来执行 onPictureTaken函数。
         * 所以说在onPictureTaken上面实现图片的保存，并不会影响Camera的性能。
         */

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            picturePreview.setImageBitmap(bitmap);

        }

    };

}
