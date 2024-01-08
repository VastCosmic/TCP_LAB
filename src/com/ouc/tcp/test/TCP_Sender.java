package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.message.TCP_PACKET;

/**
 * TCP Sender类，实现了TCP_Sender_ADT接口，用于Go-Back-N协议的可靠数据传输。
 */
public class TCP_Sender extends TCP_Sender_ADT {
	private final Object lock = new Object();  // 锁，用于同步操作
	private final SenderSlidingWindow slidingWindow = new SenderSlidingWindow(this.client);  // 发送窗口

	/* 构造函数 */
	public TCP_Sender() {
		super();    // 调用超类构造函数
		super.initTCP_Sender(this);        // 初始化TCP发送端
	}

	/**
	 * 可靠发送（应用层调用）：封装应用层数据，产生TCP数据报；
	 *
	 * @param dataIndex 序列号
	 * @param appData   应用层数据
	 */
	@Override
	public void rdt_send(int dataIndex, int[] appData) {
		tcpH.setTh_seq(dataIndex * appData.length + 1);
		tcpS.setData(appData);
		TCP_PACKET tcpPack = new TCP_PACKET(tcpH, tcpS, destinAddr);
		tcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
		tcpPack.setTcpH(tcpH);

		synchronized (lock) {
			if (slidingWindow.isFull()) {
				System.out.println("\nSliding Window is full\n");
				try {
					lock.wait(); // 等待被唤醒
				} catch (InterruptedException e) {
					System.out.println("Interrupted");
				}
			}

			try {
				slidingWindow.putPacket(tcpPack.clone());
			} catch (CloneNotSupportedException e) {
				System.out.println("Clone failed");
			}

			udt_send(tcpPack);
		}
	}

	/**
	 * 不可靠发送：将打包好的TCP数据报通过不可靠传输信道发送；
	 *
	 * @param stcpPack 打包好的TCP数据报
	 */
	@Override
	public void udt_send(TCP_PACKET stcpPack) {
		// 设置错误控制标志
		tcpH.setTh_eflag((byte) 7);
		// 发送数据报
		client.send(stcpPack);
	}

	/**
	 * 等待ACK报文
	 */
	@Override
	public void waitACK() {}

	/**
	 * 接收到ACK报文：检查校验和，将确认号插入ack队列；NACK的确认号为－1；不需要修改
	 *
	 * @param recvPack 接收到的ACK报文
	 */
	@Override
	public void recv(TCP_PACKET recvPack) {
		if (CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) {
			System.out.println("Receive ACK Number: " + recvPack.getTcpH().getTh_ack());
			slidingWindow.receiveACK((recvPack.getTcpH().getTh_ack() - 1) / 100);

			synchronized (lock) {
				if (!slidingWindow.isFull()) {
					lock.notify(); // 唤醒等待的线程
				}
			}
		}
	}
}
