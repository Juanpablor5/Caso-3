package carga;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class GeneradorInseguro {
	
private LoadGenerator loadG;
	
	public GeneradorInseguro(){
		Task work=crearTask();
		int numberOfTasks=80;
		int gapBetweenGap=1000;
		
		loadG=new LoadGenerator("Client-server Load Test",numberOfTasks,work,gapBetweenGap);
		loadG.generate();
	}

	private Task crearTask() {
		return new ClienteServerTaskInseguro();
	}
	
	public static void main(String[] args){
		@SuppressWarnings("unused")
		Generador gen=new Generador();
	}

}
