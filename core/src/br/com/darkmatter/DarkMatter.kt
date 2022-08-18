package br.com.darkmatter

import br.com.darkmatter.asset.*
import br.com.darkmatter.audio.DefaultAudioService
import br.com.darkmatter.ecs.system.*
import br.com.darkmatter.event.GameEventManager
import br.com.darkmatter.screen.LoadingScreen
import br.com.darkmatter.ui.createSkin
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.profiling.GLProfiler
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.collections.gdxArrayOf
import ktx.log.logger

private val LOG = logger<DarkMatter>()
const val V_WIDTH_PIXELS = 135
const val V_HEIGHT_PIXELS = 240
const val V_WIDTH = 9
const val V_HEIGHT = 16
const val UNIT_SCALE = 1 / 8f
const val PREFERENCE_MUSIC_ENABLED_KEY = "musicEnabled"
const val PREFERENCE_HIGHSCORE_KEY = "highScore"
class DarkMatter : KtxGame<KtxScreen>() {

    val gameViewport = FitViewport(V_WIDTH.toFloat(), V_HEIGHT.toFloat())
    val stage: Stage by lazy {
        val result = Stage(FitViewport(V_WIDTH_PIXELS.toFloat(), V_HEIGHT_PIXELS.toFloat()))
        Gdx.input.inputProcessor = result
        result
    }
    val assets: AssetStorage by lazy {
        KtxAsync.initiate()
        AssetStorage()
    }
    val gameEventManager by lazy { GameEventManager() }
    val audioService by lazy { DefaultAudioService(assets) }
    val engine by lazy {
        PooledEngine().apply {
            val atlas = assets[TextureAtlasAsset.GRAPHICS.descriptor]

            addSystem(DebugSystem(gameEventManager, audioService))
            addSystem(PowerUpSystem(gameEventManager, audioService).apply {
                setProcessing(false)
            })
            addSystem(PlayerInputSystem(gameViewport))
            addSystem(MoveSystem(gameEventManager).apply {
                setProcessing(false)
            })
            addSystem(DamageSystem(gameEventManager, audioService))
            addSystem(
                PlayerAnimationSystem(
                    atlas.findRegion("ship_base"),
                    atlas.findRegion("ship_left"),
                    atlas.findRegion("ship_right")
                ).apply {
                    setProcessing(false)
                }
            )
            addSystem(AttachSystem())
            addSystem(AnimationSystem(atlas))
            addSystem(CameraShakeSystem(gameViewport.camera, gameEventManager))
            addSystem(PlayerColorSystem(gameEventManager))
            addSystem(
                RenderSystem(
                    stage,
                    assets[ShaderProgramAsset.OUTLINE.descriptor],
                    gameViewport,
                    gameEventManager,
                    assets[TextureAsset.BACKGROUND.descriptor]
                )
            )
            addSystem(RemoveSystem(gameEventManager))
        }
    }
    val preferences: Preferences by lazy { Gdx.app.getPreferences("dark-matter") }
    private val profiler by lazy { GLProfiler(Gdx.graphics) }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_ERROR
        profiler.enable()

        // load skin and go to LoadingScreen for remaining asset loading
        var old = System.currentTimeMillis()
        val assetRefs = gdxArrayOf(
            TextureAtlasAsset.values().filter { it.isSkinAtlas }.map { assets.loadAsync(it.descriptor) },
            BitmapFontAsset.values().map { assets.loadAsync(it.descriptor) },
            I18NBundleAsset.values().map { assets.loadAsync(it.descriptor) }
        ).flatten()
        KtxAsync.launch {
            assetRefs.joinAll()
            // skin assets loaded -> create skin
            LOG.debug { "It took ${(System.currentTimeMillis() - old) * 0.001f} seconds to load skin assets" }
            old = System.currentTimeMillis()
            createSkin(assets)
            LOG.debug { "It took ${(System.currentTimeMillis() - old) * 0.001f} seconds to create the skin" }
            // go to LoadingScreen to load remaining assets
            addScreen(LoadingScreen(this@DarkMatter))
            setScreen<LoadingScreen>()
        }
    }

    override fun render() {
        profiler.reset()
        super.render()
    }

    override fun dispose() {
        LOG.debug { "Dispose game with ${this.screens.size} screen(s)" }
        LOG.debug { "Last number of draw calls: ${profiler.drawCalls}" }
        LOG.debug { "Last number of texture bindings: ${profiler.textureBindings}" }
        MusicAsset.values().forEach {
            LOG.debug { "Reference count for music $it is ${assets.getReferenceCount(it.descriptor)}" }
        }

        super.dispose()
        assets.dispose()
        stage.dispose()
    }
}