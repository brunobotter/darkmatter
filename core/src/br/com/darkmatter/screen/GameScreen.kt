package br.com.darkmatter.screen

import br.com.darkmatter.DarkMatter
import br.com.darkmatter.UNIT_SCALE
import br.com.darkmatter.V_WIDTH
import br.com.darkmatter.ecs.component.*
import br.com.darkmatter.ecs.system.DAMAGE_AREA_HEIGHT
import br.com.darkmatter.event.GameEvent
import br.com.darkmatter.event.GameEventListener
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.ashley.entity
import ktx.ashley.with
import ktx.log.Logger
import ktx.log.logger
import kotlin.math.min

private val LOG: Logger = logger<GameScreen>()
private const val MAX_DELTA_TIME = 1/20f
class GameScreen(game: DarkMatter) : DarkMatterScreen(game), GameEventListener {


    override fun show() {
        LOG.debug { "First Screen is shown" }
        gameEventManager.addListener(GameEvent::class, this)
        spawnPlayer()
        engine.entity{
            with<TransformComponent>{
                size.set(
                    V_WIDTH.toFloat(),
                    DAMAGE_AREA_HEIGHT
                )
            }
            with<AnimationComponent>{type = AnimationType.DARM_MATTER  }
            with<GraphicComponent>()
        }
    }

    private fun spawnPlayer() {
        val playerShip = engine.entity {
            with<TransformComponent> {
                setInitialPosition(4.5f, 8f, -1f)
            }
            with<MoveComponent>()
            with<GraphicComponent>()
            with<PlayerComponent>()
            with<FacingComponent>()
        }
        engine.entity {
            with<TransformComponent>()
            with<AttachComponent> {
                entity = playerShip
                offset.set(1f * UNIT_SCALE, -6f * UNIT_SCALE)
            }
            with<GraphicComponent>()
            with<AnimationComponent> {
                type = AnimationType.FIRE
            }
        }
    }

    override fun render(delta: Float) {

        (game.batch as SpriteBatch).renderCalls = 0
        engine.update(min(MAX_DELTA_TIME,delta))
    //LOG.debug { "Rendercalls: ${(game.batch as SpriteBatch).renderCalls}" }
    }

    override fun onEvent(event: GameEvent) {
        when(event){
          is GameEvent.PlayerDeath ->{
              spawnPlayer()
          }
            GameEvent.CollectPowerUp -> TODO()
        }
    }


    override fun hide() {
        super.hide()
        gameEventManager.removeListener(this)
    }
}
