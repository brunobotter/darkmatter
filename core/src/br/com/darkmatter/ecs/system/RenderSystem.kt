package br.com.darkmatter.ecs.system

import br.com.darkmatter.ecs.component.GraphicComponent
import br.com.darkmatter.ecs.component.PowerUpType
import br.com.darkmatter.ecs.component.TransformComponent
import br.com.darkmatter.event.GameEvent
import br.com.darkmatter.event.GameEventListener
import br.com.darkmatter.event.GameEventManager
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use
import ktx.log.Logger
import ktx.log.logger
import kotlin.math.min

private val LOG: Logger = logger<RenderSystem>()

class RenderSystem(
    private val batch: Batch,
    private val gameViewPort: Viewport,
    private val uiViewPort: FitViewport,
    backgroundTexture: Texture,
    private val gameEventManager: GameEventManager
) : GameEventListener, SortedIteratingSystem(allOf(TransformComponent::class, GraphicComponent::class).get(),
    compareBy { entity -> entity[TransformComponent.mapper] }
) {

    private val background = Sprite(backgroundTexture.apply {
        setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
    })

    private val backgroundScroolSpeed = Vector2(0.03f, -0.25f)

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.CollectPowerUp::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.CollectPowerUp::class, this)
    }


    override fun update(deltaTime: Float) {
        uiViewPort.apply()
        batch.use(uiViewPort.camera.combined) {
            //render background
            background.run {
                backgroundScroolSpeed.y = min(-0.25f, backgroundScroolSpeed.y + deltaTime * (1f / 10f))
                scroll(backgroundScroolSpeed.x * deltaTime, backgroundScroolSpeed.y * deltaTime)
                draw(batch)
            }
        }
        forceSort()
        gameViewPort.apply()
        batch.use(gameViewPort.camera.combined) {
            //rende entity
            super.update(deltaTime)
        }

    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform: TransformComponent? = entity[TransformComponent.mapper]
        require(transform != null) { "Entity |entity| must have a transform component entity=$entity" }
        val grapgic: GraphicComponent? = entity[GraphicComponent.mapper]
        require(grapgic != null) { "Entity |entity| must have a transform component entity=$entity" }
        if (grapgic.sprite.texture == null) {
            LOG.error { "Entity has no texture for renderer" }
            return
        }
        grapgic.sprite.run {
            rotation = transform.rotationDeg
            setBounds(
                transform.interpolationPosition.x,
                transform.interpolationPosition.y,
                transform.size.x,
                transform.size.y
            )
            draw(batch)
        }
    }

    override fun onEvent(event: GameEvent) {
        val powerUpEvent = event as GameEvent.CollectPowerUp
        if (powerUpEvent.type == PowerUpType.SPEED_1) {
            backgroundScroolSpeed.y -= 0.25f
        } else if (powerUpEvent.type == PowerUpType.SPEED_2)
            backgroundScroolSpeed.y -= 0.5f

    }
}