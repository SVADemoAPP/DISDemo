package com.gyr.disvisibledemo.fragment;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gyr.disvisibledemo.R;
import com.gyr.disvisibledemo.framework.sharef.CameraPermissionHelper;
import com.gyr.disvisibledemo.framework.sharef.DisplayRotationHelper;
import com.gyr.disvisibledemo.rending.BackgroundRenderer;
import com.gyr.disvisibledemo.rending.PlaneRenderer;
import com.gyr.disvisibledemo.rending.PointCloudRenderer;
import com.gyr.disvisibledemo.rending.VirtualObjectRenderer;
import com.gyr.disvisibledemo.util.UtilsCommon;
import com.huawei.hiar.ARAnchor;
import com.huawei.hiar.ARCamera;
import com.huawei.hiar.ARConfigBase;
import com.huawei.hiar.AREnginesApk;
import com.huawei.hiar.AREnginesSelector;
import com.huawei.hiar.ARFrame;
import com.huawei.hiar.ARLightEstimate;
import com.huawei.hiar.ARPlane;
import com.huawei.hiar.ARPointCloud;
import com.huawei.hiar.ARPose;
import com.huawei.hiar.ARSession;
import com.huawei.hiar.ARTrackable;
import com.huawei.hiar.ARWorldTrackingConfig;
import com.huawei.hiar.exceptions.ARUnSupportedConfigurationException;
import com.huawei.hiar.exceptions.ARUnavailableClientSdkTooOldException;
import com.huawei.hiar.exceptions.ARUnavailableDeviceNotCompatibleException;
import com.huawei.hiar.exceptions.ARUnavailableEmuiNotCompatibleException;
import com.huawei.hiar.exceptions.ARUnavailableServiceApkTooOldException;
import com.huawei.hiar.exceptions.ARUnavailableServiceNotInstalledException;
import com.huawei.hiar.exceptions.ARUnavailableUserDeclinedInstallationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ARFragment extends Fragment implements GLSurfaceView.Renderer {
    private static final String TAG = ARFragment.class.getSimpleName();
    private ARSession mSession;
    private GLSurfaceView mSurfaceView;
    private List<ARAnchor> mARAnchors = new ArrayList<>();
    private BackgroundRenderer mBackgroundRenderer = new BackgroundRenderer();
    private VirtualObjectRenderer mVirtualObject = new VirtualObjectRenderer();
    private PlaneRenderer mPlaneRenderer = new PlaneRenderer();
    private PointCloudRenderer mPointCloud = new PointCloudRenderer();
    private DisplayRotationHelper mDisplayRotationHelper;
    private float arx;
    private float ary;
    private final float[] mAnchorMatrix = new float[UtilsCommon.MATRIX_NUM];
    private boolean installRequested;
    private static final float[] DEFAULT_COLOR = new float[]{0f, 0f, 0f, 0f};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mArView = inflater.inflate(R.layout.fragment_ar_layout, container, false);
        initView(mArView);
        initAREngine();
        return mArView;
    }

    private void initView(View view) {
        mSurfaceView = view.findViewById(R.id.surfaceview);
        mDisplayRotationHelper = new DisplayRotationHelper(getContext());
    }

    private void initAREngine(){
        Exception exception = null;
        String message = null;
        if (null == mSession) {
            try {
                //If you do not want to switch engines, AREnginesSelector is useless.
                // You just need to use AREnginesApk.requestInstall() and the default engine
                // is Huawei AR Engine.
                AREnginesSelector.AREnginesAvaliblity enginesAvaliblity = AREnginesSelector.checkAllAvailableEngines(getContext());
                if ((enginesAvaliblity.ordinal() &
                        AREnginesSelector.AREnginesAvaliblity.HWAR_ENGINE_SUPPORTED.ordinal()) != 0) {

                    AREnginesSelector.setAREngine(AREnginesSelector.AREnginesType.HWAR_ENGINE);

                    switch (AREnginesApk.requestInstall(getActivity(), !installRequested)) {
                        case INSTALL_REQUESTED:
                            installRequested = true;
                            return;
                        case INSTALLED:
                            break;
                    }

                    if (!CameraPermissionHelper.hasPermission(getActivity())) {
                        CameraPermissionHelper.requestPermission(getActivity());
                        return;
                    }

                    mSession = new ARSession(/*context=*/getContext());
                    ARConfigBase config = new ARWorldTrackingConfig(mSession);
                    mSession.configure(config);
                } else {
                    message = "This device does not support Huawei AR Engine ";
                }
            } catch (ARUnavailableServiceNotInstalledException e) {
                message = "Please install HuaweiARService.apk";
                exception = e;
            } catch (ARUnavailableServiceApkTooOldException e) {
                message = "Please update HuaweiARService.apk";
                exception = e;
            } catch (ARUnavailableClientSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (ARUnavailableDeviceNotCompatibleException e) {
                message = "This device does not support Huawei AR Engine ";
                exception = e;
            } catch (ARUnavailableEmuiNotCompatibleException e) {
                message = "Please update EMUI version";
                exception = e;
            } catch (ARUnavailableUserDeclinedInstallationException e) {
                message = "Please agree to install!";
                exception = e;
            } catch (ARUnSupportedConfigurationException e) {
                message = "The configuration is not supported by the device!";
                exception = e;
            } catch (Exception e) {
                message = "exception throwed";
                exception = e;
            }
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Creating sesson", exception);
                if (mSession != null) {
                    mSession.stop();
                    mSession = null;
                }
                return;
            }
        }

      /*  mSession.resume();
        mSurfaceView.onResume();
        mDisplayRotationHelper.onResume();*/

    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        mBackgroundRenderer.createOnGlThread(/*context=*/getContext());

        try {
            mVirtualObject.createOnGlThread(/*context=*/getContext(), "AR_logo.obj", "AR_logo.png");
            mVirtualObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to read plane texture");
        }
        try {
            mPlaneRenderer.createOnGlThread(/*context=*/getContext(), "trigrid.png");
        } catch (IOException e) {
            Log.e(TAG, "Failed to read plane texture");
        }

        mPointCloud.createOnGlThread(/*context=*/getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mDisplayRotationHelper.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        //showFpsTextView(String.valueOf(FPSCalculate()));
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (null == mSession) {
            return;
        }
        mDisplayRotationHelper.updateSessionIfNeeded(mSession);

        try {
            mSession.setCameraTextureName(mBackgroundRenderer.getTextureId());
            ARFrame frame = mSession.update();
            ARCamera camera = frame.getCamera();
            mBackgroundRenderer.draw(frame);

            if (camera.getTrackingState() == ARTrackable.TrackingState.PAUSED) {
                return;
            }
            ARPose currentPost = camera.getDisplayOrientedPose();
            arx = currentPost.tx();
            ary = currentPost.tz();

            float[] projmtx = new float[UtilsCommon.MATRIX_NUM];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            float[] viewmtx = new float[UtilsCommon.MATRIX_NUM];
            camera.getViewMatrix(viewmtx, 0);


            ARPointCloud arPointCloud = frame.acquirePointCloud();
            mPointCloud.update(arPointCloud);
            mPointCloud.draw(viewmtx, projmtx);
            arPointCloud.release();

            mPlaneRenderer.drawPlanes(mSession.getAllTrackables(ARPlane.class), camera.getDisplayOrientedPose(), projmtx);

            drawAnchor(frame, projmtx, viewmtx);

        } catch (Throwable t) {
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    /**
     * 绘制AR锚点
     * @param frame
     * @param projmtx
     * @param viewmtx
     * @throws RemoteException
     */
    private void drawAnchor(ARFrame frame, float[] projmtx, float[] viewmtx) throws RemoteException {

        ARLightEstimate le = frame.getLightEstimate();
        float lightIntensity = 1;
        if (le.getState() != ARLightEstimate.State.NOT_VALID) {
            lightIntensity = le.getPixelIntensity();
        }
        Iterator<ARAnchor> ite = mARAnchors.iterator();
        while (ite.hasNext()) {
            ARAnchor coloredAnchor = ite.next();
            if (coloredAnchor.getTrackingState() == ARTrackable.TrackingState.STOPPED) {
                ite.remove();
            } else {
                coloredAnchor.getPose().toMatrix(mAnchorMatrix, 0);
                mVirtualObject.updateModelMatrix(mAnchorMatrix, 0.15f);
                mVirtualObject.draw(viewmtx, projmtx, lightIntensity, new float[] {66.0f, 133.0f, 244.0f, 255.0f});
            }
        }
    }


}
