package controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import model.Ball;
import model.DataBank;
import view.animaitons.BallTransition;
import view.animaitons.BallsMovement;

import java.util.Map;


public class GameController {

    private static int remainedBalls = DataBank.getNumberOfBalls();
    private static boolean isFreeze = false;
    private static int angleForRotation = 360;
    private static int ballsForFreeze = 0;
    private static boolean hasPhaseTwoStarted = false;
    private static boolean hasPhaseThreeStarted = false;
    private static boolean hasPhaseFourStarted = false;
    private static Timeline sizeTimeLine;
    private static Timeline visibilityTimeLine;
    private static int counterForReverse = 0;
    private static int angleOfBall = 0;
    private static int deltaAngle = 1;
    private static Timeline angleTimeLine;

    public void prepareMap(Pane pane) {
        pane.getChildren().add(DataBank.getCurrentMap().getMainCircle());
        for (java.util.Map.Entry<Ball, Line> entry : DataBank.getCurrentMap().getCircles().entrySet()) {
            pane.getChildren().addAll(entry.getKey(), entry.getValue());
        }
        for (int i = 0; i < 9; i++) {
            pane.getChildren().add(DataBank.getCurrentMap().getBalls().get(i));
        }
        //pane.getChildren().add(DataBank.getCurrentMap().getText());
        DataBank.getCurrentMap().setText(DataBank.getNumberOfBalls());
    }

