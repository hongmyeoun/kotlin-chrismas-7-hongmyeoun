package store.model

import camp.nextstep.edu.missionutils.DateTimes
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PromotionManager {

    fun loadPromotionFromFile(): List<Promotion> {
        val fileRoute = getPromotionFileRoute()
        val promotion = File(fileRoute).readLines().drop(1).map { line -> parsePromotion(line) }
        return promotion
    }

    private fun getPromotionFileRoute(): String {
        val classLoader = Thread.currentThread().contextClassLoader
        val file = classLoader.getResource("promotions.md")?.file ?: throw IllegalArgumentException("[ERROR] 파일이 없습니다.")
        return file
    }

    private fun parsePromotion(line: String): Promotion {
        val parts = line.split(",")
        val promotion = Promotion(
            name = parts[0].trim(),
            buy = parts[1].trim().toInt(),
            get = parts[2].trim().toInt(),
            startDate = parts[3].trim(),
            endDate =parts[4].trim()
        )
        return promotion
    }

    fun checkIsInPromotionDate(promotion: Promotion): Boolean {
        val now = DateTimes.now().formatDateTime()
        val start = promotion.startDate
        val endDate = promotion.endDate
        val isInPromotionDate = now in start..endDate
        return isInPromotionDate
    }

    private fun LocalDateTime.formatDateTime(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return this.format(formatter)
    }


}

data class Promotion(
    val name: String,
    val buy: Int,
    val get: Int,
    val startDate: String,
    val endDate: String
)