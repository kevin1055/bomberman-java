package brotherdong.bomberman;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public class Bomb extends GameObject implements Solid {

        //Server fields
        private int timer;
        private Player player;
        private int hspeed, vspeed;

        //Client fields
        private int frame;
        private List<Spark> sparks;
        private static Image[] anim;

        static {
                BufferedImage src = loadImage("bomb.png");
                anim = new Image[3];
                for (int i = 0; i < 3; i++) {
                        anim[i] = src.getSubimage(i * 32, 0, 32, 32);
                }
        }

        //Server constructor
        public Bomb(int x, int y, Player player) {
                this.x = x;
                this.y = y;
                this.width = 32;
                this.height = 32;
                this.player = player;

                depth = -1;
                timer = 120;
                hspeed = 0;
                vspeed = 0;
        }

        //Client constructor
        public Bomb(int index, DataInputStream in) throws IOException {
                super(index);
                x = in.readInt();
                y = in.readInt();
                depth = y;
                frame = 0;
                sparks = new ArrayList<Spark>();
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
                out.writeInt(x);
                out.writeInt(y);
        }

        @Override
        public void receiveUpdate(DataInputStream in) throws IOException {
                x = in.readInt();
                y = in.readInt();
        }

        @Override
        public void step() {
                //Movement
                if (x % 32 == 0 && y % 32 == 0) {
                        if (hspeed != 0) {
                                Rectangle test = getClip();
                                test.x += hspeed > 0 ? 32 : -32;
                                List<Solid> col = server.getSolidCollisions(test);
                                if (!col.isEmpty()) {
                                        hspeed = 0;
                                }
                        }
                        if (vspeed != 0) {
                                Rectangle test = getClip();
                                test.y += vspeed > 0 ? 32 : -32;
                                List<Solid> col = server.getSolidCollisions(test);
                                if (!col.isEmpty()) {
                                        vspeed = 0;
                                }
                        }
                }

                x += hspeed;
                y += vspeed;

                if (hspeed != 0 || vspeed != 0) requestUpdate();

                //Exploding
                timer--;
                if (timer <= 0 && x % 32 == 0 && y % 32 == 0) {
                        //Explode
                        server.remove(this);
                        player.numBombsAvailable++;
                        server.add(new Fire(x, y, player));
                }
        }

        public boolean moveHorizontal(int speed) {
                Rectangle test = getClip();
                test.x += speed > 0 ? 32 : -32;
                List<Solid> col = server.getSolidCollisions(test);
                if (col.isEmpty()) {
                        hspeed = speed;
                        return true;
                } else {
                        return false;
                }
        }

        public boolean moveVertical(int speed) {
                Rectangle test = getClip();
                test.y += speed > 0 ? 32 : -32;
                List<Solid> col = server.getSolidCollisions(test);
                if (col.isEmpty()) {
                        vspeed = speed;
                        return true;
                } else {
                        return false;
                }
        }

        @Override
        public void handleCollisions() {
        }

        @Override
        public void clientStep() {
                if (frame % 3 == 0) {
                        sparks.add(new Spark(x + 32, y));
                }
                for (ListIterator<Spark> it = sparks.listIterator(); it.hasNext();) {
                        Spark spark = it.next();
                        if (!spark.step()) it.remove();
                }

                if (frame < 119) frame++;

                depth = y;
        }

        @Override
        public void draw(Graphics g) {
                g.drawImage(anim[frame / 40], x, y, null);
                for (int i = 0; i < sparks.size(); i++) {
                        sparks.get(i).draw(g);
                }
        }

        private class Spark {

                public int x, y, hspeed, vspeed, lifetime;

                public Spark(int x, int y) {
                        this.x = x;
                        this.y = y;

                        this.hspeed = (int) (Math.random() * 4) - 2;
                        this.vspeed = -(int) (Math.random() * 4) - 4;
                        hspeed += hspeed > 0 ? 1 : -1;

                        lifetime = 10;
                }

                public boolean step() {
                        x += hspeed;
                        y += vspeed;

                        vspeed++;

                        lifetime--;
                        if (lifetime < 0) return false;
                        else return true;
                }

                public void draw(Graphics g) {
                        g.setColor(new Color(255, 200 + (int) (Math.random() * 55), 0));
                        g.fillRect(x - 1, y - 1, 3, 3);
                }
        }
}