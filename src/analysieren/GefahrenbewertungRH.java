package analysieren;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanTileType;

public class GefahrenbewertungRH {
	
	private Vector2 position;
	private PacmanTileType[][] view;
	private AnalyseRH bubbleRH;
	private double[][] map;
	
	/*
	 * Gefahrenbewertung bewertet die Felder um den Ghost herum
	 * */
	public GefahrenbewertungRH(Vector2 pos, AnalyseRH bubbleRH) {
		this.position = pos;
		this.view = bubbleRH.getView();
		this.bubbleRH = bubbleRH;
		map = new double[view.length][view[0].length];
		map[position.x][position.y] = -1;
		bewerteGefahr(position.x, position.y, 0);
	}
	
	public void ghostPosAkt(Vector2 pos, AnalyseRH RHBubble) {
		if(!position.equals(pos)) {
			map[position.x][position.y] = 0;
			bubbleLoeschen();
			position = pos;
			map[position.x][position.y] = -1;
			RHBubble.setMDPMap(position.x, position.y, -1);
			bewerteGefahr(position.x, position.y, 0);
		}
	}
	
	//Bewertet die Felder (Radius: 2) um den Ghost
	public void bewerteGefahr(int x, int y, int steps) {
		if((steps < 2) && (view[x][y] != null) && (view[x][y] != PacmanTileType.WALL)) {
				
			if(map[x][y] != -1) {
				steps += 1;
				
				if(view[x][y] == PacmanTileType.DOT)
					map[x][y] = ( - (0.25 / (double) steps) ) - bubbleRH.dotWert;
				else
					map[x][y] = - (0.25 / (double) steps);
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
	
	//setzt die Felder in der Map zur�ck, wenn sich der Ghost ein Feld weiter bewegt
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
