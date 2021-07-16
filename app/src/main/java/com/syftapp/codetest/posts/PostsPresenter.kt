package com.syftapp.codetest.posts

import com.syftapp.codetest.PAGE_LIMIT
import com.syftapp.codetest.TOTAL_POSTS
import com.syftapp.codetest.data.model.domain.Post
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.component.KoinComponent

class PostsPresenter(private val getPostsUseCase: GetPostsUseCase) : KoinComponent {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var view: PostsView
    private var currentPage = 1

    fun bind(view: PostsView) {
        this.view = view
        compositeDisposable.add(loadPosts())
    }
    fun loadMorePosts() {
        val endOfPostData = currentPage * PAGE_LIMIT >= TOTAL_POSTS
        if (endOfPostData) return
        currentPage++
        loadPosts(currentPage, true)
    }

    fun unbind() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    fun showDetails(post: Post) {
        view.render(PostScreenState.PostSelected(post))
    }

    private fun loadPosts(page: Int = 1, fetch: Boolean = false) =
        getPostsUseCase.execute(page, fetch)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { view.render(PostScreenState.Loading) }
            .doAfterTerminate { view.render(PostScreenState.FinishedLoading) }
            .subscribe(
                { view.render(PostScreenState.DataAvailable(it)) },
                { view.render(PostScreenState.Error(it)) }
            )
}