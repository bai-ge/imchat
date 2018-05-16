package com.baige.imchat;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import com.baige.BaseActivity;
import com.baige.common.Parm;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.data.source.local.LocalRepository;
import com.baige.login.LoginActivity;
import com.baige.pushcore.SendMessageBroadcast;
import com.baige.service.DaemonService;
import com.baige.service.IPush;
import com.baige.service.PullService;
import com.baige.util.ActivityUtils;
import com.baige.view.CircleImageView;
import com.setting.SettingActivity;

public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;

    private MainFragment mainFragment;

    //编译之后在app\build\generated\source\aidl\debug\com\carefor\service\IPush.java下
    private IPush mIpush;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mIpush = IPush.Stub.asInterface(iBinder);
            try {
                int state = mIpush.getConnectState();
                if(mainFragment != null){
                   mainFragment.showNetwork(state);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mIpush = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        Intent intent = new Intent(this, DaemonService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);//绑定服务

         mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mainFragment, R.id.content_frame);
        }
        initView();
        MainPresenter mainPresenter = new MainPresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), mainFragment);
        registerReceiver();
    }

    private void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        mainFragment.setmDrawerLayout(mDrawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        setupDrawerContent(mNavigationView);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        CircleImageView headImg = mNavigationView.getHeaderView(0).findViewById(R.id.img_user);
        TextView textView = mNavigationView.getHeaderView(0).findViewById(R.id.txt_user_name);
        mainFragment.setDrawerUserImg(headImg);
        mainFragment.setDrawerUserName(textView);
        headImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        NavigationView.OnNavigationItemSelectedListener listener = new NavigationView.OnNavigationItemSelectedListener() {
                    Intent intent;
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.navigation_menu_home:
                                showTip(menuItem.getTitle().toString());
                                break;
                            case R.id.navigation_personal_setting:
                                showTip(menuItem.getTitle().toString());
                                break;
                            case R.id.navigation_menu_setting:
                                showTip(menuItem.getTitle().toString());
                                intent = new Intent(MainActivity.this, SettingActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.navigation_menu_info:
                                showTip(menuItem.getTitle().toString());
                                break;

                            case R.id.navigation_menu_logout:
                                showTip(menuItem.getTitle().toString());
                                intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                break;

                            case R.id.navigation_menu_exit:
                                showTip(menuItem.getTitle().toString());
                                close();
                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                };
        navigationView.setNavigationItemSelectedListener(listener);
    }

    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "动作："+action);
            if(action.equals(SendMessageBroadcast.ACTION_CONNECT_STATE)){
                Bundle bundle = intent.getExtras();
                if(bundle.containsKey(SendMessageBroadcast.KEY_CONNECT_STATE)){
                    int state = bundle.getInt(SendMessageBroadcast.KEY_CONNECT_STATE);
                    if(mainFragment != null){
                        mainFragment.showNetwork(state);
                    }
                }
            }
        }
    };

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SendMessageBroadcast.ACTION_CONNECT_STATE);
        registerReceiver(connectReceiver, intentFilter);
    }

    private void close() {
        this.finishAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        unregisterReceiver(connectReceiver);
        CacheRepository.getInstance().saveConfig(getApplicationContext());
    }
}
