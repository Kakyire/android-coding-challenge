package com.syftapp.codetest.postdetail

import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.repository.BlogRepository
import io.reactivex.Maybe
import org.koin.core.component.KoinComponent

class GetPostUseCase(private val repository: BlogRepository) : KoinComponent {

    fun execute(postId: Int): Maybe<Post> {
        return repository.getPost(postId)
    }

}