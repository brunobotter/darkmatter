package br.com.darkmatter.screen

import br.com.darkmatter.DarkMatter
import br.com.darkmatter.PREFERENCE_HIGHSCORE_KEY
import br.com.darkmatter.PREFERENCE_MUSIC_ENABLED_KEY
import br.com.darkmatter.asset.MusicAsset
import br.com.darkmatter.ecs.createDarkMatter
import br.com.darkmatter.ecs.createPlayer
import br.com.darkmatter.ecs.system.MoveSystem
import br.com.darkmatter.ecs.system.PlayerAnimationSystem
import br.com.darkmatter.ecs.system.PowerUpSystem
import br.com.darkmatter.ui.ConfirmDialog
import br.com.darkmatter.ui.MenuUI
import br.com.darkmatter.ui.TextDialog
import com.badlogic.gdx.Gdx
import ktx.actors.onChangeEvent
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.ashley.getSystem
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set

private const val PLAYER_SPAWN_Y = 3f

class MenuScreen(game: DarkMatter) : Screen(game, MusicAsset.MENU) {
    private val preferences = game.preferences
    private val ui = MenuUI(bundle).apply {
        startGameButton.onClick { game.setScreen<GameScreen>() }
        soundButton.onChangeEvent {
            audioService.enabled = !this.isChecked
            preferences.flush {
                this[PREFERENCE_MUSIC_ENABLED_KEY] = audioService.enabled
            }
        }
        controlButton.onClick {
            controlsDialog.show(stage)
        }
        creditsButton.onClick {
            creditsDialog.show(stage)
        }
        quitGameButton.onClick {
            confirmDialog.show(stage)
        }
    }
    private val confirmDialog = ConfirmDialog(bundle).apply {
        yesButton.onClick { Gdx.app.exit() }
        noButton.onClick { hide() }
    }
    private val creditsDialog = TextDialog(bundle, "credits")
    private val controlsDialog = TextDialog(bundle, "controls")

    override fun show() {
        super.show()
        engine.run {
            createPlayer(assets, spawnY = PLAYER_SPAWN_Y)
            createDarkMatter()
        }
        audioService.enabled = preferences[PREFERENCE_MUSIC_ENABLED_KEY, true]
        setupUI()
    }

    private fun setupUI() {
        ui.run {
            soundButton.isChecked = !audioService.enabled
            updateHighScore(preferences[PREFERENCE_HIGHSCORE_KEY, 0])
            stage += this.table
        }
    }

    override fun hide() {
        super.hide()
        engine.run {
            getSystem<PowerUpSystem>().setProcessing(true)
            getSystem<MoveSystem>().setProcessing(true)
            getSystem<PlayerAnimationSystem>().setProcessing(true)
        }
    }

    override fun render(delta: Float) {
        engine.update(delta)
        stage.run {
            viewport.apply()
            act(delta)
            draw()
        }
    }
}