package analysieren;

import java.util.*;
import java.util.Map.Entry;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanTileType;
import exception.NoWayException;

public class Analyse {
	
	private double discount;
	public double dotWert = 0.5;
	private MDPKachel[][] mdpMap;
	private PacmanTileType[][] view;
	private Hashtable<Vector2, String> ghosttypes;
	private Gefahrenbewertung[] gefahren;
	private Vector2[] ghostPos;
	
	public void initalizedAnalyse(PacmanTileType[][] view, double discount, Hashtable<Vector2, String> ghosttypes) {
		this.view = view;
		this.mdpMap = initializedMDPMap();
		this.discount = discount;
		this.ghosttypes = ghosttypes;
		gefahren = new Gefahrenbewertung[ghosttypes.size()];					//Ghostbubble-Array wird angelegt
		ghostPos = new Vector2[ghosttypes.size()];								//Ghostpositionen-Array wird angelegt
		int i = 0;
		for(Entry<Vector2, String> e : ghosttypes.entrySet())					//Ghostpositionen-Array wird befüllt
		     ghostPos[i++] = e.getKey();
		gefahren[0] = new Gefahrenbewertung(ghostPos[0], this);					//Ghostbubble-Array wird befüllt
		gefahren[1] = new Gefahrenbewertung(ghostPos[1], this);
		befuelleMDP();															//erstmalige MDP-ähnliche Berechnung
	}

	/**
	 *	Das MDP-Feld bekommt zum ersten Mal (leere) MDPKacheln
	 */
	private MDPKachel[][] initializedMDPMap() {
		MDPKachel[][] map = new MDPKachel[view.length][view[0].length];
		
		for(int x = 0; x < view.length; x++)
			for(int y = 0; y < view[0].length; y++)
				map[x][y] = new MDPKachel();
		
		return map;
	}
	
	//Die MDP-Kacheln im MDP-Feld werden zum ersten Mal mit Werten gefüllt
	private void befuelleMDP() {
		for(int x = 0; x < view.length; x++)
			for(int y = 0; y < view[0].length; y++) {
				if(view[x][y] == PacmanTileType.DOT) {
					mdpMap[x][y].setErg((gefahren[0].getMap()[x][y] + gefahren[1].getMap()[x][y] + dotWert));
				} else {
					mdpMap[x][y].setErg((gefahren[0].getMap()[x][y] + gefahren[1].getMap()[x][y]));
				}
			}
	}
	
	//1) Sucht das erste Feld (von (1,1) aus) welches einen Dot enthält
	//2) Methodenaufruf zur MDP-Berechnung
	public void feldbewertung() throws NoWayException{
		//Step 1)
		Vector2 feld = null;
		boolean found = false;
		
		for(int x = 1; x < view.length - 1; x++) {
			for(int y = 1; y < view[0].length - 1; y++) {
				if(view[x][y] == PacmanTileType.DOT) {
					feld = new Vector2(x, y);
					found = true;
					break;
				}
			}
			
			if(found)
				break;
		}
		
		//Step 2)
		//max. MDP-Berechnungstiefe 100
		for (int i = 1; i <= 100; i++) {
			LinkedList<Vector2> closedList = new LinkedList<Vector2>();
			mdpBerechnen(feld, i, closedList);
		}
	}
	
	//Step 2)
	private void mdpBerechnen(Vector2 pos, int rekursionsTiefe, LinkedList<Vector2> closedList) throws NoWayException{
		int x = pos.x;
		int y = pos.y;
		boolean[] walls = new boolean[4];
		
		if(rekursionsTiefe > 0) {
			//Positionen werden in einer Closedlist zwischengespeichert, damit Felder nicht doppelt bewertet werden
			closedList.add(pos);										
			
			if (view[x - 1][y] == PacmanTileType.WALL) {
				mdpMap[x][y].setWest(0);
				walls[2] = true;
			} else {
				Vector2 newPos = new Vector2(x - 1, y);
				mdpMap[x][y].setWest((discount * (mdpMap[x - 1][y].getErg() + mdpMap[x - 1][y].getErgVorherigeRunde())));
				verhindertDoppelteFeldbewertung(newPos, rekursionsTiefe - 1, closedList);
			}

			if (view[x][y - 1] == PacmanTileType.WALL) {
				mdpMap[x][y].setNorth(0);
				walls[0] = true;
			} else {
				Vector2 newPos = new Vector2(x, y - 1);
				mdpMap[x][y].setNorth((discount * (mdpMap[x][y - 1].getErg() + mdpMap[x][y - 1].getErgVorherigeRunde())));
				verhindertDoppelteFeldbewertung(newPos, rekursionsTiefe - 1, closedList);
			}

			if (view[x][y + 1] == PacmanTileType.WALL) {
				mdpMap[x][y].setSouth(0);
				walls[1] = true;
			} else {
				Vector2 newPos = new Vector2(x, y + 1);
				mdpMap[x][y].setSouth((discount * (mdpMap[x][y + 1].getErg() + mdpMap[x][y + 1].getErgVorherigeRunde())));
				verhindertDoppelteFeldbewertung(newPos, rekursionsTiefe - 1, closedList);
			}
			
			
			if (view[x + 1][y] == PacmanTileType.WALL) {
				mdpMap[x][y].setEast(0);
				walls[3] = true;
			} else {
				Vector2 newPos = new Vector2(x + 1, y);
				mdpMap[x][y].setEast((discount * (mdpMap[x + 1][y].getErg() + mdpMap[x + 1][y].getErgVorherigeRunde())));
				verhindertDoppelteFeldbewertung(newPos, rekursionsTiefe - 1, closedList);
			}
			
			//Ergebnis = beste Himmelsrichtung (größter Wert)
			mdpMap[x][y].findErg(walls);
		}
	}
	
