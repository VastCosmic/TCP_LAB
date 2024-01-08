package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.TimerTask;

/**
 * 重传任务类，继承自Java的TimerTask类，用于定时重传窗口内的TCP数据包。
 */
public class RetransmitTask extends TimerTask {
    private final Client senderClient;  // 客户端对象
    private final TCP_PACKET[] packets;  // 窗口内包的数组

    /**
     * 构造函数，初始化重传任务对象。
     *
     * @param client  客户端对象
     * @param packets 窗口内包的数组
     */
    public RetransmitTask(Client client, TCP_PACKET[] packets) {
        super();
        this.senderClient = client;
        this.packets = packets;
    }

    /**
     * 定时任务执行的方法，用于重传窗口内的TCP数据包。
     */
    @Override
    public void run() {
        for (TCP_PACKET packet : packets) {
            if (packet == null) {  // 如果没有包，break
                break;
            } else {  // 递交各个包
                senderClient.send(packet);
            }
        }
    }
}
