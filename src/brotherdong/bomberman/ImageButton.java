package brotherdong.bomberman;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.AbstractButton;
import javax.swing.DefaultButtonModel;
import javax.swing.plaf.basic.BasicButtonListener;

/**
 *
 * @author 5121717
 */
public class ImageButton extends AbstractButton {

	private BufferedImage normalImg, hoverImg, pressedImg;
	private boolean isPressed, isHover;

	public ImageButton(BufferedImage image) {
		this(image, image, image);
	}

	public ImageButton(BufferedImage normalImg, BufferedImage pressedImg) {
		this(normalImg, normalImg, pressedImg);
	}

	public ImageButton(BufferedImage normalImg, BufferedImage hoverImg, BufferedImage pressedImg) {
		this.normalImg = normalImg;
		this.hoverImg = hoverImg;
		this.pressedImg = pressedImg;

		isPressed = false;
		isHover = false;

		Dimension dimen = new Dimension(normalImg.getWidth(), normalImg.getHeight());
		setMaximumSize(dimen);
		setMinimumSize(dimen);
		setPreferredSize(dimen);
		setModel(new DefaultButtonModel());
		addMouseListener(new Handler(this));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(isPressed ? pressedImg : (isHover ? hoverImg : normalImg), 0, 0, null);
	}

	private class Handler extends BasicButtonListener {

		public Handler(AbstractButton b) {
			super(b);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			isPressed = true;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			isPressed = false;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			super.mouseEntered(e);
			isHover = true;
		}

		@Override
		public void mouseExited(MouseEvent e) {
			super.mouseExited(e);
			isHover = false;
		}
	}
}
