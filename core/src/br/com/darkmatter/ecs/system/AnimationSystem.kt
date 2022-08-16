package br.com.darkmatter.ecs.system

import br.com.darkmatter.ecs.component.*
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.logger
import java.util.*

private val LOG = logger<AnimationSystem>()

class AnimationSystem(
    private val atlas: TextureAtlas
) : IteratingSystem(allOf(AnimationComponent::class, GraphicComponent::class).get()), EntityListener{
    private val animationCache = EnumMap<AnimationType, Animation2D>(AnimationType::class.java)

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(family, this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val aniCmp = entity[AnimationComponent.mapper]
        require(aniCmp != null) { "Entity |entity| must have a FacingComponent. entity=$entity" }
        val graphics = entity[GraphicComponent.mapper]
        require(graphics != null) { "Entity |entity| must have a FacingComponent. entity=$entity" }
        if(aniCmp.type == AnimationType.NONE){
            LOG.error { "No type  specified for animation component $aniCmp for |entity| $entity" }
            return
        }
        if(aniCmp.type == aniCmp.animation.type){
            //animation is correctly set -> update if
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.type)
        }
        val frame = aniCmp.animation.getKeyFrame(aniCmp.stateTime)
        graphics.setSpriteRegion(frame)
    }

    override fun entityAdded(entity: Entity) {
        entity[AnimationComponent.mapper]?.let{ aniCmp ->
            aniCmp.animation = getAnimation(aniCmp.type)
            val frame = aniCmp.animation.getKeyFrame(aniCmp.stateTime)
            entity[GraphicComponent.mapper]?.setSpriteRegion(frame)
        }

    }

    private fun getAnimation(type: AnimationType): Animation2D {
        var animation = animationCache[type]
        if(animation == null){
            var regions = atlas.findRegions(type.atlasKey)
            if(regions.isEmpty){
                LOG.error { "No regions founds for ${type.atlasKey}" }
                regions = atlas.findRegions("error")
                if(regions.isEmpty) throw  GdxRuntimeException("There is no error region in the atlas")
            }else{
                LOG.debug { "Adding animation of type $type with ${regions.size} regions"  }
            }
            animation = Animation2D(type, regions, type.playMode, type.speedRate)
            animationCache[type] = animation
        }
        return animation
    }

    override fun entityRemoved(entity: Entity) {
    }
}