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

    public AiService(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatClient = ChatClient.create(chatModel);
        this.objectMapper = objectMapper;
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
     * Search for actors based on user criteria and return top 3 results
     * Returns a JSON array with summaries and notable works
     */
    public ArrayNode aiSearchActorsByPrompt(String prompt) {
        try {
            // Build a prompt to ask LLM for top 3 actors with summaries and notable works
            String searchPrompt = prompt + "\n\n請列出 TOP 3 符合條件的韓國演員。" +
                    "以 JSON 陣列格式回覆，包含：name（台灣中文譯名），summary（30字以下簡介，包含該演員有出演的1~2部著名影視作品）。" +
                    "回覆格式：[{\"name\": \"演員名1\", \"summary\": \"簡介1\"}, {\"name\": \"演員名2\", \"summary\": \"簡介2\"}, {\"name\": \"演員名3\", \"summary\": \"簡介3\"}]" +
                    "只回覆 JSON 陣列，不需要其他文字。";

            String response = generateResponse(searchPrompt);

            // Try to parse the response as JSON
            ArrayNode results = parseAndExtractItems(response);
            
            // If parsing fails or we get fewer than 3 results, try again with clearer instructions
            if (results.size() == 0) {
                results = fallbackParseItems(response);
            }

            // Ensure we have at most 3 results
            ArrayNode limitedResults = objectMapper.createArrayNode();
            for (int i = 0; i < Math.min(results.size(), 3); i++) {
                limitedResults.add(results.get(i));
            }

            return limitedResults;

        } catch (Exception e) {
            System.err.println("Exception occurred during actor search: " + e.getMessage());
            e.printStackTrace();
            return objectMapper.createArrayNode();
        }
    }

    /**
     * Search for dramas based on user criteria and return top 3 results
     * Returns a JSON array with drama titles and summaries
     */
    public ArrayNode aiSearchDramasByPrompt(String prompt) {
        try {
            // Build a prompt to ask LLM for top 3 dramas with summaries
            String searchPrompt = prompt + "\n\n請列出 TOP 3 符合條件的韓劇。" +
                    "以 JSON 陣列格式回覆，包含：name（劇名），summary（20字左右簡介）。" +
                    "回覆格式：[{\"name\": \"劇名1\", \"summary\": \"簡介1\"}, {\"name\": \"劇名2\", \"summary\": \"簡介2\"}, {\"name\": \"劇名3\", \"summary\": \"簡介3\"}]" +
                    "只回覆 JSON 陣列，不需要其他文字。";

            String response = generateResponse(searchPrompt);

            // Try to parse the response as JSON
            ArrayNode results = parseAndExtractItems(response);
            
            // If parsing fails or we get fewer than 3 results, try again with clearer instructions
            if (results.size() == 0) {
                results = fallbackParseItems(response);
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

    public ArrayNode aiSearchMoviesByPrompt(String prompt) {
        try {
            // Build a prompt to ask LLM for top 3 movies with summaries
            String searchPrompt = prompt + "\n\n請列出 TOP 3 符合條件的韓國電影。" +
                    "以 JSON 陣列格式回覆，包含：name（劇名），summary（20字左右簡介）。" +
                    "回覆格式：[{\"name\": \"劇名1\", \"summary\": \"簡介1\"}, {\"name\": \"劇名2\", \"summary\": \"簡介2\"}, {\"name\": \"劇名3\", \"summary\": \"簡介3\"}]" +
                    "只回覆 JSON 陣列，不需要其他文字。";

            String response = generateResponse(searchPrompt);

            // Try to parse the response as JSON
            ArrayNode results = parseAndExtractItems(response);
            
            // If parsing fails or we get fewer than 3 results, try again with clearer instructions
            if (results.size() == 0) {
                results = fallbackParseItems(response);
            }

            // Ensure we have at most 3 results
            ArrayNode limitedResults = objectMapper.createArrayNode();
            for (int i = 0; i < Math.min(results.size(), 3); i++) {
                limitedResults.add(results.get(i));
            }

            return limitedResults;

        } catch (Exception e) {
            System.err.println("Exception occurred during movie search: " + e.getMessage());
            e.printStackTrace();
            return objectMapper.createArrayNode();
        }
    }

    /**
     * Parse JSON response from LLM
     */
    private ArrayNode parseAndExtractItems(String response) {
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
     * Extracts items (name and summary) from natural language response
     */
    private ArrayNode fallbackParseItems(String response) {
        ArrayNode results = objectMapper.createArrayNode();
        
        try {
            // Split by numbers (1., 2., 3.) to extract results
            String[] parts = response.split("(?=\\d\\.\\s)");
            
            int count = 0;
            for (int i = 1; i < parts.length && count < 3; i++) {
                String part = parts[i].trim();
                
                // Extract name (text after number, before first dash or longer description)
                Pattern namePattern = Pattern.compile("^\\d\\.\\s*([^\\-—\n]+?)(?:\\s*[\\-—]|$)");
                Matcher nameMatcher = namePattern.matcher(part);
                
                if (nameMatcher.find()) {
                    String name = nameMatcher.group(1).trim();
                    
                    // Extract summary (next ~20 chars after name)
                    String summary = part.substring(nameMatcher.end()).trim();
                    if (summary.length() > 50) {
                        summary = summary.substring(0, 50) + "...";
                    }
                    
                    ObjectNode item = objectMapper.createObjectNode();
                    item.put("name", name);
                    item.put("summary", summary);
                    results.add(item);
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("Fallback parsing failed: " + e.getMessage());
        }
        
        return results;
    }

    public Integer aiGetActorTmdbId(String chineseName) {
        try {
            // Build a prompt to ask LLM for TMDB ID
            String tmdbPrompt = "請問韓國演員\"" + chineseName + "\" 的 TMDB ID 是多少？" +
                    "\n\n只回覆數字，不需要其他文字。";

            String response = generateResponse(tmdbPrompt);

            // Try to extract number from response
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer aiGetDramaSeasonNumber(String dramaTitle) {
        try {
            // Build a prompt to ask LLM for number of seasons
            String seasonPrompt = "請問韓劇\"" + dramaTitle + "\"是第幾季 (注意，我問的不是有幾季，而是這個劇名是對應到整個系列的第幾季，如果這個系列就只有一季，那就是回傳1)？" +
                    "\n\n只回覆數字，不需要其他文字。";

            String response = generateResponse(seasonPrompt);

            // Try to extract number from response
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer aiGetDramaTmdbId(String chineseName) {
        try {
            // Build a prompt to ask LLM for TMDB ID
            String tmdbPrompt = "請問韓劇\"" + chineseName + "\" 的 TMDB ID 是多少？" +
                    "\n\n只回覆數字，不需要其他文字。";

            String response = generateResponse(tmdbPrompt);

            // Try to extract number from response
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer aiGetMovieTmdbId(String chineseName) {
        try {
            // Build a prompt to ask LLM for TMDB ID
            String tmdbPrompt = "請問韓國電影\"" + chineseName + "\" 的 TMDB ID 是多少？" +
                    "\n\n只回覆數字，不需要其他文字。";

            String response = generateResponse(tmdbPrompt);

            // Try to extract number from response
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Actor aiUpdateActorInfo(Actor actorToUpdate) {
        Actor actorBefore = actorToUpdate;
        try {
            // Build a prompt to ask LLM to update actor info
            String searchPrompt = "請搜尋並針對" + actorToUpdate.getChineseName() + "這位韓國演員更新內容：\n\n" +
                    "包含：biography（請為該演員撰寫最多200字的簡介）、instagramPageUrl（該演員的官方Instagram帳號）、chineseWikipediaPageUrl (中文維基百科連結) 以及 namuWikiPageUrl (韓國Namu Wiki針對此演員的介紹網頁)。" + "\n\n 以 JSON 陣列格式回覆，包含上述欄位。" +
                    "\n\n回覆格式：{\"biography\": \"簡介\", \"instagramPageUrl\": \"連結\", \"chineseWikipediaPageUrl\": \"連結\", \"namuWikiPageUrl\": \"連結\"}" +
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
                    "演員簡介: " + "更新前 - \"" + (actorBefore.getBiography() != null ? actorBefore.getBiography() : "") + "\" / 更新後 - \"" + parsed.path("biography").asText() + "\"\n" +
                    "Instagram連結: " + "更新前 - \"" + (actorBefore.getInstagramPageUrl() != null ? actorBefore.getInstagramPageUrl() : "") + "\" / 更新後 - \"" + parsed.path("instagramPageUrl").asText() + "\"\n" +
                    "中文維基百科連結: " + "更新前 - \"" + (actorBefore.getChineseWikipediaPageUrl() != null ? actorBefore.getChineseWikipediaPageUrl() : "") + "\" / 更新後 - \"" + parsed.path("chineseWikipediaPageUrl").asText() + "\"\n" +
                    "韓國Namu Wiki連結: " + "更新前 - \"" + (actorBefore.getNamuWikiPageUrl() != null ? actorBefore.getNamuWikiPageUrl() : "") + "\" / 更新後 - \"" + parsed.path("namuWikiPageUrl").asText() + "\"\n\n" +
                    "請回傳出比較好的名稱。" +
                    "\n\n回覆格式：{\"biography\": \"簡介\", \"instagramPageUrl\": \"連結\", \"chineseWikipediaPageUrl\": \"連結\", \"namuWikiPageUrl\": \"連結\"}" +
                    "\n\n只回覆 JSON 物件，不需要其他文字。";
            
            String checkResponse = generateResponse(checkPrompt);

            // Try to parse the check response as JSON
            JsonNode finalParsed = objectMapper.readTree(checkResponse);

            // Save the information of the parsed JsonNode to a .json file
            backupFilePath = "backup/ai_update_backup.json";
            backupFile = new File(backupFilePath);
            backupFile.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, finalParsed);

            // Update dramaToUpdate with finalParsed values if not empty
            if (finalParsed.has("biography") && !finalParsed.path("biography").asText().isEmpty()) {
                actorToUpdate.setBiography(finalParsed.path("biography").asText());
            }
            if (finalParsed.has("instagramPageUrl") && !finalParsed.path("instagramPageUrl").asText().isEmpty()) {
                actorToUpdate.setInstagramPageUrl(finalParsed.path("instagramPageUrl").asText());
            }
            if (finalParsed.has("chineseWikipediaPageUrl") && !finalParsed.path("chineseWikipediaPageUrl").asText().isEmpty()) {
                actorToUpdate.setChineseWikipediaPageUrl(finalParsed.path("chineseWikipediaPageUrl").asText());
            }
            if (finalParsed.has("namuWikiPageUrl") && !finalParsed.path("namuWikiPageUrl").asText().isEmpty()) {
                actorToUpdate.setNamuWikiPageUrl(finalParsed.path("namuWikiPageUrl").asText());
            }

            // Save the information of the parsed JsonNode to a .json file
            backupFilePath = "backup/actor_backup.json";
            backupFile = new File(backupFilePath);
            backupFile.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, actorToUpdate);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return actorToUpdate;
    }

    public Drama aiUpdateDramaInfo(Drama dramaToUpdate) {
        Drama dramaBefore = dramaToUpdate;
        try {
            // Build a prompt to ask LLM to update drama info
            String searchPrompt = "請搜尋並針對" + (dramaToUpdate.getChineseName() != null ? dramaToUpdate.getChineseName() : "TMDB ID是" + dramaToUpdate.getTmdbId() + "且為第" + dramaToUpdate.getSeasonNumber() + "季") + "這部韓劇更新內容：\n\n" +
                    "包含：chineseName（台灣官方劇名）、englishName（英文官方劇名，以播放平台為準）、koreanName（韓文官方劇名）、trailerUrl (預告片連結，請利用 \" YouTube網址 + /results?search_query={台灣中文官方片名}+預告片 \" 來生成搜尋連結)、chineseWikipediaPageUrl (中文維基百科連結) 以及 namuWikiPageUrl (韓國Namu Wiki針對此韓劇的介紹網頁)。" + "\n\n 以 JSON 陣列格式回覆，包含上述欄位。" +
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
                    "台灣官方劇名: " + "更新前 - \"" + (dramaBefore.getChineseName() != null ? dramaBefore.getChineseName() : "") + "\" / 更新後 - \"" + parsed.path("chineseName").asText() + "\"\n" +
                    "英文官方劇名: " + "更新前 - \"" + (dramaBefore.getEnglishName() != null ? dramaBefore.getEnglishName() : "") + "\" / 更新後 - \"" + parsed.path("englishName").asText() + "\"\n" +
                    "韓文官方劇名: " + "更新前 - \"" + (dramaBefore.getKoreanName() != null ? dramaBefore.getKoreanName() : "") + "\" / 更新後 - \"" + parsed.path("koreanName").asText() + "\"\n\n" +
                    "預告片連結: " + "更新前 - \"" + (dramaBefore.getTrailerUrl() != null ? dramaBefore.getTrailerUrl() : "") + "\" / 更新後 - \"" + parsed.path("trailerUrl").asText() + "\"\n" +
                    "中文維基百科連結: " + "更新前 - \"" + (dramaBefore.getChineseWikipediaPageUrl() != null ? dramaBefore.getChineseWikipediaPageUrl() : "")  + "\" / 更新後 - \"" + parsed.path("chineseWikipediaPageUrl").asText()  + "\"\n" +
                    "韓國Namu Wiki連結: " + "更新前 - \""  + (dramaBefore.getNamuWikiPageUrl() != null ? dramaBefore.getNamuWikiPageUrl() : "")  +"\" / 更新後 - \""  + parsed.path("namuWikiPageUrl").asText()  +"\"\n\n" +
                    "請回傳出比較好的名稱。" +
                    "\n\n回覆格式：{\"chineseName\": \"劇名\", \"englishName\": \"劇名\", \"koreanName\": \"劇名\", \"trailerUrl\": \"連結\", \"chineseWikipediaPageUrl\": \"連結\", \"namuWikiPageUrl\": \"連結\"}" +
                    "\n\n只回覆 JSON 物件，不需要其他文字。";
            
            String checkResponse = generateResponse(checkPrompt);

            // Try to parse the check response as JSON
            JsonNode finalParsed = objectMapper.readTree(checkResponse);

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

            // Save the information of the parsed JsonNode to a .json file
            backupFilePath = "backup/drama_backup.json";
            backupFile = new File(backupFilePath);
            backupFile.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, dramaToUpdate);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return dramaToUpdate;
    }

    public Movie aiUpdateMovieInfo(Movie movieToUpdate) {
        Movie movieBefore = movieToUpdate;
        try {
            // Build a prompt to ask LLM to update movie info
            String searchPrompt = "請搜尋並針對" + (movieToUpdate.getChineseName() != null ? movieToUpdate.getChineseName() : "TMDB ID是" + movieToUpdate.getTmdbId()) + "這部韓國電影更新內容：\n\n" +
                    "包含：chineseName（台灣官方片名）、englishName（英文官方片名，以播放平台為準）、koreanName（韓文官方片名）、trailerUrl (預告片連結，請利用 \" YouTube網址 + /results?search_query={台灣中文官方片名}+預告片 \" 來生成搜尋連結)、chineseWikipediaPageUrl (中文維基百科連結) 以及 namuWikiPageUrl (韓國Namu Wiki針對此韓國電影的介紹網頁)。" + "\n\n 以 JSON 陣列格式回覆，包含上述欄位。" +
                    "\n\n回覆格式：{\"chineseName\": \"片名\", \"englishName\": \"片名\", \"koreanName\": \"片名\", \"trailerUrl\": \"連結\", \"chineseWikipediaPageUrl\": \"連結\", \"namuWikiPageUrl\": \"連結\"}" +
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
                    "台灣官方片名: " + "更新前 - \"" + (movieBefore.getChineseName() != null ? movieBefore.getChineseName() : "") + "\" / 更新後 - \"" + parsed.path("chineseName").asText() + "\"\n" +
                    "英文官方片名: " + "更新前 - \"" + (movieBefore.getEnglishName() != null ? movieBefore.getEnglishName() : "") + "\" / 更新後 - \"" + parsed.path("englishName").asText() + "\"\n" +
                    "韓文官方片名: " + "更新前 - \"" + (movieBefore.getKoreanName() != null ? movieBefore.getKoreanName() : "") + "\" / 更新後 - \"" + parsed.path("koreanName").asText() + "\"\n\n" +
                    "預告片連結: " + "更新前 - \"" + (movieBefore.getTrailerUrl() != null ? movieBefore.getTrailerUrl() : "")  + "\" / 更新後 - \"" + parsed.path("trailerUrl").asText()  + "\"\n" +
                    "中文維基百科連結: " + "更新前 - \"" + (movieBefore.getChineseWikipediaPageUrl() != null ? movieBefore.getChineseWikipediaPageUrl() : "")  + "\" / 更新後 - \"" + parsed.path("chineseWikipediaPageUrl").asText()  + "\"\n" +
                    "韓國Namu Wiki連結: " + "更新前 - \""  + (movieBefore.getNamuWikiPageUrl() != null ? movieBefore.getNamuWikiPageUrl() : "")  +"\" / 更新後 - \""  + parsed.path("namuWikiPageUrl").asText()  +"\"\n\n" +
                    "請回傳出比較好的名稱。" +
                    "\n\n回覆格式：{\"chineseName\": \"片名\", \"englishName\": \"片名\", \"koreanName\": \"片名\", \"trailerUrl\": \"連結\", \"chineseWikipediaPageUrl\": \"連結\", \"namuWikiPageUrl\": \"連結\"}" +
                    "\n\n只回覆 JSON 物件，不需要其他文字。";
            
            String checkResponse = generateResponse(checkPrompt);

            // Try to parse the check response as JSON
            JsonNode finalParsed = objectMapper.readTree(checkResponse);

            // Update movieToUpdate with finalParsed values if not empty
            if (finalParsed.has("chineseName") && !finalParsed.path("chineseName").asText().isEmpty()) {
                movieToUpdate.setChineseName(finalParsed.path("chineseName").asText());
            }
            if (finalParsed.has("englishName") && !finalParsed.path("englishName").asText().isEmpty()) {
                movieToUpdate.setEnglishName(finalParsed.path("englishName").asText());
            }
            if (finalParsed.has("koreanName") && !finalParsed.path("koreanName").asText().isEmpty()) {
                movieToUpdate.setKoreanName(finalParsed.path("koreanName").asText());
            }
            if (finalParsed.has("trailerUrl") && !finalParsed.path("trailerUrl").asText().isEmpty()) {
                movieToUpdate.setTrailerUrl(finalParsed.path("trailerUrl").asText());
            }
            if (finalParsed.has("chineseWikipediaPageUrl") && !finalParsed.path("chineseWikipediaPageUrl").asText().isEmpty()) {
                movieToUpdate.setChineseWikipediaPageUrl(finalParsed.path("chineseWikipediaPageUrl").asText());
            }
            if (finalParsed.has("namuWikiPageUrl") && !finalParsed.path("namuWikiPageUrl").asText().isEmpty()) {
                movieToUpdate.setNamuWikiPageUrl(finalParsed.path("namuWikiPageUrl").asText());
            }

            // Save the information of the parsed JsonNode to a .json file
            backupFilePath = "backup/movie_backup.json";
            backupFile = new File(backupFilePath);
            backupFile.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, movieToUpdate);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return movieToUpdate;
    }
}

