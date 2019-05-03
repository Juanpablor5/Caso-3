package carga;

import uniandes.gload.core.Task;
import logic.ClienteSeguro;

public class ClientServerTask extends Task {

	@Override
	public void fail() {
		// TODO Auto-generated method stub
		System.out.println(Task.MENSAJE_FAIL);
	}

	@Override
	public void success() {
		// TODO Auto-generated method stub
		System.out.println(Task.OK_MESSAGE);
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub

		try {
			@SuppressWarnings("unused")
			ClienteSeguro cliente = new ClienteSeguro();

		} catch (Exception e) {
			System.out.println("For if the flies");
		}
	}

}
