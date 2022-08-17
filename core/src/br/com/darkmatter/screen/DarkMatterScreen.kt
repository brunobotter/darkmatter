package br.com.darkmatter.screen

import br.com.darkmatter.DarkMatter
import br.com.darkmatter.event.GameEventManager
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen

abstract class DarkMatterScreen(
    val game: DarkMatter,
    val batch: Batch = game.batch,
    val gameViewport: Viewport= game.gameViewPort,
    val uiViewPort: Viewport= game.uiViewPort,
    val engine: Engine = game.engine,
    val gameEventManager: GameEventManager = game.gameEventManager
) : KtxScreen {

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        uiViewPort.update(width, height, true)
    }

}