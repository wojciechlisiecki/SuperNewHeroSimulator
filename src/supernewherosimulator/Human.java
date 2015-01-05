/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package supernewherosimulator;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;
import static supernewherosimulator.SuperNewHeroSimulator.paths;
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
    private Rectangle character;
    
    public Human(String name, int id, int locationX, int locationY, Intersection familyTown) {
        this.setName(name);
        this.setId(id);
        this.setLocationX(locationX);
        this.setLocationY(locationY);
        this.familyTown = familyTown;
        Platform.runLater(this);
    }
    
    public Place calculateStartPosition(Intersection currentIntersection, Intersection endIntersection) {
        int bound = currentIntersection.getBound()/2;
        
        int begX = currentIntersection.getX();
        int begY = currentIntersection.getY();
        int endX = endIntersection.getX();
        int endY = endIntersection.getY();
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
        
        int begX = currentIntersection.getX();
        int begY = currentIntersection.getY();
        int endX = endIntersection.getX();
        int endY = endIntersection.getY();
        int resX,resY;
        
        if(begX == endX) {
            if(begY - endY > 0) {                                                               //up
                resX = endX + 2*bound;
                resY = endY + 2*bound;
            } else {                                                                            //down
                resX = endX;
                resY = endY ;
            }
        } else {
            if((begX - endX > 0)) {                                                             //left
                resX = endX + 2*bound;
                resY = begY;
            } else {                                                                            //right
                resX = endX;
                resY = endY + 2*bound;
            }
        }    
        
        return(new Place(resX,resY));
    }

    public void moveBetween(Place start, Place end, double delay, double time) {
        Path characterPath = new Path();
        
        int rectangleBound = 5;
        
        Rectangle human = new Rectangle(start.getX() - rectangleBound, start.getY() - rectangleBound, 10, 10);
        human.setFill(Color.web("blue"));
        Node character = human;
        
        Label characterInfo = this.getCharacterInfo();
        
        character.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                Stage stage = new Stage();
                stage.setHeight(200);
                stage.setWidth(300);
                stage.setTitle("Character details");
                
                Group detailsRoot = new Group();
                Scene scene = new Scene(detailsRoot, 300, 200);
                stage.setScene(scene);
                
                detailsRoot.getChildren().add(characterInfo);
                
                Button stop = new Button();
                stop.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent buttonEvent) {
                        //something
                    }
                });
                
                detailsRoot.getChildren().add(stop);
                
                stage.show();
//                SuperNewHeroSimulator.characterLabels.getChildren().clear();
//                SuperNewHeroSimulator.characterLabels.getChildren().add(characterInfo);
                
            }
        });
        
        
        //character.setVisible(false);
        
        characterPath.getElements().add(new MoveTo(start.getX(), start.getY()));
        characterPath.getElements().add(new LineTo(end.getX(), end.getY()));
        
        SuperNewHeroSimulator.paths.getChildren().add(character);
        SuperNewHeroSimulator.paths.getChildren().add(characterPath);
        
        final PathTransition characterTransition = new PathTransition();
        
        characterTransition.setDuration(Duration.seconds(time));
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
        //Initialization
        Planet homeTown = (Planet)this.getFamilyTown();
        Planet toGo;
        double delay = 0.0;

        Intersection currentIntersection = this.familyTown;
        Intersection endIntersection;
        Place startPosition, endPosition;
        
        //drawing a character
        int rectangleBound = 5;        
        Rectangle human = new Rectangle(this.getLocationX() - rectangleBound, this.getLocationY() - rectangleBound, 10, 10);
        human.setFill(Color.web("blue"));
        Node character = human;
        SuperNewHeroSimulator.paths.getChildren().add(character);
             
        //calculating a path
        if(homeTown.getPopulation() != 0) {
            homeTown.decreasePopulation();
            
            int toGoId;
            do {
                toGoId = randInt(0, SuperNewHeroSimulator.numOfTowns - 1);
                toGo = (Planet) SuperNewHeroSimulator.inter[toGoId];
            } while (toGo == homeTown);

            ArrayList<Intersection> path = new ArrayList<>();
            path = SuperNewHeroSimulator.findPath(homeTown, toGo);
            
            Place currentHumanPosition = (Place) currentIntersection;

            path.remove(0);
            
            SequentialTransition seqTransition = new SequentialTransition(character);

            final Duration sec15 = Duration.millis(3000);
            final Duration sec30 = Duration.millis(1500);          

           //adding path sections to sequential trasition
            for(Intersection path1 : path) {             
                
                startPosition = calculateStartPosition(currentIntersection, path1);
                endPosition = calculateEndPosition(currentIntersection, path1); 
               
                TranslateTransition chTrans1 = new TranslateTransition(sec15);
                chTrans1.setByX(startPosition.getX() - currentHumanPosition.getX());
                chTrans1.setByY(startPosition.getY() - currentHumanPosition.getY());
                
                TranslateTransition chTrans2 = new TranslateTransition(sec30);
                chTrans2.setByX(endPosition.getX() - startPosition.getX());
                chTrans2.setByY(endPosition.getY() - startPosition.getY());
                
                seqTransition.getChildren().add(chTrans1);
                seqTransition.getChildren().add(chTrans2);
                

//                final KeyFrame frame = new KeyFrame(delay, "check",)
                
//                currentHumanPosition -> startPosition
//                startPosition -> endPosition
                
                currentIntersection = path1;
                currentHumanPosition = endPosition;
            }
            //if position == intersection, check if free to go, wait or go
            
            seqTransition.setInterpolator(Interpolator.LINEAR);
            System.out.println("Cue points:" + seqTransition.getCuePoints());
            seqTransition.play();

            ObservableBooleanValue colliding;
            for(Intersection inter : SuperNewHeroSimulator.inter) {        
                colliding = Bindings.createBooleanBinding(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        return character.getBoundsInParent().intersects(inter.getInterRectangle().getBoundsInParent());
                    }
                }, character.boundsInParentProperty(), inter.getInterRectangle().boundsInParentProperty());
                
                colliding.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if(newValue) {
                            //try {
                                //seqTransition.stop();
                                //inter.sem.acquire();
                                //seqTransition.play();
                                inter.getInterRectangle().setFill(Color.YELLOW);
                            //} catch (InterruptedException ex) {
                               // Logger.getLogger(Human.class.getName()).log(Level.SEVERE, null, ex);
                            //}
                        } else {
                            //inter.sem.release();
                            //seqTransition.play();
                            inter.getInterRectangle().setFill(Color.RED);
                        }
                    }
                });  
            }    
            
            //SuperNewHeroSimulator.checkCollision(character);
            
   
//            try {
//                currentIntersection.sem.acquire();
//            } catch (InterruptedException ex) {
//                Logger.getLogger(Human.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            
//            currentIntersection.sem.release();

        } else System.out.println("Brak mieszkańców");
    }    
    
//    private void checkCollision() {
//        boolean collision = false;
//        for(Intersection inter : SuperNewHeroSimulator.inter) {
//            //Shape intersect = Shape.intersect(character, inter.getInterRectangle());
//            if(character.getBoundsInParent().intersects(inter.getInterRectangle().getBoundsInParent())) {
//                collision = true;
//            }
//        }
//        if(collision) {
//            character.setFill(Color.YELLOW);
//        }
//    }
    
    public Label getCharacterInfo() {
        Label details = new Label();
        details.setWrapText(true);
        details.setText("name:" + this.name);
        return details;
    }
    
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