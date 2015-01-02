/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package supernewherosimulator;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import static supernewherosimulator.SuperNewHeroSimulator.randInt;
/**
 *
 * @author Lisek
 */
public abstract class Human implements Runnable {
    
    private String name;
    private int id;
    private int locationX;
    private int locationY;
    private Intersection familyTown;
    private Intersection currentPosition;
    
    Human(String name, int id, int locationX, int locationY, Intersection familyTown) {
        this.setName(name);
        this.setId(id);
        this.setLocationX(locationX);
        this.setLocationY(locationY);
        this.familyTown = familyTown;
    }
    
    public Place calculateStartPosition(Intersection currentIntersection, Intersection endIntersection) {
        int bound = currentIntersection.getBound()/2;
        
        int begX = currentIntersection.getIntersectionX();
        int begY = currentIntersection.getIntersectionY();
        int endX = endIntersection.getIntersectionX();
        int endY = endIntersection.getIntersectionY();
        int resX,resY;
        
        if(begX == endX) {
            if(begY - endY > 0) {                                                               //up
                resX = begX + 2*bound;
                resY = begY;
            } else {                                                                            //down
                resX = begX;
                resY = begY + 2*bound;
            }
        } else {
            if((begX - endX > 0)) {                                                             //left
                resX = begX;
                resY = begY;
            } else {                                                                            //right
                resX = begX + 2*bound;
                resY = begY + 2*bound;
            }
        }    
        
        return(new Place(resX,resY));
    }
  
    public Place calculateEndPosition(Intersection currentIntersection, Intersection endIntersection) {
        int bound = currentIntersection.getBound()/2;
        
        int begX = currentIntersection.getIntersectionX();
        int begY = currentIntersection.getIntersectionY();
        int endX = endIntersection.getIntersectionX();
        int endY = endIntersection.getIntersectionY();
        int resX,resY;
        
        if(begX == endX) {
            if(begY - endY > 0) {                                                               //up
                resX = begX + 2*bound;
                resY = endY + 2*bound;
            } else {                                                                            //down
                resX = begX;
                resY = endY ;
            }
        } else {
            if((begX - endX > 0)) {                                                             //left
                resX = endX + 2*bound;
                resY = begY;
            } else {                                                                            //right
                resX = endX;
                resY = begY + 2*bound;
            }
        }    
        
        return(new Place(resX,resY));
    }
    
    public void moveBetween(Intersection end, double delay) {
        //Node character = this.drawHuman();
        int bound = end.getBound()/2;
        int rectangleBound = 5;
             
        Path characterPath = new Path();

        int x,y,xe,ye;
        
        
        if(this.getLocationX() == end.getIntersectionX()) {
            if((this.getLocationY() - end.getIntersectionY()) > 0) {        //up
                x = this.locationX + 2*bound;
                y = this.locationY;
                xe = x;
                ye = end.getIntersectionY() + 2*bound;
            } else {                                                        //down
                x = this.locationX;
                y = this.locationY + 2*bound;
                xe = x;
                ye = end.getIntersectionY();
            }
        } else {
            if((this.getLocationX() - end.getIntersectionX() > 0)) {        //left
                x = this.locationX;
                y = this.locationY;
                xe = end.getIntersectionX() + 2*bound;
                ye = y;
            } else {                                                        //right
                x = this.locationX + 2*bound;
                y = this.locationY + 2*bound;
                xe = end.getIntersectionX();
                ye = y;
            }
        }

        Rectangle human = new Rectangle(x - rectangleBound, y - rectangleBound, 10, 10);
        human.setFill(Color.web("blue"));
        Node character = human;
        
        characterPath.getElements().add(new MoveTo(x, y));
        //characterPath.getElements().add(new LineTo(x, y));
        characterPath.getElements().add(new LineTo(xe, ye));
        
        SuperNewHeroSimulator.paths.getChildren().add(character);
        SuperNewHeroSimulator.paths.getChildren().add(characterPath);
        
        final PathTransition characterTransition = new PathTransition();
        
        characterTransition.setDuration(Duration.seconds(1.0));
        characterTransition.setPath(characterPath);
        characterTransition.setNode(character);
        characterTransition.setDelay(Duration.seconds(delay));
        characterTransition.play(); 
        characterTransition.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                SuperNewHeroSimulator.paths.getChildren().remove(character);
            }
        });
        
        
    }
 
    public void run() {
        Planet homeTown = (Planet)this.getFamilyTown();
        Planet toGo;
        double delay = 0.0;
        
        Intersection currentIntersection = new Intersection();
        currentIntersection.setIntersection(this.familyTown.getIntersectionX(), this.familyTown.getIntersectionY());
        Intersection endIntersection;
        Place startPosition, endPosition;
        
        if(homeTown.getPopulation() != 0) {
            homeTown.decreasePopulation();
            
            int toGoId;
            do {
                toGoId = randInt(0, SuperNewHeroSimulator.numOfTowns - 1);
                toGo = (Planet) SuperNewHeroSimulator.inter[toGoId];
            } while (toGo == homeTown);

            ArrayList<Intersection> path = new ArrayList<>();
            path = SuperNewHeroSimulator.findPath(homeTown, toGo);

//            System.out.println("home town:");
//            homeTown.printIntersection();
//            System.out.println("to go:");
//            toGo.printIntersection();

            
            for (Intersection path1 : path) {
                path1.printIntersection();
                
                endIntersection = path1;
                
                startPosition = calculateStartPosition(currentIntersection, endIntersection);
                endPosition = calculateEndPosition(currentIntersection, endIntersection);
                
//                System.out.println("test koordynatów");
//                System.out.println(startPosition.getLocationX());
//                System.out.println(startPosition.getLocationY());
//                System.out.println(endPosition.getLocationX());
//                System.out.println(endPosition.getLocationX());
                
                this.moveBetween(currentIntersection, startPosition, delay);
                this.moveBetween(startPosition, endPosition, delay);
                
                currentIntersection = path1;
     
//                this.setLocationX(path1.getIntersectionX());
//                this.setLocationY(path1.getIntersectionY());
                delay += 1.0;
            }            
        } else System.out.println("Brak mieszkańców");
    }    
    
    
