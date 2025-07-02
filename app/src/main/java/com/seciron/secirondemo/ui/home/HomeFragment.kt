package com.seciron.secirondemo.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.seciron.secirondemo.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val PREF_NAME = "UserSession"
    private val PREF_USERNAME = "username"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Text default
        homeViewModel.text.observe(viewLifecycleOwner) {
            binding.textHome.text = it
        }

        // Tombol: Tampilkan username
        binding.btnShowPref.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val username = sharedPref.getString(PREF_USERNAME, null)
            if (username != null) {
                binding.textHome.text = "Logged in as: $username"
                Toast.makeText(requireContext(), "Welcome back, $username", Toast.LENGTH_SHORT).show()
            } else {
                binding.textHome.text = "No user session found"
                Toast.makeText(requireContext(), "No saved username", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol: Simpan username
        binding.btnInsertPref.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            sharedPref.edit().putString(PREF_USERNAME, "SecIronUser").apply()
            Toast.makeText(requireContext(), "User 'SecIronUser' saved", Toast.LENGTH_SHORT).show()
        }

        // Tombol: Hapus username
        binding.btnDeletePref.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            sharedPref.edit().remove(PREF_USERNAME).apply()
            Toast.makeText(requireContext(), "User data cleared", Toast.LENGTH_SHORT).show()
            binding.textHome.text = "User session removed"
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
