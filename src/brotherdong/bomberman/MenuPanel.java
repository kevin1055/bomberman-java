package brotherdong.bomberman;

import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public abstract class MenuPanel extends JPanel {

	private static final BufferedImage tile;
	private static int x, y, width, height;
	private static Timer timer;

	static {
		tile = loadImage("tile.png");
		x = 0;
		y = 0;
		width = tile.getWidth();
		height = tile.getHeight();
		timer = new Timer(FPS_DELAY, null);
	}

	public MenuPanel() {
	}

	public void init() {
		timer.start();
		timer.addActionListener(new TimerListener());
	}

	public void cleanup() {
		timer.stop();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (int i = x-tile.getWidth(); i < getWidth(); i += width) {
			for (int j = y-tile.getHeight(); j < getHeight(); j += height) {
				g.drawImage(tile, i, j, null);
			}
		}
	}

	private class TimerListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			//Animate bg
			x = (x + 2) % tile.getWidth();
			y = (y + 2) % tile.getHeight();
			repaint();
		}
	}
}