//    public void animate(ArrayList<Intersection> intersectionPath) {
//        Node character = this.drawHuman();    
//        Path characterPath = new Path();
//        
//        characterPath.getElements().add(new MoveTo(this.getLocationX(), this.getLocationY()));
//        
//        for(int i=0; i<intersectionPath.size(); i++) {
//            Intersection currentInter = intersectionPath.get(i);
//            characterPath.getElements().add(new LineTo(currentInter.getIntersectionX(), currentInter.getIntersectionY()));
//        }
//
//        SuperNewHeroSimulator.paths.getChildren().add(character);
//        SuperNewHeroSimulator.paths.getChildren().add(characterPath);
//        
//        final PathTransition characterTransition = new PathTransition();
//        
//        characterTransition.setDuration(Duration.seconds(5.0));
//        //characterTransition.setDelay(Duration.seconds(0.5));
//        characterTransition.setPath(characterPath);
//        characterTransition.setNode(character);
//        characterTransition.setCycleCount(1);
//        characterTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
//        characterTransition.setAutoReverse(false);
//        characterTransition.getInterpolator();
//        characterTransition.play();
//    }
    
//    public void run() {
//        Intersection homeTown = this.getFamilyTown();
//        Intersection toGo;
//        int toGoId;
//        do {
//            toGoId = randInt(0, SuperNewHeroSimulator.numOfTowns - 1);
//            toGo = SuperNewHeroSimulator.inter[toGoId];
//        } while (toGo == homeTown);
//        
////        this.locationX = SuperNewHeroSimulator.inter[homeTown].getIntersectionX();
////        this.locationY = SuperNewHeroSimulator.inter[homeTown].getIntersectionY();
//        ArrayList<Intersection> path = new ArrayList<>();
//        path = SuperNewHeroSimulator.findPath(homeTown, toGo);
//        System.out.println("home town:");
//        homeTown.printIntersection();
//        System.out.println("to go:");
//        toGo.printIntersection();
//        for (Intersection path1 : path) {
//            path1.printIntersection();
//        }
//        System.err.println("----");
//        this.animate(path);
//        System.out.println("Jestę wątkię");
//    }
    

    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the locationX
     */
    public int getLocationX() {
        return locationX;
    }

    /**
     * @param locationX the locationX to set
     */
    public void setLocationX(int locationX) {
        this.locationX = locationX;
    }

    public void increaseX(int change) {
        this.locationX += change;
    }
    
    /**
     * @return the locationY
     */
    public int getLocationY() {
        return locationY;
    }

    /**
     * @param locationY the locationY to set
     */
    public void setLocationY(int locationY) {
        this.locationY = locationY;
    }
    
    /**
     * @return the familyTown
     */
    public Intersection getFamilyTown() {
        return familyTown;
    }

    /**
     * @param familyTown the familyTown to set
     */
    public void setFamilyTown(Intersection familyTown) {
        this.familyTown = familyTown;
    }
    
    public Rectangle drawHuman() {
        Rectangle human = new Rectangle(this.locationX, this.locationY, 10, 10);
        human.setFill(Color.web("blue"));
        return human;
    }
}