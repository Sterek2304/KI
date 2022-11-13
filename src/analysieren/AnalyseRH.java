package analysieren;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanTileType;
import exception.NoWayException;
import general.Knoten;
import mapping.Map;
import mapping.SackgassenKachel;

public class AnalyseRH {
	private Map map;
	private SackgassenKachel[][] sackgassen;
	private int sackgassenlaenge;
	private double sackgassenWert, entfernungZuGhost, entfernungGhostZuSackgasse;
	private Vector2 hunterPos, pacPos;
	private double discount, availableTurns;
	public double dotWert = 0.5;
	private MDPKachel[][] mdpMap;
	private PacmanTileType[][] view;
	private Hashtable<Vector2, String> ghosttypes;
	private GefahrenbewertungRH[] gefahren;
	private Vector2[] ghostPos;
	
	public void initalizedAnalyse(PacmanTileType[][] view, double discount, Hashtable<Vector2, String> ghosttypes, Vector2 pacman, double turns) {
		this.view = view;
		this.mdpMap = initializedMDPMap();
		this.discount = discount;
		this.availableTurns = turns;
		this.ghosttypes = ghosttypes;
		gefahren = new GefahrenbewertungRH[ghosttypes.size()];					//Ghost-Bubble-Array wird angelegt
		ghostPos = new Vector2[ghosttypes.size()];								//Ghost-Positionen-Array wird angelegt
		int i = 0;
		for(Entry<Vector2, String> e : ghosttypes.entrySet())					//Ghost-Positionen-Array wird befüllt
		     ghostPos[i++] = e.getKey();
		gefahren[0] = new GefahrenbewertungRH(ghostPos[0], this);				//Ghost-Bubble-Array wird befüllt
		gefahren[1] = new GefahrenbewertungRH(ghostPos[1], this);
		this.map = new Map(this.view);											//Map wird angelegt
		this.sackgassen = map.getSackgassen();									//Sackgassen aus der Map werden zwischengespeichert
		for (Entry<Vector2, String> e : this.ghosttypes.entrySet())				//Hunter-Positionen wird extra zwischengespeichert
			if (e.getValue().equals("ghost_hunter"))
				this.hunterPos = e.getKey();
		this.pacPos = pacman;													//Pacman-Positione wird extra zwischengespeichert (wegen Distanzberechnung)
		befuelleMDP();															//erstmalige MDP-ähnliche Berechnung
	}
	
	//Das MDP-Feld bekommt zum ersten Mal (leere) MDPKacheln
	private MDPKachel[][] initializedMDPMap() {
		MDPKachel[][] map = new MDPKachel[view.length][view[0].length];

		for (int x = 0; x < view.length; x++)
			for (int y = 0; y < view[0].length; y++)
				map[x][y] = new MDPKachel();

		return map;
	}

	// Die MDP-Kacheln im MDP-Feld werden zum ersten Mal mit Werten gefüllt
	private void befuelleMDP() {
		for (int x = 0; x < view.length; x++)
			for (int y = 0; y < view[0].length; y++) {
				if (view[x][y] == PacmanTileType.DOT) {
					mdpMap[x][y].setErg((gefahren[0].getMap()[x][y] + gefahren[1].getMap()[x][y] + dotWert));
				} else {
					mdpMap[x][y].setErg((gefahren[0].getMap()[x][y] + gefahren[1].getMap()[x][y]));
				}
			}
	}
	
	//Bewertet eine Sackgasse danach, ob es sich lohnt in die Sackgasse hinzugehen, je nachdem wie weit der Ghost von ihr entfernt ist
	public double SackgassenFormelRH(int x, int y) {
		entfernungGhostZuSackgasse = berechneEntfernungGhostZuSackgasse(hunterPos, sackgassen[x][y].getPos());
		sackgassenlaenge = sackgassen[x][y].getSchritte();
		sackgassenWert = availableTurns + (1 / entfernungGhostZuSackgasse - sackgassenlaenge + 1);
		return -sackgassenWert;
	}
	
