package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAuthenticationBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

class AuthenticationFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthenticationBinding.inflate(inflater, container, false)
        val authenticationViewModel: AuthViewModel by viewModels()

        lifecycleScope.launch {
            authenticationViewModel.data.collect { state ->
                val token = state.token.toString()
                if (state.id != 0L && token.isNotEmpty()) {
                    findNavController().navigateUp()
                }
            }
        }

        binding.buttonEnter.setOnClickListener {
            val login = binding.login.text.toString().trim()
            val pass = binding.password.text.toString().trim()
            if (login.isNotEmpty() && pass.isNotEmpty()) {
                authenticationViewModel.login(login, pass, requireContext())
            } else {
                Toast.makeText(requireContext(), getText(R.string.all_fields_must_be_filled_in), Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }
}