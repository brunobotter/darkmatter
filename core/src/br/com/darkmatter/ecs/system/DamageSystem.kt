package br.com.darkmatter.ecs.system

import br.com.darkmatter.asset.SoundAsset
import br.com.darkmatter.audio.AudioService
import br.com.darkmatter.ecs.component.*
import br.com.darkmatter.event.GameEvent
import br.com.darkmatter.event.GameEventManager
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.*
import kotlin.math.max

const val DAMAGE_AREA_HEIGHT = 2f
private const val DAMAGE_PER_SECOND = 25f
private const val DAM_SOUND_LENGTH = 0.6f
private const val BLOCK_SOUND_LENGTH = 0.6f
private const val DEATH_EXPLOSION_SIZE = 1.5f
private const val DEATH_EXPLOSION_DURATION = 0.9f

class DamageSystem(
    private val gameEventManager: GameEventManager,
    private val audioService: AudioService
) :
    IteratingSystem(allOf(PlayerComponent::class, TransformComponent::class).exclude(RemoveComponent::class).get()) {
    private var damageTaken = false
    private var blocked = false
    private var damageSoundCountdown = 0f
    private var blockSoundCountdown = 0f

    override fun update(deltaTime: Float) {
        damageTaken = false
        blocked = false
        super.update(deltaTime)
        if (blocked) {
            if (blockSoundCountdown <= 0f) {
                audioService.play(SoundAsset.BLOCK)
                blockSoundCountdown = BLOCK_SOUND_LENGTH
            } else {
                blockSoundCountdown -= deltaTime
            }
        }
        if (damageTaken) {
            if (damageSoundCountdown <= 0f) {
                audioService.play(SoundAsset.DAMAGE)
                damageSoundCountdown = DAM_SOUND_LENGTH
            } else {
                damageSoundCountdown -= deltaTime
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity |entity| must have a TransformComponent. entity=$entity" }
        val player = entity[PlayerComponent.mapper]
        require(player != null) { "Entity |entity| must have a PlayerComponent. entity=$entity" }

        if (transform.position.y <= DAMAGE_AREA_HEIGHT) {
            var damage = DAMAGE_PER_SECOND * deltaTime
            if (player.shield > 0f) {
                val blockAmount = player.shield
                blocked = true
                player.shield = max(player.shield - damage, 0f)
                damage -= blockAmount
                gameEventManager.dispatchEvent(GameEvent.PlayerBlock.apply {
                    shield = player.shield
                    maxShield = player.maxShield
                })

                if (damage <= 0f) {
                    // entire damage was blocked
                    return
                }
            }

            damageTaken = true
            player.life -= damage

            gameEventManager.dispatchEvent(GameEvent.PlayerHit.apply {
                this.player = entity
                life = player.life
                maxLife = player.maxLife
            })
            if (player.life <= 0f) {
                entity.addComponent<RemoveComponent>(engine) {
                    delay = 1f
                }
                entity[GraphicComponent.mapper]?.sprite?.setAlpha(0f)
                engine.entity {
                    with<TransformComponent> {
                        size.set(DEATH_EXPLOSION_SIZE, DEATH_EXPLOSION_SIZE)
                        setInitialPosition(transform.position.x, transform.position.y, 2f)
                    }
                    with<AnimationComponent> {
                        type = AnimationType.EXPLOSION
                    }
                    with<GraphicComponent>()
                    with<RemoveComponent> {
                        delay = DEATH_EXPLOSION_DURATION
                    }
                }
                audioService.play(SoundAsset.EXPLOSION)
            }
        }
    }
}