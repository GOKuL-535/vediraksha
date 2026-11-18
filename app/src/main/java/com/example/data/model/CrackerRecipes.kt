package com.example.data.model

data class RecipeIngredient(
    val chemicalNameTa: String,
    val chemicalNameEn: String,
    val kgPerUnit: Double // chemical needed in kg for 1 unit of cracker
)

data class CrackerRecipe(
    val crackerNameTa: String,
    val crackerNameEn: String,
    val ingredients: List<RecipeIngredient>
)

object CrackerRecipeCatalog {

    val standardCrackerTypes = listOf(
        Pair("கம்பி மத்தாப்பு", "Sparklers"),
        Pair("பூந்தொட்டி", "Flower Pots"),
        Pair("ராக்கெட்", "Rockets"),
        Pair("தரை சக்கரம்", "Ground Chakras"),
        Pair("சத்தம் வெடி (Bijili)", "Sound Bombs")
    )

    val standardChemicals = listOf(
        Pair("பொட்டாசியம் நைட்ரேட் (KNO3)", "Potassium Nitrate (KNO3)"),
        Pair("கந்தகப் பொடி (Sulphur)", "Sulphur Powder"),
        Pair("அலுமினியம் பவுடர்", "Aluminium Flake Powder"),
        Pair("பேரியம் நைட்ரேட்", "Barium Nitrate"),
        Pair("கரிப்பொடி (Charcoal)", "Charcoal Powder")
    )

    fun getRecipe(crackerName: String): CrackerRecipe {
        val lower = crackerName.lowercase().trim()
        return when {
            lower.contains("sparkler") || lower.contains("மத்தாப்பு") -> CrackerRecipe(
                crackerNameTa = "கம்பி மத்தாப்பு",
                crackerNameEn = "Sparklers",
                ingredients = listOf(
                    RecipeIngredient("பொட்டாசியம் நைட்ரேட் (KNO3)", "Potassium Nitrate (KNO3)", 0.006),
                    RecipeIngredient("அலுமினியம் பவுடர்", "Aluminium Flake Powder", 0.008),
                    RecipeIngredient("பேரியம் நைட்ரேட்", "Barium Nitrate", 0.012)
                )
            )
            lower.contains("flower") || lower.contains("pot") || lower.contains("பூந்தொட்டி") -> CrackerRecipe(
                crackerNameTa = "பூந்தொட்டி",
                crackerNameEn = "Flower Pots",
                ingredients = listOf(
                    RecipeIngredient("பொட்டாசியம் நைட்ரேட் (KNO3)", "Potassium Nitrate (KNO3)", 0.020),
                    RecipeIngredient("கந்தகப் பொடி (Sulphur)", "Sulphur Powder", 0.006),
                    RecipeIngredient("கரிப்பொடி (Charcoal)", "Charcoal Powder", 0.005),
                    RecipeIngredient("அலுமினியம் பவுடர்", "Aluminium Flake Powder", 0.004)
                )
            )
            lower.contains("rocket") || lower.contains("ராக்கெட்") -> CrackerRecipe(
                crackerNameTa = "ராக்கெட்",
                crackerNameEn = "Rockets",
                ingredients = listOf(
                    RecipeIngredient("பொட்டாசியம் நைட்ரேட் (KNO3)", "Potassium Nitrate (KNO3)", 0.016),
                    RecipeIngredient("கந்தகப் பொடி (Sulphur)", "Sulphur Powder", 0.005),
                    RecipeIngredient("கரிப்பொடி (Charcoal)", "Charcoal Powder", 0.008)
                )
            )
            lower.contains("chakra") || lower.contains("wheel") || lower.contains("சக்கரம்") -> CrackerRecipe(
                crackerNameTa = "தரை சக்கரம்",
                crackerNameEn = "Ground Chakras",
                ingredients = listOf(
                    RecipeIngredient("பொட்டாசியம் நைட்ரேட் (KNO3)", "Potassium Nitrate (KNO3)", 0.012),
                    RecipeIngredient("கந்தகப் பொடி (Sulphur)", "Sulphur Powder", 0.004),
                    RecipeIngredient("பேரியம் நைட்ரேட்", "Barium Nitrate", 0.007)
                )
            )
            lower.contains("bomb") || lower.contains("bijili") || lower.contains("வெடி") || lower.contains("sound") -> CrackerRecipe(
                crackerNameTa = "சத்தம் வெடி (Bijili)",
                crackerNameEn = "Sound Bombs",
                ingredients = listOf(
                    RecipeIngredient("பொட்டாசியம் நைட்ரேட் (KNO3)", "Potassium Nitrate (KNO3)", 0.022),
                    RecipeIngredient("கந்தகப் பொடி (Sulphur)", "Sulphur Powder", 0.008),
                    RecipeIngredient("அலுமினியம் பவுடர்", "Aluminium Flake Powder", 0.005),
                    RecipeIngredient("கரிப்பொடி (Charcoal)", "Charcoal Powder", 0.004)
                )
            )
            else -> CrackerRecipe(
                crackerNameTa = crackerName,
                crackerNameEn = crackerName,
                ingredients = listOf(
                    RecipeIngredient("பொட்டாசியம் நைட்ரேட் (KNO3)", "Potassium Nitrate (KNO3)", 0.015),
                    RecipeIngredient("கந்தகப் பொடி (Sulphur)", "Sulphur Powder", 0.005),
                    RecipeIngredient("கரிப்பொடி (Charcoal)", "Charcoal Powder", 0.004)
                )
            )
        }
    }
}

