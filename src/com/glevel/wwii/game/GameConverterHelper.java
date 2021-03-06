package com.glevel.wwii.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;

import com.glevel.wwii.database.DatabaseHelper;
import com.glevel.wwii.database.dao.BattleDao;
import com.glevel.wwii.game.models.Battle;
import com.glevel.wwii.game.models.ObjectivePoint;
import com.glevel.wwii.game.models.Operation;
import com.glevel.wwii.game.models.Player;

public class GameConverterHelper {

    /**
     * Saves a battle.
     * 
     * @param dbHelper
     * @param battle
     * @return
     */
    public static long saveGame(DatabaseHelper dbHelper, Battle battle) {
        return dbHelper.getBattleDao().save(battle);
    }

    /**
     * Gets all the saved games (in battle mode).
     * 
     * @param dbHelper
     * @return
     */
    public static List<Battle> getUnfinishedBattles(DatabaseHelper dbHelper) {
        return dbHelper.getBattleDao().get(BattleDao.CAMPAIGN_ID + "=?", new String[] { "0" }, null, null);
    }

    /**
     * Deletes the saved games depending on the game mode (campaign or single
     * battle).
     * 
     * @param dbHelper
     */
    public static void deleteSavedBattles(DatabaseHelper dbHelper, int campaignId) {
        dbHelper.getBattleDao().delete(BattleDao.CAMPAIGN_ID + "=?", new String[] { "" + campaignId });
    }

    /**
     * Gets a list of players object from a byte array.
     * 
     * @param blob
     * @return
     */
    public static List<Player> getPlayersFromLoadGame(byte[] blob) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            ObjectInputStream in = new ObjectInputStream(bais);
            @SuppressWarnings("unchecked")
            List<Player> players = (List<Player>) in.readObject();
            in.close();
            return players;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a list of objective points object from a byte array.
     * 
     * @param blob
     * @return
     */
    public static List<ObjectivePoint> getObjectivesFromLoadGame(byte[] blob) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            ObjectInputStream in = new ObjectInputStream(bais);
            @SuppressWarnings("unchecked")
            List<ObjectivePoint> list = (List<ObjectivePoint>) in.readObject();
            in.close();
            return list;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a player object from a byte array.
     * 
     * @param blob
     * @return
     */
    public static Player getPlayerFromLoadGame(byte[] blob) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            ObjectInputStream in = new ObjectInputStream(bais);
            Player player = (Player) in.readObject();
            in.close();
            return player;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a list of operations object from a byte array.
     * 
     * @param blob
     * @return
     */
    public static List<Operation> getOperationsListFromLoadGame(byte[] blob) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            ObjectInputStream in = new ObjectInputStream(bais);
            @SuppressWarnings("unchecked")
            List<Operation> operations = (List<Operation>) in.readObject();
            in.close();
            return operations;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets an object from a byte array.
     * 
     * @param blob
     * @return
     */
    public static Object getObjectFromByte(byte[] blob) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            ObjectInputStream in = new ObjectInputStream(bais);
            Object o = (Object) in.readObject();
            in.close();
            return o;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert any object to a byte array object.
     * 
     * @param object
     * @return
     */
    public static ByteArrayOutputStream toByte(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(baos);
            out.writeObject(object);
            out.close();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
