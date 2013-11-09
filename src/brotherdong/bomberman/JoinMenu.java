package brotherdong.bomberman;

import java.awt.*;
import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.*;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public class JoinMenu implements Screen {

	private JoinPanel panel;
	private ServerTableModel tableModel;
	private Thread browserThread;
	private Timer prunerTimer;

	public JoinMenu() {
		tableModel = new ServerTableModel();
		browserThread = new BrowserThread();
		panel = new JoinPanel();
		prunerTimer = new ServerPruner();
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public void init() {
		browserThread.start();
		prunerTimer.start();
		panel.init();
	}

	@Override
	public void cleanup() {
		prunerTimer.stop();
		browserThread.interrupt();
	}

	private class JoinPanel extends MenuPanel {
		private JScrollPane scroll;
		private JTable table;
		private JPanel tablePanel;

		public JoinPanel() {
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
			tablePanel.add(Box.createHorizontalStrut(50), BorderLayout.EAST);
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
				if (e.getClickCount() == 2) {
					//TODO join server
					JoinableServer server = tableModel.getServer(table.getSelectedRow());
					server.join();
				}
			}
		}
	}

	private class ServerTableModel extends AbstractTableModel {

		private ArrayList<JoinableServer> servers = new ArrayList<JoinableServer>();

		public synchronized void addServer(JoinableServer server) {
			//Remove lingering server signatures from the same IP address
			for (int i = 0; i < servers.size(); i++) {
				if (server.ip.equals(servers.get(i).ip)) {
					servers.remove(i);
				}
			}
			servers.add(server);
			//Update table
			fireTableRowsInserted(servers.size()-1, servers.size()-1);
		}

		public synchronized void clearServers() {
			servers.clear();
			fireTableDataChanged();
		}

		//Removes servers that have not broadcast themselves for a while (2s)
		public synchronized void pruneServers() {
			long time = System.currentTimeMillis();
			for (int i = 0; i < servers.size(); i++) {
				if (time - servers.get(i).lastUpdate > BROADCAST_DELAY*2) {
					fireTableRowsDeleted(i, i);
					servers.remove(i);
				}
			}
		}

		public synchronized JoinableServer getServer(int index) {
			return servers.get(index);
		}

		@Override
		public synchronized int getRowCount() {
			return servers.size();
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
				case 0: return "Server Name";
				case 1: return "Hosted By";
				case 2: return "IP Address";
				case 3: return "Map";
				case 4: return "Players";
				default: return "";
			}
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return String.class;
		}

		@Override
		public synchronized Object getValueAt(int rowIndex, int columnIndex) {
			JoinableServer s = servers.get(rowIndex);
			switch(columnIndex) {
				case 0: return s.name;
				case 1: return s.host;
				case 2: return s.ip.toString();
				case 3: return s.map;
				case 4: return s.slots;
				default: return "";
			}
		}
	}

	private class BrowserThread extends Thread {
		@Override
		public void run() {
			MulticastSocket multicast = null;
			try {
				multicast = new MulticastSocket(MULTICAST_PORT);
				multicast.joinGroup(GROUP);
				for (;;) {
					byte[] data = new byte[1024];
					DatagramPacket msg = new DatagramPacket(new byte[1024], 1024);
					multicast.receive(msg);
					tableModel.addServer(new JoinableServer(msg));
				}
			} catch (IOException e) {
				//TODO handle exception
			} finally {
				if (multicast != null)
					multicast.close();
			}
		}
	}

	private class ServerPruner extends Timer implements ActionListener {

		public ServerPruner() {
			super(BROADCAST_DELAY, null);
			addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			tableModel.pruneServers();
		}
	}

	private class JoinableServer {

		private InetAddress ip;
		private String name, host, map, slots;
		private long lastUpdate;

		public JoinableServer(DatagramPacket data) {
			lastUpdate = System.currentTimeMillis();
			ip = data.getAddress();

			String msg = new String(data.getData()).trim();
			int a = msg.indexOf(SERVER_NAME);
			int b = msg.indexOf(SERVER_HOST);
			int c = msg.indexOf(SERVER_MAP);
			int d = msg.indexOf(SERVER_SLOTS);
			name = msg.substring(a+1, b);
			host = msg.substring(b+1, c);
			map = msg.substring(c+1, d);
			slots = msg.substring(d+1);
		}

		public void join() {
			Socket socket = null;
			try {
				socket = new Socket(ip, GAME_PORT);
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				String data = getProfile().getName();
				out.writeInt(data.length());
				out.writeBytes(data);
				//TODO send name, image, etc

				setScreen(new LobbyMenu(socket));
			} catch (IOException ex) {
				showError("Could not connect to server.");
			} finally {
				if (socket != null)
					try {socket.close();} catch (IOException e) {}
			}
		}

		//Generated method
		@Override
		public int hashCode() {
			int hash = 5;
			hash = 43 * hash + (this.ip != null ? this.ip.hashCode() : 0);
			hash = 43 * hash + (int) (this.lastUpdate ^ (this.lastUpdate >>> 32));
			return hash;
		}

		//Generated method
		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			final JoinableServer other = (JoinableServer) obj;
			if (this.ip != other.ip && (this.ip == null || !this.ip.equals(other.ip)))
				return false;
			if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
				return false;
			if ((this.host == null) ? (other.host != null) : !this.host.equals(other.host))
				return false;
			if ((this.map == null) ? (other.map != null) : !this.map.equals(other.map))
				return false;
			if ((this.slots == null) ? (other.slots != null) : !this.slots.equals(other.slots))
				return false;
			if (this.lastUpdate != other.lastUpdate) return false;
			return true;
		}
	}

	//TODO remove
	public static void main(String[] args) {
		initFrame();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setScreen(new JoinMenu());
				setFrameVisible(true);
			}
		});
	}
}
