/*
 * Copyright (c) 2012 Nahuel Barrios <barrios.nahuel@gmail.com>.
 * No se reconocerá ningún tipo de garantía.
 */

/**
 * PlayerServiceImpl.java Created by: Nahuel Barrios: 23/03/2012, 05:42:19.
 */
package com.nbempire.android.magicannotator.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.nbempire.android.magicannotator.domain.Player;
import com.nbempire.android.magicannotator.service.PlayerService;

/**
 * Implementation of the PlayerService interface.
 *
 * @author Nahuel Barrios.
 * @since 1
 */
public class PlayerServiceImpl implements PlayerService {

    public List<Player> parsePlayers(List<String> playersToParse) {
        List<Player> players = new ArrayList<Player>(playersToParse.size());
        for (String eachPlayerNickName : playersToParse) {
            players.add(new Player(eachPlayerNickName));
        }
        return players;
    }

    public List<String> getExpandablePlayers(List<Player> players) {
        List<String> result = new ArrayList<String>();
        for (Player eachPlayer : players) {
            result.add(eachPlayer.getNickName());
        }
        return result;
    }

}
