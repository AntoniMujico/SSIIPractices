import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

import javax.net.ServerSocketFactory;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class IntegrityVerifierServer {

	private static MACCalculator macCalculator;
	private ServerSocket serverSocket;
	private static int seqNumber = 0;
	private static int IntegrityMessageCount = 0;
	private static int TotalMessageCount = 0;
	private static ArrayList<Integer> dailyIntegrity = new ArrayList<Integer>();
	private static ArrayList<Integer> dailyReceived = new ArrayList<Integer>();
	static Calendar cal = Calendar.getInstance();
	static int currentMonth = cal.get(Calendar.MONTH);
	static int currentDate = cal.get(Calendar.DATE);

	// Constructor del Servidor
	public IntegrityVerifierServer() throws Exception {
		// ServerSocketFactory para construir los ServerSockets
		ServerSocketFactory socketFactory = (ServerSocketFactory) ServerSocketFactory.getDefault();
		// Creación de un objeto ServerSocket escuchando peticiones en el puerto 7070
		serverSocket = (ServerSocket) socketFactory.createServerSocket(7070);
	}

	// Ejecución del servidor para escuchar peticiones de los clientes

	private void runServer() throws IOException {
		PrintWriter pw = new PrintWriter(System.getProperty("user.dir") + "/log.txt");
		while (true) {
			// Espera las peticiones del cliente para comprobar mensaje/MAC
			if (cal.get(Calendar.DATE) != currentDate) {
				currentDate = cal.get(Calendar.DATE);
				dailyIntegrity.add(IntegrityMessageCount);
				IntegrityMessageCount = 0;
				dailyReceived.add(TotalMessageCount);
				TotalMessageCount = 0;
			}
			if (cal.get(Calendar.MONTH) != currentMonth) {
				currentMonth = cal.get(Calendar.MONTH);
				generateMonthlyReport();
				dailyIntegrity.clear();
				dailyReceived.clear();
			}
			try {
				System.err.println("Esperando	conexiones	de	clientes...");
				Socket socket = (Socket) serverSocket.accept();
				// Abre un BufferedReader para leer los datos del cliente
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// Abre un PrintWriter para enviar datos al cliente
				PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
				// Se lee del cliente el mensaje y el macdelMensajeEnviado
				String mensaje = input.readLine();

				int seqIndx = mensaje.indexOf("SEQNUM=") + 7;
				int seqNum = Integer.parseInt(mensaje.substring(seqIndx));
				seqNumber++;
				// A continuación habría que calcular el mac del MensajeEnviado que podría ser
				String macMensajeEnviado = input.readLine();
				// System.out.println("message Enviado: "+mensaje);
				// System.out.println("Own calculated mac: "+macCalculator.calculate(mensaje));
				// mac del MensajeCalculado
				TotalMessageCount++;
				if (seqNum == seqNumber) {
					if (macMensajeEnviado.equals(macCalculator.calculate(mensaje))) {
						output.println("Mensaje	enviado	integro	");
						IntegrityMessageCount++;
					} else {
						output.println("Mensaje	enviado	no	integro.");
						pw.write("Mensaje enviado no integro. Recibido MAC=" + macMensajeEnviado + " pero calculado"
								+ macCalculator.calculate(mensaje));
					}
				} else {
					output.println("Invalid Sequence Number");
					pw.write("Numero de secuencia no valido. Esperado: " + seqNumber + " pero recibido: " + seqNum);
				}

				output.close();
				input.close();
				socket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
				pw.close();
			}
		}

	}

	private void generateMonthlyReport() throws IOException {
		DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
		for (int i = 0; i < dailyIntegrity.size(); i++) {
			line_chart_dataset.addValue(dailyIntegrity.get(i) / dailyReceived.get(i), "Ratio", "" + i);
		}

		JFreeChart lineChartObject = ChartFactory.createLineChart("Summary ", "Day", "Integrity Ratio",
				line_chart_dataset, PlotOrientation.VERTICAL, true, true, false);

		int width = 640;
		int height = 480;
		File lineChart = new File(System.getProperty("user.dir") + "/report/Monthly Integrity Chart "
				+ cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.YEAR) + ".jpeg");
		ChartUtilities.saveChartAsJPEG(lineChart, lineChartObject, width, height);

	}

	// Programa principal
	public static void main(String args[]) throws Exception {
		macCalculator = new MACCalculator(args[0], args[1]);
		IntegrityVerifierServer server = new IntegrityVerifierServer();
		server.runServer();
	}
}