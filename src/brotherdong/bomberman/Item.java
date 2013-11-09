package brotherdong.bomberman;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public class Item extends GameObject {

	//Server fields
	private Type type;

	//Client fields
	private Type t;
	private double height = 0;

	//Server constructor
	public Item(int x, int y) {
		this.x = x;
		this.y = y;
		this.width = 32;
		this.height = 32;
		this.type = randomType();
	}

	//Client constructor
	public Item(int index, DataInputStream in) throws IOException {
		super(index);
		x = in.readInt();
		y = in.readInt();
		t = Type.getType(in.readInt());
		depth = y;
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(type.getIndex());
	}

	@Override
	public boolean isTransmitted() {
		return true;
	}

	@Override
	public void sendUpdate(DataOutputStream out) {
	}

	@Override
	public void receiveUpdate(DataInputStream in) {
	}

	@Override
	public void step() {
	}
	
	@Override
	public void handleCollisions() {
	}

	public void applyEffect(Player player) {
		switch (type) {
			case BOMB: player.giveBomb(); break;
			case FIRE: player.giveFire(); break;
			case KICK: player.giveKick(); break;
			case MAX_FIRE: player.giveFullFire(); break;
			case VEST: player.giveVest(); break;
			default: throw new IllegalArgumentException("Unknown item effect: " + type);
		}
	}

	private static Type randomType() {
		double rnd = Math.random();
		if      (rnd <= 0.30) return Type.BOMB;
		else if (rnd <= 0.60) return Type.FIRE;
		else if (rnd <= 0.75) return Type.MAX_FIRE;
		else if (rnd <= 0.90) return Type.KICK;
		else                  return Type.VEST;
	}

	@Override
	public void clientStep() {
		height += .1;
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(t.getImage(), x + 2, y - 2 + (int) Math.round(Math.sin(height) * 4), null);
	}
}

enum Type {
	FIRE(0), BOMB(1), MAX_FIRE(2), VEST(3), KICK(4);

	private static BufferedImage fire, bomb, fullFire, vest, kick;

	static {
		AffineTransformOp at = new AffineTransformOp(
			AffineTransform.getScaleInstance(1.75, 1.75),
			AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		BufferedImage src = loadImage("items.png");
		fire = at.filter(src.getSubimage(0, 0, 16, 16), null);
		bomb = at.filter(src.getSubimage(16, 0, 16, 16), null);
		fullFire = at.filter(src.getSubimage(32, 0, 16, 16), null);
		vest = at.filter(src.getSubimage(48, 0, 16, 16), null);
		kick = at.filter(src.getSubimage(64, 0, 16, 16), null);
	}

	private int index;

	Type(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public BufferedImage getImage() {
		switch(index) {
			case 0: return fire;
			case 1: return bomb;
			case 2: return fullFire;
			case 3: return vest;
			case 4: return kick;
			default: throw new AssertionError("Unknown item image index");
		}
	}

	public static Type getType(int index) {
		switch (index) {
			case 0: return FIRE;
			case 1: return BOMB;
			case 2: return MAX_FIRE;
			case 3: return VEST;
			case 4: return KICK;
			default: throw new IllegalArgumentException("Unknown item type " + index);
		}
	}
}
