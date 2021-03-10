package com.mballem.app.service;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

//www.java2s.com
public class IPBroadcast {

	private InetAddress broadcast;
	private int cont = 0;

	public InetAddress IP() throws SocketException {

		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		while (en.hasMoreElements()) {
			NetworkInterface ni = en.nextElement();
			List<InterfaceAddress> list = ni.getInterfaceAddresses();
			Iterator<InterfaceAddress> it = list.iterator();
			while (it.hasNext()) {
				InterfaceAddress ia = it.next();
				if (ia.getBroadcast() != null) {
					cont++;
					if (cont == 2) {
						broadcast = ia.getBroadcast();
					}
				}
			}
		}
		return broadcast;
	}
}