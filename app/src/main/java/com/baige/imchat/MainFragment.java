package com.baige.imchat;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.baige.adapter.LastChatMsgAdapter;
import com.baige.adapter.UserAdapter;
import com.baige.chat.ChatActivity;
import com.baige.data.entity.LastChatMsgInfo;
import com.baige.data.entity.User;
import com.baige.data.source.cache.CacheRepository;
import com.baige.filelist.FileListActivity;
import com.baige.login.LoginActivity;
import com.baige.util.GlideImageLoader;
import com.baige.util.ImageLoader;
import com.baige.util.Tools;
import com.baige.view.CircleImageView;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baige on 2018/5/4.
 */

public class MainFragment extends Fragment implements MainContract.View, BottomNavigationBar.OnTabSelectedListener {

    private final static String TAG = MainFragment.class.getSimpleName();

    private Toast mToast;

    private Handler mHandler;

    private MainContract.Presenter mPresenter;

    private CircleImageView mHeadImg;

    private Button mBtnMenu;

    private ViewPager mViewPager;

    private BottomNavigationBar mBottomNavigationBar;

    private PagerAdapter mPagerAdapter;

    private List<View> mViewList = new ArrayList<>();

    //组件
    /*消息*/
    private ListView mLastChatListView;

    private ViewGroup mLastChatNothingView;

    private LastChatMsgAdapter mLastChatMsgAdapter;

    /*好友*/
    private ListView mFriendsListView;

    private ViewGroup mFriendsNothingView;

    private UserAdapter mFriendsAdapter;

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
        mFriendsAdapter = new UserAdapter(new ArrayList<User>(), mOnUserItemListener);
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

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        mPresenter = presenter;
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
        }else{
            mPresenter.start();
        }
    }


    private void initView(View root) {
        mHeadImg = root.findViewById(R.id.user_img);
        mBtnMenu = root.findViewById(R.id.title_menu);
        mViewPager = root.findViewById(R.id.view_pager);
        mBottomNavigationBar = root.findViewById(R.id.bottom_navigation_bar);

//        mBottomNavigationBar.setMode(BottomNavigationBar.MODE_SHIFTING);
        mBottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        mBottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        mBottomNavigationBar.setBarBackgroundColor(R.color.white);
        mBottomNavigationBar.setInActiveColor(R.color.dark_gray);
        mBottomNavigationBar.setActiveColor(R.color.blue);


        BadgeItem badgeItem = new BadgeItem();
        badgeItem.setHideOnSelect(false)
                .setText("10")
                .setBackgroundColorResource(R.color.orange)
                .setBorderWidth(0);

        mBottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.ic_msg, R.string.tab_message).setBadgeItem(badgeItem))
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
                } else {
                    mLastChatNothingView.setVisibility(View.INVISIBLE);
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
        mFriendsAdapter.addItem(new User("Ant", "feigj"));
        mFriendsNothingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFriendsAdapter.addItem(new User("百戈", "feg"));
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
                if(mEditAlias.isEnabled()){
                    mEditAlias.requestFocus();
                    showInputMethod(getContext(), mEditAlias);
                }else{
                    mPresenter.updateAlias(mEditAlias.getText().toString());
                }
            }
        });
        mEditAlias.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "alias beforeTextChanged()"+charSequence.toString());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "alias onTextChanged()"+charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "alias afterTextChanged()"+editable);
                //CacheRepository.getInstance().who().setAlias(mEditAlias.getText().toString());
            }
        });
        mEditAlias.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d(TAG, "alias OnFocus()"+b);
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
            Log.d(TAG, "img  =" + images.get(0).toString());
            Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(images.get(0).path, size);
            if (bitmap != null) {
                showUserImg(bitmap);
            }
            mPresenter.upload(images.get(0).path);
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

    private UserAdapter.OnUserItemListener mOnUserItemListener = new UserAdapter.OnUserItemListener() {
        @Override
        public void onClickItem(User item) {

        }

        @Override
        public void onLongClickItem(User item) {
            mFriendsAdapter.clear();
        }
    };

    private LastChatMsgAdapter.OnLastChatMsgItemListener mOnLastChatMsgItemListener = new LastChatMsgAdapter.OnLastChatMsgItemListener() {
        @Override
        public void onClickItem(LastChatMsgInfo item) {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
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
    public void showUserImg(String imgName) {
//        Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(imgName, )

    }

    @Override
    public void showUserImg(final Bitmap img) {
        if (img != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCircleImageView != null) {
                        mCircleImageView.setImageBitmap(img);
                    }
                    if (mHeadImg != null) {
                        mHeadImg.setImageBitmap(img);
                    }
                }
            });
        }

    }

    @Override
    public void showUserName(final String name) {
        showTip("name"+name);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Tools.isEmpty(name)) {
                    mTxtUserName.setText("");
                } else {
                    mTxtUserName.setText(name);
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

    /**
     * 显示键盘
     * @param context
     * @param view
     */
    public static void showInputMethod(Context context, View view) {
        InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.showSoftInput(view, 0);
    }
    //隐藏虚拟键盘
    public static void HideKeyboard(View v){
        InputMethodManager imm = ( InputMethodManager) v.getContext( ).getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow( v.getApplicationWindowToken() , 0 );
        }
    }
}
