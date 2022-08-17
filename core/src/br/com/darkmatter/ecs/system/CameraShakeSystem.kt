package br.com.darkmatter.ecs.system

import br.com.darkmatter.event.GameEvent
import br.com.darkmatter.event.GameEventListener
import br.com.darkmatter.event.GameEventManager
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool
import ktx.collections.GdxArray

private class CameraShake: Pool.Poolable{
  var maxDistortion = 0f
  var duration = 0f
  lateinit var camera: Camera
  private var storeCameraPos = true
  private var originCamPosition = Vector3()
  private var currentDuration = 0f

    fun update(deltaTime: Float): Boolean{
        if(storeCameraPos){
            storeCameraPos = false
            originCamPosition.set(camera.position)
        }
        if(currentDuration < duration){
            val currentPower = maxDistortion * ((duration - currentDuration)/ duration)
            camera.position.x = originCamPosition.x + MathUtils.random(-1f,1f) * currentPower
            camera.position.y = originCamPosition.y + MathUtils.random(-1f,1f) * currentPower
            camera.update()
            currentDuration += deltaTime
            return false
        }
        camera.position.set(originCamPosition)
        camera.update()
        return true
    }

    override fun reset() {
        maxDistortion = 0f
        duration = 0f
        currentDuration = 0f
        originCamPosition.set(Vector3.Zero)
        storeCameraPos = true
    }

}

private class  CameraShakePool(private val gameCamera: Camera): Pool<CameraShake>(){
    override fun newObject() = CameraShake().apply {
        this.camera = gameCamera
    }
}

class CameraShakeSystem(
    camera: Camera,
    private val gameEventManager: GameEventManager
) : EntitySystem(), GameEventListener{
private val shakePool = CameraShakePool(camera)
private val activShake = GdxArray<CameraShake>()

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.PlayerHit::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(this)
    }

    override fun update(deltaTime: Float) {
        if(!activShake.isEmpty){
            val shake = activShake.first()
            if(shake.update(deltaTime)){
                activShake.removeIndex(0)
                shakePool.free(shake)
            }
        }
    }

    override fun onEvent(event: GameEvent) {
        if(activShake.size < 4 ){
            activShake.add(shakePool.obtain().apply {
                duration = 0.25f
                maxDistortion = 0.25f
            })
        }
    }
}