package com.kdrama.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kdrama.backend.model.*;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiService(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate a simple response using Gemini
     */
    public String generateResponse(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * Search for dramas based on user criteria and return top 3 results
     * Returns a JSON array with drama titles and summaries
     */
    public ArrayNode searchDramasByPrompt(String prompt) {
        try {
            // Build a prompt to ask LLM for top 3 dramas with summaries
            String searchPrompt = prompt + "\n\n請列出 TOP 3 符合條件的韓劇。" +
                    "以 JSON 陣列格式回覆，包含：title（劇名），summary（20字左右簡介）。" +
                    "回覆格式：[{\"title\": \"劇名1\", \"summary\": \"簡介1\"}, {\"title\": \"劇名2\", \"summary\": \"簡介2\"}, {\"title\": \"劇名3\", \"summary\": \"簡介3\"}]" +
                    "只回覆 JSON 陣列，不需要其他文字。";

            String response = generateResponse(searchPrompt);

            // Try to parse the response as JSON
            ArrayNode results = parseAndExtractDramas(response);
            
            // If parsing fails or we get fewer than 3 results, try again with clearer instructions
            if (results.size() == 0) {
                results = fallbackParseDramas(response);
            }

            // Ensure we have at most 3 results
            ArrayNode limitedResults = objectMapper.createArrayNode();
            for (int i = 0; i < Math.min(results.size(), 3); i++) {
                limitedResults.add(results.get(i));
            }

            return limitedResults;

        } catch (Exception e) {
            System.err.println("Exception occurred during drama search: " + e.getMessage());
            e.printStackTrace();
            return objectMapper.createArrayNode();
        }
    }

    /**
     * Parse JSON response from LLM
     */
    private ArrayNode parseAndExtractDramas(String response) {
        try {
            // Find JSON array pattern
            Pattern pattern = Pattern.compile("\\[\\s*\\{.*?\\}\\s*\\]", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(response);

            if (matcher.find()) {
                String jsonStr = matcher.group();
                JsonNode parsed = objectMapper.readTree(jsonStr);
                if (parsed.isArray()) {
                    return (ArrayNode) parsed;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
        }
        return objectMapper.createArrayNode();
    }

    /**
     * Fallback parsing if JSON extraction fails
     * Extracts drama titles and summaries from natural language response
     */
    private ArrayNode fallbackParseDramas(String response) {
        ArrayNode results = objectMapper.createArrayNode();
        
        try {
            // Split by numbers (1., 2., 3.) to extract results
            String[] parts = response.split("(?=\\d\\.\\s)");
            
            int count = 0;
            for (int i = 1; i < parts.length && count < 3; i++) {
                String part = parts[i].trim();
                
                // Extract title (text after number, before first dash or longer description)
                Pattern titlePattern = Pattern.compile("^\\d\\.\\s*([^\\-—\n]+?)(?:\\s*[\\-—]|$)");
                Matcher titleMatcher = titlePattern.matcher(part);
                
                if (titleMatcher.find()) {
                    String title = titleMatcher.group(1).trim();
                    
                    // Extract summary (next ~20 chars after title)
                    String summary = part.substring(titleMatcher.end()).trim();
                    if (summary.length() > 50) {
                        summary = summary.substring(0, 50) + "...";
                    }
                    
                    ObjectNode drama = objectMapper.createObjectNode();
                    drama.put("title", title);
                    drama.put("summary", summary);
                    results.add(drama);
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("Fallback parsing failed: " + e.getMessage());
        }
        
        return results;
    }

    public Drama aiUpdateDramaInfo(Drama dramaToUpdate) {
        Drama dramaBefore = dramaToUpdate;
        try {
            // Build a prompt to ask LLM to update drama info
            String searchPrompt = "請搜尋並針對" + dramaToUpdate.getChineseName() + "這部韓劇更新內容：\n\n" +
                    "包含：chineseName（台灣官方劇名）、englishName（英文官方劇名，以播放平台為準）、koreanName（韓文官方劇名）、trailerUrl (預告片連結，有台灣中文版更好)、chineseWikipediaPageUrl (中文維基百科連結) 以及 namuWikiPageUrl (韓國Namu Wiki針對此韓劇的介紹網頁)。" + "\n\n 以 JSON 陣列格式回覆，包含上述欄位。" +
                    "\n\n回覆格式：{\"chineseName\": \"劇名\", \"englishName\": \"劇名\", \"koreanName\": \"劇名\", \"trailerUrl\": \"連結\", \"chineseWikipediaPageUrl\": \"連結\", \"namuWikiPageUrl\": \"連結\"}" +
                    "\n\n只回覆 JSON 物件，不需要其他文字。" +
                    "\n\n如果無法找到相關資訊，請將對應欄位設為空字串。";

            String response = generateResponse(searchPrompt);
            
            // Try to parse the response as JSON           
            JsonNode parsed = objectMapper.readTree(response).path(0);

            // Save the information of the parsed JsonNode to a .json file
            String backupFilePath = "backup/ai_update_backup.json";
            File backupFile = new File(backupFilePath);
            backupFile.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, parsed);

            String checkPrompt = "請以正確性衡量更新前還是更新後的資訊比較好：\n\n" +
                    "台灣官方劇名: " + "更新前 - \"" + dramaBefore.getChineseName() + "\" / 更新後 - \"" + parsed.path("chineseName").asText() + "\"\n" +
                    "英文官方劇名: " + "更新前 - \"" + dramaBefore.getEnglishName() + "\" / 更新後 - \"" + parsed.path("englishName").asText() + "\"\n" +
                    "韓文官方劇名: " + "更新前 - \"" + dramaBefore.getKoreanName() + "\" / 更新後 - \"" + parsed.path("koreanName").asText() + "\"\n\n" +
                    "預告片連結，有台灣中文版更好: " + "更新前 - \"" + dramaBefore.getTrailerUrl() + "\" / 更新後 - \"" + parsed.path("trailerUrl").asText() + "\"\n" +
                    "中文維基百科連結: " + "更新前 - \"" + dramaBefore.getChineseWikipediaPageUrl() + "\" / 更新後 - \"" + parsed.path("chineseWikipediaPageUrl").asText() + "\"\n" +
                    "韓國Namu Wiki連結: " + "更新前 - \"" + dramaBefore.getNamuWikiPageUrl() + "\" / 更新後 - \"" + parsed.path("namuWikiPageUrl").asText() + "\"\n\n" +
                    "請回傳出比較好的名稱。" +
                    "\n\n回覆格式：{\"chineseName\": \"劇名\", \"englishName\": \"劇名\", \"koreanName\": \"劇名\", \"trailerUrl\": \"連結\", \"chineseWikipediaPageUrl\": \"連結\", \"namuWikiPageUrl\": \"連結\"}" +
                    "\n\n只回覆 JSON 物件，不需要其他文字。";
            
            String checkResponse = generateResponse(checkPrompt);

            // Try to parse the check response as JSON
            JsonNode finalParsed = objectMapper.readTree(checkResponse);

            // Save the information of the parsed JsonNode to a .json file
            backupFilePath = "backup/ai_update_backup_2.json";
            backupFile = new File(backupFilePath);
            backupFile.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, finalParsed);

            // Update dramaToUpdate with finalParsed values if not empty
            if (finalParsed.has("chineseName") && !finalParsed.path("chineseName").asText().isEmpty()) {
                dramaToUpdate.setChineseName(finalParsed.path("chineseName").asText());
            }
            if (finalParsed.has("englishName") && !finalParsed.path("englishName").asText().isEmpty()) {
                dramaToUpdate.setEnglishName(finalParsed.path("englishName").asText());
            }
            if (finalParsed.has("koreanName") && !finalParsed.path("koreanName").asText().isEmpty()) {
                dramaToUpdate.setKoreanName(finalParsed.path("koreanName").asText());
            }
            if (finalParsed.has("trailerUrl") && !finalParsed.path("trailerUrl").asText().isEmpty()) {
                dramaToUpdate.setTrailerUrl(finalParsed.path("trailerUrl").asText());
            }
            if (finalParsed.has("chineseWikipediaPageUrl") && !finalParsed.path("chineseWikipediaPageUrl").asText().isEmpty()) {
                dramaToUpdate.setChineseWikipediaPageUrl(finalParsed.path("chineseWikipediaPageUrl").asText());
            }
            if (finalParsed.has("namuWikiPageUrl") && !finalParsed.path("namuWikiPageUrl").asText().isEmpty()) {
                dramaToUpdate.setNamuWikiPageUrl(finalParsed.path("namuWikiPageUrl").asText());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return dramaToUpdate;
    }
}

