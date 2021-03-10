package com.mballem.app.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class EnviarMensagem {

	private static DatagramSocket datagramSocket = null;

	public static void broadcast(String messagem, String ip) throws IOException {

		datagramSocket = new DatagramSocket();
		datagramSocket.setBroadcast(true);
		
		InetAddress ipDestinatario = InetAddress.getByName(ip);
		byte[] buffer = messagem.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, ipDestinatario, 50001);
		datagramSocket.send(datagramPacket);
		datagramSocket.close();

	}
}
