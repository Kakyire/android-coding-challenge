package com.syftapp.codetest.posts

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.syftapp.codetest.Navigation
import com.syftapp.codetest.R
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.databinding.ActivityPostsBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PostsActivity : AppCompatActivity(), PostsView, KoinComponent {

    private val presenter: PostsPresenter by inject()
    private lateinit var navigation: Navigation

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
        presenter.bind(this)
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
        postsAdapter = PostsAdapter(posts, presenter)
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
