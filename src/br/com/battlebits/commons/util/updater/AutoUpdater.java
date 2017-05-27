package br.com.battlebits.commons.util.updater;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.plugin.Plugin;

public class AutoUpdater {

	private boolean running;
	private Plugin plugin;
	private String pluginName;
	private String versaoAtual;
	private String pluginPwd = "";
	protected boolean needUpdate = true;

	private boolean downloaded = false;

	private boolean failed = false;

	public AutoUpdater(Plugin plugin, String password) {
		this(plugin, plugin.getName(), plugin.getDescription().getVersion(), password);
	}

	public AutoUpdater(Plugin plugin, String pluginName, String actualVersion, String password) {
		this.plugin = plugin;
		this.pluginName = pluginName;
		versaoAtual = actualVersion;
		this.pluginPwd = password;
	}

	public boolean run() {
		if (!needUpdate)
			return false;
		if (running)
			return false;
		running = true;
		try {
			download();
			if (failed) {
				throw new Exception("Falhou");
			}
			return downloaded;
		} catch (Exception e) {
			System.out.println("============================");
			System.out.println("Erro ao procurar atualização de " + pluginName);
			System.out.println("============================");
			running = false;
			return false;
		}
	}

	private void download() {
		Socket socket = null;

		OutputStream output = null;
		InputStream input = null;

		DataInputStream dataInput = null;
		DataOutputStream dataOutput = null;

		FileInputStream fileInput = null;
		BufferedInputStream bufferedInput = null;
		try {
			socket = new Socket("update.battlebits.net", 63973);
			socket.setSoTimeout(10000);
			output = socket.getOutputStream();
			input = socket.getInputStream();

			dataInput = new DataInputStream(input);
			dataOutput = new DataOutputStream(output);

			dataOutput.writeUTF(pluginName);
			dataOutput.flush();

			String answer = dataInput.readUTF();
			if (!answer.equals("OK")) {
				throw new FailedException("Usuario nao e valido");
			}

			dataOutput.writeUTF(DigestUtils.sha384Hex(pluginPwd));
			dataOutput.flush();

			answer = dataInput.readUTF();
			if (!answer.equals("OK")) {
				throw new FailedException("Senha nao e valida");
			}

			dataOutput.writeUTF(versaoAtual);
			dataOutput.flush();

			answer = dataInput.readUTF();
			if (answer.equals("UPDATED")) {
				throw new UpdatedException("Plugin " + pluginName + " ja esta atualizado");
			}

			File to = new File(plugin.getServer().getUpdateFolderFile(), pluginName + ".jar");
			File tmp = new File(to.getPath() + ".au");
			if (!tmp.exists()) {
				plugin.getServer().getUpdateFolderFile().mkdirs();
				tmp.createNewFile();
			}

			OutputStream os = new FileOutputStream(tmp);
			byte[] buffer = new byte[4096];
			int fetched;
			while ((fetched = input.read(buffer)) != -1)
				os.write(buffer, 0, fetched);
			os.flush();
			os.close();

			if (to.exists())
				to.delete();

			tmp.renameTo(to);
			downloaded = true;
			throw new UpdatedException("Atualizacao de " + pluginName + " baixada com sucesso!");
		} catch (IOException e) {
			failed = true;
			e.printStackTrace();
		} catch (UpdatedException e) {
			System.out.println("============================");
			System.out.println(e.getMessage());
			System.out.println("============================");
		} catch (FailedException e) {
			System.out.println("============================");
			System.out.println(e.getMessage());
			System.out.println("============================");
		} finally {
			try {
				if (socket != null)
					socket.close();
				if (fileInput != null)
					fileInput.close();
				if (bufferedInput != null)
					bufferedInput.close();
				if (output != null)
					output.close();
				if (input != null)
					input.close();
				if (dataInput != null)
					dataInput.close();
				if (dataOutput != null)
					dataOutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
