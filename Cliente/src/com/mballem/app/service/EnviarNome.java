package com.mballem.app.service;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class EnviarNome {

	private static DatagramSocket datagramSocket = null;

	public static void broadcast(String nome, InetAddress ipBroadcast) throws IOException {

		datagramSocket = new DatagramSocket();
		datagramSocket.setBroadcast(true);
		
		byte[] buffer = nome.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, ipBroadcast, 50001);
		datagramSocket.send(datagramPacket);
		datagramSocket.close();
	}
}
