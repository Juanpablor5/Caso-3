package carga;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class GeneradorInseguro {

	private LoadGenerator loadG;
	private final static String PATH = "./Data/config.prop";

	public GeneradorInseguro() throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		try (FileInputStream in = new FileInputStream(PATH)) {
			prop.load(in);
		}

		int nTask = Integer.parseInt(prop.getProperty("nTask"));
		int gap = Integer.parseInt(prop.getProperty("gap"));

		Task work = crearTask();
		int numberOfTasks = nTask;
		int gapBetweenGap = gap;

		loadG = new LoadGenerator("Client-server Load Test", numberOfTasks, work, gapBetweenGap);
		loadG.generate();
	}

	private Task crearTask() {
		return new ClienteServerTaskInseguro();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		@SuppressWarnings("unused")
		Generador gen = new Generador();
	}

}
