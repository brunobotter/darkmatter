package br.com.darkmatter.ecs.system

import br.com.darkmatter.ecs.component.*
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.addComponent
import ktx.ashley.allOf
import ktx.ashley.get

class AttachSystem : EntityListener,
    IteratingSystem(allOf(AttachComponent::class,
        TransformComponent::class, GraphicComponent::class).get()){

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }
    override fun entityAdded(entity: Entity) {
    }

    override fun entityRemoved(removeEntity: Entity) {
        entities.forEach{entity ->
            entity[AttachComponent.mapper]?.let{ attach ->
                if(attach.entity == removeEntity){
                    entity.addComponent<RemoveComponent>(engine)
                }
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val graphics = entity[GraphicComponent.mapper]
        require(graphics != null) { "Entity |entity| must have a FacingComponent. entity=$entity" }
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity |entity| must have a FacingComponent. entity=$entity" }
        val attach = entity[AttachComponent.mapper]
        require(attach != null) { "Entity |entity| must have a FacingComponent. entity=$entity" }
         //update position
        attach.entity[TransformComponent.mapper]?.let{attachTransform ->
            transform.interpolationPosition.set(
                attachTransform.interpolationPosition.x + attach.offset.x,
                attachTransform.interpolationPosition.y + attach.offset.y,
                transform.position.z
            )
        }
        //update graphic alpha value
        attach.entity[GraphicComponent.mapper]?.let{attachGraphic ->
            graphics.sprite.setAlpha(attachGraphic.sprite.color.a)
        }
    }
}