package com.nbstocks.nbstocks.presentation.ui.sign_up

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nbstocks.nbstocks.R
import com.nbstocks.nbstocks.common.extensions.*
import com.nbstocks.nbstocks.databinding.FragmentSignUpBinding
import com.nbstocks.nbstocks.presentation.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : BaseFragment<FragmentSignUpBinding>(FragmentSignUpBinding::inflate) {

    private val viewModel: SignUpViewModel by viewModels()

    override fun start() {
        listeners()
        observer()
    }

    private fun listeners() {
        binding.btnSignUp.setOnClickListener {
            registration()
        }
        binding.tvLogIn.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun registration() {
        binding.tilEmail.isValid(isEmail = true)?.let { email ->
            binding.tilPassword.isValid(isPassword = true)?.let { password ->
                viewModel.signUp(email, password)
            }
        }
    }

    private fun observer() {
        asynchronously {
            viewModel.registerResponse.collect { resource ->
                resource.doOnSuccess {
                    findNavController().popBackStack(R.id.homeFragment, false)

                }.doOnFailure { it ->
                    it.localizedMessage?.let { binding.root.makeSnackbar(it, true) }
                }
            }
        }
        asynchronously {
            viewModel.loaderState.collect{
                binding.progressBar.isVisible = it
            }
        }
    }
}


