package brotherdong.bomberman;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public class LobbyMenu implements Screen {
	
	private JPanel panel;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private Thread serverHandler;
	
	private ClientTableModel tableModel;
	
	public LobbyMenu(Socket socket) throws IOException {
		panel = new LobbyPanel();
		this.socket = socket;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		
		serverHandler = new ServerHandler();
		tableModel = new ClientTableModel();
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public void init() {
	}

	@Override
	public void cleanup() {
	}
	
	private class LobbyPanel extends MenuPanel {
		private JScrollPane scroll;
		private JTable table;
		private JPanel tablePanel;

		public LobbyPanel() {
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
		private ArrayList<Client> clients = new ArrayList<Client>();

		public synchronized void addClient(Client client) {
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
			return columnIndex == 0 ? "" : clients.get(rowIndex).getName();
		}
	}
	
	private class ServerHandler extends Thread {
		
		@Override
		public void run() {
			try {
				for(;;) {
					byte b = in.readByte();
					if (b == REMOVE) {
						tableModel.clearClients();
					} else if (b == ADD) {
						int len = in.readInt();
						byte[] ba = new byte[len];
						in.read(ba);
						tableModel.addClient(new Client(new String(ba).trim()));
					}
					if (Thread.interrupted())
						throw new InterruptedException();
				}
			} catch (IOException e) {
				
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	private class Client {
		
		private String name;
		
		public Client(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}
