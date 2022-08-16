package br.com.darkmatter.ecs.system

import br.com.darkmatter.ecs.component.RemoveComponent
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import ktx.ashley.get

class RemoveSystem : IteratingSystem(allOf(RemoveComponent::class).get()) {


    override fun processEntity(entity: Entity, deltaTime: Float) {

        val remove = entity[RemoveComponent.mapper]
        require(remove != null) { "Entity |entity| must have a FacingComponent. entity=$entity" }
        remove.delay -= deltaTime
        if (remove.delay <= 0f) {
            engine.removeEntity(entity)
        }
    }
}