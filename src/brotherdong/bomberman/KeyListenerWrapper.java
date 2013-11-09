package brotherdong.bomberman;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

/**
* A simple, small and easy to use KeyListener wrapper to fix the famous
* Linux key repeat *bug*.
*
* @author Ren Jeschke (rene_jeschke@yahoo.de)
*/
public final class KeyListenerWrapper implements KeyListener, Runnable
{
	/** The listener to delegate key events to. */
	final KeyListener wrappedListener;
	/** Our background thread. */
	private final Thread watcher;
	/** Our key event queue. */
	private ArrayBlockingQueue<KeyEvent> keyEvents = new ArrayBlockingQueue<KeyEvent>(2048);
	/** Whether to post KeyEvents using invokeLater or not. */
	private final boolean useInvokeLater;

	/**
	 * Ctor.
	 *
	 * @param wrapped The wrapped KeyListener
	 * @param useInvokeLater Whether to post KeyEvents using invokeLater or not.
	 */
	private KeyListenerWrapper(final KeyListener wrapped, final boolean useInvokeLater)
	{
		this.wrappedListener = wrapped;
		this.useInvokeLater = useInvokeLater;
		this.watcher = new Thread(this);
		this.watcher.setDaemon(true);
	}

	/**
	 * Initializes this key listener wrapper (also starts the background thread).
	 *
	 * @param wrapped The KeyListener to wrap.
	 * @param useInvokeLater Whether to post KeyEvents using invokeLater or not.
	 * @return this
	 */
	public static KeyListenerWrapper init(final KeyListener wrapped, final boolean useInvokeLater)
	{
		final KeyListenerWrapper wrapper = new KeyListenerWrapper(wrapped, useInvokeLater);
		wrapper.watcher.start();
		return wrapper;
	}

	/**
	 * Posts a key event.
	 *
	 * @param e The KeyEvent.
	 * @param invokeLater Whether to use invokeLater or not.
	 */
	void postKeyEvent(final KeyEvent e, final boolean invokeLater)
	{
		if(invokeLater)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					KeyListenerWrapper.this.postKeyEvent(e, false);
				}
			});
		}
		else
		{
			switch(e.getID())
			{
			case KeyEvent.KEY_PRESSED:
				this.wrappedListener.keyPressed(e);
				break;
			case KeyEvent.KEY_RELEASED:
				this.wrappedListener.keyReleased(e);
				break;
			case KeyEvent.KEY_TYPED:
				this.wrappedListener.keyTyped(e);
				break;
			}
		}
	}

	/**
	 * @see KeyListener#keyPressed(KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent e)
	{
		this.keyEvents.add(e);
	}

	/**
	 * @see KeyListener#keyReleased(KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent e)
	{
		this.keyEvents.add(e);
	}

	/**
	 * @see KeyListener#keyTyped(KeyEvent)
	 */
	@Override
	public void keyTyped(KeyEvent e)
	{
		this.keyEvents.add(e);
	}

	/**
	 * @see Runnable#run()
	 */
	@Override
	public void run()
	{
		KeyEvent last = null;

		for(;;) /* I could do this all day long ... */
		{
			try
			{
				if(last == null)
				{
					last = this.keyEvents.take();
				}

				switch(last.getID())
				{
				case KeyEvent.KEY_PRESSED:
				case KeyEvent.KEY_TYPED:
					this.postKeyEvent(last, this.useInvokeLater);
					last = null;
					break;
				case KeyEvent.KEY_RELEASED:
					{
						final KeyEvent next = this.keyEvents.poll(5, TimeUnit.MILLISECONDS);
						if(next == null)
						{
							this.postKeyEvent(last, this.useInvokeLater);
							last = null;
						}
						else if(next.getID() == KeyEvent.KEY_PRESSED
								&& next.getKeyCode() == last.getKeyCode()
								&& next.getWhen() == last.getWhen())
						{
							last = next;
						}
						else
						{
							this.postKeyEvent(last, this.useInvokeLater);
							last = next;
						}
					}
					break;
				}
			}
			catch (InterruptedException eaten)
			{
				// *munch*
			}
		}
	}
}
