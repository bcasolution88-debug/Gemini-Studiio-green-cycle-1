package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiRecycleSuggestion(
    val categoryName: String,
    val subCategoryName: String,
    val recyclingAdvice: String,
    val estimatedPriceText: String,
    val rawResponse: String
)

object GeminiRepository {
    private const val TAG = "GeminiRepository"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeRecycleItem(itemTitle: String, itemDescription: String): GeminiRecycleSuggestion = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val promptText = """
            请作为专业的废品回收与环保环保分类专家，分析以下待回收物品:
            物品名称: "$itemTitle"
            物品描述: "$itemDescription"

            请按以下JSON格式严格输出评估（不要包含markdown标记以外的多余解释）:
            {
              "categoryName": "废旧家电/废纸/旧衣物/废金属/塑料/家具/玻璃/其它",
              "subCategoryName": "具体细分类别",
              "recyclingAdvice": "针对该物品的具体环保拆解、预处理、分类投放建议及生态环保价值",
              "estimatedPriceText": "预估回收估价区间（如: 15-30元 或 无偿捐赠）"
            }
        """.trimIndent()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Local fallback rule-based suggestion if API key is unconfigured
            return@withContext fallbackSuggestion(itemTitle, itemDescription)
        }

        try {
            val requestJson = JSONObject().apply {
                val contentsArr = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArr = JSONArray().apply {
                            put(JSONObject().apply { put("text", promptText) })
                        }
                        put("parts", partsArr)
                    }
                    put(contentObj)
                }
                put("contents", contentsArr)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful || responseString.isBlank()) {
                Log.e(TAG, "Gemini API error: ${response.code} $responseString")
                return@withContext fallbackSuggestion(itemTitle, itemDescription)
            }

            val rootObj = JSONObject(responseString)
            val candidates = rootObj.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textResult = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Parse json from result
            val cleanJson = textResult.substringAfter("{").substringBeforeLast("}")
            if (cleanJson.isNotBlank()) {
                val parsedObj = JSONObject("{$cleanJson}")
                GeminiRecycleSuggestion(
                    categoryName = parsedObj.optString("categoryName", "废旧环保品"),
                    subCategoryName = parsedObj.optString("subCategoryName", "通用类别"),
                    recyclingAdvice = parsedObj.optString("recyclingAdvice", "建议进行清洁分类后排入专用回收站。"),
                    estimatedPriceText = parsedObj.optString("estimatedPriceText", "5 - 50 元"),
                    rawResponse = textResult
                )
            } else {
                fallbackSuggestion(itemTitle, itemDescription)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini API call", e)
            fallbackSuggestion(itemTitle, itemDescription)
        }
    }

    suspend fun askRecyclingAssistant(question: String, lang: com.example.data.model.Language): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val languagePrompt = if (lang == com.example.data.model.Language.ZH) "用中文回答" else "Answer in English"
        val promptText = """
            你是 GreenCycle 绿碳智投 专业的 AI 垃圾分类与废品回收顾问。
            用户问题: "$question"

            要求:
            1. 明确判断该物品是否可回收（如: 可回收物 / 危险有害废弃物 / 厨余湿垃圾 / 其它干垃圾 / 大件预约等）。
            2. 提供标准无害化或清洁投放的 3 个具体步骤（例如: 倒空残液、剥离标签、平整压扁或防漏包装）。
            3. 提示生态价值及环保减碳积分预估。
            4. $languagePrompt，使用Scannable结构化排版（包含 Emoji 图标）。
        """.trimIndent()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext localFallbackAssistantAnswer(question, lang)
        }

        try {
            val requestJson = JSONObject().apply {
                val contentsArr = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArr = JSONArray().apply {
                            put(JSONObject().apply { put("text", promptText) })
                        }
                        put("parts", partsArr)
                    }
                    put(contentObj)
                }
                put("contents", contentsArr)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful || responseString.isBlank()) {
                return@withContext localFallbackAssistantAnswer(question, lang)
            }

            val rootObj = JSONObject(responseString)
            val candidates = rootObj.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textResult = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (textResult.isNotBlank()) {
                textResult
            } else {
                localFallbackAssistantAnswer(question, lang)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini AI Recycling Assistant API call", e)
            localFallbackAssistantAnswer(question, lang)
        }
    }

    private fun localFallbackAssistantAnswer(q: String, lang: com.example.data.model.Language): String {
        val query = q.lowercase()
        val isZh = lang == com.example.data.model.Language.ZH

        return when {
            query.contains("电池") || query.contains("battery") || query.contains("锂") -> {
                if (isZh) {
                    """
                    🔋【分类结果】有害废弃物 / 锂电池与危险物专回收
                    
                    📋【正确处置步骤】
                    1. 绝缘保护: 用透明胶带封住电池正负极接线端子，防止磨擦短路。
                    2. 独立密封: 放入塑料自封袋中，避免雨水淋湿防漏液。
                    3. 专柜投递: 投递至社区 GreenCycle 有害垃圾回收箱或电子回收驿站。
                    
                    🌱【环保价值】回收 1 块手机电池可防止 60,000 升水受到重金属污染，预计可获得 +20 碳减排积分！
                    """.trimIndent()
                } else {
                    """
                    🔋 [Classification] Hazardous Household Waste
                    
                    📋 [Disposal Steps]
                    1. Insulate Terminals: Tape over positive & negative ends with clear tape.
                    2. Seal Dry: Place in a ziplock bag to avoid leakage.
                    3. Designated Drop-off: Hand over to GreenCycle E-waste collection station.
                    
                    🌱 [Eco Value] Recycling 1 battery prevents 60,000L of water contamination (+20 Eco Credits).
                    """.trimIndent()
                }
            }
            query.contains("衣") || query.contains("裤") || query.contains("鞋") || query.contains("cloth") || query.contains("textiles") -> {
                if (isZh) {
                    """
                    👕【分类结果】可回收物 - 旧衣纺织品类
                    
                    📋【正确处置步骤】
                    1. 保持干爽: 确保衣服无水渍、无明显霉变或强油污。
                    2. 折叠整理: 装入干净塑料袋或绳索捆扎，方便专员过磅。
                    3. 预约投递: 在 GreenCycle 上预约旧衣上门回收或捐赠给公益机构。
                    
                    🌱【环保价值】每回收 1kg 旧衣物，可减少 3.6kg 碳排放及 6,000 升生产用水，获 +15 积分/kg！
                    """.trimIndent()
                } else {
                    """
                    👕 [Classification] Recyclable Material - Textiles
                    
                    📋 [Disposal Steps]
                    1. Clean & Dry: Ensure garments are free of moisture or heavy stain.
                    2. Pack Neatly: Pack in clean bags or tie securely.
                    3. Doorstep Pickup: Schedule a GreenCycle textile collection or donation.
                    
                    🌱 [Eco Value] 1kg recycled textiles saves 3.6kg CO2 & 6,000L water (+15 Credits/kg).
                    """.trimIndent()
                }
            }
            query.contains("瓶") || query.contains("塑料") || query.contains("pet") || query.contains("plastic") -> {
                if (isZh) {
                    """
                    ♻️【分类结果】可回收物 - PET/HDPE 塑胶容器
                    
                    📋【正确处置步骤】
                    1. 倒空清洗: 冲洗残留液体（如饮料/洗发水/奶瓶），晾干。
                    2. 剥离瓶贴: 尽量剥离纸质标签或异质塑料封套。
                    3. 扁平压紧: 拧紧瓶盖前用力压扁，节省 70% 运输容积。
                    
                    🌱【环保价值】1,000 个 PET 废瓶可制成 1 件环保涤纶冲锋衣，每公斤获 +10 碳积分！
                    """.trimIndent()
                } else {
                    """
                    ♻️ [Classification] Recyclable - PET/HDPE Plastics
                    
                    📋 [Disposal Steps]
                    1. Rinse Empty: Rinse out liquid residue and dry.
                    2. Remove Sleeves: Peel off paper sleeves or shrink film.
                    3. Flatten: Crush flat before replacing caps to reduce storage bulk.
                    
                    🌱 [Eco Value] 1,000 PET bottles can be spun into a recycled polyester jacket (+10 Credits/kg).
                    """.trimIndent()
                }
            }
            else -> {
                if (isZh) {
                    """
                    🔍【分类结果】通用可回收旧物 / 资源再生类
                    
                    📋【标准处置指导】
                    1. 物品检查: 去除不可回收沾污物（如湿面巾纸、剩饭残渣）。
                    2. 分类打包: 按纸类、金属、塑料、旧衣或废家电分类装箱。
                    3. 预约回收: 使用 GreenCycle 首页‘一键预约’或寻访邻近绿碳回收站。
                    
                    🌱【环保建议】若物品保持8成新以上，强烈推荐发布至社区二手流转区，实现 100% 零废弃！
                    """.trimIndent()
                } else {
                    """
                    🔍 [Classification] General Recyclable / Resource Material
                    
                    📋 [Standard Guidance]
                    1. Inspect & Clean: Remove non-recyclable contaminants.
                    2. Sort & Pack: Separate into paper, metal, plastic, or electronics.
                    3. Schedule Pickup: Use GreenCycle doorstep pickup or station drop-off.
                    
                    🌱 [Eco Tip] If in reusable condition, consider posting it to the Community Exchange section!
                    """.trimIndent()
                }
            }
        }
    }

    private fun fallbackSuggestion(itemTitle: String, itemDescription: String): GeminiRecycleSuggestion {
        val combined = "$itemTitle $itemDescription".lowercase()
        return when {
            combined.contains("床") || combined.contains("柜") || combined.contains("桌") || combined.contains("沙发") -> {
                GeminiRecycleSuggestion(
                    categoryName = "大件家具",
                    subCategoryName = "木质/复合大件",
                    recyclingAdvice = "【AI智能识别】大件家具建议保持部件完整，提前预约专业搬运专员。拆解后木材可再生成颗粒板，可获得高额环保积分。",
                    estimatedPriceText = "150 - 300 元",
                    rawResponse = "AI Rule Matching"
                )
            }
            combined.contains("电") || combined.contains("机") || combined.contains("电脑") || combined.contains("手机") -> {
                GeminiRecycleSuggestion(
                    categoryName = "废旧家电数码",
                    subCategoryName = "数码电子与家电",
                    recyclingAdvice = "【AI智能识别】含有电池及PCB电路板，请勿随意丢弃。专业拆解提炼贵金属，符合国家电子废弃物安全环保标准。",
                    estimatedPriceText = "50 - 500 元",
                    rawResponse = "AI Rule Matching"
                )
            }
            combined.contains("纸") || combined.contains("箱") || combined.contains("书") -> {
                GeminiRecycleSuggestion(
                    categoryName = "废纸板/图书",
                    subCategoryName = "瓦楞纸与印刷品",
                    recyclingAdvice = "【AI智能识别】废纸板需压平捆扎，避免潮湿水浸。回收用于造纸厂重打纸浆，每回收1kg相当于保护0.01棵树木。",
                    estimatedPriceText = "1.2 - 1.8 元/kg",
                    rawResponse = "AI Rule Matching"
                )
            }
            else -> {
                GeminiRecycleSuggestion(
                    categoryName = "生活再生资源",
                    subCategoryName = "综合分类旧物",
                    recyclingAdvice = "【AI智能识别】建议清洗干净并进行分类压平打包，预约阳光专员上门过磅检测回收，投递可获得碳减排奖励积分。",
                    estimatedPriceText = "10 - 80 元",
                    rawResponse = "AI Rule Matching"
                )
            }
        }
    }
}
