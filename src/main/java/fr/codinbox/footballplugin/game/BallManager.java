package fr.codinbox.footballplugin.game;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.configuration.FootConfig;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class BallManager {

    private ArrayList<Ball> balls;

    private Thread ballParticleThread;
    private int ballParticleTask;

    public BallManager() {
        this.balls = new ArrayList<>();
        this.ballParticleThread = new Thread(() ->  {
            ballParticleTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(FootballPlugin.INSTANCE, () -> {
                balls.forEach(ball -> {
                    ball.runPhysics();
                    ball.particle();
                });
            }, 0, FootConfig.BALL_PHYSICS_TICK);
        });
        this.ballParticleThread.start();
    }

    public Ball spawnBall(Location location) {
        Ball ball = new Ball(location, new ItemBuilder(Material.PLAYER_HEAD, 1, (byte) 3).setName(UUID.randomUUID().toString()).setSkullOwner(FootConfig.BALL_PLAYER_NAME).toItemStack());
        addBall(ball);
        return ball;
    }

    protected void addBall(Ball ball) {
        this.balls.add(ball);
    }

    public Ball getBallByEntity(Entity entity) {
        if(!(entity instanceof Item))
            return null;
        Item dropItem = (Item) entity;
        for(Ball ball : balls)
            if(ball.getBallItem().equals(dropItem))
                return ball;
        return null;
    }

    public boolean isBall(Entity entity) {
        return getBallByEntity(entity) != null;
    }

    public ArrayList<Ball> getNearbyBalls(Location location) {
        ArrayList<Ball> balls = new ArrayList<>();
        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, FootConfig.BALL_REACH_DISTANCE, FootConfig.BALL_REACH_DISTANCE, FootConfig.BALL_REACH_DISTANCE);
        for(Entity entity : nearbyEntities) {
            Ball ball = getBallByEntity(entity);
            if(ball != null)
                balls.add(ball);
        }
        return balls;
    }

    public void exit() {
        Bukkit.getScheduler().cancelTask(this.ballParticleTask);
        if(this.ballParticleThread != null && this.ballParticleThread.isAlive() && !this.ballParticleThread.isInterrupted())
            this.ballParticleThread.interrupt();
        for (Ball ball : this.balls) {
            ball.destroy();
        }
    }
}
