/**
 * GameFactory.java Created by: Nahuel Barrios: 24/03/2012, 01:53:59.
 */
package com.nbempire.android.magicannotator.service;

import android.util.Log;
import com.nbempire.android.magicannotator.GUIKeys;
import com.nbempire.android.magicannotator.domain.game.Chancho;
import com.nbempire.android.magicannotator.domain.game.Game;
import com.nbempire.android.magicannotator.domain.game.Truco;
import com.nbempire.android.magicannotator.domain.game.Tute;

/**
 * Factory to creates an instance of games based on a specific gameKey.
 * 
 * @author Nahuel Barrios.
 * @since 1.0
 */
public abstract class GameFactory {

    /**
     * Tag for class' log.
     */
    private static final String LOG_TAG = "GameFactory";

    /**
     * Creates an instance of games based on a specific gameKey.
     * @param gameKey The key of a specific game.
     * @return An instance of Game based on the specified gameKey.
     * @throws IllegalArgumentException
     */
    public static Game getInstance(String gameKey) throws IllegalArgumentException {
        try {
            if (gameKey.equals("Chancho")) {
                return new Chancho();
            } else if (gameKey.equals("Otro")) {
                return null;
            } else if (gameKey.equals("Truco")) {
                return new Truco();
            } else if (gameKey.equals("Tute")) {
                return new Tute();
            } else {
                throw new IllegalArgumentException(GUIKeys.EXCEPTION_GAME_INVALID_GAME_KEY);
            }
        } catch (NullPointerException nullPointerException) {
            Log.e(LOG_TAG, "Selected game's key can't be null.");
            throw new IllegalArgumentException(GUIKeys.EXCEPTION_GAME_INVALID_GAME_KEY);
        }

    }
}
