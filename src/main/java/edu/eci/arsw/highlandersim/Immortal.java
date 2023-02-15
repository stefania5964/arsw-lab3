
package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;



public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private AtomicInteger health;

    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean espera = false;

    private int posi;

    private boolean isDead = false;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, int n) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = new AtomicInteger(health);
        this.defaultDamageValue = defaultDamageValue;
        this.posi = n;
    }

    public void run() {

        while (!isDead) {
            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            this.fight(im);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (espera) {
                synchronized (im) {
                    try {
                        im.wait();
                        espera = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    public void fight(Immortal i2) {
        Immortal i1 = this;
        if (i1.getPosi() > i2.getPosi()) {
            Immortal temp = i1;
            i1 = i2;
            i2 = temp;
        }
        if (isDead()) {
            this.kill();
            immortalsPopulation.remove(this);
        }
        synchronized (i1) {
            synchronized (i2) {
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    health.addAndGet(defaultDamageValue);

                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                } else {
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                }
            }
        }

    }

    public void espera() {
        espera = true;
    }

    public synchronized void resumes() {
        espera = false;
        notifyAll();
    }

    public boolean isDead() {
        return isDead;
    }

    public void kill(){isDead=true;}

    public void changeHealth(int v) {
        health.set(v);
        if (health.get() == 0) {
            isDead = true;
        }
    }

    public int getHealth() {
        return health.get();
    }

    public int getPosi() {
        return posi;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
