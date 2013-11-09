/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brotherdong.bomberman;

import java.io.File;

/**
 * Represents a playable map. TODO everything
 * @author Kevin
 */
public class Map {

	private String name;
	private byte[] data;

	private Map(String name, byte[] data) {
		this.name = name;
		this.data = data;
	}

	public static Map newInstance(File file) {
		//TODO implement
		return null;
	}

	public String getName() {
		return name;
	}

	public GameObject[] getObjects() {
		//TODO implement
		return null;
	}

	@Override
	public String toString() {
		return name;
	}
}
