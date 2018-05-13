package com.baige.search;

import android.util.Log;


import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.User;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.Tools;


import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/26.
 */

public class SearchPresenter implements SearchContract.Presenter {

    private Repository mRepository;
    private SearchFragment mFragment;

    public SearchPresenter(Repository instance, SearchFragment searchFragment) {
        mRepository = checkNotNull(instance);
        mFragment = checkNotNull(searchFragment);
        searchFragment.setPresenter(this);
    }



    @Override
    public void start() {

    }

    @Override
    public void search(String word) {
        //是否是电话号码
//        if(word.matches("^[0-9]+$")){
//
//            Log.d("RemoteRepository","以电话搜索");
//        }else{
//
//            Log.d("RemoteRepository","以姓名搜索");
//        }

        if(!Tools.isEmpty(word)){
            mFragment.setRefreshing(true);
            User user = CacheRepository.getInstance().who();
            mRepository.searchUserBykeyword(user.getId(), user.getVerification(), word, new HttpBaseCallback(){
                @Override
                public void loadUsers(List<User> list) {
                    super.loadUsers(list);
                    mFragment.showUsers(list);
                }

                @Override
                public void meaning(String text) {
                    super.meaning(text);
                    mFragment.showTip(text);
                }

                @Override
                public void success() {
                    super.success();
                    mFragment.showTip("搜索成功");
                }

                @Override
                public void fail() {
                    super.fail();
                    mFragment.showTip("没有结果");
                }
            });
        }
    }

    @Override
    public void relate(User user) {

    }
}
