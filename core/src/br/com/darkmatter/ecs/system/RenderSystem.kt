package br.com.darkmatter.ecs.system

import br.com.darkmatter.ecs.component.GraphicComponent
import br.com.darkmatter.ecs.component.TransformComponent
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use
import ktx.log.Logger
import ktx.log.logger

private val LOG: Logger = logger<RenderSystem>()
class RenderSystem(
    private val batch: Batch,
    private val gameViewPort: Viewport
) : SortedIteratingSystem(allOf(TransformComponent::class, GraphicComponent::class).get(),
    compareBy { entity -> entity[TransformComponent.mapper] }
) {
    override fun update(deltaTime: Float) {
        forceSort()
        gameViewPort.apply()
        batch.use(gameViewPort.camera.combined){
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform: TransformComponent? = entity[TransformComponent.mapper]
        require(transform != null) { "Entity |entity| must have a transform component entity=$entity" }
        val grapgic: GraphicComponent? = entity[GraphicComponent.mapper]
        require(grapgic != null) { "Entity |entity| must have a transform component entity=$entity" }
        if(grapgic.sprite.texture == null){
            LOG.error{ "Entity has no texture for renderer"}
            return
        }
        grapgic.sprite.run{
            rotation = transform.rotationDeg
            setBounds(transform.interpolationPosition.x, transform.interpolationPosition.y, transform.size.x, transform.size.y)
            draw(batch)
        }
    }
}