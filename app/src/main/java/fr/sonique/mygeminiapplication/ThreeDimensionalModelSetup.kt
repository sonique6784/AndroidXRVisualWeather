package fr.sonique.mygeminiapplication


/**
 * Contains specific model setup for 3D models
 * Each model may have different size and scale
 * this class aim to store the data that is right
 * for Android XR so the model is properly scaled
 * and positioned for the current scene.
 */
data class ThreeDimensionalModelSetup(val model: String,
                                      val scale: Float,
                                      val position: FloatArray, // (x,y,z)
                                      val animation: String? = null) // default anim to play
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ThreeDimensionalModelSetup

        if (scale != other.scale) return false
        if (model != other.model) return false
        if (!position.contentEquals(other.position)) return false
        if (animation != other.animation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scale.hashCode()
        result = 31 * result + model.hashCode()
        result = 31 * result + position.contentHashCode()
        result = 31 * result + (animation?.hashCode() ?: 0)
        return result
    }
}
