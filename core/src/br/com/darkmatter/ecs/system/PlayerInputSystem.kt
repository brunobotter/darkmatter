package br.com.darkmatter.ecs.system

import br.com.darkmatter.ecs.component.FacingComponent
import br.com.darkmatter.ecs.component.FacingDirection
import br.com.darkmatter.ecs.component.PlayerComponent
import br.com.darkmatter.ecs.component.TransformComponent
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.allOf
import ktx.ashley.get
private const val TOUCH_TOLETANCE_DISTANCE =  0.2f
class PlayerInputSystem(
    private val gameViewport: Viewport
) : IteratingSystem(allOf(PlayerComponent::class, TransformComponent::class, FacingComponent::class).get()){
    private val tmpVec = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val facing: FacingComponent? =  entity[FacingComponent.mapper]
        require(facing!=null){"Entity |entity| must have a FacingComponent. entity=$entity"}
        val transform: TransformComponent? =  entity[TransformComponent.mapper]
        require(transform!=null){"Entity |entity| must have a TransformComponent. entity=$entity"}
        tmpVec.x = Gdx.input.x.toFloat()
        gameViewport.unproject(Vector2(Gdx.input.x.toFloat(), 0f))
        val diffX = tmpVec.x - transform.position.x - transform.size.x * 0.5f
        facing.direction = when{
            diffX < -TOUCH_TOLETANCE_DISTANCE -> FacingDirection.LEFT
            diffX > TOUCH_TOLETANCE_DISTANCE -> FacingDirection.RIGHT
            else -> FacingDirection.DEFAULT
        }
    }
}