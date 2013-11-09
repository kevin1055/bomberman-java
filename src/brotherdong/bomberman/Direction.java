package brotherdong.bomberman;

/**
 *
 * @author Kevin
 */
public enum Direction {
	EAST(0), NORTH(1), WEST(2), SOUTH(3);

	private int index;

	Direction(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public static Direction getDirection(int index) {
		switch(index) {
			case 0: return EAST;
			case 1: return NORTH;
			case 2: return WEST;
			case 3: return SOUTH;
			default: return null;
		}
	}
}
