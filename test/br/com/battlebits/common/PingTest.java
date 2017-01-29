package br.com.battlebits.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PingTest {

	public static void main(String[] args) {
		PingTest test = new PingTest();
		int i = 1000;
		while (i < 65535) {
			if (test.getValue() > 1000) {
				try {
					Thread.sleep(100L);

				} catch (Exception e) {
					e.printStackTrace();
				}
				continue;
			}
			final int k = i;
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					test.increment();
					Socket socket = null;

					try {
						socket = new Socket("164.132.205.101", k);
						socket.setTcpNoDelay(true);
						socket.setSoTimeout(1000);

						DataOutputStream out = new DataOutputStream(socket.getOutputStream());
						DataInputStream in = new DataInputStream(socket.getInputStream());

						out.write(0xFE);

						int b;
						StringBuffer str = new StringBuffer();
						while ((b = in.read()) != -1) {
							if (b != 0 && b > 16 && b != 255 && b != 23 && b != 24) {
								str.append((char) b);
							}
						}

						String[] data = str.toString().split("§");
						String serverMotd = data[0];
						int onlinePlayers = Integer.parseInt(data[1]);
						int maxPlayers = Integer.parseInt(data[2]);

						System.out.println(serverMotd + " / " + onlinePlayers + " / " + maxPlayers + " / " + k);

					} catch (Exception e) {
					}

					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					test.decrement();
				}
			});

			thread.start();
			i++;
		}

	}

	private volatile int counter;

	public synchronized void increment() {
		counter++;
	}

	public synchronized void decrement() {
		counter--;
	}

	public int getValue() {
		return counter;
	}
}
