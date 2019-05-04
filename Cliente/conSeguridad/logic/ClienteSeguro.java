package logic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.cert.CertificateFactory;
import java.util.Properties;
import java.util.Scanner;

import java.security.cert.X509Certificate;
import org.bouncycastle.util.encoders.Hex;
import logic.Seguridad;

import carga.Generador;

public class ClienteSeguro {
	// ------------------------------------------------------------
	// -----------------------Constantes---------------------------
	// ------------------------------------------------------------
	public static final String HOLA = "HOLA";
	public static final String OK = "OK";
	public static final String AlGORITMOS = "ALGORITMOS";
	public static final String ERROR = "ERROR";
	public static final String SEPARADOR = ":";
	public static final String[] ALGS_SIMETRICOS = { "AES", "Blowfish" };
	public static final String[] ALGS_ASIMETRICOS = { "RSA" };
	public static final String[] ALGS_HMAC = { "HMACMD5", "HMACSHA1", "HMACSHA256" };

	private final static String PATH = "./Data/config.prop";

	// ------------------------------------------------------------
	// ------------------------Atributos---------------------------
	// ------------------------------------------------------------

	private Socket socketCliente;
	private Scanner sc;
	private BufferedReader reader;
	private PrintWriter writer;
	private Seguridad seguridad;

	// ------------------------------------------------------------
	// ------------------------New---------------------------------
	// ------------------------------------------------------------

	public static long timeVertificacion;
	public static long timeRespuesta;
	public static long startTime;
	public static long endTime;
	public static long nTransaccionesAtendidas;
	public static long nTransaccionesPerdidas;
	public static long cpu;
	
	// ------------------------------------------------------------
	// ----------------------Constructor---------------------------
	// ------------------------------------------------------------

	public ClienteSeguro() {
		try {

			System.out.println("----------------Caso 3 - Infraestructura Computacional----------------");
			sc = new Scanner(System.in);

			Properties prop = new Properties();
			try (FileInputStream in = new FileInputStream(PATH)) {
				prop.load(in);
			}

			String host = prop.getProperty("host");
			int puerto = Integer.parseInt(prop.getProperty("puerto"));

			seguridad = new Seguridad();
			socketCliente = new Socket(host, puerto);
			socketCliente.setKeepAlive(true);
			writer = new PrintWriter(socketCliente.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

			procesar();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			nTransaccionesPerdidas++;
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
		int estado = 0;
		String respuesta = "";
		String comando = "";
		boolean responde = false;
		byte[] cifra;
		// Se escribe HOLA
		System.out.println("Cliente se comunica con servidor");
		startTime = 0;
		endTime = 0;
		String hola = "HOLA";
		writer.println(hola);
		while (!termino) {
			if (reader.ready()) {
				esperando = true;
				comando = reader.readLine();
				if (comando == null || comando.equals(""))
					continue;
				else if (comando.toLowerCase().contains(ERROR.toLowerCase()) && estado != 5)
					throw new Exception(comando);
				else if (comando.toLowerCase().contains(OK.toLowerCase()))
					System.out.println("Servidor: " + comando);

				switch (estado) {
				case 0:
					if (comando.equals(OK)) {
						System.out.println("INICIANDO");
						System.out.println("escribe 3 algoritmos. s"
								+ "e colocan ordenados por 'simetrico','asimetrico','HMAC' y separados por comas (e.g AES,RSA,HMACMD5) ");
						// Los algoritmos se colocan ordenados por 'simetrico','asimetrico','HMAC' y
						// separados por comas (e.g AES,RSA,HMACMD5)
						String algos = "AES,RSA,HMACMD5";
						String[] algoritmos = algos.split(",");
						seguridad.SetAlgoritmos(algoritmos);
						respuesta = AlGORITMOS + seguridad.darAlgoritmos();
						writer.println(respuesta);
						estado++;

					} else {
						nTransaccionesPerdidas++;
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

						estado++;
					} else if (comando.equals(ERROR)) {
						System.out.println(ERROR);
						nTransaccionesPerdidas++;
						esperando = false;
					}
					break;
				case 2:
					if (!comando.equals(OK)) {

						System.out.println("Certificado Digital del Servidor");
						System.out.println(comando);
						startTime = System.currentTimeMillis();
						CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
						InputStream in = new ByteArrayInputStream(Hex.decode(comando));
						X509Certificate certiServi = (X509Certificate) certFactory.generateCertificate(in);
						seguridad.setCertificado(certiServi);

						System.out.println("CLIENTE: OK");
						writer.println(OK);

						estado++;
					}
					break;
				case 3:

					cifra = Hex.decode(comando);
					System.out.println(cifra.length);
					String valor = seguridad.decifrarAsimetricamente(cifra);
					seguridad.setLlaveSimetrica(valor.getBytes());
					cifra = seguridad.cifrarAsimetrica(valor);
					cifra = Hex.encode(cifra);
					writer.println(new String(cifra));

					estado++;

					break;
				case 4:
					if (comando.equals(OK)) {
						endTime = System.currentTimeMillis();
						timeVertificacion = endTime - startTime;
						System.out.println("Tiempo de vertifiacion (milliseconds)= " + timeVertificacion);
						System.out.println("Ingrese la consulta");
						startTime = 0;
						endTime = 0;
						startTime = System.currentTimeMillis();
						String id = "1018499615";
						System.out.println(id);
						cifra = seguridad.cifrarSimetrica(id.getBytes());
						cifra = Hex.encode(cifra);
						writer.println(new String(cifra));

						cifra = seguridad.getKeyDigest((id.getBytes()));
						cifra = Hex.encode(cifra);
						writer.println(new String(cifra));
						System.out.println("Consulta HMAC");
						estado++;

					}

					break;

				case 5:
					if (comando.contains(OK)) {
						System.out.println("Servidor: " + comando.split(":")[1]);
						cpu = (long) Double.parseDouble(comando.split(":")[3]);
						endTime = System.currentTimeMillis();
						timeRespuesta = (endTime - startTime);
						System.out.println("Tiempo de respuesta a consulta (milliseconds)= " + timeRespuesta);
						termino = true;
						imprimirResultados();
					} else {
						System.out.println("Hubo un error al realizar la consulta: " + comando);
						break;
					}
					break;
				default:
					estado = 0;
					break;
				}

			} else {
				if (esperando) {
					System.out.println("waiting...");
					esperando = false;
				}
			}
		}

	}

	private void imprimirResultados() {
		try {
			File tiempos = new File("./docs/datos" + Generador.nThreads + "-" + Generador.numberOfTasks + "-"
					+ Generador.gapBetweenGap);
			PrintWriter writer = new PrintWriter(new FileWriter(tiempos, true));
			writer.println(timeRespuesta + " " + timeVertificacion + " " + cpu);
			writer.println();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new ClienteSeguro();
	}

}