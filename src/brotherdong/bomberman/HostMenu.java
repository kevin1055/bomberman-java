package brotherdong.bomberman;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public class HostMenu implements Screen {

	private HostPanel panel;
	private ClientTableModel tableModel;
	private Thread accepterThread;
	private Thread broadcasterThread;
	private final Object updateWaiter;
	private ArrayList<ConnectedClient> clients;

	private String name;
	private int slotsFilled, slotsTotal;
	private Map map;

	public HostMenu(String serverName) {
		this.name = serverName;
		slotsFilled = 1;
		//TODO
		slotsTotal = 10;

		panel = new HostPanel();
		accepterThread = new ConnectionAccepter();
		broadcasterThread = new Broadcaster();
		updateWaiter = new Object();
		clients = new ArrayList<ConnectedClient>();
	}

//	public HostMenu(List<Socket> sockets) {
//	}

	@Override
	public void init() {
		accepterThread.start();
		broadcasterThread.start();
		panel.init();
	}

	@Override
	public void cleanup() {
		accepterThread.interrupt();
		broadcasterThread.interrupt();
		for (ConnectedClient c : clients) {
			c.interrupt();
		}
		panel.cleanup();
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	private class HostPanel extends MenuPanel {
		private JScrollPane scroll;
		private JTable table;
		private JPanel tablePanel;

		public HostPanel() {
			addComponentListener(new PanelHandler());
			setLayout(null);

			table = new JTable(tableModel);
			table.setShowVerticalLines(false);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.addMouseListener(new TableHandler());
			scroll = new JScrollPane(table);
			scroll.getViewport().setBackground(new Color(1f, 1f, 1f, 0.5f));
			scroll.setOpaque(false);
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			tablePanel = new JPanel();
			tablePanel.setOpaque(false);
			tablePanel.setLayout(new BorderLayout());
			tablePanel.add(scroll, BorderLayout.CENTER);
			tablePanel.add(Box.createHorizontalStrut(50), BorderLayout.WEST);
			tablePanel.add(Box.createHorizontalStrut(200), BorderLayout.EAST);
			tablePanel.add(Box.createVerticalStrut(50), BorderLayout.NORTH);
			tablePanel.add(Box.createVerticalStrut(50), BorderLayout.SOUTH);
			this.add(tablePanel);
			tablePanel.setBounds(0, 0, getWidth(), getHeight());
		}

		private class PanelHandler extends ComponentAdapter {
			@Override
			public void componentResized(ComponentEvent e) {
				tablePanel.setBounds(0, 0, getWidth(), getHeight());
			}
		}

		private class TableHandler extends MouseAdapter {
			@Override
			public void mouseClicked(MouseEvent e) {
				//TODO kick players
			}
		}
	}

	private class ClientTableModel extends AbstractTableModel {
		private ArrayList<ConnectedClient> clients = new ArrayList<ConnectedClient>();

		public synchronized void addClient(ConnectedClient client) {
			clients.add(client);
			//Update table
			fireTableRowsInserted(clients.size()-1, clients.size()-1);
		}

		public synchronized void clearClients() {
			clients.clear();
			fireTableDataChanged();
		}

		@Override
		public synchronized int getRowCount() {
			return clients.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			return column == 1 ? "Name" : "";
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return String.class;
			//TODO add icons
			//return column == 0 ? Icon.class : String.class;
		}

		@Override
		public synchronized Object getValueAt(int rowIndex, int columnIndex) {
			//TODO add icons
			return columnIndex == 0 ? "" : clients.get(rowIndex).name;
		}
	}

	//Periodically sends out a packet with this server's info.
	private class Broadcaster extends Thread {
		@Override
		public void run() {
			MulticastSocket multicast = null;
			try {
				multicast = new MulticastSocket(MULTICAST_PORT);

				String info = SERVER_NAME + name
					+ SERVER_HOST + getProfile().getName()
					+ SERVER_MAP + map
					+ SERVER_SLOTS + slotsFilled + "/" + slotsTotal;
				byte[] infoBytes = info.getBytes();
				DatagramPacket msg = new DatagramPacket(
					infoBytes, infoBytes.length, GROUP, MULTICAST_PORT);

				LoopDelay loop = new LoopDelay(BROADCAST_DELAY); //1000ms
				for(;;) {
					multicast.send(msg);
					loop.await();
				}
			} catch (IOException e) {
				//TODO handle exception
				e.printStackTrace();
			} catch (InterruptedException e) {
				//TODO end gracefully
				e.printStackTrace();
			} finally {
				if (multicast != null)
					multicast.close();
			}
		}
	}

	private class ConnectionAccepter extends Thread {
		@Override
		public void run() {
			ServerSocket socket = null;
			try {
				socket = new ServerSocket(GAME_PORT);
				for(;;) {
					ConnectedClient ch = new ConnectedClient(socket.accept());
					clients.add(ch);
					ch.start();
					synchronized(updateWaiter) {
						updateWaiter.notifyAll();
					}
					if (Thread.interrupted())
						break;
				}
			} catch (IOException e) {
				//TODO handle
			} finally {
				if (socket != null)
					try {socket.close();} catch (IOException e) {}
			}
		}
	}

	private class ConnectedClient extends Thread {
		private Socket socket;
		private String name;
		private DataInputStream in;
		private DataOutputStream out;

		public ConnectedClient(Socket socket) throws IOException {
			this.socket = socket;
		}

		public String getClientName() {
			return name;
		}

		@Override
		public void run() {
			try {
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());

				byte[] b = new byte[in.readInt()];
				in.read(b);
				name = new String(b).trim();

				for(;;) {
					synchronized(updateWaiter) {
						updateWaiter.wait();
					}
					out.writeByte(REMOVE);
					for (int i = 0; i < clients.size(); i++) {
						String data = clients.get(i).getClientName();
						out.writeByte(ADD);
						out.writeInt(data.length());
						out.writeBytes(data);
					}
				}
			} catch (IOException e) {

			} catch (InterruptedException e) {

			}
		}

		public void sendStartGame() throws IOException {
			out.writeByte(START_GAME);
		}
	}

	//TODO remove
	public static void main(String[] args) {
		initFrame();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setScreen(new HostMenu("Awesome Server"));
				setFrameVisible(true);
			}
		});
	}
}
