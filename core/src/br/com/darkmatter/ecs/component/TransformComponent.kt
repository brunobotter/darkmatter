package br.com.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.gdx.math.Vector3

import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2
import ktx.math.vec3

class TransformComponent : Component, Pool.Poolable, Comparable<TransformComponent> {

    val position = vec3()
    val prevPosition = vec3()
    val interpolationPosition = vec3()
    val size = vec2(1f, 1f)
    var rotationDeg = 0f

    override fun reset() {
        position.set(Vector3.Zero)
        prevPosition.set(Vector3.Zero)
        interpolationPosition.set(Vector3.Zero)
        size.set(1f, 1f)
        rotationDeg = 0f
    }

    fun setInitialPosition(x: Float, y: Float, z: Float){
        position.set(x,y,z)
        prevPosition.set(x,y,z)
        interpolationPosition.set(x,y,z)
    }

    override fun compareTo(other: TransformComponent): Int {
        position.z.compareTo(other.position.z).let { return if (it != 0) it else position.y.compareTo(other.position.y) }
    }

    //companion object nao guarda estado, como no java que o static guarda estado
    companion object {
        val mapper: ComponentMapper<TransformComponent> = mapperFor<TransformComponent>()
    }

}
