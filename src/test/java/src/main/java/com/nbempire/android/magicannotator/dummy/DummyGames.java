/**
 * DummyGames.java Created by: Nahuel Barrios: 17/03/2012, 11:33:47.
 */
package com.nbempire.android.magicannotator.dummy;

import com.nbempire.android.magicannotator.domain.game.Game;
import com.nbempire.android.magicannotator.domain.game.Truco;

/**
 * TODO : JavaDoc : for DummyGames.
 * 
 * @author Nahuel Barrios.
 * @version 1.0.
 * @since 17/03/2012, 11:33:47.
 */
public abstract class DummyGames {

    public static Game truco() {
        return new Truco();
    }

}
