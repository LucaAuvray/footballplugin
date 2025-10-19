package fr.codinbox.footballplugin.game;

import fr.codinbox.footballplugin.configuration.FootConfig;
import fr.codinbox.footballplugin.utils.TimeEntry;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Ball {

    /**
     * The ball entity item
     */
    private final Item ballItem;

    private final ArrayList<TimeEntry<UUID>> hits;

    private Vector velocity;

    protected Ball(Location location, ItemStack itemStack) {
        this.hits = new ArrayList<>();
        this.ballItem = location.getWorld().dropItem(location, itemStack);
        this.velocity = new Vector(0f, 0f, 0f);
    }

    public void kicked(Player player) {
        if(GameMode.SPECTATOR.equals(player.getGameMode()))
            return;

        if(this.ballItem.isDead())
            return;

        Location playerLocation = player.getEyeLocation();
        float pitch = playerLocation.getPitch();

        pitch -= FootConfig.BALL_ADDITIONAL_PITCH;

        if(pitch > 0.0f)
            pitch = 0f;

        if(pitch < FootConfig.BALL_MAX_PITCH)
            pitch = FootConfig.BALL_MAX_PITCH;

        playerLocation.setPitch(pitch);

        Vector velocityVector = playerLocation.getDirection();
        velocityVector = velocityVector.multiply(FootConfig.BALL_HIT_POWER);

        setVelocity(velocityVector);

        if(this.hits.isEmpty() || !getLastHit().getValue().equals(player.getUniqueId()))
            this.hits.add(new TimeEntry<>(System.currentTimeMillis(), player.getUniqueId()));
        else
            this.hits.set(this.hits.size() - 1, new TimeEntry<>(System.currentTimeMillis(), player.getUniqueId()));
        this.ballItem.getWorld().playSound(ballItem.getLocation(), Sound.BLOCK_STONE_STEP, 1f, 1f);
        Random r = new Random();
        for(int i = 0; i < 30; i++) {
            double randomX = -0.2 + (0.2 - -0.2) * r.nextDouble();
            double randomY = 0.2 + (0.4 - 0.2) * r.nextDouble();
            double randomZ = -0.2 + (0.2 - -0.2) * r.nextDouble();
            this.ballItem.getWorld().spawnParticle(Particle.BLOCK_CRACK, ballItem.getLocation().clone().add(randomX, randomY, randomZ), 1, 0, 0, 0, 0, Material.ORANGE_TERRACOTTA.createBlockData());
        }
    }

    protected void runPhysics() {
        Vector lastVelocity = ballItem.getVelocity();
        if (this.velocity != null) {
            lastVelocity = this.velocity;
        }
        Vector newVelocity = ballItem.getVelocity();

        if (newVelocity.getX() == 0.0D) {
            newVelocity.setX(-velocity.getX() * 0.5D);
        } else if (Math.abs(velocity.getX() - newVelocity.getX()) < 0.15D) {
            newVelocity.setX(velocity.getX() * 0.975D);
        }
        if ((newVelocity.getY() == 0.0D) && (velocity.getY() < -0.1D)) {
            newVelocity.setY(-velocity.getY() * 0.9D);
        }
        if (newVelocity.getZ() == 0.0D) {
            newVelocity.setZ(-velocity.getZ() * 0.5D);
        } else if (Math.abs(velocity.getZ() - newVelocity.getZ()) < 0.15D) {
            newVelocity.setZ(velocity.getZ() * 0.975D);
        }

        ballItem.setVelocity(newVelocity);
        this.velocity = newVelocity;
    }

    protected void particle() {
        this.ballItem.getWorld().spawnParticle(Particle.END_ROD, ballItem.getLocation(), 0);
    }

    public void destroy() {
        this.ballItem.remove();
    }

    public void setVelocity(Vector velocity) {
        this.ballItem.setVelocity(velocity);
        this.velocity = velocity;
    }

    public void teleport(Location location) {
        this.ballItem.teleport(location);
    }

    public Item getBallItem() {
        return ballItem;
    }

    public ArrayList<TimeEntry<UUID>> getHits() {
        return hits;
    }

    public TimeEntry<UUID> getLastHit() {
        if(this.hits.isEmpty())
            return null;
        return this.hits.get(this.hits.size() - 1);
    }

    public TimeEntry<UUID> getPassHit() {
        if(this.hits.isEmpty() || this.hits.size() < 2)
            return null;
        return this.hits.get(this.hits.size() - 2);
    }

    public Location getLocation() {
        return this.ballItem.getLocation();
    }

    public void resetHits() {
        this.hits.clear();
    }

}
