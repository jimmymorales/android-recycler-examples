package dev.jimmymorales.rvdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import dev.jimmymorales.rvdemo.databinding.FragmentMainBinding
import dev.jimmymorales.rvdemo.databinding.FragmentNestedscrollviewExampleBinding
import dev.jimmymorales.rvdemo.databinding.FragmentRecyclerViewBinding
import dev.jimmymorales.rvdemo.databinding.LayoutHeaderViewBinding
import dev.jimmymorales.rvdemo.databinding.LayoutItemViewBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity(R.layout.activity_main)

class MainFragment : Fragment(R.layout.fragment_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(FragmentMainBinding.bind(view)) {
            nsvExampleButton.setOnClickListener {
                findNavController()
                    .navigate(MainFragmentDirections.toNestedScrollViewExampleFragment())
            }
            rvWithTypesExampleButton.setOnClickListener {
                findNavController()
                    .navigate(MainFragmentDirections.toRecyclerViewTypesExampleFragment())
            }
        }
    }
}


/**
 * Shared ModelView
 */
data class UIState(
    val header: String,
    val items: List<String>,
)

class MainViewModel : ViewModel() {
    private val initialState = UIState(
        header = "<INSERT HEADER HERE>",
        items = (1..20).map { "ITEM #$it" },
    )
    val uiState: StateFlow<UIState> = MutableStateFlow(initialState)
}


/**
 * LIST ADAPTER EXAMPLE
 */
class ItemsAdapter : ListAdapter<String, ItemViewHolder>(diffUtilItemCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = LayoutItemViewBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val diffUtilItemCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    }
}

class ItemViewHolder(
    private val binding: LayoutItemViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: String) {
        binding.textView.text = item
    }
}

abstract class BaseFragment<VB : ViewBinding>(
    @LayoutRes contentLayoutId: Int,
) : Fragment(contentLayoutId) {

    private val viewModel by navGraphViewModels<MainViewModel>(R.id.nav_graph)

    abstract fun createBinding(view: View): VB

    abstract fun initView(binding: VB)

    abstract fun onStateChanged(binding: VB, state: UIState)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(createBinding(view)) {
            initView(binding = this)

            viewModel.uiState
                .onEach { uiState ->
                    onStateChanged(binding = this, uiState)
                }
                .launchIn(viewLifecycleOwner.lifecycle.coroutineScope)
        }
    }
}


/**
 * Example with a NestedScrollView
 * DON'T DO THIS! The RecyclerView will have an 'infinite' height so it will render all of the
 * view holder items.
 */
class NestedScrollViewExampleFragment :
    BaseFragment<FragmentNestedscrollviewExampleBinding>(
        R.layout.fragment_nestedscrollview_example
    ) {

    override fun createBinding(view: View) = FragmentNestedscrollviewExampleBinding.bind(view)

    override fun initView(binding: FragmentNestedscrollviewExampleBinding) {
        binding.itemsRecyclerView.adapter = ItemsAdapter()
    }

    override fun onStateChanged(binding: FragmentNestedscrollviewExampleBinding, state: UIState) {
        with(binding) {
            headerLayout.headerTextView.text = state.header
            (itemsRecyclerView.adapter as ItemsAdapter).submitList(state.items)
        }
    }
}


/**
 * RECYCLER VIEW EXAMPLE WITH VIEW TYPES
 */
class ItemsAndHeaderAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var header = ""
    private val items = mutableListOf<String>()

    fun updateList(newItems: List<String>) {
        items.apply {
            clear()
            addAll(newItems)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == R.layout.layout_header_view) {
            val binding = LayoutHeaderViewBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = LayoutItemViewBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            ItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.layout_header_view -> (holder as HeaderViewHolder).bind(header)
            R.layout.layout_item_view -> (holder as ItemViewHolder).bind(items[position - 1])
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (position == 0) R.layout.layout_header_view else R.layout.layout_item_view

    // Includes Header
    override fun getItemCount(): Int = items.count() + 1
}

class HeaderViewHolder(
    private val binding: LayoutHeaderViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(header: String) {
        binding.headerTextView.text = header
    }
}

/**
 * Example with a RecyclerView with different view types
 */
class RecyclerViewTypesExampleFragment :
    BaseFragment<FragmentRecyclerViewBinding>(R.layout.fragment_recycler_view) {

    override fun createBinding(view: View) = FragmentRecyclerViewBinding.bind(view)

    override fun initView(binding: FragmentRecyclerViewBinding) {
        binding.recyclerView.adapter = ItemsAndHeaderAdapter()
    }

    override fun onStateChanged(binding: FragmentRecyclerViewBinding, state: UIState) {
        val adapter = (binding.recyclerView.adapter as ItemsAndHeaderAdapter)
        adapter.header = state.header
        adapter.updateList(state.items)
    }
}
