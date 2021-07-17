package com.syftapp.codetest.posts

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.syftapp.codetest.Navigation
import com.syftapp.codetest.R
import com.syftapp.codetest.TOTAL_POSTS
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.databinding.ActivityPostsBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PostsActivity : AppCompatActivity(), PostsView, KoinComponent {

    private val presenter: PostsPresenter by inject()
    private lateinit var navigation: Navigation

    private var isScrolling = false

    private lateinit var postsAdapter: PostsAdapter

    private lateinit var binding: ActivityPostsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigation = Navigation(this)

        binding.listOfPosts.apply {
            layoutManager = LinearLayoutManager(this@PostsActivity)
            val separator =
                DividerItemDecoration(this@PostsActivity, DividerItemDecoration.VERTICAL)
            addItemDecoration(separator)
        }

        loadMorePost()

        presenter.bind(this)
    }

    //load more posts if not at the end of the posts data from remote
    //or if all data has not been cached
    private fun loadMorePost() {


        binding.listOfPosts.apply {
            val layoutManager = layoutManager as LinearLayoutManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        isScrolling = true
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val totalItemCount = layoutManager.itemCount
                    val visibleItems = layoutManager.childCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()


                    //check if all posts has been loaded
                    val reachEndOfPage = totalItemCount == TOTAL_POSTS

                    if (isScrolling &&
                        !reachEndOfPage &&
                        (visibleItems + firstVisibleItem) >= totalItemCount
                    ) {
                        presenter.loadMorePosts()
                        isScrolling = false
                    }
                }
            })


        }
    }

    override fun onDestroy() {
        presenter.unbind()
        super.onDestroy()
    }

    override fun render(state: PostScreenState) {
        when (state) {
            is PostScreenState.Loading -> showLoading()
            is PostScreenState.DataAvailable -> showPosts(state.posts)
            is PostScreenState.Error -> showError(getString(R.string.load_posts_error_message))
            is PostScreenState.FinishedLoading -> hideLoading()
            is PostScreenState.PostSelected -> navigation.navigateToPostDetail(state.post.id)
        }
    }

    private fun showLoading() {
        binding.apply {
            error.visibility = View.GONE
            loading.visibility = View.VISIBLE
        }
    }

    private fun hideLoading() {
        binding.loading.visibility = View.GONE
    }

    private fun showPosts(posts: List<Post>) {
        // this is a fairly crude implementation, if it was Flowable, it would
        // be better to use DiffUtil and consider notifyRangeChanged, notifyItemInserted, etc
        // to preserve animations on the RecyclerView
        postsAdapter = PostsAdapter(presenter)
            .apply { submitList(posts) }
        binding.listOfPosts.apply {
            adapter = postsAdapter
            visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        binding.error.apply {
            visibility = View.VISIBLE
            text = message
        }
    }
}
