package dev.jimmymorales.rvdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jimmymorales.rvdemo.databinding.FragmentMainBinding
import dev.jimmymorales.rvdemo.databinding.FragmentNestedscrollviewExampleBinding
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


/**
 * Example with a NestedScrollView
 * DON'T DO THIS! The RecyclerView will have an 'infinite' height so it will render all of the
 * view holder items.
 */
class NestedScrollViewExampleFragment : Fragment(R.layout.fragment_nestedscrollview_example) {

    private val viewModel by navGraphViewModels<MainViewModel>(R.id.nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(FragmentNestedscrollviewExampleBinding.bind(view)) {
            itemsRecyclerView.adapter = ItemsAdapter()

            viewModel.uiState
                .onEach { uiState ->
                    headerTextView.text = uiState.header
                    (itemsRecyclerView.adapter as ItemsAdapter).submitList(uiState.items)
                }
                .launchIn(viewLifecycleOwner.lifecycle.coroutineScope)
        }
    }
}