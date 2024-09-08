package com.drtaa.feature_sign

import android.content.Intent
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.drtaa.core_ui.base.BaseFragment
import com.drtaa.feature_main.MainActivity
import com.drtaa.feature_sign.databinding.FragmentSignInBinding
import com.drtaa.feature_sign.util.SocialLoginManager
import com.drtaa.feature_sign.util.SocialLoginManager.Companion.GOOGLE
import com.drtaa.feature_sign.util.SocialLoginManager.Companion.NAVER
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : BaseFragment<FragmentSignInBinding>(R.layout.fragment_sign_in) {

    private val signViewModel: SignViewModel by activityViewModels()

    @Inject
    lateinit var socialLoginManager: SocialLoginManager

    override fun initView() {
        initEvent()
        initObserver()
    }

    private fun initEvent() {
        binding.btnSignInMoveToMain.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }

        binding.btnSignInNaver.setOnClickListener {
            socialLoginManager.login(NAVER, requireActivity())
        }

        binding.btnSignIn.setOnClickListener {
            signViewModel.formLogin(
                id = binding.etSignInId.text.toString(),
                pw = binding.etSignInPw.text.toString()
            )
        }

        binding.btnSignInSignUp.setOnClickListener {
            navigateDestination(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.btnSignInGoogle.setOnClickListener {
            socialLoginManager.login(GOOGLE, requireActivity())
        }
    }

    private fun initObserver() {
        socialLoginManager.resultLogin.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { result ->
                result.onSuccess { data ->
                    Timber.tag("login success").d("$data")
                    signViewModel.getTokens(data)
                }.onFailure {
                    Timber.tag("login fail").d("$result")
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        signViewModel.tokens.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { result ->
                result.onSuccess {
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }.onFailure {}
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
}