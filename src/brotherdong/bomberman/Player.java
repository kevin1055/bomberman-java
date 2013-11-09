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
public class Player extends GameObject {

	//Shared fields
	private Direction direction;
	public int numBombs, numFire;
	private boolean invuln = false;
	private boolean kick = false;

	//Server fields
	public static final int SPEED = 2;
	private static final int MAX_BOMBS = 10;
	private static final int MAX_FIRE = 6;

	public final int playerIndex;
	private int hspeed, vspeed;
	public int numBombsAvailable;
	private int invulnTimer = 0;
	public boolean dead = false;

	//Client fields
	private int xPrev, yPrev;
	private boolean moving;
	private int frame;
	private double graySin;
	private float grayOffset;

	private Image[] right, up, left, down;
	private static Silhouette[] rightS, upS, leftS, downS;

	static {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("kevindu/bomberman/res/silhouette.dat")));
			downS = new Silhouette[] {
				new Silhouette(input.readLine()),
				new Silhouette(input.readLine()),
				new Silhouette(input.readLine()),
			};
			leftS = new Silhouette[] {
				new Silhouette(input.readLine()),
				new Silhouette(input.readLine()),
				new Silhouette(input.readLine()),
			};
			rightS = new Silhouette[] {
				new Silhouette(input.readLine()),
				new Silhouette(input.readLine()),
				new Silhouette(input.readLine()),
			};
			upS = new Silhouette[] {
				new Silhouette(input.readLine()),
				new Silhouette(input.readLine()),
				new Silhouette(input.readLine()),
			};
		} catch (IOException e) {
			showErrorAndExit("Missing file: silhouette.dat");
		}
	}

	public static void loadSilhouettes() {
		downS.hashCode();
		leftS.hashCode();
		rightS.hashCode();
		upS.hashCode();
	}

	//Server constructor
	public Player(int x, int y, int player) {
		this.x = x;
		this.y = y;
		this.width = 32;
		this.height = 32;
		this.playerIndex = player;

		hspeed = 0;
		vspeed = 0;
		direction = Direction.SOUTH;
		numBombs = 1;
		numBombsAvailable = 1;
		numFire = 1;
	}

	//Client constructor
	public Player(int index, DataInputStream in) throws IOException {
		super(index);
		x = in.readInt();
		y = in.readInt();
		playerIndex = in.readInt();

		xPrev = x;
		yPrev = y;

		kick = false;
		invuln = false;
		numBombs = 1;
		numFire = 1;
		moving = false;
		direction = Direction.SOUTH;
		frame = 0;
		grayOffset = 0f;
		graySin = 0.0;

		BufferedImage src = loadImage("player" + playerIndex + ".png");
		down = new Image[] {
			src.getSubimage(0,  0, 36, 60),
			src.getSubimage(36, 0, 36, 60),
			src.getSubimage(72, 0, 36, 60)
		};
		left = new Image[] {
			src.getSubimage(108, 0, 40, 60),
			src.getSubimage(148, 0, 40, 60),
			src.getSubimage(188, 0, 40, 60)
		};
		right = new Image[] {
			src.getSubimage(228, 0, 40, 60),
			src.getSubimage(268, 0, 40, 60),
			src.getSubimage(308, 0, 40, 60)
		};
		up = new Image[] {
			src.getSubimage(348, 0, 32, 60),
			src.getSubimage(380, 0, 32, 60),
			src.getSubimage(412, 0, 32, 60)
		};
	}

	@Override
	public void send(DataOutputStream out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(playerIndex);
	}

	@Override
	public boolean isTransmitted() {
		return true;
	}

	@Override
	public void sendUpdate(DataOutputStream out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
		out.writeBoolean(kick);
		out.writeBoolean(invuln);
		out.writeInt(direction.getIndex());
		out.writeInt(numBombs);
		out.writeInt(numFire);
	}

	@Override
	public void receiveUpdate(DataInputStream in) throws IOException {
		x = in.readInt();
		y = in.readInt();
		if (x != xPrev || y != yPrev) {
			xPrev = x;
			yPrev = y;
			moving = true;
		} else {
			moving = false;
		}
		kick = in.readBoolean();
		invuln = in.readBoolean();
		direction = Direction.getDirection(in.readInt());
		numBombs = in.readInt();
		numFire = in.readInt();
	}

	@Override
	public void step() {
		if (!dead) {
			boolean[] keys = server.getClient(index).getKeys();

			if (keys[4]) {
				//Place bomb
				if (numBombsAvailable > 0) {
					int x2 = Math.round((this.x/32f)) * 32;
					int y2 = Math.round((this.y/32f)) * 32;
					List<Solid> col = server.getSolidCollisions(new Rectangle(x2, y2, 32, 32));
					if (col.isEmpty()) {
						numBombsAvailable--;
						server.add(new Bomb(x2, y2, this));
					}
				}
			}

			if (y % 32 == 0 && x % 32 == 0) {
				hspeed = 0;
				vspeed = 0;
			}

			if (y % 32 == 0) {
				//Aligned horizontally
				if (keys[0]) {
					hspeed = SPEED;
				} else if (keys[2]) {
					hspeed = -SPEED;
				}
			}
			if (x % 32 == 0 && hspeed == 0) {
				//Aligned vertically
				if (keys[1]) {
					vspeed = -SPEED;
				} else if(keys[3]) {
					vspeed = SPEED;
				}
			}

			if (x % 32 == 0 && y % 32 == 0) {
				if (hspeed != 0) {
					Rectangle test = getClip();
					test.x += hspeed > 0 ? 32 : -32;
					List<Solid> col = server.getSolidCollisions(test);
					if (!col.isEmpty()) {
						if (kick) {
							for (int i = 0; i < col.size(); i++) {
								Solid s = col.get(i);
								if (s instanceof Bomb) {
									if (!((Bomb) s).moveHorizontal(hspeed*2)) hspeed = 0;
								} else {
									hspeed = 0;
									break;
								}
							}
						} else {
							hspeed = 0;
						}
					}
				}
				if (vspeed != 0) {
					Rectangle test = getClip();
					test.y += vspeed > 0 ? 32 : -32;
					List<Solid> col = server.getSolidCollisions(test);
					if (!col.isEmpty()) {
						if (kick) {
							for (int i = 0; i < col.size(); i++) {
								Solid s = col.get(i);
								if (s instanceof Bomb) {
									if (!((Bomb) s).moveVertical(vspeed*2)) vspeed = 0;
								} else {
									vspeed = 0;
									break;
								}
							}
						} else {
							vspeed = 0;
						}
					}
				}
			}

			x += hspeed;
			y += vspeed;

			Direction d = null;
			if      (keys[0]) d = Direction.EAST;
			else if (keys[1]) d = Direction.NORTH;
			else if (keys[2]) d = Direction.WEST;
			else if (keys[3]) d = Direction.SOUTH;

			if (d != null && d != direction) {
				direction = d;
			}

			//Invulnerability
			if (invuln) {
				if (invulnTimer > 0) {
					invulnTimer--;
				} else {
					invuln = false;
				}
			}

			requestUpdate();
		}
	}

	@Override
	public void handleCollisions() {
		if (!dead) {
			List<GameObject> col = server.getAllCollisions(getClip());
			System.out.println(col);
			for (int i = 0; i < col.size(); i++) {
				GameObject s = col.get(i);
				if (s instanceof Item) {
					Item item = (Item) s;
					item.applyEffect(this);
					server.remove(item);
				} else if (s instanceof Fire) {
					Fire fire = (Fire) s;
					if (fire.timer > 18 && !invuln) {
						kill();
					}
				}
			}
		}
	}

	public void kill() {
		server.add(new DeadPlayer(x, y, playerIndex));
		server.livePlayers--;
		dead = true;
		x = -100;
		y = -100;
		requestUpdate();
	}

	public void giveBomb() {
		numBombs = Math.min(numBombs+1, MAX_BOMBS);
		numBombsAvailable = Math.min(numBombsAvailable+1, MAX_BOMBS);
		requestUpdate();
	}

