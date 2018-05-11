package com.baige.imchat;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.baige.BaseActivity;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.data.source.local.LocalRepository;
import com.baige.login.LoginActivity;
import com.baige.util.ActivityUtils;
import com.baige.view.CircleImageView;

public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;

    private MainFragment mainFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);


         mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mainFragment, R.id.content_frame);
        }
        initView();
        MainPresenter mainPresenter = new MainPresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), mainFragment);
    }

    private void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
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
    private void close() {
        this.finishAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CacheRepository.getInstance().saveConfig(getApplicationContext());
    }
}
