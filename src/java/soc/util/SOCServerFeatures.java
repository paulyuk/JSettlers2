/**
 * Java Settlers - An online multiplayer version of the game Settlers of Catan
 * This file Copyright (C) 2014 Jeremy D Monin <jeremy@nand.net>
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
package soc.util;

/**
 * Set of optional server features that are currently active.
 * Sent from server to client during connect via {@link soc.message.SOCVersion} fields.
 * Added in v1.1.19; earlier clients assume the server has the two features defined in 1.1.19.
 * Use the {@link #SOCServerFeatures(boolean) SOCServerFeatures(true)} constructor when connecting
 * to a server older than 1.1.19.
 *<P>
 * Feature names are kept simple (lowercase alphanumerics, underscore, dash) for encoding into network message fields.
 *<P>
 * <b>Locks:</b> Not thread-safe.  Caller must guard against potential multi-threaded modifications or access.
 *
 * @since 1.1.19
 * @author Jeremy D Monin &lt;jeremy@nand.net&gt;
 */
public class SOCServerFeatures
{
    /** Minimum version (1.1.19) of client/server which send and recognize server features */
    public static final int VERSION_FOR_SERVERFEATURES = 1119;

    /**
     * Users defined in a persistent database.
     * If this feature is active, nicknames and passwords are authenticated.
     * Otherwise there are no passwords defined.
     */
    public static final String FEAT_USERS = "users";

    /**
     * Chat channels.
     * If this feature is active, users are allowed to create chat channels.
     * Otherwise no channels are allowed.
     */
    public static final String FEAT_CHANNELS = "ch";

    /**
     * Separator character ';' between features in {@link #featureList}.
     * Avoid separators defined in {@code SOCMessage}.
     */
    private static char SEP_CHAR = ';';

    /**
     * Active feature list, or null if none.
     * If not null, the list starts and ends with {@link #SEP_CHAR} for ease of search.
     */
    private String featureList = null;

    /**
     * Create a new empty SOCServerFeatures, with none active or defaults active.
     * After construction, use {@link #add(String)} to add active features.
     * @param withOldDefaults  If false, nothing is active. If true, include the default features
     *     which were assumed always active in servers older than v1.1.19:
     *     {@link #FEAT_CHANNELS}, {@link #FEAT_USERS}.
     */
    public SOCServerFeatures(final boolean withOldDefaults)
    {
        if (withOldDefaults)
        {
            featureList = SEP_CHAR + FEAT_CHANNELS + SEP_CHAR + FEAT_USERS + SEP_CHAR;
        } else {
            // featureList is already null
        }
    }

    /**
     * Create a new SOCServerFeatures from an encoded list; useful at client.
     * @param encodedList  List from {@link #getEncodedList()}, or null or "" for none
     * @throws IllegalArgumentException if {@code encodedList} is not empty but
     *     doesn't start and end with the separator character
     */
    public SOCServerFeatures(String encodedList)
        throws IllegalArgumentException
    {
        if (encodedList != null)
        {
            final int L = encodedList.length();
            if (L == 0)
                encodedList = null;
            else if ((encodedList.charAt(0) != SEP_CHAR) || (encodedList.charAt(L - 1) != SEP_CHAR))
                throw new IllegalArgumentException("Bad encoding: " + encodedList);
        }

        featureList = encodedList;
    }

    /**
     * Is this feature active?
     * @param featureName  A defined feature name, such as {@link #FEAT_USERS}
     * @return  True if {@code featureName} is in the features list
     * @throws IllegalArgumentException if {@code featureName} is null or ""
     */
    public boolean isActive(final String featureName)
        throws IllegalArgumentException
    {
        if ((featureName == null) || (featureName.length() == 0))
            throw new IllegalArgumentException("featureName: " + featureName);

        if (featureList == null)
            return false;

        return featureList.contains(SEP_CHAR + featureName + SEP_CHAR);
    }

    /**
     * Add this active feature.
     * @param featureName  A defined feature name, such as {@link #FEAT_USERS}
     * @throws IllegalArgumentException if {@code featureName} is null or ""
     */
    public void add(final String featureName)
        throws IllegalArgumentException
    {
        if ((featureName == null) || (featureName.length() == 0))
            throw new IllegalArgumentException("featureName: " + featureName);

        if (featureList == null)
            featureList = SEP_CHAR + featureName + SEP_CHAR;
        else
            featureList = featureList.concat(featureName + SEP_CHAR);
    }

    /**
     * Get the encoded list of all active features, to send to a client for {@link #SOCServerFeatures(String)}.
     * @return The active features list, or null if none
     */
    public String getEncodedList()
    {
        return featureList;
    }

    /**
     * Human-readable representation of active features.
     * Based on super.toString + featureList. Possible Formats:
     *<UL>
     * <LI> soc.util.SOCServerFeatures@86c347{;ch;users;}
     * <LI> soc.util.SOCServerFeatures@f7e6a96{(empty)}
     *</UL>
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append('{');
        sb.append((featureList != null) ? featureList : "(empty)");
        sb.append('}');
        return sb.toString();
    }

}