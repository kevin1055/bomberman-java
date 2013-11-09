package brotherdong.bomberman;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.*;
import javax.swing.JPanel;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public class Client implements Screen {

	private final List<GameObject> objects = new ArrayList<GameObject>();
	private final ReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock read = rwl.readLock();
	private final Lock write = rwl.writeLock();

	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;

	private final InputThread inputThread;
	private final OutputThread outputThread;
	private final GameThread gameThread;

	private KeyListener listener = KeyListenerWrapper.init(new ClientKL(), true);
	private JPanel gamePanel = new GamePanel();
	private final boolean[] keys = new boolean[5];

	private static Image bg;
	static {
		bg = loadImage("bg.png");
		Player.loadSilhouettes();
	}

	public Client(Socket socket) throws IOException {
		this.socket = socket;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());

		inputThread = new InputThread();
		outputThread = new OutputThread();
		gameThread = new GameThread();
	}

	public Client(InputStream in, OutputStream out) throws IOException {
		socket = null;
		this.in = new DataInputStream(in);
		this.out = new DataOutputStream(out);

		inputThread = new InputThread();
		outputThread = new OutputThread();
		gameThread = new GameThread();
	}

	public void init() {
		inputThread.start();
		outputThread.start();
		gameThread.start();
		addKeyListener(listener);
	}

	public void cleanup() {
		removeKeyListener(listener);

		inputThread.interrupt();
		outputThread.interrupt();
		gameThread.interrupt();
	}

	@Override
	public JPanel getPanel() {
		return gamePanel;
	}

	private class ClientKL implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			synchronized (keys) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_RIGHT:	case KeyEvent.VK_D: keys[0] = true; break;
					case KeyEvent.VK_UP:	case KeyEvent.VK_W: keys[1] = true; break;
					case KeyEvent.VK_LEFT:	case KeyEvent.VK_A: keys[2] = true; break;
					case KeyEvent.VK_DOWN:	case KeyEvent.VK_S: keys[3] = true; break;
					case KeyEvent.VK_SPACE:	keys[4] = true; break;
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			synchronized (keys) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_RIGHT:	case KeyEvent.VK_D: keys[0] = false; break;
					case KeyEvent.VK_UP:	case KeyEvent.VK_W: keys[1] = false; break;
					case KeyEvent.VK_LEFT:	case KeyEvent.VK_A: keys[2] = false; break;
					case KeyEvent.VK_DOWN:	case KeyEvent.VK_S: keys[3] = false; break;
					case KeyEvent.VK_SPACE:	keys[4] = false; break;
				}
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {}
	}

	private class GamePanel extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(bg, 0, 4, null);
			read.lock();
			try {
				for (int i = 0; i < objects.size(); i++) {
					objects.get(i).draw(g);
				}
			} finally {
				read.unlock();
			}
		}
	}

	private class InputThread extends Thread {
		@Override
		public void run() {
			try {
				for (;;) {
					//Get input
					byte state = in.readByte();
					switch (state) {
						case ADD: {
							int index = in.readInt();
							byte id = in.readByte();
							GameObject c = receiveObject(index, id, in);
							write.lockInterruptibly();
							try {
								objects.add(c);
								Collections.sort(objects);
							} finally {
								write.unlock();
							}
						} break;
						case REMOVE: {
							int index = in.readInt();
							write.lockInterruptibly();
							try {
								for (int i = 0; i < objects.size(); i++) {
									if (objects.get(i).index == index) {
										objects.remove(i);
										break;
									}
								}
							} finally {
								write.unlock();
							}
						} break;
						case UPDATE: {
							int index = in.readInt();
							read.lockInterruptibly();
							try {
								for (int i = 0; i < objects.size(); i++) {
									GameObject next = objects.get(i);
									if (next.index == index) {
										next.receiveUpdate(in);
										break;
									}
								}
							} finally {
								read.unlock();
							}
						} break;
						case GAME_OVER: {
							System.out.println("Game over!");
							cleanup();
							if (!isServer) {
								//Return to lobby
								//new Lobby(socket);
							}
						}
					}

					if (Thread.interrupted())
						throw new InterruptedException();
				}
			} catch (InterruptedException e)  {
				e.printStackTrace(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

			}
		}
	}

	private class OutputThread extends Thread {
		@Override
		public void run() {
			LoopDelay delay = new LoopDelay(UPDATE_DELAY);
			try {
				try {
					for(;;) {
						//Send keyboard info
						byte data = 0x0;
						synchronized (keys) {
							data |= keys[0] ? 0x1 : 0;
							data |= keys[1] ? 0x2 : 0;
							data |= keys[2] ? 0x4 : 0;
							data |= keys[3] ? 0x8 : 0;
							data |= keys[4] ? 0x10 : 0;
						}
						out.writeByte(data);

						delay.await();
					}
				} catch (InterruptedException e) {
					out.writeByte(GAME_OVER);
					e.printStackTrace(System.out);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

			}
		}
	}

	private class GameThread extends Thread {
		@Override
		public void run() {
			LoopDelay delay = new LoopDelay(FPS_DELAY);
			try {
				for(;;) {
					read.lockInterruptibly();
					try {
						for (int i = 0; i < objects.size(); i++) {
							objects.get(i).clientStep();
						}
						Collections.sort(objects);
					} finally {
						read.unlock();
					}
					gamePanel.repaint();
					delay.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace(System.out);
			}
		}
	}
}
