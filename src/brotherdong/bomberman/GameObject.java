package brotherdong.bomberman;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.*;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public abstract class GameObject implements Comparable<GameObject> {
	public static Server server;
	public final int id = getID(this);

	public final int index;
	public int x = 0, y = 0, width = 0, height = 0, depth = 0;
	private boolean update;

	public GameObject() {
		index = server.nextIndex();
	}

	protected GameObject(int index) {
		this.index = index;
	}

	protected final void add(GameObject o) {
		server.add(o);
	}

	protected final void remove(GameObject o) {
		server.remove(o);
	}

	protected final void requestUpdate() {
		update = true;
	}

	public final boolean updateRequired() {
		try {
			return update;
		} finally {
			update = false;
		}
	}

	public abstract void step();

	public abstract void handleCollisions();

	public abstract void clientStep();

	public abstract void send(DataOutputStream out) throws IOException;

	public abstract void sendUpdate(DataOutputStream out) throws IOException;

	public abstract void receiveUpdate(DataInputStream in) throws IOException;

	public abstract boolean isTransmitted();

	public abstract void draw(Graphics g);

	public Rectangle getClip() {
		return new Rectangle(x, y, width, height);
	}

	@Override
	public final int compareTo(GameObject o) {
		return depth - o.depth;
	}

	@Override
	public String toString() {
		return String.format(
			"%s%d (%d,%d,%d,%d)",
			getClass().getName(),
			index, x, y, width, height);
	}
}
