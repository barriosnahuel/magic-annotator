/*
 * Copyright (c) 2012 Nahuel Barrios <barrios.nahuel@gmail.com>.
 * No se reconocerá ningún tipo de garantía.
 */

/**
 * ChoosePlayersActivity.java Created by: Nahuel Barrios: 29/02/2012, 09:22:36.
 */
package com.nbempire.android.magicannotator.component.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TableRow.LayoutParams;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.nbempire.android.magicannotator.AppParameter;
import com.nbempire.android.magicannotator.R;
import com.nbempire.android.magicannotator.domain.Player;
import com.nbempire.android.magicannotator.domain.Team;
import com.nbempire.android.magicannotator.domain.exception.UserException;
import com.nbempire.android.magicannotator.domain.game.Chancho;
import com.nbempire.android.magicannotator.domain.game.Game;
import com.nbempire.android.magicannotator.service.AnnotatorService;
import com.nbempire.android.magicannotator.service.PlayerService;
import com.nbempire.android.magicannotator.service.ServiceFactory;
import com.nbempire.android.magicannotator.service.impl.AnnotatorServiceImpl;
import com.nbempire.android.magicannotator.service.impl.PlayerServiceImpl;
import com.nbempire.android.magicannotator.storage.MagicAnnotatorDBHelper;
import com.nbempire.android.magicannotator.util.android.analytics.AnalyticsUtil;

/**
 * Activity to let users select which players are going to play.
 *
 * @author Nahuel Barrios.
 * @since 1
 */
public class ChoosePlayersActivity extends Activity {

    /**
     * Tag for class' log.
     */
    private static final String LOG_TAG = "ChoosePlayersActivity";

    /**
     * A service for the Player entity.
     */
    private PlayerService playerService;

    private static final String SELECTED_PLAYERS_KEY = "selectedPlayers";

    private final Set<CharSequence> players = new TreeSet<CharSequence>();

    private ArrayList<String> selectedPlayers;

    private Game aGame;

    private int annotatorId = -1;

    /**
     * The MagicAnnotator database.
     */
    private MagicAnnotatorDBHelper magicAnnotatorDBHelper;

    /**
     * Service for the annotators.
     */
    private final AnnotatorService annotatorService = new AnnotatorServiceImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsTracker.getInstance().trackPageView(AnalyticsUtil.generatePageName(LOG_TAG));
        setContentView(R.layout.chooseplayers);

        Bundle extras = getIntent().getExtras();
        Object parameter = extras.get(AppParameter.GAME);

        if (parameter instanceof Game) {
            aGame = (Game) extras.getSerializable(AppParameter.GAME);
            if (aGame instanceof Chancho) {
                ((Button) findViewById(R.id.choosePlayers_button_makeTeams)).setText(R.string.play);
            }
        } else {
            annotatorId = extras.getInt(AppParameter.GAME);
            switch (annotatorId) {
                case R.string.annotator_otro:
                    ((Button) findViewById(R.id.choosePlayers_button_makeTeams)).setText(R.string.play);
                    break;
                default:
                    break;
            }
        }

        if (parameter instanceof Game) {
            addOnClickActionForMakeTeamsButtonForOldGames();
        } else {
            addOnClickActionForMakeTeamsButtonForNewGames();
        }

        initializeDependencies(this);

        selectedPlayers = savedInstanceState == null ? new ArrayList<String>() : savedInstanceState.getStringArrayList(SELECTED_PLAYERS_KEY);
        loadPlayers(selectedPlayers);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initializeDependencies(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(LOG_TAG, "Closing MagicAnnotatorDBHelper...");
        //  Close the DBHelper in onPause because of the Activities lifecycle and the OS may find a memory leak if it's not closed.
        magicAnnotatorDBHelper.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(SELECTED_PLAYERS_KEY, selectedPlayers);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chooseplayers, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOG_TAG, "User selected " + item.getTitle() + " option from the menu.");

        switch (item.getItemId()) {
            case R.id.choosePlayersMenuItem_deleteAll:
                Log.i(LOG_TAG, "All players will be deleted from storage.");
                playerService.deleteAll();
                players.clear();
                onCreate(null);
                break;
            default:
                Log.e(LOG_TAG, "The menu item " + item.getTitle() + " has no action attached.");
                break;
        }

