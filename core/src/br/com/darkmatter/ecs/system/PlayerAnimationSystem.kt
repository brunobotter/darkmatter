package br.com.darkmatter.ecs.system

import br.com.darkmatter.ecs.component.FacingComponent
import br.com.darkmatter.ecs.component.FacingDirection
import br.com.darkmatter.ecs.component.GraphicComponent
import br.com.darkmatter.ecs.component.PlayerComponent
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.TextureRegion
import ktx.ashley.allOf
import ktx.ashley.get

class PlayerAnimationSystem(
    private val defaultRegion: TextureRegion,
    private val lefttRegion: TextureRegion,
    private val rightRegion: TextureRegion
): IteratingSystem(allOf(PlayerComponent::class, FacingComponent::class, GraphicComponent::class).get()),
EntityListener{

    private var lastDirection = FacingDirection.DEFAULT
    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(family, this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun entityAdded(entity: Entity) {
        entity[GraphicComponent.mapper]?.setSpriteRegion(defaultRegion)
    }

    override fun entityRemoved(entity: Entity) = Unit

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val facing: FacingComponent? =  entity[FacingComponent.mapper]
        require(facing!=null){"Entity |entity| must have a FacingComponent. entity=$entity"}
        val grapgic: GraphicComponent? = entity[GraphicComponent.mapper]
        require(grapgic != null) { "Entity |entity| must have a transform component entity=$entity" }
        if(facing.direction == lastDirection && grapgic.sprite.texture != null){
            return
        }
        lastDirection = facing.direction
        var region: TextureRegion = when(facing.direction){
            FacingDirection.LEFT -> lefttRegion
            FacingDirection.RIGHT -> rightRegion
            else -> defaultRegion
        }
        grapgic.setSpriteRegion(region)
    }
}