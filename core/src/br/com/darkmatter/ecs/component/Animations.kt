package br.com.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.collections.GdxArray

private const val DEFAULT_FRAME_DURATION = 1 / 20f

enum class AnimationType(
    val atlasKey: String,
    val playMode: Animation.PlayMode = LOOP,
    val speedRate: Float = 1f
) {
    NONE(""),
    DARM_MATTER("dark_matter", speedRate = 3f),
    FIRE("fire"),
    SPEED_1("orb_blue", speedRate = 0.5f),
    SPEED_2("orb_yelow", speedRate = 0.5f),
    LIFE("life", speedRate = 0.5f),
    SHIELD("shield", speedRate = 0.5f)
}

class Animation2D(
    val type: AnimationType,
    keyFrames: GdxArray<out TextureRegion>,
    playMode: PlayMode = LOOP,
    speedRate: Float = 1f
): Animation<TextureRegion>((DEFAULT_FRAME_DURATION) / speedRate, keyFrames, playMode)

class AnimationComponent : Component, Pool.Poolable {
    lateinit var animation: Animation2D
    var stateTime = 0f
    var type = AnimationType.NONE

    override fun reset() {
        type = AnimationType.NONE
    }

    companion object {
        val mapper: ComponentMapper<AnimationComponent> = mapperFor<AnimationComponent>()
    }
}
