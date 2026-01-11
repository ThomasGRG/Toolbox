/*
Toolbox
Copyright (C) 2026 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.toolbox.services

import android.app.NotificationManager
import android.media.AudioManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class VibrateTileService : TileService() {
    // Called when the user adds your tile.
    override fun onTileAdded() {
        super.onTileAdded()
        qsTile.state = getTileState()
        qsTile.updateTile()
    }

    // Called when your app can update your tile.
    override fun onStartListening() {
        super.onStartListening()
        qsTile.state = getTileState()
        qsTile.updateTile()
    }

    // Called when your app can no longer update your tile.
    override fun onStopListening() {
        super.onStopListening()
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        try {
            if (audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            } else {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }
        } catch (exception: Exception) {
        }
        qsTile.state = getTileState()
        qsTile.updateTile()
    }

    // Called when the user removes your tile.
    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    private fun getTileState(): Int {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL) {
            return Tile.STATE_UNAVAILABLE
        }
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        return if (audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }
    }
}