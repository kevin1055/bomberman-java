package brotherdong.bomberman;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public class DeadPlayer extends GameObject {

	//Server fields
	private int timer = 20;
	private int player;

	//Client fields
	private Image[] anim;
	private int frame;

	//Server constructor
	public DeadPlayer(int x, int y, int player) {
		this.x = x;
		this.y = y;
		this.player = player;
	}

	//Client constructor
	public DeadPlayer(int index, DataInputStream in) throws IOException {
		super(index);
		x = in.readInt();
		y = in.readInt();
		depth = y;
		frame = 0;

		int player = in.readInt();
		BufferedImage src = loadImage("playerexpl" + player + ".png");
		anim = new Image[7];
		for (int i = 0; i < 7; i++) {
			anim[i] = src.getSubimage(i * 60, 0, 60, 81);
		}
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(player);
	}

	@Override
	public void step() {
		timer--;
		if (timer <= 0) server.remove(this);
	}
	
	@Override
	public void handleCollisions() {
	}

	@Override
	public void sendUpdate(DataOutputStream out) {
	}

	@Override
	public void receiveUpdate(DataInputStream in) {
	}

	@Override
	public void clientStep() {
		if (frame < 16) frame++;
	}

	@Override
	public void draw(Graphics g) {
		int i;
		if (frame < 4) i = 0;
		else i = (frame-4)/2;

		g.drawImage(anim[i], x - 12, y - 44, null);
	}

	@Override
	public boolean isTransmitted() {
		return true;
	}
}