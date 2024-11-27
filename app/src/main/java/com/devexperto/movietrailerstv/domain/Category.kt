package com.devexperto.movietrailerstv.domain

enum class Category(val id: String) {
    POPULAR("popularity.desc"),
    NUEVO("release_date.desc"),
    DESTACADO("vote_average.desc"),
    RELEVANTE("revenue.desc")
}