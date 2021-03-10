
package com.mballem.app.frame;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import com.mballem.app.service.EnviarMensagem;
import com.mballem.app.service.EnviarNome;
import com.mballem.app.service.IPBroadcast;
import com.mballem.app.service.ServidorConectar;

public class ClienteFrame extends javax.swing.JFrame {

	private JButton jButtonConnectar;
	private JButton jButtonEnviar;
	private JButton jButtonLimpar;
	private JButton jButtonSair;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JPanel jPanel3;
	private JPanel jPanel;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JScrollPane jScrollPane3;
	private JList jListOnlines;
	private JTextArea jTextAreaReceive;
	private JTextArea jTextAreaSend;
	private JTextField jTextFieldName;
	private ArrayList<String> nomes;
	private ArrayList<String> ips;
	private static IPBroadcast IP;
	private Thread threadAtualizar, threadConectar;
	private EnviarNome enviarNome;
	private EnviarMensagem enviarMensagem;
	public int seguraPosicao = -1;
	private String seguraIp = "";
	private Thread threadInicio;
	private ServidorConectar servidorConectar;

	public ClienteFrame() {
		seguraPosicao = -1;
		setSize(650, 590);
		setVisible(true);
		// Não fazer nada ao clicar na opção fechar(X)
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setContentPane(getjPanel());
		repaint();
		validate();

		// Adiciona ação na tela caso clique na opção fechar(X)
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					// Enviar a mensagem de saida para que os outros saibam que saí
					getEnviarNome().broadcast("////SAIR////", InetAddress.getByName(getIPBroadcast().IP().toString()
							.substring(1, (getIPBroadcast().IP().toString()).length())));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// Fechamento da aplicação
				System.exit(0);
			}
		});
	}

	private void connected() {
		this.jButtonConnectar.setEnabled(false);
		this.jTextFieldName.setEditable(false);
		this.jButtonSair.setEnabled(true);
		this.jTextAreaSend.setEnabled(true);
		this.jTextAreaReceive.setEnabled(true);
		this.jButtonEnviar.setEnabled(true);
		this.jButtonLimpar.setEnabled(true);
	}

	private void disconnected() {
		this.jButtonConnectar.setEnabled(true);
		this.jTextFieldName.setEditable(true);
		this.jButtonSair.setEnabled(false);
		this.jTextAreaSend.setEnabled(false);
		this.jTextAreaReceive.setEnabled(false);
		this.jButtonEnviar.setEnabled(false);
		this.jButtonLimpar.setEnabled(false);
		this.jTextAreaReceive.setText("");
		this.jTextAreaSend.setText("");
		JOptionPane.showMessageDialog(this, "Você saiu do chat!");
	}

	// Atualiza quem está online
	private void refreshOnlines() {
		while (true) {
			String meuIp = null;
			try {
				meuIp = InetAddress.getLocalHost().getHostAddress().toString();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			meuIp.replace("/", "");
			for (int i = 0; i < getIps().size(); i++) {
				if (getIps().get(i).equals(meuIp)) {
					getIps().remove(i);
					getNomes().remove(i);
					break;
				}
			}
			if (getNomes().size() != 0) {
				getjListOnlines().setListData(getNomes().toArray());
				getjListOnlines().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				getjListOnlines().setLayoutOrientation(JList.VERTICAL);
				if (seguraPosicao != -1) {
					try {
						System.out.println(jListOnlines.getModel().getSize());
						if (getIps().get(seguraPosicao).equals(seguraIp) && jListOnlines.getModel().getSize() > 0) {
							jListOnlines.setSelectedIndex(seguraPosicao);
						} else {
							seguraPosicao = -1;
							seguraIp = "";
						}
					} catch (Exception e) {
						seguraPosicao = -1;
						seguraIp = "";
					}
				}
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void jButtonConnectarActionPerformed(java.awt.event.ActionEvent evt) {
		String name = this.jTextFieldName.getText();

		// Se nome não estiver vazio
		if (!name.isEmpty()) {
			// Se as threads não tiver em execução elas serão executadas
			if (!getThreadConectar().isAlive()) {
				getThreadConectar().start();
			}
			if (!getThreadAtualizar().isAlive()) {
				getThreadAtualizar().start();
			}
			if (!getThreadInicio().isAlive()) {
				getThreadInicio().start();
			}
			connected();
			JOptionPane.showMessageDialog(this, "Você está conectado no chat!");
		}
	}

	private void jButtonSairActionPerformed(java.awt.event.ActionEvent evt) {
		// Envia para os outros online que vc saiu
		try {
			getEnviarNome().broadcast("////SAIR////", InetAddress.getByName(
					getIPBroadcast().IP().toString().substring(1, (getIPBroadcast().IP().toString()).length())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		getThreadAtualizar().stop();
		getThreadConectar().stop();
		threadAtualizar = null;
		threadConectar = null;
		disconnected();
	}

	private void jButtonLimparActionPerformed(java.awt.event.ActionEvent evt) {
		this.jTextAreaSend.setText("");
	}

	private void jButtonEnviarActionPerformed(java.awt.event.ActionEvent evt) {
		String text = this.jTextAreaSend.getText();
		// Envia a mensagem
		if (seguraPosicao > -1 && !text.isEmpty() && jListOnlines.getModel().getSize() > 0) {
			try {
				getEnviarMensagem().broadcast("%¨6(#@" + getjTextAreaSend().getText(), getIps().get(seguraPosicao));
			} catch (IOException e) {
				e.printStackTrace();
			}
//			getjListOnlines().clearSelection();
			this.jTextAreaReceive.append("Você disse para " + getNomes().get(seguraPosicao) + " : " + text + "\n");
			this.jTextAreaSend.setText("");
		}
//		seguraPosicao = -1;
	}

	public JButton getjButtonConnectar() {
		if (jButtonConnectar == null) {
			jButtonConnectar = new JButton("Conectar");
			jButtonConnectar.setBounds(223, 19, 89, 36);
			jButtonConnectar.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButtonConnectarActionPerformed(evt);
				}
			});
		}
		return jButtonConnectar;
	}

	public JButton getjButtonEnviar() {
		if (jButtonEnviar == null) {
			jButtonEnviar = new JButton("Enviar");
			jButtonEnviar.setText("Enviar");
			jButtonEnviar.setEnabled(false);
			jButtonEnviar.setBounds(329, 382, 101, 46);
			jButtonEnviar.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButtonEnviarActionPerformed(evt);
				}
			});

		}
		return jButtonEnviar;
	}

	public JButton getjButtonLimpar() {
		if (jButtonLimpar == null) {
			jButtonLimpar = new JButton("Limpar");
			jButtonLimpar.setBounds(218, 382, 101, 46);
			jButtonLimpar.setEnabled(false);
			jButtonLimpar.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButtonLimparActionPerformed(evt);
				}
			});
		}
		return jButtonLimpar;
	}

	public JButton getjButtonSair() {
		if (jButtonSair == null) {
			jButtonSair = new JButton("Sair");
			jButtonSair.setBounds(321, 19, 89, 36);
			jButtonSair.setEnabled(false);
			jButtonSair.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButtonSairActionPerformed(evt);
				}
			});
		}
		return jButtonSair;
	}

	public JPanel getjPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setBackground(Color.LIGHT_GRAY);
			jPanel1.setLayout(null);
			jPanel1.setBorder(BorderFactory.createTitledBorder("Conectar"));
			jPanel1.add(getjButtonConnectar());
			jPanel1.add(getjButtonSair());
			jPanel1.add(getjTextFieldName());
			jPanel1.setBounds(10, 23, 420, 66);
		}
		return jPanel1;
	}

	public JPanel getjPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setBackground(Color.LIGHT_GRAY);
			jPanel.setSize(636, 552);
			jPanel.setLayout(null);
			jPanel.add(getjPanel1());
			jPanel.add(getjPanel2());
			jPanel.add(getjPanel3());
		}
		return jPanel;
	}

	public JPanel getjPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setBackground(Color.LIGHT_GRAY);
			jPanel2.setBounds(440, 23, 190, 518);
			jPanel2.setLayout(null);
			jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Onlines"));
			jPanel2.add(getjScrollPane3());
		}
		return jPanel2;
	}

	public JPanel getjPanel3() {
		if (jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.setLayout(null);
			jPanel3.setSize(440, 450);
			jPanel3.setLocation(0, 91);
			jPanel3.setBackground(Color.LIGHT_GRAY);
			jPanel3.setLayout(null);
			jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
			jPanel3.add(getjScrollPane1());
			jPanel3.add(getjScrollPane2());
			jPanel3.add(getjButtonLimpar());
			jPanel3.add(getjButtonEnviar());
		}
		return jPanel3;
	}

	public JScrollPane getjScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getjTextAreaReceive());
			jScrollPane1.setBounds(10, 11, 420, 273);
		}
		return jScrollPane1;
	}

	public JScrollPane getjScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getjTextAreaSend());
			jScrollPane2.setBounds(10, 295, 420, 76);
		}
		return jScrollPane2;
	}

	public JScrollPane getjScrollPane3() {
		if (jScrollPane3 == null) {
			jScrollPane3 = new JScrollPane();
			jScrollPane3.setViewportView(getjListOnlines());
			jScrollPane3.setBounds(10, 26, 170, 422);
		}
		return jScrollPane3;
	}

	public JList getjListOnlines() {
		if (jListOnlines == null) {
			jListOnlines = new JList();
			jListOnlines.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					seguraPosicao = jListOnlines.getSelectedIndex();
					seguraIp = getIps().get(seguraPosicao);
				}
			});
			jListOnlines.setBounds(10, 26, 170, 422);
		}
		return jListOnlines;
	}

	public JTextArea getjTextAreaReceive() {
		if (jTextAreaReceive == null) {
			jTextAreaReceive = new JTextArea();
			jTextAreaReceive.setBounds(10, 11, 420, 273);
			jTextAreaReceive.setEditable(false);
			jTextAreaReceive.setColumns(20);
			jTextAreaReceive.setRows(5);
			jTextAreaReceive.setEnabled(false);
		}
		return jTextAreaReceive;
	}

	public JTextArea getjTextAreaSend() {
		if (jTextAreaSend == null) {
			jTextAreaSend = new JTextArea();
			jTextAreaSend.setBounds(10, 295, 420, 76);
			jTextAreaSend.setColumns(20);
			jTextAreaSend.setRows(5);
			jTextAreaSend.setEnabled(false);
		}
		return jTextAreaSend;
	}

	public JTextField getjTextFieldName() {
		if (jTextFieldName == null) {
			jTextFieldName = new JTextField();
			jTextFieldName.setBounds(10, 19, 203, 36);
		}
		return jTextFieldName;
	}

	public ArrayList<String> getNomes() {
		if (nomes == null) {
			nomes = new ArrayList<String>();
		}
		return nomes;
	}

	public ArrayList<String> getIps() {
		if (ips == null) {
			ips = new ArrayList<String>();
		}
		return ips;
	}

	public EnviarNome getEnviarNome() {
		if (enviarNome == null) {
			enviarNome = new EnviarNome();
		}
		return enviarNome;
	}

	public EnviarMensagem getEnviarMensagem() {
		if (enviarMensagem == null) {
			enviarMensagem = new EnviarMensagem();
		}
		return enviarMensagem;
	}

	public static IPBroadcast getIPBroadcast() {

		if (IP == null) {
			IP = new IPBroadcast();
		}

		return IP;
	}

	public void AddNome(ArrayList<String> nomes) {
		this.nomes = nomes;
	}

	public void AddIp(ArrayList<String> ips) {
		this.ips = ips;
	}

	public Thread getThreadAtualizar() {
		if (threadAtualizar == null) {
			threadAtualizar = new Thread(new Runnable() {

				@Override
				public void run() {
					refreshOnlines();
				}
			});
		}
		return threadAtualizar;
	}

	public Thread getThreadInicio() {
		if (threadInicio == null) {
			threadInicio = new Thread(new Runnable() {
				@Override
				public void run() {
					getServidorConectar();
				}
			});
		}
		return threadInicio;
	}

	private ServidorConectar getServidorConectar() {
		if (servidorConectar == null) {
			servidorConectar = new ServidorConectar(this);
		}
		return servidorConectar;
	}

	public Thread getThreadConectar() {
		if (threadConectar == null) {
			threadConectar = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					while (true) {
						try {
							getEnviarNome().broadcast(jTextFieldName.getText(), InetAddress.getByName(getIPBroadcast()
									.IP().toString().substring(1, (getIPBroadcast().IP().toString()).length())));
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SocketException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
		}
		return threadConectar;
	}

	public void removerJlist(String ip) {
		for (int i = 0; i < getIps().size(); i++) {
			if (getIps().get(i).equals(ip)) {
				getIps().remove(i);
				getNomes().remove(i);
				this.jListOnlines.setListData(getNomes().toArray());
				this.jListOnlines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				this.jListOnlines.setLayoutOrientation(JList.VERTICAL);
			}
		}
	}

	public void setarMensagem(String mensagem, String ip) {
		for (int i = 0; i < getIps().size(); i++) {
			if (getIps().get(i).equals(ip)) {
				this.jTextAreaReceive.append(getNomes().get(i) + " Disse : " + mensagem + "\n");
			}
		}
	}
}
