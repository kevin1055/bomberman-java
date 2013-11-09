package brotherdong.bomberman;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public class Fire extends GameObject {

	//Server fields
	public int timer = 20;
	private Player player;
	private int direction;
	private boolean tail;

	//Client fields
	private int frame = -11;
	private Image[] img;
	private static Image[] center, hor, vert, up, down, left, right;

	static {
		BufferedImage src = loadImage("expl.png");
		center = new Image[] {
			src.getSubimage(0,  0, 32, 32),
			src.getSubimage(32, 0, 32, 32),
			src.getSubimage(64, 0, 32, 32),
			src.getSubimage(96, 0, 32, 32)
		};
		hor = new Image[] {
			src.getSubimage(160, 0,  32, 32),
			src.getSubimage(160, 32, 32, 32),
			src.getSubimage(160, 64, 32, 32),
			src.getSubimage(160, 96, 32, 32)
		};
		vert = new Image[] {
			src.getSubimage(0,  64, 32, 32),
			src.getSubimage(32, 64, 32, 32),
			src.getSubimage(64, 64, 32, 32),
			src.getSubimage(96, 64, 32, 32)
		};
		up = new Image[] {
			src.getSubimage(0,  32, 32, 32),
			src.getSubimage(32, 32, 32, 32),
			src.getSubimage(64, 32, 32, 32),
			src.getSubimage(96, 32, 32, 32)
		};
		down = new Image[] {
			src.getSubimage(0,  96, 32, 32),
			src.getSubimage(32, 96, 32, 32),
			src.getSubimage(64, 96, 32, 32),
			src.getSubimage(96, 96, 32, 32)
		};
		left = new Image[] {
			src.getSubimage(128, 0,  32, 32),
			src.getSubimage(128, 32, 32, 32),
			src.getSubimage(128, 64, 32, 32),
			src.getSubimage(128, 96, 32, 32)
		};
		right = new Image[] {
			src.getSubimage(192, 0,  32, 32),
			src.getSubimage(192, 32, 32, 32),
			src.getSubimage(192, 64, 32, 32),
			src.getSubimage(192, 96, 32, 32)
		};
	}

	//Server constructor
	public Fire(int x, int y, Player player) {
		this(x, y, null, 0, player);
	}

	//Server constructor (private, recursive)
	private Fire(int x, int y, Direction dir, int pow, Player player) {
		this.x = x;
		this.y = y;
		this.width = 32;
		this.height = 32;
		this.depth = 1;
		this.player = player;

		boolean isTail = true;
		if (dir == null) {
			for (Direction d : Direction.values()) {
				//Add new fire in all directions

				Rectangle test = getClip();
				switch (d) {
					case NORTH:	test.y -= 32; break;
					case SOUTH:	test.y += 32; break;
					case EAST:	test.x += 32; break;
					case WEST:	test.x -= 32; break;
					default: throw new AssertionError("Unknown direction");
				}

				List<Solid> col = server.getSolidCollisions(test);
				if (col.isEmpty()) {
					server.add(new Fire(test.x, test.y, d, pow+1, player));
					isTail = false;
				} else {
					//Test for destructable objects in way
					List<GameObject> col2 = server.getAllCollisions(test);
					for (int i = 0; i < col2.size(); i++) {
						GameObject s = col2.get(i);
						if (s instanceof SoftBlock) {
							((SoftBlock) s).destroy();
							server.add(new Fire(test.x, test.y, d, -1, player));
							isTail = false;
							break;
						}
						if (s instanceof Item) {
							server.remove(s);
							server.add(new Fire(test.x, test.y, d, pow+1, player));
							isTail = false;
							break;
						}
					}
				}
			}
		} else if (pow < player.numFire && pow != -1) {
			//Add new fire in specified direction
			Rectangle test = getClip();
			switch (dir) {
				case NORTH:	test.y -= 32; break;
				case SOUTH:	test.y += 32; break;
				case EAST:	test.x += 32; break;
				case WEST:	test.x -= 32; break;
				default: throw new AssertionError("Unknown direction");
			}

			//Test for block in the way
			List<Solid> col = server.getSolidCollisions(test);
			if (col.isEmpty()) {
				server.add(new Fire(test.x, test.y, dir, pow+1, player));
				isTail = false;
			} else {
				//Test for destructable objects in way
				List<GameObject> col2 = server.getAllCollisions(test);
				for (int i = 0; i < col2.size(); i++) {
					GameObject s = col2.get(i);
					if (s instanceof SoftBlock) {
						((SoftBlock) s).destroy();
						server.add(new Fire(test.x, test.y, dir, -1, player));
						isTail = false;
						break;
					}
				}
			}
		}

		//Set appearance
		this.direction = dir == null ? -1 : dir.getIndex();
		this.tail = isTail;
	}

	//Client constructor
	public Fire(int index, DataInputStream in) throws IOException {
		super(index);
		x = in.readInt();
		y = in.readInt();
		Direction dir = Direction.getDirection(in.readInt());
		boolean tail = in.readBoolean();
		depth = y;

		//Set image
		if (dir == null) img = center;
		else switch (dir) {
			case NORTH: img = tail ? up : vert; break;
			case SOUTH: img = tail ? down : vert; break;
			case EAST: img = tail ? right : hor; break;
			case WEST: img = tail ? left : hor; break;
			default: img = center;
		}
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(direction);
		out.writeBoolean(tail);
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
		timer--;
		if (timer <= 0) {
			server.remove(this);
		}
	}
	
	@Override
	public void handleCollisions() {
	}

	@Override
	public void clientStep() {
		//Animate
		if (frame < 10) frame++;
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(img[3-Math.abs(frame/3)], x, y, null);
	}
}