package mods.flammpfeil.slashblade.entity;

import net.minecraft.entity.Entity;

public interface IShootable {

    Entity getShooter();
    void setShooter(Entity shooter);

    double getDamage();
}