	//A*-Suche: Sucht den schnellsten Weg vom Ghost zur Sackgasse und gibt die Kantenkosten (Entfernung) des Zielknotens zurück
	public int berechneEntfernungGhostZuSackgasse(Vector2 ghostPos, Vector2 sackgasse) {
		LinkedList<Knoten> openlist = new LinkedList<Knoten>();
        LinkedList<Knoten> closedlist = new LinkedList<Knoten>();
        Knoten first = new Knoten(null, view, sackgasse, ghostPos);
        
        openlist.add(first);
        
        while(!openlist.isEmpty()) {
        	Knoten tmp = openlist.remove(0);
        	if(tmp.isGoal(sackgasse)) {											//Ghost-Position == Sackgassen-Position
        		return tmp.getKantenkosten();
        	} else {
        		if(!closedlist.contains(tmp)) {
        			for(Knoten kind : tmp.expand()) {
        				kind.setKantenkosten(kind.kantenkostenberechnen());
        				kind.setHeuristikkosten(kind.heuristikberechnen());
        				double kantenUndHeuristik = kind.getHeuristikkosten() + kind.getKantenkosten();
        				
        				boolean geadded = false;
        				for(int i = 0; i < openlist.size(); i++) {
        					if(openlist.get(i).getKantenkosten() + openlist.get(i).getHeuristikkosten() > kantenUndHeuristik) {
        						openlist.add(i, kind);
        						geadded = true;
        						break;
        					}
        				}
        				
        				if(!geadded)
    						openlist.add(kind);
        			}
        			
        			closedlist.add(tmp);
        		}
        	}
        }
        
		return -1;
	}
	
	
	public double bewerteSackgassenRH(int x, int y) {
		//Feld muss ein SackgassenEntrance und nicht finished (sonst läuft er wieder in Sackgasse hinein) sein
		if(map.getSackgassenEntrance().contains(sackgassen[x][y]) && !sackgassen[x][y].isFinished()) {
			entfernungZuGhost = entfernungHunterPac(pacPos, hunterPos);

			if (((sackgassen[x][y].getSchritte() * 2) + 2) >= entfernungZuGhost && map.isSackgassenkachelInts(x, y))
				return SackgassenFormelRH(x, y);
		}
		
		return -1;
	}
	
	//A* Suche: Sucht den schnellsten Weg vom Ghost zum Pacman und gibt die Kantenkosten (Entfernung) des Zielknotens zurück
	private int entfernungHunterPac(Vector2 pacmanPos, Vector2 ghostPos) {
		LinkedList<Knoten> openlist = new LinkedList<Knoten>();
        LinkedList<Knoten> closedlist = new LinkedList<Knoten>();
        Knoten first = new Knoten(null, view, pacmanPos, ghostPos);
        
        openlist.add(first);
        
        while(!openlist.isEmpty()) {
        	Knoten tmp = openlist.remove(0);
        	if(tmp.isGoal()) {
        		return tmp.getKantenkosten();
        	} else {
        		if(!closedlist.contains(tmp)) {
        			for(Knoten kind : tmp.expand()) {
        				kind.setKantenkosten(kind.kantenkostenberechnen());
        				kind.setHeuristikkosten(kind.heuristikberechnen());
        				double kantenUndHeuristik = kind.getHeuristikkosten() + kind.getKantenkosten();
        				
        				boolean geadded = false;
        				for(int i = 0; i < openlist.size(); i++) {
        					if(openlist.get(i).getKantenkosten() + openlist.get(i).getHeuristikkosten() > kantenUndHeuristik) {
        						openlist.add(i, kind);
        						geadded = true;
        						break;
        					}
        				}
        				if(!geadded)
    						openlist.add(kind);
        			}
        			
        			closedlist.add(tmp);
        		}
        	}
        }
        
        return -1;
	}
	
