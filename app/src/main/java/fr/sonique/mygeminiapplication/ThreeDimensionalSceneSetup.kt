package fr.sonique.mygeminiapplication

/**
 * Contains specific scene setup for 3D models.
 * a scene can have one or more 3D models.
 * the models can be combine and can appear
 * next to each others. to prevent rendering model on top
 * of each other, this class also aim to provide required
 * modifications (scale, position...) to the models.
 * so the render as a beautiful and coherent scene.
 *
 * Example: Sun and Clouds with sun on the left of clouds and the sun is
 * slightly behind the clouds.
 */
data class ThreeDimensionalSceneSetup(
    val models: List<ThreeDimensionalModelSetup>,
    val modifications: List<FloatArray>) // (x,y,z,scale)
