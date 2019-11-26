package net.fkm.livepushertest.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import androidx.core.app.ActivityCompat;

import net.fkm.livepushertest.R;
import net.fkm.livepushertest.utils.CheckNetwork;
import net.fkm.livepushertest.utils.CommonUtil;
import net.fkm.livepushertest.utils.ShareUtils;
import net.fkm.livepushertest.utils.ToastUtil;
import net.fkm.livepushertest.view.widget.ClearEditText;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomeActivity extends BaseActivity {

    public static HomeActivity instance;
    private static final int REQUEST_STATE_CODE = 1020;

    @BindView(R.id.etPushDomain)
    ClearEditText etPushDomain;
    @BindView(R.id.etLiveDomain)
    ClearEditText etLiveDomain;
    @BindView(R.id.etPushKey)
    ClearEditText etPushKey;
    @BindView(R.id.etLiveKey)
    ClearEditText etLiveKey;
    @BindView(R.id.mRadioGroup)
    RadioGroup mRadioGroup;
    @BindViews({R.id.checkbox1, R.id.checkbox2, R.id.checkbox3, R.id.checkbox4, R.id.checkbox5, R.id.checkbox6})
    List<CheckBox> radios; // 单选组

    @Override
    protected int getLayoutId() {
        instance = this;
        return R.layout.activity_home_layout;
    }

    @Override
    protected void initView() {
        ButterKnife.bind(this);
        ShareUtils.putString(this, "Resolution", "540P");
        ShareUtils.putString(this, "PusherDirection", "portrait");

        String pushDomain = ShareUtils.getString(this, "PushDomain", "");
        String liveDomain = ShareUtils.getString(this, "LiveDomain", "");
        String pushKey = ShareUtils.getString(this, "PushKey", "");
        String liveKey = ShareUtils.getString(this, "LiveKey", "");

        etPushDomain.setText(pushDomain);
        etLiveDomain.setText(liveDomain);
        etPushKey.setText(pushKey);
        etLiveKey.setText(liveKey);
    }

    @Override
    protected void initData() {

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbLandscape) {
                    ShareUtils.putString(HomeActivity.this, "PusherDirection", "landscape");
                } else if (checkedId == R.id.rbPortrait) {
                    ShareUtils.putString(HomeActivity.this, "PusherDirection", "portrait");
                }
            }
        });

    }

    @OnClick({R.id.btnTest})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnTest:
                //  判断是否为Android 6.0 以上的系统版本，如果是，需要动态添加权限
                if (Build.VERSION.SDK_INT >= 23) {
                    showPermissions();
                } else {
                    startPushActivity();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 单选项点击事件
     *
     * @param checkBox
     */
    @OnClick({R.id.checkbox1, R.id.checkbox2, R.id.checkbox3, R.id.checkbox4, R.id.checkbox5, R.id.checkbox6})
    void changeRadios(CheckBox checkBox) {
        CommonUtil.unCheck(radios);
        checkBox.setChecked(true);
        // 显示选中项值
        String checkedValues = CommonUtil.getOne(radios);
        if ("chb1".equals(checkedValues)) {
            ShareUtils.putString(this, "Resolution", "180P");
        } else if ("chb2".equals(checkedValues)) {
            ShareUtils.putString(this, "Resolution", "240P");
        } else if ("chb3".equals(checkedValues)) {
            ShareUtils.putString(this, "Resolution", "360P");
        } else if ("chb4".equals(checkedValues)) {
            ShareUtils.putString(this, "Resolution", "480P");
        } else if ("chb5".equals(checkedValues)) {
            ShareUtils.putString(this, "Resolution", "540P");
        } else if ("chb6".equals(checkedValues)) {
            ShareUtils.putString(this, "Resolution", "720P");
        }
    }

    public void startPushActivity() {

        String pushDomain = etPushDomain.getText().toString().trim();
        String liveDomain = etLiveDomain.getText().toString().trim();
        String pushKey = etPushKey.getText().toString().trim();
        String liveKey = etLiveKey.getText().toString().trim();

        if (TextUtils.isEmpty(pushDomain) || TextUtils.isEmpty(liveDomain)
                || TextUtils.isEmpty(pushKey) || TextUtils.isEmpty(liveKey)) {
            ToastUtil.showToastLong("推流播流域名或Key值不能为空");
            return;
        }

        if (!CheckNetwork.isNetworkConnected(this)) {
            ToastUtil.showToastLong(getString(R.string.network_unavailable));
            return;
        }

        ShareUtils.putString(this, "PushDomain", pushDomain);
        ShareUtils.putString(this, "LiveDomain", liveDomain);
        ShareUtils.putString(this, "PushKey", pushKey);
        ShareUtils.putString(this, "LiveKey", liveKey);

        startActivity(new Intent(this, LivePusherActivity.class));

        finish();

    }

    /**
     * Android 6.0 以上的版本的定位方法
     */
    public void showPermissions() {
        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_STATE_CODE);
        } else {
            startPushActivity();
        }
    }

    //Android 6.0 以上的版本申请权限的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case REQUEST_STATE_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startPushActivity();
                } else {
                    // 没有获取到权限，做特殊处理
                    ToastUtil.showToastLong("获取相关权限失败，请手动开启方可使用相应的功能");
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
