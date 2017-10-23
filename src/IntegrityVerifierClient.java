import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.swing.*;
import javax.net.*;

public class IntegrityVerifierClient {
	private static MACCalculator macCalculator;

	// Constructor que abre una conexión Socket para enviar mensaje/MAC al servidor
	public IntegrityVerifierClient() {
		try {
			SocketFactory socketFactory = (SocketFactory) SocketFactory.getDefault();
			Socket socket = (Socket) socketFactory.createSocket("localhost", 7070);
			// Crea un PrintWriter para enviar mensaje/MAC al servidor
			PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			String messageToSend = JOptionPane.showInputDialog(null, "Introduzca	su	mensaje:");
			// Envío del mensaje al servidor
			output.println(messageToSend);
			// Habría que calcular el correspondiente MAC con la clave compartida por
			// servidor/cliente
			//System.out.println("Client mac: "+macCalculator.calculate(messageToSend));
			output.println(macCalculator.calculate(messageToSend));
			// Importante para que el mensaje se envíe
			output.flush();

			// Crea un objeto BufferedReader para leer la respuesta del servidor
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// Lee la respuesta del servidor
			String respuesta = input.readLine();
			// Muestra la respuesta al cliente
			JOptionPane.showMessageDialog(null, respuesta);

			// Se cierra la conexion
			output.close();
			input.close();
			socket.close();
		} // end try
		catch (IOException ioException) {
			ioException.printStackTrace();
		}

		// Salida de la aplicacion
		finally {
			System.exit(0);
		}
	}

	// ejecución del cliente de verificación de la integridad
	public static void main(String args[]) throws InvalidKeyException, NoSuchAlgorithmException {
		//message = args[1];
		macCalculator = new MACCalculator(args[0],args[1]);
		new IntegrityVerifierClient();
	}
}