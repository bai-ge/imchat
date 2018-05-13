package com.baige.search;



import com.baige.BasePresenter;
import com.baige.BaseView;
import com.baige.data.entity.User;

import java.util.List;

/**
 * Created by baige on 2017/12/26.
 */

public interface SearchContract {
    interface Presenter extends BasePresenter {
        void search(String word);
        void relate(User user);

    }
    interface View extends BaseView<Presenter> {
        void showTip(String text);
        void showUsers(List<User> users);
        void setRefreshing(boolean refresh);
    }
}
