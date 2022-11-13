package exception;

public class NoWayException extends Exception{

	public NoWayException() {
		super("keine nextAction gefunden, Problem in Klasse MDPKachel, Methode findErg(), switchcase, default");
	}
	
	
}
