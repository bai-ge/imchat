package com.baige.imchat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.NotificationCompat;
import android.support.v7.view.menu.MenuPopupHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.baige.BaseApplication;
import com.baige.adapter.FriendAdapter;
import com.baige.adapter.LastChatMsgAdapter;
import com.baige.adapter.UserAdapter;
import com.baige.chat.ChatActivity;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.LastChatMsgInfo;
import com.baige.data.entity.User;
import com.baige.data.source.cache.CacheRepository;
import com.baige.filelist.FileListActivity;
import com.baige.friend.FriendActivity;
import com.baige.login.LoginActivity;
import com.baige.search.SearchActivity;
import com.baige.util.BitmapTools;
import com.baige.util.FileUtils;
import com.baige.util.GlideImageLoader;
import com.baige.util.ImageLoader;
import com.baige.util.Tools;
import com.baige.view.CircleImageView;
import com.google.common.collect.BiMap;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.setting.SettingActivity;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by baige on 2018/5/4.
 */

public class MainFragment extends Fragment implements MainContract.View, BottomNavigationBar.OnTabSelectedListener {

    private final static String TAG = MainFragment.class.getSimpleName();

    private Toast mToast;

    private Handler mHandler;

    private MainContract.Presenter mPresenter;

    private CircleImageView mTitleHeadImg;

    private Button mBtnMenu;

    private ViewPager mViewPager;

    private BottomNavigationBar mBottomNavigationBar;

    private PagerAdapter mPagerAdapter;

    private List<View> mViewList = new ArrayList<>();

    private NotificationManager mNotificationManager; //通知服务

    private final static int mNotificationId = 125;

    //组件
    /*主界面 抽屉*/
    private CircleImageView mDrawerUserImg;

    private TextView mDrawerUserName;


    /*消息*/
    private BadgeItem mBadgeItem;//消息上的小点

    private ListView mLastChatListView;

    private ViewGroup mLastChatNothingView;

    private LastChatMsgAdapter mLastChatMsgAdapter;

    /*好友*/
    private ListView mFriendsListView;

    private ViewGroup mFriendsNothingView;

    private FriendAdapter mFriendsAdapter;

    /*文件*/
    private ImageButton mBtnSystemFiles;

    private ImageButton mBtnDownloadFiles;

    private ImageButton mBtnShareFiles;

    /*个人信息*/

    private CircleImageView mCircleImageView;

    private TextView mTxtUserName;

    private EditText mEditAlias;

    private ImagePicker mImagePicker;

    private final int IMAGE_PICKER = 100;

    private ArrayList<ImageItem> images = null;




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mHandler = new Handler();
        mNotificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);

        mFriendsAdapter = new FriendAdapter(new ArrayList<FriendView>(), mOnFriendItemListener);
        mLastChatMsgAdapter = new LastChatMsgAdapter(new ArrayList<LastChatMsgInfo>(), mOnLastChatMsgItemListener);
        mImagePicker = ImagePicker.getInstance();
        mImagePicker.setImageLoader(new GlideImageLoader());//设置图片加载器

        mImagePicker.setMultiMode(false);
        mImagePicker.setStyle(CropImageView.Style.CIRCLE);


        mImagePicker.setShowCamera(true);  //显示拍照按钮
        mImagePicker.setCrop(true);        //允许裁剪（单选才有效）
        mImagePicker.setSaveRectangle(true); //是否按矩形区域保存
