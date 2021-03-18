package dev.jimmymorales.rvdemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.jimmymorales.rvdemo.databinding.MainFragmentBinding

class MainActivity : AppCompatActivity(R.layout.activity_main)

class MainFragment : Fragment(R.layout.main_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = MainFragmentBinding.bind(view)

    }
}