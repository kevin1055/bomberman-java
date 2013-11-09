package brotherdong.bomberman;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import static brotherdong.bomberman.Global.*;

/**
 *
 * @author Kevin
 */
public class MainMenu implements Screen {

	private static BufferedImage title;

	static {
		title = loadImage("title.png");
	}

	public MainMenu() {

	}

	@Override
	public void init() {

	}

	@Override
	public void cleanup() {

	}

	@Override
	public JPanel getPanel() {
		return null;
	}
}
