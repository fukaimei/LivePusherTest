package net.fkm.livepushertest.activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.alivc.live.pusher.AlivcResolutionEnum;
import com.alivc.live.pusher.SurfaceStatus;

import net.fkm.livepushertest.R;
import net.fkm.livepushertest.utils.AliyunPushUtils;
import net.fkm.livepushertest.utils.BaseTools;
import net.fkm.livepushertest.utils.CheckNetwork;
import net.fkm.livepushertest.utils.L;
import net.fkm.livepushertest.utils.ShareUtils;
import net.fkm.livepushertest.utils.ToastUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LivePusherActivity extends BaseActivity {

    public static LivePusherActivity instance;

    private AlivcLivePushConfig mAlivcLivePushConfig;
    private AlivcLivePusher mAlivcLivePusher = null;
    private SurfaceStatus mSurfaceStatus = SurfaceStatus.UNINITED;
    private boolean mAsync = false;
    private boolean videoThreadOn = false;

    private String pusherDirection;

    @BindView(R.id.preview_view)
    SurfaceView mPreviewView;
    @BindView(R.id.tv_push_info)
    TextView tv_push_info;

    @Override
    protected int getLayoutId() {
        instance = this;
        hiddenStatusBar();
        pusherDirection = ShareUtils.getString(this, "PusherDirection", "landscape");
        if ("landscape".equals(pusherDirection)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  // 横屏
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // 竖屏
        }
        return R.layout.activity_live_pusher;
    }

    @Override
    protected void initView() {
        ButterKnife.bind(this);

    }

    @Override
    protected void initData() {

        // 初始化直播推流设置信息
        initAlivcLivePushConfig();

    }

    private void initAlivcLivePushConfig() {

        mPreviewView.getHolder().addCallback(mCallback);

        String resolution = ShareUtils.getString(this, "Resolution", "540P");

        mAlivcLivePushConfig = new AlivcLivePushConfig();

        if ("landscape".equals(pusherDirection)) {
            mAlivcLivePushConfig.setPreviewOrientation(AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_RIGHT);  // 横屏推流
        } else {
            mAlivcLivePushConfig.setPreviewOrientation(AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT);  // 竖屏推流
        }
        if (resolution.equals("180P")) {
            mAlivcLivePushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_180P);
        } else if (resolution.equals("240P")) {
            mAlivcLivePushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_240P);
        } else if (resolution.equals("360P")) {
            mAlivcLivePushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_360P);
        } else if (resolution.equals("480P")) {
            mAlivcLivePushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_480P);
        } else if (resolution.equals("540P")) {
            mAlivcLivePushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_540P);
        } else if (resolution.equals("720P")) {
            mAlivcLivePushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_720P);
        } else {
            mAlivcLivePushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_540P);
        }

        mAlivcLivePusher = new AlivcLivePusher();

        try {
            mAlivcLivePusher.init(getApplicationContext(), mAlivcLivePushConfig);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            if (mSurfaceStatus == SurfaceStatus.UNINITED) {
                mSurfaceStatus = SurfaceStatus.CREATED;
                if (mAlivcLivePusher != null) {
                    try {
                        if (mAsync) {
                            mAlivcLivePusher.startPreviewAysnc(mPreviewView);
                        } else {
                            mAlivcLivePusher.startPreview(mPreviewView);
                        }
                        if (mAlivcLivePushConfig.isExternMainStream()) {
                            startYUV(getApplicationContext());
                        }
                    } catch (IllegalArgumentException e) {
                        e.toString();
                    } catch (IllegalStateException e) {
                        e.toString();
                    }
                }
            } else if (mSurfaceStatus == SurfaceStatus.DESTROYED) {
                mSurfaceStatus = SurfaceStatus.RECREATED;
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            mSurfaceStatus = SurfaceStatus.CHANGED;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            mSurfaceStatus = SurfaceStatus.DESTROYED;
        }

    };

    @OnClick({R.id.btnStartPush, R.id.btnStopPush, R.id.btnSwitchCamera})
    public void onClick(View v) {
        switch (v.getId()) {
            // 开始推流
            case R.id.btnStartPush:
                if (!CheckNetwork.isNetworkConnected(this)) {
                    ToastUtil.showToastLong(getString(R.string.network_unavailable));
                    return;
                }
                startPush();
                break;
            // 停止推流
            case R.id.btnStopPush:
                stopPush();
                break;
            // 切换摄像头
            case R.id.btnSwitchCamera:
                switchCamera();
                break;
            default:
                break;
        }
    }

    public void startPush() {

        String time = BaseTools.dateToStamp();
        // 第一个参数传递流的名称（相对于直播时的房间号），第二个参数传递过期十位数的时间戳
        String pushUrl = AliyunPushUtils.CreatePushUrl("test123", time);
        // 传递的参数值必须和推流的一致，第一个参数传递流的名称（相对于直播时的房间号），第二个参数传递过期十位数的时间戳
        String liveUrl = AliyunPushUtils.GetPlayUrl("test123", time);
        L.i("获取推流地址：" + pushUrl);
        L.i("获取拉流地址：\n" + liveUrl);
        String pushInfo = "推流URL：" + pushUrl + "\n" + liveUrl;
        tv_push_info.setText(String.format("推流和拉流地址：\n%s", pushInfo));
        try {
            mAlivcLivePusher.startPush(pushUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ToastUtil.showToast("已成功推流");

    }

    public void stopPush() {

        try {
            mAlivcLivePusher.stopPush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tv_push_info.setText("");
        ToastUtil.showToast("已停止推流");
    }

    public void switchCamera() {
        try {
            mAlivcLivePusher.switchCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ToastUtil.showToast("已切换摄像头");
    }

    public void startYUV(final Context context) {

        new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            private AtomicInteger atoInteger = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("LivePushActivity-readYUV-Thread" + atoInteger.getAndIncrement());
                return t;
            }
        }).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                videoThreadOn = true;
                byte[] yuv;
                InputStream myInput = null;
                try {
                    File f = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "alivc_resource/capture0.yuv");
                    myInput = new FileInputStream(f);
                    byte[] buffer = new byte[1280 * 720 * 3 / 2];
                    int length = myInput.read(buffer);
                    //发数据
                    while (length > 0 && videoThreadOn) {
                        mAlivcLivePusher.inputStreamVideoData(buffer, 720, 1280, 720, 1280 * 720 * 3 / 2, System.nanoTime() / 1000, 0);
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //发数据
                        length = myInput.read(buffer);
                        if (length <= 0) {
                            myInput.close();
                            myInput = new FileInputStream(f);
                            length = myInput.read(buffer);
                        }
                    }
                    myInput.close();
                    videoThreadOn = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        if (mAlivcLivePusher != null) {
            // 停止推流
            mAlivcLivePusher.stopPush();
            // 停止预览
            try {
                mAlivcLivePusher.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 释放推流
            mAlivcLivePusher.destroy();
            mAlivcLivePusher.setLivePushInfoListener(null);
            mAlivcLivePusher = null;
        }

        super.onDestroy();
    }

}
