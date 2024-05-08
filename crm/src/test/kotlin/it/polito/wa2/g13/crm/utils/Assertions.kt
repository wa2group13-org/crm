package it.polito.wa2.g13.crm.utils

import org.assertj.core.api.Assertions
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration

fun <T : Any> assertRecursive(expected: T, actual: T?) {
    Assertions.assertThat(actual)
        .usingRecursiveComparison(
            RecursiveComparisonConfiguration
                .builder()
                .withIgnoreCollectionOrder(true)
                .build()
        )
        .isEqualTo(expected)
}