//	public void giveFullBombs() {
//		numBombs = MAX_BOMBS;
//		numBombsAvailable = Math.min(numBombsAvailable+1, MAX_BOMBS);
//		requestUpdate();
//	}

	public void giveFire() {
		numFire = Math.min(numFire+1, MAX_FIRE);
		requestUpdate();
	}

	public void giveFullFire() {
		numFire = MAX_FIRE;
		requestUpdate();
	}

	public void giveKick() {
		kick = true;
		requestUpdate();
	}

	public void giveVest() {
		invuln = true;
		invulnTimer = 300;
		requestUpdate();
	}

	@Override
	public void clientStep() {
		//Animate player
		if (moving)
			frame++;
		else
			frame = 0;
		if (frame > 19) frame = 0;

		//Animate invulnerability
		graySin += Math.PI/15;
		grayOffset = (float) Math.sin(graySin) / 4 + 0.25f;

		//Set depth
		depth = y;
	}

	@Override
	public void draw(Graphics g) {
		int i;
		switch (frame/5) {
			case 0: case 2: i = 0; break;
			case 1: i = 1; break;
			case 3: i = 2; break;
			default: throw new AssertionError("Invalid frame number " + frame);
		}
		int xOffset, yOffset;
		Image[] img;
		Silhouette[] sil;
		switch (direction) {
			case EAST:  img = right; sil = rightS; xOffset = -2; yOffset = -32; break;
			case NORTH: img = up;    sil = upS;    xOffset = 0;  yOffset = -28; break;
			case WEST:  img = left;  sil = leftS;  xOffset = -2; yOffset = -32; break;
			case SOUTH: img = down;  sil = downS;  xOffset = -2; yOffset = -32; break;
			default: throw new AssertionError("Invalid direction " + direction);
		}

		g.drawImage(img[i], x + xOffset, y + yOffset, null);

		if (invuln) {
			g.setColor(new Color(1f, 1f, 1f, 0.25f+grayOffset));
			g.translate(x + xOffset, y + yOffset);
			sil[i].draw(g);
		}

		//Draw bombs/fire icons
	}
}
