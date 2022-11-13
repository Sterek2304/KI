package analysieren;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanTileType;

public class Gefahrenbewertung {

	private Vector2 position;
	private PacmanTileType[][] view;
	private Analyse bubble;
	private double[][] map;

	/*
	 * Gefahrenbewertung bewertet die Felder (in einem Radius von 2 Feldern) um den Ghost herum
	 */
	public Gefahrenbewertung(Vector2 pos, Analyse bubble) {
		this.position = pos;
		this.view = bubble.getView();
		this.bubble = bubble;
		map = new double[view.length][view[0].length];								//Map wird angelegt, die nachher gemerged wird
		map[position.x][position.y] = -1;											//Ghost bekommt die Bewertung -1
		bewerteGefahr(position.x, position.y, 0);									//erstmaliges erstellen der Bubble
	}

	// Methode zum aktualisieren der Ghostpositionen
	public void ghostPosAkt(Vector2 pos) {
		if (!position.equals(pos)) {
			map[position.x][position.y] = 0;
			bubbleLoeschen();
			position = pos;
			map[position.x][position.y] = -1;
			bubble.setMDPMap(position.x, position.y, -1);
			bewerteGefahr(position.x, position.y, 0);
		}
	}

	// Bewertet die Felder (Radius: 2) um den Ghost
	public void bewerteGefahr(int x, int y, int steps) {
		if ((steps < 2) && (view[x][y] != null) && (view[x][y] != PacmanTileType.WALL)) {

			if (map[x][y] != -1) {
				steps += 1;

				if (view[x][y] == PacmanTileType.DOT)
					map[x][y] = (-(0.25 / (double) steps)) - bubble.dotWert;
				else
					map[x][y] = -(0.25 / (double) steps);
			}

			if (view[x + 1][y] != PacmanTileType.WALL && map[x + 1][y] == 0)
				bewerteGefahr(x + 1, y, steps);

			if (view[x][y + 1] != PacmanTileType.WALL && map[x][y + 1] == 0)
				bewerteGefahr(x, y + 1, steps);

			if (view[x - 1][y] != PacmanTileType.WALL && map[x - 1][y] == 0)
				bewerteGefahr(x - 1, y, steps);

			if (view[x][y - 1] != PacmanTileType.WALL && map[x][y - 1] == 0)
				bewerteGefahr(x, y - 1, steps);
		}
	}

	// setzt die Felder in der Map zurück, wenn sich der Ghost ein Feld weiter bewegt
	private void bubbleLoeschen() {
		this.map = new double[view.length][view[0].length];
	}

	public Vector2 getPosition() {
		return this.position;
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public double[][] getMap() {
		return this.map;
	}
}
