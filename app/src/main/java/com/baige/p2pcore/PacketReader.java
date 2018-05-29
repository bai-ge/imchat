package com.baige.p2pcore;

import com.baige.connect.SocketPacket;

/**
 * Created by baige on 2018/5/28.
 */

public interface PacketReader {

    SocketPacket read(String uuid, long number);

    void sendSocketPacket(SocketPacket socketPacket);
}
