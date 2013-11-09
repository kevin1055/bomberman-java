package brotherdong.bomberman;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public class Server {

	private final List<GameObject> objects = new ArrayList<GameObject>();
	private final List<GameObject> addQueue = new ArrayList<GameObject>();
	private final List<GameObject> removeQueue = new ArrayList<GameObject>();
	private final List<GameObject> sendAddQueue = new ArrayList<GameObject>();
	private final List<GameObject> sendRemoveQueue = new ArrayList<GameObject>();

	private final List<Client> clients = new ArrayList<Client>(4);
	private final GameThread gameThread = new GameThread();

	private int currentIndex = 0;
	private int winner = -1;
	private int gameEndTimer = 100;
	public int livePlayers;

	public Server(Collection<Socket> sockets) {
		int i = 0;
		for (Socket s : sockets) {
			try {
				clients.add(new Client(s, i++));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		livePlayers = clients.size();
	}

	public Client getClient(int c) {
		return clients.get(c);
	}

	public void start() {
		isServer = true;
		GameObject.server = this;
		System.out.println("Clients size: " + clients.size());
		for (int i = 0; i < clients.size(); i++) {
			switch (i) {
				case 0: add(new Player(32, 32, 0)); break;
				case 1: add(new Player(416, 352, 1)); break;
				case 2: add(new Player(32, 352, 2)); break;
				case 3: add(new Player(416, 32, 3)); break;
				default: throw new RuntimeException("Too many players " + clients.size());
			}
		}

		for (int i = 64; i <= 384; i += 64) {
			for (int j = 64; j <= 320; j += 64) {
				add(new HardBlock(i, j, 32, 32));
			}
		}
		add(new HardBlock(0, 0, 480, 32));
		add(new HardBlock(0, 32, 32, 352));
		add(new HardBlock(448, 32, 32, 352));
		add(new HardBlock(0, 384, 480, 32));

		for (int i = 32; i <= 416; i += 32) {
			for (int j = 96; j <= 288; j += 64) {
				add(new SoftBlock(i, j));
			}
		}

		for (int i = 96; i <= 352; i += 64) {
			for (int j = 64; j <= 352; j += 64) {
				add(new SoftBlock(i, j));
			}
		}

		for (int i = 96; i <= 352; i += 32) {
			add(new SoftBlock(i, 32));
			add(new SoftBlock(i, 352));
		}

		for (int j = 128; j <= 288; j += 64) {
			add(new SoftBlock(32, j));
			add(new SoftBlock(416, j));
		}

		for (Client c : clients)
			c.start();
		gameThread.start();
	}

	public void add(GameObject object) {
		addQueue.add(object);
		sendAddQueue.add(object);
	}

	public void remove(GameObject object) {
		removeQueue.add(object);
		sendRemoveQueue.add(object);
	}

	public void removeAll() {
		removeQueue.addAll(objects);
		sendRemoveQueue.addAll(objects);
	}

	private void update() {
		objects.addAll(addQueue);
		addQueue.clear();
		objects.removeAll(removeQueue);
		removeQueue.clear();
		Collections.sort(objects);
	}

	public int nextIndex() {
		return currentIndex++;
	}

	public List<Solid> getSolidCollisions(Rectangle clip) {
		ArrayList<Solid> ans = new ArrayList<Solid>();
		for (int i = 0; i < objects.size(); i++) {
			GameObject o = objects.get(i);
			if (o instanceof Solid && clip.intersects(o.getClip()))
				ans.add((Solid) o);
		}
		return ans;
	}

	public List<GameObject> getAllCollisions(Rectangle clip) {
		ArrayList<GameObject> ans = new ArrayList<GameObject>();
		for (int i = 0; i < objects.size(); i++) {
			GameObject o = objects.get(i);
			if (clip.intersects(o.getClip()))
				ans.add(o);
		}
		return ans;
	}

	public void cleanup() {
		isServer = false;
		for (Client c : clients)
			c.interrupt();
		gameThread.interrupt();
	}

	public class Client {
		private int index;
		private Socket socket;
		private DataInputStream in;
		private DataOutputStream out;
		private Thread thread;
		private final boolean[] keys;

		public Client(Socket sock, int index) throws IOException {
			this.socket = sock;
			this.in = new DataInputStream(sock.getInputStream());
			this.out = new DataOutputStream(sock.getOutputStream());
			this.thread = new ClientThread();
			this.index = index;
			this.keys = new boolean[5];
		}

		public boolean[] getKeys() {
			synchronized(keys) {
				return keys.clone();
			}
		}

		private void start() {
			thread.start();
		}

		private void interrupt() {
			thread.interrupt();
		}

		private class ClientThread extends Thread {
			@Override
			public void run() {
				try {
					for(;;) {
						//Get input
						byte input = in.readByte();

						if (input == GAME_OVER) {
							throw new InterruptedException();
						}

						//Read input
						synchronized(keys) {
							keys[0] = (input & 0x1) == 0x1;
							keys[1] = (input & 0x2) == 0x2;
							keys[2] = (input & 0x4) == 0x4;
							keys[3] = (input & 0x8) == 0x8;
							keys[4] = (input & 0x10) == 0x10;
						}

						//Check for interrupt
						if (Thread.interrupted())
							throw new InterruptedException();
					}
				} catch (InterruptedException e) {
					//Game over
					e.printStackTrace(System.out);
					List<Socket> sockets = new ArrayList<Socket>();
					for (Client c : clients)
						sockets.add(c.socket);

					cleanup();
					//new HostMenu(sockets);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {

				}
			}
		}
	}

	private class GameThread extends Thread {
		@Override
		public void run() {
			try {
				LoopDelay delay = new LoopDelay(UPDATE_DELAY);

				for(;;) {
					//Update game
					for (int i = 0; i < objects.size(); i++) {
						objects.get(i).step();
					}
					update();

					//Test game over
					if (livePlayers <= 1) {
						if (winner == -1) {
							winner = 0;
							for (int i = 0; i < objects.size(); i++) {
								GameObject s = objects.get(i);
								if (s instanceof Player) {
									Player p = (Player) s;
									if (p.dead == false) {
										winner = p.playerIndex;
										p.giveVest();
										p.dead = true;
										break;
									}
								}
							}
						} else {
							gameEndTimer--;
						}
					}

					//Send data to each client
					ByteArrayOutputStream o = new ByteArrayOutputStream();
					DataOutputStream out = new DataOutputStream(o);

					//Send added objects
					for (int i = 0; i < sendAddQueue.size(); i++) {
						GameObject s = sendAddQueue.get(i);
						if (s.isTransmitted()) {
							out.write(ADD);
							out.writeInt(s.index);
							out.write(s.id);
							s.send(out);
						}
					}

					//Send removed object indexes
					for (int i = 0; i < sendRemoveQueue.size(); i++) {
						GameObject s = sendRemoveQueue.get(i);
						if (s.isTransmitted()) {
							out.write(REMOVE);
							out.writeInt(s.index);
						}
					}


					//Send updated info for objects
					for (int i = 0; i < objects.size(); i++) {
						GameObject s = objects.get(i);
						if (s.updateRequired()) {
							out.write(UPDATE);
							out.writeInt(s.index);
							s.sendUpdate(out);
						}
					}

					//Clear queues
					sendAddQueue.clear();
					sendRemoveQueue.clear();

					//Send win info
					if (gameEndTimer <= 0) {
						out.write(GAME_OVER);
						out.writeByte((byte) winner);
					}

					//Send data
					byte[] b = o.toByteArray();
					for (int i = 0; i < clients.size(); i++) {
						Client c = clients.get(i);
						c.out.write(b);
					}

					//Sleep until next update
					delay.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

			}
		}
	}
}