	//Prüft, ob die neue Position schon in der ClosedList vorhanden ist.
	private void verhindertDoppelteFeldbewertung(Vector2 newPos, int rekursionsTiefe, LinkedList<Vector2> closedList) throws NoWayException{
		if(!closedList.contains(newPos))
			mdpBerechnen(newPos, rekursionsTiefe, closedList);
	}
	
	public void MDPFeldAkt(PacmanTileType[][] view, Hashtable<Vector2, String> ghosts) {
		this.ghostsAkt(ghosts);
		
		//Merge der mdpMap mit den beiden Gefahrenbewertungsmaps
		for (int x = 1; x < view.length - 1; x++)
			for (int y = 1; y < view[0].length - 1; y++) {
				if(view[x][y] == PacmanTileType.DOT) {
					mdpMap[x][y].setErg((gefahren[0].getMap()[x][y] + gefahren[1].getMap()[x][y]) + dotWert);
				} else if(view[x][y] == PacmanTileType.GHOST_AND_DOT || view[x][y] == PacmanTileType.GHOST || view[x][y] == PacmanTileType.EMPTY || view[x][y] == PacmanTileType.PACMAN) {
					mdpMap[x][y].setErg(gefahren[0].getMap()[x][y] + gefahren[1].getMap()[x][y]);
				}
			}
		
		try {
			feldbewertung();
		} catch (NoWayException e) {
			e.printStackTrace();
		}
	}
	
	public PacmanAction MDPInterpretieren(Vector2 pos) {								//entgültige Aktion wird bestimmt
		return mdpMap[pos.x][pos.y].getAction();
	}

	public void ghostsAkt(Hashtable<Vector2, String> ghosts) {							//Ghostpositionen werden aktualisiert
		int i = 0;
		for(Entry<Vector2, String> e : ghosts.entrySet())									
		     ghostPos[i++] = e.getKey();
		
		if(checkGhostPos(ghostPos[0])) {												
			gefahren[0].ghostPosAkt(ghostPos[0]);
			gefahren[1].ghostPosAkt(ghostPos[1]);
		} else {
			gefahren[0].ghostPosAkt(ghostPos[1]);
			gefahren[1].ghostPosAkt(ghostPos[0]);
		}
	}
	
	private boolean checkGhostPos(Vector2 pos) {										//Vergleich: alte und neue Ghostposition
		Vector2 ghost1Pos = gefahren[0].getPosition();
		
		if( (pos.x >= ghost1Pos.x - 1) && (pos.x <= ghost1Pos.x + 1) )
			if( (pos.y >= ghost1Pos.y - 1) && (pos.y <= ghost1Pos.y + 1) )
				return true;
		
		return false;
	}
	
	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public MDPKachel[][] getMDPMap() {
		return mdpMap;
	}
	
	public void setMDPMap(int x, int y, double wert) {
		mdpMap[x][y].setErg(wert);
	}

	public PacmanTileType[][] getView() {
		return view;
	}

	public void setView(PacmanTileType[][] view) {
		this.view = view;
	}

	public Hashtable<Vector2, String> getGhosttypes() {
		return ghosttypes;
	}

	public void setGhosttypes(Hashtable<Vector2, String> ghosttypes) {
		this.ghosttypes = ghosttypes;
	}

	public Gefahrenbewertung[] getGefahren() {
		return gefahren;
	}

	public void setGefahren(Gefahrenbewertung[] gefahren) {
		this.gefahren = gefahren;
	}

	public Vector2[] getGhostPos() {
		return ghostPos;
	}

	public void setGhostPos(Vector2[] ghostPos) {
		this.ghostPos = ghostPos;
	}
}