package com.mballem.app.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.mballem.app.Cliente;
import com.mballem.app.frame.ClienteFrame;

public class ServidorConectar {

	private static DatagramSocket datagramSocket;
	private static ArrayList<String> ips;
	private static ArrayList<String> nomes;
	private static ClienteFrame clienteFrame;

	public ServidorConectar(ClienteFrame clienteFrame) {
		this.clienteFrame = clienteFrame;
		try {

			datagramSocket = new DatagramSocket(50001);

			// Inicio da Thread que vai ficar rodando o servidor que receberá as mensagens

			// Servidor de inicio que receberá o ip e nome de quem está online e ip de quem
			// sair
			while (true) {

				// O nome da pessoa que está online ou mensagem de saida ficara armazenada aqui
				byte[] by = new byte[10000];

				// Nesse objeto ficará contido o ip de quem enviar o nome ou msg de saida
				DatagramPacket datagramPacket = new DatagramPacket(by, by.length);

				datagramSocket.receive(datagramPacket);

				String nome = new String(by, 0, by.length);
				String ip = datagramPacket.getAddress().toString();
				String meuIp = null;
				try {
					meuIp = InetAddress.getLocalHost().getHostAddress().toString();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				String ipCorreto = ip.substring(1, ip.length());
				String sair = nome.substring(0, 12);
				System.out.println(nome);
				System.out.println(ipCorreto);
				// Se a pessoa saiu deve ser removido dos contatos onlines
				if (sair.equals("////SAIR////")) {
					for (int i = 0; i < getIps().size(); i++) {
						if (getIps().get(i).equals(ipCorreto)) {
							if (getIps().get(clienteFrame.seguraPosicao).equals(ipCorreto)) {
								clienteFrame.seguraPosicao = -1;
							}
							clienteFrame.removerJlist(ipCorreto);
						}
					}
				}
				// Verifica se o ip ja existe e se o ip é diferente do meu
				else if (!getIps().contains(ipCorreto) && !ipCorreto.equals(meuIp)) {
					nome.replace("-", "");
					getNomes().add(nome);
					getIps().add(ipCorreto);
					clienteFrame.AddNome(getNomes());
					clienteFrame.AddIp(getIps());
				}
				for (int i = 0; i < getNomes().size(); i++) {
					if (!getNomes().get(i).equals(nome) && !sair.equals("////SAIR////")) {
//						System.out.println(nome);
						nome = nome.replace("%¨6(#@", "");
						String[] anderson = new String[3];
						anderson = nome.split("-");
						if (anderson.length > 1) {
							if (anderson[1].toString().contains("¨¨")) {
								clienteFrame.setarMensagem(anderson[1].replace("¨¨", ""), ipCorreto);
							}
						} else {
							clienteFrame.setarMensagem(nome, ipCorreto);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getNomes() {
		if (nomes == null) {
			nomes = new ArrayList<String>();
		}
		return nomes;
	}

	public static ArrayList<String> getIps() {
		if (ips == null) {
			ips = new ArrayList<String>();
		}
		return ips;
	}
}
