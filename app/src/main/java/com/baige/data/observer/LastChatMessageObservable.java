package com.baige.data.observer;


import com.baige.common.State;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.LastChatMsgInfo;
import com.baige.data.source.cache.CacheRepository;


import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by baige on 2018/5/15.
 */

public class LastChatMessageObservable extends BaseObservable<LastChatMsgInfo> {

    @Override
    public LastChatMsgInfo put(LastChatMsgInfo item) {
        if (item != null) {
            synchronized (this) {
                getCacheMap().put(item.getUid(), item);
            }
            setChanged();
            notifyObservers(item);
        }
        return item;
    }

    @Override
    public List<LastChatMsgInfo> put(List<LastChatMsgInfo> items) {
        if (items != null && items.size() > 0) {
            synchronized (this) {
                for (int i = 0; i < items.size(); i++) {
                    getCacheMap().put(items.get(i).getUid(), items.get(i));
                }
            }
            setChanged();
            notifyObservers(items);
        }
        return items;
    }

    private void initCount() {
        Iterator<Map.Entry<Object, LastChatMsgInfo>> iterator = getCacheMap().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, LastChatMsgInfo> entity = iterator.next();
            entity.getValue().setMsgCount(0);
        }

    }

    public void analyze(List<ChatMsgInfo> chatMsgInfos, int uid) {
        int friendId = 0;
        FriendView friendView;
        LastChatMsgInfo lastChatMsgInfo = null;
        int realState = 0;
        initCount();
        for (ChatMsgInfo chat : chatMsgInfos) {
            if (chat.getSenderId() == uid) {
                friendId = chat.getReceiveId();
            }
            if (chat.getReceiveId() == uid) {
                friendId = chat.getSenderId();
            }
            friendView = CacheRepository.getInstance().getFriendViewObservable().get(friendId);
            if(friendView.isFriend()){
                lastChatMsgInfo = get(friendId);

                if (lastChatMsgInfo == null ) {
                    lastChatMsgInfo = new LastChatMsgInfo();
                    lastChatMsgInfo.setUid(friendId);
                    if (friendView != null) {
                        lastChatMsgInfo.setName(friendView.getFriendName());
                        lastChatMsgInfo.setAlias(friendView.getAlias());
                        lastChatMsgInfo.setFriendAlias(friendView.getFriendAlias());
                        lastChatMsgInfo.setImagName(friendView.getFriendImgName());
                    }
                }
                if (lastChatMsgInfo.getLastTime() == 0 || lastChatMsgInfo.getLastTime() < chat.getSendTime()) {
                    lastChatMsgInfo.setLastMessage(chat.getContext());
                    lastChatMsgInfo.setMsgType(chat.getContextType());
                    lastChatMsgInfo.setLastTime(chat.getSendTime());
                }
                if (chat.isReceive() && chat.getContextState() != null && chat.getContextState() == State.MSG_STATE_UNREAD) {
                    lastChatMsgInfo.setMsgCount(lastChatMsgInfo.getMsgCount() + 1);
                }
                getCacheMap().put(lastChatMsgInfo.getUid(), lastChatMsgInfo);
            }else{
                getCacheMap().remove(friendId);
            }
            //TODO 未读信息条数统计
        }
        setChanged();
        notifyObservers();
    }

    public void analyze(List<FriendView> friendViews) {
        int friendId = 0;
        for (FriendView f : friendViews) {
            friendId = f.getFriendId();
            LastChatMsgInfo l = get(friendId);
            if (l != null) {
                l.setUid(f.getFriendId());
                l.setName(f.getFriendName());
                l.setAlias(f.getAlias());
                l.setFriendAlias(f.getFriendAlias());
                l.setImagName(f.getFriendImgName());
            }
        }
        setChanged();
        notifyObservers();
    }
}
