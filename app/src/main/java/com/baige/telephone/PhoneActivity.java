package com.baige.telephone;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.baige.BaseActivity;
import com.baige.data.entity.FriendView;
import com.baige.data.source.Repository;
import com.baige.data.source.local.LocalRepository;
import com.baige.imchat.R;
import com.baige.util.ActivityUtils;

import java.util.List;


/**
 * Created by baige on 2017/10/29.
 */

public class PhoneActivity extends BaseActivity {
    private final static String TAG = PhoneActivity.class.getCanonicalName();

    private Toast mToast;



    //屏幕控制
    private PowerManager.WakeLock mWakelock;

    /**
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Telephone);
        //显示在锁屏界面上
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//        try {
//            //状态栏颜色的修改在4.4和5.x环境下分别有不同的方式,低于4.4以下是不能修改的
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//                win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                win.setStatusBarColor(getResources().getColor(R.color.white));
//                //底部导航栏
//                //window.setNavigationBarColor(activity.getResources().getColor(colorResId));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        setContentView(R.layout.act_common);
        mToast = Toast.makeText(this,"", Toast.LENGTH_SHORT);

        PhoneFragment phoneFragment =
                (PhoneFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (phoneFragment == null) {
            // Create the fragment
            phoneFragment = PhoneFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), phoneFragment, R.id.content_frame);
        }
        PhonePresenter presenter = new PhonePresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), phoneFragment);

        if (getIntent().getExtras().containsKey("friend")) {
            FriendView friendView = getIntent().getExtras().getParcelable("friend");
            if (friendView != null) {
                presenter.setFriendView(friendView);
            }
        }

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "Telephone");

        requestRunTimePermission(new String[]{android.Manifest.permission.INTERNET, android.Manifest.permission.RECORD_AUDIO},
                new PermissionListener() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onGranted(List<String> grantedPermission) {

            }

            @Override
            public void onDenied(List<String> deniedPermission) {
                StringBuffer stringBuffer = new StringBuffer();
                for (String text : deniedPermission){
                    stringBuffer.append(text+"\n");
                }
                showTip("获取权限失败："+stringBuffer.toString());
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        mWakelock.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mWakelock != null){
            mWakelock.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
