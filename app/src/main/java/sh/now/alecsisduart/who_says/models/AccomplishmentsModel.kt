package sh.now.alecsisduarte.who_says.models

class AccomplishmentsModel {
    var learningTheBasicsAchievement: Boolean = false
    var youGotItAchievement: Boolean = false
    var youGotGoodAtItAchievement: Boolean = false
    var amazingAchievement: Boolean = false
    var boredAchievement: Boolean = false
    var veryVeryBoredAchievement: Boolean = false

    var normalScore: Int = 0
    var bigScore: Int = 0
    var plays: Int = 0

    val isEmpty: Boolean get() {
            return !learningTheBasicsAchievement &&
                    !youGotItAchievement &&
                    !youGotGoodAtItAchievement &&
                    !amazingAchievement &&
                    !boredAchievement &&
                    !veryVeryBoredAchievement &&
                    normalScore == 0 &&
                    bigScore == 0 &&
                    plays == 0

        }
}