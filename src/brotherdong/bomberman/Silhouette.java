package brotherdong.bomberman;

import java.awt.*;
import java.util.StringTokenizer;
import static java.lang.Integer.parseInt;

/**
 *
 * @author Kevin
 */
public class Silhouette {
	private Rectangle[] shape;

	public Silhouette(String input) {
		StringTokenizer s = new StringTokenizer(input);
		shape = new Rectangle[parseInt(s.nextToken())];
		for (int i = 0; i < shape.length; i++) {
			shape[i] = new Rectangle(
				parseInt(s.nextToken()),
				parseInt(s.nextToken()),
				parseInt(s.nextToken()),
				parseInt(s.nextToken()));
		}
	}

	public void draw(Graphics g) {
		for (int i = 0; i < shape.length; i++) {
			Rectangle r = shape[i];
			g.fillRect(r.x, r.y, r.width, r.height);
		}
	}
}