//        mImagePicker.setSelectLimit(9);    //选中数量限制
//       mImagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
//        mImagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
//        mImagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
//        mImagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
//        mImagePicker.setOutPutY(1000);//保存文件的高度。单位像素
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_main, container, false);
        View pagerView = inflater.inflate(R.layout.pager_message, container, false);
        initMessageView(pagerView);
        mViewList.add(pagerView);

        pagerView = inflater.inflate(R.layout.pager_friends, container, false);
        initFriendsView(pagerView);
        mViewList.add(pagerView);

        pagerView = inflater.inflate(R.layout.pager_file, container, false);
        initFileView(pagerView);
        mViewList.add(pagerView);

        pagerView = inflater.inflate(R.layout.pager_person, container, false);
        initPersonView(pagerView);
        mViewList.add(pagerView);

        initView(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        CacheRepository cacheRepository = CacheRepository.getInstance();
        if (!cacheRepository.isLogin()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            getActivity().startActivity(intent);
        } else {
            mPresenter.start();
        }
    }


    private void initView(View root) {
        mTitleHeadImg = root.findViewById(R.id.user_img);
        mBtnMenu = root.findViewById(R.id.title_menu);
        mViewPager = root.findViewById(R.id.view_pager);
        mBottomNavigationBar = root.findViewById(R.id.bottom_navigation_bar);

//        mBottomNavigationBar.setMode(BottomNavigationBar.MODE_SHIFTING);
        mBottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        mBottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        mBottomNavigationBar.setBarBackgroundColor(R.color.white);
        mBottomNavigationBar.setInActiveColor(R.color.dark_gray);
        mBottomNavigationBar.setActiveColor(R.color.blue);

        mBtnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });

        mBadgeItem = new BadgeItem();
        mBadgeItem.setHideOnSelect(false)
                .setText("10")
                .setBackgroundColorResource(R.color.orange)
                .setBorderWidth(0);

        mBottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.ic_msg, R.string.tab_message).setBadgeItem(mBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_friends, R.string.tab_friends))
                .addItem(new BottomNavigationItem(R.drawable.ic_folder, R.string.tab_file))
                .addItem(new BottomNavigationItem(R.drawable.ic_person, R.string.tab_person))
                .setFirstSelectedPosition(0)
                .initialise();

        mBottomNavigationBar.setTabSelectedListener(this);
        //mBottomNavigationBar.selectTab();

        mPagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return mViewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView(mViewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mViewList.get(position));
                return mViewList.get(position);
            }

        };
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mBottomNavigationBar.selectTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setAdapter(mPagerAdapter);
    }

    private void initMessageView(View view) {
        mLastChatListView = view.findViewById(R.id.list_view);
        mLastChatNothingView = view.findViewById(R.id.layout_null);
        mLastChatListView.setAdapter(mLastChatMsgAdapter);
        mLastChatNothingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLastChatMsgAdapter.addItem(new LastChatMsgInfo("baige", "百戈", "这是一条信息", System.currentTimeMillis() - 451257, 129));
            }
        });
        mLastChatMsgAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mLastChatMsgAdapter.getCount() == 0) {
                    mLastChatNothingView.setVisibility(View.VISIBLE);
                    mBadgeItem.hide();
                } else {
                    mLastChatNothingView.setVisibility(View.INVISIBLE);
                    int count = mLastChatMsgAdapter.getMessageCount();
                    if(count == 0){
                        mBadgeItem.hide();
                    }else{
                        if(count <= 99){
                            mBadgeItem.setText(String.valueOf(count));
                        }else{
                            mBadgeItem.setText("99+");
                        }
                        mBadgeItem.show();
                    }
                }
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
            }
        });
    }

    private void initFriendsView(View view) {
        mFriendsListView = view.findViewById(R.id.list_view);
        mFriendsNothingView = view.findViewById(R.id.layout_null);
        mFriendsListView.setAdapter(mFriendsAdapter);
        mFriendsAdapter.addItem(new FriendView("茵茵", "Ant", "1851454215"));
        mFriendsNothingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFriendsAdapter.addItem(new FriendView("茵茵", "Ant", "1851454215"));
                Log.d(TAG, "size:" + mFriendsAdapter.getCount());
            }
        });
        mFriendsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mFriendsAdapter.getCount() == 0) {
                    mFriendsNothingView.setVisibility(View.VISIBLE);
                } else {
                    mFriendsNothingView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
            }
        });

    }

    private void initFileView(View view) {
        mBtnSystemFiles = view.findViewById(R.id.btn_system_files);
        mBtnDownloadFiles = view.findViewById(R.id.btn_download_files);
        mBtnShareFiles = view.findViewById(R.id.btn_share_files);

        mBtnSystemFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FileListActivity.class);
                startActivity(intent);
            }
        });

    }

    private void initPersonView(View view) {
        mCircleImageView = view.findViewById(R.id.img_user);
        mTxtUserName = view.findViewById(R.id.txt_user_name);
        mEditAlias = view.findViewById(R.id.edit_user_alias);
        mEditAlias.setEnabled(false);
        view.findViewById(R.id.btn_edit_alias).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditAlias.setEnabled(!mEditAlias.isEnabled());
                if (mEditAlias.isEnabled()) {
                    mEditAlias.requestFocus();
                    Tools.showInputMethod(getContext(), mEditAlias);
                    mEditAlias.setSelection(mEditAlias.getText().length());
                } else {
                    mPresenter.updateAlias(mEditAlias.getText().toString());
                }
            }
        });
        mEditAlias.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "alias beforeTextChanged()" + charSequence.toString());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "alias onTextChanged()" + charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "alias afterTextChanged()" + editable);
                //CacheRepository.getInstance().who().setAlias(mEditAlias.getText().toString());
            }
        });
        mEditAlias.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d(TAG, "alias OnFocus()" + b);
            }
        });
        mEditAlias.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_BACK)) {
                    Log.d(TAG, "editTextvalue:" + mEditAlias.getText());
                    mEditAlias.setEnabled(false);
                    mPresenter.updateAlias(mEditAlias.getText().toString());
                    return true;
                }
                return false;
            }
        });
        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mImagePicker.setMultiMode(false);
                mImagePicker.setStyle(CropImageView.Style.CIRCLE);

                int size = mCircleImageView.getWidth();
                Integer radius = size / 3;
                radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, getResources().getDisplayMetrics());
                Log.d(TAG, "radius =" + radius);
                mImagePicker.setFocusWidth(radius * 2);
                mImagePicker.setFocusHeight(radius * 2);
                mImagePicker.setOutPutX(radius * 2);
                mImagePicker.setOutPutY(radius * 2);

                Intent intent = new Intent(getActivity(), ImageGridActivity.class);
                intent.putExtra(ImageGridActivity.EXTRAS_IMAGES, images);
                startActivityForResult(intent, IMAGE_PICKER);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onResult()" + requestCode);
        if (data != null && requestCode == IMAGE_PICKER) {
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            int size = mCircleImageView.getWidth();
            File headPath = new File(BaseApplication.headImgPath);
            if(!headPath.exists()){
                headPath.mkdirs();
            }
            File oldFile = new File(images.get(0).path);
            File imgFile = new File(headPath, oldFile.getName());
            FileUtils.moveTo(oldFile, imgFile);
            images.get(0).path = imgFile.getAbsolutePath();
            Log.d(TAG, "img  =" + images.get(0).toString() + "name:"+images.get(0).name);
            Log.d(TAG, "截图图片宽度："+size);
            Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(images.get(0).path, size);
            if (bitmap != null) {
                ImageLoader.getInstance().addBitmapToMemoryCache(images.get(0).path, bitmap);
                showUserImg(bitmap);
            }
            mPresenter.changeImg(images.get(0).path);
            // mImagePicker.getImageLoader().displayImage(getActivity(), images.get(0).path, mCircleImageView, size / 4, size / 4);
            Log.d(TAG, "img path =" + images.get(0).path);
        } else {
            showTip("没有数据");
            Log.d(TAG, "onResult()" + null);
        }
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onTabSelected(int position) {
        mViewPager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

    private FriendAdapter.OnFriendItemListener mOnFriendItemListener = new FriendAdapter.OnFriendItemListener() {
        @Override
        public void onClickItem(FriendView item) {
            Intent intent = new Intent(getContext(), FriendActivity.class);
            Log.d(TAG, item.toString());
            intent.putExtra("friend", item);
            startActivity(intent);
        }

        @Override
        public void onLongClickItem(FriendView item) {
            mFriendsAdapter.clear();
        }
    };

    private LastChatMsgAdapter.OnLastChatMsgItemListener mOnLastChatMsgItemListener = new LastChatMsgAdapter.OnLastChatMsgItemListener() {
        @Override
        public void onClickItem(LastChatMsgInfo item) {
            Log.d(TAG, "onclickItem "+item);
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            FriendView friendView = CacheRepository.getInstance().getFriendViewObservable().get(item.getUid());
            intent.putExtra("friend", friendView);
            Log.d(TAG, "friend"+friendView);
            startActivity(intent);
        }

        @Override
        public void onLongClickItem(LastChatMsgInfo item) {
            mLastChatMsgAdapter.clear();
        }
    };


    @Override
    public void showTip(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
                mToast.show();
            }
        });
    }

    @Override
    public void showUserImg(final String imgName) {
        Log.d(TAG, imgName);
        if (!Tools.isEquals(mCircleImageView.getTag(), imgName)) {
            Log.d(TAG, "加载"+imgName);
            String url = BaseApplication.headImgPath + File.separator + imgName;
            Bitmap bitmap = ImageLoader.getInstance().getBitmapFromMemoryCache(url);
            if(bitmap == null){
                Log.d(TAG, "从文件"+imgName);
                int size = mCircleImageView.getWidth();
                Log.d(TAG, "图片宽度："+size);
                if(size <= 10){
                    size = 200;
                }
                bitmap = ImageLoader.decodeSampledBitmapFromResource(url, size);
            }
            if (bitmap == null) {
                Log.d(TAG, "从网络"+imgName);
                mPresenter.downloadImg(imgName);
            } else {
                Log.d(TAG, "显示"+imgName);
                mCircleImageView.setTag(imgName);
                ImageLoader.getInstance().addBitmapToMemoryCache(url, bitmap);
                showUserImg(bitmap);
            }
        }
    }

    @Override
    public void showUserImg(final Bitmap img) {
        if (img != null) {
//            final Bitmap bitmap = img.copy(Bitmap.Config.ARGB_8888, true);
            if(img != null){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCircleImageView != null) {
                            mCircleImageView.setImageBitmap(img);
                        }
                        if (mTitleHeadImg != null) {
                            mTitleHeadImg.setImageBitmap(img);
                        }
                        if (mDrawerUserImg != null) {
                            mDrawerUserImg.setImageBitmap(img);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void showUserName(final String name) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Tools.isEmpty(name)) {
                    mTxtUserName.setText("");
                    if (mDrawerUserName != null) {
                        mDrawerUserName.setText("");
                    }
                } else {
                    mTxtUserName.setText(name);
                    if (mDrawerUserName != null) {
                        mDrawerUserName.setText(name);
                    }
                }
            }
        });
    }

    @Override
    public void showUserAlias(final String alias) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Tools.isEmpty(alias)) {
                    mEditAlias.setText("");
                } else {
                    mEditAlias.setText(alias);
                }
            }
        });
    }


    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add_friend:
                        Intent intent = new Intent(getContext(), SearchActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.refresh:
                        mPresenter.loadFriends();
                        mPresenter.loadMsg();
                        break;
                    case R.id.add_notify:
                        setSelfNotification();
                        break;
                    case R.id.cancel_notify:
                        cancelNotification();
                        break;
                }
                return false;
            }
        });
        setIconEnable(popupMenu.getMenu(), true);
        popupMenu.show();
    }

    private void setIconEnable(Menu menu, boolean enable)
    {
        try
        {
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            //传入参数
            m.invoke(menu, enable);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /*
    小图标，通过 setSmallIcon() 方法设置
    标题，通过 setContentTitle() 方法设置
    内容，通过 setContentText() 方法设置
*/
    protected void setSelfNotification() {
        // TODO Auto-generated method stub

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setSmallIcon(R.mipmap.ic_launcher)//通知图标
                .setOngoing(true)//true，设置他为一个正在进行的通知
                .setAutoCancel(true)//用户点击就自动消失
                .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL))//Notification.FLAG_AUTO_CANCEL
                .setContent(getRemoteView()); //根据当前版本返回一个合适的视图

        mNotificationManager.notify(mNotificationId, builder.build());


//        Notification notification = new Notification();
//        notification.icon = R.drawable.ic_album;//通知图标
//        notification.when = System.currentTimeMillis();//通知产生的时间，会在通知信息里显示
//        notification.flags |= Notification.FLAG_NO_CLEAR;
//
//        RemoteViews remoteViews = getRemoteView();
//
//        notification.contentView = remoteViews;
//        notification.contentIntent = getDefalutIntent(Notification.FLAG_AUTO_CANCEL);
//
//        mNotificationManager.notify(mNotificationId, notification);
    }
    protected void cancelNotification() {
        mNotificationManager.cancel(mNotificationId);
    }

    private RemoteViews getRemoteView() {
        RemoteViews remoteViews = new RemoteViews(getContext().getPackageName(), R.layout.notify_normal);
        remoteViews.setTextViewText(R.id.notify_title, "添加好友");
        remoteViews.setTextViewText(R.id.notify_time, Tools.getSuitableTimeFormat(System.currentTimeMillis()));
        remoteViews.setTextViewText(R.id.notify_content, "百戈请求添加您为好友");
        User user = CacheRepository.getInstance().who();
        if(user != null && !Tools.isEmpty(user.getImgName())){
            String url = BaseApplication.headImgPath + File.separator + user.getImgName();
            Bitmap bitmap = ImageLoader.getInstance().getBitmapFromMemoryCache(url+"notify");
            Bitmap showBitmap = null;
            if(bitmap != null){
                showBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                remoteViews.setImageViewBitmap(R.id.notify_img, showBitmap);
            }else{
                bitmap = ImageLoader.getInstance().getBitmapFromMemoryCache(url);
                if(bitmap == null){
                    bitmap = ImageLoader.decodeSampledBitmapFromResource(url, BitmapTools.dp2px(getContext(), 48));
                }
                if(bitmap != null){
                    Bitmap bm =  BitmapTools.drawCircleView(bitmap, BitmapTools.dp2px(getContext(), 45), BitmapTools.dp2px(getContext(), 45));
                    showBitmap = bm.copy(Bitmap.Config.ARGB_8888, true);
                    ImageLoader.getInstance().addBitmapToMemoryCache(url+"notify", bm);
                    remoteViews.setImageViewBitmap(R.id.notify_img, showBitmap);
                }else{
                    remoteViews.setImageViewResource(R.id.notify_img, R.drawable.head_img);
                }
            }
        }

//        remoteViews.setOnClickPendingIntent(R.id.control_left, getServiceIntent(PlayerService.class, PRE_CONTROL, Notification.FLAG_AUTO_CANCEL));
//        remoteViews.setOnClickPendingIntent(R.id.control_play, getServiceIntent(PlayerService.class, PLAY_PAUSE_CONTROL, Notification.FLAG_AUTO_CANCEL));
//        remoteViews.setOnClickPendingIntent(R.id.control_pause, getServiceIntent(PlayerService.class, PLAY_PAUSE_CONTROL, Notification.FLAG_AUTO_CANCEL));
//        remoteViews.setOnClickPendingIntent(R.id.control_right, getServiceIntent(PlayerService.class, NEXT_CONTROL, Notification.FLAG_AUTO_CANCEL));
//
//        if (mMediaPlayer.isPlaying()) {
//            remoteViews.setViewVisibility(R.id.control_play, View.GONE);
//            remoteViews.setViewVisibility(R.id.control_pause, View.VISIBLE);
//        } else {
//            remoteViews.setViewVisibility(R.id.control_play, View.VISIBLE);
//            remoteViews.setViewVisibility(R.id.control_pause, View.GONE);
//        }

//        remoteViews.setTextViewText(R.id.tv_content_title, "歌曲名");
//        remoteViews.setTextViewText(R.id.tv_content_text, "歌手");
//        //打开上一首
//        remoteViews.setOnClickPendingIntent(R.id.btn_pre, getClickPendingIntent(NOTIFICATION_PRE));
//        //打开下一首
//        remoteViews.setOnClickPendingIntent(R.id.btn_next, getClickPendingIntent(NOTIFICATION_NEXT));
//        //点击整体布局时,打开播放器
//        remoteViews.setOnClickPendingIntent(R.id.ll_root, getClickPendingIntent(NOTIFICATION_OPEN));
        return remoteViews;
    }

    private PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), MainActivity.class), flags);
        return pendingIntent;
    }

    private PendingIntent getServiceIntent(Class content, int control, int flag) {
        Intent intent = new Intent(getContext(), content);
        PendingIntent pendingIntent = PendingIntent.getService(getContext(), control, intent, flag);
        return pendingIntent;
    }

    @Override
    public void showFriends(final List<FriendView> friendViewList) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFriendsAdapter.updateList(friendViewList);
            }
        });
    }

    @Override
    public void showLastChatMsgs(final List<LastChatMsgInfo> lastChatMsgInfos) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mLastChatMsgAdapter.updateList(lastChatMsgInfos);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.stop();
    }

    /*get and set*/
    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        mPresenter = presenter;
    }


    public void setDrawerUserImg(CircleImageView circleImageView) {
        this.mDrawerUserImg = circleImageView;
    }

    public void setDrawerUserName(TextView textView) {
        this.mDrawerUserName = textView;
    }
}
