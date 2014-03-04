package com.glevel.wwii.game.models.units.categories;

import java.util.List;

import com.glevel.wwii.R;
import com.glevel.wwii.activities.GameActivity;
import com.glevel.wwii.game.GameUtils;
import com.glevel.wwii.game.data.ArmiesData;
import com.glevel.wwii.game.logic.MapLogic;
import com.glevel.wwii.game.logic.pathfinding.MovingElement;
import com.glevel.wwii.game.logic.pathfinding.Node;
import com.glevel.wwii.game.models.Battle;
import com.glevel.wwii.game.models.GameElement;
import com.glevel.wwii.game.models.map.Map;
import com.glevel.wwii.game.models.map.Tile;
import com.glevel.wwii.game.models.orders.DefendOrder;
import com.glevel.wwii.game.models.orders.FireOrder;
import com.glevel.wwii.game.models.orders.HideOrder;
import com.glevel.wwii.game.models.orders.MoveOrder;
import com.glevel.wwii.game.models.orders.Order;
import com.glevel.wwii.game.models.units.Soldier;
import com.glevel.wwii.game.models.units.Tank;
import com.glevel.wwii.game.models.weapons.Turret;
import com.glevel.wwii.game.models.weapons.categories.Weapon;

public abstract class Unit extends GameElement implements MovingElement {

    /**
     * 
     */
    private static final long serialVersionUID = -1514358997270651189L;

    // private static final float DEFEND_ORDER_AMBUSH_DISTANCE = 300;

    protected final ArmiesData army;
    private final int image;
    protected final int moveSpeed;
    private List<Weapon> weapons;
    protected Experience experience;
    private String spriteName;
    private float spriteScale;

    private InjuryState health;
    private int frags;
    private boolean isAvailable;
    private Order order;
    protected Action currentAction;
    private int panic;

    protected static enum RotationStatus {
        NONE, ROTATING, REVERSE
    }

    public static enum InjuryState {
        NONE(R.color.green), INJURED(R.color.yellow), BADLYINJURED(R.color.orange), DEAD(R.color.red);

        private final int color;

        private InjuryState(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

    }

    public static enum Experience {
        RECRUIT(0), VETERAN(R.drawable.ic_veteran), ELITE(R.drawable.ic_elite);

        private final int image;

        private Experience(int image) {
            this.image = image;
        }

        public int getImage() {
            return image;
        }

    }

    public static enum Action {
        WAITING, MOVING, RUNNING, FIRING, HIDING, RELOADING, AIMING, DEFENDING
    }

    public Unit(ArmiesData army, int name, int image, Experience experience, List<Weapon> weapons, int moveSpeed,
            String spriteName, float spriteScale) {
        super(name, spriteName, spriteScale);
        this.army = army;
        this.image = image;
        this.experience = experience;
        this.weapons = weapons;
        this.moveSpeed = moveSpeed;
        this.health = InjuryState.NONE;
        this.currentAction = Action.WAITING;
        this.setPanic(0);
        this.frags = 0;
        this.spriteName = spriteName;
        this.spriteScale = spriteScale;
        this.order = new HideOrder();
    }

    protected abstract float getUnitSpeed();

    public abstract float getUnitTerrainProtection();

    public abstract int getPrice();

    public InjuryState getHealth() {
        return health;
    }

    public void setHealth(InjuryState health) {
        this.health = health;
    }

    public int getMoveSpeed() {
        return moveSpeed;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<Weapon> weapons) {
        this.weapons = weapons;
    }

    public Experience getExperience() {
        return experience;
    }

    public void setExperience(Experience experience) {
        this.experience = experience;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        if (!canMove() && order instanceof MoveOrder) {
            return;
        }
        this.order = order;
    }

