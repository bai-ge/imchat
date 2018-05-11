package com.baige.imchat;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;


import com.baige.BaseActivity;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.data.source.local.LocalRepository;
import com.baige.login.LoginActivity;
import com.baige.util.ActivityUtils;

public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        initView();

        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mainFragment, R.id.content_frame);
        }
        MainPresenter mainPresenter = new MainPresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), mainFragment);
    }

    private void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        setupDrawerContent(mNavigationView);
    }

    private void setupDrawerContent(NavigationView navigationView) {
//        ImageView headImg = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.head_img);
//        headImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, EditUserActivity.class);
//                startActivity(intent);
//            }
//        });
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
