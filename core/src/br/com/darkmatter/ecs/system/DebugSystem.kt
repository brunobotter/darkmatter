package br.com.darkmatter.ecs.system

import br.com.darkmatter.ecs.component.GraphicComponent
import br.com.darkmatter.ecs.component.PlayerComponent
import br.com.darkmatter.ecs.component.TransformComponent
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.getSystem
import kotlin.math.min

private const val WINDOW_INFO_UPDATE_RATE = 0.25f

class DebugSystem: IntervalIteratingSystem(allOf(PlayerComponent::class).get(), WINDOW_INFO_UPDATE_RATE) {

    init {
        setProcessing(true)
    }

    override fun processEntity(entity: Entity) {
        val transform: TransformComponent? = entity[TransformComponent.mapper]
        require(transform != null) { "Entity |entity| must have a transform component entity=$entity" }
        val player: PlayerComponent? = entity[PlayerComponent.mapper]
        require(player != null) { "Entity |entity| must have a transform component entity=$entity" }
        when{
            Gdx.input.isKeyPressed(Input.Keys.NUM_1) ->{
              //kill player
                transform.position.y = 0f
                transform.position.x = 0f
                player.life = 0f
                player.shield = 0f
            }
            Gdx.input.isKeyPressed(Input.Keys.NUM_2) ->{
                //add shield
                player.shield = min(player.maxShiel, player.shield + 25f)
            }
            Gdx.input.isKeyPressed(Input.Keys.NUM_3) ->{
                //remove shield
                player.shield = min(0f, player.shield - 25f)
            }
            Gdx.input.isKeyPressed(Input.Keys.NUM_4) ->{
                //disable moviment
                engine.getSystem<MoveSystem>().setProcessing(false)
            }
            Gdx.input.isKeyPressed(Input.Keys.NUM_5) ->{
                //enable moviment
                engine.getSystem<MoveSystem>().setProcessing(true)
            }
        }
        Gdx.graphics.setTitle("DM Debug - pos:${transform.position}, life:${player.life}, shield:${player.shield}")
    }
}