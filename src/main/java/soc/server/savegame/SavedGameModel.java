/**
 * Java Settlers - An online multiplayer version of the game Settlers of Catan
 * This file Copyright (C) 2020 Jeremy D Monin <jeremy@nand.net>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The maintainer of this program can be reached at jsettlers@nand.net
 **/
package soc.server.savegame;

import java.util.Map;

import soc.game.SOCGame;
import soc.game.SOCGameOption;
import soc.game.SOCPlayer;

/**
 * Data model for a game saved to/loaded from a file.
 *<P>
 * To save, use the {@link #SavedGameModel(SOCGame)} constructor.
 * To load, use {@link #SavedGameModel()}.
 * See those constructors' javadocs for usage details.
 *<P>
 * This standalone model is cleaner than trying to serialize/deserialize {@link SOCGame}, SOCBoard, etc.
 *
 * @author Jeremy D Monin &lt;jeremy@nand.net&gt;
 * @since 2.3.00
 */
public class SavedGameModel
{
    /** Current model version: 2300 for v2.3.00 */
    public static int MODEL_VERSION = 2300;

    private transient SOCGame game = null;

    /* DATA FIELDS to be saved into file */

    /** Model version when saved, in same format as {@link #MODEL_VERSION} */
    int modelVersion;

    /** Game minimum version, from {@link SOCGame#getClientVersionMinRequired()} */
    int gameVersion;

    String gameName;

    /** Game options (or null), from {@link SOCGameOption#packOptionsToString(Map, boolean)}. */
    String gameOptions;

    /** Game duration, from {@link SOCGame#getStartTime()} */
    int gameDurationSeconds;

    /** Current state, from {@link SOCGame#getGameState()} */
    int gameState;

    /** Player info and empty seats. Size is {@link SOCGame#maxPlayers}. */
    PlayerInfo[] playerSeats;

    /* End of DATA FIELDS */

    /**
     * Create an empty SavedGameModel to load a game file into.
     * Once loaded, state will temporarily be {@link SOCGame#LOADING},
     * and {@link SOCGame#savedGameModel} will be this SGM.
     * Call {@link #resumePlay(boolean)} to continue play.
     */
    public SavedGameModel()
    {
    }

    /**
     * Create a SavedGameModel to save as a game file.
     * Game state must be {@link SOCGame#ROLL_OR_CARD} or higher.
     * @param ga  Game data to save
     * @throws IllegalStateException if game state &lt; {@link SOCGame#ROLL_OR_CARD}
     */
    public SavedGameModel(final SOCGame ga)
        throws IllegalStateException
    {
        this();

        if (ga.getGameState() < SOCGame.ROLL_OR_CARD)
            throw new IllegalStateException("gameState");

        modelVersion = MODEL_VERSION;
        game = ga;

        // save data fields:
        gameName = ga.getName();
        final Map<String, SOCGameOption> opts = ga.getGameOptions();
        if (opts != null)
            gameOptions = SOCGameOption.packOptionsToString(opts, false);
        gameDurationSeconds = (int) (((System.currentTimeMillis() - ga.getStartTime().getTime()) + 500) / 1000L);
            // same rounding calc as SSMH.processDebugCommand_gameStats
        gameState = ga.getGameState();
        gameVersion = ga.getClientVersionMinRequired();
        playerSeats = new PlayerInfo[ga.maxPlayers];
        for (int pn = 0; pn < ga.maxPlayers; ++pn)
            playerSeats[pn] = new PlayerInfo(ga.getPlayer(pn), ga.isSeatVacant(pn));
    }

    /**
     * Get the completely loaded game, or the game which was "saved" into this model.
     * @return Game, or {@code null} if not loaded successfully
     */
    public SOCGame getGame()
    {
        return game;
    }

    /**
     * Resume play of a loaded game: Check any constraints, update gameState.
     * @param ignoreConstraints  If true, don't check any {@link Constraint}s in the model
     * @return game ready to play, with {@link SOCGame#getGameState()} same as when it was saved
     * @throws UnsupportedOperationException if gameState != {@link SOCGame#LOADING}
     * @throws IllegalStateException if a constraint is not met
     */
    public SOCGame resumePlay(final boolean ignoreConstraints)
        throws UnsupportedOperationException, IllegalStateException
    {
        if (game.getGameState() != SOCGame.LOADING)
            throw new UnsupportedOperationException("gameState");

        // TODO maybe check constraints

        game.setGameState(gameState);

        return game;
    }

    /**
     * Info on one player position sitting in the game.
     * @see soc.server.SOCClientData
     */
    static class PlayerInfo
    {
        String name;
        boolean isSeatVacant;
        int totalVP;
        boolean isRobot, isBuiltInRobot;

        // TODO resource counts, piece counts, dev cards

        PlayerInfo(SOCPlayer pl, boolean isVacant)
        {
            name = pl.getName();
            isSeatVacant = isVacant;
            totalVP = pl.getTotalVP();
            isRobot = pl.isRobot();
            isBuiltInRobot = pl.isBuiltInRobot();
        }
    }

    /**
     * A constraining condition requested before resuming the game.
     * For example, player 3 must be a "faster" built-in bot, or be a certain third-party bot class.
     * Constraints can be ignored when resuming.
     */
    public static class Constraint
    {
        // TBD
    }

}
