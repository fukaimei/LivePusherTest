package net.fkm.livepushertest.activity;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @function: 所有Activity的基类，用来处理一些公共事件，如：数据统计
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reverseStatusColor();
        setContentView(getLayoutId());
        initView();
        initData();
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * 隐藏状态栏
     */
    public void hiddenStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 改变状态栏颜色
     *
     * @param color
     */
    public void changeStatusBarColor(@ColorRes int color) {
//        StatusBarUtil.setStatusBarColor(this, color);
    }

    /**
     * 调整状态栏为亮模式，这样状态栏的文字颜色就为深模式了。
     */
    private void reverseStatusColor() {
//        StatusBarUtil.statusBarLightMode(this);
    }

    /**
     * Activity绑定到滑动关闭,
     * 在使用了浸淫式状态栏后再用效果就不太好了
     */
//    public void attachSlidr() {
//        //可以搞一个单例优化一下代码。
//        SlidrConfig config = new SlidrConfig.Builder()
//                .position(SlidrPosition.LEFT)
//                .sensitivity(1f)
//                .scrimColor(Color.BLACK)
//                .scrimStartAlpha(0.8f)
//                .scrimEndAlpha(0f)
//                .velocityThreshold(2400)
//                .distanceThreshold(0.25f)
//                .edge(true)
//                .edgeSize(0.18f) // The % of the screen that counts as the edge, default 18%
//                .build();
//        Slidr.attach(this, config);
//    }
}
