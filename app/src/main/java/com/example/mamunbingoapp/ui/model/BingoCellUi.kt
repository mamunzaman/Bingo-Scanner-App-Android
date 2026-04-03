package com.example.mamunbingoapp.ui.model

data class BingoCellUi(
    val number: String?,
    val isMarked: Boolean,
    val isCalled: Boolean,
    val isEditable: Boolean,
    val isDisabled: Boolean,
    val isSelected: Boolean = false
) {
    companion object {
        fun placeholderCells25(): List<BingoCellUi> = listOf(
            BingoCellUi("05", true, false, false, false),
            BingoCellUi("12", false, false, false, false),
            BingoCellUi("24", true, false, false, false),
            BingoCellUi("32", false, false, false, false),
            BingoCellUi("45", false, false, false, false),
            BingoCellUi("08", false, false, false, false),
            BingoCellUi("19", true, false, false, false),
            BingoCellUi("21", false, false, false, false),
            BingoCellUi("38", true, false, false, false),
            BingoCellUi("52", false, false, false, false),
            BingoCellUi("11", false, false, false, false),
            BingoCellUi("23", false, false, false, false),
            BingoCellUi("31", true, false, false, false),
            BingoCellUi("49", false, false, false, false),
            BingoCellUi("60", true, false, false, false),
            BingoCellUi("02", false, false, false, false),
            BingoCellUi("17", true, false, false, false),
            BingoCellUi("28", false, false, false, false),
            BingoCellUi("42", true, false, false, false),
            BingoCellUi("55", false, false, false, false),
            BingoCellUi("04", true, false, false, false),
            BingoCellUi("20", false, false, false, false),
            BingoCellUi("36", true, false, false, false),
            BingoCellUi("50", false, false, false, false),
            BingoCellUi("74", true, false, false, false)
        )
    }
}
