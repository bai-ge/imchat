package com.baige.chat;

import android.graphics.Bitmap;


import com.baige.BasePresenter;
import com.baige.BaseView;

/**
 * Created by 百戈 on 2017/2/19.
 */

public interface ChatContract {

    interface Presenter extends BasePresenter {

    }

    interface View extends BaseView<Presenter> {

    }

}
