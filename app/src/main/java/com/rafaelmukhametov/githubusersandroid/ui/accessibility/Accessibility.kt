package com.rafaelmukhametov.githubusersandroid.ui.accessibility

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.Modifier

/**
 * Accessibility модификаторы для улучшения поддержки TalkBack и других accessibility сервисов
 */
object Accessibility {
    
    /**
     * Добавляет contentDescription для изображений
     */
    fun Modifier.imageDescription(description: String): Modifier {
        return this.semantics {
            contentDescription = description
        }
    }
    
    /**
     * Добавляет testTag для тестирования
     */
    fun Modifier.testTag(tag: String): Modifier {
        return this.semantics {
            testTag = tag
        }
    }
    
    /**
     * Комбинированный модификатор для accessibility
     */
    fun Modifier.accessibility(
        contentDescription: String? = null,
        testTag: String? = null
    ): Modifier {
        return this.semantics {
            contentDescription?.let { this.contentDescription = it }
            testTag?.let { this.testTag = it }
        }
    }
}

