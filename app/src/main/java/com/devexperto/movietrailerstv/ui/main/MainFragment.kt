package com.devexperto.movietrailerstv.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.viewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.devexperto.movietrailerstv.R
import com.devexperto.movietrailerstv.data.repository.MoviesRepository
import com.devexperto.movietrailerstv.domain.Category
import com.devexperto.movietrailerstv.domain.Movie
import com.devexperto.movietrailerstv.ui.detail.DetailActivity
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat

class MainFragment : BrowseSupportFragment() {

    private val backgroundState = BackgroundState(this)

    private val vm by viewModels<MainViewModel> {
        MainViewModelFactory(MoviesRepository(getString(R.string.api_key)))
    }
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Configuración del título de la aplicación
        title = getString(R.string.browse_title)

        // Establece el ícono o banner en la esquina superior izquierda
        val bannerDrawable = resources.getDrawable(R.mipmap.banner, requireContext().theme)
        setBadgeDrawable(bannerDrawable) // Ícono en la izquierda

        // Habilita la barra de búsqueda en la parte derecha
        val searchBarColor = ContextCompat.getColor(requireContext(), R.color.search_bar_color)
        setSearchAffordanceColor(searchBarColor)

        // Configuración del adaptador principal
        adapter = rowsAdapter

        // Configura el comportamiento al hacer clic en un elemento
        onItemViewClickedListener = OnItemViewClickedListener { vh, movie, _, _ ->
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(),
                (vh.view as ImageCardView).mainImageView,
                DetailActivity.SHARED_ELEMENT_NAME
            ).toBundle()

            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra(DetailActivity.MOVIE_EXTRA, movie as Movie)
            }
            startActivity(intent, bundle)
        }

        // Configura el comportamiento al seleccionar un elemento
        onItemViewSelectedListener = OnItemViewSelectedListener { _, movie, _, _ ->
            (movie as? Movie)?.let { backgroundState.loadUrl(movie.backdrop) }
        }

        // Observa el estado de la vista y actualiza la UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                vm.state.collect {
                    if (it.isLoading) progressBarManager.show() else progressBarManager.hide()
                    updateUi(it.categories)
                }
            }
        }

        vm.onUiReady()
    }

    private fun updateUi(categories: Map<Category, List<Movie>>) {
        rowsAdapter.clear()
        val cardPresenter = CardPresenter()
        categories.forEach { (category, movies) ->
            val listRowAdapter = ArrayObjectAdapter(cardPresenter).apply {
                addAll(0, movies)
            }

            val header = HeaderItem(category.name)
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }
    }
}