        return true;
    }

    /**
     * Instantiates the {@code magicAnnotatorDB} and all activity's dependencies.
     *
     * @param context
     *         The activity Context.
     *
     * @since 13
     */
    private void initializeDependencies(Context context) {
        magicAnnotatorDBHelper = new MagicAnnotatorDBHelper(context);
        playerService = new PlayerServiceImpl(magicAnnotatorDBHelper.getWritableDatabase());
    }

    /**
     * Cargo los jugadores dinámicamente.
     *
     * @param selectedPlayersList
     *         {@link List} de {@link String} con los jugadores ya seleccionados para ver cu�les tengo que tildar.
     */
    private void loadPlayers(List<String> selectedPlayersList) {
        List<Player> savedPlayers = playerService.findAll();
        for (Player eachSavedPlayer : savedPlayers) {
            players.add(eachSavedPlayer.getNickName());
        }

        TableLayout playersLayout = (TableLayout) findViewById(R.id.choosePlayers_playersLayout);

        for (CharSequence eachPlayer : players) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            String playerName = eachPlayer.toString();
            tableRow.addView(preparePlayerSelector(playerName, selectedPlayersList.contains(playerName)));
            playersLayout.addView(tableRow);
        }
    }

    /**
     * Creates a checkbox and set its status (checked or not) based on {@code checked} parameter.
     *
     * @param playerName
     *         String with the name of the player to put in the checkbox.
     * @param checked
     *         Boolean value with {@code true} if the player has to be checked. {@code false} if not.
     *
     * @return The View ready to add to the GUI.
     *
     * @since 1
     */
    private View preparePlayerSelector(String playerName, boolean checked) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(playerName);
        if (checked) {
            checkBox.setChecked(checked);
        }
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedPlayers.add(buttonView.getText().toString());
                } else {
                    selectedPlayers.remove(buttonView.getText().toString());
                }
                Toast.makeText(getApplicationContext(),
                                      getText(R.string.choosePlayers_selected).toString() + " " + selectedPlayers.size(),
                                      Toast.LENGTH_SHORT).show();
            }
        });
        return checkBox;
    }

    /**
     * Método llamado desde la definición del layout para permitir al usuario crear un jugador nuevo, y de crearlo, agregarlo al listado ya
     * tildado.
     *
     * @param view
     *         {@link View} desde la cuál se llamó a este método.
     *
     * @since 1
     */
    public void openPlayerCreator(View view) {
        final EditText input = new EditText(view.getContext());

        new AlertDialog.Builder(this).setTitle(R.string.newplayer_enterNickName).setView(input)
                .setPositiveButton(R.string.newplayer_createPlayer, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newPlayerNickname = input.getText().toString();
                        if (newPlayerNickname.length() > 0) {
                            if (addCheckedDynamicPlayer(newPlayerNickname)) {
                                Toast.makeText(getApplicationContext(),
                                                      newPlayerNickname
                                                              + " "
                                                              + getText(R.string.choosePlayers_playerAlreadyExists).toString(),
                                                      Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).show();
    }

    /**
     * Creates functionality for make teams button.
     *
     * @since 1
     */
    private void addOnClickActionForMakeTeamsButtonForNewGames() {
        findViewById(R.id.choosePlayers_button_makeTeams).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (selectedPlayers.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getText(R.string.mustSelectAtLeastOne).toString(), Toast.LENGTH_SHORT).show();
                } else {
                    Intent nextIntentToShow = new Intent(view.getContext(), annotatorService.get(annotatorId));

                    nextIntentToShow.putExtra(AppParameter.GAME, annotatorId);
                    nextIntentToShow.putExtra(AppParameter.PLAYERS, selectedPlayers);

                    startActivity(nextIntentToShow);
                }
            }
        });
    }

    /**
     * Creo la acción para el botón armar equipos.
     * <p/>
     * TODO : Refactor : Remove this method. Use the new one.
     *
     * @since 1
     */
    private void addOnClickActionForMakeTeamsButtonForOldGames() {
        findViewById(R.id.choosePlayers_button_makeTeams).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                try {
                    List<Team> teams = ServiceFactory.getInstance(aGame).makeTeams(playerService.parsePlayers(selectedPlayers));
                    aGame.setTeams(teams);

                    Intent nextIntentToShow;
                    if (teams.size() > 1) {
                        nextIntentToShow = new Intent(view.getContext(), ViewTeamsActivity.class);
                    } else {
                        nextIntentToShow = new Intent(view.getContext(), annotatorService.get(aGame));
                    }

                    nextIntentToShow.putExtra(AppParameter.GAME, aGame);
                    startActivity(nextIntentToShow);
                } catch (UserException userException) {
                    Log.e(LOG_TAG, userException.getMessage());

                    AlertDialog winMessageAlertDialog = new AlertDialog.Builder(view.getContext()).create();
                    winMessageAlertDialog.setTitle(userException.getGuiMessage());
                    winMessageAlertDialog.setButton(getText(R.string.commonLabel_OK), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            //  Do nothing.
                        }
                    });
                    winMessageAlertDialog.show();
                }
            }
        });
    }

    /**
     * Adds a new row to the table layout. This new row will show a checked checkbox containing as label the playerNickName parameter.
     *
     * @param playerNickName
     *         {@link String} with the player's nickname.
     *
     * @return {@link boolean} {@code true} if the player was not added. {@code false} otherwise.
     *
     * @since 1
     */
    private boolean addCheckedDynamicPlayer(String playerNickName) {
        boolean added = players.add(playerNickName);
        if (added) {
            TableLayout playersLayout = (TableLayout) findViewById(R.id.choosePlayers_playersLayout);
            TableRow tableRow = new TableRow(playersLayout.getContext());
            tableRow.addView(preparePlayerSelector(playerNickName, true));
            playersLayout.addView(tableRow);

            selectedPlayers.add(playerNickName);
            playerService.save(new Player(playerNickName));
        }
        return !added;
    }

}
