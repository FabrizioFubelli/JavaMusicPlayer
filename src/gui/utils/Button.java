package gui.utils;

import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleRole;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static gui.utils.Button.ButtonType.PAUSE;
import static gui.utils.Button.ButtonType.PLAY;
import static ulg.utils.UtilFunctions.SCREEN_WIDTH;

/**
 * Created by: Fabrizio Fubelli
 * Date: 09/01/2017.
 */
public interface Button {
    /** Tipi di bottone */
    enum ButtonType {
        PREVIOUS,
        PLAY,
        PAUSE,
        NEXT,
        STOP
    }

    Set<ButtonImage> AnimatedButtonImages = Collections.synchronizedSet(new HashSet<ButtonImage>());
    AtomicBoolean AnimationThreadFree = new AtomicBoolean(true);
    ForkJoinPool AnimationPool = new ForkJoinPool();

    /**
     * Fa attendere al processo chiamante, che l'AnimationPool termini l'esecuzione
     */
    static void waitAnimationThread() {
        while (!AnimationThreadFree.get()) {
            synchronized (AnimationThreadFree) {
                try {
                    AnimationThreadFree.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Runnable AnimationRunnable = () -> {
        waitAnimationThread();
        AnimationThreadFree.set(false);
        final List<ButtonImage> aBI = new ArrayList<>(AnimatedButtonImages);
        try {
            boolean dispari = true;
            for (int i = 0; i < 2; i++) {
                for (ButtonImage buttonImage : aBI) {
                    double diff = 0 + buttonImage.size / (dispari ? -2.9 : 2.9);
                    double addDiff = diff / 30;
                    for (int add = 0; add < 30; add++) {
                        buttonImage.setTemporarySize(buttonImage.getScaleX() + addDiff);
                        Thread.sleep(2);
                    }
                }
                dispari = !dispari;
                Thread.sleep(80);
            }
        } catch (Exception e) { e.printStackTrace(); }
        aBI.forEach(buttonImage -> buttonImage.setTemporarySize(buttonImage.size + (buttonImage.size / 8)));
        AnimatedButtonImages.removeAll(aBI);
        AnimationThreadFree.set(true);
        synchronized (AnimationThreadFree) {
            AnimationThreadFree.notifyAll();
        }
    };

    /**
     * Classe che estende ImageView, rappresenta le immagini di pezzo nella GUI
     */
    class ButtonImage extends ImageView {

        private final AtomicInteger requestNumber = new AtomicInteger(0);
        private final ButtonType BUTTON_TYPE;
        private final double size;
        private final Image firstImage;
        private final Image secondImage;

        private volatile ButtonType actualImage;
        private volatile double tempSize;
        private volatile long lastTimeMouseExited;

        /**
         * Crea un bottone di un certo tipo
         *
         * @param buttonType il bottone raffigurato
         */
        ButtonImage(ButtonType buttonType) {
            this.setAccessibleRole(AccessibleRole.IMAGE_VIEW);
            this.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            this.firstImage = new Image(getClass().getResource(buttonType.name() + ".png").toString());
            if (buttonType == PLAY) this.secondImage = new Image(getClass().getResource("PAUSE.png").toString());
            else this.secondImage = null;

            this.setImage(this.firstImage);
            this.setFitHeight(this.firstImage.getHeight()/3);
            this.setFitWidth(this.firstImage.getWidth()/3);
            this.actualImage = buttonType;
            if (buttonType == PLAY) {
                this.setFitHeight(this.firstImage.getHeight() / 2);
                this.setFitWidth(this.firstImage.getWidth() / 2);
                this.size = SCREEN_WIDTH / 1500;
            } else if (buttonType == ButtonType.STOP) {
                this.setFitHeight(this.firstImage.getHeight() / 2.2);
                this.setFitHeight(this.firstImage.getHeight() / 2.2);
                this.size = SCREEN_WIDTH / 2100;
            } else {
                this.setFitHeight(this.firstImage.getHeight() / 2.5);
                this.setFitHeight(this.firstImage.getHeight() / 2.5);
                this.size = SCREEN_WIDTH / 2600;
            }
            this.tempSize = this.size;
            this.BUTTON_TYPE = buttonType;
            this.setTemporarySize(this.size);
            this.setOnMouseEntered(e -> this.mouseIn());
            this.setOnMouseExited(e -> this.mouseOutAndNormalSize());
        }

        public void mouseClick() {
            AnimatedButtonImages.add(this);
            AnimationPool.execute(AnimationRunnable);
        }

        synchronized long getLastTimeMouseExited() {
            return this.lastTimeMouseExited;
        }

        public void setPlayButton(boolean play) {
            if (this.BUTTON_TYPE != PLAY) return;
            if ((play && this.actualImage == PLAY) || !play && this.actualImage != PLAY) return;
            //final int thisRequestNumber = this.playRequestNumber.incrementAndGet();
            AnimationPool.submit(() -> {
                try {
                    //Thread.sleep(350);
                    //if (this.playRequestNumber.get() > thisRequestNumber) return;
                    double op = 1;
                    while (op > 0) {
                        this.setOpacity(op -= 0.05);
                        Thread.sleep(20);
                    }
                    this.setImage(play ? this.firstImage : this.secondImage);
                    this.actualImage = play ? PLAY : PAUSE;
                    while (op < 1) {
                        this.setOpacity(op += 0.05);
                        Thread.sleep(20);
                    }
                } catch (InterruptedException e) {
                    // skip
                }
            }).join();
        }

        private void mouseIn() {
            this.requestNumber.addAndGet(1);
            if (AnimatedButtonImages.contains(this)) return;
            this.setTemporarySize(this.size+(this.size/8));
        }

        private synchronized void mouseOutAndNormalSize() {
            lastTimeMouseExited = System.currentTimeMillis();
            final int request = this.requestNumber.get();
            if (AnimatedButtonImages.contains(this)) {
                AnimationPool.execute(() -> {
                    waitAnimationThread();
                    if (request < this.requestNumber.get()) return;
                    this.setTemporarySize(this.size);
                });
                return;
            }
            this.setTemporarySize(this.size);
        }

        /**
         * Scala l'immagine con la dimensione data
         * @param size la dimensione con cui scalare l'immagine
         */
        private void setTemporarySize(double size) {
            this.tempSize = size;
            this.setScaleX(size);
            this.setScaleY(size);
        }

        private synchronized double getTempSize() {
            return this.tempSize;
        }

        public void setButtonDisabled(boolean b) {
            //System.out.println(this.BUTTON_TYPE+" -> setButtonDisabled("+b+") called");
            this.setDisable(b);
            this.setOpacity(b ? 0.5 : 1);
            this.setTemporarySize(this.size);
        }

        /**
         * @return la dimensione usata per scalare il bottone
         */
        double getSize() {
            return this.size;
        }

        /**
         * @return il tipo di bottone
         */
        public synchronized ButtonType getButtonType() {
            return this.BUTTON_TYPE;
        }

        @Override
        public synchronized boolean equals(Object v) {
            if (!Objects.isNull(v) && v.getClass().equals(getClass())) {
                ButtonImage o = (ButtonImage) v;
                return this.BUTTON_TYPE == o.BUTTON_TYPE;
            }
            return false;
        }

        @Override
        public synchronized int hashCode() {
            return Objects.hashCode(this.BUTTON_TYPE);
        }
    }
}
