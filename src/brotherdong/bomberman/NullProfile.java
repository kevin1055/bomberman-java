package brotherdong.bomberman;

/**
 * Placeholder for a guest profile.
 * @author Kevin
 */
public class NullProfile extends Profile {

	public NullProfile() {
		super("Guest" + (System.currentTimeMillis() % 1000), new char[0]);
	}

	@Override
	public int getKills() {
		return 0;
	}

	@Override
	public int getDeaths() {
		return 0;
	}

	@Override
	public int getWins() {
		return 0;
	}

	@Override
	public int getLosses() {
		return 0;
	}

	@Override
	public int getScore() {
		return 0;
	}

	@Override
	public int getBombsUsed() {
		return 0;
	}

	@Override
	public int getDistanceWalked() {
		return 0;
	}

	@Override
	public int getTimePlayed() {
		return 0;
	}

	@Override
	public int getKillsForPlayer(String player) {
		return 0;
	}

	@Override
	public void addKills(int kills) {
	}

	@Override
	public void addDeaths(int deaths) {
	}

	@Override
	public void addWins(int wins) {
	}

	@Override
	public void addLosses(int losses) {
	}

	@Override
	public void addScore(int score) {
	}

	@Override
	public void addBombsUsed(int bombsUsed) {
	}

	@Override
	public void addDistanceWalked(int distanceWalked) {
	}

	@Override
	public void addTimePlayed(int timePlayed) {
	}

	@Override
	public void addKillsForPlayer(String player, int kills) {
	}

	@Override
	public void setName(String name) {
	}

	@Override
	public void setKills(int kills) {
	}

	@Override
	public void setDeaths(int deaths) {
	}

	@Override
	public void setWins(int wins) {
	}

	@Override
	public void setLosses(int losses) {
	}

	@Override
	public void setScore(int score) {
	}

	@Override
	public void setBombsUsed(int bombsUsed) {
	}

	@Override
	public void setDistanceWalked(int distanceWalked) {
	}

	@Override
	public void setTimePlayed(int timePlayed) {
	}

	@Override
	public void setKillsForPlayer(String player, int kills) {
	}
}
