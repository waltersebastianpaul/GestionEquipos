package com.example.gestionequipos.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.ViewModelProvider
import com.example.gestionequipos.MainActivity // Importa MainActivity
import com.example.gestionequipos.R
import com.example.gestionequipos.auth.LoginActivity
import com.example.gestionequipos.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // Método dentro de la clase
        super.onViewCreated(view, savedInstanceState)

        // Lógica del botón "Parte Diario"
        binding.buttonParteDiario.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_partediario)
        }

        binding.verPartesButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_listaPartesFragment)
        }

        // Obtiene el nombre y el rol del usuario del Intent
//        val nombreUsuario = requireActivity().intent.getStringExtra("nombre") ?: ""
        val rolUsuario = requireActivity().intent.getStringArrayListExtra("rol") ?: arrayListOf()

        // Muestra u oculta vistas según el rol del usuario
        if (rolUsuario.contains("administrador")) {
            binding.adminTextView.visibility = View.VISIBLE
        }
        if (rolUsuario.contains("usuario")) {
            binding.userTextView.visibility = View.VISIBLE
        }


        // Lógica del botón "Logout"
        binding.logoutButton.setOnClickListener {
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}