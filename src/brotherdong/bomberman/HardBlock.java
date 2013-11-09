package brotherdong.bomberman;

import java.awt.*;
import java.io.*;

/**
 *
 * @author Kevin
 */
public class HardBlock extends GameObject implements Solid {

	//Server constructor
	public HardBlock(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	//Client constructor
	public HardBlock(int index, DataInputStream in) throws IOException {
		super(index);
		x = in.readInt();
		y = in.readInt();
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
	public void sendUpdate(DataOutputStream out) throws IOException {
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

	@Override
	public void clientStep() {
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(Color.BLACK);
		g.drawRect(x, y, 32, 32);
		g.setColor(Color.GRAY);
		g.fillRect(x, y, 32, 32);
	}
}