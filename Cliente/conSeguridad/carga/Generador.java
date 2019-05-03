package carga;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

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

	public Generador() {
		nThreads = 1;
		work = crearTask();
		numberOfTasks = 400; // 400,20,80
		gapBetweenGap = 20;// 20,40,100
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

	public static void main(String[] args) {

		@SuppressWarnings("unused")
		Generador gen = new Generador();

	}
}
