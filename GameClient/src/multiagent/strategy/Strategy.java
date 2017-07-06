package multiagent.strategy;

import multiagent.remote.IStrategy;
import gameclient.AgentUtils;

import java.util.ArrayList;
import java.awt.List;
import java.awt.Point;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import multiagent.remote.IAgent;

public class Strategy extends UnicastRemoteObject implements IStrategy, Serializable {
	
	boolean presented;

    //private Field[][] rememberField;
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    
    public Strategy() throws RemoteException {
        super();
        System.out.println("Strategie Remember gestartet");
        presented = false;
    }

    @Override
    public void nextAction(IAgent agent) throws RemoteException {
    	if (!presented) {
            System.out.println("Hallo, ich bin " + agent.getName());
            presented = true;
    	}
    	
        agent.take(); //Default Aktion
                
        agent.setCustomData(0, 1, 2);
		
        if (agent.getLoad() >= agent.getCapacity()){
        	System.out.println("Ich gehe nach Hause.");
        	int home = agent.getHomeXY();
        	if ((agent.getPosx() > home+1) && (agent.requestField(AgentUtils.LEFT))){
        		agent.go(AgentUtils.LEFT);
        	} else if ((agent.getPosx() < home-1) && (agent.requestField(AgentUtils.RIGHT))){
        		agent.go(AgentUtils.RIGHT);
        	} else if ((agent.getPosy() > home+1) && (agent.requestField(AgentUtils.TOP))){
        		agent.go(AgentUtils.TOP);
        	} else if ((agent.getPosy() < home-1) && (agent.requestField(AgentUtils.BOTTOM))){
        		agent.go(AgentUtils.BOTTOM);
        	} else if (agent.checkIfOnSpawn()){
        		agent.put();
        	} else {
        		System.out.println("Ich will nach Hause, komme nicht durch und laufe deshalb blöd herum.");
            	//zufällige Richtung festlegen
            	String direction = getRandomDirection(agent);
            	agent.go(direction);
        	}
        	System.out.println("Ressourcen nach Aufnehmen: " + agent.getRememberResources(agent.getPosx(), agent.getPosy()) );
        	//aktuelle Position mit Anzahl an Ressourcen in Spielfeld eintragen
        	agent.setRememberResources(agent.check());
        } else if (agent.check() > 0) {
        	//einsammeln
            agent.take();
        	//aktuelle Position mit Anzahl an Ressourcen in Spielfeld eintragen (1 einzusammelnde Ressource bereits abziehen)
            System.out.println("Ressourcen davor: " + agent.getRememberResources(agent.getPosx(), agent.getPosy()) );
            agent.setRememberResources(agent.check() - 1);
        	System.out.println("Agent.check: " + agent.check() );
        	System.out.println("Ressourcen danach: " + agent.getRememberResources(agent.getPosx(), agent.getPosy()) );
        } else if (agent.getLoad() > 0 && agent.checkIfOnSpawn()) {
        	//Agent hat Ressourcen geladen (Kapazitätsgrenze aber noch nicht erreicht)
        	//und befindet sich auf Spawn-Feld --> abladen
        	agent.put();
        } else if(agent.buyPossible()){
        	agent.buy();
        	return;
        } else {
        	//aktuelle Position mit Anzahl an Ressourcen in Spielfeld eintragen
        	agent.setRememberResources(agent.check());
        	//aktuelle Position = 0 Ressourcen
        	
        	// nächste Mine nach gemerktem Spielfeld bestimmen
            Point goal = getNearestGoal(agent, agent.getCapacity() - agent.getLoad());
            
            String direction = "";
            
            if (goal.x != -1 && goal.y != -1) {
            	System.out.println("Ich gehe zu: (" + goal.x + "|" + goal.y + ")");
            	
            	//Richtung anhand des Ziels festlegen
            	if (goal.x < agent.getPosx()) {
            		if (agent.requestField(AgentUtils.LEFT))
            			direction = AgentUtils.LEFT;
            	} else if (goal.x > agent.getPosx()) {
            		if (agent.requestField(AgentUtils.RIGHT))
            			direction = AgentUtils.RIGHT;
            	}
            	
            	if (goal.y < agent.getPosy()) {
            		if (agent.requestField(AgentUtils.TOP))
            			direction = AgentUtils.TOP;
            	} else if (goal.y > agent.getPosy()) {
            		if (agent.requestField(AgentUtils.BOTTOM))
            			direction = AgentUtils.BOTTOM;
            	} 
            	
            	if (direction == "" || !agent.requestField(direction)){
                	System.out.println("Ich komme nicht durch und laufe deshalb blöd herum.");
                	//zufällige Richtung festlegen
                	direction = getRandomDirection(agent);
            	}
            } else {
            	System.out.println("Ich laufe blöd herum.");
            	//zufällige Richtung festlegen
            	direction = getRandomDirection(agent);
            }
                    	
        	if (direction != "")
        		agent.go(direction);
            
        }
        
        
    }
    
    public Point getNearestGoal(IAgent agent, int minRessources) {   	
    	int xPos = agent.getPosx();
    	int yPos = agent.getPosy();
    	
    	int distance = 0;
    	
    	while (distance < agent.getRememberFieldSize()) {
	    	for (int i = xPos - distance; i <= xPos + distance; i++) {
    			if (i >= 0 && i < agent.getRememberFieldSize()) {
		    		for (int j = yPos - distance; j <= yPos + distance; j++) {
		    			if (j >= 0 && j < agent.getRememberFieldSize()) {
		    				if (i == xPos - distance || i == xPos + distance || j == yPos - distance || j == yPos + distance) {
								if (agent.getRememberResources(i, j) >= minRessources) {
					            	System.out.println("Ressources: (" + i + "|" + j + "): " + agent.getRememberResources(i, j) + " - min: " + minRessources);							
									return new Point(i, j);
								}
			    			}
		    			}
		    			else {
		    				continue;
		    			}
					}
    			}
    			else {
    				continue;
    			}
			}
	    	distance += 1;
    	}
        
    	return new Point(-1, -1);    	
    }

    public String getRandomDirection(IAgent agent) {
    	int direction = (int)(Math.random() * 4);

    	String goTo = "";
    	int counter = 0;
    	    	
    	while (goTo == "" && counter < 4) {
    		switch (direction){
        	case 0:
        		if (agent.requestField(AgentUtils.LEFT)) {
                    goTo = AgentUtils.LEFT;
                } else {
                	direction += 1;
                }
        		break;
        	case 1:
        		if (agent.requestField(AgentUtils.TOP)) {
        			goTo = AgentUtils.TOP;
                } else {
                	direction += 1;
                }
        		break;
        	case 2:
        		if (agent.requestField(AgentUtils.RIGHT)) {
        			goTo = AgentUtils.RIGHT;
                } else {
                	direction += 1;
                }
        		break;
        	case 3:
        		if (agent.requestField(AgentUtils.BOTTOM)) {
        			goTo = AgentUtils.BOTTOM;
        		} else {
                	direction += 1;
                }
        		break;
        	default:
        		return AgentUtils.BOTTOM;
        	}
    		direction = direction % 4;
    		counter += 1;
    	}
    	
    	if (goTo == "") //eingekesselt --> Richtung egal, Agent kann nichts tun
    		goTo = AgentUtils.BOTTOM;
    	
    	    	
    	return goTo;
    }

}
