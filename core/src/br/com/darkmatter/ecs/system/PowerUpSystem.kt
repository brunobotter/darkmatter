package br.com.darkmatter.ecs.system

import br.com.darkmatter.V_WIDTH
import br.com.darkmatter.ecs.component.*
import br.com.darkmatter.event.GameEvent
import br.com.darkmatter.event.GameEventManager
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import ktx.ashley.*
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.log.logger
import java.lang.Math.min


private val LOG = logger<PowerUpSystem>()
private const val MAX_SPAWN_INTERVAL = 1.5f
private const val MIN_SPAWN_INTERVAL = 0.9f
private const val POWER_UP_SPEED = -8.75f


private class SpawnPatter(
    type1: PowerUpType = PowerUpType.NONE,
    type2: PowerUpType = PowerUpType.NONE,
    type3: PowerUpType = PowerUpType.NONE,
    type4: PowerUpType = PowerUpType.NONE,
    type5: PowerUpType = PowerUpType.NONE,
    val types: GdxArray<PowerUpType> = gdxArrayOf(type1, type2, type3, type4, type5)
)

class PowerUpSystem(
    private val gameEventManager: GameEventManager
) : IteratingSystem(allOf(PowerUpComponent::class, TransformComponent::class).exclude(RemoveComponent::class).get()) {

    private val playerBoudingRect = Rectangle()
    private val powerUpBoudingRect = Rectangle()
    private val playerEntities by lazy {
        engine.getEntitiesFor(
            allOf(PlayerComponent::class).exclude(RemoveComponent::class).get()
        )
    }
    private var spawnTime = 0f
    private val spawnPatters = gdxArrayOf(
        SpawnPatter(type1 = PowerUpType.SPEED_1, type2 = PowerUpType.SPEED_2, type5 = PowerUpType.SHIELD),
        SpawnPatter(type1 = PowerUpType.SPEED_2, type2 = PowerUpType.LIFE, type5 = PowerUpType.SPEED_1),
        SpawnPatter(type2 = PowerUpType.SPEED_1, type4 = PowerUpType.SPEED_1, type5 = PowerUpType.SPEED_1),
        SpawnPatter(type2 = PowerUpType.SPEED_1, type4 = PowerUpType.SPEED_1),
        SpawnPatter(
            type1 = PowerUpType.SHIELD,
            type2 = PowerUpType.SHIELD,
            type4 = PowerUpType.LIFE,
            type5 = PowerUpType.SPEED_2
        )
    )
    private val currentSpawsPattern = GdxArray<PowerUpType>(spawnPatters.size)

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        spawnTime -= deltaTime
        if (spawnTime <= 0f) {
            spawnTime = MathUtils.random(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL)
            if (currentSpawsPattern.isEmpty) {
                currentSpawsPattern.addAll(spawnPatters[MathUtils.random(0, spawnPatters.size - 1)].types)
                LOG.debug { "Next pattern: $currentSpawsPattern" }
            }
            val powerUpType = currentSpawsPattern.removeIndex(0)
            if (powerUpType == PowerUpType.NONE) {
                //nothing to spawn
                return
            }
            spawnPowerUp(powerUpType, x = 1f * MathUtils.random(0, V_WIDTH - 1), y = 16f)
        }
    }

    private fun spawnPowerUp(powerUpType: PowerUpType, x: Float, y: Float) {
        engine.entity {
            with<TransformComponent> {
                setInitialPosition(x, y, 0f)
                LOG.debug { "Spawn power of type $powerUpType at $position" }
            }
            with<PowerUpComponent> { type = powerUpType }
            with<AnimationComponent> { type = powerUpType.animationType }
            with<GraphicComponent>()
            with<MoveComponent> { speed.y = POWER_UP_SPEED }
        }
    }

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity |entity| must have a FacingComponent. entity=$entity" }
        if (transform.position.y <= 1f) {
            //power up is not collect in time
            entity.addComponent<RemoveComponent>(engine)
            return
        }

        playerEntities.forEach { player ->
            player[TransformComponent.mapper]?.let { playerTransform ->
                playerBoudingRect.set(
                    playerTransform.position.x,
                    playerTransform.position.y,
                    playerTransform.size.x,
                    playerTransform.size.y
                )
                playerBoudingRect.set(
                    transform.position.x,
                    transform.position.y,
                    transform.size.x,
                    transform.size.y
                )
                if (playerBoudingRect.overlaps(powerUpBoudingRect)) {
                    collectPowerUp(player, entity)
                }
            }
        }
    }

    private fun collectPowerUp(player: Entity, powerUp: Entity) {
        val powerUpComp = player[PowerUpComponent.mapper]
        require(powerUpComp != null) { "Entity |entity| must have a FacingComponent. entity=$player" }
        powerUpComp.type.also { powerUpType ->
            LOG.debug { "Picking up power up of type ${powerUpComp.type}" }
            player[MoveComponent.mapper]?.let { it.speed.y += powerUpType.speedGain }
            player[PlayerComponent.mapper]?.let {
                it.life = min(it.maxLife, it.life + powerUpType.lifeGain)
                it.shield = min(it.maxShiel, it.shield + powerUpType.shieldGain) }
            gameEventManager.dispatchEvent(GameEvent.CollectPowerUp.apply {
                this.player = player
                this.type = powerUpType
            })
            powerUp.addComponent<RemoveComponent>(engine)
        }
    }

}