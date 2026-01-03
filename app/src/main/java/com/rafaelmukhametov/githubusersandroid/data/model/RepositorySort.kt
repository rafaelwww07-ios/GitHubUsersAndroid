package com.rafaelmukhametov.githubusersandroid.data.model

/**
 * Способы сортировки репозиториев
 */
enum class RepositorySort(val value: String) {
    CREATED("created"),
    UPDATED("updated"),
    PUSHED("pushed"),
    FULL_NAME("full_name"),
    STARS("stars")
}

/**
 * Порядок сортировки
 */
enum class RepositoryOrder(val value: String) {
    ASC("asc"),
    DESC("desc")
}

