package logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import org.bouncycastle.util.encoders.Hex;

import seguridad.Seguridad;

public class ClienteInseguro {
	
	public static final String HOLA = "HOLA";
	public static final String OK = "OK";
	public static final String AlGORITMOS = "ALGORITMOS";
	public static final String ERROR = "ERROR";
	public static final String SEPARADOR = ":";
	public static final int PUERTO = 8080;
	private Socket socketCliente;
	private Scanner sc;
	private BufferedReader reader;
	private PrintWriter writer;
	private Seguridad seguridad;

	public static long timeVertificacion;
	public static long timeRespuesta;
	public static long startTime; 
	public static long endTime; 
	public static long nTransaccionesAtendidas;
	public static long nTransaccionesPerdidas;
	public static long cpu;

	public ClienteInseguro(){

		try{

			System.out.println("----------------Caso 2 - Infraestructura Computacional----------------");
			sc = new Scanner(System.in);
			//Inicializar el servidor en el puerto 8080
			seguridad = new Seguridad();
			socketCliente = new Socket("localhost",PUERTO);
			socketCliente.setKeepAlive(true);
			writer = new PrintWriter(socketCliente.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

			procesar();
			//System.out.println("Tiempo verificacion en millis: " + timeVerifi);
			//System.out.println("Tiempo respuesta consulta en millis: " + timeRes);
		}catch (Exception e) {
			nTransaccionesPerdidas++;
			//System.out.println("Cantidad de perdidas actual: "+numPerdidas);
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			reader.close();
			socketCliente.close();
			writer.close();
			sc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String convertByteArrayHexa(byte[] byteArray) {
		String out = "";
		for (int i = 0; i < byteArray.length; i++) {
			if ((byteArray[i] & 0xff) <= 0xf) {
				out += "0";
			}
			out += Integer.toHexString(byteArray[i] & 0xff).toUpperCase();
		}

		return out;
	}


	public void procesar() throws Exception {
		boolean termino = false;
		boolean esperando = true;
	     startTime=0;
		 endTime=0;
		int estado = 0;
		String respuesta = "";
		String comando = "";
		//Se escribe HOLA
		System.out.println("Cliente se comunica con servidor");
		String hola = "HOLA";
		writer.println(hola);
		while (!termino) {
			if (reader.ready()) {
				esperando = true;
				comando = reader.readLine();
				if (comando == null || comando.equals(""))
					continue;
				else if (comando.toLowerCase().contains(ERROR.toLowerCase()) && estado != 5) throw new Exception(comando);
				else if (comando.toLowerCase().contains(OK.toLowerCase())) System.out.println("Servidor: " + comando);

				switch (estado) {
				case 0:
					if (comando.equals(OK)) {
						System.out.println("INICIANDO");
						String algos = "AES,RSA,HMACMD5";
						String[] algoritmos = algos.split(","); 
						seguridad.SetAlgoritmos(algoritmos);
						respuesta = AlGORITMOS+ seguridad.darAlgoritmos();
						writer.println(respuesta);
						estado++;
					}
					break;
				case 1:
					if (comando.equals(OK)) {
						System.out.println("Se intercambiará el Certificado Digital");
						seguridad.setLlaveAsimetrica();
						java.security.cert.X509Certificate certi = seguridad.crearCertificado();
						byte[] bytesCertiPem = certi.getEncoded();
						String certiString = new String(Hex.toHexString(bytesCertiPem));
						String certiFinal = certiString;
						writer.println(certiFinal);
						estado ++;
					}
					break;
				case 2:
					if (!comando.equals(OK)) {
						System.out.println("Certificado Digital del Servidor");

						System.out.println("Procesando certificado...");
						writer.println(OK);
						startTime = System.currentTimeMillis();
						estado ++;
					}
					break;
				case 3:
					System.out.println("Llave secreta recibida");
					System.out.println("Enviando llave secreta...");
					writer.println(comando);
					estado ++;

					break;
				case 4:
					if (comando.equals(OK)) {
						endTime= System.currentTimeMillis();
						timeVertificacion = endTime - startTime;
						startTime=0;
						endTime=0;
						System.out.println("tiempo de vertificacion (milliseconds): "+timeVertificacion);
						String id = "1";
						writer.println(id);
						startTime = System.currentTimeMillis();
						writer.println(id);
						estado ++;
					}

					break;

				case 5:
					endTime = System.currentTimeMillis();
					timeRespuesta= endTime-startTime;
					System.out.println("Tiempo de respuesta (milliseconds): "+ timeRespuesta);
					if (comando.contains(OK)) {
						System.out.println("Estado: "+comando.split(":")[1]);
						imprimirResultados();
						System.out.println("terminó");
					}
					else {
						System.out.println("Hubo un error al realizar la consulta: " + comando);
					}
					termino = true;
					break;

				default:
					estado = 0;
					break;
				}
			} else {
				if (esperando) {
					esperando = false;
				}
			}
		}

	}
	private void imprimirResultados() {
		try{
			File tiempos = new File("./docs/datos"+ Generador.nThreads  + "-" + Generador.numberOfTasks + "-" + Generador.gapBetweenGap);
			PrintWriter writer = new PrintWriter(new FileWriter(tiempos,true));
			writer.println("tiempoRespuestaConsulta:"+ timeRespuesta +"-tiempoVerificacion:"+ timeVertificacion+"-CPU:"+cpu);
			writer.println();
			writer.close();
		}catch (Exception e){
			e.printStackTrace();
		}		
	}


}
