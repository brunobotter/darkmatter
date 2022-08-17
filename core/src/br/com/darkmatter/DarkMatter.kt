package br.com.darkmatter

import br.com.darkmatter.ecs.system.*
import br.com.darkmatter.event.GameEventManager
import br.com.darkmatter.screen.DarkMatterScreen
import br.com.darkmatter.screen.GameScreen
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxGame
import ktx.log.Logger
import ktx.log.logger

private val Logs: Logger = logger<DarkMatter>()
const val UNIT_SCALE: Float = 1 / 16f
const val V_WIDTH = 9
const val V_HEIGHT = 16
const val V_WIDTH_PIXELS = 135
const val V_HEIGHT_PIXELS = 240

class DarkMatter : KtxGame<DarkMatterScreen>() {

    val uiViewPort = FitViewport(V_WIDTH_PIXELS.toFloat(), V_HEIGHT_PIXELS.toFloat())
    val gameViewPort = FitViewport(V_WIDTH.toFloat(), V_HEIGHT.toFloat())
    val batch: Batch by lazy { SpriteBatch() }
    val graphicAtlas by lazy { TextureAtlas(Gdx.files.internal("graphics/graphics.atlas")) }
    val backgroundTexture by lazy { Texture("graphics/background.png") }
    val gameEventManager = GameEventManager()

    val engine: Engine by lazy {
        PooledEngine().apply {
            addSystem(PlayerInputSystem(gameViewPort))
            addSystem(MoveSystem())
            addSystem(PowerUpSystem(gameEventManager))
            addSystem(DamageSystem(gameEventManager))
            addSystem(CameraShakeSystem(gameViewPort.camera,gameEventManager))
            addSystem(PlayerAnimationSystem(
                graphicAtlas.findRegion("player_base"),
                graphicAtlas.findRegion("player_left"),
                graphicAtlas.findRegion("player_right")
            ))
            addSystem(AttachSystem())
            addSystem(AnimationSystem(graphicAtlas))
            addSystem(RenderSystem(batch, gameViewPort, uiViewPort, backgroundTexture, gameEventManager))
            addSystem(RemoveSystem())
            addSystem(DebugSystem())
        }
    }

    override fun create() {
        Gdx.app.logLevel = LOG_DEBUG
        Logs.debug { "Create game instance" }
        //Adiciona tela
        addScreen(GameScreen(this))
        //exibe a tela
        setScreen<GameScreen>()
    }

    override fun dispose() {
        super.dispose()
        Logs.debug { "Sprites im batch: ${(batch as SpriteBatch).maxSpritesInBatch}" }
        batch.dispose()
        graphicAtlas.dispose()
        backgroundTexture.dispose()
    }
}