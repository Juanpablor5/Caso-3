package logic;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.x509.X509V3CertificateGenerator;

import logic.ClienteSeguro;

@SuppressWarnings("deprecation")
public class Seguridad {

	private String padding;
	private String aSimetrico;
	private SecretKey key;
	private String aAsimetrico;
	private String aHMAC;
	private KeyPair pair;
	private X509Certificate certificado;

	public String getAlgSimetrico() {
		return aSimetrico;
	}

	public void setAlgSimetrico(String algSimetrico) {
		this.aSimetrico = algSimetrico;
	}

	public SecretKey getLlave() {
		return key;
	}

	public void setLlave(SecretKey llave) {
		this.key = llave;
	}

	public String getAlgAsimetrico() {
		return aAsimetrico;
	}

	public void setAlgAsimetrico(String algAsimetrico) {
		this.aAsimetrico = algAsimetrico;
	}

	public void setCertificado(X509Certificate certificado) {
		this.certificado = certificado;
	}

	public String darAlgoritmos() {
		return ":" + aSimetrico + ":" + aAsimetrico + ":" + aHMAC;
	}

	public void SetAlgoritmos(String[] algs) {
		aSimetrico = algs[0];
		aAsimetrico = algs[1];
		aHMAC = algs[2];
	}

	public void setLlaveSimetrica(byte[] valor) throws Exception {

		byte[] x = new byte[16];
		x = valor;
		key = new SecretKeySpec(x, aSimetrico);
	}

	public void setLlaveAsimetrica() throws Exception {
		KeyPairGenerator generador = KeyPairGenerator.getInstance(aAsimetrico);
		SecureRandom ran = SecureRandom.getInstance("SHA1PRNG");
		generador.initialize(1024, ran);
		pair = generador.generateKeyPair();
	}

	public byte[] cifrarSimetrica(byte[] arg) throws Exception {
		byte[] cipheredText;
		String PADDING = "";
		if (aSimetrico.equals(ClienteSeguro.ALGS_SIMETRICOS[0])) {
			System.out.println("Algoritmo: " + ClienteSeguro.ALGS_SIMETRICOS[0]);
		}
		PADDING = "AES/ECB/PKCS5Padding";
		Cipher cipher = Cipher.getInstance(PADDING);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		cipheredText = cipher.doFinal(arg);
		return cipheredText;
	}

	public byte[] cifrarAsimetrica(String arg) throws Exception {

		Cipher cipher = Cipher.getInstance(aAsimetrico);
		byte[] byts = arg.getBytes();
		String txt = new String(byts);
		System.out.println("la clave original es: " + txt);
		cipher.init(Cipher.ENCRYPT_MODE, certificado.getPublicKey());
		byte[] bytsCifra = cipher.doFinal(byts);
		System.out.println("clave Cifrada " + bytsCifra);
		return bytsCifra;
	}

	public String decifrarSimetricamente(byte[] arg) throws Exception {
		padding = "AES/ECB/PKCS5Padding";
		Cipher cipher = Cipher.getInstance(padding);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cifrado = cipher.doFinal(arg);
		String resultado = new String(cifrado);
		return resultado;
	}

	public String decifrarAsimetricamente(byte[] arg) throws Exception {
		Cipher ci = Cipher.getInstance(aAsimetrico);
		ci.init(Cipher.DECRYPT_MODE, pair.getPrivate());
		byte[] txtOriginal = ci.doFinal(arg);
		String originalString = new String(txtOriginal);
		return originalString;

	}

	public X509Certificate crearCertificado() throws Exception {
		Date fechaInicio = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date fechaFin = calendar.getTime();
		BigInteger numeroSerie = new BigInteger("" + Math.abs(SecureRandom.getInstance("SHA1PRNG").nextLong()));
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		X500Principal dn = new X500Principal("CN=Test CA Certificate");
		certGen.setSerialNumber(numeroSerie);
		certGen.setIssuerDN(dn);
		certGen.setNotBefore(fechaInicio);
		certGen.setNotAfter(fechaFin);
		certGen.setSubjectDN(dn);
		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA1withRSA");
		return certGen.generate(pair.getPrivate());
	}

	public byte[] cifrarB(byte[] msjRecibido) {
		byte[] cipheredText;
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

			byte[] clearText = msjRecibido;
			String s1 = new String(clearText);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			long startTime = System.nanoTime();
			cipheredText = cipher.doFinal(clearText);
			long endTime = System.nanoTime();
			String s2 = new String(cipheredText);
			return cipheredText;
		} catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
			return null;
		}
	}

	public byte[] getKeyDigest(byte[] buffer) throws Exception {
		Mac mac = Mac.getInstance(aHMAC);
		mac.init(key);
		byte[] bytes = mac.doFinal(buffer);
		return bytes;
	}

}
