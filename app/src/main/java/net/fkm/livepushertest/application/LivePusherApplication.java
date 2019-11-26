package net.fkm.livepushertest.application;

import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.multidex.MultiDexApplication;

public class LivePusherApplication extends MultiDexApplication {

    private static LivePusherApplication livePusherApplication;

    public static LivePusherApplication getInstance() {
        return livePusherApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        livePusherApplication = this;
        initTextSize();
    }

    /**
     * 使其系统更改字体大小无效
     */
    private void initTextSize() {
        Resources res = getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

}
