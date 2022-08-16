package br.com.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class FacingComponent: Component, Pool.Poolable {

    var direction = FacingDirection.DEFAULT


    override fun reset() {
        direction = FacingDirection.DEFAULT
    }

    companion object{
        val mapper: ComponentMapper<FacingComponent> = mapperFor<FacingComponent>()
    }
}

enum class FacingDirection{
    LEFT, DEFAULT, RIGHT
}