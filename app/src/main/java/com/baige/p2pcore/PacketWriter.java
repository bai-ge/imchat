package com.baige.p2pcore;

import com.baige.connect.SocketPacket;

/**
 * Created by baige on 2018/5/28.
 */

public interface PacketWriter {

    boolean write(SocketPacket packet);

    void responeAffirm(String uuid, long number);

    void askPacket(String uuid, long number);

    void error();

    void finish();
}
