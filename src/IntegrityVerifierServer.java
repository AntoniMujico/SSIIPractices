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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ServerSocketFactory;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class IntegrityVerifierServer {

	private static MACCalculator macCalculator;
	private ServerSocket serverSocket;
	private static double IntegrityMessageCount = 0;
	private static double TotalMessageCount = 0;
	private static ArrayList<Double> dailyIntegrity = new ArrayList<Double>();
	private static ArrayList<Double> dailyReceived = new ArrayList<Double>();
	static Calendar cal = Calendar.getInstance();
	static int currentMonth = cal.get(Calendar.MONTH);
	static int currentDate = cal.get(Calendar.DATE);
	static boolean clientAccepted = false;
	static ArrayList<String> seqNumberStore = new ArrayList<String>();
	//static int count = 0; // DEBUG

	// Constructor del Servidor
	public IntegrityVerifierServer() throws Exception {
		// ServerSocketFactory para construir los ServerSockets
		ServerSocketFactory socketFactory = (ServerSocketFactory) ServerSocketFactory.getDefault();
		// Creación de un objeto ServerSocket escuchando peticiones en el puerto 7070
		serverSocket = (ServerSocket) socketFactory.createServerSocket(7070);
	}

	private void runServer() throws IOException, InterruptedException {
		while (true) {
			System.err.println("Esperando	conexiones	de	clientes...");
			Socket socket = serverSocket.accept();
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			PrintWriter pw = new PrintWriter(System.getProperty("user.dir") + "/log.txt");
			while (true) {
				try {
					String mensaje = input.readLine();
					String macMensajeEnviado = input.readLine();
					int seqIndx = mensaje.indexOf("SEQNUM=") + 7;
					String seqNum = mensaje.substring(seqIndx);
					TotalMessageCount++;
					if (!seqNumberStore.contains(seqNum)) {
						seqNumberStore.add(seqNum);
						if (macMensajeEnviado.equals(macCalculator.calculate(mensaje))) {
							output.println("Mensaje	enviado	integro	");
							IntegrityMessageCount++;
						} else {
							output.println("Mensaje	enviado	no	integro.");
							pw.write("Mensaje enviado no integro. Recibido MAC=" + macMensajeEnviado + " pero calculado"
									+ macCalculator.calculate(mensaje) + "\n");
						}
					} else {
						output.println("Invalid Sequence Number");
						pw.write("Numero de secuencia no valido. Recibido: " + seqNum + "\n");
					}
					output.flush();
					Thread.sleep(1);
				} catch (Exception e) {
					input.close();
					output.close();
					socket.close();
					pw.close();
					break;
				}
			}
			Thread.sleep(1);

		}

	}

	public static void generateMonthlyReport() throws IOException {
		DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
		for (int i = 0; i < dailyIntegrity.size(); i++) {
			System.out.println("add Value " + dailyIntegrity.get(i) + " " + dailyReceived.get(i));
			if (dailyReceived.get(i) == 0.0) {
				line_chart_dataset.addValue(1, "Ratio", "" + i);
			} else {
				line_chart_dataset.addValue(dailyIntegrity.get(i) / dailyReceived.get(i), "Ratio", "" + i);
			}
		}

		JFreeChart lineChartObject = ChartFactory.createLineChart("Summary ", "Day", "Integrity Ratio",
				line_chart_dataset, PlotOrientation.VERTICAL, true, true, false);

		int width = 640;
		int height = 480;
		File lineChart = new File(System.getProperty("user.dir") + "/Monthly Integrity Chart" + cal.get(Calendar.MONTH)
				+ "-" + cal.get(Calendar.YEAR) + ".jpeg");
		ChartUtilities.saveChartAsJPEG(lineChart, lineChartObject, width, height);

	}

	public static void collectDailyData() {
		dailyIntegrity.add(IntegrityMessageCount);
		IntegrityMessageCount = 0.0;
		dailyReceived.add(TotalMessageCount);
		TotalMessageCount = 0.0;
		//count++;//DEBUG!
		//if (count == 5) { //DEBUG!
		  if (cal.get(Calendar.MONTH) != currentMonth) {
			currentMonth = cal.get(Calendar.MONTH);
			try {
				generateMonthlyReport();
			} catch (IOException e) {
				e.printStackTrace();
			}
			dailyIntegrity.clear();
			dailyReceived.clear();
		}

	}

	// Programa principal
	public static void main(String args[]) throws Exception {
		macCalculator = new MACCalculator(args[0], args[1]);
		IntegrityVerifierServer server = new IntegrityVerifierServer();
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
		scheduler.scheduleAtFixedRate(() -> {
			IntegrityVerifierServer.collectDailyData();
		}, 0, 5, TimeUnit.SECONDS);
		server.runServer();

	}
}