    public EventHandler<KeyEvent> getEventHandler(Pane pane, ProgressBar progressBar, Text angle) {
        EventHandler<KeyEvent> event = keyEvent -> {
            String keyName = keyEvent.getCode().getName();
            if (keyName.equals("Tab") && ballsForFreeze == 5) {
                ballsForFreeze = 0;
                progressBar.setProgress(0);
                isFreeze = true;
                getRotationTimeline().play();
                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(DataBank.getFreezeTimer()),
                        actionEvent -> returnToNormal()));
                timeline.setCycleCount(1);
                timeline.play();
            } else if (keyName.equals("Space")) {
                new BallTransition(DataBank.getCurrentMap().getBalls().get(0), pane, Math.toRadians(angleOfBall)).play();
                remainedBalls -= 1;
                DataBank.getCurrentMap().setText(remainedBalls);
                ballsForFreeze += 1;
                ballsForFreeze = Math.min(5, ballsForFreeze);
                progressBar.setProgress(ballsForFreeze * 1.0 / 5);
                DataBank.getCurrentMap().getBalls().remove(DataBank.getCurrentMap().getBalls().get(0));
                if (DataBank.getCurrentMap().getBalls().size() >= 9)
                    pane.getChildren().add(DataBank.getCurrentMap().getBalls().get(8));
                if(remainedBalls!=0)
                    new BallsMovement(DataBank.getCurrentMap().getBalls()).play();
                if (remainedBalls <= DataBank.getNumberOfBalls() * 0.75 && !hasPhaseTwoStarted) {
                    changeToPhaseTwo(pane);
                    hasPhaseTwoStarted = true;
                }
                if (remainedBalls <= DataBank.getNumberOfBalls() * 0.5 && !hasPhaseThreeStarted) {
                    changeToPhaseThree();
                    hasPhaseThreeStarted = true;
                }
                if (remainedBalls <= DataBank.getNumberOfBalls() * 0.25 && !hasPhaseFourStarted) {
                    changeToPhaseFour(angle);
                    hasPhaseFourStarted = true;
                }
            } else if (keyName.equals("Right") && hasPhaseFourStarted) {
                moveRight();
            } else if (keyName.equals("Left") && hasPhaseFourStarted) {
                moveLeft();
            }
        };
        return event;
    }


    public void moveLeft() {
        if (DataBank.getCurrentMap().getBalls().get(0).getCenterX() > 15)
            DataBank.getCurrentMap().getBalls().get(0).setCenterX(DataBank.getCurrentMap().getBalls().get(0).getCenterX() - 15);
    }

    public void moveRight() {
        if (DataBank.getCurrentMap().getBalls().get(0).getCenterX() < 385)
            DataBank.getCurrentMap().getBalls().get(0).setCenterX(DataBank.getCurrentMap().getBalls().get(0).getCenterX() + 15);
    }

    private void changeToPhaseTwo(Pane pane) {
        Timeline sizeTimeLine = new Timeline(new KeyFrame(Duration.seconds(1),
                actionEvent -> changeSizeOfBalls(pane)));
        sizeTimeLine.setCycleCount(-1);
        sizeTimeLine.play();
        GameController.sizeTimeLine = sizeTimeLine;
    }

    private void changeToPhaseThree() {
        Timeline visibilityTimeLine = new Timeline(new KeyFrame(Duration.seconds(1),
                actionEvent -> changeVisibility()));
        visibilityTimeLine.setCycleCount(-1);
        visibilityTimeLine.play();
        GameController.visibilityTimeLine = visibilityTimeLine;
    }

    private void changeToPhaseFour(Text angle) {
        Timeline angleTimeline = new Timeline(new KeyFrame(Duration.millis(500 / 1.5 * DataBank.getWindSpeed()),
                actionEvent -> changeAngle(angle)));
        angleTimeline.setCycleCount(-1);
        GameController.angleTimeLine = angleTimeline;
        angleTimeline.play();
    }

    private void changeAngle(Text angle) {
        angleOfBall += deltaAngle;
        if (angleOfBall >= 15 && deltaAngle > 0) {
            deltaAngle *= -1;
        }
        if (angleOfBall <= -15 && deltaAngle < 0) {
            deltaAngle *= -1;
        }
        angle.setText("angle: " + angleOfBall);
    }

    public static int getRemainedBalls() {
        return remainedBalls;
    }

    private void changeVisibility() {
        for (Map.Entry<Ball, Line> entry : DataBank.getCurrentMap().getCircles().entrySet()) {
            if (entry.getKey().isVisible()) {
                entry.getKey().setVisible(false);
                entry.getValue().setVisible(false);
            } else {
                entry.getValue().setVisible(true);
                entry.getKey().setVisible(true);
            }
        }
    }

    private void changeSizeOfBalls(Pane pane) {
        counterForReverse++;
        for (Map.Entry<Ball, Line> ballLineEntry : DataBank.getCurrentMap().getCircles().entrySet()) {
            if (ballLineEntry.getKey().getRadius() >= 7.9 && ballLineEntry.getKey().getRadius() <= 8.1)
                ballLineEntry.getKey().setRadius(ballLineEntry.getKey().getRadius() * 1.15);
            else
                ballLineEntry.getKey().setRadius(8);
        }
        if (isCollisionHappened()) {
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1),
                    actionEvent -> afterCollision(pane)));
            timeline.setCycleCount(1);
            timeline.play();
        }
        if (counterForReverse >= 4) {
            int random_int = (int) Math.floor(Math.random() * 2 + 1);
            if (random_int == 2) {
                counterForReverse = 0;
                angleForRotation *= -1;
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1),
                        actionEvent -> getRotationTimeline().play()));
                timeline.setCycleCount(1);
                timeline.play();
            }
        }
    }


    public static Timeline getRotationTimeline() {
        double duration = 20 * 1.0 / DataBank.getRotationSpeed();
        if (isFreeze)
            duration *= 3;
        Rotate rotation = new Rotate();
        rotation.setPivotX(DataBank.getCurrentMap().getCenterX());
        rotation.setPivotY(DataBank.getCurrentMap().getCenterY());
        DataBank.getCurrentMap().getMainCircle().getTransforms().add(rotation);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rotation.angleProperty(), 0)),
                new KeyFrame(Duration.seconds(duration),
                        new KeyValue(rotation.angleProperty(), angleForRotation)));
        timeline.setCycleCount(-1);
        for (java.util.Map.Entry<Ball, Line> circleLineEntry : DataBank.getCurrentMap().getCircles().entrySet()) {
            if (circleLineEntry.getKey().getTimeline() != null)
                circleLineEntry.getKey().getTimeline().stop();
            circleLineEntry.getKey().getTransforms().add(rotation);
            circleLineEntry.getValue().getTransforms().add(rotation);
            circleLineEntry.getKey().setTimeline(timeline);
            circleLineEntry.getKey().setRotation(rotation);
        }
        return timeline;
    }

    private void returnToNormal() {
        isFreeze = false;
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1),
                actionEvent -> getRotationTimeline().play()));
        timeline.setCycleCount(1);
        timeline.play();
    }

    private boolean isCollisionHappened() {
        for (Map.Entry<Ball, Line> entry1 : DataBank.getCurrentMap().getCircles().entrySet()) {
            for (Map.Entry<Ball, Line> entry2 : DataBank.getCurrentMap().getCircles().entrySet()) {
                if (entry1.getKey().getBoundsInParent().intersects(entry2.getKey().getBoundsInParent())
                        && !entry1.getKey().equals(entry2.getKey())) {//todo fix bugs of intersection
                   /* System.out.println(entry1.getKey().getCenterX() + " " + entry1.getKey().getCenterY());
                    System.out.println(entry2.getKey().getCenterX() + " " + entry2.getKey().getCenterY());
                    entry1.getKey().setFill(new Color(0,1,0,1));
                    entry2.getKey().setFill(new Color(0,0,1,1));*/
                    return true;
                }
            }
        }
        return false;
    }

    private static void pauseTimeLines() {
        if (sizeTimeLine != null)
            sizeTimeLine.stop();
        for (Map.Entry<Ball, Line> entry : DataBank.getCurrentMap().getCircles().entrySet()) {
            entry.getKey().getTimeline().stop();
            if (!entry.getKey().isVisible()) {
                entry.getKey().setVisible(true);
                entry.getValue().setVisible(true);
            }
        }
        if (visibilityTimeLine != null)
            visibilityTimeLine.stop();
        if (angleTimeLine != null) {
            angleTimeLine.stop();
        }
    }

    public static void afterCollision(Pane pane) {
        BackgroundFill backgroundFill = new BackgroundFill(Color.color(1, 0, 0), CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        pane.setBackground(background);
        pauseTimeLines();
    }

    public static void afterWin(Pane pane) {
        BackgroundFill backgroundFill = new BackgroundFill(Color.color(0.12, 0.55, 0.184), CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        pane.setBackground(background);
        pauseTimeLines();
    }

}
