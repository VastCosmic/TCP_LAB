package com.ouc.tcp.test;

import java.util.zip.CRC32;

import com.ouc.tcp.message.TCP_HEADER;
import com.ouc.tcp.message.TCP_PACKET;

public class CheckSum {
	
	/*计算TCP报文段校验和：只需校验TCP首部中的seq、ack和sum，以及TCP数据字段*/
	public static short computeChkSum(TCP_PACKET tcpPack) {
		CRC32 crc32 = new CRC32();

		// 获取TCP报文头
		TCP_HEADER tcpHead = tcpPack.getTcpH();

		// 使用crc对seq和ack进行校验
		crc32.update(tcpHead.getTh_seq());
		crc32.update(tcpHead.getTh_ack());

		// TCP报文段数据校验
		if (tcpPack.getTcpH().getTh_flags() == 1) {
			// 如果是SYN报文段，不需要校验数据
			crc32.update(0);
		} else {
			// 如果是数据报文段，需要校验数据
			for(int i = 0; i < tcpPack.getTcpS().getData().length; i++) {
				crc32.update(tcpPack.getTcpS().getData()[i]);
			}
		}

		// 返回校验和
		return (short) crc32.getValue();
	}
}