    public Action getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(Action currentAction) {
        this.currentAction = currentAction;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public int getFrags() {
        return frags;
    }

    public int getImage() {
        return image;
    }

    public ArmiesData getArmy() {
        return army;
    }

    public int getRealSellPrice(boolean isSelling) {
        if (isSelling) {
            return (int) (getPrice() * GameUtils.SELL_PRICE_FACTOR);
        } else {
            return getPrice();
        }
    }

    /**
     * Create a new instance of Unit. Used when we buy a unit.
     * 
     * @param army
     * @return
     */
    public Unit copy() {
        Unit unit = null;
        if (this instanceof Soldier) {
            unit = new Soldier(army, name, image, experience, weapons, moveSpeed, spriteName, spriteScale);
        } else if (this instanceof Tank) {
            Vehicle vehicle = (Vehicle) this;
            unit = new Tank(army, name, image, experience, weapons, moveSpeed, vehicle.getArmor(), spriteName,
                    spriteScale);
        }
        return unit;
    }

    public void updateMovementPath(Map map) {
    }

    public void move(Battle battle) {
        this.currentAction = Action.MOVING;
        MoveOrder moveOrder = (MoveOrder) order;

        updateUnitRotation(moveOrder.getXDestination(), moveOrder.getYDestination());
        float dx = moveOrder.getXDestination() - sprite.getX();
        float dy = moveOrder.getYDestination() - sprite.getY();
        double angle = Math.atan(dy / dx);
        float dd = moveSpeed * 10 * 0.04f * getUnitSpeed();

        boolean hasArrived = false;
        float distanceLeft = MapLogic.getDistanceBetween(moveOrder.getXDestination(), moveOrder.getYDestination(),
                sprite.getX(), sprite.getY());
        if (distanceLeft < dd) {
            hasArrived = true;
            dd = distanceLeft;
        }

        float[] newPosition = MapLogic.getCoordinatesAfterTranslation(sprite.getX(), sprite.getY(), dd, angle, dx > 0);

        Tile nextTile = MapLogic.getTileAtCoordinates(battle.getMap(), newPosition[0], newPosition[1]);

        if (!canMoveIn(nextTile)) {
            return;
        }

        sprite.setPosition(newPosition[0], newPosition[1]);

        if (nextTile.getTileX() != getTilePosition().getTileX() || nextTile.getTileY() != getTilePosition().getTileY()) {
            setTilePosition(battle, nextTile);
        }

        if (hasArrived) {
            order = null;
        }
    }

    protected RotationStatus updateUnitRotation(float xDestination, float yDestination) {
        float dx = xDestination - sprite.getX();
        float dy = yDestination - sprite.getY();
        double angle = Math.atan(dy / dx);
        if (dx > 0) {
            sprite.setRotation((float) (angle * 180 / Math.PI + 90));
        } else {
            sprite.setRotation((float) (angle * 180 / Math.PI + 270));
        }
        return RotationStatus.NONE;
    }

    public void fire(Battle battle, Unit target) {

        if (target.isDead() || !MapLogic.canSee(battle.getMap(), this, target)) {
            // if target is dead or is not visible anymore, stop to shoot
            order = new DefendOrder();
            return;
        }

        // get most suitable weapon
        Weapon weapon = getBestWeapon(battle, target);
        if (weapon != null) {
            fireWithWeapon(battle, weapon, target);
        } else {
            // no weapon available for this fire order
            this.order = new DefendOrder();
        }
    }

    protected void fireWithWeapon(Battle battle, Weapon weapon, Unit target) {
        if (weapon instanceof Turret) {
            // turrets take time to rotate
            Turret turret = (Turret) weapon;
            boolean isRotatingOver = turret.rotateTurret(sprite, target.getSprite().getX(), target.getSprite().getY());
            if (!isRotatingOver) {
                return;
            }
        } else {
            RotationStatus rotationStatus = updateUnitRotation(target.getSprite().getX(), target.getSprite().getY());
            if (rotationStatus == RotationStatus.ROTATING) {
                return;
            }
        }

        if (weapon.getReloadCounter() > 0) {
            if (weapon.getAimCounter() == 0) {
                weapon.setAimCounter(-10);
                // aiming
                this.currentAction = Action.AIMING;
            } else if (weapon.getAimCounter() < 0) {
                weapon.setAimCounter(weapon.getAimCounter() + 1);
                if (weapon.getAimCounter() == 0) {
                    weapon.setAimCounter(weapon.getCadence());
                }
                // aiming
                this.currentAction = Action.AIMING;
            } else if (GameActivity.gameCounter % (11 - weapon.getShootSpeed()) == 0) {
                // firing !!!
                currentAction = Action.FIRING;

                // add muzzle flash sprite
                sprite.startFireAnimation(weapon);

                weapon.setAmmoAmount(weapon.getAmmoAmount() - 1);
                weapon.setReloadCounter(weapon.getReloadCounter() - 1);
                weapon.setAimCounter(weapon.getAimCounter() - 1);

                // if not a lot of ammo, more aiming !
                if (weapon.getAmmoAmount() == weapon.getMagazineSize() * 2) {
                    weapon.setCadence(Math.max(1, weapon.getCadence() / 2));
                }

                // increase target panic
                target.getShots(this, battle.getMap());

                if (!target.isDead()) {// prevent the multiple frags bug

                    weapon.resolveFireShot(battle, this, target);

                    if (target.isDead()) {
                        target.died(battle);
                        killedSomeone(target);
                    }
                }
            }
        } else if (weapon.getReloadCounter() == 0) {
            // need to reload
            this.currentAction = Action.RELOADING;
            weapon.setReloadCounter(-weapon.getReloadSpeed());
        } else {
            // reloading
            this.currentAction = Action.RELOADING;
            if (GameActivity.gameCounter % 12 == 0) {
                weapon.setReloadCounter(weapon.getReloadCounter() + 1);
                if (weapon.getReloadCounter() == 0) {
                    // reloading is over
                    weapon.setReloadCounter(weapon.getMagazineSize());
                }
            }

        }
    }

    public Weapon getBestWeapon(Battle battle, Unit target) {
        boolean canSeeTarget = MapLogic.canSee(battle.getMap(), this, target);
        Weapon bestWeapon = null;
        for (Weapon weapon : weapons) {
            if (weapon.canUseWeapon(this, target, canSeeTarget)) {
                if (bestWeapon == null || weapon.getEfficiencyAgainst(target) > bestWeapon.getEfficiencyAgainst(target)) {
                    bestWeapon = weapon;
                }
            }
        }

        return bestWeapon;
    }

    public void applyDamage(int damage) {
        health = InjuryState.values()[Math.min(InjuryState.DEAD.ordinal(), health.ordinal() + damage)];
    }

    public void defendPosition() {
        this.currentAction = Action.DEFENDING;

        if (GameActivity.gameCounter % 3 == 0) {
            if (this.panic > 0) {
                this.panic--;
            }
        }
    }

    public void hide() {
        this.currentAction = Action.HIDING;

        if (GameActivity.gameCounter % 3 == 0) {
            if (this.panic > 0) {
                this.panic--;
            }
        }
    }

    public int getPanic() {
        return panic;
    }

    public void setPanic(int panic) {
        this.panic = panic;
    }

    public void resolveOrder(Battle battle) {
        if (panic > 0) {
            // test if the unit can react
            if (Math.random() * 10 + getExperience().ordinal() < panic) {
                // the unit is under fire
                hide();
                return;
            }
        }

        if (order instanceof MoveOrder) {
            if (this instanceof Vehicle) {
                // vehicles can move and fire at the same time
                for (Unit u : battle.getEnemies(this)) {
                    if (!u.isDead() && MapLogic.canSee(battle.getMap(), this, u)) {
                        boolean done = ((Vehicle) this).fireWhileMoving(battle, u);
                        if (done) {
                            return;
                        }
                    }
                }
                ((Turret) getWeapons().get(0)).rotateTurretTo(sprite, sprite.getRotation());
            }
            updateMovementPath(battle.getMap());
        } else if (order instanceof DefendOrder) {
            // search for enemies
            for (Unit u : battle.getEnemies(this)) {
                if (!u.isDead() && MapLogic.canSee(battle.getMap(), this, u)) {
                    order = new FireOrder(u);
                    return;
                }
            }
            // stay ambush
            defendPosition();
        } else if (order instanceof FireOrder) {
            fire(battle, ((FireOrder) order).getTarget());
        } else if (order instanceof HideOrder) {
            hide();
        }
    }

    public void takeInitiative() {
        order = new DefendOrder();
    }

    public boolean isDead() {
        return health == InjuryState.DEAD;
    }

    public void getShots(Unit shooter, Map map) {
        // increase panic
        if (panic < 10) {
            panic++;
        }

        // fight back
        if (order == null || order instanceof DefendOrder || order instanceof MoveOrder && Math.random() < 0.5) {
            if (MapLogic.canSee(map, this, shooter)) {
                order = new FireOrder(shooter);
            }
        }
    }

    public void killedSomeone(Unit unitKilled) {
        // add frags
        if (unitKilled instanceof Tank) {
            frags += 5;
        } else if (unitKilled instanceof Vehicle) {
            frags += 2;
        } else if (unitKilled instanceof Soldier) {
            if (unitKilled.getWeapons().get(0) instanceof Turret) {
                frags += 2;
            } else {
                frags += 1;
            }
        }
    }

    public void died(Battle battle) {
        if (this instanceof Soldier) {
            setTilePosition(battle, null);
        }

        if (!sprite.isVisible()) {
            sprite.setVisible(true);
        }
        sprite.setZIndex(10);
        sprite.setCanBeDragged(false);
        order = null;
        // draw sprite
        if (this instanceof Tank || weapons.size() > 0 && weapons.get(0) instanceof Turret) {
            // smoke
            battle.getOnNewSprite().drawAnimatedSprite(getSprite().getX(), getSprite().getY() - 70, "smoke.png", 120,
                    2.0f, -1, false, 60);
        } else if (this instanceof Soldier) {
            // blood
            battle.getOnNewSprite().drawAnimatedSprite(getSprite().getX(), getSprite().getY(), "blood.png", 120, 0.6f,
                    0, false, 15);
        }

    }

    public boolean canMove() {
        return getMoveSpeed() > 0;
    }

    @Override
    public boolean canMoveIn(Node node) {
        return ((Tile) node).getContent() == null || ((Tile) node).getContent() == this;
    }

    public void instantlyKilledSomeone(Battle battle, Unit target) {
        target.setHealth(InjuryState.DEAD);
        target.died(battle);
        killedSomeone(target);
    }

    public boolean canBeDeployedThere(Battle battle, Tile tile) {
        return canMoveIn(tile);
    }

}
