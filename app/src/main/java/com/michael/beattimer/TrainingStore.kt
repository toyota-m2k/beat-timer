package com.michael.beattimer

data class Training(val title:String, val beat:IBeat)

object TrainingStore {
    private val SQUAT7 = Training(
        "７秒スクワット",
        Phrase.root(
            Phrase.verse(listOf(
                Phrase.tick5,
                Tick.tick(2),
                Tick.rest(2)
            ), 10))
    )

    private val HIIT = Training(
        "H.I.I.T",
        Phrase.root(
            Phrase.verse(listOf(
                Phrase.tick5sec(4,"がんがんやる"),
                TickCounterReset,
                Phrase("Silent10", Tick.silent(10), 1, "休憩")
            ), 8))
    )
    private val LBD_BASIC_1 = Training(
        "基本呼吸 (1)",
        Phrase.root(
            Phrase.verse(listOf(
                Phrase.ticksAndAccent(3, "鼻から吸って"),
                Tick.rest(1),
                TickCounterReset,
                Phrase.ticksAndAccent(15, "口から吐く"),
                Tick.rest(2)
        ),6))
    )
    private val LBD_BASIC_2_3 = Training(
        "基本呼吸 (２,３)",
        Phrase.root(
            Phrase.verse(listOf(
                Phrase.ticksAndAccent(3, "鼻から吸って"),
                Tick.rest(1),
                TickCounterReset,
                Phrase.ticksAndAccent(10, "口から吐く"),
                Tick.rest(2)
            ),6))
    )
    private val LBD_4_10 = Training(
        "肩こり・腰痛改善 (4, 10)",
        Phrase.root(
            Phrase.verse(listOf(
                Phrase("LBD4-Left", listOf(
                    Phrase.ticksAndAccent(3, "鼻から吸って"),
                    Tick.rest(1),
                    TickCounterReset,
                    Phrase.ticksAndAccent(7, "ぷぅ、ってやる"),
                    Tick.rest(2)
                ),3),
                Phrase("Interlude", listOf(Tick.tick(3,false), Tick("middle", TickType.MIDDLE, 1, false)), 1, "左右入れ替えて。。。"),
                Phrase("LBD4-Left", listOf(
                    Phrase.ticksAndAccent(3, "鼻から吸って"),
                    Tick.rest(1),
                    TickCounterReset,
                    Phrase.ticksAndAccent(7, "ぷぅ、ってやる"),
                    Tick.rest(2)
                ),3)

            ),1)
        )
    )

    private val LBD_5 = Training(
        "肩こり・腰痛改善 (5)",
        Phrase.root(
            Phrase.verse(listOf(
                    Phrase.ticksAndAccent(3, "鼻から吸って"),
                    Tick.rest(1),
                    TickCounterReset,
                    Phrase.ticksAndAccent(7, "ぷぅ、ってやる"),
                    Tick.rest(2)
            ),6)
        )
    )

    private val LBD_11 = Training (
        "マインドフルネス (11)",
        Phrase.root(
            Phrase.verse(listOf(
                Phrase.ticksAndAccent(5, "鼻から吸って"),
                Phrase.ticksAndAccent(5, "止めて"),
                Phrase.ticksAndAccent(10, "吐いて")
            ), 9)
        )
    )

    private val trainings = listOf(SQUAT7, HIIT, LBD_BASIC_1, LBD_BASIC_2_3, LBD_4_10, LBD_5, LBD_11)

    val trainingTitles = trainings.map {it.title}
    fun trainingOf(title:String) = trainings.find {it.title==title}
}