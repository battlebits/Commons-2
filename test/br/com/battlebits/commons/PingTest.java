package br.com.battlebits.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PingTest {

	public static void main(String[] args) {
		PingTest test = new PingTest();
		int i = 1;
		while (i < 256) {
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
						socket = new Socket("187.108.195." + k, 80);
						socket.setTcpNoDelay(true);
						socket.setSoTimeout(1000);


						System.out.println(socket.getInetAddress().getHostAddress() + " / ");

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
