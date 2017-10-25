import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.swing.*;
import javax.net.*;

public class IntegrityVerifierClient {
	private static MACCalculator macCalculator;
	//private static int seqNumber = 0;
	//private static UUID uuid = new UUID(seqNumber, seqNumber);

	// Constructor que abre una conexión Socket para enviar mensaje/MAC al servidor
	public IntegrityVerifierClient() throws InterruptedException {
		try {
			SocketFactory socketFactory = (SocketFactory) SocketFactory.getDefault();
			Socket socket = (Socket) socketFactory.createSocket("localhost", 7070);
			// Crea un PrintWriter para enviar mensaje/MAC al servidor
			PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			// Crea un objeto BufferedReader para leer la respuesta del servidor
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (true) {
				String messageToSend = JOptionPane.showInputDialog(null, "Introduzca	su	mensaje:");
				if (messageToSend.equals("exit")) {
					// Se cierra la conexion
					output.close();
					input.close();
					socket.close();
					break;
				}
				// Envío del mensaje al servidor
				//seqNumber++;
				messageToSend += "SEQNUM=" + UUID.randomUUID().toString();
				output.println(messageToSend);
				// Habría que calcular el correspondiente MAC con la clave compartida por
				// servidor/cliente
				// System.out.println("Client mac: "+macCalculator.calculate(messageToSend));
				output.println(macCalculator.calculate(messageToSend));
				// Importante para que el mensaje se envíe
				output.flush();
				// Second message
				/*output.println(messageToSend);
				output.println(macCalculator.calculate(messageToSend));
				System.out.println("Sending again");
				output.flush();*/

				// Lee la respuesta del servidor
				String respuesta = input.readLine();
				// Muestra la respuesta al cliente
				JOptionPane.showMessageDialog(null, respuesta);
			}

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
	public static void main(String args[]) throws InvalidKeyException, NoSuchAlgorithmException, InterruptedException {
		// message = args[1];
		macCalculator = new MACCalculator(args[0], args[1]);
		new IntegrityVerifierClient();
	}
}