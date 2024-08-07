package com.example.gestionequipos.ui.home

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.gestionequipos.MainActivity
import com.example.gestionequipos.R
import com.example.gestionequipos.auth.LoginActivity
import com.example.gestionequipos.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonParteDiario.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_partediario)
        }

        binding.verPartesButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_listaPartesFragment)
        }

        val rolUsuario = requireActivity().intent.getStringArrayListExtra("rol") ?: arrayListOf()

        if (rolUsuario.contains("administrador")) {
            binding.adminTextView.visibility = View.VISIBLE
        }
        if (rolUsuario.contains("usuario")) {
            binding.userTextView.visibility = View.VISIBLE
        }

//        binding.logoutButton.setOnClickListener {
//            val sharedPreferences = requireActivity().getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE)
//            val editor = sharedPreferences.edit()
//            editor.clear()
//            editor.apply()
//
//            val intent = Intent(requireActivity(), LoginActivity::class.java)
//            startActivity(intent)
//            requireActivity().finish()
//        }
    }

    override fun onResume() {
        super.onResume()

        // Configuración del FloatingActionButton regular
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.visibility = View.VISIBLE
        fab.setImageResource(R.drawable.ic_add)
        fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        fab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))

        fab.setOnClickListener {
            findNavController().navigate(R.id.nav_partediario)
        }

//        // Configuración del ExtendedFloatingActionButton
//        val extendedFab: ExtendedFloatingActionButton? = requireActivity().findViewById(R.id.extended_fab)
//        extendedFab?.let { fab ->
//            fab.setIconResource(R.drawable.ic_add)
//            fab.text = getString(R.string.etiqueta_fab)
//            fab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//            fab.backgroundTintList = ColorStateList.valueOf(
//                ContextCompat.getColor(requireContext(), R.color.colorPrimary)
//            )
//            fab.iconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
//
//            fab.setOnClickListener {
//                try {
//                    findNavController().navigate(R.id.nav_partediario)
//                } catch (e: Exception) {
//                    Snackbar.make(requireView(), "Error al navegar: ${e.message}", Snackbar.LENGTH_LONG).show()
//                }
//            }
//        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


//package com.example.gestionequipos.ui.home
//
//import android.content.Context
//import android.content.Intent
//import android.content.res.ColorStateList
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.Lifecycle
//import androidx.navigation.fragment.findNavController
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.repeatOnLifecycle
//import com.example.gestionequipos.MainActivity
//import com.example.gestionequipos.R
//import com.example.gestionequipos.auth.LoginActivity
//import com.example.gestionequipos.databinding.FragmentHomeBinding
//import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.android.material.snackbar.Snackbar
//import kotlinx.coroutines.launch
//
//class HomeFragment : Fragment() {
//
//    private var _binding: FragmentHomeBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var mainActivity: MainActivity
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
//        _binding = FragmentHomeBinding.inflate(inflater, container, false)
//        val root: View = binding.root
//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
//
//        return root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val extendedFab: ExtendedFloatingActionButton? = (activity as? MainActivity)?.findViewById(R.id.extended_fab)
//        extendedFab?.let {
//            it.extend()
//            it.shrink()
//            it.setIconResource(R.drawable.ic_add)
//            it.text = getString(R.string.crear_parte)
//            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//            it.backgroundTintList = ColorStateList.valueOf(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.colorPrimary
//                )
//            )
//            it.iconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
//
//            it.setOnClickListener {
//                findNavController().navigate(R.id.nav_partediario)
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                val mainActivity = activity as? MainActivity
//                mainActivity?.let {
//                    mainActivity.showFab()
//                    mainActivity.setFabIcon(R.drawable.ic_add)
//                    mainActivity.setFabClickListener {
//                        Snackbar.make(view, "Acción del FAB en HomeFragment", Snackbar.LENGTH_LONG).show()
//                    }
//                }
//            }
//        }
//
//        binding.buttonParteDiario.setOnClickListener {
//            findNavController().navigate(R.id.action_nav_home_to_nav_partediario)
//        }
//
//        binding.verPartesButton.setOnClickListener {
//            findNavController().navigate(R.id.action_navigation_home_to_listaPartesFragment)
//        }
//
//        val rolUsuario = requireActivity().intent.getStringArrayListExtra("rol") ?: arrayListOf()
//
//        if (rolUsuario.contains("administrador")) {
//            binding.adminTextView.visibility = View.VISIBLE
//        }
//        if (rolUsuario.contains("usuario")) {
//            binding.userTextView.visibility = View.VISIBLE
//        }
//
//        binding.logoutButton.setOnClickListener {
//            // Borrar las preferencias compartidas (si las estás utilizando)
//            val sharedPreferences = requireActivity().getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE)
//            val editor = sharedPreferences.edit()
//            editor.clear()
//            editor.apply()
//
//            // Reiniciar el estado de la aplicación (si es necesario)
//
//            // Redirigir al usuario a la pantalla de login
//            val intent = Intent(requireActivity(), LoginActivity::class.java)
//            startActivity(intent)
//            requireActivity().finish()
//        }
//
//
//
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
//        fab.visibility = View.VISIBLE
//        // Configura el FloatingActionButton
//        fab.setImageResource(R.drawable.ic_add)
//        fab.backgroundTintList =
//            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
//        fab.imageTintList =
//            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
//
//        fab.setOnClickListener {
//            findNavController().navigate(R.id.nav_partediario)
//        }
//
//
//
//
//        // ... (resto de tu código para configurar el FAB)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
////        val mainActivity = activity as? MainActivity
////        mainActivity?.hideFab() // Ocultar el FAB
//        _binding = null
//    }
//
//}
//
