package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

/**
 * 窗口类，用于实现滑动窗口机制。
 */
public class Window {
    public Client client;  // 客户端
    public int size = 32;  // 窗口大小
    public TCP_PACKET[] packets = new TCP_PACKET[size];  // 存储窗口内的包
    public int base = 0;  // 窗口左指针
    public int nextIndex = 0;  // 下一个包的指针

    /**
     * 构造函数，初始化窗口对象。
     *
     * @param client 客户端对象
     */
    public Window(Client client) {
        this.client = client;
    }

    /**
     * 判断窗口是否满。
     *
     * @return 如果窗口已满返回true，否则返回false
     */
    public boolean isFull() {
        return size <= nextIndex;
    }
}
