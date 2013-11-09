package brotherdong.bomberman;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 *
 * @author Kevin
 */
public final class Global {

	//Speed caps
	public static final int UPDATE_FREQUENCY = 30;
	public static final int FPS_MAX = 30;
	public static final int UPDATE_DELAY = Math.round(1000f/UPDATE_FREQUENCY);
	public static final int FPS_DELAY = Math.round(1000f/FPS_MAX);
	public static final int BROADCAST_DELAY = 500;

	//Constants sent between server/client
	public static final byte ADD = -128;
	public static final byte REMOVE = -127;
	public static final byte UPDATE = -126;
	public static final byte GAME_OVER = -125;

	public static final int END = Integer.MIN_VALUE;
	public static final int START_GAME = Integer.MAX_VALUE;

	public static final char SERVER_NAME =	65535;
	public static final char SERVER_HOST =	65534;
	public static final char SERVER_MAP =	65533;
	public static final char SERVER_SLOTS =	65532;

	//Ports
	public static final int MULTICAST_PORT = 24844;
	public static final int GAME_PORT = 24845;
	public static final int LOCAL_PORT = 24844;

	//Server validation
	public static final InetAddress GROUP;
	public static final String IDENTIFIER = "kevindu.bombermanDEV|"; //TODO change for release

	//Main JFrame
	private static final JFrame frame = new JFrame();
	private static Screen screen;

	//Main profile
	private static Profile profile = new NullProfile();

	//Whether client is also host
	public static boolean isServer = false;

	static {
		try {
			GROUP = InetAddress.getByName("230.5.5.5");
		} catch (UnknownHostException e) {
			throw new AssertionError(e);
		}

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				StackTraceElement[] stack = e.getStackTrace();
				StackTraceElement mine = null;
				for (StackTraceElement s : stack) {
					if (s.getClassName().startsWith("brotherdong.bomberman.")) {
						mine = s;
						break;
					}
				}
				int option = JOptionPane.showConfirmDialog(
					frame,
					"Uh oh! Unhandled exception occurred: \n\n" + e
						+ (mine == null ? "\n"
							: ("\n" + mine.getClassName()
							+ "(" + mine.getFileName()
							+ ":" + mine.getLineNumber() + ")\n\n"))
						+ "You can try to continue, but this is most likely an unrecoverable situation.\n"
						+ "Abort the program?",
					"Error!",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				if (option == JOptionPane.YES_OPTION)
					System.exit(1);
			}
		});
	}

	private Global() {
	}

	public static void initFrame() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					//TODO set size
					frame.setBounds(0, 0, 600, 600);
					frame.setLocationByPlatform(true);
					//TODO handle closing
					//frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
			});
		} catch (InterruptedException ex) {
			throw new Error("Thread interrupted while initializing frame!", ex);
		} catch (InvocationTargetException ex) {
			throw new Error(ex);
		}
	}

	public static BufferedImage loadImage(String filename) {
		BufferedImage src;
		try {
			src = ImageIO.read(ClassLoader.getSystemResource("res/"+filename));
		} catch (IOException e) {
			showErrorAndExit("Missing file: " + filename);
			return null;
		}

		//Make image compatible (improves draw speed)
		BufferedImage result = frame.getGraphicsConfiguration()
			.createCompatibleImage(src.getWidth(), src.getHeight(), src.getTransparency());
		Graphics g = result.getGraphics();
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return result;
	}

	public static int getID(GameObject obj) {
		if (obj instanceof Bomb)              return 0;
		if (obj instanceof Player)            return 1;
		if (obj instanceof HardBlock)         return 2;
		if (obj instanceof SoftBlock)         return 3;
		if (obj instanceof Fire)              return 4;
		if (obj instanceof Item)              return 5;
		if (obj instanceof DeadPlayer)        return 6;
		throw new RuntimeException("Object unassigned ID: " + obj.getClass().getSimpleName());
	}

	public static GameObject receiveObject(int index, byte id, DataInputStream in) throws IOException {
		switch (id) {
			case 0: return new Bomb(index, in);
			case 1: return new Player(index, in);
			case 2: return new HardBlock(index, in);
			case 3: return new SoftBlock(index, in);
			case 4: return new Fire(index, in);
			case 5: return new Item(index, in);
			case 6: return new DeadPlayer(index, in);
			default: throw new RuntimeException("Object with unknown ID " + id + " found");
		}
	}

	public static Profile getProfile() {
		return profile;
	}

	public static void setProfile(Profile profile) {
		if (profile == null)
			Global.profile = new NullProfile();
		else
			Global.profile = profile;
	}

	public static void setFrameVisible(boolean visible) {
		frame.setVisible(visible);
	}

	public static Screen getScreen() {
		return screen;
	}

	public static void setScreen(Screen screen) {
		if (screen == null)
			throw new NullPointerException("Attempting to set screen as null");
		if (Global.screen != null)
			Global.screen.cleanup();
		screen.init();
		frame.setContentPane(screen.getPanel());
		frame.validate();

		Global.screen = screen;
	}

	public static void addKeyListener(KeyListener listener) {
		frame.addKeyListener(listener);
		frame.requestFocus();
	}

	public static void removeKeyListener(KeyListener listener) {
		frame.removeKeyListener(listener);
	}

	public static void showError(String message) {
		JOptionPane.showMessageDialog(frame, message, "Error!", JOptionPane.ERROR_MESSAGE);
	}

	public static void showErrorAndExit(String message) {
		JOptionPane.showMessageDialog(frame, message, "Error!", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
}