	public void MDPFeldAkt(PacmanTileType[][] view, Hashtable<Vector2, String> ghosts) {
		this.ghostsAkt(ghosts);
		
		for (Entry<Vector2, String> e : ghosts.entrySet())
			if (e.getValue().equals("ghost_hunter"))
				this.hunterPos = e.getKey();
		
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
	
	//1) Sucht das erste Feld (von (1,1) aus) welches einen Dot enthält
	//2) Methodenaufruf zur MDP-Berechnung
	public void feldbewertung() throws NoWayException {
		//Step 1)
		Vector2 feld = null;
		boolean found = false;

		for (int x = 1; x < mdpMap.length - 1; x++) {
			for (int y = 1; y < mdpMap[0].length - 1; y++) {
				if (view[x][y] == PacmanTileType.DOT) {
					feld = new Vector2(x, y);
					found = true;
					break;
				}
			}

			if (found)
				break;
		}

		//Step 2)
		//max. MDP-Berechnungstiefe 100
		for (int i = 1; i <= 100; i++) {
			LinkedList<Vector2> closedList = new LinkedList<Vector2>();
			mdpBerechnen(feld, i, closedList);
		}
	}

	// Step 2)
	private void mdpBerechnen(Vector2 pos, int rekursionsTiefe, LinkedList<Vector2> closedList) throws NoWayException {
		int x = pos.x;
		int y = pos.y;
		
		boolean[] walls = new boolean[4];
 
		if (rekursionsTiefe > 0) {
			//Positionen werden in einer Closedlist zwischengespeichert, damit Felder nicht doppelt bewertet werden
			closedList.add(pos);

			if (view[x - 1][y] == PacmanTileType.WALL) {
				mdpMap[x][y].setWest(0);
				walls[2] = true;
			} else {
				Vector2 newPos = new Vector2(x - 1, y);
				mdpMap[x][y].setWest((discount * (mdpMap[x - 1][y].getErg() + mdpMap[x - 1][y].getErgVorherigeRunde())) + bewerteSackgassenRH(x, y));
				verhindertDoppelteFeldbewertung(newPos, rekursionsTiefe - 1, closedList);
			}

			if (view[x][y - 1] == PacmanTileType.WALL) {
				mdpMap[x][y].setNorth(0);
				walls[0] = true;
			} else {
				Vector2 newPos = new Vector2(x, y - 1);
				mdpMap[x][y].setNorth((discount * (mdpMap[x][y - 1].getErg() + mdpMap[x][y - 1].getErgVorherigeRunde())) + bewerteSackgassenRH(x, y));
				verhindertDoppelteFeldbewertung(newPos, rekursionsTiefe - 1, closedList);
			}

			if (view[x][y + 1] == PacmanTileType.WALL) {
				mdpMap[x][y].setSouth(0);
				walls[1] = true;
			} else {
				Vector2 newPos = new Vector2(x, y + 1);
				mdpMap[x][y].setSouth((discount * (mdpMap[x][y + 1].getErg() + mdpMap[x][y + 1].getErgVorherigeRunde())) + bewerteSackgassenRH(x, y));
				verhindertDoppelteFeldbewertung(newPos, rekursionsTiefe - 1, closedList);
			}

			if (view[x + 1][y] == PacmanTileType.WALL) {
				mdpMap[x][y].setEast(0);
				walls[3] = true;
			} else {
				Vector2 newPos = new Vector2(x + 1, y);
				mdpMap[x][y].setEast((discount * (mdpMap[x + 1][y].getErg() + mdpMap[x + 1][y].getErgVorherigeRunde())) + bewerteSackgassenRH(x, y));
				verhindertDoppelteFeldbewertung(newPos, rekursionsTiefe - 1, closedList);
			}
			
			//Ergebnis = beste Himmelsrichtung (größter Wert)
			mdpMap[x][y].findErg(walls);
		}
	}

	// Prüft, ob die neue Position schon in der ClosedList vorhanden ist.
	private void verhindertDoppelteFeldbewertung(Vector2 newPos, int rekursionsTiefe, LinkedList<Vector2> closedList)
			throws NoWayException {
		if (!closedList.contains(newPos))
			mdpBerechnen(newPos, rekursionsTiefe, closedList);
	}

	public void ghostsAkt(Hashtable<Vector2, String> ghosts) {							//Ghostpositionen werden aktualisiert
		int i = 0;
		for(Entry<Vector2, String> e : ghosts.entrySet())									
		     ghostPos[i++] = e.getKey();
		
		if(checkGhostPos(ghostPos[0])) {
			gefahren[0].ghostPosAkt(ghostPos[0], this);
			gefahren[1].ghostPosAkt(ghostPos[1], this);
		} else {
			gefahren[0].ghostPosAkt(ghostPos[1], this);
			gefahren[1].ghostPosAkt(ghostPos[0], this);
		}
	}
	
	private boolean checkGhostPos(Vector2 pos) {										//Vergleich: alte und neue Ghostposition
		Vector2 ghost1Pos = gefahren[0].getPosition();
		
		if( (pos.x >= ghost1Pos.x - 1) && (pos.x <= ghost1Pos.x + 1) )
			if( (pos.y >= ghost1Pos.y - 1) && (pos.y <= ghost1Pos.y + 1) )
				return true;
		
		return false;
	}
	
	//entgültige Aktion wird bestimmt
	public PacmanAction MDPInterpretieren(Vector2 pos) {	
		
		//Nutze die Actions der Sackgassenkacheln
		if(map.isSackgassenkachelInts(pos.x, pos.y) && map.getSackgassen()[pos.x][pos.y].getParent() != null && !map.getSackgassen()[pos.x][pos.y].betreten() && !map.getSackgassen()[pos.x][pos.y].isFinished()) {
			map.getSackgassen()[pos.x][pos.y].setBetreten();
			return sackgassenNavi(pos.x, pos.y);
		} else if(map.isSackgassenkachelInts(pos.x, pos.y) && map.getSackgassen()[pos.x][pos.y].getParent() == null && !map.getSackgassen()[pos.x][pos.y].betreten() && !map.getSackgassen()[pos.x][pos.y].isFinished()) {
			map.getSackgassen()[pos.x][pos.y].setBetreten();
		}
		
		if(map.isSackgassenkachelInts(pos.x, pos.y) && map.getSackgassen()[pos.x][pos.y].betreten()) {
			if(map.getSackgassenEntrance().contains(map.getSackgassen()[pos.x][pos.y]))
				map.getSackgassen()[pos.x][pos.y].setFinished();
		
			return map.getSackgassen()[pos.x][pos.y].getAction();
		}
		
		//ansonsten nutze die Actions der mdpMap
		return mdpMap[pos.x][pos.y].getAction();
	}
	
	//Weg in die Sackgasse wird durch Map erzwungen (Richung wird umgekehrt)
	public PacmanAction sackgassenNavi(int x, int y) {
		PacmanAction action = map.getSackgassen()[x][y].getParent().getAction();
		switch(action) {
			case GO_EAST:
				action = PacmanAction.GO_WEST;
				break;
			case GO_WEST:
				action = PacmanAction.GO_EAST;
				break;
			case GO_NORTH:
				action = PacmanAction.GO_SOUTH;
				break;
			case GO_SOUTH:
				action = PacmanAction.GO_NORTH;
				break;
			default:
					break;
		}
        return action;
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

	public GefahrenbewertungRH[] getGefahren() {
		return gefahren;
	}

	public void setGefahren(GefahrenbewertungRH[] gefahren) {
		this.gefahren = gefahren;
	}

	public Vector2[] getGhostPos() {
		return ghostPos;
	}

	public void setGhostPos(Vector2[] ghostPos) {
		this.ghostPos = ghostPos;
	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public SackgassenKachel[][] getSackgassen() {
		return sackgassen;
	}

	public void setSackgassen(SackgassenKachel[][] sackgassen) {
		this.sackgassen = sackgassen;
	}
}