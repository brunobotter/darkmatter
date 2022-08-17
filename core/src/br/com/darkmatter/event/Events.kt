package br.com.darkmatter.event

import br.com.darkmatter.ecs.component.PowerUpType
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import ktx.collections.GdxSet
import kotlin.reflect.KClass

sealed class  GameEvent{
    object PlayerDeath: GameEvent(){
        var distance = 0f
        override fun toString() = "PlayerDeath(distance=${distance})"
    }

    object CollectPowerUp: GameEvent(){
        lateinit var player: Entity
        var type = PowerUpType.NONE

        override fun toString() = "CollectPower(player=${player}, type=${type})"
    }

    object PlayerHit: GameEvent(){
        lateinit var player: Entity
        var life = 0f
        var maxLife = 0f

        override fun toString() = "PlayerHit(player=${player}, life=${life}, maxLife${maxLife})"
    }
}

interface GameEventListener{
    fun onEvent(event:GameEvent)
}

class GameEventManager{
    private val listeners = ObjectMap<KClass<out GameEvent>, GdxSet<GameEventListener>>()

    fun addListener(type: KClass<out GameEvent>, listener: GameEventListener){
        var eventListener = listeners[type]
        if(eventListener==null){
            eventListener = GdxSet()
            listeners.put(type, eventListener)
        }
        eventListener.add(listener)
    }

    fun removeListener(type: KClass<out GameEvent>, listener: GameEventListener) {
        listeners[type]?.remove(listener)
    }

    fun removeListener(listener: GameEventListener) {
        listeners.values().forEach{ it.remove(listener)}
    }

    fun dispatchEvent(event: GameEvent ){
        listeners[event::class]?.forEach{ it.onEvent(event)}
    }
}