package brotherdong.bomberman;

import javax.swing.JPanel;

/**
 *
 * @author Kevin
 */
public interface Screen {

	public JPanel getPanel();

	public void init();

	public void cleanup();

}
