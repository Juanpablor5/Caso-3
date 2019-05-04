package carga;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Scanner;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

import logic.ClienteSeguro;

public class Generador {
	private LoadGenerator loadG;
	private Task work;
	public static int numberOfTasks;
	public static int gapBetweenGap;
	private PrintWriter writer;
	public static int nThreads;
	long[] tiempoRespuestaConsulta = new long[numberOfTasks]; // 10 =Numero de itraciones
	long[] transaccionesFallidas = new long[numberOfTasks];
	long[] tiempoVerificacion = new long[numberOfTasks];
	
	private final static String PATH = "./Data/config.prop";

	public Generador() throws FileNotFoundException, IOException {
        
		Properties prop = new Properties();
		try (FileInputStream in = new FileInputStream(PATH)) {
			prop.load(in);
		}
		
		int numThreads = Integer.parseInt(prop.getProperty("nThreads"));
		int nTask = Integer.parseInt(prop.getProperty("nTask"));
		int gap = Integer.parseInt(prop.getProperty("gap"));
		
		nThreads = numThreads;
		work = crearTask();
		numberOfTasks = nTask;
		gapBetweenGap = gap;
		try {
			writer = new PrintWriter(
					"./docs/datosTransaccionesPerdidas" + nThreads + "-" + numberOfTasks + "-" + gapBetweenGap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		loadG = new LoadGenerator("Client-server Load Test", numberOfTasks, work, gapBetweenGap);
		loadG.generate();
		writer.println("Numero de transacciones fallidas: " + ClienteSeguro.nTransaccionesPerdidas);
		writer.close();
	}

	private Task crearTask() {
		return new ClientServerTask();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		@SuppressWarnings("unused")
		Generador gen = new Generador();

	}
}
