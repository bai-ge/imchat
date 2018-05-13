package com.baige.search;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.baige.adapter.UserAdapter;
import com.baige.data.entity.User;
import com.baige.imchat.R;
import com.baige.util.Tools;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by baige on 2017/12/26.
 */

public class SearchFragment extends Fragment implements SearchContract.View {

    private SearchContract.Presenter mPresenter;

    private Handler mHandler;

    private Toast mToast;

    private EditText mEtSearchWord;

    private Button mBtnSearch;

    private ListView mUsersListView;

    private ViewGroup mFriendsNothingView;

    private UserAdapter mUserAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mUserAdapter = new UserAdapter(new ArrayList<User>(0), mUserItemListener);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_search, container, false);
        initView(root);
        return root;
    }
    private void initView(View root) {
        mEtSearchWord = (EditText) root.findViewById(R.id.et_find_word);
        mBtnSearch = (Button) root.findViewById(R.id.btn_search);
        mUsersListView = (ListView) root.findViewById(R.id.list_view);
        mUsersListView.setAdapter(mUserAdapter);
        mFriendsNothingView = root.findViewById(R.id.layout_null);

        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = mEtSearchWord.getText().toString();
                if(!Tools.isEmpty(word)){
                    mPresenter.search(word);
                    showTip("搜索"+word);
                }
            }
        });
        mUserAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if(mUserAdapter.getCount() > 0){
                    mFriendsNothingView.setVisibility(View.INVISIBLE);
                }else{
                    mFriendsNothingView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void setPresenter(SearchContract.Presenter presenter) {
        mPresenter = presenter;
    }

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
    public void showUsers(final List<User> users) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mUserAdapter.updateList(users);
            }
        });
    }

    private UserAdapter.OnUserItemListener mUserItemListener = new UserAdapter.OnUserItemListener() {
        @Override
        public void onClickItem(User user) {
            showTip("点击"+user.getName());
            mPresenter.relate(user);
        }

        @Override
        public void onLongClickItem(User item) {

        }
    };

    @Override
    public void setRefreshing(boolean refresh) {

    }
}
