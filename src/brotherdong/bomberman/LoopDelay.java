package brotherdong.bomberman;

/**
 *
 * @author Kevin
 */
public class LoopDelay {

	private long delay;
	private long previous;

	public LoopDelay(long delay) {
		this.delay = delay;
		previous = System.currentTimeMillis();
	}

	public void reset() {
		previous = System.currentTimeMillis();
	}

	public void await() throws InterruptedException {
		long time = System.currentTimeMillis();
		long dif = time - previous;
		previous = time;

		if (dif < delay)
			Thread.sleep(delay-dif);
	}
}
