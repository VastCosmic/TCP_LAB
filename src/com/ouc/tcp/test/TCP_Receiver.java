package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Receiver_ADT;
import com.ouc.tcp.message.TCP_PACKET;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TCP_Receiver extends TCP_Receiver_ADT {
	int expSequence = 0;  //记录期望收到的序号

	/*构造函数*/
	public TCP_Receiver() {
		super();    //调用超类构造函数
		super.initTCP_Receiver(this);    //初始化TCP接收端
	}

	@Override
	//接收到数据报：检查校验和，设置回复的ACK报文段
	public void rdt_recv(TCP_PACKET recvPack) {
		//检查校验码，生成ACK
		if (CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) {
			// 当前包的seq
			int currentSequence = (recvPack.getTcpH().getTh_seq() - 1) / 100;
            //回复的ACK报文段
            TCP_PACKET ackPack;
            if (expSequence == currentSequence) {  // 当前收到的包的序号是期望的序号
				//生成ACK报文段，ack为收到的TCP分组的seq
				tcpH.setTh_ack(recvPack.getTcpH().getTh_seq());
				ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
				tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));

				reply(ackPack);

				dataQueue.add(recvPack.getTcpS().getData());
				expSequence += 1;  // 更新期望收到的包的seq
			} else {
				tcpH.setTh_ack((expSequence - 1) * 100 + 1);  // 设置确认号为已确认的最大序号
				ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
				tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));

				reply(ackPack);
			}
		}

		System.out.println();


		//交付数据（每20组数据交付一次）
		if (dataQueue.size() == 20)
			deliver_data();
	}

	@Override
	//交付数据（将数据写入文件）；不需要修改
	public void deliver_data() {
		//检查dataQueue，将数据写入文件
		File fw = new File("recvData.txt");
		BufferedWriter writer;

		try {
			writer = new BufferedWriter(new FileWriter(fw, true));

			//循环检查data队列中是否有新交付数据
			while (!dataQueue.isEmpty()) {
				int[] data = dataQueue.poll();

				//将数据写入文件
                for (int datum : data) {
                    writer.write(datum + "\n");
                }

				writer.flush();        //清空输出缓存
			}
			writer.close();
		} catch (IOException e) {
			System.out.println("Error at deliver_data(): " + e);
		}
	}

	@Override
	//回复ACK报文段
	public void reply(TCP_PACKET replyPack) {
		//设置错误控制标志
		tcpH.setTh_eflag((byte) 7);
		//发送数据报
		client.send(replyPack);
	}
}