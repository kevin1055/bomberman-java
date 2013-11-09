package brotherdong.bomberman;

import java.awt.Color;
import java.awt.Graphics;
import java.io.*;

/**
 *
 * @author Kevin
 */
public class SoftBlock extends GameObject implements Solid {

	//Server constructor
	public SoftBlock(int x, int y) {
		this.x = x;
		this.y = y;
		this.width = 32;
		this.height = 32;
	}

	//Client constructor
	public SoftBlock(int index, DataInputStream in) throws IOException {
		super(index);
		x = in.readInt();
		y = in.readInt();
		depth = y;
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
	}

	@Override
	public boolean isTransmitted() {
		return true;
	}

	@Override
	public void sendUpdate(DataOutputStream out) {
	}

	@Override
	public void receiveUpdate(DataInputStream in) throws IOException {
	}

	@Override
	public void step() {
	}

	@Override
	public void handleCollisions() {
	}

	public void destroy() {
		if (Math.random() < 0.5)
			server.add(new Item(x, y));
		server.remove(this);
	}

	@Override
	public void clientStep() {
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(Color.ORANGE);
		g.fillRect(x, y, 32, 32);
	}
}