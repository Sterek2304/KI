package general;

import java.util.Hashtable;
import java.util.Map.Entry;

import analysieren.Analyse;
import analysieren.AnalyseRH;
import analysieren.MDPKachel;
import de.fh.kiServer.agents.Agent;
import de.fh.kiServer.util.Vector2;
import de.fh.pacman.PacmanAgent_2021;
import de.fh.pacman.PacmanGameResult;
import de.fh.pacman.PacmanPercept;
import de.fh.pacman.PacmanStartInfo;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanActionEffect;
import de.fh.pacman.enums.PacmanTileType;

public class MyAgent_P3 extends PacmanAgent_2021 {

	/**
	 * Die aktuelle Wahrnehmung der Spielwelt
	 */
	private PacmanPercept percept;
	/**
	 * Die empfangene Rückmeldung des Servers auf die zuletzt ausgeführte Aktion
	 */
	private PacmanActionEffect actionEffect;
	/**
	 * Das aktuell wahrgenommene 2D Array der Spielwelt
	 */
	private PacmanTileType[][] view;
	
	/**
	 * Der gefundene Lösungknoten der Suche
	 */
	private PacmanAction nextAction;
	private boolean isRH;
	private Analyse analyse;
	private AnalyseRH analyseRH;
	//private MDPKachel[][] mdpMap, mdpMapRH;
	
	public MyAgent_P3(String name) {
		super(name);
		analyse = new Analyse();
		analyseRH = new AnalyseRH();
	}
	
	public static void main(String[] args) {
		MyAgent_P3 agent = new MyAgent_P3("MyAgent");
		Agent.start(agent, "127.0.0.1", 5000);
	}

	@Override
	public PacmanAction action(PacmanPercept percept, PacmanActionEffect actionEffect) {
		/*
		 * Aktuelle Wahrnehmung des Agenten, bspw. Position der Geister und Zustand aller Felder der Welt.
		 */
		this.percept = percept;
		
		/*
         * Aktuelle Rückmeldung des Server auf die letzte übermittelte Aktion.
         * 
         * Alle möglichen Serverrückmeldungen:
         * PacmanActionEffect.GAME_INITIALIZED
         * PacmanActionEffect.GAME_OVER
         * PacmanActionEffect.BUMPED_INTO_WALL
         * PacmanActionEffect.MOVEMENT_SUCCESSFUL
         * PacmanActionEffect.DOT_EATEN
		 */
		this.actionEffect = actionEffect;
		
		/*
         * percept.getView() enthält die aktuelle Felderbelegung in einem 2D Array
		 * 
         * Folgende Felderbelegungen sind möglich:
		 * PacmanTileType.WALL;
         * PacmanTileType.DOT
         * PacmanTileType.EMPTY
         * PacmanTileType.PACMAN
         * PacmanTileType.GHOST
         * PacmanTileType.GHOST_AND_DOT
		 */
		this.view = percept.getView();
	
		/*
		 * Die möglichen zurückzugebenden PacmanActions sind:
		 * PacmanAction.GO_EAST
		 * PacmanAction.GO_NORTH
		 * PacmanAction.GO_SOUTH
		 * PacmanAction.GO_WEST
		 * PacmanAction.QUIT_GAME
		 * PacmanAction.WAIT
		 */
		
		Hashtable<Vector2, String> ghosttypes = percept.getGhostTypes();			//Liste mit Ghosttypen und dessen Positionen
		Vector2 position = percept.getPosition();									//Pacman Position
		
		if(actionEffect == PacmanActionEffect.GAME_INITIALIZED) {
			//Ghostsituation bestimmen
			for (Entry<Vector2, String> e : percept.getGhostTypes().entrySet())
				if (e.getValue().equals("ghost_hunter"))
					isRH = true;
			
			if(!isRH) {
				analyse.initalizedAnalyse(view, 0.8, ghosttypes);									//Das Analyse-Objekt wird initialisiert
			} else {
				analyseRH.initalizedAnalyse(view, 0.8, ghosttypes, position, percept.getTurn());	//Das Analyse-Objekt wird initialisiert
			}
		}
		
		if (!isRH) {
			analyse.MDPFeldAkt(view, ghosttypes);									//MDP-Feld wird aktualisiert
			nextAction = analyse.MDPInterpretieren(position);						//Neue Bewegung für den Pacman setzen
		} else {
			analyseRH.MDPFeldAkt(view, ghosttypes);									//MDP-Feld wird aktualisiert
			nextAction = analyseRH.MDPInterpretieren(position);						//Neue Bewegung für den Pacman setzen
		}

		return nextAction;
	}

	@Override
	protected void onGameStart(PacmanStartInfo startInfo) {
		
	}

	@Override
	protected void onGameover(PacmanGameResult gameResult) {
		
	}
	
}
