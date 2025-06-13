package com.research.oproom
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH

interface OproomApi {
    @PATCH("oproom_api/update/post.php")
    public fun patchPost(@Body post: Post): Call<Post>

}